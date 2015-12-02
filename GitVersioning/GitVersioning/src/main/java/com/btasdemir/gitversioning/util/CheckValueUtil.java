package com.btasdemir.gitversioning.util;

import org.eclipse.jgit.util.StringUtils;

import com.btasdemir.gitversioning.constant.CommonConstants;

/**
 * Static methods to check values inside system
 * 
 * @author bahadir.tasdemir@hotmail.com.tr
 */
public class CheckValueUtil {

	/**
	 * Check if given string value empty and throw exception if empty
	 * 
	 * @param val
	 *            Given string value
	 * @throws IllegalArgumentException
	 */
	public static void checkStringVal(String val) {
		if (StringUtils.isEmptyOrNull(val)) {
			throw new IllegalArgumentException(CommonConstants.ERROR_EMPTY_ARGUMENT);
		}
	}

	/**
	 * Plural kind of checkStringVal, throws IllegalARgumentException if a value
	 * is empty or null
	 * 
	 * @param vals
	 *            String values to be checked
	 */
	public static void checkStringVals(String... vals) throws IllegalArgumentException {
		if (vals == null) {
			throw new IllegalArgumentException(CommonConstants.ERROR_EMPTY_ARGUMENT);
		} else {
			for (String val : vals) {
				if (StringUtils.isEmptyOrNull(val)) {
					throw new IllegalArgumentException(CommonConstants.ERROR_EMPTY_ARGUMENT);
				}
			}
		}
	}

}
