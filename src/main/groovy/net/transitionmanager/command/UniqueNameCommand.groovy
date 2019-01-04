package net.transitionmanager.command

class UniqueNameCommand implements CommandObject{
	/**
	 * Id of the domain object.
	 */
	Long assetId

	/**
	 * String representing the name be validated.
	 */
	String name

	static constraints = {
		name nullable: false, blank: false
		assetId nullable: true
	}


}
