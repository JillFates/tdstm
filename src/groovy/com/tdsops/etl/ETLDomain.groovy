package com.tdsops.etl

enum ETLDomain {

	Application, Device, Database, Storage, External, Task, Person, Comment, Asset, Manufacturer, Model, Dependency, Rack, Bundle, Room

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

	/**
	 * Lookup the correct asset domain class base on an instance of ETLDomain
	 * @param domain an instance of ETLDomain
	 * @return The correct class implementation of Asset domain class
	 * 			based on an instance of ETLDomain
	 */
	static Class<?> lookupDomainClass(ETLDomain domain) {

		Class<?> domainClass
		switch (domain) {
			case Application:
				domainClass = com.tds.asset.Application
				break
			case Database:
				domainClass = com.tds.asset.Database
				break
			case Device:
				domainClass = com.tds.asset.AssetEntity
				break
			case Storage:
				domainClass = com.tds.asset.AssetEntity
				break
			case External:
				domainClass = com.tds.asset.AssetEntity
				break
			case Task:
				domainClass = com.tds.asset.AssetComment
				break
			case Person:
				domainClass = net.transitionmanager.domain.Person
				break
			case Comment:
				domainClass = com.tds.asset.AssetComment
				break
			case Asset:
				domainClass = com.tds.asset.AssetEntity
				break
			case Dependency:
				domainClass = com.tds.asset.AssetDependency
				break
			case Rack:
				domainClass = net.transitionmanager.domain.Rack
				break
			case Room:
				domainClass = net.transitionmanager.domain.Room
				break
			case Manufacturer:
				domainClass = net.transitionmanager.domain.Manufacturer
				break
			case Bundle:
				domainClass = net.transitionmanager.domain.MoveBundle
				break
			default:
				domainClass = com.tds.asset.AssetEntity
				break
		}
		return domainClass
	}
}