package net.transitionmanager.command




class ApiCatalogCommand implements CommandObject{
	Long id
	String dictionary
	Long version

	static constraints = {
		id nullable: true
		dictionary blank: false, nullable: false
	}
}
