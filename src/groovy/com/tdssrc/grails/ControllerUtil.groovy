package com.tdssrc.grails

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.tdsops.common.grails.ApplicationContextHolder
import javax.servlet.http.HttpSession

/**
 * A set of methods used to support common functionality used in the Controllers
 */
@Singleton
class ControllerUtil {

	private static log
	private static securityService


	/**
	 * Constructor
	 */
	ControllerUtil() {
		log = LogFactory.getLog(this.class)
		securityService = ApplicationContextHolder.getBean('securityService')
	}

	/**
	 * Used to get the user default project for a page request or redirects user to the Project List view
	 * @param controller - a reference to the controller
	 * @return The user's currently selected Project object if selected otherwise null
	 */
	static Object getProjectForPage(Object controller) {

		def project = securityService.getUserCurrentProject();
		if (! project) {
			log.info "User ${securityService.getUserLogin()} didn't have default project"
			controller.flash.message = "Please select a project before continuing"
			controller.redirect(controller:'project', action:'list')
		}

		return project
	}

	/**
	 * Used to load the Asset specified for the given asset class and id
	 * @param controller - a reference to the controller object
	 * @param project - the user's current project
	 * @param assetClass - the class of object to return (e.g. AssetEntity, Application, Database, etc)
	 * @param id - the id of the asset to be retrieved
	 * @param readOnly - will controll if the fetch uses read or get for a transaction (default true)
	 * @return an asset object if it is found and associated with the given project
	 */
	static Object getAssetForPage(Object controller, Project project, Object assetClass, String id, Boolean readOnly=true) {
		def asset
		if (! id.isInteger()) {
			log.warn "Invalid asset id ($id) requested for page ${urlInfo(controller)} by user ${securityService.getUserLogin()}"
			controller.flash.message = "The requested asset id is invalid"
		} else {
			asset = (readOnly ? assetClass.read(id) : assetClass.get(id))
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
	 * Returns the URL information about the current controller
	 * @param controller - the current controller 
	 * @return the URL called on the controller
	 */
	static String urlInfo(Object controller) {
		return "${controller.controllerUri} ${controller.actionName}"
	}

	/**
	 * Used to redirect the user to the default web page
	 * @param controller - the controller object from where the method is being called
	 * @param flashMessage - an optional message to add to flash
	 */
	static void redirectToDefaultPage(Object controller, flashMessage) {
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
