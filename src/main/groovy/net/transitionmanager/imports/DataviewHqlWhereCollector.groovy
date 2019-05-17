package net.transitionmanager.imports

import groovy.transform.CompileStatic

/**
 * TODO: dcorrea. Complete docs
 */
@CompileStatic
class DataviewHqlWhereCollector {

	List<String> conditions = []
	Map<String, ?> params = [:]


	DataviewHqlWhereCollector addCondition(String condition) {
		if (condition) {
			this.conditions.add(condition)
		}
		return this
	}

	DataviewHqlWhereCollector addParams(Map<String, ?> params) {
		if (params) {
			this.params.putAll(params)
		}
		return this
	}
}
