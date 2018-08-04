package com.tdsops.etl

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdssrc.grails.StringUtil
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
import spock.lang.Issue

/**
 * Test about ETLProcessor commands:
 * <ul>
 *     <li><b>lookup</b></li>
 * </ul>
 */
@TestFor(FileSystemService)
@Mock([DataScript, AssetDependency, AssetEntity, Application, Database, Files, Room, Manufacturer, MoveBundle, Rack, Model])
class ETLLookupSpec extends ETLBaseSpec {


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
		GMDEMO = Mock(Project)
		GMDEMO.getId() >> 125612l

		TMDEMO = Mock(Project)
		TMDEMO.getId() >> 125612l

		validator = createDomainClassFieldsValidator()

		debugConsole = new DebugConsole(buffer: new StringBuffer())
	}

	void 'test can throw an Exception if lookup does not contain a valid field name'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(DependencyDataSetContent)

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
							extract 'server' load 'Name' set nameVar
							extract 'model' load 'model'
							extract 'dependsOn' set dependsOnVar

							lookup 'unknown' with 'dependsOnVar'
							if ( LOOKUP ) {
								load 'custom1' with 'dependsOnVar'
							} else {
								log 'Repeated asset'
							}
						}
						""".stripIndent())

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown ETLProcessorException
			e.message == ETLProcessorException.unknownDomainFieldName(ETLDomain.Device, 'unknown').message

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can lookup results and used LOOKUP.found() to check results'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(DependencyDataSetContent)

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
							extract 'server' load 'Name' set nameVar
							extract 'model' load 'model'
							extract 'dependsOn' set dependsOnVar

							lookup 'assetName' with dependsOnVar
							if ( LOOKUP.found() ) {
								load 'custom1' with dependsOnVar
							} else {
								log 'Repeated asset'
							}
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'model', 'custom1'] as Set
					data.size() == 3
					with(data[0]) {
						with(fields.assetName) {
							value == 'xray01'
							originalValue == 'xray01'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
						with(fields.custom1) {
							value == 'xray01'
							originalValue == 'xray01'
						}
					}

					with(data[1]) {
						with(fields.assetName) {
							value == 'deltasrv03'
							originalValue == 'deltasrv03'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}

					with(data[2]) {
						with(fields.assetName) {
							value == 'alpha'
							originalValue == 'alpha'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}
				}
			}
			etlProcessor.debugConsole.content().count('Repeated asset') == 2

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	@Issue("https://support.transitionmanager.com/browse/TM-10625")
	void 'test group data using LOOKUP as a String list in a custom field'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(DependencyDataSetContent)

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
							extract 'server' set nameVar
							extract 'model' set modelVar
							extract 'dependsOn' set dependsOnVar

							lookup 'model' with modelVar
							if ( LOOKUP.notFound() ) {
								load 'model' with modelVar
								load 'custom1' with nameVar
							} else {
								load 'custom1' with DOMAIN.custom1 + ', ' + nameVar
							}
						}

						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with (etlProcessor.finalResult()){
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Device.name()
					fieldNames == ['model', 'custom1'] as Set
					data.size() == 1
					with(data[0]){
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
						with(fields.custom1){
							value == 'xray01, deltasrv03, alpha'
							originalValue == 'xray01, deltasrv03, alpha'
						}
					}
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can lookup results and used LOOKUP.notFound() to check results'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(DependencyDataSetContent)

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
							extract 'server' load 'Name' set nameVar
							extract 'model' load 'model'
							extract 'dependsOn' set dependsOnVar

							lookup 'assetName' with dependsOnVar
							if ( LOOKUP.notFound() ) {
								log 'Repeated asset'
							}
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'model'] as Set
					data.size() == 3
					with(data[0]) {
						with(fields.assetName) {
							value == 'xray01'
							originalValue == 'xray01'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}

					with(data[1]) {
						with(fields.assetName) {
							value == 'deltasrv03'
							originalValue == 'deltasrv03'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}

					with(data[2]) {
						with(fields.assetName) {
							value == 'alpha'
							originalValue == 'alpha'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}
				}
			}
			etlProcessor.debugConsole.content().count('Repeated asset') == 2

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test that when the lookup finds previous results that the current result is the earlier one'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(DependencyDataSetContent)

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
							extract 'server' load 'Name' set nameVar
							extract 'model' load 'model'
							extract 'dependsOn' set dependsOnVar

							lookup 'assetName' with dependsOnVar
							if ( LOOKUP ) {
								load 'custom1' with dependsOnVar
							} else {
								log 'Repeated asset'
							}
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'model', 'custom1'] as Set
					data.size() == 3
					with(data[0]) {
						with(fields.assetName) {
							value == 'xray01'
							originalValue == 'xray01'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
						with(fields.custom1) {
							value == 'xray01'
							originalValue == 'xray01'
						}
					}

					with(data[1]) {
						with(fields.assetName) {
							value == 'deltasrv03'
							originalValue == 'deltasrv03'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}

					with(data[2]) {
						with(fields.assetName) {
							value == 'alpha'
							originalValue == 'alpha'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}
				}
			}
			etlProcessor.debugConsole.content().count('Repeated asset') == 2

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test can lookup results and used !LOOKUP to check results'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(DependencyDataSetContent)

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
							extract 'server' load 'Name' set nameVar
							extract 'model' load 'model'
							extract 'dependsOn' set dependsOnVar

							lookup 'assetName' with dependsOnVar
							if ( !LOOKUP ) {
								log 'Repeated asset'
							}
						}
						""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 1
				with(domains[0]) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'model'] as Set
					data.size() == 3
					with(data[0]) {
						with(fields.assetName) {
							value == 'xray01'
							originalValue == 'xray01'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}

					with(data[1]) {
						with(fields.assetName) {
							value == 'deltasrv03'
							originalValue == 'deltasrv03'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}

					with(data[2]) {
						with(fields.assetName) {
							value == 'alpha'
							originalValue == 'alpha'
						}
						with(fields.model) {
							value == 'VM'
							originalValue == 'VM'
						}
					}
				}
			}
			etlProcessor.debugConsole.content().count('Repeated asset') == 2


		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	void 'test when lookup does not find results that the current result is new'() {

		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet(RVToolsCSVContent)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		and:
			GroovySpy(Application, global: true)
			Application.executeQuery(_, _) >> { String query, Map args ->
				return []
			}

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
					def assetTypeVM = 'VM'
					def vmWare = 'VMWare'

					read labels
					iterate {
						domain Device

							extract 'Vm' load 'Name'
							def vmName = CE

							extract 'Vm Id' load 'externalRefId'
							extract 'Vm Uuid' load 'serialNumber'
							extract 'OS according to the VMware Tools' load 'os'

							// Grab the cluster name to be used in Application and Dependency section
							extract 'Cluster' set clusterNameVar

						domain Application
							lookup 'assetName' with clusterNameVar
							if ( LOOKUP.notFound() ) {
								load 'assetName' with clusterNameVar

								load 'id' with ''
								find Application by 'assetName', 'appVendor' with DOMAIN.assetName, 'vmWare' into 'id'
								elseFind Application by 'assetName' with DOMAIN.assetName into 'id' warn 'Not sure about this match'
								whenNotFound 'id' create {
									assetClass Application
									assetName clusterNameVar
									appVendor 'vmWare'
								}
							}

						domain Dependency

							/*
							* Create the dependencies
							*
							* Host supports the Cluster
							* Cluster supports the VM
							*/

							load 'asset' with 'vmName'
							find Device by 'assetName', 'assetType' with 'vmName', 'assetTypeVM' into 'asset'
							whenNotFound 'asset' create {
								assetClass Device
								assetName 'vmName'
								assetType 'assetTypeVM'
								manufacturer 'VMWare'
								model 'VM'
							}

							load 'dependent' with 'clusterName'
							find Application by 'assetName' with 'clusterName' into 'dependent'
							whenNotFound 'dependent' create {
								assetClass Application
								assetName 'clusterName'
								appVendor 'VMWare'
							}

							load 'type' with 'Hosts'
							load 'status' with 'Validated'
							load 'dataFlowFreq' with 'constant'
							initialize 'comment' with 'From RVTools'
					}
					""".stripIndent())

		then: 'Results should contain Application domain results associated'
			with(etlProcessor.finalResult()) {
				domains.size() == 3
				with(domains[0]) {
					domain == ETLDomain.Device.name()
					fieldNames == ['assetName', 'externalRefId', 'serialNumber', 'os'] as Set
					data.size() == 5
				}

				with(domains[1]) {
					domain == ETLDomain.Application.name()
					fieldNames == ['assetName', 'id'] as Set
					data.size() == 2
				}

				with(domains[2]) {
					domain == ETLDomain.Dependency.name()
					fieldNames == ['asset', 'dependent', 'type', 'status', 'dataFlowFreq', 'comment'] as Set
					data.size() == 5
				}
			}

		cleanup:
			if(fileName) service.deleteTemporaryFile(fileName)
	}

	static final String DependencyDataSetContent = """server,model,dependsOn
xray01,VM,
deltasrv03,VM,xray01
alpha,VM,""".stripIndent().trim()

	static String RVToolsCSVContent = """VM,Powerstate,Template,Config status,DNS Name,Connection state,Guest state,Heartbeat,Consolidation Needed,PowerOn,Suspend time,Change Version,CPUs,Memory,NICs,Disks,Network #1,Network #2,Network #3,Network #4,Num Monitors,Video Ram KB,Resource pool,Folder,vApp,DAS protection,FT State,FT Latency,FT Bandwidth,FT Sec. Latency,Provisioned MB,In Use MB,Unshared MB,HA Restart Priority,HA Isolation Response,HA VM Monitoring,Cluster rule(s),Cluster rule name(s),Boot Required,Boot delay,Boot retry delay,Boot retry enabled,Boot BIOS setup,Firmware,HW version,HW upgrade status,HW upgrade policy,HW target,Path,Annotation,BridgeWays.VMware.ESX.CustomField.CollectCPUStatistics,BridgeWays.VMware.ESX.CustomField.CollectMemoryStatistics,BridgeWays.VMware.ESX.CustomField.CollectNetStatistics,BridgeWays.VMware.ESX.CustomField.WasteWhitelisted,NB_LAST_BACKUP,Datacenter,Cluster,Host,OS according to the configuration file,OS according to the VMware Tools,VM ID,VM UUID,VI SDK Server type,VI SDK API Version,VI SDK Server,VI SDK UUID
59,poweredOff,False,green,,connected,notRunning,gray,False,,,2017-11-28T19:32:21.560802Z,2,"16,384",1,2,PROD_10_3_24,,,,1,"4,096",/MD_DATACENTER/MD_CLUSTER_1/Resources,,,,notConfigured,gray,-1,-1,"206,057","189,440","189,440",medium,none,vmMonitoringOnly,,,False,0,"10,000",True,True,bios,8,none,never,,[AP_VNX5400_ESX06_PROD] 59/59.vmx,,,,,,,MD_DATACENTER,MD_CLUSTER_1,apesx01.moredirect.com,Red Hat Enterprise Linux 6 (64-bit),Red Hat Enterprise Linux 6 (64-bit),vm-98720,422e6d92-22c1-c6c1-1de8-eceec50f94bc,VMware vCenter Server 5.1.0 build-880146,5.1,apvcenter.moredirect.com,258FD56F-AB48-4992-9D1D-8CED6C827CF0
59admin,poweredOn,False,green,59admin.moredirect.com,connected,running,gray,False,2017/11/28 16:14:28,,2017-11-28T21:14:28.910375Z,2,"16,384",1,2,PROD_10_3_24,,,,1,"8,192",/MD_DATACENTER/MD_CLUSTER_1/Resources,,,True,notConfigured,gray,-1,-1,"118,892","118,892","118,892",medium,none,vmMonitoringOnly,,,False,0,"10,000",True,False,bios,8,none,never,,[AP_VNX5400_ESX06_PROD] 59admin/59admin.vmx,,,,,,,MD_DATACENTER,MD_CLUSTER_1,apesx01.moredirect.com,Red Hat Enterprise Linux 6 (64-bit),Red Hat Enterprise Linux 6 (64-bit),vm-98718,422e2244-f78c-2012-b56a-e435d7519abf,VMware vCenter Server 5.1.0 build-880146,5.1,apvcenter.moredirect.com,258FD56F-AB48-4992-9D1D-8CED6C827CF0
APDC03,poweredOn,False,green,APDC03.pcc.int,connected,running,green,False,,,2017-02-02T22:38:49.341678Z,2,"4,096",1,2,PROD_10_3_24,,,,1,"8,192",/MD_DATACENTER/MD_CLUSTER_1/Resources,,,True,notConfigured,gray,-1,-1,"65,744","65,744","65,744",medium,none,vmMonitoringOnly,,,False,0,"10,000",False,False,bios,9,none,never,,[AP_VNX5300_ESX13_PROD] APDC03/APDC03.vmx,PCC.INT Domain Controller 6/3/15 - CMG,,,,,"Sat Jan 20 05:26:53 2018
,apntbkup01,VMWare_ESX_50_FlashBackup",MD_DATACENTER,MD_CLUSTER_1,apesx01.moredirect.com,Microsoft Windows Server 2012 (64-bit),Microsoft Windows Server 2012 (64-bit),vm-44956,422ea90a-b80a-81de-0d4c-6f111142c4f7,VMware vCenter Server 5.1.0 build-880146,5.1,apvcenter.moredirect.com,258FD56F-AB48-4992-9D1D-8CED6C827CF0
APESRS,poweredOn,False,green,,connected,notRunning,gray,False,,,2017-04-28T16:38:21.437802Z,1,"4,096",1,1,PROD_10_3_24,,,,1,"4,096",/MD_DATACENTER/MD_CLUSTER_1/Resources,,,True,notConfigured,gray,-1,-1,"69,723","69,723","69,723",medium,none,vmMonitoringOnly,,,False,0,"10,000",False,False,bios,4,none,never,,[AP_VNX5300_ESX07_PROD] APESRS/APESRS.vmx,,,,,,,MD_DATACENTER,MD_CLUSTER_DEV,apesx01.moredirect.com,SUSE Linux Enterprise 11 (64-bit),SUSE Linux Enterprise 11 (64-bit),vm-67122,422e1dd3-acd2-9e60-3720-2d69ba848df2,VMware vCenter Server 5.1.0 build-880146,5.1,apvcenter.moredirect.com,258FD56F-AB48-4992-9D1D-8CED6C827CF0
APESX01-SCVM,poweredOn,False,green,APESX01-SCVM,connected,running,green,False,2018/01/16 16:43:22,,2018-01-16T21:43:23.155196Z,2,"4,096",1,1,PROD_10_3_24,,,,1,"4,096",/MD_DATACENTER/MD_CLUSTER_1/Resources,,,True,notConfigured,gray,-1,-1,"20,702","20,702","20,702",medium,none,vmMonitoringOnly,,,False,0,"10,000",False,False,bios,9,none,never,,[AP_VNX5400_ESX06_PROD] APESX01-SCVM/APESX01-SCVM.vmx,"The ""Sophos for Virtual Environments"" Appliance, part of the Sophos Endpoint Security suite of products, provides a unique integrated solution that centralizes threat protection across virtual machines.",,,,,,MD_DATACENTER,MD_CLUSTER_1,apesx01.moredirect.com,Other Linux (64-bit),Ubuntu Linux (64-bit),vm-100365,422ed1b6-466d-8155-4483-fb786405a8a3,VMware vCenter Server 5.1.0 build-880146,5.1,apvcenter.moredirect.com,258FD56F-AB48-4992-9D1D-8CED6C827CF0""".stripIndent()
}
