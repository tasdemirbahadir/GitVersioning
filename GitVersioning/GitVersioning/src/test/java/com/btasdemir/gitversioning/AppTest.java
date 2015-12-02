package com.btasdemir.gitversioning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.btasdemir.gitversioning.versioner.GitVersionerWrapper;
import com.btasdemir.gitversioning.constant.GITValueConstants;
import com.btasdemir.gitversioning.model.Version;

/**
 * JUnit test for GIT versioner library
 * 
 * @author bahadir.tasdemir@hotmail.com.tr
 */
public class AppTest {

	@Test
	public void testGitVersioner() {
		// ------------------------INIT GIT------------------------
		GitVersionerWrapper gitVersionerWrapper = new GitVersionerWrapper(GITValueConstants.LOCAL_REPO_PATH,
				GITValueConstants.REMOTE_REPO_PATH, GITValueConstants.REMOTE_REPO_USER_NAME,
				GITValueConstants.REMOTE_REPO_PASSWORD, false);
		Assert.assertNotEquals("Initiated the git versioner wrapper", gitVersionerWrapper, null);
		// ------------------------VERSION FILE------------------------
		try {
			FileWriter fileWriter = new FileWriter(
					new File(GITValueConstants.LOCAL_REPO_PATH + "/" + GITValueConstants.FILE_NAME), true);
			fileWriter.write("First version to commit file\n");
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		boolean isFileVersioned = gitVersionerWrapper.versionFile(GITValueConstants.FILE_NAME,
				"First commit for the new file");
		Assert.assertEquals("A file is versioned", isFileVersioned, true);
		// ------------------------REVERSION FILE------------------------
		try {
			Files.write(Paths.get(GITValueConstants.LOCAL_REPO_PATH + "/" + GITValueConstants.FILE_NAME),
					"Second version to commit file\n".getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			e.printStackTrace();
		}
		isFileVersioned = gitVersionerWrapper.versionFile(GITValueConstants.FILE_NAME,
				"Second commit for the new file");
		Assert.assertEquals("Same file is reversioned", isFileVersioned, true);
		// ------------------------GET VERSIONS------------------------
		List<Version> versions = gitVersionerWrapper.getVersionsOfFile(GITValueConstants.FILE_NAME);
		Assert.assertEquals("Versiones of the file are fetched", versions.size() > 0, true);
		Assert.assertNotEquals("Last revision must not be null", versions.get(0), null);
		// ------------------------CLOSE RESOURCES------------------------
		gitVersionerWrapper.finish();

	}
}
