import groovy.mock.interceptor.*
import org.codehaus.groovy.grails.commons.ConfigurationHolder

import org.apache.log4j.* 

import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * Unit test cases for the CookbookService class
 */
@TestFor(CookbookService)
class CookbookServiceTests extends Specification {
	
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

	def problemRecipe2 = """
groups: [
	[	name: 'PHY',
		filter: [
			class: 'device',
			asset: [
				physical: true,
			],
		]
	]
],
tasks: [
	[
		id: 2120,
		description: 'Task with just indirect',
		title: 'Blah \${it.assetName}',
		team: 'SYS_ADMIN',
		duration: '#startupDuration',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2140,
		description: 'Task with indirect duration and default value',
		title: 'Blah \${it.assetName}',
		team: 'SYS_ADMIN',
		duration: '#startupDuration,10',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2142,
		description: 'Task with indirect duration and default value with valid scale',
		title: 'Blah \${it.assetName}',
		team: 'SYS_ADMIN',
		duration: '#startupDuration,10h',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2144,
		description: 'Task with indirect duration and default value with invalid scale',
		title: 'Blah \${it.assetName}',
		team: 'SYS_ADMIN',
		duration: '#startupDuration,10x',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2160,
		description: 'Task with bogus duration string and unknown team',
		title: 'Blah \${it.assetName}',
		team: 'SWAT_TEAM',
		duration: 'abc',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2200,
		description: 'Task with valid filter group reference',
		title: 'Blah \${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			group: 'PHY',
		],
	],
	[
		id: 2210,
		description: 'Task with bogus filter group reference',
		title: 'Blah \${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			group: 'BOGUS',
		],
	],
	[
		id: 2300,
		description: 'Task with valid filter include reference',
		title: 'Blah \${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			include: 'PHY',
		],
	],
	[
		id: 2301,
		description: 'Task with valid filter include reference',
		title: 'Blah \${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			include: 'BOGUS',
		],
	],
	[
		id: 2310,
		description: 'Task with BOGUS filter include reference',
		title: 'Blah \${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			exclude: ['PHY', 'BOGUS'],
		],
	],
	[
		id: 2320,
		description: 'Task with valid predecessor reference',
		title: 'Blah \${it.assetName}',
		category: 'startup',
		class: 'device',
		predecessor: [
			group: 'PHY',
		],
	],
	[
		id: 2330,
		description: 'Task with bogus predecessor reference',
		title: 'Blah \${it.assetName}',
		category: 'startup',
		class: 'device',
		predecessor: [
			group: ['PHY', 'BOGUS'],
		],
	],
]
	"""
		
