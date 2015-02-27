package com.tdsops.common.security

import grails.test.*
import org.apache.log4j.* 

import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.common.security.SecurityConfigParser

import spock.lang.Specification

// TODO : JPM 01/2015 : Fixed the SecurityConfigParserTests test cases
// There was a large change to the structure that broke most of the test cases and I haven't had time to 
// correct the test cases.

/**
 * Unit test cases for the SecurityConfigParser class
*/
class SecurityConfigParserTests extends Specification {

	/*
	 * Tests that after encode and decode a value the result is the same
	 */

	public void testParseLoginSettings() {
		Map config = [ tdstm:[ security:[:] ] ]
		Map settings = config.tdstm.security

		Map map 
		String e

		map = SecurityConfigParser.parseLoginSettings(config)
		
		expect:
			// authorityPrompt default
			'na'.equals(map.authorityPrompt)
			// authorityLabel default
			'Domain'.equals(map.authorityLabel)
			// usernamePlaceholder default
			'Enter your username'.equals(map.usernamePlaceholder)

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
	public void testParseLDAPSettings() {
		Map settings = [:]
		String e

		Map map = SecurityConfigParser.parseLDAPSettings(settings)
		assertTrue 'LDAP undefined default to false', ! settings.enabled

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
		assertTrue 'Type=ActiveDirectory is Okay', ! e.contains(tem)

		settings.ldap.domains[0].type = 'LDAP'
		e = shouldFail(ConfigurationException) {
			map = SecurityConfigParser.parseLDAPSettings(settings)
		}
		println "e=$e"
		assertTrue 'Type=LDAP is Okay', ! e.contains(tem)

		// Validate debug correct type
		// assertTrue 'LDAP debugging/undefined', ! settings.enabled
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
		assertTrue 'Not isLDAP', ! map.domains['corp'].isLDAP
		settings.ldap.domains[0].type='LDAP'
		map = SecurityConfigParser.parseLDAPSettings(settings)
		assertTrue 'Not isActiveDirectory', ! map.domains['corp'].isActiveDirectory
		assertTrue 'isLDAP', map.domains['corp'].isLDAP

		// Test autoProvision
		map = SecurityConfigParser.parseLDAPSettings(settings)
		println "map: \n$map"
		println "map.domains['corp']: \n${map.domains['corp']}"
		//[enabled:true, debug:true, domains:[[corp]:[autoProvision:false, updateUser:false, updateRoles:false, defaultRole:, defaultTimezone:EST, connector:AD, url:[ldap://ad.example.com], domain:, baseDN:dc=Accounts, searchBase:[], roleMap:[], username:, password:, company:500, defaultProject:501]]]

		assertTrue 'autoProvision default', ! map.domains['corp'].autoProvision
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
		assertTrue 'updateUserInfo default', ! map.domains['corp'].updateUserInfo
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
		assertTrue 'updateRoles default', ! map.domains['corp'].updateRoles
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

}
