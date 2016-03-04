import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import com.tdsops.tm.enums.domain.AssetClass

import javax.servlet.http.HttpSession
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.common.exceptions.ServiceException

import org.springframework.web.context.request.RequestContextHolder

/**
 * A set of methods used to support common functionality used in the Controllers
 */
class ControllerService {

	static transactional=false

	def assetEntityService
	def securityService
	def personService
	def userPreferenceService
	def auditService

	/** 
	 * A utility service to just dump the parameters
	 * @param controller - the controller object
	 * @param params - the parameters
	 */
	void dumpParams(controller, params) {
		StringBuffer sb = new StringBuffer('<h1>Dump of params</h1><ul>')
		params.each {k,v -> sb.append("<li>$k=$v</li>")}
		sb.append('</ul>')
		controller.render sb.toString()
	}

	/**
	 * Used to get the user default project for a page request or redirects user to the Project List view
	 * @param controller - a reference to the controller
	 * @param perm - the permission (String) or permissions List<String> that the user requires (optional)
	 * @return The LoginUser object and their currently selected Project object
	 * @param controller - a reference to the controller
	 * @return The user's currently selected Project object if selected otherwise null
	 */
	Project getProjectForPage(controller, UserLogin user, perm=null) {
		def project = securityService.getUserCurrentProject();
		if (! project) {
			log.info "User ${user} didn't have default project"
			controller.flash.message = "Please select a project before continuing"
			controller.redirect(controller:'project', action:'list')
		}

		if (perm) {
			if (perm instanceof String)
				perm = [perm]

			boolean hasPerm=false
			for(int i=0; i < perm.size(); i++) {
				if (securityService.hasPermission(user, perm[i])) {
					hasPerm = true
					break
				}
			}
			if (!hasPerm) {
				def request = RequestContextHolder.currentRequestAttributes().request
				String 
				securityService.reportViolation("attempted to access ${request.forwardURI} without permission ($perm)", user)
				redirectToDefaultPage(controller, "Sorry but you are unauthorized to perform that action")
				project = null
			}
		}

		return project
	}

	/**
	 * Validates is the user have the permission
	 *
	 * @param perm - the permission (String) or permissions List<String> that the user requires
	 */
	boolean hasPermission(user, perm) {
		if (perm instanceof String)
			perm = [perm]

		boolean hasPerm=false
		for(int i=0; i < perm.size(); i++) {
			if (securityService.hasPermission(user, perm[i])) {
				hasPerm = true
				break
			}
		}
		return hasPerm
	}

	/**
	 * Validates is the user have the permission and if not redirect to default page
	 * @param controller - a reference to the controller
	 * @param perm - the permission (String) or permissions List<String> that the user requires (optional)
	 * @return true if the user have the permission
	 */
	boolean checkPermission(controller, perm, logError=true) {
		def user = securityService.getUserLogin()
		boolean hasPerm = hasPermission(user, perm)

		if (!hasPerm && logError) {
			def request = RequestContextHolder.currentRequestAttributes().request
			securityService.reportViolation("attempted to access ${request.forwardURI} without permission ($perm)", user)
			redirectToDefaultPage(controller, "Sorry but you are unauthorized to perform that action")
		}

		return hasPerm
	}

	/**
	 * Validates is the user have the permission
	 *
	 * @param perm - the permission (String) or permissions List<String> that the user requires
	 */
	boolean checkPermissionForWS(perm, logError=true) {
		def user = securityService.getUserLogin()
		boolean hasPerm = hasPermission(user, perm)

		if (!hasPerm && logError) {
			def request = RequestContextHolder.currentRequestAttributes().request
			securityService.reportViolation("attempted to access ${request.forwardURI} without permission ($perm)", user)
			throw new UnauthorizedException("Don't have the permission $perm")
		}

		return hasPerm
	}

	/**
	 * Overloaded version of the getProjectForPage(controller, user)
	 * @param controller - a reference to the controller
	 * @param perm - the permission (String) or permissions List<String> that the user requires (optional)
	 * @return The user's currently selected Project object or null if not seleted
	 */
	Project getProjectForPage(controller, perm=null) {
		def (project, user) = getProjectAndUserForPage(controller, perm)
		return project
	}

