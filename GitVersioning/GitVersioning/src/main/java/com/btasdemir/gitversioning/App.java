package com.btasdemir.gitversioning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import com.btasdemir.gitversioning.versioner.GitVersionerWrapper;
import com.btasdemir.gitversioning.constant.GITValueConstants;
import com.btasdemir.gitversioning.model.Version;

/**
 * Main usage of the GIT versioner library
 * 
 * @author bahadir.tasdemir@hotmail.com.tr
 */
public class App {

	public static void main(String[] args) {

		/*
		 * Initialize the GIT versioner wrapper
		 */
		System.out.println("Git versioner is initializing");
		GitVersionerWrapper gitVersionerWrapper = new GitVersionerWrapper(GITValueConstants.LOCAL_REPO_PATH,
				GITValueConstants.REMOTE_REPO_PATH, GITValueConstants.REMOTE_REPO_USER_NAME,
				GITValueConstants.REMOTE_REPO_PASSWORD, false);

		/*
		 * Create a file to commit to the version system
		 */
		System.out.println("A file is creating to commit to the system");
		try {
			FileWriter fileWriter = new FileWriter(
					new File(GITValueConstants.LOCAL_REPO_PATH + "/" + GITValueConstants.FILE_NAME), true);
			fileWriter.write("First version to commit file\n");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * Version the first version of the file
		 */
		System.out.println("The file is being versioned");
		gitVersionerWrapper.versionFile(GITValueConstants.FILE_NAME, "First commit for the new file");

		/*
		 * Append to the file and commit again for versioning
		 */
		System.out.println("The file is changing for the next version");
		try {
			Files.write(Paths.get(GITValueConstants.LOCAL_REPO_PATH + "/" + GITValueConstants.FILE_NAME),
					"Second version to commit file\n".getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}

		/*
		 * Version the second version of the file
		 */
		System.out.println("New version of the file is entering to the system");
		gitVersionerWrapper.versionFile(GITValueConstants.FILE_NAME, "Second commit for the new file");

		/*
		 * Get versions of the file
		 */
		System.out.println("Getting versions of the file");
		List<Version> versions = gitVersionerWrapper.getVersionsOfFile(GITValueConstants.FILE_NAME);
		if (versions != null) {
			for (Version version : versions) {
				System.out.println(
						version.getCommiterName() + " - " + version.getCommitId() + " - " + version.getCommitDate());
			}
			/*
			 * Open the old version of the file
			 */
			System.out.println("Opening the versions of the file from newest to oldest");
			File file;
			BufferedReader bufferedReader;
			String line;
			for (Version version : versions) {
				file = gitVersionerWrapper.getRevisionOfFileByRevId(version.getCommitId(), GITValueConstants.FILE_NAME);
				try {
					System.out.println("\n--Start to write file:");
					bufferedReader = new BufferedReader(new FileReader(file));
					while ((line = bufferedReader.readLine()) != null) {
						System.out.println(line);
					}
					bufferedReader.close();
					System.out.println("--Finish to write file\n");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		/*
		 * Close the resources of the GIT versioner
		 */
		System.out.println("Closing the resources");
		gitVersionerWrapper.finish();
		System.out.println("Finished");

	}
}
