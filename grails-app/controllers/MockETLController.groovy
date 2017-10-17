import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
import getl.tfs.TFS
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project
import net.transitionmanager.service.dataingestion.ScriptProcessorService
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException

@Secured('isAuthenticated()')
class MockETLController implements ControllerMethods {

    ScriptProcessorService scriptProcessorService

    String exampleData = """DEVICE ID,MODEL NAME,MANUFACTURER NAME,ENVIRONMENT
152254,SRW24G4,LINKSYS,Prod
152255,ZPHA MODULE,TippingPoint,Dev
152256,Slideaway,ATEN,Prod
152258,CCM4850",Avocent,Prod
152259,DSR2035",Avocent,Dev
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
                params.mockData = connection.inputStream.text

                params.script = "console on"
//                file << connection.inputStream
//                file.close()

            } else {

            }

            mockData = params.mockData ? """${params.mockData}""" : exampleData

            List<List<String>> data = []

            mockData.trim().eachLine { line, count ->
                data.addAll([line.split(',') as List])
            }

            script = params.script ?: exampleScript


            etlProcessor = scriptProcessorService.execute(project, script, data)

        } catch (MultipleCompilationErrorsException cfe) {
            errorCollector = cfe.getErrorCollector()
        } catch (MissingPropertyException mpe) {
            lineNumber = mpe.stackTrace.find { StackTraceElement ste -> ste.fileName == ETLProcessor.class.name }?.lineNumber
            missingPropertyError = mpe.getMessage()
        } catch (ETLProcessorException pe) {
            missingPropertyError = pe.getMessage()
        } catch (IOException ioe) {
            missingPropertyError = ioe.getMessage()
        }

        [
                mockData            : mockData,
                script              : script?.trim(),
                etlProcessor        : etlProcessor,
                errorCollector      : errorCollector,
                lineNumber          : lineNumber,
                missingPropertyError: missingPropertyError,
                logContent          : etlProcessor?.debugConsole?.content(),
                jsonResult          : (etlProcessor?.results as JSON)?.toString(true)
        ]
    }


}