	/*
	 * Used to get the user and their default/project objects for a page request. If no selected project it will redirect the user to 
	 * the Project List view automatically. In this case the returned project object will be null. The routine will optionally validation
	 * that the user has the permission specified. If the user doesn't have the permission it will redirect the user to the default page 
	 * and provide a standard error message.
	 * @param controller - a reference to the controller
	 * @param perm - the permission (String) or permissions List<String> that the user requires (optional)
	 * @return The user's currently selected Project object and their LoginUser object
	 */
	List getProjectAndUserForPage(controller, perm=null) {
		def user = securityService.getUserLogin()
		return [getProjectForPage(controller, user, perm), user]
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

		if (! id.isInteger()) {
			log.warn "Invalid asset id ($id) requested for page ${urlInfo(controller)} by user ${securityService.getUserLogin()}"
			controller.flash.message = "The requested asset id is invalid"
		} else {
			asset = AssetEntityHelper.getAssetById(project, ac, id)
			if (! asset) {
				log.warn "Missing asset id ($id) requested for page ${urlInfo(controller)} by user ${securityService.getUserLogin()}"
				controller.flash.message = "The requested asset was not found"
			}
		}
		return asset
	}

	/**
	 * Used to retrieve an Event object for a given user appropriately. If not found then the flash message will be populated with
	 * an message and a null is returned. It validates that the event is associated to the project as well and if not logs a security
	 * message and returns null.
	 * 
	 * @param controller - the controller that is calling this method
	 * @param project - the user's current project
	 * @param user - the user requesting the page
	 * @param eventId - the event id to lookup
	 * @return The move event if found otherwise null
	 */
	MoveEvent getEventForPage(controller, Project project, UserLogin user, String eventId) {
		MoveEvent event
		Long id = NumberUtil.toLong(eventId)
		if (!id) {
			log.info "getEventForPage() Invalid id ($id) from user $user"
			return null
		} 

		// handle move events
		event = MoveEvent.read(id)
		if (! event) {
			log.info "getEventForPage() was unable to find event ($id) for user $user, project ${project?.id}"
		}

		if (event && event.project != project) {
			log.error "SECURITY : getEventForPage() User $user attempted to access an event ($id) unrelated to project ${project?.id}"
			event = null
		}

		if (! event) {
			controller.flash.message = 'Unable to find specified event'
		}

		return event
	}

	/**
	 * Used to retrieve an Bundle object for a given user appropriately. If not found then the flash message will be populated with
	 * an message and a null is returned. It validates that the event is associated to the project as well and if not logs a security
	 * message and returns null.
	 * 
	 * @param controller - the controller that is calling this method
	 * @param project - the user's current project
	 * @param user - the user requesting the page
	 * @param eventId - the event id to lookup
	 * @return The move event if found otherwise null
	 */
	MoveBundle getBundleForPage(controller, Project project, UserLogin user, String bundleId) {
		MoveBundle bundle
		Long id = NumberUtil.toLong(bundleId)
		if (!id) {
			log.info "getBundleForPage() Invalid id ($id) from user $user"
			return null
		} 

		// handle move events
		bundle = MoveBundle.read(id)
		if (! bundle) {
			log.info "getBundleForPage() was unable to find bundle ($id) for user $user, project ${project?.id}"
		}

		if (bundle && bundle.project != project) {
			log.error "SECURITY : getBundleForPage() User $user attempted to access an event ($id) unrelated to project ${project?.id}"
			bundle = null
		}

		if (! bundle) {
			controller.flash.message = 'Unable to find specified bundle'
		}

		return bundle
	}

	/**
	 * Used to convert the Maint Expiration and Retire date parameters from strings to Dates
	 * @param params - the map of the params
	 */
	public void parseMaintExpDateAndRetireDate(Object params, session) {
		params.maintExpDate = TimeUtil.parseDate(session, params.maintExpDate)
		params.retireDate   = TimeUtil.parseDate(session, params.retireDate)
	}

