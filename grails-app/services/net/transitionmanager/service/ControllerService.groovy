package net.transitionmanager.service

import com.tdsops.common.exceptions.ServiceException
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import org.springframework.web.context.request.RequestContextHolder

/**
 * A set of methods used to support common functionality used in the Controllers
 */
class ControllerService implements ServiceMethods {

	static transactional = false

	private static
	final List<String> contentTypes = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif'].asImmutable()

	AssetEntityService assetEntityService
	AuditService auditService
	PersonService personService
	UserPreferenceService userPreferenceService

	/**
	 * A utility service to just dump the parameters
	 * @param controller - the controller object
	 * @param params - the parameters
	 */
	void dumpParams(controller, params) {
		StringBuilder sb = new StringBuilder('<h1>Dump of params</h1><ul>')
		params.each { k, v -> sb.append("<li>$k=$v</li>") }
		sb.append('</ul>')
		controller.render sb.toString()
	}

	/**
	 * Validates is the user have the permission and if not redirect to default page
	 * @param controller - a reference to the controller
	 * @param perm - the permission (String) or permissions List<String> that the user requires (optional),
	 * 			PLEASE PLEASE PLEASE use Permission.<Constant> for permission reference
	 * @return true if the user have the permission
	 * @see net.transitionmanager.security.Permission
	 */
	boolean checkPermission(controller, String perm) {
		if (!securityService.hasPermission(perm)) {
			String forwardURI = RequestContextHolder.currentRequestAttributes().request.forwardURI
			securityService.reportViolation("attempted to access $forwardURI without permission ($perm)")
			redirectToDefaultPage(controller, "Sorry but you are unauthorized to perform that action")
			return false
		}

		true
	}

	/**
	 * Validates is the user have the permission
	 *
	 * @param perm - the permission that the user requires, PLEASE PLEASE PLEASE use Permission.<Constant>
	 *     for permission reference
	 * @see net.transitionmanager.security.Permission
	 */
	void checkPermissionForWS(String perm) {
		if (!securityService.hasPermission(perm)) {
			String forwardURI = RequestContextHolder.currentRequestAttributes().request.forwardURI
			securityService.reportViolation("attempted to access $forwardURI without permission ($perm)")
			throw new UnauthorizedException("Don't have the permission $perm")
		}
	}

	/**
	 * Get the user default project or redirects user to the Project List view.
	 * @param controller a reference to the controller
	 * @return The user's currently selected Project object or null if not seleted
	 */
	Project getProjectForPage(controller, String flashMessageEnd = 'before continuing') {
		Project project = securityService.userCurrentProject
		if (project) {
			project
		}
		else {
			log.info "User $securityService.currentUsername didn't have default project"
			controller.flash.message = 'Please select a project ' + flashMessageEnd
			controller.redirect(controller: 'project', action: 'list')
		}
	}

// TODO BB move to trait
	/**
	 * Get the user default project or redirects user to the Project List view.
	 * @param controller a reference to the controller
	 * @return The user's currently selected Project object or null if not seleted
	 */
	Project getRequiredProject() {
		return securityService.getUserCurrentProjectOrException()
	}

	/**
	 * Used to load the Asset specified for the given asset class and id
	 * @param controller - a reference to the controller object
	 * @param project - the user's current project
	 * @param assetClass - the class of object to return (e.g. AssetEntity, Application, Database, etc)
	 * @param id - the id of the asset to be retrieved
	 * @return an asset object if it is found and associated with the given project
	 */
	Object getAssetForPage(Object controller, Project project, AssetClass ac, String id) {
		def asset

		if (!id.isInteger()) {
			log.warn "Invalid asset id ($id) requested for page ${urlInfo(controller)} by user $securityService.currentUsername"
			controller.flash.message = "The requested asset id is invalid"
		}
		else {
			asset = AssetEntityHelper.getAssetById(project, ac, id)
			if (!asset) {
				log.warn "Missing asset id ($id) requested for page ${urlInfo(controller)} by user $securityService.currentUsername"
				controller.flash.message = "The requested asset was not found"
			}
		}
		return asset
	}

