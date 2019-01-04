package com.tdsops.etl

import com.tdssrc.grails.WorkbookUtil
import getl.data.Dataset
import getl.data.Field
import getl.excel.ExcelDriver
import getl.exception.ExceptionGETL
import getl.utils.BoolUtils
import getl.utils.FileUtils
import getl.utils.ListUtils
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook

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

	/**
	 * Workbook instance. It's open once using TDSExcelDriver#readWorkbookAndSheets
	 */
	Workbook workbook
	/**
	 * Map of sheet names as a map with name and ordinal position as a key
	 * <pre>
	 *  [
	 *      0: sheet1,
	 *      1: sheet2,
	 *      'Applications': sheet1
	 *      'Devices': sheet2
	 *  ]
	 * </pre>
	 */
	Map<Object, Sheet> sheetsMap

	/**
	 * Maps of fields base on listName param.
	 * <pre>
	 * [
	 *      0: [{ ..field1.. }, { ..field1.. }, ..., { ..fieldN.. }]
	 * ]
	 * </pre>
	 * <pre>
	 * [
	 *      'Applications': [{ ..field1.. }, { ..field1.. }, ..., { ..fieldN.. }]
	 * ]
	 * </pre>
	 */
	Map<Object, List<Field>> fieldsMap = [:]


	@Override
	long eachRow(Dataset dataset, Map params, Closure prepareCode, Closure code) {

		if(!workbook) {
			this.readWorkbookAndSheets(dataset)
		}
		Sheet sheet = this.sheetsMap[dataset.params.listName]

		dataset.field = fields(dataset)
		if (dataset.field.isEmpty()) throw new ExceptionGETL("Required fields description with dataset")

		Map datasetParams = dataset.params
		def header = BoolUtils.IsValue([params.header, datasetParams.header], false)
		Number offsetRows = dataset.params.currentRowIndex?:0
		Number offsetCells = 0
		long countRec = 0
		if (prepareCode != null) prepareCode([])

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

	@Override
	List<Field> fields(Dataset dataset) {

		dataset.params.listName = dataset.params.listName?:0
		if(!fieldsMap.containsKey(dataset.params.listName)){

			if(!workbook) {
				this.readWorkbookAndSheets(dataset)
			}
			Sheet sheet = this.sheetsMap[dataset.params.listName]
			Integer currentRowIndex = dataset.params.currentRowIndex ?:0

			List<Cell> cells = WorkbookUtil.getCellsForSheet(currentRowIndex, sheet)
			List<Field> fields = cells.collect { Cell cell ->
				new Field(name: cellValue(cell), type: cellType(cell))
			}
			fieldsMap[dataset.params.listName] = fields
		}

		return fieldsMap[dataset.params.listName]
	}

	/**
	 * Calculates String value of a Cell
	 * @param cell
	 * @param dataset
	 * @param columnIndex
	 * @return
	 */
	@Override
	private static getCellValue(final Cell cell, final Dataset dataset, final int columnIndex) {
		cell.setCellType(Cell.CELL_TYPE_STRING)
		return cell.stringCellValue
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
	 * Initialization for TDSExcelDriver#workbook field
	 * and TDSExcelDriver#sheetsMap
	 * @param dataset the dataset used in getl.excel.ExcelConnection
	 * @return an instance with the Workbook created from dataset content
	 * @see getl.excel.ExcelConnection
	 */
	protected Workbook readWorkbookAndSheets(Dataset dataset){

		if(!workbook){
			String path = dataset.connection.params.path
			String fileName = dataset.connection.params.fileName
			String fullPath = FileUtils.ConvertToDefaultOSPath(path + File.separator + fileName)

			if (!path) throw new ExceptionGETL("Required \"path\" parameter with connection")
			if (!fileName) throw new ExceptionGETL("Required \"fileName\" parameter with connection")
			if (!FileUtils.ExistsFile(fullPath)) throw new ExceptionGETL("File \"${fileName}\" doesn't exists in \"${path}\"")

			workbook = getWorkbookType(fullPath)
			sheetsMap = WorkbookUtil.getSheetsMap(workbook)
		}

		return workbook
	}

	/**
	 * Check if a Workbook instance has a Sheet based on a sheet name
	 * @param dataset
	 * @param listName
	 * @return
	 */
	boolean hasSheet(Dataset dataset, String listName) {
		if(!sheetsMap){
			readWorkbookAndSheets(dataset)
		}
		return  sheetsMap.containsKey(listName)
	}

	/**
	 * Check if a Workbook instance has a Sheet based on an ordinal sheet number
	 * @param dataset
	 * @param listName
	 * @return
	 */
	boolean hasSheet(Dataset dataset, int sheetNumber) {
		if(!sheetsMap){
			readWorkbookAndSheets(dataset)
		}
		return  sheetsMap.containsKey(sheetNumber)
	}

	/**
	 * Lookups a Sheet instance based on a listName
	 * @param dataSet
	 * @param workbook
	 * @param listName
	 * @return
	 */
	private Sheet getSheetFromWorkbook(Dataset dataset, Workbook workbook, String listName) {
		return WorkbookUtil.getSheetFromWorkbook(workbook, listName)
	}

	/**
	 * Lookups a Sheet instance based on a sheetNumber
	 * @param dataSet
	 * @param workbook
	 * @param listName
	 * @return
	 */
	private Sheet getSheetFromWorkbook(Dataset dataset, Workbook workbook, int sheetNumer) {
		Sheet sheet = WorkbookUtil.getSheetFromWorkbookAt(workbook, sheetNumer)
		dataset.params.listName = WorkbookUtil.getSheetName(workbook, sheetNumer)
		return sheet
	}
}
