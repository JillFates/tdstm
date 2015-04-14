package com.tdssrc.grails;

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.DateUtil

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
		def row = sheet.getRow(rowIdx)
		if (!row) {
			row = sheet.createRow(rowIdx)
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
			result = row.getLastCellNum()
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

	public static getIntegerCellValue(sheet, columnIdx, rowIdx) {
		def result = null
		def cell = getCell(sheet, columnIdx, rowIdx)
		if (cell) {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BOOLEAN:
					result = cell.getBooleanCellValue()? 1 : 0;
					break;
				case Cell.CELL_TYPE_NUMERIC:
					result = cell.getNumericCellValue().intValue()
					break;
				case Cell.CELL_TYPE_STRING:
					result = Integer.parseInt(cell.getStringCellValue())
					break;
				default:
					throw new NumberFormatException("Invalid cell number.")
			}
		}
		return result
	}

	public static getStringCellValue(sheet, columnIdx, rowIdx, defaultValue=null) {
		def result = defaultValue
		def cell = getCell(sheet, columnIdx, rowIdx)
		if (cell) {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					result = ''
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					result = cell.getBooleanCellValue().toString()
					break;
				case Cell.CELL_TYPE_ERROR:
					result = 'error'
					break;
				case Cell.CELL_TYPE_FORMULA:
					result = cell.getCellFormula()
					break;
				case Cell.CELL_TYPE_NUMERIC:
					if (DateUtil.isCellDateFormatted(cell)) {
						result = cell.getDateCellValue().toString()
					} else {
						result = cell.getNumericCellValue().intValue().toString()
					}
					break;
				case Cell.CELL_TYPE_STRING:
					result = cell.getStringCellValue()
					break;
			}
		}
		return result
	}

	public static applyStyleToCell(sheet, columnIdx, rowIdx, style) {
		def cell = getCell(sheet, columnIdx, rowIdx)
		if (cell) {
			cell.setCellStyle(style)
		}
	}

}
