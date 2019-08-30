package version.v4_7_1

import net.transitionmanager.security.Permission

/**
 * @author ecantu
 * Add Asset Bulk Select Permission.
 * @See TM-15753
 */
databaseChangeLog = {
    changeSet(author: "ecantu", id: "20190823 TM-15753-1") {
        comment('Add new permission for bulk select assets in the Asset List screens')

        grailsChange {
            change {
                def perms = [
                        (Permission.AssetBulkSelect): [
                                group      : 'NONE',
                                description: 'Can select multiple Asset in the Asset Lists',
                                roles      : ['ROLE_ADMIN', 'ROLE_CLIENT_ADMIN', 'ROLE_CLIENT_MGR', 'ROLE_SUPERVISOR', 'ROLE_EDITOR']
                        ]
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }
}