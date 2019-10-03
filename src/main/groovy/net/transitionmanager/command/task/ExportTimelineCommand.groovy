package net.transitionmanager.command.task

class ExportTimelineCommand extends ReadTimelineCommandObject {

	/**
	 *
	 */
	Boolean showAll = false

	static constraints = {
		showAll nullable: false
	}

}
