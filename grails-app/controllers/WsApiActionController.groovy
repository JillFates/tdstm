import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
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
    def agentDetails(String id) {
        renderAsJson(apiActionService.agentDetails(id))
    }

}
