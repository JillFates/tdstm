package net.transitionmanager.imports

import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.ProgressCallback
import com.tdssrc.grails.FileSystemUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.common.ProgressService
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.i18n.Message
import net.transitionmanager.job.ETLImportDataJob
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.ServiceMethods
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl

@Transactional(readOnly = true)
@CompileStatic
class DataTransformService implements ServiceMethods {

    ProgressService progressService
    FileSystemService fileSystemService
    ScriptProcessorService scriptProcessorService
    FailureNotificationService failureNotificationService
    Scheduler quartzScheduler
    /**
     * Transform the Data from ETL as part of the Asset Import process' first step.
     *
     * @param project
     * @param dataScriptId
     * @param filename
     * @return
     */
    @NotTransactional
    Map transformEtlData(UserLogin userLogin, Long projectId, Long dataScriptId, String filename, Boolean sendResultsByEmail, String progressKey) {

        validateAllowedFilename(filename)
        Project project = Project.get(projectId)
        DataScript dataScript = findDataScript(project, dataScriptId)
        ProgressCallback updateProgressCallback = createProgressCallback(progressKey)

        try {
            return executeTransformationAndScheduleImport(userLogin, project, dataScript, filename, sendResultsByEmail, updateProgressCallback)

        } catch (Throwable e) {
            String errorMessage = e.getMessage()
            reportFailureProgress(updateProgressCallback, errorMessage)

            if (dataScript?.isAutoProcess) {
                failureNotificationService.notifyDataTransformFailure(userLogin.person, dataScript, errorMessage)
            }
            return [errorMessage: errorMessage]
        }
    }

    /**
     * This method takes 2 responsibilities:
     * 1) Executes ETL transformation {@link DataTransformService#executeTransformationAndScheduleImport(net.transitionmanager.security.UserLogin, net.transitionmanager.project.Project, net.transitionmanager.imports.DataScript, java.lang.String, java.lang.Boolean, com.tdsops.etl.ProgressCallback)}
     * <br>
     * 2) If {@link DataScript#isAutoProcess} is true, it schedules net.transitionmanager.job.ETLImportDataJob with results from 2
     *
     * @param userLogin
     * @param project
     * @param dataScript
     * @param filename
     * @param sendResultsByEmail
     * @param progressKey
     * @return a Map with execution results
     */
    Map<String, ?> executeTransformationAndScheduleImport(UserLogin userLogin, Project project, DataScript dataScript, String filename, Boolean sendResultsByEmail, ProgressCallback updateProgressCallback) {

        String filenameResults = executeTransformETLData(project, dataScript, filename, sendResultsByEmail, updateProgressCallback)

        reportCompletedProgress(updateProgressCallback, filenameResults)

        if (dataScript.isAutoProcess) {
            return scheduleImportDataJob(
                    project,
                    userLogin,
                    filenameResults,
                    dataScript.id,
                    sendResultsByEmail
            )
        } else {
            return [
                    filename: filenameResults
            ]
        }
    }

    /**
     * Transform the Data from ETL as part of the Asset Import process' first step.
     *
     * @param project
     * @param dataScriptId
     * @param filename
     * @param sendResultsByEmail
     * @param updateProgressCallback
     * @return filename with results saved in fileSystem
     */
    String executeTransformETLData(Project project, DataScript dataScript, String filename, Boolean sendResultsByEmail, ProgressCallback updateProgressCallback) {

        ETLProcessorResult processorResult = scriptProcessorService.executeAndRetrieveResults(
                project,
                dataScript?.id,
                dataScript.etlSourceCode,
                fileSystemService.getTemporaryFullFilename(filename),
                updateProgressCallback
        )

        return scriptProcessorService.saveResultsUsingStreaming(processorResult)
    }

    /**
     * Using an instance of {@link ProgressCallback} interface,
     * it reports failures in ETL transformations
     *
     * @param updateProgressCallback
     * @param filename
     */
    void reportCompletedProgress(ProgressCallback updateProgressCallback, String filename) {
        updateProgressCallback.reportProgress(
                100,
                true,
                ProgressCallback.ProgressStatus.COMPLETED,
                filename)
    }

