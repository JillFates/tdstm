import net.transitionmanager.domain.Project

/**
 * This set of database changes will create the default_bundle_id column in project table and
 * Update all projects to set "default_bundle_id" and update asset_entity move_bundle_id to default bundle if null
 * and set asset_entity's move_bundle_id column to not null
 */

databaseChangeLog = {

    changeSet(author: "arecordon", id: "20171025 TM-7858-1") {
        comment("Create the default bundle for projects without one.")
        grailsChange {
            change {
                def projects = Project.findAllByDefaultBundleIsNull()
                def projectService = ctx.getBean('projectService')
                projects.each{ project ->
                    project.defaultBundle = projectService.getDefaultBundle(project)
                    project.save()
                }
            }
        }
    }

}