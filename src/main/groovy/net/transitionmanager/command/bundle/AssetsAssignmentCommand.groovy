package net.transitionmanager.command.bundle

import net.transitionmanager.command.CommandObject


/**
 * A command object used in filtering a list of tags.
 *
 * @param moveBundle The id of the move bundle to assign to the assets.
 * @param planStatus The plan status to assign to the assets.
 * @param assets The assets to assign moveBundle,planStatus, and tags to.
 * @param tagIds The tags to apply to the assets.
 */

class AssetsAssignmentCommand implements CommandObject{
	Long moveBundle
	String planStatus
	List<Long> assets   = []
	List<Long> tagIds   = []


	static constraints = {}
}
