package net.transitionmanager.controller

import com.tdsops.common.exceptions.InvalidLicenseException
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.GormUtil
import grails.converters.JSON
import grails.validation.ValidationException
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.InvalidRequestException
import net.transitionmanager.service.LicenseAdminService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UnauthorizedException
import com.tdsops.common.grails.ApplicationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.acls.model.NotFoundException
import org.springframework.validation.Errors

import static org.springframework.http.HttpStatus.BAD_REQUEST
import static org.springframework.http.HttpStatus.FORBIDDEN
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.UNAUTHORIZED

/**
 * This set of traits are intended to be used by the controller methods to respond to
 * a variety of Ajax and API calls with a standardized set actions.
 * TODO : JPM : 12/2016 - Burt copied all of the methods from the ServiceResults class
 * 		but should have just called the methods on that object instead to elminate duplication.
 */
trait ControllerMethods {

	// injected dependencies
	MessageSource messageSource
	SecurityService securityService
	LicenseAdminService licenseAdminService

	static final String ERROR_MESG_HEADER = 'X-TM-Error-Message'

	void renderAsJson(data) {
		render(data as JSON)
	}

	void renderSuccessJson(data = [:]) {
		renderAsJson success(data)
	}

	void renderFailureJson(data = [:]) {
		renderAsJson fail(data)
	}

	void renderErrorJson(errorStringOrList) {
		renderAsJson errors(errorStringOrList)
	}

	void renderWarningJson(warningStringOrList) {
		renderAsJson warnings(warningStringOrList)
	}

	Map success(data = [:]) {
		[status: 'success', data: data]
	}

	// TODO - JPM 5/2015 - I think that the fail() and respondWithFailure should? return list instead of map?
	Map fail(data = [:]) {
		[status: 'fail', data: data]
	}

	Map errors(errorStringOrList) {
		[status: 'error', errors: CollectionUtils.asList(errorStringOrList)]
	}

	Map warnings(warnStringOrList) {
		[status: 'warning', warnings: CollectionUtils.asList(warnStringOrList)]
	}

//	void respondAsJson(File file) {
//		response.setStatus(200)
//		setContentTypeJson(response)
//
//		response.outputStream << file.text
//		response.flushBuffer()
//	}

	void sendUnauthorized() {
		sendError UNAUTHORIZED // 401
	}

	void sendMethodFailure() {
		response.sendError(424, 'Method Failure')
	}

	void sendInternalError(log, Exception e) {
		log.error(e.message, e)
		response.addHeader('errorMessage', e.message)
		sendError INTERNAL_SERVER_ERROR
	}

	Map errorsInValidation(Errors validationErrors) {
		errors(validationErrors.allErrors.collect { messageSource.getMessage(it, LocaleContextHolder.locale) })
	}

	Map invalidParams(errorStringOrList) {
		errors(CollectionUtils.asList(errorStringOrList))
	}

	void sendForbidden(log = null, Exception e = null) {
		if (e) {
			response.addHeader('errorMessage', e.message)
		}
		sendError FORBIDDEN // 403
	}

	void sendNotFound(String message='') {
		render(status:NOT_FOUND, message)
		//sendError NOT_FOUND // 404
	}

	/**
	 * Used to respond with a 400 Bad Request
	 */
	void sendBadRequest() {
		response.sendError(400, 'Bad Request')
	}

	/**
	 * Used to indicate that the request input was missing or improperly formatted
	 * @param message - an optional error message as to why the input was invalid, when included will appear in an X header
	 */
	void sendInvalidInput(String message = '') {
		if (message) {
			response.addHeader(ERROR_MESG_HEADER, message)
		}
		render(status:400, text: 'Invalid Input')
	}

	void setContentTypeJson() {
		response.contentType = 'text/json'
	}

	void setContentTypeCsv() {
		response.contentType = 'text/csv'
	}

	void setContentTypeXml() {
		response.contentType = 'text/xml'
	}

	void setContentTypeExcel() {
		response.contentType = 'application/vnd.ms-excel'
	}

	void sendError(HttpStatus status, String message = null) {
		response.sendError(status.value(), message ?: status.reasonPhrase)
	}

	void setStatus(HttpStatus status) {
		response.status = status.value()
	}

	/**
	 * Used to respond with a standard response appropriately for any sort of exception which
	 * will return messages from certain exceptions that have consumer facing messages and
	 * obscuring others that users should NOT see.
	 * @param e - the exception that occurred
	 * @param log - the logger object to log to
	 */
	void handleException(Exception e, log) {
		// oluna: TM-7275 I like type-matching better :)
		switch( e ) {
			case UnauthorizedException:
			case IllegalArgumentException:
				sendForbidden()
				break

			case EmptyResultException:
				sendMethodFailure()
				break

			case ValidationException:
				renderAsJson errorsInValidation(e.errors)
				break

			case InvalidParamException:
			case DomainUpdateException:
				renderErrorJson(e.message)
				break

			case InvalidRequestException:
				renderErrorJson('The request was invalid')
				break

			/*
				If any of the following try to send it to the default Exception handler
				in UrlMappings
			 */
			case AccessDeniedException:
			case NotFoundException:
			case InvalidLicenseException:
				throw e

			default:
				if (log) {
					log.warn ExceptionUtil.stackTraceToString('Unexpected Exception', e, 20)
				}

				renderErrorJson('An unresolved error has occurred')
		}

	}

