package com.tdsops.etl

import spock.lang.Specification
import spock.lang.Unroll


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
			DomainClassQueryHelper.getPropertyForField(clazz, field, value) == result

		where:
			clazz                           | field            | value     || result
			ETLDomain.Device.getClazz()     | 'id'             | 234234l   || 'D.id'
			ETLDomain.Device.getClazz()     | 'id'             | '234234'  || 'D.id'
			ETLDomain.Device.getClazz()     | 'id'             | 'Foo Bar' || 'D.assetName'
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
}
