package net.transitionmanager.controller

import au.com.bytecode.opencsv.CSVWriter
import com.google.gson.JsonSyntaxException
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import net.transitionmanager.exception.InvalidLicenseException
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.WebUtil
import grails.converters.JSON
import grails.validation.ValidationException
import net.transitionmanager.person.Person
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.Project
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.common.ErrorHandlerService
import net.transitionmanager.exception.InvalidConfigurationException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.exception.InvalidRequestException
import net.transitionmanager.exception.InvalidSyntaxException
import net.transitionmanager.license.LicenseAdminService
import net.transitionmanager.exception.LogicException
import net.transitionmanager.exception.ProjectRequiredException
import net.transitionmanager.project.ProjectService
import net.transitionmanager.security.SecurityService
import net.transitionmanager.exception.UnauthorizedException
import net.transitionmanager.security.UserLogin
import org.grails.web.databinding.bindingsource.InvalidRequestBodyException
import org.slf4j.LoggerFactory
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.validation.Errors

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
	ErrorHandlerService errorHandlerService
	// MessageSourceService messageSourceService
	MessageSource messageSource

	// TODO : JPM 2/2018 : Remove licenseAdminService declaration - fix controllers that use but don't declare
	LicenseAdminService licenseAdminService

	ProjectService projectService

	SecurityService securityService

	UserPreferenceService userPreferenceService

	static final String ERROR_MESG_HEADER = 'X-TM-Error-Message'

	// TODO : JPM 4/2019 : Message should be in i18N messages
	static final String INVALID_CSRF_TOKEN = 'Unable to perform action due to missing form token. Please retry form entry.'


	/**
	 * Used to redirect the browser to the login form
	 */
	void redirectToLoginForm() {
		String redirectUrl = securityService.loginUrl()
		// Adding the X-Login-URL header so that we can catch it in Ajax calls
		response.setHeader('X-Login-URL', redirectUrl)
		redirect(uri: redirectUrl)
	}

	/**
	 * Renders a list of maps to a CSV file.
	 *
	 * @param data a list of maps to render to a csv file
	 *
	 * @param fileName The file name of the csv file defaults to filename.
	 */
	void renderAsCSV(List<Map> data, String fileName = 'filename') {
		OutputStreamWriter writer

		try {
			response.setHeader("Content-disposition", "attachment; filename=${fileName}.csv")
			setContentTypeCsv()
			OutputStream outputStream = response.outputStream
			writer = new OutputStreamWriter(outputStream)
			if (data) {
				CSVWriter csvWriter = new CSVWriter(writer)

				// write headers
				String[] rowArray = (data[0].keySet()).toArray()
				csvWriter.writeNext(rowArray)

				// Iterate over the rows to write out the data
				data.each { Map row ->
					rowArray = (row.values()*.toString()).toArray()
					csvWriter.writeNext(rowArray)
				}
			} else {
				writer.write('no results found')
			}
		} finally {
			writer.flush()
			writer.close()
		}
	}

	void renderAsJson(data) {
		response.addHeader('content-type', 'application/json')
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
		[status: 'error', errors: CollectionUtils.asHTMLSanitizedList(errorStringOrList)]
	}

	Map warnings(warnStringOrList) {
		[status: 'warning', warnings: CollectionUtils.asHTMLSanitizedList(warnStringOrList)]
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

	@Deprecated
	void sendInternalError(log, Exception e) {
		log.error(e.message, e)
		response.addHeader('errorMessage', e.message)
		sendError INTERNAL_SERVER_ERROR
	}

	void sendError(HttpStatus status, String message = null) {
		response.sendError(status.value(), message ?: status.reasonPhrase)
	}

	void setStatus(HttpStatus status) {
		response.status = status.value()
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
	void sendBadRequest(String errorMsg='') {
		if (errorMsg) {
			response.addHeader(ERROR_MESG_HEADER, errorMsg)
		}
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

	/**
 	 * Sets the Content-Disposition response-header field to the given filename parameter
	 *
	 * @param filename  The filename to be set
	 */
	void setHeaderContentDisposition(String filename) {
		response.addHeader("Content-Disposition", "attachment; filename=\""+filename+"\"")
	}

	/**
	 * Used to collect a list of Grails GORM Validation Errors and translate them using messageSource bean.
	 * @param validationErrors
	 * @return List of locale specific error messages
	 */
	@Deprecated
	List errorsInValidation(Errors validationErrors) {
		validationErrors.allErrors.collect { messageSource.getMessage(it, LocaleContextHolder.locale) }
	}
	@Deprecated
	List errorsInValidation(List<Errors> validationErrors) {
		validationErrors.findAll {it.allErrors}.collect {it.allErrors.collect { messageSource.getMessage(it, LocaleContextHolder.locale) }}.flatten()
	}

	/**
	 * Various Exception Handlers that will catch each type of exception from the Controllers
	 * and respond appropriately with various messages accordingly. This addresses both page and
	 * Ajax calls by forwarding to certain pages or returning the standard error response JSON structure
	 * with the appropriate message(s).
	 */
	// TODO : JPM : 2/2018 : TM-9204 Replace messages with i18N
	def accessDeniedExceptionHandler(AccessDeniedException e) {
		String msg = 'You do not have permission to perform this action'
		handleException(e, 'forbidden', msg)
	}
	def domainUpdateExceptionHandler(DomainUpdateException e) {
		String msg = e.getMessage() ?: 'Failed to save information'
		handleException(e, 'error', msg)
	}
	def emptyResultExceptionHandler(EmptyResultException e) {
		String msg = e.message ?: 'Requested information was not found'
		handleException(e, 'notFound', msg)
	}
	def illegalArgumentExceptionHandler(IllegalArgumentException e) {
		String msg = e.getMessage() ?: 'An invalid argument was received'
		handleException(e, 'error', msg)
	}
	def invalidConfigurationExceptionHandler(InvalidConfigurationException e) {
		String msg = e.getMessage() ?: 'A configuration is invalid'
		handleException(e, 'error', msg)
	}
	def invalidLicenseExceptionHandler(InvalidLicenseException e) {
		String msg = e.getMessage() ?: 'An invalid argument was received'
		handleException(e, 'licensing', msg)
	}
	def invalidParamExceptionHandler(InvalidParamException e) {
		String msg = e.getMessage() ?: 'A field values was invalid'
		handleException(e, 'error', msg)
	}
	def invalidRequestBodyExceptionHandler(InvalidRequestBodyException e) {
		// Grails throws this exception for a number of reasons so we need to grab the real cause and deal with that
		String defaultMsg = 'An invalid request was received'
		String causeMsg = e.message
		boolean dumpstack = false
		String msg=''

		// This list will continue was we discover more exceptions that we want to report cleanly to the user
		if (causeMsg.startsWith('com.google.gson.JsonSyntaxException:')) {
		 	msg = 'Invalid JSON : ' + e.cause.cause.message ?: defaultMsg
		} else {
			msg = causeMsg ?: defaultMsg
			dumpstack = true
		}
		handleException(e, 'error', msg, dumpstack)
	}
	def invalidRequestExceptionHandler(InvalidRequestException e) {
		String msg = e.getMessage() ?: 'The request was missing required parameters'
		handleException(e, 'error', msg)
	}
	def invalidSyntaxExceptionHandler(InvalidSyntaxException e) {
		String msg = e.getMessage() ?: 'The syntax is invalid'
		handleException(e, 'error', msg)
	}
	def jsonSyntaxExceptionHandler(JsonSyntaxException e) {
		String msg = (e.getMessage() ? "JSON syntax error: ${e.getMessage()}" : 'JSON syntax is invalid')
		handleException(e, 'error', msg)
	}
	def logicExceptionHandler(LogicException e) {
		String msg = e.getMessage() ?: 'A Logic error was encountered'
		handleException(e, 'error', msg)
	}
	// TODO : JM 2/2018 : TM-9203 - replacing this with ForbiddenException
	def unauthorizedExceptionHandler(UnauthorizedException e) {
		String msg = 'You do not have permission to access this part of the system'
		handleException(e, 'forbidden', msg)
	}
	// Thrown when validation fails during a GORM save(failOnError:true)
	def validationExceptionHandler(ValidationException e) {
		List<String> msgs = GormUtil.validateErrorsI18n(e, LocaleContextHolder.locale)
		handleException(e, 'error', msgs)
	}

	// If all else fails the default exception hander will catch the rest
	def defaultExceptionHandler(Exception e) {
		handleException(e, 'error', '', true)
	}

	/**
	 * Used to determine if the current request is Ajax or not
	 * @return true if the request is Ajax or otherwise false
	 */
	boolean isAjaxRequest() {
		return WebUtil.isAjax(request)
	}

	/**
	 * Used by the various Exception Handler methods to appropriately respond to exceptions
	 * @param e - the Exception that was returned back to the controller
	 * @param viewName - the name of the view page to rendered if it was a page request (assuming the controller = errorHandler)
	 * @param errorStringOrList - the message(s) to display otherwise the Exception message is used
	 * @param dumpStacktrace - a flag to dump the stacktrace to the log (default false)
	 */
	private void handleException(Exception e, String viewName, errorStringOrList='', Boolean dumpStacktrace=false) {
		log.debug "handleException(${e.getClass().getName()}) called"
		if (dumpStacktrace || (e instanceof java.lang.IllegalArgumentException)) {
			log.warn ExceptionUtil.stackTraceToString(e.getMessage(), e, 60)
		}

		String alternateMsg = ''
		if (!errorStringOrList) {
			alternateMsg = e.getMessage() ?: e.getClass().getName()
		}

		if (WebUtil.isAjax(request)) {
			renderErrorJson( (errorStringOrList ?: alternateMsg ) )
		} else {
			// Stuff the exception into the request so that the page can access
			errorHandlerService.setException(e, request)

			// Put the message into a header so we can see the error
			String msg = errorStringOrList ? errorStringOrList.toString() : alternateMsg
		 	response.setHeader(ERROR_MESG_HEADER, msg)

			forward controller: 'errorHandler', action: viewName, model: [exceptionLogged:true]
		}
	}

	/**
	 * Used to fetch a domain class by the id property in the params
	 */
	def <T> T getFromParams(Class<T> clazz, Map params) {
		T t = (T) clazz.get(GormUtil.hasStringId(clazz) ? params.id : params.long('id'))
		if (t) {
			t
		} else {
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

		instance.save(flush: flush, failOnError:false)

		if (instance.hasErrors()) {
			LoggerFactory.getLogger('grails.app.controllers.' + getClass().name).error(
				'Validation errors saving {} with id {} {}',
				instance.getClass().simpleName, instance.id, GormUtil.allErrorsString(instance))
		}

		instance
	}

	/**
	 * This will validate a command object and if there is any error(s) it will throw an InvalidParamException
	 * containing the message of the validation errors.
	 *
	 * @throws InvalidParamException
	 */

	// void validateCommand(net.transitionmanager.command.CredentialCommand co) {
	void validateCommandObject(Object co) {
		if (co?.hasProperty('project') && co.project){
			validateProject(co.project)
		}

		if (! co.validate()) {
			String msg = GormUtil.allErrorsString(co)
			// Call the invalidParamExceptionHandler
			throw new InvalidParamException(msg)
		}
	}

	/**
	 * Validates if a project parameter is accessible for the current userLogin.
	 *
	 * @param project an instance of {@code Project}
	 */
	void validateProject(Project project){
		if (!securityService.hasAccessToProject(project, securityService.userLogin)){
			throw new InvalidParamException('Invalid project')
		}
	}
	/**
	 * Populates command object using the request.JSON from body in a http request
	 * <pre>
	 *      ApiActionCommand apiActionCommand = populateCommandObject(ApiActionCommand)
	 * </pr>
	 * @param commandObjectClass command object class
	 * @param autoValidate if the command object should be auto validated.
	 *
	 * @return a instance of commandObjectClass
	 */
	def <T> T populateCommandObject(Class<T> commandObjectClass, autoValidate = true){
		// NOTE: For PUT command does populate the command objects properly
		// SEE: https://github.com/grails/grails-core/issues/9172

		T command = commandObjectClass.newInstance()

		if (request.JSON) {
			// Request was JSON in the body
			bindData(command, request.JSON)
		} else {
			// Request contained query parameters
			bindData(command, params)
		}

		if(autoValidate) {
			validateCommandObject(command)
		}

		return command
	}

	/**
	 * Used to retrieve the version number that should be passed along with any domain attributes that are
	 * going to be updated.
	 * This is separated out from the Command objects because the Grails bindData ignores version and we want
	 * to avoid setting the version on the domain when assigning the command to the domain.
	 * @return the version number passed as a parameter or more likely in the body as JSON
	 */
	Long getDomainVersion() {
		Long version = NumberUtil.toLong(request.JSON?.version)
		if (version == null) {
			version = params.version?.toLong()
		}
		return version
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

	/**
	 * Used by web services to retrieve the project object based on three conditions listed in the logical order:
	 *    1. If projectId argument is passed into the method that will be used
	 *    2. Else If the HTTP Request params contains projectId then that will be used
	 *    3. Else the user's saved current project prefererence is used
	 * @param projectid - the id of a project (if supplied) which will be validated that the user has access to
	 * @return a Project object if found and the user has permission to access
	 * @throws EmptyResultException - if projectId is provided and does not exist or user does not have access to the project
	 * @throws ProjectRequiredException - projectId not provided and user has not selected current project
	 */
	Project getProjectForWs(Long projectId = null) {
		Project project = null

		// Load param projectId if an id wasn't specified directly
		if (! projectId && params.containsKey('projectId') ) {
			projectId = params.long('projectId')
		}

		if (projectId) {
			// Determine if the user has access to the specified project
			if (projectService.hasAccessToProject(null, projectId)) {
				project = Project.get(projectId)
			}
		} else {

			// Load the user's currently selected project
			project = securityService.userCurrentProject
			if (! project) {
				throw new ProjectRequiredException('No current project selected for session')
			}
		}

		if (! project) {
			throw new EmptyResultException('Project not found')
		}

		return project
	}

	/**
	 * Used to retrieve an domain record using the
	 *
	 */
	def <T> T fetchDomain(Class<T> clazz, Map params, Project project = null) {
		if (! params.id) {
			throw new InvalidParamException('Id was missing')
		}

		T t = (T) clazz.get(GormUtil.hasStringId(clazz) ? params.id : params.id.toLong())
		if (! t) {
			throw new EmptyResultException()
		}

		if (GormUtil.isDomainProperty(t, 'project')) {
			Project currentProject = project

			// fetch user current project only if no project was provided
			if (!project) {
				currentProject = securityService.userCurrentProject
			}

			if (! currentProject) {
				// TODO : JPM 2/2018 : Change fetchDomain to throw new Exception for no project selected
				throw new EmptyResultException()
			} else {
				if (currentProject.id != t.project.id) {
					securityService.reportViolation("attempted to access asset from unrelated project (asset ${t.id})")
					throw new EmptyResultException()
				}
			}
		}
		return t
	}

	/**
	 * Used to load the currentPerson into the controller
	 * @return
	 */
	Person currentPerson() {
		securityService.loadCurrentPerson()
	}


	/**
	 * Analyze the request in search of the given parameter, either as part of the request body
	 * or as a query param. If it isn't found, the given UserPreference will be returned.
	 *
	 * This method considers that the parameter can be null, an empty string, false, etc.
	 * @param param - the name of the parameter to look for.
	 * @param userPreference - the UserPreference code
	 * @return the value of the parameter or the corresponding user preference's.
	 */
	def getParamOrPreference(String param, UserPreferenceEnum userPreference) {
		UserLogin userLogin = securityService.getUserLogin()
		def paramValue
		// Flag that is used to signal that the given parameter has been found.
		boolean paramIsPresent = false
		// Check if the parameter is part of the request body.
		if (request.JSON) {
			if (request.JSON.containsKey(param)) {
				paramValue = request.JSON[param]
				paramIsPresent = true
			}
		// Check if the param is present in the query params.
		} else if (params.containsKey(param)) {
			paramValue = params[param]
			paramIsPresent = true
		}

		return paramIsPresent ? paramValue : userPreferenceService.getPreference(userLogin, userPreference)
	}
}
