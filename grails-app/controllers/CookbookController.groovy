import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.UserPreferenceService
import grails.plugin.springsecurity.annotation.Secured

@Secured('isAuthenticated()')
class CookbookController implements ControllerMethods {

	UserPreferenceService userPreferenceService

	def index() {
		licenseAdminService.checkValidForLicense()
		[userPreferenceService: userPreferenceService]
	}
}
