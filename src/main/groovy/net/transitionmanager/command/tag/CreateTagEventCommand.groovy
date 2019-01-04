package net.transitionmanager.command.tag

import net.transitionmanager.command.CommandObject


/**
 * A command object used in creating a TagEvent.
 */

class CreateTagEventCommand implements CommandObject{
	List<Long> tagIds
	Long eventId
}
