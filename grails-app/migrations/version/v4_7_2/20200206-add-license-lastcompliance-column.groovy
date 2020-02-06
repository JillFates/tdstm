package version.v4_7_2

databaseChangeLog = {
    changeSet(author: 'oluna', id: '20200206 TM-16920-1') {
        comment("Add lastCompliance columns to License table")

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'license', columnName: 'last_compliance')
            }
        }

        addColumn(tableName: "license") {
            column(name: "last_compliance", type: "varchar(255)")
        }
    }
}
