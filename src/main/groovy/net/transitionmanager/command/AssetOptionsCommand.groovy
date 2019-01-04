package net.transitionmanager.command

import com.tds.asset.AssetOptions



class AssetOptionsCommand implements CommandObject {

	AssetOptions.AssetOptionsType type
	String value

	static constraints = {
		value blank: false
	}

}
