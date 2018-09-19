package net.transitionmanager.command

/**
 * The DataviewApiParamsCommand is used to filter API dataviews requests
 */
@grails.validation.Validateable
class DataviewApiParamsCommand implements CommandObject {

	static final String FILTER_PARAMETER_SEPARATOR_CHARACTER = '='

    int offset = 0
    int limit = 25
	/**
	 * Defined by the following structure:
	 * common.environment=Production,common.assetName=PDV*
	 */
    List<String> filters
	List<Map<String, Object>> filterParams = []

    static constraints = {
        offset min: 0
        limit min: 0, max: 100
		filters nullable: true, blank: true, validator: { val, obj ->
			if(val){
				val.each { String param ->
					def (String key, String value) = param.split(FILTER_PARAMETER_SEPARATOR_CHARACTER)
					obj.filterParams.add([(key): value])
				}
				return true
			}
		}
		filterParams nullable: true
    }
}