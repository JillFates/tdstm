package com.tdsops.etl

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.domain.Project
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import spock.lang.Specification

class ETLProcessorSpec extends Specification {

    void 'test can define a the primary domain' () {

        given:
            ETLProcessor etlProcessor = new ETLProcessor()

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        domain Application
                        
                     """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'A domain is selected'
            etlProcessor.selectedDomain == ETLDomain.Application
    }

    void 'test can add groovy comments' () {

        given:
            ETLProcessor etlProcessor = new ETLProcessor()

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        // Script supports one line comments
                        domain Application
                        /*
                            And multiple Lines comments
                        */
                        
                     """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'A domain is selected'
            etlProcessor.selectedDomain == ETLDomain.Application
    }

    void 'test can throw an exception if an invalid domain is defined' () {

        given:
            ETLProcessor etlProcessor = new ETLProcessor()

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""

                        domain Unknown
                        
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'An ETLProcessorException is thrown'
            ETLProcessorException e = thrown ETLProcessorException
            e.message == "Invalid domain: 'Unknown'. It should be one of these values: ${ETLDomain.values()}"
    }

    void 'test can define a domain more than once within the script' () {

        given:
            ETLProcessor etlProcessor = new ETLProcessor()

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""

                        domain Application
                        domain Device
                        domain Storage
                        
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'The last domain selected could be recovered'
            etlProcessor.selectedDomain == ETLDomain.Storage
    }

    void 'test can skip a fixed number of rows' () {

        given:
            ETLProcessor etlProcessor = new ETLProcessor([
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152251", "SRW24G1", "LINKSYS"],
                    ["152252", "SRW24G2", "LINKSYS"],
                    ["152253", "SRW24G3", "LINKSYS"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "SRW24G5", "LINKSYS"],
                    ["152256", "ZPHA MODULE", "TippingPoint"]
            ])

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                    
                        skip 2
                        
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'The current row index is increased by 2'
            etlProcessor.currentRowIndex == 2
    }

    void 'test can throw an Exception if the skip parameter is bigger that rows count' () {

        given:
            ETLProcessor etlProcessor = new ETLProcessor([
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152251", "SRW24G1", "LINKSYS"],
                    ["152252", "SRW24G2", "LINKSYS"],
                    ["152253", "SRW24G3", "LINKSYS"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "SRW24G5", "LINKSYS"],
                    ["152256", "ZPHA MODULE", "TippingPoint"]
            ])

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("skip 20", ETLProcessor.class.name)

        then: 'An ETLProcessorException is thrown'
            ETLProcessorException e = thrown ETLProcessorException
            e.message == "Incorrect skip step: 20"
    }

    void 'test can throw an Exception if the scrip command is not recognized' () {

        given:
            ETLProcessor etlProcessor = new ETLProcessor([
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152251", "SRW24G1", "LINKSYS"],
                    ["152252", "SRW24G2", "LINKSYS"],
                    ["152253", "SRW24G3", "LINKSYS"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "SRW24G5", "LINKSYS"],
                    ["152256", "ZPHA MODULE", "TippingPoint"]
            ])

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("invalid command", ETLProcessor.class.name)

        then: 'An MissingMethodException exception is thrown'
            MissingMethodException missingMethodException = thrown MissingMethodException
            missingMethodException.stackTrace.find { StackTraceElement ste -> ste.fileName == ETLProcessor.class.name }?.lineNumber == 1
    }

