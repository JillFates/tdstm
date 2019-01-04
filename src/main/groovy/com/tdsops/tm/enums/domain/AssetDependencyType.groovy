package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * The valid options for the AssetDependancy.type property
 */
@CompileStatic
class AssetDependencyType {

	public static final String BACKUP  = 'Backup'
	public static final String BATCH   = 'Batch'
	public static final String DB      = 'DB'
	public static final String FILE    = 'File'
	public static final String HOSTS   = 'Hosts'
	public static final String RUNSON  = 'Runs     On'
	public static final String UNKNOWN = 'Unknown'
	public static final String WEB     = 'Web'

	static final List<String> list = [BACKUP, BATCH, DB, FILE, HOSTS, RUNSON, UNKNOWN, WEB].asImmutable()
}
