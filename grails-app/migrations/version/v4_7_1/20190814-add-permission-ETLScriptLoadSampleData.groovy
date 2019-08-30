package version.v4_7_1

import net.transitionmanager.security.Permission

/**
 * @author ecantu
 * Add new Permission ETLScriptLoadSampleData.
 * @See TM-15721
 */
databaseChangeLog = {
    changeSet(author: "ecantu", id: "20190814 TM-15721-1") {
        comment('Add new permission for loading sample data in ETL Scripts')

        grailsChange {
            change {
                def perms = [
                        (Permission.ETLScriptLoadSampleData): [
                                group      : 'NONE',
                                description: 'Can load sample data for ETL Script',
                                roles      : ['ROLE_ADMIN', 'ROLE_CLIENT_ADMIN', 'ROLE_CLIENT_MGR', 'ROLE_SUPERVISOR', 'ROLE_EDITOR']
                        ]
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }
}