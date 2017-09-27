import com.tdsops.etl.*
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

@Secured('isAuthenticated()')
class MockETLController implements ControllerMethods {


    def index() {

        def mockData = params.mockData ? """${params.mockData}""" : """DEVICE ID,MODEL NAME,MANUFACTURER NAME
152254,SRW24G4,LINKSYS
152255,ZPHA MODULE,TippingPoint
152256,Slideaway,ATEN
152258,CCM4850",Avocent
152259,DSR2035",Avocent
152266,2U Cable Management,Generic
152275,ProLiant BL465c G7,HP"""

        def data = []

        mockData.trim().eachLine { line, count ->
            data.addAll([line.split(',')])
        }

        def script = params.script ?: """console on
domain Application
read labels
iterate {
    extract 'MODEL NAME' transform lowercase
    extract 2 transform uppercase
}
"""

        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DomainAssets.class.name
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars ConsoleStatus.class.name

        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
//        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
        secureASTCustomizer.starImportsWhitelist = []

        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer

        DebugConsole console = new DebugConsole(buffer: new StringBuffer())
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data, debugConsole: console)

        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                console     : etlProcessor.&console,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        ErrorCollector errorCollector
        String missingPropertyError
        Integer lineNumber
        try {
            GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
            shell.evaluate(script, "ETLProcessor")

        } catch (MultipleCompilationErrorsException cfe) {
            errorCollector = cfe.getErrorCollector()
        } catch (MissingPropertyException mpe) {
            lineNumber = mpe.stackTrace.find {StackTraceElement ste -> ste.fileName == "ETLProcessor"}?.lineNumber
            missingPropertyError = mpe.getMessage()
        }

        [mockData: mockData, script: script.trim(), etlProcessor: etlProcessor, errorCollector: errorCollector, lineNumber: lineNumber, missingPropertyError: missingPropertyError, logContent: console.content()]
    }


}
