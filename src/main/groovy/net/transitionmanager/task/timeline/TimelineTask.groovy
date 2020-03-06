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
    Integer taskSpec

    List<TimelineDependency> taskDependencies = []

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

    static TimelineTask fromResultSet(Object values) {
        return new TimelineTask(
                id: values[0],
                taskNumber: values[1],
                comment: values[2],
                duration: values[3],
                isCriticalPath: values[4],
                status: values[5],
                actStart: values[6],
                statusUpdated: values[7],
                durationScale: values[8],
                estStart: values[9],
                estFinish: values[10],
                latestFinish: values[11],
                slack: values[12],
                team: values[13],
                apiActionId: values[14],
                apiActionName: values[15],
                whomId: values[16],
                firstName: values[17],
                lastName: values[18],
                assetEntityId: values[19],
                assetName: values[20],
                assetType: values[21],
                assetClass: values[22],
                taskSpec: values[23]
        )
    }
}
