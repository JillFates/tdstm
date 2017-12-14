import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.SecurityService

@Secured("isAuthenticated()")
@Slf4j(value='logger', category='grails.app.controllers.WsApiActionController')
class WsApiActionController implements ControllerMethods{

    ApiActionService apiActionService
    SecurityService securityService

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
        ApiAction apiAction = apiActionService.findOrException(id, project)
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
}
