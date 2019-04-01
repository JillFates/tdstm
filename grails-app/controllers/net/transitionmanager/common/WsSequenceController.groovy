package net.transitionmanager.common

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.common.SequenceService

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class WsSequenceController implements ControllerMethods {

	SequenceService sequenceService

	@HasPermission(Permission.SequenceGetNext)
	def retrieveNext() {
		renderAsJson(seq: sequenceService.next(params.int('contextId'), params.name))
	}
}
