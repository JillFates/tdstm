
public enum PermissionGroup {
	
	NAVIGATION('navigation'),
	ASSET('asset'),
	PROJECT('project'),
	EVENT('event'),
	BUNDLE('bundle'),
	CLIENTCONSOLEBULKEDIT('clientConsoleBulkEdit'),
	CLIENTCONSOLEALL('clientConsoleAll'),
	CLIENTCONSOLECOMMENT('clientConsoleComment'),
	CLIENTCONSOLECHECKBOX('clientConsoleCheckBox'),
	CLIENTTEAMSLIST('clientTeams'),
	VALIDATEMODEL('validateModel'),
	ADDPERSON('addPerson'),
	EDITASSETINRACKLAYOUTS('EditAssetInRackLayout'),
	REPORTS('reports'),
	USERLOGIN('userLogin')
	
	String name
	
	PermissionGroup(String name) {
		 this.name = name
	 }
	
	String getKey(){
		name()
	}
	
	static list() {
		 [NAVIGATION,ASSET,	PROJECT,EVENT,BUNDLE,CLIENTCONSOLEBULKEDIT,CLIENTCONSOLEALL,CLIENTCONSOLECOMMENT,CLIENTCONSOLECHECKBOX,CLIENTTEAMSLIST,VALIDATEMODEL,ADDPERSON,
	      EDITASSETINRACKLAYOUTS,REPORTS,USERLOGIN]
		}
	String toString() {
		return name
	}
}

