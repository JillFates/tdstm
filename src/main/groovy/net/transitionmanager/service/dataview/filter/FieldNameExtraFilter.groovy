package net.transitionmanager.service.dataview.filter

import groovy.transform.CompileStatic
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.project.Project

@CompileStatic
class FieldNameExtraFilter {

	String domain
	/**
	 * This field defines a property for extra filters.
	 * It could have different meaning based on the following rules: <br/>
	 * <pre>
	 * 		{"property" : "assetName", "filter": "FOOBAR"}
	 * 		{"property" : "common_assetName", "filter": "FOOBAR"}
	 * 		{"property" : "common_moveBundle.id", "filter": "3885"}
	 * 	</pre>
	 * @see net.transitionmanager.service.dataview.ExtraFilterBuilder#build(java.util.List, net.transitionmanager.dataview.FieldSpecProject)
	 */
	String property
	String filter
	/**
	 * Field used to to validate filters like:
	 * "moveBundle.id" or "sme.id"
	 */
	String referenceProperty
	FieldSpec fieldSpec

}
