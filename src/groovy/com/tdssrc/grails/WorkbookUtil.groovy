package com.tdssrc.grails

import groovy.util.logging.Commons
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.ss.usermodel.BuiltinFormats
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.DataValidation
import org.apache.poi.ss.usermodel.DataValidationHelper
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Name
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint
import org.apache.poi.xssf.usermodel.XSSFSheet

import java.text.DateFormat

/**
 * Apache POI manipulation methods.
 */
@Commons
@Singleton
class WorkbookUtil {

	static List<String> getSheetNames(Workbook workbook) {
		(0 .. workbook.numberOfSheets - 1).collect { int i -> workbook.getSheetName(i) }
	}

	static void addCell(Sheet sheet, int columnIndex, int rowIndex, value, Integer type = null) {
		//println "columnIndex=$columnIndex, rowIndex=$rowIndex, value=$value, rowIndex isa ${rowIndex.getClass().name}, sheet isa ${sheet.getClass().name}"
		Row row = sheet.getRow(rowIndex) ?: sheet.createRow(rowIndex)
		Cell cell = row.getCell(columnIndex) ?: row.createCell(columnIndex)
		if (type) {
			cell.cellType = type
			if (type == Cell.CELL_TYPE_NUMERIC) { // This resolves to a Numeric no decimal spaces Value
				//Set Cell Type to allow Type Number
				Workbook wb = sheet.workbook
				CellStyle style = wb.createCellStyle()
				style.dataFormat = wb.createDataFormat().getFormat(BuiltinFormats.getBuiltinFormat(1)) //this is "0" mask format
				cell.cellStyle = style
			}
			else if (type == Cell.CELL_TYPE_STRING) {
				CellStyle textFormatStyle = sheet.workbook.createCellStyle()
				textFormatStyle.setDataFormat((short)BuiltinFormats.getBuiltinFormat("text"))
				cell.cellStyle = textFormatStyle
			}
		}
		cell.cellValue = value
	}

	static int getColumnsCount(Sheet sheet) {
		int count = 0
		Row row = sheet.getRow(0)
		if (row) {
			short c = row.lastCellNum
			while (c >= 0) {
				if (!StringUtil.isBlank(getStringCellValue(sheet, c, 0))) {
					count = c + 1
					break
				}
				c--
			}
		}
		return count
	}

