databaseChangeLog = {
	changeSet(author: 'slopez', id: '20180613 TM-9933-1') {
		comment('Add httpMethod column for ApiAction')

		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(tableName: 'api_action', columnName: 'http_method' )
			}
		}

		addColumn(tableName: 'api_action') {
			column(name: 'http_method', type: 'VARCHAR(10)', defaultValue: 'GET')
		}

		update(tableName: 'api_action') {
			column(name: 'http_method', value: 'GET')
		}

		addNotNullConstraint(
				tableName: 'api_action',
				columnDataType: 'VARCHAR(10)',
				columnName: 'http_method',
				defaultNullValue: 'GET'
		)
	}

}
