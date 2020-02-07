package com.tdsops.etl

import com.monitorjbl.xlsx.StreamingReader
import com.monitorjbl.xlsx.impl.StreamingCell
import com.monitorjbl.xlsx.impl.StreamingWorkbook
import com.tdssrc.grails.WorkbookUtil
import getl.data.Dataset
import getl.data.Field
import getl.excel.ExcelDriver
import getl.exception.ExceptionGETL
import getl.utils.BoolUtils
import getl.utils.FileUtils
import getl.utils.ListUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
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
 * @see ExcelDriver* @see TDSExcelDriver#fields(getl.data.Dataset)
 * @see TDSExcelDriver#hasSheet(getl.data.Dataset, int)
 * @see TDSExcelDriver#hasSheet(getl.data.Dataset, String)
 */
class TDSExcelDriver extends ExcelDriver {

	/**
	 * Workbook instance. It's open once using TDSExcelDriver#readWorkbookAndSheets
	 */
	Workbook workbook
	/**
	 * Current {@code Sheet} used in iterations
	 */
	Sheet currentSheet
	/**
	 * Current Row used for iteration on {@code TDSExcelDriver#currentSheet}
	 */
	Row currentRow
	/**
	 * Total amount of row processed for {@code TDSExcelDriver#currentSheet}
	 */
	Integer rowsProcessed = 0

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

		if (!workbook) {
			this.readWorkbookAndSheets(dataset)
		}
		currentSheet = this.sheetsMap[dataset.params.listName]

		dataset.field = fields(dataset)
		if (dataset.field.isEmpty()) throw new ExceptionGETL("Required fields description with dataset")

		Map datasetParams = dataset.params
		def header = BoolUtils.IsValue([params.header, datasetParams.header], false)
		Number offsetRows = dataset.params.currentRowIndex ?: 0

		if (workbook instanceof StreamingWorkbook) {
			// Stremaing processing is moving iterator when fields are calculated,
			// at this point we need to move only the skip rows defined by user
			offsetRows = offsetRows - rowsProcessed
		}

		Number offsetCells = 0
		long countRec = 0
		if (prepareCode != null) prepareCode([])

		def limit = ListUtils.NotNullValue([params.limit, datasetParams.limit, currentSheet.lastRowNum])

		Iterator rows = currentSheet.rowIterator()

		if (offsetRows > 0) {
			(1..(offsetRows)).each {
				if (rows.hasNext()) {
					rows.next()
				}
			}
		}
		int additionalRows = limit + offsetRows + (header ? (1 as int) : (0 as int))

		rows.each { Row row ->
			if (!(workbook instanceof StreamingWorkbook) && row.rowNum >= additionalRows) return
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

		dataset.params.listName = dataset.params.listName ?: 0
		if (!fieldsMap.containsKey(dataset.params.listName)) {

			if (!workbook) {
				this.readWorkbookAndSheets(dataset)
			}
			currentSheet = this.sheetsMap[dataset.params.listName]
			rowsProcessed = 0
			Integer currentRowIndex = dataset.params.currentRowIndex ?: 0

			Iterator rows = currentSheet.rowIterator()
			if (currentRowIndex != 0) (1..currentRowIndex).each {
				rows.next()
				rowsProcessed += 1
			}

			Row row = rows.next()
			rowsProcessed += 1
			dataset.params.currentRowIndex = rowsProcessed
			Iterator cells = row.cellIterator()
			List<Field> fields = []
			cells.each { Cell cell ->
				fields.add(new Field(name: cellValue(cell), type: cellType(cell)))
			}
			fieldsMap[dataset.params.listName] = fields
		}

		return fieldsMap[dataset.params.listName]
	}

	/**
	 * Calculates String value of a Cell
	 * If {@code Cell} is an instance of {@code StreamingCell},
	 * it means we are iterating using Streaming API.
	 * It does not have correct support for {@code Cell#toString} method.
	 * @param cell
	 * @param dataset
	 * @param columnIndex
	 * @return
	 */
	@Override
	private static getCellValue(final Cell cell, final Dataset dataset, final int columnIndex) {
		if (cell instanceof StreamingCell) {
			return cell.stringCellValue
		} else {
			String cellValue = cell.toString()
			// See (TM-16942) When file is 'xls', with NUMERIC formats, It adds .0 at the end.
			if(cell.getCellTypeEnum() == CellType.NUMERIC && cellValue.endsWith('.0')){
				cellValue = cellValue[0..-3]
			}
			return cellValue
		}
	}

