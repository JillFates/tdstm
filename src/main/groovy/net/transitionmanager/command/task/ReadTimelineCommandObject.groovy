package net.transitionmanager.command.task

import net.transitionmanager.command.CommandObject

class ReadTimelineCommandObject implements CommandObject {

	/**
	 * Should belongs to {@code MoveEvent#id}
	 */
	Long id
	/**
	 * It defines if task listed in CPA should include or not {@code Task.isPublished}
	 */
	Boolean viewUnpublished = false

	static constraints = {
		id nullable: false
		viewUnpublished nullable: true
	}
}
