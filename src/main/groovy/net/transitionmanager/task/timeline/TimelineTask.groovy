package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.TimeScale
import net.transitionmanager.person.Person
import net.transitionmanager.security.SecurityService

/**
 * DTO class for {@code Task} results in database. It's a preview with an small amount of fields used in CPA calculation.
 * <pre>
 *  tasks = Task.withCriteria { //
 *      resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP) // Creates a Map with columns
 *      ...
 *      projections { //
 *          property('id', 'id')
 *          property('taskNumber', 'taskNumber')
 *          ...
 *
 *      resultTransformer org.hibernate.transform.Transformers.aliasToBean(TimelineTask.class)
 *
 * </pre>
 * @see TimelineService#getEventTasks(net.transitionmanager.project.MoveEvent)
 */
class TimelineTask {

    Long id
    Integer taskNumber
    String comment
    Integer duration
    String status
    Date actStart
    Date statusUpdated
    TimeScale durationScale
    Date estStart
    Date estFinish
    Date actFinish
    Date latestFinish
    Integer slack
    Long whomId
    String firstName
    String lastName
    String team
    Long apiActionId
    String apiActionName
    Long assetEntityId
    String assetName
    String assetType
    AssetClass assetClass
    Boolean isCriticalPath

    List<TimelineDependency> taskDependencies = []

    TimelineTask(Object values) {
        this.id = values[0]
        this.taskNumber = values[1]
        this.comment = values[2]
        this.duration = values[3]
        this.isCriticalPath = values[4]
        this.status = values[5]
        this.actStart = values[6]
        this.statusUpdated = values[7]
        this.durationScale = values[8]
        this.estStart = values[9]
        this.estFinish = values[10]
        this.latestFinish = values[11]
        this.slack = values[12]
        this.team = values[13]
        this.apiActionId = values[14]
        this.apiActionName = values[15]
        this.whomId = values[16]
        this.firstName = values[17]
        this.lastName = values[18]
        this.assetEntityId = values[19]
        this.assetName = values[20]
        this.assetType = values[21]
        this.assetClass = values[22]
    }

    /**
     * Determines if the task is Automatic processed
     * @return
     */
    boolean isAutomatic() {
        return SecurityService.AUTOMATIC_ROLE == team ||
                (firstName && lastName && Person.SYSTEM_USER_AT.firstName == firstName && Person.SYSTEM_USER_AT.lastName == lastName)
    }

    /**
     * Used to determine if the task has an action associated with it
     * @return true if the task has an associated action
     */
    boolean hasAction() {
        return apiActionId != null
    }
}
