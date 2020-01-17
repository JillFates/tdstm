package net.transitionmanager.asset

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.asset.BulkAssetChangeService
import net.transitionmanager.command.bulk.BulkChangeCommand
import net.transitionmanager.command.bulk.BulkETLCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission

@Secured('isAuthenticated()')
class WsBulkAssetChangeController implements ControllerMethods {
	BulkAssetChangeService bulkAssetChangeService
	BulkAssetETLService    bulkAssetETLService

	/**
	 * This action handles bulk changes delegating to the bulkAssetChangeService
	 *
	 * @return a success JSON
	 */
	@HasPermission(Permission.AssetEdit)
	def change() {
		BulkChangeCommand bulkChange = populateCommandObject(BulkChangeCommand)
		bulkAssetChangeService.bulkChange(projectForWs, bulkChange)

		renderSuccessJson()
	}

	/**
	 * This action handle running bulk etl script on assets delegating to the bulkAssetETLService.
	 */
	@HasPermission(Permission.AssetEdit)
	def runETL() {
		BulkETLCommand bulkETL = populateCommandObject(BulkETLCommand)
		renderSuccessJson(bulkAssetETLService.runBulkETL(projectForWs, bulkETL))
	}
}
