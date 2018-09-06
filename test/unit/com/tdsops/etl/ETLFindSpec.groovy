package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.ImportOperationEnum
import com.tdssrc.grails.NumberUtil
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.FileSystemService
import spock.lang.See
import spock.util.mop.ConfineMetaClassChanges

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>find</b></li>
 *     <li><b>elseFind</b></li>
 *     <li><b>warn</b></li>
 * </ul>
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Rack, Room, Database, Model])
class ETLFindSpec extends ETLBaseSpec {

	String assetDependencyDataSetContent
	String applicationDataSetContent
	String deviceBladeChassisDataSetContent
	String deviceDataSetContent
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

		deviceDataSetContent = """
			device id,model name,manufacturer name,rackId,RoomId,Tag,Location,Model,Room,Source,RoomX,RoomY,PowerA,PowerB,PowerC,Type,Front
			152254,SRW24G1,LINKSYS,322223,100,D7,ACME Data Center,48U Rack,ACME Data Center / DC1,0,500,235,3300,3300,0,Rack,R
			152255,ZPHA MODULE,TippingPoint,13145,102,C8,ACME Data Center,48U Rack,ACME Data Center / DC1,0,280,252,3300,3300,0,Rack,L
			152256,Slideaway,ATEN,322224,4344344,VMAX-1,ACME Data Center,VMAX 20K Rack,ACME Data Center / DC1,1,160,0,1430,1430,0,Rack,R""".stripIndent()

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

