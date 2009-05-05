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

	String toString(){
		"$userLogin.id : $preferenceCode : $value"
	}
}
