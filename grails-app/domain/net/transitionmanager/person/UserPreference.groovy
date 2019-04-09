package net.transitionmanager.person

import com.tdssrc.grails.TimeUtil

import groovy.transform.EqualsAndHashCode
import net.transitionmanager.security.UserLogin

@EqualsAndHashCode(includes = ['userLogin', 'preferenceCode'])
class UserPreference implements Serializable {

	UserLogin userLogin
	String    preferenceCode
	String    value

	static mapping = {
		version false
		id composite: ['userLogin', 'preferenceCode'], generator: 'assigned'
		preferenceCode sqlType: 'varchar(50)'
	}

	// TODO BB I think this isn't needed with the current logic in UserPreferenceService
	String getValue() {
//		if ((!value || value == 'undefined') && UserPreferenceService.defaults[preferenceCode]) {
//			value = UserPreferenceService.defaults[preferenceCode]
//		}
		if (!value || value == 'undefined') {
			value = TimeUtil.defaultTimeZone
		}

		return value
	}

	String toString() {
		"$userLoginId : $preferenceCode : $value"
	}
}
