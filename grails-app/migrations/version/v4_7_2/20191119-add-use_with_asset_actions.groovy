package version.v4_7_2
/**
 * Add new not nullable column useWithAssetActions on DataScript Domain class (data_script table).
 */
databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20191119 TM-16420-1') {
		comment('Add column use_with_asset_actions to the data_script table')

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'data_script', columnName: 'use_with_asset_actions')
			}
		}

		addColumn(tableName: 'data_script') {
			column(name: 'use_with_asset_actions', type: 'TINYINT(1)', defaultValue: '0') {
				constraints(nullable: 'false')
			}
		}
	}
}
