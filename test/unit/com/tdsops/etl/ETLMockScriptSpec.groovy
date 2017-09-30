package com.tdsops.etl

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import spock.lang.Specification

class ETLMockScriptSpec extends Specification {


    void 'test can specify once the primary Domain for the ETL'() {

        given:
        String scriptText = """
            domain Application
        """

        and:
        Binding binding = new ETLBinding([
                *: DomainAssets.values().collectEntries { [(it.name()): it] }
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
//        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
//        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
//        secureASTCustomizer.starImportsWhitelist = []
//        secureASTCustomizer.staticStarImportsWhitelist = ['java.lang.Math'] // Only allow the java.lang.Math.* static import

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer
        configuration.scriptBaseClass = ETLMockScript.class.name


        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        binding.getVariable('metadata').domain == DomainAssets.Application
    }

    void 'test can specify several times the primary Domain for the ETL'() {

        given:
        String scriptText = """import net.transitionmanager.service.DeviceService
            domain Application
            domain Device
            domain Unknown
        """

        and:
        Binding binding = new ETLBinding([
                *: DomainAssets.values().collectEntries { [(it.name()): it] }
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
//        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
//        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
//        secureASTCustomizer.starImportsWhitelist = []
//        secureASTCustomizer.staticStarImportsWhitelist = ['java.lang.Math'] // Only allow the java.lang.Math.* static import

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer
        configuration.scriptBaseClass = ETLMockScript.class.name


        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        binding.getVariable('metadata').domain == DomainAssets.External
    }

    void 'test can read labels from Datasource and create a map of columns'() {

        // Reads the labels and creates a map of column names and ordinal positions. Upon reading the header
        // it increments to row pointer automatically.

        given:
        String scriptText = """
            domain Device
            read labels
        """

        and:
        List<List<String>> datasource = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA MODULE", "TippingPoint"]
        ]

        and:
        Binding binding = new Binding([
                datasource: datasource
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
//        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
//        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
//        secureASTCustomizer.starImportsWhitelist = []
//        secureASTCustomizer.staticStarImportsWhitelist = ['java.lang.Math'] // Only allow the java.lang.Math.* static import

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer
        configuration.scriptBaseClass = ETLMockScript.class.name


        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        binding.getVariable('datasource').columns == DomainAssets.Application

        binding.getVariable('datasource').columns["DEVICE ID"].ordinal == 0
        binding.getVariable('datasource').columns["MODEL NAME"] == 1
        binding.getVariable('datasource').columns["MANUFACTURER NAME"] == 2

    }

    void 'test can disallow closure creation using a secure syntax with AST customizer'() {

        given:
        String scriptText = """
            domain Device
            read labels
            def greeting = { String name -> "Hello, \$name!" }
            assert greeting('Diego') == 'Hello, Diego!'
        """

        and:
        Binding binding = new ETLBinding([
                uppercase: new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase: new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
//        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
//        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
//        secureASTCustomizer.starImportsWhitelist = []
//        secureASTCustomizer.staticStarImportsWhitelist = ['java.lang.Math'] // Only allow the java.lang.Math.* static import


        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer
        configuration.scriptBaseClass = ETLMockScript.class.name

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
        e.errorCollector.errors[0].cause*.message == ["Closures are not allowed"]
    }

    void 'test can disallow method creation using a secure syntax with AST customizer'() {

        given:
        String scriptText = """
            domain Device
            read labels
            def greeting(String name){ 
                "Hello, \$name!" 
            }
            assert greeting('Diego') == 'Hello, Diego!'
        """
        and:
        Binding binding = new ETLBinding([
                uppercase: new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase: new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
//        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
//        secureASTCustomizer.starImportsWhitelist = []
//        secureASTCustomizer.staticStarImportsWhitelist = ['java.lang.Math'] // Only allow the java.lang.Math.* static import


        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer
        configuration.scriptBaseClass = ETLMockScript.class.name

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
        e.errorCollector.errors*.cause*.message == ["Method definitions are not allowed"]
    }

    void 'test can disallow unnecessary imports using a secure syntax with AST customizer'() {

        given:
        String scriptText = """
            
            import java.lang.Math
            
            domain Device
            read labels
            Math.max 10, 100
        """
        and:
        Binding binding = new ETLBinding([
                uppercase: new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase: new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
//        secureASTCustomizer.starImportsWhitelist = []
//        secureASTCustomizer.staticStarImportsWhitelist = ['java.lang.Math'] // Only allow the java.lang.Math.* static import


        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer
        configuration.scriptBaseClass = ETLMockScript.class.name

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
        e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math] is not allowed"]
    }

    void 'test can disallow unnecessary stars imports using a secure syntax with AST customizer'() {

        given:
        String scriptText = """
            
            import java.lang.Math.*
            
            domain Device
            read labels
            max 10, 100
        """
        and:
        Binding binding = new ETLBinding([
                uppercase: new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase: new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
        secureASTCustomizer.starImportsWhitelist = []
//        secureASTCustomizer.staticStarImportsWhitelist = ['java.lang.Math'] // Only allow the java.lang.Math.* static import

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer
        configuration.scriptBaseClass = ETLMockScript.class.name


        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
        e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math.*] is not allowed"]
    }
}
