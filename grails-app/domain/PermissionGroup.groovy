

public enum PermissionGroup {
	
	NAVIGATION('Navigation'),
	ASSETENTITY('Asset Entity'),
	PROJECT('Project'),
	CONSOLE('Console'),
	ASSETTRACKER('Asset Tracker'),
	CLIENTTEAMS('Client Teams'),
	DASHBOARD('Dashboard'),
	MOVEBUNDLE("Move Bundle"),
	MOVEEVENT("Move Event"),
	MODEL('Model'),
	PERSON('Person'),
	RACKLAYOUT('Rack Layout'),
	ROLETYPE('Role Type'),
	ROOMLAYOUT('room'),
	REPORTS('Reports'),
	USER('User'),
	PARTY('Party'),
	COOKBOOK('Cookbook')
	
	String name
	
	PermissionGroup(String name) {
		 this.name = name
	 }
	
	String getKey(){
		name()
	}
	
	static list() {
		 [NAVIGATION,ASSETENTITY,PROJECT,CONSOLE,ASSETTRACKER,CLIENTTEAMS,DASHBOARD,MOVEBUNDLE,
			 MOVEEVENT,MODEL,PERSON,RACKLAYOUT,ROLETYPE,ROOMLAYOUT,REPORTS,USER,PARTY,COOKBOOK]
		}
	String toString() {
		return name
	}
}

