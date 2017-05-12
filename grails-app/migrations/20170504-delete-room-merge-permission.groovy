/**
 * Delete RoomMerge permission.
 */
databaseChangeLog = {

    changeSet(author: "arecordon", id: "20170504 TM-6188-1") {
        comment('Deletes the Room Merge permission.')
        grailsChange{
            change{
                ctx.getBean('databaseMigrationService').removePermissions(sql, ["RoomMerge"])
            }
        }
        
    }
}