    void 'test can read labels from dataSource and create a map of columns' () {

        // Reads the labels and creates a map of column names and ordinal positions. Upon reading the header
        // it increments to row pointer automatically.

        given:
            String scriptText = """
            domain Device
            read labels
        """
        and:
            ETLProcessor etlProcessor = new ETLProcessor([
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA MODULE", "TippingPoint"]
            ])

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate(scriptText, ETLProcessor.class.name)

        then: 'A column map is created'
            etlProcessor.column("DEVICE ID").index == 0
            etlProcessor.column(0).label == "DEVICE ID"

        and:
            etlProcessor.column("MODEL NAME").index == 1
            etlProcessor.column(1).label == "MODEL NAME"

        and:
            etlProcessor.column("MANUFACTURER NAME").index == 2
            etlProcessor.column(2).label == "MANUFACTURER NAME"

        and:
            etlProcessor.currentRowIndex == 1
    }
    /**
     * The iterate command will create a loop that iterate over the remaining rows in the data source
     */
    void 'test can iterate over all data source rows' () {

        // Reads the labels and creates a map of column names and ordinal positions. Upon reading the header
        // it increments to row pointer automatically.

        given:
            List<List<String>> dataSource = [
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
            ETLProcessor etlProcessor = new ETLProcessor(dataSource)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            println it
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'The current row index is the last row in data source'
            etlProcessor.currentRowIndex == dataSource.size() - 1
    }
    /**
     * 	The 'extract' command takes a parameter that can be the ordinal position or the label identified in the 'read labels'.
     * 	The extract puts the value into a local register that can then be manipulated and eventually
     * 	saved into the target domain object.
     */
    void 'test can extract a field value over all rows based on column ordinal position' () {

        given:
            List<List<String>> dataSource = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA MODULE", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(dataSource)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""

                        domain Device
                        read labels
                        iterate {
                            extract 1
                        }
                        
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'The last column index is selected correctly'
            etlProcessor.currentColumnIndex == 1

        and: 'The last column and row is selected'
            etlProcessor.currentRow.getElement(1).value == "Slideaway"
            etlProcessor.currentRow.getElement(1).originalValue == "Slideaway"
    }
    /**
     * 	The 'extract' command takes a parameter that can be the ordinal position or the label identified in the 'read labels'.
     * 	The extract puts the value into a local register that can then be manipulated and eventually
     * 	saved into the target domain object.
     */
    void 'test can extract a field value over all rows based on column name' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA MODULE", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                    
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME'
                        }
                        
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'The last column index is selected correctly'
            etlProcessor.currentColumnIndex == 1

