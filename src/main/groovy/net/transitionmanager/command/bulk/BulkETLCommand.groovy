package net.transitionmanager.command.bulk


import net.transitionmanager.command.CommandObject
import net.transitionmanager.command.dataview.DataviewUserParamsCommand

/**
 * Represents Bulk ETL change parameters.
 *
 * @param userParams The command object that holds the filters for assets.
 * @param dataViewId The data view id related to the filtering of assets.
 * @param dataScriptId The ETL datascript to run on the assets.
 * @param ids Optional asset Ids to to run the bulk changes on. If not set the allAssets flag, userParams, and dataviewId must be set
 * @param allAssets A flag to determine if the bulk change should be run against all assets filtered by the userParams.
 * @param sendEmail A flag to determine if the builk ETL should send an email notification.
 */
class BulkETLCommand implements CommandObject {

	DataviewUserParamsCommand userParams
	Long                      dataViewId
	Long                      dataScriptId
	List<Long>                ids
	Boolean                   allIds    = false
	Boolean                   sendEmail = false

	static constraints = {
		userParams cascade: true, nullable: true
		dataViewId nullable: true

		ids nullable: true, validator: { ids, command ->
			if (!ids && !(command.allIds && command.dataViewId && command.userParams)) {
				return 'default.empty.ids.list.message'
			}
		}
	}
}
