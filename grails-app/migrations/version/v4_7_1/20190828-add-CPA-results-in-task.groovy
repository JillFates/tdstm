package version.v4_7_1
/**
 * Add new column on Task domain class (asset_comment table).
 * After critical path analysis (CPA), some variables are calculated.
 * We can save that information in Task domain class.
 *
 * @See TM-13147
 * @author dcorrea
 */
databaseChangeLog = {

	changeSet(author: 'dcorrea', id: '20190828 TM-13147') {
		comment('Add column is_critical_path to the asset_comment table')

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'asset_comment', columnName: 'is_critical_path')
			}
		}

		addColumn(tableName: 'asset_comment') {
			column(name: 'is_critical_path', type: 'TINYINT(1)') {
				constraints(nullable: 'true')
			}
		}
	}
}
