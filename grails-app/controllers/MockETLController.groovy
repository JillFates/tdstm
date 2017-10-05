import com.tdsops.etl.*
import com.tdsops.tm.enums.domain.AssetClass
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException

@Secured('isAuthenticated()')
class MockETLController implements ControllerMethods {


    def customDomainService

    def index () {

        Project project = securityService.userCurrentProject

        Map<ETLDomain, List<Map<String, ?>>> domainFieldsSpec = [:]
        domainFieldsSpec[ETLDomain.Application] = customDomainService.allFieldSpecs(project,
                AssetClass.APPLICATION.name())[AssetClass.APPLICATION.name()]["fields"]
        domainFieldsSpec[ETLDomain.Device] = customDomainService.allFieldSpecs(project,
                AssetClass.DEVICE.name())[AssetClass.DEVICE.name()]["fields"]
        domainFieldsSpec[ETLDomain.Database] = customDomainService.allFieldSpecs(project,
                AssetClass.DATABASE.name())[AssetClass.DATABASE.name()]["fields"]
        domainFieldsSpec[ETLDomain.Storage] = customDomainService.allFieldSpecs(project,
                AssetClass.STORAGE.name())[AssetClass.STORAGE.name()]["fields"]


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
domain Application
read labels
iterate {
    extract 0 load id
    extract 'MODEL NAME' transform lowercase load Name
    extract 2 transform uppercase  load description
    load environment with 'Production'
}
"""

        DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        ETLProcessor etlProcessor = new ETLProcessor(data, console, domainFieldsSpec)

        ETLBinding binding = new ETLBinding(etlProcessor, [
                uppercase: new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase: new StringTransformation(closure: { String value -> value.toLowerCase() }),
                first    : new StringTransformation(closure: { String value -> value.size() > 0 ? value[0] : "" }),
                blanks   : new StringTransformation(closure: { String value -> value.replaceAll(" ", "") })
        ])

        ErrorCollector errorCollector
        String missingPropertyError
        Integer lineNumber
        try {
            new GroovyShell(this.class.classLoader, binding, binding.configuration).evaluate(script, ETLProcessor.class.name)

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
                logContent          : console.content(),
                jsonResult          : (etlProcessor?.results as JSON)?.toString(true)
        ]
    }


}
