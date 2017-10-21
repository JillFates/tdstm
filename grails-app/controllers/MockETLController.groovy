import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
import getl.tfs.TFS
import getl.utils.FileUtils
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.domain.DataScriptMode
import net.transitionmanager.service.dataingestion.ScriptProcessorService
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException


@Slf4j(value='log', category='grails.app.controllers.MockETLController')
@Secured('isAuthenticated()')
class MockETLController implements ControllerMethods {

    ScriptProcessorService scriptProcessorService

    String exampleData = """DEVICE ID,MODEL NAME,MANUFACTURER NAME,ENVIRONMENT
152254,SRW24G4,LINKSYS,Prod
152255,ZPHA MODULE,TippingPoint,Dev
152256,Slideaway,ATEN,Prod
152258,CCM4850,Avocent,Prod
152259,DSR2035,Avocent,Dev
152266,2U Cable Management,Generic,Prod
152275,ProLiant BL465c G7,HP,Dev"""

    String exampleScript = """console on

read labels
iterate {
    domain Application
    extract 0 load id
    extract 'MODEL NAME' transform lowercase load Name
    extract 2 transform uppercase  load description
    load environment with 'Production'
    //reference id with id
    reference assetName, id with Name, id

    domain Device
    extract 0 load id
    extract 'MODEL NAME' transform uppercase load Name

}
""".stripIndent()


    def index () {

        Project project = securityService.userCurrentProject
        ErrorCollector errorCollector
        String missingPropertyError
        Integer lineNumber
        ETLProcessor etlProcessor

        def mockData
        def script

        try {

            if (params.fetch && params.file) {

//                String fileName = "${TFS.systemPath}/${UUID.randomUUID()}".toString()
//                def file = new File(fileName).newOutputStream()

                URLConnection connection = new URL(params.file).openConnection()
                String userpass = "Dcorrea:boston2004"
                String basicAuth = "Basic " + userpass.encodeAsBase64()
                connection.setRequestProperty("Authorization", basicAuth)
                connection.setRequestProperty('Accept', 'application/csv')

                String fileName = "${TFS.systemPath}/cmdb_ci_appl_list.csv".toString()
                def file = new File(fileName).newOutputStream()
                file << connection.inputStream
                file.close()

                params.script = "console on"
                script = params.script
                params.mockData = new File(fileName).text?.replace('"', '')
                mockData = params.mockData

                etlProcessor = scriptProcessorService.executeForDemo(project, params.script, fileName)

            } else {

                mockData = params.mockData ? """${params.mockData}""" : exampleData

                List<List<String>> data = []

                mockData.trim().eachLine { line, count ->
                    data.addAll([line.split(',') as List])
                }

                script = params.script ?: ""
                etlProcessor = scriptProcessorService.executeForDemo(project, script, data)
            }


        } catch (MultipleCompilationErrorsException cfe) {
            errorCollector = cfe.getErrorCollector()
        } catch (MissingPropertyException mpe) {
            lineNumber = mpe.stackTrace.find { StackTraceElement ste -> ste.fileName == ETLProcessor.class.name }?.lineNumber
            missingPropertyError = mpe.getMessage()
        } catch (ETLProcessorException pe) {
            missingPropertyError = pe.getMessage()
        } catch (Exception ioe) {
            missingPropertyError = ioe.getMessage()
        }

        [
                mockData            : mockData,
                script              : script?.trim(),
                lineNumbers         : Math.max(script.readLines().size(), 10),
                etlProcessor        : etlProcessor,
                errorCollector      : errorCollector,
                lineNumber          : lineNumber,
                availableMethods    : (etlProcessor?.availableMethods as JSON).toString(),
                assetFields         : (etlProcessor?.assetFields as JSON).toString(),
                missingPropertyError: missingPropertyError,
                logContent          : etlProcessor?.debugConsole?.content(),
                jsonResult          : (etlProcessor?.results as JSON),
                dataScriptId        : params.dataScriptId,
                providerName        : params.providerName,
                dataScriptName      : params.dataScriptName

        ]
    }

    /**
     * Used to retrieve the ETL source code for a particular DataScript
     * @param id
     * @return
     */
    def dataScriptSource(Long id) {
        Project project = getProjectForWs()

        switch(request.method) {
            case 'GET':
                if (! id) {
                    sendInvalidInput('Please provide Data Script Id')
                    return
                }
                DataScript script = DataScript.findByProjectAndId(project, id)
                if (script) {
                    Map result = [id:script.id, script:script.etlSourceCode, provider: script.provider.name, name: script.name]
                    renderAsJson(result)
                } else {
                    sendNotFound()
                }
                break

            case 'POST':
                if (! id) {
                    sendInvalidInput('Please provide Data Script Id')
                    return
                }
                DataScript script = DataScript.findByProjectAndId(project, id)
                if (! script) {
                    sendNotFound()
                    return
                }

                if (! request.JSON.script) {
                    sendInvalidInput('Update was missing the script or script was blank')
                } else {
                    script.etlSourceCode = request.JSON.script
                    script.save(failOnError:true)
                    log.debug 'Saved script: {}', request.JSON.script
                    render text:'success'

                }
                break

            case 'PUT':
                if (! request.JSON.script) {
                    sendInvalidInput('Can not create a blank script')
                    return
                }

                if (! request.JSON.providerName) {
                    sendInvalidInput('Must specify a Provider name')
                    return
                }

                if (! request.JSON.name) {
                    sendInvalidInput('Must specify a name for the script')
                    return
                }

                Provider provider = Provider.findByProjectAndName(project, request.JSON.providerName)
                if (!provider) {
                    sendInvalidInput('Provider name was not found')
                    return
                }

                // Check for existing Script by the same name
                DataScript script = DataScript.findByProviderAndName(provider, request.JSON.name)
                if (script) {
                    sendInvalidInput('Script name already exists')
                    return
                }

                script = new DataScript(
                    project: project,
                    name: request.JSON.name,
                    mode:DataScriptMode.IMPORT,
                    etlSourceCode: request.JSON.script,
                    provider: provider,
                    createdBy: currentPerson(),
                    target: 'not null',
                    description: 'some description'
                )
                //script.provider = provider

                script.save(flush:true, failOnError:true)
                Map results = [id:script.id]
                renderAsJson(results)

                break

            default:
                sendInvalidInput("Unsupported request method ${request.method}")
        }

    }
}
