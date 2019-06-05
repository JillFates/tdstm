package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic

/**
 * enum definition for Special extra filter keys.
 * <code>
 *     assert SpecialExtraFilterType.lookup('_filter') ==  SpecialExtraFilterType.FILTER
 * </code>
 */
@CompileStatic
enum SpecialExtraFilterType {

	FILTER('_filter'),
	EVENT('_event'),
	PLAN_METHOD('_planMethod')

	final String name

	private SpecialExtraFilterType(String name) {
		this.name = name
	}

	private static final Map<String, SpecialExtraFilterType> nameIndex = [:]

	static {
		for (SpecialExtraFilterType specialExtraFilterType : SpecialExtraFilterType.values()) {
			nameIndex.put(specialExtraFilterType.name, specialExtraFilterType);
		}
	}

	/**
	 * Lookup for ETLDomain from a String.
	 * <code>
	 *     assert SpecialExtraFilterType.lookupByName('_filter') ==  SpecialExtraFilterType.FILTER
	 *     assert SpecialExtraFilterType.lookupByName('_event') ==  SpecialExtraFilterType.EVENT
	 *     assert SpecialExtraFilterType.lookupByName('moveBundle.id') ==  null
	 *     assert SpecialExtraFilterType.lookupByName(null) ==  null
	 * </code>
	 * @param code
	 * @return
	 */
	static SpecialExtraFilterType lookupByName(String name) {
		return nameIndex.get(name)
	}

}
