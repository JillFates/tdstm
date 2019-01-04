package net.transitionmanager.command




class DataviewNameValidationCommand implements CommandObject{

	Long dataViewId
	String name

	static constraints = {
		dataViewId nullable: true
		name nullable: false, blank: false
	}
}
