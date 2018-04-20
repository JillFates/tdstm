package com.tdsops.etl

import getl.data.Dataset
import getl.data.Field
import getl.excel.ExcelDataset

class DataSetFacade {

	private Dataset dataSet

	DataSetFacade(Dataset dataSet) {
		this.dataSet = dataSet
	}

	/**
	 * Return rows from dataset
	 * @return
	 */
	List<Map> rows() {
		return dataSet.rows()
	}

	/**
	 * Count reading rows from dataset
	 */
	Long rowsSize() {
		return dataSet.rows().size()
	}

	/**
	 * Returns a lists with GETL fields associated to a GETL Dataset instance
	 * @return a list of GETL fields
	 */
	List<Field> fields() {
		return dataSet.connection.driver.fields(dataSet)
	}

	/**
	 * Returns the original file name from dataset instance.
	 * @return a String value with the filename
	 */
	String fileName(){
		return dataSet.params.fileName
	}

	Dataset getDataSet() {
		return dataSet
	}

	/**
	 * Set the sheet name in a TDSExcelDriver.
	 * If workbook doesn't contains that sheet name it throws an ETLProcessorException
	 * @param sheetName an string to be used in setting list name
	 */
	void setSheetName(String sheetName) {
		validateSheetName(sheetName)
		((ExcelDataset)dataSet).setListName(sheetName)
	}

	/**
	 * Set the sheet number in a TDSExcelDriver.
	 * If workbook doesn't contains a sheet in an ordinal position it throws an ETLProcessorException
	 * @param sheetNumber an integer to be used in setting list name
	 */
	void setSheetNumber(Integer sheetNumber) {
		validateSheetNumber(sheetNumber)
		((ExcelDataset)dataSet).params.listName = sheetNumber
	}

	/**
	 * Set the current row index in the params of the DataSet instance.
	 * It will use to read labels or reading rows content
	 * @param currentRowIndex
	 */
	void setCurrentRowIndex(int currentRowIndex) {
		dataSet.params.currentRowIndex = currentRowIndex
	}

	/**
	 * Validates if sheet name is correct.
	 * It checks:
	 * 1) if DataSetFacade was configured correctly with an instance of ExcelDataset
	 * 2) If the Workbook in the Excel Driver contains a sheet with sheetName parameter
	 * @param sheetName a string with the sheet name
	 */
	private void validateSheetName(String sheetName) {
		if(!dataSet.class.isAssignableFrom(ExcelDataset)){
			throw ETLProcessorException.invalidSheetCommand()
		}

		TDSExcelDriver excelDriver = (TDSExcelDriver)dataSet.connection.driver

		boolean hasSheet
		try{
			hasSheet = excelDriver.hasSheet(dataSet, sheetName)
		} catch(all){
			throw ETLProcessorException.invalidSheetName(sheetName)
		}

		if(!hasSheet){
			throw ETLProcessorException.invalidSheetName(sheetName)
		}
	}

	/**
	 * Validates if sheet number is correct.
	 * It checks:
	 * 1) if DataSetFacade was configured correctly with an instance of ExcelDataset
	 * 2) If the Workbook in the Excel Driver contains a sheet in the position
	 * of the ordinal sheet number parameter
	 * @param sheetNumber a integer with an ordinal sheet number
	 */
	private void validateSheetNumber(int sheetNumber) {
		if(!dataSet.class.isAssignableFrom(ExcelDataset)){
			throw ETLProcessorException.invalidSheetCommand()
		}

		TDSExcelDriver excelDriver = (TDSExcelDriver)dataSet.connection.driver
		boolean hasSheet
		try{
			hasSheet = excelDriver.hasSheet(dataSet, sheetNumber)
		} catch(all){
			throw ETLProcessorException.invalidSheetNumber(sheetNumber)
		}

		if(!hasSheet){
			throw ETLProcessorException.invalidSheetNumber(sheetNumber)
		}
	}

	/**
	 * If the Dataset is an ExcelDataSet, it takes the sheet names from its TDSExcelDriver instances.
	 * @return a list of sheet names
	 */
	List<String> getSheetNames() {
		TDSExcelDriver excelDriver = excelDriver()
		return excelDriver.sheetNames
	}

	/**
	 * Validates if the current DataSet is an instance of ExcelDataSet
	 * and if it using an instance of TDSExcelDriver.
	 * If not it throws an ETLProcessorException#invalidExcelDriver exception
	 * @return the instance of TDSExcelDriver configured for the current DataSet.
	 */
	private TDSExcelDriver excelDriver(){
		if(!dataSet.class.isAssignableFrom(ExcelDataset)){
			throw ETLProcessorException.invalidExcelDriver()
		}
		return (TDSExcelDriver)dataSet.connection.driver
	}
}
