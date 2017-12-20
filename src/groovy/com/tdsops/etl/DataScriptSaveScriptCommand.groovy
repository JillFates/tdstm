package com.tdsops.etl

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject

/**
 * The DataScriptSaveScriptCommand represents the parameters that are necessary to test an ETL script
 */
@Validateable
class DataScriptSaveScriptCommand implements CommandObject {

    Long id
    String script

    static constraints = {
        id nullable: false
        script blank: false
    }
}