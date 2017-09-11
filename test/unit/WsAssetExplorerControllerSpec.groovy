import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files
import grails.converters.JSON
import grails.test.mixin.TestFor
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.command.PaginationCommand
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.SecurityService
import org.codehaus.groovy.grails.web.json.JSONObject
import test.AbstractUnitSpec

import static javax.servlet.http.HttpServletResponse.SC_OK

/**
 * TODO: implement tests for controller.
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(WsAssetExplorerController)
@Mock([Project, UserLogin, Application, AssetEntity, Database, Files])
class WsAssetExplorerControllerSpec extends AbstractUnitSpec {

    def setup() {
    }

    def cleanup() {
    }

    void "test pagination valid command object"() {

        given: ''
        controller.dataviewService = new DataviewService() {
            List<Map> previewQuery(
                    Project project,
                    Class domainClass,
                    JSONObject dataviewSpecJson,
                    DataviewUserParamsCommand userParams,
                    PaginationCommand pagination) {
                []
            }
        }

        controller.securityService = new SecurityService() {
            Project getUserCurrentProject() {
                new Project()
            }
        }
        login()

        and:
        def jsonBody = [
                "offset"      : 5,
                "limit"       : 25,
                "sortDomain"  : "application",
                "sortProperty": "license",
                "sortOrder"   : "a",
                "justPlanning": true,
                "filters"     : [
                        "domains": ["application", "device"],
                        "columns": [
                                ["domain": "common", "property": "environment", "filter": "production|development"],
                                ["domain": "common", "property": "assetName", "filter": "exchange"],
                                ["domain": "application", "property": "license", "filter": ""]
                        ],
                        "justPlanning": true
                ]
        ] as JSON

        when:
        request.json = jsonBody
        request.method = 'POST'
        controller.previewQuery()

        then:
        response.json.data == JSON.parse("""
            []
        """)
        response.status == SC_OK

    }

    void "test valid command object"() {

        when:
        def json = JSON.parse("""{
                "offset":5,
                "limit": 25,
                "sortDomain": "application",
                "sortProperty": "sme",
                "sortOrder": "a",
                "filters": {
                    "columns": [ 
                        {"domain": "common", "property": "environment", "filter": "production|development" },
                        {"domain": "common", "property": "assetName", "filter": "exchange" },
                    ],
                }
            }""")

        then:
        new PaginationCommand(json).validate()

    }
}
