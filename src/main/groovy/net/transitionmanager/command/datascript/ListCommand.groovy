package net.transitionmanager.command.datascript

import net.transitionmanager.command.CommandObject
/**
 * The {@code DataviewUserParamsCommand} represents the various parameters that make up the properties
 * that the user can override in the view.
 * It is defined by a nested {@code CommandObject} to refer validations of input data.
 */

class ListCommand implements CommandObject {

	Long providerId
	Boolean useWithAssetActions = false


	static constraints = {
		providerId nullable: true
		useWithAssetActions nullable: true
	}
}
