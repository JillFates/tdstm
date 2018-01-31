package net.transitionmanager.service.dataingestion

import com.tdsops.etl.*
import com.tdsops.tm.enums.domain.AssetClass
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.utils.FileUtils
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException

@Transactional
@Slf4j
class ScriptProcessorService {

    CustomDomainService customDomainService

    /**
     *
     * @param project
     * @param scriptContent
     * @param fileName
     * @return
     */
    ETLProcessorResult execute (Project project, String scriptContent, String fileName) {
        return process(project, scriptContent, fileName).result
    }

    /**
     * Execute a DSL script using an instance of ETLProcessor using a project as a reference
     * and a file as an input of the ETL content data
     * @param project
     * @param scriptContent
     * @param fileName
     * @return and instance of ETLProcessor used to process the scriptContent
     */
    ETLProcessor process (Project project, String scriptContent, String fileName) {

        CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fileName))
        CSVDataset dataset = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fileName), header: true)

        DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        ETLAssetClassFieldsValidator validator = createFieldsSpecValidator(project)

        ETLProcessor etlProcessor = new ETLProcessor(project, dataset, console, validator)

        new GroovyShell(this.class.classLoader, etlProcessor.binding).evaluate(scriptContent?.trim(), ETLProcessor.class.name)

        return etlProcessor
    }

    /**
     * Base on a project it creates a ETLAssetClassFieldsValidator instance tha implements ETLFieldsValidator.
     * @param project a defined Project instance to be used in fields spec request
     * @see ETLFieldsValidator interface
     * @return an instance of ETLAssetClassFieldsValidator.
     */
    private ETLAssetClassFieldsValidator createFieldsSpecValidator (Project project) {

        def configureUsingDomain = { AssetClass assetClass ->
            customDomainService.allFieldSpecs(project, assetClass.name())[assetClass.name()]["fields"]
        }

        ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()

        validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, configureUsingDomain(AssetClass.APPLICATION))
        validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, configureUsingDomain(AssetClass.DEVICE))
        validator.addAssetClassFieldsSpecFor(AssetClass.DATABASE, configureUsingDomain(AssetClass.DATABASE))
        validator.addAssetClassFieldsSpecFor(AssetClass.STORAGE, configureUsingDomain(AssetClass.STORAGE))
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
            CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fileName))

            etlProcessor = new ETLProcessor(project,
                    new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fileName), header: true),
                    new DebugConsole(buffer: new StringBuffer()),
                    createFieldsSpecValidator(project))

            new GroovyShell(this.class.classLoader, etlProcessor.binding).evaluate(scriptContent?.trim(), ETLProcessor.class.name)

            result.isValid = true
        } catch (all) {
            log.warn('Error testing script: ' + all.getMessage(), all)
            result.error = all.getMessage()
        }

        result.consoleLog = etlProcessor?.debugConsole?.content()
        result.data = etlProcessor?.result.domains

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

        CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fileName))
        CSVDataset dataset = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fileName), header: true)

        DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        ETLProcessor etlProcessor = new ETLProcessor(project, dataset, console, new ETLAssetClassFieldsValidator())

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
     * Updates a DataScript instance with a scriptContent String instance
     * @param dataScript an instance of DataScript already saved in database
     * @param scriptContent the new ETL Script content to be saved in DataScript instance
     * @return the DataScript instance with the etlSourceCode already updated in database
     */
    DataScript saveScript (DataScript dataScript, String scriptContent) {

        log.info "Updating DataScript ID: ${dataScript.id} with the following script content: ${scriptContent}"
        dataScript.etlSourceCode = scriptContent
        dataScript.save(failOnError: true)
        return dataScript
    }

}