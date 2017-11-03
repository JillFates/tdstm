import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Provider
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ProviderService

/**
 * Provide the endpoints for working with Providers.
 */
@Secured("isAuthenticated()")
@Slf4j(value='logger', category='grails.app.controllers.WsProviderController')
class WsProviderController implements ControllerMethods{

    private final static DELETE_OK_MESSAGE = "Provider deleted successfully.";

    ProviderService providerService

    /**
     * Create a new Provider
     */
    @HasPermission(Permission.ProviderCreate)
    def createProvider() {
        try {
            Provider provider = providerService.saveOrUpdateProvider(request.JSON)
            renderSuccessJson([provider: provider.toMap()])
        } catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Update an existing Provider.
     *
     * @param id - Provider id
     */
    @HasPermission(Permission.ProviderUpdate)
    def updateProvider(Long id) {
        try {
            Provider provider = providerService.saveOrUpdateProvider(request.JSON, id)
            renderSuccessJson([provider: provider.toMap()])
        } catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Lookup and return a provider by its id.
     *
     * @param id - Provider id
     * @return
     */
    @HasPermission(Permission.ProviderView)
    def getProvider(Long id) {
        try {
            Provider provider = providerService.getProvider(id)
            renderSuccessJson([provider: provider.toMap()])
        } catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Determine if a provider name is unique across projects
     *
     * @param name - the name to lookup.
     */
    @HasPermission(Permission.ProviderView)
    def validateUniqueName(String name) {
        try {
            Long providerId = NumberUtil.toLong(request.JSON.providerId)
            boolean isUnique = providerService.validateUniqueName(name, providerId)
            renderSuccessJson([isUnique: isUnique])
        } catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Return all the available providers for the user's current project.
     *
     */
    @HasPermission(Permission.ProviderView)
    def getProviders(){
        try {
            List<Provider> providers = providerService.getProviders()
            renderSuccessJson(providers*.toMap())
        }catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Delete a given Provider.
     *
     * @param id - id of the provider to be deleted.
     * @return
     */
    @HasPermission(Permission.ProviderDelete)
    def deleteProvider(Long id) {
        try {
            providerService.deleteProvider(id)
            renderSuccessJson([status: DELETE_OK_MESSAGE] )
        } catch(Exception e) {
            handleException(e, logger)
        }
    }
}
