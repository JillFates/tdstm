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
class GormUtilUnitSpec extends Specification {

	void '1. Test isDomainProperty'() {
		when:
			AssetEntity asset = new AssetEntity()
		then:
			GormUtil.isDomainProperty(asset, 'assetName')
			GormUtil.isDomainProperty(asset, 'id')
			!GormUtil.isDomainProperty(asset, 'bogusPropertyName')
			GormUtil.isDomainProperty(AssetEntity, 'assetName')
			!GormUtil.isDomainProperty(AssetEntity, 'bogusPropertyName')

		when: 'called with a non-domain class'
			GormUtil.isDomainProperty(Specification, 'NotADomainClass')
		then: 'an exception is thrown'
			thrown RuntimeException
	}

	void '2. Test getDomainPropertyType'() {
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

	void '3. Test getDomainPropertyType for a Class.'() {
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
	void '4. test if #clazz is a DomainClass'() {
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
	void '5. test if #instance instance is a DomainClass'() {
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
	void '6. test if #propertyName is a valid property for a DomainClass'() {
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
	void '7. test if #propertyName is a valid property for #clazz DomainClass'() {
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

	void '8. test can throw an exception if it ask for a property in a not DomainClass'() {

		given:
			Class clazz = ApiActionResponse.class

		when: 'trying to evaluate a property for a non-domain Class'
			GormUtil.isDomainProperty(clazz, 'assetType')
		then: 'an exception should be thrown'
			thrown RuntimeException

		when: 'trying to evaluate a property for non-domain class instance'
			GormUtil.isDomainProperty(new ApiActionResponse(), 'assetType')
		then: 'an exception should be thrown'
			thrown RuntimeException

	}

	@Unroll
	void '9. test can return a GrailsDomainClassProperty for #propertyName and #clazz DomainClass'() {
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
	void '10. test if #propertyName is a property for an instance of a DomainClass and is an identifier'() {
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
	void '11. test if #propertyName is a property for a DomainClass is an identifier'() {
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
	void '12. test if #propertyName is a property for a and instance of a DomainClass is a reference'() {
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
	void '13. test if #propertyName is a property for a #clazz DomainClass is a reference'() {
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

	void '14. test the getDomainClass'() {
		when: 'getDomainClass is called for a domain class'
			def dc = GormUtil.getDomainClass(com.tds.asset.AssetEntity)
		then: 'a DefaultGrailsDomainClass should be returned'
			'org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass' == dc.getClass().getName()
		and: 'the name should be AssetEntity'
			'AssetEntity' == dc.name

		when: 'getDomainClass is called for a non-domain class'
			GormUtil.getDomainClass(spock.lang.Specification)
		then: 'an exception should occur'
			RuntimeException e = thrown RuntimeException
			e.message == 'Identity property not found, but required in domain class [spock.lang.Specification]'

		when: 'getDomainClass is called with a null value'
			GormUtil.getDomainClass(null)
		then: 'an exception should occur'
			e = thrown RuntimeException
			e.message == 'getDomainClass() called with null class argument'


	}

	void '15. Test the domainShortName method'() {
		expect: 'shortname of net.transitionmanager.domain.Person should be Person'
			'Person' == GormUtil.domainShortName(net.transitionmanager.domain.Person)
	}
}
