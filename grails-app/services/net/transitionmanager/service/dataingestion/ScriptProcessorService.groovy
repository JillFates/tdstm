package net.transitionmanager.service.dataingestion

import com.tdsops.etl.*
import com.tdsops.tm.enums.domain.AssetClass
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.utils.FileUtils
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService

@Transactional
@Slf4j
class ScriptProcessorService {

    CustomDomainService customDomainService

    /**
     *
     * It can executes an script content using a ETL Processor
     *
     * @param project
     * @param scriptContent
     * @param data
     * @return
     */
    ETLProcessor executeForDemo (Project project, String scriptContent, List<List<String>> data) {

        log.info 'Created temporary file {}{}'
        DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        def configureUsingDomain = { AssetClass assetClass ->
            customDomainService.allFieldSpecs(project, assetClass.name())[assetClass.name()]["fields"]
        }

        ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()

        validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, configureUsingDomain(AssetClass.APPLICATION))
        validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, configureUsingDomain(AssetClass.DEVICE))
        validator.addAssetClassFieldsSpecFor(AssetClass.DATABASE, configureUsingDomain(AssetClass.DATABASE))
        validator.addAssetClassFieldsSpecFor(AssetClass.STORAGE, configureUsingDomain(AssetClass.STORAGE))

        ETLProcessor etlProcessor = new ETLProcessor(data, console, validator, [
                uppercase: new ElementTransformation(closure: { it.value = it.value.toUpperCase() }),
                lowercase: new ElementTransformation(closure: { it.value = it.value.toLowerCase() }),
                first    : new ElementTransformation(closure: { String value -> value.size() > 0 ? value[0] : "" }),
                blanks   : new ElementTransformation(closure: { String value -> value.replaceAll(" ", "") })
        ])

        ETLBinding binding = new ETLBinding(etlProcessor)

        new GroovyShell(this.class.classLoader, binding).evaluate(scriptContent?.trim(), ETLProcessor.class.name)

        etlProcessor
    }

    /**
     *
     * It can executes an script content using a ETL Processor with a file input and a script content
     *
     * @param project
     * @param scriptContent
     * @param fileName a fully qualified filename
     * @return
     */

    ETLProcessor executeForDemo (Project project, String scriptContent, String fileName) {

        CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fileName))
        CSVDataset dataset = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fileName), header: true)

        DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        def configureUsingDomain = { AssetClass assetClass ->
            customDomainService.allFieldSpecs(project, assetClass.name())[assetClass.name()]["fields"]
        }

        ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()

        validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, configureUsingDomain(AssetClass.APPLICATION))
        validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, configureUsingDomain(AssetClass.DEVICE))
        validator.addAssetClassFieldsSpecFor(AssetClass.DATABASE, configureUsingDomain(AssetClass.DATABASE))
        validator.addAssetClassFieldsSpecFor(AssetClass.STORAGE, configureUsingDomain(AssetClass.STORAGE))

        List<List<String>> data = []

        def fields = dataset.connection.driver.fields(dataset)*.name

        data.add(fields)

        dataset.eachRow { row ->
            data.add(fields.collect { columnName ->
                row[columnName] ?: ""
            })
        }

        ETLProcessor etlProcessor = new ETLProcessor(data, console, validator, [
                uppercase: new ElementTransformation(closure: { it.value = it.value.toUpperCase() }),
                lowercase: new ElementTransformation(closure: { it.value = it.value.toLowerCase() }),
                first    : new ElementTransformation(closure: { String value -> value.size() > 0 ? value[0] : "" }),
                blanks   : new ElementTransformation(closure: { String value -> value.replaceAll(" ", "") })
        ])

        ETLBinding binding = new ETLBinding(etlProcessor)

        new GroovyShell(this.class.classLoader, binding).evaluate(scriptContent?.trim(), ETLProcessor.class.name)

        etlProcessor
    }
    /**
     *
     * @param project
     * @param scriptContent
     * @param fileName
     * @return
     */
    Map<ETLDomain, List<ReferenceResult>> execute (Project project, String scriptContent, String fileName) {

        CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fileName))
        CSVDataset dataset = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fileName), header: true)

        DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        def configureUsingDomain = { AssetClass assetClass ->
            customDomainService.allFieldSpecs(project, assetClass.name())[assetClass.name()]["fields"]
        }

        ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()

        validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, configureUsingDomain(AssetClass.APPLICATION))
        validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, configureUsingDomain(AssetClass.DEVICE))
        validator.addAssetClassFieldsSpecFor(AssetClass.DATABASE, configureUsingDomain(AssetClass.DATABASE))
        validator.addAssetClassFieldsSpecFor(AssetClass.STORAGE, configureUsingDomain(AssetClass.STORAGE))

        List<List<String>> data = []

        def fields = dataset.connection.driver.fields(dataset)*.name

        data.add(fields)

        dataset.eachRow { row ->
            data.add(fields.collect { columnName ->
                row[columnName] ?: ""
            })
        }

        ETLProcessor etlProcessor = new ETLProcessor(data, console, validator, [
                uppercase: new ElementTransformation(closure: { it.value = it.value.toUpperCase() }),
                lowercase: new ElementTransformation(closure: { it.value = it.value.toLowerCase() }),
                first    : new ElementTransformation(closure: { String value -> value.size() > 0 ? value[0] : "" }),
                blanks   : new ElementTransformation(closure: { String value -> value.replaceAll(" ", "") })
        ])

        ETLBinding binding = new ETLBinding(etlProcessor)

        new GroovyShell(this.class.classLoader, binding).evaluate(scriptContent?.trim(), ETLProcessor.class.name)

        etlProcessor.results
    }

}
