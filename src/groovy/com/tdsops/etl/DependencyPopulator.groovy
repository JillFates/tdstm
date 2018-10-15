package com.tdsops.etl

import groovy.transform.CompileStatic

/**
 *
 * <code>
 * 	iterate {
 * 		domain Application
 * 		...
 * 		set assetResultVar with DOMAIN
 *
 * 	    domain Device
 * 	    ...
 * 	    set dependentResult with DOMAIN
 *
 * 		// Here's the cool stuff
 * 		domain Dependency with assetResultVar and dependentResult
 * 		...
 *	}
 * </code>
 * @see TM-12031.
 *
 */
@CompileStatic
class DependencyPopulator {

	ETLProcessor processor
	DomainFacade asset
	DomainFacade dependent

	DependencyPopulator(ETLProcessor processor){
		this.processor = processor
	}

	/**
	 * Defines dependent parameter
	 *
	 * <code>
	 *  // Load the asset side of the Dependency
	 *  domain Dependency with assetResultVar
	 *	// or alternatively
	 *	domain Dependency with assetResultVar and null
	 *  }
	 * <code>
	 * @param dependent
	 * @return
	 */
	DependencyPopulator with(DomainFacade asset){
		this.asset = asset
		this.processAsset()
		return this
	}

	/**
	 * Defines dependent parameter
	 *
	 * <code>
	 *  // Load the asset side of the Dependency
	 *  domain Dependency with assetResultVar
	 *	// or alternatively
	 *	domain Dependency with assetResultVar and null
	 *
	 *  	...
	 *  }
	 * <code>
	 * @param dependent
	 * @return
	 */
	DependencyPopulator and(DomainFacade dependent){
		this.dependent = dependent
		this.processDependent()
		return this
	}

	private void processAsset(){
		if(this.asset){

		}
	}

	private void processDependent(){
		validateParams()
	}

	/**
	 * Validate that the command arguments are correct
	 */
	private void validateParams(){
		if(!asset && !dependent){
			throw ETLProcessorException.invalidDependentParamsCommand()
		}
	}





}
