package net.transitionmanager.imports

import com.tdsops.event.ETLTransformImportFailureEvent
import grails.events.annotation.Publisher
import net.transitionmanager.person.Person
import net.transitionmanager.service.ServiceMethods

/**
 * Event notification for Data Import / Data Import process.
 *
 * @see ETLTransformImportFailureEvent
 */
class ETLImportFailureService implements ServiceMethods {

    /**
     * Dispatch an email for ETL transfomration failure cases. It uses Grails events for notifying it.
     *
     * @param whom {@link Person} that send failure message
     * @param dataScript {@link DataScript} that origins import failure
     * @param errorMessage String with error message
     * @return an instance of {@link ETLTransformImportFailureEvent}
     */
    @Publisher(ETLTransformImportFailureEvent.EVENT_NAME)
    ETLTransformImportFailureEvent notifyDataTransformFailure(Person whom, DataScript dataScript, String errorMessage) {
        return new ETLTransformImportFailureEvent.Builder('Transformation')
                .withPerson(whom)
                .withDataScript(dataScript)
                .withErrorMessage(errorMessage)
                .build()
    }

    /**
     * Dispatch an email for ETL import failure cases. It uses Grails events for notifying it.
     *
     * @param whom {@link net.transitionmanager.person.Person} that send failure message
     * @param dataScript {@link DataScript} that origins import failure
     * @param groupGuid String with Group Guid generated for ImportBatches
     * @param errorMessage String with error message
     * @return an instance of {@link com.tdsops.event.ETLTransformImportFailureEvent}
     */
    @Publisher(ETLTransformImportFailureEvent.EVENT_NAME)
    ETLTransformImportFailureEvent notifyDataImportFailure(Person whom, DataScript dataScript, String groupGuid, String errorMessage) {
        return new ETLTransformImportFailureEvent.Builder('Batch Creation process')
                .withPerson(whom)
                .withDataScript(dataScript)
                .withGroupGuid(groupGuid)
                .withErrorMessage(errorMessage)
                .build()
    }
}
