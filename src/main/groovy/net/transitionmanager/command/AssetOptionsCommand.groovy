package net.transitionmanager.command

import net.transitionmanager.asset.AssetOptions



class AssetOptionsCommand implements CommandObject {

	AssetOptions.AssetOptionsType type
	String value

	static constraints = {
		value blank: false
	}

}
