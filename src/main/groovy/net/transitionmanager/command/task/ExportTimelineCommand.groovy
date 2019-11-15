package net.transitionmanager.command.task

import net.transitionmanager.command.CommandObject

class ExportTimelineCommand implements CommandObject{

	/**
	 * Should belongs to {@code MoveEvent#id}
	 */
	Long id
	/**
	 * Export All Task or not
	 */
	Boolean showAll = false

	static constraints = {
		id nullable: false
		showAll nullable: false
	}

}
