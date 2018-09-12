package net.transitionmanager.command.cookbook

import grails.validation.Validateable
import net.transitionmanager.command.task.batch.TagCommand

/**
 * A command object used in creating a Task context.
 */
@Validateable
class ContextCommand {

	Long eventId
	List <Long> tag = []
	String tagMatch = 'ANY'

	static constraints = {
		eventId nullable: true
		tagMatch inList: ['ANY', 'ALL']
	}

	List<Long> getTagIds() {
		return tag
	}
}
