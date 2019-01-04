/**
 * @author ecantu
 * Drop is_resolved column from the asset_comment table.
 * See TM-11379 - Replace Comment isResolve property with method based on resolvedDate
 */
databaseChangeLog = {
    changeSet(author: "ecantu", id: "20180719 TM-11379-1") {
        comment('Drop is_resolved column from the asset_comment table')
        preConditions(onFail:'MARK_RAN') {
            columnExists(tableName:'asset_comment', columnName:'is_resolved' )
        }
        dropColumn(tableName:'asset_comment', columnName:'is_resolved')
    }
}
