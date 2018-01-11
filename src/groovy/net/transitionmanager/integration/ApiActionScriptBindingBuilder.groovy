package net.transitionmanager.integration

import net.transitionmanager.i18n.Message
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Scope
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component

/**
 * Fluent API for Builder pattern.
 * You must define all the necessary params to build an instance of ApiActionScriptBinding
 * <code>
 *      ApiActionScriptBinding scriptBinding = applicationContext.getBean(ApiActionScriptBindingBuilder)
 *              .with(new ActionRequest(['property1': 'value1']))
 *              .with(new ApiActionResponse().asImmutable())
 *              .with(new ReactionAssetFacade())
 *              .with(new ReactionTaskFacade())
 *              .with(new ApiActionJob())
 *                  .build(ReactionScriptCode.FINAL)
 * </code>
 */
@Component
@Scope("prototype")
class ApiActionScriptBindingBuilder {

	MessageSource messageSource

	ActionRequest request
	ApiActionResponse response
	ReactionAssetFacade asset
	ReactionTaskFacade task
	ApiActionJob job

	ApiActionScriptBindingBuilder with(ActionRequest request) {
		this.request = request
		this
	}

	ApiActionScriptBindingBuilder with(ApiActionResponse response) {
		this.response = response
		this
	}

	ApiActionScriptBindingBuilder with(ReactionAssetFacade asset) {
		this.asset = asset
		this
	}

	ApiActionScriptBindingBuilder with(ReactionTaskFacade task) {
		this.task = task
		this
	}

	ApiActionScriptBindingBuilder with(ApiActionJob job) {
		this.job = job
		this
	}
	/**
	 * Check if all the params where defined to build an instance of ApiActionScriptBinding
	 * to be used for ReactionScriptCode code Script
	 * @param code
	 * @param params
	 */
	void checkParams(ReactionScriptCode code, List<String> params) {
		params.each { String param ->

			if (!this."${param}") {
				String message = messageSource.getMessage(Message.ApiActionInvalidBindingParams,
						[code, param] as String[],
						'Can not build a biding context for {0} without {1} object',
						LocaleContextHolder.locale)

				throw new ApiActionException(message)
			}
		}
	}

	/**
	 * Build an instance of ApiActionScriptBinding using all the neccessary objects
	 * accordingly to the ReactionScriptCode code param.
	 * @param code an instance of ReactionScriptCode to define all the necessary object in the binding context
	 * @return an instance of ApiActionScriptBinding
	 */
	ApiActionScriptBinding build(ReactionScriptCode code) {

		ApiActionScriptBinding binding

		switch (code) {
			case ReactionScriptCode.PRE:
				checkParams(ReactionScriptCode.PRE, ['request', 'asset', 'task', 'job'])
				binding = new ApiActionScriptBinding(messageSource,
						[
								request: request,
								asset  : asset,
								task   : task,
								job    : job
						])
				break
			case ReactionScriptCode.EVALUATE:
				checkParams(ReactionScriptCode.EVALUATE, ['request', 'response'])
				binding = new ApiActionScriptBinding(messageSource,
						[
								request : request,
								response: response,
								*       : ReactionScriptCode.values()
										.collectEntries {
									[(it.name()): it]
										}
						])
				break
			default:
				checkParams(ReactionScriptCode.DEFAULT, ['request', 'response', 'asset', 'task', 'job'])
				binding = new ApiActionScriptBinding(messageSource,
						[
								request : request,
								response: response,
								asset   : asset,
								task    : task,
								job     : job,
								*       : ReactionScriptCode.values().collectEntries { [(it.name()): it] }
						])
				break
		}

		return binding
	}

}
