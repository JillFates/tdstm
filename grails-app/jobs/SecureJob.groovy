import org.quartz.JobExecutionContext
import org.springframework.util.Assert
import net.transitionmanager.service.SecurityService

/**
 * The SecureJob class is used to initialize the security context of a job to that of the user
 * specified in the Job Datamap property 'username'.
 */
public abstract class SecureJob {

	SecurityService securityService

	/**
	 * Used to initialize the security context
	 * @param context - the Job Execution Context that requires the datamap property 'username'
	 * @return the context mergedJobDataMap
	 */
	Map initialize(JobExecutionContext context) {
		Map dataMap = context.mergedJobDataMap
		String username = dataMap.getString('username')
		Assert.hasText(username, 'Secure jobs must have username specified in job data')

		// Load the User context to in order to execute the job
		securityService.assumeUserIdentity(username)

		return dataMap
	}
}
