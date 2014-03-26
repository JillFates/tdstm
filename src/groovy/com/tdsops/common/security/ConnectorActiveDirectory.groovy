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
			assert config.baseDN

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
				try {
					results = ldap.search(queryUser, config.searchBase[i], SearchScope.SUB )
					if (results.size()) {
						if (config.debug)
							log.info "Found user ${results[0].dn} in ${config.searchBase[i]}"
						break
					}
				} catch (javax.naming.NameNotFoundException userSearchEx) {
					// Don't need to do anything
				}
			}

			if (! results.size()) {
				if (config.debug)
					log.info "Unable to locate username $username"
				throw new javax.naming.NameNotFoundException('Unable to locate username')
			} 
			
			def u = results[0]
			def nestedGroups = []
			def roles = []

			// Grab the user's nested group memberships by iterating over one or more searchBase values
			def queryNestedGroups = "(member:1.2.840.113556.1.4.1941:=${u.distinguishedname})"
			if (config.debug)
				log.info "About to search for nested groups with query $queryNestedGroups"
			def g = ldap.search(queryNestedGroups, config.baseDN, SearchScope.SUB)
			if (g)
				nestedGroups.addAll(g*.dn)

			config.roleMap.each { role, filter ->
				def groupDN="$filter,${config.baseDN}".toLowerCase()
				if (config.debug)
					log.info "Trying to find role $role for DN $groupDN"
				if (nestedGroups.find { it.toLowerCase() == groupDN })
					roles << role
			}

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
			userInfo.roles = roles

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
			emsg = 'Possibly invalid service user credentials or LDAP URL'
			if (config.debug)
				log.info "$emsg : ${npe.getMessage()}"
		} catch (Exception e) {
			emsg = 'Unexpected error'
			log.error "$emsg : ${e.class} ${e.getMessage()}"			
		}

		if (emsg)
			throw new RuntimeException(emsg)

		return userInfo
	}
}
