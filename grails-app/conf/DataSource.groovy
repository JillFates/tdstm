dataSource {
	dbCreate = 'none'
	dialect = 'com.tdsops.common.sql.CustomMySQLDialect'
	driverClassName = 'com.mysql.jdbc.Driver'
	jmxExport = true
	pooled = true
}

hibernate {
	cache {
		use_second_level_cache = true
		use_query_cache = false
		region.factory_class = 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory' // Hibernate 4
	}
	flush.mode = 'manual' // OSIV session flush mode outside of transactional context
	// format_sql = true
	singleSession = true
	// use_sql_comments = true
}

environments {
	development {
		dataSource {
			// url = "jdbc:mysql://localhost/tdstm?autoReconnect=true"
			username = "tdstmapp"
			password = "tdstmpswd"
			logSql = false

			// See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
			properties {
				defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
				initialSize=15
				jdbcInterceptors = 'ConnectionState'
				jmxEnabled = true
				maxActive=70
				maxAge = 10 * 60000
				maxIdle=15
				maxWait = 10000
				minEvictableIdleTimeMillis = 1000 * 60 * 5 // Evictions set to 5 minutes of idle time
				minIdle = 5
				removeAbandoned = true
				removeAbandonedTimeout = 600
				testOnBorrow = true
				testOnReturn = false
				testWhileIdle = false
				timeBetweenEvictionRunsMillis = 1000 * 60 // Run evictions on idle connections every 60 seconds (default 5 seconds)
				validationInterval = 15000
				validationQuery='/* ping */'
				validationQueryTimeout = 3
			}
		}
	}
	test {
		dataSource {
			url = "jdbc:mysql://localhost/tdstm?autoReconnect=true"
			username = "tdstmapp"
			password = "tdstmpswd"
			logSql = false
		}
	}
	production {
		dataSource {
			// url = "jdbc:mysql://127.0.0.1/tdstm"
			// username = ''
			// password = ''

			// See http://grails.org/doc/latest/guide/conf.html#dataSource for documentation
			properties {
				defaultTransactionIsolation = java.sql.Connection.TRANSACTION_READ_COMMITTED
				initialSize=15
				jdbcInterceptors = 'ConnectionState'
				jmxEnabled = true
				maxActive=70
				maxAge = 10 * 60000
				maxIdle=15
				maxWait = 10000
				minEvictableIdleTimeMillis=(1000*60*5) // Evictions set to 5 minutes of idle time
				minIdle = 5
				removeAbandoned=true
				removeAbandonedTimeout=600
				testOnBorrow=true
				testOnReturn=false
				testWhileIdle=false
				timeBetweenEvictionRunsMillis=(1000*60) // Run evictions on idle connections every 60 seconds (default 5 seconds)
				validationInterval = 15000
				validationQuery='/* ping */'
				validationQueryTimeout = 3
			}
		}
	}
}
