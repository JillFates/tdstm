package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic
import net.transitionmanager.command.DataviewApiFilterParam
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.exception.InvalidParamException

/**
 * Defines extra filter structure for {@code Dataview}
 */
@CompileStatic
class ExtraFilter {

	String domain
	/**
	 * This field defines a property for extra filters.
	 * It could have different meaning based on the following rules: <br/>
	 * 1)If it is a valid field spec name, it could be used for filtering using filter value
	 * <pre>
	 *     {"property" : "assetName", "filter": "FOOBAR"}
	 * </pre>
	 * 2) If it is a specific named filter
	 * <pre>
	 *     {"property" : "_filter", "filter": "physicalServer"}
	 *     {"property" : "_event", "filter": "364"}
	 *     {"property" : "_ufp", "filter": "true"}
	 * </pre>
	 * @see ExtraFilterBuilder#build(java.util.List, net.transitionmanager.dataview.FieldSpecProject)
	 */
	String property
	String filter
	FieldSpec fieldSpec

	ExtraFilter(String property, String filter, String domain = null, FieldSpec fieldSpec = null) {
		this.domain = domain
		this.property = property
		this.filter = filter
		this.fieldSpec = fieldSpec
	}

	boolean isAssetField(){
		return this.fieldSpec != null
	}

	static ExtraFilterBuilder builder() {
		return new ExtraFilterBuilder()
	}

}
/**
 * Builder class logic for building an instance of {@code ExtraFilter}.
 * It can defines if extra filter is:
 * 1) A named filter:
 * <pre>
 *     {"property" : "_ufp", "filter": "true"}
 * </pre>
 * A custom filter:
 * <pre>
 *     {"property" : "_event", "filter": "364"}
 * </pre>
 * Or a simple asset field filter:
 * <pre>
 *     {"property" : "assetName", "filter": "FOOBAR"}
 *     {"property" : "common_assetName", "filter": "FOOBAR"}
 *     {"property" : "appTech", "filter": "Apple"}
 *     {"property" : "application_appTech", "filter": "Apple"}
 * </pre>
 *
 *
 */
class ExtraFilterBuilder {

	String property
	String filter

	/**
	 * Builder implementation for {@code ExtraFilter} instances.
	 * After configuring {@code ExtraFilter#property} and {@code ExtraFilter#filter},
	 * this Builder implementation creates a new instance of {@code ExtraFilter}
	 * based on the following rules:
	 *  1) A named filter:
	 *  <pre>
	 *  	{"property" : "_ufp", "filter": "true"}
	 *  </pre>
	 *  A custom filter:
	 *  <pre>
	 *  	{"property" : "_event", "filter": "364"}
	 *  </pre>
	 *	Or a simple asset field filter:
	 *  <pre>
	 *		{"property" : "assetName", "filter": "FOOBAR"}
	 *  	{"property" : "common_assetName", "filter": "FOOBAR"}
	 *  	{"property" : "appTech", "filter": "Apple"}
	 *  	{"property" : "application_appTech", "filter": "Apple"}
	 *  </pre>
	 *
	 * @param domains
	 * @param fieldSpecProject
	 * @return
	 */
	ExtraFilter build(List<String> domains, FieldSpecProject fieldSpecProject) {

		if (this.property in ['_filter', '_event', '_ufp'] ) {
			return new ExtraFilter(this.property, this.filter)
		} else if (this.property.contains('_')) {
			def (String domain, String field) = this.property.split(DataviewApiFilterParam.FIELD_NAME_SEPARATOR_CHARACTER) as List<String>
			FieldSpec fieldSpec = fieldSpecProject.getFieldSpec(domain, field)
			return new ExtraFilter(field, this.filter, domain, fieldSpec)
	 	} else {
			def (String domain, FieldSpec fieldSpec) = fieldSpecProject.lookupFieldSpec(domains, this.property)
			if (!fieldSpec){
				throw new InvalidParamException("Field Spec '$property' not found")
			}
			return new ExtraFilter(fieldSpec.field, this.filter, domain, fieldSpec)
		}
	}

	ExtraFilterBuilder withProperty(String property) {
		this.property = property
		return this
	}

	ExtraFilterBuilder withFilter(String filter) {
		this.filter = filter
		return this
	}
}
