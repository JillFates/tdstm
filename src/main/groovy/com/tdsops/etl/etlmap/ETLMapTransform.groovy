package com.tdsops.etl.etlmap

import groovy.transform.CompileStatic

@CompileStatic
class ETLMapTransform {
    String methodName
    List<Object> parameters

    ETLMapTransform(String methodName, List<Object> parameters = []) {
        this.methodName = methodName
        this.parameters = parameters
    }
}
