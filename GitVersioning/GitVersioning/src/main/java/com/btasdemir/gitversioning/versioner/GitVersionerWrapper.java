package com.btasdemir.gitversioning.versioner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.btasdemir.gitversioning.model.Version;

/**
 * Wrapper class of the GIT versioner library
 * 
 * @author bahadir.tasdemir@hotmail.com.tr
 */
public class GitVersionerWrapper {

	protected GitVersioner gitVersioner;
	protected static final Logger logger = LogManager.getLogger(GitVersionerWrapper.class);

	public GitVersionerWrapper(String localPath, String remotePath, String userName, String password,
			boolean cloneIfRepoDoesNotExist) {
		this.gitVersioner = new GitVersioner(localPath, remotePath, userName, password, cloneIfRepoDoesNotExist);
		try {
			this.gitVersioner.init();
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (GitAPIException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Add file to the GIT version system File must be inside the local GIT
	 * repository (inside local GIT base path)
	 * 
	 * @param fileName
	 *            File name to be added
	 */
	public void addFile(String fileName) {
		try {
			this.gitVersioner.add(fileName);
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (GitAPIException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Remove file from the GIT version system
	 * 
	 * @param fileName
	 *            Name of the file to be removed
	 * @param onlyRemoveFromIndex
	 *            Set true if the file is demanded to remove from only indexing.
	 *            Set false if you want to also delete the file
	 */
	public void removeFile(String fileName, boolean onlyRemoveFromIndex) {
		try {
			this.gitVersioner.remove(fileName, onlyRemoveFromIndex);
		} catch (NoFilepatternException e) {
			logger.error(e.getMessage());
		} catch (GitAPIException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Commit the all changes that are made
	 * 
	 * @param message
	 *            Message to set with this commit
	 */
	public void commitChanges(String message) {
		try {
			this.gitVersioner.commit(message);
		} catch (GitAPIException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Push all commits to remote
	 */
	public void pushCommits() {
		try {
			this.gitVersioner.push();
		} catch (GitAPIException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Completely version the current file: +Add to index +Commit to local +Push
	 * to remote
	 * 
	 * @param fileName
	 *            File name to version
	 * @param commitMessage
	 *            Message to put while committing the file
	 * @return True if the operation is successful
	 */
	public boolean versionFile(String fileName, String commitMessage) {
		this.addFile(fileName);
		if (this.gitVersioner.cloneIfRepoDoesNotExist) {
			this.commitAndPushChanges(commitMessage);
		} else {
			this.commitChanges(commitMessage);
		}
		return true;
	}

	/**
	 * Alternative of commitChanges and pushCommits
	 * 
	 * @param message
	 *            Message to set with this commit
	 */
	public void commitAndPushChanges(String message) {
		try {
			this.gitVersioner.commitAndPush(message);
		} catch (GitAPIException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Pull changes if any from remote repository
	 */
	public void pullRemoteChanges() {
		try {
			this.gitVersioner.pull();
		} catch (GitAPIException e) {
			logger.error(e.getMessage());
		}
	}

	/**
	 * Get local path of the current GIT system
	 * 
	 * @return Local path of the current GIT system
	 */
	public String getLocalPath() {
		return this.gitVersioner.getLocalPath();
	}

	/**
	 * Get versions of a file by name
	 * 
	 * @param fileName
	 *            Name of the demanded file
	 * @return A list that contains [id - commiter_name - date]
	 */
	public List<Version> getVersionsOfFile(String fileName) {
		try {
			return this.gitVersioner.listVersions(fileName);
		} catch (NoHeadException e) {
			logger.error(e.getMessage());
		} catch (GitAPIException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * Get an older revision of a file Also saves the revision to the folder
	 * named same with the demanded file
	 * 
	 * @param revId
	 *            Id of the demanded revision, can be get by getVersionsOfFile
	 * @param fileName
	 *            Name of the demanded file
	 * @return File got from the revision
	 */
	public File getRevisionOfFileByRevId(String revId, String fileName) {
		try {
			return this.gitVersioner.getRevisionFileById(revId, fileName);
		} catch (IOException e) {
			logger.error("This version may refer to a deletion: " + e.getMessage());
		}
		return null;
	}

	/**
	 * Call when the object is destroyed
	 */
	public void finish() {
		this.gitVersioner.close();
	}

}
