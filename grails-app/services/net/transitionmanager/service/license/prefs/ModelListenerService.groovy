package net.transitionmanager.service.license.prefs

import grails.events.annotation.gorm.Listener
import grails.events.bus.EventBusAware
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.Person
import net.transitionmanager.service.SecurityService
import org.grails.datastore.mapping.engine.event.PreInsertEvent

class ModelListenerService implements EventBusAware {
	SecurityService securityService

	@Listener(Model)
	void onSprocketPreInsert(PreInsertEvent event) {

		if (!event.entityAccess.getProperty('createdBy')) {
			Person person = securityService.userLoginPerson

			if (person) {
				event.entityAccess.setProperty('createdBy', person)
			} else {
				log.info('No user found to associate to the model.')
			}
		}

	}

}
