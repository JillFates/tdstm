package net.transitionmanager.service.dataingestion

import com.tdsops.etl.DataScriptValidateScriptCommand
import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.DomainClassFieldsValidator
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.StringUtil
import getl.data.Dataset
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.ProgressService
import net.transitionmanager.service.SecurityService
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl

@Transactional
@Slf4j
class ScriptProcessorService {

    CustomDomainService customDomainService
	FileSystemService fileSystemService
	SecurityService securityService
	ProgressService progressService
	Scheduler quartzScheduler

    /**
     * Execute a DSL script using an instance of ETLProcessor using a project as a reference
     * and a file as an input of the ETL content data
     * @param project
     * @param scriptContent
     * @param fileName
     * @return and instance of ETLProcessor used to execute the scriptContent
     */
    ETLProcessor execute (Project project, String scriptContent, String fileName) {

        Dataset dataset = FileSystemService.buildDataset(fileName)

        DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        DomainClassFieldsValidator validator = createFieldsSpecValidator(project)

        ETLProcessor etlProcessor = new ETLProcessor(project, new DataSetFacade(dataset), console, validator)

	    etlProcessor.evaluate(scriptContent?.trim())

        return etlProcessor
    }

    /**
     * Base on a project it creates a DomainClassFieldsValidator instance tha implements ETLFieldsValidator.
     * @param project a defined Project instance to be used in fields spec request
     * @see com.tdsops.etl.ETLFieldsValidator interface
     * @return an instance of DomainClassFieldsValidator.
     */
    private DomainClassFieldsValidator createFieldsSpecValidator (Project project) {

	    Map<String, ?> fieldsSpecMap = customDomainService.fieldSpecsWithCommon(project)

	    Map<String, ?> commonFieldsSpec = fieldsSpecMap[CustomDomainService.COMMON]
	    Map<String, ?> applicationFieldsSpec = fieldsSpecMap[AssetClass.APPLICATION.name()]
	    Map<String, ?> deviceFieldsSpec = fieldsSpecMap[AssetClass.DEVICE.name()]
	    Map<String, ?> storageFieldsSpec = fieldsSpecMap[AssetClass.STORAGE.name()]
	    Map<String, ?> dataBaseFieldsSpec = fieldsSpecMap[AssetClass.DATABASE.name()]

	    DomainClassFieldsValidator validator = new DomainClassFieldsValidator()
	    validator.addAssetClassFieldsSpecFor(ETLDomain.Application, commonFieldsSpec.fields + applicationFieldsSpec.fields)
	    validator.addAssetClassFieldsSpecFor(ETLDomain.Device, commonFieldsSpec.fields + deviceFieldsSpec.fields)
	    validator.addAssetClassFieldsSpecFor(ETLDomain.Storage, commonFieldsSpec.fields + storageFieldsSpec.fields)
	    validator.addAssetClassFieldsSpecFor(ETLDomain.Database, commonFieldsSpec.fields + dataBaseFieldsSpec.fields)
	    validator.addAssetClassFieldsSpecFor(ETLDomain.Asset, commonFieldsSpec.fields)

        return validator
    }

    /**
     * Test a DSL script using an instance of ETLProcessor
     * using a project as a reference and a file as an input of the ETL content data
     * @param project
     * @param scriptContent
     * @param fileName
     * @return a map with isValid boolean result, the console output log and ETLProcessor data results
     */
    Map<String, ?> testScript(Project project, String scriptContent, String filename, String progressKey = null) {

        ETLProcessor etlProcessor
        Map<String, ?> result = [isValid: false]

        try {

	        Dataset dataset = FileSystemService.buildDataset(filename)

            etlProcessor = new ETLProcessor(project,
                    new DataSetFacade(dataset),
                    new DebugConsole(buffer: new StringBuffer()),
                    createFieldsSpecValidator(project))

			// update progress closure
			def updateProgressClosure = { Integer percentComp, String status, String detail ->
				// if progress key is not provided, then just skip updating progress service
				// this is useful during integration test invocation
				if (progressKey) {
					progressService.update(progressKey, percentComp, status, detail)
				}
			}

			etlProcessor.evaluate(scriptContent?.trim()/*, updateProgressClosure*/)

            result.isValid = true
        } catch (all) {
            log.warn('Error testing script: ' + all.getMessage(), all)
            result.error = all.getMessage()
        }

	     if (etlProcessor) {
		     result.consoleLog = etlProcessor?.debugConsole?.content()
		     result.data = etlProcessor.resultsMap()
	     }

        return result
    }

