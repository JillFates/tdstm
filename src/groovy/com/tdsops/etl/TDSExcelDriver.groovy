package com.tdsops.etl

import com.tdssrc.grails.WorkbookUtil
import getl.data.Dataset
import getl.data.Field
import getl.excel.ExcelDataset
import getl.excel.ExcelDriver
import getl.exception.ExceptionGETL
import getl.utils.FileUtils
import org.apache.poi.sl.usermodel.Sheet
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFSheet

class TDSExcelDriver extends ExcelDriver {

	Workbook workbook

	@Override
	protected List<Field> fields(Dataset dataset) {

		Workbook workbook = getWorkbook(dataset)
		Integer rowLabels = dataset.params.rowLabels ?:0
		String sheetName = ((ExcelDataset)dataset).listName
		String sheetNumber = dataset.params.sheetNumber ?:0

		if(!sheetName && !sheetNumber){
			throw new ExceptionGETL("Required \"sheet name\" or \"sheet number\" parameter")
		}

		XSSFSheet sheet = WorkbookUtil.getSheetFromWorkbook(workbook, sheetName)
		List<Cell> cells = WorkbookUtil.getCellsForSheet(rowLabels, sheet)
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
		return cell.toString()
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
}
