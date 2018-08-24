import com.tdsops.etl.ETLProcessor
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

@Slf4j(value = 'log', category = 'grails.app.controllers.MockETLController')
@Secured('isAuthenticated()')
class MockETLController implements ControllerMethods {

    ScriptProcessorService scriptProcessorService
    FileSystemService fileSystemService

    String exampleDataSet = """\
            device id,model name,manufacturer name,environment
            122097,SRW24G4,LINKSYS,Prod
            122105,ZPHA MODULE,TippingPoint,Dev
            122151,Slideaway,ATEN,Prod
            122072,CCM4850,Avocent,Prod
            122076,DSR2035,Avocent,Dev
            122117,2U Cable Management,Generic,Prod
            205820,ProLiant BL465c G7,HP,Dev\
    """.stripIndent().trim()


	String exampleScript = """\
        console on
        read labels
        
        iterate {
            domain Application
        
            extract 1 load 'id' set applicationIdVar
            extract 'model name' transform with lowercase() load 'Name'
            extract 3 transform with uppercase() load 'description'
        
            set environmentVar with 'Production'
            find Application by 'id' eq applicationIdVar into 'id'
            whenNotFound 'id' create {
                assetClass Device
                environment 'Name' 
                description DOMAIN.'description' 
            }
            
            whenFound 'id' update {
                environment 'Name' 
            }
            
            domain Device
            extract 1 load 'id' set deviceIdVar
            extract 'model name' transform with uppercase() load 'Name'
            find Device by 'id' eq deviceIdVar into 'id'
        }\
    """.stripIndent().trim()


	def index(){

		Project project = securityService.userCurrentProject
		ETLProcessor etlProcessor

		String dataSet
		String script
		Map<String, ?> error

		try {

			String.mixin StringAppendElement

			if(params.fetch && params.file){

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

			String fileType = 'csv'
			if(params.filetype){
				fileType = params.filetype
			}

			log.debug("Filetype to use: {}", fileType)

			def (String fileName, OutputStream os) = fileSystemService.createTemporaryFile('import-', fileType)
			fileSystemService.initFile(fileName, os, dataSet)

			etlProcessor = scriptProcessorService.execute(project, script, fileSystemService.getTemporaryFullFilename(fileName))
			fileSystemService.deleteTemporaryFile(fileName)

		} catch( Throwable t){
			error = ETLProcessor.getErrorMessage(t)
		}

		[
			dataSet         : dataSet,
			script          : script?.trim(),
			lineNumbers     : Math.max(script.readLines().size(), 10),
			etlProcessor    : etlProcessor,
			error           : error,
			availableMethods: (etlProcessor?.availableMethods as JSON).toString(),
			assetFields     : (etlProcessor?.assetFields as JSON).toString(),
			logContent      : etlProcessor?.debugConsole?.content(),
			jsonResult      : (etlProcessor?.finalResult()?.domains as JSON),
			dataScriptId    : params.dataScriptId,
			providerName    : params.providerName,
			dataScriptName  : params.dataScriptName,
			filename        : params.filename
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
