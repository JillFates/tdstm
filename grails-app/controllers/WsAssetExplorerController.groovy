import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods

/**
 * Created by dontiveros
 */
@Secured('isAuthenticated()')
@Slf4j(value='logger', category='grails.app.controllers.WsAssetExplorerController')
class WsAssetExplorerController implements ControllerMethods {

    def listReports() {
		List<Map> mockData = [
			[
				id: 1,
				name: 'Project 1',
				isSystem: true,
				isShared: false,
				schema: null
			]
		]

		renderSuccessJson(mockData)
	}
}

