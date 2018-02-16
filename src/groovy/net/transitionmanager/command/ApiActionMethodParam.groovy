package net.transitionmanager.command

import grails.validation.Validateable

@Validateable
class ApiActionMethodParam {
	String param
	String desc
	String type
	String context
	String property
	String value
}
