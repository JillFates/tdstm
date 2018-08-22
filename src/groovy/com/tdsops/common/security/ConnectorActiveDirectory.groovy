package com.tdsops.common.security

import com.tdsops.common.lang.CollectionUtils
import groovy.util.logging.Commons
import org.apache.directory.groovyldap.LDAP
import org.apache.directory.groovyldap.SearchScope
import com.tdsops.common.lang.ExceptionUtil
import javax.naming.AuthenticationException
import javax.naming.InvalidNameException
import javax.naming.NameNotFoundException
import javax.naming.directory.InvalidSearchFilterException
/**
 * Authenticates with Active Directory via LDAP protocol.
 */
@Commons
@Singleton
class ConnectorActiveDirectory {

	/**
	 * Used to authenticate and get user information from an Active Directory server. If any issues occur during the lookup
	 * then it will throw a UnhandledAuthException exception as the user may be able to authenticate with a different realm.
	 *
	 * @param authority - the domain authority to check up against
	 * @param username - the user login name to lookup
	 * @param password - the password for the account
	 * @param config - A map of the necessary configuration values needed to connect to the AD server and navigate the tree
	 * @return a map of the user information containing the following:
	 * 		username
	 * 		firstname
	 * 		lastname
	 * 		fullname
	 * 		email
	 * 		telephone
	 * 		mobile
	 * 		roles - a list of roles (e.g. User, Editor, Manager, Admin)
	 */

