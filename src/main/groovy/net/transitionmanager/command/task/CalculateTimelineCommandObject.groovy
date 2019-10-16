package net.transitionmanager.command.task

class CalculateTimelineCommandObject extends ReadTimelineCommandObject {

	/**
	 * The mode parameter will be set to C)urrent or R)ecalculate where the latter uses the CPA.
	 */
	String mode = 'R'

	static constraints = {
		mode inList: ['R', 'C']
	}

	Boolean isRecalculate() {
		return mode == 'R'
	}

}
