package version.v5_0_0

databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20190920 TM-15923-1') {
		comment("Adding foreign key constraints to partyRelationship")

		sql('DELETE FROM party_relationship WHERE party_id_from_id IS NULL OR party_id_to_id IS NULL;')

		addForeignKeyConstraint(
			baseColumnNames: 'party_id_from_id',
			baseTableName: 'party_relationship',
			constraintName: 'FK_party_relationship_party_id_from_id_party_party_id',
			onDelete: 'CASCADE',
			deferrable: 'false',
			initiallyDeferred: 'false',
			referencedColumnNames: 'party_id',
			referencedTableName: 'party',
			referencesUniqueColumn: 'false'
		)

		createIndex(indexName: 'FK_party_relationship_party_id_from_id_party_party_id', tableName: 'party_relationship') {
			column(name: 'party_id_from_id')
		}

		addForeignKeyConstraint(
			baseColumnNames: 'party_id_to_id',
			baseTableName: 'party_relationship',
			constraintName: 'FK_party_relationship_party_id_to_id_party_party_id',
			onDelete: 'CASCADE',
			deferrable: 'false',
			initiallyDeferred: 'false',
			referencedColumnNames: 'party_id',
			referencedTableName: 'party',
			referencesUniqueColumn: 'false'
		)

		createIndex(indexName: 'FK_party_relationship_party_id_to_id_party_party_id', tableName: 'party_relationship') {
			column(name: 'party_id_to_id')
		}

	}
}
