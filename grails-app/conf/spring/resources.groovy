import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.shiro.FirstExceptionStrategy
import com.tdsops.common.security.shiro.SHA2CredentialsMatcher
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

	credentialMatcher(SHA2CredentialsMatcher)

	shiroAuthenticationStrategy(FirstExceptionStrategy)
}
