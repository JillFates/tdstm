package com.tdsops.etl

import com.tds.asset.AssetEntity


enum ETLDomain {

	Application(com.tds.asset.Application),
	Device(com.tds.asset.AssetEntity),
	Database(com.tds.asset.Database),
	Storage(com.tds.asset.Files),
	External(com.tds.asset.AssetEntity),
	Task(com.tds.asset.AssetComment),
	Person(net.transitionmanager.domain.Person),
	Comment(com.tds.asset.AssetComment),
	Asset(com.tds.asset.AssetEntity),
	Manufacturer(net.transitionmanager.domain.Manufacturer),
	Model(net.transitionmanager.domain.Model),
	Dependency(com.tds.asset.AssetDependency),
	Rack(net.transitionmanager.domain.Rack),
	Bundle(net.transitionmanager.domain.MoveBundle),
	Room(net.transitionmanager.domain.Room),
	Files(com.tds.asset.Files)

	private Class<?> clazz

	private ETLDomain(Class<?> clazz) {
		this.clazz = clazz
	}

	Class<?> getClazz() {
		return clazz
	}

	static final ASSET_CLASSNAMES = [ 'com.tds.asset.Application', 'com.tds.asset.AssetEntity', 'com.tds.asset.Database', 'com.tds.asset.Files']

	/**
	 * Check if a domain instance has a Class in the AssetEntity hierarchy
	 * @return true if clazz is assignable from AssetEntity class.
	 */
	boolean isAsset() {
		boolean isaAsset = false
		if (clazz != null) {
			String name = clazz.getName()
			isaAsset = name in ASSET_CLASSNAMES
		}

		return isaAsset
	}
	/**
	 * Check if a domain is in the {@code AssetEntity} hierarchy.
	 * @param domain
	 * @return true if domain belongs to {@code AssetEntity} hierarchy.
	 * 			otherwise returns false
	 */
	static boolean isDomainAsset(String domain){
		return lookup(domain) in [
			ETLDomain.Application,
			ETLDomain.Device,
			ETLDomain.Database,
			ETLDomain.Storage
		]
	}

	/**
	 * Lookup for ETLDomain from a String.
	 * <code>
	 *     assert ETLDomain.lookup('Application') ==  ETLDomain.Application
	 *     assert ETLDomain.lookup('APPLICATION') ==  ETLDomain.Application
	 *     assert ETLDomain.lookup('FOO') ==  null
	 *     assert ETLDomain.lookup(null) ==  null
	 * </code>
	 * @param code
	 * @return
	 */
	static ETLDomain lookup(String code) {
		return ETLDomain.enumConstantDirectory().get(code)
	}
}