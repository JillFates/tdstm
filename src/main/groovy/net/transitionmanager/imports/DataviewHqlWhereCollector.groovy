package net.transitionmanager.imports

import groovy.transform.CompileStatic

/**
 * HQL Where clause collector. Its used to build a HQL sentence with params.
 * <pre>
 * 	DataviewHqlWhereCollector whereCollector = new DataviewHqlWhereCollector()
 * 	whereCollector
 * 		.addCondition("AE.moveBundle in (:moveBundles)")
 * 		.addParams([ moveBundles: moveBundleList])
 * </pre>
 */
@CompileStatic
class DataviewHqlWhereCollector {

	List<String> conditions = []
	Map<String, ?> params = [:]

	/**
	 * Add String condition to be added in internal list of conditions
	 * @param condition a String HQL sentence
	 * @return current instance of {@code DataviewHqlWhereCollector}
	 * 			to continue with chain of methods
	 */
	DataviewHqlWhereCollector addCondition(String condition) {
		if (condition) {
			this.conditions.add(condition)
		}
		return this
	}

	/**
	 * Add a Map of parameters to be used in where HQL sentence
	 * @param params a Map with key values from a HQL sentence condition
	 * @return current instance of {@code DataviewHqlWhereCollector}
	 * 			to continue with chain of methods
	 */
	DataviewHqlWhereCollector addParams(Map<String, ?> params) {
		if (params) {
			this.params.putAll(params)
		}
		return this
	}
}
