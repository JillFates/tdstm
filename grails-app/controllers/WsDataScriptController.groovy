import com.tdsops.common.security.spring.HasPermission
import com.tdsops.etl.DataScriptParamsCommand
import com.tdsops.etl.SaveDataScriptParamsCommand
import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.DataScriptService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.dataingestion.ScriptProcessorService

/**
 * Provide the endpoints for working with DataScripts.
 */
@Secured("isAuthenticated()")
@Slf4j(value='logger', category='grails.app.controllers.WsDataScriptController')
class WsDataScriptController implements ControllerMethods{

    private final static DELETE_OK_MESSAGE = "DataScript deleted successfully.";

    DataScriptService dataScriptService
    ScriptProcessorService scriptProcessorService
    SecurityService securityService

    /**
     * Endpoint for creating a new DataScript.
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
     * @param id - id of the DataScript that has to be deleted.
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

    /**
     * Runs the script against the data provided and returns resulting transformed data
     * @return
     */
    def testScript (DataScriptParamsCommand command) {

        if (!command.validate()) {
            renderErrorJson('Invalid parameters')
            return
        }

        Project project = securityService.userCurrentProject

        Map<String, ?> result = scriptProcessorService.testScript(project, command.script, command.fileName)

        renderSuccessJson(result)
    }

    /**
     * Compiles the script and returns any syntax errors
     * @return
     */
    def checkSyntax (DataScriptParamsCommand command) {

        if (!command.validate()) {
            renderErrorJson('Invalid parameters')
            return
        }

        Project project = securityService.userCurrentProject

        Map<String, ?> result = scriptProcessorService.checkSyntax(project, command.script, command.fileName)

        renderSuccessJson(result)
    }

    /**
     * Saves the script to the datascript domain record
     * @return
     */
    def saveScript (SaveDataScriptParamsCommand command) {

        if (!command.validate()) {
            renderErrorJson('Invalid parameters')
            return
        }

        Project project = securityService.userCurrentProject

        DataScript dataScript = DataScript.get(command.id)

        if (dataScript.project != project) {
            securityService.reportViolation("attempted to ACTION dataview ($command.id) not assoc with project")
        }

        DataScript saveScript = scriptProcessorService.saveScript(dataScript, command.script)

        renderSuccessJson()
    }


}
