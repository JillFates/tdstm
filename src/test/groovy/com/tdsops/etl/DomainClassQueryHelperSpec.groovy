package com.tdsops.etl

import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import grails.testing.gorm.DomainUnitTest
import groovy.json.internal.LazyMap
import net.transitionmanager.project.Project
import spock.lang.See
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

class DomainClassQueryHelperSpec extends Specification implements DomainUnitTest<AssetEntity> {

	@Shared
	LazyMap lazy = new LazyMap()

	@Shared
	Project project = new Project()

	void setupSpec(){
		mockDomain AssetDependency
	}

	@Unroll
	void 'test can get parameter for field for domain #domain and field #field'() {

		expect:
			DomainClassQueryHelper.getNamedParameterForField(domain, new FindCondition(field, value)) == result

		where:
			domain               | field            | value      || result
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

	@See('TM-13978')
	@Unroll
	@ConfineMetaClassChanges([AssetEntity])
	void 'test can convert Device #propertyName field correctly with value #findConditionValue'() {

		given:
			mockDomain(AssetEntity)
			AssetEntity.metaClass.static.executeQuery = { String query, Map namedParams, Map metaParams ->
				assert namedParams[propertyName] == hqlNamedParam
				return []
			}

		expect:
			DomainClassQueryHelper.where(
				ETLDomain.Device,
				project,
				[
					new FindCondition(propertyName, findConditionValue)
				],
				true
			).size() == 0

		where:
			propertyName    | findConditionValue || hqlNamedParam
			'description'   | 22                 || '22'
			'description'   | 22f                || '22.0'
			'description'   | 22d                || '22.0'
			'description'   | 22l                || '22'
			'description'   | '22'               || '22'
			'id'            | 12                 || 12l
			'id'            | 12f                || 12l
			'id'            | 12d                || 12l
			'id'            | 12l                || 12l
			'id'            | '12'               || 12l
			'purchasePrice' | 22                 || 22d
			'purchasePrice' | 22f                || 22d
			'purchasePrice' | 22d                || 22d
			'purchasePrice' | 22l                || 22d
			'purchasePrice' | '22'               || 22d
	}
}
