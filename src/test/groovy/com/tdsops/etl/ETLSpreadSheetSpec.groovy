package com.tdsops.etl

import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import grails.test.mixin.Mock
import net.transitionmanager.imports.DataScript
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService

/**
 * Using SpreadSheet in ETL script. It manages the following commands:
 * <ul>
 *     <li><b>sheet Devices</b></li>
 *     <li><b>sheet 'Production Apps'</b></li>
 *     <li><b>read labels on 2</b></li>
 * </ul>
 */

@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLSpreadSheetSpec extends ETLBaseSpec {

	Project GMDEMO
	Project TMDEMO
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

	def setupSpec() {
		String.mixin StringAppendElement
	}

	def setup() {
		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l

		TMDEMO = Mock(Project)
		TMDEMO.getId() >> 125612l

		validator = createDomainClassFieldsValidator()

		debugConsole = new DebugConsole(buffer: new StringBuilder())
	}

	void 'test can define a sheet for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 'Applications'
					""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 0

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can define more than one sheet for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSetWithMultipleSheets(
				[
					'Applications': ApplicationDataSet,
					'Devices'     : DeviceDataSet
				]
			)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						sheet 'Applications'
						sheet 'Devices'
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 0

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read labels by default in first row by default for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 'Applications'
						read labels
						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 1

		and: 'A column map is created'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('vendor name').index == 1
			etlProcessor.column(1).label == 'vendor name'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('location').index == 3
			etlProcessor.column(3).label == 'location'

		and:
			etlProcessor.currentRowIndex == 1


		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can define a quoted string sheet for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications Tab', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 'Applications Tab'
						read labels
						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 1

		and: 'A column map is created'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('vendor name').index == 1
			etlProcessor.column(1).label == 'vendor name'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('location').index == 3
			etlProcessor.column(3).label == 'location'

		and:
			etlProcessor.currentRowIndex == 1
		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read labels by an ordinal sheet number for a spreadSheet DataSet'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
								sheet 0
								read labels
								""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 1

		and: 'A column map is created'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('vendor name').index == 1
			etlProcessor.column(1).label == 'vendor name'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('location').index == 3
			etlProcessor.column(3).label == 'location'

		and:
			etlProcessor.currentRowIndex == 1


		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception if sheet number is incorrect for a spreadSheet DataSet'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 10
						read labels
						""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Sheet number 10 not found in workbook'

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception if sheet name is incorrect for a spreadSheet DataSet'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 'Active Applications'
						read labels
						""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Sheet 'Active Applications' not found in workbook"

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception if sheet name case is incorrect for a spreadSheet DataSet'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 'applications'
						read labels
						""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Sheet 'applications' not found in workbook"

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read labels by default using sheet number zero for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						read labels
						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 1

		and: 'A column map is created'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('vendor name').index == 1
			etlProcessor.column(1).label == 'vendor name'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('location').index == 3
			etlProcessor.column(3).label == 'location'

		and:
			etlProcessor.currentRowIndex == 1


		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read labels using sheet number as a sheet name for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('2', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet '2'
						read labels
						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 1

		and: 'A column map is created'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('vendor name').index == 1
			etlProcessor.column(1).label == 'vendor name'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('location').index == 3
			etlProcessor.column(3).label == 'location'

		and:
			etlProcessor.currentRowIndex == 1


		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read labels skipping rows for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications',
				"invalid headers, are not part, of the valid\n" + ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 'Applications'
						skip 1
						read labels

						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 0
			etlProcessor.currentRowIndex == 2

		and: 'A column map is created'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('vendor name').index == 1
			etlProcessor.column(1).label == 'vendor name'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('location').index == 3
			etlProcessor.column(3).label == 'location'

		and:
			etlProcessor.currentRowIndex == 2


		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read iterate rows for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 'Applications'
						read labels
						iterate {
							domain Application
							extract 'vendor name' load 'Vendor'
						}
						""".stripIndent())

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 2
				with(data[0].fields.appVendor) {
					originalValue == 'Microsoft'
					value == 'Microsoft'
				}

				with(data[1].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read iterate rows for more than one sheet in a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSetWithMultipleSheets(
				[
					'Applications': ApplicationDataSet,
					'Devices'     : DeviceDataSet
				]
			)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
					sheet 'Applications'
					read labels
					domain Application
					iterate {
						extract 'vendor name' load 'Vendor'
					}

					sheet 'Devices'
					read labels
					domain Device
					iterate {
						extract 'name' load 'Name'
					}
					""".stripIndent(),
						  ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.finalResult().domains.size() == 2

		and: 'Results contains values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 2
				with(data[0].fields.appVendor) {
					originalValue == 'Microsoft'
					value == 'Microsoft'
				}

				with(data[1].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

			with(etlProcessor.finalResult().domains[1]) {
				domain == ETLDomain.Device.name()
				data.size() == 2
				with(data[0].fields.assetName) {
					originalValue == 'xraysrv01'
					value == 'xraysrv01'
				}

				with(data[1].fields.assetName) {
					originalValue == 'zuludb01'
					value == 'zuludb01'
				}
			}

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read labels skipping rows before for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications',
				"invalid headers, are not part, of the valid\n" + ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						skip 1
						read labels
						""".stripIndent())

		then: 'Results contains'
			etlProcessor.finalResult().domains.size() == 0

		and: 'Results contains values'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('vendor name').index == 1
			etlProcessor.column(1).label == 'vendor name'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('location').index == 3
			etlProcessor.column(3).label == 'location'

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read labels skipping more than one row before for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications',
				"invalid headers, are not part, of the valid\n" +
					"Another, Lines with, invalid, headers, are not part, of the valid\n" +
					ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						skip 2
						read labels
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results contains'
			etlProcessor.finalResult().domains.size() == 0

		and: 'Results contains values'
			etlProcessor.column('application id').index == 0
			etlProcessor.column(0).label == 'application id'

		and:
			etlProcessor.column('vendor name').index == 1
			etlProcessor.column(1).label == 'vendor name'

		and:
			etlProcessor.column('technology').index == 2
			etlProcessor.column(2).label == 'technology'

		and:
			etlProcessor.column('location').index == 3
			etlProcessor.column(3).label == 'location'

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read rows skipping rows before an iteration for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications',
				"invalid headers, are not part, of the valid\n" + ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 'Applications'
						skip 1
						read labels
						skip 1
						domain Application
						iterate {
							extract 'vendor name' load 'Vendor'
						}
						""".stripIndent())

		then: 'Results contains'
			etlProcessor.finalResult().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 1
				with(data[0].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can read rows ignoring rows in the middle of an iteration for a spreadSheet DataSet'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
						sheet 'Applications'
						read labels

						iterate {
							domain Application

							extract 'vendor name' load 'Vendor'
							if(CE == 'Microsoft'){
								ignore record
							}
						}
						""".stripIndent())

		then: 'Results contains'
			etlProcessor.finalResult().domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.finalResult().domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 1
				with(data[0].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if (fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	static final String ApplicationDataSet = """
		application id,vendor name,technology,location
		152254,Microsoft,(xlsx updated),ACME Data Center
		152255,Mozilla,NGM,ACME Data Center
		""".stripIndent().trim()

	static final String DeviceDataSet = """
		name,mfg,model,type
		xraysrv01,Dell,PE2950,Server
		zuludb01,HP,BL380,Blade
		""".stripIndent().trim()

}
