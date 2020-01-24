package net.transitionmanager.command.task

import com.tdssrc.grails.TimeUtil
import grails.databinding.BindUsing
import net.transitionmanager.command.CommandObject

class TaskCommand implements CommandObject {
    Long apiActionId
    Long assetEntity
    String assignedTo // Can be 'AUTO'.
    String attribute
    String category
    String comment
    List deletedPreds
    String displayOption
    @BindUsing({ obj, source ->
        return TimeUtil.parseDate(source['dueDate'])
    })
    Date dueDate
    Long duration
    Integer durationLocked
    String durationScale
    @BindUsing({ obj, source ->
        return TimeUtil.parseDateTime(source['estFinish'])
    })
    Date estFinish
    @BindUsing({ obj, source ->
        return TimeUtil.parseDateTime(source['estStart'])
    })
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
    Integer priority
    String resolution
    String role
    String status
    Integer sendNotification
    List taskDependency
    List taskSuccessor

    static constraints = {
        apiActionId nullable: true
        assetEntity nullable: true
        assignedTo nullable: true
        attribute nullable: true
        category nullable: true
        deletedPreds nullable: true
        displayOption nullable: true
        dueDate nullable: true
        duration nullable: true
        durationLocked nullable: true
        durationScale nullable: true
        estFinish nullable: true
        estStart nullable: true
        hardAssigned nullable: true
        id nullable: true
        instructionsLink nullable: true
        isResolved nullable: true
        manageDependency nullable: true
        moveEvent nullable: true
        mustVerify nullable: true
        note nullable: true
        override nullable: true
        percentageComplete nullable: true
        prevAsset nullable: true
        priority nullable: true
        resolution nullable: true
        role nullable: true
        sendNotification nullable: true
        taskDependency nullable: true
        taskSuccessor nullable: true
    }

}
