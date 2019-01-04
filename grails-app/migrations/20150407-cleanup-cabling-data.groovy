/**
 * Cleanup cabling data
 */
databaseChangeLog = {
	
	changeSet(author: "dscarpa", id: "20150407 TM-3788-1") {
		comment('Update devices that have orphaned Models')
		sql("""
			update asset_entity a
			set model_id=null
			where a.model_id is not null and not exists (select 1 from model m where  m.model_id = a.model_id);
		""")
	}
	
	changeSet(author: "dscarpa", id: "20150407 TM-3788-2") {
		comment('Update devices that have orphaned Mfg')
		sql("""
			update asset_entity a
			set manufacturer_id=null
			where a.manufacturer_id is not null and exists (select 1 from manufacturer m where  m. manufacturer_id = a. manufacturer_id);
		""")
	}

	changeSet(author: "dscarpa", id: "20150407 TM-3788-3") {
		comment('Delete Asset Cables that are orphaned from relationships')
		sql("""
			DELETE FROM asset_cable_map
			WHERE NOT EXISTS (select 1 from asset_entity a where a.asset_entity_id=asset_from_id);
		""")
	}

	changeSet(author: "dscarpa", id: "20150407 TM-3788-4") {
		comment('Delete Asset Cables that are orphaned to relationships')
		sql("""
			DELETE FROM asset_cable_map 
			WHERE asset_to_id IS NOT NULL AND
				NOT EXISTS (select 1 from asset_entity a where a.asset_entity_id=asset_to_id);
		""")
	}

	changeSet(author: "dscarpa", id: "20150407 TM-3788-5") {
		comment('Delete Asset Cables that have orphaned model connectors from relationship')
		sql("""
			DELETE FROM asset_cable_map WHERE 
				asset_from_port_id IS NOT NULL AND 
				NOT EXISTS (select 1 from model_connector mc1 where mc1.model_connectors_id = asset_from_port_id);
		""")
	}

	changeSet(author: "dscarpa", id: "20150407 TM-3788-6") {
		comment('Delete Asset Cables that have orphaned model connectors to relationship')
		sql("""
			DELETE FROM asset_cable_map WHERE 
				asset_to_port_id IS NOT NULL AND 
				NOT EXISTS (select 1 from model_connector mc1 where mc1.model_connectors_id = asset_to_port_id);
		""")
	}

}