	/**
	 * Method called by quartz job to test an ETL script
	 * @param projectId - user's current project
	 * @param scriptFilename - temporary test script filename
	 * @param filename - temporary data filename
	 * @param progressKey - progress key used for this ETL test interaction
	 * @return
	 */
	Map<String, ?> testScript(Long projectId, String scriptFilename, String filename, String progressKey = null) {
		// obtain test ETL script from temporary script file
		String scriptContent = fileSystemService.openTemporaryFile(scriptFilename).text

		// build test data dataset from temporary file
		String sampleDataFullFilename = fileSystemService.getTemporaryFullFilename(filename)

		// obtain project
		Project project = Project.get(projectId)

		// invoke test script with parameters passed by quartz job and return resulting data
		return testScript(project, scriptContent, sampleDataFullFilename, progressKey)
	}

    /**
     * Checks if the syntax for a script content is correct using GrooyShell.parse method.
     * All the errors are collected and returned in a map like the following example:
     * <code>
     * getErrorCollector().getErrors().collect { error ->
     *      [
     *          startLine: error.cause?.startLine,
     *          endLine: error.cause?.endLine,
     *          startColumn: error.cause?.startColumn,
     *          endColumn: error.cause?.endColumn,
     *          fatal: error.cause?.fatal,
     *          message: error.cause?.message
     *      ]
     *}* </code>
     * @param project
     * @param scriptContent
     * @param fileName
     * @return a map with validSyntax boolean result and a list with map erros
     */
    Map<String, ?> checkSyntax (Project project, String scriptContent, String fileName) {

	    Dataset dataset = FileSystemService.buildDataset(fileName)

        DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        ETLProcessor etlProcessor = new ETLProcessor(project, new DataSetFacade(dataset), console, new DomainClassFieldsValidator())

        List<Map<String, ?>> errors = []

        try {

            new GroovyShell(this.class.classLoader, etlProcessor.binding).parse(scriptContent?.trim(), ETLProcessor.class.name)

        } catch (MultipleCompilationErrorsException cfe) {
            ErrorCollector errorCollector = cfe.getErrorCollector()
            log.error("ETL script parse errors: " + errorCollector.getErrorCount(), cfe)
            errors = errorCollector.getErrors()
        }

        List errorsMap = errors.collect { error ->
            [
                    startLine  : error.cause?.startLine,
                    endLine    : error.cause?.endLine,
                    startColumn: error.cause?.startColumn,
                    endColumn  : error.cause?.endColumn,
                    fatal      : error.cause?.fatal,
                    message    : error.cause?.message
            ]
        }

        [
                validSyntax: errors.isEmpty(),
                errors     : errorsMap
        ]
    }

	/**
	 * Schedule a quartz job for the test ETL transform data process
	 * @param project - user's current project
	 * @param command - test ETL script and temporary data file name
	 * @return
	 */
	Map<String, String> scheduleTestScript(Project project, DataScriptValidateScriptCommand command) {

		// check if temporary data file still exists
		if (!fileSystemService.getTemporaryFullFilename(command.filename)) {
			throw new InvalidParamException('Invalid temporary data file name')
		}

		// create test script temporary file
		def (String scriptFilename, OutputStream os) = fileSystemService.createTemporaryFile('testETLScript')
		IOUtils.write(command.script, os)
		os.flush()
		os.close()

		// create progress key
		String key = 'ETL-Transform-Data-' + project.id + '-' + scriptFilename + '-' + StringUtil.generateGuid()
		progressService.create(key, ProgressService.PENDING)

		// Kickoff the background job to generate the tasks
		def jobTriggerName = 'TM-ETLTransformData-' + project.id + '-' + scriptFilename

		// The triggerName/Group will allow us to controller on import
		Date startTime = new Date(System.currentTimeMillis() + 2000)
		// Delay 2 seconds to allow this current transaction to commit before firing off the job

		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl(jobTriggerName, null, startTime)
		trigger.jobDataMap.projectId = project.id
		trigger.jobDataMap.scriptFilename = scriptFilename
		trigger.jobDataMap.filename = command.filename
		trigger.jobDataMap.progressKey = key
		trigger.setJobName('ETLTransformDataJob')
		trigger.setJobGroup('tdstm-etl-transform-data')
		quartzScheduler.scheduleJob(trigger)

		log.info('scheduleJob() {} kicked of an test ETL transform data process for script and filename ({},{})',
				securityService.currentUsername, scriptFilename, command.filename)

		// return progress key
		return ['progressKey': key]
	}
}