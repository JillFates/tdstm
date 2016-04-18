databaseChangeLog = {
	// this changeset is used to add 'monitoring' permission item in permission table and assign 'ADMIN'  by default.

	changeSet(author: "jmartin", id: "20160415 TM-4735") {
		comment('Add new permissions for the Account Import/Export feature')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 
				'select count(*) from permissions where permission_group="USER" and permission_item = "ExportUserLogin"')
		}

		grailsChange {
			change {
				Map perms = [
					'ExportUserLogin' : [
						group: 'USER',
						description: 'Ability to export user login information',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'ImportUserLogin' : [
						group: 'USER',
						description: 'Ability to update and create users from import',
						roles: ['ADMIN', 'CLIENT_ADMIN']
					],
					'ImportPerson' : [
						group: 'PERSON',
						description: 'Ability to update and create persons from import',
						roles: ['ADMIN', 'CLIENT_ADMIN', 'CLIENT_MGR']
					]
				]

				ctx.getBean('databaseMigrationService').addPermissions(sql, perms)
			}
		}
	}

	changeSet(author: "jmartin", id: "20160415 TM-4735-1") {
		comment('Move the PersonExport permission to the PERSON group from TASK')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0', 
				'select count(*) from permissions where permission_group="TASK" and permission_item = "PersonExport"')
		}

		sql("""UPDATE permissions SET permission_group='PERSON' 
			WHERE permission_group='TASK' AND permission_item='ExportPerson""")
	}

}
