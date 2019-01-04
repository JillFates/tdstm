/**
 * Adds 2 columns to the role type table: level and type.
 */
databaseChangeLog = {	
	
	// Adds the level and type columns to the role type table.
	changeSet(author: "arecordon", id: "20150803 TM-4058-1") {
		comment('Adds the level and type columns to the role type table.')
		// Adds type column.
		sql("ALTER TABLE role_type ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT ''")
		// Adds constraint on the newly created type column. 
		sql("ALTER TABLE role_type ADD CONSTRAINT RoleTypeInList CHECK(type IN('APP','PARTY','PROJECT','TEAM','SECURITY'))")
		// Adds level column.
		sql("ALTER TABLE role_type ADD COLUMN level TINYINT")

	}

	// Updates existing role types with default values for type.
	changeSet(author: "arecordon", id: "20150803 TM-4058-2") {
		comment('Updates existing role types with default values for type.')
		grailsChange{
			change{
				def typeValues = ["SECURITY", "TEAM", "PROJECT", "PARTY", "APP"]
				def targetDescriptions = ["System", "Staff", "Project", "Party", "App"]
				(1..typeValues.size()).each{
					def tv = typeValues[it - 1]
					def td = "${targetDescriptions[it - 1]}%"
					sql.execute("UPDATE role_type SET type = '${tv}' WHERE description LIKE '${td}'")
				}	
			}
			
		}
	}


	// Updates existing role types with default values for level.
	changeSet(author: "arecordon", id: "20150803 TM-4058-3") {
		comment('Updates existing role types with default values for level.')
		grailsChange{
			change{
				def levelValues = [100, 50, 40, 30, 20, 10]
				def targetTypeCodes = ["ADMIN", "CLIENT_ADMIN", "CLIENT_MGR", "SUPERVISOR", "EDITOR", "USER"]
				(1..levelValues.size()).each{
					def lv = levelValues[it - 1]
					def ttc = targetTypeCodes[it - 1]
					sql.execute("UPDATE role_type SET level = ${lv} WHERE role_type_code = '${ttc}'")
				}
			}
		}
	}

	// Drops the default value for the type column.
	changeSet(author: "arecordon", id: "20150803 TM-4058-4") {
		comment('Drops the default value for the type column.')
		// Adds type column.
		sql("ALTER TABLE role_type ALTER type DROP DEFAULT")
	
	}

}
