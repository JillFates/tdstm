package net.transitionmanager.command.cookbook

import grails.validation.Validateable
/**
 * A command object used ...
 */
@Validateable
class GroupCommand {
	Long recipeVersionId
	String sourceCode
	ContextCommand context

	static constraints = {
		recipeVersionId nullable: true
		context cascade: true
		sourceCode nullable: true
	}
}
