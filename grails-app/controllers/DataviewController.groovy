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
	 *
	 *
	 * .../tdstm/ws/dataview/1/data?limit=3&offset=2
	 * .../tdstm/ws/dataview/1/data?limit=3&offset=2&filter=environment=Production|Development
	 * .../tdstm/ws/dataview/1/data?limit=3&offset=2&filter=environment=!Production&filter=Name=PDV*
	 *
	 * @param id
	 * @param apiParamsCommand
	 * @return
	 */
	@HasPermission(Permission.AssetView)
	def data(Long id) {

		Project project = getProjectForWs()
		DataviewApiParamsCommand apiParamsCommand = populateCommandObject(DataviewApiParamsCommand)
		// 09/21/2018 dcorrea: It's necessary to convert command.filter in to command.filterParams
		apiParamsCommand.validate()

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
/*
{
   "offset":0,
   "limit":1000,
   "sortDomain":"common",
   "sortProperty":"id",
   "sortOrder":"a",
   "filters":{
      "domains":[
         "common",
         "application",
         "device"
      ],
      "columns":[
         {
            "domain":"common",
            "property":"id",
            "width":200,
            "locked":false,
            "edit":false,
            "label":"Id",
            "filter":""
         },
         {
            "domain":"common",
            "property":"environment",
            "width":200,
            "locked":false,
            "edit":false,
            "label":"Environment",
            "filter":"Production|Development"
         },
         {
            "domain":"common",
            "property":"assetName",
            "width":200,
            "locked":false,
            "edit":false,
            "label":"Name",
            "filter":""
         },
         {
            "domain":"application",
            "property":"sme",
            "width":200,
            "locked":false,
            "edit":false,
            "label":"SME1",
            "filter":""
         }
      ]
   }
}
 */
