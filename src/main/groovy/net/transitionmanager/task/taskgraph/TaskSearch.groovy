package net.transitionmanager.task.taskgraph


import net.transitionmanager.command.task.TaskSearchCommand
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.project.Project
import net.transitionmanager.task.timeline.CPAResults
import net.transitionmanager.task.timeline.TaskVertex

class TaskSearch {

    /**
     * The user's current project.
     */
    Project project

    /**
     * The Command Object with the filters.
     */
    TaskSearchCommand taskSearchCommand

    /**
     * The parameters for the main query.
     */
    Map queryParams = [:]

    /**
     * Basic constructor that keeps a local copy of the command object that needs to be used
     * to search for the tasks.
     * @param TaskSearchCommand
     */
    TaskSearch(Project project, TaskSearchCommand taskSearchCommand) {
        this.project = project
        this.taskSearchCommand = taskSearchCommand
    }


    /**
     * Evaluate if, based on the command object, the CPA should be calculated.
     * @return true: the CPA needs to be calculated. False otherwise.
     */
    boolean needsCPACalculation() {
        return taskSearchCommand.criticalPathMode == 'Realtime' || taskSearchCommand.cyclicalPath == 1
    }

    /**
     * Based on the CPAResults, obtain a list of unique task ids.
     *
     * @param cpaResults - the CPA results.
     * @return a list of unique task ids.
     */
    List<Long> getCPATasks(CPAResults cpaResults) {
        List<Long> tasksFound = []
        // If the CPA mode is set to Realtime fetch the CPA tasks ids.
        if (taskSearchCommand.criticalPathMode == 'Realtime') {
            tasksFound = cpaResults.tasks.findAll { it.isCriticalPath }*.id
        }
        // If the cyclical path flag is set, add the tasks in the cycles.
        if (taskSearchCommand.cyclicalPath == 1) {
            for (List<TaskVertex> cycle in cpaResults.summary.cycles) {
                tasksFound.addAll(cycle*.taskId)
            }
        }
        // Return a unique list of task ids.
        return tasksFound.unique { Long a, Long b -> a <=> b }

    }

    /**
     * Based on the TaskSearchCommand, the given event and the viewUnpublished flag,
     * construct a SQL query and its required parameters.
     *
     * @param moveEvent
     * @param viewUnpublished
     * @return a map with the query and its parameters.
     */
    Map buildSearchQuery(MoveEvent moveEvent, boolean viewUnpublished) {

        String query = """
            SELECT TASK.asset_comment_id as taskId
            FROM asset_comment TASK
                 LEFT OUTER JOIN api_action API_ACTION on TASK.api_action_id = API_ACTION.id
                 LEFT OUTER JOIN asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id
                 ${joinOwnerSmeId()}
                 ${joinTags()}
            WHERE TASK.comment_type = 'issue'
              ${AND(moveEventOrProject(moveEvent, project))}
              ${AND(taskText())}
              ${AND(environment())}
              ${AND(withAction())}
              ${AND(withActionRequiringTMD())}
              ${AND(taskAssignedToPerson())}
              ${AND(ownerSmeId())}
              ${AND(teamCode())}
              ${AND(withBaseline())}
              ${AND(withViewUnpublished(viewUnpublished))};
        """.stripIndent().trim()

        return [
                query : query,
                params: queryParams
        ]
    }


    String joinTags() {
        String sentence = ''
        if (taskSearchCommand.tagIds) {
            queryParams['tagList'] = taskSearchCommand.tagIds
            Closure<String> innerJoin = { String internalSentence ->
                return "INNER JOIN ($internalSentence) TAG ON (TASK.asset_entity_id = TAG.asset_id)".toString()
            }

            if (taskSearchCommand.tagMatch == 'ANY') {
                sentence = innerJoin('SELECT DISTINCT(TAG_ASSET.asset_id) as asset_id FROM tag_asset TAG_ASSET WHERE TAG_ASSET.tag_id IN (:tagList)')
            } else {
                queryParams['tagListSize'] = taskSearchCommand.tagIds.size()
                sentence = innerJoin('SELECT TAG_ASSET.asset_id FROM tag_asset TAG_ASSET WHERE TAG_ASSET.tag_id IN (:tagList) GROUP BY TAG_ASSET.asset_id HAVING COUNT(*) = :tagListSize')
            }
        }
        return sentence
    }

    String joinOwnerSmeId() {
        String sentence = ''
        if (taskSearchCommand.ownerSmeId) {
            sentence = 'LEFT OUTER JOIN application APPLICATION on ASSET_ENTITY.asset_entity_id = APPLICATION.app_id'
        }
        return sentence
    }

    String taskAssignedToPerson() {
        if (taskSearchCommand.assignedPersonId) {
            queryParams['assignedPersonId'] = taskSearchCommand.assignedPersonId
            return 'AND TASK.assigned_to_id = :assignedPersonId'
        }
    }

    String environment() {
        if (taskSearchCommand.environment) {
            queryParams['environment'] = taskSearchCommand.environment
            return 'AND ASSET_ENTITY.environment = :environment'
        }
    }


    String ownerSmeId() {
        if (taskSearchCommand.ownerSmeId) {
            queryParams['ownerSmeId'] = taskSearchCommand.ownerSmeId
            return 'AND (ASSET_ENTITY.app_owner_id = :ownerSmeId OR APPLICATION.sme_id = :ownerSmeId OR APPLICATION.sme2_id = :ownerSmeId)'
        }
    }

    String teamCode() {
        if (taskSearchCommand.teamCode) {
            queryParams['teamCode'] = taskSearchCommand.teamCode
            return 'AND TASK.role = :teamCode'
        }
    }

    String withAction() {
        if (taskSearchCommand.withActions) {
            return 'AND TASK.api_action_id IS NOT NULL'
        }
    }

    String withActionRequiringTMD() {
        if (taskSearchCommand.withTmdActions) {
            return 'AND API_ACTION.is_remote = true'
        }
    }

    String withBaseline() {
        if (taskSearchCommand.criticalPathMode == 'Baseline') {
            return 'AND TASK.is_critical_path = true'
        }
    }

    String withViewUnpublished(boolean view) {
        if (view) {
            return 'AND TASK.is_published = true'
        }
    }

    String taskText() {
        if (taskSearchCommand.taskText) {
            String comment = "%${taskSearchCommand.taskText}%"
            queryParams['taskText'] = comment
            return 'AND TASK.comment LIKE :taskText'
        }
    }

    String moveEventOrProject(MoveEvent moveEvent, Project project) {
        if (moveEvent) {
            queryParams['eventId'] = moveEvent.id
            return 'AND TASK.move_event_id = :eventId'
        } else {
            queryParams['projectId'] = project.id
            return 'AND TASK.project_id = :projectId'
        }
    }

    String AND(Closure<String> callable) {
        return AND(callable.call())
    }

    String AND(String sentence) {
        return sentence ?: ''
    }
}
