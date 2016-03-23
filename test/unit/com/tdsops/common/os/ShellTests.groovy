package com.tdsops.common.os

import grails.test.*

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the Shell class
 */
class ShellTests extends Specification {

  /**
  * test the OS execution level command
  */
  public void testExecuteCommand() {
    String fileToCreate = '/tmp/RandomFilename_${System.currentTimeMillis()}'
    String cmd = "touch ${fileToCreate}"
    println cmd
    def retVal = Shell.executeCommand(cmd)

    File file = new File(fileToCreate)
    expect:
      assert retVal.exitValue == 0
      assert file.exists() == true

    file.delete() 
  }

  public void testSystemLog() {
    def retVal = Shell.systemLog("Log to system")
    println "cmd: ${retVal.message}"
    expect:
      assert retVal.exitValue == 0
  }
}