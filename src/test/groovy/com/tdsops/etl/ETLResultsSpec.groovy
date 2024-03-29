package com.tdsops.etl

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.etl.dataset.ETLDataset
import com.tdsops.tm.enums.domain.AssetClass
import grails.testing.gorm.DataTest
import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.imports.DataScript
import net.transitionmanager.model.Model
import net.transitionmanager.project.Project
import org.grails.web.json.JSONObject
import spock.lang.See

/**
 * Test about ETLProcessorResults and JSON transformation:
 */
class ETLResultsSpec extends ETLBaseSpec implements DataTest {

	String assetDependencyDataSetContent
	String applicationDataSetContent
	String deviceBladeChassisDataSetContent
	String deviceDataSetContent
	Project GMDEMO
	Project TMDEMO
	DebugConsole debugConsole
	ETLFieldsValidator validator

    Closure doWithSpring() {
        { ->
            coreService(CoreService) {
                grailsApplication = ref('grailsApplication')
            }
            fileSystemService(FileSystemService) {
                coreService = ref('coreService')
            }
            applicationContextHolder(ApplicationContextHolder) { bean ->
                bean.factoryMethod = 'getInstance'
            }
        }
    }

	def setupSpec() {
		mockDomains DataScript, AssetDependency, AssetEntity, Application, Database, Rack, Room, Database, Model
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
			152256,Slideaway,ATEN,322224,4344344,VMAX-1,ACME Data Center,VMAX 20K Rack,ACME Data Center / DC1,1,160,0,1430,1430,0,Rack,R""".
			stripIndent()

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
		ETLProcessorResult.registerObjectMarshaller()
	}

	def teardown(){
	}

	@See ('TM-10695')
	void 'test can transform to JSON the result of an ETL script evaluation'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet(applicationDataSetContent)

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
				applications.findAll { it.id == args.id && it.project.id == args.project.id }
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

			JSONObject jsonResult = new JSONObject(etlProcessor.finalResult().properties)

		then: 'Results should contain Application domain results associated'
			assertWith(jsonResult) {
				domains.size() == 1
				assertWith(domains[0]) {
					domain == ETLDomain.Application.name()
					assertWith(data[0].fields.environment) {
						originalValue == 'Production'
						value == 'Production'
					}

					assertWith(data[0].fields.id) {
						originalValue == '152254'
						value == '152254'

						 
						find.query.size() == 1
						assertWith(find.query[0]) {
							domain == 'Application'
							assertWith(criteria[0]){
								propertyName == 'id'
								operator == 'eq'
								value == 152254l
							}
						}
					}

					assertWith(data[1].fields.environment) {
						originalValue == 'Production'
						value == 'Production'
					}

					assertWith(data[1].fields.id) {
						originalValue == '152255'
						value == '152255'

						find.query.size() == 1
						assertWith(find.query[0]) {
							domain == 'Application'
							assertWith(criteria[0]){
								propertyName == 'id'
								operator == 'eq'
								value == 152255l
							}
						}
					}
				}
			}

		cleanup:
			if (fileName) fileSystemServiceTestBean.deleteTemporaryFile(fileName)
	}
}
