package version.v5_0_0

import net.transitionmanager.security.Permission
import net.transitionmanager.common.DatabaseMigrationService


databaseChangeLog = {
    changeSet (author: 'arecordon', id: '20191126 TM-13916-1') {
        comment('Add TaskManagerView, TaskEdit and CommentCreate to all the users with USER role')

        grailsChange {
            change {
                List<String> permissions = [Permission.TaskManagerView, Permission.TaskEdit, Permission.CommentCreate]
                String sqlStatement = """
                    INSERT INTO role_permissions (permission_id, role) 
                    VALUES ((SELECT id FROM permissions WHERE permission_item=:item), :role)"""
                String userRole = "ROLE_USER"
                permissions.each { String permission ->
                    Map queryParams = [
                            item: permission,
                            role: userRole
                    ]
                    sql.executeUpdate(sqlStatement, queryParams)
                }
            }
        }
    }

    changeSet(author: "arecordon", id: "20191126 TM-13916-2") {
        comment('Add new ActionView and TaskManagerAllTasks permissions.')

        grailsChange {
            change {
                Map perms = [
                        (Permission.ActionView): [
                                group      : 'NONE',
                                description: 'Can view action details',
                                roles      : ['ROLE_ADMIN', 'ROLE_CLIENT_ADMIN', 'ROLE_CLIENT_MGR', 'ROLE_SUPERVISOR']
                        ],
                        (Permission.TaskManagerAllTasks): [
                                group      : 'NONE',
                                description: 'Can view all tasks and toggle My Tasks in Task Manager',
                                roles      : ['ROLE_ADMIN', 'ROLE_CLIENT_ADMIN', 'ROLE_CLIENT_MGR', 'ROLE_SUPERVISOR', 'ROLE_EDITOR', 'ROLE_USER']
                        ]
                ]

                DatabaseMigrationService databaseMigrationService = ctx.getBean('databaseMigrationService')
                databaseMigrationService.addPermissions(sql, perms)
            }
        }
    }
}