import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.agent.AbstractAgent
import net.transitionmanager.command.ApiActionCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import net.transitionmanager.integration.ActionRequest
import net.transitionmanager.integration.ApiActionJob
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.integration.ApiActionScriptCommand
import net.transitionmanager.integration.ApiActionValidateScriptCommand
import net.transitionmanager.integration.ReactionAssetFacade
import net.transitionmanager.integration.ReactionTaskFacade
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.SecurityService

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
        ApiAction apiAction = apiActionService.find(id, project, true)
        renderSuccessJson(apiActionService.apiActionToMap(apiAction))
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
    def create(ApiActionCommand apiActionCommand) {
        ApiAction apiAction = apiActionService.saveOrUpdateApiAction(apiActionCommand)
        renderSuccessJson(apiActionService.apiActionToMap(apiAction))
    }

    /**
     * Update the corresponding ApiAction.
     */
    @HasPermission(Permission.ActionEdit)
    def update(Long id, ApiActionCommand apiActionCommand) {
        ApiAction apiAction = apiActionService.saveOrUpdateApiAction(apiActionCommand, id)
        renderSuccessJson(apiActionService.apiActionToMap(apiAction))
    }

	@HasPermission(Permission.ActionInvoke)
	def validateSyntax(ApiActionValidateScriptCommand command) {

		if (!command.validate() || command.scripts.collect { it.validate() }.any {!it}) {
			renderAsJson errorsInValidation([command.errors] + command.scripts.collect {it.errors})
		} else {
			renderSuccessJson(apiActionService.validateSyntax(command.scripts))
		}
	}

}