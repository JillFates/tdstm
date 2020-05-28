package net.transitionmanager.asset

import grails.gorm.DetachedCriteria
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.AssetOptions.AssetOptionsType
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
	 *
	 * @param assetOptionsType - asset option type
	 * @param sort - if the list of asset options should be sorted by value. Defaults to true.
	 *
	 * @return A list of AssetOptions for a AssetOptionsType.
	 */
	List<AssetOptions> findAllByType(AssetOptionsType assetOptionsType, boolean sort = true) {
		DetachedCriteria options = AssetOptions.where {
			type == assetOptionsType
		}

		if (sort) {
			return options.order("value").list()
		} else {
			return options.list()
		}
	}

	/**
	 * Gets the list of task categories as a list of Strings.
	 *
	 * @return a list of the task categories from the asset options.
	 */
	List<String> taskCategories(){
		findAllByType(AssetOptions.AssetOptionsType.TASK_CATEGORY)*.value
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
