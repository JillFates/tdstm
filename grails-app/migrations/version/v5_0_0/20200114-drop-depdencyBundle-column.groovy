package version.v5_0_0

databaseChangeLog = {


	changeSet(author: 'tpelletier', id: 'TM-16772') {
		comment('drop dependency_bundle')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'asset_entity', columnName: 'dependency_bundle')
		}

		dropColumn(tableName: 'asset_entity', columnName: 'dependency_bundle')
	}



}