	def invalidFilters = """
groups: [
	[
		name: 'GOOD-GROUP',
		filter : [
			class: 'device',
		]
	],
	[
		name: 'BAD-GROUP',
		description: 'This has an asset attribute in the parent filter element',
		filter : [
			class: 'device',
			custom8: '%hasLic%'
		]
	],
],
tasks: [
	[
		id: 4100,
		description: 'custom8 attribute in the proper filter.asset element',
		title: 'GOOD TASK',
		team: 'SYS_ADMIN_LNX',
		category: 'startup',
		duration: 13,
		filter : [
			class: 'asset',
			asset:[
				custom8: '%hasLic%'
			]
		],
	],
	[
		id: 4120,
		description: 'Task has an asset attribute in the parent filter element',
		title: 'BAD TASK',
		team: 'SYS_ADMIN_LNX',
		category: 'startup',
		duration: 13,
		filter : [
			class: 'asset',
			custom8: '%hasLic%'
		],
	],


]
"""
			def notificationRecipeProblem1 = """
/**
			 * Recipe to verify handling non-number input
			 */
tasks: [
	[
		id: 2000,
		title: 'Test 1',
		type: 'milestone',
		category: 'moveday',
		team: 'PROJ_MGR',
		whom: 'Jack	Wawrzynkiewicz',
		sendNotification: a
	]
]
	"""
			def notificationRecipeProblem2 = """
/**
			 * Recipe to verify handling non 0/1 input
			 */
tasks: [
	[
		id: 2000,
		title: 'Test 1',
		type: 'milestone',
		category: 'moveday',
		team: 'PROJ_MGR',
		whom: 'Jack	Wawrzynkiewicz',
		sendNotification: 2
	]
]
	"""
				def notificationRecipeGood1 = """
/**
			 * Recipe to verify notification set to true
			 */
tasks: [
	[
		id: 2000,
		title: 'Test 1',
		type: 'milestone',
		category: 'moveday',
		team: 'PROJ_MGR',
		whom: 'Jack	Wawrzynkiewicz',
		sendNotification: 1
	]
]
	"""
			def notificationRecipeGood2 = """
/**
			 * Recipe to verify notification set to false
			 */
tasks: [
	[
		id: 2000,
		title: 'Test 1',
		type: 'milestone',
		category: 'moveday',
		team: 'PROJ_MGR',
		whom: 'Jack	Wawrzynkiewicz',
		sendNotification: 0
	]
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

	void setup() {
		// add the super call to avoid the "NullPointerException: Cannot invoke method containsKey() on null object" when calling mockDomain 
		//super.setUp() 

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

    def cleanup() {
    }

	//
	// Series of tests for the validateSyntax method as there are numerous issues that could arise
	//
	void testValidateSyntaxGroupIsProperlyFormatted() {
		def recipe = "groups: [$goodGroup], tasks:[$predTask, $goodGeneralTask]"
		def errors = cookbookService.validateSyntax( recipe )
		expect:
			errors == null
	}
	
	void testInvalidFilters() {
		def errors = cookbookService.validateSyntax(invalidFilters)
		
		def expectedErrors = [
			"Group BAD-GROUP in element 2 contains unknown property 'custom8'",
			"Task id 4100 in element 1 property 'class' contains invalid value 'asset'",
			"Task id 4120 in element 1 property 'class' contains invalid value 'asset'",
			"Task id 4120 in element 2 contains unknown property 'custom8'"
		].sort{ a, b -> a.compareTo(b) }
		
		def returnedErrors = errors.collect {
			it.detail
		}.sort{ a, b -> a.compareTo(b) }
		
		expect:
			errors != null
			errors.size() == 4
			returnedErrors == expectedErrors
	}
	
	void testValidateProblem2() {
		def errors = cookbookService.validateSyntax( problemRecipe2 )
		def returnedErrors = errors.collect { 
			it.detail 
		}.sort{ a, b -> a.compareTo(b) }
		def expectedErrors = [
				"Task id 2144 'duration' has invalid reference (#startupDuration,10x)",
				"Task id 2160 'duration' has invalid value (abc)",
				"Task id 2210 'filter/group' references an invalid group BOGUS",
				"Task id 2301 'filter/include' references an invalid group BOGUS",
				"Task id 2310 'filter/exclude' references an invalid group BOGUS",
				"Task id 2330 'predecessor/group' references an invalid group BOGUS"
			].sort{ a, b -> a.compareTo(b) }

		expect:
			errors != null
			errors.size() == 6
			returnedErrors == expectedErrors
	}
	
	void testValidateProblem1() {
		def errors = cookbookService.validateSyntax( problemRecipe1 )
		expect:
			errors == null
	}
	
	void testValidateNotificationProblem1() {
		def errors = cookbookService.validateSyntax( notificationRecipeProblem1 )
		expect:
			errors != null
	}
	
	void testValidateNotificationProblem2() {
		def errors = cookbookService.validateSyntax( notificationRecipeProblem2 )
		expect:
			errors != null
	}
	void testValidateNotificationGood1() {
		def errors = cookbookService.validateSyntax( notificationRecipeGood1 )
		expect:
			errors == null
	}
	void testValidateNotificationGood2() {
		def errors = cookbookService.validateSyntax( notificationRecipeGood2 )
		expect:
			errors == null
	}
	void testSimple() {
		def recipe = "tasks: {}"
		def errors = cookbookService.validateSyntax( recipe )
		expect:
			errors != null
	}

	// Testing the Groups Section
	void testValidateSyntaxGroupMissingName() {
		def recipe = """
			groups: [ [name:'', filter: [class:'application'] ] ], 
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		expect:
			errors != null
			errors.size() == 1
			errors[0].detail.contains('is blank')
	}

	void testValidateSyntaxGroupNameHasSpace() {
		def recipe = """
			groups: [ [name:'a b c', filter: [class:'application'] ] ], 
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		expect:
			errors != null
			errors.size() == 1
			errors[0].detail.contains('contains unsupported space character(s)')
	}

	void testValidateSyntaxGroupHasDuplicateNameDefined() {
		def recipe = """
			groups: [ $goodGroup, $goodGroup ], 
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		expect:
			errors != null
			errors.size() == 1
			errors[0].detail.contains('duplicated in group')
	}

	void testValidateSyntaxGroupHasInvalidClassName() {
		def recipe = """
			groups: [ [name:'APPS', filter: [class:'invalidClassName'] ] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		expect:
			errors != null
			errors.size() == 1
			errors[0].detail.contains("'class' contains invalid value")
	}

	void testValidateSyntaxGroupIsMissingFilterDefinition() {
		def recipe = """
			groups: [ [name:'APPS'] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		expect:
			errors != null
			errors.size() == 1
			errors[0].detail.contains('is missing require section \'filter\'')
	}

	void testValidateSyntaxGroupFilterIsNotMap() {
		def recipe = """
			groups: [ [ name:'APPS', filter: 'Should be a Map but it is not' ] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = cookbookService.validateSyntax( recipe )
		log.info errors
		expect:
			errors != null
			errors.size() == 1
			errors[0].detail.contains('\'filter\' element not properly defined as a map')
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
		expect:
			errors != null
			errors.size() == 1
			errors[0].detail.contains('references undefined group')
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
		expect:
			errors != null
			errors.size() == 1
			errors[0].detail.contains('references undefined group')
	}

	// Testing the Tasks Section
	void testValidateSyntaxGroupMissingTasksSection() {
		def recipe = "groups: [$goodGroup]"
		def errors = cookbookService.validateSyntax( recipe )
		expect:
			errors != null
			errors.size() == 1
			'Recipe is missing required \'tasks\' section' == errors[0].detail
	}

}