package version.v5_0_0

databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20191112 TM-16348-1') {
		comment("Adding foreign key constraints to asset comment")

		sql('''UPDATE asset_comment c 
		LEFT OUTER JOIN asset_entity a ON a.asset_entity_id = c.asset_entity_id
		SET c.asset_entity_id = null
		WHERE c.asset_entity_id IS NOT null AND a.asset_entity_id IS null;
		''')


		addForeignKeyConstraint(
			baseColumnNames: 'asset_entity_id',
			baseTableName: 'asset_comment',
			constraintName: 'FK_asset_comment_asset_entity_id_to_asset_entity_id',
			onDelete: 'SET NULL',
			deferrable: 'false',
			initiallyDeferred: 'false',
			referencedColumnNames: 'asset_entity_id',
			referencedTableName: 'asset_entity',
			referencesUniqueColumn: 'false'
		)


		createIndex(indexName: 'FK_asset_comment_asset_entity_id_to_asset_entity_id', tableName: 'asset_comment') {
			column(name: 'asset_entity_id')
		}

	}
}
