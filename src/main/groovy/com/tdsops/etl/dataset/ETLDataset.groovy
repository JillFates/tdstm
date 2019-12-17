package com.tdsops.etl.dataset

import com.tdsops.etl.Column

interface ETLDataset {


    List<Column> readColumns()
}