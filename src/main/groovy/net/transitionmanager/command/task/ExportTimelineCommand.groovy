package net.transitionmanager.command.task

class ExportTimelineCommand {

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
