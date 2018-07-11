package net.transitionmanager.command

import grails.validation.Validateable

@Validateable
class ApiCatalogCommand {
	Long id
	String dictionary
	Long version

	static constraints = {
		id nullable: true
		dictionary blank: false, nullable: false
	}
}
