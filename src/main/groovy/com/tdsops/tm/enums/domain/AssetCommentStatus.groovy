package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Represents the valid options for the AssetComment domain property status.
 *
 * This should be an Enum but we need to first switch all of the references of a string to use the class reference
 * and then we can switch this to an Enum.
 */
@CompileStatic
class AssetCommentStatus {

	public static final String HOLD       = 'Hold'
	public static final String PLANNED    = 'Planned'
	public static final String READY      = 'Ready'
	public static final String PENDING    = 'Pending'
	public static final String STARTED    = 'Started'
	public static final String COMPLETED  = 'Completed'
	public static final String TERMINATED = 'Terminated'

	static final List<String> list = [PLANNED, PENDING, READY, STARTED, COMPLETED, HOLD].asImmutable()

	static final List<String> topStatusList = [PENDING, READY, STARTED, HOLD, COMPLETED].asImmutable()

	/**
	 * The statuses of tasks that the invocation of actions are allowed
	 */
	static final List<String> CanInvokeActionStatusCodes = [READY, STARTED].asImmutable()

	static final List<String> ActionableStatusCodes = [READY, STARTED, HOLD].asImmutable()

	/**
	 * The statuses of tasks that the resetting of actions are allowed
	 */
	static final List<String> AllowedStatusesToResetAction = [READY, STARTED, HOLD, COMPLETED]

	/**
	 * The statuses that are allowed to change states to
	 */
	static final List<String> AllowedStatusesForSetState = [READY, STARTED, HOLD, COMPLETED]

}
