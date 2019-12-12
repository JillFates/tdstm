package net.transitionmanager.command.task

import net.transitionmanager.command.CommandObject

class TaskCommand implements CommandObject {
    Long apiActionId
    Long assetEntity
    String assignedTo // Can be 'AUTO'.
    String attribute
    String category
    String comment
//    Long commentFromId
    String currentStatus
    List deletedPreds
    String displayOption
    Date dueDate
    Long duration
    Integer durationLocked
    String durationScale
    Date estFinish
    Date estStart
    Integer hardAssigned
    Long id
    String instructionsLink
    Integer isResolved
    Integer manageDependency
    Long moveEvent
    Integer mustVerify
    String note
    Integer override
    Integer percentageComplete
    Integer prevAsset
    String priority
    String resolution
    String role
    String status
    Integer sendNotification
    List taskDependency
    List taskSuccessor

}
