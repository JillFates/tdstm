package com.tdssrc.grails

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tdsops.etl.ETLProcessor
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.domain.Person
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
class GormUtilUnitSpec extends Specification {

	protected void setup() {
	}

	protected void cleanup() {
	}

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

		when: 'It tries to evaluate a property for an instance'
			GormUtil.isDomainProperty(new ApiActionResponse(), 'assetType')

		then: 'An exception is thrown'
			e = thrown GrailsDomainException
			e.message == 'Identity property not found, but required in domain class [net.transitionmanager.integration.ApiActionResponse]'

	}

	@Unroll
	void 'test can return a GrailsDomainClassProperty for #propertyName and #clazz DomainClass'() {
		expect:
			GrailsDomainClassProperty grailsDomainClassProperty = GormUtil.getGrailsDomainClassProperty(clazz, propertyName)
			grailsDomainClassProperty.name == name

		where:
			clazz       | propertyName || name
			AssetEntity | 'id'         || 'id'
			Database    | 'assetType'  || 'assetType'
			Application | 'assetName'  || 'assetName'
			Room        | 'roomName'   || 'roomName'
			Person      | 'firstName'  || 'firstName'
	}

}
