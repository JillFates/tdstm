import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.bulk.BulkChangeCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.BulkAssetChangeService

@Secured('isAuthenticated()')
class WsBulkAssetChangeController implements ControllerMethods {
	BulkAssetChangeService bulkAssetChangeService

	/**
	 * This action handles bulk changes delegating to the bulkAssetChangeService
	 *
	 * @return a success JSON
	 */
	@HasPermission(Permission.AssetEdit)
	@HasPermission(Permission.TagDelete)
	def change() {
		BulkChangeCommand bulkChange = populateCommandObject(BulkChangeCommand)
		validateCommandObject(bulkChange)
		bulkAssetChangeService.bulkChange(projectForWs, bulkChange)

		renderSuccessJson()
	}

	//TODO Should be removed, once changes are made to the ui
	@Deprecated
	def fields() {
		renderSuccessJson(BulkAssetChangeService.fields)
	}

	//TODO Should be removed, once changes are made to the ui
	@Deprecated
	def actions() {
		renderSuccessJson(BulkAssetChangeService.actions)
	}
}
