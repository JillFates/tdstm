package com.tdsops.etl

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject

/**
 * The DataScriptValidateScriptCommand represents the parameters that are necessary to test an ETL script
 */
@Validateable
class DataScriptValidateScriptCommand implements CommandObject {

    String script
    String fileName

    static constraints = {
        script blank: false
        fileName blank: false
    }
}
