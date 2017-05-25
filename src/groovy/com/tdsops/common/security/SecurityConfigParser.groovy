package com.tdsops.common.security

import com.tdsops.common.exceptions.ConfigurationException
import com.tdsops.common.lang.CollectionUtils
import com.tdssrc.grails.NumberUtil

/**
 * Parses and validates the configuration security related properties.
 */
class SecurityConfigParser {
	private static final int DEFAULT_INACTIVITY_DAYS_LOCKOUT = 60
	private static final String propNamePrefix = 'tdstm.security'

	private static final validLocalUserSettingsNames = [
		enabled:                         true,
		minPasswordLength:                  8,
		maxLoginFailureAttempts:            5,
		failedLoginLockoutPeriodMinutes:    1,
		clearLockoutsOnRestart:          true,
		passwordHistoryRetentionDays: 365 * 2,
		passwordHistoryRetentionCount:      0,
		maxPasswordAgeDays:                 0,
		forgotMyPasswordResetTimeLimit:    60,	// Minutes
		accountActivationTimeLimit:         3 * 24 * 60,		// 3 days
		forgotMyPasswordRetainHistoryDays: 30,
		minPeriodToChangePswd:              0,
		forceUseNewEncryption:          false]

	// Gets the value of a property
	private static prop(value, defaultValue = null) {
		value == null && defaultValue != null ? defaultValue : value
	}

	// Get the boolean value of a property and confirm that it is valid
	private static String requiredProp(String propName, value, String extraMsg = '') {
		if (value == null || value.size() == 0) {
			throwInvalid(propName, 'is required' + (extraMsg ? ' ' + extraMsg : ''))
			throw new ConfigurationException("Configuration setting ${propNamePrefix}.$propName is required" + (extraMsg ? " $extraMsg" : ''))
		}
		return value
	}


	// Get the boolean value of a property and confirm that it is valid
	private static boolean boolProp(String propName, value, Boolean defaultValue = null) {
		if (value == null || value instanceof ConfigObject) {
			value = defaultValue
		}
		if (!(value instanceof Boolean)) {
			throwInvalid(propName, 'has invalid value, options: true|false')
		}
		return value
	}

	// Get the long value of a property and confirm that it is valid (i.e. it is zero or greater)
	private static Long zeroOrPositiveProp(String propName, value, Number defaultValue = null) {
		if (value == null || (value instanceof ConfigObject)) {
			value = defaultValue
		}
		value = NumberUtil.toLong(value, 0)
		if (value < 0) {
			throwInvalid(propName, 'must have value greater or equal than zero (>=0)')
		}
		return value
	}

	private static boolean requiredBoolProp(String propName, value) {
		value = boolProp(propName, value)
		if (value == null) {
			throwInvalid(propName, 'is required')
		}
		return value
	}

	private static List listProp(String propName, value, defaultValue = null) {
		if (value == null && defaultValue != null) {
			value = defaultValue
		}

		value = CollectionUtils.asList(value)
		value
	}

	private static List requiredListProp(String propName, value) {
		if (value == null) {
			throwInvalid(propName, 'is required')
		}

		listProp(propName, value)
	}

	private static String validProp(String propName, value, List values, String defaultValue) {
		if (value == null) {
			return defaultValue
		}

		if (!(value in values)) {
			throwInvalid(propName, 'has invalid value (' + value + '), options: ' + values)
		}

		value
	}

	private static String requiredAndValidProp(String propName, value, List values, String extraMsg = '') {
		if (!(value in values)) {
			throwInvalid(propName, 'has invalid value' + (extraMsg ? ' ' + extraMsg : '') + ', options: ' + values)
		}
		return value
	}

	private static Long requiredLongProp(String propName, value) {
		value = NumberUtil.toLong(value, 0)
		if (value < 1) {
			throwInvalid(propName, 'must have value greater than zero (>0)')
		}
		return value
	}

