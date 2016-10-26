import com.tdsops.common.lang.ExceptionUtil
import com.tdssrc.grails.GormUtil
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ProgressService
import org.quartz.JobExecutionContext

class ExportAssetEntityJob {

	def group = 'tdstm-export-asset'
	static triggers = {}

	AssetEntityService assetEntityService
	ProgressService progressService

	/**
	 * executes AssetEntityController.basicExport()
	 */
	void execute(JobExecutionContext context) {
		try {
			assetEntityService.export(context.mergedJobDataMap)
		}
		catch (e) {
			log.error "execute() received exception $e.message\n${ExceptionUtil.stackTraceToString(e)}"
//			progressService.fail(progressKey, e.getMessage())
		}
		finally {
			GormUtil.releaseLocalThreadMemory()
		}
	}
}
