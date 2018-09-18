package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.common.lang.CollectionUtils
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ManufacturerAlias
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
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
				//toManufacturer.findOrCreateAliasByName(it.name, true)
				findOrCreateAliasByName(toManufacturer, it.trim(), true)
			}
			//merging fromManufacturer as AKA of toManufacturer
			//toManufacturer.findOrCreateAliasByName(fromManufacturer.name, true)
			findOrCreateAliasByName(toManufacturer, fromManufacturer.name, true)

			// Delete manufacturer record.
			fromManufacturer.delete()
		} else {
			//	Delete manufacturer record.
			fromManufacturer.delete()
		}
	}

	/**
	 * 1. Manufacturer name must be unique
	 * 2. Manufacturer name must not duplicate any AKA name within all manufacturers
	 *
	 * @param name
	 * @param id
	 * @return
	 */
	boolean isValidName(String name, Long id) {
		// rule #1
		int count = Manufacturer.where {
			name == name
			if (id) {
				id != id
			}
		}.count()

		if (count == 0) {
			// rule #2
			count = ManufacturerAlias.where {
				name == name
			}.count()

			if (count == 0) {
				return true
			} else {
				String error = "Manufacturer name (${name}) duplicates an existing AKA."
				throw new ServiceException(error)
			}

		} else {
			String error = "Manufacturer name (${name}) is not unique."
			throw new ServiceException(error)
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

	boolean save(Manufacturer manufacturer, List<String> akaNames) {
		if (isValidName(manufacturer.name, manufacturer.id) && !manufacturer.hasErrors() && manufacturer.save()) {
			if (CollectionUtils.isNotEmpty(akaNames) && !(akaNames.size() == 1 && StringUtil.isBlank(akaNames[0]))) {
				akaNames.each { aka ->
					findOrCreateAliasByName(manufacturer, aka, true)
				}
			}
			return true
		} else {
			return false
		}
	}

	boolean update(Manufacturer manufacturer, String deletedAka, List<String> akaToSave, Map<String, String> akaToUpdate) {
		if (deletedAka) {
			List<Long> maIds = deletedAka.split(",").collect() { it as Long }
			ManufacturerAlias.executeUpdate("delete from ManufacturerAlias ma where ma.id in :maIds", [maIds: maIds])
		}
		def manufacturerAliasList = ManufacturerAlias.findAllByManufacturer(manufacturer)
		manufacturerAliasList.each { manufacturerAlias ->
			manufacturerAlias.name = akaToUpdate["aka_${manufacturerAlias.id}"]
			manufacturerAlias.save(flush:true)
		}
		akaToSave.each { aka ->
			findOrCreateAliasByName(manufacturer, aka, true)
		}
		return isValidName(manufacturer.name, manufacturer.id) && !manufacturer.hasErrors() && manufacturer.save()
	}

	/**
	 * Deletes a Manufacturer and all associated entities.
	 * This method will delete the given manufacturer, plus:
	 * - The associated manufacturer aliases (if any)
	 * - The associated models and model aliases (if any)
	 * Finally it will update the manufacturer and model references in all asset entities to be null.
	 *
	 * @param manufacturer  The manufacturer to be deleted
	 */
	void delete(Manufacturer manufacturer) {
		// delete existing aliases
		ManufacturerAlias.executeUpdate("delete from ManufacturerAlias ma where ma.manufacturer.id = :maId", [maId: manufacturer.id])
		ModelAlias.executeUpdate("delete from ModelAlias ma where ma.manufacturer.id = :maId", [maId: manufacturer.id])
		// update Manufacturer references in all assets to null
		AssetEntity.executeUpdate("update AssetEntity ae set ae.manufacturer=null where ae.manufacturer.id = :maId", [maId: manufacturer.id])
		//Find the associated Models to this manufacturer
		List<Long> modelIds =
				Model.where {
					manufacturer == manufacturer
				}
				.projections { property 'id' }
						.list()
		// update Model references in all assets to null
		AssetEntity.executeUpdate("update AssetEntity ae set ae.model=null where ae.model.id in (:moId)", [moId: modelIds])
		// finally, delete the Manufacturer
		manufacturer.delete(flush:true) // Note that here associated models will also be deleted by cascade
	}

	/**
	 * Get a ManufacturerAlias object by name and create one (optionally) if it doesn't exist
	 * @param name - name of the manufacturer alias
	 * @param createIfNotFound - optional flag to indicating if record should be created (default false)
	 * @return a ManufacturerAlias object if found or was successfully created , or null if not found or not created
	 */
	ManufacturerAlias findOrCreateAliasByName(Manufacturer manufacturer, String name, boolean createIfNotFound = false) {
		ManufacturerAlias alias = ManufacturerAlias.findByNameAndManufacturer(name, manufacturer)
		if (!alias && createIfNotFound) {
			def isValid = isValidAlias(name, manufacturer, false)
			alias = new ManufacturerAlias(name: name.trim(), manufacturer: manufacturer)
			if (!isValid || !alias.save(flush: true)) {
//				log.error GormUtil.allErrorsString(alias)
//				return null
				throw new ServiceException("AKA or Manufacturer with same name already exists: ${name}")
			}
		}
		return alias
	}

}
