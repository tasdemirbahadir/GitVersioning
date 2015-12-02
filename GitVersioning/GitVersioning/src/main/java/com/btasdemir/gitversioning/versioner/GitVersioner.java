package com.btasdemir.gitversioning.versioner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.RmCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import com.btasdemir.gitversioning.constant.CommonConstants;
import com.btasdemir.gitversioning.constant.MessageConstants;
import com.btasdemir.gitversioning.model.Version;
import com.btasdemir.gitversioning.util.CheckValueUtil;

/**
 * Main GIT versioner class that applies JGIT library
 * 
 * @author bahadir.tasdemir@hotmail.com.tr
 */
public class GitVersioner {

	private String localPath;
	protected String remotePath;
	protected String userName;
	protected String password;
	protected Git git;
	protected Repository localRepo;
	protected boolean cloneIfRepoDoesNotExist;
	protected static final Logger logger = LogManager.getLogger(GitVersioner.class);

	public GitVersioner(String localPath, String remotePath, String userName, String password,
			boolean cloneIfRepoDoesNotExist) {
		CheckValueUtil.checkStringVals(localPath);
		this.userName = userName;
		this.password = password;
		this.setLocalPath(localPath);
		this.remotePath = remotePath;
		this.cloneIfRepoDoesNotExist = cloneIfRepoDoesNotExist;
	}

	/**
	 * Initialize the GIT versioner
	 * 
	 * @throws IOException
	 *             Throws if an IO exception occurs
	 * @throws GitAPIException
	 *             Throws if a GIT API exception occurs
	 */
	public void init() throws IOException, GitAPIException {
		// Check GIT variable if already initialized
		if (git == null) {
			if (localRepo == null) {
				// Check if local existing repo is valid
				if (!isValidLocalRepository(getLocalPath() + CommonConstants.GIT_EXTENSION)) {
					logger.warn(MessageConstants.WARNING_INVALID_LOCAL_REPO);
					if (cloneIfRepoDoesNotExist) {
						// Clone from remote repository
						this.cloneRepo();
						return;
					} else {
						// Create on local repository
						this.createRepo();
					}
				} else {
					logger.info(MessageConstants.INFO_LOCAL_REPO_VALID);
				}
			} else {
				logger.info(MessageConstants.INFO_LOCAL_VAR_REPO_EXISTS);
			}
			if (cloneIfRepoDoesNotExist) {
				localRepo = setOriginToRepo(localRepo);
			}
			git = new Git(localRepo);
			if (cloneIfRepoDoesNotExist) {
				this.pull();
			}
		} else {
			logger.info(MessageConstants.INFO_GIT_ALREADY_INITIALIZED);
		}
	}

	/**
	 * Create the repository on local
	 * 
	 * @throws IOException
	 *             Throws if any IO exception occurs
	 */
	public void createRepo() throws IOException {
		localRepo = new FileRepository(getLocalPath() + File.separator + ".git");
		localRepo.create();
		logger.info("Local repository is created.");
	}

