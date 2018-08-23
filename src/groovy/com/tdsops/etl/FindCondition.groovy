package com.tdsops.etl

/**
 *
 */
class FindCondition {

	String propertyName
	FindOperator operator
	Object value

	FindCondition(String propertyName) {
		this.propertyName = propertyName
	}

	FindCondition(String propertyName, Object value, FindOperator operator = FindOperator.eq) {
		this.propertyName = propertyName
		defineOperator(operator, value)
	}

	FindCondition(String propertyName, Object value, String operator) {
		this.propertyName = propertyName
		defineOperator(operator as FindOperator, value)
	}

	void defineOperator(FindOperator operator, Object value) {
		this.operator = operator
		this.value = ETLValueHelper.valueOf(value)
	}

	Boolean isComplete() {
		return this.propertyName && this.operator
	}

	String toString() {
		return "$propertyName ${operator.name()} $value"
	}

	/**
	 * It builds a list of conditions from a JSON Object
	 * <pre>
	 *  [
	 * 	  { "propertyName": "assetName", "operator":"notContains", "value": "prod"},
	 * 	  { "propertyName": "priority", "operator":"gt",alue": 4}
	 * 	]
	 * </pre>
	 * It is going to be converted to:
	 * <pre>
	 *  List<FindCondition> conditions = FindCondition.buildCriteria(json)
	 *  conditions[0].propertyName == 'assetName'
	 *  conditions[0].operator == FindOperator.notContains
	 *  conditions[0].value == 'prod'
	 *
	 *  conditions[1].propertyName == 'priority'
	 *  conditions[1].operator == FindOperator.gt
	 *  conditions[1].value == 4
	 * </pre>
	 * @param criteria a json parameter
	 * @return a list of {@code FindCondition}
	 * @see FindOperator
	 */
	static List<FindCondition> buildCriteria(List<Map> criteria){
		return criteria.collect{ Map map ->
			new FindCondition(
				map['propertyName'],
				map['value'],
				map['operator']
			)
		}
	}
}
