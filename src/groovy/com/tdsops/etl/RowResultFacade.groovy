package com.tdsops.etl

/**
 * <p>This class is used as a Facade implementation for a {@code RowResult} content</p>
 * <p>Every time users use the following command, an instance of {@code RowResultFacade} is created</p>
 * <pre>
 * iterate {
 * 	extract 'name' load 'Name'
 *
 * 	set assetResultVar with DOMAIN
 * }
 * </pre>
 *
 * <p>An instance of {@code RowResultFacade} is compound by 2 objects</p>
 * <ul>
 *     <li> 1) An instance of {@code RowResult}. <br>
 *     		It's used to calculate later, in an instance of {@code DependencyBuilder}
 *     		all the necessary Dependency fields to use in a 'domain Dependency with ...' command.
 *     </li>
 *     <li> 2) A Map with label in keys and field in value.<BR>
 *         This Maps is used when a user tris to use a DOMAIN variable for accessing to the fields or labels.
 *         <pre>
 *             ...
 *             set assetResultVar with DOMAIN
 *			   ...
 *			   assert assetResultVar.assetName == 'xraysrv01'
 *			   assert assetResultVar.Name == 'xraysrv01'
 *         </pre>
 *     </li>
 * </ul>
 * @see RowResultFacade#getProperty(java.lang.String)
 * @see DependencyBuilder#process(java.lang.String, com.tdsops.etl.RowResultFacade, java.lang.String)
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
	 * <p>This method can detect if a parameter is a field or a label
	 * in order to return the correct {@code FieldResult#value}.
	 * To do that, it is using {@code RowResultFacade#labelFieldMap}</p>
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
