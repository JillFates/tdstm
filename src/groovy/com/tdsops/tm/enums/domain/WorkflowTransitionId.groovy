package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * WorkflowTransitionId - represents the valid transId values for the WorkflowTransion domain property transId.
 */
@CompileStatic
class WorkflowTransitionId {
	public static final int HOLD       = 10
	public static final int READY      = 20
	public static final int TRANSPORT  = 110
	public static final int COMPLETED  = 280
	public static final int TERMINATED = 900

	static final List<Integer> list = [HOLD, READY, TRANSPORT, COMPLETED, TERMINATED].asImmutable()
}
