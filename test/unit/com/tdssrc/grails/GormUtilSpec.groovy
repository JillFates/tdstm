package com.tdssrc.grails

import com.tds.asset.Application
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tdsops.etl.ETLProcessor
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.service.DataviewService
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.exceptions.GrailsDomainException
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test cases for the GormUtil class
 */
class GormUtilSpec extends Specification {

	void 'Test isDomainProperty'() {
		when:
			AssetEntity asset = new AssetEntity()
		then:
			GormUtil.isDomainProperty(asset, 'assetName')
			GormUtil.isDomainProperty(asset, 'id')
			!GormUtil.isDomainProperty(asset, 'bogusPropertyName')
	}

	void 'Test getDomainPropertyType'() {
		when:
			AssetEntity asset = new AssetEntity()
		then:
			(GormUtil.getDomainPropertyType(asset, 'assetName') == java.lang.String)
		and:
			(GormUtil.getDomainPropertyType(asset, 'custom1') == java.lang.String)
		and:
			(GormUtil.getDomainPropertyType(asset, 'priority') == java.lang.Integer)
		and:
			!GormUtil.getDomainPropertyType(asset, 'bogusPropertyName')
	}

	void 'Test getDomainPropertyType for a Class.'() {
		expect:
			GormUtil.getDomainPropertyType(clazz, property) == type
		where:
			clazz       | property            | type
			AssetEntity | "assetName"         | java.lang.String
			AssetEntity | "custom1"           | java.lang.String
			AssetEntity | "priority"          | java.lang.Integer
			AssetEntity | "bogusPropertyName" | null
	}

	@Unroll
	void 'test if #clazz is a DomainClass'() {
		expect:
			GormUtil.isDomainClass(clazz) == isDomainClass

		where:
			clazz           || isDomainClass
			AssetEntity     || true
			Application     || true
			Room            || true
			Person          || true
			Long            || false
			ETLProcessor    || false
			DataviewService || false
			String          || false
	}

	@Unroll
	void 'test if #instance instance is a DomainClass'() {
		expect:
			GormUtil.isDomainClass(instance) == isDomainClass

		where:
			instance                               || isDomainClass
			new AssetEntity()                      || true
			new Application()                      || true
			new Room()                             || true
			new Person()                           || true
			123l                                   || false
			new RuntimeException('A simple Error') || false
			new DataviewUserParamsCommand()        || false
			'Hello World'                          || false
	}

	@Unroll
	void 'test if #propertyName is a valid property for a DomainClass'() {
		expect:
			GormUtil.isDomainProperty(instance, propertyName) == isDomainClass

		where:
			instance          | propertyName || isDomainClass
			new AssetEntity() | 'id'         || true
			new AssetEntity() | 'assetType'  || true
			new Application() | 'assetName'  || true
			new Room()        | 'roomName'   || true
			new Person()      | 'firstName'  || true
	}

	@Unroll
	void 'test if #propertyName is a valid property for #clazz DomainClass'() {
		expect:
			GormUtil.isDomainProperty(clazz, propertyName) == isDomainClass

		where:
			clazz       | propertyName || isDomainClass
			AssetEntity | 'id'         || true
			Database    | 'assetType'  || true
			Application | 'assetName'  || true
			Room        | 'roomName'   || true
			Person      | 'firstName'  || true
	}

	void 'test can throw an exception if it ask for a property in a not DomainClass'() {

		given:
			Class clazz = ApiActionResponse.class

		when: 'It tries to evaluate a property for a clazz'
			GormUtil.isDomainProperty(clazz, 'assetType')

		then: 'An exception is thrown'
			GrailsDomainException e = thrown GrailsDomainException
			e.message == 'Identity property not found, but required in domain class [net.transitionmanager.integration.ApiActionResponse]'

		when: 'It tries to evaluate a property for an clazz'
			GormUtil.isDomainProperty(new ApiActionResponse(), 'assetType')

		then: 'An exception is thrown'
			e = thrown GrailsDomainException
			e.message == 'Identity property not found, but required in domain class [net.transitionmanager.integration.ApiActionResponse]'

	}

	@Unroll
	void 'test can return a GrailsDomainClassProperty for #propertyName and #clazz DomainClass'() {
		expect:
			GrailsDomainClassProperty grailsDomainClassProperty = GormUtil.getDomainProperty(clazz, propertyName)
			grailsDomainClassProperty.name == name
			grailsDomainClassProperty.type == type

		where:
			clazz           | propertyName || name        | type
			AssetEntity     | 'id'         || 'id'        | Long
			AssetDependency | 'id'         || 'id'        | Long
			Database        | 'assetType'  || 'assetType' | String
			Application     | 'sme'        || 'sme'       | Person
			Room            | 'roomName'   || 'roomName'  | String
			Person          | 'firstName'  || 'firstName' | String
	}

	@Unroll
	void 'test if #propertyName is a property for a and instance of a DomainClass is an identifier'() {
		expect:
			GormUtil.isDomainIdentifier(instance, propertyName) == isDomainIdentifier

		where:
			instance          | propertyName || isDomainIdentifier
			new AssetEntity() | 'id'         || true
			new AssetEntity() | 'assetType'  || false
			new Application() | 'id'         || true
			new Room()        | 'roomName'   || false
			new Person()      | 'id'         || true
	}

	@Unroll
	void 'test if #propertyName is a property for a DomainClass is an identifier'() {
		expect:
			GormUtil.isDomainIdentifier(clazz, propertyName) == isDomainIdentifier

		where:
			clazz       | propertyName || isDomainIdentifier
			AssetEntity | 'id'         || true
			AssetEntity | 'assetType'  || false
			Application | 'id'         || true
			Room        | 'roomName'   || false
			Person      | 'id'         || true
	}

	@Unroll
	void 'test if #propertyName is a property for a and instance of a DomainClass is a reference'() {
		expect:
			GormUtil.isReferenceProperty(instance, propertyName) == isReferenceProperty

		where:
			instance          | propertyName || isReferenceProperty
			new AssetEntity() | 'id'         || false
			new AssetEntity() | 'moveBundle' || true
			new Application() | 'sme'        || true
			new Room()        | 'roomName'   || false
			new Person()      | 'id'         || false
	}

	@Unroll
	void 'test if #propertyName is a property for a #clazz DomainClass is a reference'() {
		expect:
			GormUtil.isReferenceProperty(clazz, propertyName) == isReferenceProperty

		where:
			clazz       | propertyName || isReferenceProperty
			AssetEntity | 'id'         || false
			AssetEntity | 'moveBundle' || true
			Rack        | 'room'       || true
			Application | 'id'         || false
			Room        | 'roomName'   || false
			Person      | 'id'         || false
	}
}
