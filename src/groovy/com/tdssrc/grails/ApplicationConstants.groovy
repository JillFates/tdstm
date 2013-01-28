package com.tdssrc.grails

class ApplicationConstants {
	
	// these are default global map contains planning console map details e.g. height , width .
	
	// Map for small graph which have less than 30 entities.
	static def graphDefaultSmallMap = ["force":-110, "linkdistance":90, "width":800, "height":400]
	
	// Map for medium graph which have greater than 30 entities and less than 200 entities.
	static def graphDefaultMediumMap = ["force":-100, "linkdistance":80, "width":1200, "height":600]
	
	// Map for medium graph which have greater than 200 entities .
	static def graphDefaultLargeMap = ["force":-70, "linkdistance":40, "width":1800, "height":2200]
	
	// Map for planning dashboard filters to asset list.
	static def assetFilters = [All:['Server','VM','Blade'], physical:['Server','Blade'], virtual:['VM'],
								other:['Server','VM','Blade','Application','Database','Files']]

}
