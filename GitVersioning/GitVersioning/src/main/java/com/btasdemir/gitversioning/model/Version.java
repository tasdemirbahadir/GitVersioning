package com.btasdemir.gitversioning.model;

/**
 * Version model to get versions of a file inside GIT system.
 * 
 * The attributes can be changed according to the needs.
 * 
 * @author bahadir.tasdemir@hotmail.com.tr
 */
public class Version {

	protected String commiterName;
	protected String commitDate;
	protected String commitId;

	public Version() {
	}

	public Version(String commiterName, String commitDate, String commitId) {
		this.commitDate = commitDate;
		this.commiterName = commiterName;
		this.commitId = commitId;
	}

	public String getCommiterName() {
		return this.commiterName;
	}

	public void setCommiterName(String commiterName) {
		this.commiterName = commiterName;
	}

	public String getCommitDate() {
		return this.commitDate;
	}

	public void setCommitDate(String commitDate) {
		this.commitDate = commitDate;
	}

	public String getCommitId() {
		return this.commitId;
	}

	public void setCommitId(String commitId) {
		this.commitId = commitId;
	}

}
