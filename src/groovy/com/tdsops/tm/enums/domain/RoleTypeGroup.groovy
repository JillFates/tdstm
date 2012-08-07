package com.tdsops.tm.enums.domain

enum RoleTypeGroup {
	STAFF('Staff'), SYSTEM('System')

	final String value
	
	RoleTypeGroup(String value) { this.value = value }
	
	static def getList() { return RoleTypeGroup.values()*.value }
	String toString() { value }

}
