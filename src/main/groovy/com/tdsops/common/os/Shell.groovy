package com.tdsops.common.os

import groovy.transform.CompileStatic

@CompileStatic
class Shell {
	/**
	 * Executes a command and bo
	 * @author @tavo_luna
	 */
	static Map executeCommand(String command) {
		def exitValue = -1
		String message
		try {
			def proc = command.execute()
			proc.waitFor()
			exitValue = proc.exitValue()
			InputStream os = (exitValue == 0) ? proc.in : proc.err
			message = os.text
		}
		catch (IOException e) {
			message = e.message
		}

		[exitValue: exitValue, message: message]
	}

	static Map systemLog(String message) {
		executeCommand 'logger ' + message
	}
}
