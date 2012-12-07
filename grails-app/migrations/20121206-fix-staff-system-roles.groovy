/**
 * This set of Database changes that are required to reorganize System and Staff role_type and how they are used in party_role and party_relationship
 * associations. It also fixes all task roles to be Staff types or clears them out.
 */
databaseChangeLog = {

	changeSet(author: "jmartin", id: "20121206 TM-1135.1") {
		comment('Add new role_type PROJ_ADMIN for Staff type and reassign party relationships')
		sql("INSERT INTO role_type VALUES('PROJ_ADMIN','Staff : Project Admin')")
		sql("UPDATE party_relationship SET role_type_code_to_id='PROJ_ADMIN' WHERE role_type_code_to_id='PROJECT_ADMIN'")
		sql("UPDATE move_event_staff SET role_id='PROJ_ADMIN' WHERE role_id='PROJECT_ADMIN'")		
	}

	changeSet(author: "jmartin", id: "20121206 TM-1135.2") {
		comment('Rename PROJECT_ADMIN role to CLIENT_ADMIN System type, change RolePermissions and add CLIENT_ADMIN system role to users where they have PROJ_ADMIN relation')
		sql("UPDATE role_type SET role_type_code='CLIENT_ADMIN', description='System : Client Admin' WHERE role_type_code='PROJECT_ADMIN'")
		sql("UPDATE role_permissions SET role='CLIENT_ADMIN' WHERE role='PROJECT_ADMIN'")
		sql("""INSERT INTO party_role 
			(SELECT DISTINCT party_id_to_id, 'CLIENT_ADMIN' FROM party_relationship WHERE role_type_code_to_id='PROJ_ADMIN')
			ON DUPLICATE KEY UPDATE role_type_id='CLIENT_ADMIN'""")
	}
	
	changeSet(author: "jmartin", id: "20121206 TM-1135.3") {
		comment('Add new CLIENT_MGR system role, change PartyRole PROJ_MGR to CLIENT_MGR, change RolePermissions and create CLIENT_MRG PartyRole where user has PROJ_MRG relation')
		// Note that we're not doing anything to the PROJ_MGR role
		sql("INSERT INTO role_type VALUES('CLIENT_MGR','System : Client Manager')")
		sql("UPDATE party_role SET role_type_id='CLIENT_MGR' WHERE role_type_id='PROJ_MGR'")
		sql("UPDATE role_permissions SET role='CLIENT_MGR' WHERE role='PROJECT_MANAGER'")
		sql("""INSERT INTO party_role 
			(SELECT DISTINCT party_id_to_id, 'CLIENT_MGR' FROM party_relationship WHERE role_type_code_to_id='PROJ_MGR')
			ON DUPLICATE KEY UPDATE role_type_id='CLIENT_MGR'""")
	}
	
	changeSet(author: "jmartin", id: "20121206 TM-1135.4") {
		comment('Delete WORKSTATION RoleType')
		sql("DELETE FROM party_role WHERE role_type_id='WORKSTATION'")
		sql("DELETE FROM role_type WHERE role_type_code='WORKSTATION'")
	}
	
	changeSet(author: "jmartin", id: "20121206 TM-1135.5") {
		comment('Add SUPERVISOR party_role')
		sql("INSERT INTO role_type VALUES('SUPERVISOR','System : Supervisor')")
	}

	changeSet(author: "jmartin", id: "20121206 TM-1135.6") {
		comment('Delete PartyRole entries where RoleType are not System')
		sql("""DELETE FROM party_role 
			WHERE role_type_id NOT IN ( (SELECT role_type_code FROM role_type WHERE description LIKE 'System%') )""")
	}
	
	changeSet(author: "jmartin", id: "20121206 TM-1135.7") {
		comment('Clear task roles that are not Staff')
		sql("""UPDATE asset_comment SET role=NULL 
			WHERE role NOT IN ( (SELECT role_type_code FROM role_type WHERE description LIKE 'Staff%') )""")
	}
	
	changeSet(author: "jmartin", id: "20121206 TM-1135.8") {
		comment('Delete relations using System roles')
		sql("""DELETE FROM party_relationship
			WHERE role_type_code_to_id IN (SELECT role_type_code FROM role_type WHERE description LIKE 'System%')""")
	}
	
	changeSet(author: "jmartin", id: "20121206 TM-1135.9") {
		comment('Change role OBSERVER to USER in RolePermissions and remove from PartyRole and RoleType')
		sql("UPDATE role_permissions SET role='USER' WHERE role='OBSERVER'")			
		sql("DELETE FROM party_role WHERE role_type_id='OBSERVER'")	
		sql("DELETE FROM role_type WHERE role_type_code='OBSERVER'")
	}
	
	changeSet(author: "jmartin", id: "20121206 TM-1135.10") {
		comment('Change RoleType from MANAGER to EDITOR')
		sql("INSERT INTO role_type VALUES('EDITOR','System : Editor')")
		sql("UPDATE party_role SET role_type_id='MANAGER' WHERE role_type_id='EDITOR'")
		sql("UPDATE role_permissions SET role='EDITOR' WHERE role='MANAGER'")			
		sql("DELETE FROM role_type WHERE role_type_code='MANAGER'")
	}
	
}
