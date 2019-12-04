package version.v4_7_2

import net.transitionmanager.security.Permission

/**
 * @author jdanahy
 * Add new Permissions AssetExplorerOverrideAllUserGlobal and AssetExplorerOverrideAllUserProject.
 * Also add dataview property overridesView
 * @See TM-16316
 */
databaseChangeLog = {
    changeSet(author: "jdanahy", id: "20191105 TM-16316-1") {
        comment('Add new permissions for checking possibility of Save/SaveAs in backend')

        grailsChange {
            change {
                def perms = [
                        (Permission.AssetExplorerOverrideAllUserGlobal) : [
                                group      : 'NONE',
                                description: 'Can override a system view across all projects for all users',
                                roles      : ['ROLE_ADMIN', 'ROLE_CLIENT_ADMIN']
                        ],
                        (Permission.AssetExplorerOverrideAllUserProject): [
                                group      : 'NONE',
                                description: 'Can override a system view for current project for all users',
                                roles      : ['ROLE_ADMIN', 'ROLE_CLIENT_ADMIN', 'ROLE_CLIENT_MANAGER']
                        ]
                ]

                def databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)

            }
        }
    }
    changeSet(author: 'jdanahy', id: '20191105 TM-16316-2') {
        comment('Added DataView property overridesView')

        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'dataview', columnName: 'overrides_view_id')
            }
        }

        addColumn(tableName: 'dataview') {
            column(name: 'overrides_view_id', type: 'BIGINT') {
                constraints(nullable: 'true')
            }
        }

        createIndex(indexName: 'FK_override_view', tableName: 'dataview') {
            column(name: 'overrides_view_id')
        }

        addForeignKeyConstraint(
                baseColumnNames: 'overrides_view_id',
                baseTableName: 'dataview',
                constraintName: 'FK_override_view',
                onDelete: 'CASCADE',
                referencedColumnNames: 'id',
                referencedTableName: 'dataview'
        )
    }
}