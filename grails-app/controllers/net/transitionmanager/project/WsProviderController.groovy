package net.transitionmanager.project

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.action.Provider
import net.transitionmanager.security.Permission
import net.transitionmanager.project.ProviderService
/**
 * Provide the endpoints for working with Providers.
 */
@Secured("isAuthenticated()")
class WsProviderController implements ControllerMethods{

    private final static DELETE_OK_MESSAGE = "Provider deleted successfully.";

    ProviderService providerService

    /**
     * Create a new Provider
     */
    @HasPermission(Permission.ProviderCreate)
    def createProvider() {
        Provider provider = providerService.saveOrUpdateProvider(request.JSON)
        renderSuccessJson([provider: provider.toMap()])
    }

    /**
     * Update an existing Provider.
     *
     * @param id - Provider id
     */
    @HasPermission(Permission.ProviderUpdate)
    def updateProvider(Long id) {
        Provider provider = providerService.saveOrUpdateProvider(request.JSON, id)
        renderSuccessJson([provider: provider.toMap()])
    }

    /**
     * Lookup and return a provider by its id.
     *
     * @param id - Provider id
     * @return
     */
    @HasPermission(Permission.ProviderView)
    def getProvider(Long id) {
        Provider provider = providerService.getProvider(id, null, true)
        renderSuccessJson([provider: provider.toMap()])
    }

    /**
     * Determine if a provider name is unique across projects
     *
     * @param name - the name to lookup.
     */
    @HasPermission(Permission.ProviderView)
    def validateUniqueName(String name) {
        Long providerId = NumberUtil.toLong(request.JSON.providerId)
        boolean isUnique = providerService.validateUniqueName(name, providerId)
        renderSuccessJson([isUnique: isUnique])
    }

    /**
     * Return all the available providers for the user's current project.
     *
     */
    @HasPermission(Permission.ProviderView)
    def getProviders(){
        List<Provider> providers = providerService.getProviders()
        renderSuccessJson(providers*.toMap())
    }

    /**
     * Delete a given Provider.
     *
     * @param id - id of the provider to be deleted.
     * @return
     */
    @HasPermission(Permission.ProviderDelete)
    def deleteProvider(Long id) {
        providerService.deleteProvider(id)
        renderSuccessJson([status: DELETE_OK_MESSAGE] )
    }
}
