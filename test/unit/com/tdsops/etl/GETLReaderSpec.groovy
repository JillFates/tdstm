package com.tdsops.etl

import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.data.Field
import spock.lang.Specification

class GETLReaderSpec extends Specification {

    void 'test can read csv file defining fields' () {

        given:
            String fileName = "applications.csv"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            CSVConnection csvCon = new CSVConnection(config: "csv", path: pathName)
            CSVDataset csvFile = new CSVDataset(connection: csvCon, fileName: fileName, header: true)

        when: 'CSVDataset has defined field'
            csvFile.field << new Field(name: 'Id', type: Field.Type.BIGINT)
            csvFile.field << new Field(name: 'Name', type: Field.Type.STRING)
            csvFile.field << new Field(name: 'Description', type: Field.Type.STRING)
            csvFile.field << new Field(name: 'Environment', type: Field.Type.STRING)

        then: 'It can read rows count'
            csvFile.readRowCount() == 3
    }

    void 'test can read csv file without defining fields' () {

        given:
            String fileName = "applications.csv"
            String pathName = "test/unit/com/tdsops/etl/resources"

        and:
            CSVConnection csvCon = new CSVConnection(config: "csv", path: pathName)

        when: 'CSVDataset has defined field'
            CSVDataset csvFile = new CSVDataset(connection: csvCon, fileName: fileName, header: true)

        then: 'It can read rows count'
            csvFile.readRowCount() == 3
    }

}
