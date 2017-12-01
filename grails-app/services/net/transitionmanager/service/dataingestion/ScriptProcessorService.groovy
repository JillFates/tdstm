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
     * @param project
     * @param scriptContent
     * @param fileName
     * @return
     */
    Map<ETLDomain, List<ReferenceResult>> execute (Project project, String scriptContent, String fileName) {
        process(project, scriptContent, fileName).results
    }
    /**
     *
     * Execute a DSL script using an instance of ETLProcessor using a project as a reference and a file as an input of the ETL content data
     *
     * @param project
     * @param scriptContent
     * @param fileName
     * @return and instance of ETLProcessor used to process the scriptContent
     */
    ETLProcessor process (Project project, String scriptContent, String fileName) {

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

        ETLProcessor etlProcessor = new ETLProcessor(project, dataset, console, validator)

        new GroovyShell(this.class.classLoader, etlProcessor.binding).evaluate(scriptContent?.trim(), ETLProcessor.class.name)

        etlProcessor
    }

}