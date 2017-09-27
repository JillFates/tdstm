import com.tdsops.etl.DataPart
import com.tdsops.etl.DomainAssets
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.StringTransformation
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

        def script = params.script ?: """domain Application
read labels
iterate {
    extract 'MODEL NAME' transform lowercase
    extract 2 transform uppercase
}
"""

        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DomainAssets.class.name
        customizer.addStaticStars DataPart.class.name

        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
//        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
        secureASTCustomizer.starImportsWhitelist = []

        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer

        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain   : etlProcessor.&domain,
                read     : etlProcessor.&read,
                iterate  : etlProcessor.&iterate,
                transform: etlProcessor.&transform,
                uppercase: new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase: new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        ErrorCollector errorCollector
        String missingPropertyError

        try {
            GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
            shell.evaluate(script)

        } catch (MultipleCompilationErrorsException cfe) {
            errorCollector = cfe.getErrorCollector()
        } catch (MissingPropertyException mpe){
            missingPropertyError = mpe.getMessage() + ". Property: ${mpe.getProperty()}"
        }

        [mockData: mockData, script: script, etlProcessor: etlProcessor, errorCollector: errorCollector, missingPropertyError: missingPropertyError]
    }


}
