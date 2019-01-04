package net.transitionmanager.command

import grails.validation.Validateable

@Validateable
class ApiActionMethodParam {
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
