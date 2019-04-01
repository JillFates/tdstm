package net.transitionmanager.move

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.GormUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.ProjectService

@Secured('isAuthenticated()')
class EventController implements ControllerMethods {

	ProjectService projectService
	MoveEventService moveEventService

	/**
	 * List all available Move Events for the user's current project including more detailed list of fields.
	 */
	@HasPermission(Permission.EventView)
	def index() {
		Project project = getProjectForWs()
		List<MoveEvent> events = moveEventService.listMoveEvents(project)
		List<Map> eventsMap = events.collect { event ->
			GormUtil.domainObjectToMap(event, MoveEvent.DETAILED_LIST_FIELDS, null, false)
		}
		renderSuccessJson(eventsMap)
	}

}
