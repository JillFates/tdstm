/**
 * User: John Martin
 * Date: Apr 14, 2009
 * Time: 2:33:15 PM
 */


/*
 * The StateEngineService provides a state management engine driven by definitions in
 * XML files.  This service is used with workflow logic to help manage a stateful system.
 */
public class StateEngineService {

	static def processMap = []

	/*
	 * Constructor
	 * Loads all workflow process definitions from the resource directory into an internal
	 * map to be use by workflow processes afters.
	 */
	StateEngineService() {
		// Properly initialize the processMap variable and any additional vars

		// Search the resource directory for all files workflow_*.xml and call the loadProcessDefinition
		// method

	}

	/*
	 * Used to get the list of valid tasks that are associated to a particular swimlane based
	 * on the current state for the specified process flow.
	 * @return list of states that a task can be changed to
	 * @AssertEquals getTasks("STD_PROCESS","ROLE_WF_MANAGER", "Ready") , ['Hold', 'PoweredDown', 'Release]
	 */
	def getTasks( String process, String swimlane, String currentState ) {
	  // return processMap.process.swimlays[swimlane].currentState keys

	}

	/*
	 * Used to get the list of valid tasks that are associated to a particular swimlane based
	 * on the current state for the specified process flow.  This will include the additional
	 * data (i.e. flags) associated with each state.
	 *
	 * @return list of states that a task can be changed to
	 */
	def getTasksExtended( String process, String swimlane, String currentState ) {
	  // return processMap.process.swimlays[swimlane].currentState keys

	}

	/*
	 * Used to get the flags associated with a given state
	 * @assertEquals getFlags("STD_PROCESS","ROLE_WF_MANAGER", "Ready", "Release"), "skipped"
	 * @return list of states that a task can be changed to
	 */
	def getFlags( String process, String swimlane, String currentState, String toState ) {

	}

	/*
	 * Using XMLSluper to read XML Workflow Process definition and load into processMap
	 */
	def loadProcessDefinition( xmlText ) {

		def map = []

		// Iterate over the list of transitions and create a list of values
		// 	color
		//		ordinal position in the list
		def tlist= []
		// tlist[
		// 	'Hold' : [color: 'red', seq: 1],
		//		'Ready' : [color: 'yellow', seq: 2], ...
		map.put('transitions', tlist)

      // Iterate over the list of swimlanes and build a list putting into
		// map['swimlanes']
		def slist = []
		map.put('swimlanes', slist)
		

		// Iterate over all of the task-nodes and build list of task actions by swimlanes
		def taskTransitions = []
		// taskTranstions [
		// 	'Hold': [flag: transition.flag],
		//		'Release': [flag: transition.flag], ...
		// map.swimlanes.$swimlane = taskTransitions
		// map['ROLE_WF_SUPERVISOR'.'Ready'] = ['Hold'..., 'Release'..., ...]



		// All data about a process definition will be keyed off of the process-definition.code element
		processMap.put(process-definition.code, )
		// processMap['STD_PROCESS'] = []

	}

}