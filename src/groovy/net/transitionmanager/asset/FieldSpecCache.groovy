package net.transitionmanager.asset

import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.service.CustomDomainService


/**
 * This class converts {@code customDomainService.fieldSpecsWithCommon ( project )}
 * in a fieldsSpecMap like this:
 * <pre>
 * 	[
 * 		id: [
 * 			field: 'id',
 * 			label: 'Id',
 * 			control: Number
 * 		],
 * 		assetName: [
 * 			field: 'assetName',
 * 			label: 'Name',
 * 			control: String
 * 		]
 * 	]
 * </pre>
 */
class FieldSpecCache {

	Map<AssetClass, Map<String, FieldSpec>> fieldsSpecMap = [
		(CustomDomainService.COMMON)   : [:],
		(AssetClass.APPLICATION.name()): [:],
		(AssetClass.DEVICE.name())     : [:],
		(AssetClass.STORAGE.name())    : [:],
		(AssetClass.DATABASE.name())   : [:]
	]

	/**
	 * Creates an instance of FieldSpecMapper using results from
	 * {@code CustomDomainService # fieldSpecsWithCommon} results.
	 * It defines common fields and manages field specs type during a {@code DataviewService # query}
	 */
	FieldSpecCache(Map<String, ?> fieldsSpec) {
		addFieldSpecs(CustomDomainService.COMMON, fieldsSpec)
		addFieldSpecs(AssetClass.APPLICATION.name(), fieldsSpec)
		addFieldSpecs(AssetClass.DEVICE.name(), fieldsSpec)
		addFieldSpecs(AssetClass.STORAGE.name(), fieldsSpec)
		addFieldSpecs(AssetClass.DATABASE.name(), fieldsSpec)
	}

	private void addFieldSpecs(String key, Map<String, ?> fieldsSpec) {
		List<Map<String, ?>> fieldsSpecList = fieldsSpec[key].fields

		fieldsSpecMap[key] = fieldsSpecList.collectEntries {
			[(it.field): new FieldSpec(it)]
		}
	}

	String getType(String assetClass, String fieldName) {
		FieldSpec fieldSpec = fieldsSpecMap[assetClass.toUpperCase()][fieldName]
		return fieldSpec.type
	}

	/**
	 * Returns an instance of {@code FieldSpec} based on an assetclass definition and a field name
	 * @param assetClass
	 * @param fieldName
	 * @return
	 */
	FieldSpec getFieldSpec(String assetClass, String fieldName) {
		return fieldsSpecMap[assetClass.toUpperCase()][fieldName]
	}
}