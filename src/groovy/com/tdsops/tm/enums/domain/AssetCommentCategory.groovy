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
	static final String VERIFY='verify'
	static final String ANALYSIS='analysis'
	static final String DESIGN='design'
	static final String BUILDOUT='buildout'
	static final String TRANSPORT='transport'
	static final String CLOSEOUT='closeout'

	static final getList() {
		return [GENERAL, DISCOVERY, PLANNING, WALKTHRU, PREMOVE, MOVEDAY, SHUTDOWN, PHYSICAL, STARTUP, POSTMOVE, VERIFY, ANALYSIS, DESIGN, BUILDOUT, TRANSPORT, CLOSEOUT]
	}

	static final getPreMoveCategories() {
		return [GENERAL,DISCOVERY,VERIFY,ANALYSIS,PLANNING,DESIGN,BUILDOUT,WALKTHRU,PREMOVE]
	}

	static final getMoveDayCategories() {
		return [MOVEDAY,SHUTDOWN,PHYSICAL,TRANSPORT, STARTUP]
	}

	static final getPostMoveCategories() {
		return [POSTMOVE,CLOSEOUT]
	}

	static final getDiscoveryCategories() {
		return [DISCOVERY,VERIFY]
	}

	static final getPlanningCategories() {
		return [ANALYSIS,PLANNING,DESIGN,BUILDOUT]
	}

}