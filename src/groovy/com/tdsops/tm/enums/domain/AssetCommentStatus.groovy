package com.tdsops.tm.enums.domain

/**
 * AssetCommentStatus - represents the valid options for the AssetComment domain property status.
 *
 * This should be an Enum but we need to first switch all of the references of a string to use the class reference
 * and then we can switch this to an Enum.
 *
 */ 
class AssetCommentStatus {
	static final String HOLD='Hold'
	static final String PLANNED='Planned'
	static final String READY='Ready'
	static final String PENDING='Pending'
	static final String STARTED='Started'
	static final String COMPLETED='Completed'
	static final String DONE='Completed'
	static final String TERMINATED='Terminated'
	static final String[] list = [PLANNED, PENDING, READY, STARTED, DONE, HOLD]
	static final getList() {
		return list as List
	}
	static final getTopStatusList() {
		return [PENDING, READY, STARTED, DONE] as List
	}
}	
