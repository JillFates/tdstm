
class ExportAssetEntityJob {

	// Quartz Properties
	def group = 'tdstm-export-asset'
	// def concurrent = false
	static triggers = { }

	// IOC services
	def assetEntityService

	/**
	 * executes the AssetEntityController.basicExport
	 * @param context
	 * @return void
	 */
	 def execute(context) {
		def dataMap = context.mergedJobDataMap
		assetEntityService.export(dataMap)
	}
}
