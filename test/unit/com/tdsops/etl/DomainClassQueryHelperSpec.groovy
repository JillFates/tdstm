package com.tdsops.etl

import net.transitionmanager.domain.Project
import com.tds.asset.AssetEntity

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin

import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges


@TestFor(AssetEntity)
@TestMixin([DomainClassUnitTestMixin])
class DomainClassQueryHelperSpec extends Specification {

	@Unroll
	void 'test can get parameter for field for clazz #clazz and field #field'() {

		expect:
			DomainClassQueryHelper.getNamedParameterForField(clazz, field) == result

		where:
			clazz                           | field            || result
			ETLDomain.Device.getClazz()     | 'id'             || 'id'
			ETLDomain.Device.getClazz()     | 'manufacturer'   || 'manufacturer_name'
			ETLDomain.Device.getClazz()     | 'moveBundle'     || 'moveBundle_name'
			ETLDomain.Device.getClazz()     | 'rackSource'     || 'rackSource_tag'
			ETLDomain.Device.getClazz()     | 'locationSource' || 'roomSource_location'
			ETLDomain.Room.getClazz()       | 'roomName'       || 'roomName'
			ETLDomain.Rack.getClazz()       | 'room'           || 'room'
			ETLDomain.Dependency.getClazz() | 'asset'          || 'asset'
	}

	@Unroll
	void 'test can get property for field for clazz #clazz and field #field'() {
		expect:
			DomainClassQueryHelper.getPropertyForField(clazz, field) == result

		where:
			clazz                           | field            | value     || result
			ETLDomain.Device.getClazz()     | 'id'             | 234234l   || 'D.id'
			ETLDomain.Device.getClazz()     | 'id'             | '234234'  || 'D.id'
			ETLDomain.Device.getClazz()     | 'manufacturer'   | 'Foo Bar' || 'D.manufacturer.name'
			ETLDomain.Device.getClazz()     | 'moveBundle'     | 'Foo Bar' || 'D.moveBundle.name'
			ETLDomain.Device.getClazz()     | 'rackSource'     | 'Foo Bar' || 'D.rackSource.tag'
			ETLDomain.Device.getClazz()     | 'locationSource' | 'Foo Bar' || 'D.roomSource.location'
			ETLDomain.Room.getClazz()       | 'roomName'       | 'Foo Bar' || 'D.roomName'
			ETLDomain.Rack.getClazz()       | 'room'           | 'Foo Bar' || 'D.room'
			ETLDomain.Dependency.getClazz() | 'asset'          | 'Foo Bar' || 'D.asset'
	}

	@Unroll
	void 'test can get join for field for clazz #clazz and field #field'() {

		expect:
			DomainClassQueryHelper.getJoinForField(clazz, field) == result

		where:
			clazz                           | field            || result
			ETLDomain.Device.getClazz()     | 'id'             || ''
			ETLDomain.Device.getClazz()     | 'manufacturer'   || 'left outer join D.manufacturer'
			ETLDomain.Device.getClazz()     | 'moveBundle'     || 'left outer join D.moveBundle'
			ETLDomain.Device.getClazz()     | 'rackSource'     || 'left outer join D.rackSource'
			ETLDomain.Device.getClazz()     | 'locationSource' || 'left outer join D.roomSource'
			ETLDomain.Room.getClazz()       | 'roomName'       || ''
			ETLDomain.Rack.getClazz()       | 'room'           || 'left outer join D.room'
			ETLDomain.Dependency.getClazz() | 'asset'          || 'left outer join D.asset'
	}

	@ConfineMetaClassChanges([AssetEntity])
	void 'test where method returns nothing when value is not set'() {
		given: 'necessary objects are created'
			Project project = new Project()
			FindCondition conditions = new FindCondition('assetName', null, 'eq')
		and: 'AssetEntity has been mocked to return a list'
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[1,2,3]
			}

		when: 'calling where with a null value'
			List results = DomainClassQueryHelper.where(ETLDomain.Device, project, [conditions], true)
		then: 'nothing should be returned and no errors'
			0 == results.size()

		when: 'the value is a LazyMap'
			conditions = new FindCondition('assetName', new groovy.json.internal.LazyMap(), 'eq')
		then: 'calling where should return nothing and no errors should occur'
			0 == DomainClassQueryHelper.where(ETLDomain.Device, project, [conditions], true).size()

		when: 'the value is a String'
			conditions = new FindCondition('assetName', 'find this sucker', 'eq')
		then: 'calling where should return a list of objects'
			3 == DomainClassQueryHelper.where(ETLDomain.Device, project, [conditions], true).size()
	}
}
