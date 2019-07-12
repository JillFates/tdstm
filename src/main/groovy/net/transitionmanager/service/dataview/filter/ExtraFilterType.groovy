package net.transitionmanager.service.dataview.filter

import groovy.transform.CompileStatic

/**
 * enum definition for Special extra filter keys.
 * <code>
 *     assert ExtraFilterType.lookupByName('_event') ==  ExtraFilterType.EVENT
 *     assert ExtraFilterType.lookupByName('_assetType') ==  ExtraFilterType.ASSET_TYPE
 *     assert ExtraFilterType.lookupByName('_planMethod') ==  ExtraFilterType.PLAN_METHOD
 * </code>
 */
@CompileStatic
enum ExtraFilterType {

	ASSET_TYPE('_assetType'),
	EVENT('_event'),
	PLAN_METHOD('_planMethod')

	final String name

	private ExtraFilterType(String name) {
		this.name = name
	}

	/**
	 * Map of {@code ExtraFilterType} to be used in
	 * {@code ExtraFilterType#lookupByName} method.
	 */
	private static final Map<String, ExtraFilterType> nameIndex = [:]

	static {
		for (ExtraFilterType specialExtraFilterType : ExtraFilterType.values()) {
			nameIndex.put(specialExtraFilterType.name, specialExtraFilterType);
		}
	}

	/**
	 * Lookup for ExtraFilterType instance from a String.
	 * <code>
	 *     assert ExtraFilterType.lookupByName('_assetType') ==  ExtraFilterType.ASSET_TYPE
	 *     assert ExtraFilterType.lookupByName('_event') ==  ExtraFilterType.EVENT
	 *     assert ExtraFilterType.lookupByName('moveBundle.id') ==  null
	 *     assert ExtraFilterType.lookupByName(null) ==  null
	 * </code>
	 * @param name
	 * @return and instance of {@code ExtraFilterType} or null if it does not exist.
	 */
	static ExtraFilterType lookupByName(String name) {
		return nameIndex.get(name)
	}
}
