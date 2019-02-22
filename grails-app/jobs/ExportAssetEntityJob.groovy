import com.tdsops.common.lang.ExceptionUtil
import net.transitionmanager.service.AssetExportService
import net.transitionmanager.service.ProgressService
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

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
	def execute(JobExecutionContext context) {
		JobDataMap dataMap = context.mergedJobDataMap
		try {
			assetExportService.export(dataMap)
		} catch (e) {
			log.error "execute() received exception ${e.getMessage()}\n${ExceptionUtil.stackTraceToString(e)}"
			progressService.fail(dataMap.key, e.getMessage())
		}
	}
}
