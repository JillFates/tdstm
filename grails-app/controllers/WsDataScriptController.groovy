import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.DataScript
import net.transitionmanager.security.Permission
import net.transitionmanager.service.DataScriptService

/**
 * Provide the endpoints for working with DataScripts.
 */
@Secured("isAuthenticated()")
@Slf4j(value='logger', category='grails.app.controllers.WsDatasourceController')
class WsDataScriptController implements ControllerMethods{

    private final static DELETE_OK_MESSAGE = "DataScript deleted successfully.";

    DataScriptService dataScriptService

    /**
     * Endpoint for creating a new datascript.
     */
    @HasPermission(Permission.DataScriptCreate)
    def createDataScript() {
        try {
            DataScript dataScript = dataScriptService.saveOrUpdateDataScript(request.JSON)
            renderSuccessJson([dataScript: dataScript.toMap()])
        } catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Endpoint for updating a DataScript instance.
     *
     * @param id - DataScript id
     */
    @HasPermission(Permission.DataScriptUpdate)
    def updateDataScript(Long id) {
        try {
            DataScript dataScript = dataScriptService.saveOrUpdateDataScript(request.JSON, id)
            renderSuccessJson([dataScript: dataScript.toMap()])
        } catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Endpoint for searching for a particular DataScript.
     *
     * @param id - DataScript id
     * @return
     */
    @HasPermission(Permission.DataScriptView)
    def getDataScript(Long id) {
        try {
            DataScript dataScript = dataScriptService.getDataScript(id)
            renderSuccessJson([dataScript: dataScript.toMap()])
        } catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Endpoint to query if a given name is unique across provider and project. Along with the
     * name to look up, this method also expects:
     *
     * - Provider Id
     * - Data Script Id.
     *
     * The latter is to contemplate the scenario where the user is editing a DataScript and this
     * endpoint is invoked. If the name hasn't changed, it would report the name as not unique.
     */
    @HasPermission(Permission.DataScriptView)
    def validateUniqueName(String name) {
        try {
            Long providerId = NumberUtil.toLong(request.JSON.providerId)
            Long dataScriptId = NumberUtil.toLong(request.JSON.dataScriptId)
            boolean isUnique = dataScriptService.validateUniqueName(name, dataScriptId, providerId)
            renderSuccessJson([isUnique: isUnique])
        } catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Return a list with all the DataScripts for this project. Optionally, a provider Id can
     * be included in the request to narrow down the search.
     *
     * @return
     */
    @HasPermission(Permission.DataScriptView)
    def getDataScripts(){
        try {
            Long providerId = NumberUtil.toLong(request.JSON.providerId)
            List<DataScript> dataScripts = dataScriptService.getDataScripts(providerId)
            renderSuccessJson(dataScripts*.toMap())
        }catch(Exception e) {
            handleException(e, logger)
        }
    }

    /**
     * Delete a given DataScript from the system.
     *
     * @param id DataScript - id
     * @return
     */
    @HasPermission(Permission.DataScriptDelete)
    def deleteDataScript(Long id) {
        try {
            dataScriptService.deleteDataScript(id)
            renderSuccessJson([status: DELETE_OK_MESSAGE] )
        } catch(Exception e) {
            handleException(e, logger)
        }
    }

}
