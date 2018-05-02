package net.transitionmanager.command

import com.tdsops.tm.enums.domain.AssetClass
import grails.validation.Validateable

/**
 * Command Object to be used for when saving/updating assets.
 */
@Validateable
class AssetCommand {

	/**
	 * The class of the asset being created/updated.
	 */
	AssetClass assetClass

	/**
	 * The 'form' data. This is defined as generic as possible to
	 * be able to reuse it for all the different assets.
	 */
	Map data

	static constraints = {
		assetClass nullable: false
		data nullable: false
	}
}
