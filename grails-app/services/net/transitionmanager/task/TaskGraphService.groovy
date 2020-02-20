package net.transitionmanager.task

import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.StringUtil
import net.transitionmanager.command.task.TaskHighlightOptionsCommand
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.security.SecurityService
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.taskgraph.TaskHighlightOptions
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

import java.sql.ResultSet
import java.sql.SQLException

class TaskGraphService implements ServiceMethods {

    NamedParameterJdbcTemplate namedParameterJdbcTemplate


    SecurityService securityService

    /**
     * This method returns a map containing four lists:
     * - persons: a list of all the persons assigned to tasks in the event (starting with 'Unassigned').
     * - teams: a list with all the different teams (roles) associated with the tasks (starting with 'Unassigned').
     * - environments: all the different environments for the tasks in the event.
     * - ownersAndSmes: a list with the names of all the people assigned as AppOwner, SME or SME2 in applications
     *                  associated with a task in the event.
     * @param project
     * @param command
     * @return
     */
    Map getTaskHighlightOptions(Project project, TaskHighlightOptionsCommand command) {
        // For viewing unpublished tasks, the flag must be set to 1 and the user must have the appropriate permission.
        boolean viewUnpublished = StringUtil.toBoolean(command.viewUnpublished) && securityService.hasPermission(Permission.TaskViewUnpublished)
        // Get the corresponding event or fail if not found for the project.
        MoveEvent moveEvent = get(MoveEvent, command.eventId, project)
        // Get the query string.
        String query = TaskHighlightOptions.getHighlightOptionsQuery(viewUnpublished)
        // Retrieve all the information in a single query.
        List<Map> tasks = namedParameterJdbcTemplate.query(query, [moveEventId: moveEvent.id], new TaskHighlightOptions.TaskHighlightOptionsMapper())

        return TaskHighlightOptions.getHighlightOptions(tasks)
    }

}
