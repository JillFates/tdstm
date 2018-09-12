import com.tdsops.common.security.spring.HasPermission
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.DataviewApiParamsCommand
import net.transitionmanager.command.DataviewUserParamsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.UserPreferenceService


@GrailsCompileStatic
@Secured('isAuthenticated()')
@Slf4j(value = 'logger', category = 'grails.app.controllers.WsDataviewController')
class WsDataviewController implements ControllerMethods, PaginationMethods {

	DataviewService dataviewService
	UserPreferenceService userPreferenceService

	@HasPermission(Permission.UserGeneralAccess)
	def fetch(Long id, DataviewApiParamsCommand apiParamsCommand) {

		Project project = securityService.userCurrentProject

		if (apiParamsCommand.hasErrors()) {
			renderErrorJson('API filtering was invalid')
			return
		}

		Dataview dataview = Dataview.get(id)
		if (!dataview) {
			renderErrorJson('Dataview invalid')
			return
		}

		Map queryResult = dataviewService.query(project, dataview, apiParamsCommand)
		renderSuccessJson(queryResult)
	}
}
