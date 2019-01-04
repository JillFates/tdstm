import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.spring.SecurityBeanFactoryPostProcessor
import com.tdsops.common.security.spring.TdsHttpSessionRequestCache
import com.tdsops.common.security.spring.TdsPasswordEncoder
import com.tdsops.common.security.spring.TdsPermissionEvaluator
import com.tdsops.common.security.spring.TdsPostAuthenticationChecks
import com.tdsops.common.security.spring.TdsPreAuthenticationChecks
import com.tdsops.common.security.spring.TdsSaltSource
import com.tdsops.common.security.spring.TdsUserDetailsService
import com.tdsops.ldap.TdsBindAuthenticator
import com.tdsops.ldap.TdsLdapAuthenticationProvider
import com.tdsops.ldap.TdsLdapUserDetailsMapper
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.ldap.core.GrailsSimpleDirContextAuthenticationStrategy
import grails.plugin.springsecurity.ldap.core.SimpleAuthenticationSource
import net.transitionmanager.integration.ApiActionScriptBindingBuilder
import net.transitionmanager.task.TaskFacade
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper
import org.springframework.security.ldap.DefaultSpringSecurityContextSource
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator
import org.springframework.security.web.access.ExceptionTranslationFilter

beans = {
	// uses the grails dataSource from DataSource.groovy
	jdbcTemplate(JdbcTemplate) {
		dataSource = ref('dataSource')
	}

	namedParameterJdbcTemplate(NamedParameterJdbcTemplate, ref('dataSource'))

	// A custom context holder to allow us to gain access to the application context and other components of the runtime environment
	applicationContextHolder(ApplicationContextHolder) { bean ->
		bean.factoryMethod = 'getInstance'
	}

	permissionEvaluator(TdsPermissionEvaluator)

	userDetailsService(TdsUserDetailsService)
	tdsSaltSource(TdsSaltSource)

	preAuthenticationChecks(TdsPreAuthenticationChecks) {
		auditService = ref('auditService')
		grailsApplication = ref('grailsApplication')
		userPreferenceService = ref('userPreferenceService')
	}

	postAuthenticationChecks(TdsPostAuthenticationChecks) {
		grailsApplication = ref('grailsApplication')
		passwordService = ref('passwordService')
	}

	passwordEncoder(TdsPasswordEncoder) {
		grailsApplication = ref('grailsApplication')
		passwordService = ref('passwordService')
	}

	// See: SpringSecurityCoreGrailsPlugin for reference
	requestCache(TdsHttpSessionRequestCache) {
		portResolver = ref('portResolver')
		createSessionAllowed = true
		requestMatcher = ref('requestMatcher')
		coreService = ref('coreService')
	}

	exceptionTranslationFilter(ExceptionTranslationFilter, ref('authenticationEntryPoint'), ref('requestCache')) {
		accessDeniedHandler = ref('accessDeniedHandler')
		authenticationTrustResolver = ref('authenticationTrustResolver')
		throwableAnalyzer = ref('throwableAnalyzer')
	}

	securityBeanFactoryPostProcessor(SecurityBeanFactoryPostProcessor)

	SpringSecurityUtils.loadSecondaryConfig 'DefaultLdapSecurityConfig'

	def config = application.config.tdstm.security.ldap.domains
	def globalConfig = SpringSecurityUtils.securityConfig

	if (config instanceof Map && application.config.tdstm.security.ldap.enabled) {

		ldapUserDetailsMapper(TdsLdapUserDetailsMapper)

		ldapAuthoritiesMapper(NullAuthoritiesMapper)

		config.each { key, value ->
			String ldapDomain = key.toString()
			if (value instanceof Map) {
				Map conf = (Map)value

				println "\nConfiguring Spring Security LDAP for ${ldapDomain} ..."

				if (application.config.tdstm.security.ldap.debug == true) {
					def debugConfig = new ConfigObject(conf)
					debugConfig.servicePassword = "REDACTED"
					println "\nConfiguration for LDAP domain ${ldapDomain}:"
					println debugConfig.toString()
				}

				SpringSecurityUtils.registerProvider "ldapAuthProvider${ldapDomain}"

				Class<?> contextFactoryClass = com.sun.jndi.ldap.LdapCtxFactory
				Class<?> dirObjectFactoryClass = org.springframework.ldap.core.support.DefaultDirObjectFactory

				String managerDn = conf.serviceName
				String managerPassword = conf.servicePassword
				String server = conf.url[0]
				String searchBase = conf.userSearchBase[0]
				String searchFilter = conf.userSearchOn == "SAM" ? 'sAMAccountName={0}' : '(uid={0})'


				"contextSource$ldapDomain"(DefaultSpringSecurityContextSource, server) { // 'ldap://localhost:389'
					authenticationSource = new SimpleAuthenticationSource(principal: managerDn, credentials: managerPassword)
					authenticationStrategy = new GrailsSimpleDirContextAuthenticationStrategy(userDn: managerDn)
					userDn = managerDn // 'cn=admin,dc=example,dc=com'
					password = managerPassword // 'secret'
					contextFactory = contextFactoryClass
					dirObjectFactory = dirObjectFactoryClass
					baseEnvironmentProperties = globalConfig.ldap.context.baseEnvironmentProperties // none
					cacheEnvironmentProperties = globalConfig.ldap.context.cacheEnvironmentProperties // true
					anonymousReadOnly = globalConfig.ldap.context.anonymousReadOnly // false
					referral = globalConfig.ldap.context.referral // null
				}


				"ldapUserSearch$ldapDomain"(FilterBasedLdapUserSearch, searchBase, searchFilter, ref("contextSource$ldapDomain")) {
					searchSubtree = globalConfig.ldap.search.searchSubtree // true
					derefLinkFlag = globalConfig.ldap.search.derefLink // false
					searchTimeLimit = globalConfig.ldap.search.timeLimit // 0 (unlimited)
					returningAttributes = null
				}

				"ldapAuthenticator$ldapDomain"(TdsBindAuthenticator, ref("contextSource$ldapDomain")) {
					userSearch = ref("ldapUserSearch$ldapDomain")
					sourceDomain = ldapDomain
				}

				String groupSearchBase = conf.roleBaseDN
				String defaultRole = conf.defaultRole
				String _groupSearchFilter
				if (conf.roleSearchMode == 'direct') {
					_groupSearchFilter = 'member={0}'
				} else if (conf.roleSearchMode == 'nested') {
					_groupSearchFilter = 'member:1.2.840.113556.1.4.1941:={0}'
				}


				"ldapAuthoritiesPopulator$ldapDomain"(DefaultLdapAuthoritiesPopulator, ref("contextSource$ldapDomain"), groupSearchBase) {
					groupRoleAttribute = globalConfig.ldap.authorities.groupRoleAttribute // 'cn'
					groupSearchFilter = _groupSearchFilter
					searchSubtree = globalConfig.ldap.authorities.searchSubtree // true
					if (defaultRole) {
						defaultRole = defaultRole
					}
					convertToUpperCase = true
					rolePrefix = '' // 'ROLE_'
					ignorePartialResultException = true
				}

				"ldapAuthProvider$ldapDomain"(TdsLdapAuthenticationProvider, ref("ldapAuthenticator$ldapDomain"), ref("ldapAuthoritiesPopulator$ldapDomain")) {
					userDetailsContextMapper = ref('ldapUserDetailsMapper')
					hideUserNotFoundExceptions = globalConfig.ldap.auth.hideUserNotFoundExceptions // true
					useAuthenticationRequestCredentials = globalConfig.ldap.auth.useAuthPassword // true
					authoritiesMapper = ref('ldapAuthoritiesMapper')
					ldapDebug = application.config.tdstm.security.ldap.debug
				}

				println "... finished configuring Spring Security LDAP for ${ldapDomain}\n"

			}
		}
	} else {
		if (log.isDebugEnabled()) {
			log.debug("TDSTM LDAP configuration skipped due to either no domains configured or ldap is disabled")
		}
	}

	taskFacade(TaskFacade) { bean ->
		bean.scope = 'prototype'
		taskService = ref('taskService')
		messageSourceService = ref('messageSourceService')
	}

	apiActionScriptBindingBuilder(ApiActionScriptBindingBuilder) { bean ->
		bean.scope = 'prototype'
		messageSourceService = ref('messageSourceService')
	}

}
