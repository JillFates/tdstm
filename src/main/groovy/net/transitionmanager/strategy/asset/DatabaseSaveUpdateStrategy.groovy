package net.transitionmanager.strategy.asset

import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.Database
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
