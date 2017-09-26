package com.tdsops.etl

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import spock.lang.Specification

class ETLProcessorSpec extends Specification {

    void 'test can specify which is the primary Domain for the ETL'() {

        given:
        String scriptText = """
            domain Application
        """

        and:
        ETLProcessor etlProcessor = new ETLProcessor()

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                *           : DomainAssets.values().collectEntries { [(it.name()): it] },
                domain      : etlProcessor.&domain
        ])

        when:
        GroovyShell shell = new GroovyShell(binding)
        shell.evaluate(scriptText)

        then:
        etlProcessor.selectedDomain == DomainAssets.Application
    }

    void 'test can specify which is the primary Domain for the ETL using Import Customizer'() {

        given:
        String scriptText = """
            domain Application
        """

        and:
        ETLProcessor etlProcessor = new ETLProcessor()

        and:
        Binding binding = new Binding([
                etlProcessor: etlProcessor,
                domain      : etlProcessor.&domain
        ])

        and:
        ImportCustomizer customizer = new ImportCustomizer()
        customizer.addStaticStars com.tdsops.etl.DomainAssets.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        etlProcessor.selectedDomain == DomainAssets.Application
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
        shell.evaluate(scriptText)

        then:
        !!etlProcessor.labelMap
        0 == etlProcessor.labelMap["DEVICE ID"]
        1 == etlProcessor.labelMap["MODEL NAME"]
        2 == etlProcessor.labelMap["MANUFACTURER NAME"]

        and:
        1 == etlProcessor.currentRowPosition
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
        shell.evaluate(scriptText)

        then:
        data.size() == etlProcessor.currentRowPosition
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
        customizer.addStaticStars DomainAssets.class.name
        customizer.addStaticStars DataPart.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        data.size() == etlProcessor.currentRowPosition

        and:
        "Slideaway" == etlProcessor.currentFieldValue
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
        customizer.addStaticStars DomainAssets.class.name
        customizer.addStaticStars DataPart.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        data.size() == etlProcessor.currentRowPosition

        and:
        "Slideaway" == etlProcessor.currentFieldValue
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
        customizer.addStaticStars DomainAssets.class.name
        customizer.addStaticStars DataPart.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        data.size() == etlProcessor.currentRowPosition

        and:
        "SRW24G4" == etlProcessor.crudData[1][1]
        "ZPHA MODULE" == etlProcessor.crudData[2][1]
        "SLIDEAWAY" == etlProcessor.crudData[3][1]
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
        customizer.addStaticStars DomainAssets.class.name
        customizer.addStaticStars DataPart.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        data.size() == etlProcessor.currentRowPosition

        and:
        "srw24g4" == etlProcessor.crudData[1][1]
        "zpha module" == etlProcessor.crudData[2][1]
        "slideaway" == etlProcessor.crudData[3][1]
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
        customizer.addStaticStars DomainAssets.class.name
        customizer.addStaticStars DataPart.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.parse(scriptText)

        then:
        thrown MultipleCompilationErrorsException
    }

    void 'test can check syntax errors at evaluate time'() {

        given:
        String scriptText = """
            domain Device
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
        customizer.addStaticStars DomainAssets.class.name
        customizer.addStaticStars DataPart.class.name

        and:
        CompilerConfiguration configuration = new CompilerConfiguration()
        configuration.addCompilationCustomizers customizer

        when:
        GroovyShell shell = new GroovyShell(this.class.classLoader, binding, configuration)
        shell.evaluate(scriptText)

        then:
        thrown MissingPropertyException
    }
}
