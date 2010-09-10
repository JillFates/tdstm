import org.springframework.beans.factory.InitializingBeanimport org.springframework.web.context.request.RequestContextHolderimport org.codehaus.groovy.grails.commons.ApplicationHolderimport org.apache.commons.logging.Logimport org.apache.commons.logging.LogFactory/** * User: John Martin * Date: Apr 14, 2009 * Time: 2:33:15 PM *//*------------------------------------------------------------------------------------------ * The StateEngineService provides a state management engine driven by definitions in * XML files.  This service is used with workflow logic to help manage a stateful system. *------------------------------------------------------------------------------------------*/public class StateEngineService implements InitializingBean {		protected static Log log = LogFactory.getLog( StateEngineService.class )		def userPreferenceService	def jdbcTemplate	static transactional = false	static def processMap = new HashMap()	/*	 * Constructor	 * Loads all workflow process definitions from the resource directory into an internal	 * map to be use by workflow processes afters.	 */	private StateEngineService() {			// Properly initialize the processMap variable and any additional vars		// Search the resource directory for all files workflow_*.xml and call the loadProcessDefinition		// method		}	/*-------------------------------------------------------------------------	 * @author : lokanada Reddy	 * @return : Call the loadProcessDefinition function by passing workflow_standerd.xml 	 * Invoked by a BeanFactory after it has set all bean properties supplied 	 *-------------------------------------------------------------------------*/	void afterPropertiesSet()    {/*		try{			jdbcTemplate.update("delete from workflow_transition")			def filePath = ApplicationHolder.application.parentContext.getResource("/WEB-INF/workflow").file			// Get file path			def dir = new File( "${filePath.absolutePath}" )		    def children = dir.list()		    if ( children ) {		    	for (int i=0; i<children.length; i++) {		            // Get filename		            def filename = children[i]		            if ( filename.endsWith('.xml') ) {			            def xmlFile =  ApplicationHolder.application.parentContext.getResource( "/WEB-INF/workflow/${filename}" ).getFile()			            loadProcessDefinition( xmlFile )		            }		        }		    } 		}catch (Exception ex) {            ex.printStackTrace()        }		    */}			/*------------------------------------------------------------------------------------------	 * @author : Lokanada Reddy	 * @param  : project workflowCode	 * loading workflow transitions details into processMap	 *------------------------------------------------------------------------------------------*/	def loadWorkflowTransitionsIntoMap( processCode ) {		def taskNodeList = []		def map = new HashMap() 		def tlist= new HashMap()		def tType = new HashMap()		def predecessors = new HashMap()		def dashboardLabels = new HashMap()		def taskList = []		def taskToList = []		def headColor = new HashMap()		def transList = new StringBuffer()		/*		 * get the workflow transitions for the current project		 * */		def workflow = Workflow.findByProcess( processCode )		if(workflow){			def transitions = WorkflowTransition.findAllByWorkflow( workflow )			def transitionsMap = WorkflowTransitionMap.findAllByWorkflow( workflow )			def swimlanesList = Swimlane.findAllByWorkflow( workflow )			// Iterate the transitions and load into processMap			transitions.each{ transition->				tlist.put( transition.transId.toString(), [ 'color' : transition.color,'code':transition.code,'name':transition.name ] )				tType.put(transition.code , transition.type )				headColor.put(transition.code , transition.header)				predecessors.put( transition.transId.toString() , transition.predecessor )				dashboardLabels.put( transition.transId.toString() , transition.dashboardLabel )				map.put(transition.code, transition.transId.toString())				taskList << transition.transId.toString()				taskToList << transition.code				headColor.put( transition.code, transition.header )				def subTransitions 								swimlanesList.each{ swimlane ->					def taskTransitions = new HashMap()					subTransitions = transitionsMap.findAll { it.workflowTransition.id == transition.id && it.swimlane.name == swimlane.name }					subTransitions.each{ task->						taskTransitions.put(transitions.find{it.transId == task.transId}.code ,[ 'flag' : task.flag] )					}					map.put([swimlane.name,transition.code],taskTransitions)				}				if(subTransitions.size()) taskNodeList << transition.code			 }			map.put('transitions', tlist)			map.put('TASK_ID', taskList)			map.put('TASK_NAME', taskToList)			map.put('TASK_TYPE', tType)			map.put('HEADER_COLOR', headColor)			map.put( 'PREDECESSOR', predecessors )			map.put( 'DASHBOARD_LABEL', dashboardLabels )			// All data about a process definition will be keyed off of the process-definition.code element			processMap.put(processCode,map )			def taskNodeListLength = taskNodeList.size()			def taskNodeErrorList = []			for ( int i=0; i<taskNodeListLength; i++ ) {				if ( !taskToList.contains( taskNodeList[i] ) ) {					taskNodeErrorList << taskNodeList[i]				}			}			if ( taskNodeErrorList.size() > 0 ) {				println " ${processCode} transition code = ${taskNodeErrorList} doesn't exist "				System.exit(0)			}		}	}	/*------------------------------------------------------------------------------------------	 * @author : Lokanada Reddy	 * @param  : workflow_standerd.xml as File	 * Using XMLSluper to read XML Workflow Process definition and load into processMap	 *------------------------------------------------------------------------------------------*/	def loadProcessDefinition( xmlText ) {		def taskNodeList = []		def map = new HashMap() 		def xml = new XmlSlurper().parse(xmlText)				// Iterate over the list of transitions and create a list of values		// 	color		//		ordinal position in the list				def tlist= new HashMap()		def tType = new HashMap()		def predecessors = new HashMap()		def dashboardLabels = new HashMap()		def taskList = []		def taskToList = []		def headColor = new HashMap()		def transList = new StringBuffer()		// tlist[		// 	'Hold' : [color: 'red', seq: 1],		//		'Ready' : [color: 'yellow', seq: 2], ...		xml.transitions.each{			it.transition.each{				tlist.put(it.@id.text() , ['color' : it.@color.text(),'seq':it.@seq.text(),'to':it.@to.text(),'name':it.@name.text() ])				tType.put(it.@to.text() , it.@type.text())				headColor.put(it.@to.text() , it.@header.text())				predecessors.put( it.@id.text() , it.@predecessor.text() )				dashboardLabels.put( it.@id.text() , it.@dashboardLabel.text() )				map.put(it.@to.text(),it.@id.text())				taskList << it.@id.text()				taskToList << it.@to.text()				headColor				transList.append("('${xml.@code.text()}',${it.@id.text()},'${it.@to.text()}','${it.@name.text()}','${it.@type.text()}','${it.@color.text()}'),")			}		}		def transListString = transList.toString()		/* Insert transition into Workflow Transition table*/		jdbcTemplate.update("insert into workflow_transition(process,trans_id,code,name,type,color) values "+transListString.substring(0,transListString.lastIndexOf(','))+"");		map.put('transitions', tlist)		map.put('TASK_ID', taskList)		map.put('TASK_NAME', taskToList)		map.put('TASK_TYPE', tType)		map.put('HEADER_COLOR', headColor)		map.put( 'PREDECESSOR', predecessors )		map.put( 'DASHBOARD_LABEL', dashboardLabels )        // Iterate over the list of swimlanes and build a list putting into		// map['swimlanes']		def slist = []		xml.swimlane.each{			slist<<it.@name.text()		}		map.put('swimlanes', slist)		// Iterate over all of the task-nodes and build list of task actions by swimlanes		// taskTranstions [		// 	'Hold': [flag: transition.flag],		//		'Release': [flag: transition.flag], ...		// map.swimlanes.$swimlane = taskTransitions		// map['ROLE_WF_SUPERVISOR'.'Ready'] = ['Hold'..., 'Release'..., ...]		def taskNodes = xml.taskNode		taskNodes.each{tasknode ->						tasknode.task.each{task->				def taskTransitions = new HashMap()                task.transition.each{ transition ->					taskTransitions.put(transition.@to.text() ,[ 'flag' : transition.@flag.text()] )				}				map.put([task.@swimlane.text(),tasknode.@name.text()],taskTransitions)							}			taskNodeList << tasknode.@name.text()		}		// startStates		def startStates = xml.startState		startStates.each{startState ->						startState.task.each{task->				def taskTransitions = new HashMap()                task.transition.each{ transition ->					taskTransitions.put(transition.@to.text() ,[ 'flag' : transition.@flag.text()] )				}				map.put([task.@swimlane.text(),startState.@name.text()],taskTransitions)							}		}		// All data about a process definition will be keyed off of the process-definition.code element		//processMap.put(process-definition.code, )		// processMap['STD_PROCESS'] = []		processMap.put(xml.@code.text(),map )		def taskNodeListLength = taskNodeList.size()		def taskNodeErrorList = []		for ( int i=0; i<taskNodeListLength; i++ ) {			if ( !taskToList.contains( taskNodeList[i] ) ) {				taskNodeErrorList << taskNodeList[i]			}		}		if ( taskNodeErrorList.size() > 0 ) {			println " ${xml.@code.text()} transition to = ${taskNodeErrorList} doesn't exist "			System.exit(0)		}	}	/*------------------------------------------------------------------------------------------------------	 * @author : lokanada Reddy	 * @param  : Process, Swimeline and currentState 	 * Used to get the list of valid tasks that are associated to a particular swimlane based	 * on the current state for the specified process flow.	 * @return list of states that a task can be changed to	 * @AssertEquals getTasks("STD_PROCESS","ROLE_WF_MANAGER", "Ready") , ['Hold', 'PoweredDown', 'Release]	 *------------------------------------------------------------------------------------------------------*/	def getTasks( String process, String swimlane, String currentState ) {        if(processMap[process][swimlane,currentState]){            return processMap[process][swimlane,currentState].keySet()        } else {            return []        }	}	/* --------------------------------------------------------------------------------------	 * @author : lokanada Reddy	 * @param  : Process, Swimeline and currentState 	 * Used to get the list of valid tasks that are associated to a particular swimlane based	 * on the current state for the specified process flow.  This will include the additional	 * data (i.e. flags) associated with each state.	 * @return list of states that a task can be changed to	 *---------------------------------------------------------------------------------------*/	def getTasksExtended( String process, String swimlane, String currentState ) {        if(processMap[process][swimlane,currentState]){            return processMap[process][swimlane,currentState].keySet()        } else {            return []        }	}	/*------------------------------------------------------------------------------------------	 * @author : lokanada Reddy	 * @param  : Process, Swimeline , currentState and toState 	 * Used to get the flags associated with a given state	 * @assertEquals getFlags("STD_PROCESS","ROLE_WF_MANAGER", "Ready", "Release"), "skipped"	 * @return list of states that a task can be changed to	 *-----------------------------------------------------------------------------------------*/	def getFlags( String process, String swimlane, String currentState, String toState ) {		if(processMap[process][swimlane,currentState]){			if(processMap[process][swimlane,currentState][toState]){	            return processMap[process][swimlane,currentState][toState]["flag"]	        } else {	            return []	        }		} else {		    return []		}	}		/*--------------------------------------------------------------	 * @author : lokanada Reddy	 * @param  : Process, Swimeline , currentState and toState 	 * Used to verify the ROLE can change the currentState / toState	 * @return boolean flag	 *-------------------------------------------------------------*/	def canDoTask( String process, String swimlane, String currentState, String toState ) {		boolean flag = false		def stateType = getStateType( process, toState )		if(stateType == "boolean"){			flag = true		} else {			def fromStateMap = processMap[process][swimlane,currentState]			if( fromStateMap != null){				if(fromStateMap[toState] != null){					flag = true				}			}		}		return flag 	}	/*----------------------------------------------	 * @author : lokanada Reddy	 * @param  : Process, currentState	 * Used to get status for Corresponding statusId	 * @return the state value for id 	 *---------------------------------------------*/	def getState( String process, Integer currentState ) {		if(processMap[process]['transitions'][currentState.toString()]){			return processMap[process]['transitions'][currentState.toString()]['code']		} else {			return ""		}	}	/*----------------------------------------------	 * @author : lokanada Reddy	 * @param  : Process, currentState	 * Used to get status for Corresponding statusId	 * @return the state Label for id 	 *---------------------------------------------*/	def getStateLabel( String process, Integer currentState ) {		if(processMap[process]['transitions'][currentState.toString()]){			return processMap[process]['transitions'][currentState.toString()]['name']		} else {			return ""		}	}	/*----------------------------------------------	 * @author : lokanada Reddy	 * @param  : Process, "TASK_ID"	 * Used to get status for Corresponding statusId	 * @return list of all states	 *---------------------------------------------*/	def getTasks( String process , String task) {        def tasks = processMap[process][task]        def sourceWalkthru = getStateId( process, "SourceWalkthru" )        def targetWalkthru = getStateId( process, "TargetWalkthru" )        tasks.remove(sourceWalkthru)        tasks.remove(targetWalkthru)		return tasks	}	/*---------------------------------------------	 * @author : lokanada Reddy	 * @param  : Process, state	 * Used to get statusId for Corresponding status	 * @return the id value for state 	 *---------------------------------------------*/	def getStateId( String process, String state ) {		return processMap[process][state]	}	/*---------------------------------------------	 * @author : lokanada Reddy	 * @param  : Process, state	 * Used to get statusId for Corresponding status	 * @return the id value for state 	 *---------------------------------------------*/	def getStateType( String process, String state ) {		return processMap[process]["TASK_TYPE"][state]	}		/*-------------------------------------------------------	 * @author : lokanada Reddy	 * @param  : State and Process	 * @return : StateId as Integer 	 *-------------------------------------------------------*/	def getStateIdAsInt( String process, String state ) {		return Integer.parseInt( processMap[process][state] )	}		/*-------------------------------------------------------	 * @author : Mallikarjun	 * @return : Process Code 	 *-------------------------------------------------------*/	def getWorkflowCode () {		return Workflow.findAll()?.process	}			/*------------------------------------------------------------------------------------------	 * @author : Lokanada Reddy	 * @param  : Process, transitionId	 * Used to get the predecessor associated with a given transitionId	 * @assertEquals getPredecessor("STD_PROCESS",60),	 * @return predecessor that is associated with transitonId	 *-----------------------------------------------------------------------------------------*/	 	def getPredecessor( String process, Integer transitionId ) {				if(processMap[process]['PREDECESSOR'][transitionId.toString()]){			return processMap[process]['PREDECESSOR'][transitionId.toString()]		} else {			return false		}			}	/*------------------------------------------------------------------------------------------	 * @author : Lokanada Reddy	 * @param  : Process, transitionId	 * Used to get the DASHBOARD_LABEL associated with a given transitionId	 * @assertEquals getFlags("STD_PROCESS",60),	 * @return DASHBOARD_LABEL that is associated with transitonId	 *-----------------------------------------------------------------------------------------*/	 	def getDashboardLabel( String process, Integer transitionId ) {				if(processMap[process]['DASHBOARD_LABEL'][transitionId.toString()]){			return processMap[process]['DASHBOARD_LABEL'][transitionId.toString()]		} else { // if DASHBOARD_LABEL not exist return the name of the transition			return getStateLabel(process,transitionId)		}			}		/*------------------------------------------------------------------------------------------	 *  The process will be to get the entire list, then remove those steps referenced by the predecessor field. 	 *  For instance, Unracked has predecessor Unracking, so you would remove Unracking from the list. 	 *  @param  : Process	 *  @return : entire list except that have predecessor field.	 *---------------------------------------------------------------------------------------------*/	def getDashboardSteps( String process ){		// get the list of tasks for the selected process		def transitionsList = getTasks( process, "TASK_ID")		def dashboardSteps = []		// loop the list of tasks and assign them into list 		transitionsList.each{					def predecessor = getPredecessor( process, Integer.parseInt(it) )						def dashboardLabel = getDashboardLabel( process, Integer.parseInt( it ) )						// add dashboard labels into list			dashboardSteps << ["id":Integer.parseInt(it), "label":dashboardLabel, 'name':getStateLabel(process,Integer.parseInt( it ))]						// remove the predecessor from the list if exist			if( predecessor ){				dashboardSteps.remove(["id":predecessor, "label":getDashboardLabel( process, predecessor  ), 									   'name':getStateLabel(process,predecessor)])				}					}				log.debug("Dashboard Steps are --> ${dashboardSteps}")				return dashboardSteps	}	/*---------------------------------------------	 * Used to get step header color in PMO Asset Tracking	 * @author : Lokanada Reddy	 * @param  : Process, state	 * @return the header color value 	 *---------------------------------------------*/	 def getHeaderColor(String process, String state){		 def stateType = getStateType( process, state )		 def headerColor = processMap[process]["HEADER_COLOR"][state]		 if( !headerColor ){			 headerColor = stateType == "boolean" ? "#FF8000" : "#336600"		 }		 return headerColor	 }}