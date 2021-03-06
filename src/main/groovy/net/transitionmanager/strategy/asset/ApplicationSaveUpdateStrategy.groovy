package net.transitionmanager.strategy.asset

import com.tdsops.common.grails.ApplicationContextHolder
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetType
import net.transitionmanager.command.AssetCommand
import net.transitionmanager.person.Person
import net.transitionmanager.person.PersonService

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
		application.sme = personService.getPerson(project, command.asset.sme?.id)
		application.appOwner = personService.getPerson(project, command.asset.appOwner?.id)
		application.sme2 = personService.getPerson(project, command.asset.sme2?.id)
		application.shutdownFixed = command.asset.shutdownFixed ?  1 : 0
		application.startupFixed = command.asset.startupFixed ?  1 : 0
		application.testingFixed = command.asset.testingFixed ?  1 : 0
	}


	/**
	 * Return the person service instance in the container.
	 * @return
	 */
	static PersonService getPersonService() {
		return ApplicationContextHolder.getBean('personService')
	}
}
