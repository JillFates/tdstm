package com.tdsops.etl

/**
 * Defines a selected ETL domain behaviour.
 * <pre>
 *     domain Application
 * </pre>
 */
class SelectedDomain {

	/**
	 * ETL Domain selected using 'domain' command
	 */
	ETLDomain domain
	/**
	 * Defines if a new row should be created next time ETLProcessor and a result in an ETLProcessResult
	 * @see ETLProcessor#domain(com.tdsops.etl.ETLDomain)
	 * @see ETLProcessorResult#currentRowData()
	 */
	boolean addNewRow

	SelectedDomain(ETLDomain selected){
		this.domain = selected
		this.addNewRow = false
	}
}
