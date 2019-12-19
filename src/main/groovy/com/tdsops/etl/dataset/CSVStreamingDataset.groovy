package com.tdsops.etl.dataset

import com.tdsops.etl.Column
import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.apache.commons.io.LineIterator

import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream

@CompileStatic
class CSVStreamingDataset implements ETLDataset {

    private static final String CSV_DELIMITER = ','

    String filename
    Long amountOfRows
    LineIterator lineIterator
    List<String> columnNames

    CSVStreamingDataset(String filename) {
        this.filename = filename
        Stream<String> stream = Files.lines(Paths.get(this.filename))
        this.amountOfRows = stream.count()
        lineIterator = FileUtils.lineIterator(new File(this.filename), "UTF-8")
    }

    /**
     *
     * @return
     */
    String filename() {
        return this.filename
    }

    Long rowsSize() {
        return amountOfRows
    }

    Integer columnsSize() {
        return columnNames.size()
    }

    void skip(Integer amountOfLines) {
        amountOfLines.times {
            lineIterator.next()
        }
    }

    @Override
    List<Column> readColumns() {
        columnNames = lineIterator.nextLine().split(CSV_DELIMITER) as List

        return this.columnNames.withIndex().collect { String columnName, Integer index ->
            new Column(columnName.trim(), index)
        }
    }

    ETLIterator iterator() {
        return new CSVStreamingIterator(lineIterator)
    }

    class CSVStreamingIterator implements ETLIterator {

        LineIterator lineIterator

        CSVStreamingIterator(LineIterator lineIterator) {
            this.lineIterator = lineIterator
        }

        @Override
        boolean hasNext() {
            return lineIterator.hasNext()
        }

        @Override
        Map<String, ?> next() {
            List<String> rowValues = lineIterator.next()?.split(CSV_DELIMITER) as List
            //TODO: dcorrea. Add logic for CSV with incorrect amount of columns
            Map<String, ?> row = [:]
            columnNames.size().times {
                row[columnNames.get(it)] = rowValues.get(it)
            }
            return row
        }

        @Override
        void close() {
            lineIterator.close()
        }
    }
}