	/**
	 * Used to retrieve an Event object for the current user appropriately. If not found then the flash message will be populated with
	 * an message and a null is returned. It validates that the event is associated to the project as well and if not logs a security
	 * message and returns null.
	 *
	 * @param controller - the controller that is calling this method
	 * @param project - the user's current project
	 * @param eventId - the event id to lookup
	 * @return The move event if found otherwise null
	 */
	MoveEvent getEventForPage(controller, Project project, String eventId) {
		MoveEvent event
		Long id = NumberUtil.toLong(eventId)
		if (!id) {
			log.info "getEventForPage() Invalid id ($id) from user $securityService.currentUsername"
			return null
		}

		// handle move events
		event = MoveEvent.read(id)
		if (!event) {
			log.info "getEventForPage() was unable to find event ($id) for user $securityService.currentUsername, project ${project?.id}"
		}

		if (event && event.project != project) {
			log.error "SECURITY : getEventForPage() User $securityService.currentUsername attempted to access an event ($id) unrelated to project ${project?.id}"
			event = null
		}

		if (!event) {
			controller.flash.message = 'Unable to find specified event'
		}

		return event
	}

	/**
	 * Used to retrieve an Bundle object for the current user appropriately. If not found then the flash message will be populated with
	 * an message and a null is returned. It validates that the event is associated to the project as well and if not logs a security
	 * message and returns null.
	 *
	 * @param controller - the controller that is calling this method
	 * @param project - the user's current project
	 * @param eventId - the event id to lookup
	 * @return The move event if found otherwise null
	 */
	MoveBundle getBundleForPage(controller, Project project, String bundleId) {
		MoveBundle bundle
		Long id = NumberUtil.toLong(bundleId)
		if (!id) {
			log.info "getBundleForPage() Invalid id ($id) from user $securityService.currentUsername"
			return null
		}

		// handle move events
		bundle = MoveBundle.read(id)
		if (!bundle) {
			log.info "getBundleForPage() was unable to find bundle ($id) for user $securityService.currentUsername, project ${project?.id}"
		}

		if (bundle && bundle.project != project) {
			log.error "SECURITY : getBundleForPage() User $securityService.currentUsername attempted to access an event ($id) unrelated to project ${project?.id}"
			bundle = null
		}

		if (!bundle) {
			controller.flash.message = 'Unable to find specified bundle'
		}

		return bundle
	}

	/**
	 * Used by the various asset controller methods to perform save/update using a standardized method names on the domain service classes
	 * @param assetService - a reference to the domain service class (e.g. databaseService)
	 * @param assetClass - the type of class that the domain is
	 * @param params - the controller parameters submitted in request
	 */
	protected String saveUpdateAssetHandler(controller, assetService, Map params) {
		String errorMsg
		def asset
		Map model
		Long id = NumberUtil.toLong(params.id)
		boolean isNew = id == null

		params.maintExpDate = TimeUtil.parseDate(params.maintExpDate)
		params.retireDate = TimeUtil.parseDate(params.retireDate)

		try {
			Project project = getProjectForPage(controller)
			if (project && checkPermission(controller, Permission.AssetEdit)) {
				if (isNew) {
					log.debug "saveUpdateAssetHandler() calling saveAssetFromForm()"
					asset = assetService.saveAssetFromForm(project, params)
					log.debug "saveUpdateAssetHandler() saveAssetFromForm() returned $asset"
				}
				else {
					asset = getAssetForPage(controller, project, AssetClass.DEVICE, params.id)
					assetService.updateAssetFromForm(project, params, asset.id)
				}
				// Reload the asset due to the hibernate session
				//asset = AssetEntity.read(asset.id)
				asset.discard()
				model = AssetEntityHelper.simpleModelOfAsset(asset)
			}
			errorMsg = controller.flash.message
			controller.flash.message = null
		} catch (InvalidRequestException | EmptyResultException | UnauthorizedException | DomainUpdateException e) {
			errorMsg = e.message
		} catch (e) {
			log.error ExceptionUtil.stackTraceToString('saveUpdateAssetHandler() failed', e, 80)
			errorMsg = "An error occurred during the update"
		}

		assetEntityService.renderUpdateAssetJsonResponse(controller, model, errorMsg)

		errorMsg
	}

