
databaseChangeLog = {
	changeSet(author: 'arecordon', id: 'TM-8966-1') {
		comment("Create import_batch table")
		createTable(tableName: 'import_batch') {
			column(name: "import_batch_id", autoIncrement: "true", type: "BIGINT(20)") {
				constraints(primaryKey: true)
			}

			column(name: "project_id", type: "BIGINT(20)") {
				constraints(
						nullable: false,
						foreignKeyName: "import_batch_project",
						references: "project(project_id)",
						deleteCascade: true
				)
			}

			column(name: "status", type: "VARCHAR(32)") {
				constraints(nullable: false)
			}

			column(name: "domain_class_name", type: "VARCHAR(255)") {
				constraints(nullable: false)
			}

			column(name: "provider_id", type: "BIGINT(20)") {
				constraints(
						foreignKeyName: "import_batch_provider",
						references: "provider(provider_id)",
						deleteCascade: false
				)
			}

			column(name: "data_script_id", type: "BIGINT(20)") {
				constraints(
						foreignKeyName: "import_batch_data_script",
						references: "data_script(data_script_id)",
						deleteCascade: false
				)

			}

			column(name: "created_by", type: "BIGINT(20)") {
				constraints(
						nullable: true,
						foreignKeyName: "import_batch_created_by",
						references: "person(person_id)",
						deleteCascade: true
				)
			}

			column(name: "archived", type: "TINYINT(1)", defaultValueNumeric: 0) {
				constraints(nullable: false)
			}

			column(name: "timezone", type: "VARCHAR(255)") {
				constraints(nullable: false)
			}

			column(name: "date_format", type: "VARCHAR(32)") {
				constraints(nullable: false)
			}

			column(name: "progress_info_job", type: "VARCHAR(255)")

			column(name: "original_filename", type: "VARCHAR(255)") {
				constraints(nullable: false)
			}

			column(name: "null_indicator", type: "VARCHAR(255)") {
				constraints(nullable: false)
			}

			column(name: "overwrite_with_blanks", type:"TINYINT(1)",  defaultValueNumeric: 0)

			column(name: "auto_process", type:"TINYINT(1)",  defaultValueNumeric: 0) {
				constraints(nullable: false)
			}

			column(name: "warn_on_changes_after", type: "DATETIME")

			column(name: "field_name_list", type: "JSON") {
				constraints(nullable: false)
			}

			column(name: "date_created", type: "DATETIME", defaultValueComputed="CURRENT_TIMESTAMP"){
				constraints(nullable: false)
			}

			column(name: "last_updated", type: "DATETIME")

			column(name: "version", type: "INT(11)")

		}
	}

	changeSet(author: 'arecordon', id: 'TM-8966-2') {
		comment("Create import_batch_record table")
		createTable(tableName: 'import_batch_record') {
			column(name: "batch_import_record_id", autoIncrement: "true", type: "BIGINT(20)") {
				constraints(primaryKey: true)
			}

			column(name: "import_batch_id", type: "BIGINT(20)") {
				constraints(
						nullable: false,
						foreignKeyName: "import_batch_record_import_batch",
						references: "import_batch(import_batch_id)",
						deleteCascade: true
				)
			}

			column(name: "status", type: "VARCHAR(32)") {
				constraints(nullable: false)
			}

			column(name: "operation", type: "VARCHAR(32)") {
				constraints(nullable: false)
			}

			column(name: "domain_primary_id", type: "BIGINT(20)")

			column(name: "source_row_id", type: "BIGINT(8)", defaultValueNumeric: 0) {
				constraints(nullable: false)
			}

			column(name: "error_count", type: "BIGINT(8)", defaultValueNumeric: 0) {
				constraints(nullable: false)
			}

			column(name: "ignored", type: "TINYINT(1)", defaultValueNumeric: 0) {
				constraints(nullable: false)
			}

			column(name: "warn", type: "TINYINT(1)", defaultValueNumeric: 0) {
				constraints(nullable: false)
			}

			column(name: "duplicate_references", type: "TINYINT(1)", defaultValueNumeric: 0) {
				constraints(nullable: false)
			}

			column(name: "errors", type: "JSON") {
				constraints(nullable: false)
			}

			column(name: "find_info", type: "JSON") {
				constraints(nullable: false)
			}

			column(name: "create_info", type: "JSON") {
				constraints(nullable: false)
			}

			column(name: "update_info", type: "JSON") {
				constraints(nullable: false)
			}

			column(name: "fields_info", type: "MEDIUMTEXT") {
				constraints(nullable: false)
			}

			column(name: "last_updated", type: "DATETIME")

			column(name: "version", type: "INT(11)")
		}
	}
}
