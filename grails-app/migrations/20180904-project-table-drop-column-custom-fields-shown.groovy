/**
 * @author ecantu
 * Removes custom_fields_shown column from Project table.
 * See TM-6616 - Remove old Field Settings implementation
 */

databaseChangeLog = {

    changeSet(author: "ecantu", id: "20180904 TM-6616-1") {
        comment("Drop column custom_fields_shown")
        preConditions(onFail:'MARK_RAN') {
            columnExists(tableName:'project', columnName:'custom_fields_shown' )
        }
        dropColumn(tableName: 'project', columnName: 'custom_fields_shown')
    }

}
