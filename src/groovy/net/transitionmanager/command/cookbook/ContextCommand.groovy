package net.transitionmanager.command.cookbook

import grails.validation.Validateable

/**
 * A command object used in creating a Task context.
 */
@Validateable
class ContextCommand {

	Long eventId
	List<Long> bundleId
	List <Long> tag = []
	boolean and = true

	static constraints = {
		eventId nullable: true
		bundleId nullable: true
	}
}
