package com.tdsops.common.exceptions

/**
 * Exception used to signal that a task can not be completed
 */
class TaskCompletionException extends Exception {
	public TaskCompletionException(String message) {
		super(message);
	}
}