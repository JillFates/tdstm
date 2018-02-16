package com.tdsops.etl


enum ETLDomain {

	Application(com.tds.asset.Application),
	Device(com.tds.asset.AssetEntity),
	Database(com.tds.asset.Database),
	Storage(com.tds.asset.AssetEntity),
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