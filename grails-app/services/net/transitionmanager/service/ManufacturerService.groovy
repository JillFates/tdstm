package net.transitionmanager.service

import grails.transaction.Transactional
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ManufacturerAlias
import org.springframework.jdbc.core.JdbcTemplate

@Transactional
class ManufacturerService implements ServiceMethods {

	JdbcTemplate jdbcTemplate

	/**
	 *	1. Add to the AKA field list in the target record
	 *	2. Revise Model, Asset, and any other records that may point to this manufacturer
	 *	3. Delete manufacturer record.
	 *	4. Return to manufacturer list view with the flash message "Merge completed."
	 */
	void merge(String manufacturerToId, String manufacturerFromId) {

		// Get the manufacturer instances for params ids
		def toManufacturer = Manufacturer.get(manufacturerToId)
		def fromManufacturer = Manufacturer.get(manufacturerFromId)

		// Revise Model, Asset, and any other records that may point to this manufacturer
		def updateAssetsQuery = "update asset_entity set manufacturer_id = $toManufacturer.id where manufacturer_id='$fromManufacturer.id'"
		jdbcTemplate.update(updateAssetsQuery)

		def updateModelsQuery = "update model set manufacturer_id = $toManufacturer.id where manufacturer_id='$fromManufacturer.id'"
		jdbcTemplate.update(updateModelsQuery)

		def updateModelsAliasQuery = "update model_alias set manufacturer_id = $toManufacturer.id where manufacturer_id='$fromManufacturer.id'"
		jdbcTemplate.update(updateModelsAliasQuery)

		// Add alias
		def toManufacturerAlias = ManufacturerAlias.findAllByManufacturer(toManufacturer).name

		// Add to the AKA field list in the target record
		if(!toManufacturerAlias?.contains(fromManufacturer.name)){
			def fromManufacturerAlias = ManufacturerAlias.findAllByManufacturer(fromManufacturer)
			ManufacturerAlias.executeUpdate("delete from ManufacturerAlias ma where ma.manufacturer = $fromManufacturer.id")
			fromManufacturerAlias.each {
				toManufacturer.findOrCreateAliasByName(it.name, true)
			}
			//merging fromManufacturer as AKA of toManufacturer
			toManufacturer.findOrCreateAliasByName(fromManufacturer.name, true)

			// Delete manufacturer record.
			fromManufacturer.delete()
		} else {
			//	Delete manufacturer record.
			fromManufacturer.delete()
		}
	}
	
	/**
	 * Validates whether the given alias is valid for the given manufacturer
	 * @param newAlias, the alias to be added
	 * @param manufacturer, the manufacturer this alias is being applied to
	 * @param allowLocalDuplicates, if true, the alias will not be checked against this manufacturer's current aliases
	 * @param manufacturerName, if given, the alias will validated using this name instead of the given manufacturer's current name
	 * @return true if the alias is valid for the given parameters
	 */
	boolean isValidAlias (String newAlias, Manufacturer manufacturer, boolean allowLocalDuplicates = false, String manufacturerName = null) {
		// if there wasn't enough information supplied 
		if (!manufacturer && !manufacturerName)
			return false
		
		// get the manufacturer's name if an alternative wasn't given
		manufacturerName = (manufacturerName == null) ? (manufacturer.name) : (manufacturerName)
		
		// check if the alias matches the manufacturer name
		if (newAlias == manufacturerName)
			return false
		
		// check if there is another manufacturer with this alias as their name 
		def manufacturersWithName = Manufacturer.createCriteria().list {
			eq('name', newAlias)
			if (manufacturer)
				ne('name', manufacturer.name)
		}
		
		if (manufacturersWithName)
			return false
		
		// check if there is a manufacturer already using this alias
		def manufacturersWithAlias = ManufacturerAlias.createCriteria().list {
			eq('name', newAlias)
			if (allowLocalDuplicates && manufacturer)
				ne('manufacturer', manufacturer)
		}
		
		if (manufacturersWithAlias)
			return false
		
		
		// if all the tests were passes, this is a valid alias
		return true
	}
}
