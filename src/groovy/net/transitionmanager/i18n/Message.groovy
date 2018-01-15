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

	/*
		i18N messages for working with API Action endpoints.
	 */
	String ApiActionInvalidId = "apiActionCommand.id.invalid"
	String ApiActionInvalidAgentClass = "apiActionCommand.agentClass.invalid"
	String ApiActionInvalidCallbackMode = "apiActionCommand.callbackMode.invalid"
	String ApiActionInvalidCredential = "apiActionCommand.credentialId.invalid"
	String ApiActionInvalidDefaultDataScript = "apiActionCommand.defaultDataScriptId.invalid"
	String ApiActionInvalidName = "apiActionCommand.name.invalid"
	String ApiActionInvalidProvider = "apiActionCommand.providerId.invalid"

	// Messages for the different errors in the ApiAction reactionJson
	String ApiActionInvalidReactionJson = "apiAction.reactionJson.invalid"
	String ApiActionMissingEvaluateOrSuccessInReactionJson = "apiAction.reactionJson.emptyEvaluateOrSuccess"
	String ApiActionMissingDefaultAndErrorInReactionJson = "apiAction.reactionJson.emptyDefaultAndError"
	String ApiActionInvalidProviderPreventsDataScriptValidation = "apiActionCommand.defaultDataScriptId.cannotbevalidated"
	String ApiActionInvalidProviderPreventsCredentialValidation = "apiActionCommand.crendetial.cannotbevalidated"


}