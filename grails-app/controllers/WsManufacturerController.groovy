import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.ManufacturerService
/**
 * Handles WS calls of the ManufacturerService.
 *
 * @author Diego Scarpa
 */
@Secured('isAuthenticated()')
class WsManufacturerController implements ControllerMethods {

	ControllerService controllerService
	ManufacturerService manufacturerService

	/**
	 * Merge to manufacturers
	 */
	@HasPermission(Permission.ManufacturerMerge)
	def merge() {
		controllerService.checkPermissionForWS(Permission.ModelEdit)
		manufacturerService.merge(params.id, params.fromId)
		renderSuccessJson()
	}
}
