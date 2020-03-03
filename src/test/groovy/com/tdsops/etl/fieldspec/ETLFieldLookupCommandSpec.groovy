package com.tdsops.etl.fieldspec

import com.tdsops.etl.DebugConsole
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLAssertTrait
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLFileSystemTrait
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
import com.tdsops.etl.FieldResult
import com.tdsops.etl.FieldSpecValidateableTrait
import com.tdsops.etl.FindOperator
import com.tdsops.etl.FindResult
import com.tdsops.etl.RowResult
import com.tdsops.etl.dataset.ETLDataset
import grails.testing.gorm.DataTest
import grails.testing.spring.AutowiredTest
import net.transitionmanager.common.CoreService
import net.transitionmanager.common.FileSystemService
import net.transitionmanager.project.Project
import spock.lang.Specification

class ETLFieldLookupCommandSpec extends Specification implements FieldSpecValidateableTrait, ETLFileSystemTrait, ETLAssertTrait, DataTest, AutowiredTest {

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


    void 'test can lookup a field by its label'() {

        given:
           	def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				name,cost
				acmevmprod01,1234
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
				domain Device
				iterate {
				    extract 'name' load 'assetName' set name
				    fieldLookup Device with 'Cost Basis' set costField
				    extract 'cost' transform with toLong() load costField set cost
				    find Device by 'assetName' eq name and costField eq cost into 'Id' 
				}
				
			""".stripIndent())

		then: 'Results should contain Application domain results associated'
		   assertWith(etlProcessor.finalResult()) {
				assertWith(domains[0], DomainResult) {
					domain == ETLDomain.Device.name()
					data.size() == 1

					assertWith(data[0], RowResult) {
						errorCount == 0
						assertWith(fields['assetName'], FieldResult) {
							value == 'acmevmprod01'
							originalValue == 'acmevmprod01'
						}
						assertWith(fields['custom3'], FieldResult) {
							value == 1234l
							originalValue = '1234'
						}
						assertWith(fields['id'], FieldResult) {
							value == null
							originalValue = null
						assertWith(find, FindResult){
							query.size() == 1
							assertQueryResult(
								query[0],
								ETLDomain.Device,
								[
									['assetName', FindOperator.eq.name(), 'acmevmprod01'],
									['custom3', FindOperator.eq.name(), 1234l]
								]
							)
						}
						}
					}
				}
			}

        cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
    }

    void 'test can throws an Exception if could not lookup a custom field spec'() {

        given:
           	def (String fileName, ETLDataset dataSet) = buildCSVDataSet("""
				name,cost
				acmevmprod01,1234
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
				domain Device
				iterate {
				    extract 'name' load 'assetName' set name
				    fieldLookup Device with 'Invalid Label Name' set costField
				    extract 'cost' transform with toLong() load costField set cost
				    find Device by 'assetName' eq name and costField eq cost into 'Id' 
				}
				
			""".stripIndent())

		then: 'It throws an Exception'
			ETLProcessorException e = thrown ETLProcessorException
			with (ETLProcessor.getErrorMessage(e)) {
				message == 'The domain Device does not have field name: Invalid Label Name at line 6'
				startLine == 6
				endLine == 6
				startColumn == null
				endColumn == null
				fatal == true
			}

        cleanup:
			deleteTemporaryFile(fileName, fileSystemService)
    }
}
