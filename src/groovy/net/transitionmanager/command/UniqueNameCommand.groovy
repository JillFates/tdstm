package net.transitionmanager.command

class UniqueNameCommand {
	/**
	 * Id of the domain object.
	 */
	Long id

	/**
	 * String representing the name be validated.
	 */
	String name

	static constraints = {
		name nullable: false, blank: false
		id nullable: true
	}


}
