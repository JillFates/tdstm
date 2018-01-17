package net.transitionmanager.integration

import net.transitionmanager.i18n.Message
import net.transitionmanager.service.MessageSourceService
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

/**
 * This class is used for binding context in every Api Action script processed.
 */
class ApiActionScriptBinding extends Binding {

	MessageSourceService messageSourceService

	ApiActionScriptBinding(MessageSourceService messageSourceService, Map vars = [:]) {
		this.messageSourceService = messageSourceService
		this.variables.putAll([
				SC: ReactionHttpStatus,
				* : vars
		])
	}

	/**
	 * Custom lookup variable. If a variable isn't found it throws an exception
	 * @param name
	 * @return
	 */
	@Override
	Object getVariable(String name) {

		if (variables?.containsKey(name)) {
			return variables.get(name)
		} else {
			throw new ApiActionException(messageSourceService.i18nMessage(
					Message.ApiActionNotBoundProperty,
					[name] as String[],
					'There is no property with name {0} bound in this script context'))
		}
	}

}
