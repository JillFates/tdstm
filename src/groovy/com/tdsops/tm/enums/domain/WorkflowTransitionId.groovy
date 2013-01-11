package com.tdsops.tm.enums.domain

/**
 * WorkflowTransitionId - represents the valid transId values for the WorkflowTransion domain property transId.
 *
 */ 
class WorkflowTransitionId {
	static final Integer HOLD=10
	static final Integer READY=20
	static final Integer TRANSPORT=110
	static final Integer COMPLETED=280
	static final Integer TERMINATED=900
	static final getList() {
		return [HOLD,READY,TRANSPORT,COMPLETED,TERMINATED]
	}
}	