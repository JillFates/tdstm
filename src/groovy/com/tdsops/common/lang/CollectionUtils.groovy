package com.tdsops.common.lang

import org.springframework.util.ObjectUtils

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

	/**
	 * Return {@code true} if the supplied Array is {@code null} or empty.
	 * @param array
	 * @return
	 */
	static boolean isEmpty(Object... array) {
		return ObjectUtils.isEmpty(array)
	}

	/**
	 * Return {@code false} if the supplied Array is not {@code null} or not empty.
	 * @param array
	 * @return
	 */
	static boolean isNotEmpty(Object... array) {
		return !isEmpty(array)
	}

	/**
	 * Flatten authentication response map when session information source is in a JSON format,
	 * So the JSON is converted to flatten map to have direct access to properties even using dotted notation.
	 *
	 * e.g.
	 * Map: ['result': ['user_name': 'test', 'roles': 'USER']]
	 *
	 * is translated to:
	 * ['result.user_name': 'test', 'result.roles': ['USER']]
	 *
	 * Having that if the sessionNameProperties are like:
	 * username@json:result.user_name
	 *
	 * Then, the map can be accessed easily like: map['result.user_name']
	 *
	 * @param map
	 * @return a flatten map
	 */
	static Map<String, ?> flattenMap(Map<String, ?> map) {
		map.collectEntries { k, v ->
			(v instanceof Map) ?
					flattenMap(v).collectEntries { k1, v1 ->
						[(String.valueOf(k) + '.' + String.valueOf(k1)): v1]
					}
					:
					[(k): v]
		}
	}

	/**
	 * Used to perform a deep clone of a Map so that all of the objects are duplicated instead
	 * of referencing the same sub elements. This is implemented by serializing the object and
	 * then reconstituing it from the serialization.
	 * @param originalMap - the map to be cloned
	 * @return the cloned map object
	 */
	static Map deepClone(Map originalMap) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream()
		ObjectOutputStream oos = new ObjectOutputStream(bos)
		oos.writeObject(originalMap)
		oos.flush()
		ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray())
		ObjectInputStream ois = new ObjectInputStream(bin)
		return ois.readObject()
	}
}
