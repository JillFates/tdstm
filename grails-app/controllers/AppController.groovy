import grails.plugin.springsecurity.annotation.Secured

/**
 * Holder for AngularJs App
 *
 * @author Jorge Morayta
 */

@Secured('isAuthenticated()')
class AppController {

	/*
	 * It show the index.gsp that draw the AngularJs App
	 */

	def index() { }
}
