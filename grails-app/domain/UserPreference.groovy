import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes=["userLogin", "preferenceCode"])
class UserPreference implements Serializable {
	UserLogin userLogin
	String preferenceCode
	String value

	static constraints = {
	}

	static mapping  = {
		version false
		id composite:['userLogin', 'preferenceCode'], generator:'assigned'
		preferenceCode sqlType:'varchar(50)'
	}


	public String getValue(){
		if((!value || value == 'undefined') && UserPreferenceService.defaults[preferenceCode]){
			value = UserPreferenceService.defaults[preferenceCode]
		}

		return value
	}

	String toString(){
		"$userLogin.id : $preferenceCode : $value"
	}
}
