package com.tdsops.etl

import com.tdssrc.grails.WorkbookUtil
import getl.data.Dataset
import getl.data.Field
import getl.excel.ExcelDriver
import getl.exception.ExceptionGETL
import getl.utils.BoolUtils
import getl.utils.FileUtils
import getl.utils.ListUtils
import getl.utils.Logs
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFSheet

/**
 * Custom implementation of ExcelDriver. It adds support for several ETL script commands.
 * For example:
 * <pre>
 *  sheet 'Active Applications'
 *  .....
 *  sheet 4
 *  .....
 *  sheet '4'
 *  ....
 * </pre>
 *  Is validated by @see TDSExcelDriver#hasSheet
 * <br>
 *  After that, reading workbook column headers
 * <pre>
 *  sheet 'Active Applications'
 *  read labels
 * </pre>
 * It is implemented TDSExcelDriver#fields
 *
 * @see ExcelDriver
 * @see TDSExcelDriver#fields(getl.data.Dataset)
 * @see TDSExcelDriver#hasSheet(getl.data.Dataset, int)
 * @see TDSExcelDriver#hasSheet(getl.data.Dataset, String)
 */
class TDSExcelDriver extends ExcelDriver {

	Workbook workbook

	@Override
	protected long eachRow(Dataset dataset, Map params, Closure prepareCode, Closure code) {
		String path = dataset.connection.params.path
		String fileName = dataset.connection.params.fileName
		String fullPath = FileUtils.ConvertToDefaultOSPath(path + File.separator + fileName)
		boolean warnings = params.showWarnings

		dataset.field = fields(dataset)
		if (dataset.field.isEmpty()) throw new ExceptionGETL("Required fields description with dataset")
		if (!path) throw new ExceptionGETL("Required \"path\" parameter with connection")
		if (!fileName) throw new ExceptionGETL("Required \"fileName\" parameter with connection")
		if (!FileUtils.ExistsFile(fullPath)) throw new ExceptionGETL("File \"${fileName}\" doesn't exists in \"${path}\"")

		Map datasetParams = dataset.params

		def ln = datasetParams.listName?:0
		def header = BoolUtils.IsValue([params.header, datasetParams.header], false)

		Number offsetRows = dataset.params.currentRowIndex?:0
		Number offsetCells = 0

		long countRec = 0

		if (prepareCode != null) prepareCode([])

		Workbook workbook = getWorkbookType(fullPath)
		org.apache.poi.ss.usermodel.Sheet sheet

		if (ln instanceof String) sheet = workbook.getSheet(ln as String)
		else {
			sheet = workbook.getSheetAt(ln)
			dataset.params.listName = workbook.getSheetName(ln)
			if (warnings) Logs.Warning("Parameter listName not found. Using list name: '${dataset.params.listName}'")
		}

		def limit = ListUtils.NotNullValue([params.limit, datasetParams.limit, sheet.lastRowNum])

		Iterator rows = sheet.rowIterator()

		if (offsetRows != 0) (1..offsetRows).each {
			rows.next()
		}
		int additionalRows = limit + offsetRows + (header?(1 as int):(0 as int))

		rows.each { org.apache.poi.ss.usermodel.Row row ->
			if (row.rowNum >= additionalRows) return
			Iterator cells = row.cellIterator()
			LinkedHashMap<String, Object> updater = [:]

			if (offsetCells != 0) 1..offsetCells.each { cells.next() }

			cells.each { Cell cell ->
				int columnIndex = cell.columnIndex - offsetCells
				if (columnIndex >= dataset.field.size()) return
				updater."${dataset.field.get(columnIndex).name}" = getCellValue(cell, dataset, columnIndex)
			}

			code(updater)
			countRec++
		}

		countRec
	}

