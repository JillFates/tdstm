package com.tdsops.etl

abstract class ETLProcessorBaseScript extends Script {

     @Delegate @Lazy ETLProcessor etlProcessor = this.binding.etlProcessor

    def methodMissing(String methodName, args) {
        etlProcessor."${methodName}"(*args)
    }
}
