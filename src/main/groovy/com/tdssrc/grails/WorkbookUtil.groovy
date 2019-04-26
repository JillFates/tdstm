package com.tdssrc.grails

import com.tdsops.tm.asset.WorkbookSheetName
import groovy.transform.CompileStatic
import groovy.util.logging.Commons
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddressList
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import javax.servlet.http.HttpSession
import java.text.DateFormat

/**
 * The WorkbookUtil class contains a collection of useful Apache POI manipulation methods
 */
@Commons
//@Singleton
@CompileStatic
class WorkbookUtil {

	/**
	 * This is the max length for an XLS cell.
	 */
	static final Integer XLS_CELL_MAX_LENGTH = 32767

	/**
	 * Creates a new Workbook based on file extension
	 * @param extension
	 * @return an instance or Workbook
	 * @trows a RuntimeException if the extension is not supported
	 */
	static Workbook createWorkbook(String extension = 'XLSX'){

		Workbook workbook
		if (extension.toUpperCase() == 'XLSX') {
			workbook = new XSSFWorkbook()
		} else if (extension.toUpperCase() == 'XLS') {
			workbook = new HSSFWorkbook()
		}

		if (!workbook) {
			throw new RuntimeException("Unsupported File Format $extension")
		}

		return workbook
	}

	/**
	 * Saves a workbook instance in an outputStream
	 * @param workbook an instance of Workbook class
	 * @param outputStream Stream of data used to save workbook parameter
	 * @return same instance of workbook used to saved data
	 * @throws Exception if there was an error working with outputStream parameter
	 */
	static Workbook saveToOutputStream(Workbook workbook, OutputStream outputStream) throws Exception{
		workbook.write(outputStream)
		workbook.close()
		outputStream.close()
		return workbook
	}

	/**
	 *
	 * @param rowNumber
	 * @param sheet
	 * @return
	 */
	static List<Cell> getCellsForSheet(int rowNumber, Sheet sheet) {

		List<Cell> cells = []
		Row row = sheet.getRow(rowNumber)
		Iterator iterator = row.iterator()
		while(iterator.hasNext()){
			cells.add((Cell)iterator.next())
		}
		return cells
	}


