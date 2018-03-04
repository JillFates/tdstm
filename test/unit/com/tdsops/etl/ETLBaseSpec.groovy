package com.tdsops.etl

import com.tdsops.tm.enums.domain.AssetClass
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.utils.FileUtils
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.service.CustomDomainService
import spock.lang.Specification

abstract class ETLBaseSpec extends Specification {

	void 'test can throw an exception if an domain is not specified'() {
		given:
			def (String fileName, DataSetFacade dataSet) = buildCSVDataSet("""
id
1""".stripIndent())
			ETLProcessor etlProcessor = new ETLProcessor(GroovyMock(Project), dataSet, GroovyMock(DebugConsole), GroovyMock(ETLFieldsValidator))

		when: 'The ETL script is evaluated'
			new GroovyShell(this.class.classLoader, etlProcessor.binding)
				.evaluate("""
					read labels

					iterate {
						extract 1 load id
					}
					""".stripIndent(),
				ETLProcessor.class.name)

		then: 'An ETLProcessorException is thrown'
			ETLProcessorException e = thrown()
			e.message == 'A domain must be specified'
	}

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
	 * Builds a CSV dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	private List buildCSVDataSet(String csvContent) {

		def (String fileName, OutputStream sixRowsDataSetOS) = service.createTemporaryFile('unit-test-', 'csv')
		sixRowsDataSetOS << csvContent
		sixRowsDataSetOS.close()

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
