package net.transitionmanager.imports

import com.tdsops.etl.ProgressCallback
import net.transitionmanager.common.ProgressService

/**
 * <p>Calculator of the progress in an ETL Import process. It is used inside
 * {@link DataImportService#loadETLJsonIntoImportBatch(net.transitionmanager.project.Project, net.transitionmanager.security.UserLogin, java.lang.String)}
 *
 * <p>It needs an instance of {@link ProgressService} with the necessary progressKey.</p>
 * <pre>
 *   ImportProgressCalculator progressCalculator = new ImportProgressCalculator(progressKey, totalAmountOfRows, progressService)
 * </pre>
 */
class ImportProgressCalculator {

    String progressKey
    Integer totalRows
    /**
     * Used to determine after n rows actually report progress for current iterate loop
     */
    Integer factorStepFrequency = 0
    /**
     * Used to track # of rows processed in a iterate loop before actually reporting progress, -1 signifies first row in iterate
     */
    Integer frequencyCounter = 0

    Integer rowsProcessed = 0

    Integer progress = 0

    ProgressService progressService

    ImportProgressCalculator(String progressKey, Integer totalRows, ProgressService progressService) {
        this.progressKey = progressKey
        this.totalRows = totalRows
        this.progressService = progressService
        this.factorStepFrequency = totalRows / 100
    }
    /**
     * Starts progressKey with status 'RUNNING'
     */
    void start() {
        progressService.update(progressKey,
                0,
                ProgressCallback.ProgressStatus.RUNNING.name(),
                ''
        )
    }

    /**
     * Increase on row processed in an ETL import process, based on {@link ImportProgressCalculator#factorStepFrequency}.
     */
    void increase() {

        if (frequencyCounter++ == factorStepFrequency) {

            progressService.update(progressKey,
                    progress,
                    ProgressCallback.ProgressStatus.RUNNING.name(),
                    "TotalRows:$totalRows. RowsProcessed:$rowsProcessed. Progress:${progress++}%"
            )
            frequencyCounter = 0
        }
        rowsProcessed++
    }

    /**
     * Starts progressKey with status 'COMPLETED'
     */
    void finish(String groupGuid) {
        progressService.update(progressKey,
                100,
                ProgressCallback.ProgressStatus.COMPLETED.name(),
                "Total amount of rows:$totalRows processed",
                null,
                [groupGuid: groupGuid]
        )
    }

}