	/**
	 * Parses the properties read from the configuration file and constructs the appropriate map for the Login form properties
	 * @param properties - the configuration mapping for security properties
	 * @return A map of all of the necessary parameters
	 *		authorityPrompt - na=n/a, select=select dropdown, prompt=Input field, hidden=Hidden field populated with authorityName
	 *		authorityLabel - used as the label for the authority prompt in login form
	 *		authorityName - used when authorityPrompt == 'hidden', which references one of the defined LDAP domain labels
	 *		autorityList - a list of all of the defined LDAP domain labels
	 * 		usernamePlaceholder -  Text used to override the placeholder value in the username input field
	 */
	static Map parseLoginSettings(properties) {
		// Default values
		Map map = [authorityPrompt: 'na', authorityLabel: 'Domain', authorityName: '', authorityList: [],
		           usernamePlaceholder: 'Enter your username']

		Map props = properties.tdstm.security

		// println "**** Properties====$properties"

		def ap = requiredAndValidProp('authorityPrompt', props?.authorityPrompt ?: map.authorityPrompt,
			['select', 'prompt', 'hidden', 'na'])

		map.authorityPrompt = ap

		if (ap != 'na') {
			// If we're prompting for an authority that means that LDAP integration must be enabled so
			// lets check that and that there are domains defined
			def ldapEnabled = requiredBoolProp('ldap.enabled', props.ldap?.enabled)

			List domains = props.ldap?.domains?.keySet() as List
			if (!domains) {
				throwInvalid('ldap.domains', 'must be defined')
			}

			map.authorityList = domains

			// If there is no user prompting for the domain, lets validate that the
			if (ap == 'hidden') {
				map.authorityName = requiredAndValidProp('authorityName',
					props.authorityName, domains, "for authorityPrompt='hidden'")
			}
		}

		def label = prop(props.authorityLabel, '')
		if (label != '') {
			map.authorityLabel = label
		}

		def uph = prop(props.usernamePlaceholder, '')
		if (uph != '') {
			map.usernamePlaceholder = uph
		}

		map.inactiveDaysLockout = prop(props.inactiveDaysLockout ?: null, DEFAULT_INACTIVITY_DAYS_LOCKOUT)
		map.inactivityWhitelist = prop(props.inactivityWhitelist ?: null, [])

		return map
	}

	/**
	 * Parse the properties read from the configuration file and construct the appropriate map for the Login form properties
	 * @param properties - the configuration mapping for security properties
	 * @return A map of all of the necessary parameters
	 */
	static Map parseLDAPSettings(properties) {
		Map map = [enabled: false]

		Map ldap = properties.tdstm.security?.ldap
		if (!ldap) {
			return map
		}

		map.enabled = boolProp('ldap.enabled', ldap.enabled, false)
		if (!map.enabled) {
			return map
		}

		map.debug = boolProp('ldap.debug', ldap.debug, false)

		map.authorityPrompt = prop(properties.tdstm.security.authorityPrompt, 'na')

		map.domainList = ldap.domains?.keySet()
		if (!map.domainList) {
			throwInvalid('ldap.domains', 'must be defined')
		}
		map.domains = [:]

		// Map all of the individual fields for each of the domains with the default values
		map.domainList.each { domainCode ->
			Map dm = [autoProvision:false, updateUser:false, updateRoles:false, defaultRole:'', defaultTimezone:'GMT']
			String errMsg = "Security properties tdstm.security"

			String p = 'ldap.domains.' + domainCode
			Map d = ldap.domains[domainCode]
			dm.with {
				type = requiredAndValidProp(p + '.type', d.type, ['ActiveDirectory', 'LDAP'])
				isActiveDirectory = (type == 'ActiveDirectory')
				isLDAP = !isActiveDirectory

				// Get the URL(s) and validate that the port number is tacked onto the URL
				url = requiredListProp(p + '.url', d.url ?: null)
				url.each { u ->
					if (!(u ==~ /.*:[0-9]{1,}/)) {
						throw new ConfigurationException(errMsg + " url '$u' is missing required port # (e.g. 'ldap://ad.example.com:389')")
					}
				}

				domain = prop(d.domain) ?: ''
				fqdn = prop(d.fqdn) ?: ''

				usernameFormat = requiredProp(p + '.usernameFormat', d.usernameFormat ?: null)

				userSearchBase = requiredListProp(p + '.userSearchBase', d.userSearchBase ?: null)

				// Get the service credentials
				serviceName = requiredProp(p + '.serviceName', d.serviceName ?: null)
				servicePassword = requiredProp(p + '.servicePassword', d.servicePassword ?: null)

				userSearchOn = validProp(p + '.userSearchOn', d.userSearchOn ?: null, ['UPN','SAM'], 'UPN')

				roleBaseDN = requiredProp(p + '.roleBaseDN', d.roleBaseDN ?: null)

				company = requiredLongProp(p + '.company', d.company ?: null)
				defaultProject = requiredLongProp(p + '.defaultProject', d.defaultProject ?: null)

				autoProvision = boolProp(p + '.autoProvision', d.autoProvision, false)
				updateUserInfo = boolProp(p + '.updateUserInfo', d.updateUserInfo, false)
				updateRoles = boolProp(p + '.updateRoles', d.updateRoles, false)

				defaultRole = prop(d.defaultRole, '')

				// roleMap
				if (d.roleMap) {
					if (d.roleMap instanceof Map) {
						roleMap = d.roleMap
					}
					else {
						throw new ConfigurationException(errMsg + '.' + p  + '.roleMap declaration is invalid and must be a Map')
					}
				} else {
					roleMap = [:]
				}

				if (updateRoles && !dm.roleMap && StringUtil.isEmpty(defaultRole)) {
					throw new ConfigurationException(errMsg + '.' + p  + '.roleMap and/or "defaultRole" ' +
						'property is required in order to support updateRoles')
				}

				// roleSearchMode
				List rsm=['direct', 'nested']
				// println "dm.roleSearchMode = $dm.roleSearchMode"
				if (roleMap.size()) {
					roleSearchMode = requiredAndValidProp(p + '.roleSearchMode', d.roleSearchMode, rsm)
				} else {
					roleSearchMode = prop(d.roleSearchMode, rsm)
					if (roleSearchMode && !rsm.contains(roleSearchMode)) {
						throw new ConfigurationException(errMsg + '.' + p  + '.roleSearchMode has invalid value (' +
							roleSearchMode + '), options are: ' + rsm)
					}
				}

				defaultTimezone = prop(d.defaultTimezone) ?: 'GMT'
			}
			map.domains[domainCode.toLowerCase()] = dm
		}
		return map
	}

