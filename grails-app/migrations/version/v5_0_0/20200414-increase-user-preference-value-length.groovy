package version.v5_0_0

databaseChangeLog = {


    changeSet(author: 'arecordon', id: 'TM-17272') {
        comment('Extend the length of the User Preference value to 4096')

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'user_preference', columnName: 'value')
        }

        modifyDataType(tableName: 'user_preference', columnName: 'value', newDataType: 'varchar(4096)')
    }
}