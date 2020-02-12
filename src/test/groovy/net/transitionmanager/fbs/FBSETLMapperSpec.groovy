package net.transitionmanager.fbs

import com.google.flatbuffers.FlatBufferBuilder
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.ETLBaseSpec
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

import java.nio.ByteBuffer

@Mock([DataScript, AssetDependency, AssetEntity, Application, Database])
class FBSETLMapperSpec extends ETLBaseSpec {

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

    def setup() {
        TMDEMO = Mock(Project)
        TMDEMO.getId() >> 125612l
        GMDEMO = Mock(Project)
        GMDEMO.getId() >> 125612l
        validator = createDomainClassFieldsValidator()
        debugConsole = new DebugConsole(buffer: new StringBuilder())
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
					validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				iterate {
					domain Application
					load 'environment' with 'Production'
					extract 'application id' load 'Id'

					find Application by 'Id' with SOURCE.'application id' into 'Id'
					
				}
			""".stripIndent())

			

		    ETLFlatBuffersConverter mapper = new ETLFlatBuffersConverter(etlProcessor.finalResult())
		    FlatBufferBuilder bufferBuilder = mapper.build()

		    ByteBuffer buf = bufferBuilder.dataBuffer()
            FBSProcessorResult result = FBSProcessorResult.getRootAsFBSProcessorResult(buf)

		then: ''
            result != null

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
