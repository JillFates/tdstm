package com.tdsops.etl.etlmap

import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException

/**
 * The intent of this class is to define an intermediate step in ETL map definition.
 * {@code ETLProcessor#defineMap} command uses this class.
 *
 * @see DefineETLMapCommand#methodMissing(java.lang.String, java.lang.Object)
 */
class DefineETLMapCommand {

    private ETLDomain domain
    private ETLProcessor processor

    DefineETLMapCommand(ETLDomain domain, ETLProcessor processor) {
        this.domain = domain
        this.processor = processor
    }

    /**
     * Method missing overrided to detect the second part of
     * the following ETL Command:
     * <pre>
     *  defineMap Device 'verni-devices' { //
     *      ...
     * </pre>
     * Once second part is invoked ('verni-devices' { ...} ),
     * this method creates a new instance of {@code ETLMapBuilder}
     * to build a new instance of {@code ETLMap}
     * @param name
     * @param args
     * @return an instance of {@code ETLProcessor} defined in constructor
     */
    def methodMissing(String name, def args) {
        if (args?.size() != 1) {
            throw ETLProcessorException.invalidAmountOfArguments()
        }
        if (!(args[0] instanceof Closure)) {
            throw ETLProcessorException.invalidArgument()
        }
        ETLMap etlMap = new ETLMapBuilder(this.domain, this.processor).build(args[0])
        this.processor.addETLMap(name, etlMap)
        return this.processor
    }
}
