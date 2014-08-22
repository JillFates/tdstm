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
	 * Used to get the user default project for a page request
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

}
