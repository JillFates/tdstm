import net.transitionmanager.domain.Project
import net.transitionmanager.service.ProjectService


/**
 * Create the default bundle for projects that have none.
 */

databaseChangeLog = {

    changeSet(author: "arecordon", id: "20171025 TM-7858-2") {
        comment("Create the default bundle for projects without one.")
        grailsChange {
            change {
                Project.withNewSession() {
                    List<Project> projects = Project.findAllByDefaultBundleIsNull()
                    ProjectService projectService = ctx.getBean('projectService')
                    for (project in projects) {
                        projectService.createDefaultBundle(project, 'TBD')
                    }
                }
            }
        }
    }

}
