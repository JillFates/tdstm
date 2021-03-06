package net.transitionmanager.command

import com.tdsops.tm.enums.domain.AssetClass


/**
 * Command Object to be used for when saving/updating assets.
 */

class AssetCommand implements CommandObject{

	/**
	 * The class of the asset being created/updated.
	 */
	AssetClass assetClass

	/**
	 * The 'form' data. This is defined as generic as possible to
	 * be able to reuse it for all the different assets.
	 */
	Map asset

	/**
	 * Map with the dependency information
	 */
	Map dependencyMap

	static constraints = {
		assetClass nullable: false
		asset nullable: false
		dependencyMap nullable: false

	}
}
