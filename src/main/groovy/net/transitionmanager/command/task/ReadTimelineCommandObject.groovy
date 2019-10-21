package net.transitionmanager.command.task

import net.transitionmanager.command.CommandObject

class ReadTimelineCommandObject implements CommandObject {

	/**
	 * Should belongs to {@code MoveEvent#id}
	 */
	Long id

	static constraints = {
		id nullable: false
	}
}
