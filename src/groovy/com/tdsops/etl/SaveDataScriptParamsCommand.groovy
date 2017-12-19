package com.tdsops.etl

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject

/**
 * The DataScriptParamsCommand represents the parameters that are necessary to test an ETL script
 */
@Validateable
class SaveDataScriptParamsCommand implements CommandObject {

    Long id
    String script

    static constraints = {
        id nullable: false
        script blank: false
    }
}