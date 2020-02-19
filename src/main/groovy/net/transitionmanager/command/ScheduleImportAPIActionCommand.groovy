package net.transitionmanager.command

import com.tdssrc.grails.FileSystemUtil
import net.transitionmanager.i18n.Message
import net.transitionmanager.project.Project

/**
 * Command class that will be used typically for the following request:
 * <pre>
 *  curl --location --request POST 'http://server:8080/tdstm/api/import/processFile' \
 *       --header 'Accept-Version: 1.0' \
 *       --header 'Content-Type: application/x-www-form-urlencoded' \
 *       --header 'Accept: application/json' \
 *       --header 'Authorization: Bearer XXXX' \
 *       --form 'sendNotification=true' \
 *       --form 'dataScriptId=14' \
 *       --form 'file=@/path/to/file/TM-16291.csv' \
 *       --form 'project.id=2'
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
    String providerName
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
        project nullable: true, validator: { val, obj ->
            if (!val) {
                return 'default.missing.required'
            }
        }
        sendNotification nullable: true, validator: { val, obj ->
            if (!val) {
                return 'default.missing.required'
            }
        }
        file nullable: true, validator: { file, cmd ->
            if (!file) {
                return 'default.missing.required'
            }
            if (!FileSystemUtil.validateExtension(file.getOriginalFilename(), cmd.getValidFileExtension())) {
                return Message.FileSystemInvalidFileExtension
            }
        }
        dataScriptId nullable: true
        dataScriptName nullable: true
        providerName nullable: true, validator: { val, obj ->
            if (obj.dataScriptId && !obj.dataScriptName && !val
                    || !obj.dataScriptId && obj.dataScriptName && val) {
                return true
            }

            if (obj.dataScriptName && !val && !obj.dataScriptId) {
                return 'default.missing.required'
            }

            if (!obj.dataScriptId && (val || obj.dataScriptName)) {
                return 'default.missing.required'
            }

            return 'api.import.missing.datascript.reference'
        }
    }
}
