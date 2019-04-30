package com.tdsops.etl

import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.Database
import com.tdsops.tm.enums.domain.AssetClass
import grails.test.mixin.Mock
import net.transitionmanager.imports.DataScript
import net.transitionmanager.model.Model
import net.transitionmanager.project.Project
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import spock.lang.See
import spock.util.mop.ConfineMetaClassChanges

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>whenFound</b></li>
 *     <li><b>whenNotFound</b></li>
 * </ul>
 */

@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Rack, Model, AssetOptions, Room])
class ETLWhenFoundSpec extends ETLBaseSpec {

	String assetDependencyDataSetContent
	String applicationDataSetContent
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

		assetDependencyDataSetContent = """
				AssetDependencyId,AssetId,AssetName,AssetType,DependentId,DependentName,DependentType,Type
				1,151954,ACMEVMPROD01,VM,152402,VMWare Vcenter,Application,Hosts
				2,151971,ACMEVMPROD18,VM,152402,VMWare Vcenter,Application,Hosts
				3,151974,ACMEVMPROD21,VM,152402,VMWare Vcenter,Application,Hosts
				4,151975,ACMEVMPROD22,VM,152402,VMWare Vcenter,Application,Hosts
				5,151978,ATXVMPROD25,VM,152368,V Cluster Prod,Application,Hosts
				6,151990,ACMEVMDEV01,VM,152403,VMWare Vcenter Test,Application,Hosts
				7,151999,ACMEVMDEV10,VM,152063,PE-1650-01,Server,Unknown
				8,152098,Mailserver01,Server,151960,ACMEVMPROD07,VM,Unknown
				9,152100,PL-DL580-01,Server,151960,ACMEVMPROD07,VM,Unknown
				10,152106,SH-E-380-1,Server,152357,Epic,Application,Unknown
				11,152117,System z10 Cab 1,Server,152118,System z10 Cab 2,Server,Runs On
				12,152118,System z10 Cab 2,Server,152006,VMAX-1,Storage,File
				13,152118,System z10 Cab 2,Server,152007,VMAX-2,Storage,File
				14,152118,System z10 Cab 2,Server,152008,VMAX-3,Storage,File
		""".stripIndent().trim()

		applicationDataSetContent = """
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
		""".stripIndent().trim()

		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l

		TMDEMO = Mock(Project)
		TMDEMO.getId() >> 125612l

		validator = createDomainClassFieldsValidator()

		debugConsole = new DebugConsole(buffer: new StringBuilder())
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can create a domain when not found a instance with find command using local variables'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(assetDependencyDataSetContent)

