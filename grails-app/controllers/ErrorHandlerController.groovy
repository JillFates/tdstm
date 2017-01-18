import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.ErrorHandlerService
import net.transitionmanager.service.LicenseAdminService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UnauthorizedException
import com.tdsops.common.lang.ExceptionUtil

import grails.plugin.springsecurity.annotation.Secured
import java.lang.IllegalArgumentException


/**
 * The ErrorHandlerController controller is used by the system to handle response to various non-success responses such
 * as the 404 Not Found, 403 Forbidden, 500 Runtime errors, or the like. The controller must recogonize page, Ajax/web
 * service requests and API calls so that it can respond appropriately. Page responses will return HTML pages that users
 * can read while the Ajax and API calls respond differently.
 */
@Secured('permitAll')
class ErrorHandlerController implements ControllerMethods {

	CoreService coreService
	ErrorHandlerService errorHandlerService
	SecurityService securityService
	LicenseAdminService licenseAdminService

	static final String SESSION_ATTR_ERROR = 'ErrorHandlerController.error'
	//
	// The following are used for testing purposes only
	//
	def testForbidden() {
		forward action:'forbidden'
	}

	def testUnauthorized() {
		forward action:'unauthorized'
	}

	def testError() {
		5/0
	}

	// ------------

	private Map fetchModel(){
		def model = errorHandlerService.model(request)
		model.isLicenseManagerEnabled = licenseAdminService.isManagerEnabled()
		return model
	}

	/**
	 * unauthorized is triggered when resources are requested that require authentication but the security session
	 * is un-authenticated.
	 * TODO : JPM 12/2016 :  need to determine what we want to do here.
	 */
	def unauthorized() {
		log.debug "Hit unauthorized()"
		// Retrieves the model for the response.
		Map model = fetchModel()

		// Determines the Login URI.
		def loginURI = coreService.getConfigSetting("grails.plugin.springsecurity.auth.loginFormUrl")
		// Appends the Login URI to the continue URL.
		model.continueUrl = model.continueUrl + loginURI
		// Determines whether this is an AJAX request.
		if (errorHandlerService.isAjaxRequest(request)){
			response.setHeader('X-Login-URL', model.continueUrl)
			render ""
		}else{
			return model	
		}

	}

	/**
	 * forbidden handles http 403 error codes that occurs when someone attempts to access a resource without
	 * the necessary permission. For page requests it will return an appropriate error page. For AJAX calls it will
	 * respond with what?
	 */
	def forbidden() {
		log.debug "Hit forbidden()"

		Map model = fetchModel()

		// Add current user project
		model.currProject = securityService.getUserCurrentProject()
		String msg = model.exceptionMsg ?: 'URL not found'
		securityService.reportViolation("${msg} while accessing ${model.requestUri}")
		response.status = 200
		return model
	}

	/**
	 * notFound handles the 404 type of error messages. For page requests it will return a Not Found web page
	 * with a 200 status code and for any static type content (e.g. jpg, gif, css, js) it will return strictly
	 * the 404 status code
	 */
	def notFound() {
		log.debug "Hit notFound()"

		header 'Title', '404 Not Found'
		header 'Content-Type', 'text/html'

		if (errorHandlerService.isStaticContent(request)) {
			response.status = 404
			render ''
		} else {
			response.status = 200
			Map model = fetchModel()
			model.currProject = securityService.getUserCurrentProject()

			return model
		}
	}

	/**
	 * This method is invoked for two primary reason that include any uncaught RuntimeExceptions and
	 * when a controller method is called where there is no Spring
	 */
	def error() {
		log.debug "Hit error()"

		// Need to check for recursive errors stemming primarily from an error in the error gsp page
		boolean beenHereBefore = request.getAttribute(SESSION_ATTR_ERROR)
		if (beenHereBefore) {
			def ex = errorHandlerService.getException(request)
			log.error ExceptionUtil.stackTraceToString('error() The errorHandler got into recursive loop so we just stopped', ex)
			render 'The application ran into a recursive loop trying to render this page so it was terminated.'
			return
		} else {
			request.setAttribute(SESSION_ATTR_ERROR, true)
		}

		Map model = fetchModel()

		// Handle Ajax error messages
		if (errorHandlerService.isAjaxRequest(request)) {
			response.status = 200
			if (model.exception) {
				handleException(model.exception, log)
			}
			renderErrorJson('An unresolved error occurred')
			return
		}

		if (model.exception) {
			switch (model.exception) {
				case UnauthorizedException:
					forward action:'forbidden'
					return
					break

				case IllegalArgumentException:
					// This case handles invalid controller names which is the equivalant of a 404 Not Found
					if (model.exceptionMsg =~ /^Secure object invocation FilterInvocation.*/) {
						forward action:'notFound'
						return
					}
					break

				default:
					log.warn ExceptionUtil.stackTraceToString('Unhandled Exception', model.exception)
					// drop down into the standard error handler
			}
		}

		// Set the status back to 200 so that it doesn't appear as a real error (TBD if this is the proper thing to do)
		response.status = 200

		return model
	}

	def licensing(){
		response.status = 200
		Map model = fetchModel()
		model.licenseStateMap = licenseAdminService.getLicenseStateMap()
		return model
	}


}