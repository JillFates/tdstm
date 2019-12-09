package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic
import net.transitionmanager.command.dataview.DataviewApiFilterParam
import net.transitionmanager.dataview.FieldSpec
import net.transitionmanager.dataview.FieldSpecProject
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.service.dataview.filter.ExtraFilterType
import net.transitionmanager.service.dataview.filter.FieldNameExtraFilter
import net.transitionmanager.service.dataview.filter.special.AssetTypeExtraFilter
import net.transitionmanager.service.dataview.filter.special.EventExtraFilter

import net.transitionmanager.service.dataview.filter.special.PlanMethodExtraFilter
import net.transitionmanager.service.dataview.filter.special.SpecialExtraFilter

/**
 * Builder class logic for building an instance of Extra Filters.
 * It can resolve if extra filter is:
 * 1) A special extra filter:
 * <pre>
 * 	{ "property" : "moveBundle.id", "filter": "3239"}
 * 	{ "property" : "_event.id", "filter": "364"}
 * </pre>
 * 2) Or a simple domain field name filter:
 * <pre>
 *	{"property" : "assetName", "filter": "FOOBAR" }
 *	{"property" : "common_assetName", "filter": "FOOBAR"}
 *	{"property" : "appTech", "filter": "Apple"}
 *	{"property" : "application_appTech", "filter": "Apple"}
 * </pre>
 */
@CompileStatic
class ExtraFilterBuilder {

	String property
	String referenceProperty
	String filter

	/**
	 * Builder implementation for {@code ExtraFilter} instances.
	 * After configuring {@code ExtraFilterBuilder#property} and {@code ExtraFilterBuilder#filter},
	 * this Builder implementation creates new instances of {@code SpecialExtraFilter}
	 * and {@code FieldNameExtraFilter}
	 *  1) Special filters:
	 *  <pre>
	 *		{"property" : "_event", "filter": "364"}					// EventExtraFilter extends SpecialExtraFilter
	 *		{"property" : "_assetType", "filter": "Server"}				// AssetTypeExtraFilter extends SpecialExtraFilter
	 * 		{"property" : "_planMethod", "filter": "Unknown"}			// PlanMethodologyExtraFilter extends SpecialExtraFilter
	 *	</pre>
	 * 	Or a simple domain field filter:
	 *  <pre>
	 *		{"property" : "assetName", "filter": "FOOBAR"}  			// FieldNameExtraFilter
	 *		{"property" : "common_assetName", "filter": "FOOBAR"} 		// FieldNameExtraFilter
	 *		{"property" : "appTech", "filter": "Apple"} 				// FieldNameExtraFilter
	 *		{"property" : "application_appTech", "filter": "Apple"}		// FieldNameExtraFilter
	 *		{"property" : "common_moveBundle.id", "filter": "3223"}		// FieldNameExtraFilter
	 *		{"property" : "moveBundle.id", "filter": "3223"}			// FieldNameExtraFilter
	 *	</pre>
	 *
	 * @param dataviewSpec an instance of {@code DataviewSpec} used to collect built resuls.
	 * @param fieldSpecProject an instance of {@code FieldSpecProject}
	 * 			used to lookup {@code FieldSpec}
	 */
	void build(DataviewSpec dataviewSpec, FieldSpecProject fieldSpecProject) {

		ExtraFilterType extraFilterType = ExtraFilterType.lookupByName(this.property)
		if (extraFilterType) {
			// build Special Filter and add it to dataviewSpec.specialExtraFilters List
			dataviewSpec.specialExtraFilters.add(buildExtraNamedFilter(extraFilterType))

		} else {
			DomainFieldNameResult domainFieldNameResult = resultsDomainAndFieldName(dataviewSpec.domains, fieldSpecProject)
			FieldSpec fieldSpec = domainFieldNameResult.fieldSpec
			if (fieldSpec) {
				// build FieldName Filter and add it to dataviewSpec.fieldNameExtraFilters List
				dataviewSpec.fieldNameExtraFilters.add(new FieldNameExtraFilter(
					domain: domainFieldNameResult.domain,
					property: domainFieldNameResult.fieldName,
					fieldSpec: fieldSpec,
					filter: this.filter,
					referenceProperty: this.referenceProperty)
				)
			}
		}
	}

	/**
	 * <p>Determines if {@code ExtraFilterBuilder#property} contains a domain definition
	 * or if it need to be resolved using {@code DataviewSpec#domains}</p>
	 * @param domains List of String values with domain names. e.g. ['common', 'application', 'device']
	 * @param fieldSpecProject an instance of {@code FieldSpecProject}
	 * 	  			used to lookup {@code FieldSpec}
	 * @return an instance of {@code DomainFieldNameResult}
	 */
	DomainFieldNameResult resultsDomainAndFieldName(List<String> domains, FieldSpecProject fieldSpecProject) {
		if (this.property.contains('_')) {
			return resultsDomainAndFieldNameUsingDefinedDomain(fieldSpecProject)
		} else {
			return resultsDomainAndFieldNameUsingDataviewDomains(domains, fieldSpecProject)
		}
	}

