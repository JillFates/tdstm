package net.transitionmanager.command.task

import com.tdssrc.grails.StringUtil
import net.transitionmanager.command.CommandObject

class ViewUnpublishedCommand implements CommandObject{

    Long eventId
    Integer viewUnpublished

    static constraints = {
        viewUnpublished inList: [0, 1]
    }

    /**
     * Convert the viewUnpublished from 0 or 1 into the appropriate boolean.
     * @param hasViewUnpublishedPermission - whether or not the user has permission to view unpublished tasks.
     * @return whether or not unpublished tasks should be included (considering the parameter in the request and the permission).
     */
    boolean viewUnpublishedTasks(boolean hasViewUnpublishedPermission) {
        return hasViewUnpublishedPermission && StringUtil.toBoolean(viewUnpublished)
    }
}
