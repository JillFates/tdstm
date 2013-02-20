import org.apache.shiro.SecurityUtils
/*
 * Quartz job to runs snapshotService background process periodically 
 */
class DashboardProcessJob {
	
	def stepSnapshotService
	def concurrent = false
	/*
	 * Will schedule to wait for 'startDelay' time and after that will call the 'execute' method every 'repeatInterval' time. 
	 * The 'repeatInterval' and 'startDelay' properties are specified in milliseconds and must have Integer or Long type. 
	 * If these properties are not specified default values are applied (1 minute for 'repeatInterval' property and 30 seconds for 'startDelay' property).
	 */
	static triggers = {
	    simple name: 'snapshotProcessTrigger', startDelay: 120000, repeatInterval: 120000  
	}
	/*
	 * Default method which will execute the trigger after delay time and for every intervel.
	 */
    def execute() {
		 
		 stepSnapshotService.backgroundSnapshotProcess()
    
	 }
}
