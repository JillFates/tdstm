package com.tdsops.etl

import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.common.CustomDomainService

/**
 * <p>Implements {@code ETLFieldsValidator} construction for Test purposes.</p>
 * Use it:
 * <pre>
 * class ETLSpec extends Specification implements FieldSpecValidateableTrait {
 *
 *	....
 *	validator = createDomainClassFieldsValidator()
 * }
 *
 * </pre>
 */
trait FieldSpecValidateableTrait {

	/**
	 * <p>Creates an instance of {@code ETLFieldsValidator} for testing purposes. </p>
	 * <p>It is configured with all the domain Asset fields definitions</p>
	 * @return an instance of {@code ETLFieldsValidator}
	 */
	ETLFieldsValidator createDomainClassFieldsValidator() {
		ETLFieldsValidator validator = new ETLFieldsValidator()
		List<Map<String, ?>> commonFieldsSpec = buildFieldSpecsFor(CustomDomainService.COMMON)

		validator.addAssetClassFieldsSpecFor(ETLDomain.Asset, commonFieldsSpec)
		validator.addAssetClassFieldsSpecFor(ETLDomain.Application, buildFieldSpecsFor(AssetClass.APPLICATION) + commonFieldsSpec)
		validator.addAssetClassFieldsSpecFor(ETLDomain.Device, buildFieldSpecsFor(AssetClass.DEVICE) + commonFieldsSpec)
		validator.addAssetClassFieldsSpecFor(ETLDomain.Storage, buildFieldSpecsFor(AssetClass.STORAGE) + commonFieldsSpec)
		validator.addAssetClassFieldsSpecFor(ETLDomain.Database, buildFieldSpecsFor(AssetClass.DATABASE) + commonFieldsSpec)

		return validator
	}

	/**
	 * <p>It builds all the fields Spec definitions copied from database configuration </p>
	 * <p>It returns a {@code List} of FieldSpec represented by a {@code Map} instance</p>
	 * @param asset an instance of an asset class name
	 * @return a {@code List} of Field Spec represented by a {@code Map}
	 */
	List<Map<String, ?>> buildFieldSpecsFor(Object asset) {

		List<Map<String, ?>> fieldSpecs = []
		switch (asset) {
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
					buildFieldSpec('dependent', 'Dependent'),
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
	 * Builds a spec structure used to validate asset fields
	 * @param field
	 * @param label
	 * @param type
	 * @param required
	 * @return a map with the correct fieldSpec format
	 */
	Map<String, ?> buildFieldSpec(String field, String label, String type = "String", Integer required = 0) {
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
}