package net.transitionmanager.tasks

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.task.TaskHighlightOptionsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.task.TaskGraphService

@Secured('isAuthenticated()')
@Slf4j
class WsTaskGraphController implements ControllerMethods {

    TaskGraphService taskGraphService

    /**
     * Retrieve a map with the options for Task Highlight.
     * @return
     */
    @HasPermission(Permission.TaskView)
    def taskHighlightOptions() {
        Project project = getProjectForWs()
        // Populate and validate the command object.
        TaskHighlightOptionsCommand command = populateCommandObject(TaskHighlightOptionsCommand)
        // Fetch the map with the information (already formatted and sorted).
        Map highlightOptions = taskGraphService.getTaskHighlightOptions(project, command)

        renderSuccessJson(highlightOptions)
    }

}
