package net.transitionmanager.command.tag

import grails.validation.Validateable

/**
 * A command object used in filtering a list of tags.
 */
@Validateable
class SearchCommand {

	String     name
	String     description
	Date       dateCreated
	Date       lastUpdated
	List<Long> moveBundleIds =[]
	Long       moveEventId

	static constraints = {
		name nullable: true
		description nullable: true
		dateCreated nullable: true
		lastUpdated nullable: true
		moveEventId nullable: true
	}
}