	/**
	 * Standardized Exception Handler that will catch any exceptions from the Controllers
	 */
	def standardControllerExceptionHandler(Exception e) {
		handleException(e, log)
	}

	def <T> T getFromParams(Class<T> clazz, Map params) {
		T t = (T) clazz.get(GormUtil.hasStringId(clazz) ? params.id : params.long('id'))
		if (t) {
			t
		}
		else {
			flash.message = clazz.simpleName + ' not found with id ' + params
			redirect action: 'list'
			null
		}
	}

	/**
	 * You shouldn't be calling this method or doing any database writes from a controller, it would
	 * be much better to move the persistence and business logic to a transactional service. Until then
	 * this is a convenient way to automatically log validation errors.
	 *
	 * save() is called on the domain class instance, optionally with flushing, and logs warnings
	 * if there are validation errors. Returns the instance whether the save was successful or
	 * not, so it's possible to use a syntax like
	 *
	 *    Person person = save new Person(firstName: '..' ..)
	 *
	 * or just
	 *
	 *    Person person = ...
	 *    save person
	 *
	 * After calling this if you need to do further work depending on whether the save failed, use
	 * the hasErrors(), method, e.g.
	 *
	 *    RoleType rt = save new RoleType(...)
	 *    if (rt.hasErrors()) {
	 *       // panic ensues
	 *    }
	 *    else {
	 *       // everyone in the squire rejoiced
	 *    }
	 *
	 * @param instance  the instance
	 * @param flush  whether to flush (should be avoided until the end of the transaction, there's a lot
	 * of expensive unnecessary flush calls in the app)
	 * @return  the instance passed in
	 */
	def <T> T saveWithWarnings(T instance, boolean flush = false) {
		if (instance == null) return null

		instance.save(flush: flush)

		if (instance.hasErrors()) {
			LoggerFactory.getLogger('grails.app.controllers.' + getClass().name).error(
				'Validation errors saving {} with id {} {}',
				instance.getClass().simpleName, instance.id, GormUtil.allErrorsString(instance))
		}

		instance
	}

	// ----------------------
	// TODO : JPM 12/2016 : Refactor the methods (convertPower, safeJoinAlt, safeJoin) appropriately
	// I have NO clue why Burt put these here...
	// ----------------------

	Number convertPower(Number watts, String powerType) {
		if (watts == null) watts = 0
		powerType == 'Watts' ? Math.round(watts.floatValue()) : (watts / 120).toFloat().round(1)
	}

	/**
	 * Join multiple parts with empty/null values replaced by the 'alt' attribute.
	 * Shortens (x ?: '--') + '/' + (y ?: '--') + '/' + (z ?: '--') to safeJoinAlt('/', '--', x, y, z)
	 *
	 * @param separator  the string to use between parts
	 * @param alt  the value to use if a part evaluates to Groovy-false
	 * @param parts  the parts to join
	 * @return  the concatenated string
	 */
	String safeJoinAlt(String separator, String alt, Object... parts) {
		parts.collect { it ?: '--' }.join(separator)
	}

	/**
	 * Join multiple parts with empty/null values ignored.
	 * Shortens (x ?: '') + '/' + (y ?: '') + '/' + (z ?: '') to safeJoin('/', x, y, z)
	 *
	 * @param separator  the string to use between parts
	 * @param alt  the value to use if a part evaluates to Groovy-false
	 * @param parts  the parts to join
	 * @return  the concatenated string
	 */
	String safeJoin(String separator, Object... parts) {
		parts.findAll().join(separator)
	}

	Project getProjectForWs() {
		SecurityService securityService = ApplicationContextHolder.getBean('securityService')
		Project project = securityService.userCurrentProject
		if (! project) {
			throw new InvalidRequestException('No current project selected for session')
		}
		return project
	}

	/**
	 * Used to retrieve an domain record using the
	 *
	 */
	def <T> T fetchDomain(Class<T> clazz, Map params) {
		T t = (T) clazz.get(GormUtil.hasStringId(clazz) ? params.id : params.long('id'))
		if (t) {
			if (GormUtil.isDomainProperty(t, 'project')) {
				SecurityService securityService = ApplicationContextHolder.getBean('securityService')
				Project project = securityService.userCurrentProject
				if (! project) {
					sendNotFound()
					return null
				} else {
					if (project.id != t.project.id) {
						securityService.reportViolation("attempted to access asset from unrelated project (asset ${t.id})")
						sendNotFound()
						return null
					}
				}
			}
			return t
		} else {
			sendNotFound()
			return null
		}
	}


	/**
	 * Used to load the currentPerson into the controller
	 * @return
	 */
	Person currentPerson() {
		securityService.loadCurrentPerson()
	}
}
