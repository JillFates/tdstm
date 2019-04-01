package net.transitionmanager.service.domain.listeners

import com.tdsops.common.grails.ApplicationContextHolder
import grails.events.annotation.gorm.Listener
import grails.events.bus.EventBusAware
import net.transitionmanager.license.LicenseActivityTrack
import net.transitionmanager.license.LicensedClient
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.SecurityService
import org.grails.datastore.mapping.engine.event.PreUpdateEvent

class LicensedClientListenerService implements EventBusAware {
	SecurityService securityService

	@Listener(LicensedClient)
	void onLicensedClientPreUpdate(PreUpdateEvent event) {
		LicensedClient licensedClient = event.entityAccess.getEntity()
		//Convert non standard Json Values (String, Number, Boolean) to the toString representation
		Closure toBasicType = { input ->
			switch (input) {
				case String:
				case Number:
				case Boolean: break
				default:
					input = String.valueOf(input)
			}
			input
		}

		/*
		  WARNING: Beware of the IDEs!! somehow IntelliJ doesn't get the 'dirtyPropertyNames'
		  but this is the correct way of doing it!!
		 */
		Collection<String> dirtyProperties = licensedClient.dirtyPropertyNames
		List<Map> changes = []
		dirtyProperties.each { field ->
			Object currentValue = toBasicType(licensedClient[field])
			Object originalValue = toBasicType(licensedClient.getPersistentValue(field))

			if (currentValue != originalValue) {
				changes << [field: field, oldValue: originalValue, newValue: currentValue]
			}
		}

		LicenseActivityTrack licenseActivityTrack
		if (changes) {
			SecurityService securityService = ApplicationContextHolder.getService("securityService")
			UserLogin userLogin = securityService.userLogin
			licenseActivityTrack = new LicenseActivityTrack(userLogin, licensedClient, changes)
			licenseActivityTrack.save()
		}
		licenseActivityTrack

	}

}
