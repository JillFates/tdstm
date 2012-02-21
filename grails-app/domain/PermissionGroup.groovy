
public enum PermissionGroup {
	
	NAVIGATION('navigation'),
	PROJECT('project'),
	CLIENTCONSOLE('clientConsole'),
	CLIENTTEAMS('clientTeams'),
	MODEL('model'),
	PERSON('person'),
	RACKLAYOUTS('rackLayout'),
	REPORTS('reports'),
	USER('user')
	
	String name
	
	PermissionGroup(String name) {
		 this.name = name
	 }
	
	String getKey(){
		name()
	}
	
	static list() {
		 [NAVIGATION,PROJECT,CLIENTCONSOLE,CLIENTTEAMS,MODEL,PERSON,RACKLAYOUTS,REPORTS,USER]
		}
	String toString() {
		return name
	}
}

