package net.transitionmanager.api.v1_0

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.StopWatch
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.ScheduleImportAPIActionCommand
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.imports.DataImportService
import net.transitionmanager.imports.DataScript
import net.transitionmanager.security.Permission

@Secured('isAuthenticated()')
class ImportController implements ControllerMethods {

    static namespace = 'v1'

    static allowedMethods = [
            processFile: 'POST',
    ]

    DataImportService dataImportService
    FileSystemService fileSystemService

    /**
     * <p>Import process for a particular ETL Script, defined by an instance of {@code DataScript#id}</p>
     * <p>First, this method validates request params, {@code InitiateTransformAPIActionCommand#id}
     * and {@code InitiateTransformAPIActionCommand#sendNotification}.</p>
     * <p>After that, this method validates {@code UploadFileCommand#file} request.</p>
     * <p>After validating all request parameters, this method initializes an ETL Script transformation
     * using {@code FileSystemService#.transferFileToFileSystem(actionCommand,FileSystemService.ETL_SOURCE_DATA_PREFIX)}</p>
     *
     * @return JSON results with Job initializing results.
     */
    @HasPermission(Permission.AssetImport)
    def processFile() {
        def stopwatch = new StopWatch()
        stopwatch.start()

        ScheduleImportAPIActionCommand actionCommand = populateCommandObject(ScheduleImportAPIActionCommand)
        String fileName = fileSystemService.transferFileToFileSystem(actionCommand, FileSystemService.ETL_SOURCE_DATA_PREFIX)

        validateCommandObject(actionCommand)
        validateProject(actionCommand.project)

        DataScript dataScript = null
        if (actionCommand.dataScriptId) {

            dataScript = DataScript.where {
                id == actionCommand.dataScriptId
                project == actionCommand.project
            }.get()

        } else {

            dataScript = DataScript.where {
                name == actionCommand.dataScriptName
                'provider.name' == actionCommand.dataScriptProvider
                project == actionCommand.project
            }.get()
        }

        Map result = dataImportService.scheduleETLTransformDataJob(
                actionCommand.project,
                dataScript.id,
                fileName,
                actionCommand.sendNotification
        )
        renderSuccessJson(result)
        log.info 'DataScriptController.autoBatchProcessing() took {}', stopwatch.endDuration()
    }
}
