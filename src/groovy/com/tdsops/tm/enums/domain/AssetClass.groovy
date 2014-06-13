package com.tdsops.tm.enums.domain

import com.tdssrc.grails.EnumUtil

/**
 * Define all the possible classes of assets that are supported by TransitionManager
 */
enum AssetClass {
	APPLICATION,
	DATABASE,
	DEVICE, 
	STORAGE;

	static AssetClass safeValueOf(String key) {
		AssetClass obj
		try {
			obj = key as AssetClass
		} catch (e) { }
		return obj
	}

}