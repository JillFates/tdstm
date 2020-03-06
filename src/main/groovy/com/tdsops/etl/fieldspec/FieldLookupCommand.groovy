package com.tdsops.etl.fieldspec

import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldDefinition
import com.tdsops.etl.ETLProcessor
import com.tdsops.etl.ETLProcessorException
import com.tdsops.etl.LocalVariableDefinition

/**
 * This Class interprets the following ETL command:
 * <pre>
 *  fieldLookup Application with 'TCO - Current Cost' set tcoCurrentCostField
 * </pre>
 *
 */
class FieldLookupCommand {

    ETLDomain domain
    ETLProcessor etlProcessor
    ETLFieldDefinition fieldDefinition

    FieldLookupCommand(ETLDomain domain, ETLProcessor etlProcessor) {
        if (!domain.isAsset()) {
            throw ETLProcessorException.domainWithoutFieldSpec(domain)
        }
        this.domain = domain
        this.etlProcessor = etlProcessor
    }
    /**
     * Lookup part for this command. It can find an instance of {@link ETLFieldDefinition}
     * based on its field label value using {@link ETLProcessor#lookUpFieldDefinition(com.tdsops.etl.ETLDomain, java.lang.String)}
     * If there is not a field label defined it throws a {@link com.tdsops.etl.ETLProcessorException}.
     *
     * @param labelName a String field label value
     * @return current instance of {@link FieldLookupCommand}
     * @see com.tdsops.etl.ETLFieldsValidator#lookup(com.tdsops.etl.ETLDomain, java.lang.String)
     */
    FieldLookupCommand with(String labelName) {
        fieldDefinition = etlProcessor.lookUpFieldDefinition(domain, labelName)
        return this
    }

    /**
     *
     * @param localVariable
     */
    void set(LocalVariableDefinition localVariable) {
        etlProcessor.addLocalVariableInBinding(localVariable.name, fieldDefinition.name)
    }

}
