import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.domain.Project
import grails.transaction.Transactional

@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsCustomDomainController')
class WsCustomDomainController implements ControllerMethods {
    CustomDomainService customDomainService

    @HasPermission(Permission.UserGeneralAccess)
    @Transactional(readOnly = true)
    def getFieldSpec() {
        try {
            Project project = getProjectForWs()
            renderAsJson(customDomainService.allFieldSpecs(project, params.domain))
        } catch (e) {
            handleException(e, logger)
        }
    }

    @HasPermission(Permission.ProjectFieldSettingsEdit)
    def saveFieldSpec() {
        try {
            Project project = getProjectForWs()
            customDomainService.saveFieldSpecs(project, params.domain, request.JSON)
            render status: 200
        } catch (e) {
            handleException(e, logger)
        }
    }

    /**
     * Used to retrieve the default values of a particular asset property
     * @param id - the domain name that should be inspected or ASSET for all asset classes
     * @param request.JSON.fieldSpec - the request will contain a JSON payload containing the individual field specification
     * @return A list of distinct values from the individual domain or all asset domains for one column
     * @see TM-6451 for implementation details
     */
    @HasPermission(Permission.ProjectFieldSettingsEdit)
    @Transactional(readOnly = true)
    def distinctValues() {
        try {
            Project project = getProjectForWs()
            renderAsJson(customDomainService.distinctValues(project, params.id, request.JSON))
        } catch (e) {
            handleException(e, logger)
        }
    }

    def checkConstraints() {
        throw new RuntimeException('method not implemented')
    }

    def invalidValues() {
        throw new RuntimeException('method not implemented')
    }
}
