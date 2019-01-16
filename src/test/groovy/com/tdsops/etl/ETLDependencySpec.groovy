package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
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
import spock.util.mop.ConfineMetaClassChanges

/**
 * Test about ETL Current Element (CE):
 */
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model, AssetOptions])
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
				iterate {
				
					domain Device
					extract 'name' load 'Name'
					extract 'mfg' load 'Manufacturer'
					extract 'model' load 'Model'
				
					set assetResultVar with DOMAIN
					
					assert assetResultVar.assetName == 'xraysrv01'
					assert assetResultVar.Name == 'xraysrv01'
					assert assetResultVar.manufacturer == 'Dell'
					assert assetResultVar.Manufacturer == 'Dell'
					assert assetResultVar.model == 'PE2950'
					assert assetResultVar.Model == 'PE2950'
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

	void 'test can throw an Exception domain class is not Dependency'() {
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
					set myVar with DOMAIN
					domain Application with myVar
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is being incorrectly configured'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidDomainForDomainDependencyWithCommand().message} at line 8"
				startLine == 8
				endLine == 8
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if asset is null values'() {
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
					domain Dependency with null
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.incorrectDomainVariableForDomainWithCommand().message} at line 6"
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

	void 'test can throw an Exception if asset is not a DOMAIN'() {
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
				iterate {
					domain Device
					extract 'name' load 'Name' set nameVar
					domain Dependency with nameVar
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.incorrectDomainVariableForDomainWithCommand().message} at line 7"
				startLine == 7
				endLine == 7
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if asset has not an AssetEntity domain'() {
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
				
				iterate {
					domain Task
					extract 1 load 'comment'
					set assetVar with DOMAIN
					domain Dependency with assetVar
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidAssetEntityClassForDomainDependencyWithCommand().message} at line 9"
				startLine == 9
				endLine == 9
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if asset is not defined before define an asset dependency type'() {
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
				iterate {
					domain Device
					extract 'name' load 'Name'
					set dependentVar with DOMAIN
					
					domain Dependency 'Runs On' dependentVar
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message.startsWith(ETLProcessorException.unrecognizedDomainCommandArguments('Runs On').message)
				startLine == 9
				endLine == 9
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if Dependency type is incorrect'() {
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
				iterate {
					domain Device
					extract 'name' load 'Name'
					set assetVar with DOMAIN
					
					domain Task
					extract 1 load 'comment'
					set dependentVar with DOMAIN
					
					domain Dependency with assetVar 'Runs On' dependentVar
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidDependencyTypeInDomainDependencyWithCommand('Runs On').message} at line 13"
				startLine == 13
				endLine == 13
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if dependent has not an AssetEntity parameter'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				name,mfg,model
				xraysrv01,Dell,PE2950
			""".stripIndent())

			AssetOptions dependencyType = new AssetOptions(
				value: 'Runs On',
				type: AssetOptions.AssetOptionsType.DEPENDENCY_TYPE
			).save(failOnError: true, flush: true)

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
					domain Device
					extract 'name' load 'Name'
					set assetVar with DOMAIN
					
					domain Task
					extract 1 load 'comment'
					set dependentVar with DOMAIN
					
					domain Dependency with assetVar '${dependencyType.value}' dependentVar
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidAssetEntityClassForDomainDependencyWithCommand().message} at line 13"
				startLine == 13
				endLine == 13
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
	void 'test can create an asset and dependent for Dependency using domain command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				name,mfg,model,application id,vendor name,technology,url
				xraysrv01,Dell,PE2950,152254,Microsoft,(xlsx updated),www.microsoft.com
			'''.stripIndent())

			AssetOptions dependencyType = new AssetOptions(
				value: 'Runs On',
				type: AssetOptions.AssetOptionsType.DEPENDENCY_TYPE
			).save(failOnError: true, flush: true)

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
					
					domain Dependency with assetVar '${dependencyType.value}' dependentVar
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
					fieldNames == ['asset', 'dependent', 'type'] as Set
					data.size() == 1
					with(data[0], RowResult) {
						fields.size() == 3

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
								!it.containsKey('id')
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
								!it.containsKey('id')
								it.'appVendor' == 'Microsoft'
								it.'appTech' == '(xlsx updated)'
								it.'url' == 'www.microsoft.com'
							}
						}

						assertFieldResult(fields['type'], dependencyType.value, dependencyType.value)
					}

				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

	@See('TM-12031')
	@ConfineMetaClassChanges([AssetEntity])
	void 'test can use domain command with application and devices to auto populate Dependency'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				application,vendor,server_name,manufacturer,model,server_guid
				Exchange,Microsoft,exchangedb01,VMWare,VM,123-abc-456-def
			'''.stripIndent())

			AssetOptions dependencyType = new AssetOptions(
				value: 'Runs On',
				type: AssetOptions.AssetOptionsType.DEPENDENCY_TYPE
			).save(failOnError: true, flush: true)

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
				read labels
				iterate {
					// Process Application
					domain Application
					extract 'application' load 'Name' set appNameVar
					extract 'vendor' load 'appVendor'
				
					find Application by 'Name' eq appNameVar and 'Vendor' eq 'Microsoft' into 'id'
					elseFind Application by 'Name' eq appNameVar into 'id'
				
					set assetResultVar with DOMAIN
				
					// Process Server
					domain Device
					extract 'server_name' load 'Name' set srvNameVar
					extract 'manufacturer' load 'Manufacturer' set mfgNameVar
					extract 'model' load 'Model' set modelNameVar
					extract 'server_guid' load 'externalRefId' set extRefIdVar
				
					find Device by 'externalRefId' eq extRefIdVar into 'id' 
					elseFind Device by 'Name' eq srvNameVar and 'Manufacturer' eq mfgNameVar and 'Model' eq modelNameVar into 'id'
					elseFind Device by 'Name' eq srvNameVar and 'Manufacturer' eq mfgNameVar into 'id'
					elseFind Device by 'Name' eq srvNameVar and 'Model' eq modelNameVar into 'id'
					elseFind Device by 'Name' eq srvNameVar into 'id'
				
					set dependentResultVar with DOMAIN
				
					// Process Dependency with improved domain command
					domain Dependency with assetResultVar '${dependencyType.value}' dependentResultVar
				
					load 'comment' with 'This is pretty cool eh?'
				}
			""".stripIndent())

		then: 'Results contains the following values'
			with(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 3

				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['assetName', 'appVendor', 'id'] as Set
					data.size() == 1

					with(data[0], RowResult) {
						fields.size() == 3
						assertFieldResult(fields['assetName'], 'Exchange', 'Exchange')
						assertFieldResult(fields['appVendor'], 'Microsoft', 'Microsoft')
						assertFieldResult(fields['id'], null, null)
						with(fields['id'].find, FindResult) {
							query.size() == 2
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['assetName', FindOperator.eq.name(), 'Exchange'],
									['appVendor', FindOperator.eq.name(), 'Microsoft'],
								]
							)
						}
					}
				}

				with(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'manufacturer', 'model', 'externalRefId','id'] as Set
					data.size() == 1

					with(data[0], RowResult) {
						fields.size() == 5
						assertFieldResult(fields['assetName'], 'exchangedb01', 'exchangedb01')
						assertFieldResult(fields['manufacturer'], 'VMWare', 'VMWare')
						assertFieldResult(fields['model'], 'VM', 'VM')
						assertFieldResult(fields['externalRefId'], '123-abc-456-def', '123-abc-456-def')
						assertFieldResult(fields['id'], null, null)

						with(fields['id'].find, FindResult) {
							query.size() == 5
							assertQueryResult(
								query[0],
								ETLDomain.Device,
								[
									['externalRefId', FindOperator.eq.name(), '123-abc-456-def']
								]
							)
						}
					}
				}

				with(domains[2], DomainResult) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['asset', 'dependent', 'type', 'comment'] as Set
					data.size() == 1
					with(data[0], RowResult) {
						fields.size() == 4

						assertFieldResult(fields['asset'])
						with(fields['asset'].find, FindResult) {
							query.size() == 2
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['assetName', FindOperator.eq.name(), 'Exchange'],
									['appVendor', FindOperator.eq.name(), 'Microsoft'],
								]
							)

							with(fields['asset'].create) {
								!it.containsKey('id')
								it.'assetName' == 'Exchange'
								it.'appVendor' == 'Microsoft'
							}
						}

						assertFieldResult(fields['dependent'])
						with(fields['dependent'].find, FindResult) {
							query.size() == 5
							assertQueryResult(
								query[0],
								ETLDomain.Device,
								[
									['externalRefId', FindOperator.eq.name(), '123-abc-456-def']
								]
							)

							with(fields['dependent'].create) {
								!it.containsKey('id')
								it.'assetName' == 'exchangedb01'
								it.'manufacturer' == 'VMWare'
								it.'model' == 'VM'
								it.'externalRefId' == '123-abc-456-def'
							}
						}
						assertFieldResult(fields['type'], dependencyType.value, dependencyType.value)
						assertFieldResult(fields['comment'], 'This is pretty cool eh?', 'This is pretty cool eh?')
					}

				}
			}

		cleanup:
			if (fileName) {
				service.deleteTemporaryFile(fileName)
			}
	}

}
