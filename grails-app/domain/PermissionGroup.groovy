
public enum PermissionGroup {
	
	NAVIGATION('navigation'),
	ASSET('asset'),
	PROJECT('project'),
	EVENT('event'),
	BUNDLE('bundle'),
	CLIENTCONSOLE('clientConsole'),
	CLIENTTEAMS('clientTeams'),
	DASHBOARD('dashboard'),
	MODEL('model'),
	PERSON('person'),
	PROJECTUTIL('projectUtil'),
	RACKLAYOUTS('rackLayouts'),
	REPORTS('reports'),
	USERLOGIN('userLogin'),
	PMOASSETTRACKING('pmoAssetTracking'),
	SUPERVISORCONSOLE('supervisorConsole'),
    MOVEEVENTNEWS('moveEventNews')
	
	String name
	
	PermissionGroup(String name) {
		 this.name = name
	 }
	
	String getKey(){
		name()
	}
	
	static list() {
		 [NAVIGATION,ASSET,	PROJECT,EVENT,BUNDLE,CLIENTCONSOLE,CLIENTTEAMS,DASHBOARD,MODEL,PERSON,PROJECTUTIL,
	      RACKLAYOUTS,REPORTS,USERLOGIN,PMOASSETTRACKING,SUPERVISORCONSOLE,MOVEEVENTNEWS]
		}
	String toString() {
		return name
	}
}