        and: 'The last column and row is selected'
            etlProcessor.currentRow.getElement(1).value == "Slideaway"
            etlProcessor.currentRow.getElement(1).originalValue == "Slideaway"
    }

    void 'test can throw an Exception if a column name is invalid' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA MODULE", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                    
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAM'
                        }
                        
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'An ETLProcessorException is thrown'
            ETLProcessorException e = thrown ETLProcessorException
            e.message == "Extracting a missing column name 'MODEL NAM'"

    }

    void 'test can throw an Exception if a column index is not between row elements range' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA MODULE", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                    
                        domain Device
                        read labels
                        iterate {
                            extract 10000
                        }
                        
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'An ETLProcessorException is thrown'
            ETLProcessorException e = thrown ETLProcessorException
            e.message == "Invalid column index: 10000"
    }

    void 'test can transform a field value with uppercase transformation' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform {
                                    uppercase
                            } 
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row is transformed to uppercase'
            etlProcessor.getRow(0).getElement(1).value == "SRW24G4"
            etlProcessor.getRow(1).getElement(1).value == "ZPHA MODULE"
            etlProcessor.getRow(2).getElement(1).value == "SLIDEAWAY"
    }

    void 'test can transform a field value to lowercase transformation' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform { lowercase }
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row is transformed to uppercase'
            etlProcessor.getRow(0).getElement(1).value == "srw24g4"
            etlProcessor.getRow(1).getElement(1).value == "zpha module"
            etlProcessor.getRow(2).getElement(1).value == "slideaway"
    }

    void 'test can transform a field value with taking left 4 characters' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(etlProcessor.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform { 
                                        left 4
                                  }
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row is transformed to left 4 transformation'
            etlProcessor.getRow(0).getElement(1).value == "SRW2"
            etlProcessor.getRow(1).getElement(1).value == "ZPHA"
            etlProcessor.getRow(2).getElement(1).value == "Slid"
    }

    void 'test can transform a field value with taking middle 2 characters' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(etlProcessor.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform { 
                                    middle 2, 3 
                                    lowercase  
                                }
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row is transformed with middle 2 transformation'
            etlProcessor.getRow(0).getElement(1).value == "w2"
            etlProcessor.getRow(1).getElement(1).value == "ha"
            etlProcessor.getRow(2).getElement(1).value == "id"
    }

    void 'test can transform a field value striping first A characters' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRA24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(etlProcessor.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform { 
                                    uppercase 
                                    first 'A'
                                }
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row striping first "A" character'
            etlProcessor.getRow(0).getElement(1).value == "SR24G4"
            etlProcessor.getRow(1).getElement(1).value == "ZPH MODULE"
            etlProcessor.getRow(2).getElement(1).value == "SLIDEWAY"
    }

    void 'test can transform a field value striping last A characters' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRA24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(etlProcessor.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform { 
                                        uppercase 
                                        last 'A'
                                     }
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row striping last "A" character'
            etlProcessor.getRow(0).getElement(1).value == "SR24G4"
            etlProcessor.getRow(1).getElement(1).value == "ZPH MODULE"
            etlProcessor.getRow(2).getElement(1).value == "SLIDEAWY"
    }

    void 'test can transform a field value striping all A characters' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRA24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(etlProcessor.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform { 
                                    uppercase 
                                    last 'A'
                            }  
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row striping all "A" characters'
            etlProcessor.getRow(0).getElement(1).value == "SR24G4"
            etlProcessor.getRow(1).getElement(1).value == "ZPH MODULE"
            etlProcessor.getRow(2).getElement(1).value == "SLIDEAWY"
    }

    void 'test can apply another transformation for a field value after striping all A characters' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRA24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(etlProcessor.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform { 
                                        uppercase
                                        last 'A' 
                                        lowercase
                                    }
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row striping all "A" characters'
            etlProcessor.getRow(0).getElement(1).value == "sr24g4"
            etlProcessor.getRow(1).getElement(1).value == "zph module"
            etlProcessor.getRow(2).getElement(1).value == "slideawy"
    }

    void 'test can transform a field value with taking right 4 characters' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(etlProcessor.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform {
                                right 4
                            } 
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row is transformed with right 4 transformation'
            etlProcessor.getRow(0).getElement(1).value == "24G4"
            etlProcessor.getRow(1).getElement(1).value == "dule"
            etlProcessor.getRow(2).getElement(1).value == "away"
    }

    void 'test can transform a use left 4 transformation in a chain of transformations' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(etlProcessor.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform { 
                                    left 4 
                                    lowercase
                            } 
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row is transformed to uppercase'
            etlProcessor.getRow(0).getElement(1).value == "srw2"
            etlProcessor.getRow(1).getElement(1).value == "zpha"
            etlProcessor.getRow(2).getElement(1).value == "slid"
    }

    void 'test can apply transformations on a field value many times' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate {
                            extract 'MODEL NAME' transform  { 
                                uppercase 
                                lowercase
                            }
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every column for every row is transformed to uppercase'
            etlProcessor.getRow(0).getElement(1).value == "srw24g4"
            etlProcessor.getRow(1).getElement(1).value == "zpha module"
            etlProcessor.getRow(2).getElement(1).value == "slideaway"
    }

    void 'test can check syntax errors at parsing time' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        domain Device
                        read labels
                        iterate 
                            extract 'MODEL NAME' transform { 
                                    unknown
                            }
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'An MultipleCompilationErrorsException exception is thrown'
            thrown MultipleCompilationErrorsException
    }

    void 'test can check syntax errors at evaluation time' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""domain Device
                        read labels
                        iterate 
                            extract 'MODEL NAME' 
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'An MultipleCompilationErrorsException exception is thrown'
            thrown MultipleCompilationErrorsException
    }

    void 'test can disallow closure creation using a secure syntax with AST customizer' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        and:
            ImportCustomizer customizer = new ImportCustomizer()
            customizer.addStaticStars ETLDomain.class.name

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


        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding, configuration)
                    .evaluate("""
                        domain Device
                        read labels
                        def greeting = { String name -> "Hello, \$name!" }
                        assert greeting('Diego') == 'Hello, Diego!'
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'An MissingMethodException exception is thrown'
            MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
            e.errorCollector.errors[0].cause*.message == ["Closures are not allowed"]
    }

    void 'test can disallow method creation using a secure syntax with AST customizer' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        and:
            ImportCustomizer customizer = new ImportCustomizer()
            customizer.addStaticStars ETLDomain.class.name

        and:
            SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
            secureASTCustomizer.closuresAllowed = false             // disallow closure creation
            secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions

        and:
            CompilerConfiguration configuration = new CompilerConfiguration()
            configuration.addCompilationCustomizers customizer, secureASTCustomizer


        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding, configuration).
                    evaluate("""
            domain Device
            read labels
            def greeting(String name){ 
                "Hello, \$name!" 
            }
            assert greeting('Diego') == 'Hello, Diego!'
        """.stripIndent(), ETLProcessor.class.name)

        then: 'An MissingMethodException exception is thrown'
            MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
            e.errorCollector.errors*.cause*.message == ["Method definitions are not allowed"]
    }

    void 'test can disallow unnecessary imports using a secure syntax with AST customizer' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        and:
            ImportCustomizer customizer = new ImportCustomizer()
            customizer.addStaticStars ETLDomain.class.name

        and:
            SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
            secureASTCustomizer.closuresAllowed = false             // disallow closure creation
            secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
            secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports

        and:
            CompilerConfiguration configuration = new CompilerConfiguration()
            configuration.addCompilationCustomizers customizer, secureASTCustomizer


        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding, configuration).
                    evaluate("""
            
            import java.lang.Math
            
            domain Device
            read labels
            Math.max 10, 100
        """.stripIndent(),
                            ETLProcessor.class.name)

        then: 'An MultipleCompilationErrorsException exception is thrown'
            MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
            e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math] is not allowed"]
    }

    void 'test can disallow unnecessary stars imports using a secure syntax with AST customizer' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        and:
            ImportCustomizer customizer = new ImportCustomizer()
            customizer.addStaticStars ETLDomain.class.name

        and:
            SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
            secureASTCustomizer.closuresAllowed = false             // disallow closure creation
            secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
            secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
            secureASTCustomizer.starImportsWhitelist = []

        and:
            CompilerConfiguration configuration = new CompilerConfiguration()
            configuration.addCompilationCustomizers customizer, secureASTCustomizer


        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding, configuration).
                    evaluate("""
            
            import java.lang.Math.*
            
            domain Device
            read labels
            max 10, 100
        """.stripIndent(),
                            ETLProcessor.class.name)

        then: 'An MultipleCompilationErrorsException exception is thrown'
            MultipleCompilationErrorsException e = thrown MultipleCompilationErrorsException
            e.errorCollector.errors*.cause*.message == ["Importing [java.lang.Math.*] is not allowed"]
    }

    void 'test can allow stars imports using a secure syntax with AST customizer' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        and:
            ImportCustomizer customizer = new ImportCustomizer()
            customizer.addStaticStars Math.class.name
            customizer.addStaticStars DebugConsole.ConsoleStatus.class.name

        and:
            SecureASTCustomizer secureASTCustomizer = new SecureASTCustomizer()
            secureASTCustomizer.closuresAllowed = false             // disallow closure creation
            secureASTCustomizer.methodDefinitionAllowed = false     // disallow method definitions
            secureASTCustomizer.importsWhitelist = []  // Empty withe list means forbid imports
            secureASTCustomizer.starImportsWhitelist = []

        and:
            CompilerConfiguration configuration = new CompilerConfiguration()
            configuration.addCompilationCustomizers customizer, secureASTCustomizer

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding, configuration)
                    .evaluate("""
            read labels
            max 10, 100
        """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'An MultipleCompilationErrorsException exception is not thrown'
            notThrown MultipleCompilationErrorsException
    }

    void 'test can enable console and log domain selected' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                            console on
                            domain Device
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'A console content could be recovered after processing an ETL Scrtipt'
            console.buffer.toString() == new StringBuffer("INFO - Console status changed: on")
                    .append(System.lineSeparator())
                    .append("INFO - Selected Domain: Device")
                    .append(System.lineSeparator())
                    .toString()
    }

    void 'test can debug a selected value for a column name' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                            console on
                            read labels
                            domain Device
                            iterate {
                                debug 'DEVICE ID'
                            }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'A console content could be recovered after processing an ETL Scrtipt'
            console.buffer.toString() == new StringBuffer("INFO - Console status changed: on")
                    .append(System.lineSeparator())
                    .append("INFO - Reading labels [0:DEVICE ID, 1:MODEL NAME, 2:MANUFACTURER NAME]")
                    .append(System.lineSeparator())
                    .append("INFO - Selected Domain: Device")
                    .append(System.lineSeparator())
                    .append("DEBUG - [position:[0, 1], value:152254]")
                    .append(System.lineSeparator())
                    .append("DEBUG - [position:[0, 2], value:152255]")
                    .append(System.lineSeparator())
                    .append("DEBUG - [position:[0, 3], value:152256]")
                    .append(System.lineSeparator())
                    .toString()
    }

    void 'test can throw an ETLProcessorException for an invalid console status' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME"],
                    ["152254", "SRW24G4", "LINKSYS"],
                    ["152255", "ZPHA Module", "TippingPoint"],
                    ["152256", "Slideaway", "ATEN"]
            ]

        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                            console open
                            domain Device
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'An ETLProcessorException is thrown'
            ETLProcessorException e = thrown ETLProcessorException
            e.message == "Unknown console command option: open"
    }

    void 'test can translate an extracted value using a dictionary' () {

        given:
            List<List<String>> data = [
                    ["DEVICE ID", "MODEL NAME", "MANUFACTURER NAME", "ENVIRONMENT"],
                    ["152254", "SRW24G4", "LINKSYS", "Prod"],
                    ["152255", "ZPHA Module", "TippingPoint", "Prod"],
                    ["152256", "Slideaway", "ATEN", "Dev"]
            ]

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""domain Device
                            def final dictionary = [prod: 'Production', dev: 'Development']
                            read labels
                            iterate {
                                extract 'ENVIRONMENT' transform { 
                                            lowercase
                                            translate with: dictionary
                                } 
                                        
                            }""".stripIndent(),
                    ETLProcessor.class.name)

        then: 'The column is trsanlated for every row'
            etlProcessor.getRow(0).getElement(3).value == "Production"
            etlProcessor.getRow(1).getElement(3).value == "Production"
            etlProcessor.getRow(2).getElement(3).value == "Development"
    }

    void 'test can load field with an extracted element value after validate fields specs' () {

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY", "LOCATION"],
                    ["152254", "Microsoft", "(xlsx updated)", "ACME Data Center"],
                    ["152255", "Mozilla", "NGM", "ACME Data Center"]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                                read labels
                                domain Application
                                iterate {
                                    extract 'VENDOR NAME' load appVendor
                                }""".stripIndent(),
                    ETLProcessor.class.name)

        then: 'Results should contain domain results associated'
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].originalValue == "Microsoft"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].value == "Microsoft"

            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.name == "appVendor"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.label == "Vendor"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.control == "String"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.constraints.required == 0

            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].originalValue == "Mozilla"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].value == "Mozilla"

            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].field.name == "appVendor"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].field.label == "Vendor"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].field.control == "String"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].field.constraints.required == 0
    }

    void 'test can load field many times with the same extracted value' () {

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY", "LOCATION"],
                    ["152254", "Microsoft", "(xlsx updated)", "ACME Data Center"],
                    ["152255", "Mozilla", "NGM", "ACME Data Center"]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
                    ],
                    [constraints: [required: 0],
                     "control"  : "String",
                     "default"  : "",
                     "field"    : "description",
                     "imp"      : "N",
                     "label"    : "Description",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ])

        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                                read labels
                                domain Application
                                iterate {
                                    extract 'VENDOR NAME' load appVendor load description
                                }""".stripIndent(),
                    ETLProcessor.class.name)

        then: 'Results should contain domain results associated'
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].originalValue == "Microsoft"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].value == "Microsoft"

            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.name == "appVendor"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.label == "Vendor"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.control == "String"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.constraints.required == 0

            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].originalValue == "Mozilla"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].value == "Mozilla"

            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].field.name == "appVendor"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].field.label == "Vendor"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].field.control == "String"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].field.constraints.required == 0
    }

    void 'test can throw an ETLProcessorException when try to load without domain definition' () {

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY", "LOCATION"],
                    ["152254", "Microsoft", "(xlsx updated)", "ACME Data Center"],
                    ["152255", "Mozilla", "NGM", "ACME Data Center"]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()

        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                                domain Application
                                read labels
                                iterate {
                                    extract 'VENDOR NAME' load appVendor
                                }""".stripIndent(),
                    ETLProcessor.class.name)

        then: 'An ETLProcessorException is thrown'
            ETLProcessorException e = thrown ETLProcessorException
            e.message == "There is not validator for domain Application"

    }

    void 'test can throw an ETLProcessorException when try to load with domain definition but without domain fields specification ' () {

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY", "LOCATION"],
                    ["152254", "Microsoft", "(xlsx updated)", "ACME Data Center"],
                    ["152255", "Mozilla", "NGM", "ACME Data Center"]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                                read labels
                                domain Application
                                iterate {
                                    extract 'VENDOR NAME' load vendor
                                }""".stripIndent(),
                    ETLProcessor.class.name)

        then: 'An ETLProcessorException is thrown'
            ETLProcessorException e = thrown ETLProcessorException
            e.message == "The domain Application does not have specifications for field: vendor"
    }

    void 'test can extract a field value and load into a domain object property name' () {

        // The 'load into' command will take whatever value is in the internal register and map it to the domain object
        // property name.  The command takes a String argument that will map to the property name of the domain. This
        // should use the AssetFieldSettings Specifications for the domain to validate the property names. It should error
        // with an explaination that the property does not exist and reference the line of the error if possible.

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY"],
                    ["152254", "Microsoft", "(xlsx updated)"],
                    ["152255", "Mozilla", "NGM"],
                    ["152256", "VMWare", ""]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        domain Application
                        read labels
                        iterate {
                            extract 'VENDOR NAME' load appVendor
                        }
                    """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Every field property is assigned to the correct element'
            etlProcessor.getRow(0).getElement(1).value == "Microsoft"
            etlProcessor.getRow(0).getElement(1).field.name == "appVendor"
            etlProcessor.getRow(1).getElement(1).value == "Mozilla"
            etlProcessor.getRow(1).getElement(1).field.name == "appVendor"
            etlProcessor.getRow(2).getElement(1).value == "VMWare"
            etlProcessor.getRow(2).getElement(1).field.name == "appVendor"
    }

    void 'test can process a selected domain rows' () {

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY"],
                    ["152254", "Microsoft", "(xlsx updated)"],
                    ["152255", "Mozilla", "NGM"],
                    ["152256", "VMWare", ""]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        domain Application
                        read labels
                        iterate {
                            extract 'VENDOR NAME' load appVendor
                        }
                        """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Results should contain domain results associated'
            etlProcessor.getRow(0).getElement(1).value == "Microsoft"
            etlProcessor.getRow(0).getElement(1).field.name == "appVendor"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].value == "Microsoft"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.name == "appVendor"

            etlProcessor.getRow(1).getElement(1).value == "Mozilla"
            etlProcessor.getRow(1).getElement(1).field.name == "appVendor"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].value == "Mozilla"
            etlProcessor.results.get(ETLDomain.Application)[1].elements[0].field.name == "appVendor"

            etlProcessor.getRow(2).getElement(1).value == "VMWare"
            etlProcessor.getRow(2).getElement(1).field.name == "appVendor"
            etlProcessor.results.get(ETLDomain.Application)[2].elements[0].value == "VMWare"
            etlProcessor.results.get(ETLDomain.Application)[2].elements[0].field.name == "appVendor"
    }

    void 'test can process multiple domains in same row' () {

        // The 'load into' command will take whatever value is in the internal register and map it to the domain object
        // property name.  The command takes a String argument that will map to the property name of the domain. This
        // should use the AssetFieldSettings Specifications for the domain to validate the property names. It should error
        // with an explaination that the property does not exist and reference the line of the error if possible.
        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY", "LOCATION"],
                    ["152254", "Microsoft", "(xlsx updated)", "ACME Data Center"],
                    ["152255", "Mozilla", "NGM", "ACME Data Center"],
                    ["152256", "VMWare", "VMWare", "VMWare offices"]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
            validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, [
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
                     "field"    : "location",
                     "imp"      : "N",
                     "label"    : "Location",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ])

        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        read labels
                        iterate {
                            domain Application
                            extract 0 load id
                            extract 'VENDOR NAME' load appVendor
                            
                            domain Device
                            extract 'LOCATION' load location
                        }
                        """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Results should contain domain results associated'
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].value == "152254"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[0].field.name == "id"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[1].value == "Microsoft"
            etlProcessor.results.get(ETLDomain.Application)[0].elements[1].field.name == "appVendor"

            etlProcessor.results.get(ETLDomain.Device)[0].elements[0].value == "ACME Data Center"
            etlProcessor.results.get(ETLDomain.Device)[0].elements[0].field.name == "location"
    }

    void 'test can load values without extract previously' () {

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY", "LOCATION"],
                    ["152254", "Microsoft", "(xlsx updated)", "ACME Data Center"],
                    ["152255", "Mozilla", "NGM", "ACME Data Center"]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
                    ],
                    [constraints: [required: 0],
                     "control"  : "String",
                     "default"  : "",
                     "field"    : "environment",
                     "imp"      : "N",
                     "label"    : "Environment",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ])
            validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, [
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
                     "field"    : "location",
                     "imp"      : "N",
                     "label"    : "Location",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ])
        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        read labels
                        
                        iterate {
                        
                            domain Application
                            load environment with Production
                            extract 0 load id
                            extract 'VENDOR NAME' load appVendor
                            
                            domain Device
                            extract 0 load id 
                            load location with 'Development'        
                        }
                        """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Results should contain Application domain results associated'
            with(etlProcessor.results.get(ETLDomain.Application)[0]) {

                with(elements[0]) {
                    originalValue == "Production"
                    value == "Production"
                    field.name == "environment"
                }

                with(elements[1]) {
                    originalValue == "152254"
                    value == "152254"
                    field.name == "id"
                }
            }

            with(etlProcessor.results.get(ETLDomain.Application)[1]) {
                with(elements[0]) {
                    originalValue == "Production"
                    value == "Production"
                    field.name == "environment"
                }

                with(elements[1]) {
                    originalValue == "152255"
                    value == "152255"
                    field.name == "id"
                }
            }

        and: 'Results should contain Device domain results associated'
            etlProcessor.results.get(ETLDomain.Device)[0].elements[1].originalValue == "Development"
            etlProcessor.results.get(ETLDomain.Device)[0].elements[1].value == "Development"
            etlProcessor.results.get(ETLDomain.Device)[0].elements[1].field.name == "location"

            etlProcessor.results.get(ETLDomain.Device)[1].elements[1].originalValue == "Development"
            etlProcessor.results.get(ETLDomain.Device)[1].elements[1].value == "Development"
            etlProcessor.results.get(ETLDomain.Device)[1].elements[1].field.name == "location"

            etlProcessor.results.get(ETLDomain.Device)[0].elements[0].originalValue == "152254"
            etlProcessor.results.get(ETLDomain.Device)[0].elements[0].value == "152254"
            etlProcessor.results.get(ETLDomain.Device)[0].elements[0].field.name == "id"

            etlProcessor.results.get(ETLDomain.Device)[1].elements[0].originalValue == "152255"
            etlProcessor.results.get(ETLDomain.Device)[1].elements[0].value == "152255"
            etlProcessor.results.get(ETLDomain.Device)[1].elements[0].field.name == "id"
    }

    void 'test can reference a domain Property Name with loaded Data Value' () {

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY", "LOCATION"],
                    ["152254", "Microsoft", "(xlsx updated)", "ACME Data Center"],
                    ["152255", "Mozilla", "NGM", "ACME Data Center"]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
                    ],
                    [constraints: [required: 0],
                     "control"  : "String",
                     "default"  : "",
                     "field"    : "environment",
                     "imp"      : "N",
                     "label"    : "Environment",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ])
            validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, [
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
                     "field"    : "location",
                     "imp"      : "N",
                     "label"    : "Location",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ])

        and:
            Project GMDEMO = Mock(Project)
            GMDEMO.getId() >> 125612l

            Project TMDEMO = Mock(Project)
            TMDEMO.getId() >> 125612l

            List<AssetEntity> applications = [
                    [assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
                    [assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
                    [assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
            ].collect {
                AssetEntity mock = Mock()
                mock.getId() >> it.id
                mock.getAssetClass() >> it.assetClass
                mock.getAssetName() >> it.assetName
                mock.getProject() >> it.project
                mock
            }

        and:
            AssetEntity.metaClass.static.executeQuery = { String query, Map args ->
                applications.findAll { it.id == args.id && it.project.id == args.project.id }
            }

        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(GMDEMO, data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        console on
                        read labels
                        iterate {
                            domain Application
                            load environment with Production
                            extract 'APPLICATION ID' load id
                            reference id with id
                        }
                        """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Results should contain Application domain results associated'

            with(etlProcessor.results.get(ETLDomain.Application)[0]) {

                with(elements[0]) {
                    originalValue == "Production"
                    value == "Production"
                    field.name == "environment"
                }

                with(elements[1]) {
                    originalValue == "152254"
                    value == "152254"
                    field.name == "id"
                }

                reference == [152254]
            }

            with(etlProcessor.results.get(ETLDomain.Application)[1]) {

                with(elements[0]) {
                    originalValue == "Production"
                    value == "Production"
                    field.name == "environment"
                }

                with(elements[1]) {
                    originalValue == "152255"
                    value == "152255"
                    field.name == "id"
                }

                reference == [152255]
            }
    }

    void 'test can throw an Exception if script reference to a domain Property and it was not defined in the ETL Processor' () {

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY", "LOCATION"],
                    ["152254", "Microsoft", "(xlsx updated)", "ACME Data Center"],
                    ["152255", "Mozilla", "NGM", "ACME Data Center"]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
                    ],
                    [constraints: [required: 0],
                     "control"  : "String",
                     "default"  : "",
                     "field"    : "environment",
                     "imp"      : "N",
                     "label"    : "Environment",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ])
            validator.addAssetClassFieldsSpecFor(AssetClass.DEVICE, [
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
                     "field"    : "location",
                     "imp"      : "N",
                     "label"    : "Location",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ])

        and:
            Project GMDEMO = Mock(Project)
            GMDEMO.getId() >> 125612l

            Project TMDEMO = Mock(Project)
            TMDEMO.getId() >> 125612l

            List<AssetEntity> applications = [
                    [assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
                    [assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "Another Data Center", project: GMDEMO],
                    [assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
            ].collect {
                AssetEntity mock = Mock()
                mock.getId() >> it.id
                mock.getAssetClass() >> it.assetClass
                mock.getAssetName() >> it.assetName
                mock.getProject() >> it.project
                mock
            }

        and:
            AssetEntity.metaClass.static.executeQuery = { String query, Map args ->
                applications.findAll { it.id == args.id && it.project.id == args.project.id }
            }

        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        console on
                        read labels
                        iterate {
                            domain Application
                            load environment with Production
                            extract 'APPLICATION ID' load id
                            reference id with id
                        }
                        """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'It throws an Exception because project was not defined'

            ETLProcessorException e = thrown ETLProcessorException
            e.message == 'Project not defined.'
    }

    void 'test can reference multiple asset entities for a domain Property Name with loaded Data Value' () {

        given:
            List<List<String>> data = [
                    ["APPLICATION ID", "VENDOR NAME", "TECHNOLOGY", "LOCATION"],
                    ["152254", "Microsoft", "(xlsx updated)", "ACME Data Center"],
                    ["152255", "Mozilla", "NGM", "ACME Data Center"]
            ]

        and:
            ETLFieldsValidator validator = new ETLAssetClassFieldsValidator()
            validator.addAssetClassFieldsSpecFor(AssetClass.APPLICATION, [
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
                    ],
                    [constraints: [required: 0],
                     "control"  : "String",
                     "default"  : "",
                     "field"    : "environment",
                     "imp"      : "N",
                     "label"    : "Environment",
                     "order"    : 0,
                     "shared"   : 0,
                     "show"     : 0,
                     "tip"      : "",
                     "udf"      : 0
                    ]
            ])

        and:
            Project GMDEMO = Mock(Project)
            GMDEMO.getId() >> 125612l

            Project TMDEMO = Mock(Project)
            TMDEMO.getId() >> 125612l

            List<AssetEntity> applications = [
                    [assetClass: AssetClass.APPLICATION, id: 152254l, assetName: "ACME Data Center", project: GMDEMO],
                    [assetClass: AssetClass.APPLICATION, id: 152255l, assetName: "ACME Data Center", project: GMDEMO],
                    [assetClass: AssetClass.DEVICE, id: 152256l, assetName: "Application Microsoft", project: TMDEMO]
            ].collect {
                AssetEntity mock = Mock()
                mock.getId() >> it.id
                mock.getAssetClass() >> it.assetClass
                mock.getAssetName() >> it.assetName
                mock.getProject() >> it.project
                mock
            }

        and:
            AssetEntity.metaClass.static.executeQuery = { String query, Map args ->
                applications.findAll { it.assetName == args.assetName && it.project.id == args.project.id }
            }

        and:
            DebugConsole console = new DebugConsole(buffer: new StringBuffer())

        and:
            ETLProcessor etlProcessor = new ETLProcessor(GMDEMO, data, console, validator)

        and:
            ETLBinding binding = new ETLBinding(etlProcessor)

        when: 'The ETL script is evaluated'
            new GroovyShell(this.class.classLoader, binding)
                    .evaluate("""
                        console on
                        read labels
                        iterate {
                            domain Application
                            load environment with Production
                            extract 'LOCATION' load Vendor
                            reference assetName with Vendor
                        }
                        """.stripIndent(),
                    ETLProcessor.class.name)

        then: 'Results should contain Application domain results associated'

            with(etlProcessor.results.get(ETLDomain.Application)[0]) {

                with(elements[0]) {
                    originalValue == "Production"
                    value == "Production"
                    field.name == "environment"
                }

                with(elements[1]) {
                    originalValue == "ACME Data Center"
                    value == "ACME Data Center"
                    field.name == "Vendor"
                    field.label == "Vendor"
                }

                reference == [152254, 152255]
            }

            with(etlProcessor.results.get(ETLDomain.Application)[1]) {

                with(elements[0]) {
                    originalValue == "Production"
                    value == "Production"
                    field.name == "environment"
                }

                with(elements[1]) {
                    originalValue == "ACME Data Center"
                    value == "ACME Data Center"
                    field.name == "Vendor"
                    field.label == "Vendor"
                }

                reference == [152254, 152255]
            }
    }

}
