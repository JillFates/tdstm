/**
 * Deletes orphaned asset dependency bundles (with no project or invalid asset type)
 */
databaseChangeLog = {
	
	changeSet(author: "arecordon", id: "20150526 TM-3870") {
		comment("Deletes orphaned asset dependency bundles (with no project or invalid asset type).")
		sql("""
			DELETE FROM asset_dependency_bundle
  			WHERE project_id NOT IN (SELECT p.project_id FROM project p) OR asset_id NOT IN (SELECT asset_entity_id FROM asset_entity)
		""")
	}
}
