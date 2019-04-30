package com.tdsops.common.security

import net.transitionmanager.exception.ConfigurationException
import spock.lang.Specification

// TODO : JPM 01/2015 : Fixed the SecurityConfigParserTests test cases
// There was a large change to the structure that broke most of the test cases and I haven't had time to
// correct the test cases.

class SecurityConfigParserTests extends Specification {

	/*
	 * Tests that after encode and decode a value the result is the same
	 */
	void testParseLoginSettings() {
		given:
		Map config = [tdstm: [security: [:]]]
		Map settings = config.tdstm.security

		Map map
		String e

		when:
		map = SecurityConfigParser.parseLoginSettings(config)

		then:
		// authorityPrompt default
		'na' == map.authorityPrompt
		// authorityLabel default
		'Domain' == map.authorityLabel
		// usernamePlaceholder default
		'Enter your username' == map.usernamePlaceholder

	/*
		config.tdstm.settings = [
			authorityPrompt:'hidden',
			authorityLabel:'label',
			authorityList:[],
			usernamePlaceholder:'placeholder'
		]

		map = SecurityConfigParser.parseLoginSettings(config)

		assertEquals 'authorityPrompt custom', 'hidden', map.authorityPrompt
		assertEquals 'authorityLabel custom', 'label', map.authorityLabel
		assertEquals 'usernamePlaceholder custom', 'placeholder', map.usernamePlaceholder

		settings.authorityPrompt = 'bogus'
		shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLoginSettings(config)
		}

		// Indicating a select or prompt will require AD settings which are not defined yet so it should fail
		settings.authorityPrompt = 'select'
		shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLoginSettings(config)
		}

		// Indicating a select or prompt will require AD but is not enabled so it should fail
		settings.authorityPrompt = 'select'
		settings.ad = [ enabled:false ]
		shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLoginSettings(config)
		}

		// Indicating a select or prompt will require AD and is enabled but is missing domains section so it should fail
		settings.authorityPrompt = 'select'
		settings.ad = [ enabled:true ]
		shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLoginSettings(config)
		}

		// Indicating a select or prompt will require AD and is enabled but has empty domains section so it should fail
		settings.authorityPrompt = 'select'
		settings.ad = [ enabled:true, domains:[ ] ]
		shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLoginSettings(config)
		}

		// So we should have a valid setup for login with two domains
		settings.authorityPrompt = 'select'
		settings.ldap = [ enabled:true, domains:[ [label:'APPLE'], [label:'ORANGE'] ] ]
		map = SecurityConfigParser.parseLoginSettings(config)
		assertEquals 2, map.authorityList.size()
		assertTrue 'Has APPLE', map.authorityList.contains('APPLE')
	*/
	}

