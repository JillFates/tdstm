package com.tdssrc.grails;

import java.util.Iterator;

/**
 * The EnumUtil class contains a collection of useful enums manipulation methods
 * 
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 *
 */
class EnumUtil {
	
	/**
	 * Looks in an array for a value and return it
	 * 
	 * @param values an array of values to look in
	 * @param param the value to search
	 */
	static searchfParam(values, String param ) {
		return param?values.find { it.value == param }:null
	}

}