    /**
     * Using an instance of {@link ProgressCallback} interface,
     * it reports failures in ETL transformations
     *
     * @param updateProgressCallback
     * @param errorMessage
     */
    void reportFailureProgress(ProgressCallback updateProgressCallback, String errorMessage) {
        log.info "etl transformation throw an exception $errorMessage"

        updateProgressCallback.reportProgress(
                100I,
                true,
                ProgressCallback.ProgressStatus.FAILED,
                errorMessage)
    }

    /**
     * Creates an instance of {@link ProgressCallback} interface
     *
     * @param progressKey
     * @return an instance of {@link ProgressCallback}
     */
    ProgressCallback createProgressCallback(String progressKey) {

        // The progress closure that will be used by the ETL process to report back to this service the overall progress
        return { Integer percentComp, Boolean forceReport, ProgressCallback.ProgressStatus status, String detail ->
            // if progress key is not provided, then just skip updating progress service
            // this is useful during integration test invocation
            if (progressKey) {
                // log.debug "updateProgressCallback() ${percentComp}%, forceReport=$forceReport, status=$status, detail=$detail"
                progressService.update(progressKey, percentComp, status.name(), detail)
            }
        } as ProgressCallback
    }

    /**
     *
     * @param projectId
     * @param dataScriptId
     * @return
     */
    DataScript findDataScript(Project project, Long dataScriptId) {

        if (!dataScriptId) {
            throwException(InvalidParamException, 'dataScriptId.missing.parameter', 'Missing required dataScriptId parameter')
        }

        DataScript dataScript = GormUtil.findInProject(project, DataScript, dataScriptId, true)

        if (!dataScript.etlSourceCode) {
            throwException(InvalidParamException, 'dataScriptSource.not.specified', 'ETL Script has no source specified')
        }

        return dataScript
    }

    /**
     * Validates if filename parameter exists and it is a valida ETL file extension.
     *
     * @param filename
     */
    void validateAllowedFilename(String filename) {
        if (!filename) {
            throwException(InvalidParamException, 'filename.missing.parameter', 'Missing filename parameter')
        }

        List<String> allowedExtensions = fileSystemService.getAllowedExtensions()
        if (!FileSystemUtil.validateExtension(filename, allowedExtensions)) {
            throwException(InvalidParamException, Message.FileSystemInvalidFileExtension, i18nMessage(Message.FileSystemInvalidFileExtension))
        }
    }

    /**
     * Schedules an execution of {@link ETLImportDataJob} with correct parameters.
     *
     * @param project
     * @param userLogin
     * @param datasetFileName
     * @param dataScriptId
     * @param sendResultsByEmail
     * @return JSON map containing the following:
     * 		progressKey: <String> progress key generated
     * 		jobTriggerName: <String> Trigger name of the executed.
     */
    Map<String, String> scheduleImportDataJob(
            Project project,
            UserLogin userLogin,
            String filename,
            Long dataScriptId,
            Boolean sendResultsByEmail = false
    ) {
        String key = 'ETL-Import-Data-' + project.id + '-' + StringUtil.generateGuid()
        progressService.create(key, ProgressService.PENDING)

        def jobTriggerName = 'TM-ImportData-' + project.id + '-' + StringUtil.generateGuid()

        Trigger trigger = new SimpleTriggerImpl(jobTriggerName)
        trigger.jobDataMap.project = project
        trigger.jobDataMap.filename = filename
        trigger.jobDataMap.progressKey = key
        trigger.jobDataMap.dataScriptId = dataScriptId
        trigger.jobDataMap.sendResultsByEmail = sendResultsByEmail
        trigger.jobDataMap.userLogin = userLogin
        trigger.setJobName(ETLImportDataJob.class.name)
        trigger.setJobGroup('tdstm-etl-import-data')
        quartzScheduler.scheduleJob(trigger)

        log.info('scheduleJob() {} kicked of an ETL Import data process for filename ({})', userLogin.username, filename)

        return ['progressKey': key, 'jobTriggerName': jobTriggerName]
    }
}