	/**
	 * Clone repository from a remote one
	 * 
	 * @throws IOException
	 *             Throws if any IO exception occurs
	 * @throws GitAPIException
	 *             Throws if any GIT API exception occurs
	 */
	public void cloneRepo() throws IOException, GitAPIException {
		git = Git.cloneRepository().setURI(remotePath)
				.setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.userName, this.password))
				.setDirectory(new File(localPath)).call();
		// Initiate local repository if null
		if (localRepo == null) {
			localRepo = new FileRepository(getLocalPath() + CommonConstants.GIT_EXTENSION);
		}
		// Set origin value to repository
		localRepo = setOriginToRepo(localRepo);
	}

	/**
	 * Add file to the versioning
	 * 
	 * @param fileName
	 *            File to be add to the versioning
	 * @throws IOException
	 *             Throws if any IO exception occurs
	 * @throws GitAPIException
	 *             Throws if any GIT API exception occurs
	 */
	public void add(String fileName) throws IOException, GitAPIException {
		this.checkGit();
		// Open given file and add the the index
		// The file must be inside of the GIT repository folder
		File myfile = new File(getLocalPath() + File.separator + fileName);
		myfile.createNewFile();
		git.add().addFilepattern(fileName).call();
		logger.info(MessageFormat.format(MessageConstants.INFO_FILE_ADD, fileName));
	}

	/**
	 * Remove file from index and/or from disc
	 * 
	 * @param file
	 *            Complete file path to remove
	 * @param onlyRemoveFromIndex
	 *            Set true if the file must be removed from only index. The file
	 *            is removed from the local disc too when set false.
	 * @throws NoFilepatternException
	 *             Throws when a NoFilepatternException occurs.
	 * @throws GitAPIException
	 *             Throws when a NoFilepatternException occurs.
	 */
	public void remove(String file, boolean onlyRemoveFromIndex) throws NoFilepatternException, GitAPIException {
		this.checkGit();
		RmCommand rmCommand = git.rm();
		rmCommand.setCached(onlyRemoveFromIndex);
		rmCommand.addFilepattern(file);
		rmCommand.call();
		logger.info(MessageFormat.format(MessageConstants.INFO_REMOVED_FROM_INDEX, file,
				String.valueOf(onlyRemoveFromIndex)));
	}

	/**
	 * Commit changes in the repository
	 * 
	 * @param message
	 *            Message to be put while committing
	 * @throws GitAPIException
	 *             Throws if any GIT API exception occurs
	 */
	public void commit(String message) throws GitAPIException {
		CheckValueUtil.checkStringVal(message);
		this.checkGit();
		git.commit().setMessage(message).call();
		logger.info(MessageFormat.format(MessageConstants.INFO_COMMIT, message));
	}

	/**
	 * Push local repository changes to remote
	 * 
	 * @throws GitAPIException
	 *             Throws if any GIT API exception occurs
	 */
	public void push() throws GitAPIException {
		this.checkGit();
		PushCommand pushCommand = git.push();
		pushCommand.setRemote(remotePath);
		pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.userName, this.password));
		pushCommand.call();
		logger.info(MessageConstants.INFO_PUSH);
	}

	/**
	 * Commit and then push changes to remote
	 * 
	 * @param message
	 *            Message to be put while committing
	 * @throws GitAPIException
	 *             Throws if any GIT API exception occurs
	 */
	public void commitAndPush(String message) throws GitAPIException {
		CheckValueUtil.checkStringVal(message);
		this.checkGit();
		this.commit(message);
		this.push();
		logger.info(MessageConstants.INFO_COMMIT_AND_PUSH);
	}

	/**
	 * Pull latest version from the remote
	 * 
	 * @throws GitAPIException
	 *             Throws if any GIT API exception occurs
	 */
	public void pull() throws GitAPIException {
		this.checkGit();
		PullCommand pullCommand = git.pull();
		pullCommand.setRemote("origin");
		pullCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(this.userName, this.password));
		pullCommand.setStrategy(MergeStrategy.SIMPLE_TWO_WAY_IN_CORE);
		pullCommand.call();
		logger.info(MessageConstants.INFO_PULL);
	}

	/**
	 * Get current local path of the local repository
	 * 
	 * @return Current local repository path
	 */
	public String getLocalPath() {
		return localPath;
	}

	/**
	 * Set path of the local repository
	 * 
	 * @param localPath
	 *            Path of the local repository
	 */
	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	/**
	 * Close the GIT and local repository
	 */
	public void close() {
		if (git != null) {
			git.close();
		}
		if (localRepo != null) {
			localRepo.close();
		}
	}

	/**
	 * List versions of the given file path
	 * 
	 * @param file
	 *            File path that needed versions belong to
	 * @return Commit versions of the given file path
	 * @throws NoHeadException
	 *             Throws when a No Head exception occurs.
	 * @throws GitAPIException
	 *             Throws when a GIT API exception occurs.
	 * @throws IOException
	 *             Throws when an IO Head exception occurs.
	 */
	public List<Version> listVersions(String file) throws NoHeadException, GitAPIException, IOException {
		this.checkGit();
		// Collect commits into a list
		List<Version> commits = new ArrayList<Version>();
		// Create revision walk parameter
		RevWalk revWalk = new RevWalk(localRepo);
		// Get any difference commit
		revWalk.setTreeFilter(AndTreeFilter.create(PathFilterGroup.createFromStrings(file), TreeFilter.ANY_DIFF));
		// Fetch beginning from the head
		RevCommit rootCommit = revWalk.parseCommit(localRepo.resolve(CommonConstants.GIT_TERM_HEAD));
		// Sort from newest to oldest
		revWalk.sort(RevSort.COMMIT_TIME_DESC);
		revWalk.markStart(rootCommit);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CommonConstants.DATE_TIME_PATTERN);
		// Collect commits
		for (RevCommit revCommit : revWalk) {
			commits.add(new Version(revCommit.getAuthorIdent().getName(),
					simpleDateFormat.format(new Date(revCommit.getCommitTime() * 1000L)),
					String.valueOf(revCommit.getId().getName())));
		}
		revWalk.close();
		// Return commits as versions
		return commits;
	}

	/**
	 * Get an old revision of a file by it's revision Id
	 * 
	 * @param revId
	 *            Id of the revision that the file must belong
	 * @param fileName
	 *            Name of the file that is going to be re-visioned
	 * @return File that belongs to given revision
	 * @throws IOException
	 *             Throws if an IO exception occurs
	 */
	public File getRevisionFileById(String revId, String fileName) throws IOException {
		// Get a revision file by id
		// Get object id by revision id
		ObjectId lastCommitId = localRepo.resolve(revId);
		// Create revision walk parameter
		RevWalk revWalk = new RevWalk(localRepo);
		// get last commit
		RevCommit commit = revWalk.parseCommit(lastCommitId);
		// Create tree
		RevTree tree = commit.getTree();
		// Create tree walk parameter
		TreeWalk treeWalk = new TreeWalk(localRepo);
		// Add tree to tree walk parameter
		treeWalk.addTree(tree);
		treeWalk.setRecursive(true);
		treeWalk.setFilter(PathFilter.create(fileName));
		// Check if last commit is found
		if (!treeWalk.next()) {
			treeWalk.close();
			revWalk.close();
			logger.error(MessageFormat.format(MessageConstants.ERROR_REVISION_NOT_FOUND, revId, fileName));
			return null;
		}
		// Create inputStrem from the needed revision to read file
		ObjectId objectId = treeWalk.getObjectId(0);
		ObjectLoader loader = localRepo.open(objectId);
		InputStream in = loader.openStream();
		treeWalk.close();
		revWalk.close();
		// Create a folder to insert into revisions
		int i = fileName.lastIndexOf(CommonConstants.DOT);
		String fileExtension = fileName.substring(i);
		// Remove extension from file name
		fileName = fileName.replaceFirst(CommonConstants.REGEX_FILE_EXTENSION, StringUtils.EMPTY);
		File file = new File(getLocalPath() + File.separator + fileName + File.separator + revId + fileExtension);
		file.getParentFile().mkdirs();
		// Create path if does not exist
		FileWriter writer = new FileWriter(file);
		writer.close();
		// Create file
		FileOutputStream oFile;
		file.createNewFile();
		oFile = new FileOutputStream(file, false);
		int cByte;
		while ((cByte = in.read()) != -1) {
			oFile.write(cByte);
		}
		oFile.close();
		// Return file
		return file;
	}

	/**
	 * Check if GIT is null, and initialize
	 */
	protected void checkGit() {
		// Check if GIT variable is null and initialize if needed
		if (git == null) {
			try {
				init();
			} catch (IOException e) {
				logger.error(e.getMessage());
			} catch (GitAPIException e) {
				logger.error(e.getMessage());
			}
		}
	}

	/**
	 * Check if repository at the given uri is valid
	 * 
	 * @param uri
	 *            URI of the local repository
	 * @return True if a valid repository
	 */
	protected boolean isValidLocalRepository(String uri) {
		boolean result;
		try {
			// Try to create local repository
			localRepo = new FileRepository(uri);
			result = localRepo.getObjectDatabase().exists();
		} catch (IOException e) {
			// Local repository is invalid
			logger.error(e.getMessage());
			result = false;
		}
		return result;
	}

	/**
	 * Set alias to the origin as URL of the remote
	 * 
	 * @param repo
	 *            Current repository
	 * @return Repository after setting URL (origin is an alias to the URL)
	 */
	protected Repository setOriginToRepo(Repository repo) {
		// Set origin value to the repository.
		// Otherwise gives an error as "origin value could not be found"
		StoredConfig config = repo.getConfig();
		if (config.getString(CommonConstants.GIT_TERM_REMOTE, CommonConstants.GIT_TERM_ORIGIN,
				CommonConstants.TERM_URL) == null) {
			config.setString(CommonConstants.GIT_TERM_REMOTE, CommonConstants.GIT_TERM_ORIGIN, CommonConstants.TERM_URL,
					remotePath);
			try {
				config.save();
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
		return repo;
	}

}
