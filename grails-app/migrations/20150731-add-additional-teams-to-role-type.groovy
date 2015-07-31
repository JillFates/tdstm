/*Adds new team codes for AIX, UX, DB2, VM and Backup*/
databaseChangeLog = {
	changeSet(author: "jdanahy", id: "20150731 TM-4023-1") {
		comment('Create entry for SYS_ADMIN_AIX in role_type')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from role_type where role_type_code="SYS_ADMIN_AIX"')
		}
		sql("""
		INSERT INTO role_type VALUES ('SYS_ADMIN_AIX','Staff : System Admin-AIX','Manages IBM AIX servers');
		""")
    }
	changeSet(author: "jdanahy", id: "20150731 TM-4023-2") {
		comment('Create entry for SYS_ADMIN_UNIX in role_type')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from role_type where role_type_code="SYS_ADMIN_UNIX"')
		}
		sql("""
		INSERT INTO role_type VALUES ('SYS_ADMIN_UNIX','Staff : System Admin-Unix','Manages Unix servers');
		""")
	}
	changeSet(author: "jdanahy", id: "20150731 TM-4023-3") {
		comment('Create entry for DBA_DB2 in role_type')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from role_type where role_type_code="DBA_DB2"')
		}
		sql("""
		INSERT INTO role_type VALUES ('DBA_DB2','Staff : Database Admin-DB2','Manages IBM DB2 databases');
		""")
    }
	changeSet(author: "jdanahy", id: "20150731 TM-4023-4") {
		comment('Create entry for VM_ADMIN in role_type')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from role_type where role_type_code="VM_ADMIN"')
		}
		sql("""
		INSERT INTO role_type VALUES ('VM_ADMIN','Staff : VM Admin','Manages VM Infrastructure');
		""")
    }
	changeSet(author: "jdanahy", id: "20150731 TM-4023-5") {
		comment('Create entry for BACKUP_ADMIN in role_type')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 'select count(*) from role_type where role_type_code="BACKUP_ADMIN"')
		}
		sql("""
		INSERT INTO role_type VALUES ('BACKUP_ADMIN','Staff : Backup Admin','Manages Backup infrastructure');
		""")
    }
}