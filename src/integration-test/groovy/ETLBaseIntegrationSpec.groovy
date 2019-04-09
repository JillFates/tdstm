import com.tdsops.etl.DataSetFacade
import com.tdsops.etl.ETLDomain
import com.tdsops.etl.ETLFieldsValidator
import com.tdsops.etl.QueryResult
import com.tdsops.tm.enums.domain.AssetClass
import getl.csv.CSVConnection
import getl.csv.CSVDataset
import getl.utils.FileUtils
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.common.CustomDomainService
import spock.lang.Specification

abstract class ETLBaseIntegrationSpec extends Specification{

	/**
	 * Builds a list of Mock Room using this fields order
	 * ['id', 'project', 'roomName', 'location', 'roomDepth', 'roomWidth', 'address', 'city', 'stateProv', 'postalCode']
	 * @param valuesList
	 * @return a list of Mock(Room)
	 */
	List<Room> buildRooms(List<List<?>> valuesList){
		return valuesList.collect { List<?> values ->
			Room room = new Room()
			room.project = values[0]
			room.roomName = values[1]
			room.location = values[2]
			room.roomDepth = values[3]
			room.roomWidth = values[4]
			room.address = values[5]
			room.city = values[6]
			room.stateProv = values[7]
			room.postalCode = values[8]
			room.save()
			room
		}
	}

	/**
	 * Builds a list of Mock Room using this fields order
	 * ['project', 'room', 'manufacturer', 'model', 'location', 'front', 'source', 'roomX', 'roomY', 'powerA', 'powerB', 'powerC', 'rackType'],
	 * @param valuesList
	 * @return a list of Mock(Rack)
	 */
	List<Rack> buildRacks(List<List<?>> valuesList){
		return valuesList.collect { List<?> values ->
			Rack rack = new Rack()
			rack.project = values[0]
			rack.room = values[1]
			rack.manufacturer = values[2]
			rack.model = values[3]
			rack.location = values[4]
			rack.front = values[5]
			rack.source = values[6]
			rack.roomX = values[7]
			rack.roomY = values[8]
			rack.powerA = values[9]
			rack.powerB = values[10]
			rack.powerC = values[11]
			rack.rackType = values[12]
			rack.save()
			rack
		}
	}

	/**
	 * Builds a spec structure used to validate 'asset' fields
	 * @param field
	 * @param label
	 * @param type
	 * @param required
	 * @return a map with the correct fieldSpec format
	 */
	private Map<String, ?> buildFieldSpec(String field, String label, String type = "String", Integer required = 0){
		return [
			constraints: [
				required: required
			],
			control    : type,
			default    : '',
			field      : field,
			imp        : 'U',
			label      : label,
			order      : 0,
			shared     : 0,
			show       : 0,
			tip        : "",
			udf        : 0
		]
	}

	/**
	 * Builds a CSV dataSet from a csv content
	 * @param csvContent
	 * @return
	 */
	private List buildCSVDataSet(CharSequence csvContent){

		def (String fileName, OutputStream sixRowsDataSetOS) = fileSystemService.createTemporaryFile('unit-test-', 'csv')
		sixRowsDataSetOS << csvContent
		sixRowsDataSetOS.close()

		String fullName = fileSystemService.getTemporaryFullFilename(fileName)

		CSVConnection csvCon = new CSVConnection(config: "csv", path: FileUtils.PathFromFile(fullName))
		CSVDataset dataSet = new CSVDataset(connection: csvCon, fileName: FileUtils.FileName(fullName), header: true)

		return [fileName, new DataSetFacade(dataSet)]
	}

