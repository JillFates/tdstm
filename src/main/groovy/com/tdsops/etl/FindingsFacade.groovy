package com.tdsops.etl
/**
 * Find Element Facade used in ETL script for FINDINGS bound variable.
 * It could be used for defining the latest results
 * <pre>
 * 	// Save the results of the latest findings off to a local variable
 * 	set supportingFindings with FINDINGS
 * </pre>
 * After that definition, FindingsFacade can be used like the following examples:
 * <pre>
 *  if (supportingFindings.size == 1 and
 *      primaryFindings.isApplication() ) {
 *   	........
 *  }
 * </pre>
 */
class FindingsFacade {

	private ETLFindElement findElement

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
	 * Defines if the FindingsFacade contains a single result
	 * and that results is an Database asset instance
	 * <pre>
	 * 		def primaryFindings = FINDINGS
	 * 		if (primaryFindings.size() > 0 && primaryFindings.isDatabase()){
	 * 			....
	 * 		}
	 * </pre>
	 * @return true if results is an Database instance
	 * @throws ETLProcessorException if size is bigger than 1
	 * @See AssetEntity#isaDatabase
	 */
	boolean isDatabase() {
		if(size() > 1){
			throw ETLProcessorException.incorrectFindingsMethodInvocation('isDatabase')
		}
		return findElement.firstResult().isaDatabase()
	}

	/**
	 * <pre>
	 * 	 find Dependency by 'asset' eq assetId and 'type' eq 'Runs On' into 'id'
	 * 	 FINDINGS each { dependency ->
	 * 	 	if (['VM','Database'].contains(device.assetType)) {
	 * 	 		log device.assetName + ' is a VM'
	 *  	} else {
	 *      	log device.assetName + ' is a physical server
	 *      }
	 *      domain device
	 *      find Device by 'id' device.id
	 *      load 'Description' with 'Found by FINDINGS each command'
	 *	 }
	 * </pre>
	 * @param closure the closure applied on each element found
	 */
	List<Object> each(Closure closure) {
		List<Object> objects = queryResults()
		return objects.each { Object instance ->
			closure(instance)
		}
	}

	/**
	 * Retrieve a list of domain objects.
	 *
	 * @return List of domain objects
	 */
	private List<Object> queryResults() {
		List<Long> ids = findElement.results()
		List<Object> results = []
		if (!ids.isEmpty()) {
			Class domainClass = findElement.currentDomain.clazz
			results = domainClass.createCriteria().list {
				'in' ('id', ids)
				readOnly(true)
			}
		}
		return results
	}
	/**
	 * Defines if the FindingsFacade contains a single result
	 * and that results is an Device asset instance
	 * <pre>
	 * 		def primaryFindings = FINDINGS
	 * 		if (primaryFindings.size() > 0 && primaryFindings.isDevice()){
	 * 			....
	 * 		}
	 * </pre>
	 * @return true if results is an Device instance
	 * @throws ETLProcessorException if size is bigger than 1
	 * @See AssetEntity#isaDevice
	 */
	boolean isDevice() {
		if(size() > 1){
			throw ETLProcessorException.incorrectFindingsMethodInvocation('isDevice')
		}
		return findElement.firstResult().isaDevice()
	}

	/**
	 * Defines if the FindingsFacade contains a single result
	 * and that results is an Application asset instance
	 * <pre>
	 * 		def primaryFindings = FINDINGS
	 * 		if (primaryFindings.size() > 0 && primaryFindings.isApplication()){
	 * 			....
	 * 		}
	 * </pre>
	 * @return true if results is an Application instance
	 * @throws ETLProcessorException if size is bigger than 1
	 * @See AssetEntity#isaApplication
	 */
	boolean isApplication() {
		if(size() > 1){
			throw ETLProcessorException.incorrectFindingsMethodInvocation('isApplication')
		}
		return findElement.firstResult().isaApplication()
	}

	/**
	 * Returns the first element collected in results
	 * @return
	 */
	Object result(){
		return findElement.firstResult()
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


	@Override
	String toString() {
		return """FINDINGS {
			statement=${findElement?.currentFind?.statement?.conditions},
			results=${findElement?.results}
		}"""
	}
}
