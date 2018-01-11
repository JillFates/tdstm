package net.transitionmanager.i18n

/**
 * i18N keys for ApiAction Exceptions.
 * It can be used as the following code:
 * <code>
 * 	messageSource.addMessage(Message.ApiActionMustReturnResults,
 * 				Locale.FRENCH,
 * 				'Le script doit renvoyer SUCCESS ou ERROR')
 * </code>
 */
interface Message {

	String ApiActionNotBoundProperty = 'apiAction.not.bound.property.exception'
	String ApiActionInvalidBindingParams = 'apiAction.invalid.binding.params.exception'
	String ApiActionMustReturnResults = 'apiAction.must.return.result.exception'

}