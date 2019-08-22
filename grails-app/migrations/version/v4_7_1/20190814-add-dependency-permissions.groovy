package version.v4_7_1

import net.transitionmanager.security.Permission

/**
 * @author ecantu
 * Add new Permissions for the Dependency Domain.
 * @See TM-15734
 */
databaseChangeLog = {
    changeSet(author: "ecantu", id: "20190814 TM-15734-1") {
        comment('Add new permission for bulk select dependencies in the Dependency List screen')

        grailsChange {
            change {
                def perms = [
                        (Permission.AssetDependenciesBulkSelect): [
                                group      : 'NONE',
                                description: 'Can select multiple Asset Dependencies in the Dependency List',
                                roles      : ['ROLE_ADMIN', 'ROLE_CLIENT_ADMIN', 'ROLE_CLIENT_MGR', 'ROLE_SUPERVISOR', 'ROLE_EDITOR']
                        ]
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }

    changeSet(author: "ecantu", id: "20190814 TM-15734-2") {
        comment('Add new permission for edit dependencies')

        grailsChange {
            change {
                def perms = [
                        (Permission.AssetDependencyEdit): [
                                group      : 'NONE',
                                description: 'Can edit Asset Dependencies',
                                roles      : ['ROLE_ADMIN', 'ROLE_CLIENT_ADMIN', 'ROLE_CLIENT_MGR', 'ROLE_SUPERVISOR', 'ROLE_EDITOR']
                        ]
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }

    changeSet(author: "ecantu", id: "20190814 TM-15734-3") {
        comment('Add new permission for delete dependencies')

        grailsChange {
            change {
                def perms = [
                        (Permission.AssetDependencyDelete): [
                                group      : 'NONE',
                                description: 'Can delete Asset Dependencies',
                                roles      : ['ROLE_ADMIN', 'ROLE_CLIENT_ADMIN', 'ROLE_CLIENT_MGR', 'ROLE_SUPERVISOR', 'ROLE_EDITOR']
                        ]
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }
}