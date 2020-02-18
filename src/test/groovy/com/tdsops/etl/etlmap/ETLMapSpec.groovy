package com.tdsops.etl.etlmap

import com.tdsops.etl.Column
import com.tdsops.etl.DebugConsole
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLAssertTrait
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldDefinition
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLFileSystemTrait
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
import com.tdsops.etl.ETLProcessorResult
import com.tdsops.etl.FieldSpecValidateableTrait
import com.tdsops.etl.RowResult
import com.tdsops.etl.dataset.ETLDataset
import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.project.Project
import spock.lang.Specification

class ETLMapSpec extends Specification implements FieldSpecValidateableTrait, ETLFileSystemTrait, ETLAssertTrait, DataTest, AutowiredTest {

    Closure doWithSpring() {
        { ->
            coreService(CoreService) {
                grailsApplication = ref('grailsApplication')
            }
            fileSystemService(FileSystemService) {
                coreService = ref('coreService')
            }
        }
    }

    FileSystemService fileSystemService
    Project GMDEMO
    DebugConsole debugConsole
    ETLFieldsValidator validator

    void setupSpec() {
        mockDomain Project
    }

    void setup() {
        assert fileSystemService != null

        GMDEMO = Mock(Project)
        GMDEMO.getId() >> 125612l
        debugConsole = new DebugConsole(buffer: new StringBuilder())

        validator = createDomainClassFieldsValidator()
    }

    void 'test can define a ETL map definition using column name and domain property name'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'device-name', 'Name'
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
		    etlProcessor.etlMaps.size() == 1
		    assertWith(etlProcessor.etlMaps['verni-devices'], ETLMap){
                domain == ETLDomain.Device
                instructions.size() == 1
                assertWith(instructions[0], ETLMapInstruction){

                    assertWith(column, Column){
                        label == 'device-name'
                        index == 1
                    }

                    assertWith(domainProperty, ETLFieldDefinition){
                        name == 'assetName'
                        label == 'Name'
                    }
                    transformations.isEmpty()
                }
		    }

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	void 'test can define a ETL map definition using column name equals to a domain property name'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				Name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'Name'
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
		    etlProcessor.etlMaps.size() == 1
		    assertWith(etlProcessor.etlMaps['verni-devices'], ETLMap){
                domain == ETLDomain.Device
                instructions.size() == 1
                assertWith(instructions[0], ETLMapInstruction){

                    assertWith(column, Column){
                        label == 'Name'
                        index == 1
                    }

                    assertWith(domainProperty, ETLFieldDefinition){
                        name == 'assetName'
                        label == 'Name'
                    }
                    transformations.isEmpty()
                }
		    }

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	void 'test can define a ETL map definition using column ordinal position and domain property name'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 1, 'Name'
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
		    etlProcessor.etlMaps.size() == 1
		    assertWith(etlProcessor.etlMaps['verni-devices'], ETLMap){
                domain == ETLDomain.Device
                instructions.size() == 1
                assertWith(instructions[0], ETLMapInstruction){

                    assertWith(column, Column){
                        label == 'device-name'
                        index == 1
                    }

                    assertWith(domainProperty, ETLFieldDefinition){
                        name == 'assetName'
                        label == 'Name'
                    }
                    transformations.isEmpty()
                }
		    }

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

    void 'test can define a ETL map definition using column name a domain property name and a simple transformation without parameters'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'device-name', 'Name', uppercase()
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
		    etlProcessor.etlMaps.size() == 1
		    assertWith(etlProcessor.etlMaps['verni-devices'], ETLMap){
                domain == ETLDomain.Device
                instructions.size() == 1
                assertWith(instructions[0], ETLMapInstruction){

                    assertWith(column, Column){
                        label == 'device-name'
                        index == 1
                    }

                    assertWith(domainProperty, ETLFieldDefinition){
                        name == 'assetName'
                        label == 'Name'
                    }

                    transformations.size() == 1
                    assertWith(transformations.first(), ETLMapTransform){
                        methodName == 'uppercase'
                        parameters.size() == 0
                    }

                }
		    }

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

