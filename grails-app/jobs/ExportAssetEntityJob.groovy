import com.tdssrc.grails.GormUtil
import com.tdsops.common.lang.ExceptionUtil
import net.transitionmanager.service.AssetExportService
import net.transitionmanager.service.ProgressService

class ExportAssetEntityJob {

	// Quartz Properties
	def group = 'tdstm-export-asset'
	// def concurrent = false
	static triggers = { }

	// IOC services
	ProgressService progressService
	AssetExportService assetExportService

	/**
	 * executes the AssetEntityController.basicExport
	 * @param context
	 * @return void
	 */
	 def execute(context) {
	 	try {
			def dataMap = context.mergedJobDataMap
			assetExportService.export(dataMap)
		} catch (e) {
			log.error "execute() received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
			progressService.fail(progressKey, e.getMessage())
		} finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
