package com.tdssrc.grails;

import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

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
	
	/**
	 * This method is used to get a list of fields that allows or disallow null properties of a given Domain.
	 * using blankAndNullPropsOnly flag if it is true list will contain fields that property is blank: true, nullable:true
	 * @param domain : Domain class 
	 * @param blankAndNullPropsOnly : Boolean flag to determine returning list . 
	 * @return List<String> containing the property name(s) in the domain with/without blank/null constraint
	 */
	public static List<String> getDomainPropertiesNullAndBlank(def domain, def blankAndNullPropsOnly = true ) {
		 
		def fields = []
		def grailsDomain = new DefaultGrailsDomainClass( domain.class )
		grailsDomain.properties.each {
			def blankAlw = domain.constraints."${it.name}"?.getAppliedConstraint( 'blank' )?.blank
			def nullAlw = domain.constraints."${it.name}"?.getAppliedConstraint( 'nullable' )?.nullable
			 
			// If blankAndNullPropsOnly is true and blankAlw and nullAlw is true list will collect props 
			// having blank:true , nullable:true  vice versa
			 
			if(!blankAndNullPropsOnly && blankAlw && nullAlw)
				fields << it.name
			else if( !blankAlw && !nullAlw && blankAndNullPropsOnly)
				fields << it.name
		}
		return fields
	}

}
