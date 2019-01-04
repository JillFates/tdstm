package net.transitionmanager.command.task.batch

import net.transitionmanager.command.CommandObject


/**
 * A  sub command object used in retrieving a Task context.
 */

class TagCommand implements CommandObject{

	Long id
	String css
	String label
	Boolean strike


	static constraints = {

	}
}
