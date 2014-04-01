package com.tdsops.metaclass

/* 
 * Initialize various dynamic method injections on various classes that will be used within the application
 */

class CustomMethods {

 	// Setup a Singleton pattern so we only initialize the methods once
	private static INSTANCE 
    
	static getInitialize() {
		if (INSTANCE == null) {
			INSTANCE = new CustomMethods()
		} 
		return INSTANCE 
	}

    /**
     * private constructor that will inject the new functions onto the various classes
     */
    private CustomMethods() {
		
		/**
		 * Used to convert a List of objects into a Map using one of the objects' properties as the key
		 * @param String or Closure - as a String, it would be the property name or as a Closure a method that computes the key referencing the object
		 * @return Map
		 */
		ArrayList.metaClass.asMap = { arg ->
			def result = [:]
			def isClosure = (arg instanceof Closure)
			delegate.each {
				def key = isClosure ? arg(it) : it[arg]
				result.putAt(key.toString(), it)
			}
			return result
		}

		/**
		 * Used to convert a List of objects into a Map using one of the objects' properties as the key
		 * @param String or Closure - as a String, it would be the property name or as a Closure a method that computes the key referencing the object
		 * @return Map
		 */
		ArrayList.metaClass.asGroup = { arg ->
			def result = [:]
			def isClosure = (arg instanceof Closure)
			delegate.each { 
				def key = isClosure ? arg(it) : it[arg]
				key = key.toString()
				if ( result.containsKey(key) ) {
					result.putAt(key, result[key] + it)
				} else {
					result.putAt(key, [it])
				}
			}
			return result
		}
	}
}