import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
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

    def index () {

        Project project = securityService.userCurrentProject

        def mockData = params.mockData ? """${params.mockData}""" : """DEVICE ID,MODEL NAME,MANUFACTURER NAME,ENVIRONMENT
152254,SRW24G4,LINKSYS,Prod
152255,ZPHA MODULE,TippingPoint,Dev
152256,Slideaway,ATEN,Prod
152258,CCM4850",Avocent,Prod
152259,DSR2035",Avocent,Dev
152266,2U Cable Management,Generic,Prod
152275,ProLiant BL465c G7,HP,Dev"""

        List<List<String>> data = []

        mockData.trim().eachLine { line, count ->
            data.addAll([line.split(',') as List])
        }

        def script = params.script ?: """console on

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

        ErrorCollector errorCollector
        String missingPropertyError
        Integer lineNumber
        ETLProcessor etlProcessor
        try {

            etlProcessor = scriptProcessorService.execute(project, script, data)

        } catch (MultipleCompilationErrorsException cfe) {
            errorCollector = cfe.getErrorCollector()
        } catch (MissingPropertyException mpe) {
            lineNumber = mpe.stackTrace.find { StackTraceElement ste -> ste.fileName == ETLProcessor.class.name }?.lineNumber
            missingPropertyError = mpe.getMessage()
        } catch (ETLProcessorException pe) {
            missingPropertyError = pe.getMessage()
        }

        [
                mockData            : mockData,
                script              : script.trim(),
                etlProcessor        : etlProcessor,
                errorCollector      : errorCollector,
                lineNumber          : lineNumber,
                missingPropertyError: missingPropertyError,
                logContent          : etlProcessor?.debugConsole.content(),
                jsonResult          : (etlProcessor?.results as JSON)?.toString(true)
        ]
    }


}
