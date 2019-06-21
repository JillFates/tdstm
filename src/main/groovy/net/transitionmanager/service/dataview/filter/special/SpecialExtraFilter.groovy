package net.transitionmanager.service.dataview.filter.special

import groovy.transform.CompileStatic
import net.transitionmanager.project.Project

@CompileStatic
abstract class SpecialExtraFilter {

	String property
	/**
	 * This field defines a property for extra filters.
	 * It could have different meaning based on the following rules: <br/>
	 * 1)If it is a valid field spec name, it could be used for filtering using filter value
	 * <pre>
	 *	{"property" : "assetName", "filter": "FOOBAR"}* </pre>
	 * 2) If it is a special filter
	 * <pre>
	 *	{"property" : "_assetType", "filter": "physicalServer"}
	 *	{"property" : "_event", "filter": "364"}
	 *	{"property" : "_planMethod", "filter": "Unknown"}
	 * </pre>
	 * @see net.transitionmanager.service.dataview.ExtraFilterBuilder#build(java.util.List, net.transitionmanager.dataview.FieldSpecProject)
	 */
	String filter

	abstract Map<String, ?> generateHQL(Project project)
}
