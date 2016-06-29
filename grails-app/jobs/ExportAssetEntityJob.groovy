import com.tdssrc.grails.GormUtil
import com.tdsops.common.lang.ExceptionUtil

class ExportAssetEntityJob {

	// Quartz Properties
	def group = 'tdstm-export-asset'
	// def concurrent = false
	static triggers = { }

	// IOC services
	def assetEntityService
	def progressService

	/**
	 * executes the AssetEntityController.basicExport
	 * @param context
	 * @return void
	 */
	 def execute(context) {
	 	try {
			def dataMap = context.mergedJobDataMap
			assetEntityService.export(dataMap)
		} catch (e) {
			log.error "execute() received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"			
			progressService.fail(progressKey, e.getMessage())
		} finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
