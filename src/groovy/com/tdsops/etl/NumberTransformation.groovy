package com.tdsops.etl

class NumberTransformation {

    Closure<Number> closure

    String apply(Number value){
        closure(value)
    }
}
