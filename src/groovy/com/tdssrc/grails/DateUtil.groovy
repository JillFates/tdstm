package com.tdssrc.grails

import java.text.SimpleDateFormat

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.IllegalArgumentException;

/**
 * The DateUtil class contains a collection of useful Date manipulation methods 
 */
class DateUtil {

	protected static formatter = new SimpleDateFormat("M-d-yyyy")
	protected static simpleDateformatter = new SimpleDateFormat("MM/dd/yyyy")

	/**
	 * used to convert a string to a date in the format mm/dd/yyyy or m/d/yy
	 * @param the string to convert to a date
	 * @return the date representation of the string or null if formating failed
	 */
	static Date parseDate(String val) {
		def date
		def newVal
		if (val.size() ) {
			newVal = val.replaceAll('/','-')
			def e = newVal.split('-')
			if (e.size() != 3) {
				// log.debug "parseDate $val missing 3 elements"
				return null
			}

			// Convert two digit year to 4 digits if necessary
			if (e[2].size() == 2)
				e[2] = '20'+e[2]

			if (e[2].size() != 4) {
				// log.debug "parseDate $val year not 4 digits"
				return null				
			}
			newVal = "${e[0]}-${e[1]}-${e[2]} 00:00:00 GMT"

			try {
				date = formatter.parse(newVal)
			} catch (Exception ex) {
				println "parseDate $val failed to parse ${ex.getMessage()}"
			}
		}
		return date
	}
	
	/**
	 * This method can be used as to validate a date following mm/dd/yyyy format. 
	 * If no date is supplied return current date else if not in correct format return msg
	 * @param val date in String
	 * @return Date or errormsg
	 */
	static def parseImportedCreatedDate(String val){
		if(val){
			try{
				simpleDateformatter.setLenient(false);
				return simpleDateformatter.parse(val)
			} catch(Exception e){
				return "Created date $val not in format. mm/dd/yyyy should be the format needed."
			}
		} else {
			return new Date();
		}
	}
}