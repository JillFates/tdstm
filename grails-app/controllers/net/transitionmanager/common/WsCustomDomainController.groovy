package net.transitionmanager.common

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.project.Project
import grails.gorm.transactions.Transactional

@Secured('isAuthenticated()')
class WsCustomDomainController implements ControllerMethods {
    CustomDomainService customDomainService

    @HasPermission(Permission.UserGeneralAccess)
    @Transactional(readOnly = true)
    def getFieldSpec() {
        Project project = getProjectForWs()
        renderAsJson(customDomainService.allFieldSpecs(project, params.domain))
    }

    @HasPermission(Permission.ProjectFieldSettingsEdit)
    def saveFieldSpec() {
        Project project = getProjectForWs()
        customDomainService.saveFieldSpecs(project, params.domain, request.JSON)
        renderSuccessJson()
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
        Project project = getProjectForWs()
        renderAsJson(customDomainService.distinctValues(project, params.id, request.JSON))
    }

    def checkConstraints() {
        throw new RuntimeException('method not implemented')
    }

    def invalidValues() {
        throw new RuntimeException('method not implemented')
    }

    /**
     * Entry point to get all Common field Specs of the current project
     * JIra: TM-6838
     */
    def fieldSpecsWithCommon() {
        Project project = getProjectForWs()
        Map data = customDomainService.fieldSpecsWithCommon(project)
        renderAsJson(data)
    }
}
