dataSource {
	pooled = true
	driverClassName = "org.hsqldb.jdbcDriver"
	username = "sa"
	password = ""
}
hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='com.opensymphony.oscache.hibernate.OSCacheProvider'
}
// environment specific settings
environments {
	
	development {
		dataSource {
			/*
			// TDS Transitional Manager
			dbCreate = "create-drop" // one of 'create', 'create-drop','update'
			url = "jdbc:mysql://localhost/tdstm"
			driverClassName = "com.mysql.jdbc.Driver"
			username = "tdstm"
			password = "tdstm"
			
			logSql = true 
			*/
			
			dbCreate = "create-drop" // one of 'create', 'create-drop','update'
			url = "jdbc:hsqldb:mem:devDB"
		}
	}
	test {
		dataSource {
			dbCreate = "update"
			url = "jdbc:hsqldb:mem:testDb"
		}
	}
	production {
		dataSource {
		// TDS Transitional Manager
			dbCreate = "create-drop" // one of 'create', 'create-drop','update'
			url = "jdbc:mysql://localhost/tdstm"
			driverClassName = "com.mysql.jdbc.Driver"
			username = "tdstm"
			password = "tdstm"
			
			// logSql = true 
		}
	}
}