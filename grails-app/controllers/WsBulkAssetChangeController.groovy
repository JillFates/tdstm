import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.bulk.BulkChangeCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.BulkAssetChangeService

@Secured('isAuthenticated()')
class WsBulkAssetChangeController implements ControllerMethods {
	BulkAssetChangeService bulkAssetChangeService

	@HasPermission(Permission.AssetEdit)//TODO might want a bulk change permission?
	@HasPermission(Permission.TagDelete)
	def change() {
		BulkChangeCommand bulkChange = populateCommandObject(BulkChangeCommand)
		validateCommandObject(bulkChange)
		bulkAssetChangeService.bulkChange(projectForWs, bulkChange)

		renderSuccessJson()
	}
}
