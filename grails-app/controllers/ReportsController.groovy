import com.tds.asset.Application
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.FilenameFormat
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.ProjectStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.FilenameUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import net.transitionmanager.command.metricdefinition.MetricDefinitionsCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.AppMoveEvent
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventNews
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Workflow
import net.transitionmanager.domain.WorkflowTransition
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.MetricReportingService
import net.transitionmanager.service.MoveBundleService
import net.transitionmanager.service.MoveEventService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.ReportsService
import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang.math.NumberUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Font
import org.springframework.jdbc.core.JdbcTemplate
import org.hibernate.transform.Transformers
import java.text.DateFormat

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ReportsController implements ControllerMethods {

	AssetEntityService assetEntityService
	ControllerService controllerService
	CustomDomainService customDomainService
	JdbcTemplate jdbcTemplate
	MoveBundleService moveBundleService
	MoveEventService moveEventService
	PartyRelationshipService partyRelationshipService
	ProjectService projectService
	ReportsService reportsService
	UserPreferenceService userPreferenceService
	MetricReportingService metricReportingService


	// Generate Report Dialog
	def retrieveBundleListForReportDialog() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		List<MoveBundle> moveBundles = MoveBundle.findAllByProject(project)
		boolean browserTest = !request.getHeader('User-Agent').contains('MSIE')

		Map model = [moveBundleInstanceList: moveBundles, projectInstance: project]
		String view
		switch (params.reportId) {
			case 'Home':
				view = 'home'
				break
			case 'cart Asset':
				view = 'cartAssetReport'
				break
			case 'Issue Report':
				view = 'issueReport'
				break
			case 'Task Report':
				view = 'taskReport'
				model.moveEventInstanceList = MoveEvent.findAllByProject(project, [sort: 'name'])
				model.viewUnpublished = userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) == 'true' ? '1' : '0'
				model.remove 'moveBundleInstanceList'
				break
			case 'Transportation Asset List':
				view = 'transportationAssetReport'
				break
			case 'Asset Tag':
				view = 'assetTagLabel'
				model.browserTest =  browserTest
				break
			case 'CablingQA':
				view = 'cablingQAReport'
				model.type = 'QA'
				break
			case 'CablingConflict':
				view = 'cablingQAReport'
				model.type = 'conflict'
				break
			case 'CablingData':
				view = 'cablingData'
				break
			default:
				render 'An invalid report was specified'
				return
		}

		render view: view, model: model
	}

	def cartAssetReport() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		def reportName = params.reportName
		String tzId = userPreferenceService.timeZone
		def sortOrder = params.sortType
		def teamPartyGroup
		def partyGroupInstance = PartyGroup.get(project.id)
		// if no moveBundle was selected
		if (params.moveBundle == "null") {
			flash.message = " Please Select Bundles. "
			if(reportName == 'cartAsset') {
				redirect(action: 'retrieveBundleListForReportDialog', params: [reportId: 'cart Asset'])
			} else {
				redirect(action: 'retrieveBundleListForReportDialog', params: [reportId: 'Transportation Asset List'])
			}
		} else {
			MoveBundle moveBundle = MoveBundle.get(params.moveBundle)
			def reportFields = []
			def bundleName = "All Bundles"
			def assetEntityList
			//if moveBundle is selected (single moveBundle)
			if (moveBundle) {
				bundleName = moveBundle?.name
				assetEntityList = AssetEntity.executeQuery('''
					from AssetEntity
					where moveBundle=?
					order by moveBundle, cart, shelf
				''', [moveBundle])
			}
			//All Bundles Selected
			else {
				assetEntityList = AssetEntity.executeQuery('''
					from AssetEntity
					where project=?
					  and moveBundle != null
					order by moveBundle, cart, shelf
				''', [project])
			}

			def currDate = new Date()
			DateFormat userDateFormatter = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)
			//Source AssetList
			if( assetEntityList) {
				assetEntityList.each { asset ->
					MoveBundle bundle
					if (asset.moveBundle) {
						bundle = MoveBundle.get(asset.moveBundle.id)
					}
					teamPartyGroup = ProjectTeam.findByMoveBundleAndTeamCode(bundle, reportName == 'cartAsset' ? 'Logistics' : 'Transport')
					def assetCommentList = AssetComment.findAllByAssetEntity(asset)
					String assetCommentString = assetCommentList.collect { it.comment + '\n' }.join('')
					String cartShelf = (asset.cart ?: "") + "/" + (asset.shelf ?: "")
					if (cartShelf == "/") {
						cartShelf = ""
					}

					// sort options for reportFields
					String roomTagSort = (asset.sourceRoomName ?: "") + " " + (asset.sourceRackName ?: "") + " " + (asset?.model?.usize ?: "")
					String truckTagSort = (asset.truck ?: "") + " " + (asset.cart ?: "") + " " + (asset.shelf ?: "")

					def teamMembers = []
					if(teamPartyGroup){
						teamMembers = partyRelationshipService.getTeamMemberNames(teamPartyGroup)
					}
					reportFields << [assetName: asset.assetName, model: asset.model?.toString(),
					                 sourceTargetPos: (teamPartyGroup?.currentLocation ?: "") + "(source/ unracking)",
					                 cart: cartShelf, shelf: asset.shelf, source_team_id: teamPartyGroup?.id,
					                 move_bundle_id: asset?.moveBundle?.id, dlocation: asset.rackSource?.location ?: '',
					                 projectName: partyGroupInstance?.name, startAt: project.startDate,
					                 completedAt: project.completionDate, bundleName: moveBundle?.name,
					                 teamName: teamPartyGroup?.teamCode ? teamPartyGroup?.name + " - " + teamMembers : "",
					                 location: "Source Team", truck: asset.truck,  room: asset.sourceRoomName,
					                 instructions: assetCommentString, roomTagSort: roomTagSort, truckTagSort: truckTagSort,
					                 assetTagSort: asset.assetTag ?: "", sourcetargetLoc: "s", usize: asset?.model?.usize,
					                 timezone: tzId, rptTime: currDate, userDateFormatter: userDateFormatter]
				}
			}

			//No Assets were found for selected moveBundle,team and Location
			if (!reportFields) {
				flash.message = " No Assets Were found for  selected values  "
				redirect(action:'retrieveBundleListForReportDialog',
				         params: [reportId: reportName == 'cartAsset' ? 'cart Asset' : 'Transportation Asset List'])
			}else {
				//sort reportFields by selected sort options
				if ( sortOrder ) {
					if ( sortOrder == "TEAM_ASSET" ) {
						reportFields.sort{ it.teamTagSort }
					} else if ( sortOrder == "ROOM_RACK_USIZE" ) {
						reportFields.sort{ it.roomTagSort }
					} else if ( sortOrder == "TRUCK_CART_SHELF" ) {
						reportFields.sort{ it.truckTagSort }
					} else if ( sortOrder == "ASSET_TAG" ) {
						reportFields.sort{ it.assetTagSort }
					}
				}

				def name = reportName == "cartAsset" ? "LogisticsTeam" : "TransportTeam"
				String filename = (name + '-' + project.name + '-' + bundleName).replace(" ", "_")

				chain(controller: 'jasper', action: 'index', model: [data: reportFields],
						params: ["_format": "PDF", "_name": filename,"_file":params._file])
			}
		}
	}

	/*
	 * Generate Issue Report
	 */
	def issueReport() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		def partyGroupInstance = PartyGroup.get(project.id)
		def bundleNames = ""
		def reportFields = []
		def resolvedInfoInclude
		def sortBy = params.reportSort
		def comment = params.commentInfo
		def reportFormat = params._format

		if( params.reportSort == "sourceLocation" ) {
			sortBy = "roomSource.roomName,ac.assetEntity.rackSource.tag,ac.assetEntity.sourceRackPosition"
		}else if( params.reportSort == "targetLocation" ){
			sortBy = "roomTarget.roomName,ac.assetEntity.rackTarget.tag,ac.assetEntity.targetRackPosition"
		}
		if( params.reportResolveInfo == "false" ){
			resolvedInfoInclude = "Resolved issues were not included"
		}
		String moveBundles = params.moveBundle
		moveBundles = moveBundles.replace("[","('").replace(",]","')").replace(",","','")
		if(params.moveBundle == "null") {
			flash.message = " Please Select Bundles. "
			redirect( action:'retrieveBundleListForReportDialog', params:[reportId: 'Issue Report'] )
			return
		}

		String bundleName = "All Bundles"
		def commentType = "('issue','comment')"
		if(comment == "false"){
			commentType = "('issue')"
		}
		def commentsQuery = new StringBuilder("from AssetComment ac where ac.commentType in $commentType ")

		if( moveBundles.size() > 4 ){
			commentsQuery.append(" and ac.assetEntity.id in (select ae.id from AssetEntity ae where ae.moveBundle.id in $moveBundles ) order by ac.assetEntity.$sortBy ")
			bundleNames = MoveBundle.findAll("from MoveBundle where id in $moveBundles").name.toString()
			bundleName = bundleNames
		}else {
			commentsQuery.append(" and ac.assetEntity.id in (select ae.id from AssetEntity ae where ae.project.id = $project.id ) order by ac.assetEntity.$sortBy ")
			bundleNames = "All"
		}

		def assetCommentList = AssetComment.findAll( commentsQuery.toString() )

		String tzId = userPreferenceService.timeZone
		def currDate = new Date()
		DateFormat userDateFormatter = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)
		assetCommentList.each { ac ->
			def sourceTargetRoom = (ac?.assetEntity?.sourceRoomName ?: "--")+
								"/"+(ac?.assetEntity?.sourceRackName ?: "--")+
								"/"+(ac?.assetEntity?.sourceRackPosition ?: "--")+"\n"+
								(ac?.assetEntity?.targetRoomName ?: "--")+"/"+
								(ac?.assetEntity?.targetRackName ?: "--")+"/"+
								(ac?.assetEntity?.targetRackPosition ?: "--")
			if( params.reportResolveInfo == "true" || !ac.isResolved() ) {
				reportFields <<['assetName':ac?.assetEntity?.assetName,
				                'assetTag':ac?.assetEntity?.assetTag,
				                'moveBundle' :ac?.assetEntity?.moveBundle?.name,
								'sourceTargetRoom':sourceTargetRoom,
								'commentType':ac.commentType == 'issue' ? 'Task' : ac.commentType,
								'model':(ac?.assetEntity?.manufacturer ? ac?.assetEntity?.manufacturer?.toString() : "") +
										" " + (ac?.assetEntity?.model ? ac?.assetEntity?.model : "" ),
								'occuredAt': ac.dateCreated,
								'createdBy':ac?.createdBy?.firstName+" "+ac?.createdBy?.lastName,
								'owner':ac?.assignedTo ? ac?.assignedTo?.firstName+" "+ac?.assignedTo?.lastName : '',
								'issue':ac?.comment, 'bundleNames':bundleNames,'projectName':partyGroupInstance?.name,
								'clientName':project?.client?.name,"resolvedInfoInclude":resolvedInfoInclude,
								'timezone':tzId, "rptTime": TimeUtil.formatDate(currDate), userDateFormatter: userDateFormatter,
								'previousNote':WebUtil.listAsMultiValueString(ac.notes) ]
			}
			if( params.reportResolveInfo == "true" && ac.isResolved() ) {
				reportFields <<['assetName':null, 'assetTag':null, 'moveBundle' :null,'sourceTargetRoom':null,'model':null,
								'commentType':ac.commentType == 'issue' ? 'Task' : ac.commentType,
								'occuredAt': ac.dateResolved,
								'createdBy':ac?.resolvedBy?.firstName+" "+ac?.resolvedBy?.lastName,
								'owner':ac?.assignedTo ? ac?.assignedTo?.firstName+" "+ac?.assignedTo?.lastName : '',
								'issue':ac?.resolution, 'bundleNames':bundleNames,'projectName':partyGroupInstance?.name,
								'clientName':project?.client?.name,
								'timezone':tzId, "rptTime": TimeUtil.formatDate(currDate), userDateFormatter: userDateFormatter,
								'previousNote':WebUtil.listAsMultiValueString(ac.notes) ]
			}
		}
		if( params.newsInfo == "true" ) {
			def moveEvent = MoveEvent.findByProject(project)
			def moveEventNewsList=MoveEventNews.findAllByMoveEvent(moveEvent)
			moveEventNewsList.each { moveEventNews ->
				moveEventNews?.resolution = moveEventNews?.resolution ? moveEventNews?.resolution : ''
				reportFields <<['assetName':'', 'assetTag':'', 'moveBundle' :'','sourceTargetRoom':'','model':'',
							'commentType':"news",
							'occuredAt': moveEventNews.dateCreated,
							'createdBy':moveEventNews?.createdBy?.toString(),
							'owner':'',
							'issue':moveEventNews.message +"/"+  moveEventNews?.resolution , 'bundleNames':'',
							'projectName':project?.name,
							'clientName':project?.client?.name,
							'timezone':tzId, "rptTime": TimeUtil.formatDate(currDate), userDateFormatter: userDateFormatter,
							'previousNote':'']
			}

		}

		if (!reportFields) {
			flash.message = " No Issues Were found for  selected values  "
			redirect( action:'retrieveBundleListForReportDialog', params:[reportId: 'Issue Report'] )
			return
		}

		String filename = ('IssueReport-' + project.name + '-' + bundleName).replace(" ", "_")
		if (reportFormat == "PDF") {
			chain(controller: 'jasper', action: 'index', model: [data: reportFields],
					params: [_format: "PDF", _name: filename, _file: params._file])
			return
		}

		// Generate XLS report
		try {
			File file = grailsApplication.parentContext.getResource( "/templates/IssueReport.xls" ).getFile()
			//set MIME TYPE as Excel
			response.setContentType( "application/vnd.ms-excel" )
			response.setHeader( "Content-Disposition", 'attachment; filename="' + filename + '.xls"' )

			def book = new HSSFWorkbook(new FileInputStream( file ))
			def sheet = book.getSheet("issues")
			WorkbookUtil.addCell(sheet, 1, 1, String.valueOf( project?.client?.name ))
			WorkbookUtil.addCell(sheet, 1, 2, String.valueOf( partyGroupInstance?.name ))
			WorkbookUtil.addCell(sheet, 1, 3, String.valueOf( bundleNames ))
			for ( int r = 0; r < reportFields.size(); r++ ) {
				WorkbookUtil.addCell(sheet, 0, r+6, String.valueOf(reportFields[r].assetName ?:''))
				WorkbookUtil.addCell(sheet, 1, r+6, String.valueOf(reportFields[r].assetTag ?:''))
				WorkbookUtil.addCell(sheet, 2, r+6, String.valueOf(reportFields[r].moveBundle ?:''))
				WorkbookUtil.addCell(sheet, 3, r+6, String.valueOf(reportFields[r].sourceTargetRoom ?:''))
				WorkbookUtil.addCell(sheet, 4, r+6, String.valueOf(reportFields[r].model ?:''))
				WorkbookUtil.addCell(sheet, 5, r+6, String.valueOf(reportFields[r].commentCode ?:''))
				WorkbookUtil.addCell(sheet, 6, r+6, String.valueOf(reportFields[r].commentType ?:''))
				WorkbookUtil.addCell(sheet, 7, r+6, TimeUtil.formatDateTime(reportFields[r].occuredAt))
				WorkbookUtil.addCell(sheet, 8, r+6, String.valueOf(reportFields[r].createdBy ?:''))
				WorkbookUtil.addCell(sheet, 9, r+6, String.valueOf(reportFields[r].owner ?:''))
				WorkbookUtil.addCell(sheet, 10, r+6, String.valueOf(reportFields[r].previousNote?:''))
				WorkbookUtil.addCell(sheet, 11, r+6, String.valueOf(reportFields[r].issue ?:''))
			}
			WorkbookUtil.addCell(sheet, 0, reportFields.size()+7, String.valueOf("Note : All times are in "+reportFields[0].timezone+" time zone") )
			book.write(response.getOutputStream())
		}
		catch (e) {
			log.error e.message, e
			flash.message = "Exception occurred while exporting data: $e.message"
			redirect(action:"retrieveBundleListForReportDialog", params:[reportId:'Issue Report'] )
		}
	}


	/*
	 *  Generate PDF Cabling QA / Conflict report
	 */
	def cablingQAReport() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		def reportName = params.reportName
		def cableType = params.cableType
		// if no moveBundle was selected
		if(params.moveBundle == "null") {
			flash.message = " Please Select Bundles. "
			redirect( action:'retrieveBundleListForReportDialog', params:[reportId: 'CablingQA'] )
		} else {
			def moveBundleInstance = MoveBundle.get(params.moveBundle)
			def reportFields = []
			def bundleName = "All Bundles"
			def cablesQuery = new StringBuilder("from AssetCableMap acm where acm.assetFrom.project.id = $project.id ")
			//if moveBundleinstance is selected (single moveBundle)
			if( moveBundleInstance ) {
				bundleName = moveBundleInstance?.name
				cablesQuery.append(" and acm.assetFrom.moveBundle = $moveBundleInstance.id ")
			}
			//All Bundles Selected
			else {
				cablesQuery.append(" and acm.assetFrom.moveBundle != null ")
			}
			if(cableType){
				cablesQuery.append(" and acm.assetFromPort.type = '$cableType' ")
			}

			List assetCablesList = []
			if(reportName == "cablingQA"){
				cablesQuery.append(" order By acm.assetFrom ")
				assetCablesList = AssetCableMap.findAll( cablesQuery.toString() )
			} else {
				def conflictQuery = " and acm.assetToPort is not null group by acm.assetToPort having count(acm.assetToPort) > 1"
				def conflictList =  AssetCableMap.findAll( cablesQuery.toString() + conflictQuery )

				def unknownList = AssetCableMap.findAll( cablesQuery.toString() + " and acm.cableStatus ='$AssetCableStatus.UNKNOWN' " )

				def orphanedQuery = " and acm.assetToPort is not null and acm.assetToPort not in( select mc.id from ModelConnector mc )"
				def orphanedList = AssetCableMap.findAll( cablesQuery.toString() + orphanedQuery )

				if(conflictList.size() > 0) assetCablesList.addAll(conflictList)
				if(unknownList.size() > 0) assetCablesList.addAll(unknownList)
				if(orphanedList.size() > 0) assetCablesList.addAll(orphanedList)
			}

			String tzId = userPreferenceService.timeZone
			def currDate = new Date()
			//Source AssetList
			if( assetCablesList != null) {
				assetCablesList.each { cable ->
					def bundleInstance
					if(cable.assetFrom.moveBundle != null) {
						bundleInstance = MoveBundle.get(cable.assetFrom.moveBundle.id)
					}

					reportFields <<[from_asset_name: cable.assetFrom.assetName, from_asset_tag: cable.assetFrom.assetTag,
									cable_type: cable.assetFromPort.type,  from_rack: cable.assetFrom?.rackTarget?.tag,
									to_rack: cable.assetTo ? cable.assetTo?.rackTarget?.tag: "",
									from_upos: cable.assetFrom?.targetRackPosition, color: cable.cableColor ? cable.cableColor: "",
									to_upos: "", from_connector_label: cable.assetFromPort.label,
									to_connector_label: cable.assetToPort ? cable.assetToPort.label: "",
									to_asset_tag: cable.assetTo ? cable.assetTo?.assetTag: "",
									to_asset_name: cable.assetTo ? cable.assetTo?.assetName: "",
									asset_id: cable.assetFrom?.id, project_name: project?.name,
									report_type: reportName == 'cablingQA' ? "Cabling QA Report": "Cabling Conflict Report",
									bundle_name: bundleInstance?.name, timezone: tzId,
									rpt_time: TimeUtil.formatDateTime(currDate)]
				}
			}
			//No Assets were found for selected moveBundle,team and Location
			if(reportFields.size() <= 0) {
				flash.message = " No Cables were found for  selected values"
				redirect( action:'retrieveBundleListForReportDialog', params:[reportId: 'CablingConflict'] )
			}else {
				def name = reportName == 'cablingQA' ? "CablingQA" : "CablingConflict"
				String filename = (name + '-' + project.name + '-' + bundleName).replace(" ", "_")

				chain(controller:'jasper',action:'index',model:[data:reportFields],
						params:["_format":"PDF","_name":filename,"_file":params._file])
			}
		}
	}

	/*
	 *  Generate XLS Structured Cabling data report
	 */
	def cablingDataReport() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		def cableType = params.cableType
		// if no moveBundle was selected
		if(params.moveBundle == "null") {
			flash.message = " Please Select Bundles. "
			redirect( action:'retrieveBundleListForReportDialog', params:[reportId: 'CablingQA'] )
		} else {
			def moveBundleInstance = MoveBundle.get(params.moveBundle)
			def reportFields = []
			def bundleName = "All Bundles"




			def assetCablesList = AssetCableMap.createCriteria().list{
						createAlias("assetFrom", "af")
						and{
							eq("af.project", project)

							if(moveBundleInstance){
								eq("af.moveBundle", moveBundleInstance)
							} else {
								isNotNull("af.moveBundle")
							}

							if(cableType){
								eq("af.type", cableType)
							}
						}

						order("assetFrom")

						resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
						fetchSize 1000
					}

			if(moveBundleInstance){
				bundleName = moveBundleInstance?.name
			}

			try {
				//set MIME TYPE as Excel
				if (assetCablesList.size() == 0) {
					flash.message = "No data found for the selected filter"
					redirect(action:"retrieveBundleListForReportDialog",
					          params:[reportId:'CablingData', message:flash.message] )
				} else {
					File file = grailsApplication.parentContext.getResource( "/templates/Cabling_Details.xls" ).getFile()

					response.setContentType( "application/vnd.ms-excel" )
					String filename = ('CablingData-' + project.name + '-' + bundleName + '.xls').replace(" ", "_").replace(".xls",'')
					response.setHeader( "Content-Disposition", "attachment; filename = $filename" )
					response.setHeader( "Content-Disposition", 'attachment; filename="' + filename + '.xls"' )

					def book = new HSSFWorkbook(new FileInputStream( file ))

					def sheet = book.getSheet("cabling_data")
					assetEntityService.cablingReportData(assetCablesList, sheet)
					book.write(response.getOutputStream())
				}
			} catch( Exception ex ) {
				log.error "Exception occurred while exporting cabling data: ${ExceptionUtil.stackTraceToString(ex)}"
				flash.message = "Exception occurred while exporting data"
				redirect(action:"retrieveBundleListForReportDialog",
				          params:[reportId:'CablingData', message:flash.message] )
				return
			}
		}
	}

	/*
	 * request page for power report
	 */
	def powerReport() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		List<MoveBundle> moveBundles = MoveBundle.findAllByProject(project)
		def currentBundle = userPreferenceService.moveBundleId
		/* set first bundle as default if user pref not exist */
		boolean isCurrentBundle = true
		List<Model> models = AssetEntity.executeQuery('SELECT ae.model FROM AssetEntity ae WHERE ae.project=? GROUP BY ae.model', [project])
		if (!currentBundle) {

			currentBundle = moveBundles[0]?.id?.toString()
			isCurrentBundle = false
		}

		[moveBundleInstanceList: moveBundles, project: project,
		 currentBundle: currentBundle, isCurrentBundle: isCurrentBundle, models: models]
	}

	/*
	 *  Generate Power report in WEb, PDF, Excel format based on user input
	 */
	def powerReportDetails() {

		List bundleId = request.getParameterValues("moveBundle")
		if (bundleId == ['null']) {
			return [errorMessage: "Please Select a Bundle."]
		}

		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		def sourceRacks = []
		def targetRacks = []
		List<MoveBundle> moveBundles = MoveBundle.findAllByProject(project)
		String powerType = params.powerType ?: userPreferenceService.getPreference(PREF.CURR_POWER_TYPE)
		if (!bundleId.contains("all")){
			moveBundles = MoveBundle.findAll("from MoveBundle m where id in (${bundleId.join(',')})")
		}

		def rackIds = request.getParameterValues("sourcerack")
		if (rackIds != ['none']) {
			if (rackIds[0] == "") {
				for (MoveBundle bundle in moveBundles) {
					for (Rack rack in bundle.sourceRacks) {
						if (!sourceRacks.contains(rack)) {
							sourceRacks << rack
						}
					}
				}
			}
			else {
				for (id in rackIds) {
					Rack rack = Rack.get(id)
					if (!sourceRacks.contains(rack) ) {
						sourceRacks.add(rack)
					}
				}
			}
			sourceRacks.sort { it.tag }
		}

		rackIds = request.getParameterValues("targetrack")
		if (rackIds != ['none']) {
			if (rackIds[0] == "") {
				for (MoveBundle bundle in moveBundles) {
					for (Rack targetRack in bundle.targetRacks) {
						if (!targetRacks.contains(targetRack)) {
							targetRacks.add(targetRack)
						}
					}
				}
			}
			else {
				for (id in rackIds) {
					Rack rack = Rack.get(id)
					if (!targetRacks.contains(rack)) {
						targetRacks << rack
					}
				}
			}
			targetRacks.sort { it.tag }
		}

		def racks = sourceRacks + targetRacks
		String tzId = userPreferenceService.timeZone
		def reportDetails = []
		racks.each { rack->
			def assets = rack.assets.findAll { it.assetType !='Blade' && moveBundles?.id?.contains(it.moveBundle?.id) && it.project == project }
			int powerA = 0
			int powerB = 0
			int powerC = 0
			int powerTBD = 0
			int totalPower = 0
			assets.each { asset ->
				def assetPowerCabling = AssetCableMap.findAll("FROM AssetCableMap cap WHERE cap.assetFromPort.type = ? AND cap.assetFrom = ?",["Power",asset])
				def powerConnectors = assetPowerCabling.findAll{it.toPower != null && it.toPower != '' }.size()
				def powerDesign = asset.model?.powerDesign ? asset.model?.powerDesign : 0
				totalPower += powerDesign
				if(powerConnectors){
					def powerUseForConnector = powerDesign ? powerDesign / powerConnectors : 0
					assetPowerCabling.each { cables ->
						if(cables.toPower){
							switch(cables.toPower){
								case "A": powerA += powerUseForConnector
								break
								case "B": powerB += powerUseForConnector
								break
								case "C": powerC += powerUseForConnector
								break
							}
						}
					}
				} else {
					powerTBD += powerDesign
				}
			}
			powerA = powerType != "Watts" ?  powerA ? (powerA / 120).toFloat().round(1) : 0.0 : powerA ? Math.round(powerA):0
			powerB = powerType != "Watts" ?  powerB ? (powerB / 120).toFloat().round(1) : 0.0 : powerB ? Math.round(powerB):0
			powerC = powerType != "Watts" ?  powerC ? (powerC / 120).toFloat().round(1) : 0.0 : powerC ? Math.round(powerC):0
			powerTBD = powerType != "Watts" ?  powerTBD ? (powerTBD / 120).toFloat().round(1) : 0.0 : powerTBD ? Math.round(powerTBD):0
			totalPower = powerType != "Watts" ?  totalPower ? (totalPower / 120).toFloat().round(1) : 0.0 : totalPower ? Math.round(totalPower):0

			reportDetails << [location:rack.location?.toString(), room:rack.room?.toString(), rack:rack.tag, devices:assets.size(),
							  powerA:powerA,powerB:powerB,powerC:powerC,powerTBD:powerTBD,totalPower:totalPower]
		}
		if(params.output == "excel"){
			try {
				File file = grailsApplication.parentContext.getResource( "/templates/Power_Report.xls" ).getFile()

				//set MIME TYPE as Excel
				response.setContentType( "application/vnd.ms-excel" )
				String filename = ('Power_Report-' + project.name + '.xls').replace(" ", "_")
				response.setHeader( "Content-Disposition", "attachment; filename = $filename" )

				def book = new HSSFWorkbook(new FileInputStream( file ))

				def sheet = book.getSheet("Power_Report")

				for ( int r = 1; r <= reportDetails.size(); r++ ) {
					WorkbookUtil.addCell(sheet, 0, r, String.valueOf(reportDetails[r-1].location ))
					WorkbookUtil.addCell(sheet, 1, r, String.valueOf(reportDetails[r-1].room ))
					WorkbookUtil.addCell(sheet, 2, r, String.valueOf(reportDetails[r-1].rack ))
					WorkbookUtil.addCell(sheet, 3, r, String.valueOf(reportDetails[r-1].devices ))
					WorkbookUtil.addCell(sheet, 4, r, String.valueOf(reportDetails[r-1].powerA ))
					WorkbookUtil.addCell(sheet, 5, r, String.valueOf(reportDetails[r-1].powerB ))
					WorkbookUtil.addCell(sheet, 6, r, String.valueOf(reportDetails[r-1].powerC ))
					WorkbookUtil.addCell(sheet, 7, r, String.valueOf(reportDetails[r-1].powerTBD ))
					WorkbookUtil.addCell(sheet, 8, r, String.valueOf(reportDetails[r-1].totalPower ))
				}

				book.write(response.getOutputStream())

			} catch (e) {
				log.error "Exception occurred while exporting data $e", e
				return
			}
		} else if(params.output == "pdf"){
			String filename = ('Power_Report-' + project.name).replace(" ", "_")
			chain(controller:'jasper',action:'index',model:[data:reportDetails],
					params:["_format":"PDF","_name":filename,"_file":"Power_Report"])
		}
		return [reportDetails : reportDetails]
	}

	def preMoveCheckList() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		[moveEvents: MoveEvent.findAllByProject(project),
		 moveEventId: userPreferenceService.moveEventId]
	}

	def generateCheckList() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		def moveEventId = params.moveEvent
		MoveEvent moveEvent
		def errorMsg = "Please select a MoveEvent"
		if ( moveEventId && moveEventId.isNumber() ) {
			def isProjMoveEvent  = MoveEvent.findByIdAndProject( moveEventId, project )
			if ( !isProjMoveEvent ) {
				errorMsg = " User tried to access moveEvent $moveEventId that was not found in project : $project "
				log.warn "generateCheckList: User tried to access moveEvent $moveEventId that was not found in project : $project"
			} else {
				errorMsg = ""
				userPreferenceService.setMoveEventId(moveEventId)
				moveEvent = MoveEvent.get(moveEventId)
				return reportsService.generatePreMoveCheckList(project.id, moveEvent)
			}
		}
		flash.message = errorMsg
		redirect(action: "preMoveCheckList")
	}

	def applicationConflicts() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		List<MoveBundle> moveBundles = MoveBundle.findAllByProject(project)
		String bundleId = (userPreferenceService.moveBundleId ?: moveBundles[0]?.id).toString()
		Long moveBundleId = NumberUtil.toLong(bundleId)

		[moveBundles: moveBundles, moveBundleId: moveBundleId,
		 appOwnerList: reportsService.getSmeList(moveBundleId, false)]
	}

	/**
	 * Used to display application selection criteria page.
	 */
	def applicationProfiles() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		List<MoveBundle> moveBundles = MoveBundle.findAllByProject(project)
		def moveBundleId = params.long('moveBundle') ?: userPreferenceService.moveBundleId ?: moveBundles[0]?.id

		[moveBundles: moveBundles, moveBundleId: moveBundleId, smeList: reportsService.getSmeList(moveBundleId, true),
		 appOwnerList: reportsService.getSmeList(moveBundleId, false), selectedSme: params.smeByModel,
		 selectedOwner: params.appOwner]
	}

	/**
	 * Used to populate sme select based on the bundle Selected
	 */
	def generateSmeByBundle() {
		Long moveBundleId = params.long('bundle')
		List<Person> smeList = params.forWhom != 'conflict' ? reportsService.getSmeList(moveBundleId, true) : []
		List<Person> appOwnerList = params.forWhom != 'migration' ? reportsService.getSmeList(moveBundleId, false) : []
		render(template: "smeSelectByBundle",
		       model: [smeList: smeList, appOwnerList: appOwnerList, forWhom: params.forWhom ?: ''])
	}

	/**
	 * Used to generate Application Profiles
	 * @return list of applications
	 */
	def generateApplicationProfiles() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		def currentBundle
		def currentSme
		def applicationOwner

		def query = new StringBuilder(""" SELECT a.app_id AS id
			FROM application a
			LEFT OUTER JOIN asset_entity ae ON a.app_id=ae.asset_entity_id
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
			LEFT OUTER JOIN person p ON p.person_id=a.sme_id
			LEFT OUTER JOIN person p1 ON p1.person_id=a.sme2_id
			LEFT OUTER JOIN person p2 ON p2.person_id=ae.app_owner_id
			WHERE ae.project_id = $project.id """)

		if(params.moveBundle == 'useForPlanning'){
			def bundleIds = MoveBundle.getUseForPlanningBundlesByProject(project)?.id
			query.append(" AND mb.move_bundle_id in (${WebUtil.listAsMultiValueString(bundleIds)}) ")
		}else{
			currentBundle = MoveBundle.get(params.moveBundle)
			query.append(" AND mb.move_bundle_id=$params.moveBundle ")
		}

		if(params.smeByModel!='null'){
			currentSme = Person.get(params.smeByModel)
			query.append(" AND (p.person_id=$params.smeByModel or p1.person_id=$params.smeByModel) ")
		}

		if(params.appOwner!='null'){
			applicationOwner = Person.get(params.appOwner)
			query.append(" AND p2.person_id=$params.appOwner ")
		}

		int assetCap = 100 // default value
		if(params.report_max_assets){
			try{
				assetCap = params.report_max_assets.toInteger()
			}catch(Exception e){
				log.info("Invalid value given for assetCap: $assetCap")
			}
		}

		query.append(" LIMIT $assetCap")

		log.info "query = $query"

		def applicationList = jdbcTemplate.queryForList(query.toString())

		if( applicationList.size() > 500 ) {
			flash.message = """Your criteria results in more than the maximum 500 applications that the report allows.
				Please adjust your criteria accordingly before resubmitting."""
			redirect (action:'applicationProfiles', params:params)
		}

		// TODO: we'd like to flush the session.
		List appList = []
		//TODO:need to write a service method since the code used below is almost similar to application show.
		applicationList.eachWithIndex { app, idx ->
			def assetEntity = AssetEntity.get(app.id)
			Application application = Application.get(app.id)

			// assert assetEntity != null  //TODO: oluna should I add an assertion here?

			def assetComment
			List<AssetDependency> dependentAssets = assetEntity.requiredDependencies()
			List<AssetDependency> supportAssets =  assetEntity.supportedDependencies()
			if (AssetComment.countByAssetEntityAndCommentTypeAndDateResolved(application, 'issue', null)) {
				assetComment = "issue"
			} else if (AssetComment.countByAssetEntity(application)) {
				assetComment = "comment"
			} else {
				assetComment = "blank"
			}
			def prefValue= userPreferenceService.getPreference(PREF.SHOW_ALL_ASSET_TASKS) ?: 'FALSE'
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)
			def appMoveEvent = AppMoveEvent.findAllByApplication(application)
			def moveEventList = MoveEvent.findAllByProject(project, [sort: 'name'])
			def appMoveEventlist = AppMoveEvent.findAllByApplication(application).value

			//field importance styling for respective validation.
			def validationType = assetEntity.validation

			def shutdownBy = assetEntity.shutdownBy  ? assetEntityService.resolveByName(assetEntity.shutdownBy) : ''
			def startupBy = assetEntity.startupBy  ? assetEntityService.resolveByName(assetEntity.startupBy) : ''
			def testingBy = assetEntity.testingBy  ? assetEntityService.resolveByName(assetEntity.testingBy) : ''

			def shutdownById = shutdownBy instanceof Person ? shutdownBy.id : -1
			def startupById = startupBy instanceof Person ? startupBy.id : -1
			def testingById = testingBy instanceof Person ? testingBy.id : -1

			// TODO: we'd like to flush the session
			// GormUtil.flushAndClearSession(idx)
			appList.add([
				app: application, supportAssets: supportAssets, dependentAssets: dependentAssets,
				redirectTo: params.redirectTo, assetComment: assetComment, assetCommentList: assetCommentList,
				appMoveEvent: appMoveEvent, 
				moveEventList: moveEventList, 
				appMoveEvent: appMoveEventlist,
				dependencyBundleNumber: AssetDependencyBundle.findByAsset(application)?.dependencyBundle,
				project: project, prefValue: prefValue, 
				shutdownById: shutdownById, 
				startupById: startupById, 
				testingById: testingById,
				shutdownBy: shutdownBy, 
				startupBy: startupBy, 
				testingBy: testingBy, 
				errors: params.errors
			])
		}

		Map standardFieldSpecs = customDomainService.standardFieldSpecsByField(project, AssetClass.APPLICATION)
		List customFields = assetEntityService.getCustomFieldsSettings(project, "Application", true)

		[applicationList: appList, moveBundle: currentBundle ?: 'Planning Bundles', sme: currentSme ?: 'All',
		 appOwner: applicationOwner ?: 'All', project: project, standardFieldSpecs: standardFieldSpecs, customs: customFields]
	}

	def generateApplicationConflicts() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		def moveBundleId = params.moveBundle
		def errorMsg = "Please select a MoveBundle"
		def conflicts = params.conflicts == 'on'
		def unresolved = params.unresolved == 'on'
		def missing = params.missing == 'on'
		def appOwner = params.appOwner

		int assetCap = params.report_max_assets?params.report_max_assets.toInteger():100

		if (params.moveBundle == 'useForPlanning') {
			return reportsService.genApplicationConflicts(project.id, moveBundleId, conflicts,
					unresolved, missing, true, appOwner, assetCap)
		}

		if( moveBundleId && moveBundleId.isNumber() ){
			def isProjMoveBundle  = MoveBundle.findByIdAndProject( moveBundleId, project )
			if ( !isProjMoveBundle ) {
				errorMsg = " User tried to access moveBundle $moveBundleId that was not found in project : $project "
				log.warn "generateCheckList: User tried to access moveBundle $moveBundleId that was not found in project : $project"
			} else {
				errorMsg = ""
				userPreferenceService.setPreference(PREF.MOVE_BUNDLE, moveBundleId)
				//def eventsProjectInfo = getEventsProjectInfo(moveEvent,project,currProj,moveBundles,eventErrorList)
				return reportsService.genApplicationConflicts(project.id, moveBundleId, conflicts,
						unresolved, missing, false, appOwner, assetCap)//.add(['time':])
			}
		}
		flash.message = errorMsg
		redirect( action:"applicationConflicts")
	}

	/*
	 * Generate Issue Report
	 */
	def tasksReport() {
		def reqEvents = params.list("moveEvent").toList()
		String tzId = userPreferenceService.timeZone
		String userDTFormat = userPreferenceService.dateFormat

		if(reqEvents) {
			Project project = controllerService.getProjectForPage(this, 'to view Reports')
			boolean allBundles = reqEvents.find { it == 'all' }
			def badReqEventIds

			if( !allBundles ){
				reqEvents = reqEvents.collect {id-> NumberUtils.toDouble(id, 0).round() }
				//Verifying events id are in same project or not.
				badReqEventIds = moveEventService.verifyEventsByProject(reqEvents, project)
			}

			//if found any bad id returning to the user
			if( badReqEventIds ){
				flash.message = "Event ids $badReqEventIds is not associated with current project.\
								Kindly request for project associated  Event ids ."
				return
			}

			def argMap = [type: AssetCommentType.ISSUE, project: project]
			def taskListHql = "FROM AssetComment WHERE project =:project AND commentType =:type "

			if(!allBundles){
				taskListHql +=" AND moveEvent.id IN (:events) "
				argMap.events = reqEvents
			}

			if( params.wUnresolved ){
				taskListHql += "AND status != :status"
				argMap.status = AssetCommentStatus.COMPLETED
			}

			// handle unpublished tasks
			userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED, params.viewUnpublished as Boolean)

			boolean viewUnpublished = securityService.hasPermission(Permission.TaskPublish) && params.viewUnpublished
			if (!viewUnpublished) {
				taskListHql += " AND isPublished = :isPublished "
				argMap.isPublished = true
			}

			List taskList = AssetComment.findAll(taskListHql, argMap)
			if (viewUnpublished) {
				taskList.addAll(params.wComment ? AssetComment.findAllByCommentTypeAndProject(AssetCommentType.COMMENT, project) : [])
			}
			else {
				taskList.addAll(params.wComment ? AssetComment.findAllByCommentTypeAndProjectAndIsPublished(AssetCommentType.COMMENT, project, true) : [])
			}

			//Generating XLS Sheet
			switch(params._action_tasksReport){
				case "Generate Xls" :
					  exportTaskReportExcel(taskList, tzId, userDTFormat, project, reqEvents)
					  break

				case "Generate Pdf" :
					  exportTaskReportPdf(taskList, tzId, project, reqEvents)
					  break

				default :
					 render (view :'tasksReport',
					         model:[taskList : taskList, tzId:tzId, viewUnpublished:viewUnpublished,
					                userDTFormat:userDTFormat, tzId:tzId])
					 break
			}
		} else{
			flash.message = "Please select move event to get the task report."
			redirect( action:"retrieveBundleListForReportDialog", params:[reportId:"Task Report"])
		}
	}

	/**
	 * Export task report in XLS format
	 * @param taskList : list of tasks
	 * @param tzId : timezone
	 * @param project : project instance
	 * @param reqEvents : list of requested events.
	 * @return : will generate a XLS file having task task list
	 */
	def exportTaskReportExcel(taskList, tzId, userDTFormat, project, reqEvents){
		File file = grailsApplication.parentContext.getResource( "/templates/TaskReport.xls" ).getFile()

		def currDate = TimeUtil.nowGMT()
		String eventsTitleSheet = "ALL"
		boolean allEvents = (reqEvents.size() > 1 || reqEvents[0] != "all") ? false : true
		def moveEvents = []
		if(!allEvents){
			moveEvents = MoveEvent.findAll("FROM MoveEvent WHERE id IN(:ids)", [ids: reqEvents])
			def eventNames = moveEvents.collect{it.name}
			eventsTitleSheet = eventNames.join(", ")
		}
		def nameParams = [project:project, moveEvent: moveEvents, allEvents: allEvents]
		String filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, nameParams, 'xls')

		//set MIME TYPE as Excel
		response.setContentType( "application/vnd.ms-excel" )
		setHeaderContentDisposition(filename)


		def book = new HSSFWorkbook(new FileInputStream( file ))

		def tasksSheet = book.getSheet("tasks")

		def preMoveColumnList = ['taskNumber', 'comment', 'assetEntity', 'assetClass', 'assetId', 'taskDependencies', 'assignedTo', 'instructionsLink', 'role', 'status',
								'','','', 'notes', 'duration', 'durationLocked', 'durationScale', 'estStart','estFinish','actStart', 'dateResolved', 'workflow', 'category',
								'dueDate', 'dateCreated', 'createdBy', 'moveEvent', 'taskBatchId']

		moveBundleService.issueExport(taskList, preMoveColumnList, tasksSheet, tzId,
			userDTFormat, 3, securityService.viewUnpublished())



		def exportTitleSheet = {
			def userLogin = securityService.getUserLogin()
			def titleSheet = book.getSheet("Title")
			WorkbookUtil.addCell(titleSheet, 1, 2, project.client.toString())
			WorkbookUtil.addCell(titleSheet, 1, 3, project.id.toString())
			WorkbookUtil.addCell(titleSheet, 2, 3, project.name.toString())
			WorkbookUtil.addCell(titleSheet, 1, 4, partyRelationshipService.getProjectManagers(project).toString())
			WorkbookUtil.addCell(titleSheet, 1, 5, eventsTitleSheet)
			WorkbookUtil.addCell(titleSheet, 1, 6, userLogin.person.toString())

			def exportedOn = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, new Date(), TimeUtil.FORMAT_DATE_TIME_22)
			WorkbookUtil.addCell(titleSheet, 1, 7, exportedOn)
			WorkbookUtil.addCell(titleSheet, 1, 8, tzId)
			WorkbookUtil.addCell(titleSheet, 1, 9, userDTFormat)

			WorkbookUtil.addCell(titleSheet, 30, 0, "Note: All times are in ${tzId ? tzId : 'EDT'} time zone")
		}

		exportTitleSheet()




		book.write(response.getOutputStream())
	}

	/**
	 * Export task report in pdf format
	 * @param taskList : list of tasks
	 * @param tzId : timezone
	 * @param project : project instance
	 * @param reqEvents : list of requested events.
	 * @return : will generate a pdf file having task task list
	 */
	def exportTaskReportPdf(taskList, tzId, project, reqEvents){
		def currDate = new Date()
		def reportFields = []

		boolean viewUnpublished = securityService.viewUnpublished()

		taskList.each { task ->

			def visibleDependencies
			if (viewUnpublished) {
				visibleDependencies = task.taskDependencies
			}
			else {
				visibleDependencies = task.taskDependencies?.findAll{it?.predecessor?.isPublished}
			}

			reportFields << [
				taskNumber: task.taskNumber?.toString(),
				taskDependencies: WebUtil.listAsMultiValueString(visibleDependencies.predecessor?.comment),
				assetEntity: task.assetEntity?.assetName, comment: task.comment,
				assignedTo: task.assignedTo?.toString() ?: '', status: task.status,
				instructionsLink: task.instructionsLink?.toString() ?: '',
				datePlanned: '', outStanding: '', dateRequired: '', workflow: '',
				clientName: project?.client?.name, team: task.role?.toString() ?: '',
				projectName: project?.name, notes: WebUtil.listAsMultiValueString(task.notes),
				duration: task.duration?.toString() ?: '',
				estStart: TimeUtil.formatDate(task.estStart),
				estFinish: TimeUtil.formatDate(task.estFinish),
				actStart: TimeUtil.formatDate(task.actStart),
				actFinish: TimeUtil.formatDate(task.actFinish),
				createdOn: TimeUtil.formatDate(task.dateCreated),
				createdBy: task.createdBy.toString(), moveEvent: task.moveEvent?.toString() ?: '',
				timezone: tzId, rptTime: TimeUtil.formatDate(currDate)]
		}
		if(!reportFields) {
			flash.message = " No Assets Were found for  selected values  "
			redirect( action:'retrieveBundleListForReportDialog', params:[reportId: 'Task Report'] )
		} else {
			boolean allEvents = reqEvents.remove("all")
			def moveEvents
			if (reqEvents) {
				moveEvents = MoveEvent.findAll("FROM MoveEvent WHERE id IN(:ids)", [ids: reqEvents])
			}
			def nameParams = [project:project, moveEvent: moveEvents, allEvents: allEvents]
			String filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_EVENT_DATE, nameParams)
			chain(controller:'jasper',action:'index',model:[data:reportFields],
					params:["_format":"PDF","_name":filename,"_file":"taskReport"])
		}
	}

	/**
	 * used to render to server Conflicts selection criteria.
	 */
	def serverConflicts() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		[moveBundles: MoveBundle.findAllByProject(project),
		 moveBundleId: userPreferenceService.moveBundleId]
	}

	/**
	 * Used to generate server Conflicts.
	 */
	def generateServerConflicts() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		def moveBundleId = params.moveBundle
		def bundleConflicts = params.bundleConflicts == 'on'
		def unresolvedDependencies = params.unresolvedDep == 'on'
		def noRunsOn = params.noRuns == 'on'
		def vmWithNoSupport = params.vmWithNoSupport == 'on'
		def view = params.rows ? "_serverConflicts" : "generateServerConflicts"

		int assetCap = 100 // default value
		if(params.report_max_assets){
			try{
				assetCap = params.int('report_max_assets')
			}
			catch(e) {
				log.info("Invalid value given for assetCap: $assetCap")
			}
		}

		if( params.moveBundle == 'useForPlanning' ){
				render (view : view ,
				        model : reportsService.genServerConflicts(project, moveBundleId, bundleConflicts,
						        unresolvedDependencies, noRunsOn, vmWithNoSupport, true, params, assetCap))
				return
		}

		if (moveBundleId?.isNumber()) {
			def isProjMoveBundle  = MoveBundle.findByIdAndProject( moveBundleId, project )
			if ( !isProjMoveBundle ) {
				log.warn "generateCheckList: User tried to access moveBundle $moveBundleId that was not found in project : $project"
			} else {
				userPreferenceService.setPreference(PREF.MOVE_BUNDLE, moveBundleId)
				render(view: view , model: reportsService.genServerConflicts(project, moveBundleId, bundleConflicts, unresolvedDependencies,
					noRunsOn, vmWithNoSupport, false, params, assetCap))
				return
			}
		}

		flash.message = 'Please select a MoveBundle'
		redirect action: 'index'
	}

	/**
	 * used to render to application Migration Report selection criteria.
	 */
	def applicationMigrationReport() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return
		def moveBundleList = MoveBundle.findAllByProject(project)
		def moveBundleId = userPreferenceService.moveBundleId
		def smeList = reportsService.getSmeList(moveBundleId, true)
		def workflow = Workflow.findByProcess(project.workflowCode)
		def workflowTransitions = WorkflowTransition.findAll(
				'FROM WorkflowTransition where workflow=? order by transId', [workflow])
		String domain = AssetClass.APPLICATION.toString()
		List appAttributes = customDomainService.fieldSpecs(project, domain, CustomDomainService.ALL_FIELDS, ["field", "label"])
		[moveBundles:moveBundleList, moveBundleId:moveBundleId, smeList:smeList.sort{it.lastName},
		 workflowTransitions:workflowTransitions, appAttributes:appAttributes]
	}

	/**
	 * Used to generate Application Migration Report.
	 */
	def generateApplicationMigration() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		def applicationList
		def currentSme
		def currentBundle
		def appList = []
		if(params.moveBundle == 'useForPlanning'){
			if(params.smeByModel!='null'){
				currentSme = Person.get(params.smeByModel)
				applicationList = Application.findAll("from Application where project = :project and (sme=:smes or sme2=:smes)",
					[project:project,smes:currentSme])
			}else {
				applicationList = Application.findAllByMoveBundleInList(MoveBundle.getUseForPlanningBundlesByProject(project))
			}
		}else{
			currentBundle = MoveBundle.get(params.moveBundle)
			if(params.smeByModel!='null'){
				currentSme = Person.get(params.smeByModel)
				applicationList = Application.findAll("from Application where project = :project and moveBundle = :bundle \
					and (sme=:smes or sme2=:smes)",[project:project,bundle:currentBundle,smes:currentSme])
			}else {
				applicationList = Application.findAllByMoveBundle(currentBundle)
			}
		}

		applicationList.each {
			Application application = Application.get( it.id )
			def appComments = application.comments
			def startTimeList = appComments.findAll{it.category == params.startCateory}.sort{it.actStart}?.actStart
			def finishTimeList = appComments.findAll{it.category == params.stopCateory}.sort{it.actStart}?.actStart

			startTimeList.removeAll([null])

			def finishTime= finishTimeList ? finishTimeList[-1] : null
			def startTime = startTimeList ? startTimeList[0] : null

			def duration = new StringBuilder()
			def customParam
			def windowColor
			def workflow
			def durationHours

			if(finishTime && startTime){
				def dayTime = TimeCategory.minus(finishTime, startTime)
				durationHours = (dayTime.days*24)+dayTime.hours
				if(durationHours){
					duration.append(durationHours)
				}
				if(dayTime.minutes){
					duration.append((durationHours?':':'0:')+dayTime.minutes)
				}
			}
			if(params.outageWindow == 'drRtoDesc'){
				customParam = application.drRtoDesc ? NumberUtils.toInt((application.drRtoDesc).split(" ")[0]) : ''
				if (duration && customParam) {
					windowColor = customParam < durationHours ? 'red' : ''
				}
			} else {
				customParam = it[params.outageWindow]
			}

			if (params.workflowTransId) {
				def workflowTransaction = WorkflowTransition.get(params.workflowTransId)
				workflow = appComments.findAll{it?.workflowTransition == workflowTransaction}.sort{it.actStart}
				workflow.removeAll([null])
			}
			appList.add(app: application, startTime: startTime, finishTime: finishTime, duration: duration ?: '',
				customParam: customParam ? customParam + (params.outageWindow == 'drRtoDesc' ? 'h': '') : '',
				windowColor: windowColor, workflow: workflow ? workflow[0].duration + " " + workflow[0].durationScale : '')
		}

		[appList: appList, moveBundle: currentBundle, sme: currentSme ?: 'All', project: project]
	}

	/**
	 * Render to database Report selection criteria.
	 */
	def databaseConflicts() {
		Project project = controllerService.getProjectForPage(this, 'to view Reports')
		if (!project) return

		[moveBundles: MoveBundle.findAllByProject(project),
		 moveBundleId: userPreferenceService.moveBundleId]
	}

	/**
	 * Used to generate database conflicts Report.
	 */
	def generateDatabaseConflicts() {
		Project project = securityService.userCurrentProject
		def moveBundleId = params.moveBundle
		def bundleConflicts = params.bundleConflicts == 'on'
		def unresolvedDependencies = params.unresolvedDep == 'on'
		def noApps = params.noApps == 'on'
		def dbWithNoSupport = params.dbWithNoSupport == 'on'

		int assetCap = 100 // default value
		if(params.report_max_assets){
			try{
				assetCap = params.report_max_assets.toInteger()
			}catch(Exception e){
				log.info("Invalid value given for assetCap: $assetCap")
			}
		}

		if( params.moveBundle == 'useForPlanning' ){
				return reportsService.genDatabaseConflicts(moveBundleId, bundleConflicts, unresolvedDependencies, noApps, dbWithNoSupport, true, assetCap)
		}
		if( moveBundleId && moveBundleId.isNumber() ){
			def isProjMoveBundle  = MoveBundle.findByIdAndProject( moveBundleId, project )
			if ( !isProjMoveBundle ) {
				log.warn "generateCheckList: User tried to access moveBundle $moveBundleId that was not found in project : $project"
			} else {
				userPreferenceService.setPreference(PREF.MOVE_BUNDLE, moveBundleId)
				return reportsService.genDatabaseConflicts(moveBundleId, bundleConflicts, unresolvedDependencies, noApps, dbWithNoSupport, false, assetCap)
			}
		}
	}

	/**
	 * Used to generate project activity metrics Report.
	 */
	@HasPermission(Permission.ReportViewProjectDailyMetrics)
	def projectActivityMetrics() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def userProjects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE)
		def startDate = Calendar.instance
		startDate.set(Calendar.DATE, 1)
		startDate.add(Calendar.MONTH, -2)
		startDate = startDate.time
		def endDate = new Date()

		render(view: 'projectActivityMetricsReport',
		       model: [userProjects: userProjects, startDate: startDate, endDate: endDate])
	}

	/**
	 * Used to generate project activity metrics excel file.
	 */
	@HasPermission(Permission.ReportViewProjectDailyMetrics)
	def projectActivityMetricsExport() {

		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		List projectIds = params.list("projectId")
		def startDate = params.startDate
		def endDate = params.endDate
		def includeNonPlanning = params.includeNonPlanning

		def validDates = true
		try {
			startDate = TimeUtil.parseDate(startDate)
			endDate = TimeUtil.parseDate(endDate)
		} catch (e) {
			validDates = false
		}

		if (projectIds && validDates) {
			boolean allProjects = projectIds.find { it == 'all' }
			boolean badProjectIds = false
			List<Project> userProjects = projectService.getUserProjects(securityService.hasPermission(Permission.ProjectShowAll), ProjectStatus.ACTIVE)
			Map<Long, Project> userProjectsMap = [:]
			List<Long> invalidProjectIds = []
			List<Long> allProjectIds = []

			for (Project p in userProjects) {
				userProjectsMap[p.id] = p
				allProjectIds << p.id
			}

			if ( allProjects ) {
				projectIds = allProjectIds
			} else {
				projectIds = projectIds.collect { NumberUtil.toLong(it) }
				// Verify that the user can accesss the proj
				projectIds.each { id ->
					if (!userProjectsMap[id]) {
						invalidProjectIds << id
						badProjectIds = true
					}
				}
			}

			//if found any bad id returning to the user
			if( badProjectIds ){
				flash.message = "Project ids $invalidProjectIds are not associated with current user."
				redirect( action:"projectActivityMetrics")
				return
			}

			List<Map<String, Object>> activityMetrics = projectService.searchProjectActivityMetrics(projectIds, startDate, endDate)
		  	exportProjectActivityMetricsExcel(activityMetrics, includeNonPlanning)

		} else{
			flash.message = "Please select at least one project and valid dates."
			redirect( action:"projectActivityMetrics")
		}
	}

	/**
	 * Export task report in XLS format
	 * @param activityMetrics: activity metrics
	 * @param includeNonPlanning: display or not non planning information
	 * @return : will generate a XLS file
	 */
	private void exportProjectActivityMetricsExcel(List<Map<String, Object>> activityMetrics, includeNonPlanning) {
		File file = grailsApplication.parentContext.getResource( "/templates/ActivityMetrics.xls" ).getFile()
		String fileDate = TimeUtil.formatDateTime(TimeUtil.nowGMT(), TimeUtil.FORMAT_DATE_ISO8601)
		String filename = 'ActivityMetrics-' + fileDate + '-Report'

		//set MIME TYPE as Excel
		response.setContentType("application/vnd.ms-excel")
		response.setHeader("Content-Disposition", 'attachment; filename="' + filename + '.xls"')

		def book = new HSSFWorkbook(new FileInputStream( file ))
		def metricsSheet = book.getSheet("metrics")

		def projectNameFont = book.createFont()
		projectNameFont.setFontHeightInPoints((short)12)
		projectNameFont.setFontName("Arial")
		projectNameFont.setBoldweight(Font.BOLDWEIGHT_BOLD)

		def projectNameCellStyle
		projectNameCellStyle = book.createCellStyle()
		projectNameCellStyle.setFont(projectNameFont)

		def rowNum = 5
		def project_code

		activityMetrics.each { Map<String, Object> am ->

			if (project_code != am['project_code']) {
				rowNum++
				project_code = am['project_code']
				WorkbookUtil.addCell(metricsSheet, 0, rowNum, am['project_code'])
				WorkbookUtil.applyStyleToCell(metricsSheet, 0, rowNum, projectNameCellStyle)
			}

			WorkbookUtil.addCell(metricsSheet, 1, rowNum, TimeUtil.formatDateTime(am['metric_date'], TimeUtil.FORMAT_DATE_TIME_23))
			WorkbookUtil.addCell(metricsSheet, 2, rowNum, 'Planning')
			WorkbookUtil.addCell(metricsSheet, 3, rowNum, am['planning_servers'])
			WorkbookUtil.addCell(metricsSheet, 4, rowNum, am['planning_applications'])
			WorkbookUtil.addCell(metricsSheet, 5, rowNum, am['planning_databases'])
			WorkbookUtil.addCell(metricsSheet, 6, rowNum, am['planning_network_devices'])
			WorkbookUtil.addCell(metricsSheet, 7, rowNum, am['planning_physical_storages'])
			WorkbookUtil.addCell(metricsSheet, 8, rowNum, am['planning_logical_storages'])
			WorkbookUtil.addCell(metricsSheet, 9, rowNum, am['planning_other_devices'])
			WorkbookUtil.addCell(metricsSheet, 10, rowNum, am['dependency_mappings'])
			WorkbookUtil.addCell(metricsSheet, 11, rowNum, am['tasks_all'])
			WorkbookUtil.addCell(metricsSheet, 12, rowNum, am['tasks_done'])
			WorkbookUtil.addCell(metricsSheet, 13, rowNum, am['total_persons'])
			WorkbookUtil.addCell(metricsSheet, 14, rowNum, am['total_user_logins'])
			WorkbookUtil.addCell(metricsSheet, 15, rowNum, am['active_user_logins'])

			rowNum++

			if (includeNonPlanning) {
				WorkbookUtil.addCell(metricsSheet, 2, rowNum, 'Non Planning')
				WorkbookUtil.addCell(metricsSheet, 3, rowNum, am['non_planning_servers'])
				WorkbookUtil.addCell(metricsSheet, 4, rowNum, am['non_planning_applications'])
				WorkbookUtil.addCell(metricsSheet, 5, rowNum, am['non_planning_databases'])
				WorkbookUtil.addCell(metricsSheet, 6, rowNum, am['non_planning_network_devices'])
				WorkbookUtil.addCell(metricsSheet, 7, rowNum, am['non_planning_physical_storages'])
				WorkbookUtil.addCell(metricsSheet, 8, rowNum, am['non_planning_logical_storages'])
				WorkbookUtil.addCell(metricsSheet, 9, rowNum, am['non_planning_other_devices'])
				rowNum++
			}
		}

		book.write(response.getOutputStream())
	}

	/**
	 * Returns a GSP view with the metric definitions.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def metricDefinitions() {
		render view: 'metricDefinitions', model: metricReportingService.getDefinitions()
	}

	/**
	 * Saves metric definitions to the settings table, and returns back what was saved.
	 *
	 * @param definitions The metric definitions JSON wrapped in a command object.
	 * @param version The version of the metric defintions, used by the setting table to avoid conflicts.
	 *
	 * @return the JSON that was save to the Settings table.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def saveMetricDefinitions(Integer version) {
		MetricDefinitionsCommand definitions = populateCommandObject(MetricDefinitionsCommand.class)
		validateCommandObject(definitions)

		renderAsJson(metricReportingService.saveDefinitions(definitions, version))
	}

	/**
	 * Tests Metric definitions, returning a list of maps, as data or any errors.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def testMetricDefinitions(){
		List<Map> data = []
		String metricCodesString = request.JSON.metricCodes
		MetricDefinitionsCommand definitions = populateCommandObject(MetricDefinitionsCommand.class)
		validateCommandObject(definitions)

		if(!metricCodesString){
			return renderErrorJson('Metric codes can not be empty')
		}

		List<String> codes =  definitions.testCodes()

		codes.each { String metricCode ->
			data.addAll(metricReportingService.testMetric(metricCode.trim(), definitions))
		}

		if(!data){
			return renderErrorJson('No data found.')
		}

		renderSuccessJson(data)

	}

	/**
	 * Tests Metric definitions, returning map with the number of metrics run, and the number of errors.
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def generateDailyMetrics (){
		renderSuccessJson(metricReportingService.generateDailyMetrics())
	}
}
