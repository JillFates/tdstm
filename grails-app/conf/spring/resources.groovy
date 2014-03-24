import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.apache.commons.dbcp.BasicDataSource
// import org.apache.shiro.authc.credential.Sha512CredentialsMatcher
import org.apache.shiro.authc.credential.Sha1CredentialsMatcher

import com.tdsops.common.grails.ApplicationContextHolder
 
beans = {
	// uses the grails dataSource from DataSource.groovy
	jdbcTemplate(JdbcTemplate) {
	   dataSource = ref('dataSource')
	}
	
	namedParameterJdbcTemplate(NamedParameterJdbcTemplate, ref("dataSource")){
	}

	// A custom context holder to allow us to gain access to the application context and other components of the runtime environment
	applicationContextHolder(ApplicationContextHolder) { bean ->
		bean.factoryMethod = 'getInstance'
	}
	
	/*
	// use a different datasource
	otherDataSource(BasicDataSource) {
	   driverClassName = "com.mysql.jdbc.Driver"
	//   url = "jdbc:mysql://localhost:3306/tdstm"
	   url = "jdbc:mysql://tdstm-dbserver:3306/tdstm"
	   username = "root"
	   password = "admin"
	}
	
	otherJdbcTemplate(JdbcTemplate) {
	   dataSource = otherDataSource
	}
	*/
	
	// Shiro Password Encryption configuration
	credentialMatcher(Sha1CredentialsMatcher) {
		storedCredentialsHexEncoded = true
	}
	/*
	 * See http://suryazi.blogspot.com/2012/07/salting-password.html for more on this one
	 * 
	credentialMatcher(Sha512CredentialsMatcher) {
		storedCredentialsHexEncoded = false
		hashSalted=true
		hashIterations=1024
	}
	*/
	
	/*
	 * Database Connection String that overrides that in conf/DataSource.groovy
	 */
	/**
	dataSource(BasicDataSource) {
		driverClassName='com.mysql.jdbc.Driver'
		url = 'jdbc:mysql://localhost:3306/tdstm'
		username = 'tdstm'
		password = 'tdstm'
	
		maxActive = 50
		maxIdle = 25
		minIdle = 5
		initialSize = 5
	
		maxWait = 10000
		minEvictableIdleTimeMillis=1800000
		timeBetweenEvictionRunsMillis=1800000
		numTestsPerEvictionRun=3
		testOnBorrow=true
		testWhileIdle=true
		testOnReturn=true
		validationQuery="SELECT 1"
	}
	**/

}
