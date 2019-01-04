package net.transitionmanager.strategy.asset

import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.Files
import net.transitionmanager.command.AssetCommand

class StorageSaveUpdateStrategy extends AssetSaveUpdateStrategy{

	/**
	 * Constructor that takes a command as argument.
	 * @param assetCommand
	 */
	StorageSaveUpdateStrategy(AssetCommand assetCommand) {
		super(assetCommand)
	}

	/**
	 * Create a new File
	 * @return
	 */
	@Override
	AssetEntity createAssetInstance() {
		return new Files()
	}

	/**
	 * Initialize the Asset Entity instance.
	 * @param assetEntity
	 */
	@Override
	protected void initializeInstance(AssetEntity assetEntity) {
		super.initializeInstance(assetEntity)
		assetEntity.assetType = AssetType.STORAGE
	}
}
