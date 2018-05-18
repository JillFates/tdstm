package net.transitionmanager.service.dataingestion

import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.DomainClassFieldsValidator
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.tm.enums.domain.AssetClass
import getl.data.Dataset
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.FileSystemService
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException

@Transactional
@Slf4j
class ScriptProcessorService {

    CustomDomainService customDomainService

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
     * Test a a DSL script using an instance of ETLProcessor
     * using a project as a reference and a file as an input of the ETL content data
     * @param project
     * @param scriptContent
     * @param fileName
     * @return a map with isValid boolean result, the console output log and ETLProcessor data results
     */
    Map<String, ?> testScript (Project project, String scriptContent, String fileName) {

        ETLProcessor etlProcessor
        Map<String, ?> result = [isValid: false]

        try {

	        Dataset dataset = FileSystemService.buildDataset(fileName)

            etlProcessor = new ETLProcessor(project,
                    new DataSetFacade(dataset),
                    new DebugConsole(buffer: new StringBuffer()),
                    createFieldsSpecValidator(project))

	        etlProcessor.evaluate(scriptContent?.trim())

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

}