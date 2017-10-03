import com.tdsops.etl.*
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer

@Secured('isAuthenticated()')
class MockETLController implements ControllerMethods {


    def customDomainService

    def index() {

        Project project = securityService.userCurrentProject

        ETLFieldsMapper domainAssetFieldsMapper = new ETLFieldsMapper()

//        List<Map<String, ?>> applicationFieldSpecs = customDomainService.allFieldSpecs(project,
//                AssetClass.APPLICATION.name())[AssetClass.APPLICATION.name()]["fields"]
//        List<Map<String, ?>> deviceFieldSpecs = customDomainService.allFieldSpecs(project,
//                AssetClass.DEVICE.name())[AssetClass.DEVICE.name()]["fields"]
//        List<Map<String, ?>> databaseFieldSpecs = customDomainService.allFieldSpecs(project,
//                AssetClass.DATABASE.name())[AssetClass.DATABASE.name()]["fields"]
//        List<Map<String, ?>> storageFieldSpecs = customDomainService.allFieldSpecs(project,
//                AssetClass.STORAGE.name())[AssetClass.STORAGE.name()]["fields"]
//
//        domainAssetFieldsMapper.setFieldsSpecFor(AssetClass.APPLICATION, applicationFieldSpecs)
//        domainAssetFieldsMapper.setFieldsSpecFor(AssetClass.DEVICE, deviceFieldSpecs)
//        domainAssetFieldsMapper.setFieldsSpecFor(AssetClass.DATABASE, databaseFieldSpecs)
//        domainAssetFieldsMapper.setFieldsSpecFor(AssetClass.STORAGE, storageFieldSpecs)


        def mockData = params.mockData ? """${params.mockData}""" : """DEVICE ID,MODEL NAME,MANUFACTURER NAME,ENVIRONMENT
152254,SRW24G4,LINKSYS,Prod
152255,ZPHA MODULE,TippingPoint,Dev
152256,Slideaway,ATEN,Prod
152258,CCM4850",Avocent,Prod
152259,DSR2035",Avocent,Dev
152266,2U Cable Management,Generic,Prod
152275,ProLiant BL465c G7,HP,Dev"""

        def data = []

        mockData.trim().eachLine { line, count ->
            data.addAll([line.split(',')])
        }

        def script = params.script ?: """console on
domain Application
read labels
iterate {
    extract 'MODEL NAME' transform lowercase load 'modelName'
    extract 2 transform uppercase  load 'appName'
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
                translate   : etlProcessor.&translate,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() }),
                first       : new StringTransformation(closure: { String value -> value.size() > 0 ? value[0] : "" }),
                blanks      : new StringTransformation(closure: { String value -> value.replaceAll(" ", "") })
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
            lineNumber = mpe.stackTrace.find { StackTraceElement ste -> ste.fileName == "ETLProcessor" }?.lineNumber
            missingPropertyError = mpe.getMessage()
        }

        [mockData: mockData, script: script.trim(), etlProcessor: etlProcessor, errorCollector: errorCollector, lineNumber: lineNumber, missingPropertyError: missingPropertyError, logContent: console.content(), jsonResult: (etlProcessor?.results as JSON)?.toString(true)]
    }


}
