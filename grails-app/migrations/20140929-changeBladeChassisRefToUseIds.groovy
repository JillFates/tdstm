databaseChangeLog = {

	changeSet(author: "jmartin", id: "20140927 TM-3341-1") {
		comment('Change the source/target blade chassis references to use id instead of names')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				columnExists(schemaName:'tdstm', tableName:'asset_entity', columnName:'source_chassis_id' )
			}
		}
		
		grailsChange {
			change {
				// Add the new columns
				sql.execute("""ALTER TABLE asset_entity 
					ADD COLUMN source_chassis_id INT(11) AFTER source_blade_chassis, 
					ADD COLUMN target_chassis_id INT(11) AFTER target_blade_chassis """)
			}
		}
	}

	changeSet(author: "jmartin", id: "20140927 TM-3341-2") {
		comment('Update the blades new source/target chassis references')

		grailsChange {
			change {
				def skips = [:].withDefault {0}

				// Helper closure used to find the referenced chassis, which will also validate that the asset is a Chassis
				def findChassis = { projectId, assetTag ->
					if (assetTag) {
						def chassisRow = sql.firstRow("SELECT asset_entity_id as id, m.asset_type as assetType " + 
							"FROM asset_entity a " + 
							"LEFT OUTER JOIN model m ON m.model_id = a.model_id " +
							"WHERE a.project_id = $projectId AND (a.asset_name='$assetTag' OR a.asset_tag='$assetTag') ")
						if (chassisRow) {
							def at = chassisRow.assetType
							if (chassisRow.assetType == 'Chassis' || chassisRow.assetType == 'Blade Chassis') {
								return chassisRow.id
							} else {
								println "Chassis has wrong assetType (${chassisRow.assetType}) for project $projectId, asset ${chassisRow.id} - SKIPPED"
							}
						} else {
							println "Chassis not found for project $projectId, assetTag $assetTag - SKIPPED"
						}
						skips["$projectId"]++
					}
					return null
				}

				// Update all of the existing references
				sql.eachRow("SELECT asset_entity_id as id, asset_name, project_id, m.asset_type, source_blade_chassis as sourceChassis, target_blade_chassis as targetChassis " +
					"FROM asset_entity a " +
					"LEFT OUTER JOIN model m ON m.model_id = a.model_id " +
					"WHERE a.source_blade_chassis <> '' OR a.target_blade_chassis <> '' ORDER BY project_id, id") { row ->

					def assetType = row.asset_type
					def pid = row.project_id
					def aid = row.id

					if (assetType == 'Blade') {
						// Try and find the referenced asset
						def chassisId = findChassis(row.project_id, row.sourceChassis)
						if (chassisId) {
							sql.execute("UPDATE asset_entity SET source_chassis_id=$chassisId WHERE asset_entity_id=${aid}")
						}

						chassisId = findChassis(row.project_id, row.targetChassis)
						if (chassisId) {
							sql.execute("UPDATE asset_entity SET target_chassis_id=$chassisId WHERE asset_entity_id=${aid}")
						}
					} else {
						println "Blade has invalid assetType ($assetType ${row.asset_name}) for project $pid, asset $aid"
						skips["$pid"]++
					}

				}

				sql.execute("ALTER TABLE asset_entity DROP COLUMN source_blade_chassis, DROP COLUMN target_blade_chassis")

				// print out all of the assets that were impacted
				println "Projects with skipped chassis references: ${skips.size()}"
				skips.each { p, c -> 
					def d = sql.firstRow("SELECT pg.name as name FROM party_group pg JOIN project p ON p.client_id=pg.party_group_id WHERE p.project_id=$p")
					println "$p ${d.name} - $c"
				}
			}
		}
	}

	changeSet(author: "jmartin", id: "20140927 TM-3341-3") {
		comment('Add indexes fk_sourceChassis/fk_targetChassisto new columns')
		
		preConditions(onFail:'MARK_RAN') {
			not {
				indexExists(schemaName:"tdstm", indexName:"fk_sourceChassis" )
			}
		}
		createIndex(tableName:'asset_entity', indexName:'fk_assetEntity_sourceChassis', unique:'false') {
			column(name:'source_chassis_id')
		}
		createIndex(tableName:'asset_entity', indexName:'fk_assetEntity_targetChassis', unique:'false') {
			column(name:'target_chassis_id')
		}
	}

	changeSet(author: "jmartin", id: "20140927 TM-3341-4") {
		comment('Change the EAV Attributes for the sourceBladeChassis and targetBladeChassis properties')
		grailsChange {
			change {
				sql.execute("UPDATE eav_attribute SET attribute_code='sourceChassis' WHERE attribute_code='sourceBladeChassis'" )
				sql.execute("UPDATE eav_attribute SET attribute_code='targetChassis' WHERE attribute_code='targetBladeChassis'" )
			}
		}
	}

}
