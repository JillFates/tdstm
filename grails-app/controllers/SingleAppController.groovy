import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.service.EnvironmentService

/**
 * Single App AngularJS 2-4
 *
 * @author Jorge Morayta
 */

@Secured('isAuthenticated()')
class SingleAppController {
    EnvironmentService environmentService

    def index() {
        [buildHash: environmentService.buildHash]
    }
}