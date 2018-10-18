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
import spock.util.mop.ConfineMetaClassChanges

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
				GMDEMO,
				dataSet,
				debugConsole,
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
					
					assert assetResultVar.'assetName' == 'xraysrv01'
					assert assetResultVar.'Name' == 'xraysrv01'
					assert assetResultVar.'manufacturer' == 'Dell'
					assert assetResultVar.'Manufacturer' == 'Dell'
					assert assetResultVar.'model' == 'PE2950'
					assert assetResultVar.'Model' == 'PE2950'
				}
			""".stripIndent())

		then: 'Current element should contains values'
			with(etlProcessor.currentElement) {
				originalValue == 'PE2950'
				value == 'PE2950'
				init == null
				with(fieldDefinition, ETLFieldDefinition) {
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
				GMDEMO,
				dataSet,
				debugConsole,
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
			with(ETLProcessor.getErrorMessage(e)) {
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

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can create an asset for Dependency using domain command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model
				xraysrv01,Dell,PE2950
			""".stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Device
				iterate {
					extract 'name' load 'Name' set nameVar
					extract 'mfg' load 'Manufacturer' set mfgVar
					extract 'model' load 'Model' set modelVar
					
					find Device by 'Name' eq nameVar and 'Manufacturer' eq mfgVar and 'Model' eq modelVar into 'id'
					elseFind Device by 'Name' eq nameVar and 'Manufacturer' eq mfgVar into 'id'
					elseFind Device by 'Name' eq nameVar and 'Model' eq modelVar into 'id'
					elseFind Device by 'Name' eq nameVar into 'id'
					
					set assetResultVar with DOMAIN
					
					domain Dependency with assetResultVar
				}
			""".stripIndent())

		then: 'Results contains the following values'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 2
				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'manufacturer', 'model', 'id'] as Set
					data.size() == 1

					with(data[0], RowResult) {
						fields.size() == 4
						assertFieldResult(fields['assetName'], 'xraysrv01', 'xraysrv01')
						assertFieldResult(fields['manufacturer'], 'Dell', 'Dell')
						assertFieldResult(fields['model'], 'PE2950', 'PE2950')
						assertFieldResult(fields['id'], null, null)

						with(fields['id'].find, FindResult) {
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Device,
								[
									['assetName', FindOperator.eq.name(), 'xraysrv01'],
									['manufacturer', FindOperator.eq.name(), 'Dell'],
									['model', FindOperator.eq.name(), 'PE2950']
								]
							)
						}
					}
				}

				with(domains[1], DomainResult) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['asset'] as Set
					data.size() == 1
					with(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['asset'])

						with(fields['asset'].find, FindResult) {
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Device,
								[
									['assetName', FindOperator.eq.name(), 'xraysrv01'],
									['manufacturer', FindOperator.eq.name(), 'Dell'],
									['model', FindOperator.eq.name(), 'PE2950']
								]
							)

							with(fields['asset'].create) {
								it.'assetName' == 'xraysrv01'
								it.'manufacturer' == 'Dell'
								it.'model' == 'PE2950'
							}
						}

					}

				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@ConfineMetaClassChanges([Application])
	void 'test can create a dependent for Dependency using domain command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				application id,vendor name,technology,url
				152254,Microsoft,(xlsx updated),www.microsoft.com
			'''.stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'Vendor' set vendorVar
					extract 'technology' load 'Technology' set appTechVar
					extract 'url' load 'URL' set urlVar
					
					find Application by 'Vendor' eq vendorVar and 'Technology' eq appTechVar and 'URL' eq urlVar into 'id'
					elseFind Application by 'Vendor' eq vendorVar and 'Technology' eq appTechVar into 'id'
					elseFind Application by 'Vendor' eq vendorVar and 'URL' eq urlVar into 'id'
					elseFind Application by 'Vendor' eq vendorVar into 'id'
					
					set applicationResultVar with DOMAIN
					
					domain Dependency with null and applicationResultVar
				}
			""".stripIndent())

		then: 'Results contains the following values'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 2
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'appTech', 'url', 'id'] as Set
					data.size() == 1
					with(data[0], RowResult) {
						fields.size() == 4
						assertFieldResult(fields['appVendor'], 'Microsoft', 'Microsoft')
						assertFieldResult(fields['appTech'], '(xlsx updated)', '(xlsx updated)')
						assertFieldResult(fields['url'], 'www.microsoft.com', 'www.microsoft.com')
						assertFieldResult(fields['id'], null, null)

						with(fields['id'].find, FindResult) {
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['appVendor', FindOperator.eq.name(), 'Microsoft'],
									['appTech', FindOperator.eq.name(), '(xlsx updated)'],
									['url', FindOperator.eq.name(), 'www.microsoft.com']
								]
							)
						}
					}
				}

				with(domains[1], DomainResult) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['dependent'] as Set
					data.size() == 1
					with(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['dependent'])

						with(fields['dependent'].find, FindResult) {
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['appVendor', FindOperator.eq.name(), 'Microsoft'],
									['appTech', FindOperator.eq.name(), '(xlsx updated)'],
									['url', FindOperator.eq.name(), 'www.microsoft.com']
								]
							)

							with(fields['dependent'].create) {
								it.'appVendor' == 'Microsoft'
								it.'appTech' == '(xlsx updated)'
								it.'url' == 'www.microsoft.com'
							}
						}
					}
				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can create an asset and dependent for Dependency using domain command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				name,mfg,model,application id,vendor name,technology,url
				xraysrv01,Dell,PE2950,152254,Microsoft,(xlsx updated),www.microsoft.com
			'''.stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[]
			}

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				console on
				read labels
				
				iterate {
				
					domain Device
					extract 'name' load 'Name' set nameVar
					extract 'mfg' load 'Manufacturer' set mfgVar
					extract 'model' load 'Model' set modelVar
					
					find Device by 'Name' eq nameVar and 'Manufacturer' eq mfgVar and 'Model' eq modelVar into 'id'
					elseFind Device by 'Name' eq nameVar and 'Manufacturer' eq mfgVar into 'id'
					elseFind Device by 'Name' eq nameVar and 'Model' eq modelVar into 'id'
					elseFind Device by 'Name' eq nameVar into 'id'
					
					set assetVar with DOMAIN
					
					domain Application
					extract 'vendor name' load 'Vendor' set vendorVar
					extract 'technology' load 'Technology' set appTechVar
					extract 'url' load 'URL' set urlVar
					
					find Application by 'Vendor' eq vendorVar and 'Technology' eq appTechVar and 'URL' eq urlVar into 'id'
					elseFind Application by 'Vendor' eq vendorVar and 'Technology' eq appTechVar into 'id'
					elseFind Application by 'Vendor' eq vendorVar and 'URL' eq urlVar into 'id'
					elseFind Application by 'Vendor' eq vendorVar into 'id'
					
					set dependentVar with DOMAIN
					
					
					domain Dependency with assetVar and dependentVar
				}
			""".stripIndent())

		then: 'Results contains the following values'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 3

				with(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'manufacturer', 'model', 'id'] as Set
					data.size() == 1

					with(data[0], RowResult) {
						fields.size() == 4
						assertFieldResult(fields['assetName'], 'xraysrv01', 'xraysrv01')
						assertFieldResult(fields['manufacturer'], 'Dell', 'Dell')
						assertFieldResult(fields['model'], 'PE2950', 'PE2950')
						assertFieldResult(fields['id'], null, null)
						with(fields['id'].find, FindResult) {
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Device,
								[
									['assetName', FindOperator.eq.name(), 'xraysrv01'],
									['manufacturer', FindOperator.eq.name(), 'Dell'],
									['model', FindOperator.eq.name(), 'PE2950']
								]
							)
						}
					}
				}

				with(domains[1], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'appTech', 'url', 'id'] as Set
					data.size() == 1

					with(data[0], RowResult) {
						fields.size() == 4
						assertFieldResult(fields['appVendor'], 'Microsoft', 'Microsoft')
						assertFieldResult(fields['appTech'], '(xlsx updated)', '(xlsx updated)')
						assertFieldResult(fields['url'], 'www.microsoft.com', 'www.microsoft.com')
						assertFieldResult(fields['id'], null, null)

						with(fields['id'].find, FindResult) {
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['appVendor', FindOperator.eq.name(), 'Microsoft'],
									['appTech', FindOperator.eq.name(), '(xlsx updated)'],
									['url', FindOperator.eq.name(), 'www.microsoft.com']
								]
							)
						}
					}
				}

				with(domains[2], DomainResult) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['asset', 'dependent'] as Set
					data.size() == 1
					with(data[0], RowResult) {
						fields.size() == 2

						assertFieldResult(fields['asset'])
						with(fields['asset'].find, FindResult) {
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Device,
								[
									['assetName', FindOperator.eq.name(), 'xraysrv01'],
									['manufacturer', FindOperator.eq.name(), 'Dell'],
									['model', FindOperator.eq.name(), 'PE2950']
								]
							)

							with(fields['asset'].create) {
								it.'assetName' == 'xraysrv01'
								it.'manufacturer' == 'Dell'
								it.'model' == 'PE2950'
							}
						}

						assertFieldResult(fields['dependent'])
						with(fields['dependent'].find, FindResult) {
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['appVendor', FindOperator.eq.name(), 'Microsoft'],
									['appTech', FindOperator.eq.name(), '(xlsx updated)'],
									['url', FindOperator.eq.name(), 'www.microsoft.com']
								]
							)

							with(fields['dependent'].create) {
								it.'appVendor' == 'Microsoft'
								it.'appTech' == '(xlsx updated)'
								it.'url' == 'www.microsoft.com'
							}
						}
					}

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
