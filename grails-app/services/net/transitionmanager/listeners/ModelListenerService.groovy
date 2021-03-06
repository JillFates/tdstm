package net.transitionmanager.listeners

import grails.events.annotation.gorm.Listener
import grails.events.bus.EventBusAware
import net.transitionmanager.model.Model
import net.transitionmanager.person.Person
import net.transitionmanager.security.SecurityService
import org.grails.datastore.mapping.engine.event.PreInsertEvent

class ModelListenerService implements EventBusAware {
	SecurityService securityService

	@Listener(Model)
	void onModelPreInsert(PreInsertEvent event) {

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
