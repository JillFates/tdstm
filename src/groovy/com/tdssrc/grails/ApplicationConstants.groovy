package com.tdssrc.grails

import com.tds.asset.AssetType
import groovy.transform.CompileStatic
import groovy.transform.Immutable

@CompileStatic
@Immutable
class ApplicationConstants {

	// these are default global map contains planning console map details e.g. height , width .

	// Map for small graph which have less than 30 entities.
	static final Map<String, Integer> graphDefaultSmallMap = [force: -110, linkdistance: 90, width: 800, height: 400]

	// Map for medium graph which have greater than 30 entities and less than 200 entities.
	static final Map<String, Integer> graphDefaultMediumMap = [force: -100, linkdistance: 80, width: 1200, height: 600]

	// Map for medium graph which have greater than 200 entities .
	static final Map<String, Integer> graphDefaultLargeMap = [force: -70, linkdistance: 40, width: 1800, height: 2200]

	// Map for planning dashboard filters to asset list.
	static final Map<String, List<String>> assetFilters = [
		All:      AssetType.allServerTypes,
		physical: AssetType.physicalServerTypes,
		virtual:  AssetType.virtualServerTypes,
		other:    AssetType.allServerTypes,
		storage:  AssetType.storageTypes
	]
}
