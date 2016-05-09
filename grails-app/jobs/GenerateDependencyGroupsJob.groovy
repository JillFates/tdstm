import org.quartz.JobExecutionContext
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.JobExecutionException
import com.tdsops.common.lang.ExceptionUtil
import org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin

class GenerateDependencyGroupsJob {

	// Quartz Properties
	def group = 'tdstm-dependency-groups'

	// def concurrent = false
	static triggers = { }

	// IOC services
	def moveBundleService
	def progressService

	/**
	 * Used to invoke an asset import REVIEW process that will review the assets before they can be posted to inventory 
	 * @param context
	 * @return void
	 */
	 void execute(context) {
	 	String errorMsg
	 	String progressKey
	 	Map results
	 	Long projectId, userLoginId

	 	try {
			def dataMap = context.mergedJobDataMap

			projectId = dataMap.getLongValue('projectId')
			progressKey = dataMap.getString('key')			
			def connectionTypes = dataMap.getString('connectionTypes')
			def statusTypes = dataMap.getString('statusTypes')
			def isChecked = dataMap.getString('isChecked')
			def userLoginName = dataMap.getString('userLoginName')

			log.debug "userLoginId = ${dataMap.userLoginId}"

			log.info "execute() projectId=$projectId, userLoginId=$userLoginId, progressKey=$progressKey, connectionTypes=$connectionTypes, statusTypes=$statusTypes, userLoginName=$userLoginName, isChecked=$isChecked"

			log.info "execute() is about to invoke moveBundleService.generateDependencyGroups to start processing batch for project $projectId"

			// Generate the Dependency Groups
			results = moveBundleService.generateDependencyGroups(projectId, connectionTypes, statusTypes, isChecked, userLoginName, progressKey)

			log.info "execute() return from moveBundleService.generateDependencyGroups() : results=$results"
		
		} catch (e) {
			log.error "execute() moveBundleService.generateDependencyGroups received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
			progressService.fail(progressKey, e.getMessage())
		} finally {
			//Fixing a problem when working with requestless Domain objects (http://burtbeckwith.com/blog/?p=73)
		  DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP.get().clear()
		}
	}
}
