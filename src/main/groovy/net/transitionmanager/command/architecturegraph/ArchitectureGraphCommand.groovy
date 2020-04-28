package net.transitionmanager.command.architecturegraph

import net.transitionmanager.command.CommandObject

/**
 * A command object used for taking in architecture graph parameters
 */

class ArchitectureGraphCommand implements CommandObject {
	Long assetId
	Integer levelsUp   = 0
	Integer levelsDown = 3
	String  mode
	boolean includeCycles

	static constraints = {
		assetId nullable: true
		mode nullable: true
		includeCycles nullable: true
	}
}
