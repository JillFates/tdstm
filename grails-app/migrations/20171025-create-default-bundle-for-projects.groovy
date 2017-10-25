import net.transitionmanager.domain.Project

/**
 * Create the default bundle for projects that have none.
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