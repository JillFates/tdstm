package net.transitionmanager.command

import com.tdsops.tm.enums.domain.AssetClass

class DataviewExtraFilterParamsCommand implements CommandObject {

	String domain
	String filter
	String property

	static constraints = {
		domain nullable: true, inList: AssetClass.domainAssetTypeList
		filter nullable: false
		property nullable: false
	}

}
