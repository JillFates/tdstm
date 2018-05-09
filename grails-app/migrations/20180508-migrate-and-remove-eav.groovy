/**
 * This changelog is used to modify the TaskDependency and AssetComment tables in relationship to Runbook Optimization enhancements
 */
databaseChangeLog = {

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-1') {
		comment('Add columns last_updated, and date_created to asset_entity table')

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'asset_entity', columnName: 'last_updated')
				columnExists(tableName: 'asset_entity', columnName: 'date_created')
			}
		}

		addColumn(tableName: 'asset_entity') {
			column(name: 'last_updated', type: 'DATETIME') {
				constraints(nullable: 'false')
			}
		}

		addColumn(tableName: 'asset_entity') {
			column(name: 'date_created', type: 'DATETIME') {
				constraints(nullable: 'false')
			}
		}
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-2') {
		comment('Copy last_updated, and date_created  from eav_entry to asset_entity')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: 'eav_entity')
		}

		sql('update asset_entity ae join eav_entity e on ae.asset_entity_id = e.entity_id set ae.last_updated = e.last_updated, ae.date_created = e.date_created;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-3') {
		comment('Drop eav_entry table.')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: 'eav_entity')
		}

		sql('DROP TABLE eav_entity')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-4') {
		comment('Drop eav_attribute_id column from data_transfer_value')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'data_transfer_value', columnName: 'eav_attribute_id')
		}

		sql('ALTER TABLE data_transfer_value DROP COLUMN eav_attribute_id;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-5') {
		comment('Drop table asset_entity_varchar')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: 'asset_entity_varchar')
		}

		sql('DROP TABLE asset_entity_varchar')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-6') {
		comment('Add asset class to data_transfer_batch')

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'data_transfer_batch', columnName: 'asset_class')
			}
		}

		addColumn(tableName: 'data_transfer_batch') {
			column(name: 'asset_class', type: 'varchar(12)')
		}
	}


	changeSet(author: 'tpelletier', id: '20180508 TM-6778-7') {
		comment('Set the asset class on data_transfer_batch from eav_entity_type')

		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'eav_entry')
			}
		}

		sql('''
			UPDATE data_transfer_batch d 
			JOIN  eav_entity_type e ON  d.eav_entity_type_id = e.entity_type_id
			SET d.asset_class = CASE
				WHEN e.entity_type_code = 'Appication' THEN 'APPLICATION'
				WHEN e.entity_type_code = 'Database' THEN 'DATABASE'
				WHEN e.entity_type_code = 'Files' THEN 'STORAGE'
				WHEN e.entity_type_code = 'AssetEntity' THEN 'DEVICE'
			END;
		''')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-8') {
		comment('Drop table eav_entity_type')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_entity_type')
		}

		sql('DROP TABLE eav_entity_type;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-9') {
		comment('Copy asset types from eav_attribute_option to asset_options')

		preConditions(onFail: 'MARK_RAN') {
			sqlCheck(expectedResult: '0', "SELECT count(*) FROM asset_options WHERE type = 'ASSET_TYPE'")
		}
		sql("""
				INSERT INTO asset_options (type, value)
				SELECT 'ASSET_TYPE', value
						FROM eav_attribute_option ao
						JOIN eav_attribute ea ON ao.attribute_id = ea.attribute_id
						WHERE ea.attribute_code = 'assetType';
		""")
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-10') {
		comment('Drop table eav_attribute')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_attribute')
		}
		sql('DROP TABLE eav_attribute;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-11') {
		comment('Drop table eav_attribute_audit_log')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_attribute_audit_log')
		}

		sql('DROP TABLE eav_attribute_audit_log;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-12') {
		comment('Drop table eav_attribute_option')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_attribute_option')
		}

		sql('DROP TABLE eav_attribute_option;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-13') {
		comment('Drop table eav_attribute_set')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_attribute_set')
		}

		sql('DROP TABLE eav_attribute_set;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-14') {
		comment('Drop table eav_entity_attribute')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_entity_attribute')
		}

		sql('DROP TABLE eav_entity_attribute;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-15') {
		comment('Drop table eav_entity_datatype')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_entity_datatype')
		}

		sql('DROP TABLE eav_entity_datatype;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-16') {
		comment('Drop table eav_entity_auditable')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_entity_auditable')
		}

		sql('DROP TABLE eav_entity_auditable;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-17') {
		comment('Drop table eav_entity_varchar')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_entity_varchar')
		}

		sql('DROP TABLE eav_entity_varchar;')
	}

	changeSet(author: 'tpelletier', id: '20180508 TM-6778-18') {
		comment('Drop table eav_entity_varchar_auditable')

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: ' eav_entity_varchar_auditable')
		}

		sql('DROP TABLE eav_entity_varchar_auditable;')
	}
}