package com.tdsops.etl

/**
 * Delegate Boolean class used in an ETL script as the following script:
 * <pre>
 *  lookup assetName with clusterName
 *  if ( LOOKUP.notFound() ) {
 *      /// Logic based on LOOKUP results
 *  }
 * </pre>
 */
class LookupFacade {

	@Delegate Boolean found

	LookupFacade(Boolean found) {
		this.found = found
	}

	Boolean notFound(){
		return !found
	}

	Boolean found(){
		return found
	}

	boolean evaluate(){
		return found
	}
	/**
	 * Overrides default method to be used in logical comparisons
	 * <pre>
	 *  if(!LOOKUP) { ....... }
	 * </pre>
	 * @return the result of
	 */
	Boolean asBoolean() {
		return found
	}
}
