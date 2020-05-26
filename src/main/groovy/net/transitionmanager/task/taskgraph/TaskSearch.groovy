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
    Collection<Long> getCPATasks(CPAResults cpaResults) {
        Set<Long> tasksFound = [] as Set
        // If the CPA mode is set to Realtime fetch the CPA tasks ids.
        if (taskSearchCommand.criticalPathMode == 'Realtime') {
            tasksFound.addAll(cpaResults.tasks.findAll { it.isCriticalPath }*.id)
        }
        // If the cyclical path flag is set, add the tasks in the cycles.
        if (taskSearchCommand.cyclicalPath == 1) {
            for (List<TaskVertex> cycle in cpaResults.summary.cycles) {
                tasksFound.addAll(cycle*.taskId)
            }
        }
        // Return a unique list of task ids.
        return tasksFound
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

        TaskSearchQueryBuilder queryBuilder = new TaskSearchQueryBuilder()

        queryBuilder.with {
            select 'TASK.asset_comment_id as taskId'
            from 'asset_comment TASK'
            leftOuterJoin 'api_action API_ACTION on TASK.api_action_id = API_ACTION.id'
            leftOuterJoin 'asset_entity ASSET_ENTITY on TASK.asset_entity_id = ASSET_ENTITY.asset_entity_id'
            leftOuterJoin application()
            innerJoin assetTags(), 'TAG ON (TASK.asset_entity_id = TAG.asset_id)'
            where "TASK.comment_type = 'issue'"
            and moveEventOrProject(moveEvent, project)
            and taskText()
            and environment()
            and withAction()
            and withActionRequiringTMD()
            and taskAssignedToPerson()
            and ownerSmeId()
            and teamCode()
            and withBaseline()
            and withViewUnpublished(viewUnpublished)
        }

        return [
                query : queryBuilder.build(),
                params: queryParams
        ]
    }
    /**
     * Determines if it is necessary to JOIN {@link net.transitionmanager.tag.Tag} domain
     * for filtering results.
     * @return
     */
    String assetTags() {
        if (taskSearchCommand.tagIds) {
            queryParams['tagList'] = taskSearchCommand.tagIds
            if (taskSearchCommand.tagMatch == 'ANY') {
                return '(SELECT DISTINCT(TAG_ASSET.asset_id) as asset_id FROM tag_asset TAG_ASSET WHERE TAG_ASSET.tag_id IN (:tagList))'
            } else {
                queryParams['tagListSize'] = taskSearchCommand.tagIds.size()
                return '(SELECT TAG_ASSET.asset_id FROM tag_asset TAG_ASSET WHERE TAG_ASSET.tag_id IN (:tagList) GROUP BY TAG_ASSET.asset_id HAVING COUNT(*) = :tagListSize)'
            }
        }
    }

    /**
     * Determines if it is necessary to JOIN {@link net.transitionmanager.Application} domain
     * for filtering results.
     * @return
     */
    String application() {
        String sentence = ''
        if (taskSearchCommand.ownerSmeId) {
            sentence = 'application APPLICATION on ASSET_ENTITY.asset_entity_id = APPLICATION.app_id'
        }
        return sentence
    }

    /**
     * Defines filtering by {@link net.transitionmanager.task.AssetComment#assignedTo} field.
     * @return an HQL sentence or null
     */
    String taskAssignedToPerson() {

        switch (taskSearchCommand.assignedPersonId){
            case null:
                return null
            case 0:
                return 'TASK.assigned_to_id is null'
            default:
                queryParams['assignedPersonId'] = taskSearchCommand.assignedPersonId
                return 'TASK.assigned_to_id = :assignedPersonId'
        }
    }

    /**
     * Defines filtering by {@link net.transitionmanager.asset.AssetEntity#environment} field.
     * @return an HQL sentence or null
     */
    String environment() {
        if (taskSearchCommand.environment) {
            queryParams['environment'] = taskSearchCommand.environment
            return 'ASSET_ENTITY.environment = :environment'
        }
    }

    /**
     * Defines filtering by
     * {@link net.transitionmanager.asset.AssetEntity#appOwner} field,
     * or {@link net.transitionmanager.asset.Application#sme}
     * or {@link net.transitionmanager.asset.Application#sme2}
     * @return an HQL sentence or null
     */
    String ownerSmeId() {
        if (taskSearchCommand.ownerSmeId) {
            queryParams['ownerSmeId'] = taskSearchCommand.ownerSmeId
            return '(ASSET_ENTITY.app_owner_id = :ownerSmeId OR APPLICATION.sme_id = :ownerSmeId OR APPLICATION.sme2_id = :ownerSmeId)'
        }
    }

    /**
     * Defines filtering by {@link net.transitionmanager.task.AssetComment#role} field.
     * @return an HQL sentence or null
     */
    String teamCode() {

        switch (taskSearchCommand.teamCode){
            case null:
                return null
            case 'UNASSIGNED':
                return 'TASK.role is NULL'
            default:
                queryParams['teamCode'] = taskSearchCommand.teamCode
                return 'TASK.role = :teamCode'
        }
    }

    /**
     * Defines filtering by {@link net.transitionmanager.task.AssetComment#apiAction} field.
     * @return an HQL sentence or null
     */
    String withAction() {
        if (taskSearchCommand.withActions) {
            return 'TASK.api_action_id IS NOT NULL'
        }
    }

    /**
     * Defines filtering by {@link net.transitionmanager.action.ApiAction#isRemote} field.
     * @return an HQL sentence or null
     */
    String withActionRequiringTMD() {
        if (taskSearchCommand.withTmdActions) {
            return 'API_ACTION.is_remote = true'
        }
    }

    /**
     * Defines filtering by CPA Baseline using
     * {@link net.transitionmanager.task.Task#isCriticalPath} field.
     * @return an HQL sentence or null
     */
    String withBaseline() {
        if (taskSearchCommand.criticalPathMode == 'Baseline') {
            return 'TASK.is_critical_path = true'
        }
    }

    /**
     * Defines filtering by {@link net.transitionmanager.task.AssetComment#isPublished} field.
     * @return an HQL sentence or null
     */
    String withViewUnpublished(boolean view) {
        if (view) {
            return 'TASK.is_published = true'
        }
    }

    /**
     * Defines filtering by {@link net.transitionmanager.task.AssetComment#comment} field.
     * @return an HQL sentence or null
     */
    String taskText() {
        if (taskSearchCommand.taskText) {
            String comment = "%${taskSearchCommand.taskText}%"
            queryParams['taskText'] = comment
            return 'TASK.comment LIKE :taskText'
        }
    }

    String moveEventOrProject(MoveEvent moveEvent, Project project) {
        if (moveEvent) {
            queryParams['eventId'] = moveEvent.id
            return 'TASK.move_event_id = :eventId'
        } else {
            queryParams['projectId'] = project.id
            return 'TASK.project_id = :projectId'
        }
    }

    String AND(Closure<String> callable) {
        return AND(callable.call())
    }

    String AND(String sentence) {
        return sentence ?: ''
    }
}