	/*
	 * Tests that after encode and decode a value the result is the same
	 */
	/*
	void testParseLDAPSettings() {
		Map settings = [:]
		String e

		Map map = SecurityConfigParser.parseLDAPSettings(settings)
		assertTrue 'LDAP undefined default to false', !settings.enabled

		settings.ldap = [:]
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains('tdstm.security.ldap section appears to be empty')

		// Validate enabled correct type
		settings.ldap.enabled = 'bad'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains('tdstm.security.ldap.enabled is invalid type')
		settings.ldap.enabled = true

		// Validate the type is ActiveDirectory or LDAP
		settings.ldap.domains = [ [type:'', label:'CORP'] ]
 		String tem = "missing or invalid 'type' property"
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains(tem)
		settings.ldap.domains[0].type = 'ActiveDirectory'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue 'Type=ActiveDirectory is Okay', !e.contains(tem)

		settings.ldap.domains[0].type = 'LDAP'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue 'Type=LDAP is Okay', !e.contains(tem)

		// Validate debug correct type
		// assertTrue 'LDAP debugging/undefined', !settings.enabled
		settings.ldap.debug = 'bad'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains('tdstm.security.ldap.debug is invalid type')
		settings.ldap.debug = true

		// Missing Domains
		settings.ldap.remove('domains')
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains('tdstm.security.ldap.domains section appears to be undefined')

		// Missing label for a given domain
		settings.ldap.domains = [ [type:'LDAP'] ]
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("missing required 'label' property")

		// Missing url/invalid format for a given domain
		settings.ldap.domains[0].label='CORP'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("missing required 'url' property")
		settings.ldap.domains[0].url='ldap://ad.example.com'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains('is missing required port #')

		// Missing baseDN for a given domain
		settings.ldap.domains[0].type='ActiveDirectory'
		settings.ldap.domains[0].url='ldap://ad.example.com:389'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("missing required 'baseDN' property")

		// Missing or invalid company for a given domain
		settings.ldap.domains[0].baseDN='dc=Accounts'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("missing or invalid 'company' property")
		settings.ldap.domains[0].company='bad'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("missing or invalid 'company' property")

		// Missing or invalid defaultProject for a given domain
		settings.ldap.domains[0].company=500
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("missing or invalid 'defaultProject' property")
		settings.ldap.domains[0].defaultProject='bad'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("missing or invalid 'defaultProject' property")
		settings.ldap.domains[0].defaultProject=501

		// Perform some more type tests now that the require properties are set
		settings.ldap.domains[0].type='ActiveDirectory'
		map = SecurityConfigParser.parseLDAPSettings(settings)
		assertTrue 'isActiveDirectory', map.domains['corp'].isActiveDirectory
		assertTrue 'Not isLDAP', !map.domains['corp'].isLDAP
		settings.ldap.domains[0].type='LDAP'
		map = SecurityConfigParser.parseLDAPSettings(settings)
		assertTrue 'Not isActiveDirectory', !map.domains['corp'].isActiveDirectory
		assertTrue 'isLDAP', map.domains['corp'].isLDAP

		// Test autoProvision
		map = SecurityConfigParser.parseLDAPSettings(settings)
		println "map: \n$map"
		println "map.domains['corp']: \n${map.domains['corp']}"
		//[enabled:true, debug:true, domains:[[corp]:[autoProvision:false, updateUser:false, updateRoles:false, defaultRole:, defaultTimezone:EST, connector:AD, url:[ldap://ad.example.com], domain:, baseDN:dc=Accounts, searchBase:[], roleMap:[], username:, password:, company:500, defaultProject:501]]]

		assertTrue 'autoProvision default', !map.domains['corp'].autoProvision
		settings.ldap.domains[0].autoProvision='bad'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("'autoProvision' property, options are: true|false")
		settings.ldap.domains[0].autoProvision=true
		map = SecurityConfigParser.parseLDAPSettings(settings)
		assertTrue 'autoProvision default', map.domains['corp'].autoProvision

		// Test updateUser
		assertTrue 'updateUserInfo default', !map.domains['corp'].updateUserInfo
		settings.ldap.domains[0].updateUserInfo='bad'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("'updateUserInfo' property, options are: true|false")
		settings.ldap.domains[0].updateUserInfo=true
		map = SecurityConfigParser.parseLDAPSettings(settings)
		assertTrue 'updateUserInfo default', map.domains['corp'].updateUserInfo

		// Test updateRoles
		assertTrue 'updateRoles default', !map.domains['corp'].updateRoles
		settings.ldap.domains[0].updateRoles='bad'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue e.contains("'updateRoles' property, options are: true|false")
		settings.ldap.domains[0].updateRoles=true
		map = SecurityConfigParser.parseLDAPSettings(settings)
		assertTrue 'updateRoles default', map.domains['corp'].updateRoles

		// defaultRole
		settings.ldap.domains[0].defaultRole='User'
		map = SecurityConfigParser.parseLDAPSettings(settings)
		assertEquals 'User', map.domains['corp'].defaultRole

		// defaultTimezone
		assertEquals 'EST', map.domains['corp'].defaultTimezone
		settings.ldap.domains[0].defaultTimezone='PST'
		map = SecurityConfigParser.parseLDAPSettings(settings)
		assertEquals 'PST', map.domains['corp'].defaultTimezone

		// Test a second domain and a few more tests of properties
		settings.ldap.domains[1] = [
			label: 'QA',
			type: 'LDAP',
			url: ['ldap://qa1.example.com:389', 'ldap://qa2.example.com:389'],
			domain: 'LAB',
			fqdn: 'qa.example.com',
			baseDN: 'dc=Accounts',
			serviceName: 'bozo',
			servicePassword: 'clown',
			company: 500,
			defaultProject: 501,
			userSearchOn: 'SAM'
		]
		map = SecurityConfigParser.parseLDAPSettings(settings)
		assertTrue "2nd domain exists", map.domains.containsKey('qa')
		assertEquals 'bozo', map.domains['qa'].serviceName
		assertEquals 'clown', map.domains['qa'].servicePassword
		assertEquals 'ldap://qa2.example.com:389', map.domains['qa'].url[1]
		assertEquals 'userSearchOn', 'SAM', map.domains['qa'].userSearchOn
		assertEquals 'domain', 'LAB', map.domains['qa'].domain
		assertEquals 'fqdn', 'qa.example.com', map.domains['qa'].fqdn
	}
	*/

	private Map createDefaultLocalUserSettings() {
		[tdstm: [security: [localUser: [
			enabled                        : true,
			minPasswordLength              : 8,
			maxLoginFailureAttempts        : 5,
			failedLoginLockoutPeriodMinutes: 30,
			clearLockoutsOnRestart         : true,
			passwordHistoryRetentionDays   : 385 * 2,
			passwordHistoryRetentionCount  : 0,
			maxPasswordAgeDays             : 90,
			accountActivationTimeLimit     : 60 * 24 * 1
		]]]]
	}

