package com.tdsops.etl.dataset

import com.tdsops.etl.Column

import java.nio.file.Path
import java.util.stream.Stream

class CSVStreamingDataset implements ETLDataset, Iterator<ETLRow> {

    String filename

    CSVStreamingDataset(String filename) {
        this.filename = filename
    }

    @Override
    List<Column> readColumns() {

        return null
    }

    @Override
    boolean hasNext() {
        return false
    }

    @Override
    ETLRow next() {
        return null
    }
}
