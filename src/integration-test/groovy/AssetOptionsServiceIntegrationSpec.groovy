import net.transitionmanager.asset.AssetOptions
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.AssetOptionsCommand
import net.transitionmanager.service.AssetOptionsService
import spock.lang.Specification

import static net.transitionmanager.asset.AssetOptions.*

@Integration
@Rollback
class AssetOptionsServiceIntegrationSpec extends Specification {

	AssetOptionsService assetOptionsService

	void '1. Test save assetOption'() {
		when: 'Saving a new AssetOption'
			AssetOptionsCommand command = new AssetOptionsCommand(type: AssetOptionsType.ASSET_TYPE, value: 'asset test')
			AssetOptions assetOptions = assetOptionsService.saveAssetOptions(command)
		then: 'AssetOption is saved to db'
			assetOptions
			assetOptions.id
			assetOptions.type == AssetOptionsType.ASSET_TYPE
			assetOptions.value == 'asset test'
	}

	void '2. Test find or create assetOption'() {
		when: 'Saving a new AssetOption'
			AssetOptions assetOptions1 = assetOptionsService.findOrCreate(AssetOptionsType.ASSET_TYPE, 'asset test')
		then: 'AssetOption is saved to db'
			assetOptions1
			assetOptions1.id
			assetOptions1.type == AssetOptionsType.ASSET_TYPE
			assetOptions1.value == 'asset test'
		when: 'Saving a new AssetOption but it already exists in db'
			AssetOptions assetOptions2 = assetOptionsService.findOrCreate(AssetOptionsType.ASSET_TYPE, 'asset test')
		then: 'AssetOption is returned'
			assetOptions1
			assetOptions1.id
			assetOptions1.type == AssetOptionsType.ASSET_TYPE
			assetOptions1.value == 'asset test'
		and: 'First AssetOption is the same as the second asset option'
			assetOptions1.id == assetOptions2.id
			assetOptions1.type == assetOptions2.type
			assetOptions1.value == assetOptions2.value
	}

	void '3. Test delete assetOption'() {
		setup: 'given an AssetOption'
			AssetOptionsCommand command = new AssetOptionsCommand(type: AssetOptionsType.ASSET_TYPE, value: 'asset test')
			AssetOptions assetOptions = assetOptionsService.saveAssetOptions(command)
		when: 'Deleting an AssetOption from db'
			assetOptionsService.deleteById(assetOptions.id)
		then: 'AssetOption is deleted from db'
			null == assetOptionsService.findById(assetOptions.id)
	}

	void '4. Test find all assetOptions'() {
		setup: 'given a set of AssetOption'
			assetOptionsService.saveAssetOptions(new AssetOptionsCommand(type: AssetOptionsType.ASSET_TYPE, value: 'asset test'))
			assetOptionsService.saveAssetOptions(new AssetOptionsCommand(type: AssetOptionsType.DEPENDENCY_STATUS, value: 'asset test 2'))
		when: 'finding all asset options by type'
			List<AssetOptions> assetOptions = assetOptionsService.findAllByType(AssetOptionsType.ASSET_TYPE)
		then: 'AssetOption list by type is retrieved from db'
			assetOptions
			assetOptions.collect { it.type == AssetOptionsType.ASSET_TYPE }
		when: 'finding all asset options values by type'
			List<String> assetOptionsValues = assetOptionsService.findAllValuesByType(AssetOptionsType.DEPENDENCY_STATUS)
		then: 'AssetOptions values by type is retrieved from db'
			assetOptionsValues
			assetOptionsValues.contains('asset test 2')
	}

}
