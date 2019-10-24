package com.tdsops.etl

import grails.testing.gorm.DataTest
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Files
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.imports.DataScript
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project
import net.transitionmanager.task.AssetComment
import net.transitionmanager.task.TaskDependency
import spock.lang.See
import spock.util.mop.ConfineMetaClassChanges
/**
 * Test about ETL Current Element (CE):
 */
class ETLDependencySpec extends ETLBaseSpec implements DataTest {

	DebugConsole debugConsole
	Project GMDEMO
	ETLFieldsValidator validator

	def setupSpec() {
		mockDomains DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model, AssetOptions, TaskDependency, AssetComment
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
				
					set assetResult with DOMAIN
					
					assert assetResult.assetName == 'xraysrv01'
					assert assetResult.Name == 'xraysrv01'
					assert assetResult.manufacturer == 'Dell'
					assert assetResult.Manufacturer == 'Dell'
					assert assetResult.model == 'PE2950'
					assert assetResult.Model == 'PE2950'
				}
			""".stripIndent())

		then: 'Current element should contains values'
			assertWith(etlProcessor.currentElement) {
				originalValue == 'PE2950'
				value == 'PE2950'
				init == null
				assertWith(fieldDefinition, ETLFieldDefinition) {
					name == 'model'
					label == 'Model'
				}
			}

		and: 'Results contains the following values'
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'manufacturer', 'model'] as Set
					data.size() == 1
				}
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
					set variable with DOMAIN
					domain Application with variable
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is being incorrectly configured'
			ETLProcessorException e = thrown ETLProcessorException
			assertWith(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidDomainForDomainDependencyWithCommand().message} at line 8"
				startLine == 8
				endLine == 8
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
			assertWith(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.incorrectDomainVariableForDomainWithCommand().message} at line 6"
				startLine == 6
				endLine == 6
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
					extract 'name' load 'Name' set name
					domain Dependency with name
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			assertWith(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.incorrectDomainVariableForDomainWithCommand().message} at line 7"
				startLine == 7
				endLine == 7
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
					set asset with DOMAIN
					domain Dependency with asset
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			assertWith(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidAssetEntityClassForDomainDependencyWithCommand().message} at line 9"
				startLine == 9
				endLine == 9
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
					set dependent with DOMAIN
					
					domain Dependency 'Runs On' dependent
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			assertWith(ETLProcessor.getErrorMessage(e)) {
				message.startsWith(ETLProcessorException.unrecognizedDomainCommandArguments('Runs On').message)
				startLine == 9
				endLine == 9
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
					set asset with DOMAIN
					
					domain Task
					extract 1 load 'comment'
					set dependent with DOMAIN
					
					domain Dependency with asset 'Runs On' dependent
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			assertWith(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidDependencyTypeInDomainDependencyWithCommand('Runs On').message} at line 13"
				startLine == 13
				endLine == 13
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
			).save(flush: true)

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
					set asset with DOMAIN
					
					domain Task
					extract 1 load 'comment'
					set dependent with DOMAIN
					
					domain Dependency with asset '${dependencyType.value}' dependent
				}
			""".stripIndent())

		then: 'It throws an Exception because Dependency command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			assertWith(ETLProcessor.getErrorMessage(e)) {
				message == "${ETLProcessorException.invalidAssetEntityClassForDomainDependencyWithCommand().message} at line 13"
				startLine == 13
				endLine == 13
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if (fileName) {
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
			).save(flush: true)

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
					extract 'name' load 'Name' set name
					extract 'mfg' load 'Manufacturer' set mfg
					extract 'model' load 'Model' set model
					
					find Device by 'Name' eq name and 'Manufacturer' eq mfg and 'Model' eq model into 'id'
					elseFind Device by 'Name' eq name and 'Manufacturer' eq mfg into 'id'
					elseFind Device by 'Name' eq name and 'Model' eq model into 'id'
					elseFind Device by 'Name' eq name into 'id'
					
					set asset with DOMAIN
					
					domain Application
					extract 'vendor name' load 'Vendor' set vendor
					extract 'technology' load 'Technology' set appTech
					extract 'url' load 'URL' set url
					
					find Application by 'Vendor' eq vendor and 'Technology' eq appTech and 'URL' eq url into 'id'
					elseFind Application by 'Vendor' eq vendor and 'Technology' eq appTech into 'id'
					elseFind Application by 'Vendor' eq vendor and 'URL' eq url into 'id'
					elseFind Application by 'Vendor' eq vendor into 'id'
					
					set dependent with DOMAIN
					
					domain Dependency with asset '${dependencyType.value}' dependent
				}
			""".stripIndent())

		then: 'Results contains the following values'
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 3

				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'manufacturer', 'model', 'id'] as Set
					data.size() == 1

					assertWith(data[0], RowResult) {
						fields.size() == 4
						assertFieldResult(fields['assetName'], 'xraysrv01', 'xraysrv01')
						assertFieldResult(fields['manufacturer'], 'Dell', 'Dell')
						assertFieldResult(fields['model'], 'PE2950', 'PE2950')
						assertFieldResult(fields['id'], null, null)
						assertWith(fields['id'].find, FindResult) {
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

				assertWith(domains[1], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['appVendor', 'appTech', 'url', 'id'] as Set
					data.size() == 1

					assertWith(data[0], RowResult) {
						fields.size() == 4
						assertFieldResult(fields['appVendor'], 'Microsoft', 'Microsoft')
						assertFieldResult(fields['appTech'], '(xlsx updated)', '(xlsx updated)')
						assertFieldResult(fields['url'], 'www.microsoft.com', 'www.microsoft.com')
						assertFieldResult(fields['id'], null, null)

						assertWith(fields['id'].find, FindResult) {
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

				assertWith(domains[2], DomainResult) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['asset', 'dependent', 'type'] as Set
					data.size() == 1
					assertWith(data[0], RowResult) {
						fields.size() == 3

						assertFieldResult(fields['asset'])
						assertWith(fields['asset'].find, FindResult) {
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

							assertWith(fields['asset'].create) {
								!it.containsKey('id')
								it.'assetName' == 'xraysrv01'
								it.'manufacturer' == 'Dell'
								it.'model' == 'PE2950'
							}
						}

						assertFieldResult(fields['dependent'])
						assertWith(fields['dependent'].find, FindResult) {
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

							assertWith(fields['dependent'].create) {
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
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
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
			).save(flush: true)

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
					extract 'application' load 'Name' set appName
					extract 'vendor' load 'appVendor'
				
					find Application by 'Name' eq appName and 'Vendor' eq 'Microsoft' into 'id'
					elseFind Application by 'Name' eq appName into 'id'
				
					set assetResult with DOMAIN
				
					// Process Server
					domain Device
					extract 'server_name' load 'Name' set srvName
					extract 'manufacturer' load 'Manufacturer' set mfgName
					extract 'model' load 'Model' set modelName
					extract 'server_guid' load 'externalRefId' set extRefId
				
					find Device by 'externalRefId' eq extRefId into 'id' 
					elseFind Device by 'Name' eq srvName and 'Manufacturer' eq mfgName and 'Model' eq modelName into 'id'
					elseFind Device by 'Name' eq srvName and 'Manufacturer' eq mfgName into 'id'
					elseFind Device by 'Name' eq srvName and 'Model' eq modelName into 'id'
					elseFind Device by 'Name' eq srvName into 'id'
				
					set dependentResult with DOMAIN
				
					// Process Dependency with improved domain command
					domain Dependency with assetResult '${dependencyType.value}' dependentResult
				
					load 'comment' with 'This is pretty cool eh?'
				}
			""".stripIndent())

		then: 'Results contains the following values'
			assertWith(etlProcessor.finalResult()) {
				ETLInfo.originalFilename == fileName
				domains.size() == 3

				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['assetName', 'appVendor', 'id'] as Set
					data.size() == 1

					assertWith(data[0], RowResult) {
						fields.size() == 3
						assertFieldResult(fields['assetName'], 'Exchange', 'Exchange')
						assertFieldResult(fields['appVendor'], 'Microsoft', 'Microsoft')
						assertFieldResult(fields['id'], null, null)
						assertWith(fields['id'].find, FindResult) {
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

				assertWith(domains[1], DomainResult) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'manufacturer', 'model', 'externalRefId','id'] as Set
					data.size() == 1

					assertWith(data[0], RowResult) {
						fields.size() == 5
						assertFieldResult(fields['assetName'], 'exchangedb01', 'exchangedb01')
						assertFieldResult(fields['manufacturer'], 'VMWare', 'VMWare')
						assertFieldResult(fields['model'], 'VM', 'VM')
						assertFieldResult(fields['externalRefId'], '123-abc-456-def', '123-abc-456-def')
						assertFieldResult(fields['id'], null, null)

						assertWith(fields['id'].find, FindResult) {
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

				assertWith(domains[2], DomainResult) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['asset', 'dependent', 'type', 'comment'] as Set
					data.size() == 1
					assertWith(data[0], RowResult) {
						fields.size() == 4

						assertFieldResult(fields['asset'])
						assertWith(fields['asset'].find, FindResult) {
							query.size() == 2
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['assetName', FindOperator.eq.name(), 'Exchange'],
									['appVendor', FindOperator.eq.name(), 'Microsoft'],
								]
							)

							assertWith(fields['asset'].create) {
								!it.containsKey('id')
								it.'assetName' == 'Exchange'
								it.'appVendor' == 'Microsoft'
							}
						}

						assertFieldResult(fields['dependent'])
						assertWith(fields['dependent'].find, FindResult) {
							query.size() == 5
							assertQueryResult(
								query[0],
								ETLDomain.Device,
								[
									['externalRefId', FindOperator.eq.name(), '123-abc-456-def']
								]
							)

							assertWith(fields['dependent'].create) {
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
				fileSystemServiceTestBean.deleteTemporaryFile(fileName)
			}
	}

}
