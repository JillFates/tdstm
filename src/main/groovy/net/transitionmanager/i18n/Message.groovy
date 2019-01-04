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

	String ApiActionNotBoundProperty 			= 'apiAction.not.bound.property.exception',
	       ApiActionInvalidBindingParams 		= 'apiAction.invalid.binding.params.exception',
	       ApiActionMustReturnResults 			= 'apiAction.must.return.result.exception',
	       ApiActionTaskMessageLapsed 			= 'apiAction.task.message.lapsed',
	       ApiActionTaskMessageStalled 		= 'apiAction.task.message.stalled',
	       ApiActionTaskMessageTimedout 		= 'apiAction.task.message.timedout',
	       ApiActionTaskMessageByPassed 		= 'apiAction.task.message.bypassed',
	       DomainFailureBulk					= 'domain.failure.bulk',
	       FileSystemFilesDeleted               = 'fileSystem.files.deleted',
	       FileSystemFilesCannotBeDeleted       = 'fileSystem.files.cannotdelete',
	       FileSystemInvalidFileExtension 		= 'fileSystem.invalid.fileExtension',
	       InvalidURLFormat					= 'typeMismatch.java.net.URL',
	       ProgressInfoUnableToStopRunningJob  = 'progressInfo.unableToStop',
	       ValidationMissingIds                = 'validation.missing.ids'

	/*
		i18N messages for working with API Action endpoints.
	 */
	String InvalidFieldForDomain = "domain.invalid.field"
	String InvalidJsonFormat = "default.invalid.json.message"
	String ParamReferenceInURLNotFound = "default.param.reference.in.url.not.found"


	String ApiActionInvalidProviderPreventsDataScriptValidation = "apiActionCommand.defaultDataScriptId.cannotbevalidated"
	String ApiActionInvalidProviderPreventsCredentialValidation = "apiActionCommand.crendetial.cannotbevalidated"
	// Messages for the different errors in the ApiAction reactionJson
	String ApiActionMissingStatusOrSuccessInReactionJson = "apiAction.reactionJson.emptyStatusOrSuccess"
	String ApiActionMissingDefaultAndErrorInReactionJson = "apiAction.reactionJson.emptyDefaultAndError"


}
