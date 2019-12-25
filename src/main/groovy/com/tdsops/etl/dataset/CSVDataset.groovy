package com.tdsops.etl.dataset

import com.tdsops.etl.Column
import groovy.transform.CompileStatic
import org.apache.commons.io.FileUtils
import org.apache.commons.io.LineIterator

import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Stream

/**
 * <p>Implementation of {@code ETLDataset} interface.
 * It is in charged to manage CSV files in an ETL execution, in {@code ETLProcessor#dataset} field.</p>
 * <p>A typically startup configuration should be:</p>
 * <pre>
 *  String fileName = createCSVFIle("""
 *      UNNECESSARY HEADER
 * 	    name,cpu,description
 * 		xraysrv01,100,Description FOOBAR
 * 		zuludb01,10,Some description
 * 	""")
 *
 *  ETLProcessor etlProcessor = new ETLProcessor(
 *      GMDEMO,
 *      new CSVDataset(fileName),
 *      debugConsole,
 *      validator
 *  )
 *
 *  etlProcessor.evaluate("""
 *      skip 1
 * 	    domain Device
 * 	    read labels
 * 	    iterate
 * 	        ...
 * 	        extract 'name' load 'assetName'
 *          ...
 *
 * """)
 * </pre>
 * <p>Internally, an instance of {@code CSVDataset} can split a CSV file using {@code CSVDataset#CSV_DELIMITER.}</p>
 *
 * @see com.tdsops.etl.ETLProcessor#skip(java.lang.Integer)
 * @see com.tdsops.etl.ETLProcessor#read(com.tdsops.etl.ETLProcessor.ReservedWord)
 * @see com.tdsops.etl.ETLProcessor#iterate(groovy.lang.Closure)
 */
@CompileStatic
class CSVDataset implements ETLDataset {

    /**
     * Fixed defined splitter character for CSV dataset
     */
    private static final String CSV_DELIMITER = ','
    /**
     * Original filename with the full path to open it using an instance of {@code File}
     */
    String filename
    /**
     * Total amount of rows for {@code filename} field
     */
    Long amountOfRows
    /**
     * Total amount of rows skipped, including columns read.
     */
    Integer skippedRows = 0
    /**
     * Internal iterator class used to read a file using Streaming solution.
     */
    LineIterator lineIterator
    /**
     * Column names created after using {@code CSVDataset#readColumns} method.
     */
    List<String> columnNames

    CSVDataset(String filename) {
        this.filename = filename
        Stream<String> stream = Files.lines(Paths.get(this.filename))
        this.amountOfRows = stream.count()
        lineIterator = FileUtils.lineIterator(new File(this.filename), "UTF-8")
    }

    /**
     * @inheritDoc
     */
    String filename() {
        return this.filename
    }

    /**
     * @inheritDoc
     */
    Long rowsSize() {
        return amountOfRows - skippedRows
    }

    /**
     * @inheritDoc
     */
    void skip(Integer amountOfLines) {
        amountOfLines.times {
            lineIterator.next()
            skippedRows++
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    List<Column> readColumns() {
        columnNames = lineIterator.nextLine().split(CSV_DELIMITER) as List
        skippedRows++
        return this.columnNames.withIndex().collect { String columnName, Integer index ->
            new Column(columnName.trim(), index)
        }
    }
    /**
     * @inheritDoc
     *
     * <p>In order to ieterate more than once, an instance of {@code ETLIterator} collects skipped rows during the initialization</p>.<BR>
     * <p>After the first iterations, all the other iterations are going to use same rows were used in the first iteration.</p>
     * <pre>
     *     skip 2
     *     read labels
     *
     *     iterate { // Skipped rows until first iterate is 3
     *      ...
     *     ...
     *     iterate { // Second iterate is going to start on the third row.
     *      ...
     *
     * </pre>
     */
    ETLIterator iterator() {

        ETLIterator iterator

        if (lineIterator == null) {
            lineIterator = FileUtils.lineIterator(new File(this.filename), "UTF-8")
            skip(this.skippedRows)
        }

        iterator = new CSVStreamingIterator(lineIterator)
        this.lineIterator = null
        return iterator
    }

    class CSVStreamingIterator implements ETLIterator {

        LineIterator iterator

        CSVStreamingIterator(LineIterator lineIterator) {
            this.iterator = lineIterator
        }

        /**
         * @inheritDoc
         */
        @Override
        boolean hasNext() {
            return iterator.hasNext()
        }

        /**
         * @inheritDoc
         */
        @Override
        List<String> next() {
            List<String> rowValues = iterator.next()?.split(CSV_DELIMITER, -1) as List
            return rowValues
        }

        @Override
        void close() {
            iterator.close()
        }
    }
}
