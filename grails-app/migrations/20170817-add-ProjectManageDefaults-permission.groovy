/**
 * This migration script creates the permission for accessing the default
 * project and adds it to the ADMIN role.
 *
 * Additionally, it sets the completion date for the default project to 12/31/2100
 */
databaseChangeLog = {

    changeSet(author: "arecordon", id: "20170817 TM-6701 1") {
        comment('Adds new permission for accessing the default project')
        preConditions(onFail:'MARK_RAN') {
            sqlCheck(expectedResult:'0',
                    'SELECT COUNT(*) FROM permissions WHERE permission_item="ProjectManageDefaults"')
        }

        grailsChange {
            change {
                Map perms = [
                        'ProjectManageDefaults' : [
                                description: 'Can access the Default project in order to customize defaults',
                                roles: ['ADMIN']
                        ]
                ]

                ctx.getBean("databaseMigrationService").addPermissions(sql, perms)
            }
        }
    }

    changeSet(author: "arecordon", id: "20170817 TM-6701 2") {
        comment('Sets the completion date for the default project to the last day of 2100')

        sql("UPDATE project SET completion_date='2100-12-31' WHERE project_id=2")
    }
}
