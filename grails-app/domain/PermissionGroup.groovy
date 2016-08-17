

public enum PermissionGroup {	
	ADMIN('Admin'),
	ASSETENTITY('Asset Entity'),
	ASSETTRACKER('Asset Tracker'),
	CLIENTTEAMS('Client Teams'),
	CONSOLE('Console'),
	COMPANY('COMPANY'),
	COOKBOOK('Cookbook'),
	DASHBOARD('Dashboard'),
	MODEL('Model'),
	MOVEBUNDLE("Move Bundle"),
	MOVEEVENT("Move Event"),
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
	
	String name
	
	PermissionGroup(String name) {
		 this.name = name
	 }
	
	String getKey(){
		name()
	}
	
	static list() {
		 [ 
			ADMIN,
			MODEL,
			MOVEEVENT,
			PARTY, 
			PERSON,
			RACKLAYOUT,
			REPORTS,
			ROLETYPE,
			ROOMLAYOUT,
			TASK,
			USER,
		 	ASSETENTITY,
		 	ASSETTRACKER,
		 	CLIENTTEAMS,
		 	CONSOLE,
			COMPANY,
		 	COOKBOOK,
		 	DASHBOARD,
		 	MOVEBUNDLE,
		 	NAVIGATION,
		 	PROJECT
		]
	}
	
	String toString() {
		return name
	}
}

