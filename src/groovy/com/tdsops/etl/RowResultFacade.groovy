package com.tdsops.etl

/**
 * TODO: add docs.
 *
 * 1) Content of {@code RowResultFacade}
 * 2) Content of labelFieldMap parameter
 *
 */
class RowResultFacade {

	private RowResult rowResult
	private Map<String, String> labelFieldMap

	RowResultFacade(RowResult rowResult, Map<String, String> labelFieldMap) {
		this.rowResult = rowResult
		this.labelFieldMap = labelFieldMap
	}

	RowResult getRowResult() {
		return rowResult
	}

	/**
	 * <p>Return property value using {@code RowResult} field</p>
	 * <pre>
	 * iterate {
	 * 	extract 'name' load 'Name'
	 *
	 * 	set assetResultVar with DOMAIN
	 *
	 * 	assert assetResultVar.assetName == 'xraysrv01'
	 * 	assert assetResultVar.Name == 'xraysrv01'
	 * }
	 * </pre>
	 * @param name a property name
	 * @return an Object instance from from {code FieldResult#value}
	 */
	Object getProperty(String fieldNameOrLabel) {

		String fieldName = labelFieldMap.containsKey(fieldNameOrLabel)? labelFieldMap[fieldNameOrLabel]: fieldNameOrLabel
		return rowResult.fields[fieldName]?.value
	}

}
