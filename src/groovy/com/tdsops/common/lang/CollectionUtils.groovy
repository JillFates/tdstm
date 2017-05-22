package com.tdsops.common.lang

/**
 * CollectionUtils class provides some useful collection related manipulation and testing methods
 */
class CollectionUtils {

	/**
	 * Converts an object to a List if not already a List
	 * @param Any type of object
	 * @return the object contained within a list
	 */
	static List asList(object) {
		(object == null || object instanceof List) ? object :
				isCollectionOrArray(object) ? (object as List) : [object]
	}

	/**
	 * Function to promote an Object to a Collection
	 * @author @tavo_luna
	 * @param object the object to transform
	 * @return Collection Object
	 */
	static Collection asCollection(object) {
		isCollectionOrArray(object) ? object : [object]
	}

	/**
	 * check if the given object is a Collection, or a Java Array
	 * @author @tavo_luna
	 * @param object the object to check
	 * @return true if the object is instance of Collection or array, false in any other case
	 */
	static boolean isCollectionOrArray(object) {
    (object instanceof Collection) || object.getClass().isArray()
	}

	/**
	 * Builder to create  Closure that sorts an array using a function to get the String to compare
	 * @author @tavo_luna
	 * @param f - the closure used to extract the info
	 * @return the Comparator closure
	 */
	static Closure caseInsensitiveSorterBuilder(Closure f){
    return {a, b ->
    	//Apply function to get the actual comparator
      def fa = f(a)?:""
			def fb = f(b)?:""
			def faUp = fa.toUpperCase()
			def fbUp = fb.toUpperCase()
			def comp = faUp.compareTo(fbUp)
			//if the names are the same we set the lowercase first
			if(comp == 0) comp = fa.compareTo(fb)*-1  // <-- set the lowercase first
			return comp
    }
	}

	/**
	 * Return {@code true} if the supplied Collection is {@code null} or empty.
	 * @param collection
	 * @return
	 */
	static boolean isEmpty(Collection<?> collection) {
		return org.springframework.util.CollectionUtils.isEmpty(collection);
	}

	/**
	 * Return {@code false} if the supplied Collection is not {@code null} or not empty.
	 * @param collection
	 * @return
	 */
	static boolean isNotEmpty(Collection<?> collection) {
		return !isEmpty(collection)
	}
}
