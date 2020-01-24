package com.tdsops.etl.dataset

import com.tdsops.etl.DebugConsole
import com.tdsops.etl.DomainResult
import com.tdsops.etl.ETLBaseSpec
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.Element
import com.tdsops.etl.FieldResult
import com.tdsops.etl.RowResult
import com.tdsops.tm.enums.domain.ImportOperationEnum
import grails.testing.gorm.DataTest
import net.transitionmanager.project.Project
import spock.lang.See

class CSVDatasetSpec extends ETLBaseSpec implements DataTest {

    DebugConsole debugConsole
    Project GMDEMO
    ETLFieldsValidator validator

    def setup() {
        GMDEMO = Mock(Project)
        GMDEMO.getId() >> 125612l
        debugConsole = new DebugConsole(buffer: new StringBuilder())
        validator = createDomainClassFieldsValidator()
    }

    @See('TM-16579')
    void 'test can read labels from csv dataset and create a map of columns'() {

        given:
            String fileName = createCSVFIle("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				domain Device
				read labels
			""".stripIndent())

        then: 'A column map is created'
            etlProcessor.column('name').index == 0
            etlProcessor.column(0).label == 'name'

        and:
            etlProcessor.column('cpu').index == 1
            etlProcessor.column(1).label == 'cpu'

        and:
            etlProcessor.column('description').index == 2
            etlProcessor.column(2).label == 'description'

        and:
            etlProcessor.currentRowIndex == 1

        cleanup:
            if (fileName) {
                fileSystemServiceTestBean.deleteTemporaryFile(fileName)
            }
    }

    @See('TM-16579')
    void 'test can skip rows until read labels from csv dataset and create a map of columns'() {

        given:
            String fileName = createCSVFIle("""
                UNKNOWN
                UNNECESSARY
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				domain Device
				skip 2
				read labels
			""".stripIndent())

        then: 'A column map is created'
            etlProcessor.column('name').index == 0
            etlProcessor.column(0).label == 'name'

        and:
            etlProcessor.column('cpu').index == 1
            etlProcessor.column(1).label == 'cpu'

        and:
            etlProcessor.column('description').index == 2
            etlProcessor.column(2).label == 'description'

        and:
            etlProcessor.currentRowIndex == 3

        cleanup:
            if (fileName) {
                fileSystemServiceTestBean.deleteTemporaryFile(fileName)
            }
    }

    @See('TM-16579')
    void 'test can extract a field value over all rows based on column ordinal position'() {

        given:
            String fileName = createCSVFIle("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				domain Device
				read labels
				iterate {
					extract 1
				}
			""".stripIndent())

        then:

            assertWith(etlProcessor.currentRow.getElement(0), Element) {
                value == "zuludb01"
                originalValue == "zuludb01"
            }

        cleanup:
            if (fileName) {
                fileSystemServiceTestBean.deleteTemporaryFile(fileName)
            }
    }

    @See('TM-16579')
    void 'test can extract a field value over all rows based on column name'() {

        given:
            String fileName = createCSVFIle("""
				name,cpu,description
				xraysrv01,100,Description FOOBAR
				zuludb01,10,Some description
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				domain Device
				read labels
				iterate {
					extract 'name'
				}
			""".stripIndent())

        then:

            assertWith(etlProcessor.currentRow.getElement(0), Element) {
                value == "zuludb01"
                originalValue == "zuludb01"
            }

        cleanup:
            if (fileName) {
                fileSystemServiceTestBean.deleteTemporaryFile(fileName)
            }
    }

    @See('TM-16579')
    void 'test can load field with an extracted element value after validate fields specs'() {

        given:
            String fileName = createCSVFIle("""
				application id,vendor name,technology,location
				152254,Microsoft,(xlsx updated),ACME Data Center
				152255,Mozilla,NGM,ACME Data Center
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'vendor name' load 'Vendor'
					extract 'technology' load 'appTech'
				}
			""".stripIndent())

        then: 'Results should contain domain results associated'
            assertWith(etlProcessor.finalResult()) {
                domains.size() == 1
                assertWith(domains[0], DomainResult) {
                    domain == ETLDomain.Application.name()
                    fieldNames == ['appVendor', 'appTech'] as Set
                    assertWith(fieldLabelMap) {
                        appVendor == 'Vendor'
                        appTech == 'Technology'
                    }

                    data.size() == 2
                    assertWith(data[0], RowResult) {
                        op == ImportOperationEnum.INSERT.toString()
                        rowNum == 1
                        fields.keySet().size() == 2
                        assertWith(fields.appVendor, FieldResult) {
                            value == 'Microsoft'
                            originalValue == 'Microsoft'
                            init == null
                        }
                        assertWith(fields.appTech, FieldResult) {
                            value == '(xlsx updated)'
                            originalValue == '(xlsx updated)'
                            init == null
                        }
                    }

                    assertWith(data[1], RowResult) {
                        op == ImportOperationEnum.INSERT.toString()
                        rowNum == 2
                        fields.keySet().size() == 2
                        assertWith(fields.appVendor, FieldResult) {
                            value == 'Mozilla'
                            originalValue == 'Mozilla'
                        }
                        assertWith(fields.appTech, FieldResult) {
                            value == 'NGM'
                            originalValue == 'NGM'
                            init == null
                        }
                    }
                }
            }
    }

    @See('TM-16814')
    void 'test can load field with commas in String literals'() {

        given:
            String fileName = createCSVFIle("""
				id,name,description,owner
				123,Foo,"This, That, and the other","Jim Beam"
				456,Bar,"What out for that owner","Sparrow, Jack"
			""")

        and:
            ETLProcessor etlProcessor = new ETLProcessor(
                    GMDEMO,
                    new CSVDataset(fileName),
                    debugConsole,
                    validator
            )

        when: 'The ETL script is evaluated'
            etlProcessor.evaluate("""
				read labels
				domain Application
				iterate {
					extract 'id' load 'id'
					extract 'name' load 'assetName'
					extract 'description' load 'description'
					extract 'owner' load 'sme'
				}
			""".stripIndent())

        then: 'Results should contain domain results associated'
            assertWith(etlProcessor.finalResult()) {
                domains.size() == 1
                assertWith(domains[0], DomainResult) {
                    domain == ETLDomain.Application.name()
                    fieldNames == ['id', 'assetName', 'description', 'sme'] as Set

                    data.size() == 2
                    assertWith(data[0], RowResult) {
                        op == ImportOperationEnum.INSERT.toString()
                        rowNum == 1
                        fields.keySet().size() == 2
                        assertWith(fields.id, FieldResult) {
                            value == '123'
                            originalValue == '123'
                            init == null
                        }
                        assertWith(fields.assetName, FieldResult) {
                            value == 'Foo'
                            originalValue == 'Foo'
                            init == null
                        }
                        assertWith(fields.description, FieldResult) {
                            value == 'This, That, and the other'
                            originalValue == 'This, That, and the other'
                            init == null
                        }
                        assertWith(fields.sme, FieldResult) {
                            value == 'Jim Beam'
                            originalValue == 'Jim Beam'
                            init == null
                        }
                    }


                }
            }
    }

}
