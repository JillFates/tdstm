import grails.plugin.springsecurity.annotation.Secured

/**
 * Holder for AngularJs App
 *
 * @author Jorge Morayta
 */
class AppController {

	/*
	 * It show the module.html that draw the AngularJs App
	 */
	@Secured('permitAll')
	def index() {}
}
