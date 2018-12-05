/**
 * Delete ReportViewDiscovery permission.
 */
databaseChangeLog = {

    changeSet(author: "ecantu", id: "20181127 TM-13038-1") {
        comment('Deletes the ReportViewDiscovery permission.')
        grailsChange{
            change{
                ctx.getBean('databaseMigrationService').removePermissions(sql, ["ReportViewDiscovery"])
            }
        }

    }
}
