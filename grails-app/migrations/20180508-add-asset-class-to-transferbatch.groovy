databaseChangeLog = {
	changeSet(author: 'tpelletier', id: '20180508 TM-6778-A1') {
		comment('Add asset class to data_transfer_batch')

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'data_transfer_batch', columnName: 'asset_class')
			}
		}

		addColumn(tableName: 'data_transfer_batch') {
			column(name: 'asset_class', type: 'varchar(12)')
		}
	}
}
