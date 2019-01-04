package net.transitionmanager.command.cookbook

import net.transitionmanager.command.CommandObject


/**
 * A command object used ...
 */

class GroupCommand implements CommandObject{
	Long recipeVersionId
	String sourceCode
	ContextCommand context

	static constraints = {
		recipeVersionId nullable: true
		context cascade: true
		sourceCode nullable: true
	}
}
