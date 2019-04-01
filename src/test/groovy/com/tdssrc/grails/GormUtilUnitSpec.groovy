package com.tdssrc.grails

import net.transitionmanager.asset.Application
import net.transitionmanager.asset.AssetDependency
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Database
import com.tdsops.etl.ETLProcessor
import grails.testing.gorm.DataTest
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import groovy.transform.Memoized
import net.transitionmanager.task.AssetComment
import net.transitionmanager.asset.Files
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyRelationship
import net.transitionmanager.person.Person
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.integration.ApiActionResponse
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.service.DataviewService
import org.grails.datastore.mapping.model.PersistentProperty
import spock.lang.See
import spock.lang.Specification
import spock.lang.Unroll
/**
 * Unit test cases for the GormUtil class
 */
class GormUtilUnitSpec extends Specification implements DataTest{


	void setupSpec() {
		mockDomains AssetEntity, AssetDependency, Database, Application, Manufacturer, Model, PartyRelationship, Person, Rack, Room
	}

	void '1 Test isDomainProperty'() {
		when:
			AssetEntity asset = new AssetEntity()
		then:
			GormUtil.isDomainProperty(asset, 'assetName')
			GormUtil.isDomainProperty(asset, 'id')
			!GormUtil.isDomainProperty(asset, 'bogusPropertyName')
			GormUtil.isDomainProperty(AssetEntity, 'assetName')
			!GormUtil.isDomainProperty(AssetEntity, 'bogusPropertyName')
			GormUtil.isDomainProperty(AssetDependency, 'id')
			GormUtil.isDomainProperty(AssetDependency, 'asset')

		when: 'called with a non-domain class'
			boolean isDomain = GormUtil.isDomainProperty(Specification, 'NotADomainClass')
		then: 'The check returns false'
			!isDomain
	}

	void '2 Test getDomainPropertyType'() {
		when:
			AssetEntity asset = new AssetEntity()
		then:
			(GormUtil.getDomainPropertyType(asset, 'assetName') == java.lang.String)
		and:
			(GormUtil.getDomainPropertyType(asset, 'custom1') == java.lang.String)
		and:
			(GormUtil.getDomainPropertyType(asset, 'priority') == java.lang.Integer)
		and:
			(GormUtil.getDomainPropertyType(asset, 'owner') == PartyGroup)
		and:
			!GormUtil.getDomainPropertyType(asset, 'bogusPropertyName')
	}

