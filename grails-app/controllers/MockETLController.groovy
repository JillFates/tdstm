import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.StringAppendElement
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.Project
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

}
