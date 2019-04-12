package net.transitionmanager.strategy.asset

import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.asset.Database
import net.transitionmanager.command.AssetCommand

class DatabaseSaveUpdateStrategy extends AssetSaveUpdateStrategy{

	/**
	 * Constructor that takes a command as argument.
	 * @param assetCommand
	 */
	DatabaseSaveUpdateStrategy(AssetCommand assetCommand) {
		super(assetCommand)
	}

	/**
	 * Return a new Database instance.
	 * @return
	 */
	@Override
	AssetEntity createAssetInstance() {
		return new Database()
	}

	/**
	 * Initialize the Asset Entity instance.
	 * @param assetEntity
	 */
	@Override
	protected void initializeInstance(AssetEntity assetEntity) {
		super.initializeInstance(assetEntity)
		assetEntity.assetType = AssetType.DATABASE
	}
}