	/**
	 * Creates an instance of DomainClassFieldsValidator with all the fields spec correctly set.
	 * @return an intance of DomainClassFieldsValidator
	 */
	protected ETLFieldsValidator createDomainClassFieldsValidator(){
		ETLFieldsValidator validator = new ETLFieldsValidator()
		List<Map<String, ?>> commonFieldsSpec = buildFieldSpecsFor(CustomDomainService.COMMON)

		validator.addAssetClassFieldsSpecFor(ETLDomain.Asset, commonFieldsSpec)
		validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION) + commonFieldsSpec)
		validator.addAssetClassFieldsSpecFor(ETLDomain.Device, buildFieldSpecsFor(AssetClass.DEVICE) + commonFieldsSpec)
		validator.addAssetClassFieldsSpecFor(ETLDomain.Storage, buildFieldSpecsFor(AssetClass.STORAGE) + commonFieldsSpec)
		validator.addAssetClassFieldsSpecFor(ETLDomain.Database, buildFieldSpecsFor(AssetClass.DATABASE) + commonFieldsSpec)
		validator.addAssetClassFieldsSpecFor(ETLDomain.Dependency, buildFieldSpecsFor(ETLDomain.Dependency) + commonFieldsSpec)

		return validator
	}

	private List<Map<String, ?>> buildFieldSpecsFor(def asset){

		List<Map<String, ?>> fieldSpecs = []
		switch(asset) {
			case AssetClass.APPLICATION:
				fieldSpecs = [
					buildFieldSpec('appFunction', 'Function', 'String'),
					buildFieldSpec('appOwner', 'App Owner', 'Person'),
					buildFieldSpec('appSource', 'Source', 'String'),
					buildFieldSpec('appTech', 'Technology', 'String'),
					buildFieldSpec('appVendor', 'Vendor', 'String'),
					buildFieldSpec('appVersion', 'Version', 'String'),
					buildFieldSpec('businessUnit', 'Business Unit', 'String'),
					buildFieldSpec('criticality', 'Criticality', 'InList'),
					buildFieldSpec('drRpoDesc', 'DR RPO', 'String'),
					buildFieldSpec('drRtoDesc', 'DR RTO', 'String'),
					buildFieldSpec('latency', 'Latency OK', 'YesNo'),
					buildFieldSpec('license', 'License', 'String'),
					buildFieldSpec('maintExpDate', 'Maint Expiration', 'Date'),
					buildFieldSpec('retireDate', 'Retire Date', 'Date'),
					buildFieldSpec('shutdownBy', 'Shutdown By', 'String'),
					buildFieldSpec('shutdownDuration', 'Shutdown Duration', 'Number'),
					buildFieldSpec('shutdownFixed', 'Shutdown Fixed', 'Number'),
					buildFieldSpec('sme', 'SME1', 'Person'),
					buildFieldSpec('sme2', 'SME2', 'Person'),
					buildFieldSpec('startupBy', 'Startup By', 'String'),
					buildFieldSpec('startupDuration', 'Startup Duration', 'Number'),
					buildFieldSpec('startupFixed', 'Startup Fixed', 'Number'),
					buildFieldSpec('startupProc', 'Startup Proc OK', 'YesNo'),
					buildFieldSpec('testingBy', 'Testing By', 'String'),
					buildFieldSpec('testingDuration', 'Testing Duration', 'Number'),
					buildFieldSpec('testingFixed', 'Testing Fixed', 'Number'),
					buildFieldSpec('testProc', 'Test Proc OK', 'YesNo'),
					buildFieldSpec('url', 'URL', 'String'),
					buildFieldSpec('useFrequency', 'Use Frequency', 'String'),
					buildFieldSpec('userCount', 'User Count', 'String'),
					buildFieldSpec('userLocations', 'User Locations', 'String'),
					buildFieldSpec('custom1', 'Network Interfaces', 'String'),
					buildFieldSpec('custom2', 'SLA Name', 'String'),
					buildFieldSpec('custom3', 'Cost Basis', 'String'),
					buildFieldSpec('custom4', 'OPS Manual', 'String'),
					buildFieldSpec('custom5', 'DR Plan', 'String'),
					buildFieldSpec('custom6', 'App Code', 'String'),
					buildFieldSpec('custom7', 'Latency Timing', 'String'),
					buildFieldSpec('custom8', 'App ID', 'String'),
					buildFieldSpec('custom9', 'Backup Plan Complete', 'String'),
					buildFieldSpec('custom10', 'Custom10', 'String'),
					buildFieldSpec('custom11', 'Custom11', 'String'),
					buildFieldSpec('custom12', 'Custom12', 'String'),
				]
				break
			case AssetClass.DATABASE:
				fieldSpecs = [
					buildFieldSpec('dbFormat', 'Format', 'String'),
					buildFieldSpec('retireDate', 'Retire Date', 'Date'),
					buildFieldSpec('size', 'Size', 'String'),
					buildFieldSpec('scale', 'Scale', 'String'),
					buildFieldSpec('maintExpDate', 'Maint Expiration', 'Date'),
					buildFieldSpec('rateOfChange', 'Rate Of Change', 'Number'),
					buildFieldSpec('custom1', 'Network Interfaces', 'String'),
					buildFieldSpec('custom2', 'SLA Name', 'String'),
					buildFieldSpec('custom3', 'Cost Basis', 'String'),
					buildFieldSpec('custom4', 'OPS Manual', 'String'),
					buildFieldSpec('custom5', 'DR Plan', 'String'),
					buildFieldSpec('custom6', 'App Code', 'String'),
					buildFieldSpec('custom7', 'Latency Timing', 'String'),
					buildFieldSpec('custom8', 'App ID', 'String'),
					buildFieldSpec('custom9', 'Backup Plan Complete', 'String'),
					buildFieldSpec('custom10', 'Custom10', 'String'),
					buildFieldSpec('custom11', 'Custom11', 'String'),
					buildFieldSpec('custom12', 'Custom12', 'String'),
				]
				break
				break
			case AssetClass.DEVICE:
				fieldSpecs = [
					buildFieldSpec('assetTag', 'Asset Tag', 'String'),
					buildFieldSpec('assetType', 'Device Type', 'AssetType'),
					buildFieldSpec('cart', 'Cart', 'String'),
					buildFieldSpec('ipAddress', 'IP Address', 'String'),
					buildFieldSpec('maintExpDate', 'Maint Expiration', 'Date'),
					buildFieldSpec('manufacturer', 'Manufacturer', 'String'),
					buildFieldSpec('model', 'Model', 'String'),
					buildFieldSpec('os', 'OS', 'String'),
					buildFieldSpec('priority', 'Priority', 'Options.Priority'),
					buildFieldSpec('railType', 'Rail Type', 'InList'),
					buildFieldSpec('rateOfChange', 'Rate Of Change', 'Number'),
					buildFieldSpec('retireDate', 'Retire Date', 'Date'),
					buildFieldSpec('scale', 'Scale', 'String'),
					buildFieldSpec('serialNumber', 'Serial #', 'String'),
					buildFieldSpec('shelf', 'Shelf', 'String'),
					buildFieldSpec('shortName', 'Alternate Name', 'String'),
					buildFieldSpec('size', 'Size', 'Number'),
					buildFieldSpec('sourceBladePosition', 'Source Blade Position', 'Number'),
					buildFieldSpec('sourceChassis', 'Source Chassis', 'Chassis.S'),
					buildFieldSpec('locationSource', 'Source Location', 'Location.S'),
					buildFieldSpec('rackSource', 'Source Rack', 'Rack.S'),
					buildFieldSpec('sourceRackPosition', 'Source Position', 'Number'),
					buildFieldSpec('roomSource', 'Source Room', 'Room.S'),
					buildFieldSpec('targetBladePosition', 'Target Blade Position', 'Number'),
					buildFieldSpec('targetChassis', 'Target Chassis', 'Chassis.T'),
					buildFieldSpec('locationTarget', 'Target Location', 'Location.T'),
					buildFieldSpec('rackTarget', 'Target Rack', 'Rack.T'),
					buildFieldSpec('targetRackPosition', 'Target Position', 'Number'),
					buildFieldSpec('roomTarget', 'Target Room', 'Room.T'),
					buildFieldSpec('truck', 'Truck', 'String'),
					buildFieldSpec('custom1', 'Network Interfaces', 'String'),
					buildFieldSpec('custom2', 'SLA Name', 'String'),
					buildFieldSpec('custom3', 'Cost Basis', 'String'),
					buildFieldSpec('custom4', 'OPS Manual', 'String'),
					buildFieldSpec('custom5', 'DR Plan', 'String'),
					buildFieldSpec('custom6', 'App Code', 'String'),
					buildFieldSpec('custom7', 'Latency Timing', 'String'),
					buildFieldSpec('custom8', 'App ID', 'String'),
					buildFieldSpec('custom9', 'Backup Plan Complete', 'String'),
					buildFieldSpec('custom10', 'Custom10', 'String'),
					buildFieldSpec('custom11', 'Custom11', 'String'),
					buildFieldSpec('custom12', 'Custom12', 'String'),
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
					buildFieldSpec('assetName', 'Name', 'String'),
					buildFieldSpec('description', 'Description', 'String'),
					buildFieldSpec('environment', 'Environment', 'Options.Environment'),
					buildFieldSpec('externalRefId', 'External Ref Id', 'String'),
					buildFieldSpec('lastUpdated', 'Modified Date', 'String'),
					buildFieldSpec('moveBundle', 'Bundle', 'String'),
					buildFieldSpec('planStatus', 'Plan Status', 'Options.PlanStatus'),
					buildFieldSpec('supportType', 'Support', 'String'),
					buildFieldSpec('validation', 'Validation', 'InList'),
					buildFieldSpec('assetClass', 'Asset Class', 'String'),
				]
				break
			case AssetClass.STORAGE:
				fieldSpecs = [
					buildFieldSpec('LUN', 'LUN', 'String'),
					buildFieldSpec('fileFormat', 'Format', 'String'),
					buildFieldSpec('size', 'Size', 'String'),
					buildFieldSpec('rateOfChange', 'Rate Of Change', 'Number'),
					buildFieldSpec('scale', 'Scale', 'String'),
					buildFieldSpec('custom1', 'Network Interfaces', 'String'),
					buildFieldSpec('custom2', 'SLA Name', 'String'),
					buildFieldSpec('custom3', 'Cost Basis', 'String'),
					buildFieldSpec('custom4', 'OPS Manual', 'String'),
					buildFieldSpec('custom5', 'DR Plan', 'String'),
					buildFieldSpec('custom6', 'App Code', 'String'),
					buildFieldSpec('custom7', 'Latency Timing', 'String'),
					buildFieldSpec('custom8', 'App ID', 'String'),
					buildFieldSpec('custom9', 'Backup Plan Complete', 'String'),
					buildFieldSpec('custom10', 'Custom10', 'String'),
					buildFieldSpec('custom11', 'Custom11', 'String'),
					buildFieldSpec('custom12', 'Custom12', 'String'),
				]
				break
		}

		return fieldSpecs
	}

	/**
	 * Assertions for a {@code QueryResult} instance
	 * //TODO dcorrea add an example
	 * @param queryResult
	 * @param domain
	 * @param values
	 */
	static boolean assertQueryResult(QueryResult queryResult, ETLDomain domain, List<List<Object>> values) {
		assert queryResult.domain == domain.name()

		queryResult.criteria.eachWithIndex { Map map, int i ->
			assert map['propertyName'] == values[i][0]
			assert map['operator'] == values[i][1]
			assert map['value'] == values[i][2]
		}

		return true
	}
}
