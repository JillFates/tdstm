package net.transitionmanager.service.dataview

import org.spockframework.runtime.SpockAssertionError

/**
 * Custom Assertion test to replace 'with' assertions in Grails 3.
 * <pre>
 * 	assertWith(extraFilter, ExtraFilter) {
 * 		property == '_filter'
 * 		filter == 'physicalServer'
 * 	}
 * </pre>
 */
trait AssertionTest {

	def assertWith(Object target, Closure<?> closure) {
		if (target == null) {
			throw new SpockAssertionError("Target of 'with' block must not be null");
		}
		closure.setDelegate(target)
		closure.setResolveStrategy(Closure.DELEGATE_FIRST)
		return closure.call(target)
	}

	def assertWith(Object target, Class<?> type, Closure closure) {
		if (target != null && !type.isInstance(target)) {
			throw new SpockAssertionError(String.format("Expected target of 'with' block to have type '%s', but got '%s'",
				type, target.getClass().getName()))
		}
		return assertWith(target, closure)
	}
}