	/**
	 * Determine if any of the specified domains is ActiveDirectory
	 * @param The LDAP configuration map
	 * @return true if AD is defined
	 */
	static boolean hasActiveDirectoryDomain(Map map) {
		map.domains.entrySet().any { it.value.isActiveDirectory }
	}

	/**
	 * Use to determine if any of the specified domains is LDAP vs ActiveDirectory
	 * @param The LDAP configuration map
	 * @return true if AD is defined
	 */
	static boolean hasLDAPDomain(Map map) {
		map.domains.entrySet().any { it.value.isLDAP }
	}

	/**
	 * Return a list of RoleType codes for each of the roleMap roles specificed in a given security configuration domain
	 * @param map - a map of the configuration
	 * @param domain - the domain within the configuration to look for the associated roleMap
	 * @return the list of RoleType objects
	 */
	static List getDomainRoleTypeCodes(Map map, String domain) {
		List list = []
		domain = domain.toLowerCase()
		if (map.domains.containsKey(domain) && map.domains[domain].containsKey('roleMap')) {
			list = map.domains[domain].roleMap.keySet() as String[]
		}
		return list
	}

	/**
	 * Parses the properties read from the configuration file and constructs the map
	 * for the user local settings properties.
	 *
	 * @param properties the configuration mapping for user local settions properties
	 * @return the necessary parameters
	 */
	static Map parseLocalUserSettings(Map properties) {
		Map map = [:]

		Map<String, Object> localUser = properties.tdstm.security.localUser ?: [:]

		for (String prop in localUser.keySet()) {
			if (validLocalUserSettingsNames[prop] == null) {
				throw new ConfigurationException("Configuration setting: property ${propNamePrefix}.localUser.$prop is not valid.")
			}
		}

		// Flag if the local user accounts are supported, values true|false (default true)
		map.enabled = boolProp('localUser.enabled', localUser.enabled, true)

		// If not enabled then we're out of here
		if (!map.enabled) {
			return map
		}

		// Minimum number of characters the password must be (default 8)
		map.minPasswordLength = zeroOrPositiveProp(
			'localUser.minPasswordLength',
			localUser.minPasswordLength,
			validLocalUserSettingsNames['minPasswordLength']
		)

		// Number of times user can fail login before the account is locked out, set to zero (0) prevents lockout, default 5
		map.maxLoginFailureAttempts = zeroOrPositiveProp(
			'localUser.maxLoginFailureAttempts',
			localUser.maxLoginFailureAttempts,
			validLocalUserSettingsNames['maxLoginFailureAttempts']
		)

		// How long to disable an account that failed logins to many times, set to zero (0) will lockout the account in definitely, default 30 minutes
		map.failedLoginLockoutPeriodMinutes = zeroOrPositiveProp(
			'localUser.failedLoginLockoutPeriodMinutes',
			localUser.failedLoginLockoutPeriodMinutes,
			validLocalUserSettingsNames['failedLoginLockoutPeriodMinutes']
		)

		// As a fail-safe, locked out accounts could be cleared when the application is restarted in the event of a DOS style attack (default true)
		map.clearLockoutsOnRestart = boolProp(
			'localUser.clearLockoutsOnRestart',
			localUser.clearLockoutsOnRestart,
			validLocalUserSettingsNames['clearLockoutsOnRestart']
		)

		// How long to retain password history to prevent re-use, set to zero (0) disables password history (default 2 years). This is mutually exclusive with passwordHistoryRetentionCount.
		map.passwordHistoryRetentionDays = zeroOrPositiveProp(
			'localUser.passwordHistoryRetentionDays',
			localUser.passwordHistoryRetentionDays,
			validLocalUserSettingsNames['passwordHistoryRetentionDays']
		)

		// How many previous passwords to retain to prevent the user from reusing. This is  with passwordHistoryRetentionDays
		map.passwordHistoryRetentionCount = zeroOrPositiveProp(
			'localUser.passwordHistoryRetentionCount',
			localUser.passwordHistoryRetentionCount,
			validLocalUserSettingsNames['passwordHistoryRetentionCount']
		)

		// The maximum number of days a password can be used before the user must change it, set to zero (0) disable the feature (default 0 days)
		map.maxPasswordAgeDays = zeroOrPositiveProp(
			'localUser.maxPasswordAgeDays',
			localUser.maxPasswordAgeDays,
			validLocalUserSettingsNames['maxPasswordAgeDays']
		)

		// forgot my password reset time limit
		map.forgotMyPasswordResetTimeLimit = zeroOrPositiveProp(
			'localUser.forgotMyPasswordResetTimeLimit',
			localUser.forgotMyPasswordResetTimeLimit,
			validLocalUserSettingsNames['forgotMyPasswordResetTimeLimit']
		) //(minutes)

		// forgot my password welcome email time limit
		map.accountActivationTimeLimit = zeroOrPositiveProp(
			'localUser.accountActivationTimeLimit',
			localUser.accountActivationTimeLimit,
			validLocalUserSettingsNames['accountActivationTimeLimit']
		) //(minutes)

		// forgot my password retain history days
		map.forgotMyPasswordRetainHistoryDays = zeroOrPositiveProp(
			'localUser.forgotMyPasswordRetainHistoryDays',
			localUser.forgotMyPasswordRetainHistoryDays,
			validLocalUserSettingsNames['forgotMyPasswordRetainHistoryDays']
		)

		// prevent the user from changing their password (hours)
		map.minPeriodToChangePswd = zeroOrPositiveProp(
			'localUser.minPeriodToChangePswd',
			localUser.minPeriodToChangePswd,
			validLocalUserSettingsNames['minPeriodToChangePswd']
		)

		// Disable the forcing of users from changing their passwords if the hash method used was the obsolete one.
		map.forceUseNewEncryption = boolProp(
			'localUser.forceUseNewEncryption',
			localUser.forceUseNewEncryption,
			validLocalUserSettingsNames['forceUseNewEncryption']
		)

		if ((map.passwordHistoryRetentionDays == 0) && (map.passwordHistoryRetentionCount == 0)) {
			throw new ConfigurationException("Configuration setting: ${propNamePrefix}.localUser.passwordHistoryRetentionDays or ${propNamePrefix}.localUser.passwordHistoryRetentionCount must be greater than zero.")
		}

		if ((map.passwordHistoryRetentionDays > 0) && (map.passwordHistoryRetentionCount > 0)) {
			throw new ConfigurationException("Configuration setting: ${propNamePrefix}.localUser.passwordHistoryRetentionDays and ${propNamePrefix}.localUser.passwordHistoryRetentionCount are mutually exclusive, at least one must be zero.")
		}

		return map
	}

	private static void throwInvalid(String propertyName, String message) {
		throw new ConfigurationException('Configuration setting ' + propNamePrefix + '.' + propertyName + ' property ' + message)
	}
}
