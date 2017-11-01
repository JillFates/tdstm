package com.tdsops.etl

class Replacer extends Expando {

    Element element
    Closure applier

    /**
     *
     *
     * @param methodName
     * @param args
     */
    def methodMissing (String methodName, args) {
        if (methodName in ['first', 'last']) {
            this."$methodName"(args)
        }
    }

    def propertyMissing (String name) {
        if (name in ['lowercase', 'uppercase', 'trim']) {
            this."$name"()
        }
    }


    def replace(Element element){

    }




}
