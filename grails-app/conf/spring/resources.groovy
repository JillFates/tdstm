import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.spring.SecurityBeanFactoryPostProcessor
import com.tdsops.common.security.spring.TdsPasswordEncoder
import com.tdsops.common.security.spring.TdsPermissionEvaluator
import com.tdsops.common.security.spring.TdsPostAuthenticationChecks
import com.tdsops.common.security.spring.TdsPreAuthenticationChecks
import com.tdsops.common.security.spring.TdsSaltSource
import com.tdsops.common.security.spring.TdsUserDetailsService
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

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

	preAuthenticationChecks(TdsPreAuthenticationChecks) {
		auditService = ref('auditService')
		securityService = ref('securityService')
		userPreferenceService = ref('userPreferenceService')
	}

	postAuthenticationChecks(TdsPostAuthenticationChecks) {
		securityService = ref('securityService')
	}

	passwordEncoder(TdsPasswordEncoder) {
		securityService = ref('securityService')
	}

	saltSource(TdsSaltSource)

	userDetailsService(TdsUserDetailsService)

	securityBeanFactoryPostProcessor(SecurityBeanFactoryPostProcessor)
}
