package net.transitionmanager.command.task

import net.transitionmanager.command.CommandObject

class TaskHighlightOptionsCommand implements CommandObject{

    Long eventId
    Integer viewUnpublished

    static constraints = {
        viewUnpublished inList: [0, 1]
    }
}
