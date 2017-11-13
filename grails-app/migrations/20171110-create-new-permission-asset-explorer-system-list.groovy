import net.transitionmanager.domain.Permissions

databaseChangeLog = {
    // this changeset is for create new permission for AssetExplorerSystemList

    changeSet(author: "dcorrea", id: "20171110 TM-8093-1") {
        comment('Create new permission AssetExplorerSystemList')
        grailsChange {
            change {
                Map perms = [
                        'AssetExplorerSystemList' : [
                                group: 'NONE',
                                description: 'Can view list of system views',
                                roles: ['ADMIN']
                        ]
                ]

                ctx.getBean('databaseMigrationService').addPermissions(sql, perms)
            }
        }
    }

    changeSet(author: "dcorrea", id: "20171110 TM-8093-2") {
        comment('Update description of AssetExplorerCreate permissions')
        preConditions(onFail:'MARK_RAN') {
            sqlCheck(expectedResult:'1',
                    'select count(*) from permissions where permission_item = "AssetExplorerCreate"')
        }
        grailsChange {
            change {

                Permissions permission = Permissions.findByPermissionItem ('AssetExplorerCreate')
                permission.permissionItem = 'Can create one\'s own asset views'
                permission.save(flush:true)
            }
        }
    }

    changeSet(author: "dcorrea", id: "20171110 TM-8093-3") {
        comment('Update description of AssetExplorerDelete permissions')
        preConditions(onFail:'MARK_RAN') {
            sqlCheck(expectedResult:'1',
                    'select count(*) from permissions where permission_item = "AssetExplorerDelete"')
        }
        grailsChange {
            change {

                Permissions permission = Permissions.findByPermissionItem('AssetExplorerDelete')
                permission.permissionItem = 'Can delete one\'s own asset views'
                permission.save(flush:true)
            }
        }
    }

    changeSet(author: "dcorrea", id: "20171110 TM-8093-4") {
        comment('Update description of AssetExplorerEdit permissions')
        preConditions(onFail:'MARK_RAN') {
            sqlCheck(expectedResult:'1',
                    'select count(*) from permissions where permission_item = "AssetExplorerEdit"')
        }
        grailsChange {
            change {

                Permissions permission = Permissions.findByPermissionItem ('AssetExplorerEdit')
                permission.permissionItem = 'Can edit one\'s own asset views'
                permission.save(flush:true)
            }
        }
    }

}
