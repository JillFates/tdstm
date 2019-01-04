package net.transitionmanager.command.tag

import net.transitionmanager.command.CommandObject


/**
 * A command object used in creating a AssetTag.
 */

class CreateTagAssetCommand implements CommandObject{
	List<Long> tagIds
	Long assetId
}
