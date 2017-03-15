import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.UserPreferenceService
import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured

@Secured('isAuthenticated()')
class CookbookController implements ControllerMethods {

	UserPreferenceService userPreferenceService

	@HasPermission('CookbookView')
	def index() {
		licenseAdminService.checkValidForLicense()
		[userPreferenceService: userPreferenceService]
	}
}
