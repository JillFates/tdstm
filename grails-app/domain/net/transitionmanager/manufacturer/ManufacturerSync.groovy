package net.transitionmanager.manufacturer

import net.transitionmanager.model.ModelSyncBatch

class ManufacturerSync {

	String         name
	String         description
	String         corporateName
	String         corporateLocation
	String         website
	String         aka
	String         importStatus
	ModelSyncBatch batch
	long           manufacturerTempId

	static constraints = {
		aka nullable: true
		corporateLocation nullable: true
		corporateName nullable: true
		description nullable: true
		importStatus nullable: true
		name blank: false
		website nullable: true
	}

	static mapping = {
		version false
		id column: 'manufacturer_id'
	}

	String toString() { name }
}
