package net.transitionmanager.command.task

import grails.validation.Validateable

/**
 * A command object used in creating a Task context.
 */
@Validateable
class TaskGenerationCommand {

	Long recipeId
	Long eventId
	List <Long> tag = []
	String tagMatch = 'ANY'
	Boolean deletePrevious
	Boolean useWIP
	Boolean autoPublish

	static constraints = {
		eventId nullable: true
		tagMatch inList: ['ANY', 'ALL']
	}

	List<Long> getTagIds() {
		return tag
	}
}
