package net.transitionmanager.api.v1_0

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.StopWatch
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.ScheduleImportAPIActionCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.imports.ImportService
import net.transitionmanager.security.Permission

@Secured('isAuthenticated()')
class ImportController implements ControllerMethods {

    static namespace = 'v1'

    static allowedMethods = [
            processFile: 'POST',
    ]

    ImportService importService

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
        validateCommandObject(actionCommand)

        Map result = importService.processFile(actionCommand)
        renderSuccessJson(result)
        log.info 'DataScriptController.autoBatchProcessing() took {}', stopwatch.endDuration()
    }
}