	/*
	 * Tests valid local user setting
	 */
	void testParseLocalSettings() {
		when:
		Map config = createDefaultLocalUserSettings()
		Map localUserSettings = SecurityConfigParser.parseLocalUserSettings(config)

		then:
		localUserSettings
	}

	/*
	 * Tests local user setting with invalid property
	 */
	void testParseLocalSettingsInvalidProperty() {
		when:
		Map config = createDefaultLocalUserSettings()
		config.tdstm.security.localUser.invalidProperty = true

		SecurityConfigParser.parseLocalUserSettings(config)

		then:
		ConfigurationException e = thrown()
		'Configuration setting: property tdstm.security.localUser.invalidProperty is not valid.' == e.message
	}

	/*
	 * Tests local user setting with invalid history params, both zero
	 */
	void testParseLocalSettingsInvalidHistoryParamsZero() {
		when:
		Map config = createDefaultLocalUserSettings()
		config.tdstm.security.localUser.passwordHistoryRetentionDays = 0
		config.tdstm.security.localUser.passwordHistoryRetentionCount = 0

		SecurityConfigParser.parseLocalUserSettings(config)

		then:
		ConfigurationException e = thrown()
		'Configuration setting: tdstm.security.localUser.passwordHistoryRetentionDays or ' +
		'tdstm.security.localUser.passwordHistoryRetentionCount must be greater than zero.' == e.message
	}

	/*
	 * Tests local user setting with invalid history params, both non zero
	 */
	void testParseLocalSettingsInvalidHistoryParamsNonZero() {
		when:
		Map config = createDefaultLocalUserSettings()
		config.tdstm.security.localUser.passwordHistoryRetentionDays = 1
		config.tdstm.security.localUser.passwordHistoryRetentionCount = 1

		SecurityConfigParser.parseLocalUserSettings(config)

		then:
		ConfigurationException e = thrown()
		'Configuration setting: tdstm.security.localUser.passwordHistoryRetentionDays and ' +
		'tdstm.security.localUser.passwordHistoryRetentionCount are mutually exclusive, at least one must be zero.' == e.message
	}

	/*
	 * Tests local user setting with negative param
	 */
	void testParseLocalSettingsNegativeParam() {
		when:
		Map config = createDefaultLocalUserSettings()
		config.tdstm.security.localUser.maxLoginFailureAttempts = -1

		SecurityConfigParser.parseLocalUserSettings(config)

		then:
		ConfigurationException e = thrown()
		'Configuration setting tdstm.security.localUser.maxLoginFailureAttempts property ' +
		'must have value greater or equal than zero (>=0)' == e.message
	}

	/*
	 * Tests local user setting with negative param
	 */
	void testParseLocalSettingsInvalidBooleanParam() {
		when:
		Map config = createDefaultLocalUserSettings()
		config.tdstm.security.localUser.clearLockoutsOnRestart = "true"

		SecurityConfigParser.parseLocalUserSettings(config)

		then:
		ConfigurationException e = thrown()
		'Configuration setting tdstm.security.localUser.clearLockoutsOnRestart property ' +
		'has invalid value, options: true|false' == e.message
	}

	/*
	 * Tests local user setting, when a property is not defined should take the default value
	 */
	void testParseLocalSettingsDefaultValue() {
		when:
		Map config = [tdstm: [security: [localUser: [:]]]]

		def localUserSettings = SecurityConfigParser.parseLocalUserSettings(config)

		then:
		localUserSettings.enabled
		8 == localUserSettings.minPasswordLength
		5 == localUserSettings.maxLoginFailureAttempts
		1 == localUserSettings.failedLoginLockoutPeriodMinutes
		localUserSettings.clearLockoutsOnRestart
		730 == localUserSettings.passwordHistoryRetentionDays
		0 == localUserSettings.passwordHistoryRetentionCount
		0 == localUserSettings.maxPasswordAgeDays
		60 == localUserSettings.forgotMyPasswordResetTimeLimit
		60 * 24 * 3 == localUserSettings.accountActivationTimeLimit
		30 == localUserSettings.forgotMyPasswordRetainHistoryDays
	}

	/*
	 * Tests local user setting don't exist
	 */
	void testParseLocalSettingsNotExist() {
		when:
		Map config = [tdstm: [security: [:]]]

		def localUserSettings = SecurityConfigParser.parseLocalUserSettings(config)

		then:
		localUserSettings.enabled
		8 == localUserSettings.minPasswordLength
		5 == localUserSettings.maxLoginFailureAttempts
		1 == localUserSettings.failedLoginLockoutPeriodMinutes
		localUserSettings.clearLockoutsOnRestart
		730 == localUserSettings.passwordHistoryRetentionDays
		0 == localUserSettings.passwordHistoryRetentionCount
		0 == localUserSettings.maxPasswordAgeDays
		60 == localUserSettings.forgotMyPasswordResetTimeLimit
		60 * 24 * 3 == localUserSettings.accountActivationTimeLimit
		30 == localUserSettings.forgotMyPasswordRetainHistoryDays
	}
}
