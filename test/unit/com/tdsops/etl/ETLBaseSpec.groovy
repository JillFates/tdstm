package com.tdsops.etl

import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.WorkbookUtil
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.excel.ExcelConnection
import getl.excel.ExcelDataset
import getl.excel.ExcelDriver
import getl.utils.FileUtils
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.CustomDomainService
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFRow
import spock.lang.Specification

abstract class ETLBaseSpec extends Specification {

	/**
	 * Builds a list of Mock Room using this fields order
	 * ['id', 'project', 'roomName', 'location', 'roomDepth', 'roomWidth', 'address', 'city', 'stateProv', 'postalCode']
	 * @param valuesList
	 * @return a list of Mock(Room)
	 */
	List<Room> buildRooms(List<List<?>> valuesList) {
		return valuesList.collect { List<?> values ->
			Room room = Mock()
			room.getId() >> values[0]
			room.getProject() >> values[1]
			room.getRoomName() >> values[2]
			room.getLocation() >> values[3]
			room.getRoomDepth() >> values[4]
			room.getRoomWidth() >> values[5]
			room.getAddress() >> values[6]
			room.getCity() >> values[7]
			room.getStateProv() >> values[8]
			room.getPostalCode() >> values[9]
			room
		}
	}

	/**
	 * Builds a list of Mock Room using this fields order
	 * ['id', 'project', 'room', 'manufacturer', 'model', 'location', 'front', 'source', 'roomX', 'roomY', 'powerA', 'powerB', 'powerC', 'rackType'],
	 * @param valuesList
	 * @return a list of Mock(Rack)
	 */
	List<Rack> buildRacks(List<List<?>> valuesList) {
		return valuesList.collect { List<?> values ->
			Rack rack = Mock()
			rack.getId() >> values[0]
			rack.getProject() >> values[1]
			rack.getRoom() >> values[2]
			rack.getManufacturer() >> values[3]
			rack.getModel() >> values[4]
			rack.getLocation() >> values[5]
			rack.getFront() >> values[6]
			rack.getSource() >> values[7]
			rack.getRoomX() >> values[8]
			rack.getRoomY() >> values[9]
			rack.getPowerA() >> values[10]
			rack.getPowerB() >> values[11]
			rack.getPowerC() >> values[12]
			rack.getRackType() >> values[13]
			rack
		}
	}

	/**
	 * Builds a spec structure used to validate asset fields
	 * @param field
	 * @param label
	 * @param type
	 * @param required
	 * @return a map with the correct fieldSpec format
	 */
	private Map<String, ?> buildFieldSpec(String field, String label, String type = "String", Integer required = 0) {
		return [
			constraints: [
				required: required
			],
			control: type,
			default: '',
			field: field,
			imp: 'U',
			label: label,
			order: 0,
			shared: 0,
			show: 0,
			tip: "",
			udf: 0
		]
	}


	/**
	 * Builds a SpreadSheet dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	private List buildSpreadSheetDataSet(String sheetName, String sheetContent) {

		def (String fileName, OutputStream outputStream) = service.createTemporaryFile('unit-test-', 'xlsx')
		Workbook workbook = WorkbookUtil.createWorkbook('xlsx')

		// Getting the Sheet at index zero
		Sheet sheet = workbook.createSheet(sheetName)
		sheetContent.readLines().eachWithIndex {String line, int rowNumber ->
			XSSFRow currentRow = sheet.createRow(rowNumber)
			line.split(",").eachWithIndex{ String cellContent, int columnNumber ->
				currentRow.createCell(columnNumber).setCellValue(cellContent)
			}
		}

		WorkbookUtil.saveToOutputStream(workbook, outputStream)

		ExcelConnection con = new ExcelConnection(
			path: service.temporaryDirectory,
			fileName: fileName,
			driver: TDSExcelDriver)
		ExcelDataset dataSet = new ExcelDataset(connection: con, header: true)

		return [fileName, new DataSetFacade(dataSet)]
	}

	/**
	 * Builds a CSV dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	private List buildCSVDataSet(String csvContent) {

		def (String fileName, OutputStream dataSetOS) = service.createTemporaryFile('unit-test-', 'csv')
		dataSetOS << csvContent
		dataSetOS.close()

		String fullName = service.getTemporaryFullFilename(fileName)

		CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fullName))
		CSVDataset dataSet = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fullName), header: true)

		return [fileName, new DataSetFacade(dataSet)]
	}

	/**
	 * Helper method to create Fields Specs based on Asset definition
	 * @param asset
	 * @return
	 */
	private List<Map<String, ?>> buildFieldSpecsFor(def asset) {

		List<Map<String, ?>> fieldSpecs = []
		switch(asset){
			case AssetClass.APPLICATION:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('appVendor', 'Vendor'),
					buildFieldSpec('assetType', 'AssetType'),
					buildFieldSpec('environment', 'Environment'),
					buildFieldSpec('description', 'Description'),
					buildFieldSpec('assetName', 'Name'),
					buildFieldSpec('assetClass', 'Asset Class'),
				]
				break
			case AssetClass.DATABASE:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('name', 'Name'),
					buildFieldSpec('environment', 'Environment'),
					buildFieldSpec('assetClass', 'Asset Class'),
				]
				break
				break
			case AssetClass.DEVICE:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('location', 'Location'),
					buildFieldSpec('name', 'Name'),
					buildFieldSpec('environment', 'Environment'),
					buildFieldSpec('assetClass', 'Asset Class'),
				]
				break
			case ETLDomain.Dependency:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('assetName', 'AssetName'),
					buildFieldSpec('assetType', 'AssetType'),
					buildFieldSpec('asset', 'Asset'),
					buildFieldSpec('comment', 'Comment'),
					buildFieldSpec('status', 'Status'),
					buildFieldSpec('dataFlowFreq', 'DataFlowFreq'),
					buildFieldSpec('dataFlowDirection', 'DataFlowDirection')
				]
				break
			case CustomDomainService.COMMON:
				fieldSpecs = [
					buildFieldSpec('id', 'Id', 'Number'),
					buildFieldSpec('assetType', 'AssetType'),
					buildFieldSpec('assetName', 'Name'),
					buildFieldSpec('assetClass', 'Asset Class'),
				]
				break
			case AssetClass.STORAGE:

				break
		}

		return fieldSpecs
	}
}
