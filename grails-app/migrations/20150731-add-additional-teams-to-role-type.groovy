/*Adds new team codes for AIX, UX, DB2, VM and Backup*/
databaseChangeLog = {
	List teams = [
		['SYS_ADMIN_AIX', 'Staff : System Admin-AIX', 'Manages IBM AIX servers'],
		['SYS_ADMIN_UNIX','Staff : System Admin-Unix','Manages Unix servers'],
		['DBA_DB2', 'Staff : Database Admin-DB2','Manages IBM DB2 databases'],
		['VM_ADMIN','Staff : VM Admin','Manages VM Infrastructure'],
		['VM_ADMIN_AWS','Staff : VM Admin (AWS)','Manages Amazon Cloud Infrastructure'],
		['BACKUP_ADMIN','Staff : Backup Admin','Manages Backup infrastructure']
	]

	changeSet(author: "jmartin", id: "201602109 TM-4023-1") {
		comment('Add several new teams to the RoleType table')
		grailsChange {
			change {
				teams.each { t ->
					sql.execute("""
						INSERT INTO role_type (role_type_code, description, help, type) 
							VALUES(?, ?, ?, ?)
							ON DUPLICATE KEY UPDATE type='TEAM'
						""", [t[0], t[1], t[2], 'TEAM'] )

				}
			}
		}
	}
}
