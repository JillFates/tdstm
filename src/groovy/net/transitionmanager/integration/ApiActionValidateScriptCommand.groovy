package net.transitionmanager.integration

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject
import org.grails.databinding.BindUsing

@Validateable
class ApiActionValidateScriptCommand implements CommandObject {

	@BindUsing({ obj, source ->
		List<ApiActionScriptCommand> scripts = [].withLazyDefault { new ApiActionScriptCommand() }

		return scripts
	})
	List<Map<String, String>> scripts

	static constraints = {
		scripts nullable: false
	}
}

@Validateable
class ApiActionScriptCommand implements CommandObject {

	String code
	String script

	static constraints = {
		code nullable: false
		script nullable: false
	}
}
