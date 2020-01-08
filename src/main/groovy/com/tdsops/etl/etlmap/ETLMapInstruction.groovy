package com.tdsops.etl.etlmap

import com.tdsops.etl.Column
import com.tdsops.etl.ETLFieldDefinition
import groovy.transform.CompileStatic

@CompileStatic
class ETLMapInstruction {

    Column column
    ETLFieldDefinition domainProperty = null
    List<ETLMapTransform> transformations = []
}
