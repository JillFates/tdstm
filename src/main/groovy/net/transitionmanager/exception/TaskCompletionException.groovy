package net.transitionmanager.exception

import groovy.transform.CompileStatic

/**
 * Signals that a task can not be completed.
 */
@CompileStatic
class TaskCompletionException extends Exception {
	TaskCompletionException(String message) {
		super(message)
	}
}
