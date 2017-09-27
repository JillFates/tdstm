package com.tdsops.etl

class StringTransformation {

    Closure<String> closure

    String apply(String value){
        closure(value)
    }
}
