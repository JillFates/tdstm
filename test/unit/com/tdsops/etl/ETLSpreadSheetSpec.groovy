package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
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
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.FileSystemService

/**
 * Using SpreadSheet in ETL script. It manages the following commands:
 * <ul>
 *     <li><b>sheet Devices</b></li>
 *     <li><b>sheet 'Production Apps'</b></li>
 *     <li><b>read labels on 2</b></li>
 * </ul>
 */
@TestFor(FileSystemService)
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

		validator = new DomainClassFieldsValidator()
		validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Storage, buildFieldSpecsFor(AssetClass.STORAGE))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Device, buildFieldSpecsFor(AssetClass.DEVICE))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Asset, buildFieldSpecsFor(CustomDomainService.COMMON))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Dependency, buildFieldSpecsFor(ETLDomain.Dependency))

		debugConsole = new DebugConsole(buffer: new StringBuffer())
	}

	void 'test can define a sheet for a spreadSheet DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

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
						
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.result.domains.size() == 0

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can define more than one sheet for a spreadSheet DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSetWithMultipleSheets(
				[
					'Applications': ApplicationDataSet,
					'Devices': DeviceDataSet
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
			etlProcessor.result.domains.size() == 0

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels by default in first row by default for a spreadSheet DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

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
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.result.domains.size() == 0

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
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can define a quoted string sheet for a spreadSheet DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications Tab', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						sheet 'Applications Tab'
						read labels
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.result.domains.size() == 0

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
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels by an ordinal sheet number for a spreadSheet DataSet'(){
				given:
					def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

				and:
					ETLProcessor etlProcessor = new ETLProcessor(
						GMDEMO,
						dataSet,
						debugConsole,
						validator)

				when: 'The ETL script is evaluated'
					new GroovyShell(this.class.classLoader, etlProcessor.binding)
						.evaluate("""
								sheet 0
								read labels
								""".stripIndent(),
						ETLProcessor.class.name)

				then: 'DataSet was modified by the ETL script'
					etlProcessor.result.domains.size() == 0

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
					if(fileName) service.deleteTemporaryFile(fileName)
			}

	void 'test can throw an exception if sheet number is incorrect for a spreadSheet DataSet'(){
		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						sheet 10
						read labels
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Sheet 10 is not found in workbook"

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an exception if sheet name is incorrect for a spreadSheet DataSet'(){
		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						sheet 'Active Applications'
						read labels
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Sheet 'Active Applications' is not found in workbook"

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels by default using sheet number zero for a spreadSheet DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						read labels
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.result.domains.size() == 0

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
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels using sheet number as a sheet name for a spreadSheet DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('2', ApplicationDataSet)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						sheet '2'
						read labels
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.result.domains.size() == 0

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
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels skipping rows for a spreadSheet DataSet'(){

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
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						sheet 'Applications'
						skip 1
						read labels
						
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.result.domains.size() == 0

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
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read iterate rows for a spreadSheet DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

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
						iterate {
							domain Application
							extract 'vendor name' load 'Vendor'
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'DataSet was modified by the ETL script'
			etlProcessor.result.domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.result.domains[0]) {
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
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read labels skipping rows before for a spreadSheet DataSet'(){

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
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						skip 1
						read labels
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results contains'
			etlProcessor.result.domains.size() == 0

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
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read rows skipping rows before an iteration for a spreadSheet DataSet'(){

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
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						sheet 'Applications'
						skip 1
						read labels
						skip 1
						domain Application
						iterate {
							extract 'vendor name' load 'Vendor'
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results contains'
			etlProcessor.result.domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 1
				with(data[0].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can read rows ignoring rows in the middle of an iteration for a spreadSheet DataSet'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildSpreadSheetDataSet('Applications', ApplicationDataSet)

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
						
						iterate {
							domain Application
							
							extract 'vendor name' load 'Vendor'
							if(CE == 'Microsoft'){
								ignore row
							}
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results contains'
			etlProcessor.result.domains.size() == 1

		and: 'Results contains values'
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				data.size() == 1
				with(data[0].fields.appVendor) {
					originalValue == 'Mozilla'
					value == 'Mozilla'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
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
