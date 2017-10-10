package com.tdsops.etl

import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.data.Field
import getl.excel.ExcelConnection
import getl.excel.ExcelDataset
import spock.lang.Specification

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

        when: 'CSVDataset has defined field'
            csvFile.field << new Field(name: 'Id', type: Field.Type.BIGINT)
            csvFile.field << new Field(name: 'Name', type: Field.Type.STRING)
            csvFile.field << new Field(name: 'Description', type: Field.Type.STRING)
            csvFile.field << new Field(name: 'Environment', type: Field.Type.STRING)

        then: 'It can read rows count'
            csvFile.readRowCount() == 3

        and: 'First row results contains fields values'
            csvFile.rows()[0].Id == 114054
            csvFile.rows()[0].Name == "BlackBerry Enterprise Server"
            csvFile.rows()[0].Description == "Email sync to Blackberry handhelds"
            csvFile.rows()[0].Environment == "Production"
    }

    void 'test can read a csv file without defining fields' () {

        given:
            String fileName = "applications.csv"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            CSVConnection csvCon = new CSVConnection(config: "csv", path: pathName)

        when: 'CSVDataset can read column names without fields definition'
            CSVDataset csvFile = new CSVDataset(connection: csvCon, fileName: fileName, header: true)

        then: 'It can read total amount of rows'
            csvFile.readRowCount() == 3

        and: 'First row results contains fields values'
            csvFile.rows()[0].id == "114054"
            csvFile.rows()[0].name == "BlackBerry Enterprise Server"
            csvFile.rows()[0].description == "Email sync to Blackberry handhelds"
            csvFile.rows()[0].environment == "Production"
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
            csvFile.rows().collectEntries() == []
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
