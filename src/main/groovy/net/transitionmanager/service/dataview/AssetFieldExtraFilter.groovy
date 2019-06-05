package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.project.Project

@CompileStatic
class AssetFieldExtraFilter implements ExtraFilterHqlGenerator {

	String domain
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
	String property
	String filter
	/**
	 * Field used to to validate filters like:
	 * "moveBundle.id" or "sme.id"
	 */
	String referenceProperty
	FieldSpec fieldSpec

	@Override
	Map<String, ?> generateHQL(Project project) {
		throw new RuntimeException('Can not generate HQL directly using an instance of AssetFieldExtraFilter')
	}
}
