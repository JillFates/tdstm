package com.tdsops.etl

class StringTransformation {

    Closure<String> closure

    def apply(String value){
        closure(value)
    }
}
