package com.tdsops.etl

import org.spockframework.runtime.SpockAssertionError

/**
 * Custom assertions for ETL test
 */
trait ETLAssertTest {

	/**
	 * Assertions for a {@code FieldResult} instance
	 * @param fieldResult
	 * @param originalValue
	 * @param value
	 * @param initValue
	 * @param errors
	 * @param warn
	 */
	Object assertFieldResult(FieldResult fieldResult,
						  Object originalValue = null,
						  Object value = null,
						  Object initValue = null,
						  List errors = [],
						  Boolean warn = false) {
		assert fieldResult.originalValue == originalValue
		assert fieldResult.value == value
		assert fieldResult.init == initValue
		assert fieldResult.errors == errors
		assert fieldResult.warn == warn
		return true
	}

	/**
	 * Assertions for a {@code QueryResult} instance
	 * //TODO dcorrea add an example
	 * @param queryResult
	 * @param domain
	 * @param values
	 */
	def assertQueryResult(QueryResult queryResult, ETLDomain domain, List<List<Object>> values) {
		assert queryResult.domain == domain.name()
		queryResult.criteria.eachWithIndex { Map map, int i ->
			assert map['propertyName'] == values[i][0]
			assert map['operator'] == values[i][1]
			assert map['value'] == values[i][2]
			return true
		}
	}

	/**
	 * Assert if a {@code FindCondition} is complete and the rest of the fields are correct
	 * @param condition
	 * @param propertyName
	 * @param operator
	 * @param value
	 */
	Object assertFindConditionComplete(
		FindCondition condition,
		String propertyName,
		FindOperator operator,
		Object value,
		Boolean isComplete = true
	) {
		assert propertyName == condition.propertyName
		assert operator == condition.operator
		assert value == condition.value
		assert condition.isComplete() == isComplete
		return true
	}

	Object assertWith(Object target, Closure<?> closure) {
		if (target == null) {
			throw new SpockAssertionError("Target of 'with' block must not be null");
		}
		closure.setDelegate(target)
		closure.setResolveStrategy(Closure.DELEGATE_FIRST)
		return closure.call(target)
	}

	Object assertWith(Object target, Class<?> type, Closure closure) {
		if (target != null && !type.isInstance(target)) {
			throw new SpockAssertionError(String.format("Expected target of 'with' block to have type '%s', but got '%s'",
				type, target.getClass().getName()))
		}
		return assertWith(target, closure)
	}
}
