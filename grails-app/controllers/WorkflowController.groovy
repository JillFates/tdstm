import com.tds.asset.AssetComment
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundleStep
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RoleType
import net.transitionmanager.domain.StepSnapshot
import net.transitionmanager.domain.Swimlane
import net.transitionmanager.domain.Workflow
import net.transitionmanager.domain.WorkflowTransition
import net.transitionmanager.domain.WorkflowTransitionMap
import net.transitionmanager.security.Permission
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.StateEngineService
import org.springframework.jdbc.core.JdbcTemplate
// TODO BB all called

/**
 * @author Lokanada Reddy
 */
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class WorkflowController implements ControllerMethods {

	private static final List<String> standardTransitions = [
		'Hold', 'Ready', 'PoweredDown', 'Release', 'Unracking', 'Unracked', 'Cleaned', 'OnCart',
		'OnTruck', 'OffTruck', 'Staged', 'Reracking', 'Reracked', 'Completed', 'Terminated'].asImmutable()

	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	ProjectService projectService
	StateEngineService stateEngineService

	static defaultAction = 'home'

	/**
	 * Renders Workflow data.
	 */
	@HasPermission(Permission.WorkflowList)
	def home() {
		flash.message = params.message
		[workflowInstanceList: Workflow.list(params)]
	}

	/**
	 * Renders Workflow steps for selected workflow
	 * @param : workfow
	 */
	@HasPermission(Permission.WorkflowView)
	def workflowList() {
		String workflowId = params.workflow
		if (!workflowId) {
			redirect(action: 'home')
			return
		}

		Workflow workflow = Workflow.get(params.workflow)
		stateEngineService.loadWorkflowTransitionsIntoMap(workflow.process, 'project')

		List<Integer> transitionIds = jdbcTemplate.queryForList('''
			SELECT mbs.transition_id as transitionId
			FROM move_bundle_step mbs
			left join move_bundle mb on mb.move_bundle_id = mbs.move_bundle_id
			left join project p on p.project_id = mb.project_id
			where p.workflow_code=?
			group by mbs.transition_id''', workflow.process)*.transitionId

		List<WorkflowTransition> transitions = WorkflowTransition.findAllByWorkflow(workflow, [sort: 'transId'])

		List<Map> workflowTransitionsList = transitions.collect { transition ->
			[transition: transition, isExist: transitionIds.contains(transition.transId),
			 donotDelete: !standardTransitions.contains(transition.code)]
		}

		[workflowTransitionsList: workflowTransitionsList, workflow: workflow,
		 roles: partyRelationshipService.getStaffingRoles()]
	}

	/**
	 * Create  new workflow workflow
	 */
	@HasPermission(Permission.WorkflowCreate)
	def createWorkflow() {
		def process = params.process
		if (!process || !securityService.loggedIn) {
			redirect(action: 'home')
			return
		}

		String message
		Date dateNow = TimeUtil.nowGMT()
		Workflow workflow = new Workflow(process: process)
		if (!workflow.save(failOnError: false)) {
			message = 'Workflow "' + workflow + '" must be unique'
		}
		else {
			Workflow stdWorkflow = Workflow.get(params.workflow)
			if (stdWorkflow) {
				/* create Standard swimlanes to the workflow */
				Swimlane.findAllByWorkflow(stdWorkflow).each { stdSwimlane ->
					Swimlane swimlane = new Swimlane(workflow: workflow, name: stdSwimlane.name,
							actorId: stdSwimlane?.actorId)
					if (swimlane.save(flush: true)) {
						log.debug('Swimlane "{}" created', swimlane)
					}
				}

				/* create Standard Workflow transitions to the workflow */
				def stdWorkflowTransitions = WorkflowTransition.findAllByWorkflow(stdWorkflow)
				def defaultRole = RoleType.read('ROLE_PROJ_MGR')
				for (stdWorkflowTransition in stdWorkflowTransitions) {
					def workflowTransition = new WorkflowTransition(
							workflow: workflow,
							code: stdWorkflowTransition.code,
							name: stdWorkflowTransition.name,
							transId: stdWorkflowTransition.transId,
							type: stdWorkflowTransition.type,
							color: stdWorkflowTransition.color,
							dashboardLabel: stdWorkflowTransition.dashboardLabel,
							predecessor: stdWorkflowTransition.predecessor,
							header: stdWorkflowTransition.header,
							duration: stdWorkflowTransition.duration,
							role: stdWorkflowTransition.role ?: defaultRole)
					saveWithWarnings workflowTransition
					if (!workflowTransition.hasErrors()) {
						log.debug('Workflow step "{}" created', workflowTransition)
					}
				}

				/* Create workflow roles based on the template roles*/
				List<Swimlane> swimlanes = Swimlane.findAllByWorkflow(workflow)
				List<WorkflowTransition> transitions = WorkflowTransition.findAllByWorkflow(workflow)
				List<WorkflowTransitionMap> maps = WorkflowTransitionMap.findAllByWorkflow(stdWorkflow)
				maps.each { map ->
					def transition = transitions.find {
						it.code == map.workflowTransition.code && it.transId == map.workflowTransition.transId
					}
					def swimlane = swimlanes.find { it.name == map.swimlane.name }

					def workflowTransitionMap = new WorkflowTransitionMap(workflow: workflow, swimlane: swimlane,
							workflowTransition: transition, transId: map.transId, flag: map.flag)
					saveWithWarnings workflowTransitionMap
					if (!workflowTransitionMap.hasErrors()) {
						log.debug('Workflow Roles "{}" created', workflowTransitionMap)
					}
				}
			}
			message = 'Workflow "' + workflow + '" was created'
		}

		redirect(action: 'home', params: [message: message])
	}

	/**
	 * Update the workflow steps for selected workflow.
	 * @param : workflowId, steps
	 */
	@HasPermission(Permission.WorkflowEdit)
	def updateWorkflowSteps() {
		def workflowId = params.workflow
		def workflowTransitionsList = []
		def workflow
		def roles
		if (workflowId && securityService.loggedIn) {
			flash.message = ''
			workflow = Workflow.get(workflowId)
			workflow.updatedBy = securityService.loadCurrentPerson()
			if (workflow.save()) {
				log.debug('Workflow "{}" updated', workflow)
			}

			WorkflowTransition.findAllByWorkflow(workflow).each { transition ->
				transition.code = params['code_' + transition.id]
				transition.name = params['name_' + transition.id]
				transition.transId = params.int('transId_' + transition.id)
				transition.category = params['category_' + transition.id]
				transition.dashboardLabel = params['dashboardLabel_' + transition.id]
				transition.duration = params.int('duration_' + transition.id)
				transition.role = RoleType.load(params['role_' + transition.id])
				transition.save(flush:true)
			}
			// add new steps to the workflow
			def additionalSteps = Integer.parseInt(params.additionalSteps)
			for(int i=1; i <= additionalSteps; i++) {
				def workflowTransition = new WorkflowTransition(workflow : workflow,
					code : params['code_' + i],
					name : params['name_' + i],
					transId : params.int('transId_' + i),
					// type : params['type_' + i],
					type : 'boolean',
					category : params['category_' + i],
					// color : params['color_' + i],
					dashboardLabel : params['dashboardLabel_' + i],
					// predecessor : params.int('predecessor_' + i)
					// header : params['header_' + i],
					role : RoleType.load(params['role_' + i]),
					//effort : params.int('effort_' + i),
					duration : params.int('duration_' + i))

				if (! workflowTransition.validate() || ! workflowTransition.save(flush:true, failOnError:false)) {
					flash.message += 'Workflow step with code [' + workflowTransition.code + '] must be unique.'
				} else {
					log.debug('Workflow step "{}" updated', workflowTransition)
				}
			}
			def query = """SELECT mbs.transition_id as transitionId FROM move_bundle_step mbs
							left join move_bundle mb on mb.move_bundle_id = mbs.move_bundle_id
							left join project p on p.project_id = mb.project_id
							where p.workflow_code = '$workflow.process' group by mbs.transition_id"""
			def stepsExistInWorkflowProject = jdbcTemplate.queryForList(query)

			WorkflowTransition.findAll('FROM WorkflowTransition where workflow=? order by transId', [workflow]).each { transition ->
				boolean isExist = stepsExistInWorkflowProject.transitionId?.contains(transition.transId)

			workflowTransitionsList << [transition : transition, isExist : isExist ]
			}
			//load transitions details into application memory.
			stateEngineService.loadWorkflowTransitionsIntoMap(workflow.process, 'workflow')
			roles = partyRelationshipService.getStaffingRoles()
		} else {
			redirect(action:"home")
		}
		render(view : 'workflowList', model : [ workflowTransitionsList : workflowTransitionsList, workflow : workflow, roles:roles ])
	}

	@HasPermission(Permission.WorkflowEdit)
	def updateWorkflowRoles() {
		def currentStatus = params.currentStatus
		def workflowId = params.workflow
		def workflow = Workflow.get(workflowId)
		def swimlanes = Swimlane.findAllByWorkflow(workflow)
		def onTruck = WorkflowTransition.findByWorkflowAndCode(workflow, "OnTruck")?.transId
		def hold =  WorkflowTransition.findByWorkflowAndCode(workflow, "hold")?.transId
		def currentTransition = WorkflowTransition.get(currentStatus)
		def workflowTransitions = WorkflowTransition.findAll(
			"FROM WorkflowTransition w where w.workflow = ? AND w.code not in ('SourceWalkthru','TargetWalkthru') ",
			[workflow])

		swimlanes.each { role ->
			workflowTransitions.each { transition ->
				def input = params[role.name + '_' + transition.id]
				if (input && input.equalsIgnoreCase("on")) {
					def workflowransitionMap = WorkflowTransitionMap.findAll("From WorkflowTransitionMap wtm where wtm.workflowTransition = ? and wtm.swimlane = ? and wtm.transId = ?", [ currentTransition, role, transition.transId ])
					def flag = params["flag_${role.name}_$transition.id"]
					if (!workflowransitionMap.size()) {
						new WorkflowTransitionMap(workflow:workflow, workflowTransition:currentTransition,swimlane:role,transId:transition.transId, flag:flag).save(flush:true)
					}else{
						workflowransitionMap.each {
							it.flag = flag
							it.save(flush:true)
						}
					}
				} else {
					def workflowransitionMap = WorkflowTransitionMap.findAll("From WorkflowTransitionMap wtm where wtm.workflowTransition = ? and wtm.swimlane = ? and wtm.transId = ?", [ currentTransition, role, transition.transId ])
					if (workflowransitionMap.size()) {
						workflowransitionMap.each {
							it.delete(flush:true)
						}
					}
				}
			}

			String core = '(trans_id) FROM workflow_transition_map where swimlane_id=? and trans_id'
			Integer maxSourceId = jdbcTemplate.queryForObject('SELECT Max' + core + '<?', Integer, role.id, onTruck)
			Integer maxTargetId = jdbcTemplate.queryForObject('SELECT Max' + core + '>=?', Integer, role.id, onTruck)
			Integer minSourceId = jdbcTemplate.queryForObject('SELECT Min' + core + '<? and trans_id>?', Integer, role.id, onTruck, hold)
			Integer minTargetId = jdbcTemplate.queryForObject('SELECT Min' + core + '>=?', Integer, role.id, onTruck)

			if (minSourceId) {
				def workflowTransition = WorkflowTransitionMap.findAllBySwimlaneAndTransId(role, minSourceId,
						[sort: 'transId', order: 'asc'])?.workflowTransition
				def minProcessIds = workflowTransition?.findAll { it.transId < minSourceId }?.sort { it.transId }
				minSourceId = minProcessIds.transId[0]
				role.minSource = minSourceId ? stateEngineService.getState(workflow.process, minSourceId) : null
			}
			if (minTargetId) {
				def workflowTransition = WorkflowTransitionMap.findAllBySwimlaneAndTransId(role,minTargetId,
						[sort: 'transId', order: 'asc'])?.workflowTransition
				def minProcessIds = workflowTransition?.findAll { it.transId < minTargetId }?.sort { it.transId }
				minTargetId = minProcessIds.transId[0]
				role.minTarget = minTargetId ? stateEngineService.getState(workflow.process, minTargetId) : null
			}

			role.maxTarget = maxTargetId ? stateEngineService.getState(workflow.process, maxTargetId) : null
			role.maxSource = maxSourceId ? stateEngineService.getState(workflow.process, maxSourceId) : null

			role.save(flush:true)
		}
		//	load transitions details into application memory.
		stateEngineService.loadWorkflowTransitionsIntoMap(workflow.process, 'workflow')

		redirect(action:"workflowList", params:[workflow:workflowId])
	}

	/**
	 * @param : workfow, workflowTransition
	 * Delete the workflowTransition and associated data.
	 */
	@HasPermission(Permission.WorkflowEdit)
	def deleteTransitionFromWorkflow() {
		Workflow workflow = Workflow.get(params.workflow)
		workflow.updatedBy = securityService.loadCurrentPerson()
		workflow.save(flush:true)

		def transitionId = params.id
		if (transitionId) {
			WorkflowTransition workflowTransition = WorkflowTransition.get(transitionId)
			if (workflowTransition) {
				// verify if workflow transition has comments related to it
				def assetsComments = AssetComment.executeQuery(
						"select count(*) from AssetComment where workflowTransition >= :workflowTransition", [workflowTransition: workflowTransition])
				if (assetsComments[0] > 0) {
					flash.message = "Workflow step [${workflowTransition.code}] cannot be deleted because it has linked Asset Comments."
				} else {
					def process = workflowTransition.workflow.process
					StepSnapshot.executeUpdate('''
					delete StepSnapshot
					where moveBundleStep in (select id from MoveBundleStep
					                         where moveBundle.project.workflowCode=:workflowCode
					                           and transitionId=:transitionId)
				''', [workflowCode: workflowTransition.workflow.process, transitionId: NumberUtil.toInteger(transitionId)])
					MoveBundleStep.executeUpdate('''
					delete MoveBundleStep
					where moveBundle in (select id from MoveBundle where workflowCode=:workflowCode)
					  and transitionId=:transitionId
				''', [workflowCode: workflowTransition.workflow.process, transitionId: workflowTransition.transId])
					WorkflowTransitionMap.executeUpdate('delete WorkflowTransitionMap where workflowTransition=?',
							[workflowTransition])
					workflowTransition.delete(flush: true)

					//	load transitions details into application memory.
					stateEngineService.loadWorkflowTransitionsIntoMap(process, 'workflow')
				}
			}
		}
		redirect(action: "workflowList", params: [workflow: workflow.id])
	}

	/**
	 * @param : workfow
	 * Delete the workflow and associated projects and project's data.
	 */
	@HasPermission(Permission.WorkflowDelete)
	def deleteWorkflow() {
		def workflowId = params.id
		if (workflowId) {
			def workflow = Workflow.get(workflowId)
			def process = workflow.process
			def workflowProjects = Project.findAllByWorkflowCode(workflow?.process)
			try {
				workflowProjects.each { project ->
					projectService.deleteProject(project.id, false)
				}
				WorkflowTransitionMap.executeUpdate("delete WorkflowTransitionMap where workflow=?",[workflow])
				WorkflowTransition.executeUpdate('delete WorkflowTransition where workflow=?',[workflow])
				Swimlane.executeUpdate('delete Swimlane where workflow=?',[workflow])
				workflow.delete()

				stateEngineService.loadWorkflowTransitionsIntoMap(process, 'workflow')
			}
			catch (e) {
				flash.message = e.message
			}
		}
		redirect(action: "home", params: [message: flash.message])
	}

	/**
	 *  Update Swimlane actor Id thru ajax request
	 *  @author : Dinesh
	 *  @param : workflow, swimlaneName, actorId
	 *  @return : updated actorId
	 */
	@HasPermission(Permission.WorkflowEdit)
	def saveActorName() {
		def actorId = params.actorId
		def workFlowId = params.workflow
		def swimLaneName = params.swimlaneName
		def workFlow = Workflow.read(workFlowId)
		def swimlane = Swimlane.findWhere(name: swimLaneName , workflow : workFlow)
		swimlane?.actorId = actorId
		swimlane.save(flush:true)

		render actorId
	}
}
