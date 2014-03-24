package com.tdsops.common.security

import org.apache.directory.groovyldap.LDAP
import org.apache.directory.groovyldap.SearchScope

import java.util.regex.Pattern
import java.util.regex.Matcher
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Class used to authenticate with Active Directory via LDAP protocol
 */
@Singleton
class ConnectorActiveDirectory {

	private static log

	/**
	 * Constructor
	 */
	ConnectorActiveDirectory() {
		log = LogFactory.getLog(this.class)
	}

	/**
	 * Helper method that converts a binary GUID to a string
	 * @param guid a binary string value
	 * @return The guid converted to a hex string
	 */
	static String guidToString( guid ) {
		def addLeadingZero = { k -> 
			return (k <= 0xF ? '0' + Integer.toHexString(k) : Integer.toHexString(k))
		}

		// Where GUID is a byte array returned by a previous LDAP search
		// guid.each { b -> log.info "b is ${b.class}"; str += addLeadingZero( (int)b & 0xFF ) }
		StringBuffer str = new StringBuffer()
		for (int c=0; c < guid.size(); c++) {
			Integer digit = guid.charAt(c)
			str.append( addLeadingZero( digit & 0xFF ) )
		}
		return str
	}

	/**
	 * Used to authenticate and get user information from an Active Directory server
	 * @param username - the user login name to lookup
	 * @param password - the password for the account
	 * @param config - A map of the necessary configuration values needed to connect to the AD server and navigate the tree
	 * @return a map of the user information containing the following:
	 * 		username
	 *		firstname
	 *		lastname
	 *		fullname
	 *		email
	 *		telephone
	 *		mobile
	 *		roles - a list of roles (e.g. User, Editor, Manager, Admin)
	 * @throws ...
	 */
	static Map getUserInfo(username, password, config) {
		def emsg = ''
		def userInfo = [:]
		try {

			// Validate that everything is available in the configuration
			assert config.url
			assert config.domain
			assert config.searchBase

			// Get the user's username, stripping of the @domain if present
			def smauser = username
			if (smauser.contains('@'))
				smauser = smauser.split('@')[0]

			def queryUser = "(&(sAMAccountName=$smauser)(objectClass=user))"


			// Use the config username/password if it exists otherwise just use the credentials of the user
			// def sam = "${config.username}@${config.domain}".toString()
			def usr = config.username ?: smauser
			def pswd = config.username ? config.password : password

			// Connect using the service bind account
			if (config.debug)
				log.info 'Initiating LDAP connection with system account'
			def ldap = LDAP.newInstance(config.url[0], usr, pswd)

			// Lookup the user by their sAMAccountName
			if (config.debug)
				log.info "Peforming user lookup for $queryUser"

			def results
			def i
			for (i=0; i<config.searchBase.size(); i++) {
				results = ldap.search(queryUser, config.searchBase[i], SearchScope.SUB )
				if (results.size()) {
					if (config.debug)
						log.info "Found user in ${config.searchBase[i]}"
					break
				}
			}

			def u
			def nestedGroups = []

			if (results.size()) {

				u = results[0]

				// Grab the user's nested group memberships by iterating over one or more searchBase values
				def queryNestedGroups = "(member:1.2.840.113556.1.4.1941:=${u.distinguishedname})"
				config.searchBase.each { sb ->
					def g = ldap.search(queryNestedGroups, sb, SearchScope.SUB)
					if (config.debug) 
						log.info "Found ${( g ? g.size() : '0')} groups in $sb"
					if (g)
						nestedGroups.addAll(g*.dn)
				}

				// Weed out any duplicates that they may have
				if (nestedGroups)
					nestedGroups = nestedGroups.unique()

				if (config.debug)
					log.info "Unique groups found: $nestedGroups"

/*
				// Grab the user's nested group memberships
				def queryNestedGroups = "(member:1.2.840.113556.1.4.1941:=${u.distinguishedname})"
				nestedGroups = ldap.search(queryNestedGroups, config.searchBase, SearchScope.SUB)
				if (nestedGroups)
					nestedGroups = nestedGroups*.dn
				if (config.debug)
					log.info "Found nested groups: ${nestedGroups}"
*/

				// Now attempt a connect using the user's own DN/password and then try to compare the samaccountname to 
				// validate that the user has given us the correct credentials
				if (config.debug)
					log.info 'Initiating LDAP connection with user credentials'
				ldap = LDAP.newInstance(config.url[0], u.dn, password)
				if (config.debug)
					log.info 'Confirming user credentials'
				assert ldap.compare(u.dn, [samaccountname: smauser] )

				// Map all of the user information into TM userInfo map
				userInfo.company = config.company	// Copy over the company id
				userInfo.username = username
				userInfo.firstName = u.givenname ?: ''
				userInfo.lastName = u.sn ?: ''
				userInfo.fullName = u.cn ?: ''
				userInfo.email = u.mail ?: ''
				userInfo.telephone = u.telephonenumber ?: ''
				userInfo.mobile = u.mobile ?: ''
				userInfo.guid = (u.objectguid ? guidToString( u.objectguid ) : u.dn)
				userInfo.roles = []

			} else {
				if (config.debug)
					log.info "Unable to locate username $username"
				throw new RuntimeException('Unable to locate username')
			}

			// Try to find the user groups the user is associated to any
			if (config.roleMap && nestedGroups) {

				// Need to convert the roleMap into the Regx pattern by merging the config.groupBase array with
				// each of the regx patterns
				def patterns = [] 
				i=0
				config.groupBase.each { gb ->
					config.roleMap.each { k, v ->
						def p = "^${v},$gb"
						if (config.debug)
							log.info "Role ${++i} search pattern for $k, regex: $p"
						patterns << [ role:k, pattern: Pattern.compile( p ) ]
					}
				}

				def rmSize = config.roleMap.size()
				nestedGroups.each { group ->
					if (config.debug)
						log.info "Checking for role match of group $group"
					for (i=0; i < rmSize; i++) {					
						def match = patterns[i].pattern.matcher( group )
						if (match.matches()) {
							userInfo.roles << patterns[i].role
							if (config.debug)
								log.info "Matched ${patterns[i].role} on role ${++i}"
							break
						}
					}
				}
			}

			if (config.debug) {
				def ui = new StringBuffer("User information:\n")
				userInfo.each { k,v -> ui.append("   $k=$v\n") }
				log.info ui.toString()
			}

		} catch (javax.naming.AuthenticationException ae) {
			emsg = 'Invalid user password'
			if (config.debug)
				log.info "$emsg : ${ae.getMessage()}"
		} catch (javax.naming.NameNotFoundException nnfe) {
			emsg = 'Username not found'
			if (config.debug)
				log.info "$emsg : ${nnfe.getMessage()}"
		} catch (javax.naming.InvalidNameException ine) {
			emsg = 'User DN was invalid'
			if (config.debug)
				log.info "$emsg : ${ine.getMessage()}"
		} catch (java.lang.NullPointerException npe) {
			emsg = 'Possibly invalid system user credentials or URL'
			if (config.debug)
				log.info emsg

		} catch (Exception e) {
			emsg = 'Unexpected error'
			if (config.debug)
				log.info "$emsg : ${e.class} ${e.getMessage()}"			
		}

		if (emsg)
			throw new RuntimeException(emsg)

		return userInfo
	}
}
