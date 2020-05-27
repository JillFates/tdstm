package net.transitionmanager.command.task

class TaskSearchCommand extends ViewUnpublishedCommand {

    /** null = disabled; 0 = unassigned; >0 = specific person */
    Long assignedPersonId

    /** null = disabled; UNASSIGNED = role == null; specific team/role */
    String teamCode

    /** null = disabled; >0 = specific person */
    Long ownerSmeId

    /** null = disabled; specific environment */
    String environment

    /** A list of tags */
    List<Long> tagIds

    /** should contain ANY|ALL */
    String tagMatch

    /** Options: Baseline, Realtime, default to null = disabled */
    String criticalPathMode

    /** Set to 1 when selected otherwise 0. */
    Integer cyclicalPath

    /** Set to 1 when selected otherwise 0. */
    Integer withActions

    /** Set to 1 when selected otherwise 0. */
    Integer withTmdActions

    /** This is the top level input field. Search were task.comment like '%'text'%' */
    String taskText

    /** This will be set to zero ( 0 ). In the future we may use this to retrieve tasks for other purposes. */
    Integer fullRecord

    static constraints = {
        assignedPersonId nullable: true
        teamCode nullable: true
        ownerSmeId nullable: true
        environment nullable: true
        tagIds nullable: true
        tagMatch nullable: true
        criticalPathMode nullable: true
        cyclicalPath nullable: true, inList: [0, 1]
        withActions nullable: true, inList: [0, 1]
        withTmdActions nullable: true, inList: [0, 1]
        taskText nullable: true
        fullRecord nullable: true
    }

}
