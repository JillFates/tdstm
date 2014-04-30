import org.codehaus.groovy.grails.commons.ApplicationHolder
import com.tdssrc.grails.GormUtil
import org.apache.shiro.SecurityUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/*------------------------------------------
 * @author : Lokanada Reddy
 * Controller to perform the workflow CRUD operations
 * ----------------------------------------*/
class WorkflowController {
	protected static Log log = LogFactory.getLog( WorkflowController.class )
	def static standardTransitions = ["Hold", "Ready", "PoweredDown", "Release", "Unracking", "Unracked", "Cleaned", "OnCart", 
									  "OnTruck", "OffTruck", "Staged", "Reracking", "Reracked", "Completed", "Terminated"]
	/* Initialize the services */
	def stateEngineService
	def jdbcTemplate
	def userPreferenceService
    def partyRelationshipService
	def projectService
	def securityService
	/*-----------------------------------------------
	 * Index method for default action
	 *---------------------------------------------*/
	def index = { redirect(action:home,params:params) }
	
	/*-----------------------------------------------
	 *  Will render Workflow data
	 *---------------------------------------------*/
	def home = {
		flash.message = params.message
		[ workflowInstanceList: Workflow.list( params ) ]
	}

	/*-----------------------------------------------
	 * @param : workfow
	 * Will render Workflow steps for selected workflow
	 *---------------------------------------------*/
	def workflowList = {
		def workflowId = params.workflow
		def workflowTransitionsList = []
		def workflow
		def workflowTransitions
		def stepsExistInWorkflowProject
		if( workflowId ){
			workflow = Workflow.get(params.workflow)
			stateEngineService.loadWorkflowTransitionsIntoMap(workflow.process, 'project')
			def query = """SELECT mbs.transition_id as transitionId FROM move_bundle_step mbs 
							left join move_bundle mb on mb.move_bundle_id = mbs.move_bundle_id
							left join project p on p.project_id = mb.project_id 
							where p.workflow_code = '${workflow.process}' group by mbs.transition_id"""
			stepsExistInWorkflowProject = jdbcTemplate.queryForList( query )
			
			workflowTransitions = WorkflowTransition.findAll("FROM WorkflowTransition w where w.workflow = ? order by w.transId", [workflow] )
		} else {
			redirect(action:home)
		}
		
		workflowTransitions.each{ workflowTransition ->
			def isExist = false
			def donotDelete = false
			if(!standardTransitions.contains(workflowTransition.code))
				donotDelete = true
			if( stepsExistInWorkflowProject.size() > 0 && stepsExistInWorkflowProject?.transitionId?.contains( workflowTransition.transId ))
				isExist = true
				
			workflowTransitionsList << [transition : workflowTransition, isExist : isExist, donotDelete : donotDelete ]
		}
		def roles = partyRelationshipService.getStaffingRoles()
		return [workflowTransitionsList : workflowTransitionsList, workflow : workflow, roles:roles ]
	}
	/*-----------------------------------------------
	 * @param : workfow stepId
	 * provide controlle to set the role to change the status.
	 *---------------------------------------------*/
	def workflowRoles = {
			def transitionId = params.workflowTransition
			def workflowTransitionsList
			def workflowTransition
			def roleWiseTransitions = []
			def swimlanes
			def headerCount =0
			if( transitionId ){
				workflowTransition = WorkflowTransition.get( transitionId )
				workflowTransitionsList = WorkflowTransition.findAll("FROM WorkflowTransition w where w.workflow = ? AND w.code not in ('SourceWalkthru','TargetWalkthru') order by w.transId", [workflowTransition?.workflow] )
				def workflowTransitionMap = WorkflowTransitionMap.findAllByWorkflow( workflowTransition?.workflow )
				swimlanes = Swimlane.findAllByWorkflow( workflowTransition?.workflow )
				// construct a map for different swimlane roles
				workflowTransitionsList.each{ transition ->
					def transitionsMap = []
					swimlanes.each{ role ->
						transitionsMap << [ swimlane : role, workflowTransitionMap : workflowTransitionMap.find{it.workflowTransition.id == workflowTransition.id && it.swimlane.id ==  role.id && it.transId == transition.transId}]
					}
					roleWiseTransitions << [ transition:transition, transitionsMap : transitionsMap]
				}
			
			} else {
				redirect(action:home)
			}
			
			return [workflowTransitionsList : workflowTransitionsList, workflowTransition:workflowTransition,
					workflow : workflowTransition?.workflow, swimlanes : swimlanes,
					headerCount : headerCount, roleWiseTransitions : roleWiseTransitions ]
	}
	/*====================================================
	 *  Create  new workflow workflow
	 *==================================================*/
	def createWorkflow = {
		def process = params.process
		def workflow = params.workflow
		def principal = SecurityUtils.subject.principal
		def message
		if(process && principal){
			def userLogin = UserLogin.findByUsername( principal )
			def dateNow = GormUtil.convertInToGMT( "now", "EDT" )
			def workflowInstance = new Workflow( process : process, 
										dateCreated : dateNow,
										lastUpdated : dateNow,
										updateBy : userLogin.person
										)
			if ( ! workflowInstance.validate() || ! workflowInstance.save(insert : true, flush:true) ) {
				message =  "Workfolw \"${workflowInstance}\" should be unique"
			} else {
				def stdWorkflow = Workflow.get( workflow )
				if( stdWorkflow ){
					/* create Standerd swimlanes to the workflow */
					def stdSwimlanes = Swimlane.findAllByWorkflow( stdWorkflow )
					stdSwimlanes.each{ stdSwimlane->
						def swimlane = new Swimlane( workflow:workflowInstance, name:stdSwimlane.name, actorId:stdSwimlane?.actorId)
						if (  swimlane.validate() && swimlane.save( flush:true) ) {
							log.debug("Swimlane \"${swimlane}\" created")
						}
					}
					/* create Standerd Workflow transitions to the workflow */
					def stdWorkflowTransitions = WorkflowTransition.findAllByWorkflow( stdWorkflow )
                    def defaultRole = RoleType.read("PROJ_MGR")
					stdWorkflowTransitions.each{ stdWorkflowTransition ->
						def workflowTransition = new WorkflowTransition(workflow : workflowInstance,
							code : stdWorkflowTransition.code, 
							name : stdWorkflowTransition.name,
							transId : stdWorkflowTransition.transId,
							type : stdWorkflowTransition.type,	
							color : stdWorkflowTransition.color,
							dashboardLabel : stdWorkflowTransition.dashboardLabel,
							predecessor : stdWorkflowTransition.predecessor,
							header : stdWorkflowTransition.header,
							duration : stdWorkflowTransition.duration,
                            role : stdWorkflowTransition.role?:defaultRole
							)
						if (  workflowTransition.validate() && workflowTransition.save( flush:true) ) {
							log.debug(" Workfolw step \"${workflowTransition}\" created")
						} else {
                            log.error(" Workfolw step \"${workflowTransition}\" create issue:"+GormUtil.allErrorsString( workflowTransition ))
						}
					}
					/* Create workflow roles based on the template roles*/
					def swimlanes = Swimlane.findAllByWorkflow( workflowInstance )
					def workflowTransitions = WorkflowTransition.findAllByWorkflow( workflowInstance )
					def workflowTransitionMaps = WorkflowTransitionMap.findAllByWorkflow( stdWorkflow )
					workflowTransitionMaps.each{ map->
						def workflowTransition = workflowTransitions.find{	it.code == map.workflowTransition.code &&
																			it.transId == map.workflowTransition.transId }
						def swimlane = swimlanes.find{	it.name == map.swimlane.name }
						
						def workflowTransitionMap = new WorkflowTransitionMap(workflow:workflowInstance, workflowTransition:workflowTransition,swimlane:swimlane,transId:map.transId, flag:map.flag )
						if (  workflowTransitionMap.validate() && workflowTransitionMap.save( flush:true) ) {
							log.debug(" Workfolw Roles \"${workflowTransitionMap}\" created")
						}
					}
				}
				message = "Workflow \"${workflowInstance}\" was created"
			}
		}
		redirect( action:home, params:[ message : message ] )
	}
	/*====================================================
	 *  Update the workflow steps for selected workflow
	 *  @param : workflowId, steps 
	 *==================================================*/
	def updateWorkflowSteps = {
		def workflowId = params.workflow
		def workflowTransitions
		def workflowTransitionsList = []
		def workflow
		def principal = SecurityUtils.subject.principal
		def roles
		if(workflowId && principal){
			 flash.message = ""
			workflow = Workflow.get( workflowId )
			// update the workflow updated by
			workflow.updateBy = UserLogin.findByUsername( principal )?.person
			if ( workflow.validate() && workflow.save(insert : true, flush:true) ) {
				log.debug("Workfolw \"${workflow}\" updated")
			}	
			workflowTransitions = WorkflowTransition.findAllByWorkflow( workflow )
			workflowTransitions.each{ workflowTransition ->
				
				workflowTransition.code = params["code_"+workflowTransition.id]
				workflowTransition.name = params["name_"+workflowTransition.id]
				workflowTransition.transId = params["transId_"+workflowTransition.id] ? Integer.parseInt( params["transId_"+workflowTransition.id] ) : null
				workflowTransition.type = params["type_"+workflowTransition.id]
				workflowTransition.category = params["category_"+workflowTransition.id]
				workflowTransition.color = params["color_"+workflowTransition.id]
				workflowTransition.dashboardLabel = params["dashboardLabel_"+workflowTransition.id]
				workflowTransition.predecessor = params["predecessor_"+workflowTransition.id] ? Integer.parseInt( params["predecessor_"+workflowTransition.id] ) : null
				workflowTransition.header = params["header_"+workflowTransition.id]
				//workflowTransition.effort = params["effort_"+workflowTransition.id] ? Integer.parseInt( params["effort_"+workflowTransition.id] ) : null
				workflowTransition.duration = params["duration_"+workflowTransition.id] ? Integer.parseInt( params["duration_"+workflowTransition.id] ) : null
				workflowTransition.role = RoleType.get(params["role_"+workflowTransition.id])
				if (  ! workflowTransition.validate() || ! workflowTransition.save( flush:true) ) {
					//workflowTransition.errors.allErrors.each() { flash.message += it }
				} else {
					log.debug("Workfolw step \"${workflowTransition}\" updated")
				}
			}
			// add new steps to the workflow
			def additionalSteps = Integer.parseInt(params.additionalSteps)
			for(int i=1; i <= additionalSteps; i++){
				def workflowTransition = new WorkflowTransition(workflow : workflow,
					code : params["code_$i"], 
					name : params["name_$i"],
					transId : params["transId_$i"] ? Integer.parseInt( params["transId_$i"] ) : null,
					type : params["type_$i"],	
					category : params["category_$i"],
					color : params["color_$i"],
					dashboardLabel : params["dashboardLabel_$i"],
					predecessor : params["predecessor_$i"] ? Integer.parseInt( params["predecessor_$i"] ) : null,
					header : params["header_$i"],		
					role : RoleType.get(params["role_$i"]),
					//effort : params["effort_$i"] ? Integer.parseInt( params["effort_$i"] ) : null,
					duration : params["duration_$i"] ? Integer.parseInt( params["duration_$i"] ) : null
					)
				
				if (  ! workflowTransition.validate() || ! workflowTransition.save( flush:true) ) {
					flash.message += "["+workflowTransition.code +"]"
				} else {
					log.debug("Workfolw step \"${workflowTransition}\" updated")
				}
			}
			def query = """SELECT mbs.transition_id as transitionId FROM move_bundle_step mbs 
							left join move_bundle mb on mb.move_bundle_id = mbs.move_bundle_id
							left join project p on p.project_id = mb.project_id 
							where p.workflow_code = '${workflow.process}' group by mbs.transition_id"""
			def stepsExistInWorkflowProject = jdbcTemplate.queryForList( query )
			workflowTransitions = WorkflowTransition.findAll("FROM WorkflowTransition w where w.workflow = ? order by w.transId", [workflow] )
			
			workflowTransitions.each{ workflowTransition ->
				def isExist = false
			if( stepsExistInWorkflowProject.size() > 0 && stepsExistInWorkflowProject?.transitionId?.contains( workflowTransition.transId ) )
				isExist = true
				
			workflowTransitionsList << [transition : workflowTransition, isExist : isExist ]
			}
			//load transitions details into application memory.
			stateEngineService.loadWorkflowTransitionsIntoMap(workflow.process, 'workflow')
			roles = partyRelationshipService.getStaffingRoles()
		} else {
			redirect(action:home)
		}
		render( view : 'workflowList', model : [ workflowTransitionsList : workflowTransitionsList, workflow : workflow, roles:roles ] )
	}
	/*====================================================
	 *  Update the workflow roles
	 *==================================================*/
	def updateWorkflowRoles = {
		def currentStatus = params.currentStatus
		def workflowId = params.workflow
		def workflow = Workflow.get( workflowId )
		def swimlanes = Swimlane.findAllByWorkflow( workflow )
		def onTruck = WorkflowTransition.findByWorkflowAndCode( workflow, "OnTruck" )?.transId
		def hold =  WorkflowTransition.findByWorkflowAndCode( workflow, "hold" )?.transId
		def currentTransition = WorkflowTransition.get( currentStatus )
		def workflowTransitions = WorkflowTransition.findAll("FROM WorkflowTransition w where w.workflow = ? AND w.code not in ('SourceWalkthru','TargetWalkthru') ", [ workflow ] )
		swimlanes.each{ role->
			workflowTransitions.each{transition->
				def input = params["${role.name}_${transition.id}"]
				if(input && input.equalsIgnoreCase("on")){
					def workflowransitionMap = WorkflowTransitionMap.findAll("From WorkflowTransitionMap wtm where wtm.workflowTransition = ? and wtm.swimlane = ? and wtm.transId = ?", [ currentTransition, role, transition.transId ])
					def flag = params["flag_${role.name}_${transition.id}"]
					if(!workflowransitionMap.size()){
						workflowransitionMap = new WorkflowTransitionMap(workflow:workflow, workflowTransition:currentTransition,swimlane:role,transId:transition.transId, flag:flag ).save(flush:true)
					}else{
						workflowransitionMap.each{
							it.flag = flag
							it.save( flush:true )
						}
					}
				} else {
					def workflowransitionMap = WorkflowTransitionMap.findAll("From WorkflowTransitionMap wtm where wtm.workflowTransition = ? and wtm.swimlane = ? and wtm.transId = ?", [ currentTransition, role, transition.transId ])
					if(workflowransitionMap.size()){
						workflowransitionMap.each{
							it.delete( flush:true )
						}
					}
				}
			}
			def maxSourceId = jdbcTemplate.queryForInt("SELECT Max(trans_id) FROM workflow_transition_map where swimlane_id = ${role.id} and trans_id < ${onTruck}")
			def maxTargetId = jdbcTemplate.queryForInt("SELECT Max(trans_id) FROM workflow_transition_map where swimlane_id = ${role.id} and trans_id >= ${onTruck}")
            def minSourceId = jdbcTemplate.queryForInt("SELECT Min(trans_id) FROM workflow_transition_map where swimlane_id = ${role.id} and trans_id < ${onTruck} and trans_id > ${hold}")
			def minTargetId = jdbcTemplate.queryForInt("SELECT Min(trans_id) FROM workflow_transition_map where swimlane_id = ${role.id} and trans_id >= ${onTruck}")
			
			if(minSourceId){
				def workflowTransition = WorkflowTransitionMap.findAllBySwimlaneAndTransId(role,minSourceId, [sort:"transId", order:"asc"])?.workflowTransition
				def minProcessIds = workflowTransition?.findAll { it.transId < minSourceId }?.sort{it.transId}
				minSourceId = minProcessIds.transId[0]
				role.minSource = minSourceId ? stateEngineService.getState( workflow.process, minSourceId ) : null
			}
			if(minTargetId){
				def workflowTransition = WorkflowTransitionMap.findAllBySwimlaneAndTransId(role,minTargetId, [sort:"transId", order:"asc"])?.workflowTransition
				def minProcessIds = workflowTransition?.findAll { it.transId < minTargetId }?.sort{it.transId}
				minTargetId = minProcessIds.transId[0]
				role.minTarget = minTargetId ? stateEngineService.getState( workflow.process, minTargetId ) : null
			}
			
			role.maxTarget = maxTargetId ? stateEngineService.getState( workflow.process, maxTargetId ) : null
			role.maxSource = maxSourceId ? stateEngineService.getState( workflow.process, maxSourceId ) : null
			
			if(!role.save( flush:true )){
				role.errors.each {  println it}
			}
		}
		//	load transitions details into application memory.
    	stateEngineService.loadWorkflowTransitionsIntoMap(workflow.process, 'workflow')
		
		redirect(action:workflowList, params:[workflow:workflowId])
	 }
		
