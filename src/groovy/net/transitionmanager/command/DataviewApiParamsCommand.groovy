package net.transitionmanager.command
/**
 * The DataviewApiParamsCommand is used to filter API dataviews requests
 */
@grails.validation.Validateable
class DataviewApiParamsCommand implements CommandObject {

	int offset = 0
	int limit
	/**
	 * Defined by the following structure:
	 * filter=environment=Production&filter=assetName=PDV*
	 */
	List<String> filter

	List<DataviewApiFilterParam> filterParams = []

	static constraints = {
		offset min: 0
		limit nullable: true, min: 0, max: Integer.MAX_VALUE
		filter nullable: true, blank: true, validator: { val, obj ->
			if (val) {
				obj.filterParams = []
				val.each { String param ->
					obj.filterParams.add(new DataviewApiFilterParam(param))
				}
				return true
			}
		}
		filterParams nullable: true
	}
}

/**
 * Used to represent Dataview API command filters structure:
 * <pre>
 *   common.environment=Production&filter=assetName=PDV*
 * </pre>
 */
class DataviewApiFilterParam {

	static final String FILTER_PARAMETER_SEPARATOR_CHARACTER = '='
	static final String FILTER_PARAMETER_FIELD_NAME_SEPARATOR_CHARACTER = '.'
	static final String FILTER_PARAMETER_FIELD_NAME_SPLITTER_CHARACTER = '\\.'

	String domain
	String fieldName
	String filter

	/**
	 * It constructs an instance of {@code DataviewApiFilterParam}
	 * splitting String param by {@code FILTER_PARAMETER_SEPARATOR_CHARACTER}
	 * and then by {@code FILTER_PARAMETER_FIELD_NAME_SEPARATOR_CHARACTER}
	 * <dl>
	 * 	<dt>
	 * 	    <pre>
	 * 			common.environment=Production ==  DataviewApiFilterParam(domain: 'common', fieldName: 'environment', filter: 'Production')
	 * 			assetName=Production ==  DataviewApiFilterParam(domain: null, fieldName: 'assetName', filter: 'Production')
	 * 	    </pre>
	 * 	</dt>
	 *
	 * </dl>
	 * @param stringValue
	 */
	DataviewApiFilterParam(String stringValue) {
		def (String key, String value) = stringValue.split(DataviewApiFilterParam.FILTER_PARAMETER_SEPARATOR_CHARACTER)
		this.domain = null
		this.fieldName = key
		this.filter = value

		if (key.contains(DataviewApiFilterParam.FILTER_PARAMETER_FIELD_NAME_SEPARATOR_CHARACTER)) {
			def (String root, String path) = key.split(DataviewApiFilterParam.FILTER_PARAMETER_FIELD_NAME_SPLITTER_CHARACTER)
			this.domain = root
			this.fieldName = path
		}
	}

	/**
	 * Check if Dataview column spec matches with current {@code DataviewApiFilterParam} instance
	 *
	 * @param columnSpec a Map instance with column spec content
	 * @return true if current instance of {@code DataviewApiFilterParam}
	 * 			concidence with a dataview column spec
	 */
	Boolean matchWithDataviewColumnSpec(Map columnSpec) {
		if (this.domain) {
			return this.domain.equalsIgnoreCase(columnSpec.domain) && (columnSpec.property == this.fieldName || columnSpec.label == this.fieldName)
		} else {
			return columnSpec.property == this.fieldName || columnSpec.label == this.fieldName
		}
	}
}