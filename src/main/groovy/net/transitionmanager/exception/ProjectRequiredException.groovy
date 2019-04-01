package net.transitionmanager.exception

import groovy.transform.CompileStatic

/**
 * Indicates that some source code has invalid syntax.
 */
@CompileStatic
class ProjectRequiredException extends RuntimeException {
	ProjectRequiredException(CharSequence message='Session must have a project') {
		super(message.toString())
	}
}
