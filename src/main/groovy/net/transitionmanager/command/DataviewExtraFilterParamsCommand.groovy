package net.transitionmanager.command

import com.tdsops.tm.enums.domain.AssetClass

/**
 * Extra filter defines a JSON from UI used to extends Dataview results without adding it as columns results.
 */
class DataviewExtraFilterParamsCommand implements CommandObject {

	String domain
	String filter
	String property

	static constraints = {
		domain nullable: true, inList: AssetClass.domainAssetTypeList
		filter nullable: false, blank: true
		property nullable: false, blank: true
	}

}
