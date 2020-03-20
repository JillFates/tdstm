package com.tdsops.tm.enums.domain;

import groovy.transform.CompileStatic;

@CompileStatic
enum DataViewMap {
	ASSETS(1),
	DATABASES(2),
	DEVICES(3),
	SERVERS(4),
	STORAGE_PHYSICAL(5),
	STORAGE_VIRTUAL(6),
	APPLICATIONS(7)


	private final Integer id

	DataViewMap(Integer id) {
		this.id = id
	}

}
