package net.transitionmanager.command

import grails.util.Pair

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
	 * environment=Production&filter=assetName=PDV*
	 */
    List<String> filter

	List<Pair<String, String>> filterParams = []

    static constraints = {
        offset min: 0
        limit min: 0, max: 100
		filter nullable: true, blank: true, validator: { val, obj ->
			if(val){
				obj.filterParams = []
				val.each { String param ->
					def (String key, String value) = param.split(FILTER_PARAMETER_SEPARATOR_CHARACTER)
					obj.filterParams.add(new Pair<String, String>(key, value))
				}
				return true
			}
		}
		filterParams nullable: true
    }
}