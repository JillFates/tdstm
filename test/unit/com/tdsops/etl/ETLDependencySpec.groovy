package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService

/**
 * Test about ETL Current Element (CE):
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLDependencySpec extends ETLBaseSpec {

	DebugConsole debugConsole
	Project GMDEMO
	ETLFieldsValidator validator

	static doWithSpring = {
		coreService(CoreService) {
			grailsApplication = ref('grailsApplication')
		}
		fileSystemService(FileSystemService) {
			coreService = ref('coreService')
			transactionManager = ref('transactionManager')
		}
	}

	def setupSpec() {
		String.mixin StringAppendElement
	}

	def setup() {

		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l
		debugConsole = new DebugConsole(buffer: new StringBuilder())
		validator = createDomainClassFieldsValidator()
	}

	void 'test can assign DOMAIN variable'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model
				xraysrv01,Dell,PE2950
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					extract 'name' load 'Name'
					extract 'mfg' load 'Manufacturer'
					extract 'model' load 'Model'
				
					set assetResultVar with DOMAIN
					
					log(assetResultVar.'assetName')
					assert assetResultVar.'assetName' == 'xraysrv01'
					assert assetResultVar.'manufacturer' == 'Dell'
					assert assetResultVar.'model' == 'PE2950'
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement) {
				originalValue == 'PE2950'
				value == 'PE2950'
				init == null
				with (fieldDefinition, ETLFieldDefinition){
					name == 'model'
					label == 'Model'
				}
			}

		and: 'Results contains the following values'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'manufacturer', 'model'] as Set
					data.size() == 1
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if asset and dependent have null values'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model
				xraysrv01,Dell,PE2950
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					domain Dependency with null and null
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with (ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidDependentParamsCommand().message} at line 6"
				startLine == 6
				endLine == 6
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can create an asset for Dependency using domain command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model
				xraysrv01,Dell,PE2950
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					extract 'name' load 'Name'
					extract 'mfg' load 'Manufacturer'
					extract 'model' load 'Model'
				
					set assetResultVar with DOMAIN
					
					domain Dependency with assetResultVar
				}
			""".stripIndent())

		then: 'Results contains the following values'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'manufacturer', 'model'] as Set
					data.size() == 1
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

//	void 'test can create an event'() {
//		given:
//			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
//				name,description
//				Foo,Bar
//			""".stripIndent())
//
//		and:
//			ETLProcessor etlProcessor = new ETLProcessor(
//				GroovyMock(Project),
//				dataSet,
//				GroovyMock(DebugConsole),
//				validator)
//
//		when: 'The ETL script is evaluated'
//			etlProcessor.evaluate("""
//				console on
//				read labels
//				domain Event
//				iterate {
//					extract 'name' load 'name'
//					extract 'description' load 'description'
//				}
//			""".stripIndent())
//
//		then: 'Current element should contains values'
//
//		and: 'Results contains the following values'
//			with(etlProcessor.finalResult()) {
//				ETLInfo.originalFilename == fileName
//				domains.size() == 1
//				with(domains[0], DomainResult) {
//					domain == ETLDomain.Event.name()
//					fieldNames == ['name', 'description'] as Set
//					data.size() == 1
//				}
//			}
//
//		cleanup:
//			if (fileName) {
//				service.deleteTemporaryFile(fileName)
//			}
//	}

}
