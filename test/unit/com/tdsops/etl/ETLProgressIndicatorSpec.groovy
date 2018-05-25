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
import spock.lang.See

import static com.tdsops.etl.ProgressCallback.ProgressStatus.RUNNING

@TestFor (FileSystemService)
@Mock ([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
@See ('TM-10744')
class ETLProgressIndicatorSpec extends ETLBaseSpec {

	Project GMDEMO
	DebugConsole debugConsole
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

	def setup() {
		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l
		debugConsole = new DebugConsole(buffer: new StringBuffer())
		validator = createDomainClassFieldsValidator()
	}

	void 'test can count number of iterate loop used in a simple ETL script'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
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
					extract 'name' load 'assetName'
				}
				iterate {
					extract 'name' load 'assetName'
				}
			""".stripIndent())

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 2

		cleanup:
			if (fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can count number of iterate loop used in a complex ETL script'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate('''
				read labels
				console on
				
				iterate {
				
					extract 1	
				
				
				}
				
				 iterate{
					
					extract 1 
				 }
				
				 iterate       {extract 1 }
				
				 iterate
				 { extract 1 }
				'''.stripIndent())

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 4

		cleanup:
			if (fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can count create a ProgressCallback using a simple closure'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				""".stripIndent())

		and:
			ProgressCallback callback = {
				Integer progress,
				Boolean forceReport,
				ProgressCallback.ProgressStatus status,
				String detail ->

			} as ProgressCallback

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
					extract 'name' load 'assetName'
				}
			""".stripIndent(),
				callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 1

		cleanup:
			if (fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can report progress using an iterator'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		and:
			ProgressCallback callback = Mock(ProgressCallback)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					extract 'name' load 'assetName'
				}
			""".stripIndent(),
				callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 1

		and:
			1 * callback.reportProgress(0, true, RUNNING, '')

		and:
			1 * callback.reportProgress(50, false, RUNNING, '')

		and:
			1 * callback.reportProgress(100, false, RUNNING, '')

		and:
			1 * callback.reportProgress(100, true, RUNNING, '')

		cleanup:
			if (fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can report progress using 3 iterate loops'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		and:
			ProgressCallback callback = Mock(ProgressCallback)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					extract 'name' load 'assetName'
				}
				
				domain Application
				iterate {
					extract 'name' load 'assetName'
				}
				
				domain Database
				iterate {
					extract 'name' load 'assetName'
				}
				""".stripIndent(),
				callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 3

		and:
			1 * callback.reportProgress(0, true, RUNNING, '')

		and:
			1 * callback.reportProgress(33, true, RUNNING, '')

		and:
			1 * callback.reportProgress(67, true, RUNNING, '')

		and:
			1 * callback.reportProgress(100, true, RUNNING, '')

		cleanup:
			if (fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can report progress using more than one iterate loop'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		and:
			ProgressCallback callback = Mock(ProgressCallback)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					extract 'name' load 'assetName'
				}
				
				domain Application
				iterate {
					extract 'name' load 'assetName'
				}
			""".stripIndent(),
				callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 2

		and:
			1 * callback.reportProgress(0, true, RUNNING, '')

		and:
			1 * callback.reportProgress(25, false, RUNNING, '')

		and:
			1 * callback.reportProgress(50, false, RUNNING, '')

		and:
			1 * callback.reportProgress(50, true, RUNNING, '')

		and:
			1 * callback.reportProgress(75, false, RUNNING, '')

		and:
			1 * callback.reportProgress(100, false, RUNNING, '')

		and:
			1 * callback.reportProgress(100, true, RUNNING, '')


		cleanup:
			if (fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can report progress without using an iterate loop'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		and:
			ProgressCallback callback = Mock(ProgressCallback)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
			""".stripIndent(),
				callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 0

		and:
			1 * callback.reportProgress(0, true, RUNNING, '')

		cleanup:
			if (fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can report progress using more than one iterate loop with different number the rows'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
				xraysrv03,Dell,PE2952,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				GroovyMock(DebugConsole),
				validator)

		and:
			ProgressCallback callback = Mock(ProgressCallback)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					extract 'name' load 'assetName'
				}
				
				domain Application
				from 1 to 2 iterate {
					extract 'name' load 'assetName'
				}
			""".stripIndent(),
				callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 2

		and:
			1 * callback.reportProgress(0, true, RUNNING, '')

		and:
			1 * callback.reportProgress(25, false, RUNNING, '')

		and:
			1 * callback.reportProgress(50, false, RUNNING, '')

		and:
			1 * callback.reportProgress(50, true, RUNNING, '')

		and:
			1 * callback.reportProgress(75, false, RUNNING, '')

		and:
			1 * callback.reportProgress(100, false, RUNNING, '')

		and:
			1 * callback.reportProgress(100, true, RUNNING, '')


		cleanup:
			if (fileName){
				service.deleteTemporaryFile(fileName)
			}
	}
}