	void '3 Test getDomainPropertyType for a Class.'() {
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
	void '4 test if #clazz is a DomainClass'() {
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
	void '5 test if #instance instance is a DomainClass'() {
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
	void '6 test if #propertyName is a valid property for a DomainClass'() {
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
	void '7 test if #propertyName is a valid property for #clazz DomainClass'() {
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

	void '8 test can throw an exception if it ask for a property in a not DomainClass'() {

		given:
			Class clazz = ApiActionResponse.class

		when: 'trying to evaluate a property for a non-domain Class'
			boolean isProperty = GormUtil.isDomainProperty(clazz, 'assetType')
		then: 'false is returned.'
			!isProperty

		when: 'trying to evaluate a property for non-domain class instance'
			isProperty = GormUtil.isDomainProperty(new ApiActionResponse(), 'assetType')
		then: 'false is returned.'
			!isProperty

	}

	@Unroll
	void '9 test can return a PersistentProperty for #propertyName and #clazz DomainClass'() {
		expect:
			PersistentProperty grailsDomainClassProperty = GormUtil.getDomainProperty(clazz, propertyName)
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
	void '10 test if #propertyName is a property for an instance of a DomainClass and is an identifier'() {
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
	void '11 test if #propertyName is a property for a DomainClass is an identifier'() {
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
	void '12 test if #propertyName is a property for a and instance of a DomainClass is a reference'() {
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
	void '13 test if #propertyName is a property for a #clazz DomainClass is a reference'() {
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

	void '14 test the getDomainClass'() {
		when: 'getDomainClass is called for a domain class'
			def dc = GormUtil.getDomainClass(AssetEntity)
		then: 'a DefaultGrailsDomainClass should be returned'
			'org.grails.datastore.mapping.keyvalue.mapping.config.KeyValuePersistentEntity' == dc.getClass().getName()
		and: 'the name should be AssetEntity'
			'com.tds.asset.AssetEntity' == dc.name

		when: 'getDomainClass is called for a non-domain class'
			GormUtil.getDomainClass(spock.lang.Specification)
		then: 'an exception should occur'
			RuntimeException e = thrown RuntimeException
			e.message == 'Invalid domain name (spock.lang.Specification) specified for getDomainClass()'

		when: 'getDomainClass is called with a null value'
			GormUtil.getDomainClass(null)
		then: 'an exception should occur'
			e = thrown RuntimeException
			e.message == 'getDomainClass() called with null class argument'


	}

	void '15 Test the domainShortName method'() {
		expect: 'shortname of net.transitionmanager.domain.Person should be Person'
			'Person' == GormUtil.domainShortName(Person)
	}

	void '16 take getAlternateKeyPropertyName method for a spin'() {
		expect:
			expectedName == GormUtil.getAlternateKeyPropertyName(clazz)
		where:
			clazz				| expectedName
			Application			| 'assetName'
			Manufacturer		| 'name'
			Model				| 'modelName'
			PartyRelationship	| null
	}

	void '17 Test getDomainClassOfProperty method'() {
		expect:
			expectedClazz == GormUtil.getDomainClassOfProperty(clazz, propertyName)
		where:
			clazz				| propertyName	| expectedClazz
			Application			| 'id'			| Application
			Application			| 'sme'			| Person
			Application			| 'url'			| Application
	}

	void '18 test getDomainPropertyNames'() {
		when: 'Calling getDomainPropertyNames for Application domain'
			List props = GormUtil.getDomainPropertyNames(Application)
		then: 'various properties should be there as expected'
			'sme' in props
			'businessUnit' in props
		and: 'properties from inherited class AssetEntity should be there too'
			'assetClass' in props
			'assetName' in props
	}

	@See('TM-11461')
	void 'test can memoized is a DomainClass method'() {
		given:
			Closure closure = Mock(Closure)

			List<Class<?>> classes = [
				Application,
				AssetEntity,
				Database,
				Files,
				AssetEntity,
				AssetComment,
				Person,
				AssetComment,
				AssetEntity,
				Manufacturer,
				Model,
				AssetDependency,
				Rack,
				MoveBundle,
				Room,
				Files
			]

			List<String> propertyNames = [
			        'id',
					'assetName',
					'description',
					'environment',
					'externalRefId',
					'id',
					'lastUpdated',
					'moveBundle',
					'priority',
					'planStatus',
					'supportType',
					'validation',
			]

		when:
			(1..400).each { int index ->
				Date startTime = new Date()

				classes.each { Class clazz ->

					propertyNames.each { String propertyName ->

						isDomainClass(clazz, closure)
						Boolean isDomainProperty = GormUtil.isDomainProperty(clazz, propertyName)

						if (isDomainProperty) {
							GormUtil.isReferenceProperty(clazz, propertyName)
							GormUtil.getDomainPropertyType(clazz, propertyName)
							GormUtil.getDomainClassOfProperty(clazz, propertyName)
						}
					}
				}

				Date stopTime = new Date()
				TimeDuration timeDuration = TimeCategory.minus( stopTime, startTime )
				// println("Loop ${index}. Evaluation time: ${timeDuration.toMilliseconds()} ms (${timeDuration.toMilliseconds().intdiv(1000)} s)")
			}

		then:
			12 * closure.call(true)
	}

	@Memoized
	static boolean isDomainClass(Class domainClass, Closure closure) {
		Boolean isDomainClass = GormUtil.isDomainClass(domainClass)
		closure(isDomainClass)
		return isDomainClass
	}
}