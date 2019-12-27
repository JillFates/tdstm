package com.tdsops.etl.etlmap

import com.tdsops.etl.ETLFieldDefinition
import groovy.transform.CompileStatic

@CompileStatic
class ETLMapInstruction {

    Integer sourcePosition = null
    String sourceName = null
    ETLFieldDefinition domainProperty = null
    List<ETLMapTransform> transformations = []
}
