package com.tdsops.etl

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject

/**
 * The DataScriptParamsCommand represents the parameters that are necessary to test an ETL script
 */
@Validateable
class DataScriptParamsCommand implements CommandObject {

    String script
    String fileName

    static constraints = {
        script blank: false
        fileName blank: false
    }
}
