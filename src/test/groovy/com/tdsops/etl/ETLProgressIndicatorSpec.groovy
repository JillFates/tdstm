package com.tdsops.etl

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.etl.dataset.ETLDataset
import grails.testing.gorm.DataTest
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.imports.DataScript
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project

import static com.tdsops.etl.ProgressCallback.ProgressStatus.RUNNING

class ETLProgressIndicatorSpec extends ETLBaseSpec implements DataTest {

	Project GMDEMO
	DebugConsole debugConsole
	ETLFieldsValidator validator

    Closure doWithSpring() {
        { ->
            coreService(CoreService) {
                grailsApplication = ref('grailsApplication')
            }
            fileSystemService(FileSystemService) {
                coreService = ref('coreService')
            }
            applicationContextHolder(ApplicationContextHolder) { bean ->
                bean.factoryMethod = 'getInstance'
            }
        }
    }

	void setupSpec(){
		mockDomains DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model
	}

	def setup() {
		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l
		debugConsole = new DebugConsole(buffer: new StringBuilder())
		validator = createDomainClassFieldsValidator()
	}

	void 'test can count number of iterate loop used in a simple ETL script using a ProgressCallback'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetXLSXDataSet('Applications', """
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
			""".stripIndent().trim())

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
				sheet 'Applications'
				read labels
				domain Device
				iterate {
					extract 'name' load 'assetName'
				}
				iterate {
					extract 'name' load 'assetName'
				}
			""".stripIndent(),
				callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 2

		cleanup:
			if (fileName){
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	void 'test can count zero loops used in a simple ETL script using a ProgressCallback'() {
		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
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

		cleanup:
			if (fileName){
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	void 'test cannot count number of iterate loop used in a simple ETL script without a ProgressCallback'() {
		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
			etlProcessor.numberOfIterateLoops == 0

		cleanup:
			if (fileName){
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	void 'test can count number of iterate loop used in a complex ETL script'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetXLSXDataSet('Applications', """
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
			""".stripIndent().trim())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				debugConsole,
				validator)

		and:
			ProgressCallback callback = Mock(ProgressCallback)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate('''
				sheet 'Applications'
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
				'''.stripIndent(),
				callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 4

		cleanup:
			if (fileName){
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	void 'test can count create a ProgressCallback using a simple closure'() {
		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
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
				debugConsole,
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
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	void 'test can report progress using an iterator'() {
		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				debugConsole,
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
			with(callback) {
				1 * reportProgress(0, true, RUNNING, '')
				1 * reportProgress(50, false, RUNNING, '')
				1 * reportProgress(100, false, RUNNING, '')
				1 * reportProgress(100, true, RUNNING, '')
			}

		cleanup:
			if (fileName){
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	void 'test can report progress using a iterator using from and to'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetXLSXDataSet('Applications', """
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
				xraysrv03,Dell,PE2952,Server
				xraysrv04,Dell,PE2953,Server
			""".stripIndent().trim())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				debugConsole,
				validator)

		and:
			ProgressCallback callback = Mock(ProgressCallback)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				sheet 'Applications'
				read labels
				domain Device
				from 1 to 2 iterate {
					extract 'name' load 'assetName'
				}
			""".stripIndent(),
				callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 1

		and:
			with(callback) {
				1 * reportProgress(0, true, RUNNING, '')
				1 * reportProgress(50, false, RUNNING, '')
				1 * reportProgress(100, false, RUNNING, '')
				1 * reportProgress(100, true, RUNNING, '')
			}

		cleanup:
			if (fileName){
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	void 'test can report progress without having an iterator'() {
		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				debugConsole,
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

		and: 'Only report progress at the beginning and of the ETL script execution'
			1 * callback.reportProgress(0, true, RUNNING, '')

		and: 'It does not report the end of the ETL script execution'
			0 * callback.reportProgress(100, true, RUNNING, '')

		and: 'It does not report any other intermediate progress'
			0 * callback.reportProgress(_, false, RUNNING, '')

		cleanup:
			if (fileName){
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

	void 'test can report progress in an empty ETL script'() {
		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				name,mfg,model,type
				xraysrv01,Dell,PE2950,Server
				xraysrv02,Dell,PE2951,Server
				""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GroovyMock(Project),
				dataSet,
				debugConsole,
				validator)

		and:
			ProgressCallback callback = Mock(ProgressCallback)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("", callback)

		then: 'It calculates correctly the total amount of iterate commands'
			etlProcessor.numberOfIterateLoops == 0

		and: 'Only report progress at the beginning and of the ETL script execution'
			1 * callback.reportProgress(0, true, RUNNING, '')

		and: 'It does not report the end of the ETL script execution'
			0 * callback.reportProgress(100, true, RUNNING, '')

		and: 'It does not report any other intermediate progress'
			0 * callback.reportProgress(_, false, RUNNING, '')

		cleanup:
			if (fileName){
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}
}
