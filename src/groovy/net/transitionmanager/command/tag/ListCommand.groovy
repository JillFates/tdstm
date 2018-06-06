package net.transitionmanager.command.tag

import grails.validation.Validateable

/**
 * A command object used in filtering a list of tags.
 */
@Validateable
class ListCommand {

	String name
	String description
	Date   dateCreated
	Date   lastUpdated

	static constraints = {
		name nullable: true
		description nullable: true
		dateCreated nullable: true
		lastUpdated nullable: true
	}
}
