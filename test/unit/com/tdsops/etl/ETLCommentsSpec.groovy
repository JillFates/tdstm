package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.ImportOperationEnum
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
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


@TestMixin(ControllerUnitTestMixin)
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model, AssetOptions])
class ETLCommentsSpec extends ETLBaseSpec {

	static doWithSpring = {
		coreService(CoreService) {
			grailsApplication = ref('grailsApplication')
		}
		fileSystemService(FileSystemService) {
			coreService = ref('coreService')
			transactionManager = ref('transactionManager')
		}
	}

	DebugConsole debugConsole
	Project GMDEMO
	ETLFieldsValidator validator

	def setup() {

		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l
		debugConsole = new DebugConsole(buffer: new StringBuilder())
		validator = createDomainClassFieldsValidator()
	}


	@See('TM-11482')
	void 'test can add a comment to a Device'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GMDEMO, dataSet, debugConsole, validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Device
				iterate {
					extract 'name' load 'assetName' when populated
					extract 'description' load 'comments'
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
							init == null
						}
						comments == ['Description FOOBAR']
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.assetName) {
							value == 'zuludb01'
							originalValue == 'zuludb01'
							init == null
						}
						comments == ['Some description']
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11482')
	void 'test can add a comment using load command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GMDEMO, dataSet, debugConsole, validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Device
				iterate {
					extract 'name' load 'assetName' when populated
					load 'comments' with SOURCE['description']
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
							init == null
						}
						comments == ['Description FOOBAR']
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.assetName) {
							value == 'zuludb01'
							originalValue == 'zuludb01'
							init == null
						}
						comments == ['Some description']
					}
				}

			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11482')
	void 'test can add a comment using a ETL variable'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GMDEMO, dataSet, debugConsole, validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Device
				iterate {
					extract 'name' load 'assetName' when populated
					extract 'description' set descriptionVar
					load 'comments' with descriptionVar
				}
			""".stripIndent())

		then: 'Results should contain domain results associated'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1

				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 2
					with(data[0], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						with(fields.assetName) {
							value == 'xraysrv01'
							originalValue == 'xraysrv01'
							init == null
						}
						comments == ['Description FOOBAR']
					}

					with(data[1], RowResult) {
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						with(fields.assetName) {
							value == 'zuludb01'
							originalValue == 'zuludb01'
							init == null
						}
						comments == ['Some description']
					}
				}

			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-11482')
	void 'test can throw an Exception if it tries to attach a comment in a non Asset Entity domain'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(GMDEMO, dataSet, debugConsole, validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				domain Model
				iterate {
					load 'comments' with DOMAIN.description
				}
			""".stripIndent())

		then: 'It throws an Exception because comments command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidDomainForComments(ETLDomain.Model).message} at line 5".toString()
				startLine == 5
				endLine == 5
				startColumn == null
				endColumn == null
				fatal == true
			}


		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}
}
