import net.transitionmanager.security.Permission

databaseChangeLog = {


	changeSet(author: 'tpelletier', id: '20180601 TM-10929-1') {
		comment("Create tag table")

		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'tag')
			}
		}

		createTable(tableName: 'tag') {
			column(autoIncrement: 'true', name: 'tag_id', type: 'bigint') {
				constraints(nullable: 'false', primaryKey: 'true', primaryKeyName: 'tagPK')
			}

			column(name: 'version', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'color', type: 'varchar(255)') {
				constraints(nullable: 'false')
			}

			column(name: 'description', type: 'varchar(255)') {
				constraints(nullable: 'false')
			}

			column(name: 'name', type: 'varchar(50)') {
				constraints(nullable: 'false')
			}

			column(name: 'project_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'date_created', type: 'DATETIME') {
				constraints(nullable: 'false')
			}

			column(name: 'last_updated', type: 'DATETIME') {
				constraints(nullable: 'true')
			}
		}

		createIndex(indexName: 'FK_tag_project', tableName: 'tag') {
			column(name: 'project_id')
		}

		addForeignKeyConstraint(
			baseColumnNames: 'project_id',
			baseTableName: 'tag',
			constraintName: 'FK_tag_project',
			onDelete: 'CASCADE',
			deferrable: 'false',
			initiallyDeferred: 'false',
			referencedColumnNames: 'project_id',
			referencedTableName: 'project',
			referencesUniqueColumn: 'false'
		)

		createIndex(indexName: 'UK_tag_name_project', tableName: 'tag', unique: true) {
			column(name: 'name')
			column(name: 'project_id')
		}


		createIndex(indexName: 'IX_tag_description_project', tableName: 'tag', unique: false) {
			column(name: 'description')
			column(name: 'project_id')
		}
	}

	changeSet(author: 'tpelletier', id: '20180601 TM-10929-2') {
		comment("Create tag_asset table")

		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'tag_asset')
			}
		}

		createTable(tableName: 'tag_asset') {
			column(autoIncrement: 'true', name: 'tag_asset_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false', primaryKey: 'true', primaryKeyName: 'tag_assetPK')
			}

			column(name: 'asset_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'tag_id', type: 'BIGINT(20)') {
				constraints(nullable: 'false')
			}

			column(name: 'date_created', type: 'DATETIME') {
				constraints(nullable: 'false')
			}
		}

		createIndex(indexName: 'FK_tag_asset_tag', tableName: 'tag_asset') {
			column(name: 'tag_id')
		}

		addForeignKeyConstraint(
			baseColumnNames: 'tag_id',
			baseTableName: 'tag_asset',
			constraintName: 'FK_tag_asset_tag',
			onDelete: 'CASCADE',
			deferrable: 'false',
			initiallyDeferred: 'false',
			referencedColumnNames: 'tag_id',
			referencedTableName: 'tag',
			referencesUniqueColumn: 'false'
		)

		createIndex(indexName: 'FK_tag_asset_asset', tableName: 'tag_asset') {
			column(name: 'asset_id')
		}

		addForeignKeyConstraint(
			baseColumnNames: 'asset_id',
			baseTableName: 'tag_asset',
			constraintName: 'FK_tag_asset_asset',
			onDelete: 'CASCADE',
			deferrable: 'false',
			initiallyDeferred: 'false',
			referencedColumnNames: 'asset_entity_id',
			referencedTableName: 'asset_entity',
			referencesUniqueColumn: 'false'
		)

		createIndex(indexName: 'UK_tag_asset_asset_tag', tableName: 'tag_asset', unique: true) {
			column(name: 'asset_id')
			column(name: 'tag_id')
		}
	}


	changeSet(author: "tpelletier", id: "20180605 TM-10929-3") {
		comment('add new permission for managing tags')

		grailsChange {
			change {
				def perms = [
					(Permission.TagCreate): [
						group      : 'NONE',
						description: 'Can create tags',
						roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],

					(Permission.TagDelete): [
						group      : 'NONE',
						description: 'Can delete tags',
						roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR']
					],

					(Permission.TagEdit)  : [
						group      : 'NONE',
						description: 'Can update tags',
						roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR']
					],

					(Permission.TagView)  : [
						group      : 'NONE',
						description: 'Can view tags',
						roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR', 'SUPERVISOR', 'EDITOR', 'USER']
					],

					(Permission.TagMerge) : [
						group      : 'NONE',
						description: 'Can merge tags',
						roles      : ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
					]
				]

				def databaseMigrationService = ctx.getBean('databaseMigrationService')
				databaseMigrationService.addPermissions(sql, perms)
			}
		}
	}
}
