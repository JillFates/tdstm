package net.transitionmanager.service

import com.tdssrc.grails.NumberUtil
import net.transitionmanager.project.Swimlane
import net.transitionmanager.project.Workflow
import net.transitionmanager.project.WorkflowTransition
import net.transitionmanager.project.WorkflowTransitionMap
/**
 * Provides a state management engine driven by definitions in XML files, used
 * with workflow logic to help manage a stateful system.
 * 'Project customs unable to Update: ' + GormUtil.allErrorsString(project)
 * @author John Martin
 */
class StateEngineService implements ServiceMethods {

	static transactional = false

	private static Map processMap = [:]

	def loadWorkflowTransitionsIntoMap(processCode, action) {
		// Reload the map if it has been modified
		// TODO : There should be a semaphore around this to prevent people from trying to access the map while it being loaded/reloaded
		if (action == 'workflow') {
			processMap.remove(processCode)
		}

		// Now load or reload the workflow meta data appropriately
		if (action == 'workflow' || !processMap.containsKey(processCode)) {
			log.info 'Loading Workflow Map [{}]', processCode
			def taskNodeList = []
			def map = [:]
			def tlist = [:]
			def tType = [:]
			def predecessors = [:]
			def dashboardLabels = [:]
			def taskList = []
			def taskToList = []
			def headColor = [:]
			/*
			 * get the workflow transitions for the current project
			 * */
			def workflow = Workflow.findByProcess(processCode)
			if (workflow) {
				def transitions = WorkflowTransition.findAllByWorkflow(workflow)
				def transitionsMap = WorkflowTransitionMap.findAllByWorkflow(workflow)
				def swimlanesList = Swimlane.findAllByWorkflow(workflow)
				// Iterate the transitions and load into processMap
				transitions.each { transition ->
//					if (processCode=='STD_PROCESS') {
//						log.error 'Loading Transition: {}, {}, {}', transition.code, transition.name, transition.id
//					}
					tlist[transition.transId.toString()] = [color: transition.color, code: transition.code,
															name : transition.name, id: transition.id]
					tType[transition.code] = transition.type
					headColor[transition.code] = transition.header
					predecessors[transition.transId.toString()] = transition.predecessor
					dashboardLabels[transition.transId.toString()] = transition.dashboardLabel
					map[transition.code] = transition.transId.toString()
					taskList << transition.transId
					taskToList << transition.code
					headColor.put(transition.code, transition.header)
					def subTransitions

					swimlanesList.each { swimlane ->
						def taskTransitions = [:]
						subTransitions = transitionsMap.findAll {
							it.workflowTransition.id == transition.id && it.swimlane.name == swimlane.name
						}
						subTransitions.each { task ->
							taskTransitions.put(transitions.find { it.transId == task.transId }?.code, [flag: task.flag])
						}
						map.put([swimlane.name, transition.code], taskTransitions)
					}
					if (subTransitions?.size()) {
						taskNodeList << transition.code
					}
				}
				map.transitions = tlist
				map.TASK_ID = taskList
				map.TASK_NAME = taskToList
				map.TASK_TYPE = tType
				map.HEADER_COLOR = headColor
				map.PREDECESSOR = predecessors
				map.DASHBOARD_LABEL = dashboardLabels
				// All data about a process definition will be keyed off of the process-definition.code element
				processMap[processCode] = map
				def taskNodeListLength = taskNodeList.size()
				def taskNodeErrorList = []
				for (int i = 0; i < taskNodeListLength; i++) {
					if (!taskToList.contains(taskNodeList[i])) {
						taskNodeErrorList << taskNodeList[i]
					}
				}
				if (taskNodeErrorList.size() > 0) {
					println " $processCode transition code = $taskNodeErrorList doesn't exist "
					System.exit(0)
				}
			}
		}
	}

	/**
	 * Get the valid tasks that are associated to a particular swimlane based
	 * on the current state for the specified process flow.
	 * @param : Process, Swimeline and currentState
	 * @return list of states that a task can be changed to
	 * @AssertEquals getTasks( "STD_PROCESS" , "ROLE_WF_MANAGER" , "Ready" ) , ['Hold', 'PoweredDown', 'Release]
	 * ------------------------------------------------------------------------------------------------------ */
	def getTasks(String process, String swimlane, String currentState) {
		if (processMap[process][swimlane, currentState]) {
			sortTasks(process, processMap[process][swimlane, currentState].keySet())
		} else {
			[]
		}
	}

	/* --------------------------------------------------------------------------------------
	 * @author : lokanada Reddy
	 * @param  : Process, Swimeline and currentState
	 * Used to get the list of valid tasks that are associated to a particular swimlane based
	 * on the current state for the specified process flow.  This will include the additional
	 * data (i.e. flags) associated with each state.
	 * @return list of states that a task can be changed to
	 *---------------------------------------------------------------------------------------*/

	def getTasksExtended(String process, String swimlane, String currentState) {
		if (processMap[process][swimlane, currentState]) {
			sortTasks(process, processMap[process][swimlane, currentState].keySet())
		} else {
			[]
		}
	}

	/**
	 * Get the flags associated with a given state
	 * @param : Process, Swimeline, currentState and toState
	 * @assertEquals getFlags( "STD_PROCESS" , "ROLE_WF_MANAGER" , "Ready" , "Release" ) , "skipped"
	 * @return list of states that a task can be changed to
	 * ----------------------------------------------------------------------------------------- */
	def getFlags(String process, String swimlane, String currentState, String toState) {
		if (processMap[process][swimlane, currentState]) {
			if (processMap[process][swimlane, currentState][toState]) {
				return processMap[process][swimlane, currentState][toState]["flag"]
			} else {
				return []
			}
		} else {
			return []
		}
	}

