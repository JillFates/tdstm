package com.tdsops.common.os

import spock.lang.Specification

class ShellTests extends Specification {

	/**
	 * test the OS execution level command
	 */
	void testExecuteCommand() {
		when:
		String fileToCreate = '/tmp/RandomFilename_${System.currentTimeMillis()}'
		String cmd = "touch ${fileToCreate}"

		def retVal = Shell.executeCommand(cmd)
		File file = new File(fileToCreate)

		then:
		0 == retVal.exitValue
		file.exists()
		file.delete()
	}

	void testSystemLog() {
		when:
		def retVal = Shell.systemLog("Log to system")

		then:
		0 == retVal.exitValue
	}
}
