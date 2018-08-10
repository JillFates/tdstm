import com.tdsops.common.security.spring.HasPermission
import com.tdsops.etl.DataScriptValidateScriptCommand
import com.tdssrc.grails.NumberUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.command.DataScriptNameValidationCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.DataScriptService
import net.transitionmanager.service.FileSystemService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.dataingestion.ScriptProcessorService

/**
 * Provide the endpoints for working with DataScripts.
 */
@Secured("isAuthenticated()")
@Slf4j(value = 'logger', category = 'grails.app.controllers.WsDataScriptController')
class WsDataScriptController implements ControllerMethods, PaginationMethods {

    private final static DELETE_OK_MESSAGE = "DataScript deleted successfully.";

    DataScriptService dataScriptService
    ScriptProcessorService scriptProcessorService
    FileSystemService fileSystemService

    /**
     * Endpoint for creating a new DataScript.
     */
    @HasPermission(Permission.ETLScriptCreate)
    def createDataScript () {
        DataScript dataScript = dataScriptService.saveOrUpdateDataScript(request.JSON)
        renderSuccessJson([dataScript: dataScript.toMap()])
    }

    /**
     * Endpoint for updating a DataScript instance.
     *
     * @param id - DataScript id
     */
    @HasPermission(Permission.ETLScriptUpdate)
    def updateDataScript (Long id) {
        DataScript dataScript = dataScriptService.saveOrUpdateDataScript(request.JSON, id)
        renderSuccessJson([dataScript: dataScript.toMap()])
    }

    /**
     * Saves the script to the datascript domain record
     * @return
     */
    @HasPermission(Permission.ETLScriptUpdate)
    def saveScript () {
        Long id = request.JSON.id
        DataScript dataScript = dataScriptService.saveScript(id, request.JSON.script)
        renderSuccessJson(dataScript: dataScript.toMap())
    }

    /**
     * Endpoint for searching for a particular DataScript.
     *
     * @param id - DataScript id
     * @return
     */
    @HasPermission(Permission.ETLScriptView)
    def getDataScript (Long id) {
        DataScript dataScript = dataScriptService.getDataScript(id)
        renderSuccessJson([dataScript: dataScript.toMap()])
    }

    /**
     * Endpoint to query if a given name is unique across provider and project. Along with the
     * name to look up, this method also expects:
     *
     * - Provider Id
     * - DataScript Id.
     *
     * The latter is to contemplate the scenario where the user is editing a DataScript and this
     * endpoint is invoked. If the name hasn't changed, it would report the name as not unique.
     */
    @HasPermission(Permission.ETLScriptView)
    def validateUniqueName (String name) {
        DataScriptNameValidationCommand cmd = populateCommandObject(DataScriptNameValidationCommand)
        boolean isUnique = dataScriptService.validateUniqueName(cmd)
        renderSuccessJson([isUnique: isUnique])
    }

    /**
     * Return a list with all the DataScripts for this project. Optionally, a provider Id can
     * be included in the request to narrow down the search.
     *
     * @return
     */
    @HasPermission(Permission.ETLScriptView)
    def list() {
        Long providerId = NumberUtil.toLong(request.JSON.providerId)
        List<DataScript> dataScripts = dataScriptService.getDataScripts(providerId)?.sort {it.name}
        renderSuccessJson(dataScripts*.toMap(DataScript.BASE_INFO))
    }

    /**
     * Delete a given DataScript from the system.
     *
     * @param id - id of the DataScript that has to be deleted.
     * @return
     */
    @HasPermission(Permission.ETLScriptDelete)
    def deleteDataScript (Long id) {
        dataScriptService.deleteDataScript(id)
        renderSuccessJson([status: DELETE_OK_MESSAGE])
    }

    /**
     * Determine if the given DataScript can be safely deleted.
     * @param id
     * @return
     */
    @HasPermission(Permission.ETLScriptDelete)
    def validateDelete(Long id) {
        renderSuccessJson(dataScriptService.checkDataScriptReferences(id))
    }


    /**
     * Runs the script against the data provided and returns resulting transformed data
     * @return
     */
    @HasPermission(Permission.ETLScriptUpdate)
    def initiateTestScript(DataScriptValidateScriptCommand command) {

        if (!command.validate()) {
            throw new InvalidParamException('Invalid parameters')
        }

        Project project = securityService.getUserCurrentProjectOrException()

        Map<String, ?> result = scriptProcessorService.scheduleTestScript(project, command)

        renderSuccessJson(result)
    }

    /**
     * Compiles the script and returns any syntax errors
     * @return
     */
    @HasPermission(Permission.ETLScriptUpdate)
    def checkSyntax (DataScriptValidateScriptCommand command) {

        if (!command.validate()) {
            throw new InvalidParamException('Invalid parameters')
        }

        String fullName = fileSystemService.getTemporaryFullFilename(command.filename)

        if (!fullName) {
            throw new InvalidParamException('Invalid file name')
        }

        Project project = securityService.getUserCurrentProjectOrException()

        Map<String, ?> result = scriptProcessorService.checkSyntax(project, command.script, fullName)

        renderSuccessJson(result)
    }

    /**
     * Retrieve  sample data from uploaded file (JSON, CSV, EXCEL). For EXCEL files, it has it has the
     * ability to return a maximum amount of rows by passing the optional parameter <code>rows</code>, it works
     * the same way as other pagination endpoints.
     * @param filename - sample data temporary filename uploaded
     * @param rows - maximum amount of rows to return
     * @return
     */
    @HasPermission(Permission.ETLScriptCreate)
    def sampleData (Long id, String filename, String originalFileName) {

        // Long id = params.id?.toLong()
        // String originalFileName = params.originalFileName

        String rootNode = params.rootNode ?: ''

        Long maxRows = paginationMaxRowValue() ?: 25
        Map jsonMap = dataScriptService.parseDataFromFile(id, originalFileName, filename, rootNode, maxRows)
        renderSuccessJson(jsonMap)
    }

}
