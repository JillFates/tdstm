package com.tdsops.etl

import com.tdsops.tm.enums.domain.AssetClass
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import spock.lang.Specification

class ETLProcessorSpec extends Specification {

    void 'test can specify once the primary Domain for the ETL'() {

        given:
        String scriptText = """
            domain Application
        """

        and:
        ETLProcessor etlProcessor = new ETLProcessor()

        and:
        ETLBinding binding = new ETLBinding([etlProcessor: etlProcessor])

        when:
        new GroovyShell(this.class.classLoader, binding, binding.configuration)
                .evaluate(scriptText, ETLProcessor.class.name)

        then:
        etlProcessor.selectedDomain == DomainAssets.Application
    }

    void 'test can specify several times the primary Domain for the ETL using Import Customizer'() {

        given:
        String scriptText = """
            domain Application
            domain Device
            domain Storage
        """

        and:
        ETLProcessor etlProcessor = new ETLProcessor()

        and:
        ETLBinding binding = new ETLBinding([etlProcessor: etlProcessor])

        when:
        new GroovyShell(this.class.classLoader, binding, binding.configuration)
                .evaluate(scriptText, ETLProcessor.class.name)

        then:
        etlProcessor.selectedDomain == DomainAssets.Storage
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
        ETLProcessor etlProcessor = new ETLProcessor(crudData: [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA MODULE", "TippingPoint"]
        ])

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                *           : DomainAssets.values().collectEntries { [(it.name()): it] },
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                *           : DataPart.values().collectEntries { [(it.name()): it] },
        ])

        when:
        GroovyShell shell = new GroovyShell(binding)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        etlProcessor.column("DEVICE ID").ordinal == 0
        etlProcessor.column("MODEL NAME").ordinal == 1
        etlProcessor.column("MANUFACTURER NAME").ordinal == 2

        and:
        etlProcessor.currentRowPosition == 1
    }

    /**
     * The iterate command will create a loop that iterate over the remaining rows in the data source
     */
    void 'test can iterate over all rows'() {

        // Reads the labels and creates a map of column names and ordinal positions. Upon reading the header
        // it increments to row pointer automatically.

        given:
        String scriptText = """
            domain Device
            read labels
            iterate {
                println it
            }
        """
        and:
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA MODULE", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"],
                ["152258", "CCM4850", "Avocent"],
                ["152259", "DSR2035", "Avocent"],
                ["152266", "2U Cable Management", "Generic"],
                ["152275", "ProLiant BL465c G7", "HP"]
        ]
        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                *           : DomainAssets.values().collectEntries { [(it.name()): it] },
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                *           : DataPart.values().collectEntries { [(it.name()): it] },
                iterate     : etlProcessor.&iterate
        ])

        when:
        GroovyShell shell = new GroovyShell(binding)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        etlProcessor.currentRowPosition == data.size()
    }

    /**
     * 	The 'extract' command takes a parameter that can be the ordinal position or the label identified in the 'read labels'.
     * 	The extract puts the value into a local register that can then be manipulated and eventually
     * 	saved into the target domain object.
     */
    void 'test can extract a field value over all rows based on column ordinal position'() {

        given:
        String scriptText = """
            domain Device
            read labels
            iterate {
                extract 1
            }
        """
        and:
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA MODULE", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,

        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        data.size() == etlProcessor.currentRowPosition

        and:
        etlProcessor.currentFieldValue == "Slideaway"
    }

    /**
     * 	The 'extract' command takes a parameter that can be the ordinal position or the label identified in the 'read labels'.
     * 	The extract puts the value into a local register that can then be manipulated and eventually
     * 	saved into the target domain object.
     */
    void 'test can extract a field value over all rows based on column name'() {

        given:
        String scriptText = """
            domain Device
            read labels
            iterate {
                extract 'MODEL NAME'
            }
        """
        and:
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA MODULE", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,

        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        data.size() == etlProcessor.currentRowPosition

        and:
        etlProcessor.currentFieldValue == "Slideaway"
    }

    void 'test can transform a field value to uppercase'() {

        given:
        String scriptText = """
            domain Device
            read labels
            iterate {
                extract 'MODEL NAME' transform uppercase
            }
        """
        and:
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: {
                    it.toUpperCase()
                })

        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        data.size() == etlProcessor.currentRowPosition

        and:
        etlProcessor.crudData[1][1] == "SRW24G4"
        etlProcessor.crudData[2][1] == "ZPHA MODULE"
        etlProcessor.crudData[3][1] == "SLIDEAWAY"
    }

    void 'test can transform a field value to lowercase'() {

        given:
        String scriptText = """
            domain Device
            read labels
            iterate {
                extract 'MODEL NAME' transform lowercase
            }
        """
        and:
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        data.size() == etlProcessor.currentRowPosition

        and:
        etlProcessor.crudData[1][1] == "srw24g4"
        etlProcessor.crudData[2][1] == "zpha module"
        etlProcessor.crudData[3][1] == "slideaway"
    }

    void 'test can check syntax errors at parsing time'() {

        given:
        String scriptText = """
            domain Device
            read labels
            iterate 
                extract 'MODEL NAME' transform unknown
            }
        """
        and:
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.parse(scriptText)

        then:
        thrown MultipleCompilationErrorsException
    }

    void 'test can check syntax errors at evaluation time'() {

        given:
        String scriptText = """domain Device
            read labels
            iterate {
                extract 'MODEL NAME' transform unknown
            }
        """
        and:
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        MissingPropertyException missingPropertyException = thrown MissingPropertyException
        missingPropertyException.stackTrace.find { StackTraceElement ste -> ste.fileName == ETLProcessor.class.name }?.lineNumber == 4
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
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
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
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
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
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
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
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
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


        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
        e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math.*] is not allowed"]
    }

    void 'test can allow stars imports using a secure syntax with AST customizer'() {

        given:
        String scriptText = """            
            domain Device
            read labels
            max 10, 100
        """
        and:
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() }),
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars Math.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
        secureASTCustomizer.starImportsWhitelist = []

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer


        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        notThrown MultipleCompilationErrorsException
    }

    void 'test can enable console and log domain selected'() {

        given:
        String scriptText = """            
            console on
            domain Device
        """

        and:
        List<List<String>> data = [
                ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                ["152254", "SRW24G4", "LINKSYS"],
                ["152255", "ZPHA Module", "TippingPoint"],
                ["152256", "Slideaway", "ATEN"]
        ]

        and:
        StringBuffer buffer = new StringBuffer()
        DebugConsole console = new DebugConsole(buffer: buffer)

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data, debugConsole: console)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                console     : etlProcessor.&console,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars Math.class.name
        customizer.addStaticStars ConsoleStatus.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.closuresAllowed = false             // disallow closure creation
        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
        secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
        secureASTCustomizer.starImportsWhitelist = []

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer


        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        buffer.toString() == new StringBuffer("INFO - Console status changed: on")
                .append(System.lineSeparator())
                .append("INFO - Selected Domain: Device")
                .append(System.lineSeparator())
                .toString()
    }

    void 'test can extract a field value and load into a domain object property name'() {

        // The 'load into' command will take whatever value is in the internal register and map it to the domain object
        // property name.  The command takes a String argument that will map to the property name of the domain. This
        // should use the AssetFieldSettings Specifications for the domain to validate the property names. It should error
        // with an explaination that the property does not exist and reference the line of the error if possible.

        given:
        String scriptText = """
            domain Application
            read labels
            iterate {
                extract 'VENDOR NAME' load 'appVendor'
            }
        """

        and:
        List<List<String>> data = [
                ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY"],
                ["152254", "Microsoft", "(xlsx updated)"],
                ["152255", "Mozilla", "NGM"],
                ["152256", "VMWare", ""]
        ]

        and:
        ETLFieldsMapper domainAssetFieldsMapper = new ETLFieldsMapper()
        domainAssetFieldsMapper.setFieldsSpecFor(AssetClass.APPLICATION, [
                [constraints: [required: 0],
                 "control"  : "Number",
                 "default"  : "",
                 "field"    : "id",
                 "imp"      : "U",
                 "label"    : "Id",
                 "order"    : 0,
                 "shared"   : 0,
                 "show"     : 0,
                 "tip"      : "",
                 "udf"      : 0
                ],
                [constraints: [required: 0],
                 "control"  : "String",
                 "default"  : "",
                 "field"    : "appVendor",
                 "imp"      : "N",
                 "label"    : "Vendor",
                 "order"    : 0,
                 "shared"   : 0,
                 "show"     : 0,
                 "tip"      : "",
                 "udf"      : 0
                ]
        ])

        and:
        StringBuffer buffer = new StringBuffer()
        DebugConsole console = new DebugConsole(buffer: buffer)

        and:
        ETLProcessor etlProcessor = new ETLProcessor(crudData: data,
                debugConsole: console,
                domainAssetFieldsMapper: domainAssetFieldsMapper)

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain,
                read        : etlProcessor.&read,
                iterate     : etlProcessor.&iterate,
                transform   : etlProcessor.&transform,
                console     : etlProcessor.&console,
                load        : etlProcessor.&load,
                uppercase   : new StringTransformation(closure: { String value -> value.toUpperCase() }),
                lowercase   : new StringTransformation(closure: { String value -> value.toLowerCase() })
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars DataPart.class.name
        customizer.addStaticStars Math.class.name
        customizer.addStaticStars ConsoleStatus.class.name
        customizer.addStaticStars DomainAssets.class.name

        and:
        SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
        secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
        secureASTCustomizer.importsWhitelist = []               // Empty withe list means forbid imports
        secureASTCustomizer.starImportsWhitelist = []

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer, secureASTCustomizer


        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText, ETLProcessor.class.name)

        then:
        etlProcessor.currentFieldValue == "VMWare"


    }
}
