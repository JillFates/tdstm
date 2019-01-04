databaseChangeLog = {

	changeSet(author: 'slopez', id: '20180702 TM-10608-1') {
		comment("Create api_catalog table")

		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'api_catalog')
			}
		}

		createTable(tableName: 'api_catalog') {
			column(autoIncrement: 'true', name: 'api_catalog_id', type: 'bigint') {
				constraints(nullable: 'false', primaryKey: 'true', primaryKeyName: 'apiCatalogPK')
			}

			column(name: 'name', type: 'varchar(255)') {
				constraints(nullable: 'false')
			}

			column(name: 'dictionary', type: 'JSON') {
				constraints(nullable: 'false')
			}

			column(name: 'dictionary_transformed', type: 'JSON') {
				constraints(nullable: 'false')
			}

			column(name: 'project_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'provider_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'version', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'date_created', type: 'DATETIME') {
				constraints(nullable: 'false')
			}

			column(name: 'last_updated', type: 'DATETIME') {
				constraints(nullable: 'true')
			}

		}

		createIndex(indexName: 'FK_api_cat_project', tableName: 'api_catalog') {
			column(name: 'project_id')
		}

		addForeignKeyConstraint(
				constraintName: 'fk_api_catalog_project',
				onDelete: 'CASCADE',
				baseTableName: 'api_catalog',
				baseColumnNames: 'project_id',
				referencedTableName: 'project',
				referencedColumnNames: 'project_id'
		)

		addForeignKeyConstraint(
				constraintName: 'fk_api_catalog_provider',
				onDelete: 'CASCADE',
				baseTableName: 'api_catalog',
				baseColumnNames: 'provider_id',
				referencedTableName: 'provider',
				referencedColumnNames: 'provider_id'
		)

		createIndex(indexName: 'UK_api_cat_name_project_provider', tableName: 'api_catalog', unique: true) {
			column(name: 'name')
			column(name: 'project_id')
			column(name: 'provider_id')
		}

	}

	changeSet(author: 'slopez', id: '20180713 TM-10608-2') {
		comment("Alter api_action table")

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: 'api_action')
		}

		sql('UPDATE asset_comment SET api_action_id = NULL WHERE api_action_id IS NOT NULL;')
		sql('DELETE FROM api_action;')

		dropColumn(tableName: 'api_action', columnName: 'agent_class')

		addColumn(tableName: 'api_action') {
			column(name: 'api_catalog_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}
		}

		addForeignKeyConstraint(
				constraintName: 'fk_api_action_catalog',
				onDelete: 'CASCADE',
				baseTableName: 'api_action',
				baseColumnNames: 'api_catalog_id',
				referencedTableName: 'api_catalog',
				referencedColumnNames: 'api_catalog_id'
		)

	}

}
