package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum PermissionGroup {

	ADMIN('Admin'),
	ASSETENTITY('Asset Entity'),
	ASSETTRACKER('Asset Tracker'),
	CLIENTTEAMS('Client Teams'),
	CONSOLE('Console'),
	COMPANY('COMPANY'),
	COOKBOOK('Cookbook'),
	DASHBOARD('Dashboard'),
	MODEL('Model'),
	MOVEBUNDLE('Move Bundle'),
	MOVEEVENT('Move Event'),
	NAVIGATION('Navigation'),
	PARTY('Party'),
	PERSON('Person'),
	PROJECT('Project'),
	RACKLAYOUT('Rack Layout'),
	REPORTS('Reports'),
	ROLETYPE('Role Type'),
	ROOMLAYOUT('room'),
	TASK('Task'),
	USER('User')

	final String name

	private PermissionGroup(String label) {
		name = label
	}

	String getKey() { name() }

	String toString() { name }
}
