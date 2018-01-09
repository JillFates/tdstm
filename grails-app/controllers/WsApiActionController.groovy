import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import net.transitionmanager.integration.*
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.SecurityService
import org.codehaus.groovy.grails.web.json.JSONObject

@Secured('isAuthenticated()')
@Slf4j
class WsApiActionController implements ControllerMethods {

	ApiActionService apiActionService
	SecurityService securityService

	/**
	 * Get a list of agent names
	 * @return
	 */
	def agentNames() {
		renderAsJson(apiActionService.agentNamesList())
	}

	/**
	 * Get agent details by agent name
	 * @param id
	 * @return
	 */
	def agentDictionary(String id) {
		renderAsJson(apiActionService.agentDictionary(id))
	}

	/**
	 * List all available ApiActions for the user's project.
	 * @return
	 */
	@HasPermission(Permission.ActionEdit)
	def list(){
		Project project = securityService.getUserCurrentProjectOrException()
		List<Map> apiActions = apiActionService.list(project, false, params)
		renderSuccessJson(apiActions)
	}

	/**
	 * Fetch the ApiAction with this id, if it belongs to the user's project.
	 * @return
	 */
	@HasPermission(Permission.ActionEdit)
	def fetch(Long id){
		Project project = securityService.userCurrentProject
		ApiAction apiAction = GormUtil.findInProject(project, ApiAction, id, true)
		renderSuccessJson(apiAction.toMap(false))
	}

	/**
	 * Delete the ApiAction with this id, if it belongs to the user's project.
	 * @return
	 */
	@HasPermission(Permission.ActionDelete)
	def delete(Long id) {
		Project project = securityService.userCurrentProject
		apiActionService.delete(id, project)
		renderSuccessJson([deleted: true])
	}


	/**
	 * Create a new ApiAction.
	 */
	@HasPermission(Permission.ActionCreate)
	def create() {
		Project project = securityService.userCurrentProject
		ApiAction apiAction = apiActionService.saveOrUpdateApiAction(project, request.JSON)
		renderSuccessJson(apiAction.toMap(false))
	}


	/**
	 * Update the corresponding ApiAction.
	 */
	@HasPermission(Permission.ActionEdit)
	def update(Long id) {
		Project project = securityService.userCurrentProject
		ApiAction apiAction = apiActionService.saveOrUpdateApiAction(project, request.JSON, id)
		renderSuccessJson(apiAction.toMap(false))
	}

	/**
	 * Validates
	 */
	@HasPermission(Permission.ActionInvoke)
	def validateSyntax() {


		List<Map<String, ?>> data = []

//		if (!command.validate()) {
//			throw new InvalidParamException('Invalid parameters')
//		}
//		command.scripts.each { ApiActionScriptCommand scriptBinding ->
//
//			ReactionScriptCode code = ReactionScriptCode.valueOf(scriptBinding.code)
//			String script = scriptBinding.script
//			Map<String, ?> scriptResults = [code: code.name()]
//			try {
//
//				Map<String, ?> result = apiActionService.evaluateReactionScript(
//						code,
//						script,
//						new ActionRequest(),
//						new ApiActionResponse(),
//						new ReactionTaskFacade(),
//						new ReactionAssetFacade(),
//						new ApiActionJob()
//				)
//
//				scriptResults.result = result.result?.toString()
//				scriptResults.error = null
//
//			} catch (Exception ex){
//				log.error("Exception ", ex)
//				scriptResults.error = ex.getMessage()
//			}
//
//			data.add(scriptResults)
//		}

		def json = request.JSON
//		ApiActionValidateScriptCommand command = new ApiActionValidateScriptCommand()
//		bindData(json, command)

		json.each { JSONObject jsonObject ->

			ReactionScriptCode code = ReactionScriptCode.valueOf(jsonObject.code)
			String script = jsonObject.script
			Map<String, ?> scriptResults = [code: code.name()]
			try {

				Map<String, ?> result = apiActionService.evaluateReactionScript(
						code,
						script,
						new ActionRequest(),
						new ApiActionResponse(),
						new ReactionTaskFacade(),
						new ReactionAssetFacade(),
						new ApiActionJob()
				)

				scriptResults.result = result.result?.toString()
				scriptResults.error = null

			} catch (Exception ex){
				log.error("Exception ", ex)
				scriptResults.error = ex.getMessage()
			}

			data.add(scriptResults)
		}

		renderSuccessJson(data)
	}
}
