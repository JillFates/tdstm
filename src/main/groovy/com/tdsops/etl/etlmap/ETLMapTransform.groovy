package com.tdsops.etl.etlmap

import groovy.transform.CompileStatic

@CompileStatic
class ETLMapTransform {
    String methodName
    Object parameters

    ETLMapTransform(String methodName, Object parameters) {
        this.methodName = methodName
        this.parameters = parameters
    }
}
