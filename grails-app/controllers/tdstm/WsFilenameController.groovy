package tdstm

import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods

@Secured('isAuthenticated()')
@Slf4j
class WsFilenameController implements ControllerMethods {

   /**
    * This end point gets called from the View Manager when the user tries to export a View.
    * This generates the required filename in the appropriate format and returns it to the front end.
    * The file name is returned with no file extension as is attached by the View Manager.
    *
    * @param viewName  The name of the view to save provided by the user
    * @return  The full file name formatted according to the given rules for a View Manager export file.
    */
    def viewExportFilename(String viewName) {
       def params = [viewName:viewName]
       // TODO IF viewName is null
       return renderAsJson(filename:'mock-filename'+viewName+'end')
    }
}
