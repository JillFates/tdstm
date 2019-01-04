databaseChangeLog = {

    changeSet(author: "jmartin", id: "20171020 TM-7807_01") {
        comment("Add column defaultDataScript to ApiAction")

        addColumn(tableName: 'api_action') {
            column(name: 'default_data_script_id', type: 'BIGINT(20)')
        }

        addForeignKeyConstraint(
            constraintName: 'fk_apiaction_default_datascript',
            baseTableName: 'api_action',
            baseColumnNames: 'default_data_script_id',
            referencedTableName: 'data_script',
            referencedColumnNames: 'data_script_id',
            onDelete: 'SET NULL'
        )
    }

    changeSet(author: "jmartin", id: "20171020 TM-7807_02") {
        comment("Add column provider to ApiAction table")
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'api_action', columnName: 'provider_id')
            }
        }

        addColumn(tableName: 'api_action') {
            column(name: 'provider_id', type: 'BIGINT(20)', defaultValueNumeric:0) {
                constraints(nullable: 'false')
            }
        }

        addForeignKeyConstraint(
                constraintName: 'fk_datascript_provider',
                baseTableName: 'api_action',
                baseColumnNames: 'provider_id',
                referencedTableName: 'provider',
                referencedColumnNames: 'provider_id',
                onDelete: 'CASCADE'
        )
    }

    changeSet(author: "jmartin", id: "20171020 TM-7807_03") {
        comment("Add column producesData to ApiAction table")

        addColumn(tableName: 'api_action') {
            column(name: 'produces_data', type: 'TINYINT(1)', defaultValueNumeric: 0 ) {
                constraints(nullable: 'false')
            }
        }
    }

}
