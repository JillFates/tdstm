package com.tdsops.etl.etlmap

import com.tdsops.etl.ETLDomain
import groovy.transform.CompileStatic

@CompileStatic
class ETLMap {
    // The name of the domain class that the map was defined for
    ETLDomain domain
    List<ETLMapInstruction> instructions = []

    ETLMap(ETLDomain domain) {
        this.domain = domain
    }
}
