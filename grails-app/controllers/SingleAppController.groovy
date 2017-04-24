import grails.plugin.springsecurity.annotation.Secured

/**
 * Single App AngularJS 2-4
 *
 * @author Jorge Morayta
 */

@Secured('isAuthenticated()')
class SingleAppController {
    def index() {
        log.info("Single App Controller - Running under Angular JS")
    }
}
