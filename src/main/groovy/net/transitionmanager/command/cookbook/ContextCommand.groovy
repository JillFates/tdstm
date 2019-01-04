package net.transitionmanager.command.cookbook

import net.transitionmanager.command.CommandObject

/**
 * A command object used in creating a Task context.
 */

class ContextCommand implements CommandObject{

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