		and:
			List<AssetEntity> assetEntities = [
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', id: 151954l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD18', id: 151971l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD21', id: 151974l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD22', id: 151975l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ATXVMPROD25', id: 151978l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV01', id: 151990l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV10', id: 151999l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'Mailserver01', id: 152098l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'PL-DL580-01', id: 152100l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'SH-E-380-1', id: 152106l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 1', id: 152117l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 2', id: 152118l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", environment: 'Production', moveBundle: 'M2-Hybrid', project: TMDEMO],
				[assetClass: AssetClass.APPLICATION, assetName: 'VMWare Vcenter', id: 152402l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],

			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getEnvironment() >> it.environment
				mock.getMoveBundle() >> it.moveBundle
				mock.getProject() >> it.project
				mock
			}

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				assetEntities.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
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
						domain Dependency
						iterate {

							extract 'AssetDependencyId' load 'id'
    						extract 'AssetId' load 'asset'
							extract 'AssetName' set primaryNameVar
							extract 'AssetType' set primaryTypeVar

							find Device by 'id' with DOMAIN.asset into 'asset'
   							elseFind Device by 'assetName', 'assetClass' with SOURCE.AssetName, primaryTypeVar into 'asset'
       						elseFind Device by 'assetName' with SOURCE.DependentName into 'asset'
    						elseFind Asset by 'assetName' with SOURCE.DependentName into 'asset' warn 'found customWith( wrong asset class'

    						whenNotFound 'asset' create {
    							assetName primaryNameVar
    							assetType primaryTypeVar
    							"Modified Date" NOW
    						}
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['id', 'asset'] as Set
					data.size() == 14
					data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

					data.collect { it.fields.asset.value } == [
						'151954', '151971', '151974', '151975', '151978', '151990', '151999',
						'152098', '152100', '152106', '152117', '152118', '152118', '152118'
					]

					assertWith(data[0].fields.asset) {

						find.query.size() == 4
						assertQueryResult(find.query[0], ETLDomain.Device, [
							['id', FindOperator.eq.name(), 151954l]
						])
						assertQueryResult(find.query[1], ETLDomain.Device, [
							['assetName', FindOperator.eq.name(), 'ACMEVMPROD01'],
							['assetClass', FindOperator.eq.name(), 'VM']
						])
						assertQueryResult(find.query[2], ETLDomain.Device, [
							['assetName', FindOperator.eq.name(), 'VMWare Vcenter']
						])
						assertQueryResult(find.query[3], ETLDomain.Asset, [
							['assetName', FindOperator.eq.name(), 'VMWare Vcenter']
						])

						// whenNotFound create command assertions
						create.assetName == 'ACMEVMPROD01'
						create.assetType == 'VM'
						!!create.lastUpdated
					}
				}
			}

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can throw an Exception if whenNotFound command defines an update action using local variables'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(assetDependencyDataSetContent)

		and:
			List<AssetEntity> assetEntities = [
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', id: 151954l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD18', id: 151971l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD21', id: 151974l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD22', id: 151975l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ATXVMPROD25', id: 151978l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV01', id: 151990l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV10', id: 151999l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'Mailserver01', id: 152098l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'PL-DL580-01', id: 152100l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'SH-E-380-1', id: 152106l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 1', id: 152117l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 2', id: 152118l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", environment: 'Production', moveBundle: 'M2-Hybrid', project: TMDEMO],
				[assetClass: AssetClass.APPLICATION, assetName: 'VMWare Vcenter', id: 152402l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],

			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getEnvironment() >> it.environment
				mock.getMoveBundle() >> it.moveBundle
				mock.getProject() >> it.project
				mock
			}

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				assetEntities.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
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
						domain Dependency
						iterate {

							extract 'AssetDependencyId' load 'id'
    						extract 'AssetId' load 'asset'
							extract 'AssetName' set primaryNameVar
							extract 'AssetType' set primaryTypeVar

							find Application by 'id' with DOMAIN.asset into 'asset'
   							elseFind Application by 'assetName', 'assetClass' with SOURCE.AssetName, primaryTypeVar into 'asset'
       						elseFind Application by 'assetName' with SOURCE.DependentName into 'asset'

    						whenNotFound 'asset' update {
    							assetName primaryNameVar
    							assetType primaryTypeVar
    							"SN Last Seen" NOW
    						}
						}
						""".stripIndent())

		then: 'It throws an Exception because project when the whenNotFound was incorrectly configured'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Incorrect whenNotFound command. Use whenNotFound asset create { .... }'

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can throw an Exception if whenFound command defines a create action using local variables'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(assetDependencyDataSetContent)

		and:
			List<AssetEntity> assetEntities = [
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', id: 151954l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD18', id: 151971l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD21', id: 151974l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD22', id: 151975l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ATXVMPROD25', id: 151978l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV01', id: 151990l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV10', id: 151999l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'Mailserver01', id: 152098l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'PL-DL580-01', id: 152100l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'SH-E-380-1', id: 152106l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 1', id: 152117l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 2', id: 152118l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", environment: 'Production', moveBundle: 'M2-Hybrid', project: TMDEMO],
				[assetClass: AssetClass.APPLICATION, assetName: 'VMWare Vcenter', id: 152402l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],

			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getEnvironment() >> it.environment
				mock.getMoveBundle() >> it.moveBundle
				mock.getProject() >> it.project
				mock
			}

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				assetEntities.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
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
						domain Dependency
						iterate {

							extract 'AssetDependencyId' load 'id'
    						extract 'AssetId' load 'asset'
							extract 'AssetName' set primaryNameVar
							extract 'AssetType' set primaryTypeVar

							find Application by 'id' with DOMAIN.asset into 'asset'
   							elseFind Application by 'assetName', 'assetClass' with SOURCE.AssetName, 'primaryType' into 'asset'
       						elseFind Application by 'assetName' with SOURCE.DependentName into 'asset'
    						elseFind Asset by 'assetName' with SOURCE.DependentName into 'asset' warn 'found customWith( wrong asset class'

    						whenFound 'asset' create {
    							"TN Last Seen" NOW
    						}
						}
						""".stripIndent())

		then: 'It throws an Exception because project when the whenNotFound was incorrectly configured'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Incorrect whenFound command. Use whenFound asset update { .... }'

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can update a domain when found a instance with find command using local variables'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(assetDependencyDataSetContent)

		and:
			List<AssetEntity> assetEntities = [
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', id: 151954l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD18', id: 151971l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD21', id: 151974l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD22', id: 151975l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ATXVMPROD25', id: 151978l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV01', id: 151990l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV10', id: 151999l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'Mailserver01', id: 152098l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'PL-DL580-01', id: 152100l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'SH-E-380-1', id: 152106l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 1', id: 152117l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 2', id: 152118l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", environment: 'Production', moveBundle: 'M2-Hybrid', project: TMDEMO],
				[assetClass: AssetClass.APPLICATION, assetName: 'VMWare Vcenter', id: 152402l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],

			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getEnvironment() >> it.environment
				mock.getMoveBundle() >> it.moveBundle
				mock.getProject() >> it.project
				mock
			}

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				assetEntities.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
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
						domain Dependency
						iterate {

							extract 'AssetDependencyId' load 'id'
    						extract 'AssetId' load 'asset'
							extract 'AssetName' set primaryNameVar
							extract 'AssetType' set primaryTypeVar

							find Device by 'id' with DOMAIN.asset into 'asset'
   							elseFind Device by 'assetName', 'assetClass' with SOURCE.AssetName, primaryTypeVar into 'asset'
       						elseFind Device by 'assetName' with SOURCE.DependentName into 'asset'
    						elseFind Asset by 'assetName' with SOURCE.DependentName into 'asset' warn 'found customWith( wrong asset class'

    						whenFound 'asset' update {
    							"Modified Date" NOW
    						}
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult()) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['id', 'asset'] as Set
					data.size() == 14
					data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

					data.collect { it.fields.asset.value } == [
						'151954', '151971', '151974', '151975', '151978', '151990', '151999',
						'152098', '152100', '152106', '152117', '152118', '152118', '152118'
					]

					assertWith(data[0].fields.asset) {

						find.query.size() == 4
						assertQueryResult(find.query[0], ETLDomain.Device, [
							['id', FindOperator.eq.name(), 151954l]
						])
						assertQueryResult(find.query[1], ETLDomain.Device, [
							['assetName', FindOperator.eq.name(), 'ACMEVMPROD01'],
							['assetClass', FindOperator.eq.name(), 'VM']
						])
						assertQueryResult(find.query[2], ETLDomain.Device, [
							['assetName', FindOperator.eq.name(), 'VMWare Vcenter']
						])
						assertQueryResult(find.query[3], ETLDomain.Asset, [
							['assetName', FindOperator.eq.name(), 'VMWare Vcenter']
						])

						// whenFound update command assertions
						!!update.lastUpdated
					}
				}
			}

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can throw an Exception if whenFound is used without using previously a find command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(assetDependencyDataSetContent)

		and:
			List<AssetEntity> assetEntities = [
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', id: 151954l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD18', id: 151971l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD21', id: 151974l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD22', id: 151975l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ATXVMPROD25', id: 151978l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV01', id: 151990l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV10', id: 151999l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'Mailserver01', id: 152098l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'PL-DL580-01', id: 152100l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'SH-E-380-1', id: 152106l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 1', id: 152117l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 2', id: 152118l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", environment: 'Production', moveBundle: 'M2-Hybrid', project: TMDEMO],
				[assetClass: AssetClass.APPLICATION, assetName: 'VMWare Vcenter', id: 152402l, environment: 'Production', moveBundle: 'M2-Hybrid', project: GMDEMO],

			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getEnvironment() >> it.environment
				mock.getMoveBundle() >> it.moveBundle
				mock.getProject() >> it.project
				mock
			}

		and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				assetEntities.findAll { it.id == args.id && it.project.id == args.project.id }*.getId()
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
						domain Dependency
						iterate {

							extract 'AssetDependencyId' load 'id'
    						extract 'AssetId' load 'asset'
							extract 'AssetName' set primaryNameVar
							extract 'AssetType' set primaryTypeVar

    						whenFound 'asset' create {
    							"Modified Date" NOW
    						}
						}
						""".stripIndent())

		then: 'It throws an Exception because project when the whenNotFound was incorrectly configured'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Incorrect used: whenFound for field 'asset'. It must have defined a find command previously"

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

    @See('TM-11182')
	@ConfineMetaClassChanges([AssetEntity])
	void 'test can create throw an Exception when script tries to use a non existing variable'() {

	    given:
		    def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(assetDependencyDataSetContent)

	    and:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
			    []
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
            domain Dependency
            iterate {
                extract 'AssetId' load 'asset'
                find Device by 'id' with DOMAIN.asset into 'asset'
                whenNotFound 'asset' create {
                    description unknownVar
                }
            }
        """.stripIndent())

	    then: 'It throws an Exception because variable in not defined'
		    ETLProcessorException e = thrown ETLProcessorException
		    e.message == 'No such property: unknownVar'

	    cleanup:
		    if (fileName) {
			    fileSystemService.deleteTemporaryFile(fileName)
		    }
    }

}
