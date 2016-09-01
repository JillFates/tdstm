/*
dataSource {
	pooled = true
	driverClassName = "org.hsqldb.jdbcDriver"
	username = "sa"
	password = ""
}
*/

hibernate {
    cache.use_second_level_cache=true
    cache.use_query_cache=true
    cache.provider_class='net.sf.ehcache.hibernate.EhCacheProvider'
}

// environment specific settings

environments {
	development {
		dataSource {
			// TDS Transitional Manager
			pooled = true
			// Alternate options: 'create', 'create-drop','update'
			dbCreate = ''

			// url = "jdbc:mysql://localhost/tdstm?autoReconnect=true"
			driverClassName = "com.mysql.jdbc.Driver"
			username = "tdstmapp"
			password = "tdstmpswd"
			dbCreate = ''
			logSql = false

			properties {
				// See following page for explaination of settings
				// https://commons.apache.org/proper/commons-pool/api-1.6/org/apache/commons/pool/impl/GenericObjectPool.html
				maxActive=70
				maxIdle=15
				initialSize=15

				// Evictions set to 5 minutes of idle time
				minEvictableIdleTimeMillis=(1000*60*5)
				// Run evictions on idle connections every 60 seconds (default 5 seconds) 
				timeBetweenEvictionRunsMillis=(1000*60)

				testOnBorrow=true
				testWhileIdle=false
				testOnReturn=false
				removeAbandoned=true
				removeAbandonedTimeout=600
				validationQuery='/* ping */'
			}
		}
	}
	test {
		dataSource {
			dbCreate = "create"
			url = "jdbc:mysql://localhost/relo?autoReconnect=true"
			driverClassName = "com.mysql.jdbc.Driver"
			username = "tdstm"
			password = "tdstm"
			dbCreate = ''
			logSql = false
		}
	}
	production {
		dataSource {
			// TDS Transitional Manager
			driverClassName = "com.mysql.jdbc.Driver"

			// Alternate options: 'create', 'create-drop','update'
			dbCreate = '' 

			// url = "jdbc:mysql://127.0.0.1/tdstm"
			// username = ''
			// password = ''
			// loggingSql = true
			// logSql = true 

			properties {
				// See following page for explaination of settings
				// https://commons.apache.org/proper/commons-pool/api-1.6/org/apache/commons/pool/impl/GenericObjectPool.html
				maxActive=70
				maxIdle=15
				initialSize=15

				// Evictions set to 5 minutes of idle time
				minEvictableIdleTimeMillis=(1000*60*5)
				// Run evictions on idle connections every 60 seconds (default 5 seconds) 
				timeBetweenEvictionRunsMillis=(1000*60)

				testOnBorrow=true
				testWhileIdle=false
				testOnReturn=false
				removeAbandoned=true
				removeAbandonedTimeout=600
				validationQuery='/* ping */'
			}
		}
	}
}
