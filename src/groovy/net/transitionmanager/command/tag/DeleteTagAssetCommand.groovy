package net.transitionmanager.command.tag

import grails.validation.Validateable

/**
 * A command object used in creating a AssetTag.
 */
@Validateable
class DeleteTagAssetCommand {
	List<Long> ids = []
}
