package com.tdsops.etl

import com.tds.asset.AssetEntity
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import groovy.json.internal.LazyMap
import net.transitionmanager.domain.Project
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@TestFor(AssetEntity)
@TestMixin([DomainClassUnitTestMixin])
class DomainClassQueryHelperSpec extends Specification {

	@Shared
	LazyMap lazy = new LazyMap()

	@Shared
	Project project = new Project()

	@Unroll
	void 'test can get parameter for field for domain #domain and field #field'() {

		expect:
			DomainClassQueryHelper.getNamedParameterForField(domain, new FindCondition(field, value)) == result

		where:
			domain                | field            | value      || result
			ETLDomain.Device     | 'id'             | 123        || 'id'
			ETLDomain.Device     | 'id'             | 432165l    || 'id'
			ETLDomain.Device     | 'manufacturer'   | 'IBM'      || 'manufacturer_name'
			ETLDomain.Device     | 'manufacturer'   | 123        || 'manufacturer_id'
			ETLDomain.Device     | 'manufacturer'   | 432165l    || 'manufacturer_id'
			ETLDomain.Device     | 'model'          | 'PDV FF23' || 'model_modelName'
			ETLDomain.Device     | 'model'          | 122        || 'model_id'
			ETLDomain.Device     | 'model'          | 989898l    || 'model_id'
			ETLDomain.Device     | 'moveBundle'     | 'Foo'      || 'moveBundle_name'
			ETLDomain.Device     | 'rackSource'     | 'Tag Rack' || 'rackSource_tag'
			ETLDomain.Device     | 'locationSource' | 'Boston'   || 'roomSource_location'
			ETLDomain.Room       | 'roomName'       | 'Bar'      || 'roomName'
			ETLDomain.Rack       | 'room'           | 'Bar'      || 'room'
			ETLDomain.Dependency | 'asset'          | 'IBM'      || 'asset'
	}

	@Unroll
	void 'test can get property for field for domain #domain and field #field'() {
		expect:
			DomainClassQueryHelper.getPropertyForField(domain, new FindCondition(field, value)) == result

		where:
			domain               | field            | value     || result
			ETLDomain.Device     | 'id'             | 234234l   || 'D.id'
			ETLDomain.Device     | 'id'             | '234234'  || 'D.id'
			ETLDomain.Device     | 'manufacturer'   | 'Foo Bar' || 'D.manufacturer.name'
			ETLDomain.Model      | 'manufacturer'   | 1234      || 'D.manufacturer.id'
			ETLDomain.Model      | 'manufacturer'   | 1234l     || 'D.manufacturer.id'
			ETLDomain.Model      | 'manufacturer'   | 'Foo Bar' || 'D.manufacturer.name'
			ETLDomain.Device     | 'moveBundle'     | 'Foo Bar' || 'D.moveBundle.name'
			ETLDomain.Device     | 'rackSource'     | 'Foo Bar' || 'D.rackSource.tag'
			ETLDomain.Device     | 'locationSource' | 'Foo Bar' || 'D.roomSource.location'
			ETLDomain.Room       | 'roomName'       | 'Foo Bar' || 'D.roomName'
			ETLDomain.Rack       | 'room'           | 'Foo Bar' || 'D.room.roomName'
			ETLDomain.Rack       | 'room'           | 123       || 'D.room.id'
			ETLDomain.Rack       | 'room'           | 12342134l || 'D.room.id'
			ETLDomain.Dependency | 'asset'          | 'Foo Bar' || 'D.asset.assetName'
			ETLDomain.Dependency | 'asset'          | 123       || 'D.asset.id'
			ETLDomain.Dependency | 'asset'          | 12342134l || 'D.asset.id'
			ETLDomain.Dependency | 'dependent'      | 'Foo Bar' || 'D.dependent.assetName'
			ETLDomain.Dependency | 'dependent'      | 123       || 'D.dependent.id'
			ETLDomain.Dependency | 'dependent'      | 12342134l || 'D.dependent.id'
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

	@Unroll
	@ConfineMetaClassChanges([AssetEntity])
	void 'test where method returns nothing when value is not set'() {
		given: 'necessary objects are created'
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				[1, 2, 3]
			}

		expect:
			size == DomainClassQueryHelper.where(ETLDomain.Device, project, [new FindCondition('assetName', value, operator)], true).size()
		where:
			operator    | value | size
			'eq'        | null  | 0
			'eq'        | lazy  | 0
			'eq'        | 'xyz' | 3
			'isNotNull' | null  | 3
			'isNotNull' | lazy  | 3
			'isNotNull' | '123' | 3
			'isNull'    | null  | 3
			'isNull'    | lazy  | 3
			'isNull'    | '123' | 3

	}
}
