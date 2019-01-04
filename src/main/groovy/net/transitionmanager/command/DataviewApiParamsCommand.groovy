package net.transitionmanager.command

import com.tdssrc.grails.StringUtil

/**
 * The DataviewApiParamsCommand is used to filter API dataviews requests
 */
@grails.validation.Validateable
class DataviewApiParamsCommand implements CommandObject {

	int offset = 0
	/**
	 * The API should be able to call the endpoint and receive ALL records without pagination but the API should support pagination if the consumer of the API specifies an offset/size.
	 */
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
				obj.filterParams = val.collect { new DataviewApiFilterParam(content: it) }

				List<DataviewApiFilterParam> filters = obj.filterParams.findAll{ !it.validate() }
				if(filters.isEmpty()){
					return true
				} else {
					return ['dataviewApiParamsCommand.filter.error.message', filters*.content]
				}

			}
		}
		filterParams nullable: true
	}
}

/**
 * Used to represent Dataview API command filters structure:
 * <pre>
 *   common_environment=Production&filter=assetName=PDV*
 * </pre>
 */
@grails.validation.Validateable
class DataviewApiFilterParam {

	static final String FILTER_PARAMETER_SEPARATOR_CHARACTER = '='
	static final String FIELD_NAME_SEPARATOR_CHARACTER = '_'

	String content
	String domain
	String fieldName
	String filter

	/**
	 * It constructs an instance of {@code DataviewApiFilterParam}
	 * splitting String param by {@code FILTER_PARAMETER_SEPARATOR_CHARACTER}
	 * and then by {@code FIELD_NAME_SEPARATOR_CHARACTER}
	 * <dl>
	 * 	<dt>
	 * 	    <pre>
	 * 			common_environment=Production ==  DataviewApiFilterParam(domain: 'common', fieldName: 'environment', filter: 'Production')
	 * 			assetName=Production ==  DataviewApiFilterParam(domain: null, fieldName: 'assetName', filter: 'Production')
	 * 	    </pre>
	 * 	</dt>
	 *
	 * </dl>
	 * @param stringValue
	 */
	static constraints = {
		content nullable: false, blank: false, validator: { val, obj ->

			if (!val.contains(FILTER_PARAMETER_SEPARATOR_CHARACTER)) {
				return false
			}

			List<String> filter = val.split(FILTER_PARAMETER_SEPARATOR_CHARACTER) as List
			if (filter?.size() != 2 || StringUtil.isBlank(filter[0]) || StringUtil.isBlank(filter[1])) {
				return false
			}

			obj.domain = null
			obj.fieldName = filter[0]
			obj.filter = filter[1]

			if (obj.fieldName.contains(FIELD_NAME_SEPARATOR_CHARACTER)) {
				List<String> parts = obj.fieldName.split(FIELD_NAME_SEPARATOR_CHARACTER) as List
				if (parts?.size() != 2 || StringUtil.isBlank(parts[0]) || StringUtil.isBlank(parts[1])) {
					return false
				}
				obj.domain = parts[0]
				obj.fieldName = parts[1]
			}

			return true
		}
		domain nullable: true
		fieldName nullable: true
		filter nullable: true
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