	private static getCellValue(final Cell cell, final Dataset dataset, final int columnIndex) {
		try{
			Field.Type fieldType = dataset.field.get(columnIndex).type

			switch (fieldType) {
				case Field.Type.BIGINT:
					if (cell.cellType == Cell.CELL_TYPE_STRING) (cell.stringCellValue.toBigInteger())
					else cell.numericCellValue.toBigInteger()
					break
				case Field.Type.BOOLEAN:
					cell.booleanCellValue
					break
				case Field.Type.DATE:
					cell.dateCellValue
					break
				case Field.Type.DATETIME:
					cell.dateCellValue
					break
				case Field.Type.DOUBLE:
					if (cell.cellType == Cell.CELL_TYPE_STRING) (cell.stringCellValue.toDouble())
					else cell.numericCellValue
					break
				case Field.Type.INTEGER:
					if (cell.cellType == Cell.CELL_TYPE_STRING) (cell.stringCellValue.toInteger())
					else cell.numericCellValue.toInteger()
					break
				case Field.Type.NUMERIC:
					if (cell.cellType == Cell.CELL_TYPE_STRING) (cell.stringCellValue.toBigDecimal())
					else cell.numericCellValue.toBigDecimal()
					break
				case Field.Type.STRING:
					cell.stringCellValue
					break
				default:
					throw new ExceptionGETL('Default field type not supported.')
			}
		} catch (e) {
			Logs.Warning("Error in ${cell.rowIndex} row")
			Logs.Exception(e)
			throw e
		}
	}

	@Override
	protected List<Field> fields(Dataset dataset) {

		Workbook workbook = getWorkbook(dataset)
		Integer currentRowIndex = dataset.params.currentRowIndex ?:0
		dataset.params.listName = dataset.params.listName?:0

		XSSFSheet sheet = getSheetFromWorkbook(dataset, workbook, dataset.params.listName)
		List<Cell> cells = WorkbookUtil.getCellsForSheet(currentRowIndex, sheet)
		List<Field> fields = []
		cells.each { Cell cell ->
			fields << new Field(name: cellValue(cell), type: cellType(cell))
		}

		return fields
	}

	/**
	 * Calculates String value of a Cell
	 * @param cell
	 * @return
	 */
	private String cellValue(Cell cell){
		// TODO - remove toLowerCase once GETL library is fixed - see TM-9268
		return cell.toString().trim()
	}

	/**
	 * Calculates Field Type based on Cell type
	 * @param cell
	 * @return
	 */
	private Field.Type cellType(Cell cell){
		// TODO: DMC: Complete this conversion
		return Field.Type.STRING
	}

	/**
	 * Lazy initialization for TDSExcelDriver#workbook field
	 * @param dataset
	 * @return
	 */
	protected Workbook getWorkbook(Dataset dataset){

		if(!workbook){
			String path = dataset.connection.params.path
			String fileName = dataset.connection.params.fileName
			String fullPath = FileUtils.ConvertToDefaultOSPath(path + File.separator + fileName)

			if (!path) throw new ExceptionGETL("Required \"path\" parameter with connection")
			if (!fileName) throw new ExceptionGETL("Required \"fileName\" parameter with connection")
			if (!FileUtils.ExistsFile(fullPath)) throw new ExceptionGETL("File \"${fileName}\" doesn't exists in \"${path}\"")

			workbook = getWorkbookType(fullPath)
		}

		return workbook
	}

	/**
	 * Check if a Worknbook instance has a Sheet based on a sheet name
	 * @param dataset
	 * @param listName
	 * @return
	 */
	boolean hasSheet(Dataset dataset, String listName) {
		return WorkbookUtil.getSheetFromWorkbook(getWorkbook(dataset), listName) != null
	}

	/**
	 * Check if a Worknbook instance has a Sheet based on an ordinal sheet number
	 * @param dataset
	 * @param listName
	 * @return
	 */
	boolean hasSheet(Dataset dataset, int sheetNumber) {
		return WorkbookUtil.getSheetFromWorkbookAt(getWorkbook(dataset), sheetNumber) != null
	}

	/**
	 * Lookups a Sheet instance based on a listName
	 * @param dataSet
	 * @param workbook
	 * @param listName
	 * @return
	 */
	private XSSFSheet getSheetFromWorkbook(Dataset dataset, Workbook workbook, String listName) {
		return WorkbookUtil.getSheetFromWorkbook(workbook, listName)
	}

	/**
	 * Lookups a Sheet instance based on a sheetNumber
	 * @param dataSet
	 * @param workbook
	 * @param listName
	 * @return
	 */
	private XSSFSheet getSheetFromWorkbook(Dataset dataset, Workbook workbook, int sheetNumer) {
		XSSFSheet sheet = WorkbookUtil.getSheetFromWorkbookAt(workbook, sheetNumer)
		dataset.params.listName = WorkbookUtil.getSheetName(workbook, sheetNumer)
		return sheet
	}
}
