package com.btasdemir.gitversioning.constant;

/**
 * Message strings used in the system
 * 
 * @author bahadir.tasdemir@hotmail.com.tr
 */
public class MessageConstants {

	public static final String WARNING_INVALID_LOCAL_REPO = "Local repo is invalid, clonning from remote";
	public static final String INFO_GIT_ALREADY_INITIALIZED = "Git is already initialized";
	public static final String INFO_LOCAL_VAR_REPO_EXISTS = "Local reapo variable exists";
	public static final String INFO_LOCAL_REPO_VALID = "Local reapo is valid";
	public static final String INFO_COMMIT = "Repo is commited with the message: {0}";
	public static final String INFO_PUSH = "Repo is pushed to remote";
	public static final String INFO_COMMIT_AND_PUSH = "Repo is commited and pushed to remote";
	public static final String INFO_PULL = "Remote is pulled";
	public static final String INFO_FILE_ADD = "File: {0} is added to the versioning";
	public static final String INFO_REMOVED_FROM_INDEX = "File: {0} is removed. From just index? : {1}";
	public static final String ERROR_REVISION_NOT_FOUND = "The version {0} of the file {1} cannot be found";

}
