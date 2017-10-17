package net.transitionmanager.service.dataingestion

import com.tdsops.etl.*
import com.tdsops.tm.enums.domain.AssetClass
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
}
