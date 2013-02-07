package com.tdsops.tm.enums.domain

/**
 * AssetDependencyType - represents the valid options for the AssetDependancy.type property
 */ 
class AssetDependencyType {
	static final String BACKUP='Backup'
	static final String BATCH='Batch'
	static final String DB='DB'
	static final String FILE='File'
	static final String HOSTS='Hosts'
	static final String RUNSON='Runs On'
	static final String UNKNOWN='Unknown'
	static final String WEB='Web'
	static final getList() {
		return [ BACKUP, BATCH, DB, FILE, HOSTS, RUNSON, UNKNOWN, WEB ]
	}
}	
