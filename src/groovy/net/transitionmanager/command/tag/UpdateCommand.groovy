package net.transitionmanager.command.tag

import com.tdsops.tm.enums.domain.Color
import grails.validation.Validateable

/**
 * A command object used to update tags.
 */
@Validateable
class UpdateCommand {

	String name
	String description
	Color  color

	static constraints = {
		name nullable: true
		description nullable: true, blank: true
		color nullable: true, inList: Color.values().toList()
	}
}
