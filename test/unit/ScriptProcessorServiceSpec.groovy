import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tdsops.etl.ETLDomain
import com.tdsops.tm.enums.domain.AssetClass
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.json.JSONConnection
import getl.proc.Flow
import getl.tfs.TFS
import getl.utils.FileUtils
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import net.transitionmanager.domain.*
import net.transitionmanager.service.CoreService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.SettingService
import net.transitionmanager.service.dataingestion.ScriptProcessorService
import org.codehaus.groovy.grails.commons.GrailsApplication
import spock.lang.Shared
import spock.lang.Specification

@TestFor(ScriptProcessorService)
@Mock([DataScript, Project, Database, AssetEntity, Setting])
class ScriptProcessorServiceSpec extends Specification {


    @Shared
    Map conParams = [path: "${TFS.systemPath}/test_path_csv", createPath: true, extension: 'csv', codePage: 'utf-8']

    @Shared
    CSVConnection csvConnection

    @Shared
    JSONConnection jsonConnection

    FileSystemService fileSystemService

    CSVDataset sixRowsDataSet
    CSVDataset applicationDataSet

    GrailsApplication grailsApplication

    static doWithSpring = {
        coreService(CoreService) {
            grailsApplication = ref('grailsApplication')
        }
        fileSystemService(FileSystemService) {
            coreService = ref('coreService')
        }
        settingService(SettingService)
        customDomainService(MockCustomDomainService)
    }

    def setupSpec () {
        csvConnection = new CSVConnection(config: conParams.extension, path: conParams.path, createPath: true)
        jsonConnection = new JSONConnection(config: 'json')
        FileUtils.ValidPath(conParams.path)
    }

    def cleanupSpec () {
        new File(conParams.path).deleteOnExit()
    }

    def setup () {

        sixRowsDataSet = new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true)
        sixRowsDataSet.field << new getl.data.Field(name: 'device id', alias: 'DEVICE ID', type: "STRING", isKey: true)
        sixRowsDataSet.field << new getl.data.Field(name: 'model name', alias: 'MODEL NAME', type: "STRING")
        sixRowsDataSet.field << new getl.data.Field(name: 'manufacturer name', alias: 'MANUFACTURER NAME', type: "STRING")

        new Flow().writeTo(dest: sixRowsDataSet, dest_append: true) { updater ->
            updater(['device id': "152251", 'model name': "SRW24G1", 'manufacturer name': "LINKSYS"])
            updater(['device id': "152252", 'mnodel name': "SRW24G2", 'manufacturer name': "LINKSYS"])
            updater(['device id': "152253", 'model name': "SRW24G3", 'manufacturer name': "LINKSYS"])
            updater(['device id': "152254", 'model name': "SRW24G4", 'manufacturer name': "LINKSYS"])
            updater(['device id': "152255", 'model name': "SRW24G5", 'manufacturer name': "LINKSYS"])
            updater(['device id': "152256", 'model name': "ZPHA MODULE", 'manufacturer name': "TippingPoint"])
        }

        applicationDataSet = new CSVDataset(connection: csvConnection, fileName: "${UUID.randomUUID()}.csv", autoSchema: true)
        applicationDataSet.field << new getl.data.Field(name: 'application id', alias: 'APPLICATION ID', type: "STRING", isKey: true)
        applicationDataSet.field << new getl.data.Field(name: 'vendor name', alias: 'VENDOR NAME', type: "STRING")
        applicationDataSet.field << new getl.data.Field(name: 'technology', alias: 'TECHNOLOGY', type: "STRING")
        applicationDataSet.field << new getl.data.Field(name: 'location', alias: 'LOCATION', type: "STRING")

