package net.transitionmanager.command.tag

import grails.validation.Validateable

/**
 * A command object used in creating a AssetTag.
 */
@Validateable
class CreateTagAssetCommand {
	List<Long> tagIds
	Long assetId
}
