package com.tdssrc.grails

import org.apache.poi.ss.usermodel.BuiltinFormats
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet

/**
 * Created by octavio on 7/17/17.
 * TODO: oluna - Refactor and review. Even then there's are some functions related to the use of the styles in the AssetExportService.groovy
 * they are generic but bounded to the service.
 * I believe that using a wrapper is a more Object Oriented generic form and I do't want to add the dependency
 * of an unrelated service to other services.
 *
 * This class acts as a holder for different reusable information of the spreadsheet being build
 */
class SheetWrapper {
	// Excel Sheet where we are writting to
	Sheet sheet

	// Dictionary of cellstyles
	Map<Object, CellStyle> styleMap = [:]

	SheetWrapper(Sheet sheet){
		this.sheet = sheet
	}

	/**
	 * Retrieves or creates a Row Object in the Spreadsheet
	 * @param rowIdx
	 * @return Row object of the Excel Sheet
	 */
	Row getOrCreateRow(int rowIdx){
		Row row = sheet.getRow(rowIdx)
		if (!row) {
			row = sheet.createRow(rowIdx)
		}
		return row
	}

	/**
	 * Retrieves or creates a Cell Object in the passed Row
	 * @param row
	 * @param colIdx
	 * @return
	 */
	Cell getOrCreateCell(Row row, int colIdx) {
		Cell cell = row.getCell(colIdx)
		if (!cell) {
			cell = row.createCell(colIdx)
		}
		if (type != null) {
			cell.setCellType(type)
			CellStyle style = getCellStyle(type)
			cell.setCellStyle(style)
		}
		return cell
	}

	/**
	 * Creates a Cell with the default Style, passing the Row Object, col position and value
	 * @param row
	 * @param colIdx
	 * @param value
	 */
	void addCell(Row row, int colIdx, Object value) {
		Cell cell = getOrCreateCell(row, colIdx)
		WorkbookUtil.setCellValue(cell, value)
	}

	/**
	 * Creates a Cell with the default Style, passing the row & col position and value
	 * @param colIdx
	 * @param rowIdx
	 * @param value
	 */
	void addCell(int colIdx, int rowIdx, Object value) {
		def row = getOrCreateRow(rowIdx)
		addCell(row, colIdx, value)
	}

	/**
	 * Creates a Cell, passing the Row Object, col position, value and style type
	 * @param columnIdx
	 * @param rowIdx
	 * @param value
	 * @param type
	 */
	void addCell(Row row, int colIdx, Object value, int type) {
		Cell cell = getOrCreateCell(row, colIdx)
		cell.setCellType(type)
		CellStyle style = getCellStyle(type)
		cell.setCellStyle(style)
		WorkbookUtil.setCellValue(cell, value)
	}

	/**
	 * Creates a Cell, passing the row & col position, value and style type
	 * @param columnIdx
	 * @param rowIdx
	 * @param value
	 * @param type
	 */
	void addCell(int colIdx, int rowIdx, Object value, int type) {
		def row = getOrCreateRow(rowIdx)
		addCell(row, colIdx, value, type)
	}

	/**
	 * Creates or Return a CellStyle
	 * @param type
	 * @return
	 */
	CellStyle getCellStyle(int type){
		CellStyle style = styleMap[type]

		if( ! style) {
			style = sheet.workbook.createCellStyle() // TODO <SL> Use createCellStyle()
			if (type == Cell.CELL_TYPE_NUMERIC) { // This resolves to a Numeric no decimal spaces Value
				String binFormat = BuiltinFormats.getBuiltinFormat(1) //this is "0" mask format
				def df = sheet.workbook.createDataFormat().getFormat(binFormat)
				style.setDataFormat(df)
			} else if (type == Cell.CELL_TYPE_STRING) {
				style.setDataFormat((short) BuiltinFormats.getBuiltinFormat("text"))
			}

			styleMap[type] = style
		}

		return style
	}
}
