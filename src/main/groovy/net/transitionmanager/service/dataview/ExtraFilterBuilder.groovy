package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic
import net.transitionmanager.command.dataview.DataviewApiFilterParam
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.exception.InvalidParamException

/**
 * Builder class logic for building an instance of {@code ExtraFilter}.
 * It can defines if extra filter is:
 * 1) A custom extra filter:
 * <pre>
 * 	{
 * 		"property" : "moveBundle.id",
 * 		"filter": "3239"
 * 	}
 * 	{
 * 		"property" : "_event.id",
 * 		"filter": "364"
 * 	}
 * </pre>
 * 2) Or a simple asset field filter:
 * <pre>
 *	{
 *		"property" : "assetName",
 *		"filter": "FOOBAR"
 *	}
 *	{
 *		"property" : "common_assetName",
 *		"filter": "FOOBAR"
 *	}
 *	{
 *		"property" : "appTech",
 *		"filter": "Apple"
 *	}
 *	{
 *		"property" : "application_appTech",
 *		"filter": "Apple"
 *	}
 *	</pre>
 */
@CompileStatic
class ExtraFilterBuilder {

	String property
	String referenceProperty
	String filter
	/**
	 * Builder implementation for {@code ExtraFilter} instances.
	 * After configuring {@code ExtraFilter#property} and {@code ExtraFilter#filter},
	 * this Builder implementation creates a new instance of {@code ExtraFilter}
	 * based on the following rules:
	 *  1) A named filter:
	 *  <pre>
	 *{"property" : "_ufp", "filter": "true"}*  </pre>
	 *  A custom filter:
	 *  <pre>
	 *{"property" : "_event", "filter": "364"}*  </pre>
	 * 	Or a simple asset field filter:
	 *  <pre>
	 *{"property" : "assetName", "filter": "FOOBAR"}*{"property" : "common_assetName", "filter": "FOOBAR"}*{"property" : "appTech", "filter": "Apple"}*{"property" : "application_appTech", "filter": "Apple"}*  </pre>
	 *
	 * @param domains
	 * @param fieldSpecProject
	 * @return
	 */
	ExtraFilter build(List<String> domains, FieldSpecProject fieldSpecProject) {

		if (SpecialExtraFilterType.lookupByName(this.property)) {
			return new ExtraFilter(
				property: this.property,
				filter: this.filter
			)
		} else if (this.property.contains('_')) {
			return buildExtraFilterWithDomainDefinedInProperty(fieldSpecProject)
		} else {
			return buildExtraFilterSelectingDomainFromDataview(domains, fieldSpecProject)
		}
	}

	/**
	 * It builds an instance of {@code ExtraFilter} using {@code DataviewSpec} columns.
	 * It lookups an instance of {@code FieldSpec} combining columns defined in {@code DataviewSpec}.
	 *
	 * @param domains a list of asset entity domain.
	 * @param fieldSpecProject and instance of {@code FieldSpecProject}
	 * @return an instance of {@code ExtraFilter}
	 */
	private ExtraFilter buildExtraFilterSelectingDomainFromDataview(List<String> domains, FieldSpecProject fieldSpecProject) {
		FieldSpec fieldSpec = null
		String selectedDomain = domains.find { String domain ->
			fieldSpec = fieldSpecProject.getFieldSpec(domain, this.property)
			return fieldSpec != null
		}

		if (!fieldSpec) {
			throw new InvalidParamException("Field Spec '$property' not found")
		}
		return new ExtraFilter(
			property: this.property,
			referenceProperty: this.referenceProperty,
			filter: this.filter,
			fieldSpec: fieldSpec,
			domain: selectedDomain
		)
	}

	/**
	 * Builds an instance of {@code ExtraFilter} using {@code FieldSpecProject}
	 * to add {@FieldSpec} as a creation param
	 * @param fieldSpecProject an instance of {@code FieldSpecProject}
	 * @return an instance of {@code ExtraFilter}
	 */

	private ExtraFilter buildExtraFilterWithDomainDefinedInProperty(FieldSpecProject fieldSpecProject) {
		String[] parts = this.property.split(DataviewApiFilterParam.FIELD_NAME_SEPARATOR_CHARACTER)
		if (parts.size() != 2){
			throw new InvalidParamException("Unresolved filter property $property")
		}
		String domain = parts[0]
		String fieldName = parts[1]
		FieldSpec fieldSpec = fieldSpecProject.getFieldSpec(domain, fieldName)
		if (!fieldSpec) {
			throw new InvalidParamException("Unresolved domain $domain and field $fieldName")
		}
		return new ExtraFilter(
			property: fieldName,
			referenceProperty: this.referenceProperty,
			filter: this.filter,
			fieldSpec: fieldSpec,
			domain: domain
		)
	}

	/**
	 * Defines {@code ExtraFilterBuilder#property}
	 * and determines if this is a request for a reference property
	 * @param property
	 * @return an instance of {@code ExtraFilterBuilder}
	 */
	ExtraFilterBuilder withProperty(String property) {
		List<String> propertyParts = property.split('\\.') as List<String>
		if (propertyParts.size() == 1) {
			this.property = property
			this.referenceProperty = null
		} else if (propertyParts.size() == 2) {
			this.property = propertyParts[0]
			this.referenceProperty = propertyParts[1]
		} else {
			throw new InvalidParamException("Unresolved filter property $property")
		}

		return this
	}

	ExtraFilterBuilder withFilter(String filter) {
		this.filter = filter
		return this
	}
}