package net.transitionmanager.action

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.ActionType
import com.tdsops.tm.enums.domain.ApiActionHttpMethod
import com.tdsops.tm.enums.domain.AssetCommentPropertyEnum
import com.tdsops.tm.enums.domain.RemoteCredentialMethod
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.ApiActionCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.Project
import net.transitionmanager.integration.ApiActionValidateScriptCommand
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.ApiCatalogService

@Secured('isAuthenticated()')
@Slf4j
class WsApiActionController implements ControllerMethods {

    ApiActionService apiActionService
    ApiCatalogService apiCatalogService

    /**
     * Get connector details by connector name
     * @param id
     * @return
     */
    def connectorDictionary(String id) {
        renderAsJson(apiCatalogService.getCatalogMethods(id as Long))
    }

    /**
     * List all available ApiActions for the user's project.
     * @return
     */
    @HasPermission(Permission.ActionEdit)
    def list(){
        Project project = securityService.getUserCurrentProjectOrException()
        List<Map> apiActions = apiActionService.list(project, true, params)
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
    def create() {
        ApiActionCommand command = populateCommandObject(ApiActionCommand)
        ApiAction apiAction = apiActionService.saveOrUpdateApiAction(command)
        renderSuccessJson(apiActionService.apiActionToMap(apiAction))
    }

    /**
     * Update the ApiAction domain object with values present in the ApiActionCommand object
     * @return a Map of the ApiAction record that was updated
     */
    @HasPermission(Permission.ActionEdit)
    def update(Long id) {
        ApiActionCommand command = populateCommandObject(ApiActionCommand)
        // TODO : JPM 3/2018 : where is domainVersion coming from?
        ApiAction apiAction = apiActionService.saveOrUpdateApiAction(command, id, domainVersion)
        renderSuccessJson(apiActionService.apiActionToMap(apiAction))
    }

    /**
     * Used to validate that the API Reaction Scripts have the proper syntax
     */
	@HasPermission(Permission.ActionInvoke)
	def validateSyntax() {
		ApiActionValidateScriptCommand commandObject = populateCommandObject(ApiActionValidateScriptCommand)
		validateCommandObject(commandObject)
		renderSuccessJson(apiActionService.validateSyntax(commandObject.scripts))
	}

    /**
     * Retrieve domain properties for API Action CRUD
     */
    @HasPermission(Permission.UserGeneralAccess)
    def domainFields(String domains) {
        Map<String, String> domainsFieldsMap = [:]
        if (domains) {
            for (String domain : domains.split(',')) {
                switch (domain) {
                    case ~/(?i)(task)/:
                        domainsFieldsMap.put(domain, AssetCommentPropertyEnum.toMap())
                        break;
                }
            }
        }
        renderSuccessJson(domains: [domainsFieldsMap])
    }

    /**
     * Returns a JSON map containing the values of all of the enums used to
     * support the ApiAction domain.
     */
    @HasPermission([Permission.ActionCreate, Permission.ActionEdit])
    def enums() {
        renderSuccessJson([
                'httpMethod': ApiActionHttpMethod.names(),
                'agentNames': apiCatalogService.listCatalogNames(),
                'actionTypes': ActionType.toMap(),
                'remoteCredentialMethods': RemoteCredentialMethod.toMap()
        ])
    }
}
