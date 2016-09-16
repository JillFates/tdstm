import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.servlet.ServletContext
import javax.servlet.http.HttpSession

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.InitializingBean

@CompileStatic
@Slf4j
class MaintService implements InitializingBean {

	private static String MAINT_MODE_FILE_PATH = ''
	private static final String MAINT_BACKDOOR_ENTRY = 'MAINT_BACKDOOR_ENTRY'

	static boolean inMaintMode = false

	ServletContext servletContext
	GrailsApplication grailsApplication

	@CompileDynamic
	void afterPropertiesSet() {
		MAINT_MODE_FILE_PATH = grailsApplication.config.tdsops.maintModeFile
	}

	/**
	 * Check if the Maintenance Mode flag file exists.
	 */
	void checkForMaintFile() {
		//Check for resource availability at given path.
		def resource = servletContext.getResource(MaintService.MAINT_MODE_FILE_PATH)
		if (resource) {
			if (!inMaintMode) {
				inMaintMode = true
				log.info 'Maintenance Mode was enabled @ {}', new Date()
			}
		} else {
			if (inMaintMode) {
				inMaintMode = false
				log.info 'Maintenance Mode was disabled @ {}', new Date()
			}
		}
	}

	/**
	 * Allows a user to make a backdoor entry to indicate that the application
	 * is in maintenance mode.
	 */
	void toggleUsersBackdoor(HttpSession session) {
		def hasAccess = session.getAttribute(MAINT_BACKDOOR_ENTRY)
		if (!hasAccess) {
			session.setAttribute(MAINT_BACKDOOR_ENTRY, true)
			log.info 'User allowed for back door access @ {}', new Date()
		}
	}///////////////////////////

	/**
	 * Returns true if the backdoor access flag is set.
	 */
	boolean hasBackdoorAccess(HttpSession session) {
		session.getAttribute(MAINT_BACKDOOR_ENTRY) ?: false
	}
}
