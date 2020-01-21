package net.transitionmanager.task.timeline

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.TimeScale
import net.transitionmanager.person.Person
import net.transitionmanager.security.SecurityService

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
