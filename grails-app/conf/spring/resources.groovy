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
   url = "jdbc:mysql://localhost:3306/tdstm"
   username = "root"
   password = "admin"
}

otherJdbcTemplate(JdbcTemplate) {
   dataSource = otherDataSource
}
}