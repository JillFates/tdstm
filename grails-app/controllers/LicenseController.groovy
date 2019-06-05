import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.LicenseAdminService

@Secured('isAuthenticated()')
@Slf4j()
class LicenseController implements ControllerMethods {

	// IOC
	LicenseAdminService licenseAdminService

	/**
	 * Returns the licensing information for the project (id) or the user's current project if null
	 * @param id - the project id to get licensing for (defaults to user's current id)
	 * @return JSON structure
	 */
	def info() {
		println "in info() "
		Project project = getProjectForWs()
		Map licenseInfo = licenseAdminService.licenseInfo(project)

		renderSuccessJson(license: licenseInfo)
	}
}