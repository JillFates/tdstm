import com.tdssrc.grails.GormUtil
import com.tdsops.common.lang.ExceptionUtil

class EmailDispatchJob {

	// Quartz Properties
	def group = 'tdstm-send-email'

	// def concurrent = false
	static triggers = { }

	// IOC services
	def emailDispatchService

	/**
	 * executes the AssetEntityController.basicExport
	 * @param context
	 * @return void
	 */
	 def execute(context) {
	 	try {
			def dataMap = context.mergedJobDataMap
			emailDispatchService.sendEmail(dataMap)
		} catch (e) {
			log.error "execute() received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"			
		} finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
