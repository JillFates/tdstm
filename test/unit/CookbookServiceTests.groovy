import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.domain.UserPreference
import net.transitionmanager.service.CookbookService
import net.transitionmanager.service.SecurityService
import test.AbstractUnitSpec

@Mock([UserLogin, UserPreference, SecurityService])
@TestMixin(ControllerUnitTestMixin)
@TestFor(CookbookService)
class CookbookServiceTests extends AbstractUnitSpec { //Specification {

	void setup() {
		login()
		Project project = buildMockProject()

		//Mocking up the Users Current Project
		service.securityService.metaClass.getUserCurrentProject = {
			project
		}
	}

	private static final String goodGroup = '''[
			name: 'ALL_APPS',
			description: 'All applications',
			filter: [
				class: 'application'
			]
		]'''

	private static final String predTask = '''[
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
		]'''

	private static final String goodGeneralTask = '''[
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
		]'''

	private static final String problemRecipe1 = '''
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
		title: 'Validate app ${it.assetName}',
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
		title: 'Schedule review of app ${it.assetName}',
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
		title: 'Document app ${it.assetName}',
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
		title: 'Wrap up app ${it.assetName}',
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
	'''

	private static final String problemRecipe2 = '''
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
		title: 'Blah ${it.assetName}',
		team: 'SYS_ADMIN',
		duration: '#startupDuration',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2140,
		description: 'Task with indirect duration and default value',
		title: 'Blah ${it.assetName}',
		team: 'SYS_ADMIN',
		duration: '#startupDuration,10',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2142,
		description: 'Task with indirect duration and default value with valid scale',
		title: 'Blah ${it.assetName}',
		team: 'SYS_ADMIN',
		duration: '#startupDuration,10h',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2144,
		description: 'Task with indirect duration and default value with invalid scale',
		title: 'Blah ${it.assetName}',
		team: 'SYS_ADMIN',
		duration: '#startupDuration,10x',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2160,
		description: 'Task with bogus duration string and unknown team',
		title: 'Blah ${it.assetName}',
		team: 'SWAT_TEAM',
		duration: 'abc',
		category: 'startup',
		class: 'device',
	],
	[
		id: 2200,
		description: 'Task with valid filter group reference',
		title: 'Blah ${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			group: 'PHY',
		],
	],
	[
		id: 2210,
		description: 'Task with bogus filter group reference',
		title: 'Blah ${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			group: 'BOGUS',
		],
	],
	[
		id: 2300,
		description: 'Task with valid filter include reference',
		title: 'Blah ${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			include: 'PHY',
		],
	],
	[
		id: 2301,
		description: 'Task with valid filter include reference',
		title: 'Blah ${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			include: 'BOGUS',
		],
	],
	[
		id: 2310,
		description: 'Task with BOGUS filter include reference',
		title: 'Blah ${it.assetName}',
		category: 'startup',
		class: 'device',
		filter: [
			exclude: ['PHY', 'BOGUS'],
		],
	],
	[
		id: 2320,
		description: 'Task with valid predecessor reference',
		title: 'Blah ${it.assetName}',
		category: 'startup',
		class: 'device',
		predecessor: [
			group: 'PHY',
		],
	],
	[
		id: 2330,
		description: 'Task with bogus predecessor reference',
		title: 'Blah ${it.assetName}',
		category: 'startup',
		class: 'device',
		predecessor: [
			group: ['PHY', 'BOGUS'],
		],
	],
]
	'''

	private static final String invalidFilters = '''
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
'''

	private static final String notificationRecipeProblem1 = '''
/**
 * Recipe to verify handling string (non-boolean) input
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
	'''

	private static final String notificationRecipeProblem2 = '''
/**
 * Recipe to verify handling integer (non-boolean) input
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
	'''

	private static final String notificationRecipeGood1 = '''
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
		sendNotification: true
	]
]
	'''

	private static final String notificationRecipeGood2 = '''
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
		sendNotification: false
	]
]
	'''

	void testValidateSyntaxGroupIsProperlyFormatted() {
		when:
		def recipe = "groups: [$goodGroup], tasks:[$predTask, $goodGeneralTask]"
		def errors = service.validateSyntax(recipe)
		then:
		!errors
	}