	/**
	 * Calculates String value of a Cell
	 * If {@code Cell} is an instance of {@code StreamingCell},
	 * it means we are iterating using Streaming API.
	 * It does not have correct support for {@code Cell#toString} method.
	 * @param cell
	 * @return
	 */
	private String cellValue(Cell cell) {
		if (cell instanceof StreamingCell) {
			return cell.stringCellValue
		} else {
			return cell.toString()
		}
	}

	/**
	 * Calculates Field Type based on Cell type
	 * @param cell
	 * @return
	 */
	private Field.Type cellType(Cell cell) {
		// TODO: DMC: Complete this conversion
		return Field.Type.STRING
	}

	/**
	 * Initialization for TDSExcelDriver#workbook field
	 * and TDSExcelDriver#sheetsMap
	 *
	 * @param dataset the dataset used in getl.excel.ExcelConnection
	 *
	 * @return an instance with the Workbook created from dataset content
	 * @see getl.excel.ExcelConnection
	 */
	protected Workbook readWorkbookAndSheets(Dataset dataset) {

		if (!workbook) {
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
	 * Creates an instance of {@code Workbook} from a filename parameter.
	 * It can prepare 2 different types of {@code Workbook}.
	 * For 'xls' files, It is created using {@code HSSFWorkbook}
	 * <pre>
	 * 		new HSSFWorkbook(new NPOIFSFileSystem(new File(fileName)))
	 * </pre>
	 * For 'xlsx' files, It is created using {@code StreamingReader#builder}
	 * <pre>
	 *  	InputStream is = new FileInputStream(new File(fileName))
	 * 		StreamingReader.builder()
	 * 			.rowCacheSize(100)
	 * 			.bufferSize(4096)
	 * 			.open(is);
	 * </pre>
	 *
	 * @param fileName excel file name
	 * @return an instance of {@code Workbook}
	 */
	private static getWorkbookType(final String fileName) {
		def ext = FileUtils.FileExtension(fileName)
		if (!(new File(fileName).exists())) throw new ExceptionGETL("File '$fileName' doesn't exists")
		if (!(ext in ['xls', 'xlsx'])) throw new ExceptionGETL("'$ext' is not available. Please, use 'xls' or 'xlsx'.")

		switch (ext) {
			case { fileName.endsWith(ext) && ext == 'xlsx' }:
				// For 'xlsx' files, Apache POI it is not very memory efficient.
				// We are using a library for reading Excel files using streaming logic.
				InputStream is = new FileInputStream(new File(fileName))
				StreamingReader.builder()
					.rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
					.bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
					.open(is);
				break
			case { fileName.endsWith(ext) && ext == 'xls' }:
				new HSSFWorkbook(new NPOIFSFileSystem(new File(fileName)))
				break
			default:
				throw new ExceptionGETL("Something went wrong")
		}
	}
	/**
	 * Check if a Workbook instance has a Sheet based on a sheet name
	 *
	 * @param dataset an instance of {@code Dataset}
	 * @param listName
	 *
	 * @return true if {@code TDSExcelDriver#sheetsMap}
	 * 		contains a Sheet for listName parameter
	 */
	boolean hasSheet(Dataset dataset, String listName) {
		if (!sheetsMap) {
			readWorkbookAndSheets(dataset)
		}
		return sheetsMap.containsKey(listName)
	}

	/**
	 * Check if a Workbook instance has a Sheet based on an ordinal sheet number
	 *
	 * @param dataset an instance of {@code Dataset}
	 * @param listName
	 *
	 * @return true if {@code TDSExcelDriver#sheetsMap}
	 * 		contains a Sheet for sheetNumber parameter
	 */
	boolean hasSheet(Dataset dataset, int sheetNumber) {
		if (!sheetsMap) {
			readWorkbookAndSheets(dataset)
		}
		return sheetsMap.containsKey(sheetNumber)
	}
}
