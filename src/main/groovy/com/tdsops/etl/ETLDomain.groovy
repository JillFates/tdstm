package com.tdsops.etl

import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.task.AssetComment
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.MoveEvent

enum ETLDomain {

	Application(net.transitionmanager.asset.Application),
	Device(AssetEntity),
	Database(net.transitionmanager.asset.Database),
	Storage(net.transitionmanager.asset.Files),
	External(AssetEntity),
	Task(AssetComment),
	Person(net.transitionmanager.person.Person),
	Comment(AssetComment),
	Asset(AssetEntity),
	Manufacturer(net.transitionmanager.manufacturer.Manufacturer),
	Model(net.transitionmanager.model.Model),
	Dependency(AssetDependency),
	Rack(net.transitionmanager.asset.Rack),
	Bundle(MoveBundle),
	Room(net.transitionmanager.asset.Room),
	Files(net.transitionmanager.asset.Files),
	Event(MoveEvent)

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
		return lookup(domain).clazz in AssetEntity
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
