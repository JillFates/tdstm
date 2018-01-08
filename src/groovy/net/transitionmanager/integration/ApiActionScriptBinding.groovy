package net.transitionmanager.integration

import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

/**
 * This class is used for binding context in every Api Action script processed.
 */
class ApiActionScriptBinding extends Binding {

	MessageSource messageSource

	ApiActionScriptBinding(MessageSource messageSource, Map vars = [:]) {
		this.messageSource = messageSource
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
			throw new ApiActionException(messageSource.getMessage('apiAction.not.bound.property.exception',
					[ name ] as String[],
					'There is no property with name {0} bound in this script context',
					LocaleContextHolder.locale))
		}
	}

}
