import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.DomainClassFieldsValidator
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.utils.FileUtils
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.FileSystemService
import spock.lang.Specification

class ETLProcessorFindCommandIntegrationTests extends Specification {

	String assetDependencyDataSetContent
	String applicationDataSetContent
	Project GMDEMO
	Project TMDEMO
	DebugConsole debugConsole
	ETLFieldsValidator validator

	FileSystemService fileSystemService
	ProjectTestHelper projectTestHelper = new ProjectTestHelper()

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

		GMDEMO = projectTestHelper.createProject(null)
		TMDEMO = projectTestHelper.createProject(null)

		validator = new DomainClassFieldsValidator()
		validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Database, buildFieldSpecsFor(AssetClass.DATABASE))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Device, buildFieldSpecsFor(AssetClass.DEVICE))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Dependency, buildFieldSpecsFor(ETLDomain.Dependency))
		validator.addAssetClassFieldsSpecFor(ETLDomain.Asset, buildFieldSpecsFor(ETLDomain.Dependency))

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
							set environment with Production
							extract 'application id' load id
							find Application by id with SOURCE.'application id'
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property Name with loaded Data Value using elseFind command'() {

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
			GroovyMock(AssetEntity, global: true)
			AssetEntity.executeQuery(_, _) >> { String query, Map args ->
				assetEntities.findAll { it.id == args.id && it.project.id == args.project.id }
			}

		and:
			GroovyMock(AssetDependency, global: true)
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
						
							extract AssetDependencyId load id
							find Dependency of id by id with DOMAIN.id
							
							// Process the PRIMARY asset in the dependency
    						extract AssetId load asset
							
							// Set some local variables to be reused
							extract AssetName set primaryName
							extract AssetType set primaryType
    
							find Application of asset by id with DOMAIN.asset 
   							elseFind Application of asset by assetName, assetType with SOURCE.AssetName, primaryType
       						elseFind Application of asset by assetName with SOURCE.DependentName
    						elseFind Asset of asset by assetName with SOURCE.DependentName warn 'found with wrong asset class'
    						
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
						kv.assetType == 'VM'
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
							find Dependency of id by id with DOMAIN.id
							
							// Grab the reference to the FINDINGS to be used later. 
							def primaryFindings = FINDINGS
	
							if (primaryFindings.size() > 0 ){
							 	set comment with 'Asset results found'		
							} else {
							 	set comment with 'Asset results not found'
							}
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Dependency.name()
				fields == ['id', 'comment'] as Set
				data.size() == 14
				data.collect { it.fields.id.value } == (1..14).collect { it.toString() }

				// Validates command: find Application of asset by id with DOMAIN.asset
				(1..13).eachWithIndex { int value, int index ->
					with(data[index].fields.id.find) {
						query.size() == 1
						with(query[0]) {
							domain == ETLDomain.Dependency.name()
							kv.id == value.toString()
						}
					}
				}
				// Validates command: set comment with 'Asset results found'
				data[0..data.size() - 1].collect { it.fields.comment.value }.unique() == ['Asset results not found']
			}

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can find a domain Property Name with loaded Data Value for a dependent'() {

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
							extract 'application id' load asset
							find Application of asset by id with DOMAIN.asset  
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
							set environment with Production
							extract 'application id' load id
							find Application of id by id with id
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'It throws an Exception because project was not defined'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'Project not defined.'

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can find multiple asset entities for a domain Property Name with loaded Data Value and use a warn message'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(applicationDataSetContent)

		and:
			List<Application> applications = [
					[assetClass: AssetClass.APPLICATION, id: 152253l, appVendor: 'Mozilla', assetName: "ACME Data Center", project: GMDEMO],
					[assetClass: AssetClass.APPLICATION, id: 152254l, appVendor: 'Microsoft', assetName: "ACME Data Center", project: GMDEMO]
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
							set environment with Production
							extract 'vendor name' load Vendor
							extract 'application id' load id
							find Application of id by id with SOURCE.'application id'
							elseFind Application of id by appVendor with DOMAIN.appVendor warn 'found without asset id field'
						}
						""".stripIndent(),
					ETLProcessor.class.name)

		then: 'Results should contain Application domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Application.name()

				with(data[0]) {
					fields.environment.originalValue == 'Production'
					fields.environment.value == 'Production'

					fields.appVendor.originalValue == 'Microsoft'
					fields.appVendor.value == 'Microsoft'

					fields.id.originalValue == '152254'
					fields.id.value == '152254'

					// Validating queries
					with(fields.id.find) {
						query[0].domain == ETLDomain.Application.name()
						query[0].kv == [id: '152254']

						query[1].domain == ETLDomain.Application.name()
						query[1].kv == [appVendor: 'Microsoft']

						size == 1
						results == [152254]
						matchOn == 1
					}

					!fields.id.warn
					!fields.id.warnMsg
				}

				with(data[1]) {
					fields.environment.originalValue == 'Production'
					fields.environment.value == 'Production'

					fields.appVendor.originalValue == 'Mozilla'
					fields.appVendor.value == 'Mozilla'

					fields.id.originalValue == '152255'
					fields.id.value == '152255'

					// Validating queries
					with(fields.id.find) {
						query[0].domain == ETLDomain.Application.name()
						query[0].kv == [id: '152255']

						query[1].domain == ETLDomain.Application.name()
						query[1].kv == [appVendor: 'Mozilla']

						size == 1
						results == [152253]
						matchOn == 2
					}

					fields.id.warn
					fields.id.warnMsg == 'found without asset id field'
				}
			}

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
						
							extract AssetDependencyId load id
							extract AssetId load asset
							
							find Asset of asset by assetName with SOURCE.AssetName
							// Grab the reference to the FINDINGS to be used later. 
							def primaryFindings = FINDINGS
	
							if (primaryFindings.size() > 0 && primaryFindings.isApplication()){
							 	set comment with 'Asset results found'		
							} else {
							 	set comment with 'Asset results not found'
							}
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'It throws an Exception because project was not defined'

			ETLProcessorException e = thrown ETLProcessorException
			e.message == 'You cannot use isApplication with more than one results in FINDINGS'

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
    						elseFind Asset of asset by assetName with SOURCE.DependentName warn 'found with wrong asset class'
    						
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
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
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
	}

	void 'test can load Rack domain instances and find Rooms associated'() {

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
13358,,UPS 1,New Colo Provider,42U Rack,New Colo Provider / ACME Room 1,1,41,42,0,0,0,block3x5,L""".stripIndent())

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
							extract rackId load id 
							extract Location load location
							extract Room load room
					 
					        find Room of room by id with SOURCE.RoomId
						}
						""".stripIndent(),
				ETLProcessor.class.name)

		then: 'Results should contain Rack domain results associated'
			etlProcessor.result.domains.size() == 1
			with(etlProcessor.result.domains[0]) {
				domain == ETLDomain.Rack.name()
				fields == ['id', 'location', 'room'] as Set

				data.collect { it.fields.id.value } == [
					racks[0].id.toString(), '13145',
					racks[1].id.toString(),
					racks[2].id.toString(), '13358'
				]

				data.collect { it.fields.location.value } == [
					'ACME Data Center', 'ACME Data Center', 'ACME Data Center',
					'ACME Data Center', 'New Colo Provider'
				]

				data.collect { it.fields.room.value } == [
					'ACME Data Center / DC1', 'ACME Data Center / DC1',
					'ACME Data Center / DC1', 'ACME Data Center / DC1',
					'New Colo Provider / ACME Room 1'
				]

				with(data[4].fields.room){
					find.query.size() == 1
					with(find.query[0]) {
						domain == ETLDomain.Room.name()
						!kv.id
						kv.error == 'Named parameter [id] value may not be null'
					}
				}

			}

		cleanup:
			if(fileName) fileSystemService.deleteTemporaryFile(fileName)
	}


	/**
	 * Builds a list of Mock Room using this fields order
	 * ['id', 'project', 'roomName', 'location', 'roomDepth', 'roomWidth', 'address', 'city', 'stateProv', 'postalCode']
	 * @param valuesList
	 * @return a list of Mock(Room)
	 */
	List<Room> buildRooms(List<List<?>> valuesList) {
		return valuesList.collect { List<?> values ->
			Room room = new Room()
			room.project = values[0]
			room.roomName = values[1]
			room.location = values[2]
			room.roomDepth = values[3]
			room.roomWidth = values[4]
			room.address = values[5]
			room.city = values[6]
			room.stateProv = values[7]
			room.postalCode = values[8]
			room.save()
			room
		}
	}

	/**
	 * Builds a list of Mock Room using this fields order
	 * ['project', 'room', 'manufacturer', 'model', 'location', 'front', 'source', 'roomX', 'roomY', 'powerA', 'powerB', 'powerC', 'rackType'],
	 * @param valuesList
	 * @return a list of Mock(Rack)
	 */
	List<Rack> buildRacks(List<List<?>> valuesList) {
		return valuesList.collect { List<?> values ->
			Rack rack = new Rack()
			rack.project = values[0]
			rack.room = values[1]
			rack.manufacturer = values[2]
			rack.model = values[3]
			rack.location = values[4]
			rack.front = values[5]
			rack.source = values[6]
			rack.roomX = values[7]
			rack.roomY = values[8]
			rack.powerA = values[9]
			rack.powerB = values[10]
			rack.powerC = values[11]
			rack.rackType = values[12]
			rack.save()
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
		switch (asset) {
			case AssetClass.APPLICATION:
				fieldSpecs = [
						buildFieldSpec('id', 'Id', 'Number'),
						buildFieldSpec('appVendor', 'Vendor'),
						buildFieldSpec('environment', 'Environment'),
						buildFieldSpec('description', 'Description'),
						buildFieldSpec('assetType', 'AssetType'),
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
			case CustomDomainService.COMMON:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('assetType', 'AssetType'),
					buildFieldSpec('assetName', 'Name'),
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
				control    : type,
				default    : '',
				field      : field,
				imp        : 'U',
				label      : label,
				order      : 0,
				shared     : 0,
				show       : 0,
				tip        : "",
				udf        : 0
		]
	}

	/**
	 * Builds a CSV dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	private List buildCSVDataSet(String csvContent) {

		def (String fileName, OutputStream sixRowsDataSetOS) = fileSystemService.createTemporaryFile('unit-test-', 'csv')
		sixRowsDataSetOS << csvContent
		sixRowsDataSetOS.close()

		String fullName = fileSystemService.getTemporaryFullFilename(fileName)

		CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fullName))
		CSVDataset dataSet = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fullName), header: true)

		return [fileName, new DataSetFacade(dataSet)]
	}
}