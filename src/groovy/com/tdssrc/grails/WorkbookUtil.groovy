package com.tdssrc.grails

import org.apache.commons.logging.LogFactory
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFDataValidation

import java.text.DateFormat
/**
 * The WorkbookUtil class contains a collection of useful Apache POI manipulation methods
 */
@Singleton
class WorkbookUtil {
	private static log = LogFactory.getLog(WorkbookUtil.class)

	WorkbookUtil() {
		log = LogFactory.getLog(this.class)
	}

	public static getSheetNames(workbook) {
		def result = []
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			result << workbook.getSheetName(i);	
		}
		return result;
	}

	public static addCell(sheet, columnIdx, rowIdx, value, type=null) {
		//println "columnIdx=$columnIdx, rowIdx=$rowIdx, value=$value, rowIdx isa ${rowIdx.getClass().getName()}, sheet isa ${sheet.getClass().getName()}"
		def row = sheet.getRow((int)rowIdx)
		if (!row) {
			row = sheet.createRow((int)rowIdx)
		}
		def cell = row.getCell(columnIdx)
		if (!cell) {
			cell = row.createCell(columnIdx)
		}
		if (type) {
			cell.setCellType(type)
		}
		cell.setCellValue(value)
	}

	public static getColumnsCount(sheet) {
		def result = 0
		def row = sheet.getRow(0)
		if (row) {
			def value
			def c = row.getLastCellNum()
			while (c >= 0) { 
				value = getStringCellValue(sheet, c, 0)
				if (!StringUtil.isBlank(value)) {
					result = c + 1;
					break
				}
				c--
			}
		}
		return result
	}

	public static getCell(sheet, columnIdx, rowIdx) {
		def result = null
		def row = sheet.getRow(rowIdx)
		if (row) {
			result = row.getCell(columnIdx)
		}
		return result
	}

	/**
	 * Used to read a date value from a cell in a spreadsheet using a DateFormat formatter which will use the 
	 * user's currently configured timezone to read the values as string.
	 *
	 * @param sheet - the sheet to extract the value
	 * @param columnIdx - the column to reference (offset starts at zero)
	 * @param rowIdx - the row to reference (offset start at zero)
	 * @param session - the HttpSession for the user
	 * @param dateFormat - list if formats to use if parsing a string value
	 * @return The date from the specified cell or null if empty
	 * @throws IllegalArgumentException - if field does not contain String or Numeric (date) format
	 * @throws java.text.ParseException - if the field contains an invalid formatted String value
	 * @deprecated Please use getDateCellValue(Sheet sheet, Integer columnIdx, Integer rowIdx, DateFormat dateFormat, failedIndicator=-1) 
	 */
	public static getDateCellValue(Sheet sheet, Integer columnIdx, Integer rowIdx, session, Collection formatterTypes=null) {
		Date result
		Cell cell = getCell(sheet, columnIdx, rowIdx)

		if (!formatterTypes) formatterTypes = [TimeUtil.FORMAT_DATE_TIME_22]

		if (cell) {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_NUMERIC:
					// Dates stored in the spreadsheet are done so in GMT so we shouldn't need to convert it.
					result = cell.getDateCellValue()					
					// result = TimeUtil.moveDateToTZ(result, session)
					break
				case Cell.CELL_TYPE_STRING:
					String cellVal = cell.getStringCellValue()
					for(def formatterType : formatterTypes) {
						try {
							if(formatterType == TimeUtil.FORMAT_DATE){ //Parse to DATE only
								result = TimeUtil.parseDate(TimeUtil.getUserDateFormat(session), cellVal, formatterType)	
							}else{
								result = TimeUtil.parseDateTime(session, cellVal, formatterType)
							}
							if (result) {
								break
							}
						} catch(e) {
							// TODO : JPM 4/2016 : We should report an error that we were unable to read the date value here
						}
					}

					if(!result){						
						log.warn("Can't Parse '$cellVal' using any of the formatters declared in $formatterTypes")
					}
					break
				default:
					throw new IllegalArgumentException("Invalid date value in row ${rowIdx+1}/column ${columnIdx+1}")
			}
		}
		return result
	}

	/**
	 * Used to read a date value from a cell in a spreadsheet using a DateFormat formatter which will make an assumption
	 * that the date in the spreadsheet is in GMT.
	 * 
	 * The method should return the value as a Date if valid, a null if the cell was empty or will return the failedIndicator value
	 * if the cell type is wrong or there was a parser error.
	 *
	 * @param sheet - the sheet to extract the value
	 * @param columnIdx - the column to reference (offset starts at zero)
	 * @param rowIdx - the row to reference (offset start at zero)
	 * @param dateFormatter - a DateFormat object used to parser the date from a string
	 * @param failedIndicator - a value that can be checked to determine if parsing was involved and it failed (default -1)
	 * @return The date from the specified cell, null if empty or the failedIndicator if unable to parse
	 * @throws IllegalArgumentException - if field does not contain String or Numeric (date) format
	 */
	public static getDateCellValue(Sheet sheet, Integer columnIdx, Integer rowIdx, DateFormat dateFormatter, failedIndicator=-1) {
		def value = getDateTimeCellValue(sheet, columnIdx, rowIdx, 'GMT', dateFormatter, failedIndicator)
		if (value != null && value != failedIndicator) {
			// Strip any time component of the date
			value.clearTime()
		}
		return value
	}

	/**
	 * Used to read a datetime value from a cell in a spreadsheet using a DateFormat formatter. The formatter will be set to the
	 * timezone that was passed. This will attempt to read the numeric value which will have a datetime that was generated into 
	 * the timezone of the user's Timezone so it will read it in and convert the date back to GMT appropriately. 
	 *
	 * The method should return the value as a Date if valid, a null if the cell was empty or will return the failedIndicator value
	 * if the cell type is wrong or there was a parser error.
	 *
	 * @param sheet - the sheet to extract the value
	 * @param columnIdx - the column to reference (offset starts at zero)
	 * @param rowIdx - the row to reference (offset start at zero)
	 * @param tzId - the timezone used when the dates were written to the spreadsheet
	 * @param dateFormatter - a DateFormat object used to parser the date from a string
	 * @param failedIndicator - a value that can be checked to determine if parsing was involved and it failed (default -1)
	 * @return The date from the specified cell, null if empty or the failedIndicator if unable to parse
	 * @throws IllegalArgumentException - if field does not contain String or Numeric (date) format
	 */
	public static getDateTimeCellValue(Sheet sheet, Integer columnIdx, Integer rowIdx, String tzId, DateFormat dateFormatter, failedIndicator=-1) {
		def result
		Cell cell = getCell(sheet, columnIdx, rowIdx)
		// println "getDateTimeCellValue() called for $columnIdx,$rowIdx, cellType=${cell.getCellType()} FORMAT:'${ dateFormatter.toPattern() }'"
		if (cell) {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					break

				case Cell.CELL_TYPE_NUMERIC:
					// We are assuming that the dates in the spreadsheet are written in the timezone of the user (para)
					// Dates stored in the spreadsheet are done since they are already stored without TZ
					Date dateInTz = cell.getDateCellValue()	

					// Now we need to shift the date to GMT so that it is correct TZ
					result = TimeUtil.moveDateToGMT(dateInTz, tzId)
					// println "getDateTimeCellValue() CELL_TYPE_NUMERIC cell '${cell}'=>'$dateInTz' adjusted from $tzId to GMT=> $result"

					break

				case Cell.CELL_TYPE_STRING:					
					String str = cell.getStringCellValue()
					if (str) {
						try {
							// Let's not assume that the user set the Timezone on the parser
							// println "getDateTimeCellValue() CELL_TYPE_STRING str=$str; cell='${cell}' Formatter:'${ dateFormatter.toPattern() }' cell (${columnCode(columnIdx) + rowIdx+1})" 
							TimeZone tz=TimeZone.getTimeZone(tzId)
							dateFormatter.setTimeZone(tz)
							result = dateFormatter.parse(str) 
						} catch (e) { 
							log.debug "getDateCellValue() CELL_TYPE_STRING parser error ${ e.getMessage() }; FORMAT:'${ dateFormatter.toPattern() }'"
							// println "getDateTimeCellValue() CELL_TYPE_STRING parser error ${ e.getMessage() }" 
							result = failedIndicator
						}
						// println "getDateTimeCellValue() CELL_TYPE_STRING cell='${cell}' Formatter:'${ dateFormatter.toPattern() }' cell (${columnCode(columnIdx) + rowIdx+1})" 
						// println "getDateTimeCellValue() CELL_TYPE_STRING '$str' => '$result'"
					}
					break

				default:
					// If the cell type is any other value just fail
					result = failedIndicator
					break
			}
		}

		return result
	}

	public static getIntegerCellValue(sheet, columnIdx, rowIdx) {
		def result = null
		def cell = getCell(sheet, columnIdx, rowIdx)
		if (cell) {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BOOLEAN:
					result = cell.getBooleanCellValue()? 1 : 0;
					break
				case Cell.CELL_TYPE_NUMERIC:
					result = cell.getNumericCellValue().intValue()
					break
				case Cell.CELL_TYPE_STRING:
					result = Integer.parseInt(cell.getStringCellValue())
					break
				default:
					throw new NumberFormatException("Invalid cell number.")
			}
		}
		return result
	}

	public static getStringCellValue(sheet, columnIdx, rowIdx, defaultValue="", boolean sanitizeString=false) {
		def result = defaultValue
		def cell = getCell(sheet, columnIdx, rowIdx)
		if (cell) {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					result = ''
					break
				case Cell.CELL_TYPE_BOOLEAN:
					result = cell.getBooleanCellValue().toString()
					break
				case Cell.CELL_TYPE_ERROR:
					result = 'error'
					break
				case Cell.CELL_TYPE_FORMULA:
					result = cell.getCellFormula()
					break
				case Cell.CELL_TYPE_NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						result = cell.getDateCellValue().toString()
					} else {
						result = cell.getNumericCellValue().intValue().toString()
					}
					break
				case Cell.CELL_TYPE_STRING:
					result = cell.getStringCellValue()?.trim()
					if (sanitizeString) result = this.sanitize(result)
					break
			}
		}
		return result?:defaultValue
	}

	public static applyStyleToCell(sheet, columnIdx, rowIdx, style) {
		def cell = getCell(sheet, columnIdx, rowIdx)
		if (cell) {
			cell.setCellStyle(style)
		}
	}

	/** 
	 * Used to clean up String values by escaping quotes and other things
	 */
	public static sanitize(String value) {
		return value?.replace("\\", "\\\\").replace("'","\\'")
	}

	/** 
	 * Used to get the column code (AA, BF) from the column index
	 * @param colIdx - the offset start at zero for column A
	 * @return The spreadsheet column code
	 */
	public static columnCode(colIdx) {
		return CellReference.convertNumToColString(colIdx)

	}


	/**
	 * Adds Range Validation based on a validation sheet.
	 *
	 * @param validationSheet - sheet containing the validation values.
	 * @param targetSheet - sheet where the validation is to be added.
	 * @param validationColumn - column in validationSheet where the values are listed.
	 * @param firstValidationRow - first data validation value.
	 * @param lastValidationRow - last data validation value.
	 * @param targetColumn - target column, where the validation will be added.
	 * @param firstTargetRow - first row where validation is to be added.
	 * @param lastTargetRow - last row where data validation is to be added.
	 */
	public static void addCellValidation(Sheet validationSheet, Sheet targetSheet, int validationColumn, int firstValidationRow, int lastValidationRow, int targetColumn, int firstTargetRow, int lastTargetRow){

		def createFormulaString = {
			String validationColumnCode = columnCode(validationColumn)
			String formula = new StringBuffer("'${validationSheet.getSheetName()}'!")
					.append("\$$validationColumnCode\$$firstValidationRow:")
					.append("\$$validationColumnCode\$$lastValidationRow")
					.toString()
			return formula
		}

		DataValidationHelper dvHelper = targetSheet.getDataValidationHelper()
  		CellRangeAddressList addressList = new CellRangeAddressList(firstTargetRow, lastTargetRow, targetColumn, targetColumn)

  		Name namedRange = validationSheet.getWorkbook().createName()
  		String name = "list_${targetSheet.getSheetName()}_${validationColumn}"
  		namedRange.setNameName(name);
  		namedRange.setRefersToFormula(createFormulaString());
  		def dvConstraint = dvHelper.createFormulaListConstraint(name);
  		
  		DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList)
  		
  		//DataValidation dataValidation = new HSSFDataValidation(addressList, dvConstraint)
		// Note the check on the actual type of the DataValidation object.
		// If it is an instance of the XSSFDataValidation class then the
		// boolean value 'false' must be passed to the setSuppressDropDownArrow()
		// method and an explicit call made to the setShowErrorBox() method.
		if(dataValidation instanceof XSSFDataValidation) {
		 	dataValidation.setSuppressDropDownArrow(false)
			dataValidation.setShowErrorBox(true)
		}
		else {
		 	// If the Datavalidation contains an instance of the HSSFDataValidation
		 	// class then 'true' should be passed to the setSuppressDropDownArrow()
		 	// method and the call to setShowErrorBox() is not necessary.
			dataValidation.setSuppressDropDownArrow(true)
		}


  		targetSheet.addValidationData(dataValidation)


	}


	/**
	 * This method makes a sheet read-only.
	 *
	 * @param sheet
	 */
	public static void makeSheetReadOnly(Sheet sheet){

		sheet.enableLocking()
		sheet.lockDeleteColumns(true)
    	sheet.lockDeleteRows(true)
    	sheet.lockFormatCells(true)
    	sheet.lockFormatColumns(true)
    	sheet.lockFormatRows(true)
    	sheet.lockInsertColumns(true)
    	sheet.lockInsertRows(true)
	}
}