	/*-----------------------------------------------
	 * @param : workfow, workflowTransition
	 * Delete the workflowTransition and associated data.
	 *---------------------------------------------*/
	def deleteTransitionFromWorkflow = {
		def workflowId = params.workflow
		def transitionId = params.id
		if(transitionId){
			def workflowTransition = WorkflowTransition.get( transitionId )
			def process = workflowTransition.workflow.process
			//AssetTransition.executeUpdate('delete from')
			StepSnapshot.executeUpdate("delete from StepSnapshot ss where ss.moveBundleStep in (select mbs.id from MoveBundleStep mbs where mbs.moveBundle.project.workflowCode = '${workflowTransition.workflow.process}' and mbs.transitionId = ${workflowTransition.transId})")
			MoveBundleStep.executeUpdate("delete from MoveBundleStep mbs where mbs.moveBundle in (select mb.id from MoveBundle mb where mb.workflowCode = '${workflowTransition.workflow.process}') and mbs.transitionId = ?",[ workflowTransition.transId])
			WorkflowTransitionMap.executeUpdate("delete from WorkflowTransitionMap wtm where wtm.workflowTransition = ?",[workflowTransition])
			workflowTransition.delete(flush:true)
			
			//	load transitions details into application memory.
	    	stateEngineService.loadWorkflowTransitionsIntoMap( process, 'workflow')
		}
		redirect(action:workflowList, params:[workflow:workflowId])
	}
	/*-----------------------------------------------
	 * @param : workfow
	 * Delete the workflow and associated projects and project's data.
	 *---------------------------------------------*/
	def deleteWorkflow = {
		def workflowId = params.id
		if(workflowId){
			def workflow = Workflow.get( workflowId )
			def process = workflow.process
			def workflowProjects = Project.findAllByWorkflowCode(workflow?.process)
			try {
				workflowProjects.each{ project ->
					projectService.deleteProject(project, securityService.getUserLogin())
					def party = Party.get(project.id)
					party.delete()
				}
				WorkflowTransitionMap.executeUpdate("delete from WorkflowTransitionMap wtm where wtm.workflow = ?",[workflow])
				WorkflowTransition.executeUpdate("delete from WorkflowTransition wt where wt.workflow = ?",[workflow])
				Swimlane.executeUpdate("delete from Swimlane s where s.workflow = ?",[workflow])
				workflow.delete()
				//	load transitions details into application memory.
				stateEngineService.loadWorkflowTransitionsIntoMap( process, 'workflow')
			} catch (Exception ex) {
				flash.message = ex.getMessage()
			}
		}
		redirect(action:home, params:[ message : flash.message ] )
	}
	/*-----------------------------------------------
	 * @param : workfow
	 * Generate .svg file for vertical text display in FF/ Safari
	 *---------------------------------------------*/
	def generateHeder(def workflowId){
			
			def workflow = Workflow.get( workflowId )
			def tempTransitions = WorkflowTransition.findAllByWorkflow( workflow, [sort:"transId"] )
		       
			def svgHeaderFile = new StringBuffer()
			svgHeaderFile.append("<?xml version='1.0' encoding='UTF-8' standalone='no'?>")
			svgHeaderFile.append("<!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' 'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'>")
			svgHeaderFile.append("<svg version='1.1' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink'>")
			svgHeaderFile.append("<script type='text/javascript'>")
			svgHeaderFile.append("<![CDATA[")
			svgHeaderFile.append("//this will create htmljavascriptfunctionname in html document and link it to changeText")
			svgHeaderFile.append("top.htmljavascriptfunctionname = changeText;")
			svgHeaderFile.append("function changeText(txt){")
			svgHeaderFile.append("targetText=document.getElementById('thetext');")
			svgHeaderFile.append("var newText = document.createTextNode(txt);")
			svgHeaderFile.append("targetText.replaceChild(newText,targetText.childNodes[0]);")
			svgHeaderFile.append("}")
			svgHeaderFile.append("// ]]>")
			svgHeaderFile.append("</script>")
			svgHeaderFile.append("<text id='thetext' text-rendering='optimizeLegibility' transform='rotate(270, 90, 0)' font-weight='bold' "+
								"font-size='12' fill='#333333' x='-44' y='-76' font-family='verdana,arial,helvetica,sans-serif'>")
			def count = 0
			tempTransitions.sort{it.transId}.each{ transition ->
				if(transition.code == "SourceWalkthru" || transition.code == "TargetWalkthru") return 
				def processTransition = transition.name
				def fillColor = transition.header
				if( !fillColor ){
					fillColor = transition.type == "boolean" ? "#FF8000" : "#336600"
				 }
				if(count == 0){
					svgHeaderFile.append("<tspan fill='$fillColor' id='${transition.transId}'>${processTransition}</tspan>")
				} else {
					svgHeaderFile.append("<tspan x='-44' dy='26.2' fill='$fillColor' id='${transition.transId}'>${processTransition}</tspan>")
				}
				count++
			}
			svgHeaderFile.append("</text>")
			svgHeaderFile.append("<path d='M 26.2 0 l 0 140")
			def value = 26.2
			for(int i=0;i<count;i++){
				value = value+26.2
				svgHeaderFile.append(" M ${value} 0 l 0 140")
			}
			svgHeaderFile.append("' stroke = '#FFFFFF' stroke-width = '1'/>")
			svgHeaderFile.append("</svg>")
			def f = ApplicationHolder.application.parentContext.getResource("templates/headerSvg_workflow.svg").getFile()
			def fop=new FileOutputStream(f)
			if(f.exists()){
				fop.write(svgHeaderFile.toString().getBytes())
				fop.flush()
				fop.close()
			} else {
				log.error("This file is not exist")
			}
			return count
	}
	/**
	 *  Update Swimlane actor Id thru ajax request
	 *  @author : Dinesh
	 *  @param : workflow, swimlaneName, actorId
	 *  @return : updated actorId
	 */
	def saveActorName={
		def actorId = params.actorId
		def workFlowId = params.workflow
		def swimLaneName = params.swimlaneName
		def workFlow = Workflow.read( workFlowId )
		def swimlane = Swimlane.findWhere( name : swimLaneName , workflow : workFlow )
		swimlane?.actorId = actorId
		if(!swimlane.save(flush:true)){
			println"Error while updating swimlane : "
			swimlane.errors.each { println it }
		}
		render actorId
	}
	
}
