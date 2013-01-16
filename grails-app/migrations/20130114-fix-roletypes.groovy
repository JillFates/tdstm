/**
 * This set adds indexes to the UserLogin
 */
databaseChangeLog = {
	changeSet(author: "jmartin", id: "20130114 TM-1135.1") {
		comment('Change SystemAdmin-*nix to Linux')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'1', 'select count(*) from role_type where role_type_code="SYS_ADMIN_LNX" AND description="Staff : System Admin-*nix"')
	    }
		sql('update role_type set description = "Staff : System Admin-Linux" where role_type_code="SYS_ADMIN_LNX"')
	}
	
	changeSet(author: "jmartin", id: "20130114 TM-1135.2") {
		comment('Change all PartyRoles of type MANAGER to EDITOR')
		preConditions(onFail:'MARK_RAN') {
			not {
				sqlCheck(expectedResult:'0', 'SELECT count(*) FROM party_role WHERE role_type_id="MANAGER"')
			}
	    }
		sql("DELETE FROM party_role WHERE role_type_id='MANAGER' AND party_id IN ( \
			SELECT a.party_id FROM ( SELECT party_id FROM party_role b WHERE b.role_type_id='EDITOR' ) a )")
		sql("UPDATE party_role SET role_type_id='EDITOR' WHERE role_type_id='MANAGER'")
	}
	
	changeSet(author: "jmartin", id: "20130114 TM-1135.3") {
		comment('Change all PartyRoles of type OBSERVER to USER')
		preConditions(onFail:'MARK_RAN') {
			not{
				sqlCheck(expectedResult:'0', 'SELECT count(*) FROM party_role WHERE role_type_id="OBSERVER"')
			}
	    }
		sql("DELETE FROM party_role WHERE role_type_id='OBSERVER' AND party_id IN ( \
			SELECT a.party_id FROM ( SELECT party_id FROM party_role b WHERE b.role_type_id='USER' ) a )")
		sql("UPDATE party_role SET role_type_id='USER' WHERE role_type_id='OBSERVER'")
	}
	
	changeSet(author: "jmartin", id: "20130114 TM-1135.4") {
		comment('Remove all invalid PartyRole records')
		preConditions(onFail:'MARK_RAN') {
			not {				
				sqlCheck(expectedResult:'0', "SELECT COUNT(*) FROM party_role WHERE role_type_id NOT IN \
					(SELECT role_type_code FROM role_type rt WHERE description like 'System%')")
			}
	    }
		sql("DELETE FROM party_role WHERE role_type_id NOT IN \
			(SELECT role_type_code FROM role_type rt WHERE description like 'System%')")
	}

	changeSet(author: "jmartin", id: "20130114 TM-1135.5") {
		comment("Delete PartyRole records for parties that don't have UserLogin accounts")
		sql("DELETE FROM party_role where party_id IN ( SELECT id FROM ( \
			SELECT pr.party_id AS id FROM party_role pr LEFT OUTER JOIN user_login u ON u.person_id=pr.party_id WHERE u.person_id IS NULL \
			) t )")
	}
	
}