	/**
	 * <pre>
	 *	this.property == 'common_assetName'
	 *  return [
	 *  	domain: 'common',
	 *  	fieldName: 'assetName',
	 *  	fieldSpec: fieldSpecProject.getFieldSpec('common', 'assetName')
	 *  ]
	 * </pre>
	 * @param fieldSpecProject
	 * @return an instance of {@code DomainFieldNameResult}
	 */
	DomainFieldNameResult resultsDomainAndFieldNameUsingDefinedDomain(FieldSpecProject fieldSpecProject) {

		String[] parts = this.property.split(DataviewApiFilterParam.FIELD_NAME_SEPARATOR_CHARACTER)
		if (parts.size() != 2) {
			throw new InvalidParamException("Unresolved filter property $property")
		}
		String domain = parts[0]
		String fieldName = parts[1]
		FieldSpec fieldSpec = fieldSpecProject.getFieldSpec(domain, fieldName)

		return new DomainFieldNameResult(
			domain,
			fieldName,
			fieldSpec
		)
	}
	/**
	 * <pre>
	 *  domains == ['common', 'application']
	 *	this.property == 'assetName'
	 *  return [
	 *  	domain: 'common',
	 *  	fieldName: 'assetName',
	 *  	fieldSpec: fieldSpecProject.getFieldSpec('common', 'assetName')
	 *  ]
	 * </pre>
	 * @param fieldSpecProject
	 * @return an instance of {@code DomainFieldNameResult}
	 */
	DomainFieldNameResult resultsDomainAndFieldNameUsingDataviewDomains(List<String> domains, FieldSpecProject fieldSpecProject) {

		FieldSpec fieldSpec = null
		String selectedDomain = domains.find { String domain ->
			fieldSpec = fieldSpecProject.getFieldSpec(domain, this.property)
			return fieldSpec != null
		}

		return new DomainFieldNameResult(
			selectedDomain,
			this.property,
			fieldSpec
		)
	}

	/**
	 * <p>Creates an instance of {@code SpecialExtraFilter} based on
	 * {@code ExtraFilterType#name}</p>
	 *
	 * @param extraFilterName an instance of {@code ExtraFilterType}
	 * @return and instance of {@code SpecialExtraFilter}
	 */
	SpecialExtraFilter buildExtraNamedFilter(ExtraFilterType extraFilterName) {

		SpecialExtraFilter specialExtraFilter
		switch (extraFilterName) {
			case ExtraFilterType.EVENT:
				specialExtraFilter = new EventExtraFilter(property: this.property, filter: this.filter)
				break
			case ExtraFilterType.PLAN_METHOD:
				specialExtraFilter = new PlanMethodExtraFilter(property: this.property, filter: this.filter)
				break
			case ExtraFilterType.ASSET_TYPE:
				specialExtraFilter = new AssetTypeExtraFilter(property: this.property, filter: this.filter)
				break

			default:
				throw new RuntimeException('Invalid filter definition:' + this.property)
		}

		return specialExtraFilter
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
			// dcorrea: Add Validation for TM-14768
			// It's only available for '.id' references.
			if (this.referenceProperty != 'id'){
				throw new InvalidParamException("Unsupported filter property $property")
			}
		} else {
			throw new InvalidParamException("Unresolved filter property $property")
		}

		return this
	}

	/**
	 * Set the filter value for builder and returns same instance of {@code ExtraFilterBuilder}
	 * @param filter a String value used to filter Dataview results.
	 * @return an instance of {@code ExtraFilterBuilder}
	 * @see net.transitionmanager.imports.DataviewService#previewQuery(net.transitionmanager.project.Project, net.transitionmanager.service.dataview.DataviewSpec)
	 */
	ExtraFilterBuilder withFilter(String filter) {
		this.filter = filter
		return this
	}
}

/**
 * Class defined to retrieve a combination of domain and field name
 * with the fieldSpec instance related.
 * <pre>
 * 	'common_assetName' -> new DomainFieldNameResult('common', 'assetName', ...)
 * </pre>
 */
@CompileStatic
class DomainFieldNameResult {
	String domain
	String fieldName
	FieldSpec fieldSpec

	DomainFieldNameResult(String domain, String fieldName, FieldSpec fieldSpec) {
		this.domain = domain
		this.fieldName = fieldName
		this.fieldSpec = fieldSpec
	}
}