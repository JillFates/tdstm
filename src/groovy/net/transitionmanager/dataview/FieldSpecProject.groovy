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
class FieldSpecProject {

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
	FieldSpecProject(Map<String, Map> fieldsSpec) {
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
		FieldSpec fieldSpec = null
		String key = assetClassName.toUpperCase()
		if (! fieldsSpecMap.containsKey(key)) {
			throw new InvalidParamException("Domain $assetClassName not found in cache")
		}
		if (fieldsSpecMap[key].containsKey(fieldName)) {
			fieldSpec = fieldsSpecMap[key][fieldName]
		}
		return fieldSpec
	}

	/**
	 * Retrieve a {@code Map} with all customFields for an asset class definition.
	 * @param assetClassName a Class name in AssetEntity hierarchy
	 * @return a {@code Map} with all custom fields
	 * @see FieldSpec#isCustom()
	 */
	Map<String, FieldSpec> getAllCustomFields(String assetClassName){
		Map<String, FieldSpec> allFieldSpecs = fieldsSpecMap.get(assetClassName.toUpperCase())
		return allFieldSpecs.findAll { it.value.isCustom() }
	}
}