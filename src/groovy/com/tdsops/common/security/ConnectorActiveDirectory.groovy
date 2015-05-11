package com.tdsops.common.security

import org.apache.directory.groovyldap.LDAP
import org.apache.directory.groovyldap.SearchScope

import java.util.regex.Pattern
import java.util.regex.Matcher
 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tdsops.common.security.SecurityConfigParser
import com.tdsops.common.security.SecurityUtil

import com.tdsops.common.lang.ExceptionUtil
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
	 * Used to authenticate and get user information from an Active Directory server
	 * @param authority - the domain authority to check up against
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
	static Map getUserInfo(String authority, String username, String password, Map ldapConfig) {
		String emsg = ''
		Map userInfo = [:]
		boolean debug = ldapConfig.debug
		boolean serviceAuthSuccessful = false
		Map domain 

		try {

			// Get the Domain configuration by cross referencing the authority (aka host/domain) - was validated that it exists in Realm class
			domain = ldapConfig.domains[authority]

			String queryForUser
			String queryUsername=username

			switch (domain.userSearchOn) {
				case 'UPN':
					if (domain.usernameFormat == 'username') {
						// Need to add the selected domain to the username for the lookup
						queryUsername += '@' + domain.fqdn
					}
					queryForUser = "(userPrincipalName=$queryUsername)"
					break
				case 'SAM':
					// If the user is logging in with SAM check to see if the domain was part of the username and add it if not
					if (domain.domain) {
						if (! queryUsername.contains('\\')) {
							queryUsername = domain.domain = '\\' + username
						} else {
							// validate that the domain is the same as what is defined
							String userEnteredDomain = queryUsername.split(/\\+/)[0]
							if (! userEnteredDomain || userEnteredDomain.toLowerCase() == domain.domain.toLowerCase()) {
								queryUsername = domain.domain = '\\' + username
							} else {
								throw new javax.naming.AuthenticationException('Invalid domain specified')
							}
						}
					}
					queryForUser = "(sAMAccountName=$queryUsername)"
					break
				default:
					throw new RuntimeException("Unhandle switch for domain.userSearchOn(${domain.userSearchOn})")
			}
			// This user type query is the equivalant of (&(objectCategory=person)(objectClass=user)) but more efficient
			String queryUserType = '(samAccountType=805306368)'
			queryForUser = "(&$queryUserType$queryForUser)"
			if (debug)
				log.info "LDAP query for user: $queryForUser"

			// Connect using the service bind account
			if (debug)
				log.info "Initiating LDAP connection to ${domain.url[0]} with system account (${domain.serviceName})"
			def ldap = LDAP.newInstance(domain.url[0], domain.serviceName, domain.servicePassword)

			// Lookup the user by their sAMAccountName
			def results
			def i
			for (i=0; i < domain.userSearchBase.size(); i++) {
				try {
					String searchBase = domain.userSearchBase[i]
					log.debug "Attempting ldap.search($queryUsername, ${searchBase}, SearchScope.SUB)"
					results = ldap.search(queryForUser, searchBase, SearchScope.SUB )
					if (results.size()) {
						if (debug)
							log.info "Found user ${results[0].dn} in ${searchBase}"
						break
					}
				} catch (javax.naming.NameNotFoundException userSearchEx) {
					log.debug "UserSearch got javax.naming.NameNotFoundException exception"
					// Don't need to do anything
				}
			}

			if (! results.size()) {
				log.info "Unable to locate username $username"
				throw new javax.naming.NameNotFoundException('Unable to locate username')
			} 
			
			def u = results[0]
			def memberof = []
			def roles = []

			// Flag that the Service Account was successful in authenticating after a successful search
			serviceAuthSuccessful = true

			// Switch the connection to the user's credentials for the rest of the queries so that the user authentication is validated
			ldap = LDAP.newInstance(domain.url[0], u.dn, password)

			// This will validate that the user credentials are valid and will tick the BadPwdCount if it fails
			assert ldap.exists(u.dn)
			log.info "Validated user credentials for $username with AD/LDAP"

			// So we'll search through the roles to see what the user has defined
			if (domain.roleMap && domain.roleMap.size()) {
				if (debug)
					log.info "About to lookup roles ${domain.roleMap} for mode ${domain.roleSearchMode}"

				switch (domain.roleSearchMode) {
					case 'nested':
						// Grab the user's nested group memberships by iterating over one or more searchBase values
						def queryNestedGroups = "(member:1.2.840.113556.1.4.1941:=${u.distinguishedname})"
						if (debug)
							log.info "About to search for nested groups for (${domain.roleBaseDN}) with query $queryNestedGroups"
						def g = ldap.search(queryNestedGroups, domain.roleBaseDN, SearchScope.SUB)
						if (g)
							memberof.addAll(g*.dn)
						break

					case 'direct':
						memberof = ( u.memberof instanceof List ? u.memberof : [ u.memberof.toString() ] )
						break
					default:
						throw new RuntimeException("domain.roleSearchMode ${domain.roleSearchMode} is not supported")
				}

				if (debug) {
					StringBuffer sb = new StringBuffer()
					memberof.each {sb.append("\n\t$it")}
					log.info "USER MEMBEROF (${domain.roleSearchMode.toUpperCase()}): $sb"
				}

				// Search through roles and see if we can match up
				memberof = memberof*.toLowerCase()
				domain.roleMap.each { role, filter ->
					def groupDN="$filter${domain.roleBaseDN ? ','+domain.roleBaseDN : ''}".toLowerCase()
					if (memberof.find { it == groupDN })
						roles << role
				}

			} else {
				if (debug)
					log.info "No roles defined and using defaultRole (${domain.defaultRole})"
				assert domain.defaultRole
				// Set their default role
				roles << domain.defaultRole
			}

			// Map all of the user information into TM userInfo map
			userInfo.companyId = domain.company	// Copy over the company id
			userInfo.username = username
			userInfo.firstName = u.givenname ?: ''
			userInfo.lastName = u.sn ?: ''
			userInfo.fullName = u.cn ?: ''
			userInfo.email = u.mail ?: ''
			userInfo.telephone = u.telephonenumber ?: ''
			userInfo.mobile = u.mobile ?: ''
			userInfo.guid = (u.objectguid ? SecurityUtil.guidToString( u.objectguid ) : u.distinguishedname)
			userInfo.roles = roles

			if (debug) {
				def ui = new StringBuffer("User information:\n")
				userInfo.each { k,v -> ui.append("   $k=$v\n") }
				log.info ui.toString()
			}

			// Make sure that the user has at least one assigned role if we're updating roles via AD
			if (domain.updateRoles && roles.size()==0) {
				throw new javax.naming.AuthenticationException('User has no assigned roles')
			}

		} catch (javax.naming.directory.InvalidSearchFilterException e) {
			emsg = "InvalidSearchFilterException occurred : ${e.getMessage()}" 
			// log.error "$emsg : ${e.getMessage()}"
		} catch (javax.naming.AuthenticationException ae) {
			emsg = (ae.getMessage() ?: 'Invalid user password' )
			if (debug)
				log.info "$emsg : ${ae.getMessage()}"
		} catch (javax.naming.NameNotFoundException nnfe) {
			emsg = 'Username not found'
			if (debug)
				log.info "$emsg : ${nnfe.getMessage()}"
		} catch (javax.naming.InvalidNameException ine) {
			emsg = 'User DN was invalid'
			if (debug)
				log.info "$emsg : ${ine.getMessage()}"
		} catch (java.lang.NullPointerException npe) {
			emsg = serviceAuthSuccessful ? 'Invalid user credentials' : 'Possibly invalid service account credentials or LDAP URL'
			if (debug) 
				log.info "$emsg\n${ExceptionUtil.stackTraceToString(npe)}"
		} catch (Exception e) {
			emsg = 'Unexpected error'
			log.error "$emsg : ${e.class} ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"			
		}

		if (emsg)
			throw new javax.naming.AuthenticationException(emsg)

		return userInfo
	}
}