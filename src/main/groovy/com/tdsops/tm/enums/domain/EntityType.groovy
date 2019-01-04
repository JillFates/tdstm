package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
class EntityType {

	public static final String AE      = 'AssetEntity'
	public static final String APP     = 'Application'
	public static final String DB      = 'Database'
	public static final String STORAGE = 'Files'
	public static final String DEVICE = AE
	public static final String LOGICAL_STORAGE = STORAGE

	private static final Map<String, String> categories = [(AE): 'tt_asset', (APP): 'tt_app',
	                                                       (DB): 'tt_database', (STORAGE): 'tt_storage']
	private static final Map<String, String> keysByText = [Server: AE, Storage: STORAGE, Database: DB,
	                                                       Application: APP, Device: DEVICE,
	                                                       'Logical Storage': LOGICAL_STORAGE, Files: STORAGE]

	static final List<String> list = [AE, APP, DB, STORAGE, DEVICE].asImmutable()

	static String getListAsCategory(String type) {
		categories[type]
	}

	static String getKeyByText(String text) {
		keysByText[text]
	}
}
