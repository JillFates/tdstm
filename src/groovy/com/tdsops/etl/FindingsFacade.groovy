package com.tdsops.etl

import com.tds.asset.AssetEntity

/**
 * Find Element Facade used in ETL script for FINDINGS bound variable.
 * It could be used for defining the latest results
 * <pre>
 * 	// Save the results of the latest findings off to a local variable
 * 	set supportingFindings with FINDINGS
 * </pre>
 * After that definition, FindingsFacade can be used like the following examples:
 * <pre>
 *   if (supportingFindings.size == 1 and
 *   		primaryFindings.isApplication() ) {*         	........
 *}* </pre>
 */
class FindingsFacade {

	ETLFindElement findElement

	FindingsFacade(ETLFindElement findElement) {
		this.findElement = findElement
	}

	/**
	 * Returns the number of elements found.
	 * @return the number of elements in the found results list
	 */
	int size() {
		return findElement.resultSize()
	}

	/**
	 * Defines if the FindingsFacade contains results
	 * and all these results are Database assets
	 * @return
	 */
	boolean isDatabase() {
		if (hasResultsIn(AssetEntity)) {
			return findElement.results.every { it.isaDatabase() }
		}
		return false
	}

	/**
	 * Defines if the FindingsFacade contains results
	 * and all these results are Device assets
	 * @return
	 */
	boolean isDevice() {
		if (hasResultsIn(AssetEntity)) {
			return findElement.results.every { it.isaDevice() }
		}
		return false
	}

	/**
	 * Defines if the FindingsFacade contains results
	 * and all these results are Application assets
	 * @return
	 */
	boolean isApplication() {
		if (hasResultsIn(AssetEntity)) {
			return findElement.results.every { it.isaApplication() }
		}
		return false
	}

	/**
	 * It checks if find Element contains results
	 * and those results are instance of clazz parameter
	 * @param clazz a Class definition
	 * @return true if there is results in clazz hierarchy.
	 * 			otherwise return false
	 */
	private boolean hasResultsIn(Class<?> clazz){
		return findElement.hasResults() &&
					findElement.results.every{ it in clazz}
	}
}