	/**
	 * Get sheet names
	 * @param workbook
	 * @return
	 */
	static List<String> getSheetNames(Workbook workbook) {
		def result = []
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			result << workbook.getSheetName(i)
		}
		return result
	}

	/**
	 * Get sheet names maps using ordinal positions of sheets as key
	 * @param workbook
	 * @return
	 */
	static Map<Object, Sheet> getSheetsMap(Workbook workbook) {
		def result = [:]
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			String sheetNumber = workbook.getSheetName(i)
			Sheet sheet = workbook.getSheet(sheetNumber)
			result << [(i): sheet]
			result << [(sheetNumber): sheet]
		}
		return result
	}

	/**
	 * Retrieves or creates a Row Object in the Spreadsheet
	 * @param rowIdx
	 * @return Row object of the Excel Sheet
	 */
	static Row getOrCreateRow(Sheet sheet, int rowIdx){
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
	static Cell getOrCreateCell(Row row, int colIdx) {
		Cell cell = row.getCell(colIdx)
		if (!cell) {
			cell = row.createCell(colIdx)
		}
		return cell
	}

	/**
	 * Adds a cell to a XSSFWorkbook
	 * @param sheet
	 * @param columnIdx
	 * @param rowIdx
	 * @param value
	 * @param type
	 * @return
	 */
	static Cell addCell(Sheet sheet, int columnIdx, int rowIdx, Object value, Integer type = null) {
		CellStyle style = null

		if (type != null) {
			style = createCellStyle(sheet, type)
		}

		return addCell(sheet, columnIdx, rowIdx, value, type, style)
	}

	/**
	 * Adds a cell to a Row in a sheet with specific cell style
	 * @param sheet
	 * @param columnIdx
	 * @param rowIdx
	 * @param value
	 * @param cellType
	 * @param cellStyle
	 */
	static Cell addCell(Row row, int colIdx, Object value, Integer cellType = null, CellStyle cellStyle = null) {
		Cell cell = getOrCreateCell(row, colIdx)

		if(cellType) {
			cell.setCellType(cellType)
		}
		if (cellStyle != null) {
			cell.setCellStyle(cellStyle)
		}

		return setCellValue(cell, value)
	}

	/**
	 * Add a new cell to the given Row at col with provided value, cell type and style
	 * @param sheet
	 * @param columnIdx
	 * @param rowIdx
	 * @param value
	 * @param cellType
	 * @param workbookCellStyles
	 */
	static Cell addCell(Row row, int colIdx, Object value, Integer cellType, Map<Integer, CellStyle> workbookCellStyles) {
		CellStyle cellStyle = null
		if(cellType){
			cellStyle = getCellStyle(row.sheet, cellType, workbookCellStyles)
		}
		return addCell(row, colIdx, value, cellType, cellStyle)
	}

	/**
	 * Adds a cell to a Workbook with specific cell style
	 * @param sheet
	 * @param columnIdx
	 * @param rowIdx
	 * @param value
	 * @param cellType
	 * @param cellStyle
	 */
	static Cell addCell(Sheet sheet, int columnIdx, int rowIdx, Object value, Integer cellType, CellStyle cellStyle) {
		def row = getOrCreateRow(sheet, rowIdx)
		return addCell(row, columnIdx, value, cellType, cellStyle)
	}

	/**
	 * Add a new cell to the given sheet at col, row with provided value, cell type and style
	 * @param sheet
	 * @param columnIdx
	 * @param rowIdx
	 * @param value
	 * @param cellType
	 * @param workbookCellStyles
	 */
	static Cell addCell(Sheet sheet, int colIdx, int rowIdx, Object value, Integer cellType, Map<Integer, CellStyle> workbookCellStyles) {
		def row = getOrCreateRow(sheet, rowIdx)
		return addCell(row, colIdx, value, cellType, workbookCellStyles)
	}



	/**
	 * creates a new cell style or get one from cache
	 * @param sheet
	 * @param cellType
	 * @param workbookCellStyles
	 * @return
	 */
	static CellStyle getCellStyle(Sheet sheet, int cellType, Map<Integer, CellStyle> workbookCellStyles) {
		CellStyle cellStyle = workbookCellStyles[cellType]
		if (cellStyle == null) {
			cellStyle = WorkbookUtil.createCellStyle(sheet, cellType)
			workbookCellStyles[cellType] = cellStyle
		}
		return cellStyle
	}

	/**
	 * Set given cell value
	 * @param cell
	 * @param value
	 */
	static Cell setCellValue(Cell cell, Object value) {
		switch (value) {
			case Number :
				cell.setCellValue((double) value)
				break

			default :
				String stringValue = value.toString()
				if (stringValue?.matches("(=|-|\\+|@).*")) {
					// See TM-14767: cell.setQuotePrefixed(true) won't produce a result accepted across all versions of Excel.
					stringValue = "'$stringValue"
				}
				// Prevent the cell from exceeding the maximum allowed length for XLS.
				cell.setCellValue(trucanteXlsCell(stringValue))
		}

		return cell
	}

	/**
	 * Add a cell to a row and the style
	 * @param row
	 * @param col
	 * @param value
	 * @param cellStyles
	 * @return
	 */
	static Cell addCellAndStyle(Row row, int col, Object value , List<CellStyle> cellStyles) {
		Cell cell = null

		if ( value != null || ( (value instanceof String) && ((String)value).length() > 0 )) {
			cell = addCell(row, col, value)

			CellStyle cellStyle = cellStyles[col]
			if (cellStyle) {
				cell.cellStyle = cellStyle
			}
		}

		return cell
	}

	static int getColumnsCount(Sheet sheet) {
		def result = 0
		def row = sheet.getRow(0)
		if (row) {
			String value
			def c = row.getLastCellNum()
			while (c >= 0) {
				value = getStringCellValue(sheet, c, 0)
				if (!StringUtil.isBlank(value)) {
					result = c + 1
					break
				}
				c--
			}
		}
		return result
	}

	static Cell getCell(Sheet sheet, int columnIdx, int rowIdx) {
		Cell result = null
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
	static Date getDateCellValue(Sheet sheet, Integer columnIdx, Integer rowIdx, HttpSession session, Collection formatterTypes=null) {
		Date result = null
		Cell cell = getCell(sheet, columnIdx, rowIdx)

		if (!formatterTypes) formatterTypes = [TimeUtil.FORMAT_DATE_TIME_22]

		if (cell) {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					break
				case Cell.CELL_TYPE_NUMERIC:
					// Dates stored in the spreadsheet are done so in GMT so we shouldn't need to convert it.
					result = cell.getDateCellValue()
					break
				case Cell.CELL_TYPE_STRING:
					String cellVal = cell.getStringCellValue()
					for(def formatterType : formatterTypes) {
						try {
							if(formatterType == TimeUtil.FORMAT_DATE){ //Parse to DATE only
								result = TimeUtil.parseDate(TimeUtil.getUserDateFormat(session), cellVal, formatterType.toString())
							}else{
								result = TimeUtil.parseDateTime(cellVal, formatterType.toString())
							}
							if (result) {
								break
							}
						} catch(e) {
							// TODO : JPM 4/2016 : We should report an error that we were unable to read the date value here
						}
					}

					if (!result) {
						log.warn("Unable to parse date in cell ${columnCode(columnIdx+1)}${rowIdx+1} with value '$cellVal'")
					}
					break
				case Cell.CELL_TYPE_FORMULA:
					throw new IllegalArgumentException("Cell ${columnCode(columnIdx+1)}${rowIdx+1} contains a formula")
				default:
					throw new IllegalArgumentException("Cell ${columnCode(columnIdx+1)}${rowIdx+1} contains an invalid date value")
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
	static Date getDateCellValue(Sheet sheet, Integer columnIdx, Integer rowIdx, DateFormat dateFormatter, failedIndicator=-1) {
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
	static Date getDateTimeCellValue(Sheet sheet, Integer columnIdx, Integer rowIdx, String tzId, DateFormat dateFormatter, failedIndicator=-1) {
		Date result = null
		Cell cell = getCell(sheet, columnIdx, rowIdx)
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
					break
				case Cell.CELL_TYPE_STRING:
					String str = cell.getStringCellValue()
					if (str) {
						try {
							// Let's not assume that the user set the Timezone on the parser
							TimeZone tz=TimeZone.getTimeZone(tzId)
							dateFormatter.setTimeZone(tz)
							result = dateFormatter.parse(str)
						} catch (e) {
							log.debug "getDateCellValue() CELL_TYPE_STRING parser error ${ e.getMessage() }; FORMAT:'${dateFormatter}'"
							result = null
						}
					}
					break
				default:
					// If the cell type is any other value just fail
					result = null
					break
			}
		}

		return result
	}

	/**
	 * Get Integer Cell Value
	 * @param sheet
	 * @param columnIdx
	 * @param rowIdx
	 * @return
	 */
	static Integer getIntegerCellValue(Sheet sheet, int columnIdx, int rowIdx) {
		Integer result = null
		Cell cell = getCell(sheet, columnIdx, rowIdx)
		if (cell) {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BOOLEAN:
					result = cell.getBooleanCellValue() ? 1 : 0
					break
				case Cell.CELL_TYPE_NUMERIC:
					result = ((Double)cell.getNumericCellValue()).intValue()
					break
				case Cell.CELL_TYPE_STRING:
					result = Integer.parseInt(cell.getStringCellValue())
					break
				case Cell.CELL_TYPE_BLANK:
					result = null
					break
				default:
					throw new NumberFormatException("Invalid cell number.")
			}
		}
		return result
	}

	/**
	 * Get String Cell Value
	 * @param sheet
	 * @param columnIdx
	 * @param rowIdx
	 * @param defaultValue
	 * @param sanitizeString
	 * @return
	 */
	static String getStringCellValue(Sheet sheet, int columnIdx, int rowIdx, String defaultValue = "", boolean sanitizeString = false) {
		String result = defaultValue
		Cell cell = getCell(sheet, columnIdx, rowIdx)
		if (cell) {
			switch (cell.getCellType()) {
				case Cell.CELL_TYPE_BLANK:
					result = ''
					break
				case Cell.CELL_TYPE_BOOLEAN:
					result = Boolean.toString(cell.getBooleanCellValue())
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
						DataFormatter formatter = new DataFormatter();
						result = formatter.formatCellValue(cell)
					}
					break
				case Cell.CELL_TYPE_STRING:
					result = cell.getStringCellValue()?.trim()
					if (sanitizeString) result = sanitize(result)
					break
			}
		}
		return result ?: defaultValue
	}

	/**
	 * Apply Style To Cell
	 * @param sheet
	 * @param columnIdx
	 * @param rowIdx
	 * @param style
	 * @return
	 */
	static void applyStyleToCell(Sheet sheet, int columnIdx, int rowIdx, CellStyle style) {
		Cell cell = getCell(sheet, columnIdx, rowIdx)
		if (cell) {
			cell.setCellStyle(style)
		}
	}

	/**
	 * Used to clean up String values by escaping quotes and other things
	 */
	static String sanitize(String value) {
		return value?.replace("\\", "\\\\").replace("'","\\'")
	}

	/**
	 * Used to get the column code (AA, BF) from the column index
	 * @param colIdx - the offset start at zero for column A
	 * @return The spreadsheet column code
	 */
	static String columnCode(int colIdx) {
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
	static void addCellValidation(Sheet validationSheet, Sheet targetSheet, int validationColumn, int firstValidationRow, int lastValidationRow, int targetColumn, int firstTargetRow, int lastTargetRow){

		def createFormulaString = {
			String validationColumnCode = columnCode(validationColumn)
			String formula = new StringBuilder("'${validationSheet.getSheetName()}'!")
					  .append("\$$validationColumnCode\$$firstValidationRow:")
					  .append("\$$validationColumnCode\$$lastValidationRow")
					  .toString()
			return formula
		}

		DataValidationHelper dvHelper = targetSheet.getDataValidationHelper()
		// This is for cases of empty sheets.
		if(lastTargetRow < firstTargetRow){
			lastTargetRow = firstTargetRow
		}
		CellRangeAddressList addressList = new CellRangeAddressList(firstTargetRow, lastTargetRow, targetColumn, targetColumn)

		Name namedRange = validationSheet.getWorkbook().createName()
		String name = "list_${targetSheet.getSheetName()}_${validationColumn}"
		namedRange.setNameName(name)
		namedRange.setRefersToFormula(createFormulaString())
		def dvConstraint = dvHelper.createFormulaListConstraint(name)

		DataValidation dataValidation = dvHelper.createValidation(dvConstraint, addressList)
		if(dvConstraint instanceof XSSFDataValidationConstraint){
			dataValidation.setSuppressDropDownArrow(false)
			dataValidation.setShowErrorBox(true)
		}else{
			dataValidation.setSuppressDropDownArrow(true)
		}

		targetSheet.addValidationData(dataValidation)
	}


	/**
	 * This method makes a sheet read-only.
	 *
	 * @param sheet
	 */
	static void makeSheetReadOnly(Sheet sheet){
		if(sheet instanceof XSSFSheet) {
			XSSFSheet xsheet = (XSSFSheet)sheet
			xsheet.enableLocking()
			xsheet.lockDeleteColumns(true)
			xsheet.lockDeleteRows(true)
			xsheet.lockFormatCells(true)
			xsheet.lockFormatColumns(true)
			xsheet.lockFormatRows(true)
			xsheet.lockInsertColumns(true)
			xsheet.lockInsertRows(true)
		}else if(sheet instanceof HSSFSheet){
			HSSFSheet hsheet = (HSSFSheet)sheet
			hsheet.protectSheet("")
		}else {
			log.error("makeSheetReadOnly: Operation Not Supported on object of type ${sheet.class}")
		}
	}

	/**
	 * Get a readable/writable instance of the workbook template so application can override title sheet and custom headers
	 * @param fileExtension template spreadsheet file extension
	 * @return a Workbook instance
	 */
	static Workbook getInitWorkbookInstance(File workbookTemplate) {
		FileInputStream fileInputStream = new FileInputStream(workbookTemplate)
		return WorkbookFactory.create(fileInputStream)
	}

	/**
	 * Get a workbook sheet
	 * @param workbook
	 * @param sheetName
	 * @return Sheet
	 */
	static Sheet getSheetFromWorkbook(Workbook workbook, String sheetName) {
		Sheet sheet = workbook.getSheet(sheetName)
		if (!sheet) {
			throw new RuntimeException("Unable to find sheet $sheetName in the uploaded spreadsheet")
		}
		if (sheet instanceof SXSSFSheet) {
			((SXSSFSheet)sheet).setRandomAccessWindowSize(25)
		}
		return sheet
	}

	/**
	 * Get a workbook sheet based on a sheetNumber
	 * @param workbook
	 * @param sheetName
	 * @return Sheet
	 */
	static Sheet getSheetFromWorkbookAt(Workbook workbook, int sheetNumber) {
		Sheet sheet = workbook.getSheetAt(sheetNumber)
		if (!sheet) {
			throw new RuntimeException("Unable to find sheet $sheetNumber in the uploaded spreadsheet")
		}
		if (sheet instanceof SXSSFSheet) {
			((SXSSFSheet)sheet).setRandomAccessWindowSize(25)
		}
		return sheet
	}

	/**
	 * Read sheet name using Workbook instance
	 * @param workbook
	 * @param sheetNumber
	 * @return
	 */
	static String getSheetName(Workbook workbook, int sheetNumber) {
		return workbook.getSheetName(sheetNumber)
	}

	/**
	 * Get a workbook sheet
	 * @param workbook
	 * @param sheetName
	 * @return
	 */
	static Sheet getSheetFromWorkbook(Workbook workbook, WorkbookSheetName sheetName) {
		return getSheetFromWorkbook(workbook, sheetName.toString())
	}

	/**
	 * Clone a Workbook from the original one
	 * @param workbook
	 * @return cloned workbook
	 */
	static Workbook cloneWorkbookInstance(Workbook originalWorkbook) {
		File tempWorkbook = File.createTempFile("assetEntityExport_" + UUID.randomUUID().toString(), null)
		FileOutputStream fileOutputStream =  new FileOutputStream(tempWorkbook)
		originalWorkbook.write(fileOutputStream)
		fileOutputStream.close()

		// Open a new Workbook from the one saved previously
		FileInputStream fileInputStream = new FileInputStream(tempWorkbook)

		Workbook wb_template
		if ( originalWorkbook instanceof XSSFWorkbook ) {
			XSSFWorkbook xssfWorkbook = new XSSFWorkbook( fileInputStream )
			wb_template = xssfWorkbook

			// wb_template = new SXSSFWorkbook( xssfWorkbook )

		} else if ( originalWorkbook instanceof  HSSFWorkbook ) {
			wb_template = new HSSFWorkbook( fileInputStream )

		} else {
			throw new RuntimeException ( "Unsupported format Exception , Kill me please!" )

		}

		fileInputStream.close()
		originalWorkbook.close()
		tempWorkbook.delete()

		return wb_template
	}

	/**
	 * Save a workbook to a temporary file
	 * @param workbook
	 * @return absolute path of the temporary workbook file saved
	 */
	static String saveWorkbook(Workbook workbook) {
		File tempExportFile = File.createTempFile("assetEntityExport_" + UUID.randomUUID().toString(), null)
		FileOutputStream out =  new FileOutputStream(tempExportFile)
		workbook.write(out)
		out.close()
		workbook.close()
		return tempExportFile.getAbsolutePath()
	}

	/**
	 * Creates a cell style
	 * @param sheet
	 * @param cellType
	 * @return
	 */
	static CellStyle createCellStyle(Sheet sheet, int cellType) {
		CellStyle cellStyle = sheet.workbook.createCellStyle()
		if (cellType == Cell.CELL_TYPE_NUMERIC) { // This resolves to a Numeric no decimal spaces Value
			String binFormat = BuiltinFormats.getBuiltinFormat(1) //this is "0" mask format
			def df = sheet.workbook.createDataFormat().getFormat(binFormat)
			cellStyle.setDataFormat(df)
		} else if (cellType == Cell.CELL_TYPE_STRING) {
			cellStyle.setDataFormat((short) BuiltinFormats.getBuiltinFormat("text"))
		}
		return cellStyle
	}

	/**
	 * Assume always row 0 and stops when reach last cell in row or cell value is blank
	 * @param sheet
	 * @return
	 */
	static List<String> getSheetHeadersAsList(Sheet sheet) {
		List<String> headers = new ArrayList<>()
		Row row = sheet.getRow(0)
		short cellIndex = 0
		while (true) {
			Cell cell = row.getCell(cellIndex)
			if (cell == null || StringUtil.isBlank(cell.getStringCellValue())) {
				break;
			}
			headers.add(cell.getStringCellValue())
			cellIndex++
		}
		return headers
	}

	/**
	 * This method returns the last non-empty row in the sheet.
	 *
	 * @param sheet
	 * @return
	 */
	static int getLastRowNum(Sheet sheet) {
		int lastRow = sheet.getLastRowNum()
		while (lastRow > 0 && isRowEmpty(sheet, lastRow)) {
			lastRow --
		}
		return lastRow
	}

	/**
	 * This method determines if the given row is empty (all null or empty values).
	 * @param sheet
	 * @param rowNumber
	 * @return true if the row is empty, false otherwise.
	 */
	static boolean isRowEmpty(Sheet sheet, int rowNumber){
		Row row = sheet.getRow(rowNumber)
		if (row) {
			for (int col = row.getFirstCellNum(); col < row.getLastCellNum(); col ++) {
				Cell cell = row.getCell(col)
				if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
					return false
				}

			}
		}
		return true
	}

	/**
	 * Get a list of the Cellstyles from the header of the Spreadsheet
	 * @param workbookCellStyles
	 * @param sheet
	 * @return
	 */
	static List<CellStyle> getHeaderStyles(Map<Integer, CellStyle> workbookCellStyles, Sheet sheet) {
		List<CellStyle> rowStyles = []
		Row rowHeader =  sheet.getRow(0)
		int lastCellNum = rowHeader.getLastCellNum()

		for( int i = 0; i <= lastCellNum; i++ ) {

			CellStyle cellStyle = null
			Cell cell = rowHeader.getCell(i)
			if(cell) {
				short dataFormat = cell.cellStyle.dataFormat

				cellStyle = workbookCellStyles.get(dataFormat)

				if( cellStyle == null ) {
					cellStyle = sheet.workbook.createCellStyle()
					cellStyle.dataFormat = dataFormat
				}
			}

			rowStyles << cellStyle
		}

		return rowStyles
	}

	/**
	 * Truncate an XLS cell to the max length allowed.
	 * @param value
	 * @return
	 */
	static String trucanteXlsCell(String value) {
		return StringUtil.truncateIfBigger(value, XLS_CELL_MAX_LENGTH)
	}

}
