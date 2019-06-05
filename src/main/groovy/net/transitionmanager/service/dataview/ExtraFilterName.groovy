package net.transitionmanager.service.dataview

import groovy.transform.CompileStatic

/**
 * enum definition for Special extra filter keys.
 * <code>
 *     assert ExtraFilterName.lookupByName('_filter') ==  ExtraFilterName.FILTER
 *     assert ExtraFilterName.lookupByName('_event') ==  ExtraFilterName.EVENT
 *     assert ExtraFilterName.lookupByName('_planMethod') ==  ExtraFilterName.PLAN_METHOD
 * </code>
 */
@CompileStatic
enum ExtraFilterName {

	FILTER('_filter'),
	EVENT('_event'),
	PLAN_METHOD('_planMethod')

	final String name

	private ExtraFilterName(String name) {
		this.name = name
	}

	/**
	 * Map of {@code ExtraFilterName} to be used in
	 * {@code ExtraFilterName#lookupByName} method.
	 */
	private static final Map<String, ExtraFilterName> nameIndex = [:]

	static {
		for (ExtraFilterName specialExtraFilterType : ExtraFilterName.values()) {
			nameIndex.put(specialExtraFilterType.name, specialExtraFilterType);
		}
	}

	/**
	 * Lookup for ETLDomain from a String.
	 * <code>
	 *     assert ExtraFilterName.lookupByName('_filter') ==  ExtraFilterName.FILTER
	 *     assert ExtraFilterName.lookupByName('_event') ==  ExtraFilterName.EVENT
	 *     assert ExtraFilterName.lookupByName('moveBundle.id') ==  null
	 *     assert ExtraFilterName.lookupByName(null) ==  null
	 * </code>
	 * @param code
	 * @return
	 */
	static ExtraFilterName lookupByName(String name) {
		return nameIndex.get(name)
	}

}
