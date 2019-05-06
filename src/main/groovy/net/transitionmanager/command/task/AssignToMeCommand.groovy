package net.transitionmanager.command.task

import com.tdsops.tm.enums.domain.AssetCommentStatus
import net.transitionmanager.command.CommandObject


/**
 * A command object used in creating a Task context.
 */

class AssignToMeCommand implements CommandObject{

	Long id
	String status


	static constraints = {
		status inList: AssetCommentStatus.list
	}
}