	static Map getUserInfo(String authority, String username, String password, Map ldapConfig) {
		String emsg = ''
		String logPrefix = 'getUserInfo()'
		Map userInfo = [:]
		boolean isError = false
		boolean debug = ldapConfig.debug
		boolean serviceAuthSuccessful = false
		Map domain

		try {

			// Get the Domain configuration by cross referencing the authority (aka host/domain) - was validated that it exists in Realm class
			domain = ldapConfig.domains[authority]

			String queryForUser
			String queryUsername = username

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
					/**
					 * We were originally adding (at least thinking that we were - code error) the domain to the query but in
					 * reality we don't need to so this code has been commented out. After we roll this out and confirm all clients are okay
					 * then we can remove this.
					 * TODO : JPM 10/2015 : Remove this code in 3.2.0
					 *
					 */
					/*
					if (false && domain.domain) {
						if (! queryUsername.contains('\\')) {
							queryUsername = domain.domain + '\\' + username
						} else {
							// validate that the domain is the same as what is defined
							String userEnteredDomain = queryUsername.split(/\\+/)[0]
							if (! userEnteredDomain || userEnteredDomain.toLowerCase() == domain.domain.toLowerCase()) {
								queryUsername = domain.domain = '\\' + username
							} else {
								emsg = 'Invalid domain specified'
								log.info "$logPrefix $emsg by user $username, domain $userEnteredDomain"
								throw new UnhandledAuthException(emsg)
							}
						}
					}
					*/
					queryForUser = "(sAMAccountName=$queryUsername)"
					break
				default:
					emsg = "Unhandle switch for domain.userSearchOn($domain.userSearchOn)"
					log.error "$logPrefix $emsg"
			////// TODO BB throw new UnhandledAuthException(emsg)
			}

			// This user type query is the equivalant of (&(objectCategory=person)(objectClass=user)) but more efficient
			String queryUserType = '(samAccountType=805306368)'
			queryForUser = "(&$queryUserType$queryForUser)"
			if (debug) {
				log.info "$logPrefix LDAP query for user: $queryForUser"
			}

			// Connect using the service bind account
			if (debug) {
				log.info "$logPrefix Initiating LDAP connection to ${domain.url[0]} with system account ($domain.serviceName)"
			}
			def ldap = LDAP.newInstance(domain.url[0], domain.serviceName, domain.servicePassword)

			// Lookup the user by their sAMAccountName
			def results
			int i
			for (i = 0; i < domain.userSearchBase.size(); i++) {
				try {
					String searchBase = domain.userSearchBase[i]
					if (debug) {
						log.debug "$logPrefix Attempting ldap.search($queryUsername, $searchBase, SearchScope.SUB)"
					}
					results = ldap.search(queryForUser, searchBase, SearchScope.SUB)
					if (results.size()) {
						if (debug) {
							log.info "$logPrefix Found user ${results[0].dn} in $searchBase"
						}
						break
					}
				}
				catch (NameNotFoundException userSearchEx) {
					log.info "$logPrefix UserSearch got javax.naming.NameNotFoundException exception"
					// Don't need to do anything
				}
			}

			if (!results.size()) {
				if (debug) {
					log.info "$logPrefix Unable to locate username $username"
				}
				////// TODO BB throw new UnhandledAuthException('Unable to locate username')
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
			log.info "$logPrefix Validated user credentials for $username with AD/LDAP"

			// So we'll search through the roles to see what the user has defined
			if (domain.roleMap && domain.roleMap.size()) {
				if (debug) {
					log.info "$logPrefix Looking up roles $domain.roleMap for mode $domain.roleSearchMode"
				}

				switch (domain.roleSearchMode) {
					case 'nested':
						// Grab the user's nested group memberships by iterating over one or more searchBase values
						def queryNestedGroups = "(member:1.2.840.113556.1.4.1941:=$u.distinguishedname)"
						if (debug) {
							log.info "$logPrefix About to search for nested groups for ($domain.roleBaseDN) with query $queryNestedGroups"
						}
						def g = ldap.search(queryNestedGroups, domain.roleBaseDN, SearchScope.SUB)
						if (g) {
							memberof.addAll(g*.dn)
						}
						break

					case 'direct':
						memberof = CollectionUtils.asList(u.memberof)
						break
					default:
						emsg = "domain.roleSearchMode $domain.roleSearchMode is not supported"
						log.error "$logPrefix $emsg"
				////// TODO BB throw new UnhandledAuthException(emsg)
				}

				if (debug) {
					StringBuilder sb = new StringBuilder()
					memberof.each { sb.append("\n\t'$it'") }
					log.info "$logPrefix User MemberOf (${domain.roleSearchMode.toUpperCase()}): $sb"
				}

				// Search through roles and see if we can match up
				memberof = memberof*.toLowerCase()
				domain.roleMap.each { role, filter ->
					def groupDN = "$filter${domain.roleBaseDN ? ',' + domain.roleBaseDN : ''}"
					if (debug) {
						log.info "$logPrefix searching MemberOf list for '$groupDN'"
					}
					if (memberof.find { it == groupDN.toLowerCase() }) {
						roles << role
						if (debug) {
							log.info "$logPrefix found $role"
						}
					}
				}
			}
			else {
				if (debug) {
					log.info "$logPrefix No roles defined and using defaultRole ($domain.defaultRole)"
				}

				assert domain.defaultRole

				// Set their default role
				roles << domain.defaultRole
			}

			// Map all of the user information into TM userInfo map
			userInfo.companyId = domain.company   // Copy over the company id
			userInfo.username = username
			userInfo.firstName = u.givenname ?: ''
			userInfo.lastName = u.sn ?: ''
			userInfo.fullName = u.cn ?: ''
			userInfo.email = u.mail ?: ''
			userInfo.telephone = u.telephonenumber ?: ''
			userInfo.mobile = u.mobile ?: ''
			userInfo.guid = (u.objectguid ? SecurityUtil.guidToString(u.objectguid) : u.distinguishedname)
			userInfo.roles = roles

			if (debug) {
				def ui = new StringBuilder("User information:\n")
				userInfo.each { k, v -> ui.append("   $k=$v\n") }
				log.info ui.toString()
			}

		}
		catch (InvalidSearchFilterException e) {
			isError = !serviceAuthSuccessful
			emsg = 'InvalidSearchFilterException occurred for ' + (serviceAuthSuccessful ? 'user info search' : 'service lookup of user')
			logMessage("${emsg} : ${e.message}", serviceAuthSuccessful, isError, debug)
		}
		catch (AuthenticationException e) {
			isError = !serviceAuthSuccessful
			emsg = 'Invalid ' + (serviceAuthSuccessful ? 'user' : 'service account') + " password"
			logMessage("${emsg} : ${e.message}", serviceAuthSuccessful, isError, debug)
		}
		catch (NameNotFoundException e) {
			isError = !serviceAuthSuccessful
			emsg = (serviceAuthSuccessful ? 'User' : 'Service account') + ' not found'
			logMessage(emsg, serviceAuthSuccessful, isError, debug)
		}
		catch (InvalidNameException e) {
			emsg = 'User DN was invalid'
			logMessage("$emsg : $e.message", serviceAuthSuccessful, isError, debug)
		}
		catch (NullPointerException e) {
			isError = !serviceAuthSuccessful
			emsg = (serviceAuthSuccessful ? 'Invalid user credentials' : 'Possibly invalid service account credentials or LDAP URL')
			logMessage(emsg, serviceAuthSuccessful, isError, debug)
		}
//		catch (UnhandledAuthException e) {
//			isError = !serviceAuthSuccessful
//			emsg = e.message
//			logMessage(emsg, serviceAuthSuccessful, isError, debug)
//		}
		catch (Exception e) {
			emsg = "Unexpected error with ${(serviceAuthSuccessful ? 'service account' : 'user')} : ${e.message}"
			logMessage(emsg, false, isError, debug, e)
		}

// TODO BB
//		if (emsg) {
//			throw new UnhandledAuthException(emsg)
//		}

		return userInfo
	}

	/**
	 * Used to log messages for debug or error based on if it was a service account issue or not and debugging is enabled
	 * @param msg - the message to log
	 * @param serviceAuthSuccessful - flag that indicates that the Service Account was successfully authenticated
	 * @param isDebugEnabled
	 */
	private static void logMessage(String msg, boolean serviceAuthSuccessful, boolean logError,
		                            boolean isDebugEnabled, Exception exception = null) {
		if (logError) {
			log.error msg + (exception ? " : ${ExceptionUtil.stackTraceToString(exception)}" : '')
		}
		else if (isDebugEnabled) {
			log.info msg + (exception != null ? " : ${ExceptionUtil.stackTraceToString(exception)}" : '')
		}
	}
}
