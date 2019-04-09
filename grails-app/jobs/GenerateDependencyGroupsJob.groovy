import com.tdsops.common.lang.ExceptionUtil
import net.transitionmanager.project.MoveBundleService
import net.transitionmanager.common.ProgressService
import org.quartz.JobExecutionContext

class GenerateDependencyGroupsJob {

	static group = 'tdstm-dependency-groups'
	static triggers = {}

	MoveBundleService moveBundleService
	ProgressService progressService

	/**
	 * Invokes an asset import REVIEW process that will review the assets before
	 * they can be posted to inventory.
	 */
	void execute(JobExecutionContext context) {
		String progressKey
		Map results
		Long userLoginId

		try {
			def dataMap = context.mergedJobDataMap

			long projectId = dataMap.getLongValue('projectId')
			progressKey = dataMap.getString('key')
			String connectionTypes = dataMap.getString('connectionTypes')
			String statusTypes = dataMap.getString('statusTypes')
			String isChecked = dataMap.getString('isChecked')
			String userLoginName = dataMap.getString('userLoginName')

			log.debug "userLoginId = ${dataMap.userLoginId}"

			log.info "execute() projectId=$projectId, userLoginId=$userLoginId, progressKey=$progressKey, connectionTypes=$connectionTypes, statusTypes=$statusTypes, userLoginName=$userLoginName, isChecked=$isChecked"

			log.info "execute() is about to invoke moveBundleService.generateDependencyGroups to start processing batch for project $projectId"

			// Generate the Dependency Groups
			results = moveBundleService.generateDependencyGroups(projectId, connectionTypes, statusTypes,
					isChecked, userLoginName, progressKey)

			log.info "execute() return from moveBundleService.generateDependencyGroups() : results=$results"
		}
		catch (e) {
			log.error "execute() moveBundleService.generateDependencyGroups received exception $e.message\n${ExceptionUtil.stackTraceToString(e)}"
			progressService.fail(progressKey, e.message)
		}
	}
}
