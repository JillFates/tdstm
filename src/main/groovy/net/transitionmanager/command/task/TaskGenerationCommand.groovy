package net.transitionmanager.command.task

import net.transitionmanager.command.CommandObject


/**
 * A command object used in creating a Task context.
 */

class TaskGenerationCommand implements CommandObject{

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
