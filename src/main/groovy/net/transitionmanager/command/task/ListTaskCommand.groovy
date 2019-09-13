package net.transitionmanager.command.task

import net.transitionmanager.command.CommandObject

class ListTaskCommand implements CommandObject {

	Integer rows            = 25
	Integer page            = 0
	Integer justRemaining   = 1
	Integer viewUnpublished = 0
	Long    moveEvent       = 0
	String  sortColumn
	String  sortOrder       = 'ASC'

	static constraints = {

		justRemaining range: 0..1, nullable: true
		viewUnpublished range: 0..1, nullable: true
		sortOrder inList: ['ASC', 'DESC']
		moveEvent nullable: true

	}
}
