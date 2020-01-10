package net.transitionmanager.command

import net.transitionmanager.project.Project

/**
 * Command class that will be used typically for the following request:
 * <pre>
 * 	POST
 * 	/tdstm/api/import/processFile??sendNotification=true&dataScriptId=11
 * </pre>
 * <pre>
 * 	POST
 * 	/tdstm/api/import/processFile??sendNotification=true&dataScriptName=TM-16291&dataScriptProvider=VmWare
 * </pre>
 */
class ScheduleImportAPIActionCommand extends UploadFileCommand {
    /**
     * ID for an instance of {@code Datascript} domain  class.
     * This instance contains the ETL script content
     * that is going to be executed with {@code ScheduleImportAPIActionCommand#filename} field.
     */
    Long dataScriptId
    /**
     * {@code DataScript#name} field.
     * This instance contains the ETL script content
     * that is going to be executed with {@code ScheduleImportAPIActionCommand#filename} field.
     */
    String dataScriptName
    /**
     * {@code DataScript#provider} field for an instance of {@code Datascript} domain  class.
     * This instance contains the ETL script content
     * that is going to be executed with {@code ScheduleImportAPIActionCommand#filename} field.
     */
    String dataScriptProvider
    /**
     * Project defined by user to be used in dataScript tenant validation.
     */
    Project project
    /**
     * Defines if Transformation results and the auto import process
     * should be sent by email after finishing the post step.
     */
    Boolean sendNotification = false

    static constraints = {
        dataScriptId nullable: true
        dataScriptName nullable: true
        dataScriptProvider nullable: true, validator: { val, obj ->
            if (obj.dataScriptId && !obj.dataScriptName && !val
                    || !obj.dataScriptId && obj.dataScriptName && val) {
                return true
            }

            return 'api.import.missing.datascript.reference'
        }
        sendNotification nullable: false
    }
}
