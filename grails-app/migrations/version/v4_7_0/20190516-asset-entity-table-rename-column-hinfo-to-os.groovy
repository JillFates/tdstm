/**
 * @author ecantu
 * Renames hinfo column to os from asset_entity table.
 * See TM-14962 - Change AssetEntity column hinfo to os
 */
databaseChangeLog = {

    changeSet(author: "ecantu", id: "TM-14962-1") {
        comment("Rename hinfo column to os")

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'asset_entity', columnName: 'hinfo')
        }

        renameColumn(tableName: 'asset_entity', oldColumnName: 'hinfo', newColumnName: 'os', columnDataType: 'varchar(255)')
    }
}
