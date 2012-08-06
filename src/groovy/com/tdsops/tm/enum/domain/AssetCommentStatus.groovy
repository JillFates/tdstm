package com.tdsops.tm.enum.domain

/**
 * AssetCommentStatus - represents the valid options for the AssetComment domain property status.
 *
 * This should be an Enum but we need to first switch all of the references of a string to use the class reference
 * and then we can switch this to an Enum.
 *
 */ 
class AssetCommentStatus {
	static final String READY='Ready'
	static final String PENDING='Pending'
	static final String PLANNED='Planned'
	static final String STARTED='Started'
	static final String HOLD='Hold'
	static final String COMPLETED='Completed'
	static final getList() {
		return [PLANNED, PENDING, READY, STARTED, HOLD, COMPLETED]
	}
}	
