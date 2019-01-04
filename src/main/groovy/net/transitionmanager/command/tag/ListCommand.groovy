package net.transitionmanager.command.tag

import net.transitionmanager.command.CommandObject


/**
 * A command object used in filtering a list of tags.
 */

class ListCommand implements CommandObject{

	String name
	String description
	Date   dateCreated
	Date   lastUpdated
	Long   bundleId
	Long   eventId

	static constraints = {
		name nullable: true
		description nullable: true
		dateCreated nullable: true
		lastUpdated nullable: true
		bundleId nullable: true
		eventId nullable: true
	}
}
