package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic

@CompileStatic
class NamedExtraFilter {

	String property
	/**
	 * This field defines a property for extra filters.
	 * It could have different meaning based on the following rules: <br/>
	 * 1)If it is a valid field spec name, it could be used for filtering using filter value
	 * <pre>
	 *{"property" : "assetName", "filter": "FOOBAR"}* </pre>
	 * 2) If it is a specific named filter
	 * <pre>
	 *{"property" : "_filter", "filter": "physicalServer"}*{"property" : "_event", "filter": "364"}*{"property" : "_ufp", "filter": "true"}* </pre>
	 * @see ExtraFilterBuilder#build(java.util.List, net.transitionmanager.dataview.FieldSpecProject)
	 */
	String filter
}
