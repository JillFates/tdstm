package net.transitionmanager.command
/**
 * The DataviewApiParamsCommand is used to filter API dataviews requests
 */
@grails.validation.Validateable
class DataviewApiParamsCommand implements CommandObject {


    int offset = 0
    int limit = 25
	/**
	 * Defined by the following structure:
	 * filter=environment=Production&filter=assetName=PDV*
	 */
    List<String> filter

	List<DataviewApiFilterParam> filterParams = []

    static constraints = {
        offset min: 0
        limit min: 0, max: 100
		filter nullable: true, blank: true, validator: { val, obj ->
			if(val){
				obj.filterParams = []
				val.each { String param ->
					filterParams.add(new DataviewApiFilterParam(param))
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
	static final String FILTER_PARAMETER_FILEDNAME_SEPARATOR_CHARACTER = '.'

	String domain
	String fieldName
	String filter

	DataviewApiFilterParam(String stringValue){
		def (String key, String value) = stringValue.split(FILTER_PARAMETER_SEPARATOR_CHARACTER)
		this.filter = value
		//TODO: add exception if key or value are null??
		def (String root, String path) = key.split(FILTER_PARAMETER_FILEDNAME_SEPARATOR_CHARACTER)
		if(path){
			this.domain = root
			this.fieldName = path
		} else{
			this.domain = null
			this.fieldName = root
		}
	}

	/**
	 * Check if Dataview column spec coinciden with current {@code DataviewApiFilterParam} instance
	 *
	 * @param columnSpec a Map instance with column spec content
	 * @return true if current instance of {@code DataviewApiFilterParam}
	 *			concidence with a dataview column spec
	 */
	Boolean matchWithDataviewColumnSpec(Map columnSpec){
		if(domain){
			return domain.equalsIgnoreCase(columnSpec.domain) && (columnSpec.property == fieldName || columnSpec.label == fieldName)
		} else {
			return columnSpec.property == fieldName || columnSpec.label == fieldName
		}
	}
}