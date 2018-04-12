package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tdsops.tm.enums.domain.AssetClass
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
import com.tdssrc.grails.NumberUtil

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>find</b></li>
 *     <li><b>elseFind</b></li>
 *     <li><b>warn</b></li>
 * </ul>
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Rack, Model])
class ETLFindSpec extends ETLBaseSpec {

	String assetDependencyDataSetContent
	String applicationDataSetContent
	String deviceBladeChassisDataSetContent
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


		deviceBladeChassisDataSetContent = """
			name,mfg,model,type,chassis,slot
			hpchassis01,HP,BladeSystem Z7000,Blade Chassis,,
			xrayblade01,HP,ProLiant BL460c G1,Blade,hpchassis01,1
			xrayblade02,HP,ProLiant BL460c G1,Blade,hpchassis01,2
			xrayblade03,HP,ProLiant BL460c G1,Blade,hpchassis01,3
			xrayblade04,HP,ProLiant BL460c G1,Blade,hpchassis01,4
		""".stripIndent()

		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l

		TMDEMO = Mock(Project)
		TMDEMO.getId() >> 125612l

		validator = createDomainClassFieldsValidator()

		debugConsole = new DebugConsole(buffer: new StringBuffer())
	}

	void 'test can find a domain Property Name with loaded Data Value'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
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
						iterate {
							domain Application
							load 'environment' with 'Production'
							extract 'application id' load 'id'
							
							find Application by 'id' with SOURCE.'application id' into 'id'
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				with(data[0].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[0].fields.id) {
					originalValue == '152254'
					value == '152254'

					find.query.size() == 1
					with(find.query[0]) {
						domain == 'Application'
						kv == [id: '152254']
					}
				}

				with(data[1].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[1].fields.id) {
					originalValue == '152255'
					value == '152255'

					find.query.size() == 1
					with(find.query[0]) {
						domain == 'Application'
						kv == [id: '152255']
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property Name with DOMAIN bound instance'() {

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
				mock.getBundle() >> it.bundle
				mock.getProject() >> it.project
				mock
			}

		and:
			List<AssetDependency> assetDependencies = [
					[id    : 1l, asset: assetEntities.find { it.getId() == 151954l }, dependent: assetEntities.find {
						it.getId() == 152402l
					}, type: 'Hosts'],
					[id    : 2l, asset: assetEntities.find { it.getId() == 151954l }, dependent: assetEntities.find {
						it.getId() == 152402l
					}, type: 'Hosts'],
					[id    : 3l, asset: assetEntities.find { it.getId() == 151954l }, dependent: assetEntities.find {
						it.getId() == 152402l
					}, type: 'Hosts'],
			].collect {
				AssetDependency mock = Mock()
				mock.getId() >> it.id
				mock.getType() >> it.type
				mock.getAsset() >> it.asset
				mock.getDependent() >> it.dependent
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assetEntities.findAll { it.id == args.id }
			}
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
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
						domain Application
						iterate {
							extract 'AssetId' load 'id'
							find Application by 'id' with DOMAIN.id into 'id' 
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				fieldNames == ['id'] as Set
				data.size() == 14
				data.collect { it.fields.id.value.toLong() } == [151954, 151971, 151974, 151975, 151978, 151990, 151999, 152098, 152100, 152106, 152117, 152118, 152118, 152118]


				with(data[0].fields.id) {
					find.query.size() == 1
					find.query[0].domain == ETLDomain.Application.name()
					find.query[0].kv.id == '151954'
					find.results == [151954]
					find.matchOn == 0
				}

				with(data[1].fields.id) {
					find.query.size() == 1
					find.query[0].domain == ETLDomain.Application.name()
					find.query[0].kv.id == '151971'
					find.results == [151971]
					find.matchOn == 0
				}

				with(data[2].fields.id) {
					find.query.size() == 1
					find.query[0].domain == ETLDomain.Application.name()
					find.query[0].kv.id == '151974'
					find.results == [151974]
					find.matchOn == 0
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property Name with loaded Data Value using elseFind command and local variables'() {

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
			List<AssetDependency> assetDependencies = [
					[id    : 1l, asset: assetEntities.find { it.getId() == 151954l }, dependent: assetEntities.find {
						it.getId() == 152402l
					}, type: 'Hosts'],
					[id    : 2l, asset: assetEntities.find { it.getId() == 151954l }, dependent: assetEntities.find {
						it.getId() == 152402l
					}, type: 'Hosts'],
					[id    : 3l, asset: assetEntities.find { it.getId() == 151954l }, dependent: assetEntities.find {
						it.getId() == 152402l
					}, type: 'Hosts'],
			].collect {
				AssetDependency mock = Mock()
				mock.getId() >> it.id
				mock.getType() >> it.type
				mock.getAsset() >> it.asset
				mock.getDependent() >> it.dependent
				mock
			}

		and:
			GroovySpy(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assetEntities.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			GroovySpy(AssetDependency, global: true)
			AssetDependency.executeQuery(_, _) >> { String query, Map args ->
				assetDependencies.findAll { it.id == args.id }
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
						
							extract 'AssetDependencyId' load 'id'
							find Dependency by 'id' with DOMAIN.id into 'id'
							
    						extract 'AssetId' load 'asset'
							
							extract 'AssetName' set primaryNameVar
							extract 'AssetType' set primaryTypeVar
    
							find Application by 'id' with DOMAIN.asset into 'asset'  
   							elseFind Application by 'assetName', 'assetClass' with SOURCE.AssetName, primaryTypeVar into 'asset'
       						elseFind Application by 'assetName' with SOURCE.DependentName into 'asset'
    						elseFind Asset by 'assetName' with SOURCE.DependentName into 'asset' warn 'found with wrong asset class'
    						
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()
				fieldNames == ['id', 'asset'] as Set
				data.size() == 14
				data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

				data.collect { it.fields.asset.value } == [
						'151954', '151971', '151974', '151975', '151978', '151990', '151999',
						'152098', '152100', '152106', '152117', '152118', '152118', '152118'
				]

				// Validates command: find Application of asset by id with DOMAIN.asset
				(1..14).eachWithIndex { int value, int index ->
					with(data[index].fields.id.find) {
						query.size() == 1
						with(query[0]) {
							domain == ETLDomain.Dependency.name()
							kv.id == value.toString()
						}
					}
				}

				// Validates command: elseFind Application of asset by assetName, assetType with SOURCE.AssetName, primaryType
				with(data[0].fields.asset.find) {
					query.size() == 4
					with(query[0]) {
						domain == ETLDomain.Application.name()
						kv.id == '151954'
					}

					with(query[1]) {
						domain == ETLDomain.Application.name()
						kv.assetName == 'ACMEVMPROD01'
						kv.assetClass == 'VM'
					}

					with(query[2]) {
						domain == ETLDomain.Application.name()
						kv.assetName == 'VMWare Vcenter'
					}

					with(query[3]) {
						domain == ETLDomain.Asset.name()
						kv.assetName == 'VMWare Vcenter'
					}
				}
			}

		cleanup:
			if(fileName)  service.deleteTemporaryFile(fileName)
	}

	void 'test can grab the reference to the FINDINGS to be used later'() {

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
				mock.getBundle() >> it.bundle
				mock.getProject() >> it.project
				mock.isaApplication() >> (it.assetClass.name().toLowerCase() == 'application')
				mock
			}

		and:
			List<AssetDependency> assetDependencies = [
					[id    : 1l, asset: assetEntities.find { it.getId() == 151954l }, dependent: assetEntities.find {
						it.getId() == 152402l
					}, type: 'Hosts'],
					[id    : 2l, asset: assetEntities.find { it.getId() == 151954l }, dependent: assetEntities.find {
						it.getId() == 152402l
					}, type: 'Hosts'],
					[id    : 3l, asset: assetEntities.find { it.getId() == 151954l }, dependent: assetEntities.find {
						it.getId() == 152402l
					}, type: 'Hosts'],
			].collect {
				AssetDependency mock = Mock()
				mock.getId() >> it.id
				mock.getType() >> it.type
				mock.getAsset() >> it.asset
				mock.getDependent() >> it.dependent
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assetDependencies.findAll { it.id == args.id }
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
						
							extract 'AssetId' load 'id'
							find Application by 'id' with DOMAIN.id into 'id'
							
							// Grab the reference to the FINDINGS to be used later. 
							def primaryFindings = FINDINGS
	
							if (primaryFindings.size() > 0 ){
							 	load 'comment' with 'Asset results found'		
							} else {
							 	load 'comment' with 'Asset results not found'
							}
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()
				fieldNames == ['id', 'comment'] as Set
				data.size() == 14
				data.collect { it.fields.id.value.toLong() } == [151954, 151971, 151974, 151975, 151978, 151990, 151999, 152098, 152100, 152106, 152117, 152118, 152118, 152118]
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property Name with loaded Data Value for a findId'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
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
							extract 'application id' load 'asset'
							find Application by 'id' with DOMAIN.asset into 'asset'   
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()

				with(data[0].fields.asset) {
					originalValue == '152254'
					value == '152254'
				}

				with(data[1].fields.asset) {
					originalValue == '152255'
					value == '152255'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an Exception if script find to a domain Property and it was not defined in the ETL Processor'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.assetName == args.assetName && it.project.id == args.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GroovyMock(Project),
					dataSet,
					debugConsole,
					validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
					.evaluate("""
						console on
						read labels
						iterate {
							domain Application
							load 'environment' with 'Production'
							extract 'application id' load 'id'
							find Application by 'id' with 'id' into 'id'
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'It throws an Exception because project was not defined'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Project not defined.'

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find multiple asset entities for a domain Property Name with loaded Data Value and use a warn message'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<Application> applications = [
					[assetClass: AssetClass.APPLICATION, id: 1l, appVendor: 'Mozilla', assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 2l, appVendor: 'Apple', assetName: "ACME Data Center", project: GMDEMO]
			].collect {
				Application mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock.getAppVendor() >> it.appVendor
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				if (args.containsKey('id')) {
					applications.findAll { it.getId() == args.id && it.project.id == args.project.id }
				} else {
					applications.findAll { it.getAppVendor() == args.appVendor && it.project.id == args.project.id }
				}
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
						iterate {
							domain Application
							load 'environment' with 'Production'
							extract 'vendor name' load 'Vendor'
							extract 'application id' load 'id'
							
							find Application by 'id' with SOURCE.'application id' into 'id' 
							elseFind Application by 'appVendor' with DOMAIN.appVendor into 'id' warn 'found without asset id field'
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()

				with(data[0]) {
					!warn

					with(fields) {

						with(environment){
							originalValue == 'Production'
							value == 'Production'
						}
						with(appVendor){
							originalValue == 'Microsoft'
							value == 'Microsoft'
						}
						with(id){
							originalValue == '152254'
							value == '152254'

							// Validating queries
							with(find) {
								query[0].domain == ETLDomain.Application.name()
								query[0].kv == [id: '152254']

								query[1].domain == ETLDomain.Application.name()
								query[1].kv == [appVendor: 'Microsoft']

								results == []
								matchOn == null
							}
						}
					}
				}

				with(data[1]) {
					warn
					errors == ['found without asset id field']
					with(fields){

						with(environment){
							originalValue == 'Production'
							value == 'Production'
						}
						with(appVendor){
							originalValue == 'Mozilla'
							value == 'Mozilla'
						}
						with(id){
							originalValue == '152255'
							value == '152255'
							// Validating queries
							with(find) {
								query[0].domain == ETLDomain.Application.name()
								query[0].kv == [id: '152255']

								query[1].domain == ETLDomain.Application.name()
								query[1].kv == [appVendor: 'Mozilla']

								results == [1l]
								matchOn == 1
							}
							warn
							errors == ['found without asset id field']
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can collect error messages in find command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				throw new RuntimeException('Invalid query for this Spec')
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
						iterate {
							domain Application
							extract 'application id' load 'id'
							extract 'vendor name' load 'Vendor'
							
							find Application by 'id' with SOURCE.'application id' into 'id' 
							elseFind Application by 'appVendor' with DOMAIN.appVendor into 'id'
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				fieldNames == ['id', 'appVendor'] as Set
				with(data[0]) {

					errorCount == 2
					with(fields) {

						with(id) {
							originalValue == '152254'
							value == '152254'
							with(find) {
								query[0].domain == ETLDomain.Application.name()
								query[0].kv == [id: '152254']
								query[1].domain == ETLDomain.Application.name()
								query[1].kv == [appVendor: 'Microsoft']
							}
							errors == ['Invalid query for this Spec', 'Invalid query for this Spec']
						}
						with(appVendor) {
							originalValue == 'Microsoft'
							value == 'Microsoft'
						}
					}
				}
				with(data[1]) {
					errorCount == 2
					with(fields) {

						with(id) {
							originalValue == '152255'
							value == '152255'
							with(find) {
								query[0].domain == ETLDomain.Application.name()
								query[0].kv == [id: '152255']
								query[1].domain == ETLDomain.Application.name()
								query[1].kv == [appVendor: 'Mozilla']
							}

							errors == ['Invalid query for this Spec', 'Invalid query for this Spec']
						}
						with(appVendor) {
							originalValue == 'Mozilla'
							value == 'Mozilla'
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can trows an Exception if try to use FINDINGS incorrectly based on its results'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
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
				14,152118,System z10 Cab 2,Server,152008,VMAX-3,Storage,File""".stripIndent())

		and:
			Project GMDEMO = Mock(Project)
			GMDEMO.getId() >> 125612l

			Project TMDEMO = Mock(Project)
			TMDEMO.getId() >> 125612l

			List<AssetEntity> assetEntities = [
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', id: 151954l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD18', id: 151971l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD21', id: 151974l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD22', id: 151975l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ATXVMPROD25', id: 151978l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMPROD01', id: 151990l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'ACMEVMDEV10', id: 151999l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'Mailserver01', id: 152098l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'PL-DL580-01', id: 152100l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'SH-E-380-1', id: 152106l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 1', id: 152117l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, assetName: 'System z10 Cab 2', id: 152118l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", environment: 'Production', bundle: 'M2-Hybrid', project: TMDEMO],
				[assetClass: AssetClass.APPLICATION, assetName: 'VMWare Vcenter', id: 152402l, environment: 'Production', bundle: 'M2-Hybrid', project: GMDEMO],

			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getEnvironment() >> it.environment
				mock.getProject() >> it.project
				mock.isaApplication() >> (it.assetClass.name().toLowerCase() == 'application')
				mock
			}

		and:
			GroovySpy(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				if (args.containsKey('id')){
					return assetEntities.findAll { it.getProject() == GMDEMO && it.id == args.id }
				} else if (args.containsKey('assetName')){
					return assetEntities.findAll { it.getProject() == GMDEMO && it.getAssetName() == args.assetName }
				}
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				new DebugConsole(buffer: new StringBuffer()),
				validator)

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
					console on
					read labels
					domain Dependency
					iterate {
					
						extract 'AssetDependencyId' load 'id'
						extract 'AssetId' load 'asset'
						
						find Asset by 'assetName' with SOURCE.AssetName into 'asset'
						// Grab the reference to the FINDINGS to be used later. 
						def primaryFindings = FINDINGS

						if (primaryFindings.size() > 0 && primaryFindings.isApplication()){
						    set commentVar with 'Asset results found'		
						} else {
						    set commentVar with 'Asset results not found'
						}
					}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'It throws an Exception because project was not defined'

			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'You cannot use isApplication with more than one results in FINDINGS'

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can load Rack domain instances and find Rooms associated'() {

		given:
			List<Room> rooms = buildRooms([
				[1, GMDEMO, 'DC1', 'ACME Data Center', 26, 40, '112 Main St', 'Cumberland', 'IA', '50843'],
				[2, GMDEMO, 'ACME Room 1', 'New Colo Provider', 40, 42, '411 Elm St', 'Dallas', 'TX', '75202']
			])

		and:
			List<Rack> racks = buildRacks([
				[4, GMDEMO, rooms[0], null, null, 'ACME Data Center', 'R', 0, 500, 235, 3300, 3300, 0, 'Rack'],
				[5, GMDEMO, rooms[1], null, null, 'ACME Data Center', 'L', 1, 160, 0, 1430, 1430, 0, 'Rack'],
				[6, GMDEMO, null, null, null, 'New Colo Provider', 'L', 0, 41, 42, 0, 0, 0, 'block3x5']
			])

		and:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""rackId,RoomId,Tag,Location,Model,Room,Source,RoomX,RoomY,PowerA,PowerB,PowerC,Type,Front
				${racks[0].getId()},100,D7,ACME Data Center,48U Rack,ACME Data Center / DC1,0,500,235,3300,3300,0,Rack,R
				13145,102,C8,ACME Data Center,48U Rack,ACME Data Center / DC1,0,280,252,3300,3300,0,Rack,L
				${racks[1].getId()},${rooms[0].getId()},VMAX-1,ACME Data Center,VMAX 20K Rack,ACME Data Center / DC1,1,160,0,1430,1430,0,Rack,R
				${racks[2].getId()},${rooms[1].getId()},Storage,ACME Data Center,42U Rack,ACME Data Center / DC1,1,1,15,0,0,0,Object,L
				13358,,UPS 1,New Colo Provider,42U Rack,New Colo Provider / ACME Room 1,1,41,42,0,0,0,block3x5,L""".stripIndent())

		and:
			GroovyMock(Room, global: true)
			Room.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			Room.executeQuery(_, _) >> { String query, Map args ->
				rooms.findAll { it.id == args.id }
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
					iterate {
						domain Rack
						extract 'rackId' load 'id' 
						extract 'Location' load 'location'
						extract 'Room' load 'room'
				 
						find Room by 'id' with SOURCE.RoomId into 'room'
					}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Rack domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Rack.name()
				fieldNames == ['id', 'location', 'room'] as Set
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an Exception if the find/elseFind command is incorrectly formatted'() {

		given:
			List<Room> rooms = buildRooms([
				[1, GMDEMO, 'DC1', 'ACME Data Center', 26, 40, '112 Main St', 'Cumberland', 'IA', '50843'],
				[2, GMDEMO, 'ACME Room 1', 'New Colo Provider', 40, 42, '411 Elm St', 'Dallas', 'TX', '75202']
			])

		and:
			List<Rack> racks = buildRacks([
				[4, GMDEMO, rooms[0], null, null, 'ACME Data Center', 'R', 0, 500, 235, 3300, 3300, 0, 'Rack'],
				[5, GMDEMO, rooms[1], null, null, 'ACME Data Center', 'L', 1, 160, 0, 1430, 1430, 0, 'Rack'],
				[6, GMDEMO, null, null, null, 'New Colo Provider', 'L', 0, 41, 42, 0, 0, 0, 'block3x5']
			])

		and:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""rackId,RoomId,Tag,Location,Model,Room,Source,RoomX,RoomY,PowerA,PowerB,PowerC,Type,Front
				${racks[0].getId()},100,D7,ACME Data Center,48U Rack,ACME Data Center / DC1,0,500,235,3300,3300,0,Rack,R
				13145,102,C8,ACME Data Center,48U Rack,ACME Data Center / DC1,0,280,252,3300,3300,0,Rack,L
				${racks[1].getId()},${rooms[0].getId()},VMAX-1,ACME Data Center,VMAX 20K Rack,ACME Data Center / DC1,1,160,0,1430,1430,0,Rack,R
				${racks[2].getId()},${rooms[1].getId()},Storage,ACME Data Center,42U Rack,ACME Data Center / DC1,1,1,15,0,0,0,Object,L
				13358,,UPS 1,New Colo Provider,42U Rack,New Colo Provider / ACME Room 1,1,41,42,0,0,0,block3x5,L""".stripIndent())

		and:
			GroovyMock(Room, global: true)
			Room.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			Room.executeQuery(_, _) >> { String query, Map args ->
				rooms.findAll { it.id == args.id }
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
					iterate {
						domain Rack
						extract 'rackId' load 'id' 
						extract 'Location' load 'location'
						extract 'Room' load 'room'
				 
						find Room 'for' 'room' by 'id' with SOURCE.RoomId
					}
					""".stripIndent(),
				ETLProcessor.class.name)


		then: 'It throws an Exception because find command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Unrecognized command for with args [room] for the find / elseFind command'

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find element using integer transformation'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<AssetEntity> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assert NumberUtil.isaNumber(args.id)
				applications.findAll { it.id.toInteger() == args.id && it.project.id == args.project.id }
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
						iterate {
							domain Application
							load 'environment' with 'Production'
							extract 'application id' transform with toInteger() load 'id'
							
							find Application by 'id' with DOMAIN.id into 'id'
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				with(data[0].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[0].fields.id) {
					originalValue == '152254'
					value == 152254

					find.query.size() == 1
					with(find.query[0]) {
						domain == 'Application'
						kv == [id: 152254]
					}
				}

				with(data[1].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[1].fields.id) {
					originalValue == '152255'
					value == 152255

					find.query.size() == 1
					with(find.query[0]) {
						domain == 'Application'
						kv == [id: 152255]
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find element using number transformation'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<AssetEntity> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assert NumberUtil.isLong(args.id)
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
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
						iterate {
							domain Application
							load 'environment' with 'Production'
							extract 'application id' transform with toLong() load 'id'
							
							find Application by 'id' with DOMAIN.id into 'id'
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()
				with(data[0].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[0].fields.id) {
					originalValue == '152254'
					value == 152254l

					find.query.size() == 1
					with(find.query[0]) {
						domain == 'Application'
						kv == [id: 152254l]
					}
				}

				with(data[1].fields.environment) {
					originalValue == 'Production'
					value == 'Production'
				}

				with(data[1].fields.id) {
					originalValue == '152255'
					value == 152255l

					find.query.size() == 1
					with(find.query[0]) {
						domain == 'Application'
						kv == [id: 152255l]
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property and create the target property automatically with 0 results'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				[]
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
					read labels
					domain Dependency
					iterate {
						find Application by 'id' with SOURCE.'application id' into 'id'
					}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()

				with(data[0]){
					op == 'I'
					warn == false
					duplicate == false
					errors == []
					rowNum == 1
					with(fields.id) {
						originalValue == null
						value == null
						init == null
						errors == []
						warn == false
						with (find){
							results == []
							matchOn == null
							with (query[0]){
								domain == 'Application'
								with(kv){
									id == '152254'
								}
							}
						}
					}
				}

				with(data[1]){
					op == 'I'
					warn == false
					duplicate == false
					errors == []
					rowNum == 2
					with(fields.id) {
						originalValue == null
						value == null
						init == null
						errors == []
						warn == false
						with (find){
							results == []
							matchOn == null
							with (query[0]){
								domain == 'Application'
								with(kv){
									id == '152255'
								}
							}
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property and create the target property automatically with 1 result'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<AssetEntity> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152253l, assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152258l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
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
					read labels
					domain Dependency
					iterate {
						find Application by 'id' with SOURCE.'application id' into 'id' 
					}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()

				with(data[0]){
					op == 'I'
					warn == false
					duplicate == false
					errors == []
					rowNum == 1
					with(fields.id) {
						originalValue == null
						value == null
						init == null
						errors == []
						warn == false
						with (find){
							results == []
							matchOn == null
							with (query[0]){
								domain == 'Application'
								with(kv){
									id == '152254'
								}
							}
						}
					}
				}

				with(data[1]){
					op == 'I'
					warn == false
					duplicate == false
					errors == []
					rowNum == 2
					with(fields.id) {
						originalValue == null
						value == null
						init == null
						errors == []
						warn == false
						with (find){
							results == [152255]
							matchOn == 0
							with (query[0]){
								domain == 'Application'
								with(kv){
									id == '152255'
								}
							}
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property and create the target property automatically with more than 1 results'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<AssetEntity> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152253l, assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152255l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
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
					read labels
					domain Dependency
					iterate {
						find Application by 'id' with SOURCE.'application id' into 'id' 
					}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()

				with(data[0]){
					op == 'I'
					warn == false
					duplicate == false
					errors == []
					rowNum == 1
					with(fields.id) {
						originalValue == null
						value == null
						init == null
						errors == []
						warn == false
						with (find){
							results == []
							matchOn == null
							with (query[0]){
								domain == 'Application'
								with(kv){
									id == '152254'
								}
							}
						}
					}
				}

				with(data[1]){
					op == 'I'
					warn == false
					duplicate == false
					errors == []
					rowNum == 2
					with(fields.id) {
						originalValue == null
						value == null
						init == null
						errors == ['The find/elseFind command(s) found multiple records']
						warn == false
						with (find){
							matchOn == 0
							results == [152255, 152255]
							with (query[0]){
								domain == 'Application'
								with(kv){
									id == '152255'
								}
							}
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find a balades and chassis with their relationships'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(deviceBladeChassisDataSetContent)

		and:
			List<AssetEntity> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.isAssignableFrom(_) >> { Class<?> clazz->
				return true
			}
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
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
					read labels
					domain Device
					iterate {
						extract 'name' load 'Name'
						extract 'mfg' load 'manufacturer' set mfgVar
						extract 'model' load 'model'
						extract 'type' load 'assetType' set typeVar
						
						if (typeVar == 'Blade') {
							extract 'chassis' load 'sourceChassis' set chassisNameVar
							// Try to find the Chassis to create the reference
							// Assumming that the chassis is the same manufacturer as the Blade
							find Device by 'Name', 'Manufacturer', 'assetType' with chassisNameVar, mfgVar, 'Blade Chassis' into 'sourceChassis'
							elseFind Device by 'Name', 'assetType' with chassisNameVar, 'Blade Chassis' into 'sourceChassis'
							
							whenNotFound 'sourceChassis' create {
								assetName chassisNameVar
								assetType 'Blade Chassis'
								manufacturer mfgVar
								// Don't know what the model will be at the point we're working with a blade
							}
							
							// Set the slot that the blade is installed into
							extract 'slot' transform with toInteger() load 'sourceBladePosition'
						}
					}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()

				with(data[0].fields.asset) {
					originalValue == '152254'
					value == '152254'
				}

				with(data[1].fields.asset) {
					originalValue == '152255'
					value == '152255'
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

}
