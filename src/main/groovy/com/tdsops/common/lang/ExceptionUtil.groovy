package com.tdsops.common.lang
import groovy.transform.CompileStatic

/**
 * A collection of functions useful for working with exceptions
 */
@CompileStatic
class ExceptionUtil {

	/**
	 * Used to generate a Stacktrace as a string that limited to N lines
	 * @param e - the exception to get the stacktrace on
	 * @param lines - the number of lines to include in the trace (default 40)
	 * @return The string containing the stacktrace
	 */
	static String stackTraceToString(Throwable e, Integer lines = 40) {
		stackTraceToString('', e, lines)
	}

	/**
	 * Used to generate a Stacktrace as a string that limited to N lines with a message prefix
	 * @param msg - the prefix message to include in the resulting string
	 * @param e - the exception to get the stacktrace on
	 * @param lines - the number of lines to include in the trace (default 40), set to zero (0) will show all
	 * @return The string containing the stacktrace
	 */
	static String stackTraceToString(String msg, Throwable e, Integer lines = 40) {
		StringWriter sw = new StringWriter()
		PrintWriter pw = new PrintWriter(sw)
		e.printStackTrace(pw)
		String st = (msg ? msg + ' ' + '\n' : '') + sw
		if (lines > 0) {
			def parsed = st.split(/\n/)
			def numLines = parsed.size()
			if (numLines < lines) {
				lines = numLines
			}
			st = parsed[0..(--lines)].join("\n")
		}
		return st
	}

	/**
	 * Used to generate a stacktrace with a prefixed message
	 * @param msg - the prefix message to include in the result
	 * @param e - the exception object
	 * @param lines - the number of lines to include in the stacktrace (default 40)
	 * @return the message + the stacktrace
	 */
	static String messageWithStacktrace(String msg, Exception e, Integer lines = 40) {
		return stackTraceToString(msg, e, lines)
	}
}
