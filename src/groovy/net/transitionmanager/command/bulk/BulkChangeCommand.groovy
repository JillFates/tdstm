package net.transitionmanager.command.bulk

import grails.validation.Validateable
import net.transitionmanager.command.DataviewUserParamsCommand

/**
 * Represents a list of bulk changes.
 *
 * @param userParams The command object that holds the filters for assets.
 * @param dataViewId The data view id related to the filtering of assets.
 * @param edits A list of bulk change edits to be delegated and run.
 * @param assetIds Optional asset Ids to to run the bulk changes on. If not set the allAssets flag, userParams, and dataviewId must be set
 * @param allAssets A flag to determine if the bulk change should be run against all assets filtered by the userParams.
 */
@Validateable
class BulkChangeCommand {

	DataviewUserParamsCommand userParams
	Long                      dataViewId
	List<EditCommand>         edits
	List<Long>                assetIds
	Boolean                   allAssets = false

	static constraints = {
		userParams cascade: true, nullable:true
		dataViewId nullable: true
		edits cascade: true
		assetIds nullable: true, validator: { assetIds, command ->
			if (!assetIds && !(command.allAssets && command.dataViewId && command.userParams)) {
				return 'code for assets need to be specified or all assets'
			}
		}
	}
}
