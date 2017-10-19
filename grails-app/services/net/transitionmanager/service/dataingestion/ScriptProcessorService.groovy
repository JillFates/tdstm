package net.transitionmanager.service.dataingestion

import com.tdsops.etl.*
import com.tdsops.tm.enums.domain.AssetClass
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.tfs.TFS
import grails.transaction.Transactional
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService

@Transactional
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
    ETLProcessor execute (Project project, String scriptContent, List<List<String>> data) {

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
     * @param filename
     * @return
     */
    ETLProcessor execute (Project project, String scriptContent, String filename) {


        CSVConnection csvCon = new CSVConnection(config: "csv", path: TFS.systemPath)
        CSVDataset dataset = new CSVDataset(connection: csvCon, fileName: filename, header: true)

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
                row[columnName]?:""
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

}
