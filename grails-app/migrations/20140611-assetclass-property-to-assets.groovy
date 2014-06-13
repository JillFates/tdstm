databaseChangeLog = {
	changeSet(author: "jmartin", id: "20140611 TM-2830-1") {
		comment('Add new AssetEntity.assetClass column')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'asset_class')
			}
		}
		addColumn(tableName: 'asset_entity') {
			column(name: 'asset_class', type: 'varchar(12)')
		}
	}

	changeSet(author: "jmartin", id: "20140611 TM-2830-2") {
		comment('Update new AssetEntity.assetClass column based on the subclasses appropriately and add constraint & index')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				sqlCheck(expectedResult:'0', 'SELECT COUNT(*) FROM asset_entity WHERE asset_class IS NULL')
			}
		}
		sql("UPDATE asset_entity SET asset_class='DATABASE' WHERE asset_entity_id IN (SELECT db_id FROM data_base)")
		sql("UPDATE asset_entity SET asset_class='APPLICATION' WHERE asset_entity_id IN (SELECT app_id FROM application)")
		sql("UPDATE asset_entity SET asset_class='STORAGE' WHERE asset_entity_id IN (SELECT files_id FROM files)")
		sql("UPDATE asset_entity SET asset_class='DEVICE' WHERE asset_class IS NULL")

		addNotNullConstraint(tableName: 'asset_entity', columnName: 'asset_class', columnDataType: 'varchar(12)', defaultNullValue:'device')

		createIndex(tableName:'asset_entity', indexName:'AssetEntity_AssetClass_idx', unique:'false') {
			column(name:'asset_class')
		}

	}
}
