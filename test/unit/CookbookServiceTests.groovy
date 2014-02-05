import groovy.mock.interceptor.*
import grails.test.GrailsUnitTestCase
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.apache.log4j.* 

/**
 * Unit test cases for the CookbookService class
 */
class CookbookServiceTests extends GrailsUnitTestCase {
	
	def cookbookService = new CookbookService()
	def log

	def goodGroup = """[
			name: 'ALL_APPS',
			description: 'All applications',
			filter: [
				class: 'application'
			]
		]"""

	def goodGeneralTask = """[
			id: 1110,
			description: 'Make NAT changes for various CSG services',
			title: [
				'Make NAT change for STL-PROD-ws.suddenlink.cequel3.com',
				'Make NAT change for STL-PROD-wsp.suddenlink.com-internal',
			],
			team: 'NETWORK_ADMIN',
			duration: 5,
			category: 'shutdown',
			type: 'general',
			terminal: true,
			chain: false,
			predecessor: [
				taskSpec: 1106,
			]
		]"""

	/**
	 * This is used to load the grails-app/conf/Config.groovy which contains both configurations as well as 
	 * dynamic method injections for various object classes to be used by the application
	 */
	private void loadConfig() { 
		GroovyClassLoader classLoader = new GroovyClassLoader(this.class.classLoader) 
		ConfigSlurper slurper = new ConfigSlurper('TEST') 
		ConfigurationHolder.config = slurper.parse(classLoader.loadClass("Config")) 
	} 

	void setUp() {
		// add the super call to avoid the "NullPointerException: Cannot invoke method containsKey() on null object" when calling mockDomain 
		super.setUp() 

		// build a logger...
		BasicConfigurator.configure() 
		LogManager.rootLogger.level = Level.DEBUG
		log = LogManager.getLogger("CookbookService")

		// use groovy metaClass to put the log into your class
		CookbookService.class.metaClass.getLog << {-> log }

		//loadConfig()
		// Initialize various custom methods used by our application
		com.tdsops.metaclass.CustomMethods.initialize

	}

	//
	// Series of tests for the validateSyntax method as there are numerous issues that could arise
	//
	void testValidateSyntaxGroupIsProperlyFormatted() {
		def recipe = "groups: [$goodGroup], tasks:[$goodGeneralTask]"
		def errors = cookbookService.validateSyntax( recipe )
		assertNull errors
	}

	// Testing the Groups Section
	void testValidateSyntaxGroupMissingName() {
		def recipe = """
			groups: [ [name:'', filter: [class:'application'] ] ], 
			tasks:[$goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have the is blank error message for the name element', errors[0].detail.contains('is blank')
	}

	void testValidateSyntaxGroupNameHasSpace() {
		def recipe = """
			groups: [ [name:'a b c', filter: [class:'application'] ] ], 
			tasks:[$goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have the spaces not allowed error for the name element', errors[0].detail.contains('contains unsupported space character(s)')
	}

	void testValidateSyntaxGroupHasDuplicateNameDefined() {
		def recipe = """
			groups: [ $goodGroup, $goodGroup ], 
			tasks:[$goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have the duplicate name error for the name element', errors[0].detail.contains('duplicated in group')
	}

	void testValidateSyntaxGroupHasInvalidClassName() {
		def recipe = """
			groups: [ [name:'APPS', filter: [class:'invalidClassName'] ] ],
			tasks:[$goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have the invalid class name', errors[0].detail.contains('has invalid filter.class value')
	}

	void testValidateSyntaxGroupIsMissingFilterDefinition() {
		def recipe = """
			groups: [ [name:'APPS'] ],
			tasks:[$goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have missing filter error message', errors[0].detail.contains('is missing require section \'filter\'')
	}

	void testValidateSyntaxGroupFilterIsNotMap() {
		def recipe = """
			groups: [ [ name:'APPS', filter: 'Should be a Map but it is not' ] ],
			tasks:[$goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have filter not a map error message', errors[0].detail.contains('\'filter\' element not properly defined as a map')
	}

	void testValidateSyntaxGroupFilterExcludeBadReference() {
		def recipe = """
			groups: [ 
				[ name:'APPS', 
					filter: [
						class: 'device',
						exclude: 'FOO'
					]
				]
			],
			tasks:[$goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have undefined group reference error message', errors[0].detail.contains('references undefined group')
	}

	void testValidateSyntaxGroupFilterIncludeBadReferenceInAnArray() {
		def recipe = """
			groups: [ 
				[ name:'APPS', 
					filter: [
						class: 'device',
						include: ['FOO']
					]
				]
			],
			tasks:[$goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have undefined group reference error message', errors[0].detail.contains('references undefined group')
	}

	// Testing the Tasks Section
	void testValidateSyntaxGroupMissingTasksSection() {
		def recipe = "groups: [$goodGroup]"
		def errors = cookbookService.validateSyntax( recipe )
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertEquals 'Should have tasks section error message', 'Recipe is missing required \'tasks\' section', errors[0].detail
	}

}