	def getState(String process, Integer currentState) {
		if (processMap[process]['transitions'][currentState.toString()]) {
			return processMap[process]['transitions'][currentState.toString()]['code']
		} else {
			log.error 'getState: Cannot find state [{}] of workflow [{}]', currentState, process
			return ""
		}
	}
	/*----------------------------------------------
	 * @author : lokanada Reddy
	 * @param  : Process, currentState
	 * Used to get status for Corresponding statusId
	 * @return the state Label for id
	 *---------------------------------------------*/

	def getStateLabel(String process, Integer currentState) {
		if (processMap[process]['transitions'][currentState.toString()]) {
			return processMap[process]['transitions'][currentState.toString()]['name']
		} else {
			log.error 'getStateLabel: Cannot find label for state [{}] of workflow [{}]', currentState, process
			return ""
		}
	}

	/*----------------------------------------------
	 * @author : lokanada Reddy
	 * @param  : Process, "TASK_ID"
	 * Used to get status for Corresponding statusId
	 * @return list of all states
	 *---------------------------------------------*/

	def getTasks(String process, String task) {
		def tasks = processMap[process][task]
		def sourceWalkthru = getStateId(process, "SourceWalkthru")
		def targetWalkthru = getStateId(process, "TargetWalkthru")
		tasks.remove(sourceWalkthru)
		tasks.remove(targetWalkthru)
		return tasks
	}
	/*---------------------------------------------
	 * @author : lokanada Reddy
	 * @param  : Process, state
	 * Used to get statusId for Corresponding status
	 * @return the id value for state
	 *---------------------------------------------*/

	def getStateId(String process, String state) {
		if (processMap[process]) {
			return processMap[process][state]
		}

		log.error 'Unable to find id for state [{}] of workflow [{}]', state, process
		return "0"
	}

	/*---------------------------------------------
	 * @author : lokanada Reddy
	 * @param  : Process, state
	 * Used to get statusId for Corresponding status
	 * @return the id value for state
	 *---------------------------------------------*/

	def getStateType(String process, String state) {
		return processMap[process]["TASK_TYPE"][state]
	}

	/*-------------------------------------------------------
	 * @author : lokanada Reddy
	 * @param  : State and Process
	 * @return : StateId as Integer
	 *-------------------------------------------------------*/

	def getStateIdAsInt(String process, String state) {
		def stateId = processMap[process][state]
		return stateId ? Integer.parseInt(processMap[process][state]) : null
	}

	/*-------------------------------------------------------
	 * @author : Mallikarjun
	 * @return : Process Code
	 *-------------------------------------------------------*/

	def getWorkflowCode() {
		return Workflow.findAll()?.process
	}

	/*------------------------------------------------------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : Process, transitionId
	 * Used to get the predecessor associated with a given transitionId
	 * @assertEquals getPredecessor("STD_PROCESS",60),
	 * @return predecessor that is associated with transitonId
	 *-----------------------------------------------------------------------------------------*/

	def getPredecessor(String process, Integer transitionId) {

		if (processMap[process]['PREDECESSOR'][transitionId.toString()]) {
			return processMap[process]['PREDECESSOR'][transitionId.toString()]
		} else {
			return false
		}
	}

	/*------------------------------------------------------------------------------------------
	 * @author : Lokanada Reddy
	 * @param  : Process, transitionId
	 * Used to get the DASHBOARD_LABEL associated with a given transitionId
	 * @assertEquals getFlags("STD_PROCESS",60),
	 * @return DASHBOARD_LABEL that is associated with transitonId
	 *-----------------------------------------------------------------------------------------*/

	def getDashboardLabel(String process, Integer transitionId) {

		if (processMap[process]['DASHBOARD_LABEL'][transitionId.toString()]) {
			return processMap[process]['DASHBOARD_LABEL'][transitionId.toString()]
		} else { // if DASHBOARD_LABEL not exist return the name of the transition
			return getStateLabel(process, transitionId)
		}
	}

	/**
	 * The process will be to get the entire list, then remove those steps referenced by the predecessor field.
	 *  For instance, Unracked has predecessor Unracking, so you would remove Unracking from the list.
	 * @param : Process
	 * @return : entire list except that have predecessor field.
	 */
	def getDashboardSteps(String process) {
		def dashboardSteps = []
		// loop the list of tasks and assign them into list
		getTasks(process, 'TASK_ID').each {
			int taskId = NumberUtil.toInteger(it)
			def predecessor = getPredecessor(process, taskId)

			def dashboardLabel = getDashboardLabel(process, taskId)

			// add dashboard labels into list
			dashboardSteps << [id: taskId, label: dashboardLabel, name: getStateLabel(process, taskId)]

			// remove the predecessor from the list if exist
			if (predecessor) {
				dashboardSteps.remove(id: predecessor, label: getDashboardLabel(process, predecessor),
									  name: getStateLabel(process, predecessor))
			}
		}

		log.debug('Dashboard Steps are --> {}', dashboardSteps)

		return dashboardSteps
	}

	/**
	 *  Sort the list by transId
	 */
	def sortTasks(process, list) {
		def listIds = []
		list.each {
			def stateId = getStateIdAsInt(process, it)

			if (stateId) {
				listIds.add(getStateIdAsInt(process, it))
			}
		}
		def listToReturn = []
		listIds.sort().each {
			listToReturn << getState(process, it)
		}
		return listToReturn
	}
}
