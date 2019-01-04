package net.transitionmanager.command




class DataScriptNameValidationCommand implements CommandObject{

	Long providerId
	Long dataScriptId
	String name

	static constraints = {
		providerId nullable:false
		dataScriptId nullable: true
		name nullable: false, blank: false
	}
}
