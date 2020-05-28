package com.tdsops.event

import net.transitionmanager.imports.DataScript
import net.transitionmanager.person.Person

/**
 * If there is an error in the transformation step for ETL import process, you can use the following:
 * <pre>
 *  new ETLTransformImportFailureEvent.Builder('Transformation')
 * 				.withPerson(whom)
 * 				.withDataScript(dataScript)
 * 				.withGroupGuid(groupGuid)
 * 				.withErrorMessage(errorMessage)
 * 				.build()
 * </pre>
 *
 * If there is an error in the import step for ETL import process, you can use the following:
 * <pre>
 *  ETLTransformImportFailureEvent.importFailureFor(whom, dataScript, errorMessage)
 * </pre>
 * @see net.transitionmanager.imports.DataImportService#loadETLJsonIntoImportBatch(net.transitionmanager.project.Project, net.transitionmanager.security.UserLogin, java.lang.String, java.lang.Long)
 * @see net.transitionmanager.imports.DataTransformService#sendEmailForETLTransformationFailure(net.transitionmanager.person.Person, net.transitionmanager.imports.DataScript, java.lang.String)
 */
class ETLTransformImportFailureEvent implements Serializable {

    /**
     * static String definition for an Event name
     */
    static final String EVENT_NAME = 'ETL_TRANSFORM_IMPORT_FAILURE'

    Long personId
    Long dataScriptId
    String failureDuring
    String errorMessage
    String groupGuid

    protected ETLTransformImportFailureEvent(){
    }

    @Override
    String toString() {
        return "PersonId:$personId, DataScriptId:$dataScriptId,failureDuring:$failureDuring,groupGuid:$groupGuid,errorMessage:$errorMessage"
    }

    static class Builder {

        ETLTransformImportFailureEvent event

        Builder(String failureDuring) {
            this.event = new ETLTransformImportFailureEvent()
            this.event.failureDuring = failureDuring
        }

        Builder withPerson(Person person) {
            this.event.personId = person.id
            return this
        }

        Builder withDataScript(DataScript dataScript) {
            this.event.dataScriptId = dataScript.id
            return this
        }

        Builder withErrorMessage(String errorMessage) {
            this.event.errorMessage = errorMessage
            return this
        }

        Builder withGroupGuid(String groupGuid) {
            this.event.groupGuid = groupGuid
            return this
        }

        ETLTransformImportFailureEvent build() {
            return event
        }
    }
}
