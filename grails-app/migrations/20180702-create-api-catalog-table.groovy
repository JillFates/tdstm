databaseChangeLog = {

	changeSet(author: 'slopez', id: '20180702 TM-10608-1') {
		comment("Create tag table")

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

}