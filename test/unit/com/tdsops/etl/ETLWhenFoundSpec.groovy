package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tdsops.tm.enums.domain.AssetClass
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.utils.FileUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.FileSystemService
import spock.lang.Specification

@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Rack, Model])
class ETLWhenFoundSpec extends Specification {

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
14,152118,System z10 Cab 2,Server,152008,VMAX-3,Storage,File""".stripIndent()

		applicationDataSetContent = """
application id,vendor name,technology,location
152254,Microsoft,(xlsx updated),ACME Data Center
152255,Mozilla,NGM,ACME Data Center""".stripIndent()

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

	void 'test can create a domain when not found a instance with find command'() {

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
			GroovySpy(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assetEntities.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						console on
						read labels
						domain Dependency
						iterate {
							
							extract AssetDependencyId load id
    						extract AssetId load asset
							extract AssetName set primaryName
							extract AssetType set primaryType
    
							find Application of asset by id with DOMAIN.asset 
   							elseFind Application of asset by assetName, assetType with SOURCE.AssetName, primaryType
       						elseFind Application of asset by assetName with SOURCE.DependentName
    						elseFind Asset of asset by assetName with SOURCE.DependentName warn 'found with wrong asset class'
    						
    						whenNotFound asset create {
    							assetClass Application
    							assetName primaryName
    							assetType primaryType
    							"SN Last Seen" NOW
    						}
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()
				fields == ['id', 'asset'] as Set
				data.size() == 14
				data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

				data.collect { it.fields.asset.value } == [
					'151954', '151971', '151974', '151975', '151978', '151990', '151999',
					'152098', '152100', '152106', '152117', '152118', '152118', '152118'
				]

				with(data[0].fields.asset) {

					find.query.size() == 4
					with(find.query[0]) {
						domain == ETLDomain.Application.name()
						kv.id == '151954'
					}

					with(find.query[1]) {
						domain == ETLDomain.Application.name()
						kv.assetName == 'ACMEVMPROD01'
						kv.assetType == 'VM'
					}

					with(find.query[2]) {
						domain == ETLDomain.Application.name()
						kv.assetName == 'VMWare Vcenter'
					}

					with(find.query[3]) {
						domain == ETLDomain.Asset.name()
						kv.assetName == 'VMWare Vcenter'
					}

					// whenNotFound create command assertions
					create.assetClass == ETLDomain.Application.name()
					create.assetName == 'ACMEVMPROD01'
					create.assetType == 'VM'
					!!create."SN Last Seen"
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if whenNotFound command defines an update action'() {

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
			GroovySpy(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assetEntities.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						console on
						read labels
						domain Dependency
						iterate {
							
							extract AssetDependencyId load id
    						extract AssetId load asset
							extract AssetName set primaryName
							extract AssetType set primaryType
    
							find Application of asset by id with DOMAIN.asset 
   							elseFind Application of asset by assetName, assetType with SOURCE.AssetName, primaryType
       						elseFind Application of asset by assetName with SOURCE.DependentName
    						//elseFind Asset of asset by assetName with SOURCE.DependentName warn 'found with wrong asset class'
    						
    						whenNotFound asset update {
    							assetClass Application
    							assetName primaryName
    							assetType primaryType
    							"SN Last Seen" NOW
    						}
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'It throws an Exception because project when the whenNotFound was incorrectly configured'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Incorrect whenNotFound command. Use whenNotFound asset create { .... }'

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if whenFound command defines a create action'() {

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
			GroovySpy(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assetEntities.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						console on
						read labels
						domain Dependency
						iterate {
							
							extract AssetDependencyId load id
    						extract AssetId load asset
							extract AssetName set primaryName
							extract AssetType set primaryType
    
							find Application of asset by id with DOMAIN.asset 
   							elseFind Application of asset by assetName, assetType with SOURCE.AssetName, primaryType
       						elseFind Application of asset by assetName with SOURCE.DependentName
    						elseFind Asset of asset by assetName with SOURCE.DependentName warn 'found with wrong asset class'
    						
    						whenFound asset create {
    							"TN Last Seen" NOW
    						}
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'It throws an Exception because project when the whenNotFound was incorrectly configured'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Incorrect whenFound command. Use whenFound asset update { .... }'

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if whenFound or whenNotFound command does not match a supported domain '() {

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
			GroovySpy(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assetEntities.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						console on
						read labels
						domain Dependency
						iterate {
							
							extract AssetDependencyId load id
    						extract AssetId load asset
							extract AssetName set primaryName
							extract AssetType set primaryType
    
							find Application of asset by id with DOMAIN.asset 
   							elseFind Application of asset by assetName, assetType with SOURCE.AssetName, primaryType
       						elseFind Application of asset by assetName with SOURCE.DependentName
    						elseFind Asset of asset by assetName with SOURCE.DependentName warn 'found with wrong asset class'
    						
							whenNotFound asset create {
								assetClass Unknown
								assetName primaryName
								assetType primaryType
								"SN Last Seen" NOW
							}

						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'It throws an Exception because project when the whenNotFound was incorrectly configured'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == "Invalid domain: 'Unknown'. It should be one of these values: ${ETLDomain.values()}"

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	void 'test can update a domain when found a instance with find command'() {

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
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assetEntities.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
						console on
						read labels
						domain Dependency
						iterate {
							
							extract AssetDependencyId load id
    						extract AssetId load asset
							extract AssetName set primaryName
							extract AssetType set primaryType
    
							find Application of asset by id with DOMAIN.asset 
   							elseFind Application of asset by assetName, assetType with SOURCE.AssetName, primaryType
       						elseFind Application of asset by assetName with SOURCE.DependentName
    						elseFind Asset of asset by assetName with SOURCE.DependentName warn 'found with wrong asset class'
    						
    						whenFound asset update {
    							"TN Last Seen" NOW
    						}
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()
				fields == ['id', 'asset'] as Set
				data.size() == 14
				data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

				data.collect { it.fields.asset.value } == [
					'151954', '151971', '151974', '151975', '151978', '151990', '151999',
					'152098', '152100', '152106', '152117', '152118', '152118', '152118'
				]

				with(data[0].fields.asset) {

					find.query.size() == 4
					with(find.query[0]) {
						domain == ETLDomain.Application.name()
						kv.id == '151954'
					}

					with(find.query[1]) {
						domain == ETLDomain.Application.name()
						kv.assetName == 'ACMEVMPROD01'
						kv.assetType == 'VM'
					}

					with(find.query[2]) {
						domain == ETLDomain.Application.name()
						kv.assetName == 'VMWare Vcenter'
					}

					with(find.query[3]) {
						domain == ETLDomain.Asset.name()
						kv.assetName == 'VMWare Vcenter'
					}

					// whenFound update command assertions
					!!update."TN Last Seen"
				}
			}

		cleanup:
			if(fileName){
				service.deleteTemporaryFile(fileName)
			}
	}

	/**
	 * Builds a list of Mock Room using this fields order
	 * ['id', 'project', 'roomName', 'location', 'roomDepth', 'roomWidth', 'address', 'city', 'stateProv', 'postalCode']
	 * @param valuesList
	 * @return a list of Mock(Room)
	 */
	List<Room> buildRooms(List<List<?>> valuesList) {
		return valuesList.collect { List<?> values ->
			Room room = Mock()
			room.getId() >> values[0]
			room.getProject() >> values[1]
			room.getRoomName() >> values[2]
			room.getLocation() >> values[3]
			room.getRoomDepth() >> values[4]
			room.getRoomWidth() >> values[5]
			room.getAddress() >> values[6]
			room.getCity() >> values[7]
			room.getStateProv() >> values[8]
			room.getPostalCode() >> values[9]
			room
		}
	}

	/**
	 * Builds a list of Mock Room using this fields order
	 * ['id', 'project', 'room', 'manufacturer', 'model', 'location', 'front', 'source', 'roomX', 'roomY', 'powerA', 'powerB', 'powerC', 'rackType'],
	 * @param valuesList
	 * @return a list of Mock(Rack)
	 */
	List<Rack> buildRacks(List<List<?>> valuesList) {
		return valuesList.collect { List<?> values ->
			Rack rack = Mock()
			rack.getId() >> values[0]
			rack.getProject() >> values[1]
			rack.getRoom() >> values[2]
			rack.getManufacturer() >> values[3]
			rack.getModel() >> values[4]
			rack.getLocation() >> values[5]
			rack.getFront() >> values[6]
			rack.getSource() >> values[7]
			rack.getRoomX() >> values[8]
			rack.getRoomY() >> values[9]
			rack.getPowerA() >> values[10]
			rack.getPowerB() >> values[11]
			rack.getPowerC() >> values[12]
			rack.getRackType() >> values[13]
			rack
		}
	}
	/**
	 * Helper method to create Fields Specs based on Asset definition
	 * @param asset
	 * @return
	 */
	private List<Map<String, ?>> buildFieldSpecsFor(def asset) {

		List<Map<String, ?>> fieldSpecs = []
		switch(asset){
			case AssetClass.APPLICATION:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('appVendor', 'Vendor'),
					buildFieldSpec('assetType', 'AssetType'),
					buildFieldSpec('environment', 'Environment'),
					buildFieldSpec('description', 'Description'),
					buildFieldSpec('assetName', 'Name'),
					buildFieldSpec('assetClass', 'Asset Class'),
				]
				break
			case AssetClass.DATABASE:

				break
			case AssetClass.DEVICE:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('location', 'Location'),
					buildFieldSpec('name', 'Name'),
					buildFieldSpec('environment', 'Environment'),
					buildFieldSpec('assetClass', 'Asset Class'),
				]
				break
			case ETLDomain.Dependency:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('assetName', 'AssetName'),
					buildFieldSpec('assetType', 'AssetType'),
					buildFieldSpec('asset', 'Asset'),
					buildFieldSpec('comment', 'Comment'),
					buildFieldSpec('status', 'Status'),
					buildFieldSpec('dataFlowFreq', 'DataFlowFreq'),
					buildFieldSpec('dataFlowDirection', 'DataFlowDirection')
				]
				break
			case CustomDomainService.COMMON:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('assetType', 'AssetType'),
					buildFieldSpec('assetName', 'Name'),
					buildFieldSpec('assetClass', 'Asset Class'),
				]
				break
			case AssetClass.STORAGE:

				break
		}

		return fieldSpecs
	}

	/**
	 * Builds a spec structure used to validate asset fields
	 * @param field
	 * @param label
	 * @param type
	 * @param required
	 * @return a map with the correct fieldSpec format
	 */
	private Map<String, ?> buildFieldSpec(String field, String label, String type = "String", Integer required = 0) {
		return [
			constraints: [
				required: required
			],
			control: type,
			default: '',
			field: field,
			imp: 'U',
			label: label,
			order: 0,
			shared: 0,
			show: 0,
			tip: "",
			udf: 0
		]
	}

	/**
	 * Builds a CSV dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	private List buildCSVDataSet(String csvContent) {

		def (String fileName, OutputStream sixRowsDataSetOS) = service.createTemporaryFile('unit-test-', 'csv')
		sixRowsDataSetOS << csvContent
		sixRowsDataSetOS.close()

		String fullName = service.getTemporaryFullFilename(fileName)

		CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fullName))
		CSVDataset dataSet = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fullName), header: true)

		return [fileName, new DataSetFacade(dataSet)]
	}
}
