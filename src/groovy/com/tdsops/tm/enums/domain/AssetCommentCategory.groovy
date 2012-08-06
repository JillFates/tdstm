package com.tdsops.tm.enums.domain

/**
 * AssetCommentCategory - represents the valid options for the AssetComment domain property category.
 *
 * This should be an Enum but we need to first switch all of the references of a string to use the class reference
 * and then we can switch this to an Enum.
 *
 */ 
class AssetCommentCategory {
	static final String GENERAL='general'
	static final String DISCOVERY='discovery'
	static final String PLANNING='planning'
	static final String WALKTHRU='walkthru'
	static final String PREMOVE='premove'
	static final String MOVEDAY='moveday'
	static final String SHUTDOWN='shutdown'
	static final String PHYSICAL='physical'
	static final String STARTUP='startup'
	static final String POSTMOVE='postmove'
	static final getList() {
		return [GENERAL, DISCOVERY, PLANNING, WALKTHRU, PREMOVE, MOVEDAY, SHUTDOWN, PHYSICAL, STARTUP, POSTMOVE]
	}
}	
