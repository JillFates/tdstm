import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
import com.tdsops.etl.FindCondition
import com.tdsops.tm.enums.domain.AssetClass
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.FileSystemService
import spock.lang.Shared

@Integration
@Rollback
class ETLProcessorFindCommandIntegrationSpec extends ETLBaseIntegrationSpec {

	String assetDependencyDataSetContent
	String applicationDataSetContent
	Project GMDEMO
	Project TMDEMO
	DebugConsole debugConsole
	ETLFieldsValidator validator

	FileSystemService fileSystemService

	@Shared
	ProjectTestHelper projectTestHelper

	def setup(){

		projectTestHelper = new ProjectTestHelper()

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

		GMDEMO = projectTestHelper.createProject(null)
		TMDEMO = projectTestHelper.createProject(null)

		validator = createDomainClassFieldsValidator()
		debugConsole = new DebugConsole(buffer: new StringBuilder())
	}

	void 'test can find a domain Property Name with loaded Data Value'(){

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
				mock.getProjectId() >> it.project.id
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				applications.findAll { it.getId() == namedParams.id && it.getProjectId() == namedParams.project.getId() }*.getId()
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

			def results = etlProcessor.finalResult()
			results.domains.size() == 1

			results.domains[0].domain == ETLDomain.Application.name()
			results.domains[0].data[0].fields.environment.originalValue == 'Production'
			results.domains[0].data[0].fields.environment.value == 'Production'


			results.domains[0].data[0].fields.id.originalValue == '152254'
			results.domains[0].data[0].fields.id.value == '152254'

			results.domains[0].data[0].fields.id.find.query.size() == 1
			assertQueryResult(results.domains[0].data[0].fields.id.find.query[0], ETLDomain.Application, [['id', 'eq', 152254]])


			results.domains[0].data[1].fields.environment.originalValue == 'Production'
			results.domains[0].data[1].fields.environment.value == 'Production'


			results.domains[0].data[1].fields.id.originalValue == '152255'
			results.domains[0].data[1].fields.id.value == '152255'

			results.domains[0].data[1].fields.id.find.query.size() == 1
			assertQueryResult(results.domains[0].data[1].fields.id.find.query[0], ETLDomain.Application, [['id', 'eq', 152255]])


			etlProcessor.findCache.size() == 2
			etlProcessor.findCache.hitCountRate() == 0
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', 152254l)]) == [152254l]
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', 152255l)]) == [152255l]


		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can find a domain Property Name with loaded Data Value using elseFind command'(){

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
				mock.getProjectId() >> it.project
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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				assetEntities.findAll { it.getId() == namedParams.id && it.getProjectId() == namedParams.project.id }*.getId()
			}

