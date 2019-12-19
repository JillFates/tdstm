package com.tdsops.etl.dataset

import com.tdsops.etl.Column

/**
 * Interface to define all the necessary methods to interact with an instance of {@code ETLProcessor}
 *
 * @see com.tdsops.etl.ETLProcessor#iterate(groovy.lang.Closure)
 * @see com.tdsops.etl.ETLProcessor#read(com.tdsops.etl.ETLProcessor.ReservedWord)
 */
interface ETLDataset {

    String filename()

    Long rowsSize()

    Integer columnsSize()

    void skip(Integer amountOfLines)

    List<Column> readColumns()

    ETLIterator iterator()
}

interface ETLIterator extends Iterator<Map<String, ?>> {

    void close()
}