package com.tdsops.etl

/**
 * ETLProcessor Reporting Percentage Complete.
 * <p>Perhaps the most complicated will be to determine the % complete.
 * An accurate determination of % complete is impossible because a script can have multiple iterations;
 * can switch between sheets; and not all is known at the start of the process.
 * Therefore the logic is going to perform a simplistic approach</p>
 * <br>
 * <pre>
 *   new ProgressCallback(){
 *      @Override
 *      void reportProgress(
 *          Integer progress,
 *          Boolean forceReport, ProgressCallback.ProgressStatus status,
 *          String detail) {
 *
 *          }
 *   }
 * </pre>
 * @see ProgressCallback.ProgressStatus
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
	 * Used to determine after n rows actually report progress for current iterate loop
	 */
	Integer factorStepFrequency = 0

	/**
	 * Will contain the percentage of overall % complete for each iterate loop
	 */
	BigDecimal iterateRatio

	/**
	 * Used to track # of rows processed in a iterate loop before actually reporting progress, -1 signifies first row in iterate
	 */
	Integer frequencyCounter = -1

	/**
	 * Prepare script progress indicator using a instance of ProgressCallback
	 * and an ETL script content
	 * @param aCallback callback closure used to report progress
	 * @param script an ETL script content
	 * @see ProgressCallback
	 */
	void prepareProgressIndicator(String script, ProgressCallback aProgressCallback) {
		progressCallback = aProgressCallback
		numberOfIterateLoops = calculateNumberOfIterateLoops(script)
		if(numberOfIterateLoops > 0){
			iterateRatio = 1 / numberOfIterateLoops
		} else {
			iterateRatio = 0
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
		def matcher = scriptContent =~ /(?m)^\s*(iterate\s*\{)/
		return matcher.getCount()
	}

	/**
	 * Used to report row level progress back to progress service
	 * @param currentRow
	 * @param totalRows - number of rows in the dataSet for the current iterate loop
	 */
	void reportRowProgress(Integer currentRow, Integer totalRows) {
		if (progressCallback) {

			if (frequencyCounter == -1) {
				frequencyCounter = 0
				// Calculate how many rows to process before actually reporting the progress back for the current iterate loop
				factorStepFrequency = (totalRows / 100).intValue()
			}
			frequencyCounter += 1
			if (factorStepFrequency < frequencyCounter) {
				// Integer percentage = ((currentRow + totalRows*iterateCounter )/ (totalRows*numberOfIterateLoops)*100).intValue()
				Integer percentage = (((frequencyCounter * iterateRatio) + (currentRow / totalRows / iterateRatio)) * 100).intValue()
				progressCallback.reportProgress(percentage, false, ProgressCallback.ProgressStatus.RUNNING, '')
				frequencyCounter = 0
			}
		}
	}

	/**
	 * This method will be a call to reportProgress with the current row # and total rows for the dataset.
	 */
	void finishIterate() {
		if (progressCallback){
			Integer percentage = Math.round(frequencyCounter * iterateRatio * 100)
			progressCallback.reportProgress(percentage, true, ProgressCallback.ProgressStatus.RUNNING, '')
		}
		frequencyCounter = -1
	}

	/**
	 * Reporting Success.
	 *  In this method the following call should be made to indicate to the Progress Service
	 *  that the ETL process has completed
	 * @param filename
	 */
	void scriptStarted() {
		if (progressCallback){
			progressCallback.reportProgress(0, true, ProgressCallback.ProgressStatus.RUNNING, '')
		}
	}
}