		and:
			GroovyMock(AssetDependency, global: true)
			AssetDependency.getName() >> { 'com.tds.asset.AssetDependency' }
			AssetDependency.executeQuery(_, _) >> { String query, Map namedParams ->
				assetDependencies.findAll { it.getId() == namedParams.id }
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

					// Process the PRIMARY 'asset' in the dependency
                    extract 'AssetId' load 'asset'

					// Set some local variables to be reused
					extract 'AssetName' set primaryNameVar
					extract 'AssetType' set primaryTypeVar

					find Application by 'id' with DOMAIN.asset into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.AssetName and 'assetClass' eq primaryTypeVar into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.DependentName into 'asset'
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'

			def results = etlProcessor.finalResult()
			results.domains.size() == 1

			results.domains[0].domain == ETLDomain.Dependency.name()
			results.domains[0].fieldNames == ['id', 'asset'] as Set
			results.domains[0].data.size() == 14
			results.domains[0].data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

			results.domains[0].data.collect { it.fields.asset.value } == [
				'151954', '151971', '151974', '151975', '151978', '151990', '151999',
				'152098', '152100', '152106', '152117', '152118', '152118', '152118'
			]

			// Validates command: find Application by 'id' with DOMAIN.asset
			(1..14).eachWithIndex { int value, int index ->

				results.domains[0].data[index].fields.id.find.query.size() == 1
				assertQueryResult(results.domains[0].data[index].fields.id.find.query[0], ETLDomain.Dependency, [['id', 'eq', value]])

			}

			// Validates command: elseFind Application by 'assetName', 'assetType' with SOURCE.AssetName, primaryTypeVar

			results.domains[0].data[0].fields.asset.find.query.size() == 3
			assertQueryResult(results.domains[0].data[0].fields.asset.find.query[0], ETLDomain.Application, [['id', 'eq', 151954]])
			assertQueryResult(results.domains[0].data[0].fields.asset.find.query[1], ETLDomain.Application, [
				['assetName', 'eq', 'ACMEVMPROD01'],
				['assetClass', 'eq', 'VM']
			])
			assertQueryResult(results.domains[0].data[0].fields.asset.find.query[2], ETLDomain.Application, [['assetName', 'eq', 'VMWare Vcenter']])

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can grab the reference to the FINDINGS to be used later'(){

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
				mock.getProjectId() >> it.project.id
				mock.isaApplication() >> (it.assetClass.name().toLowerCase() == 'application')
				mock
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
			def results = etlProcessor.finalResult()
			results.domains.size() == 1

			results.domains[0].domain == ETLDomain.Dependency.name()
			results.domains[0].fieldNames == ['id', 'comment'] as Set
			results.domains[0].data.size() == 14
			results.domains[0].data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

			// Validates command: find Application by 'id' with DOMAIN.asset
			(1..13).eachWithIndex { int value, int index ->

				results.domains[0].data[index].fields.id.find.query.size() == 1
				assertQueryResult(results.domains[0].data[index].fields.id.find.query[0] , ETLDomain.Dependency , [['id', 'eq', value]] )

			}
			// Validates command: set comment with 'Asset results found'
			results.domains[0].data[0..results.domains[0].data.size() - 1].collect { it.fields.comment.value }.unique() == ['Asset results not found']

			etlProcessor.findCache.size() == 14

			etlProcessor.findCache.hitCountRate() == 0
			[1..14].each {
				etlProcessor.findCache.get(ETLDomain.Dependency.name(), [new FindCondition('id', it.toString())]) == []
			}

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can find a domain Property Name with loaded Data Value for a dependent'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<AssetEntity> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
					[assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
			].collect {
				AssetEntity mock = new AssetEntity()
				mock.id = it.id
				mock.assetClass = it.assetClass
				mock.assetName = it.assetName
				mock.project = it.project
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
					find Application by 'id' eq DOMAIN.asset into 'asset'
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			def results = etlProcessor.finalResult()
			results.domains.size() == 1

			results.domains[0].domain == ETLDomain.Dependency.name()


			results.domains[0].data[0].fields.asset.originalValue == '152254'
			results.domains[0].data[0].fields.asset.value == '152254'


			results.domains[0].data[1].fields.asset.originalValue == '152255'
			results.domains[0].data[1].fields.asset.value == '152255'

			etlProcessor.findCache.size() == 2
			etlProcessor.findCache.hitCountRate() == 0
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', '152254')]) == [152254l]
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', '152255')]) == [152255l]

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if script find to a domain Property and it was not defined in the ETL Processor'(){

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
				applications.findAll {
					it.assetName == namedParams.assetName && it.project.id == namedParams.project.id
				}*.getId()
			}

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
					set environmentVar with 'Production'
					extract 'application id' load 'id'
					find Application by 'id' with 'id' into 'asset'
				}
			""".stripIndent())

		then: 'It throws an Exception because project was not defined'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'No project selected in the user context'

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can find multiple asset entities for a domain Property Name with loaded Data Value and use a warn message'(){

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<Application> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152253l, appVendor: 'Mozilla', assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152254l, appVendor: 'Microsoft', assetName: "ACME Data Center", project: GMDEMO]
			].collect {
				Application mock = new Application()
				mock.id = it.id
				mock.assetClass = it.assetClass
				mock.assetName = it.assetName
				mock.assetName = it.assetName
				mock.project = it.project
				mock.appVendor = it.appVendor
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				if(namedParams.containsKey('id')){
					applications.findAll {
						it.getId() == namedParams.id && it.project.id == namedParams.project.id
					}*.getId()
				} else {
					applications.findAll {
						it.getAppVendor() == namedParams.appVendor && it.project.id == namedParams.project.id
					}*.getId()
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
					find Application by 'id' eq SOURCE.'application id' into 'id'
					elseFind Application by 'appVendor' eq DOMAIN.appVendor into 'id' warn 'found without asset id field'
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
				def results = etlProcessor.finalResult()
			results.domains.size() == 1

			results.domains[0].domain == ETLDomain.Application.name()


			results.domains[0].data[0].fields.environment.originalValue == 'Production'
			results.domains[0].data[0].fields.environment.value == 'Production'

			results.domains[0].data[0].fields.appVendor.originalValue == 'Microsoft'
			results.domains[0].data[0].fields.appVendor.value == 'Microsoft'

			results.domains[0].data[0].fields.id.originalValue == '152254'
			results.domains[0].data[0].fields.id.value == '152254'

			// Validating queries
			assertQueryResult(results.domains[0].data[0].fields.id.find.query[0], ETLDomain.Application, [['id', 'eq', 152254]])
			assertQueryResult(results.domains[0].data[0].fields.id.find.query[1], ETLDomain.Application, [['appVendor', 'eq', 'Microsoft']])


			results.domains[0].data[1].fields.environment.originalValue == 'Production'
			results.domains[0].data[1].fields.environment.value == 'Production'

			results.domains[0].data[1].fields.appVendor.originalValue == 'Mozilla'
			results.domains[0].data[1].fields.appVendor.value == 'Mozilla'

			results.domains[0].data[1].fields.id.originalValue == '152255'
			results.domains[0].data[1].fields.id.value == '152255'

			// Validating queries
			assertQueryResult(results.domains[0].data[1].fields.id.find.query[0], ETLDomain.Application, [['id', 'eq', 152255]])
			assertQueryResult(results.domains[0].data[1].fields.id.find.query[1], ETLDomain.Application, [['appVendor', 'eq', 'Mozilla']])

			results.domains[0].data[1].fields.id.warn

			etlProcessor.findCache.size() == 3
			etlProcessor.findCache.hitCountRate() == 0
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', '152254')]) == [152254l]
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', '152255')]) == []
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('appVendor', 'Mozilla')]) == [152253l]

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can trows an Exception if try to use FINDINGS incorrectly based on its results'(){

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
				mock.getProjectId() >> it.project.id
				mock.isaApplication() >> (it.assetClass.name().toLowerCase() == 'application')
				mock
			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.getName() >> 'com.tds.asset.AssetEntity'
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
				if (namedParams.containsKey('id')){
					return assetEntities.findAll { it.getProjectId() == GMDEMO.id && it.id == namedParams.id }*.getId()
				} else if (namedParams.containsKey('assetName')){
					return assetEntities.findAll {
						it.getProjectId() == GMDEMO.id && it.getAssetName() == namedParams.assetName
					}*.getId()
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
					extract 'AssetDependencyId' load 'id'
					extract 'AssetId' load 'asset'

					find Application by 'assetName' eq SOURCE.AssetName into 'asset'
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
			e.message == 'You cannot use isApplication with more than one results in FINDINGS'

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can create a domain when not found a instance with find command'(){

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
				AssetEntity assetEntity = new AssetEntity()
				assetEntity.id = it.id
				assetEntity.assetClass = it.assetClass
				assetEntity.assetName = it.assetName
				assetEntity.environment = it.environment
				assetEntity.project = it.project
				assetEntity
			}

		and:
			GroovySpy(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
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

					find Application by 'id' eq DOMAIN.asset into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.AssetName and 'assetClass' eq primaryTypeVar into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.DependentName into 'asset'

                    whenNotFound 'asset' create {
                        assetName primaryNameVar
                        assetClass primaryTypeVar
                        "Modified Date" NOW
                    }
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			def results = etlProcessor.finalResult()
			results.domains.size() == 1

			results.domains[0].domain == ETLDomain.Dependency.name()
			results.domains[0].fieldNames == ['id', 'asset'] as Set
			results.domains[0].data.size() == 14
			results.domains[0].data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

			results.domains[0].data.collect { it.fields.asset.value } == [
				'151954', '151971', '151974', '151975', '151978', '151990', '151999',
				'152098', '152100', '152106', '152117', '152118', '152118', '152118'
			]

			results.domains[0].data[0].fields.asset.find.query.size() == 3
			assertQueryResult(results.domains[0].data[0].fields.asset.find.query[0], ETLDomain.Application, [['id', 'eq', 151954]])
			assertQueryResult(results.domains[0].data[0].fields.asset.find.query[1], ETLDomain.Application, [
				['assetName', 'eq', 'ACMEVMPROD01'],
				['assetClass', 'eq', 'VM']
			])
			assertQueryResult(results.domains[0].data[0].fields.asset.find.query[2], ETLDomain.Application, [['assetName', 'eq', 'VMWare Vcenter']])

			// whenNotFound create command assertions
			results.domains[0].data[0].fields.asset.create.assetName == 'ACMEVMPROD01'
			results.domains[0].data[0].fields.asset.create.assetClass == 'VM'
			!!results.domains[0].data[0].fields.asset.create.lastUpdated

			etlProcessor.findCache.size() == 12
			etlProcessor.findCache.hitCountRate() == 14.29
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', '151954')]) == [151954l]
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', '151971')]) == [151971l]
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', '151974')]) == [151974l]
			etlProcessor.findCache.get(ETLDomain.Application.name(), [new FindCondition('id', '151975')]) == [151975l]

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can throw an Exception if whenNotFound command defines an update action'(){

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
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

					find Application by 'id' eq DOMAIN.asset into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.AssetName and 'assetClass' eq primaryTypeVar into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.DependentName into 'asset'

                    whenNotFound 'asset' update {
                        assetClass Application
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

	void 'test can throw an Exception if whenFound command defines a create action'(){

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
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

					find Application by 'id' eq DOMAIN.asset into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.AssetName and 'assetClass' eq primaryTypeVar into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.DependentName into 'asset'

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

	void 'test can throw an Exception if whenFound or whenNotFound command does not match a supported domain '(){

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
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
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

					find Application by 'id' eq DOMAIN.asset into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.AssetName and 'assetClass' eq primaryTypeVar into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.DependentName into 'asset'

					whenNotFound 'asset' create {
						assetClass Unknown
						assetName primaryNameVar
						assetType primaryTypeVar
						"SN Last Seen" NOW
					}

				}
			""".stripIndent())

		then: 'It throws an Exception because project when the whenNotFound was incorrectly configured'
			MissingPropertyException e = thrown MissingPropertyException
			e.message == "No such property: Unknown for class: com.tdsops.etl.WhenNotFoundElement"

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can update a domain when found a instance with find command'(){

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
				AssetEntity assetEntity = new AssetEntity()
				assetEntity.id = it.id
				assetEntity.assetClass = it.assetClass
				assetEntity.assetName = it.assetName
				assetEntity.environment = it.environment
				assetEntity.project = it.project
				assetEntity

			}

		and:
			GroovyMock(AssetEntity, global: true)
			AssetEntity.getName() { 'AssetEntity' }
			AssetEntity.executeQuery(_, _, _) >> { String query, Map namedParams, Map metaParams ->
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

					find Application by 'id' eq DOMAIN.asset into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.AssetName and 'assetClass' eq primaryTypeVar into 'asset'
                    elseFind Application by 'assetName' eq SOURCE.DependentName into 'asset'

                    whenFound 'asset' update {
                        assetName primaryNameVar
                    }
				}
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			def results = etlProcessor.finalResult()

			results.domains.size() == 1
			results.domains[0].domain == ETLDomain.Dependency.name()
			results.domains[0].fieldNames == ['id', 'asset'] as Set
			results.domains[0].data.size() == 14
			results.domains[0].data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

			results.domains[0].data.collect { it.fields.asset.value } == [
				'151954', '151971', '151974', '151975', '151978', '151990', '151999',
				'152098', '152100', '152106', '152117', '152118', '152118', '152118'
			]

			results.domains[0].data[0].fields.asset.find.query.size() == 3
			assertQueryResult(results.domains[0].data[0].fields.asset.find.query[0], ETLDomain.Application, [['id', 'eq', 151954]])
			assertQueryResult(results.domains[0].data[0].fields.asset.find.query[1], ETLDomain.Application, [
				['assetName', 'eq', 'ACMEVMPROD01'],
				['assetClass', 'eq', 'VM']
			])
			assertQueryResult(results.domains[0].data[0].fields.asset.find.query[2], ETLDomain.Application, [['assetName', 'eq', 'VMWare Vcenter']])

			etlProcessor.findCache.size() == 12
			etlProcessor.findCache.hitCountRate() == 14.29

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

	void 'test can load Rack domain instances and find Rooms associated'(){

		given:
			List<Room> rooms = buildRooms([
					[GMDEMO, 'DC1', 'ACME Data Center', 26, 40, '112 Main St', 'Cumberland', 'IA', '50843'],
					[GMDEMO, 'ACME Room 1', 'New Colo Provider', 40, 42, '411 Elm St', 'Dallas', 'TX', '75202']
			])

		and:
			List<Rack> racks = buildRacks([
					[GMDEMO, rooms[0], null, null, 'ACME Data Center', 'R', 0, 500, 235, 3300, 3300, 0, 'Rack'],
					[GMDEMO, rooms[1], null, null, 'ACME Data Center', 'L', 1, 160, 0, 1430, 1430, 0, 'Rack'],
					[GMDEMO, null, null, null, 'New Colo Provider', 'L', 0, 41, 42, 0, 0, 0, 'block3x5']
			])

		and:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
				rackId,RoomId,Tag,Location,Model,Room,Source,RoomX,RoomY,PowerA,PowerB,PowerC,Type,Front
				${racks[0].id},100,D7,ACME Data Center,48U Rack,ACME Data Center / DC1,0,500,235,3300,3300,0,Rack,R
				13145,102,C8,ACME Data Center,48U Rack,ACME Data Center / DC1,0,280,252,3300,3300,0,Rack,L
				${racks[1].id},${rooms[0].id},VMAX-1,ACME Data Center,VMAX 20K Rack,ACME Data Center / DC1,1,160,0,1430,1430,0,Rack,R
				${racks[2].id},${rooms[1].id},Storage,ACME Data Center,42U Rack,ACME Data Center / DC1,1,1,15,0,0,0,Object,L
				13358,,UPS 1,New Colo Provider,42U Rack,New Colo Provider / ACME Room 1,1,41,42,0,0,0,block3x5,L
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
					domain Rack
					extract 'rackId' load 'id'
					extract 'Location' load 'location'
					extract 'Room' load 'room'

			        find Room by 'id' eq SOURCE.RoomId into 'id'
				}
			""".stripIndent())

		then: 'Results should contain Rack domain results associated'
			def results = etlProcessor.finalResult()
			results.domains.size() == 1

			results.domains[0].domain == ETLDomain.Rack.name()
			results.domains[0].fieldNames == ['id', 'location', 'room'] as Set

			results.domains[0].data.collect { it.fields.id.value } == [
				racks[0].id.toString(), '13145',
				racks[1].id.toString(),
				racks[2].id.toString(), '13358'
			]

			results.domains[0].data.collect { it.fields.location.value } == [
				'ACME Data Center', 'ACME Data Center', 'ACME Data Center',
				'ACME Data Center', 'New Colo Provider'
			]

			results.domains[0].data.collect { it.fields.room.value } == [
				'ACME Data Center / DC1', 'ACME Data Center / DC1',
				'ACME Data Center / DC1', 'ACME Data Center / DC1',
				'New Colo Provider / ACME Room 1'
			]


			results.domains[0].data[4].fields.room.find.query.size() == 0

		cleanup:
			if(fileName){
				fileSystemService.deleteTemporaryFile(fileName)
			}
	}

}