        new Flow().writeTo(dest: applicationDataSet, dest_append: true) { updater ->
            updater(['application id': '152254', 'vendor name': 'Microsoft', 'technology': '(xlsx updated)', 'location': 'ACME Data Center'])
            updater(['application id': '152255', 'vendor name': 'Mozilla', 'technology': 'NGM', 'location': 'ACME Data Center'])
        }
    }

    def cleanup () {

    }

    def 'test can check a script content without errors' () {

        given:
            Project project = GroovyMock(Project)
        and:
            String script = """
                console on
                iterate { }
                
            """.stripIndent()

        when: 'Service executes the script with correct syntax'
            Map<String, ?> result = service.checkSyntax(project, script, sixRowsDataSet.fullFileName())

        then: 'Service result has validSyntax equals true an a empty list of errors'
            with(result) {
                validSyntax
                errors.isEmpty()
            }
    }

    def 'test can check a script content with an incorrect iterate closure definition' () {

        given:
            Project project = GroovyMock(Project)
        and:
            String script = """
                console on
                iterate {
                
            """.stripIndent()

        when: 'Service executes the script with incorrect syntax'
            Map<String, ?> result = service.checkSyntax(project, script, sixRowsDataSet.fullFileName())

        then: 'Service result has validSyntax equals false and a list of errors'
            with(result) {
                !validSyntax
                errors.size() == 1
                with(errors[0]) {
                    startLine == 2
                    endLine == 2
                    startColumn == 10
                    endColumn == 11
                    fatal
                    message == 'unexpected token:  @ line 2, column 10.'
                }
            }
    }

    def 'test can test a script content for Application domain Asset' () {

        given:

            Project GMDEMO = Mock(Project)
            GMDEMO.getId() >> 125612l

            Project TMDEMO = Mock(Project)
            TMDEMO.getId() >> 125612l

            List<AssetEntity> applications = [
                    [assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
                    [assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "ACME Data Center", project: GMDEMO],
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
            String script = """
                console on
                read labels
                iterate {
                    domain Application
                    load environment with Production
                    extract 'location' load Vendor
                    reference assetName with Vendor
                }
            """.stripIndent()

        when: 'Service executes the script with incorrect syntax'
            Map<String, ?> result = service.testScript(GMDEMO, script, applicationDataSet.fullFileName())

        then: 'Service result has validSyntax equals false and a list of errors'
            with(result) {
                consoleLog.contains('INFO - Reading labels [0:application id, 1:vendor name, 2:technology, 3:location]')
                with(data.get(ETLDomain.Application)[0]) {

                    with(elements[0]) {
                        originalValue == "Production"
                        value == "Production"
                        field.name == "environment"
                    }

                    with(elements[1]) {
                        originalValue == "ACME Data Center"
                        value == "ACME Data Center"
                        field.name == "Vendor"
                        field.label == "Vendor"
                    }

                    reference == [152254, 152255]
                }

                with(data.get(ETLDomain.Application)[1]) {

                    with(elements[0]) {
                        originalValue == "Production"
                        value == "Production"
                        field.name == "environment"
                    }

                    with(elements[1]) {
                        originalValue == "ACME Data Center"
                        value == "ACME Data Center"
                        field.name == "Vendor"
                        field.label == "Vendor"
                    }

                    reference == [152254, 152255]
                }

            }
    }

    def 'test can save a script' () {

        given:
            String etlSourceCode = """
                console on
                read labels
                iterate {
                    domain Application
                    load environment with Production
                    extract 'location' load Vendor
                    reference assetName with Vendor
                }
            """.stripIndent()

            DataScript dataScript = GroovyMock(DataScript)
            dataScript.getName() >> 'Script test'
            dataScript.getDescription() >> 'Description Test'
            dataScript.getTarget() >> 'Target Test'
            dataScript.getMode() >> DataScriptMode.IMPORT
            dataScript.getProvider() >> GroovyMock(Provider)
            dataScript.getEtlSourceCode() >> etlSourceCode
            dataScript.getProject() >> GroovyMock(Project)
            dataScript.getCreatedBy() >> GroovyMock(Person)


        when: 'Service saves the scripts updating source code'
            DataScript result = service.saveScript(dataScript, etlSourceCode)

        then: 'Service results contains console details and ETLProcessor results'
            result.etlSourceCode == etlSourceCode
    }
}

class MockCustomDomainService extends CustomDomainService {

    Map fieldSpecsMap = [
            (AssetClass.APPLICATION.toString()): [
                    [constraints: [required: 0],
                     "control"  : "Number",
                     "default"  : "",
                     "field"    : "id",
                     "imp"      : "U",
                     "label"    : "Id",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ],
                    [constraints: [required: 0],
                     "control"  : "String",
                     "default"  : "",
                     "field"    : "appVendor",
                     "imp"      : "N",
                     "label"    : "Vendor",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ],
                    [constraints: [required: 0],
                     "control"  : "String",
                     "default"  : "",
                     "field"    : "environment",
                     "imp"      : "N",
                     "label"    : "Environment",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ],
                    [constraints: [required: 0],
                     "control"  : "String",
                     "default"  : "",
                     "field"    : "location",
                     "imp"      : "N",
                     "label"    : "Location",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ],
            (AssetClass.DEVICE.toString())     : [
                    [constraints: [required: 0],
                     "control"  : "Number",
                     "default"  : "",
                     "field"    : "id",
                     "imp"      : "U",
                     "label"    : "Id",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ],
                    [constraints: [required: 0],
                     "control"  : "String",
                     "default"  : "",
                     "field"    : "location",
                     "imp"      : "N",
                     "label"    : "Location",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ]
    ]
    /**
     * Retrieve all field specifications as a Map
     * @param domain
     * @return
     */
    Map allFieldSpecs (Project project, String domain) {
        Map fieldSpec = [:]

        fieldSpec[domain] = [fields: fieldSpecsMap[domain]]
        fieldSpec
    }

}