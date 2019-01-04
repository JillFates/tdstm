package net.transitionmanager.command.tag

import com.tdsops.tm.enums.domain.Color
import net.transitionmanager.command.CommandObject


/**
 * A command object used in creating a Tag.
 */

class CreateCommand implements CommandObject{

	String name
	String description = ''
	Color  color

	static constraints = {
		color inList: Color.values().toList()
	}
}
