package net.transitionmanager.integration
/**
 * This class is used for binding context in every Api Action script processed.
 */
class ApiActionScriptBinding extends Binding {

	ApiActionScriptBinding(Map vars = [:]) {
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
			throw new MissingPropertyException('There is no property with name: ' + name + ' bound in this script context')
		}
	}

	/**
	 * Fluent API for Builder pattern.
	 * You must define all the necessary params to build an instance of ApiActionScriptBinding
	 * <code>
	 *      ApiActionScriptBinding scriptBinding = new ApiActionScriptBinding.Builder()
	 *              .with(new ActionRequest(['property1': 'value1']))
	 *              .with(new ApiActionResponse().asImmutable())
	 *              .with(new ReactionAssetFacade())
	 *              .with(new ReactionTaskFacade())
	 *              .with(new ApiActionJob())
	 *                  .build(ReactionScriptCode.FINAL)
	 * </code>
	 */
	static class Builder {

		ActionRequest request
		ApiActionResponse response
		ReactionAssetFacade asset
		ReactionTaskFacade task
		ApiActionJob job

		Builder with(ActionRequest request) {
			this.request = request
			this
		}

		Builder with(ApiActionResponse response) {
			this.response = response
			this
		}

		Builder with(ReactionAssetFacade asset) {
			this.asset = asset
			this
		}

		Builder with(ReactionTaskFacade task) {
			this.task = task
			this
		}

		Builder with(ApiActionJob job) {
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
					throw new Exception("Can not build a biding context for ${code} without ${param} object")
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
					binding = new ApiActionScriptBinding([
							request: request,
							asset  : asset,
							task   : task,
							job    : job
					])
					break
				case ReactionScriptCode.EVALUATE:
					checkParams(ReactionScriptCode.EVALUATE, ['request', 'response'])
					binding = new ApiActionScriptBinding([
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
					binding = new ApiActionScriptBinding([
							request : request,
							response: response,
							asset   : asset,
							task    : task,
							job     : job,
							*       : ReactionScriptCode.values()
									.collectEntries {
								[(it.name()): it]
									}
					])
					break
			}

			return binding
		}
	}
}