	static getCell(Sheet sheet, int columnIndex, int rowIndex) {
		sheet.getRow(rowIndex)?.getCell(columnIndex)
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
	 * @throws ParseException - if the field contains an invalid formatted String value
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
	 * Read a date value from a cell in a spreadsheet using a DateFormat formatter which will make an assumption
	 * that the date in the spreadsheet is in GMT.
	 *
	 * The method should return the value as a Date if valid, a null if the cell was empty or will return the failedIndicator value
	 * if the cell type is wrong or there was a parser error.
	 *
	 * @param sheet - the sheet to extract the value
	 * @param columnIndex - the column to reference (offset starts at zero)
	 * @param rowIndex - the row to reference (offset start at zero)
	 * @param dateFormatter - a DateFormat object used to parser the date from a string
	 * @param failedIndicator - a value that can be checked to determine if parsing was involved and it failed (default -1)
	 * @return The date from the specified cell, null if empty or the failedIndicator if unable to parse
	 * @throws IllegalArgumentException - if field does not contain String or Numeric (date) format
	 */
	static Date getDateCellValue(Sheet sheet, Integer columnIndex, Integer rowIndex, DateFormat dateFormatter, int failedIndicator = -1) {
		Date value = getDateTimeCellValue(sheet, columnIndex, rowIndex, 'GMT', dateFormatter, failedIndicator)
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
	 * @param columnIndex - the column to reference (offset starts at zero)
	 * @param rowIndex - the row to reference (offset start at zero)
	 * @param tzId - the timezone used when the dates were written to the spreadsheet
	 * @param dateFormatter - a DateFormat object used to parser the date from a string
	 * @param failedIndicator - a value that can be checked to determine if parsing was involved and it failed (default -1)
	 * @return The date from the specified cell, null if empty or the failedIndicator if unable to parse
	 * @throws IllegalArgumentException - if field does not contain String or Numeric (date) format
	 */
	static Date getDateTimeCellValue(Sheet sheet, Integer columnIndex, Integer rowIndex, String tzId, DateFormat dateFormatter, failedIndicator=-1) {
		def result
		Cell cell = getCell(sheet, columnIndex, rowIndex)
		// println "getDateTimeCellValue() called for $columnIndex,$rowIndex, cellType=$cell.cellType FORMAT:'${dateFormatter.toPattern()}'"
		if (cell) {
			switch (cell.cellType) {
				case Cell.CELL_TYPE_BLANK:
					break

				case Cell.CELL_TYPE_NUMERIC:
					// We are assuming that the dates in the spreadsheet are written in the timezone of the user (para)
					// Dates stored in the spreadsheet are done since they are already stored without TZ
					Date dateInTz = cell.dateCellValue

					// Now we need to shift the date to GMT so that it is correct TZ
					result = TimeUtil.moveDateToGMT(dateInTz, tzId)
					// println "getDateTimeCellValue() CELL_TYPE_NUMERIC cell '$cell'=>'$dateInTz' adjusted from $tzId to GMT=> $result"

					break

				case Cell.CELL_TYPE_STRING:
					String str = cell.stringCellValue
					if (str) {
						try {
							// Let's not assume that the user set the Timezone on the parser
							// log.debug "getDateTimeCellValue() CELL_TYPE_STRING str=$str; cell='${cell}' Formatter:'${ dateFormatter.toPattern() }' cell (${columnCode(columnIdx) + rowIdx+1})"
							dateFormatter.timeZone = TimeZone.getTimeZone(tzId)
							result = dateFormatter.parse(str)
						} catch (e) {
							log.debug "getDateCellValue() CELL_TYPE_STRING parser error $e.message; FORMAT:'${dateFormatter.toPattern()}'"
							// println "getDateTimeCellValue() CELL_TYPE_STRING parser error $e.message"
							result = failedIndicator
						}
						// println "getDateTimeCellValue() CELL_TYPE_STRING cell='$cell' Formatter:'${dateFormatter.toPattern()}' cell (${columnCode(columnIndex) + rowIndex+1})"
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

	static Integer getIntegerCellValue(Sheet sheet, int columnIndex, int rowIndex) {
		Cell cell = getCell(sheet, columnIndex, rowIndex)
		if (cell) {
			switch (cell.cellType) {
				case Cell.CELL_TYPE_BOOLEAN: return cell.booleanCellValue ? 1 : 0
				case Cell.CELL_TYPE_NUMERIC: return cell.numericCellValue.intValue()
				case Cell.CELL_TYPE_STRING:  return Integer.parseInt(cell.stringCellValue)
				default: throw new NumberFormatException("Invalid cell type")
			}
		}
	}

	static String getStringCellValue(Sheet sheet, int columnIndex, int rowIndex, String defaultValue = '',
	                                 boolean sanitizeString=false) {
		def result = defaultValue
		def cell = getCell(sheet, columnIndex, rowIndex)
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
						//TODO: oluna this is bad!!! that the cell is numeric doesn't make it INT presicion lost! but how we do change it without hitting everything else
						//result = cell.getNumericCellValue().intValue().toString()
						result = new DataFormatter().formatCellValue(cell)
					}
					break
				case Cell.CELL_TYPE_STRING:
					result = cell.getStringCellValue()?.trim()
					if (sanitizeString) result = sanitize(result)
					break
			}
		}
		return result?:defaultValue
	}

	static applyStyleToCell(sheet, columnIndex, rowIndex, style) {
		def cell = getCell(sheet, columnIndex, rowIndex)
		if (cell) {
			cell.setCellStyle(style)
		}
	}

	/**
	 * Used to clean up String values by escaping quotes and other things
	 */
	static sanitize(String value) {
		return value?.replace("\\", "\\\\").replace("'","\\'")
	}

	/**
	 * Used to get the column code (AA, BF) from the column index
	 * @param colIndex - the offset start at zero for column A
	 * @return The spreadsheet column code
	 */
	static columnCode(colIndex) {
		return CellReference.convertNumToColString(colIndex)
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
	static void addCellValidation(Sheet validationSheet, Sheet targetSheet, int validationColumn, int firstValidationRow,
	                              int lastValidationRow, int targetColumn, int firstTargetRow, int lastTargetRow){

		def createFormulaString = {
			String validationColumnCode = columnCode(validationColumn)
			"'$validationSheet.sheetName'!\$$validationColumnCode\$$firstValidationRow:\$$validationColumnCode\$$lastValidationRow"
		}

		DataValidationHelper dvHelper = targetSheet.getDataValidationHelper()
		// This is for cases of empty sheets.
		if(lastTargetRow < firstTargetRow){
			lastTargetRow = firstTargetRow
		}
		CellRangeAddressList addressList = new CellRangeAddressList(firstTargetRow, lastTargetRow, targetColumn, targetColumn)

		Name namedRange = validationSheet.getWorkbook().createName()
		String name = 'list_' + targetSheet.sheetName + '_' + validationColumn
		namedRange.setNameName(name)
		namedRange.setRefersToFormula(createFormulaString())
		def dvConstraint = dvHelper.createFormulaListConstraint(name)

		DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList)
		if (dvConstraint instanceof XSSFDataValidationConstraint) {
			dataValidation.setSuppressDropDownArrow(false)
			dataValidation.setShowErrorBox(true)
		} else {
			dataValidation.setSuppressDropDownArrow(true)
		}

		targetSheet.addValidationData(dataValidation)
	}

	static void makeSheetReadOnly(Sheet sheet) {
		if (sheet instanceof XSSFSheet) {
			XSSFSheet xsheet = (XSSFSheet)sheet
			xsheet.enableLocking()
			xsheet.lockDeleteColumns(true)
			xsheet.lockDeleteRows(true)
			xsheet.lockFormatCells(true)
			xsheet.lockFormatColumns(true)
			xsheet.lockFormatRows(true)
			xsheet.lockInsertColumns(true)
			xsheet.lockInsertRows(true)
		}
		else if (sheet instanceof HSSFSheet) {
			((HSSFSheet)sheet).protectSheet("")
		}
		else {
			log.error("makeSheetReadOnly: Operation Not Supported on object of type ${sheet.getClass().name}")
		}
	}
}
