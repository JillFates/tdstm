package net.transitionmanager.command.task.batch

import grails.validation.Validateable

/**
 * A command object used for retrieving a Task context.
 */
@Validateable
class StoredContextCommand {

	Long eventId
	List <TagCommand> tag = []
	String tagMatch = 'ANY'

	static constraints = {
		eventId nullable: true
		tagMatch inList: ['ANY', 'ALL']
	}

	List<Long> getTagIds(){
		return tag.collect{TagCommand t -> t.id}
	}
}