	void testInvalidFilters() {
		when:
		def errors = service.validateSyntax(invalidFilters)
		def returnedErrors = errors*.detail.sort()
		def expectedErrors = [
				"Group BAD-GROUP in element 2 contains unknown property 'custom8'",
				"Task id 4100 in element 1 property 'class' contains invalid value 'asset'",
				"Task id 4120 in element 1 property 'class' contains invalid value 'asset'",
				"Task id 4120 in element 2 contains unknown property 'custom8'"
		].sort()

		then:
		errors
		errors.size() == 4
		returnedErrors == expectedErrors
	}

	void testValidateProblem2() {
		when:
		def errors = service.validateSyntax(problemRecipe2)
		def returnedErrors = errors*.detail.sort()
		def expectedErrors = [
				"Task id 2144 'duration' has invalid reference (#startupDuration,10x)",
				"Task id 2160 'duration' has invalid value (abc)",
				"Task id 2210 'filter/group' references an invalid group BOGUS",
				"Task id 2301 'filter/include' references an invalid group BOGUS",
				"Task id 2310 'filter/exclude' references an invalid group BOGUS",
				"Task id 2330 'predecessor/group' references an invalid group BOGUS"
		].sort()

		then:
		errors
		errors.size() == 6
		returnedErrors == expectedErrors
	}

	void testValidateProblem1() {
		expect:
		!service.validateSyntax(problemRecipe1)
	}

	void testValidateNotificationProblem1() {
		expect:
		service.validateSyntax(notificationRecipeProblem1)
	}

	void testValidateNotificationProblem2() {
		expect:
		service.validateSyntax(notificationRecipeProblem2)
	}

	void testValidateNotificationGood1() {
		expect:
		!service.validateSyntax(notificationRecipeGood1)
	}

	void testValidateNotificationGood2() {
		expect:
		!service.validateSyntax(notificationRecipeGood2)
	}

	void testSimple() {
		expect service.validateSyntax('tasks: {}')
	}

	// Testing the Groups Section
	void testValidateSyntaxGroupMissingName() {
		when:
		def recipe = """
			groups: [ [name:'', filter: [class:'application'] ] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = service.validateSyntax(recipe)

		then:
		errors
		errors.size() == 1
		errors[0].detail.contains('is blank')
	}

	void testValidateSyntaxGroupNameHasSpace() {
		when:
		def recipe = """
			groups: [ [name:'a b c', filter: [class:'application'] ] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = service.validateSyntax(recipe)

		then:
		errors
		errors.size() == 1
		errors[0].detail.contains('contains unsupported space character(s)')
	}

	void testValidateSyntaxGroupHasDuplicateNameDefined() {
		when:
		def recipe = """
			groups: [ $goodGroup, $goodGroup ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = service.validateSyntax(recipe)

		then:
		errors
		errors.size() == 1
		errors[0].detail.contains('duplicated in group')
	}

	void testValidateSyntaxGroupHasInvalidClassName() {
		when:
		def recipe = """
			groups: [ [name:'APPS', filter: [class:'invalidClassName'] ] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = service.validateSyntax(recipe)

		then:
		errors
		errors.size() == 1
		errors[0].detail.contains("'class' contains invalid value")
	}

	void testValidateSyntaxGroupIsMissingFilterDefinition() {
		when:
		def recipe = """
			groups: [ [name:'APPS'] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = service.validateSyntax(recipe)

		then:
		errors
		errors.size() == 1
		errors[0].detail.contains('is missing require section \'filter\'')
	}

	void testValidateSyntaxGroupFilterIsNotMap() {
		when:
		def recipe = """
			groups: [ [ name:'APPS', filter: 'Should be a Map but it is not' ] ],
			tasks:[$predTask, $goodGeneralTask]"""
		def errors = service.validateSyntax(recipe)

		then:
		errors
		errors.size() == 1
		errors[0].detail.contains('\'filter\' element not properly defined as a map')
	}

	void testValidateSyntaxGroupFilterExcludeBadReference() {
		when:
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
		def errors = service.validateSyntax(recipe)

		then:
		errors
		errors.size() == 1
		errors[0].detail.contains('references undefined group')
	}

	void testValidateSyntaxGroupFilterIncludeBadReferenceInAnArray() {
		when:
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
		def errors = service.validateSyntax(recipe)

		then:
		errors
		errors.size() == 1
		errors[0].detail.contains('references undefined group')
	}

	// Testing the Tasks Section
	void testValidateSyntaxGroupMissingTasksSection() {
		when:
		def recipe = "groups: [$goodGroup]"
		def errors = service.validateSyntax(recipe)

		then:
		errors
		errors.size() == 1
		'''Recipe is missing required 'tasks' section''' == errors[0].detail
	}
}
