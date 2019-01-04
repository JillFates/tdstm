package net.transitionmanager.command.task.batch

import net.transitionmanager.command.CommandObject


/**
 * A command object used for retrieving a Task context.
 */

class StoredContextCommand implements CommandObject{

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
