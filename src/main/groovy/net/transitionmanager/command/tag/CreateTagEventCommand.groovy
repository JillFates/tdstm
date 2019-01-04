package net.transitionmanager.command.tag

import grails.validation.Validateable

/**
 * A command object used in creating a TagEvent.
 */
@Validateable
class CreateTagEventCommand {
	List<Long> tagIds
	Long eventId
}
