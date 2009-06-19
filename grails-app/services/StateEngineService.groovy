import org.springframework.beans.factory.InitializingBeanimport org.springframework.web.context.request.RequestContextHolderimport org.codehaus.groovy.grails.commons.ApplicationHolder/** * User: John Martin * Date: Apr 14, 2009 * Time: 2:33:15 PM *//*------------------------------------------------------------------------------------------ * The StateEngineService provides a state management engine driven by definitions in * XML files.  This service is used with workflow logic to help manage a stateful system. *------------------------------------------------------------------------------------------*/public class StateEngineService implements InitializingBean {	def userPreferenceService	static transactional = false	static def processMap = new HashMap()	/*	 * Constructor	 * Loads all workflow process definitions from the resource directory into an internal	 * map to be use by workflow processes afters.	 */	private StateEngineService() {			// Properly initialize the processMap variable and any additional vars		// Search the resource directory for all files workflow_*.xml and call the loadProcessDefinition		// method		}	/*-------------------------------------------------------------------------	 * @author : Lokanath Reddy	 * @return : Call the loadProcessDefinition function by passing workflow_standerd.xml 	 * Invoked by a BeanFactory after it has set all bean properties supplied 	 *-------------------------------------------------------------------------*/	void afterPropertiesSet()    {		try{			File file =  ApplicationHolder.application.parentContext.getResource("/resource/workflow_standard.xml").getFile() 			//File file = new File("/WEB-INF/workflow_standard.xml")		    	        loadProcessDefinition(file)		}catch (Exception ex) {            ex.printStackTrace()        }		    }	/*------------------------------------------------------------------------------------------------------	 * @author : Lokanath Reddy	 * @param  : Process, Swimeline and currentState 	 * Used to get the list of valid tasks that are associated to a particular swimlane based	 * on the current state for the specified process flow.	 * @return list of states that a task can be changed to	 * @AssertEquals getTasks("STD_PROCESS","ROLE_WF_MANAGER", "Ready") , ['Hold', 'PoweredDown', 'Release]	 *------------------------------------------------------------------------------------------------------*/	def getTasks( String process, String swimlane, String currentState ) {        if(processMap[process][swimlane,currentState]){            return processMap[process][swimlane,currentState].keySet()        } else {            return []        }	}	/* --------------------------------------------------------------------------------------	 * @author : Lokanath Reddy	 * @param  : Process, Swimeline and currentState 	 * Used to get the list of valid tasks that are associated to a particular swimlane based	 * on the current state for the specified process flow.  This will include the additional	 * data (i.e. flags) associated with each state.	 * @return list of states that a task can be changed to	 *---------------------------------------------------------------------------------------*/	def getTasksExtended( String process, String swimlane, String currentState ) {        if(processMap[process][swimlane,currentState]){            return processMap[process][swimlane,currentState].keySet()        } else {            return []        }	}	/*------------------------------------------------------------------------------------------	 * @author : Lokanath Reddy	 * @param  : Process, Swimeline , currentState and toState 	 * Used to get the flags associated with a given state	 * @assertEquals getFlags("STD_PROCESS","ROLE_WF_MANAGER", "Ready", "Release"), "skipped"	 * @return list of states that a task can be changed to	 *-----------------------------------------------------------------------------------------*/	def getFlags( String process, String swimlane, String currentState, String toState ) {		if(processMap[process][swimlane,currentState]){			if(processMap[process][swimlane,currentState][toState]){	            return processMap[process][swimlane,currentState][toState]["flag"]	        } else {	            return []	        }		} else {		    return []		}	}		/*--------------------------------------------------------------	 * @author : Lokanath Reddy	 * @param  : Process, Swimeline , currentState and toState 	 * Used to verify the ROLE can change the currentState / toState	 * @return boolean flag	 *-------------------------------------------------------------*/	def canDoTask( String process, String swimlane, String currentState, String toState ) {		boolean flag = false		def fromStateMap = processMap[process][swimlane,currentState]		if( fromStateMap != null){			if(fromStateMap[toState] != null){				flag = true			}		}		return flag 	}	/*----------------------------------------------	 * @author : Lokanath Reddy	 * @param  : Process, currentState	 * Used to get status for Corresponding statusId	 * @return the state value for id 	 *---------------------------------------------*/	def getState( String process, Integer currentState ) {		if(processMap[process]['transitions'][currentState.toString()]){			return processMap[process]['transitions'][currentState.toString()]['to']		} else {			return ""		}	}	/*----------------------------------------------	 * @author : Lokanath Reddy	 * @param  : Process, currentState	 * Used to get status for Corresponding statusId	 * @return the state Label for id 	 *---------------------------------------------*/	def getStateLabel( String process, Integer currentState ) {		if(processMap[process]['transitions'][currentState.toString()]){			return processMap[process]['transitions'][currentState.toString()]['name']		} else {			return ""		}	}	/*----------------------------------------------	 * @author : Lokanath Reddy	 * @param  : Process, "TASK_ID"	 * Used to get status for Corresponding statusId	 * @return list of all states	 *---------------------------------------------*/	def getTasks( String process , String task) {		return processMap[process][task]	}	/*---------------------------------------------	 * @author : Lokanath Reddy	 * @param  : Process, state	 * Used to get statusId for Corresponding status	 * @return the id value for state 	 *---------------------------------------------*/	def getStateId( String process, String state ) {		return processMap[process][state]	}	/*------------------------------------------------------------------------------------------	 * @author : Lokanath Reddy	 * @param  : workflow_standerd.xml as File	 * Using XMLSluper to read XML Workflow Process definition and load into processMap	 *------------------------------------------------------------------------------------------*/	def loadProcessDefinition( xmlText ) {		def map = new HashMap() 		def xml = new XmlSlurper().parse(xmlText)				// Iterate over the list of transitions and create a list of values		// 	color		//		ordinal position in the list				def tlist= new HashMap()		def taskList = []		def taskToList = []		// tlist[		// 	'Hold' : [color: 'red', seq: 1],		//		'Ready' : [color: 'yellow', seq: 2], ...		xml.transitions.each{			it.transition.each{				tlist.put(it.@id.text() , ['color' : it.@color.text(),'seq':it.@seq.text(),'to':it.@to.text(),'name':it.@name.text() ])				map.put(it.@to.text(),it.@id.text())				taskList << it.@id.text()				taskToList << it.@to.text()			}		}		map.put('transitions', tlist)		map.put('TASK_ID', taskList)		map.put('TASK_NAME', taskToList)        // Iterate over the list of swimlanes and build a list putting into		// map['swimlanes']		def slist = []		xml.swimlane.each{			slist<<it.@name.text()		}		map.put('swimlanes', slist)		// Iterate over all of the task-nodes and build list of task actions by swimlanes		// taskTranstions [		// 	'Hold': [flag: transition.flag],		//		'Release': [flag: transition.flag], ...		// map.swimlanes.$swimlane = taskTransitions		// map['ROLE_WF_SUPERVISOR'.'Ready'] = ['Hold'..., 'Release'..., ...]		def taskNodes = xml.taskNode		taskNodes.each{tasknode ->						tasknode.task.each{task->				def taskTransitions = new HashMap()                task.transition.each{ transition ->					taskTransitions.put(transition.@to.text() ,[ 'flag' : transition.@flag.text()] )				}				map.put([task.@swimlane.text(),tasknode.@name.text()],taskTransitions)							}		}		// startStates		def startStates = xml.startState		startStates.each{startState ->						startState.task.each{task->				def taskTransitions = new HashMap()                task.transition.each{ transition ->					taskTransitions.put(transition.@to.text() ,[ 'flag' : transition.@flag.text()] )				}				map.put([task.@swimlane.text(),startState.@name.text()],taskTransitions)							}		}		// All data about a process definition will be keyed off of the process-definition.code element		//processMap.put(process-definition.code, )		// processMap['STD_PROCESS'] = []		processMap.put(xml.@code.text(),map )	}		/*-------------------------------------------------------	 * @author : Lokanath Reddy	 * @param  : State and Process	 * @return : StateId as Integer 	 *-------------------------------------------------------*/	def getStateIdAsInt( String process, String state ) {		return Integer.parseInt( processMap[process][state] )	}}