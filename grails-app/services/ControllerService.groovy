import com.tdssrc.grails.NumberUtil

import javax.servlet.http.HttpSession
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A set of methods used to support common functionality used in the Controllers
 */
class ControllerService {

	static transactional=false

	def securityService

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
				redirectToDefaultPage(controller, "Sorry but you are unauthorized to perform that action")
				project = null
			}
		}

		return project
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
	Object getAssetForPage(Object controller, Project project, Object assetClass, String id) {
		def asset

		if (! id.isInteger()) {
			log.warn "Invalid asset id ($id) requested for page ${urlInfo(controller)} by user ${securityService.getUserLogin()}"
			controller.flash.message = "The requested asset id is invalid"
		} else {
			asset = assetClass.read(id)
			if (! asset) {
				log.warn "Missing asset id ($id) requested for page ${urlInfo(controller)} by user ${securityService.getUserLogin()}"
				controller.flash.message = "The requested asset was not found"
			} else if ( ! asset.project.equals(project)) {
				log.warn "SECURITY : referenced asset ($id) not associated to user project for page ${urlInfo(controller)} by user ${securityService.getUserLogin()}"
				controller.flash.message = "The requested asset was not found"
				asset = null				
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
			event = null
		}

		if (! bundle) {
			controller.flash.message = 'Unable to find specified bundle'
		}

		return bundle
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

}