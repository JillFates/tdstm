import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.AssetDependencyType

/**
 * Updates values Asset Dependency / type and status columns, handling
 * case sensitive differences and setting to 'Unknown' those that don't
 * have a match.
 */
databaseChangeLog = {	

	// Updates asset dep type values with the correct capitalization.
	changeSet(author: "arecordon", id: "20151214 TM-3968-1") {
		comment("Updates asset dep type values with the correct capitalization")
		grailsChange{
			change{
				def typeValues = AssetDependencyType.getList()
				typeValues.remove("Unknown")
				(1..typeValues.size()).each{
					def tv = typeValues[it - 1]
					sql.execute("UPDATE asset_dependency SET type='${tv}' WHERE LOWER(type) = LOWER('${tv}') AND type != '${tv}'")
				}	
			}
			
		}
	}


	// Updates asset dep status values with the correct capitalization.
	changeSet(author: "arecordon", id: "20151214 TM-3968-2") {
		comment("Updates asset dep status values with the correct capitalization")
		grailsChange{
			change{
				def statusValues = AssetDependencyStatus.getList()
				statusValues.remove("Unknown")
				(1..statusValues.size()).each{
					def sv = statusValues[it - 1]
					sql.execute("UPDATE asset_dependency SET status='${sv}' WHERE LOWER(type) = LOWER('${sv}') AND status != '${sv}'")
				}	
			}
			
		}
	}


	// Set to 'Unknown' the Asset Dep type column for those records that don't match any of the accepted values.
	changeSet(author: "arecordon", id: "20151214 TM-3968-3") {

		comment("Set to 'Unknown' the Asset Dep type column for those records that don't match any of the accepted values.")

		sql("""
				UPDATE asset_dependency SET type = 'Unknown' WHERE type NOT IN ('Backup','Batch', 'DB', 'File', 'Hosts', 'Runs On', 'Web')
			""")
	}


	// Set to 'Unknown' the Asset Dep status column for those records that don't match any of the accepted values.
	changeSet(author: "arecordon", id: "20151214 TM-3968-4") {

		comment("Set to 'Unknown' the Asset Dep status column for those records that don't match any of the accepted values.")

		sql("""
				UPDATE asset_dependency SET status = 'Unknown' WHERE status NOT IN ('Validated', 'Not Applicable', 'Questioned', 'Archived', 'Future', 'Testing')
			""")
	}



}