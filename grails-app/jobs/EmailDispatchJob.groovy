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
		def dataMap = context.mergedJobDataMap
		emailDispatchService.sendEmail(dataMap)
	}
}
