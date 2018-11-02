package net.transitionmanager.command.partygroup

import grails.validation.Validateable
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.command.bulk.EditCommand

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
class ListCommand {

	String sidx ='companyName'
	String sord = 'asc'
	Integer rows = 25
	Integer page = 1
	String companyName
	String dateCreated
	String lastUpdated
	String partner

	static constraints = {
		sord inList: ['asc', 'desc']
	}
}
