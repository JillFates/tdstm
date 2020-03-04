package net.transitionmanager.manufacturer

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.model.Model
import net.transitionmanager.asset.Rack

class Manufacturer {

	def manufacturerService

	String name
	String description
	String corporateName
	String corporateLocation
	String website
	Date dateCreated
	Date lastModified

	static String alternateKey = 'name'

	static hasMany = [models: Model, racks: Rack]

	static constraints = {
		corporateLocation nullable: true
		corporateName nullable: true
		description nullable: true
		lastModified nullable: true
		name blank: false, unique: true
		website nullable: true
	}

	static mapping = {
		autoTimestamp false
		id column: 'manufacturer_id'
	}

	String toString() { name }

	def beforeInsert = {
		dateCreated = lastModified = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}

	/**
	 * The number of Models associated with this Manufacturer.
	 */
	int getModelsCount() {
		Model.countByManufacturer(this)
	}

	/**
	 * Alias records for the current manufacturer.
	 */
	List<ManufacturerAlias> getAliases() {
		ManufacturerAlias.findAllByManufacturer(this, [sort: 'name'])
	}

	/**
	 * Look for the first Manufacturer by alias name
	 * @param aliasName
	 * @return
	 */
	static Manufacturer lookupFirstAlias(String aliasName) {
		ManufacturerAlias.where {
			name == aliasName
		}.find()?.manufacturer
	}

	/**
	 * Get a ManufacturerAlias object by name and create one (optionally) if it doesn't exist
	 * @param name - name of the manufacturer alias
	 * @param createIfNotFound - optional flag to indicating if record should be created (default false)
	 * @return a ManufacturerAlias object if found or was successfully created , or null if not found or not created
	 *
	 * @deprecated use {@link net.transitionmanager.asset.ManufacturerService#findOrCreateAliasByName(Manufacturer, java.lang.String, boolean) ManufacturerService} instead.
	 */
	@Deprecated
	ManufacturerAlias findOrCreateAliasByName(String name, boolean createIfNotFound = false) {
		return manufacturerService.findOrCreateAliasByName(this, name, createIfNotFound)
	}
}
