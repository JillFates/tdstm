import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.DataviewApiParamsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.domain.Dataview
import net.transitionmanager.domain.Project
import net.transitionmanager.security.Permission
import net.transitionmanager.service.DataviewService
import net.transitionmanager.service.UserPreferenceService

@Secured('isAuthenticated()')
class DataviewController implements ControllerMethods, PaginationMethods {

	static namespace = 'v1'

	DataviewService dataviewService
	UserPreferenceService userPreferenceService

	/**
	 * <p>This endpoint is used by the DataView framework to retrieve the data.</p>
	 * <p>It receives basically the entire definition of the dataview specification.</p>
	 * <code>
	 *    /tdstm/ws/dataview/1/data
	 * </code>
	 *
	 * <p>It can define pagination</p>
	 * <code>
	 *    /tdstm/ws/dataview/1/data?limit=3&offset=2
	 * </code>
	 *
	 * <p>It can apply filters over the some columns</p>
	 * <code>
	 *    /tdstm/ws/dataview/1/data?limit=3&offset=2&filter=environment%3DProduction%7CDevelopment
	 *    /tdstm/ws/dataview/1/data?limit=3&offset=2&filter=environment%3D%21Production&filter=Name%3DPDV*
	 * </code>
	 * <p>Response contains a JSON structure with records and pagination details</p>
	 *
	 * <code>
	 *	{
	 *     "status": "success",
	 *     "data": {
	 *         "pagination": {
	 *             "offset": 1,
	 *             "max": 2,
	 *             "total": 222
	 *         },
	 *         "asset [
	 *             {
	 *                 "common_assetName": "VMwareVirtualCenter",
	 *                 ....
	 *             },
	 *             {
	 *                 "common_assetName": "VMWareUpdateManager",
	 *                 ....
	 *             }
	 *         ]
	 *     }
	 * }
	 * </code>
	 * @param apiParamsCommand API params validated by a typicall
	 */
	@HasPermission(Permission.AssetView)
	def data() {

		Project project = getProjectForWs()
		DataviewApiParamsCommand apiParamsCommand = populateCommandObject(DataviewApiParamsCommand)

		validateCommandObject(apiParamsCommand)

		Dataview dataview = fetchDomain(Dataview, params)

		Map queryResult = dataviewService.query(project, dataview, apiParamsCommand)
		renderSuccessJson(queryResult)
	}
}