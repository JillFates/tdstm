package net.transitionmanager.agent

trait AgentTrait {

	abstract static List<DictionaryItem> dict = []

	// A commonly used set of Maps used for varius parameters and results
	private static Map taskIdParam() { [type:Integer, description: 'The task Id to reference the task'] }
	private static Map assetGuidParam() { [type:String, descrition: 'The globally unique reference id for an asset'] }

	private static Map statusResult() { [type:String, description: 'Status of process (success|error|failed|running)'] }
	private static Map causeResult() { [type:String, description: 'The cause of an error or failure'] }

	private static Map invokeResults() { [status: statusResult(), cause: causeResults() ] }
	private static Map notImplementedResults() { [status:'error', cause:'Method not implemented'] }

	/**
	 * Used to get a catalog of the methods that can be invoked along with the parameters and
	 * results map that the methods have
	 * @return List that describes the callable methods for an Agent class
	 */
	static List<DictionaryItem> dictionary() {
		return dict
	}

}