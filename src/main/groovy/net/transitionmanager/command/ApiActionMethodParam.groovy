package net.transitionmanager.command




class ApiActionMethodParam implements CommandObject{
	String paramName
	String param 	// TODO : JPM 3/2018 - the param property should be renamed to paramName?
	String desc
	String type
	String context
	String fieldName
	String value
	Integer encoded
	Integer readonly
	Integer required
}
