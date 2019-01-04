/**
 * Updates values Asset Dependency / type and status columns, handling
 * case sensitive differences and setting to 'Unknown' those that don't
 * have a match.
 */
databaseChangeLog = {	

	// Updates asset dep type values with the correct capitalization.
	changeSet(author: "jmartin", id: "20151214 TM-3968-1-Fix.1") {
		comment("Updates asset dep type values with the correct capitalization")
		sql("""
			update asset_dependency ad
			join asset_options ao on ao.value=ad.type and ao.type='DEPENDENCY_TYPE' and binary ao.value <> binary ad.type
			set ad.type = ao.value
		""")
	}

	changeSet(author: "jmartin", id: "20151214 TM-3968-1-Fix.2") {
		comment("Updates asset dep status values with the correct capitalization")
		sql("""
			update asset_dependency ad
			join asset_options ao on ao.value=ad.status and ao.type='DEPENDENCY_STATUS' and binary ao.value <> binary ad.status
			set ad.status = ao.value
		""")
	}

	changeSet(author: "jmartin", id: "20151214 TM-3968-2-Fix.1") {
		comment("Set to 'Unknown' the Asset Dep type column for records that do not match values from AssetOptions")
		sql("""
			update asset_dependency ad
			set ad.type='Unknown'
			where ad.asset_dependency_id in (
				select id from (
					select asset_dependency_id as id
					from asset_dependency ad
					left outer join asset_options ao on ao.value=ad.type and ao.type='DEPENDENCY_TYPE'
					where ad.type is not null and ao.value is null
				) as queryForAssetDepId
			)
		""")
	}

	changeSet(author: "jmartin", id: "20151214 TM-3968-2-Fix.2") {
		comment("Set to 'Unknown' the Asset Dep type column for records that do not match values from AssetOptions")
		sql("""
			update asset_dependency ad
			set ad.status='Unknown'
			where ad.asset_dependency_id in (
				select id from (
					select asset_dependency_id as id
					from asset_dependency ad
					left outer join asset_options ao on ao.value=ad.status and ao.type='DEPENDENCY_STATUS'
					where ad.status is not null and ao.value is null
				) as queryForAssetDepId
			)
		""")
	}
}
