/**
 * This set adds indexes to the UserLogin
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20130111 TM-1176.1") {
		comment('Add a few new RoleType records and update an existing one')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from role_type where role_type_code="SYS_ADMIN_WIN"')
	    }
		insert(tableName:'role_type') {
			column(name:'role_type_code', value:'SYS_ADMIN_WIN')
			column(name:'description', value:'Staff : System Admin-Win')
		}
		insert(tableName:'role_type') {
			column(name:'role_type_code', value:'SYS_ADMIN_LNX')
			column(name:'description', value:'Staff : System Admin-*nix')
		}
		insert(tableName:'role_type') {
			column(name:'role_type_code', value:'DB_ADMIN_ORA')
			column(name:'description', value:'Staff : Database Admin-Oracle')
		}
		insert(tableName:'role_type') {
			column(name:'role_type_code', value:'DB_ADMIN_MS')
			column(name:'description', value:'Staff : Database Admin-MSSQL')
		}
		insert(tableName:'role_type') {
			column(name:'role_type_code', value:'MOVE_TECH_SR')
			column(name:'description', value:'Staff : Move Technician-Sr')
		}
		sql('update role_type set description = "Project : Move Bundle" where role_type_code="MOVE_BUNDLE"')
	}
}
