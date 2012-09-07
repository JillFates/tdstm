package com.tdssrc.grails;

import org.jsecurity.SecurityUtils

public class GormUtil {
	def public static allErrorsString = { domain, separator=" : " ->
		def text = ""
		domain?.errors.allErrors.each() { text += separator + it }
		text
	}
	/**
	 * Converts date into GMT
	 * @param date
	 * @return converted Date
	 * @deprecated GormUtil.convertInToGMT has be refactored to the DateUtil.convertInToGMT method
	 */
	def public static convertInToGMT( date, tzId ) {
		return TimeUtil.convertInToGMT(date, tzId)
	}

	/**
	 * Converts date from GMT to local format
	 * @param date
	 * @return converted Date
	 * @deprecated GormUtil.convertInToUserTZ has be refactored to the DateUtil.convertInTOUserTZ method
	 */
	def public static convertInToUserTZ(date, tzId) {
		return TimeUtil.convertInToUserTZ(date, tzId)
	}
	
	/*
	 * Convert a list into a comma delimited of type String to use inside sql statement.
	 * @param List
	 * @return converted List in to String as comma delimited
	 */
	 public static String asCommaDelimitedString( def list ){
		return list.toString().replace("[","").replace("]","")
	}
	
	/*
	 * Convert a list into a quoted comma delimited of type String to use inside sql statement.
	 * @param List
	 * @return converted List in to String as comma delimited
	 */
	 public static String asQuoteCommaDelimitedString( def list ) {
		StringBuffer sb = new StringBuffer()
		def first = true
		list.each { 
			sb.append( (first?'':',') + "'${it}'")
			first = false
		}
		return sb.toString()
	}
	
}
