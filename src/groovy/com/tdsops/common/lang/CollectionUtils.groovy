package com.tdsops.common.lang

import java.util.ArrayList
import java.util.LinkedHashMap

/**
 * CollectionUtils class provides some useful collection related manipulation and testing methods
 */
class CollectionUtils {

	/**
	 * Used to convert an object to a java.util.ArrayList if not already an ArrayList
	 * @param Any type of object
	 * @return the object contained within an array
	 */
	static ArrayList asList(Object object) { 
		if (! ( object instanceof ArrayList ) )
			object = [ object ]
		return object
	}

	/**
	 * Used to determine if an object is a List
	 * @param object - the object being tested
	 * @return true if the object is a java.util.ArrayList
	 */
	static boolean isaList( Object object ) {
		return object instanceof ArrayList
	}

	/**
	 * Used to determine if an object is a java.util.LinkedHashMap
	 * @param object - the object being tested
	 * @return true if the object is a java.util.LinkedHashMap
	 */
	static boolean isaMap( Object object ) {
		return object instanceof LinkedHashMap
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


}