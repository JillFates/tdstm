package net.transitionmanager.service

import com.tds.asset.AssetOptions
import com.tds.asset.AssetOptions.AssetOptionsType
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.AssetOptionsCommand

@Slf4j
@Transactional
class AssetOptionsService {

	/**
	 * Find asset option by id
	 * @param id - asset option id
	 * @return
	 */
	AssetOptions findById(Long id) {
		return AssetOptions.where {
			id == id
		}.get()
	}

	/**
	 * Find asset option by value
	 * @param value - asset option value
	 * @return
	 */
	AssetOptions findByValue(String value) {
		return AssetOptions.where {
			value == value
		}.get()
	}

	/**
	 * Find all asset options by type
	 * @param assetOptionsType - asset option type
	 * @return
	 */
	List<AssetOptions> findAllByType(AssetOptionsType assetOptionsType) {
		return AssetOptions.where {
			type == assetOptionsType
		}.order("value").list()
	}

	/**
	 * Find all asset option values by type
	 * @param assetOptionsType - asset option type
	 * @return
	 */
	List<String> findAllValuesByType(AssetOptionsType assetOptionsType) {
		List<AssetOptions> assetOptions = findAllByType(assetOptionsType)
		if (assetOptions) {
			return assetOptions.value
		}
		return null
	}

	/**
	 * Save an asset option
	 * @param command
	 * @return
	 */
	@Transactional
	AssetOptions saveAssetOptions(AssetOptionsCommand command) {

		AssetOptions assetOption = new AssetOptions()
		command.populateDomain(assetOption, false, ['constraintsMap'])

		assetOption.save(flush: true)

		return assetOption
	}

	/**
	 * Find or create an asset option
	 * @param assetOptionsType
	 * @param value
	 * @return
	 */
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

	/**
	 * Delete an asset option
	 * @param id
	 */
	@Transactional
	void deleteById(Long id) {
		AssetOptions foundAssetOptions = findById(id)
		if (foundAssetOptions) {
			foundAssetOptions.delete(flush: true)
		}
	}

}
