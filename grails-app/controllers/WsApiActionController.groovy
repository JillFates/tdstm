import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.ApiActionCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import net.transitionmanager.integration.ApiActionValidateScriptCommand
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiActionService

@Secured('isAuthenticated()')
@Slf4j
class WsApiActionController implements ControllerMethods {

    ApiActionService apiActionService

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
    def fetch(Long id) {
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
    def update(Long id) {
        // TODO: DMC For PUT command does populate the command objects properly
        // SEE: https://github.com/grails/grails-core/issues/9172
        ApiActionCommand apiActionCommand = populateCommandObject(ApiActionCommand)
        ApiAction apiAction = apiActionService.saveOrUpdateApiAction(apiActionCommand, id)
        renderSuccessJson(apiActionService.apiActionToMap(apiAction))
    }

    /**
     * Used to validate that the API Reaction Scripts have the proper syntax
     */
	@HasPermission(Permission.ActionInvoke)
	def validateSyntax(ApiActionValidateScriptCommand command) {
        // TODO : JPM 2/2018 : TM-9414 Revisit the script validation - this doesn't look quite right
		if (!command.validate() || command.scripts.collect { it.validate() }.any {!it}) {
			renderErrorJson( errorsInValidation([command.errors] + command.scripts.collect {it.errors}) )
		} else {
			renderSuccessJson(apiActionService.validateSyntax(command.scripts))
		}
	}

}
