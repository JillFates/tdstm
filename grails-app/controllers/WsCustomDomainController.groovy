import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CustomDomainService

@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsCustomDomainController')
class WsCustomDomainController implements ControllerMethods {
    CustomDomainService customDomainService

    @HasPermission(Permission.UserGeneralAccess)
    def getFieldSpec() {
        try {
            renderAsJson(customDomainService.allFieldSpecs(params.domain))
        } catch (e) {
            handleException(e, logger)
        }
    }

    @HasPermission(Permission.ProjectFieldSettingsEdit)
    def saveFieldSpec() {
        try {
            renderAsJson(["key": "value"])
        } catch (e) {
            handleException(e, logger)
        }
    }

}
