package net.transitionmanager.command.tag

import com.tdsops.tm.enums.domain.Color
import grails.validation.Validateable

/**
 * A command object used in creating a Tag.
 */
@Validateable
class CreateCommand {

	String name
	String description = ''
	Color  color

	static constraints = {
		color inList: Color.values().toList()
	}
}