    void 'test can define a ETL map definition using column name a domain property name and a simple transformation with parameters'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'device-name', 'Name', left(4)
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
		    etlProcessor.etlMaps.size() == 1
		    assertWith(etlProcessor.etlMaps['verni-devices'], ETLMap){
                domain == ETLDomain.Device
                instructions.size() == 1
                assertWith(instructions[0], ETLMapInstruction){

                    assertWith(column, Column){
                        label == 'device-name'
                        index == 1
                    }

                    assertWith(domainProperty, ETLFieldDefinition){
                        name == 'assetName'
                        label == 'Name'
                    }

                    transformations.size() == 1
                    assertWith(transformations.first(), ETLMapTransform){
                        methodName == 'uppercase'
                        parameters.size() == 1
                        parameters.first() == 4
                    }
                }
		    }

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	void 'test can define a ETL map definition using column name equals to a domain property name and a simple transformation without parameters'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				Name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'Name', uppercase()
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
		    etlProcessor.etlMaps.size() == 1
		    assertWith(etlProcessor.etlMaps['verni-devices'], ETLMap){
                domain == ETLDomain.Device
                instructions.size() == 1
                assertWith(instructions[0], ETLMapInstruction){
                    assertWith(column, Column){
                        label == 'Name'
                        index == 1
                    }
                    assertWith(domainProperty, ETLFieldDefinition){
                        name == 'assetName'
                        label == 'Name'
                    }
                    transformations.size() == 1
                    assertWith(transformations.first(), ETLMapTransform){
                        methodName == 'uppercase'
                        parameters.size() == 0
                    }
                }
		    }

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	void 'test can define a ETL map definition using column name equals to a domain property name and a simple transformation with parameters'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				Name,environment
				acmevmprod01,PROD
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'environment', 'Environment', substitute(['PROD':'Production', 'DEV': 'Development'])
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
		    etlProcessor.etlMaps.size() == 1
		    assertWith(etlProcessor.etlMaps['verni-devices'], ETLMap){
                domain == ETLDomain.Device
                instructions.size() == 1
                assertWith(instructions[0], ETLMapInstruction){

                    assertWith(column, Column){
                        label == 'environment'
                        index == 1
                    }
                    assertWith(domainProperty, ETLFieldDefinition){
                        name == 'environment'
                        label == 'Environment'
                    }
                    transformations.size() == 1
                    assertWith(transformations.first(), ETLMapTransform){
                        methodName == 'substitute'
                        parameters.size() == 1
                        parameters.first() == ['PROD':'Production', 'DEV': 'Development']
                    }
                }
		    }

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

    /********************************************************************************************/
    /********************************************************************************************/
    /*                              Exceptions using defineMap                               */
    /********************************************************************************************/
    /********************************************************************************************/

     void 'test can throw an exception when a defined ETL map uses an invalid domain property name'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'device-name', 'Unknown'
				}
				
			""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == ETLProcessorException.unknownDomainFieldName(ETLDomain.Device, 'Unknown').message + ' at line 4'
				startLine == 4
				endLine == 4
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

     void 'test can throw an exception when a defined ETL map uses an invalid source name'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'Unknown', 'assetName'
				}
				
			""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == ETLProcessorException.extractMissingColumn( 'Unknown').message + ' at line 4'
				startLine == 4
				endLine == 4
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

     void 'test can throw an exception when a defined ETL map uses an invalid source position'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 4, 'assetName'
				}
			""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == 'Invalid index = 4 at line 4'
				startLine == 4
				endLine == 4
				startColumn == null
				endColumn == null
				fatal == true
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	void 'test can throw an exception when a defined ETL map receives incorrect ammount of arguments'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' 'Unknown', {
				    map 4, 'assetName'
				}
			""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == ETLProcessorException.invalidAmountOfArguments().message + ' at line 3'
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	void 'test can throw an exception when a defined ETL map does not receive a Closure as argument'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' 'Unknown'
			""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			with(ETLProcessor.getErrorMessage(e)) {
				message == ETLProcessorException.invalidArgument().message + ' at line 3'
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

    /********************************************************************************************/
    /********************************************************************************************/
    /*                         LOAD USING ETL MAP command tests                                 */
    /********************************************************************************************/
    /********************************************************************************************/

    void 'test can load a defined ETL map definition using column name and domain property name'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'device-name', 'Name'
				}
				
				domain Device
				iterate {
				    loadMap 'verni-devices'
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['assetName'], 'acmevmprod01', 'acmevmprod01')
					}
				}
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	    void 'test can load a defined ETL map definition using column name, domain property name and a transformation without arguments'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'device-name', 'Name', uppercase()
				}
				
				domain Device
				iterate {
				    loadMap 'verni-devices'
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['assetName'], 'acmevmprod01', 'ACMEVMPROD01')
					}
				}
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	    void 'test can load a defined ETL map definition using column name, domain property name and a transformation with a simple argument'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				device-name
				acmevmprod01
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'device-name', 'Name', left(4)
				}
				
				domain Device
				iterate {
				    loadMap 'verni-devices'
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['assetName'], 'acmevmprod01', 'acme')
					}
				}
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

	void 'test can load a defined ETL map definition using column name, domain property name and a transformation with a Map argument'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				environment
				PROD
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'environment', 'Environment', substitute(['PROD':'Production', 'DEV': 'Development'])
				}
				
				domain Device
				iterate {
				    loadMap 'verni-devices'
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['environment'], 'PROD', 'Production')
					}
				}
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

		void 'test can load a defined ETL map definition using column name, domain property name and a multiple transformations'() {

		given:
			def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				environment
				prod
			""", fileSystemService)

		and:
			ETLProcessor etlProcessor = new ETLProcessor(
				GMDEMO,
				dataSet,
				debugConsole,
				validator)

		when: 'The ETL script is evaluated'
			etlProcessor.evaluate("""
				read labels
				defineMap Device 'verni-devices' {
				    map 'environment', 'Environment', uppercase(), substitute(['PROD':'Production', 'DEV': 'Development'])
				}
				
				domain Device
				iterate {
				    loadMap 'verni-devices'
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
			assertWith(etlProcessor.finalResult(), ETLProcessorResult) {
				domains.size() == 1
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					assertWith(data[0], RowResult) {
						fields.size() == 1
						assertFieldResult(fields['environment'], 'prod', 'Production')
					}
				}
			}

		cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
	}

}
