package com.tdssrc.grails;

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.hssf.util.CellReference

/**
 * The WorkbookUtil class contains a collection of useful Apache POI manipulation methods
 * 
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 *
 */
class WorkbookUtil {
	
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
	 * Used to attempt to access a date value from a cell
	 * @param sheet - the sheet to extract the value
	 * @param columnIdx - the column to reference (offset starts at zero)
	 * @param rowIdx - the row to reference (offset start at zero)
	 * @param session - user session
	 * @param dateFormat - list if formats to use if parsing a string value
	 * @return The date from the specified cell or null if empty
	 * @throws IllegalArgumentException - if field does not contain String or Numeric (date) format
	 * @throws ParseException - if the field contains an invalid formatted String value
	 * 
	 */
	public static getDateCellValue(Sheet sheet, Integer columnIdx, Integer rowIdx, session, formatterTypes=null) {
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
					for(def formatterType : formatterTypes) {
						try {
							result = TimeUtil.parseDateTime(session, cell.getStringCellValue(), formatterType)
							if (result) {
								break
							}
						} catch(e) {
							// TODO : JPM 4/2016 : We should report an error that we were unable to read the date value here
						}
					}
					break
				default:
					throw new IllegalArgumentException("Invalid date value in row ${rowIdx+1}/column ${columnIdx+1}")
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
}