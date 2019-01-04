package net.transitionmanager.command.tag

import com.tdsops.tm.enums.domain.Color
import net.transitionmanager.command.CommandObject


/**
 * A command object used to update tags.
 */

class UpdateCommand implements CommandObject{

	String name
	String description
	Color  color

	static constraints = {
		name nullable: true
		description nullable: true, blank: true
		color nullable: true, inList: Color.values().toList()
	}
}
