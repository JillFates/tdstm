package com.tdsops.etl

import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.data.Field
import getl.excel.ExcelConnection
import getl.excel.ExcelDataset
import spock.lang.Specification


class GETLReaderSpec extends Specification {


    void 'test can create a CSV connection' () {

        given:
            String pathName = "test/unit/com/tdsops/etl/resources"

        when: 'It creates a new CSV Connection'
            CSVConnection files = new CSVConnection(config: "csv", path: pathName)

        then: 'It can read all files'
            files.retrieveObjects().size() == 3
            files.retrieveObjects()[0].name == "applications.csv"
            files.retrieveObjects()[1].name == "applications.xlsx"
            files.retrieveObjects()[2].name == "applications.xml"

        and: 'It can read all CSV files'
            files.retrieveObjects(mask: "(?i).*[.]CSV").size() == 1

        and: 'It can read all Excel files'
            files.retrieveObjects(mask: "(?i).*[.]xlsx").size() == 1

        and: 'It can read subdirectories'
            files.retrieveObjects(type: "DIR").size() == 0

        and: 'It can read files from parent directory'
            files.retrieveObjects(directory: "..").size() == 3

    }

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

        then: 'It can read rows count'
            csvFile.readRowCount() == 3

        and: 'First row results contains fields values'
            csvFile.rows()[0].id == 114054
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
            fields[0].name = 'id'
            fields[1].name = 'name'
            fields[2].name = 'description'
            fields[3].name = 'environment'
            fields[4].name = 'modified date'

        and: 'All CSV field are Type of Field.Type.STRING'
            fields[0].type == Field.Type.STRING
            fields[1].type == Field.Type.STRING
            fields[2].type == Field.Type.STRING
            fields[3].type == Field.Type.STRING
            fields[4].type == Field.Type.STRING

        and: 'First row results contains fields values'
            dataset.rows()[0].id == "114054"
            dataset.rows()[0].name == "BlackBerry Enterprise Server"
            dataset.rows()[0].description == "Email sync to Blackberry handhelds"
            dataset.rows()[0].environment == "Production"
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

    void 'test can read an excel file without defining fields' () {

        given:
            String fileName = "applications.xlsx"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            ExcelConnection excelConnection = new ExcelConnection(path: pathName, fileName: fileName)

        when:
            ExcelDataset excelDataset = new ExcelDataset(
                    connection: excelConnection,
                    listName: "applications",
                    showWarnings: true)

        then: 'It can read rows count'
            excelDataset.rows().size() == 3
    }

}
