package com.tdsops.etl

/**
 * Interface to inform the percentage of progress in an ETL execution script.
 *
 */
interface ProgressCallback {

	enum ProgressStatus{
		RUNNING, COMPLETED, ERROR
	}

	/**
	 * Reports the percentage of execution in an ETL script.
	 * It also notifies the status of the progress with a detail,
	 * and if report must be reported to the user.
	 * @param progress an integer value between 0 and 100 that represents the percentage of progress
	 * @param forceReport
	 * @param status Status of the report. It could be RUNNING, COMPLETE, or ERROR
	 * @param detail
	 */
	void reportProgress(Integer progress, Boolean forceReport, ProgressStatus status, String detail)
}