	/**
	 * Returns the URL information about the current controller
	 * @param controller - the current controller
	 * @return the URL called on the controller
	 */
	private String urlInfo(controller) {
		controller.controllerUri + ' ' + controller.actionName
	}

	/**
	 * Used to redirect the user to the default web page
	 * @param controller - the controller object from where the method is being called
	 * @param flashMessage - an optional message to add to flash
	 */
	private void redirectToDefaultPage(controller, flashMessage) {

		if (flashMessage?.size()) {
			controller.flash.message = flashMessage
		}

		if (securityService.loggedIn) {
			controller.redirect(controller: 'module', action: 'user', link: 'dashboard')
		}
		else {
			controller.redirect(controller: 'auth', action: 'login')
		}
	}

	String getDefaultErrorMessage() {
		return "An error occurred. Please contact support for further assistance."
	}

	/**
	 * Check if the exception have a message code to look in the message.properties, and if not use default message
	 */
	String getExceptionMessage(controller, exception) {
		if ((exception instanceof ServiceException) && exception.messageCode) {
			return controller.message(code: exception.messageCode, args: exception.messageArgs)
		}
		else {
			return exception.message
		}
	}

	/**
	 * Switch user project to the new project, only if the user have access to the project
	 */
	String switchContextToProject(Project newProject) {
		String currentProjectId = securityService.userCurrentProjectId
		if (currentProjectId != newProject.id.toString()) {
			if (personService.hasAccessToProject(newProject)) {
				userPreferenceService.setCurrentProjectId(newProject.id)
			}
			else {
				auditService.logSecurityViolation("Try to acccess project $newProject.projectCode")
			}
		}
	}

	/**
	 * Used to handle uploaded files
	 * @param file - the File object that will be set upon success
	 * @param controller - the reference to the controller
	 * @param paramName - the request parameter name of the upload file
	 * @param maxSize - the maximum size allowed for file (default 0 - not checked)
	 * @param contentTypes - a List of acceptable content-types (default [] - allows any)
	 * @return blank if successful otherwise an error message as to the failure
	 */
	private getUploadFile(controller, String paramName, long maxSize = 0, List<String> contentTypes = []) {
		def localFile
		String emsg = ''
		assert paramName
		if (controller.params[paramName]) {
			while (true) {
				localFile = controller.request.getFile(paramName)
				if (!localFile) {
					emsg = 'Unable to retrieve uploaded file'
					break
				}

				if (localFile.isEmpty()) {
					localFile = null
					break
				}

				if (maxSize > 0) {
					if (localFile.size > maxSize) {
						emsg = "Uploaded file exceeds size limit of ${sprintf('%,3d', maxSize)} bytes"
						break
					}
				}

				if (contentTypes.size()) {
					String ct = localFile.getContentType()
					if (!ct) {
						emsg = "Upload file was missing required content-type"
						break
					}
					if (!contentTypes.contains(ct)) {
						emsg = "Content-type of the upload file was invalid"
						break
					}
				}
				// We're good so set the param var with the localFile reference
				break
			}

		}

		return emsg ?: localFile
	}

	/**
	 * Handles uploaded image files, restricted to content-types of
	 *    ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
	 * @param file - the File object that will be set upon success
	 * @param controller - the reference to the controller
	 * @param paramName - the request parameter name of the upload file
	 * @param maxSize - the maximum size allowed for file (default 0 - not checked)
	 * @return blank if successful otherwise an error message as to the failure
	 */
	Object getUploadImageFile(controller, String paramName, long maxSize = 0) {
		return getUploadFile(controller, paramName, maxSize, contentTypes)
	}
}
