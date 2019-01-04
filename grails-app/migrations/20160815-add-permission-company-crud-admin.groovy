/**
 * @author @tavo_luna
 * This set is required to add new permissions to Create|Edit|Delete Companies
 */

databaseChangeLog = {
	// this changeset is used to add 'RestartApplication' permission item in permission table and assign 'ADMIN' by default.
	changeSet(author: "oluna", id: "20160815 TM-5164") {
		comment('Add Company Create|Edit|Delete permissions')
		preConditions(onFail:'MARK_RAN') {
			sqlCheck(expectedResult:'0',
					'select count(*) from permissions where permission_group="COMPANY" and permission_item = "CompanyCreate"')
		}

		grailsChange {
			change {
				Map perms = [
						'CompanyCreate' : [
								group: 'COMPANY',
								description: 'Ability to create Companies',
								roles: ['ADMIN']
						],
						'CompanyEdit' : [
								group: 'COMPANY',
								description: 'Ability to Edit Companies',
								roles: ['ADMIN']
						],
						'CompanyDelete' : [
								group: 'COMPANY',
								description: 'Ability to Delete Companies',
								roles: ['ADMIN']
						]
				]

				ctx.getBean('databaseMigrationService').addPermissions(sql, perms)
			}
		}
	}
}
