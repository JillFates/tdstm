package com.tdsops.etl

/**
 * Defines a Row Facade used in commands like the following:
 * <pre>
 * 		find Application 'for' id by id with SOURCE.'application id'
 * </pre>
 * <p>Source application will recover the Dataset 'application id' over each row</p>
 *
 *
 */
class DataSetRowFacade {

    private Map row = [:]

    DataSetRowFacade(Map row) {
        this.row = row
    }

    Object getProperty(String name) {
        // TODO - remove toLowerCase once GETL library is fixed - see TM-9268
        if (!row.containsKey(name)) {
            throw ETLProcessorException.unknownDataSetProperty(name)
        }
        return new SourceField(row[name])
    }
    /**
     * Checks if Dataset contains a particular column name.
     * @param name a column name
     * @return true if {@code DataSetRowFacade#row} contains a key with name parameter
     * 			otherwise it returns false
     */
    boolean containsKey(String name) {
        return row.containsKey(name)
    }

    @Override
    String toString() {
        return """SOURCE {
			row=${row}
		}"""
    }
}
