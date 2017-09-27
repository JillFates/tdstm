import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.TaskDependency
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavEntityAttribute
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.converters.JSON
import grails.transaction.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Environment
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.ServiceResults
import net.transitionmanager.domain.DataTransferAttributeMap
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectAssetMap
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.domain.Workflow
import net.transitionmanager.domain.WorkflowTransition
import net.transitionmanager.security.Permission
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.CommentService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.DeviceService
import net.transitionmanager.service.ImportService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.LicenseAdminService
import net.transitionmanager.service.MoveBundleService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProgressService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.StateEngineService
import net.transitionmanager.service.TaskImportExportService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UnauthorizedException
import net.transitionmanager.service.InvalidRequestException
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.UserService
import net.transitionmanager.utils.Profiler
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringEscapeUtils as SEU
import org.apache.commons.lang.math.NumberUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

@SuppressWarnings('GrMethodMayBeStatic')
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class AssetEntityController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	// This is a has table that sets what status from/to are available
	private static final Map statusOptionForRole = [
		ALL: [
			'*EMPTY*':                    AssetCommentStatus.list,
			(AssetCommentStatus.PLANNED): AssetCommentStatus.list,
			(AssetCommentStatus.PENDING): AssetCommentStatus.list,
			(AssetCommentStatus.READY):   AssetCommentStatus.list,
			(AssetCommentStatus.STARTED): AssetCommentStatus.list,
			(AssetCommentStatus.HOLD):    AssetCommentStatus.list,
			(AssetCommentStatus.COMPLETED):    AssetCommentStatus.list
		],
		LIMITED: [
			'*EMPTY*':                    [AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING, AssetCommentStatus.HOLD],
			(AssetCommentStatus.PLANNED): [AssetCommentStatus.PLANNED],
			(AssetCommentStatus.PENDING): [AssetCommentStatus.PENDING],
			(AssetCommentStatus.READY):   [AssetCommentStatus.READY, AssetCommentStatus.STARTED,
			                               AssetCommentStatus.COMPLETED,  AssetCommentStatus.HOLD],
			(AssetCommentStatus.STARTED): [AssetCommentStatus.READY, AssetCommentStatus.STARTED,
			                               AssetCommentStatus.COMPLETED,  AssetCommentStatus.HOLD],
			(AssetCommentStatus.COMPLETED):    [AssetCommentStatus.COMPLETED,  AssetCommentStatus.HOLD],
			(AssetCommentStatus.HOLD):    [AssetCommentStatus.HOLD]
		]
	]

	AssetEntityService assetEntityService
	CommentService commentService
	ControllerService controllerService
	DeviceService deviceService
	def filterService
	JdbcTemplate jdbcTemplate
	MoveBundleService moveBundleService
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProgressService progressService
	ProjectService projectService
	Scheduler quartzScheduler
	SecurityService securityService
	StateEngineService stateEngineService
	TaskImportExportService taskImportExportService
	TaskService taskService
	UserPreferenceService userPreferenceService
	UserService userService
	LicenseAdminService licenseAdminService
	ImportService importService

	/**
	 * To Filter the Data on AssetEntityList Page
	 * @param  Selected Filter Values
	 * @return Will return filters data to AssetEntity
	 */
	@HasPermission(Permission.AssetView)
	def filter() {
		if (params.rowVal) {
			if (!params.max) params.max = params.rowVal
			userPreferenceService.setPreference(PREF.MAX_ASSET_LIST, params.rowVal)
		} else {
			if (!params.max) {
				String maxAssetList = userPreferenceService.getPreference(PREF.MAX_ASSET_LIST)
				if (maxAssetList) {
					params.max = maxAssetList ?: 50
				}
			}
		}

		Project project = securityService.userCurrentProject
		params['project.id'] = project.id

		List<AssetEntity> assetEntityInstanceList = filterService.filter(params, AssetEntity).findAll { it.projectId == project.id }

		try {
			render(view: 'list',
				    model: [assetEntityInstanceList: assetEntityInstanceList, params: params, maxVal: params.max,
				            assetEntityCount: filterService.count(params, AssetEntity), projectId: project.id,
				            filterParams: com.zeddware.grails.plugins.filterpane.FilterUtils.extractFilterParams(params)])
		}
		catch (e) {
			redirect (action: 'list')
		}
	}

	/**
	 * The initial Asset Import form
	 */
	@HasPermission(Permission.AssetImport)
	def assetImport() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def prefMap = userPreferenceService.getImportPreferences()

		//List assetsByProject	= AssetEntity.findAllByProject(project)
		//List moveBundleInstanceList = MoveBundle.findAllByProject(project)

		def dataTransferSetImport = DataTransferSet.executeQuery("from DataTransferSet where transferMode IN ('B','I') ")
		def dataTransferSetExport = DataTransferSet.executeQuery("from DataTransferSet where transferMode IN ('B','E') ")

		def dataTransferBatchs = DataTransferBatch.findAllByProject(project).size()
		setBatchId 0
		setTotalAssets 0

		boolean isMSIE = false
		def userAgent = request.getHeader("User-Agent")
		if (userAgent.contains("MSIE") || userAgent.contains("Firefox"))
			isMSIE = true

		render(view: "importExport",
			    model: [projectId: project.id, error: params.error,
			            dataTransferSetImport: dataTransferSetImport,
			            dataTransferSetExport: dataTransferSetExport, prefMap: prefMap, message: params.message,
			            dataTransferBatchs: dataTransferBatchs, args: params.list("args"), isMSIE: isMSIE])
	}

	/**
	 * Render the Export form
	 */
	@HasPermission(Permission.AssetExport)
	def assetExport() {}

	/**
	 * Renders the export form.
	 */
	@HasPermission(Permission.AssetExport)
	def exportAssets() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			flash.message = " No Projects are Associated, Please select Project. "
			return
		}

		List dataTransferSetExport = DataTransferSet.executeQuery("from DataTransferSet where transferMode IN ('B','E') ")

		[
			// dataTransferBatchs: DataTransferBatch.countByProject(project),
			prefMap: userPreferenceService.getExportPreferences(),
			//moveBundleInstanceList: moveBundleService.findAllByProject(project),
			moveBundleList: moveBundleService.lookupList(project),
			dataTransferSetExport: dataTransferSetExport, projectId: project.id,
			useForPlanningArgName: MoveBundle.USE_FOR_PLANNING
		]
	}



	/**
	 * Upload the Data from the ExcelSheet
	 * @param DataTransferSet,Project,Excel Sheet
	 * @return currentPage(assetImport Page)
	 */
	@HasPermission(Permission.AssetImport)
	def upload() {

		// URL action to forward to if there is an error
		String forwardAction = 'assetImport'

		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			String warnMsg = flash.message
			flash.message = null
			forward(action: forwardAction, params: [error: warnMsg])
			return
		}

		// closure used to redirect user with an error message for sever issues
		def failWithError = { message ->
			log.error "upload() $securityService.currentUsername was $message"
			forward action: forwardAction, params: [error: message]
		}

		try {
			// Get the uploaded spreadsheet file
			MultipartHttpServletRequest mpr = (MultipartHttpServletRequest) request
			CommonsMultipartFile file = (CommonsMultipartFile) mpr.getFile("file")

			StringBuilder message = importService.validateAndProcessWorkbookSheets(project, file, params)
			forward action:forwardAction, params: [ message: message.toString() ]

		} catch (InvalidParamException ipe) {
			failWithError ipe.message
		} catch (Exception e) {
			failWithError e.message
		}
	}

	/**
	 * Used to kick off the export process that will schedule a Quartz job to run in background so that the user will get
	 * an immediate response and can poll for the status of the job.
	 */
	@HasPermission(Permission.AssetExport)
	def export() {
		def key = "AssetExport-" + UUID.randomUUID()
		progressService.create(key)

		log.info "Initiate Export"

		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl("TM-" + key, null, new Date(System.currentTimeMillis() + 2000))
		trigger.jobDataMap.putAll(params)

		trigger.jobDataMap.bundle = request.getParameterValues("bundle")
		trigger.jobDataMap.key = key
		trigger.jobDataMap.username = securityService.currentUsername
		trigger.jobDataMap.projectId = securityService.userCurrentProjectId
		trigger.jobDataMap.tzId = userPreferenceService.timeZone
		trigger.jobDataMap.userDTFormat = userPreferenceService.dateFormat
		trigger.jobDataMap[Profiler.KEY_NAME] = session[Profiler.KEY_NAME]

		trigger.setJobName('ExportAssetEntityJob')
		trigger.setJobGroup('tdstm-export-asset')
		quartzScheduler.scheduleJob(trigger)

		progressService.update(key, 1, 'In progress')

		renderSuccessJson(key: key)
	}

	@HasPermission(Permission.AssetExport)
	def downloadExport() {
		String key = params.key
		InputStream io = new FileInputStream(new File(progressService.getData(key, 'filename')))

		response.setContentType(progressService.getData(key, 'contenttype'))
		response.setHeader("Content-Disposition", progressService.getData(key, 'header'))

		OutputStream out = response.outputStream
		IOUtils.copy(io, out)
		out.flush()
		IOUtils.closeQuietly(io)
		IOUtils.closeQuietly(out)
	}

	/**
	 * Used for the assetEntity List to load the initial model. The list subsequently calls listJson to get the
	 * actual data which is rendered by JQ-Grid
	 * @param project, filter, number of properties
	 * @return model data to initate the asset list
	 **/
	@HasPermission(Permission.AssetView)
	def list() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		assetEntityService.getDeviceModelForList(project, session, params, userPreferenceService.timeZone)
	}

	/**
	 * Used by JQgrid to load assetList
	 */
	@HasPermission(Permission.AssetView)
	def listJson() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		renderAsJson assetEntityService.getDeviceDataForList(project, session, params)
	}

	@HasPermission(Permission.AssetDelete)
	def delete() {
		AssetEntity assetEntityInstance = AssetEntity.get(params.id)
		if (!assetEntityInstance) {
			flash.message = "AssetEntity not found with id $params.id"
			redirect(action: 'list')
			return
		}

		assetEntityService.deleteAsset(assetEntityInstance)
		assetEntityInstance.delete()
		flash.message = "AssetEntity $assetEntityInstance.assetName deleted"

		def redirectAsset = params.dstPath
		if (redirectAsset?.contains("room_")) {
			def newredirectAsset = redirectAsset.split("_")
			redirectAsset = newredirectAsset[0]
			session.setAttribute("RACK_ID", newredirectAsset[1])
		}

		switch (redirectAsset) {
			case "room": redirect(controller: 'room', action: 'list'); break
			case "rack": redirect(controller: 'rackLayouts', action: 'create'); break
			case "application": redirect(controller: 'application', action: 'list'); break
			case "database": redirect(controller: 'database', action: 'list'); break
			case "files": redirect(controller: 'files', action: 'list'); break
			case "dependencyConsole": forward(action: 'retrieveLists',
			                                  params: [entity: 'server',
			                                           dependencyBundle: session.getAttribute("dependencyBundle")])
				break
			case "assetAudit": render "AssetEntity $assetEntityInstance.assetName deleted"; break
			default: redirect(action: 'list')
		}
	}

	/**
	 * Remove the asset from project
	 */
	@HasPermission(Permission.AssetDelete)
	def remove() {
		AssetEntity assetEntity = AssetEntity.get(params.id)
		if (assetEntity) {
			ProjectAssetMap.executeUpdate('delete ProjectAssetMap where asset=?', [assetEntity])
			ProjectTeam.executeUpdate('update ProjectTeam set latestAsset=null where latestAsset=?', [assetEntity])
			AssetEntity.executeUpdate('''
				update AssetEntity
				set moveBundle=null, project=null, sourceTeamMt=null, targetTeamMt=null, sourceTeamLog=null,
				    targetTeamLog=null, sourceTeamSa=null, targetTeamSa=null, sourceTeamDba=null, targetTeamDba=null
				where id=?''', assetEntity.id)
			flash.message = "AssetEntity $assetEntity.assetName Removed from Project"
		}
		else {
			flash.message = "AssetEntity not found with id $params.id"
		}
		redirect(action: 'list')
	}

	/**
	 * remote link for asset entity dialog.
	 */
	@HasPermission(Permission.AssetEdit)
	def editShow() {
		Project project = securityService.userCurrentProject
		AssetEntity assetEntity = AssetEntity.get(params.id)
		List<EavEntityAttribute> entityAttributes = EavEntityAttribute.executeQuery(
			'from EavEntityAttribute where eavAttributeSet=? order by sortOrder', [assetEntity.attributeSetId])
		def items = entityAttributes.collect { EavEntityAttribute entityAttribute ->
			EavAttribute attribute = entityAttribute.attribute
			String code = attribute.attributeCode
			if (!AssetEntityService.bundleMoveAndClientTeams.contains(code) && code != "currentStatus" && code != "usize") {
				String frontEndLabel = attribute.frontendLabel
				if (AssetEntityService.customLabels.contains(frontEndLabel)) {
					frontEndLabel = project[code] ?: frontEndLabel
				}

				List<EavAttributeOption> attributeOptions = EavAttributeOption.findAllByAttribute(attribute, [sort: 'value', order: 'asc'])
				[label: frontEndLabel, attributeCode: code, frontendInput: attribute.frontendInput,
				 options: attributeOptions.collect { [option: it.value] }, modelId: assetEntity?.modelId,
				 value: assetEntity.(code)?.toString() ?: '', bundleId: assetEntity?.moveBundleId,
				 manufacturerId: assetEntity?.manufacturerId]
			}
		}
		renderAsJson items
	}

	@HasPermission(Permission.AssetView)
	def retrieveAttributes() {
		def items = []

		if (params.attribSet) {
			Project project = securityService.userCurrentProject
			List<EavEntityAttribute> entityAttributes = EavEntityAttribute.executeQuery('''
				from EavEntityAttribute
				where eavAttributeSet = :attributeSetId
				order by sortOrder
			''', [attributeSetId: params.attribSet])
			for (EavEntityAttribute it in entityAttributes) {
				List<EavAttributeOption> attributeOptions = EavAttributeOption.findAllByAttribute(it.attribute,
					[sort: 'value', order: 'asc'])
				def options = attributeOptions.collect { option -> [option: option.value] }
				String code = it.attribute.attributeCode
				if (!AssetEntityService.bundleMoveAndClientTeams.contains(code) &&
					 	code != "moveBundle" && code != "currentStatus" && code != "usize") {
					def frontEndLabel = it.attribute.frontendLabel
					if (AssetEntityService.customLabels.contains(frontEndLabel)) {
						frontEndLabel = project[code] ?: frontEndLabel
					}
					items << [label: frontEndLabel, attributeCode: code,
					          frontendInput: it.attribute.frontendInput, options: options]
				}
			}
		}

		renderAsJson items
	}

	@HasPermission(Permission.AssetView)
	def retrieveAssetAttributes() {
		def items = []
		if (params.assetId) {
			def entityAttributes = EavEntityAttribute.executeUpdate(
				'from EavEntityAttribute where eavAttributeSet = :attributeSetId order by sortOrder',
				[attributeSetId: AssetEntity.load(params.assetId).attributeSetId])
			for (EavEntityAttribute it in entityAttributes) {
				String code = it.attribute.attributeCode
				if (!AssetEntityService.bundleMoveAndClientTeams.contains(code) &&
						code != "currentStatus" && code != "usize") {
					items << [attributeCode: code, frontendInput: it.attribute.frontendInput]
				}
			}
		}
		renderAsJson items
	}

	@HasPermission(Permission.AssetEdit)
	def retrieveAutoCompleteDate(String autoCompAttribs) {
		def data = []
		if (autoCompAttribs) {
			Project project = securityService.userCurrentProject
			autoCompAttribs.split(",").each {
				def value = AssetEntity.executeQuery('''
					select distinct ''' + it + '''
					from AssetEntity
					where owner.id = :ownerId
				''', [ownerId: project.clientId])
				data << [value: value, attributeCode: it]
			}
		}
		renderAsJson data
	}

	@HasPermission([Permission.CommentView, Permission.TaskView])
	def listComments() {
		def assetEntityInstance = AssetEntity.get(params.id)
		def commentType = params.commentType

        def assetCommentsInstance = []

        if(securityService.hasPermission(Permission.TaskView) && (!commentType || commentType == AssetCommentType.TASK)) {
            assetCommentsInstance = taskService.findAllByAssetEntity(assetEntityInstance)
        }

        if(securityService.hasPermission(Permission.CommentView) && (!commentType || commentType == AssetCommentType.COMMENT)) {
            assetCommentsInstance.addAll(commentService.findAllByAssetEntity(assetEntityInstance))
        }
		def assetCommentsList = []
		def today = new Date()
		boolean viewUnpublished = securityService.viewUnpublished()
        boolean canEditComments = securityService.hasPermission(Permission.CommentEdit)
        boolean canEditTasks = securityService.hasPermission(Permission.TaskEdit)

		assetCommentsInstance.each {
			if (viewUnpublished || it.isPublished)
				assetCommentsList <<[commentInstance: it, assetEntityId: it.assetEntity.id,
				                     cssClass: it.dueDate < today ? 'Lightpink' : 'White',
				                     assetName: it.assetEntity.assetName, assetType: it.assetEntity.assetType,
				                     assignedTo: it.assignedTo?.toString() ?: '', role: it.role ?: '',
				                     canEditComments: canEditComments,
                                     canEditTasks: canEditTasks]
		}

		renderAsJson assetCommentsList
	}

	@HasPermission([Permission.CommentCreate, Permission.TaskCreate])
	def showComment() {
		def commentList = []
		def personResolvedObj
		def personCreateObj
		def dtCreated
		def dtResolved

		AssetComment assetComment = AssetComment.get(params.id)
		if (assetComment) {
			if (assetComment.createdBy) {
				personCreateObj = assetComment.createdBy.toString()
				dtCreated = TimeUtil.formatDateTime(assetComment.dateCreated)
			}
			if (assetComment.dateResolved) {
				personResolvedObj = assetComment.resolvedBy?.toString()
				dtResolved = TimeUtil.formatDateTime(assetComment.dateResolved)
			}

			String etStart = TimeUtil.formatDateTime(assetComment.estStart)
			String etFinish = TimeUtil.formatDateTime(assetComment.estFinish)
			String atStart = TimeUtil.formatDateTime(assetComment.actStart)
			String dueDate = TimeUtil.formatDate(assetComment.dueDate)
			String lastUpdated = TimeUtil.formatDateTime(assetComment.lastUpdated)

			def workflowTransition = assetComment?.workflowTransition
			String workflow = workflowTransition?.name

			def noteList = assetComment.notes.sort{it.dateCreated}
			def notes = []
			noteList.each {
				notes << [TimeUtil.formatDateTime(it.dateCreated, TimeUtil.FORMAT_DATE_TIME_3),
				          it.createdBy?.toString(), it.note, it.createdBy?.id]
			}

			// Get the name of the User Role by Name to display
			def roles = securityService.getRoleName(assetComment.role)

			def instructionsLinkURL
			def instructionsLinkLabel

			if (assetComment.instructionsLink) {
				List<String> instructionsLinkInfo = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)
				if (instructionsLinkInfo) {
					if (instructionsLinkInfo.size() > 1) {
						instructionsLinkURL = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)[1]
						instructionsLinkLabel = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)[0]
					}
					else {
						instructionsLinkURL = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)[0]
					}
				}
			}

			StringBuilder predecessorTable
			def predecessorList = []
			def taskDependencies = assetComment.taskDependencies
			if (taskDependencies.size() > 0) {
				taskDependencies = taskDependencies.sort{ it.predecessor.taskNumber }
				predecessorTable = new StringBuilder('<table cellspacing="0" style="border:0px;"><tbody>')
				taskDependencies.each { taskDep ->
					def task = taskDep.predecessor
					def css = taskService.getCssClassForStatus(task.status)
					def taskDesc = task.comment?.length()>50 ? task.comment.substring(0,50): task.comment
					predecessorList << [id: taskDep.id, taskId: task.id, category: task.category, desc: taskDesc, taskNumber: task.taskNumber, status: task.status]
					predecessorTable << """<tr class="$css" style="cursor:pointer;" onClick="showAssetComment($task.id, 'show')">""" <<
							"""<td>$task.category</td><td>${task.taskNumber ? task.taskNumber+':' :''}$taskDesc</td></tr>"""
				}
				predecessorTable.append('</tbody></table>')
			}
			def taskSuccessors = TaskDependency.findAllByPredecessor(assetComment)
			def successorsCount= taskSuccessors.size()
			def predecessorsCount = taskDependencies.size()
			StringBuilder successorTable
			def successorList = []
			if (taskSuccessors) {
				taskSuccessors = taskSuccessors.sort { it.assetComment.taskNumber }
				successorTable = new StringBuilder('<table  cellspacing="0" style="border:0px;" ><tbody>')
				taskSuccessors.each { successor ->
					def task = successor.assetComment
					def css = taskService.getCssClassForStatus(task.status)
					def succDesc = task.comment?.length()>50 ? task.comment.substring(0,50): task.comment
					successorList << [id: successor.id, taskId: task.id, category: task.category, desc: succDesc,
					                  taskNumber: task.taskNumber, status: task.status]
					successorTable << """<tr class="$css" style="cursor:pointer;" onClick="showAssetComment($task.id, 'show')">""" <<
							"""<td>$task.category</td><td>$task</td>"""
				}
				successorTable.append("""</tbody></table>""")
			}

			def cssForCommentStatus = taskService.getCssClassForStatus(assetComment.status)
			def canEdit = userCanEditComments(assetComment.commentType)

		// TODO : Security : Should reduce the person objects (create,resolved,assignedTo) to JUST the necessary properties using a closure
			assetComment.durationScale = assetComment.durationScale.toString()
			commentList << [
				assetComment:assetComment,
				durationScale:assetComment.durationScale.value(),
				personCreateObj:personCreateObj,
				personResolvedObj:personResolvedObj,
				dtCreated:dtCreated ?: "",
				dtResolved:dtResolved ?: "",
				assignedTo:assetComment.assignedTo?.toString() ?:'Unassigned',
				assetName:assetComment.assetEntity?.assetName ?: "",
				eventName:assetComment.moveEvent?.name ?: "",
				dueDate:dueDate,
				etStart:etStart,
				etFinish:etFinish,
				atStart:atStart,
				notes:notes,
				workflow:workflow,
				roles:roles?:'Unassigned',
				predecessorTable:predecessorTable ?: '',
				successorTable:successorTable ?: '',
				cssForCommentStatus: cssForCommentStatus,
				statusWarn: taskService.canChangeStatus (assetComment) ? 0 : 1,
				successorsCount: successorsCount,
				predecessorsCount: predecessorsCount,
				taskSpecId: assetComment.taskSpec,
				assetId: assetComment.assetEntity?.id ?: "",
				assetType: assetComment.assetEntity?.assetType,
				assetClass: assetComment.assetEntity?.assetClass?.toString(),
				predecessorList: predecessorList,
				successorList: successorList,
				instructionsLinkURL: instructionsLinkURL ?: "",
				instructionsLinkLabel: instructionsLinkLabel ?: "",
				canEdit: canEdit,
				lastUpdated: lastUpdated,
			]
		} else {
			def errorMsg = " Task Not Found : Was unable to find the Task for the specified id - $params.id "
			log.error "showComment: show comment view - $errorMsg"
			commentList << [error:errorMsg]
		}
		renderAsJson commentList
	}

	// def saveComment() { com.tdsops.tm.command.AssetCommentCommand cmd ->
	@HasPermission([Permission.CommentCreate, Permission.TaskCreate])
	def saveComment() {
		String tzId = userPreferenceService.timeZone
		String userDTFormat = userPreferenceService.dateFormat
		def map = commentService.saveUpdateCommentAndNotes(tzId, userDTFormat, params, true, flash)
		if (params.forWhom == "update") {
			def assetEntity = AssetEntity.get(params.prevAsset)
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)
			render(template: "commentList", model: [assetCommentList: assetCommentList])
		} else {
			renderAsJson map
		}
	}

	@HasPermission([Permission.CommentCreate, Permission.TaskCreate])
	def updateComment() {
		String tzId = userPreferenceService.timeZone
		String userDTFormat = userPreferenceService.dateFormat
		def map = commentService.saveUpdateCommentAndNotes(tzId, userDTFormat, params, false, flash)
		if (params.open == "view") {
			if (map.error) {
				flash.message = map.error
			}
			forward(action: "showComment", params: [id: params.id])
		} else if (params.view == "myTask") {
			if (map.error) {
				flash.message = map.error
			}
			forward(action: 'listComment', params: [view: params.view, tab: params.tab])
		} else if (params.open != "view") {
			renderAsJson map
		}
	}

	/* delete the comment record
	 * @param assetComment
	 * @author Lokanath
	 * @return assetCommentList
	 */
	@HasPermission(Permission.CommentDelete)
	def deleteComment() {
		// TODO - SECURITY - deleteComment - verify that the asset is part of a project that the user has the rights to delete the note
		AssetComment assetComment = AssetComment.get(params.id)
		if (assetComment) {
			TaskDependency.executeUpdate('delete TaskDependency where assetComment=:assetComment OR predecessor=:assetComment',
					[assetComment: assetComment])
			assetComment.delete()
		}

		// TODO - deleteComment - Need to be fixed to handle non-asset type comments
		def assetCommentsList = []
		if (params.assetEntity) {
			AssetComment.findAllByAssetEntityAndIdNotEqual(AssetEntity.load(params.assetEntity), params.id).each {
				assetCommentsList << [commentInstance: it, assetEntityId: it.assetEntity.id]
			}
		}
		renderAsJson assetCommentsList
	}

	/*----------------------------------
	 * @author: Lokanath Redy
	 * @param : fromState and toState
	 * @return: boolean value to validate comment field
	 *---------------------------------*/
	@HasPermission(Permission.WorkflowView)
	def retrieveFlag() {
		def moveBundleInstance = MoveBundle.get(params.moveBundle)
		def toState = params.toState
		def fromState = params.fromState
		def status = []
		def flag = stateEngineService.getFlags(moveBundleInstance.workflowCode,"SUPERVISOR", fromState, toState)
		if (flag?.contains("comment") || flag?.contains("issue")) {
			status << ['status':'true']
		}
		renderAsJson status
	}

	/*-----------------------------------------
	 *@param : state value
	 *@return: List of valid stated for param state
	 *----------------------------------------*/
	@HasPermission(Permission.WorkflowView)
	def retrieveStates(def state,def assetEntity) {
		def stateIdList = []
		def validStates
		if (state) {
			validStates = stateEngineService.getTasks(assetEntity.moveBundle.workflowCode,"SUPERVISOR", state)
		} else {
			validStates = ["Ready"]
			//stateEngineService.getTasks("STD_PROCESS","TASK_NAME")
		}
		validStates.each {
			stateIdList<<stateEngineService.getStateIdAsInt(assetEntity.moveBundle.workflowCode,it)
		}
		return stateIdList
	}

	@HasPermission(Permission.AssetImport)
	def retrieveProgress() {
		def importedData
		def progressData = []
		Long batchId = getBatchId()
		Integer total = getTotalAssets()
		if (batchId) {
			importedData = jdbcTemplate.queryForList('''
				select count(distinct row_id) as rows
				from data_transfer_value
				where data_transfer_batch_id=?''', batchId).rows
		}
		progressData << [imported: importedData, total: total]
		renderAsJson progressData
	}

	/**
	 * Presents the CRUD form for a new Device entry form
	 * @param params.redirectTo - used to redirect the user back to the appropriate page afterward
	 * @return : render to create page based on condition as if redirectTo is assetAudit then redirecting
	 * to auditCreate view
	 */
	@HasPermission(Permission.AssetCreate)
	@Transactional(readOnly = true)
	def create() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def (device, model) = assetEntityService.getDeviceAndModelForCreate(project, params)

		model.action = 'save'
		model.whom = 'Device'

		// TODO : JPM 10/2014 : I'm guessing this is needed to make the save action work correctly
		model.redirectTo = params.redirectTo ?: 'list'

		if (params.redirectTo == "assetAudit") {
			model.source = params.source
			model.assetType = params.assetType
			render(template: "createAuditDetails", model: model)
		} else {
			// model.each { n,v -> println "$n:\t$v"}
			render(view: 'createEdit', model: model)
		}
	}

	/**
	 * Render the edit view.
	 * @param : redirectTo
	 * @return : render to edit page based on condition as if 'redirectTo' is roomAudit then redirecting
	 * to auditEdit view
	 */
	@HasPermission(Permission.AssetEdit)
	@Transactional(readOnly = true)
	def edit() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def (device, Map model) = assetEntityService.getDeviceModelForEdit(project, params.id, params)

		if (!device) {
			render '<span class="error">Unable to find asset to edit</span>'
			return
		}

		if (params.redirectTo == "roomAudit") {
			// TODO : JPM 9/2014 : Need to determine the assetType
			model.assetType = params.assetType
			model.source = params.source

			render(template: "auditEdit", model: model)
			return
		}

		model.action = 'update'

		// model.each { n,v -> println "$n:\t$v"}
		render (view: 'createEdit', 'model' : model)
	}

	/**
	 * Render the clone view.
	 * @return : render the clone view for cloning assets
	 */
	@HasPermission(Permission.AssetCreate)
	def cloneEntity() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def (device, Map model) = assetEntityService.getDeviceModelForEdit(project, params.id, params)

		return [asset: model]
	}

	/**
	 * Used to create and save a new device and associated dependencies. Upon success or failure it will redirect the
	 * user to the place that they came from based on the params.redirectTo param. The return content varies based on that
	 * param as well.
	 */
	@HasPermission(Permission.AssetCreate)
	def save() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, deviceService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.AE?.JQ_FILTERS = params
	}

	/**
	 * Update an AssetEntity.
	 * @param redirectTo : a flag to redirect view to page after update
	 * @param id : id of assetEntity
	 * @return : render to appropriate view
	 */
	@HasPermission(Permission.AssetEdit)
	def update() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, deviceService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.AE?.JQ_FILTERS = params
	}

	/**
	* Renders the detail of an AssetEntity
	*/
	@HasPermission(Permission.AssetView)
	def show() {

		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def assetId = params.id
		def assetEntity = controllerService.getAssetForPage(this, project, AssetClass.DEVICE, assetId)

		if (!assetEntity) {
			flash.message = "Unable to find asset within current project with id $params.id"
			log.warn "show - asset id ($params.id) not found for project ($project.id) by user $securityService.currentUsername"
			renderAsJson(errMsg: flash.message)
		} else {
			def model = deviceService.getModelForShow(project, assetEntity, params)
			if (!model) {
				render "Unable to load specified asset"
				return
			}

			if (params.redirectTo == "roomAudit") {
				// model.source = params.source
				// model.assetType = params.assetType
				render(template: "auditDetails", model: model)
			}

			//model.each { n,v -> println "$n:\t$v"}
			return model
		}
	}

	/**
	 * Get Manufacturers ordered by manufacturer name display at
	 * assetEntity CRUD and AssetAudit CRUD
	 * @param assetType : requested assetType for which we need to get manufacturer list
	 * @return : render to manufacturerView
	 */
	@HasPermission(Permission.ManufacturerView)
	def retrieveManufacturersList() {
		def assetType = params.assetType
		def manufacturers = Model.executeQuery("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
		def prefVal =  userPreferenceService.getPreference(PREF.LAST_MANUFACTURER)
		def selectedManu = prefVal ? Manufacturer.findByName(prefVal)?.id : null
		render(view: 'manufacturerView', model: [manufacturers: manufacturers, selectedManu: selectedManu, forWhom: params.forWhom])
	}

	/**
	 * Used to set showAllAssetTasks preference, which is used to show all or hide the inactive tasks
	 */
	@HasPermission(Permission.UserGeneralAccess)
	def setShowAllPreference() {
		userPreferenceService.setPreference(PREF.SHOW_ALL_ASSET_TASKS, params.selected == '1')
		render true
	}

	/**
	 * Used to set showAllAssetTasks preference, which is used to show all or hide the inactive tasks
	 */
	@HasPermission(Permission.UserGeneralAccess)
	@HasPermission(Permission.TaskPublish)
	def setViewUnpublishedPreference () {
		userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED,
			params.viewUnpublished == '1' || params.viewUnpublished == 'true')
		render true
	}

	/**
	 * Get Models to display at assetEntity CRUD and AssetAudit CRUD
	 * @param assetType : requested assetType for which we need to get manufacturer list
	 * @return : render to manufacturerView
	 */
	@HasPermission(Permission.ModelView)
	def retrieveModelsList() {
		def manufacturer = params.manufacturer
		def models=[]
		if (manufacturer!="null") {
			def manufacturerInstance = Manufacturer.read(manufacturer)
			models=assetEntityService.getModelSortedByStatus(manufacturerInstance)
		}
		render (view :'_deviceModelSelect', model:[models : models, forWhom:params.forWhom])
	}

	/**
	 * Used to generate the List for Task Manager, which leverages a shared closure with listComment
	 */
	@HasPermission(Permission.TaskManagerView)
	def listTasks() {
		licenseAdminService.checkValidForLicenseOrThrowException()
		securityService.requirePermission 'TaskManagerView'

		Project project = controllerService.getProjectForPage(this, 'to view Tasks')
		if (!project) return

		licenseAdminService.isLicenseCompliant(project)

		try {
			if (params.containsKey('viewUnpublished') && params.viewUnpublished in ['0', '1']) {
				userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED, params.viewUnpublished == '1')
			}

			params.commentType = AssetCommentType.TASK

			if (params.initSession) {
				session.TASK = [:]
			}

			List<MoveEvent> moveEvents = MoveEvent.findAllByProject(project)
			def filters = session.TASK?.JQ_FILTERS

			// Deal with the parameters
			def taskPref = assetEntityService.getExistingPref('Task_Columns')
			def assetCommentFields = AssetComment.getTaskCustomizeFieldAndLabel()
			def modelPref = [:]
			taskPref.each { key, value -> modelPref[key] = assetCommentFields[value] }
			long filterEvent = NumberUtil.toPositiveLong(params.moveEvent, 0L)

			// column name and its associated javascript cell formatter name
			def formatterMap = [assetEntity:'assetFormatter', estStart:'estStartFormatter', estFinish: 'estFinishFormatter']
			def moveEvent


			if (params.containsKey("justRemaining")) {
				userPreferenceService.setPreference(PREF.JUST_REMAINING, params.justRemaining)
			}
			if (params.moveEvent) {
				// zero (0) = All events
				// log.info "listCommentsOrTasks: Handling MoveEvent based on params $params.moveEvent"
				if (params.moveEvent != '0') {
					moveEvent = MoveEvent.findByIdAndProject(params.moveEvent,project)
					if (!moveEvent) {
						log.warn "listCommentsOrTasks: $person tried to access moveEvent $params.moveEvent that was not found in project $project.id"
					}
				}
			} else {
				// Try getting the move Event from the user's session
				def moveEventId = userPreferenceService.moveEventId
				// log.info "listCommentsOrTasks: getting MOVE_EVENT preference $moveEventId for $person"
				if (moveEventId) {
					moveEvent = MoveEvent.findByIdAndProject(moveEventId,project)
				}
			}
			if (moveEvent && params.section != 'dashBoard') {
				// Add filter to SQL statement and update the user's preferences
				userPreferenceService.setMoveEventId moveEvent.id
				filterEvent = moveEvent.id
			} else {
				userPreferenceService.removePreference(PREF.MOVE_EVENT)
			}
			def justRemaining = userPreferenceService.getPreference(PREF.JUST_REMAINING) ?: "1"
			// Set the Checkbox values to that which were submitted or default if we're coming into the list for the first time
			def justMyTasks = params.containsKey('justMyTasks') ? params.justMyTasks : "0"
			boolean viewUnpublished = (userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) == 'true') ? '1' : '0'
			def timeToRefresh = userPreferenceService.getPreference(PREF.TASKMGR_REFRESH)
			def entities = assetEntityService.entityInfo(project)
			def moveBundleList = MoveBundle.findAllByProject(project, [sort: 'name'])
			def companiesList = partyRelationshipService.getCompaniesList()
			def role = filters?.role ?: params.role ?: ''
			def status = params.status ?: filters?.status ?: ''
			return [
					timeToUpdate: timeToRefresh ?: 60,
					servers: entities.servers,
					applications: entities.applications,
			        dbs: entities.dbs,
					files: entities.files,
					networks: entities.networks,
					moveEvents:moveEvents,
			        dependencyType: entities.dependencyType,
					dependencyStatus: entities.dependencyStatus,
			        assetDependency: new AssetDependency(),
					filterEvent: filterEvent,
					justRemaining: justRemaining,
			        justMyTasks: justMyTasks,
					filter: params.filter,
					comment: filters?.comment ?:'',
					role: role,
			        taskNumber: filters?.taskNumber ?:'',
					assetName: filters?.assetEntity ?:'',
					modelPref: modelPref,
			        assetType: filters?.assetType ?:'',
					dueDate: filters?.dueDate ?:'',
					status: status,
			        assignedTo: filters?.assignedTo ?:'',
					category: filters?.category ?:'',
					moveEvent: moveEvent,
			        moveBundleList: moveBundleList,
					viewUnpublished: viewUnpublished,
					taskPref: taskPref,
					formatterMap: formatterMap,
			        staffRoles: taskService.getTeamRolesForTasks(),
					assetCommentFields: assetCommentFields.sort { it.value },
			        sizePref: userPreferenceService.getPreference(PREF.ASSET_LIST_SIZE) ?: '25',
			        partyGroupList: companiesList,
					company: project.client,
					step: params.step]
		} catch (RuntimeException e) {
			log.error e.message, e
			response.sendError(401, "Unauthorized Error")
		}
	}

	/**
	 * Generates the List of Comments, which leverages a shared closeure with the above listTasks controller.
	 */
	@HasPermission(Permission.CommentView)
	def listComment() {
		Project project = controllerService.getProjectForPage(this, 'to view Comments')
		if (!project) return

		def entities = assetEntityService.entityInfo(project)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		boolean canEditComments = securityService.hasPermission(Permission.AssetEdit)

		[rediectTo: 'comment', servers: entities.servers, applications: entities.applications, dbs: entities.dbs,
		 files: entities.files, dependencyType: entities.dependencyType, dependencyStatus: entities.dependencyStatus,
		 assetDependency: new AssetDependency(), moveBundleList: moveBundleList, canEditComments: canEditComments]
	}

	/**
	 * Used to generate list of comments using jqgrid
	 * @return : list of tasks as JSON
	 */
	@HasPermission(Permission.CommentView)
	def listCommentJson() {
		String sortIndex = params.sidx ?: 'lastUpdated'
		String sortOrder = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows

		Project project = securityService.userCurrentProject
		List<Date> lastUpdatedTime = params.lastUpdated ? AssetComment.executeQuery('''
			select lastUpdated from AssetComment
			where project=:project
			  and commentType=:comment
			  and str(lastUpdated) like :lastUpdated
		''', [project: project, comment: AssetCommentType.COMMENT, lastUpdated: '%' + params.lastUpdated + '%']) : []

		def assetCommentList = AssetComment.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			eq("commentType", AssetCommentType.COMMENT)
			createAlias("assetEntity","ae")
			if (params.comment) {
				ilike('comment', "%$params.comment%")
			}
			if (params.commentType) {
				ilike('commentType', "%$params.commentType%")
			}
			if (params.category) {
				ilike('category', "%$params.category%")
			}
			if (lastUpdatedTime) {
				'in'('lastUpdated',lastUpdatedTime)
			}
			if (params.assetType) {
				ilike('ae.assetType',"%$params.assetType%")
			}
			if (params.assetName) {
				ilike('ae.assetName',"%$params.assetName%")
			}
			String sid = sortIndex == 'assetName' || sortIndex  == 'assetType' ? "ae.$sortIndex" : sortIndex
			order(new Order(sid, sortOrder == 'asc').ignoreCase())
		}

		int totalRows = assetCommentList.totalCount
		int numberOfPages = Math.ceil(totalRows / maxRows)

		def results = assetCommentList?.collect {
			[id: it.id,
			 cell: ['',
			        StringUtil.ellipsis(it.comment ?: '', 50).replace("\n", ""),
			        TimeUtil.formatDate(it.lastUpdated),
			        it.commentType,
			        it.assetEntity?.assetName ?:'',
			        it.assetEntity?.assetType ?:'',
			        it.category,
			        it.assetEntity?.id,
			        it.assetEntity?.assetClass?.toString()
				]
			]
		}

		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	/**
	 * This will be called from TaskManager screen to load jqgrid
	 * @return : list of tasks as JSON
	 */
	@HasPermission(Permission.TaskManagerView)
	def listTaskJSON() {
		String sortIndex =  params.sidx ?: session.TASK?.JQ_FILTERS?.sidx
		String sortOrder =  params.sord ?: session.TASK?.JQ_FILTERS?.sord
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows

		userPreferenceService.setPreference(PREF.ASSET_LIST_SIZE, maxRows)

		Project project = securityService.userCurrentProject
		def today = new Date().clearTime()
		def moveEvent
		if (params.moveEvent) {
			// zero (0) = All events
			// log.info "listCommentsOrTasks: Handling MoveEvent based on params $params.moveEvent"
			if (params.moveEvent != '0') {
				moveEvent = MoveEvent.findByIdAndProject(params.moveEvent,project)
				if (!moveEvent) {
					log.warn "listCommentsOrTasks: $securityService.currentUsername tried to access moveEvent $params.moveEvent that was not found in project $project.id"
				}
			}
		} else {
			// Try getting the move Event from the user's session
			def moveEventId = userPreferenceService.moveEventId
			// log.info "listCommentsOrTasks: getting MOVE_EVENT preference $moveEventId for $securityService.currentUsername"
			if (moveEventId) {
				moveEvent = MoveEvent.findByIdAndProject(moveEventId,project)
			}
		}
		if (moveEvent) {
			userPreferenceService.setMoveEventId moveEvent.id
		}

		def assetType = params.filter ? ApplicationConstants.assetFilters[params.filter ] : []

		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%$params.moveBundle%", project) : []
		def models = params.model ? Model.findAllByModelNameIlike("%$params.model%") : []

		def taskNumbers = params.taskNumber ? AssetComment.findAll("from AssetComment where project =:project \
			and taskNumber like '%$params.taskNumber%'",[project:project])?.taskNumber : []

		def durations = params.duration ? AssetComment.findAll("from AssetComment where project =:project \
			and duration like '%$params.duration%'",[project:project])?.duration : []

		boolean viewUnpublished = securityService.viewUnpublished()

		// TODO TM-2515 - SHOULD NOT need ANY of these queries as they should be implemented directly into the criteria
		/*
		 oluna 170921 (for the developer of the future) : the use of "DATE like param" in the queries is interpolated due to the fact that we are comparing against the
		 	String "Date" representation of the field ("2017-05-13 17:35:24") and if we try to use "named-params" it will fail the type-check (comparing Strings to Dates)
		 	so leave this implementation alone unless you find a better way :)
		*/
		def dates = params.dueDate ? AssetComment.findAll("from AssetComment where project =:project and dueDate like '%$params.dueDate%' ",[project:project])?.dueDate : []
		def estStartdates = params.estStart ? AssetComment.findAll("from AssetComment where project=:project and estStart like '%$params.estStart%' ",[project:project])?.estStart : []
		def actStartdates = params.actStart ? AssetComment.findAll("from AssetComment where project=:project and actStart like '%$params.actStart%' ",[project:project])?.actStart : []
		def dateCreateddates = params.dateCreated ? AssetComment.findAll("from AssetComment where project=:project and dateCreated like '%$params.dateCreated%' ",[project:project])?.dateCreated : []
		def dateResolveddates = params.dateResolved ? AssetComment.findAll("from AssetComment where project=:project and dateResolved like '%$params.dateResolved%' ",[project:project])?.dateResolved : []
		def estFinishdates = params.estFinish ? AssetComment.findAll("from AssetComment where project=:project and estFinish like '%$params.estFinish%' ",[project:project])?.estFinish : []
		def statusUpdated = params.statusUpdated ? AssetComment.findAll("from AssetComment where project=:project and statusUpdated like '%$params.statusUpdated%' ",[project:project])?.statusUpdated : []

		// TODO TM-2515 - ONLY do the lookups if params used by the queries are populated
		def assigned = params.assignedTo ? Person.findAllByFirstNameIlikeOrLastNameIlike("%$params.assignedTo%","%$params.assignedTo%") : []
		def createdBy = params.createdBy ? Person.findAllByFirstNameIlikeOrLastNameIlike("%$params.createdBy%","%$params.createdBy%") : []
		def resolvedBy = params.resolvedBy ? Person.findAllByFirstNameIlikeOrLastNameIlike("%$params.resolvedBy%","%$params.resolvedBy%") : []

		def tasks = AssetComment.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			eq("commentType", AssetCommentType.TASK)
			createAlias('assetEntity', 'assetEntity', CriteriaSpecification.LEFT_JOIN)
			createAlias("moveEvent", "moveEvent", CriteriaSpecification.LEFT_JOIN)
			createAlias("assetEntity.moveBundle", "moveBundle", CriteriaSpecification.LEFT_JOIN)
			if (params.step) {
				createAlias("workflowTransition", "workflowTransition", CriteriaSpecification.LEFT_JOIN)
				eq("workflowTransition.id", params.step.toLong())
			}
			if (!viewUnpublished) {
				eq("isPublished", true)
			}
			if (params.assetType) {
				ilike('assetEntity.assetType', "%$params.assetType%")
			}
			if (params.assetName) {
				ilike('assetEntity.assetName', "%$params.assetName%")
			}
			if (params.comment) {
				ilike('comment', "%$params.comment%")
			}
			if (params.resolution) {
				ilike('resolution', "%$params.resolution%")
			}
			if (params.instructionsLink) {
				ilike('instructionsLink', "%$params.instructionsLink%")
			}
			if (params.status) {
				ilike('status', "%$params.status%")
			}
			if (params.role) {
				ilike('role', "%$params.role%")
			}
			if (params.commentType) {
				ilike('commentType', "%$params.commentType%")
			}
			if (params.displayOption) {
				ilike('displayOption', "%$params.displayOption%")
			}
			if (durations) {
				'in'('duration', durations)
			}
			if (params.durationScale) {+
				ilike('durationScale', "%$params.durationScale%")
			}
			if (params.category) {
				ilike('category', "%$params.category%")
			}
			if (params.attribute) {
				ilike('attribute', "%$params.attribute%")
			}
			if (params.autoGenerated) {
				eq('autoGenerated', params.autoGenerated)
			}
			if (params.event) {
				ilike("moveEvent.name", "%$params.event%")
			}
			if (params.bundle) {
				ilike("moveBundle.name", "%$params.bundle%")
			}
			if (taskNumbers) {
				'in'('taskNumber', taskNumbers)
			}
			if (params.isResolved?.isNumber()) {
				eq('isResolved', params.int('isResolved'))
			}
			if (params.priority?.isNumber()) {
				eq('priority', params.int('priority'))
			}
			if (StringUtil.isLike(params.isPublished, 'true')) {
				eq('isPublished', true)
			} else if (StringUtil.isLike(params.isPublished, 'false')) {
					eq('isPublished', false)
			}
			if (StringUtil.isLike(params.sendNotification, 'true')) {
				eq('sendNotification', true)
			} else if (StringUtil.isLike(params.sendNotification, 'false')) {
					eq('sendNotification', false)
			}
			if (params.hardAssigned?.isNumber()) {
				eq('hardAssigned', params.int('hardAssigned'))
			}

			if (dates) {
				and {
					or {
						'in'('dueDate', dates)
						'in'('estFinish', dates)
					}
				}
			}

			if (estStartdates) {
				'in'('estStart',estStartdates)
			}
			if (actStartdates) {
				'in'('actStart',actStartdates)
			}
			if (estFinishdates) {
				'in'('estFinish',estFinishdates)
			}
			if (statusUpdated) {
				'in'('statusUpdated',statusUpdated)
			}
			if (dateCreateddates) {
				'in'('dateCreated',dateCreateddates)
			}
			if (dateResolveddates) {
				'in'('dateResolved',dateResolveddates)
			}
			if (createdBy) {
				'in'('createdBy', createdBy)
			}
			if (resolvedBy) {
				'in'('resolvedBy', resolvedBy)
			}
			if (assigned) {
				'in'('assignedTo', assigned)
			}
			if (sortIndex && sortOrder) {
				String sortIdx
				switch(sortIndex) {
					case "assetName":
					case "assetType":
						sortIdx = "assetEntity." + sortIndex
						break
					case "bundle":
						sortIdx = "moveBundle.name"
						break
					case "event":
						sortIdx = "moveEvent.name"
						break
					default:
						sortIdx = sortIndex
						break
				}
				order(sortIdx, sortOrder)

			} else {
				and {
					order('score','desc')
					order('taskNumber','asc')
					order('dueDate','asc')
					order('dateCreated','desc')
				}
			}
			if (moveEvent) {
				eq("moveEvent", moveEvent)
			}
			if (params.justRemaining == "1") {
				ne("status", AssetCommentStatus.COMPLETED)
			}
			if (params.justMyTasks == "1") {
				eq("assignedTo", securityService.loadCurrentPerson())
			}
			switch(params.filter) {
				case "openIssue" :
					'in'('category', AssetComment.discoveryCategories)
					break
				case "dueOpenIssue":
					'in'('category', AssetComment.discoveryCategories)
					lt('dueDate',today)
					break
				case "analysisIssue" :
					eq("status", AssetCommentStatus.READY)
					'in'('category', AssetComment.planningCategories)
					break
				case "generalOverDue" :
					'in'('category', AssetComment.planningCategories)
					lt('dueDate',today)
					break
			}
		}

		def createJsonTime = new Date()

		def totalRows = tasks.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)
		def updatedTime
		def dueClass
		def estStartClass
		def estFinishClass
		def nowGMT = TimeUtil.nowGMT()
		def taskPref = assetEntityService.getExistingPref('Task_Columns')


		def results = tasks?.collect {
			def isRunbookTask = it.isRunbookTask()
			updatedTime =  isRunbookTask ? it.statusUpdated : it.lastUpdated

			def elapsed = TimeUtil.elapsed(it.statusUpdated, nowGMT)
			def elapsedSec = elapsed.toMilliseconds() / 1000

			// clear out the CSS classes for overDue
			dueClass = ''

			if (it.estFinish) {
				elapsed = TimeUtil.elapsed(it.estFinish, nowGMT)
				elapsedSec = elapsed.toMilliseconds() / 1000
				if (elapsedSec > 300) {
					dueClass = 'task_overdue'
				}
			}

			Map estimatedColumnsCSS = getEstimatedColumnsCSS(it, nowGMT)

			estStartClass = estimatedColumnsCSS['estStartClass']
			estFinishClass = estimatedColumnsCSS['estFinishClass']

			String dueDate = TimeUtil.formatDate(it.dueDate)

			// Clears time portion of dueDate for date comparison
			Date due = it.dueDate?.clearTime()

			// Highlight Due Date column for tardy and late tasks
			if (it.dueDate && it.isActionable()) {
				if (due > today) {
					dueClass = ''
				} else {
					if (due < today)
						dueClass = 'task_late'
					else
						dueClass = 'task_tardy' // due == today
				}
			}

			def deps = TaskDependency.findAllByPredecessor(it)
			def depCount = 0
			deps.each {
				if (viewUnpublished || (it.assetComment?.isPublished && it.predecessor?.isPublished))
					++depCount
			}

			// Have the dependency count be a link to the Task Neighborhood graph if there are dependencies
			def nGraphUrl = depCount == 0 ? depCount : '<a href="' + createLink(controller:'task', action:'taskGraph') +
					'?neighborhoodTaskId=' + it.id + '" target="_blank">' + depCount + '</a>'

			def status = it.status
			def userSelectedCols = []
			(1..5).each { colId ->
				def value = taskManagerValues(taskPref[colId.toString()], it)
				userSelectedCols << (value?.getClass()?.isEnum() ? value?.value() : value)
			}

			def instructionsLinkURL
			if (HtmlUtil.isMarkupURL(it.instructionsLink)) {
				instructionsLinkURL = HtmlUtil.parseMarkupURL(it.instructionsLink)[1]
			} else {
				instructionsLinkURL = it.instructionsLink
			}

			[ cell: [
					'',
					it.taskNumber,
					it.comment,
					userSelectedCols[0], // taskManagerValues(taskPref["1"],it),
					userSelectedCols[1], // taskManagerValues(taskPref["2"],it),
					updatedTime ? TimeUtil.ago(updatedTime, TimeUtil.nowGMT()) : '',
					dueDate,
					status ?: '',
					userSelectedCols[2], // taskManagerValues(taskPref["3"],it),
					userSelectedCols[3], // taskManagerValues(taskPref["4"],it),
					userSelectedCols[4], // taskManagerValues(taskPref["5"],it),
					nGraphUrl,
					it.score ?: 0,
					status ? 'task_' + it.status.toLowerCase() : 'task_na',
					dueClass,
					it.assetEntity?.id, // 16
					it.assetEntity?.assetType, // 17
					it.assetEntity?.assetClass?.toString(), // 18
					instructionsLinkURL, // 19
					estStartClass,	// 20
					estFinishClass	// 21
			],
			  id:it.id
			]
		}
		// If sessions variables exists, set them with params and sort
		session.TASK?.JQ_FILTERS = params
		session.TASK?.JQ_FILTERS?.sidx = sortIndex
		session.TASK?.JQ_FILTERS?.sord = sortOrder

		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	/**
	 * Get assetColumn value based on field name. .
	 */
	private taskManagerValues(value, task) {
		def result
		switch (value) {
			case 'assetName': result = task.assetEntity?.assetName; break
			case 'assetType': result = task.assetEntity?.assetType; break
			case 'assignedTo': result = (task.hardAssigned ? '*' : '') + (task.assignedTo?.toString() ?: ''); break
			case 'resolvedBy': result = task.resolvedBy?.toString() ?: ''; break
			case 'createdBy': result = task.createdBy?.toString() ?: ''; break
			case ~/statusUpdated|estFinish|dateCreated|dateResolved|estStart|actStart|lastUpdated/:
				result = TimeUtil.formatDateTime(task[value])
			break
			case "event": result = task.moveEvent?.name; break
			case "bundle": result = task.assetEntity?.moveBundle?.name; break
			default: result = task[value]
		}
		return result
	}

	/**
	 * Get AssetOptions by type to display at admin's AssetOption page .
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def assetOptions() {
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		def environment = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		[planStatusOptions: planStatusOptions, priorityOption: priorityOption, dependencyType: dependencyType,
		 dependencyStatus: dependencyStatus, environment: environment]
	}

	/**
	 * Save AssetOptions by type to display at admin's AssetOption page .
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def saveAssetoptions() {
		AssetOptions assetOption = new AssetOptions()
		switch(params.assetOptionType) {
			case 'planStatus':
				assetOption.type = AssetOptions.AssetOptionsType.STATUS_OPTION
				assetOption.value = params.planStatus
				break
			case 'Priority':
				assetOption.type = AssetOptions.AssetOptionsType.PRIORITY_OPTION
				assetOption.value = params.priorityOption
				break
			case 'dependency':
				assetOption.type = AssetOptions.AssetOptionsType.DEPENDENCY_TYPE
				assetOption.value = params.dependencyType
				break
			case 'environment':
				assetOption.type = AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION
				assetOption.value = params.environment
				break
			default:
				assetOption.type = AssetOptions.AssetOptionsType.DEPENDENCY_STATUS
				assetOption.value = params.dependencyStatus
		}

		if (!assetOption.save(flush:true)) {
			assetOption.errors.allErrors.each { log.error  it }
		}

		renderAsJson(id: assetOption.id)
	}

	/**
	 * Deletes AssetOptions by type from admin's AssetOption page .
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def deleteAssetOptions() {
		String idParamName
		switch(params.assetOptionType) {
			case 'planStatus':  idParamName = 'assetStatusId'; break
			case 'Priority':    idParamName = 'priorityId'; break
			case 'dependency':  idParamName = 'dependecyId'; break
			case 'environment': idParamName = 'environmentId'; break
			default:            idParamName = 'dependecyId'; break
		}

		AssetOptions assetOption = AssetOptions.get(params[idParamName])
		assetOption.delete(flush: true)
		render assetOption.id
	}

	/**
	 * Render Summary of assigned and unassgined assets.
	 */
	@HasPermission(Permission.AssetView)
	def assetSummary() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		int totalAsset = 0
		int totalPhysical = 0
		int totalApplication = 0
		int totalDatabase = 0
		int totalFiles = 0

		List assetSummaryList = MoveBundle.findAllByProject(project, [sort: 'name']).collect { MoveBundle moveBundle ->
			int physicalCount = AssetEntity.executeQuery('''
				select count(*) from AssetEntity
				where moveBundle=:mb
				  and assetClass=:ac
				  and coalesce(assetType) not in (:at)
			''', [mb: moveBundle, ac: AssetClass.DEVICE, at: AssetType.virtualServerTypes])[0]
			int assetCount = AssetEntity.countByMoveBundleAndAssetTypeInList(moveBundle, AssetType.serverTypes, params)
			int applicationCount = Application.countByMoveBundle(moveBundle)
			int databaseCount = Database.countByMoveBundle(moveBundle)
			int filesCount = Files.countByMoveBundle(moveBundle)

			totalAsset += assetCount
			totalPhysical += physicalCount
			totalApplication += applicationCount
			totalDatabase += databaseCount
			totalFiles += filesCount

			[name: moveBundle, assetCount: assetCount, applicationCount: applicationCount, physicalCount: physicalCount,
			 databaseCount: databaseCount, filesCount: filesCount, id: moveBundle.id]
		}

		[assetSummaryList: assetSummaryList, totalAsset: totalAsset, totalApplication: totalApplication,
		 totalDatabase: totalDatabase,totalPhysical: totalPhysical, totalFiles: totalFiles]
	}

	/**
	 * Used by the dependency console to load up the individual tabs for a dependency bundle
	 * @param String entity - the entity type to view (server,database,file,app)
	 * @param Integer dependencyBundle - the dependency bundle ID
	 * @return String HTML representing the page
	 */
	@HasPermission(Permission.DepAnalyzerView)
	def retrieveLists() {

		def start = new Date()
		session.removeAttribute('assetDependentlist')

		Project project = securityService.userCurrentProject

		def depGroups = []
		def depGroupsJson = session.getAttribute('Dep_Groups')
		if (depGroupsJson) {
			depGroups = JSON.parse(depGroupsJson)
		}
		if (depGroups.size() == 0) {
			depGroups = [-1]
		}

		def assetDependentlist = []
		String selectionQuery = ''
		//String mapQuery
		def nodesQuery = []
		boolean multiple = false
		if (params.dependencyBundle?.isNumber()) {
			// Get just the assets for a particular dependency group id
			//mapQuery = " AND deps.bundle = $params.dependencyBundle"
			depGroups = [params.dependencyBundle]
			//nodesQuery = " AND dependency_bundle = $params.dependencyBundle "
			nodesQuery = NumberUtil.mapToPositiveInteger([params.dependencyBundle])
		} else if (params.dependencyBundle == 'onePlus') {
			// Get all the groups other than zero - these are the groups that have interdependencies
			multiple = true
			//mapQuery = " AND deps.bundle in (${WebUtil.listAsMultiValueString(depGroups-[0])})"
			depGroups = depGroups-[0]
			//nodesQuery = " AND dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups-[0])})"
			nodesQuery = NumberUtil.mapToPositiveInteger(depGroups)
		} else {
			// Get 'all' assets that were bundled
			multiple = true
			//mapQuery = " AND deps.bundle in (${WebUtil.listAsMultiValueString(depGroups)})"
			//nodesQuery = " AND dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups)})"
			nodesQuery = NumberUtil.mapToPositiveInteger(depGroups)
		}
		// Query only if there're groups left.
		if(depGroups && nodesQuery){

			def queryFordepsList = """
			SELECT DISTINCT deps.asset_id AS assetId, ae.asset_name AS assetName, deps.dependency_bundle AS bundle, mb.move_bundle_id AS moveBundleId, mb.name AS moveBundleName,
			ae.asset_type AS type, ae.asset_class AS assetClass, me.move_event_id AS moveEvent, me.name AS eventName, app.criticality AS criticality,
			if (ac_task.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus, if (ac_comment.comment_type IS NULL, 'noComments','comments') AS commentsStatus,
			ae.environment as environment, srcr.location AS sourceLocation, srcr.room_name AS sourceRoomName, srcr.room_id as sourceRoomId,
			tarr.location AS targetLocation, tarr.room_name AS targetRoomName, tarr.room_id as targetRoomId
			FROM (
				SELECT * FROM asset_dependency_bundle
				WHERE project_id=? AND dependency_bundle in (${nodesQuery.join(',')})
				ORDER BY dependency_bundle
			) AS deps
			LEFT OUTER JOIN asset_entity ae ON ae.asset_entity_id = deps.asset_id
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id = ae.move_bundle_id
			LEFT OUTER JOIN move_event me ON me.move_event_id = mb.move_event_id
			LEFT OUTER JOIN application app ON app.app_id = ae.asset_entity_id
			LEFT OUTER JOIN asset_comment ac_task ON ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue'
			LEFT OUTER JOIN asset_comment ac_comment ON ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment'
			LEFT OUTER JOIN room srcr ON srcr.room_id = ae.room_source_id 
			LEFT OUTER JOIN room tarr ON tarr.room_id = ae.room_target_id
			"""
			assetDependentlist = jdbcTemplate.queryForList(queryFordepsList, project.id)
		}

		//log.error "getLists() : query for assetDependentlist took ${TimeUtil.elapsed(start)}"
		// Took 0.296 seconds

		// Save the group id into the session as it is used to redirect the user back after updating assets or doing assignments
		session.setAttribute('dependencyBundle', params.dependencyBundle)
		// TODO : This pig of a list should NOT be stored into the session and the logic needs to be reworked
		//session.setAttribute('assetDependentlist', assetDependentlist)

		def depMap
		String sortOn = params.sort ?: 'assetName'
		String orderBy = params.orderBy ?: 'asc'
		def asset

		if (params.entity != 'graph') {
			depMap = moveBundleService.dependencyConsoleMap(project, null, null,
				params.dependencyBundle != "null" ? params.dependencyBundle : "all")
			depMap = depMap.gridStats
		}
		else {
			depMap = moveBundleService.dependencyConsoleMap(project, null, null,
				params.dependencyBundle != "null" ? params.dependencyBundle : "all", true)
		}
		def model = [entity: params.entity ?: 'apps', stats: depMap]

		model.dependencyBundle = params.dependencyBundle
		model.asset = params.entity
		model.orderBy = orderBy
		model.sortBy = sortOn
		model.haveAssetEditPerm = securityService.hasPermission(Permission.AssetEdit)

		// Switch on the desired entity type to be shown, and render the page for that type
		switch(params.entity) {
			case "all" :
				def assetList = []

				assetDependentlist.each {
					asset = AssetEntity.read(it.assetId)
					String type = it.assetClass == AssetClass.STORAGE.toString() ? 'Logical Storage' : asset.assetType
					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus, type: type, depGroup: it.bundle?.toInteger()]
				}
				assetList = sortAssetByColumn(assetList, sortOn != "type" ? (sortOn) : "assetType", orderBy)
				model.assetList = assetList
				model.assetListSize = assetDependentlist.size()
				render(template:"allList", model:model)
				break

			case "apps" :
				def applicationList = assetDependentlist.findAll { it.type ==  AssetType.APPLICATION.toString() }
				def appList = []

				applicationList.each {
					asset = Application.read(it.assetId)

					appList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus, depGroup: it.bundle?.toInteger()]
				}
				appList = sortAssetByColumn(appList,sortOn,orderBy)
				model.appList = appList
				model.applicationListSize = applicationList.size()

				render(template:"appList", model:model)
				break

			case "server":
				def assetList = []
				def assetEntityList = assetDependentlist.findAll { AssetType.allServerTypes.contains(it.type) }

				assetEntityList.each {
					asset = AssetEntity.read(it.assetId)
					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus, locRoom:asset.roomSource, depGroup: it.bundle?.toInteger()]
				}
				assetList = sortAssetByColumn(assetList,sortOn,orderBy)
				model.assetList = assetList
				model.assetEntityListSize = assetEntityList.size()
				render(template:"assetList", model:model)
				break

			case "database" :
				def databaseList = assetDependentlist.findAll{it.type == AssetType.DATABASE.toString() }
				def dbList = []

				databaseList.each {
					asset = Database.read(it.assetId)

					dbList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus, depGroup: it.bundle?.toInteger()]
				}
				dbList = sortAssetByColumn(dbList,sortOn,orderBy)
				model.databaseList = dbList
				model.dbDependentListSize = databaseList.size()
				render(template:'dbList', model:model)
				break

			case "files" :
				def filesList = assetDependentlist.findAll { it.assetClass == AssetClass.STORAGE.toString() ||
				                                             (it.assetClass == AssetClass.DEVICE.toString() &&
						                                             it.type in AssetType.storageTypes)}
				def assetList = []
				def fileList = []

				filesList.each {
					asset = AssetEntity.read(it.assetId)

					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus, depGroup: it.bundle?.toInteger()]
				}

				assetList.each {
					def item = [id:it.asset.id, assetName:it.asset.assetName, assetType:it.asset.assetType,
						validation:it.asset.validation, moveBundle:it.asset.moveBundle, planStatus:it.asset.planStatus,
						depToResolve:it.asset.depToResolve, depToConflic:it.asset.depToConflict, assetClass:it.asset.assetClass, depGroup: it.depGroup?.toInteger()]

					// check if the object is a logical or physical strage
					if (it.asset.assetClass.toString() == 'DEVICE') {
						item.fileFormat = ''
						item.storageType = 'Server'
						item.type = item.assetType
					} else {
						item.fileFormat = Files.read(it.asset.id)?.fileFormat ?: ''
						item.storageType = 'Files'
						item.type = 'Logical'
					}

					fileList << [asset: item, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus]
				}

				fileList = sortAssetByColumn(fileList,sortOn,orderBy)
				model.filesList = fileList
				model.filesDependentListSize = fileList.size()

				render(template: 'filesList', model: model)
				break

			case "graph" :
				def moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project, true)
				Set uniqueMoveEventList = moveBundleList.moveEvent
				uniqueMoveEventList.remove(null)
				List moveEventList = uniqueMoveEventList.toList()
				moveEventList.sort { it?.name }

				def defaultPrefs = [colorBy: 'group', appLbl: 'true', maxEdgeCount: '4']
				def graphPrefs = userPreferenceService.getPreference(PREF.DEP_GRAPH)
				def prefsObject = graphPrefs ? JSON.parse(graphPrefs) : defaultPrefs

				// front end labels for the Color By groups
				def colorByGroupLabels = ['group': 'Group', 'bundle': 'Bundle', 'event': 'Event', 'environment': 'Environment', 'sourceLocation': 'Source Location', 'targetLocation': 'Target Location']
				// contains all the subgroups found in the Color By groups of this dependency group
				def colorByGroups = [:]

				// Create the Nodes
				def graphNodes = []
				def name = ''
				def shape = 'circle'
				def size = 150
				def title = ''
				def color = ''
				def type = ''
				def assetType = ''
				def assetClass = ''
				def criticalitySizes = [Minor: 150, Important: 200, Major: 325, Critical: 500]
				Map<Long, String> dependencyBundleMap = new TreeMap<Long, String>()
				Map<Long, String> moveBundleMap = new TreeMap<Long, String>()
				Map<Long, String> moveEventMap = new TreeMap<Long, String>()
				def t1 = TimeUtil.elapsed(start).millis + TimeUtil.elapsed(start).seconds * 1000

				assetDependentlist.each {
					assetType = it.model?.assetType?:it.type
					assetClass = it.assetClass
					size = 150

					type = getImageName(assetClass, assetType)
					if (type == AssetType.APPLICATION.toString())
						size = it.criticality ? criticalitySizes[it.criticality] : 200

					if (!dependencyBundleMap.containsKey(it.bundle)) {
						dependencyBundleMap[it.bundle] = 'Group ' + it.bundle
					}
					if (!moveBundleMap.containsKey(it.moveBundleId))  {
						moveBundleMap[it.moveBundleId] = it.moveBundleName
					}

					String moveEventName = it.eventName ?: 'No Event'
					color = it.eventName ? 'grey' : 'red'
					long moveEventId = it.moveEvent ?: (long)0
					boolean hasMoveEvent = it.eventName

					if (!moveEventMap.containsKey(moveEventId)) {
						moveEventMap[moveEventId] = moveEventName
					}


					// get the label for this node's environment
					def environment = it.environment
					if (environment == null || environment == '')
						environment = 'Unassigned'

					// get the id and label for the target room
					def targetRoomId = it.targetRoomId ?: (long) 0
					def targetLocationName = 'Unassigned'
					if (targetRoomId != 0)
						targetLocationName = it.targetLocation + ' / ' + it.targetRoomName

					// get the id and label for the source room
					def sourceRoomId = it.sourceRoomId ?: (long) 0
					def sourceLocationName = 'Unassigned'
					if (sourceRoomId != 0)
						sourceLocationName = it.sourceLocation + ' / ' + it.sourceRoomName

					// map the query parameters to the ids and names of the Color By groups
					def colorByGroupIds = [group: it.bundle, bundle: it.moveBundleId, event: moveEventId, environment: environment, sourceLocation: sourceRoomId, targetLocation: targetRoomId]
					def colorByGroupNames = [group: 'Group ' + it.bundle, bundle: it.moveBundleName, event: moveEventName, environment: environment, sourceLocation: sourceLocationName, targetLocation: targetLocationName]


					// add these groups to the master group set
					colorByGroupLabels.each {
						// create this group category if it doesn't exist in the master set yet
						def prop = it.getKey()
						if (colorByGroups[prop] == null)
							colorByGroups[prop] = [:]

						// add this group to the master set
						def id = colorByGroupIds[prop]
						colorByGroups[prop][id] = colorByGroupNames[prop]
					}

					graphNodes << [id: it.assetId, name: it.assetName, type: type, depBundleId: it.bundle,
								   moveBundleId: it.moveBundleId, moveEventId: moveEventId, hasMoveEvent: hasMoveEvent,
								   shape: shape, size: size, title: it.assetName, color: color, dependsOn: [],
								   supports: [], assetClass: it.assetClass, cutGroup: -1, colorByProperties: colorByGroupIds]
				}
				// set the dep bundle, move bundle, and move event properties to indices
				List<Long> sortedDepBundles = dependencyBundleMap.keySet() as List
				List<Long> sortedMoveBundles = moveBundleMap.keySet() as List
				List sortedMoveEvents = moveEventMap.keySet() as List
				Map dependencyGroupIndexMap = [:]
				Map moveBundleIndexMap = [:]
				Map  moveEventIndexMap = [:]
				graphNodes.each {
					def mbid = it.depBundleId
					def index = sortedDepBundles.indexOf(mbid)
					it.depBundleIndex = index
					dependencyGroupIndexMap[index] = dependencyBundleMap[mbid]

					mbid = it.moveBundleId
					index = sortedMoveBundles.indexOf(mbid)
					it.moveBundleIndex = index
					moveBundleIndexMap[index] = moveBundleMap[mbid]

					mbid = it.moveEventId
					index = sortedMoveEvents.indexOf(mbid)
					it.moveEventIndex = index
					moveEventIndexMap[index] = moveEventMap[mbid]
				}

				// Define a map of all the options for asset types
				def assetTypes = assetEntityService.ASSET_TYPE_NAME_MAP

				// Create a seperate list of just the node ids to use while creating the links (this makes it much faster)
				def nodeIds = graphNodes*.id

				// Report the time it took to create the nodes
				def t2 = TimeUtil.elapsed(start).millis + TimeUtil.elapsed(start).seconds * 1000
				def td = t2 - t1
				float avg = 0
				if (assetDependentlist) {
					avg = td / assetDependentlist.size()
				}

				// Set the defaults map to be used in the dependeny graph
				def defaults = moveBundleService.getMapDefaults(graphNodes.size())
				if (multiple) {
					defaults.force = -200
					defaults.linkSize = 140
				}

				// Query for only the dependencies that will be shown
				def depBundle = params.dependencyBundle.isNumber() ? params.dependencyBundle : 0

				//map Groups array String values to Integer
				depGroups = NumberUtil.mapToPositiveInteger(depGroups)

				// handle the case of empty dependency groups
				if (depGroups == [])
					depGroups = -1

				def assetDependencies = AssetDependency.executeQuery('''
					SELECT NEW MAP (ad.asset AS ASSET, ad.status AS status, ad.isFuture AS future,
					                ad.isStatusResolved AS resolved, adb1.asset.id AS assetId, adb2.asset.id AS dependentId,
					                (CASE WHEN ad.asset.moveBundle != ad.dependent.moveBundle
					                       AND ad.status in (:statuses)
					                      THEN true ELSE false
					                 END) AS bundleConflict)
					FROM AssetDependency ad, AssetDependencyBundle adb1, AssetDependencyBundle adb2, Project p
					WHERE ad.asset = adb1.asset
						AND ad.dependent = adb2.asset
						AND adb1.dependencyBundle in (:depGroups)
						AND adb2.dependencyBundle in (:depGroups)
						AND adb1.project = p
						AND adb2.project = p
						AND p.id = :projectId
					GROUP BY ad.id
				''', [
						statuses: [AssetDependencyStatus.UNKNOWN, AssetDependencyStatus.VALIDATED, AssetDependencyStatus.QUESTIONED],
						depGroups: depGroups,
						projectId: project.id
				])

				def multiCheck = new Date()

				// Create the links
				def graphLinks = []
				def linkTable = [][]
				def i = 0
				def opacity = 1
				def statusColor = 'grey'
				assetDependencies.each {
					opacity = 1
					statusColor = 'grey'
					boolean notApplicable = false
					boolean future = false
					if (!it.resolved) {
						statusColor='red'
					} else if (it.status == AssetDependencyStatus.FUTURE) {
						future = true
					} else if (!(it.status in [AssetDependencyStatus.UNKNOWN, AssetDependencyStatus.VALIDATED])) {
						notApplicable = true
					}

					def sourceIndex = nodeIds.indexOf(it.assetId)
					def targetIndex = nodeIds.indexOf(it.dependentId)
					if (sourceIndex != -1 && targetIndex != -1) {

						// check if this link is the 2nd part of a 2-way dependency
						if (!linkTable[sourceIndex]) {
							linkTable[sourceIndex] = []
						}
						linkTable[sourceIndex][targetIndex] = true
						def duplicate = (linkTable[targetIndex] && linkTable[targetIndex][sourceIndex])

						graphLinks << [id: i, source: sourceIndex, target: targetIndex, value: 2, statusColor: statusColor,
						               opacity: opacity, unresolved: !it.resolved, notApplicable: notApplicable,
						               future: future, bundleConflict: it.bundleConflict, duplicate: duplicate]
						i++
					}
				}

				// Set the dependency properties of the nodes
				graphLinks.each {
					graphNodes[it.source].dependsOn.add(it.id)
					graphNodes[it.target].supports.add(it.id)
				}

				// Create the model that will be used while rendering the page
				model.defaults = defaults
				model.defaultsJson = defaults as JSON
				model.defaultPrefs = defaultPrefs as JSON
				model.graphPrefs = prefsObject
				model.showControls = params.showControls
				model.fullscreen = params.fullscreen ?: false
				model.nodes = graphNodes as JSON
				model.links = graphLinks as JSON
				model.multiple = multiple
				model.assetTypes = assetTypes
				model.assetTypesJson = assetTypes as JSON
				model.colorByGroups = colorByGroups as JSON
				model.colorByGroupLabels = colorByGroupLabels
				model.colorByGroupLabelsJson = colorByGroupLabels as JSON
				model.depBundleMap = dependencyGroupIndexMap as JSON
				model.moveBundleMap = moveBundleIndexMap as JSON
				model.moveEventMap = moveEventIndexMap as JSON
				model.depGroup = params.dependencyBundle

				// Render dependency graph
				render(template:'dependencyGraph', model:model)
				break
		} // switch
		log.info "Loading dependency console took ${TimeUtil.elapsed(start)}"
	}

	// removes the user's dependency analyzer map related preferences
	@HasPermission(Permission.UserGeneralAccess)
	def removeUserGraphPrefs () {
		userPreferenceService.removePreference(params.preferenceName ?: 'depGraph')
		render true
	}

	/**
	* Delete multiple  Assets, Apps, Databases and files .
	* @param : assetLists[]  : list of ids for which assets are requested to deleted
	*/
	@HasPermission(Permission.AssetDelete)
	def deleteBulkAsset() {
		renderAsJson(resp: assetEntityService.deleteBulkAssets(params.type, params.list("assetLists[]")))
	}

	/**
	 * Get workflowTransition select for comment id
	 * @param assetCommentId : id of assetComment
	 * @param format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return select or a JSON array
	 */
	@HasPermission(Permission.WorkflowView)
	def retrieveWorkflowTransition() {
		Project project = securityService.userCurrentProject
		def format = params.format
		def assetCommentId = params.assetCommentId
		AssetComment assetComment = AssetComment.read(assetCommentId)
		AssetEntity assetEntity = AssetEntity.get(params.assetId)
		String workflowCode = assetEntity?.moveBundle?.workflowCode ?: project.workflowCode
		Workflow workFlow = Workflow.findByProcess(workflowCode)
		List<WorkflowTransition> workFlowTransitions = WorkflowTransition.findAllByWorkflowAndCategory(workFlow, params.category)

		//def workFlowTransitions = WorkflowTransition.findAllByWorkflow(workFlow) TODO : should be removed after completion of this new feature
		if (assetEntity) {
			def existingWorkflows = assetCommentId ? AssetComment.findAllByAssetEntityAndIdNotEqual(assetEntity, assetCommentId).workflowTransition : AssetComment.findAllByAssetEntity(assetEntity).workflowTransition
			workFlowTransitions.removeAll(existingWorkflows)
		}

		if (format == 'json') {
			renderSuccessJson(workFlowTransitions.collect { [ id:it.id, name: it.name] })
		} else {
			String result = ''
			if (workFlowTransitions) {
				result = HtmlUtil.generateSelect(selectId: 'workFlowId', selectName: 'workFlow', options: workFlowTransitions,
					firstOption: [value: '', display: ''], optionKey: 'id', optionValue: 'name',
					optionSelected: assetComment?.workflowTransitionId)
			}
			render result
		}
	}

	/**
	 * Provides a SELECT control with Staff associated with a project and the assigned staff selected if task id included
	 * @param forView - The CSS ID for the SELECT control
	 * @param id - the id of the existing task (aka comment)
	 * @param format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return HTML select of staff belongs to company and TDS or a JSON array
	 */
	@HasPermission(Permission.ProjectStaffShow)
	def updateAssignedToSelect() {

		// TODO : Need to refactor this function to use the new TaskService.assignToSelectHtml method

		Project project = securityService.userCurrentProject
		def viewId = params.forView
		def format = params.format
		def selectedId = 0
		Person person

		// Find the person assigned to existing comment or default to the current user
		if (params.containsKey('id')) {
			if (params.id && params.id != '0') {
				person = AssetComment.findByIdAndProject(params.id, project)?.assignedTo
			} else {
				person = securityService.userLoginPerson
			}
		}
		if (person) selectedId = person.id

		def projectStaff = partyRelationshipService.getProjectStaff(project.id)

		// Now morph the list into a list of name: Role names
		def list = []
		projectStaff.each {
			String roleDescription = it.role.toString()
			list << [ id:it.staff.id,
				nameRole:"${roleDescription}: $it.staff",
				sortOn:"${roleDescription},$it.staff.firstName $it.staff.lastName"
			]
		}
		list.sort { it.sortOn }

		if (format == 'json') {
			renderSuccessJson(list)
			return
		}

		render HtmlUtil.generateSelect(selectId: viewId, selectName: viewId, options: list, optionKey: 'id',
		                               optionValue: 'nameRole', optionSelected: selectedId,
		                               firstOption: [value: '0', display: 'Unassigned'])
	}

	@HasPermission(Permission.UserGeneralAccess)
	def isAllowToChangeStatus() {
		def taskId = params.id
		boolean allowed = true
		if (taskId) {
			def status = AssetComment.read(taskId)?.status
			def isChangePendingStatusAllowed = securityService.isChangePendingStatusAllowed()
			if (status == "Pending" && !isChangePendingStatusAllowed) {
				allowed = false
			}
		}
		renderAsJson(isAllowToChangeStatus: allowed)
	}

	/**
	 * Generates an HTML SELECT control for the AssetComment.status property according to user role and current status of AssetComment(id)
	 * @param	params.id	The ID of the AssetComment to generate the SELECT for
	 * @param   format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return render HTML or a JSON array
	 */
	@HasPermission([Permission.CommentView, Permission.TaskView])
	def updateStatusSelect() {
		//Changing code to populate all select options without checking security roles.
		def mapKey = 'ALL'//securityService.hasRole([ADMIN.name(),SUPERVISOR.name(),CLIENT_ADMIN.name(),CLIENT_MGR.name()]) ? 'ALL' : 'LIMITED'
		def optionForRole = statusOptionForRole.get(mapKey)
		def format = params.format
		def taskId = params.id
		def status = taskId ? (AssetComment.read(taskId)?.status?: '*EMPTY*') : AssetCommentStatus.READY
		def optionList = optionForRole.get(status)
		def firstOption = [value:'', display:'Please Select']
		def selectId = taskId ? "statusEditId" : "statusCreateId"
		def optionSelected = taskId ? (status != '*EMPTY*' ? status : 'na'): AssetCommentStatus.READY

		if (format == 'json') {
			renderSuccessJson(optionList)
		} else {
			render HtmlUtil.generateSelect(selectId: selectId, selectName: 'statusEditId', options: optionList,
				selectClass: "task_${optionSelected.toLowerCase()}", optionSelected: optionSelected,
				javascript: "onChange='this.className=this.options[this.selectedIndex].className'",
				firstOption: firstOption, optionClass: '')
		}
	}

	/**
	 * Generates an HTML table containing all the predecessor for a task with corresponding Category and Tasks SELECT controls for
	 * a speciied assetList of predecessors HTML SELECT control for the AssetComment at editing time
	 * @param	params.id	The ID of the AssetComment to load  predecessor SELECT for
	 * @return render HTML
	 */
	@HasPermission(Permission.TaskView)
	def predecessorTableHtml() {
		//def sw = new org.springframework.util.StopWatch("predecessorTableHtml Stopwatch")
		//sw.start("Get current project")
		def task = AssetComment.findByIdAndProject(params.commentId, securityService.loadUserCurrentProject())
		if (! task) {
			log.error "predecessorTableHtml - unable to find task $params.commentId for project $securityService.userCurrentProjectId"
			render "An unexpected error occured"
		} else {
			render taskService.genTableHtmlForDependencies(task.taskDependencies, task, "predecessor")
		}
	}

	/**
	 * Generates options for task dependency select
	 * @param : taskId  : id of task for which select options are generating .
	 * @param : category : category for options .
	 * @return : options
	 */
	@HasPermission(Permission.TaskEdit)
	def generateDepSelect() {
		def task = AssetComment.read(params.taskId)
		def category = params.category

		def queryForPredecessor = new StringBuilder('''
			FROM AssetComment a
			WHERE a.project.id=projectId
			AND a.category=:category
			AND a.commentType.id=:commentTypeId
			AND a.id!=:taskId
		''')
		Map queryArgs = [projectId: securityService.userCurrentProjectId, category: category,
		                 commentTypeId: AssetCommentType.TASK.toString(), taskId: task.id]
		if (task.moveEvent) {
			queryForPredecessor.append('AND a.moveEvent=:moveEvent')
			queryArgs.moveEvent = task.moveEvent
		}
		queryForPredecessor.append(' ORDER BY a.taskNumber ASC')
		def predecessors = AssetComment.executeQuery(queryForPredecessor.toString())

		StringBuilder options = new StringBuilder()
		predecessors.each {
			options << "<option value='" << it.id << "'>" << it << '</option>'
		}
		render options.toString()
	}

	/**
	 * Generates an HTML table containing all the successor for a task with corresponding Category and Tasks SELECT controls for
	 * a speciied assetList of successors HTML SELECT control for the AssetComment at editing time
	 * @param   params.id   The ID of the AssetComment to load  successor SELECT for
	 * @return render HTML
	 */
	@HasPermission(Permission.TaskView)
	def successorTableHtml() {
		def task = AssetComment.findByIdAndProject(params.commentId, securityService.loadUserCurrentProject())
		if (! task) {
			log.error "successorTableHtml - unable to find task $params.commentId for project $securityService.userCurrentProjectId"
			render "An unexpected error occured"
		} else {
			def taskSuccessors = TaskDependency.findAllByPredecessor(task).sort{ it.assetComment.taskNumber }
			render taskService.genTableHtmlForDependencies(taskSuccessors, task, "successor")
		}
	}

	/**
	 * Generates the HTML SELECT for a single Predecessor
	 * Used to generate a SELECT control for a project and category with an optional task. When a task is presented the list will
	 * also be filtered on tasks from the moveEvent.
	 * If a taskId is included, the SELECT will have CSS ID taskDependencyEditId otherwise taskDependencyId and the SELECT name of
	 * taskDependencyEdit or taskDependencySave accordingly since having an Id means that we're in edit mode vs create mode.
	 * @param commentId - the comment (aka task) that the predecessor will use
	 * @param category - comment category to filter the list of tasks by
	 * @param format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return String - HTML Select of prdecessor list or a JSON
	 */
	@HasPermission(Permission.TaskView)
	def predecessorSelectHtml() {
		Project project = securityService.userCurrentProject
		def task
		def format=params.format
		def moveEventId=params.moveEvent

		if (params.commentId) {
			task = AssetComment.findByIdAndProject(params.commentId, project)
			if (! task) {
				log.warn "predecessorSelectHtml - Unable to find task id $params.commentId in project $project.id"
			}
		}

		def taskList = taskService.search(project, task, moveEventId)

		if (format=='json') {
			def list = []
			list << [ id: '', desc: 'Please Select', category: '', taskNumber: '']
			taskList.list.each {
				def desc = it.comment?.length()>50 ? it.comment.substring(0,50): it.comment
				list << [ id: it.id, desc: desc, category: it.category, taskNumber: it.taskNumber]
			}
			renderSuccessJson(list)
		} else {
			// Build the SELECT HTML
			render HtmlUtil.generateSelect(selectId: task ? 'taskDependencyEditId' : 'taskDependencyId',
				selectName: params.forWhom, options: taskList.list, optionKey: 'id',
				firstOption: [value:'', display:'Please Select'])
		}
	}

	/**
	 * returns a list of tasks paginated and filtered
	 */
	@HasPermission(Permission.TaskView)
	def tasksSearch() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def task
		def moveEventId=Long.parseLong(params.moveEvent)
		def page=Long.parseLong(params.page)
		def pageSize=Long.parseLong(params.pageSize)
		def filterDesc=params['filter[filters][0][value]']

		if (params.commentId) {
			task = AssetComment.findByIdAndProject(params.commentId, project)
			if (! task) {
				log.warn "predecessorSelectHtml - Unable to find task id $params.commentId in project $project.id"
			}
		}

		def tasksData = taskService.search(project, task, moveEventId, page, pageSize, filterDesc)

		def list = []

		list << [ id: '', desc: 'Please Select', category: '', taskNumber: '']
		tasksData.total++

		tasksData.list.each {
			def desc = it.comment?.length()>50 ? it.comment.substring(0,50): it.comment
			list << [ id: it.id, desc: it.taskNumber + ': ' + desc, category: it.category, taskNumber: it.taskNumber]
		}

		tasksData.list = list
		renderSuccessJson(tasksData)
	}

	/**
	 * Return the task index in the search
	 */
	@HasPermission(Permission.TaskView)
	def taskSearchMap() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def moveEventId = params.moveEvent
		def taskId = params.taskId
		def task

		if (params.commentId) {
			task = AssetComment.findByIdAndProject(params.commentId, project)
			if (!task) {
				log.warn "predecessorSelectHtml - Unable to find task id $params.commentId in project $project.id"
			}
		}

		def taskIdx = taskService.searchTaskIndexForTask(project, params.category, task, moveEventId, taskId)

		renderSuccessJson([taskIdx.intValue()])
	}

	/**
	 * Export Special Report
	 * @param NA
	 * @return : Export data in WH project format
	 */
	@HasPermission(Permission.AssetExport)
	def exportSpecialReport() {
		Project project = securityService.userCurrentProject
		def today = TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_TIME_5)
		try{
			String filePath = "/templates/TDS-Storage-Inventory.xls"
			def book = ExportUtil.loadSpreadsheetTemplate(filePath)

			def spcExpSheet = book.getSheet("SpecialExport")
			def storageInventoryList = assetEntityService.getSpecialExportData(project)
			def spcColumnList = ["server_id", "app_id", "server_name", "server_type", "app_name", "tru", "tru2",
			                     "move_bundle", "move_date", "status","group_id", "environment", "criticality" ]

			for (int r = 0; r < storageInventoryList.size(); r++) {
				 for(int c = 0; c < spcColumnList.size(); c++) {
					def valueForSheet = storageInventoryList[r][spcColumnList[c]] ? String.valueOf(storageInventoryList[r][spcColumnList[c]]) : ""
					WorkbookUtil.addCell(spcExpSheet, c, r+1, valueForSheet)
				 }
			}

			ExportUtil.setContentType(response, project.name + 'SpecialExport-' + today +
				"." + ExportUtil.getWorkbookExtension(book))
			book.write(response.outputStream)
		}
		catch(e) {
			log.error "Exception occurred while exporting data: $e.message", e
			flash.message = e.message
		}
		redirect(action:"exportAssets")
	}

	/**
	 * Fetch Asset's modelType to use to select asset type fpr asset acording to model
	 * @param : id - Requested model's id
	 * @return : assetType if exist for requested model else 0
	 */
	@HasPermission(Permission.ModelView)
	def retrieveAssetModelType() {
		def assetType = 0
		if (params.id?.isNumber()) {
			assetType = Model.read(params.id).assetType ?: 0
		}
		render assetType
	}

	/**
	 * Populates the dependency section of the asset forms for support and dependent relationships
	 * @param id : asset id
	 * @return : HTML code containing support and dependent edit form
	 */
	@HasPermission(Permission.AssetEdit)
	def populateDependency() {
		try {
			Map model = assetEntityService.dependencyEditMap(params)
			render(template:'dependentCreateEdit', model:model)
		}
		catch (InvalidRequestException e) {
			render e.message
		}
		catch (e) {
			log.error ExceptionUtil.stackTraceToString('Unable load dependency model', e)
			render 'Unable to load dependencies due to a run-time error'
		}
	}

	/**
	 * Returns a lightweight list of assets filtered on  on the asset class
	 * @param id - class of asset to filter on (e.g. Application, Database, Server)
	 * @return JSON array of asset id, assetName
	 */
	@HasPermission(Permission.AssetView)
	def assetSelectDataByClass() {
		renderAsJson(assets: assetEntityService.getAssetsByType(params.id).collect { [value: it.id, caption: it.assetName] })
	}

	/**
	 * Gets validation for particular fields
	 * @param type,validation
	 */
	@HasPermission(Permission.AssetView)
	def retrieveAssetImportance() {
		renderAsJson assetEntityService.getConfig(params.type, params.validation).config
	}

	/**
	 * Gets highlighting css for particular fields
	 */
	@HasPermission(Permission.AssetView)
	def retrieveHighlightCssMap() {
		def assetType = params.type
		Class clazz
		switch(assetType) {
			case 'Application': clazz = Application; break
			case 'Database':    clazz = Database; break
			case 'Files':       clazz = Files; break
			default:            clazz = AssetEntity; break
		}

		def assetEntity = params.id ? clazz.get(params.id) : clazz.newInstance(appOwner: '')
		def configMap = assetEntityService.getConfig(assetType, params.validation)
		renderAsJson assetEntityService.getHighlightedInfo(assetType, assetEntity, configMap)
	}

	/**
	 * Updates columnList with custom labels.
	 * @param entityDTAMap :  dataTransferEntityMap for entity type
	 * @param columnslist :  column Names
	 * @param project :project instance
	 */
	@Deprecated
	private retrieveColumnNames(entityDTAMap, columnslist, project) {
		entityDTAMap.eachWithIndex { item, pos ->
			if (AssetEntityService.customLabels.contains(item.columnName)) {
				columnslist.add(project[item.eavAttribute?.attributeCode] ?: item.columnName)
			} else {
				columnslist.add(item.columnName)
			}
		}
		return columnslist
	}

	/**
	 * Sets Import preferences.(ImportApplication,ImportServer,ImportDatabase, ImportStorage,ImportRoom,ImportRack,ImportDependency)
	 * @param preference
	 * @param value
	 */
	@HasPermission(Permission.AssetImport)
	def setImportPerferences() {
		def key = params.preference
		def value = params.value
		if (value) {
			userPreferenceService.setPreference(key, value)
			session.setAttribute(key,value)
		}
		render true
	}

	/**
	 * Action to return on list Dependency
	 */
	@HasPermission(Permission.AssetView)
	def listDependencies() {
		Project project = controllerService.getProjectForPage(this, 'to view Dependencies')
		if (!project) return

		def entities = assetEntityService.entityInfo(project)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		def depPref = assetEntityService.getExistingPref('Dep_Columns')
		def attributes = ['c1':'C1','c2':'C2','c3':'C3','c4':'C4','frequency':'Frequency','comment':'Comment','direction':'Direction']
		def columnLabelpref = [:]
		depPref.each { key, value ->
			columnLabelpref[key] = attributes[value]
		}

		return [
			applications: entities.applications,
			assetDependency: new AssetDependency(),
			attributesList: attributes.keySet().sort{it},
			columnLabelpref:columnLabelpref,
			dbs: entities.dbs,
			depPref: depPref,
			dependencyStatus: entities.dependencyStatus,
			dependencyType: entities.dependencyType,
			files: entities.files,
			moveBundleList: moveBundleList,
			networks: entities.networks,
			// projectId: project.id,
			servers: entities.servers
		]
	}

	/**
	 * Show list of dependencies using jqgrid.
	 */
	@HasPermission(Permission.AssetView)
	def listDepJson() {
		String sortIndex = params.sidx ?: 'asset'
		String sortOrder = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows
		def sid

		def filterParams = [assetName: params.assetName, assetType: params.assetType, assetBundle: params.assetBundle,
		                    type: params.type, dependentName: params.dependentName, dependentType: params.dependentType,
		                    dependentBundle: params.dependentBundle, status: params.status,frequency: params.frequency,
		                    comment: params.comment, c1: params.c1, c2: params.c2, c3: params.c3, c4: params.c4,
		                    direction: params.direction]
		def depPref= assetEntityService.getExistingPref('Dep_Columns')
		StringBuffer query = new StringBuffer("""
			SELECT * FROM (
				SELECT asset_dependency_id AS id,
					ae.asset_name AS assetName,
					ae.asset_class AS assetClass,
					ae.asset_type AS assetType,
					mb.name AS assetBundle,
					ad.type AS type,
					aed.asset_name AS dependentName,
					aed.asset_class AS dependentClass,
					aed.asset_type AS dependentType,
					mbd.name AS dependentBundle,
					ad.status AS status,ad.comment AS comment, ad.data_flow_freq AS frequency, ae.asset_entity_id AS assetId,
					aed.asset_entity_id AS dependentId,
					ad.c1 AS c1, ad.c2 AS c2, ad.c3 AS c3,ad.c4 AS c4,
					ad.data_flow_direction AS direction
				FROM asset_dependency ad
				LEFT OUTER JOIN asset_entity ae ON ae.asset_entity_id = asset_id
				LEFT OUTER JOIN asset_entity aed ON aed.asset_entity_id = dependent_id
				LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id = ae.move_bundle_id
				LEFT OUTER JOIN move_bundle mbd ON mbd.move_bundle_id = aed.move_bundle_id
				WHERE ae.project_id = $securityService.userCurrentProjectId
				ORDER BY ${sortIndex + " " + sortOrder}
			) AS deps
		 """)

		// Handle the filtering by each column's text field
		boolean firstWhere = true
		filterParams.each {
			if (it.value) {
				if (firstWhere) {
					query.append(' WHERE ')
					firstWhere = false
				}
				else {
					query.append(' AND ')
				}
				query.append("deps.$it.key LIKE '%$it.value%'")
			}
		}

		def dependencies = jdbcTemplate.queryForList(query.toString())
		int totalRows = dependencies.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)

		if (totalRows) {
			dependencies = dependencies[rowOffset..Math.min(rowOffset + maxRows, totalRows - 1)]
		}

		def results = dependencies?.collect {
			[id: it.id,
			 cell: [it.assetName,
			        it.assetType,
			        it.assetBundle,
			        it.type,
			        it.dependentName,
			        it.dependentType,
			        it.dependentBundle,
			        (depPref['1']!='comment') ? it[depPref['1']] : (it[depPref['1']]? "<div class='commentEllip'>$it.comment</div>" : ''),
			        (depPref['2']!='comment') ? it[depPref['2']] : (it[depPref['2']]? "<div class='commentEllip'>$it.comment</div>" : ''),
			        it.status,
			        it.assetId, // 10
			        it.dependentId, // 11
			        it.assetClass,	// 12
			        it.dependentClass]	// 13
			]
		}

		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	/**
	* Change bundle when on change of asset in dependency.
	* @param dependentId
	* @param assetId
	* @render resultMap
	*/
	@HasPermission(Permission.BundleView)
	def retrieveChangedBundle() {
		def dependentId = params.dependentId
		def dependent = AssetDependency.read(dependentId.isInteger() ? dependentId.toInteger() : -1)
		Long depBundle = dependentId == "support" ? dependent?.asset?.moveBundleId : dependent?.dependent?.moveBundleId
		renderAsJson(id: AssetEntity.read(params.assetId)?.moveBundle?.id, status: dependent?.status, depBundle: depBundle)
	}

	private List sortAssetByColumn(List assetlist, String sortOn, String orderBy) {
		List result
		if(sortOn == "depGroup"){
			result = assetlist.sort { a, b ->
				if (orderBy == 'asc') {
					a.getAt(sortOn) <=> b.getAt(sortOn)
				} else {
					b.getAt(sortOn) <=> a.getAt(sortOn)
				}
			}
		} else {

			result = assetlist.sort { a, b ->
					if (orderBy == 'asc') {
						a.asset?.getAt(sortOn)?.toString() <=> b.asset?.getAt(sortOn)?.toString()
					} else {
						b.asset?.getAt(sortOn)?.toString() <=> a.asset?.getAt(sortOn)?.toString()
					}
			}
		}
		return result

	}

	/**
	 * Used to return a SELECT for a specified roomId and sourceType
	 * @param params.roomId - room id
	 * @param params.rackId - the rack id of the currently selected rack
	 * @param params.sourceTarget - S)ource or T)arget
	 * @param params.forWhom - indicates if it is Create or Edit
	 */
	@HasPermission(Permission.RackLayoutModify)
	def retrieveRackSelectForRoom() {
		Project project = controllerService.getProjectForPage(this)
		def roomId = params.roomId
		def rackId = params.rackId
		def options = assetEntityService.getRackSelectOptions(project, roomId, true)
		def sourceTarget = params.sourceTarget
		def forWhom = params.forWhom
		def tabindex = params.tabindex

		def rackDomId
		def rackDomName
		def clazz

		if (sourceTarget == 'S') {
			rackDomId = 'rackSId'
			rackDomName = 'rackSourceId'
			clazz = 'config.sourceRack'
		}
		else {
			rackDomId = 'rackTId'
			rackDomName = 'rackTargetId'
			clazz = 'config.targetRack'
		}

		render(template: 'deviceRackSelect',
		       model: [options: options, rackDomId: rackDomId, rackDomName: rackDomName, clazz: clazz,
		               rackId: rackId, forWhom: forWhom, tabindex: tabindex?: 0, sourceTarget: sourceTarget])
	}

	/**
	 * Used to return a SELECT for a specified roomId and sourceType
	 * @param params.roomId - room id
	 * @param params.id - the chassis id of the currently selected chassis
	 * @param params.sourceTarget - S)ource or T)arget
	 * @param params.forWhom - indicates if it is Create or Edit
	 */
	@HasPermission(Permission.RackLayoutModify)
	def retrieveChassisSelectForRoom() {
		Project project = controllerService.getProjectForPage(this)
		def roomId = params.roomId
		def id = params.id
		def options = assetEntityService.getChassisSelectOptions(project, roomId)
		def sourceTarget = params.sourceTarget
		def forWhom = params.forWhom
		def tabindex = params.tabindex

		def rackDomId
		def rackDomName
		def domClass=params.domClass
		def clazz

		if (sourceTarget == 'S') {
			rackDomId = 'chassisSelectSId'
			rackDomName = 'chassisSelectSourceId'
			clazz = ''
		}
		else {
			rackDomId = 'chassisSelectTId'
			rackDomName = 'chassisSelectTargetId'
			clazz = ''
		}

		render(template: 'deviceChassisSelect',
				 model: [options: options, domId: rackDomId, domName: rackDomName, domClass: domClass ?: clazz,
				         value: id, forWhom: forWhom, sourceTarget: sourceTarget, tabindex: tabindex])
	}

	@HasPermission(Permission.AssetView)
	def retrieveAssetsByType() {
		Project project = controllerService.getProjectForPage(this, 'to view assets')
		if (!project) return

		def assetType = params.assetType

		try {
			if (assetType == 'Other') {
				assetType = AssetType.NETWORK.toString()
			}
			def groups = [assetType]
			def info = assetEntityService.entityInfo(project, groups)
			def assets = []
			switch (assetType) {
				case AssetType.SERVER.toString():
					assets = info.servers
					break
				case AssetType.APPLICATION.toString():
					assets = info.applications
					break
				case AssetType.DATABASE.toString():
					assets = info.dbs
					break
				case AssetType.STORAGE.toString():
					assets = info.files
					break
				case AssetType.NETWORK.toString():
					assets = info.networks
					break
			}
			def result = [list: [], type: assetType]
			assets.each {
				result.list << [id:it[0], name: it[1]]
			}
			renderSuccessJson(result)
		}
		catch (e) {
			handleException e, log
		}
	}

	/**
	 * This service retrieves all the assets for a given asset class.
	 */
	@HasPermission(Permission.AssetView)
	def assetsByClass() {
		renderSuccessJson(assetEntityService.getAssetsByClass(params))
	}

	@HasPermission(Permission.AssetView)
	def assetClasses() {
		def results = []
		assetEntityService.getAssetClasses().each { k,v -> results << [key:k, label:v]}
		renderSuccessJson(results)
	}

	@HasPermission(Permission.AssetView)
	def classForAsset() {
		renderSuccessJson(assetClass: AssetClass.getClassOptionForAsset(AssetEntity.load(params.id)))
	}

	@HasPermission(Permission.AssetExport)
	def poiDemo() {
	}

	@HasPermission(Permission.AssetExport)
	def exportPoiDemo() {
		String filePath = "/templates/TDSMaster_Poi_template.xls" // Template file Path
		String today = TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_TIME_5)
		String filename = "Demo_POI_Export-$today" // Export file name

		def assetEntities = AssetEntity.findAllByProject(securityService.loadUserCurrentProject(), [max:50])
		File file = grailsApplication.parentContext.getResource(filePath).getFile()

		//creating Workbook insatnce with using template fileInput stream
		Workbook workbook = WorkbookFactory.create(new FileInputStream(file))
		Sheet sheet = workbook.getSheet("Servers")

		//Get all column count of first row or header
		def dataTransferSetInstance = DataTransferSet.get(1)
		def serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName(dataTransferSetInstance,"Servers")
		def serverMap = [:]
		def serverColumnNameList =[]
		def serverSheetColumnNames = [:]
		serverDTAMap.eachWithIndex { item, pos ->
			serverMap[item.columnName] = null
			serverColumnNameList << item.columnName
		}
		serverMap.DepGroup = null
		serverColumnNameList << "DepGroup"

		def serverCol = sheet.getRow(0).getPhysicalNumberOfCells()
		for (int c = 0; c < serverCol; c++) {
			String serverCellContent = sheet.getRow(0).getCell(c).stringCellValue
			serverSheetColumnNames[serverCellContent] = c
			if (serverMap.containsKey(serverCellContent)) {
				serverMap[serverCellContent] = c
			}
		}
		for (int r=1; r<=assetEntities.size(); r++) {
			// creating row here
			Row row = sheet.createRow(r)
			Cell cell = row.createCell(0)
			cell.setCellValue(assetEntities[r-1].id)
			for (String colName in serverColumnNameList) {
				cell = row.createCell(serverMap[colName])
				def attribute = serverDTAMap.eavAttribute.attributeCode[serverMap[colName]]
				if (attribute) {
					cell.setCellValue(String.valueOf(assetEntities[r-1][attribute] ?: ""))
				}
			}
		}

		response.setContentType('application/vnd.ms-excel')
		filename = filename.replace('.xls', '')
		response.setHeader('Content-Disposition', 'attachment; filename="' +  filename + '.xls"')

		try {
			OutputStream out = new FileOutputStream(file)
			workbook.write(out)
			out.close()

			StreamUtils.copy new FileInputStream(new File(file.getAbsolutePath())), response.outputStream
		}
		catch (IOException e) {
			log.error e.message, e
		}
	}

	/**
	 * Returns a JSON object containing the data used by Select2 javascript library
	 * @param assetClassOption
	 * @param max
	 * @param page
	 * @param q
	 */
	@HasPermission(Permission.AssetView)
	def assetListForSelect2() {
		def results = []
		long total = 0

		Project project = securityService.userCurrentProject
		if (project) {

			// The following will perform a count query and then a query for a subset of results based on the max and page
			// params passed into the request. The query will be constructed with @COLS@ tag that can be substitued when performing
			// the actual queries.

			int max = NumberUtil.limit(params.int('max', 10), 1, 25)
			int currentPage = NumberUtil.limit(params.int('page', 1), 1, 1000)
			int offset = (currentPage - 1) * max

			// This map will drive how the query is constructed for each of the various options
			Map qmap = [
				APPLICATION:         [ assetClass: AssetClass.APPLICATION, domain: Application ],
				'SERVER-DEVICE':     [ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.serverTypes ],
				DATABASE:            [ assetClass: AssetClass.DATABASE, domain: Database ],
				'NETWORK-DEVICE':    [ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.networkDeviceTypes ],
				// 'NETWORK-LOGICAL':   [],
				'STORAGE-DEVICE':    [assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.storageTypes ],
				'STORAGE-LOGICAL':   [assetClass: AssetClass.STORAGE, domain: Files ],
				'OTHER-DEVICE':      [assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.nonOtherTypes, notIn: true ],
				ALL:                 [domain: AssetEntity ]
			]

			String queryColumns = 'a.id as id, a.assetName as text'
			String queryCount = 'COUNT(a)'

			StringBuffer query = new StringBuffer("SELECT @COLS@ FROM ")

			if (qmap.containsKey(params.assetClassOption)) {
				def qm = qmap[params.assetClassOption]
				def assetClass = qm.assetClass
				def qparams = [ project:project ]
				if (assetClass)
					qparams = [ project:project, assetClass:qm.assetClass ]

				query.append(qm.domain.name + ' AS a ')

				def doJoin = qm.containsKey('assetType')
				def notIn = qm.containsKey('notIn') && qm.notIn
				if (doJoin) {
					if (notIn) {
						query.append('LEFT OUTER JOIN a.model AS m ')
					} else {
						query.append('JOIN a.model AS m ')
					}
				}

				if (assetClass)
					query.append('WHERE a.project=:project AND a.assetClass=:assetClass ')
				else
					query.append('WHERE a.project=:project ')

				if (params.containsKey('q') && params.q.size() > 0) {
					query.append('AND a.assetName LIKE :q ')
					qparams.q = "%$params.q%"
				}

				if (doJoin) {
					if (notIn) {
						query.append("AND COALESCE(m.assetType,'') NOT ")
					} else {
						query.append("AND m.assetType ")
					}
					query.append('IN (:assetType)')
					qparams.assetType = qm.assetType
				}

				query.append("ORDER BY a.assetName ASC")

				log.debug "***** Query: $query\nParams: $qparams"

				// Perform query and move data into normal map
				def cquery = query.toString().replace('@COLS@', queryCount)
				log.debug "***** Count Query: $cquery"

				total = qm.domain.executeQuery(cquery, qparams)[0]

				if (total > 0) {
					def rquery = query.toString().replace('@COLS@', queryColumns)
					// rquery = rquery + " ORDER BY a.assetName"
					log.debug "***** Results Query: $rquery"

					results = qm.domain.executeQuery(rquery, qparams, [max:max, offset:offset, sort:'assetName' ])

					// Convert the columns into a map that Select2 requires
					results = results.collect{ r -> [ id:r[0], text: SEU.escapeHtml(SEU.escapeJavaScript(r[1])) ]}
				}
			} else {
				// TODO - Return an error perhaps by setting total to -1 and adding an extra property for a message
				log.error "assetListForSelect2() doesn't support param assetClassOption $params.assetClassOption"
			}
		}

		renderAsJson(results: results, total: total)
	}

	/**
	 * Returns the list of models for a specific manufacturer and asset type
	 */
	@HasPermission(Permission.ModelView)
	def modelsOf() {
		try {
			def models = assetEntityService.modelsOf(params.manufacturerId, params.assetType, params.term)
			renderSuccessJson(models: models)
		}
		catch (e) {
			handleException e, log
		}
	}

	/**
	 * Returns the list of manufacturers for a specific asset type
	 */
	@HasPermission(Permission.ManufacturerView)
	def manufacturer() {
		try {
			renderSuccessJson(manufacturers: assetEntityService.manufacturersOf(params.assetType, params.term))
		}
		catch (e) {
			handleException e, log
		}
	}

	/**
	 * Returns the list of asset types for a specific manufactures
	 */
	@HasPermission(Permission.AssetView)
	def assetTypesOf() {
		try {
			renderSuccessJson(assetTypes: assetEntityService.assetTypesOf(params.manufacturerId, params.term))
		}
		catch (e) {
			handleException e, log
		}
	}

	@HasPermission(Permission.ArchitectureView)
	def architectureViewer() {
		licenseAdminService.checkValidForLicenseOrThrowException()
		Project project = securityService.userCurrentProject
		def levelsUp = NumberUtils.toInt(params.levelsUp)
		int levelsDown = NumberUtils.toInt(params.levelsDown) ?: 3

		def assetName
		if (params.assetId) {
			assetName = AssetEntityHelper.getAssetById(project, null, params.assetId).assetName
		}

		Map<String, String> defaultPrefs = [levelsUp: '0', levelsDown: '3', showCycles: true,
		                                    appLbl: true, labelOffset: '2', assetClasses: 'ALL']
		def graphPrefs = userPreferenceService.getPreference(PREF.ARCH_GRAPH)
		Map prefsObject = graphPrefs ? JSON.parse(graphPrefs) : defaultPrefs

		def model = [
			assetId : params.assetId,
			assetName: assetName,
			levelsUp: levelsUp,
			levelsDown: levelsDown,
			assetClassesForSelect: [ALL: 'All Classes'] + AssetClass.classOptions,
			moveBundleList: assetEntityService.getMoveBundles(project),
			dependencyStatus: assetEntityService.getDependencyStatuses(),
			dependencyType: assetEntityService.getDependencyTypes(),
			assetTypes: AssetEntityService.ASSET_TYPE_NAME_MAP,
			defaultPrefs:defaultPrefs as JSON,
			graphPrefs:prefsObject,
			assetClassesForSelect2: AssetClass.classOptionsDefinition
		]
		render(view: 'architectureGraph', model: model, assetId: params.assetId)
	}

	/**
	 * Returns the data needed to generate the application architecture graph
	 */
	@HasPermission(Permission.ArchitectureView)
	def applicationArchitectureGraph() {
		try {
			Project project = securityService.userCurrentProject
			def assetId = NumberUtils.toInt(params.assetId)
			def asset = AssetEntity.get(assetId)
			def levelsUp = NumberUtils.toInt(params.levelsUp)
			def levelsDown = NumberUtils.toInt(params.levelsDown)
			def deps = []
			def sups = []
			def assetsList = []
			def dependencyList = []

			// maps asset type names to simpler versions
			def assetTypes = AssetEntityService.ASSET_TYPE_NAME_MAP

			// Check if the parameters are null
			if ((assetId == null || assetId == -1) || (params.levelsUp == null || params.levelsDown == null)) {
				def model = [nodes: [] as JSON, links: [] as JSON, assetId: params.assetId, levelsUp: params.levelsUp,
				             levelsDown: params.levelsDown, assetTypes: assetTypes, assetTypesJson: assetTypes as JSON,
				             environment: Environment.current.name]
				render(view: '_applicationArchitectureGraph', model: model)
				return
			}

			if (asset.project != project) {
				throw new UnauthorizedException()
			}

			// build the graph based on a specific asset
			if (params.mode == "assetId") {

				// recursively get all the nodes and links that depend on the asset
				def stack = []
				def constructDeps
				constructDeps = { a, l ->
					deps.push(a)
					if (! (a in assetsList)) {
						assetsList.push(a)
					}
					if (l > 0) {
						def dependent = AssetDependency.findAllByAsset(a)
						dependent.each {
							if (! (it in dependencyList)) {
								dependencyList.push(it)
							}
							constructDeps(it.dependent, l-1)
						}
					}
				}
				constructDeps(asset, levelsDown)

				// recursively get all the nodes and links that support the asset
				stack = []
				def constructSups
				constructSups = { a, l ->
					sups.push(a)
					if (! (a in assetsList)) {
						assetsList.push(a)
					}
					if (l > 0) {
						def supports = AssetDependency.findAllByDependent(a)
						supports.each {
							if (! (it in dependencyList)) {
								dependencyList.push(it)
							}
							constructSups(it.asset, l-1)
						}
					}
				}
				constructSups(asset, levelsUp)

			// this mode hasn't been implemented yet
			} else if (params.mode == "dependencyBundle") {
				def bundle = params.dependencyBundle
				def assets = assetDependencyBundle.findAllWhere(project:project, dependencyBundle:bundle)
			}

			// find any links between assets that weren't found with the DFS
			def assetIds = assetsList.id
			def extraDependencies = []
			assetsList.each { a ->
				AssetDependency.findAllByAssetAndDependentInList(a, assetsList).each { dep ->
					if (!(dep in dependencyList)) {
						extraDependencies.push(dep)
					}
				}
			}

			// add in any extra dependencies that were found
			dependencyList.addAll extraDependencies

			def serverTypes = AssetType.allServerTypes

			// Create the Nodes
			def graphNodes = []
			String name = ''
			def shape = 'circle'
			def size = 150
			String title = ''
			String color = ''
			String type = ''
			String assetType = ''
			String assetClass = ''
			Map criticalitySizes = [Minor: 150, Important: 200, Major: 325, Critical: 500]

			// create a node for each asset
			assetsList.each {

				// get the type used to determine the icon used for this asset's node
				assetType = it.model?.assetType ?: it.assetType
				assetClass = it.assetClass?.toString() ?: ''
				size = 150

				type = getImageName(assetClass, assetType)
				if (type == AssetType.APPLICATION.toString()) {
					size = it.criticality ? criticalitySizes[it.criticality] : 200
				}

				graphNodes << [
					id:it.id,
					name: SEU.escapeHtml(SEU.escapeJavaScript(it.assetName)),
					type:type, assetClass:it.assetClass.toString(),
					shape:shape, size:size,
					title: SEU.escapeHtml(SEU.escapeJavaScript(it.assetName)),
					color: it == asset ? 'red' : 'grey',
					parents:[], children:[], checked:false, siblings:[]
				]
			}

			// Create a seperate list of just the node ids to use while creating the links (this makes it much faster)
			def nodeIds = graphNodes*.id
			def defaults = moveBundleService.getMapDefaults(graphNodes.size())

			// Create the links
			def graphLinks = []
			def i = 0
			def opacity = 1
			def statusColor = 'grey'
			dependencyList.each {
				boolean notApplicable = !(it.status in [AssetDependencyStatus.ARCHIVED, AssetDependencyStatus.NA, AssetDependencyStatus.TESTING])
				def future = it.isFuture
				def unresolved = !it.isStatusResolved
				def sourceIndex = nodeIds.indexOf(it.asset.id)
				def targetIndex = nodeIds.indexOf(it.dependent.id)
				if (sourceIndex != -1 && targetIndex != -1) {
					graphLinks << [id: i, parentId: it.asset.id, childId: it.dependent.id, child: targetIndex,
					               parent: sourceIndex, value: 2, opacity: opacity, redundant: false, mutual: null,
					               notApplicable: notApplicable, future: future, unresolved: unresolved]
					++i
				}
			}

			// Set the dependency properties of the nodes
			graphLinks.each {
				if (!it.cyclical) {
					graphNodes[it.child].parents.add(it.id)
					graphNodes[it.parent].children.add(it.id)
				}
			}

			render(view:'_applicationArchitectureGraph',
			       model: [nodes: graphNodes as JSON, links: graphLinks as JSON, assetId: params.assetId,
			               levelsUp: params.levelsUp, levelsDown: params.levelsDown, assetTypes: assetTypes,
			               assetTypesJson: assetTypes as JSON, environment: Environment.current.name])
		}
		catch (e) {
			handleException e, log
		}
	}

	@HasPermission(Permission.UserGeneralAccess)
	def graphLegend() {
		render(view: '_graphLegend', model: [assetTypes: assetEntityService.ASSET_TYPE_NAME_MAP])
	}

	/**
	 * Used to retrieve the task information during the import process after it has been read in from the
	 * uploaded spreadsheet and reviewed for errors.
	 * @params filename - the filename that the temporary uploaded spreadsheet was saved as
	 * @return JSON { tasks: List of tasks }
	 */
	@HasPermission(Permission.RecipeGenerateTasks)
	def importTaskReviewData() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			renderErrorJson flash.remove('message')
			return
		}

		if (!params.filename) {
			renderErrorJson 'Request was missing the required filename reference'
			return
		}

		try {
			// TODO : JPM 4/2016 : importAccountsReviewData This method should be refactored so that the bulk of the logic
			// is implemented in the service.

			Map formOptions = taskImportExportService.importParamsToOptionsMap(params)
			renderAsJson taskImportExportService.generateReviewData(project, params.filename, formOptions)
		}
		catch(e) {
			log.error "Exception occurred while importing data: $e.message", e

			renderErrorJson(['An error occurred while attempting to import tasks', e.message])
		}
	}


	/**
	 * Cancels the import process that is in flight; deletes the uploaded spreadsheet
	 * and then redirects the user back to the Import Task view.
	 * @param params.filename - the filename that the temporary uploaded spreadsheet was saved as
	 * @return JSON{ accounts: List of accounts }
	 */
	@HasPermission(Permission.RecipeGenerateTasks)
	def cancelImport() {
		try {
			Project project = controllerService.getProjectForPage(this)
			if (!project) {
				return
			}

			taskImportExportService.cancelPreviousUpload(project, [filename: params.id])
			flash.message = 'The previous import was cancelled'
		}
		catch (InvalidRequestException | DomainUpdateException | InvalidParamException | EmptyResultException e) {
			flash.message = e.message
		}
		catch (e) {
			log.error 'cancelImport() Unexpected exception occurred while cancelling Task Import', e
			flash.message = 'An error occurred while attempting to cancel Task Import'
		}

		redirect(action: 'importTask')
	}

	/**
	 * Used to import tasks. This is a three
	 * step form that take param.step to track at what point the user is in the process. The steps include:
	 *     start  - The user is presented a form
	 *     upload - The user has uploaded the spreadsheet which is saved to a temporary random filename and the user
	 *              is presented with the validation results
	 *     post   - The previously confirmed and this submission will reload the saved spreadsheet and post the
	 *              changes to the database and delete the spreadsheet.
	 */
	@HasPermission(Permission.RecipeGenerateTasks)
	def importTask() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		String formAction = 'importTask'
		String currentStep = params.step ?: 'start'

		// fileParamName is the name of the parameter that the file will be uploaded as
		String fileParamName = 'importTasksSpreadsheet'
		Map model = [ step:currentStep, projectName:project.name, fileParamName:fileParamName ]


		Map options = taskImportExportService.importParamsToOptionsMap(params)
		String view = 'importTasks'

		// There is a bug or undocumented feature that doesn't allow overriding params when forwarding which is used
		// in the upload step to forward to the review so we look for the stepAlt and use it if found.
		String step = params.stepAlt ?: params.step
		try {

			switch(step) {

				case 'upload':
					// This step will save the spreadsheet that was posted to the server after reading it
					// and verifying that it has some accounts in it. If successful it will do a forward to
					// the review step.

					options.fileParamName = fileParamName
					model = taskImportExportService.processFileUpload(request, project, options)

					// forward to the Review step
					forward(action: formAction, params: [stepAlt: 'review', filename: model.filename,
					                                     importOption: params.importOption])
					return

				case 'review':
					// This step will serve up the review template that in turn fetch the review data
					// via an Ajax request.
					model << taskImportExportService.generateModelForReview(project, options)
					// log.debug "importAccounts() case 'review':\n\toptions=$options\n\tmodel=$model"
					if (!options.filename && model.filename) {
						// log.debug "importAccounts() step=$step set filename=${model.filename}"
						options.filename = model.filename
					}

					// This is used by the AJAX request in the form to construct the URL appropriately
					model.paramsForReviewDataRequest = [filename:model.filename, importOption:params.importOption]
					view = formAction + 'Review'
					break

				case 'post':
					// This is the daddy of the steps in that it is going to post the changes back to the
					// database.

					List optionErrors = taskImportExportService.validateImportOptions(options)
					if (optionErrors) {
						throw new InvalidParamException(optionErrors.toString())
					}

					options.testMode = params.testMode == 'Y'

					// Here's the money maker call that will update existing accounts and create new ones accordingly
					model.results = taskImportExportService.postChangesToTasks(project, options)

					log.debug "importTasks() post results = ${model.results}"

					// TODO BB generateModelForPostResults() is in AccountImportExportService
					model << taskImportExportService.generateModelForPostResults(options)
					if (!options.filename && model.filename) {
						options.filename = model.filename
					}

					// This is used by the AJAX request in the form to construct the URL appropriately
					model.paramsForReviewDataRequest = [filename:model.filename, importOption:params.importOption]

					view = formAction + 'Results'
					log.debug "importTasks() view = $view"

					break

				default:
					// The default which is the first step to prompt for the spreadsheet to upload
					break
			}
		} catch (e) {
			switch (e) {
				case InvalidRequestException:
				case DomainUpdateException:
				case InvalidParamException:
				case EmptyResultException:
					log.debug "importTasks() exception ${e.getClass().name} $e.message"
					flash.message = e.message
					break
				default:
					log.error "Exception occurred while importing data (step $currentStep)", e
					flash.message = "An error occurred while attempting to import tasks."
			}
			// Attempt to delete the temporary uploaded worksheet if an exception occurred
			if (options.filename) {
				log.error e.message, e
				taskImportExportService.deletePreviousUpload(options)
			}
			redirect (action: 'importTask')
		}

		// log.debug "importAccounts() Finishing up controller step=$step, view=$view, model=$model"
		render view:view, model:model
	}


	@HasPermission(Permission.RecipeGenerateTasks)
	def importTaskPostResultsData() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			renderErrorJson(flash.message)
			flash.message = ''
			return
		}
		if (!params.filename) {
			renderErrorJson('Request was missing the required filename reference')
			return
		}

		try {
			Map formOptions = taskImportExportService.importParamsToOptionsMap(params)
			File spreadsheetFile = taskImportExportService.generatePostResultsData(params.filename, formOptions)
			ServiceResults.respondAsJson(response, spreadsheetFile)
		}
		catch (e) {
			log.error 'importTaskPostResultsData() Exception occurred while retrieving import tasks post results: ', e

			renderErrorJson(['An error occurred while attempting to retrieve import tasks post results', e.message])
		}
	}




	/**
	 * Check if a user have permissions to create/edit comments
	 */
	private boolean userCanEditComments(commentType) {
		commentType == AssetCommentType.TASK || securityService.hasPermission(Permission.AssetEdit)
	}

	private String getImageName(String assetClassId, String type) {
		switch (assetClassId) {
			case AssetClass.APPLICATION.toString(): return AssetType.APPLICATION.toString()
			case AssetClass.DATABASE.toString(): return AssetType.DATABASE.toString()
			case AssetClass.STORAGE.toString(): return AssetType.FILES.toString()
			case AssetClass.DEVICE.toString():
				if (type in AssetType.virtualServerTypes) {
					return AssetType.VM.toString()
				}
				if (type in AssetType.physicalServerTypes) {
					return AssetType.SERVER.toString()
				}
				if (type in AssetType.storageTypes) {
					return AssetType.STORAGE.toString()
				}
				if (type in AssetType.networkDeviceTypes) {
					return AssetType.NETWORK.toString()
				}
		}

		return 'Other'
	}

	private Long getBatchId() {
		(Long) session.getAttribute('BATCH_ID')
	}

	private void setBatchId(long id) {
		session.setAttribute 'BATCH_ID', id
	}

	private Long getTotalAssets() {
		(Long) session.getAttribute('TOTAL_ASSETS')
	}

	private void setTotalAssets(long count) {
		session.setAttribute 'TOTAL_ASSETS', count
	}



	/**
	 * TODO This method should be refactored to another class.
	 * Returns a Map with the Estimated Start and Estimated Finish columns CSS class names, according to
	 * the relation between those estimates and the current date/time. Also uses the Actual Start value
	 * to make the calculations in case the task is in STARTED status, and a tardy factor value to
	 * determine when a task is not late yet but it's going to be soon.
	 * If a task estimate values are not late or tardy, it returns an empty string for the class name.
	 * For more information see TM-6318.
	 *
	 * @param task : The task.
	 * @param tardyFactor : The value used to evaluate if a task it's going to be late soon.
	 * @param nowGMT : The actual time in GMT.
	 * @return : A Map with estStartClass and estFinishClass
	 * @todo: refactor getEstimatedColumnsCSS into a service and create test cases
	 */
	private Map getEstimatedColumnsCSS(AssetComment task, Date nowGMT) {

		String estStartClass = ''
		String estFinishClass = ''

		Integer durationInMin = task.durationInMinutes()
		Integer tardyFactor = computeTardyFactor(durationInMin)

		Integer nowInMin = TimeUtil.timeInMinutes(nowGMT)
		Integer estStartInMin = TimeUtil.timeInMinutes(task.estStart)
		Integer estFinishInMin = TimeUtil.timeInMinutes(task.estFinish)
		Integer actStartInMin = TimeUtil.timeInMinutes(task.actStart)

		boolean taskIsActionable = task.isActionable()

		// Determine the Est Start CSS
 		if (estStartInMin && (task.status in [ AssetCommentStatus.PENDING, AssetCommentStatus.READY ]) )  {
			// Note that in the future when we have have slack calculations in the tasks, we can
			// flag tasks that started late and won't finished by critical path finish times but for
			// now we will just flag tasks that didn't start by their est start.
			if (estStartInMin < nowInMin) {
				estStartClass = 'task_late'
			} else if ( (estStartInMin - tardyFactor) < nowInMin) {
				estStartClass = 'task_tardy'
			}
		}

		// Determine the Estimated Finish CSS
		if (estFinishInMin && taskIsActionable) {
			if (actStartInMin) {
				// If the task was started then see if it should have completed by now and should be
				// considered tardy started early but didn't finish by duration + tardy factor.
				if (estFinishInMin < nowInMin) {
					estFinishClass = 'task_late'
				} else if ( (actStartInMin + durationInMin + tardyFactor) < nowInMin ) {
					// This will clue the PM that the task should have been completed by now
					estFinishClass = 'task_tardy'
				}
			} else {
				// Check if it would finish late
				if ( (estFinishInMin - durationInMin) < nowInMin) {
					estFinishClass = 'task_late'
				} else if ( (estFinishInMin - durationInMin - tardyFactor) < nowInMin ) {
					estFinishClass = 'task_tardy'
				}
			}
		}

		return [estStartClass: estStartClass, estFinishClass: estFinishClass]
	}


	/**
	 * TODO This method should be refactored to another class.
	 * Computes the tardy factor.
	 * The intent is to adjust the factor as a percent of the duration of the task to factor in
	 * the additional buffer of time with a minimum factor of 5 minutes and a maximum of 30 minutes.
	 * @param date : The task duration in minutes.
	 * @return : the tardy factor.
	 */
	private Integer computeTardyFactor(Integer durationInMin) {
		return Math.min(30, Math.max(5, (Integer)(durationInMin * 0.1)))
	}

}
