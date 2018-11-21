package net.transitionmanager.dataview

import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.InvalidParamException
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

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
@CompileStatic
class FieldSpecCache {

	/*
	 * Contains each domains' individual set of fieldSpec objects where the Map key is the domain name.
	 * The nested map is a the fieldname/FieldSpec
	 */
	Map<String, Map<String, FieldSpec>> fieldsSpecMap = [ : ]

	/**
	 * Creates an instance of FieldSpecMapper using results from
	 * {@code CustomDomainService # fieldSpecsWithCommon} results.
	 * It defines common fields and manages field specs type during a {@code DataviewService # query}
	 */
	FieldSpecCache(Map<String, Map> fieldsSpec) {
		Set keys = fieldsSpec.keySet()
		for (String key in keys) {
			fieldsSpecMap[key] = [:]
			addFieldSpecs(key, fieldsSpec[key])
		}
	}

	/**
	 * Loads a single Domain's Field Specifications Map into the cache.
	 * Each field is converted to a FieldSpec object keyed by the fieldname
	 */
	@CompileStatic(TypeCheckingMode.SKIP)
	private void addFieldSpecs(String key, Map<String, ?> domainFieldsSpec) {
		List<Map<String, ?>> fieldsSpecList = domainFieldsSpec.fields

		for (Map fieldInfo in fieldsSpecList) {
			fieldsSpecMap[key].put(fieldInfo.field, new FieldSpec(fieldInfo))
		}
	}

	/**
	 * Returns an instance of {@code FieldSpec} based on an assetclass definition and a field name
	 * @param assetClassName
	 * @param fieldName
	 * @return
	 */
	FieldSpec getFieldSpec(String assetClassName, String fieldName) {
		String key = assetClassName.toUpperCase()
		if (! fieldsSpecMap.containsKey(key)) {
			throw new InvalidParamException("Domain $assetClassName not found in cache")
		}
		if (! fieldsSpecMap[key].containsKey(fieldName)) {
			throw new InvalidParamException("Field $fieldName of Domain $assetClassName not found in cache")
		}
		return fieldsSpecMap[key][fieldName]
	}
}