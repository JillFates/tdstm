import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.service.SequenceService

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
@Slf4j(value='logger', category='grails.app.controllers.WsSequenceController')
class WsSequenceController implements ControllerMethods {

	SequenceService sequenceService

	def retrieveNext() {
		renderAsJson(seq: sequenceService.next(params.int('contextId'), params.name))
	}
}
