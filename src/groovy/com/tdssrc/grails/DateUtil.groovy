package com.tdssrc.grails

import java.text.SimpleDateFormat

import com.sun.org.apache.xalan.internal.xsltc.cmdline.getopt.IllegalArgumentException;

/**
 * The DateUtil class contains a collection of useful Date manipulation methods 
 */
class DateUtil {

	protected static formatter = new SimpleDateFormat("M-d-yyyy")
	protected static simpleDateformatter = new SimpleDateFormat("MM/dd/yyyy")
	protected static mdySdf = new SimpleDateFormat("MM/dd/yyyy")


	/**
	 * Used to convert a date into a String with the format mm/dd/yyyy 
	 * @param the Date to convert 
	 * @return String of the format
	 */
	static String formatDate(Date date) {
		if (date) {
			return simpleDateformatter.format(date)
		} else {
			return ''
		}
	}

	/**
	 * Used to convert a string to a date in the format mm/dd/yyyy or m/d/yy
	 * @param the string to convert to a date
	 * @return the date representation of the string or null if formating failed
	 */
	static Date parseDate(String val) {
		def date
		def newVal
		if (val?.size() ) {
			newVal = val.replaceAll('-','/')
			def e = newVal.split('/')
			if (e.size() != 3) {
				throw new java.text.ParseException()
			}

			// Convert two digit year to 4 digits if necessary
			if (e[2].size() == 2)
				e[2] = '20'+e[2]

			if (e[2].size() != 4) {
				// log.debug "parseDate $val year not 4 digits"
				throw new java.text.ParseException("Year has invalid format ($e[2])", 3)			
			}
			newVal = "${e[0]}/${e[1]}/${e[2]} 00:00:00 GMT"

			date = simpleDateformatter.parse(newVal)
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
		if (val) {
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

	/**
	 * Used to parse a string into a date with the expected mm/dd/yyyy format
	 * @param str - the date string
	 * @return the parsed date or null if not valid
	 */
	static Date mdyToDate(String str) {
		Date parsedDate		
		if (str) {
			try {
				mdySdf.setLenient(false)
				parsedDate = mdySdf.parse(str)
				parsedDate.clearTime()
			} catch(Exception e) {
			}
		}
		return parsedDate
	}

}