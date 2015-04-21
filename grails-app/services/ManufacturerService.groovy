class ManufacturerService {

	def jdbcTemplate
	def sessionFactory

	/**
	 *	1. Add to the AKA field list in the target record
	 *	2. Revise Model, Asset, and any other records that may point to this manufacturer
	 *	3. Delete manufacturer record.
	 *	4. Return to manufacturer list view with the flash message "Merge completed."
	 */
	def merge(manufacturerToId, manufacturerFromId) {

		// Get the manufacturer instances for params ids
		def toManufacturer = Manufacturer.get(manufacturerToId)
		def fromManufacturer = Manufacturer.get(manufacturerFromId)
		
		// Revise Model, Asset, and any other records that may point to this manufacturer
		def updateAssetsQuery = "update asset_entity set manufacturer_id = ${toManufacturer.id} where manufacturer_id='${fromManufacturer.id}'"
		jdbcTemplate.update(updateAssetsQuery)
		
		def updateModelsQuery = "update model set manufacturer_id = ${toManufacturer.id} where manufacturer_id='${fromManufacturer.id}'"
		jdbcTemplate.update(updateModelsQuery)

		def updateModelsAliasQuery = "update model_alias set manufacturer_id = ${toManufacturer.id} where manufacturer_id='${fromManufacturer.id}'"
		jdbcTemplate.update(updateModelsAliasQuery)

		// Add alias
		def toManufacturerAlias = ManufacturerAlias.findAllByManufacturer(toManufacturer).name
		
		// Add to the AKA field list in the target record
		if(!toManufacturerAlias?.contains(fromManufacturer.name)){
			def fromManufacturerAlias = ManufacturerAlias.findAllByManufacturer(fromManufacturer)
			ManufacturerAlias.executeUpdate("delete from ManufacturerAlias ma where ma.manufacturer = ${fromManufacturer.id}")
			fromManufacturerAlias.each{
				toManufacturer.findOrCreateAliasByName(it.name, true)
			}
			//merging fromManufacturer as AKA of toManufacturer
			toManufacturer.findOrCreateAliasByName(fromManufacturer.name, true)
			
			// Delete manufacturer record.
			fromManufacturer.delete()
		} else {
			//	Delete manufacturer record.
			fromManufacturer.delete()
			sessionFactory.getCurrentSession().flush();
		}
	}

}



