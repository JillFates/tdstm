package com.tdsops.etl.dataset

import com.tdsops.etl.Column

/**
 * Interface to define all the necessary methods to interact with an instance of {@code ETLProcessor}
 * <p>There are basically, 3 steps in order to manage a ETL dataset inside an instance of {@code ETLProcessor}</p>
 * <p>1) It should be called first with skipped amount of rows:</p>
 * <pre>
 *  ...
 *  dataset.skip(1)
 *  ...
 * </pre>
 * <p>2) Once all the necessary rows were skipped, it can read labels</p>
 * <pre>
 *  ...
 *  List<Column> columns = dataset.readColumns()
 *  ...
 * </pre>
 * <p>3) Finally, once skipped and columns were defined, an instance of {@code CSVDataset} can be used for iterating all the rows:</p>
 * <pre>
 *  ...
 *  ETLIterator iterator = this.dataset.iterator()\
 *  while (iterator.hasNext())
 *      ...
 *      Map<String, ?> row = iterator.next()
 *      ...
 * </pre>
 *
 * @see com.tdsops.etl.ETLProcessor#skip(java.lang.Integer)
 * @see com.tdsops.etl.ETLProcessor#read(com.tdsops.etl.ETLProcessor.ReservedWord)
 * @see com.tdsops.etl.ETLProcessor#iterate(groovy.lang.Closure)
 */
interface ETLDataset {

    /**
     * File name (full path) used to create an instance of {@code CSVDataset}
     *
     * @return a String value
     */
    String filename()
    /**
     * Returns the total amount of rows to be processed in an ETL Script after skip and read column were invoked.
     *
     * @return total amount of rows
     */
    Long rowsSize()

    /**
     * Reading an {@code ETLDataset}, user ca skip unnecessary rows until read columns.
     *
     * @param amountOfLines an integer with the amount of rows to be skipped.
     */
    void skip(Integer amountOfLines)

    /**
     * Creates a List of {@code Column} based on the current line during an ETL Script execution.
     * Each instance of {@code Column} contains 'label' value as a column name.
     *
     * @return a List of {@code Column}
     */
    List<Column> readColumns()
    /**
     * Returns an instance of {@code ETLIterator} used at the moment of iterate all the rows for a Dataset.
     *
     * @return an instance of {@code ETLIterator}
     */
    ETLIterator iterator()

    /**
     * Given a List of values for row values, it creates a map with it.
     * <pre>
     *    // given:
     *    dataset.readColumns()*.label == ['application name', 'application vendor']
     *    List<String> rowValues = ['Zulu 01', 'ACME']
     *    // then:
     *    dataset.convertRowValuesToMap(rowValues) == [
     *          ['application name': 'Zulu 01'],
     *          ['application vendor': 'ACME'],
     *    ]
     * </pre>
     * @param List of String with row values
     *
     * @return a Map with column name as a key and row value as a Map value
     */
    Map<String, ?> convertRowValuesToMap(List<String> rowValues)
}

/**
 * Defines an instance of an {@code Iterator} used internally in an {@code ETLProcessor}
 * during the iterate command.
 *
 * @see com.tdsops.etl.ETLProcessor#iterate(groovy.lang.Closure)
 */
interface ETLIterator extends Iterator<List<String>> {

    /**
     * Close method is used to close the Streaming solution behind each implementation of {@code ETLDataset}
     */
    void close()
}