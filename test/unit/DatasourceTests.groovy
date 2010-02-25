import org.springframework.jdbc.datasource.DriverManagerDataSource

class DatasourceTests extends GroovyTestCase {

	/*
	 * Establish the database connection
	 */
	def DriverManagerDataSource getDatasource(){
		def dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
		dataSource.setUrl("jdbc:mysql://localhost/tdstm_dev");
		dataSource.setUsername("root");
		dataSource.setPassword("admin");
		return dataSource
	}
	
}
