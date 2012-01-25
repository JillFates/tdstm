
public enum PermissionGroup {
	
	NAVIGATION('navigation'),
	ASSET('asset'),
	PROJECT('project'),
	EVENT('event'),
	BUNDLE('bundle')
	
	
	String name
	
	PermissionGroup(String name) {
		 this.name = name
	 }
	
	String getKey(){
		name()
	}
	
	static list() {
		 [NAVIGATION,ASSET,	PROJECT,EVENT,BUNDLE]
		}
	String toString() {
		return name
	}
}

