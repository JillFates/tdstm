package net.transitionmanager.service

import com.tds.asset.AssetOptions
import com.tds.asset.AssetOptions.AssetOptionsType
import com.tdssrc.grails.GormUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.AssetOptionsCommand

@Slf4j
class AssetOptionsService {

	AssetOptions findById(Long id) {
		return AssetOptions.where {
			id == id
		}.get()
	}

	AssetOptions findByValue(String value) {
		return AssetOptions.where {
			value == value
		}.get()
	}

	List<AssetOptions> findAllByType(AssetOptions.AssetOptionsType assetOptionsType) {
		return AssetOptions.where {
			type == assetOptionsType
		}.order("value").list()
	}

	List<String> findAllValuesByType(AssetOptions.AssetOptionsType assetOptionsType) {
		List<AssetOptions> assetOptions = findAllByType(assetOptionsType)
		if (assetOptions) {
			return assetOptions.value
		}
		return null
	}

	@Transactional
	AssetOptions saveAssetOptions(AssetOptionsCommand command) {

		AssetOptions assetOption = new AssetOptions()
		command.populateDomain(assetOption)

		if (!assetOption.save(flush: true)) {
			log.info("Error saving AssetOption: {}", GormUtil.allErrorsString(assetOption))
		}

		return assetOption
	}

	@Transactional
	AssetOptions findOrCreate(AssetOptionsType assetOptionsType, String value) {
		AssetOptions assetOptions = AssetOptions.where {
			type == assetOptionsType
			value == value
		}.get()

		// if found, return it
		if (assetOptions) {
			return assetOptions
		}

		return saveAssetOptions(new AssetOptionsCommand(type: assetOptionsType, value: value))
	}

	@Transactional
	void deleteById(Long id) {
		AssetOptions foundAssetOptions = findById(id)
		if (foundAssetOptions) {
			foundAssetOptions.delete(flush: true)
		}
	}


}
