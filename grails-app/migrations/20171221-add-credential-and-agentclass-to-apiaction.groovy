databaseChangeLog = {
    changeSet(author: "arecordon", id: "20171221 TM-8603-1") {
        comment('Add Credential column for ApiAction')
        preConditions(onFail:'MARK_RAN') {
            not {
                columnExists(tableName:'api_action', columnName:'credential_id' )
            }
        }
        addColumn(tableName: "api_action") {
            column(name: "credential_id", type: "BIGINT(20)") {
                constraints(nullable: "true")
            }
        }

        addForeignKeyConstraint(
                constraintName: 'fk_api_action_credential',
                baseTableName: 'api_action',
                baseColumnNames: 'credential_id',
                referencedTableName: 'credential',
                referencedColumnNames: 'credential_id'
        )

    }
}
