package com.tdsops.etl

/**
 *

 */
class RowResultFacade {

	private RowResult rowResult

	RowResultFacade(RowResult rowResult) {
		this.rowResult = rowResult
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
	Object getProperty(String name) {

		String fieldName
		if(rowResult.fieldLabelMap.containsKey(name)){
			fieldName = rowResult.fieldLabelMap[name]
		}

		return rowResult.fields[fieldName]?.value
	}

}
