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

	def predTask = """[
			id: 1106,
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

		
		def problemRecipe1 = """
/**
			 * Recipe to create branching and gather tasks
			 */
tasks: [
	[
		id: 1000,
		title: 'MS: Prep for Move Event',
		type: 'milestone',
		category: 'moveday',
		team: 'PROJ_MGR'
	],	
	[
		id: 1100,
		description: 'Validate ALL applications',
		title: 'Validate app \${it.assetName}',
		whom: '#shutdownBy',
		category: 'shutdown',
		duration: 10,
		filter : [
			class: 'application',
		],
		successor: [
			defer: 'App Tasks'
		]
	],

	[
		id: 1110,
		description: 'Schedule review of ALL applications',
		title: 'Schedule review of app \${it.assetName}',
		whom: '#shutdownBy',
		category: 'shutdown',
		duration: 10,
		filter : [
			class: 'application',
		],
		successor: [
			defer: 'App Tasks'
		]
	],	
	[
		id: 1120,
		description: 'Document ALL applications',
		title: 'Document app \${it.assetName}',
		whom: '#shutdownBy',
		category: 'shutdown',
		duration: 10,
		filter : [
			class: 'application',
		],
		predecessor: [
			taskSpec:1100,	// This will allow 1110 and this (1120) to execute in parallel
 		]
	],	

		[
		id: 1130,
		description: 'Wrap-up ALL applications',
		title: 'Wrap up app \${it.assetName}',
		whom: '#shutdownBy',
		category: 'shutdown',
		duration: 10,
		filter : [
			class: 'application',
		],
		predecessor: [
			gather: 'App Tasks',
		]
	],	

	[
		id: 1200,
		title: 'MS: Prep for Move Event',
		type: 'milestone',
		category: 'moveday',
		team: 'PROJ_MGR'
	],	
]

	"""
		
		
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
		def recipe = "groups: [$goodGroup], tasks:[$predTask, $goodGeneralTask]"
		def errors = cookbookService.validateSyntax( recipe )
		assertNull errors
	}
	
	void testValidateProblem1() {
		def errors = cookbookService.validateSyntax( problemRecipe1 )
		assertNotNull errors
		assertFalse 'Failed for another reason and not isaMap', errors[0].detail.contains('isaMap')
	}
	
	void testSimple() {
		def recipe = "tasks: {}"
		def errors = cookbookService.validateSyntax( recipe )
		assertNotNull errors
	}

	// Testing the Groups Section
	void testValidateSyntaxGroupMissingName() {
		def recipe = """
			groups: [ [name:'', filter: [class:'application'] ] ], 
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have the is blank error message for the name element', errors[0].detail.contains('is blank')
	}

	void testValidateSyntaxGroupNameHasSpace() {
		def recipe = """
			groups: [ [name:'a b c', filter: [class:'application'] ] ], 
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have the spaces not allowed error for the name element', errors[0].detail.contains('contains unsupported space character(s)')
	}

	void testValidateSyntaxGroupHasDuplicateNameDefined() {
		def recipe = """
			groups: [ $goodGroup, $goodGroup ], 
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have the duplicate name error for the name element', errors[0].detail.contains('duplicated in group')
	}

	void testValidateSyntaxGroupHasInvalidClassName() {
		def recipe = """
			groups: [ [name:'APPS', filter: [class:'invalidClassName'] ] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have the invalid class name', errors[0].detail.contains('has invalid filter.class value')
	}

	void testValidateSyntaxGroupIsMissingFilterDefinition() {
		def recipe = """
			groups: [ [name:'APPS'] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		assertNotNull 'Should have errors', errors
		assertEquals 'Should have one error', 1, errors.size()
		assertTrue 'Should have missing filter error message', errors[0].detail.contains('is missing require section \'filter\'')
	}

	void testValidateSyntaxGroupFilterIsNotMap() {
		def recipe = """
			groups: [ [ name:'APPS', filter: 'Should be a Map but it is not' ] ],
			tasks:[$predTask, $goodGeneralTask]"""
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
			tasks:[$predTask, $goodGeneralTask]"""
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
			tasks:[$predTask, $goodGeneralTask]"""
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