package tdstm

import com.tdsops.tm.enums.FilenameFormat
import com.tdssrc.grails.FilenameUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.Project

@Secured('isAuthenticated()')
@Slf4j
class WsFilenameController implements ControllerMethods {

	 /**
		* This end point gets called from the View Manager when the user tries to export a View.
		* This generates the required filename in the appropriate format and returns it to the front end.
		* The file name is returned with no file extension as is attached by the View Manager.
		* Also the date is not returned, as it will be attached by the front end.
		*
		* @param viewName  The name of the view to save provided by the user
		* @return  The full file name formatted according to the given rules for a View Manager export file.
		*/
		def viewExportFilename() {
			Project project = securityService.userCurrentProject
			String viewName = request.JSON.viewName
			if (!viewName) {
				return renderFailureJson('Error: viewName cannot be null.')
			}
			Map params = [project: project, viewName: viewName, excludeDate: true]
			return renderSuccessJson(FilenameUtil.buildFilename(FilenameFormat.PROJECT_VIEW_DATE, params))
		}
}
