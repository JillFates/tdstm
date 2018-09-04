package net.transitionmanager.command.task.batch

import grails.validation.Validateable

/**
 * A  sub command object used in retrieving a Task context.
 */
@Validateable
class TagCommand {

	Long id
	String css
	String label
	Boolean strike


	static constraints = {

	}
}
