package net.transitionmanager.command.tag

import net.transitionmanager.command.CommandObject


/**
 * A command object used in filtering a list of tags.
 */

class SearchCommand implements CommandObject{

	String     name
	String     description
	Date       dateCreated
	Date       lastUpdated
	List<Long> bundleIds =[]
	Long       eventId

	static constraints = {
		name nullable: true
		description nullable: true
		dateCreated nullable: true
		lastUpdated nullable: true
		eventId nullable: true
	}
}
