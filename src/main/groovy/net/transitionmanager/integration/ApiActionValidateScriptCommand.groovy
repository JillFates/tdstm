package net.transitionmanager.integration

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject

/**
 * API action command object for /ws/apiAction/validateSyntax request.
 * It should be bind with the following JSON content example:
 * <pre>
 * {
 *		"scripts" : [
 *		{
 *			"code": "EVALUATE",
 *			"script": "if (response.status == SC.OK) {\n   return SUCCESS\n} else {\n   return ERROR\n}"
 *		},
 *		{
 *			"code": "SUCCESS",
 *			"script": "task.done()"
 *		}
 *		]
 *	}
 * </pre>
 */
@Validateable
class ApiActionValidateScriptCommand implements CommandObject {

	List<ApiActionScriptCommand> scripts

	static constraints = {
		scripts nullable: false, minSize: 1
	}
}

