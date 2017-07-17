package com.tdssrc.grails

import org.apache.poi.ss.usermodel.BuiltinFormats
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
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
	Sheet sheet
	Map<Object, CellStyle> styleMap = [:]

	SheetWrapper(Sheet sheet){
		this.sheet = sheet
	}

	void addCell(int columnIdx, int rowIdx, Object value, Integer type = null) {
		def row = sheet.getRow(rowIdx)
		if (!row) {
			row = sheet.createRow(rowIdx)
		}
		Cell cell = row.getCell(columnIdx)
		if (!cell) {
			cell = row.createCell(columnIdx)
		}
		if (type != null) {
			cell.setCellType(type)
			CellStyle style = getCellStyle(type)
			cell.setCellStyle(style)
		}
		WorkbookUtil.setCellValue(cell, value)
	}

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
