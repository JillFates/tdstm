import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
import com.tdsops.etl.StringAppendElement
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.DataScriptMode
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.dataingestion.ScriptProcessorService
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException

@Slf4j(value = 'log', category = 'grails.app.controllers.MockETLController')
@Secured('isAuthenticated()')
class MockETLController implements ControllerMethods {

    ScriptProcessorService scriptProcessorService
    FileSystemService fileSystemService

    String exampleDataSet = """device id,model name,manufacturer name,environment
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

    extract 1 load id
    extract 'model name' transform with lowercase() load Name
    extract 3 transform with uppercase() load description

    set environment with 'Production'
    find Application 'for' id by id with SOURCE.'device id'

    domain Device

    extract 1 load id
    extract 'model name' transform with uppercase() load Name
    find Device 'for' id by id with SOURCE.'device id'
}""".stripIndent()


    def index () {

        Project project = securityService.userCurrentProject
        ErrorCollector errorCollector
        String missingPropertyError
        Integer lineNumber

        ETLProcessor etlProcessor

        String dataSet
        String script

        try {

            String.mixin StringAppendElement

            if (params.fetch && params.file) {

                URLConnection connection = new URL(params.file).openConnection()
                String userpass = "Dcorrea:boston2004"
                String basicAuth = "Basic " + userpass.encodeAsBase64()
                connection.setRequestProperty("Authorization", basicAuth)
                connection.setRequestProperty('Accept', 'application/csv')

                dataSet = connection.inputStream.text?.replace('"', '')
                script = "console on"

            } else {
                dataSet = params.dataSet ?: exampleDataSet
                script = params.script ?: exampleScript
            }

            def (String fileName, OutputStream os) = fileSystemService.createTemporaryFile('import-', 'csv')
            os << dataSet
            os.close()

            etlProcessor = scriptProcessorService.execute(project, script, fileSystemService.getTemporaryFullFilename(fileName))

            fileSystemService.deleteTemporaryFile(fileName)

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
                dataSet             : dataSet,
                script              : script?.trim(),
                lineNumbers         : Math.max(script.readLines().size(), 10),
                etlProcessor        : etlProcessor,
                errorCollector      : errorCollector,
                lineNumber          : lineNumber,
                availableMethods    : (etlProcessor?.availableMethods as JSON).toString(),
                assetFields         : (etlProcessor?.assetFields as JSON).toString(),
                missingPropertyError: missingPropertyError,
                logContent          : etlProcessor?.debugConsole?.content(),
                jsonResult          : (etlProcessor?.result?.domains as JSON),
                dataScriptId        : params.dataScriptId,
                providerName        : params.providerName,
                dataScriptName      : params.dataScriptName,
                filename            : params.filename

        ]
    }

    /**
     * Used to retrieve the ETL source code for a particular DataScript
     * @param id
     * @return
     */
    def dataScriptSource (Long id) {
        Project project = getProjectForWs()

        switch (request.method) {
            case 'GET':
                if (!id) {
                    sendInvalidInput('Please provide DataScript Id')
                    return
                }
                DataScript script = DataScript.findByProjectAndId(project, id)
                if (script) {
                    Map result = [id: script.id, script: script.etlSourceCode, provider: script.provider.name, name: script.name]
                    renderAsJson(result)
                } else {
                    sendNotFound()
                }
                break

            case 'POST':
                if (!id) {
                    sendInvalidInput('Please provide DataScript Id')
                    return
                }
                DataScript script = DataScript.findByProjectAndId(project, id)
                if (!script) {
                    sendNotFound()
                    return
                }

                if (!request.JSON.script) {
                    sendInvalidInput('Update was missing the script or script was blank')
                } else {
                    script.etlSourceCode = request.JSON.script
                    script.save(failOnError: true)
                    log.debug 'Saved script: {}', request.JSON.script
                    render text: 'success'

                }
                break

            case 'PUT':
                if (!request.JSON.script) {
                    sendInvalidInput('Can not create a blank script')
                    return
                }

                if (!request.JSON.providerName) {
                    sendInvalidInput('Must specify a Provider name')
                    return
                }

                if (!request.JSON.name) {
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
                        mode: DataScriptMode.IMPORT,
                        etlSourceCode: request.JSON.script,
                        provider: provider,
                        createdBy: currentPerson(),
                        target: 'not null',
                        description: 'some description'
                )
                //script.provider = provider

                script.save(flush: true, failOnError: true)
                Map results = [id: script.id]
                renderAsJson(results)

                break

            default:
                sendInvalidInput("Unsupported request method ${request.method}")
        }

    }

}
