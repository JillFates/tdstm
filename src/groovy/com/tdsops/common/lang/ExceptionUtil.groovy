package com.tdsops.common.lang

/**
 * A collection of functions useful for working with exceptions
 */
class ExceptionUtil {
	
	static String stackTraceToString(Exception e, Integer lines=30) {
		StringWriter sw = new StringWriter()
		PrintWriter pw = new PrintWriter(sw)
		e.printStackTrace(pw)
		String st = sw.toString()
		if (lines > 0) {
			def parsed = st.split(/\n/)
			def numLines = parsed.size()
			if (numLines < lines)
				lines =  numLines
			st = parsed[0..(--lines)].join("\n")
		}
		return st
	}

}