	/**
	 * Used by the various asset controller methods to perform save/update using a standardized method names on the domain service classes
	 * @param assetServiceClass - a reference to the domain service class (e.g. databaseService)
	 * @param assetClass - the type of class that the domain is
	 * @param params - the controller parameters submitted in request
	 */
	protected void saveUpdateAssetHandler(controller, session, assetServiceClass, AssetClass assetClass, params) {
		def errorMsg=''
		def project, user
		def asset, model
		Long id = NumberUtil.toLong(params.id)
		def isNew = id == null

		parseMaintExpDateAndRetireDate(params, session)
		
		try {
			(project, user) = getProjectAndUserForPage(controller, 'AssetEdit')
			if (project) {
				if (isNew) {
					log.debug "saveUpdateAssetHandler() calling saveAssetFromForm()"
					asset = assetServiceClass.saveAssetFromForm(controller, session, project.id, user.id, params)
					log.debug "saveUpdateAssetHandler() saveAssetFromForm() returned $asset"
				} else {
					asset = getAssetForPage(controller, project, AssetClass.DEVICE, params.id)
					assetServiceClass.updateAssetFromForm(controller, session, project.id, user.id, params, asset.id)
				}
				// Reload the asset due to the hibernate session 
				//asset = AssetEntity.read(asset.id)
				asset.discard()
				model = AssetEntityHelper.simpleModelOfAsset(asset)
			} 
			errorMsg = controller.flash.message
			controller.flash.message = null
		} catch (InvalidRequestException e) {
			errorMsg = e.getMessage()
		} catch (EmptyResultException e) {
			errorMsg = e.getMessage()
		} catch (UnauthorizedException e) {
			errorMsg = e.getMessage()
		} catch (DomainUpdateException e) {
			errorMsg = e.getMessage()
		} catch (e) {
			log.error "update() failed " +  ExceptionUtil.stackTraceToString(e)
			errorMsg = "An error occurred during the update"
		}

		if ( errorMsg ) {
			// JPM 9/2014 - Not sure why we're updating the filter for JQGrid here
			session.AE?.JQ_FILTERS = params
		}

		assetEntityService.renderUpdateAssetJsonResponse(controller, model, errorMsg)
	}

	/**
	 * Returns the URL information about the current controller
	 * @param controller - the current controller 
	 * @return the URL called on the controller
	 */
	String urlInfo(Object controller) {
		return "${controller.controllerUri} ${controller.actionName}"
	}

	/**
	 * Used to redirect the user to the default web page
	 * @param controller - the controller object from where the method is being called
	 * @param flashMessage - an optional message to add to flash
	 */
	void redirectToDefaultPage(Object controller, flashMessage) {
		def userLogin = securityService.getUserLogin()

		if ( flashMessage?.size() ) {
			controller.flash.message = flashMessage
		}

		if (userLogin) {
			// Redirect to User Dashboard
			controller.redirect(controller:'dashboard', action:'userPortal')
		} else {
			// Send them to the login page
			controller.redirect(controller:'auth', action:'login')
		}
	}

	String getDefaultErrorMessage() {
		return "An error occurred. Please contact support for further assistance."
	}

	/**
	 * Check if the exception have a message code to look in the message.properties, and if not use default message
	 */
	String getExceptionMessage(Object controller, exception) {
		if ((exception instanceof ServiceException) && exception.messageCode) {
			return controller.message(code: exception.messageCode, args: exception.messageArgs)
		} else {
			return exception.getMessage()	
		}
	}
	/**
	 * Switch user project to the new project, only if the user have access to the project
	 */
	String switchContextToProject(Project newProject) {
		def project = securityService.getUserCurrentProject()
		def userLogin = securityService.getUserLogin()
		if (project.id != newProject.id) {
			if (personService.hasAccessToProject(userLogin.person, newProject)) {
				userPreferenceService.setPreference("CURR_PROJ", "${newProject.id}")
				userPreferenceService.loadPreferences("CURR_PROJ")
			} else {
				auditService.logSecurityViolation(userLogin.toString(), "Try to acccess project ${newProject.projectCode}")
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
	Object getUploadFile(Object controller, String paramName, def maxSize=0, List contentTypes=[]) {
		def localFile
		String emsg = ''
		assert paramName
		if (controller.params[paramName]) {
			while (true) {
				localFile = controller.request.getFile(paramName)
				if (! localFile) {
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
					if (! ct) {
						emsg =  "Upload file was missing required content-type"
						break
					} 
					if (! contentTypes.contains(ct)) {
						emsg =  "Content-type of the upload file was invalid"
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
	 * Used to handle uploaded image files that is restricted to content-types of
	 *    ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
	 * @param file - the File object that will be set upon success
	 * @param controller - the reference to the controller
	 * @param paramName - the request parameter name of the upload file
	 * @param maxSize - the maximum size allowed for file (default 0 - not checked)
	 * @return blank if successful otherwise an error message as to the failure
	 */
	Object getUploadImageFile(Object controller, String paramName, def maxSize=0) {
		List contentTypes = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
		return getUploadFile(controller, paramName, maxSize, contentTypes)
	}

}