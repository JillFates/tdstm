package com.tdsops.etl

import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.data.Field
import getl.excel.ExcelConnection
import getl.excel.ExcelDataset
import getl.exception.ExceptionGETL
import spock.lang.Specification

/**
 * Testing GroovyETL project.
 * It shows how to open a file using deiferrent connections and drivers.
 * <pre>
 *      CSVConnection connection = new CSVConnection(config: "csv", path: pathName)
 *      CSVDataset dataset = new CSVDataset(connection: connection, fileName: fileName, header: true)
 *      dataset.readLinesCount() == 3
 * </pre>
 */
class GETLReaderSpec extends Specification {


    /**
     *
     * CSV tests
     */
    void 'test can read a csv file defining fields' () {

        given:
            String fileName = "applications.csv"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            CSVConnection csvCon = new CSVConnection(config: "csv", path: pathName)
            CSVDataset csvFile = new CSVDataset(connection: csvCon, fileName: fileName)

        when: 'CSV Dataset has defined field'
            csvFile.field << new Field(name: 'Id', type: Field.Type.INTEGER, isNull: false, isKey: true, extended: [increment: true])
            csvFile.field << new Field(name: 'Name', type: Field.Type.STRING, isNull: false, trim: true)
            csvFile.field << new Field(name: 'Description', type: Field.Type.STRING)
            csvFile.field << new Field(name: 'Environment', type: Field.Type.STRING)
            csvFile.field << new Field(name: 'Modified Date', type: Field.Type.STRING)

        then: 'It can read rows count'
            csvFile.readRowCount() == 3

        and: 'First row results contains fields values'
            csvFile.rows()[0].id == "114054"
            csvFile.rows()[0].name == "BlackBerry Enterprise Server"
            csvFile.rows()[0].description == "Email sync to Blackberry handhelds"
            csvFile.rows()[0].environment == "Production"
    }

    void 'test can read a csv file with header labels without defining fields' () {

        given:
            String fileName = "applications.csv"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            CSVConnection connection = new CSVConnection(config: "csv", path: pathName)

        when: 'CSVDataset can read column names without fields definition'
            CSVDataset dataset = new CSVDataset(connection: connection, fileName: fileName, header: true)

        then: 'It can read total amount of rows'
            dataset.readLinesCount() == 3

        and: 'It can detect automatically the amount of CSV fields'
            List<Field> fields = dataset.connection.driver.fields(dataset)
            fields.size() == 5

        and: 'It can detect and define automatically CSV Field Names'
            fields[0].name == 'id'
            fields[1].name == 'name'
            fields[2].name == 'description'
            fields[3].name == 'environment'
            fields[4].name == 'modified date'

        and: 'All CSV field are Type of Field.Type.STRING'
            fields[0].type == Field.Type.STRING
            fields[1].type == Field.Type.STRING
            fields[2].type == Field.Type.STRING
            fields[3].type == Field.Type.STRING
            fields[4].type == Field.Type.STRING

        and: 'First row results contains fields values'
            Map row = dataset.rows()[0]
            row.id == "114054"
            row.name == "BlackBerry Enterprise Server"
            row.description == "Email sync to Blackberry handhelds"
            row.environment == "Production"
            row.'modified date' == "08/31/2015 05:15 PM"
    }

    void 'test can read a csv file extracted from Service Now and read fields automatically' () {

        given:
            String fileName = "service_now_applications.csv"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            CSVConnection connection = new CSVConnection(config: "csv", path: pathName)

        when: 'CSVDataset can read column names without fields definition'
            CSVDataset dataset = new CSVDataset(connection: connection, fileName: fileName, header: true)

        then: 'It can read total amount of rows'
            dataset.readLinesCount() == 15

        and: 'It can detect automatically the amount of CSV fields'
            List<Field> fields = dataset.connection.driver.fields(dataset)
            fields.size() == 12

        and: 'It can detect and define automatically CSV Field Names'
            fields[0].name == 'name'
            fields[1].name == 'short_description'
            fields[2].name == 'used_for'
            fields[3].name == 'sys_id'
            fields[4].name == 'sys_updated_on'
            fields[5].name == 'vendor'
            fields[6].name == 'sys_class_name'
            fields[7].name == 'department'
            fields[8].name == 'supported_by'
            fields[9].name == 'owned_by'
            fields[10].name == 'warranty_expiration'
            fields[11].name == 'fqdn'

        and: 'All CSV field are Type of Field.Type.STRING'
            fields*.type.toSet() == [Field.Type.STRING].toSet()

        and: 'First row results contains fields values'
            Map row = dataset.rows()[0]
            row.name == "apache linux den 200"
            row.short_description == null
            row.used_for == null
            row.sys_id == "5f8af237c0a8010e01a932999468b83a"
            row.sys_updated_on == "2006-07-11 14:42:56"
            row.vendor == null
            row.sys_class_name == "Web Server"
    }

    void 'test can read a csv and iterate fields' () {

        given:
            String fileName = "applications.csv"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            CSVConnection csvCon = new CSVConnection(config: "csv", path: pathName)

        when: 'CSVDataset has defined field'
            CSVDataset csvFile = new CSVDataset(connection: csvCon, fileName: fileName, header: true)

        then: 'It can read rows count'
            csvFile.rows()[0].id == "114054"
            csvFile.rows()[0].name == "BlackBerry Enterprise Server"
            csvFile.rows()[0].description == "Email sync to Blackberry handhelds"
            csvFile.rows()[0].environment == "Production"
    }

    /**
     *
     * Excel tests
     *
     */
    void 'test can read an excel file defining fields' () {

        given:
            String fileName = "applications.xlsx"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            ExcelConnection excelConnection = new ExcelConnection(path: pathName, fileName: fileName)

            ExcelDataset excelDataset = new ExcelDataset(
                    connection: excelConnection,
                    listName: "applications",
                    header: true,
                    showWarnings: true,
                    manualSchema: true)

        when: 'CSVDataset has defined field'
            excelDataset.field << new Field(name: 'Id', type: Field.Type.BIGINT)
            excelDataset.field << new Field(name: 'Name', type: Field.Type.STRING)
            excelDataset.field << new Field(name: 'Description', type: Field.Type.STRING)
            excelDataset.field << new Field(name: 'Environment', type: Field.Type.STRING)

        then: 'It can read rows count'
            excelDataset.rows().size() == 3
    }

    void 'test cannot read an excel file without defining fields' () {

        given:
            String fileName = "applications.xlsx"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            ExcelConnection excelConnection = new ExcelConnection(path: pathName, fileName: fileName)

        when:
            new ExcelDataset(
                    connection: excelConnection,
                    listName: "applications",
                    showWarnings: true,
                    header: true)
                    .rows()

        then: 'It throws a ExceptionGETL'
            ExceptionGETL e = thrown ExceptionGETL
            e.message == "Required fields description with dataset"
    }

}
