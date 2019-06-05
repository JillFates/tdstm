package net.transitionmanager.command.dataview

import com.tdsops.tm.enums.domain.AssetClass
import net.transitionmanager.command.CommandObject
import net.transitionmanager.service.dataview.ExtraFilter

/**
 * Extra filter defines a JSON from UI used to extends Dataview results without adding it as columns results.
 */
class DataviewExtraFilterParamsCommand implements CommandObject {

	String property
	String filter

	static constraints = {
		property nullable: false, blank: true
		filter nullable: false, blank: true
	}

}
