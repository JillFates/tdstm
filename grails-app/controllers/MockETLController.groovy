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

        def configureUsingDomain = { AssetClass assetClass ->
            customDomainService.allFieldSpecs(project, assetClass.name())[assetClass.name()]["fields"]
        }

        ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()

        validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, configureUsingDomain(AssetClass.APPLICATION))
        validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, configureUsingDomain(AssetClass.DEVICE))
        validator.addAssetClassFieldsSpecFor(AssetClass.DATABASE, configureUsingDomain(AssetClass.DATABASE))
        validator.addAssetClassFieldsSpecFor(AssetClass.STORAGE, configureUsingDomain(AssetClass.STORAGE))

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

        DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        ETLProcessor etlProcessor = new ETLProcessor(data, console, validator, [
                uppercase: new ElementTransformation(closure: { it.value = it.value.toUpperCase() }),
                lowercase: new ElementTransformation(closure: { it.value = it.value.toLowerCase() }),
                first    : new ElementTransformation(closure: { String value -> value.size() > 0 ? value[0] : "" }),
                blanks   : new ElementTransformation(closure: { String value -> value.replaceAll(" ", "") })
        ])

        ETLBinding binding = new ETLBinding(etlProcessor)

        ErrorCollector errorCollector
        String missingPropertyError
        Integer lineNumber
        try {
            new GroovyShell(this.class.classLoader, binding).evaluate(script?.trim(), ETLProcessor.class.name)

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
