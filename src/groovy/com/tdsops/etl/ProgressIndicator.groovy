package com.tdsops.etl

/**
 * ETLProcessor Reporting Percentage Complete.
 * <p>Perhaps the most complicated will be to determine the % complete.
 * An accurate determination of % complete is impossible because a script can have multiple iterations;
 * can switch between sheets; and not all is known at the start of the process.
 * Therefore the logic is going to perform a simplistic approach which is:</p>
 */
trait ProgressIndicator {

	/**
	 * Interface implementation used to report progress.
	 * It is loaded first in prepareProgressIndicator method
	 * @see ProgressIndicator#prepareProgressIndicator(java.lang.String, com.tdsops.etl.ProgressCallback)
	 * @see ProgressCallback
	 */
	ProgressCallback progressCallback
	/**
	 * The total number of iterations loops used in an ETL Script
	 */
	Integer numberOfIterateLoops
	/**
	 * Counter incremented each iteration complete
	 */
	Integer iterateCounter
	/**
	 * Prepare script progress indicator using a closure
	 * and an ETL script content
	 * @param aCallback callback closure used to report progress
	 * @param script an ETL script content
	 */
	void prepareProgressIndicator(String script, ProgressCallback aProgressCallback) {
		progressCallback = aProgressCallback
		numberOfIterateLoops = calculateNumberOfIterateLoops(script)
		iterateCounter = 0
	}

	/**
	 * To keep the implementation loosely coupled instead of injecting the ProgressService into the ETLProcessor logic,
	 * a closure will be passed in that the ETL code can call in order to report back:
	 * <ul>
	 *     <li>Percentage complete</li>
	 *     <li>Success (with resulting filename)</li>
	 *     <li>Fatal Error</li>
	 * </ul>
	 * @param percentComplete
	 * @param status (RUNNING, COMPLETE, or ERROR)
	 * @param detail (RUNNING: blank, COMPLETE: filename, ERROR: error message)
	 */
	void reportProgress(
		Integer currentRow,
		Integer totalRows,
		Boolean forceReport = false,
		ProgressCallback.ProgressStatus status = ProgressCallback.ProgressStatus.RUNNING,
		String detail = '') {

		if (progressCallback){
			Integer percentage = ((currentRow + totalRows*iterateCounter )/ (totalRows*numberOfIterateLoops)*100).intValue()
			progressCallback.reportProgress(percentage, forceReport, status, detail)
		}
	}

	/**
	 * <ol>
	 * <li>Parse the script to count the # of <em>iterate {...}</em> blocks
	 * <ol>
	 * <li>Retain <em>Integer numberOfIterateLoops</em> to be used by reportProgress function</li>
	 * </ol>
	 * </li>
	 * <li>Each iterate block will responsible for reporting it's percentage of completion
	 * <ul>
	 * <li>For example if there are 4 iterate statements then each is responsible for 25% of the overall % complete.</li>
	 * </ul>
	 * </li>
	 * <li>At the beginning of each iterate - the number of source rows will be determined</li>
	 * <li>A factor will be determined as to what the frequency (every n rows) that percentage complete
	 * should be reported back to the Progress Service</li>
	 * </ol>
	 * @param scriptContent
	 * @return
	 */
	Integer calculateNumberOfIterateLoops(String scriptContent) {
		return scriptContent.count('iterate')
	}

	/**
	 *
	 * @param currentRow
	 * @param totalRows
	 */
	void bottomOfIterate(Integer currentRow, Integer totalRows) {
		reportProgress(currentRow, totalRows, false, ProgressCallback.ProgressStatus.RUNNING, '')
	}

	/**
	 * This method will be a call to reportProgress with the current row # and total rows for the dataset.
	 */
	void finishIterate() {
		iterateCounter += 1
	}

	/**
	 * Reporting Success.
	 *  In this method the following call should be made to indicate to the Progress Service
	 *  that the ETL process has completed
	 * @param filename
	 */
	void scriptStarted(String filename) {
		if (progressCallback){
			progressCallback.reportProgress(0, true, ProgressCallback.ProgressStatus.RUNNING, filename)
		}
	}

	/**
	 * Reporting Success.
	 *  In this method the following call should be made to indicate to the Progress Service
	 *  that the ETL process has completed
	 * @param filename
	 */
	void scriptFinished(String filename) {
		if (progressCallback){
			progressCallback.reportProgress(100, true, ProgressCallback.ProgressStatus.COMPLETED, filename)
		}
	}

	/**
	 * In the event that any exception occures then the progress should be reported with the following call
	 * @param errorMessage
	 */
	void scriptFailed(String errorMessage) {
		reportProgress(1, 1, true, ProgressCallback.ProgressStatus.ERROR, errorMessage)
	}
}

