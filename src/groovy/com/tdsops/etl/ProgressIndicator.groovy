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
	 * It is loaded first in setUpProgressIndicator method
	 * @see ProgressIndicator#setUpProgressIndicator(java.lang.String, com.tdsops.etl.ProgressCallback)
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
	 * Counter incremented each iteration complete
	 */
	Integer iterateCounter

	/**
	 * Prepare script progress indicator using a instance of ProgressCallback
	 * and an ETL script content
	 * @param aCallback callback closure used to report progress
	 * @param script an ETL script content
	 * @see ProgressCallback
	 */
	void setUpProgressIndicator(String script, ProgressCallback aProgressCallback) {
		if(aProgressCallback){
			progressCallback = aProgressCallback
			numberOfIterateLoops = calculateNumberOfIterateLoops(script)
			if (numberOfIterateLoops > 0){
				iterateRatio = 1 / numberOfIterateLoops
			} else {
				iterateRatio = 0
			}
			iterateCounter = 0

			progressCallback.reportProgress(0, true, ProgressCallback.ProgressStatus.RUNNING, '')

		} else {
			numberOfIterateLoops = 0
			factorStepFrequency = 0
			iterateRatio = 0
			frequencyCounter = -1
			iterateCounter = 0
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
		// Match on the following commands with any sort of whitespace:
		//		iterate {...}
		//		iterate
		//		   { ... }
		//		from 1 to 10 iterate {...}
		def matcher = scriptContent =~ /(?m)^((\s*from\s*\d+\s*to\s*\d+\s*|\s*)iterate\s*\{)/
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
				factorStepFrequency = (totalRows / 100).intValue()
			}
			frequencyCounter += 1
			if (factorStepFrequency < frequencyCounter) {
				Integer percentage = (((iterateCounter * iterateRatio) + (currentRow / totalRows * iterateRatio)) * 100).intValue()
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
			iterateCounter += 1
			Integer percentage = Math.round(iterateCounter * iterateRatio * 100)
			progressCallback.reportProgress(percentage, true, ProgressCallback.ProgressStatus.RUNNING, '')
		}
		frequencyCounter = -1
	}
}
