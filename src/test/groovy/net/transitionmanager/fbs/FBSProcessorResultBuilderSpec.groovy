package net.transitionmanager.fbs

import com.google.flatbuffers.FlatBufferBuilder
import com.tdsops.ETLTagValidator
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.ETLBaseSpec
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.dataset.ETLDataset
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.mixin.Mock
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.imports.DataScript
import net.transitionmanager.project.Project
import spock.lang.See
import spock.util.mop.ConfineMetaClassChanges

@Mock([DataScript, AssetDependency, AssetEntity, Application, Database])
class FBSProcessorResultBuilderSpec extends ETLBaseSpec {

    static doWithSpring = {
        coreService(CoreService) {
            grailsApplication = ref('grailsApplication')
        }
        fileSystemService(FileSystemService) {
            coreService = ref('coreService')
        }
    }

    Project GMDEMO
    Project TMDEMO
    DebugConsole debugConsole
    ETLFieldsValidator validator
    ETLTagValidator tagValidator

    def setup() {
        TMDEMO = Mock(Project)
        TMDEMO.getId() >> 125612l
        GMDEMO = Mock(Project)
        GMDEMO.getId() >> 125612l
        validator = createDomainClassFieldsValidator()
        debugConsole = new DebugConsole(buffer: new StringBuilder())

        tagValidator = new ETLTagValidator('GDPR', 5l)
        tagValidator.addTag('HIPPA', 6l)
        tagValidator.addTag('PCI', 7l)
        tagValidator.addTag('SOX', 8l)
    }

    @ConfineMetaClassChanges([AssetEntity])
    @See('TM-16851')
    void 'test can map a ETLProcessorResult instance to a FlatBufferBuilder instance'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
                application id,vendor name,technology,location
                152254,Microsoft,(xlsx updated),ACME Data Center
                152255,Mozilla,NGM,ACME Data Center""".stripIndent()
			)

		and:
			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					debugConsole,
					validator,
					tagValidator)

		and: 'The ETL script evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					load 'environment' with 'Production'
					extract 'technology' set name
					extract 'application id' transform with toLong() load 'Id'

					find Application by 'Id' with SOURCE.'application id' into 'Id'
					
					tagAdd 'GDPR', 'PCI'
					tagRemove 'HIPPA'
					tagReplace 'PCI', 'SOX'
					
					load 'comments' with SOURCE['location']
					
					whenNotFound 'id' create {
					     assetName name   
					     environment 'Production'
					     description 'Created by ETL Script'
					}
					
					whenFound 'id' update {
					    description 'Updated by ETL Script'
					}
					
				}
			""".stripIndent())

        when: 'an instance of FBSProcessorResult is created from an instance of ETLProcessorResult'
		    FBSProcessorResultBuilder mapper = new FBSProcessorResultBuilder(etlProcessor.finalResult(true))
            FBSProcessorResult result = mapper.build()

		then: 'Serialized results contains correctly all ETLProcessorResult values'
            result.consoleLog().startsWith('INFO - Reading labels [0:application id, 1:vendor name, 2:technology, 3:location]')
            result.version() == 2l
            result.FBSInfo().originalFilename().endsWith(fileName)
            result.domainsLength() == 1

        and:
                result.domains(0).domain() == ETLDomain.Application.name()
                result.domains(0).fieldNamesLength() == 3
                result.domains(0).fieldNames(0) == 'environment'
                result.domains(0).fieldNames(1) == 'id'
                result.domains(0).fieldNames(2) == 'Id'

                result.domains(0).fieldLabelMapLength() == 4
                result.domains(0).fieldLabelMap(0).name() == 'assetName'
                result.domains(0).fieldLabelMap(0).label() == 'Name'
                result.domains(0).fieldLabelMap(1).name() == 'description'
                result.domains(0).fieldLabelMap(1).label() == 'Description'
                result.domains(0).fieldLabelMap(2).name() == 'environment'
                result.domains(0).fieldLabelMap(2).label() == 'Environment'
                result.domains(0).fieldLabelMap(3).name() == 'id'
                result.domains(0).fieldLabelMap(3).label() == 'Id'
                result.domains(0).dataLength() == 2

         and:
                // First Row
                // Fields section
                result.domains(0).data(0).fieldsLength() == 2
                result.domains(0).data(0).fields(0).key() == 'environment'
                result.domains(0).data(0).fields(0).originalValue() == 'Production'
                result.domains(0).data(0).fields(0).value() == 'Production'
                !result.domains(0).data(0).fields(0).init()
                result.domains(0).data(0).fields(0).type() == 'String'
                result.domains(0).data(0).fields(0).errorsLength() == 0
                //find section
                !result.domains(0).data(0).fields(0).find() // should have a value!
                result.domains(0).data(0).fields(0).createLength() == 0 //1
                result.domains(0).data(0).fields(0).updateLength() == 0 //1


                result.domains(0).data(0).fields(1).key() == 'id'
                result.domains(0).data(0).fields(1).originalValue() == '152254'
                result.domains(0).data(0).fields(1).value() == '152254'
                !result.domains(0).data(0).fields(1).init()
                result.domains(0).data(0).fields(1).type() == 'Long'

                result.domains(0).data(0).fields(1).createLength() == 0 //1
                result.domains(0).data(0).fields(1).updateLength() == 0 //1
                !result.domains(0).data(0).fields(1).find() // should have a value!

                // tags section
                result.domains(0).data(0).tags().addLength() == 2
                result.domains(0).data(0).tags().add(0) == 'GDPR'
                result.domains(0).data(0).tags().add(1) == 'PCI'
                result.domains(0).data(0).tags().removeLength() == 1
                result.domains(0).data(0).tags().remove(0) == 'HIPPA'
                result.domains(0).data(0).tags().replaceLength() == 1
                result.domains(0).data(0).tags().replace(0).key() == 'PCI'
                result.domains(0).data(0).tags().replace(0).value() == 'SOX'
                // comments section
                result.domains(0).data(0).commentsLength() == 1
                result.domains(0).data(0).comments(0) == 'ACME Data Center'

         and:
                //Second Row
                result.domains(0).data(1).commentsLength() == 1
                result.domains(0).data(1).comments(0) == 'ACME Data Center'
                // tags section
                result.domains(0).data(0).tags().addLength() == 2
                result.domains(0).data(0).tags().add(0) == 'GDPR'
                result.domains(0).data(0).tags().add(1) == 'PCI'
                result.domains(0).data(0).tags().removeLength() == 1
                result.domains(0).data(0).tags().remove(0) == 'HIPPA'
                result.domains(0).data(0).tags().replaceLength() == 1
                result.domains(0).data(0).tags().replace(0).key() == 'PCI'
                result.domains(0).data(0).tags().replace(0).value() == 'SOX'

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)

	}



    void 'test using FlatBufferBuilder'() {

        setup:
            FlatBufferBuilder builder = new FlatBufferBuilder(1024)

        when:
            int applicationOffset = builder.createString('Application')
            builder.addOffset(0, applicationOffset, 0)
            builder.addInt(1, 100, 0)
            builder.addInt(2, 300, 0)
            int end = builder.endObject()
            builder.finish(end)

        then:
            builder.sizedByteArray() != null

    }
}
