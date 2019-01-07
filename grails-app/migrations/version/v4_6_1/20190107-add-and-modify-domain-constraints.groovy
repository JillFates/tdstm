package version.v4_6_1

/**
 * Add/modify a number of constraints in the model to guarantee referential integrity
 * and reflect the business model relations.
 * See TM-13626 / Deleting a Provider that has an ETL script used to import data will
 * fail when user attempts to delete the provider.
 *
 */
databaseChangeLog = {
    changeSet(author: "ecantu", id: "20190107 TM-13626-01") {
        comment('Add/modify a number of constraints in the model to guarantee referential integrity and reflect the business model relations')
        // 1 - Change the FKC DataScript > ImportBatch - to set NULL on DELETE
        dropForeignKeyConstraint(baseTableName:'import_batch', constraintName:'import_batch_data_script')
        addForeignKeyConstraint(
                constraintName: 'import_batch_data_script',
                baseTableName: 'import_batch',
                baseColumnNames: 'data_script_id',
                referencedTableName: 'data_script',
                referencedColumnNames: 'data_script_id',
                onDelete: 'SET NULL'
        )
        // 2 - Add FK constraint Credential > Action - to NULL Action.credential on DELETE
        dropForeignKeyConstraint(baseTableName:'api_action', constraintName:'fk_api_action_credential')
        addForeignKeyConstraint(
                constraintName: 'fk_api_action_credential',
                baseTableName: 'api_action',
                baseColumnNames: 'credential_id',
                referencedTableName: 'credential',
                referencedColumnNames: 'credential_id',
                onDelete: 'SET NULL'
        )
        // 3 - Add FK constraint Provider > Action - to DELETE the Action (the old name for the fk constraint and index was wrong, just changing names)
        dropForeignKeyConstraint(baseTableName:'api_action', constraintName:'fk_datascript_provider')
        dropIndex(tableName:'api_action', indexName:'fk_datascript_provider')
        createIndex(indexName: 'fk_api_action_provider', tableName: 'api_action') {
            column(name: 'provider_id')
        }
        addForeignKeyConstraint(
                constraintName: 'fk_api_action_provider',
                baseTableName: 'api_action',
                baseColumnNames: 'provider_id',
                referencedTableName: 'provider',
                referencedColumnNames: 'provider_id',
                onDelete: 'CASCADE'
        )
        // 4 - Add FK constraint Action > Task (AssetComment) - to set AssetComment.action NULL on DELETE
        dropForeignKeyConstraint(baseTableName:'asset_comment', constraintName:'FK_ASSET_COMMENT_TO_API_ACTION')
        addForeignKeyConstraint(
                constraintName: 'FK_ASSET_COMMENT_TO_API_ACTION',
                baseTableName: 'asset_comment',
                baseColumnNames: 'api_action_id',
                referencedTableName: 'api_action',
                referencedColumnNames: 'id',
                onDelete: 'SET NULL'
        )
    }
}