		debugConsole = new DebugConsole(buffer: new StringBuilder())
	}

	@ConfineMetaClassChanges([AssetEntity])
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
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
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
				iterate {
					domain Application
					load 'environment' with 'Production'
					extract 'application id' load 'id'

					find Application by 'id' with SOURCE.'application id' into 'id'
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()

					with(data[0], RowResult){
						fields.size() == 2
						assertFieldResult(fields['environment'], 'Production', 'Production')
						assertFieldResult(fields['id'], '152254', '152254')

						with(fields['id'].find, FindResult){
							query.size() == 1
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['id', FindOperator.eq.name(), '152254']
								]
							)
						}
					}

					with(data[1], RowResult){
						fields.size() == 2
						assertFieldResult(fields['environment'], 'Production', 'Production')
						assertFieldResult(fields['id'], '152255', '152255')
						with(fields['id'].find, FindResult){
							query.size() == 1
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['id', FindOperator.eq.name(), '152255']
								]
							)
						}
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('id', '152254')]) == [152254l]
				get('Application', [new FindCondition('id', '152255')]) == [152255l]
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test can find a domain Property Name with loaded Data Value using a list of FindCondition'() {

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
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
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
				iterate {
					domain Application
					load 'environment' with 'Production'
					extract 'application id' load 'id'

					find Application by 'id' eq SOURCE.'application id' into 'id'
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()

					with(data[0], RowResult){
						fields.size() == 2
						assertFieldResult(fields['environment'], 'Production', 'Production')
						assertFieldResult(fields['id'], '152254', '152254')

						with(fields['id'].find, FindResult){
							query.size() == 1
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['id', FindOperator.eq.name(), '152254']
								]
							)
						}
					}

					with(data[1], RowResult){
						fields.size() == 2
						assertFieldResult(fields['environment'], 'Production', 'Production')
						assertFieldResult(fields['id'], '152255', '152255')
						with(fields['id'].find, FindResult){
							query.size() == 1
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['id', FindOperator.eq.name(), '152255']
								]
							)
						}
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('id', '152254')]) == [152254l]
				get('Application', [new FindCondition('id', '152255')]) == [152255l]
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
				mock.getMoveBundle() >> it.bundle
				mock.getProject() >> it.project
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				assetEntities.findAll { it.id == namedParams.id }*.getId()
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
						domain Application
						iterate {
							extract 'AssetId' transform with toLong() load 'id'
							find Application by 'id' with DOMAIN.id into 'id'
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id'] as Set
					data.size() == 14
					data.collect {
						it.fields.id.value.toLong()
					} == [151954, 151971, 151974, 151975, 151978, 151990, 151999, 152098, 152100, 152106, 152117, 152118, 152118, 152118]


					with(data[0].fields.id) {
						find.query.size() == 1
						assertQueryResult(
							find.query[0],
							ETLDomain.Application,
							[
								['id', 'eq', 151954l]
							]
						)
						find.results == [151954l]
						find.matchOn == 0
					}

					with(data[1].fields.id) {
						find.query.size() == 1
						find.query[0].domain == ETLDomain.Application.name()
						assertQueryResult(
							find.query[0],
							ETLDomain.Application,
							[
								['id', 'eq', 151971l]
							]
						)
						find.results == [151971l]
						find.matchOn == 0
					}

					with(data[2].fields.id) {
						find.query.size() == 1
						find.query[0].domain == ETLDomain.Application.name()
						assertQueryResult(
							find.query[0],
							ETLDomain.Application,
							[
								['id', 'eq', 151974l]
							]
						)
						find.results == [151974l]
						find.matchOn == 0
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 12
				hitCountRate() == 14.29
				get('Application', [new FindCondition('id', '151954')]) == [151954l]
				get('Application', [new FindCondition('id', '151971')]) == [151971l]
				get('Application', [new FindCondition('id', '151974')]) == [151974l]
				get('Application', [new FindCondition('id', '151975')]) == [151975l]
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void "test exception when [with] keyword is not found"() {

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
						domain Application
						iterate {
							extract 'AssetId' load 'id'
							find Application by 'id' into 'id' // <-- Missing with keyword
						}
						""".stripIndent())

		then: 'It throws an Exception because find command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with (ETLProcessor.getErrorMessage(e)) {
				message == 'Incorrect structure for find command at line 7'
				startLine == 7
				endLine == 7
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void "test exception when [into] keyword is not found"() {

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
						domain Application
						iterate {
							extract 'AssetId' load 'id'
							find Application by 'id' with DOMAIN.id // <-- Missing into keyword
						}
						""".stripIndent())

		then: 'It throws an Exception because find command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with (ETLProcessor.getErrorMessage(e)) {
				message == 'find/elseFind statement is missing required [into] keyword at line 6'
				startLine == 6
				endLine == 6
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void "test exception when [find operation] keywords are not found"(){

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
						domain Application
						iterate {
							extract 'AssetId' load 'id'
							find Application by 'id' // <-- Missing [with and into] keyword
						}
						""".stripIndent())

		then: 'It throws an Exception because find command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException

			with (ETLProcessor.getErrorMessage(e)) {
				message == 'find/elseFind statement is missing required [find operation] keyword at line 6'
				startLine == 6
				endLine == 6
				startColumn == null
				endColumn == null
				fatal == true
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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				assetEntities.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			GroovySpy(AssetDependency, global: true)
			AssetDependency.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				assetDependencies.findAll { it.id == args.id }*.getId()
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
							find Dependency by 'id' with DOMAIN.id into 'id'

    						extract 'AssetId' load 'asset'

							extract 'AssetName' set primaryNameVar
							extract 'AssetType' set primaryTypeVar

							find Application by 'id' with DOMAIN.asset into 'asset'
   							elseFind Application by 'assetName', 'assetClass' with SOURCE.AssetName, primaryTypeVar into 'asset'
       						elseFind Application by 'assetName' with SOURCE.DependentName into 'asset'
    						elseFind Asset by 'assetName' with SOURCE.DependentName into 'asset' warn 'found with wrong asset class'

						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				with(domains[0], DomainResult) {
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
							assertQueryResult(
								query[0],
								ETLDomain.Dependency,
								[
									['id', FindOperator.eq.name(), value.toString()]
								]
							)
						}
					}

					// Validates command: elseFind Application of asset by assetName, assetType with SOURCE.AssetName, primaryType
					with(data[0], RowResult){

						fields.size() == 2

						with(fields['asset'].find, FindResult){
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['id', FindOperator.eq.name(), '151954']
								]
							)
							assertQueryResult(
								query[1],
								ETLDomain.Application,
								[
									['assetName', FindOperator.eq.name(), 'ACMEVMPROD01'],
									['assetClass', FindOperator.eq.name(), 'VM']
								]
							)
							assertQueryResult(
								query[2],
								ETLDomain.Application,
								[
									['assetName', FindOperator.eq.name(), 'VMWare Vcenter']
								]
							)
							assertQueryResult(
								query[3],
								ETLDomain.Asset,
								[
									['assetName', FindOperator.eq.name(), 'VMWare Vcenter']
								]
							)
						}

					}
				}
			}

			with(etlProcessor.findCache){
				size() == 12
				hitCountRate() == 7.14
				get('Application', [new FindCondition('id', '151954')]) == [151954l]
				get('Application', [new FindCondition('id', '151971')]) == [151971l]
				get('Application', [new FindCondition('id', '151974')]) == [151974l]
				get('Application', [new FindCondition('id', '151975')]) == [151975l]
			}

		cleanup:
			if(fileName)  service.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property Name with loaded Data Value using elseFind command and local variables and using find conditions'() {

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				assetEntities.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			GroovySpy(AssetDependency, global: true)
			AssetDependency.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				assetDependencies.findAll { it.id == args.id }*.getId()
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
							find Dependency by 'id' eq DOMAIN.id into 'id'

    						extract 'AssetId' load 'asset'
							extract 'AssetName' set primaryNameVar
							extract 'AssetType' set primaryTypeVar

							find Application by 'id' eq DOMAIN.asset into 'asset'
   							elseFind Application by 'assetName' eq SOURCE.AssetName and 'assetClass' eq primaryTypeVar into 'asset'
       						elseFind Application by 'assetName' eq SOURCE.DependentName into 'asset'
    						elseFind Asset by 'assetName' eq SOURCE.DependentName into 'asset' warn 'found with wrong asset class'

						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				with(domains[0], DomainResult) {
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
							assertQueryResult(
								query[0],
								ETLDomain.Dependency,
								[
									['id', FindOperator.eq.name(), value.toString()]
								]
							)
						}
					}

					// Validates command: elseFind Application of asset by assetName, assetType with SOURCE.AssetName, primaryType
					with(data[0], RowResult){

						fields.size() == 2

						with(fields['asset'].find, FindResult){
							query.size() == 4
							assertQueryResult(
								query[0],
								ETLDomain.Application,
								[
									['id', FindOperator.eq.name(), '151954']
								]
							)
							assertQueryResult(
								query[1],
								ETLDomain.Application,
								[
									['assetName', FindOperator.eq.name(), 'ACMEVMPROD01'],
									['assetClass', FindOperator.eq.name(), 'VM']
								]
							)
							assertQueryResult(
								query[2],
								ETLDomain.Application,
								[
									['assetName', FindOperator.eq.name(), 'VMWare Vcenter']
								]
							)
							assertQueryResult(
								query[3],
								ETLDomain.Asset,
								[
									['assetName', FindOperator.eq.name(), 'VMWare Vcenter']
								]
							)
						}

					}
				}
			}

			with(etlProcessor.findCache){
				size() == 12
				hitCountRate() == 7.14
				get('Application', [new FindCondition('id', '151954')]) == [151954l]
				get('Application', [new FindCondition('id', '151971')]) == [151971l]
				get('Application', [new FindCondition('id', '151974')]) == [151974l]
				get('Application', [new FindCondition('id', '151975')]) == [151975l]
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
			etlProcessor.evaluate("""
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
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['id', 'comment'] as Set
					data.size() == 14
					data.collect {
						it.fields.id.value.toLong()
					} == [151954, 151971, 151974, 151975, 151978, 151990, 151999, 152098, 152100, 152106, 152117, 152118, 152118, 152118]
				}
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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
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
							extract 'application id' load 'asset'
							find Application by 'id' with DOMAIN.asset into 'asset'
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
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
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('id', '152254')]) == [152254l]
				get('Application', [new FindCondition('id', '152255')]) == [152255l]
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an Exception if script find to a domain Property and it was not defined in the ETL Processor'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

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
				iterate {
					domain Application
					load 'environment' with 'Production'
					extract 'application id' load 'id'
					find Application by 'id' with 'id' into 'id'
				}
				""".stripIndent())

		then: 'It throws an Exception because project was not defined'
			ETLProcessorException e = thrown ETLProcessorException
			with (ETLProcessor.getErrorMessage(e)) {
				message == 'No project selected in the user context at line 8'
				startLine == 8
				endLine == 8
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can throw an Exception if find command uses an invalid Find Operator'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

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
				iterate {
					domain Application
					load 'environment' with 'Production'
					extract 'application id' load 'id'
					find Application by 'id' equality 'id' into 'id'
				}
				""".stripIndent())

		then: 'It throws an Exception because project was not defined'
			ETLProcessorException e = thrown ETLProcessorException
			with (ETLProcessor.getErrorMessage(e)) {
				message == 'Unrecognized find criteria operator [equality] specified. Options are [eq, ne, nseq, lt, le, gt, ge, like, notLike, contains, notContains, inList, notInList, between, notBetween, isNull, isNotNull] at line 8'
				startLine == 8
				endLine == 8
				startColumn == null
				endColumn == null
				fatal == true
			}

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				if (namedParams.containsKey('id')) {
					applications.findAll { it.getId() == namedParams.id && it.project.id == namedParams.project.id }*.getId()
				} else {
					applications.findAll { it.getAppVendor() == namedParams.appVendor && it.project.id == namedParams.project.id }*.getId()
				}
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
						iterate {
							domain Application
							load 'environment' with 'Production'
							extract 'vendor name' load 'Vendor'
							extract 'application id' load 'id'

							find Application by 'id' with SOURCE.'application id' into 'id'
							elseFind Application by 'appVendor' with DOMAIN.appVendor into 'id' warn 'found without asset id field'
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()

					with(data[0]) {
						!warn

						with(fields) {

							with(environment) {
								originalValue == 'Production'
								value == 'Production'
							}
							with(appVendor) {
								originalValue == 'Microsoft'
								value == 'Microsoft'
							}
							with(id) {
								originalValue == '152254'
								value == '152254'

								// Validating queries
								with(find) {

									assertQueryResult(
										query[0],
										ETLDomain.Application,
										[
											['id', 'eq', '152254']
										]
									)

									assertQueryResult(
										query[1],
										ETLDomain.Application,
										[
											['appVendor', 'eq', 'Microsoft']
										]
									)
									results == []
									matchOn == null
								}
							}
						}
					}

					with(data[1]) {
						warn
						errors == ['found without asset id field']
						with(fields) {

							with(environment) {
								originalValue == 'Production'
								value == 'Production'
							}
							with(appVendor) {
								originalValue == 'Mozilla'
								value == 'Mozilla'
							}
							with(id) {
								originalValue == '152255'
								value == '152255'
								// Validating queries
								with(find) {

									assertQueryResult(
										query[0],
										ETLDomain.Application,
										[
											['id', 'eq', '152255']
										]
									)

									assertQueryResult(
										query[1],
										ETLDomain.Application,
										[
											['appVendor', 'eq', 'Mozilla']
										]
									)

									results == [1l]
									matchOn == 1
								}
								warn
								errors == ['found without asset id field']
							}
						}
					}
				}
			}

			with (etlProcessor.findCache) {
				size() == 4
				hitCountRate() == 0
				get('Application', [new FindCondition('id', '152254')]) == []
				get('Application', [new FindCondition('id', '152255')]) == []
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can collect error messages in find command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				throw new RuntimeException('Invalid query for this Spec')
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
						iterate {
							domain Application
							extract 'application id' load 'id'
							extract 'vendor name' load 'Vendor'

							find Application by 'id' with SOURCE.'application id' into 'id'
							elseFind Application by 'appVendor' with DOMAIN.appVendor into 'id'
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id', 'appVendor'] as Set
					with(data[0]) {

						errorCount == 2
						with(fields) {

							with(id) {
								originalValue == '152254'
								value == '152254'
								with(find) {

									assertQueryResult(query[0], ETLDomain.Application,
										[
											['id', 'eq', '152254']
										]
									)
									assertQueryResult(query[1], ETLDomain.Application,
										[
											['appVendor', 'eq', 'Microsoft']
										]
									)
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

									assertQueryResult(query[0], ETLDomain.Application,
										[
											['id', 'eq', '152255']
										]
									)
									assertQueryResult(query[1], ETLDomain.Application,
										[
											['appVendor', 'eq', 'Mozilla']
										]
									)
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
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				if (namedParams.containsKey('id')){
					return assetEntities.findAll { it.getProject() == GMDEMO && it.id == namedParams.id }*.getId()
				} else if (namedParams.containsKey('assetName')){
					return assetEntities.findAll { it.getProject() == GMDEMO && it.getAssetName() == namedParams.assetName }*.getId()
				}
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				new DebugConsole(buffer: new StringBuilder()),
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					console on
					read labels
					domain Dependency
					iterate {

						extract 'AssetDependencyId' transform with toLong() load 'id'
						extract 'AssetId' load 'asset'

						find Application by 'assetName' with SOURCE.AssetName into 'asset'
						// Grab the reference to the FINDINGS to be used later.
						def primaryFindings = FINDINGS

						if (primaryFindings.size() > 0 && primaryFindings.isApplication()){
						    set commentVar with 'Asset results found'
						} else {
						    set commentVar with 'Asset results not found'
						}
					}
					""".stripIndent())

		then: 'It throws an Exception because project was not defined'
			ETLProcessorException e = thrown ETLProcessorException

			with (ETLProcessor.getErrorMessage(e)) {
				message == 'You cannot use isApplication with more than one results in FINDINGS at line 14'
				startLine == 14
				endLine == 14
				startColumn == null
				endColumn == null
				fatal == true
			}

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
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""device id,model name,manufacturer name,rackId,RoomId,Tag,Location,Model,Room,Source,RoomX,RoomY,PowerA,PowerB,PowerC,Type,Front
				152254,SRW24G1,LINKSYS,${racks[0].getId()},100,D7,ACME Data Center,48U Rack,ACME Data Center / DC1,0,500,235,3300,3300,0,Rack,R
				152255,ZPHA MODULE,TippingPoint,13145,102,C8,ACME Data Center,48U Rack,ACME Data Center / DC1,0,280,252,3300,3300,0,Rack,L
				152256,Slideaway,ATEN,${racks[1].getId()},${rooms[0].getId()},VMAX-1,ACME Data Center,VMAX 20K Rack,ACME Data Center / DC1,1,160,0,1430,1430,0,Rack,R
				152257,ZPHA MODULE,TippingPoint,${racks[2].getId()},${rooms[1].getId()},Storage,ACME Data Center,42U Rack,ACME Data Center / DC1,1,1,15,0,0,0,Object,L
				152258,Slideaway,ATEN,13358,,UPS 1,New Colo Provider,42U Rack,New Colo Provider / ACME Room 1,1,41,42,0,0,0,block3x5,L""".stripIndent())

		and:
			GroovyMock(Room, global: true)
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
			etlProcessor.evaluate("""
					console on
					read labels
					iterate {
						domain Rack
						extract 'rackId' load 'id'
						extract 'Location' load 'location'
						extract 'Room' load 'room'

						find Room by 'id' with SOURCE.RoomId into 'room'
					}
					""".stripIndent())

		then: 'Results should contain Rack domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Rack.name()
					fieldNames == ['id', 'location', 'room'] as Set

					data.size() == 5
					with(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						with(fields.id) {
							originalValue == '4'
							value == '4'
						}
					}
					with(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						with(fields.id) {
							originalValue == '13145'
							value == '13145'
						}
					}
					with(data[2]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 3
						with(fields.id) {
							originalValue == '5'
							value == '5'
						}
					}
					with(data[3]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 4
						with(fields.id) {
							originalValue == '6'
							value == '6'
						}
					}
					with(data[4]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 5
						with(fields.id) {
							originalValue == '13358'
							value == '13358'
						}
					}

				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can load Devices with locationSource, Rooms and Racks'() {

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
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(deviceDataSetContent)

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
			GroovyMock(Room, global: true)
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
			etlProcessor
				.evaluate("""
					console on
					read labels
					iterate {
						domain Device
						extract 'device id' load 'id'
						extract 'model name' load 'assetName'
						extract 'Location' load 'locationSource'
						extract 'Room' load 'roomSource'

						find Room by 'id' with SOURCE.RoomId into 'roomSource'
					}
					""".stripIndent())

		then: 'Results should contain Rack domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Device.name()
					fieldNames == ['id', 'assetName', 'locationSource', 'roomSource'] as Set
				}
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
			etlProcessor.evaluate("""
					console on
					read labels
					iterate {
						domain Rack
						extract 'rackId' load 'id'
						extract 'Location' load 'location'
						extract 'Room' load 'room'

						find Room 'for' 'room' by 'id' with SOURCE.RoomId
					}
					""".stripIndent())


		then: 'It throws an Exception because find command is incorrect'
			ETLProcessorException e = thrown ETLProcessorException
			with (ETLProcessor.getErrorMessage(e)) {
				message == 'Unrecognized command for with args [room] for the find / elseFind command at line 10'
				startLine == 10
				endLine == 10
				startColumn == null
				endColumn == null
				fatal == true
			}

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				assert NumberUtil.isaNumber(namedParams.id)
				applications.findAll { it.id.toInteger() == namedParams.id && it.project.id == namedParams.project.id }*.getId()
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
						iterate {
							domain Application
							load 'environment' with 'Production'
							extract 'application id' transform with toInteger() load 'id'

							find Application by 'id' with DOMAIN.id into 'id'
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					with(data[0].fields.environment) {
						originalValue == 'Production'
						value == 'Production'
					}

					with(data[0].fields.id) {
						originalValue == '152254'
						value == 152254

						find.query.size() == 1
						assertQueryResult(find.query[0], ETLDomain.Application,
							[['id', 'eq', 152254l]]
						)
					}

					with(data[1].fields.environment) {
						originalValue == 'Production'
						value == 'Production'
					}

					with(data[1].fields.id) {
						originalValue == '152255'
						value == 152255

						find.query.size() == 1
						assertQueryResult(find.query[0], ETLDomain.Application, [['id', 'eq', 152255l]])
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('id', '152254')]) == [152254l]
				get('Application', [new FindCondition('id', '152255')]) == [152255l]
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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				assert NumberUtil.isLong(namedParams.id)
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
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
						iterate {
							domain Application
							load 'environment' with 'Production'
							extract 'application id' transform with toLong() load 'id'

							find Application by 'id' with DOMAIN.id into 'id'
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					with(data[0].fields.environment) {
						originalValue == 'Production'
						value == 'Production'
					}

					with(data[0].fields.id) {
						originalValue == '152254'
						value == 152254l

						find.query.size() == 1
						assertQueryResult(find.query[0], ETLDomain.Application, [['id', 'eq', 152254l]])
					}

					with(data[1].fields.environment) {
						originalValue == 'Production'
						value == 'Production'
					}

					with(data[1].fields.id) {
						originalValue == '152255'
						value == 152255l

						find.query.size() == 1
						assertQueryResult(find.query[0], ETLDomain.Application, [['id', 'eq', 152255l]])
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('id', '152254')]) == [152254l]
				get('Application', [new FindCondition('id', '152255')]) == [152255l]
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property and create the target property automatically with 0 results'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
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
					read labels
					domain Dependency
					iterate {
						extract 'application id' load 'id'
						find Application by 'id' with SOURCE.'application id' into 'id'
					}
					""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Dependency.name()
					data.size() == 2
					with(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						with(fields.id) {
							originalValue == '152254'
							value == '152254'
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', '152254']])
							}
						}
					}

					with(data[1]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						with(fields.id) {
							originalValue == '152255'
							value == '152255'
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', '152255']])
							}
						}
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('id', '152254')]) == []
				get('Application', [new FindCondition('id', '152255')]) == []
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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Dependency
					iterate {
						extract 'application id' transform with toLong() load 'id' set appIdVar
						find Application by 'id' with appIdVar into 'id'
					}
					""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Dependency.name()

					with(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						with(fields.id) {
							originalValue == '152254'
							value == 152254l
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152254l]])
							}
						}
					}

					with(data[1]) {
						op == ImportOperationEnum.UPDATE.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						with(fields.id) {
							originalValue == '152255'
							value == 152255l
							init == null
							errors == []
							warn == false
							with(find) {
								results == [152255l]
								matchOn == 0
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152255l]])
							}
						}
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('id', '152254')]) == []
				get('Application', [new FindCondition('id', '152255')]) == [152255l]
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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Dependency
					iterate {
						extract 'application id' transform with toLong() load 'id' set appIdVar
						find Application by 'id' with appIdVar into 'id'
					}
					""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Dependency.name()

					with(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						with(fields.id) {
							originalValue == '152254'
							value == 152254l
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152254l]])
							}
						}
					}

					with(data[1]) {
						op == ImportOperationEnum.TBD.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						with(fields.id) {
							originalValue == '152255'
							value == 152255l
							init == null
							errors == ['The find/elseFind command(s) found multiple records']
							warn == false
							with(find) {
								matchOn == 0
								results == [152255, 152255]
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152255l]])
							}
						}
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('id', '152254')]) == []
				get('Application', [new FindCondition('id', '152255')]) == [152255l, 152255l]
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	@See('TM-10678')
	void 'test can create new results using domain command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				Primary App,Primary Server,Supporting App,Supporting Server
				ERP,xraysrv001,Oracle7-Cluster,zuludb01""".stripIndent())

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
			read labels
			iterate {
				extract 'Primary App' set primaryAppVar
				extract 'Primary Server' set primaryServerVar
				extract 'Supporting App' set supportAppVar
				extract 'Supporting Server' set supportServerVar

				// Primary App
				domain Application
				lookup 'assetName' with primaryAppVar
				if (LOOKUP.notFound()) {
					find Application by 'assetName' with primaryAppVar into 'id'
					whenNotFound 'id' create {
						assetClass Application
						'assetName' primaryAppVar
					}
				}

				// Supporting App
				domain Application
				lookup 'assetName' with supportAppVar
				if (LOOKUP.notFound()) {
					find Application by 'assetName' with supportAppVar into 'id'
					whenNotFound 'id' create {
						assetClass Application
						'assetName' supportAppVar
					}
				}

				// Primary Server
				domain Device
				lookup 'assetName' with primaryServerVar
				if (LOOKUP.notFound()) {
					find Device by 'assetName' with primaryServerVar into 'id'
					whenNotFound 'id' create {
						assetClass Device
						'assetName' primaryServerVar
					}
				}

				// Supporting Server
				domain Device
				lookup 'assetName' with supportServerVar
				if (LOOKUP.notFound()) {
					find Device by 'assetName' with supportServerVar into 'id'
					whenNotFound 'id' create {
						assetClass Device
						'assetName' supportServerVar
					}
				}

				// Create App - App Dependency
				domain Dependency
				// Note that this doesn't work correctly yet.... I'll update this shortly
				// find Dependency by 'asset', 'dependency' with primaryAppVar, supportAppVar
				load 'asset' with primaryAppVar
				load 'dependent' with supportAppVar
				load 'type' with 'Communicates With'
				load 'status' with 'Validated'
				load 'dataFlowFreq' with 'constant'

				// Create Primary App - Primary Server
				domain Dependency
				// Note that this doesn't work correctly yet.... I'll update this shortly
				// find Dependency by 'asset', 'dependency' with primaryAppVar, primaryServerVar
				load 'asset' with primaryAppVar
				load 'dependent' with primaryServerVar
				load 'type' with 'Runs On'
				load 'status' with 'Validated'
				load 'dataFlowFreq' with 'constant'

				// Create Supporting App - Supporting Server
				domain Dependency
				// Note that this doesn't work correctly yet.... I'll update this shortly
				// find Dependency by 'asset', 'dependency' with supportAppVar, supportServerVar
				load 'asset' with supportAppVar
				load 'dependent' with supportServerVar
				load 'type' with 'Runs On'
				load 'status' with 'Validated'
				load 'dataFlowFreq' with 'constant'
			}
		""".stripIndent())

		then: 'Results should contain Application domain results associated'

			with(etlProcessor.finalResult()){
				domains.size() == 3

				with(domains[0]) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id'] as Set
					data.size() == 2
					with(data[0]){
						op == ImportOperationEnum.INSERT.toString()
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
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Application, [['assetName', 'eq', 'ERP']])
							}
							with(create){
								assetName == 'ERP'

							}
						}

					}
					with(data[1]){
						op == ImportOperationEnum.INSERT.toString()
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
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Application, [['assetName', 'eq', 'Oracle7-Cluster']])
							}
							with(create){
								assetName == 'Oracle7-Cluster'

							}
						}

					}
				}
				with(domains[1]) {
					domain == ETLDomain.Device.name()
					fieldNames == ['id'] as Set
					data.size() == 2
					with(data[0]){
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						with(fields.id) {
							originalValue == null
							value == null
							init == null
							//errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Device, [['assetName', 'eq', 'xraysrv001']])
							}
							with(create){
								assetName == 'xraysrv001'

							}
						}

					}
					with(data[1]){
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						with(fields.id) {
							originalValue == null
							value == null
							init == null
							//errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Device, [['assetName', 'eq', 'zuludb01']])
							}
							with(create){
								assetName == 'zuludb01'

							}
						}

					}
				}
				with(domains[2]) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['asset', 'dependent', 'type', 'status', 'dataFlowFreq'] as Set
					data.size() == 3
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}

	@See('TM-10695')
	void 'test can ignore row explicitly using find command without results'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
				'''.stripIndent())

		and:
			List<Application> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152253l, appVendor: 'Microsoft', assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, appVendor: 'Linux', assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152255l, appVendor: 'Linux', assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				Application mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock.getAppVendor() >> it.appVendor
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.appVendor == namedParams.appVendor && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
			read labels
			domain Application
			iterate {
				extract 'vendor name' set appVendorVar

				find Application by 'appVendor' with appVendorVar into 'id'
				if (FINDINGS.size() == 0) {
					load 'appVendor' with appVendorVar
					load 'appTech' with SOURCE.'technology'
				} else {
					ignore record
				}
			}
		""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()){
				domains.size() == 1

				with(domains[0]) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id', 'appVendor', 'appTech'] as Set
					data.size() == 1
					with(data[0]){
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						fields.keySet().size() == 3

						with(fields.id) {
							originalValue == null
							value == null
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Application, [['appVendor', 'eq', 'Mozilla']])
							}
						}
						with(fields.appVendor) {
							originalValue == 'Mozilla'
							value == 'Mozilla'
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 0
							}
						}

						with(fields.appTech) {
							originalValue == 'NGM'
							value == 'NGM'
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 0
							}
						}
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('appVendor','Microsoft')]) == [152253l]
				get('Application', [new FindCondition('appVendor', 'Mozilla')]) == []
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}

	@See('TM-10695')
	void 'test can ignore row implicitly using find command before a load command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
				'''.stripIndent())

		and:
			List<Application> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152253l, appVendor: 'Microsoft', assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, appVendor: 'Linux', assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152255l, appVendor: 'Linux', assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				Application mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock.getAppVendor() >> it.appVendor
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.appVendor == namedParams.appVendor && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
			read labels
			domain Application
			iterate {
				extract 'vendor name' set appVendorVar

				find Application by 'appVendor' with appVendorVar into 'id'
				if (FINDINGS.size() == 0) {
					load 'appVendor' with appVendorVar
					load 'appTech' with SOURCE.'technology'
				}
			}
		""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()){
				domains.size() == 1

				with(domains[0]) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id', 'appVendor', 'appTech'] as Set
					data.size() == 1
					with(data[0]){
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						fields.keySet().size() == 3

						with(fields.id) {
							originalValue == null
							value == null
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Application, [['appVendor', 'eq', 'Mozilla']])
							}
						}
						with(fields.appVendor) {
							originalValue == 'Mozilla'
							value == 'Mozilla'
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 0
							}
						}

						with(fields.appTech) {
							originalValue == 'NGM'
							value == 'NGM'
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 0
							}
						}
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('appVendor', 'Microsoft')]) == [152253l]
				get('Application', [new FindCondition('appVendor', 'Mozilla')]) == []
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}

	@See('TM-10695')
	void 'test can ignore row implicitly using find command before a initialize command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
				'''.stripIndent())

		and:
			List<Application> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152253l, appVendor: 'Microsoft', assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, appVendor: 'Linux', assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152255l, appVendor: 'Linux', assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				Application mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock.getAppVendor() >> it.appVendor
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.appVendor == namedParams.appVendor && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
			read labels
			domain Application
			iterate {
				extract 'vendor name' set appVendorVar

				find Application by 'appVendor' with appVendorVar into 'id'
				if (FINDINGS.size() == 0) {
					init 'appVendor' with appVendorVar
					initialize 'appTech' with SOURCE.'technology'
				}
			}
		""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()){
				domains.size() == 1

				with(domains[0]) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id', 'appVendor', 'appTech'] as Set
					data.size() == 1
					with(data[0]){
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						fields.keySet().size() == 3

						with(fields.id) {
							originalValue == null
							value == null
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Application, [['appVendor', 'eq', 'Mozilla']])
							}
						}
						with(fields.appVendor) {
							originalValue == null
							value == null
							init == 'Mozilla'
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 0
							}
						}

						with(fields.appTech) {
							originalValue == null
							value == null
							init == 'NGM'
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 0
							}
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}

	@See('TM-10695')
	void 'test can ignore row implicitly using find command before a whenNotFound command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
				'''.stripIndent())

		and:
			List<Application> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152253l, appVendor: 'Microsoft', assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, appVendor: 'Linux', assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152255l, appVendor: 'Linux', assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				Application mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock.getAppVendor() >> it.appVendor
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.appVendor == namedParams.appVendor && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
			read labels
			domain Application
			iterate {
				extract 'vendor name' set appVendorVar

				find Application by 'appVendor' with appVendorVar into 'id'
				if (FINDINGS.size() == 0) {

					whenNotFound 'id' create {
						assetClass Application
						appVendor appVendorVar
						appTech SOURCE.'technology'
					}
				}
			}
		""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()){
				domains.size() == 1

				with(domains[0]) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id'] as Set
					data.size() == 1
					with(data[0]){
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						fields.keySet().size() == 1

						with(fields.id) {
							originalValue == null
							value == null
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Application, [['appVendor', 'eq', 'Mozilla']])
							}
							with(create){
								assetClass == ETLDomain.Application.name()
								appVendor == 'Mozilla'
								appTech == 'NGM'

							}
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}

	@See('TM-10695')
	void 'test can ignore row implicitly using find command before a whenFound command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
				'''.stripIndent())

		and:
			List<Application> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152253l, appVendor: 'Microsoft', assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, appVendor: 'Linux', assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152255l, appVendor: 'Linux', assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				Application mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock.getAppVendor() >> it.appVendor
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.appVendor == namedParams.appVendor && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
			read labels
			domain Application
			iterate {
				extract 'vendor name' set appVendorVar

				find Application by 'appVendor' with appVendorVar into 'id'
				if (FINDINGS.size() > 0) {

					whenFound 'id' update {
						assetClass Application
						appVendor appVendorVar
						appTech SOURCE.'technology'
					}
				}
			}
		""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()){
				domains.size() == 1

				with(domains[0]) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id'] as Set
					data.size() == 1
					with(data[0]){
						op == ImportOperationEnum.UPDATE.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						fields.keySet().size() == 1

						with(fields.id) {
							originalValue == null
							value == null
							init == null
							errors == []
							warn == false
							with(find) {
								results == [152253l]
								matchOn == 0
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Application, [['appVendor', 'eq', 'Microsoft']])
							}
							with(update){
								assetClass == ETLDomain.Application.name()
								appVendor == 'Microsoft'
								appTech == '(xlsx updated)'

							}
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}

	@See('TM-10695')
	void 'test can ignore row implicitly using find command before a domain command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
				'''.stripIndent())

		and:
			List<Application> applications = [
				[assetClass: AssetClass.APPLICATION, id: 152253l, appVendor: 'Microsoft', assetName: "ACME Data Center", project: GMDEMO],
				[assetClass: AssetClass.APPLICATION, id: 152255l, appVendor: 'Linux', assetName: "Another Data Center", project: GMDEMO],
				[assetClass: AssetClass.DEVICE, id: 152255l, appVendor: 'Linux', assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				Application mock = Mock()
				mock.getId() >> it.id
				mock.getAssetClass() >> it.assetClass
				mock.getAssetName() >> it.assetName
				mock.getProject() >> it.project
				mock.getAppVendor() >> it.appVendor
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.appVendor == namedParams.appVendor && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
			read labels
			domain Application
			iterate {
				extract 'vendor name' set appVendorVar

				find Application by 'appVendor' with appVendorVar into 'id'
				if (FINDINGS.size() == 0) {
					domain Device
					load 'description' with appVendorVar
				}
			}
		""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()){
				domains.size() == 2

				with(domains[0]) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id'] as Set
					data.size() == 0
				}

				with(domains[1]) {
					domain == ETLDomain.Device.name()
					fieldNames == ['description'] as Set
					data.size() == 1
					with(data[0]){
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						fields.keySet().size() == 1

						with(fields.description) {
							originalValue == 'Mozilla'
							value == 'Mozilla'
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								query.size() == 0
							}
						}
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 2
				hitCountRate() == 0
				get('Application', [new FindCondition('appVendor', 'Microsoft')]) == [152253l]
				get('Application', [new FindCondition('appVendor', 'Mozilla')]) == []
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)

	}

	@See('TM-11192')
	void 'test can register an error if find by id is not using a long value'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
				'''.stripIndent())

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				throw new Exception('java.lang.String cannot be cast to java.lang.Long')
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
			read labels
			domain Application
			iterate {
				extract 'application id' load 'id'
				find Application by 'id' with SOURCE.'vendor name' into 'id'
			}
		""".stripIndent())

		then: 'Results should contain Application domain results associated'

			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0], DomainResult) {
					domain == ETLDomain.Application.name()
					fieldNames == ['id'] as Set
					fieldLabelMap == ['id': 'Id']
					data.size() == 2

					with(data[0], RowResult){
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						errorCount == 1
						warn == false
						duplicate == false
						errors == []
						fields.size() == 1
						with(fields['id'], FieldResult){
							originalValue == '152254'
							value == '152254'
							init == null
							errors == ['java.lang.String cannot be cast to java.lang.Long']
							warn == false
							create == null
							update == null

							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 'Microsoft']])
							}
						}
					}

					with(data[1], RowResult){
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						errorCount == 1
						warn == false
						duplicate == false
						errors == []
						fields.size() == 1
						with(fields['id'], FieldResult){
							originalValue == '152255'
							value == '152255'
							init == null
							errors == ['java.lang.String cannot be cast to java.lang.Long']
							warn == false
							create == null
							update == null

							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 'Mozilla']])
							}
						}
					}
				}
			}

			with(etlProcessor.findCache){
				size() == 0
				hitCountRate() == 0
			}

		cleanup:
			if (fileName) service.deleteTemporaryFile(fileName)
	}

	@See('TM-11262')
	void 'test can reference domain names by a String value dynamically in domain command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				name,type
				xray,App
				zulu,Srv
			'''.stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					debugConsole,
					validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate('''
				// Map the type to the domain classes as applicable
				Map map = [
					'App': Application,
					'Srv': Device
				]

				read labels
				iterate {
					extract 'name' set nameVar
					extract 'type' transform with substitute(map) set domainClassVar
					domain domainClassVar.value
				}
				'''.stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 2
				with(domains[0]) {
					domain == ETLDomain.Application.name()
				}

				with(domains[1]) {
					domain == ETLDomain.Device.name()
				}

			}
		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	@See('TM-11262')
	void 'test can reference domain names by an Element value dynamically in domain command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				name,type
				xray,App
				zulu,Srv
			'''.stripIndent())

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					debugConsole,
					validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate('''
				// Map the type to the domain classes as applicable
				Map map = [
					'App': Application,
					'Srv': Device
				]

				read labels
				iterate {
					extract 'name' set nameVar
					extract 'type' transform with substitute(map) set domainClassVar
					domain domainClassVar
				}
				'''.stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 2
				with(domains[0]) {
					domain == ETLDomain.Application.name()
				}

				with(domains[1]) {
					domain == ETLDomain.Device.name()
				}

			}
		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	@See('TM-11262')
	void 'test can reference domain names by a String value dynamically in find command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				name,type
				xray,App
				zulu,Srv
			'''.stripIndent())

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.getName() >> 'com.tds.asset.AssetEntity'
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				return []
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					debugConsole,
					validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate('''
				// Map the type to the domain classes as applicable
				Map map = [
					'App': Application,
					'Srv': Device
				]

				read labels
				iterate {
					extract 'name' set nameVar
					extract 'type' transform with substitute(map) set domainClassVar
					domain domainClassVar.value
					find domainClassVar.value by 'Name' with nameVar into 'id'
					load 'Name' with nameVar
				}
				'''.stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 2
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 1
					with(data[0], RowResult){
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						errorCount == 0
						warn == false
						duplicate == false
						errors == []
						fields.size() == 2
						with(fields['assetName'], FieldResult){
							originalValue == 'xray'
							value == 'xray'
							init == null
							create == null
							update == null
							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 0
							}
						}

						with(fields['id'], FieldResult){
							originalValue == null
							value == null
							init == null
							create == null
							update == null

							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Application, [['assetName', 'eq', 'xray']])
							}
						}
					}
				}

				with(domains[1]) {
					domain == ETLDomain.Device.name()
					data.size() == 1
					with(data[0], RowResult){
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						errorCount == 0
						warn == false
						duplicate == false
						errors == []
						fields.size() == 2
						with(fields['assetName'], FieldResult){
							originalValue == 'zulu'
							value == 'zulu'
							init == null
							create == null
							update == null
							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 0
							}
						}

						with(fields['id'], FieldResult){
							originalValue == null
							value == null
							init == null
							create == null
							update == null

							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Device, [['assetName', 'eq', 'zulu']])
							}
						}
					}
				}

			}
		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	@See('TM-11262')
	void 'test can reference domain names by an Element value dynamically in find command'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet('''
				name,type
				xray,App
				zulu,Srv
			'''.stripIndent())

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.getName() >> 'com.tds.asset.AssetEntity'
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				return []
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					debugConsole,
					validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate('''
				// Map the type to the domain classes as applicable
				Map map = [
					'App': Application,
					'Srv': Device
				]

				read labels
				iterate {
					extract 'name' set nameVar
					extract 'type' transform with substitute(map) set domainClassVar
					domain domainClassVar
					find domainClassVar by 'Name' with nameVar into 'id'
					load 'Name' with nameVar
				}
				'''.stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 2
				with(domains[0]) {
					domain == ETLDomain.Application.name()
					data.size() == 1
					with(data[0], RowResult){
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 1
						errorCount == 0
						warn == false
						duplicate == false
						errors == []
						fields.size() == 2
						with(fields['assetName'], FieldResult){
							originalValue == 'xray'
							value == 'xray'
							init == null
							create == null
							update == null
							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 0
							}
						}

						with(fields['id'], FieldResult){
							originalValue == null
							value == null
							init == null
							create == null
							update == null

							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Application, [['assetName', 'eq', 'xray']])
							}
						}
					}
				}

				with(domains[1]) {
					domain == ETLDomain.Device.name()
					data.size() == 1
					with(data[0], RowResult){
						op == ImportOperationEnum.INSERT.toString()
						rowNum == 2
						errorCount == 0
						warn == false
						duplicate == false
						errors == []
						fields.size() == 2
						with(fields['assetName'], FieldResult){
							originalValue == 'zulu'
							value == 'zulu'
							init == null
							create == null
							update == null
							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 0
							}
						}

						with(fields['id'], FieldResult){
							originalValue == null
							value == null
							init == null
							create == null
							update == null

							with(find, FindResult){
								results == []
								matchOn == null
								query.size() == 1
								assertQueryResult(query[0], ETLDomain.Device, [['assetName', 'eq', 'zulu']])
							}
						}
					}
				}

			}
		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	@See('TM-9493')
	void 'test can find a domain Property Name with loaded Data Value using an internal cache'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
			""".stripIndent())

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					debugConsole,
					validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					domain Dependency
					iterate {
						extract 'application id' transform with toLong() load 'asset' set appIdVar
						find Application by 'id' with appIdVar into 'asset'
					}
					""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Dependency.name()
					data.size() == 3

					with(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						with(fields.asset) {
							originalValue == '152254'
							value == 152254l
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152254l]])
							}
						}
					}

					with(data[1]) {
						op == ImportOperationEnum.UPDATE.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						with(fields.asset) {
							originalValue == '152255'
							value == 152255l
							init == null
							errors == []
							warn == false
							with(find) {
								results == [152255l]
								matchOn == 0
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152255l]])
							}
						}
					}

					with(data[2]) {
						op == ImportOperationEnum.UPDATE.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 3
						with(fields.asset) {
							originalValue == '152255'
							value == 152255l
							init == null
							errors == []
							warn == false
							with(find) {
								results == [152255l]
								matchOn == 0
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152255]])
							}
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	@See('TM-9493')
	void 'test can disable internal cache using an ETL script command'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
			""".stripIndent())

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.id == namedParams.id && it.project.id == namedParams.project.id }*.getId()
			}

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
					GMDEMO,
					dataSet,
					debugConsole,
					validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					read labels
					findCache 0
					domain Dependency
					iterate {
						extract 'application id' transform with toLong() load 'asset' set appIdVar
						find Application by 'id' with appIdVar into 'asset'
					}
					""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Dependency.name()
					data.size() == 3

					with(data[0]) {
						op == ImportOperationEnum.INSERT.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 1
						with(fields.asset) {
							originalValue == '152254'
							value == 152254l
							init == null
							errors == []
							warn == false
							with(find) {
								results == []
								matchOn == null
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152254l]])
							}
						}
					}

					with(data[1]) {
						op == ImportOperationEnum.UPDATE.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 2
						with(fields.asset) {
							originalValue == '152255'
							value == 152255l
							init == null
							errors == []
							warn == false
							with(find) {
								results == [152255l]
								matchOn == 0
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152255l]])
							}
						}
					}

					with(data[2]) {
						op == ImportOperationEnum.UPDATE.toString()
						warn == false
						duplicate == false
						errors == []
						rowNum == 3
						with(fields.asset) {
							originalValue == '152255'
							value == 152255l
							init == null
							errors == []
							warn == false
							with(find) {
								results == [152255l]
								matchOn == 0
								assertQueryResult(query[0], ETLDomain.Application, [['id', 'eq', 152255l]])
							}
						}
					}
				}
			}

			etlProcessor.findCache == null

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}
}


