package net.transitionmanager.command

import com.tds.asset.AssetOptions
import grails.validation.Validateable

@Validateable
class AssetOptionsCommand implements CommandObject {

	AssetOptions.AssetOptionsType type
	String value

	static constraints = {
		value blank: false
	}

}
