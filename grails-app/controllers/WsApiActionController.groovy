import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiActionService
import net.transitionmanager.service.DomainUpdateException
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
        List<Map> apiActions = apiActionService.list(project, false)
        renderSuccessJson(apiActions)
    }

    /**
     * Fetch the ApiAction with this id, if it belongs to the user's project.
     * @return
     */
    @HasPermission(Permission.ActionEdit)
    def fetch(Long id){
        Project project = securityService.userCurrentProject
        ApiAction apiAction = apiActionService.find(id, project)
        Map apiActionMap = null
        if (apiAction) {
            apiActionMap = apiAction.toMap(false)
        }
        renderSuccessJson([apiAction: apiActionMap])
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

}
