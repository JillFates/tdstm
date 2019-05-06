package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic
import net.transitionmanager.exception.InvalidParamException

/**
 * This enum represents the AssetComment properties exposed to the API Action operations
 * so the user can add them as parameters upon an API Action execution.
 */
@CompileStatic
enum AssetCommentPropertyEnum {
	TASK_NUMBER('taskNumber', 'Task Number'),
	COMMENT('comment', 'Title'),
	DATE_CREATED('dateCreated', 'Created At'),
	LAST_UPDATED('lastUpdated', 'Last Updated At'),
	DATE_RESOLVED('dateResolved', 'Completed At'),
	DUE_DATE('dueDate', 'Due Date'),
	STATUS_UPDATED('statusUpdated', 'Status Last Changed At'),
	CREATED_BY('createdBy', 'Created By'),
	ASSIGNED_TO('assignedTo', 'Assigned To'),
	CATEGORY('category', 'Category'),
	EST_START('estStart', 'Estimated Start'),
	EST_FINISH('estFinish', 'Estimated Finish'),
	ACTUAL_START('actualStart', 'Actual Start'),
	STATUS('status', 'Status'),
	DURATION_IN_MINUTES('durationInMinutes', 'Estimated Duration (min)'),
	PRIORITY('priority', 'Priority'),
	ROLE('role', 'Team'),
	SEND_NOTIFICATION('sendNotification', 'Send Notification Flag'),
	API_ACTION_INVOKED_AT('apiActionInvokedAt', 'API Action Invoked At'),
	API_ACTION_COMPLETED_AT('apiActionCompletedAt', 'API Action Completed At')

	private String field
	private String label

	private AssetCommentPropertyEnum(String field, String label) {
		this.field = field
		this.label = label
	}

	String getField() {
		return field
	}

	String getLabel() {
		return label
	}

	static AssetCommentPropertyEnum valueOfFieldOrLabel(String str) {
		AssetCommentPropertyEnum value = values().find { e ->
			e.field == str || e.label == str
		}
		if (!value) {
			throw new InvalidParamException('AssetCommentPropertyEnum field or label invalid: ' + str)
		}
		return value
	}

	static Map<String, String> toMap() {
		values().collectEntries { e ->
			[(e.field): e.label]
		}
	}

}
