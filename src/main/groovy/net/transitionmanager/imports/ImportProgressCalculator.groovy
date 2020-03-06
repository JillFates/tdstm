package net.transitionmanager.imports

import com.tdsops.etl.ProgressCallback
import net.transitionmanager.common.ProgressService

/**
 * <p>Calculator of the progress in an ETL Import process. It is used inside
 * {@link DataImportService#loadETLResultsIntoAutoProcessImportBatch(net.transitionmanager.project.Project, net.transitionmanager.security.UserLogin, com.tdsops.etl.ETLProcessorResult, java.lang.Boolean)}</p>
 *
 * <p>It needs an instance of {@link ProgressService} with the necessary progressKey.</p>
 * <pre>
 *   ImportProgressCalculator progressCalculator = new ImportProgressCalculator(progressKey, totalAmountOfRows, progressService)
 * </pre>
 */
class ImportProgressCalculator {

    String progressKey
    Integer totalRows
    ProgressService progressService
    Integer rowsProcessed = 0

    ImportProgressCalculator(String progressKey, Integer totalRows, ProgressService progressService) {
        this.progressKey = progressKey
        this.totalRows = totalRows
        this.progressService = progressService
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
     * Increase on row processed in an ETL import process.
     */
    void increase() {
        Integer progress = (++rowsProcessed / totalRows * 100).intValue()
        progressService.update(progressKey,
                progress,
                ProgressCallback.ProgressStatus.RUNNING.name(),
                ''
        )
    }

    /**
     * Starts progressKey with status 'COMPLETED'
     */
    void finish() {
        progressService.update(progressKey,
                100,
                ProgressCallback.ProgressStatus.COMPLETED.name(),
                "Total amount of rows:$totalRows".toString()
        )
    }

}
