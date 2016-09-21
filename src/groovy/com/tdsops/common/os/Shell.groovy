package com.tdsops.common.os

import groovy.transform.CompileStatic

@CompileStatic
class Shell {
  /**
   * Executes a command and bo
   * @author @tavo_luna
   */
  static executeCommand(String command){
    def exitValue = -1
    def message = ""
    try{
        def proc = command.execute()
        proc.waitFor()
        exitValue = proc.exitValue()
        def os = (exitValue == 0) ? proc.in : proc.err
        message = os.text
    }catch(IOException ioe){
        message = ioe.message
    }

    return [exitValue:exitValue, message:message]
  }

  static systemLog(String message){
    Shell.executeCommand("logger $message")
  }
}
