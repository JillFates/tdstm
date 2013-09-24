/*
 * Quartz job to check maint mode periodically
 */
class CheckMaintModeJob {
	def maintService
	static triggers = {
		cron name: 'CheckMaintModeTrigger', cronExpression: "0/30 * * * * ?" 
	}
	/*
	 * Default method which will execute the trigger after every 30 second.
	 */
	def execute() {
		maintService.checkForMaintFile()
	}
}
