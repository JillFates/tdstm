package net.transitionmanager.strategy.asset

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import net.transitionmanager.command.AssetCommand

class ApplicationSaveUpdateStrategy extends AssetSaveUpdateStrategy{

	/**
	 * Constructor that takes a command as argument.
	 * @param assetCommand
	 */
	ApplicationSaveUpdateStrategy(AssetCommand assetCommand) {
		super(assetCommand)
	}

	/**
	 * Return a new Application.
	 * @return
	 */
	@Override
	AssetEntity createAssetInstance() {
		return new Application()
	}

	/**
	 * Initialize the Asset Entity instance.
	 * @param assetEntity
	 */
	@Override
	protected void initializeInstance(AssetEntity assetEntity) {
		super.initializeInstance(assetEntity)
		assetEntity.assetType = AssetType.APPLICATION
	}

	/**
	 * Populate the Application instance with data from the command object.
	 * @param assetEntity
	 */
	@Override
	protected void populateAsset(AssetEntity assetEntity) {
		super.populateAsset(assetEntity)
		Application application = (Application) assetEntity
		application.shutdownFixed = command.data.shutdownFixed ?  1 : 0
		application.startupFixed = command.data.startupFixed ?  1 : 0
		application.testingFixed = command.data.testingFixed ?  1 : 0
	}
}
