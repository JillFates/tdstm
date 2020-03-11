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
     * @return
     */
    boolean viewUnpublishedTasks() {
        return StringUtil.toBoolean(viewUnpublished)
    }
}
