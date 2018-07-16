package net.transitionmanager.command.cookbook

import grails.validation.Validateable

/**
 * A command object used in creating a Task context.
 */
@Validateable
class ContextCommand {

	Long eventId
	List <Long> tag = []
	Boolean and = true

	static constraints = {
		eventId nullable: true
	}
}
