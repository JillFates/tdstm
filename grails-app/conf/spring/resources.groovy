import org.springframework.jdbc.core.JdbcTemplate
import org.apache.commons.dbcp.BasicDataSource
// Place your Spring DSL code here

beans = {
// uses the grails dataSource from DataSource.groovy
jdbcTemplate(JdbcTemplate) {
   dataSource = ref('dataSource')
}

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
