package net.transitionmanager.api.v1_0

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.StopWatch
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.InitiateTransformAPIActionCommand
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.imports.DataImportService
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission


@Secured('isAuthenticated()')
class DataScriptController implements ControllerMethods {

    static namespace = 'v1'

    static allowedMethods = [
            transform: 'POST',
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
     * {@code DataScript#etlSourceCode}
     *
     * @return JSON results with Job initializing results.
     */
    @HasPermission(Permission.AssetImport)
    def transform() {
        def stopwatch = new StopWatch()
        stopwatch.start()

        InitiateTransformAPIActionCommand actionCommand = populateCommandObject(InitiateTransformAPIActionCommand)
        String fileName = fileSystemService.transferFileToFileSystem(actionCommand, FileSystemService.ETL_SOURCE_DATA_PREFIX)

        validateCommandObject(actionCommand)

        Project project = getProjectForWs()
        Map result = dataImportService.scheduleETLTransformDataJob(
                project,
                actionCommand.id,
                fileName,
                actionCommand.sendNotification
        )
        renderSuccessJson(result)
        log.info 'DataScriptController.autoBatchProcessing() took {}', stopwatch.endDuration()
    }
}
