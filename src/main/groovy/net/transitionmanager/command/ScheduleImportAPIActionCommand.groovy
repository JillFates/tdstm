package net.transitionmanager.command
/**
 * Command class that will be used typically for the following request:
 * <pre>
 * 	POST
 * 	/tdstm/api/import/processFile??sendNotification=true&dataScriptId=11
 * </pre>
 * <pre>
 * 	POST
 * 	/tdstm/api/import/processFile??sendNotification=true&dataScriptName=TM-16291
 * </pre>
 */
class ScheduleImportAPIActionCommand extends UploadFileCommand {
    /**
     * ID for an instance of {@code Datascript} domain  class.
     * This instance contains the ETL script content
     * that is going to be executed against filename param content.
     */
    Long dataScriptId
    /**
     * Name field for an instance of {@code Datascript} domain  class.
     * This instance contains the ETL script content
     * that is going to be executed against filename param content.
     */
    String dataScriptName
    /**
     * Project defined by user to be used in dataScript tenant validation.
     */
    Long projectId
    /**
     * Defines if Transformation results and the auto import process
     * should be sent by email after finishing the post step.
     */
    Boolean sendNotification = false

    static constraints = {
        dataScriptId nullable: true
        dataScriptName nullable: true, validator: { String val, ScheduleImportAPIActionCommand obj ->
            if ((!val && !obj.dataScriptId) || (val && obj.dataScriptId)) {
                return 'api.import.must.be.one'
            }

        }
        sendNotification nullable: false
    }
}
