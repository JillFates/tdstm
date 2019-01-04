package com.tdsops.tm.enums.domain

/**
 * The valid options for the AssetComment.commentType property
 *
 * This should be an Enum but we need to first switch all of the references of a string to use the class reference
 * and then we can switch this to an Enum. Also the TASK type is presently using the same value as ISSUE. At some point
 * we'll rectify that situation but in the meantime this works (as ugly as it is...)
 */
class AssetCommentType {

	public static final String COMMENT     = 'comment'
	public static final String ISSUE       = 'issue'
	public static final String INSTRUCTION = 'instruction'
	public static final String TASK        = 'issue'

	static final List<String> list = [COMMENT, INSTRUCTION, ISSUE].asImmutable()
}
