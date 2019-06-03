package net.transitionmanager.command.bulk

import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.command.CommandObject
import net.transitionmanager.command.dataview.DataviewUserParamsCommand

/**
 * Represents a list of bulk changes.
 *
 * @param userParams The command object that holds the filters for assets.
 * @param dataViewId The data view id related to the filtering of assets.
 * @param edits A list of bulk change edits to be delegated and run.
 * @param assetIds Optional asset Ids to to run the bulk changes on. If not set the allAssets flag, userParams, and dataviewId must be set
 * @param allAssets A flag to determine if the bulk change should be run against all assets filtered by the userParams.
 */

class BulkChangeCommand implements CommandObject{

	DataviewUserParamsCommand userParams
	Long                      dataViewId
	List<EditCommand>         edits
	List<Long>                ids
	Boolean                   allIds = false
	String                    type

	static constraints = {
		userParams cascade: true, nullable:true
		dataViewId nullable: true
		edits cascade: true
		// The type will be each of the Asset Class names + COMMON to allow supporting changing multiple class types in one request
		type inList: AssetClass.values()*.name() + ['COMMON']
		ids nullable: true, validator: { ids, command ->
			if (!ids && !(command.allIds && command.dataViewId && command.userParams)) {
				return 'default.empty.ids.list.message'
			}
		}
	}
}
