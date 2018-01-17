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

	String 	ApiActionNotBoundProperty 			= 'apiAction.not.bound.property.exception',
			ApiActionInvalidBindingParams 		= 'apiAction.invalid.binding.params.exception',
			ApiActionMustReturnResults 			= 'apiAction.must.return.result.exception',
			ApiActionTaskMessageLapsed 			= 'apiAction.task.message.lapsed',
			ApiActionTaskMessageStalled 		= 'apiAction.task.message.stalled',
			ApiActionTaskMessageTimedout 		= 'apiAction.task.message.timedout'

}