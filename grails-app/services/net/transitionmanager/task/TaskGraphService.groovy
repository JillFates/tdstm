package net.transitionmanager.task

import com.tdsops.tm.enums.domain.UserPreferenceEnum
import net.transitionmanager.command.task.ViewUnpublishedCommand
import net.transitionmanager.command.task.TaskSearchCommand
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.security.SecurityService
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.taskgraph.TaskHighlightOptions
import net.transitionmanager.task.taskgraph.TaskSearch
import net.transitionmanager.task.timeline.CPAResults
import net.transitionmanager.task.timeline.TimelineService
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class TaskGraphService implements ServiceMethods {

    NamedParameterJdbcTemplate namedParameterJdbcTemplate


    SecurityService securityService
    TimelineService timelineService
    UserPreferenceService userPreferenceService

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
    Map getTaskHighlightOptions(Project project, ViewUnpublishedCommand command) {
        // For viewing unpublished tasks, the flag must be set to 1 and the user must have the appropriate permission.
        boolean viewUnpublished = command.viewUnpublishedTasks() && securityService.hasPermission(Permission.TaskViewUnpublished)
        // Get the corresponding event or fail if not found for the project.
        MoveEvent moveEvent = get(MoveEvent, command.eventId, project)
        // Get the query string.
        String query = TaskHighlightOptions.getHighlightOptionsQuery(viewUnpublished)
        // Retrieve all the information in a single query.
        List<Map> tasks = namedParameterJdbcTemplate.queryForList(query, [moveEventId: moveEvent.id]).collect{ Map row ->
            TaskHighlightOptions.mapRowToHighlightMap(row)
        }

        return TaskHighlightOptions.getHighlightOptions(tasks)
    }

    /**
     * Searches the tasks for the Task Graph given a Command Object with the corresponding parameters.
     * This method will respond with a list of just the IDs for each of tasks.
     *
     * Key aspects:
     *  - There are multiple filters that can be executed simultaneously.
     *  - There are three "sources" of tasks:
     *      - The query build based on the filters in the command object.
     *      - The tasks retrieved from the CPA -- if the {@code criticalPathMode} is set to "Realtime".
     *      - The tasks in the cycles in the CPA graph -- if the {@code cyclicalPath} is set to 1.
     * - If the CPA needs to be executed and yields no tasks, the main query will not be executed.
     * - An intersection of all three queries (should all of them be executed) is calculated.
     * @param moveEvent - the MoveEvent to filter by -- if any
     * @param viewUnpublished - whether or not to include unpublished tasks.
     * @return a list of tasks matching the filter options.
     */

    List<Long> searchTasks(Project project, TaskSearchCommand taskSearchCommand) {

        TaskSearch taskSearch = new TaskSearch(project, taskSearchCommand)

        List<Long> tasks = []

        // boolean that tells whether or not unpublished tasks need to be included.
        boolean viewUnpublished = taskSearchCommand.viewUnpublishedTasks()
        MoveEvent moveEvent = null
        // If a MoveEvent id is provided, fetch the domain object from the database (fail if the id is invalid or doesn't belong to the project).
        if (taskSearchCommand.eventId) {
            get(MoveEvent, taskSearchCommand.eventId, project)
        }

        // Check if the CPA needs to be calculated, in which case it gets executed.
        if (taskSearch.needsCPACalculation()) {
           // Calculate the CPA.
            CPAResults cpaResults = timelineService.calculateCPA(moveEvent, viewUnpublished)
            // Get a unique list of tasks based on the CPA results and the command object.
            tasks = taskSearch.getCPATasks(cpaResults)
            if (!tasks) {
                return tasks
            }
        }

        Map queryInfo = taskSearch.buildSearchQuery(moveEvent, viewUnpublished)

        tasks.addAll(namedParameterJdbcTemplate.queryForList(queryInfo.query, queryInfo.params)*.taskId)

        // Update the viewUnpublished and event preferences if needed.
        UserLogin userLogin = securityService.userLogin
        userPreferenceService.updatePreferenceIfNecessary(userLogin, UserPreferenceEnum.MOVE_EVENT, moveEvent)
        userPreferenceService.updatePreferenceIfNecessary(userLogin, UserPreferenceEnum.VIEW_UNPUBLISHED, viewUnpublished)

        return tasks.unique { Long a, Long b -> a <=> b }

    }

}
