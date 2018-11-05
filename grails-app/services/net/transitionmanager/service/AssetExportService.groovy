package net.transitionmanager.service

import com.tds.asset.*
import com.tdsops.common.lang.CollectionUtils
import com.tdsops.tm.asset.WorkbookSheetName
import com.tdsops.tm.asset.export.SpreadsheetColumnMapper
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.FilenameUtil
import com.tdsops.tm.enums.FilenameFormat
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WorkbookUtil
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.utils.Profiler
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.openxml4j.util.ZipSecureFile
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.*
import org.hibernate.transform.Transformers

@Slf4j
@Transactional
class AssetExportService {
	private static final ASSET_EXPORT_TEMPLATE_XLSX = "/templates/TDSMaster_template.xlsx"
	private static final ASSET_EXPORT_TEMPLATE_XLS = "/templates/TDSMaster_template.xls"
	private static final XLSX_EXTENSION = "xlsx"
	private static final XLS_EXTENSION = "xls"
	private static final MAX_XLS_RECORDS = 64000
	private static double MIN_INFLATE_RATIO = 0.0001d
	private static final String ALL_BUNDLES_OPTION = 'All'

	GrailsApplication grailsApplication
	SessionFactory sessionFactory
	ProgressService progressService
	ProjectService projectService
	PartyRelationshipService partyRelationshipService
	CustomDomainService customDomainService
	AssetOptionsService assetOptionsService

	/**
	 * This is the actual method that will generate the Excel export which it typically called by a Quartz
	 * job so that the user request can happen quickly. The job then will post progress updates to report back
	 * to the user referencing the params.key.
	 * The user's system timezone is critical to computing the datetimes in the spreadsheet since the application
	 * runs in GMT but Excel/Apache POI assume that the spreadsheet is in the system timezone. So if we generate a
	 * spreadsheet and stuff a
	 * @param Datatransferset,Project,Movebundle
	 *		params.key - the key to reference for progress status
	 *		params.projectId - the project for which to export assets
	 *		params.bundle - the bundle to export
	 *		params.dataTransferSet -
	 *		params.username - the user that made the request
	 *		params.tzId - the user's timezone (IMPORTANT that this be their system TZ and not their user preference*)
	 * 		params.userDTFormat - the user's Date Format (MIDDLE_ENDIAN, LITTLE_ENDIAN)
	 *		params.asset - flag to export devices
	 *		params.application - flag to export apps
	 *		params.database - flag to export databases
	 *		params.files - flag to export storage
	 *		params.dependency - flag to export dependencies
	 *		params.room - flag to export room details
	 *		params.rack - flag to export rack data
	 *		params.cable - flag to export cabling
	 *		params.comment - flag to export comments
	 **/
	void export(params) {
		def key = params.key
		def projectId = params.projectId

		boolean profilerEnabled = params[Profiler.KEY_NAME]==Profiler.KEY_NAME

		Profiler profiler = Profiler.create(profilerEnabled, key)

		final String mainProfTag = 'EXPORT'
		profiler.beginInfo(mainProfTag)

		try {
			def progressCount = 0
			def progressTotal = 0

			def dataTransferSet = params.dataTransferSet

			def principal = params.username
			UserLogin loginUser = UserLogin.findByUsername(principal)

			List<String> bundle = params.bundle

			// Set flag if Use for Planning was one of the chosen options and remove from the list
			boolean useForPlanning = bundle.remove(MoveBundle.USE_FOR_PLANNING)
			boolean allBundles = bundle.remove(ALL_BUNDLES_OPTION)
			int bundleSize = bundle.size()

			def dataTransferSetInstance = DataTransferSet.get( dataTransferSet )

			Project project = Project.get(projectId)
			if ( project == null) {
				progressService.update(key, 100, 'Cancelled', 'Project is required')
				return
			}

			// Maps for each class for mapping spreadsheet column names to the domain properties
			// it contains the field specs mapping by AssetClass
			List<Map<String, ?>> serverDTAMap, appDTAMap, dbDTAMap, fileDTAMap

			// Will hold the list of the assets for each of the classes
			List asset, application, database, files

			Sheet serverSheet, appSheet, dbSheet, storageSheet, titleSheet, dependencySheet, roomSheet
			def exportedEntity = ""

			// Flags to indicate which tabs to export based on which checkboxes selected in the UI
			boolean doDevice = params.asset=='asset'
			boolean doApp = params.application=='application'
			boolean doDB = params.database=='database'
			boolean doStorage = params.files=='files'
			boolean doDependency = params.dependency=='dependency'
			boolean doRoom = params.room=='room'
			boolean doRack = params.rack=='rack'
			boolean doCabling = params.cabling=='cable'
			boolean doComment = params.comment=='comment'

			// Queries for main assets
			String deviceQuery = null
			String applicationQuery = null
			String databaseQuery = null
			String filesQuery = null

			//
			// Load the asset lists and property map files based on ALL or selected bundles for the classes selected for export
			//
			//def session = sessionFactory.currentSession
			String query = ' WHERE d.project=:project '
			Map<String, ?> queryParams = [project:project]

			if (! allBundles && useForPlanning) {
				query += ' AND d.moveBundle.useForPlanning = TRUE '
			}

			List<MoveBundle> bundles = []
			String bundleNames = allBundles ? 'All Bundles' : 'Planning Bundles'
			// Setup for multiple bundle selection
			if (! allBundles && bundleSize) {
				List bundleIds = []
				for ( int i=0; i < bundleSize ; i++ ) {
					Long bid = NumberUtil.toPositiveLong(bundle[i], -1)
					if (bid > 0) {
						// TODO : Check if bundle is in the project
						bundleIds << bid
					}
				}
				String bundleStr = bundleIds.join(',')
				query += " AND d.moveBundle.id IN(${bundleStr})"
				//queryParams.bundles = bundles

				bundles = MoveBundle.where { project == project && id in bundleIds }.list()
				bundleNames = bundles.join(", ")
			}

			/*
				 The following size related variables are used to
				 update the progress meter.
			*/
			int assetSize = 0
			int appSize = 0
			int dbSize = 0
			int fileSize = 0
			int roomSize = 0
			int rackSize = 0
			int commentSize = 0
			int cablingSize = 0
			int dependencySize = 0

			// Workbook cell styles
			Map<Integer, CellStyle> workbookCellStyles = [:]

			def getAssetList = {q, qParams ->
				def hqlQuery = sessionFactory.currentSession.createQuery(q)
				hqlQuery.setReadOnly(true)
				//hqlQuery.setFetchSize(HIBERNATE_BATCH_SIZE)
				qParams.each{k, v ->
					hqlQuery.setParameter(k, v)
				}
				return hqlQuery.list()
			}

			def getAssetListScrollable = { Session  session, String q, Map qParams ->
				def hqlQuery = session.createQuery(q)
				hqlQuery.setReadOnly(true)
				qParams.each { k, v ->
					hqlQuery.setParameter(k, v)
				}
				return hqlQuery.scroll(ScrollMode.FORWARD_ONLY)
			}

			profiler.lap(mainProfTag, 'Initial loading')

			def countRows = { q, qParams ->
				def tmpQueryStr = "SELECT COUNT(*) " + q
				def countQuery = sessionFactory.currentSession.createQuery(tmpQueryStr)
				qParams.each{ k, v ->
					countQuery.setParameter(k,v)
				}
				return ((Long)countQuery.uniqueResult()).intValue()
			}

			if ( doDevice ) {
				deviceQuery = "FROM AssetEntity d " + query + " AND d.assetClass='${AssetClass.DEVICE}'"
				assetSize = countRows(deviceQuery, queryParams)
				progressTotal += assetSize
			}
			if ( doApp ) {
				applicationQuery = "FROM Application d" + query
				appSize = countRows(applicationQuery, queryParams)
				progressTotal += appSize
			}
			if ( doDB ) {
				databaseQuery = "FROM Database d" + query
				dbSize = countRows(databaseQuery, queryParams)
				progressTotal += dbSize
			}
			if ( doStorage ) {
				filesQuery = "FROM Files d" + query
				fileSize = countRows(filesQuery, queryParams)
				progressTotal += fileSize
			}

			if (doDependency) {
				dependencySize  = AssetDependency.executeQuery("SELECT COUNT(*) FROM AssetDependency WHERE asset.project = ? ",[project])[0]
				progressTotal += dependencySize
			}

			if (doRoom) {
				roomSize = Room.executeQuery("SELECT COUNT(*) FROM Room WHERE project = ?", [project])[0]
				progressTotal += roomSize
			}

			if (doRack) {
				rackSize = Rack.executeQuery("SELECT COUNT(*) FROM Rack WHERE project = ?", [project])[0]
				progressTotal += rackSize
			}

			if (doCabling) {
				cablingSize = AssetCableMap.executeQuery( "select count(*) from AssetCableMap acm where acm.assetFrom.project = ?", [project])[0]
				progressTotal += cablingSize
			}

			if (doComment) {
				commentSize = AssetComment.executeQuery("SELECT COUNT(*) FROM AssetComment WHERE project = ? AND commentType = 'comment' AND assetEntity IS NOT NULL", [project])[0]
				progressTotal += commentSize
			}

			profiler.lap(mainProfTag, 'Got row counts')

			// This variable is used to determine when to call the updateProgress
			float updateOnPercent = 0.01

			// Used by the profile sampling to determine # of rows to profile and how often within the dataset
			double percentToProfile = 25.0      // Sample 5% of all of data
			double frequencyToProfile = 5.0     // Sample the data every 5% of the way

			def tzId = params.tzId
			def userDTFormat = params.userDTFormat
			def currDate = TimeUtil.nowGMT()
			def exportDate = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, currDate, TimeUtil.FORMAT_DATE_TIME_5)

			profiler.lap(mainProfTag, 'Loaded DTAMaps')

			List assetDepBundleList = AssetDependencyBundle.executeQuery(
					  'select adb.asset.id, adb.dependencyBundle from AssetDependencyBundle adb where project=:project',
					  [project:project]
			)

			Map<Long, Integer> assetDepBundleMap = assetDepBundleList.collectEntries {
				[(it[0]): it[1]]
			}

			profiler.lap(mainProfTag, 'Created asset dep bundles')

			// Prevent! Zip bomb
			// Error: Zip bomb detected! The file would exceed the max. ratio of compressed file size to the size of the expanded data
			// Sets the ratio between de- and inflated bytes to detect zipbomb.
			ZipSecureFile.setMinInflateRatio(MIN_INFLATE_RATIO)

			Workbook initWorkbook
			if ( progressTotal < MAX_XLS_RECORDS ) {
				initWorkbook = getInitWorkbookInstance( ASSET_EXPORT_TEMPLATE_XLS )
			} else {
				initWorkbook = getInitWorkbookInstance( ASSET_EXPORT_TEMPLATE_XLSX )
			}

			// Save initialization workbook and use it as template for the streaming version of XSSFWorkbook
			Workbook workbook = WorkbookUtil.cloneWorkbookInstance(initWorkbook)

			profiler.lap(mainProfTag, 'Loaded workbook template')

			// Device
			serverSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.DEVICES)
			SpreadsheetColumnMapper serverMap = mapSheetColumnsToFields(AssetClass.DEVICE, serverSheet, project)
			List<CellStyle> serverStyles = WorkbookUtil.getHeaderStyles(workbookCellStyles, serverSheet)

			// Application
			appSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.APPLICATIONS)
			SpreadsheetColumnMapper appMap = mapSheetColumnsToFields(AssetClass.APPLICATION, appSheet, project)
			List<CellStyle> appStyles = WorkbookUtil.getHeaderStyles(workbookCellStyles, appSheet)

			// Database
			dbSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.DATABASES)
			SpreadsheetColumnMapper dbMap = mapSheetColumnsToFields(AssetClass.DATABASE, dbSheet, project)
			List<CellStyle> dbStyles = WorkbookUtil.getHeaderStyles(workbookCellStyles, dbSheet)

			// Storage
			storageSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.STORAGE)
			SpreadsheetColumnMapper fileMap = mapSheetColumnsToFields(AssetClass.STORAGE, storageSheet, project)
			List<CellStyle> storageStyles = WorkbookUtil.getHeaderStyles(workbookCellStyles, storageSheet)

			// Dependency
			dependencySheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.DEPENDENCIES)
			List<CellStyle> dependencyStyles = WorkbookUtil.getHeaderStyles(workbookCellStyles, dependencySheet)

			// Room
			roomSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.ROOM)
			List<CellStyle> roomStyles = WorkbookUtil.getHeaderStyles(workbookCellStyles, roomSheet)

			// Rack
			def rackSheetColumns = []
			def rackSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.RACK)
			List<CellStyle> rackStyles = WorkbookUtil.getHeaderStyles(workbookCellStyles, rackSheet)

			Row refRackRow = rackSheet.getRow(0)
			for ( int c = 0; c < refRackRow.getLastCellNum(); c++ ) {
				rackSheetColumns << refRackRow.getCell(c).getStringCellValue()
			}

			// Comments
			def commentSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.COMMENTS)
			List<CellStyle> commentStyles = WorkbookUtil.getHeaderStyles(workbookCellStyles, commentSheet)

			profiler.lap(mainProfTag, 'Read Spreadsheet Tabs')

			// Helper closure to create a text list from an array for debugging
			def xportList = { list ->
				def out = ''
				def x = 0
				list.each { out += "$x=$it\n"; x++}
				return out
			}

			// If there are standard headers not found in the workbook, it will return Error message
			def missingHeaders = []
			for (SpreadsheetColumnMapper spreadsheetColumnMapper in [serverMap, appMap, dbMap, fileMap]) {
				if (spreadsheetColumnMapper.hasMissingHeaders()) {
					missingHeaders.add("${spreadsheetColumnMapper.getSheetName()} sheet template is missing headers: ${spreadsheetColumnMapper.getMissingHeaders()}")
				}
			}
			if (CollectionUtils.isNotEmpty(missingHeaders)) {
				progressService.update(key, 100, 'Cancelled', missingHeaders.join("<br/>"))
				return
			}

			//Add Title Information to master SpreadSheet
			titleSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.TITLE)

			WorkbookUtil.addCell(titleSheet, 1, 2, project.client.toString())
			WorkbookUtil.addCell(titleSheet, 1, 3, projectId.toString())
			WorkbookUtil.addCell(titleSheet, 2, 3, project.name.toString())
			WorkbookUtil.addCell(titleSheet, 1, 4, partyRelationshipService.getProjectManagers(project))
			WorkbookUtil.addCell(titleSheet, 1, 6, loginUser.person.toString())
			WorkbookUtil.addCell(titleSheet, 1, 5, bundleNames)
			def exportedOn = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, new Date(), TimeUtil.FORMAT_DATE_TIME_22)
			WorkbookUtil.addCell(titleSheet, 1, 7, exportedOn)
			WorkbookUtil.addCell(titleSheet, 1, 8, tzId)
			WorkbookUtil.addCell(titleSheet, 1, 9, userDTFormat)

			WorkbookUtil.addCell(titleSheet, 30, 0, "Note: All times are in ${tzId ? tzId : 'EDT'} time zone")

			profiler.lap(mainProfTag, 'Updated title sheet')

			// update column header
			updateColumnHeaders(AssetClass.DEVICE, serverSheet, serverMap)
			updateColumnHeaders(AssetClass.APPLICATION, appSheet, appMap)
			updateColumnHeaders(AssetClass.DATABASE, dbSheet, dbMap)
			updateColumnHeaders(AssetClass.STORAGE, storageSheet, fileMap)

			profiler.lap(mainProfTag, 'Updating spreadsheet headers')

			def validationSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.VALIDATION)

			Map optionsSize = [:]

			def writeValidationColumn = { assetOptions, col, optionKey ->
				assetOptions.eachWithIndex{ option, idx ->
					WorkbookUtil.addCell(validationSheet, col, idx+1, "${option.value}")
				}
				optionsSize[optionKey] = assetOptions.size() + 1 // +1 because of the header.
			}

			def writeValidationSheet = {
				profiler.beginInfo "Validations"
				profiler.lap("Validations", "Starting to export Validation values")
				int col = 0
				writeValidationColumn(getAssetEnvironmentOptions(), col++, "Environment")
				writeValidationColumn(getAssetPriorityOptions(), col++, "Priority")
				writeValidationColumn(getAssetPlanStatusOptions(), col++, "PlanStatus")
				writeValidationColumn(getDependencyTypes(), col++, "DepType")
				writeValidationColumn(getDependencyStatuses(), col, "DepStatus")
				WorkbookUtil.makeSheetReadOnly(validationSheet)
				profiler.lap("Validations", "Finished exporting Validation values")
				profiler.endInfo("Validations", "Finished writing validation values to sheet.")
			}

			writeValidationSheet()

			def calcProfilerCriteria = { datasetSize ->
				int sampleQty = 1
				int sampleModulus = 1
				// int numOfSampleSets = (int)(1/frequencyToProfile)
				int numOfSampleSets = Math.round(100/frequencyToProfile)

				if (datasetSize > 0) {
					sampleQty = Math.round( (datasetSize * percentToProfile / 100) / numOfSampleSets)
					if (sampleQty == 0) {
						sampleQty = 1
					}

					// sampleModulus = (int)(datasetSize / numOfSampleSets)
					sampleModulus = Math.floor(datasetSize / numOfSampleSets)
					if (sampleModulus == 0) {
						sampleModulus = datasetSize
					}
				}

				return [sampleQty, sampleModulus]
			}

			// The threshold (milliseconds) to warning on when the processing time is exceeded for a row
			Map profileRowThresholds = [
					  (AssetClass.DEVICE): 30,
					  (AssetClass.APPLICATION): 30
			]

			// The maximum # of row level threshold violations to log
			final int profileThresholdLogLimit = 1000

			// Will log if setting a field exceeds this threadhold (msec)
			final int profileThresholdSettingField = 2

			final String thresholdWarnMsg = 'A total of %d row(s) exceeded the duration threshold of %d ms'

			// The following variables are used to control the profiling behavior
			//
			// Counter used to count the number of threshold violations
			int profileThresholdViolations
			int profileHighwaterMark = 0
			String profileHighwaterAsset = ''
			int profileSampleQty
			int profileSampleModulus
			int profileSamplingTics
			long lapDuration
			boolean profilingRow
			Map durationMatrix = [:]

			profiler.log(Profiler.LOG_TYPE.INFO, 'Initialization took (%s)', [profiler.getSinceStart(mainProfTag)])

			/***************************************************************************/

			// Refresh references to workbook sheets
			serverSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.DEVICES)
			appSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.APPLICATIONS)
			dbSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.DATABASES)
			storageSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.STORAGE)
			rackSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.RACK)

			/***************************************************************************/

			//
			// Device Export
			//
			if ( doDevice ) {
				Session session = sessionFactory.openSession()
				ScrollableResults scrollableResults = getAssetListScrollable(session, deviceQuery, queryParams)

				profiler.beginInfo "Devices"

				profiler.lap("Devices", "Devices Started")

				WorkbookUtil.addCellValidation(validationSheet, serverSheet, 0, 1, optionsSize["Environment"], serverMap.getColumnIndexByHeader("Environment"), 1, assetSize)
				WorkbookUtil.addCellValidation(validationSheet, serverSheet, 1, 1, optionsSize["Priority"], serverMap.getColumnIndexByHeader("Priority"), 1, assetSize)
				WorkbookUtil.addCellValidation(validationSheet, serverSheet, 2, 1, optionsSize["PlanStatus"], serverMap.getColumnIndexByHeader("PlanStatus"), 1, assetSize)

				profiler.lap("Devices", "Validations added")

				exportedEntity += 'S'
				int deviceCount = 0
				profileThresholdViolations = 0
				profilingRow = false

				String silentTag = 'DevRowSilent'

				if (profilerEnabled ) {
					(profileSampleQty, profileSampleModulus) = calcProfilerCriteria(assetSize)
				}

				profiler.lap('Devices', 'Entering while loop')

				List<Integer> autoResizeCols = ['Id'].collect { serverMap.getColumnIndexByHeader(it) }
				preSheetProcess(serverSheet, autoResizeCols)

				while (scrollableResults.next()) {
					AssetEntity currentAsset = (AssetEntity)scrollableResults.get()[0]
					profiler.beginSilent(silentTag)
					if (profilerEnabled) {
						if (deviceCount == 0 || (deviceCount % profileSampleModulus == 0)) {
							profilingRow = true
							profileSamplingTics = profileSampleQty
							profiler.lapReset('Devices')
						}

						if (profilingRow) {
							if (profileSamplingTics == 0) {
								profilingRow = false
							} else {
								profileSamplingTics--
								profiler.begin 'Device Row'
							}
						}
					}

					deviceCount++
					Row row = WorkbookUtil.getOrCreateRow(serverSheet, deviceCount)

					progressCount++

					if (profilingRow) {
						profiler.lap('Devices', 'Read device %d of %d, id=%d', [deviceCount, assetSize, currentAsset.id])
					}

					updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

					if (profilingRow) {
						profiler.lap('Devices', 'Update Progress')
						profiler.begin('Device Fields')
					}

					for (Map.Entry<String, ?> entry : serverMap.getColumnFieldMap()) {

						def colName = entry.key
						def field = entry.value["field"]
						def colNum = entry.value["order"] as int

						if (profilingRow) {
							lapDuration = profiler.getLapDuration('Devices').toMilliseconds()
							if (lapDuration > profileThresholdSettingField) {
								profiler.log(Profiler.LOG_TYPE.INFO, 'Set var %s (%s msec)', [colName, lapDuration.toString()])
							} else {
								profiler.lapReset('Devices')
							}
						}

						def colVal = ''
						switch(colName) {
							case 'Id':
								colVal = currentAsset.id
								break

							case 'DepGroup':
								colVal = assetDepBundleMap[currentAsset.id]
								break

							case ~/usize|SourcePos|TargetPos/:
								def pos = currentAsset[field] ?: 0
								// Don't bother populating position if it is a zero
								if (pos == 0) {
									continue
								}
								colVal = (Double)pos
								break

							case ~/Retire Date|Maint Expiration/:
								colVal = TimeUtil.formatDate(userDTFormat, currentAsset[field], TimeUtil.FORMAT_DATE)
								break

							case ~/Modified Date/:
								if (currentAsset[field]) {
									colVal = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, currentAsset[field], TimeUtil.FORMAT_DATE_TIME)
								}
								break

							case ~/Source Blade|Target Blade/:
								def chassis = currentAsset[field]
								def value = ""
								if (chassis) {
									value = "id:" + chassis.id + " " + chassis.assetName
								}
								colVal = value
								break
							case ~/Source Location|Target Location/:
								String fieldName = "room" + field.substring(8)
								Room room = currentAsset[fieldName]
								if (room) {
									colVal = room.location
								}
								break
							case ~/Source Room|Target Room/:
								String fieldName = "room" + field.substring(4)
								Room room = currentAsset[fieldName]
								if (room) {
									colVal = room.roomName
								}
								break
							case ~/Source Rack|Target Rack/:
								String fieldName = "rack" + field.substring(4)
								Rack rack = currentAsset[fieldName]
								if (rack) {
									colVal = rack.tag
								}
								break
							case ~/Tags/:
								Collection tagAssets = currentAsset[field]
								if (tagAssets) {
									colVal = tagAssets.tag*.name.join(",")
								} else {
                                    colVal = ""
                                }
								break

							default:
								colVal = currentAsset[field]
						}

						WorkbookUtil.addCellAndStyle(row, colNum, colVal, serverStyles)

						if (profilingRow) {
							lapDuration = profiler.getLapDuration('Devices').toMilliseconds()
							if (lapDuration > 2) {
								profiler.log(Profiler.LOG_TYPE.INFO, 'Set CELL %s (%s msec)', [colName, lapDuration.toString()])
							} else {
								profiler.lapReset('Devices')
							}
						}

					} // end columns loop

					if (profilingRow) {
						profiler.end('Device Fields')
						profiler.end('Device Row')
					}

					if (profilerEnabled) {
						// If the row duration exceeds the threshold we'll report it
						lapDuration = profiler.getLapDuration(silentTag).toMilliseconds()
						if (lapDuration > profileRowThresholds[(AssetClass.DEVICE)]) {
							profileThresholdViolations++
							profiler.log(Profiler.LOG_TYPE.WARN, "Asset '${currentAsset.assetName}' (id:${currentAsset.id}) exceeded duration threshold ($lapDuration msec)")
							if ( lapDuration > profileHighwaterMark ) {
								// Log each row that bumps up the highwater mark
								profileHighwaterMark = lapDuration
								profileHighwaterAsset = "${currentAsset.assetName} (id:${currentAsset.id})"
							}
						}

						// Compute the Duration Matrix to categorize all of the rows
						String durationGroup = "${((int)Math.floor(lapDuration/10))*10}"
						if (! durationMatrix.containsKey(durationGroup)) {
							durationMatrix[durationGroup] = [
									  count: 1,
									  duration: lapDuration,
									  average: lapDuration
							]
						} else {
							durationMatrix[durationGroup].count++
							durationMatrix[durationGroup].duration += lapDuration
							durationMatrix[durationGroup].average = (int)(durationMatrix[durationGroup].duration / durationMatrix[durationGroup].count)
						}
					}

					profiler.endSilent(silentTag)

					session.evict(currentAsset)
				} // asset.each

				session.close()
				postSheetProcess(serverSheet, autoResizeCols)

				profiler.endInfo("Devices", "processed %d rows", [assetSize])
				if (profileThresholdViolations > 0) {
					profiler.log(Profiler.LOG_TYPE.WARN, thresholdWarnMsg, [profileThresholdViolations, profileRowThresholds[(AssetClass.DEVICE)]])
				}

				if (profilerEnabled) {
					if (profileHighwaterAsset) {
						profiler.log(Profiler.LOG_TYPE.WARN, "Asset $profileHighwaterAsset set the highwater mark ($profileHighwaterMark msec)")
					}

					int durSum = durationMatrix.values().sum { it.duration }
					if (durSum > 0) {
						durationMatrix.each {k,v ->
							durationMatrix[k].perc = Math.round(v.duration/durSum*100)
						}
					}
					profiler.log(Profiler.LOG_TYPE.INFO, "Duration Matrix: $durationMatrix")
				}
			} else {
				// Add validations for the first row (which is blank)
				WorkbookUtil.addCellValidation(validationSheet, serverSheet, 0, 1, optionsSize["Environment"], serverMap.getColumnIndexByHeader("Environment"), 1, 1)
				WorkbookUtil.addCellValidation(validationSheet, serverSheet, 1, 1, optionsSize["Priority"], serverMap.getColumnIndexByHeader("Priority"), 1, 1)
				WorkbookUtil.addCellValidation(validationSheet, serverSheet, 2, 1, optionsSize["PlanStatus"], serverMap.getColumnIndexByHeader("PlanStatus"), 1, 1)
			}

			//
			// Application Export
			//
			if ( doApp ) {
				Session session = sessionFactory.openSession()
				ScrollableResults scrollableResults = getAssetListScrollable(session, applicationQuery, queryParams)

				profiler.beginInfo "Applications"

				profiler.lap("Applications", "Adding Validations")

				WorkbookUtil.addCellValidation(validationSheet, appSheet, 0, 1, optionsSize["Environment"], appMap.getColumnIndexByHeader("Environment"), 1, appSize)
				WorkbookUtil.addCellValidation(validationSheet, appSheet, 2, 1, optionsSize["PlanStatus"], appMap.getColumnIndexByHeader("PlanStatus"), 1, appSize)

				profiler.lap("Applications", "Validations added.")

				exportedEntity += 'A'

				// This determines which columns are added as Number vs String
				def numericCols = ['Id']
				def stringCols = ['Version']

				List<Integer> autoResizeCols = ['Id'].collect { serverMap.getColumnIndexByHeader(it) }
				preSheetProcess(appSheet, autoResizeCols)

				int applicationCount = 1 // Header Offset
				while (scrollableResults.next()) {
					Application app = (Application)scrollableResults.get()[0]
					progressCount++

					updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

					Row row = WorkbookUtil.getOrCreateRow(appSheet, applicationCount)

					for (Map.Entry<String, ?> entry : appMap.getColumnFieldMap()) {
						def colName = entry.key
						def field = entry.value["field"]
						def colNum = entry.value["order"] as int

						def colVal = ''
						switch(field) {
							case 'Id':
								colVal = app.id
								break
							case 'DepGroup':
								// Find the Dependency Group that this app is bound to
								colVal = assetDepBundleMap[app.id]
								break
							case ~/shutdownBy|startupBy|testingBy/:
								colVal = app[field] ? resolveByName(app[field], false)?.toString() : ''
								break
							case ~/shutdownFixed|startupFixed|testingFixed/:
								colVal = app[field] ? 'Yes' : 'No'
								break
							case ~/retireDate|maintExpDate/:
								colVal = app[field] ? TimeUtil.formatDate(userDTFormat, app[field], TimeUtil.FORMAT_DATE) : ''
								break
							case ~/lastUpdated/:
								colVal = app[field] ? TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, app[field], TimeUtil.FORMAT_DATE_TIME) : ''
								break
							case ~/tagAssets/:
								Collection tagAssets = app[field]
								if (tagAssets) {
									colVal = tagAssets.tag*.name.join(",")
								} else {
									colVal = ""
								}
								break
							default:
								colVal = app[field]
						}

						WorkbookUtil.addCellAndStyle(row, colNum, colVal, appStyles)

					} // end columns loop

					session.evict(app)
					applicationCount++
				} // application.each

				session.close()

				postSheetProcess(appSheet, autoResizeCols)

				profiler.endInfo("Applications", "processed %d rows", [appSize])
			} else {
				// Add validation for the first row
				WorkbookUtil.addCellValidation(validationSheet, appSheet, 0, 1, optionsSize["Environment"], appMap.getColumnIndexByHeader("Environment"), 1, 1)
				WorkbookUtil.addCellValidation(validationSheet, appSheet, 2, 1, optionsSize["PlanStatus"], appMap.getColumnIndexByHeader("PlanStatus"), 1, 1)
			}

			//
			// Database
			//
			if ( doDB ) {
				Session session = sessionFactory.openSession()
				ScrollableResults scrollableResults = getAssetListScrollable(session, databaseQuery, queryParams)

				profiler.beginInfo "Databases"

				profiler.lap("Databases", "Adding Validations")

				WorkbookUtil.addCellValidation(validationSheet, dbSheet, 0, 1, optionsSize["Environment"], dbMap.getColumnIndexByHeader("Environment"), 1, dbSize)
				WorkbookUtil.addCellValidation(validationSheet, dbSheet, 2, 1, optionsSize["PlanStatus"], dbMap.getColumnIndexByHeader("PlanStatus"), 1, dbSize)

				profiler.lap("Databases", "Validations added.")

				List<Integer> autoResizeCols = ['Id'].collect { serverMap.getColumnIndexByHeader(it) }
				preSheetProcess(dbSheet, autoResizeCols)

				exportedEntity += "D"
				int databaseCount = 1 // Header Offset

				while (scrollableResults.next()) {
					Database currentDatabase = (Database)scrollableResults.get()[0]
					progressCount++

					Row row = WorkbookUtil.getOrCreateRow(dbSheet, databaseCount)

					updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

					for (Map.Entry<String, ?> entry : dbMap.getColumnFieldMap()) {
						def colName = entry.key
						def field = entry.value["field"]
						def colNum = entry.value["order"] as int

						def colVal

						switch (colName) {
							case 'Id':
								colVal = currentDatabase.id
								break

							case 'DepGroup':
								colVal = assetDepBundleMap[currentDatabase.id]
								break

							case ~/Tags/:
								Collection tagAssets = currentDatabase[field]
								if (tagAssets) {
									colVal = tagAssets.tag*.name.join(",")
								} else {
									colVal = ""
								}
								break

							default:
								colVal = currentDatabase[field]
								if (colVal && (field in ['retireDate', 'maintExpDate', 'lastUpdated'])) {
									if (field == 'lastUpdated') {
										colVal = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, colVal, TimeUtil.FORMAT_DATE_TIME)
									} else {
										colVal = TimeUtil.formatDate(userDTFormat, colVal, TimeUtil.FORMAT_DATE)
									}
								}
						}

						WorkbookUtil.addCellAndStyle(row, colNum, colVal, dbStyles)
					} // end columns loop

					session.evict(currentDatabase)
					databaseCount++
				}

				session.close()
				postSheetProcess(dbSheet, autoResizeCols)

				profiler.endInfo("Databases", "processed %d rows", [dbSize])
			} else {
				// Adds validation to just the first row
				WorkbookUtil.addCellValidation(validationSheet, dbSheet, 0, 1, optionsSize["Environment"], dbMap.getColumnIndexByHeader("Environment"), 1, 1)
				WorkbookUtil.addCellValidation(validationSheet, dbSheet, 2, 1, optionsSize["PlanStatus"], dbMap.getColumnIndexByHeader("PlanStatus"), 1, 1)
			}

			//
			// Storage ( files )
			//
			if ( doStorage ) {
				Session session = sessionFactory.openSession()
				ScrollableResults scrollableResults = getAssetListScrollable(session, filesQuery, queryParams)

				profiler.beginInfo "Logical Storage"

				profiler.lap("Logical Storage", "Adding Validations")

				WorkbookUtil.addCellValidation(validationSheet, storageSheet, 0, 1, optionsSize["Environment"], fileMap.getColumnIndexByHeader("Environment"), 1, fileSize)
				WorkbookUtil.addCellValidation(validationSheet, storageSheet, 2, 1, optionsSize["PlanStatus"], fileMap.getColumnIndexByHeader("PlanStatus"), 1, fileSize)

				profiler.lap("Logical Storage", "Validations added.")

				List<Integer> autoResizeCols = ['Id'].collect { serverMap.getColumnIndexByHeader(it) }
				preSheetProcess(storageSheet, autoResizeCols)

				exportedEntity += "F"

				int filesCount = 1
				while (scrollableResults.next()) {
					Files currentFile = (Files)scrollableResults.get()[0]
					progressCount++
					Row row = WorkbookUtil.getOrCreateRow(storageSheet, filesCount)

					updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

					for (Map.Entry<String, ?> entry : fileMap.getColumnFieldMap()) {
						def colName = entry.key
						def field = entry.value["field"]
						def colNum = entry.value["order"] as int

						def colVal

						switch (colName) {
							case 'Id':
								colVal = currentFile.id
								break

							case 'DepGroup':
								colVal = assetDepBundleMap[currentFile.id]
								break

							case ~/Tags/:
								Collection tagAssets = currentFile[field]
								if (tagAssets) {
									colVal = tagAssets.tag*.name.join(",")
								} else {
									colVal = ""
								}
								break


							default:
								colVal = currentFile[field]
								if (colVal && (field in ['retireDate', 'maintExpDate', 'lastUpdated'])) {
									if (field == 'lastUpdated') {
										colVal = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, colVal, TimeUtil.FORMAT_DATE_TIME)
									} else {
										colVal = TimeUtil.formatDate(userDTFormat, colVal, TimeUtil.FORMAT_DATE)
									}
								}
						}

						WorkbookUtil.addCellAndStyle(row, colNum, colVal, storageStyles)

					} // end columns loop

					session.evict(currentFile)
					filesCount++
				} // files.each

				session.close()
				postSheetProcess(storageSheet, autoResizeCols)

				profiler.endInfo("Logical Storage", "processed %d rows", [fileSize])
			} else {
				// Adds validations to the first row
				WorkbookUtil.addCellValidation(validationSheet, storageSheet, 0, 1, optionsSize["Environment"], fileMap.getColumnIndexByHeader("Environment"), 1, 1)
				WorkbookUtil.addCellValidation(validationSheet, storageSheet, 2, 1, optionsSize["PlanStatus"], fileMap.getColumnIndexByHeader("PlanStatus"), 1, 1)
			}

			//
			// Dependencies
			//
			dependencySheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.DEPENDENCIES)
			List depProjectionFields = [
					  'id',
					  'asset.id',
					  'a.assetName',
					  'a.assetType',
					  'd.id',
					  'd.assetName',
					  'd.assetType',
					  'type',
					  'dataFlowFreq',
					  'dataFlowDirection',
					  'status',
					  'comment',
					  'c1',
					  'c2',
					  'c3',
					  'c4'
			]

			if ( doDependency ) {
				profiler.beginInfo "Dependencies"
				exportedEntity += "X"

				profiler.lap("Dependencies", "Adding Validations")
				WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 3, 1, optionsSize["DepType"],  depProjectionFields.indexOf("type"), 1, dependencySize)
				WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 4, 1, optionsSize["DepStatus"],  depProjectionFields.indexOf("status"), 1, dependencySize)
				profiler.lap("Dependencies", "Validations added.")

				List<Integer> autoResizeCols = [0,1]
				preSheetProcess(dependencySheet, autoResizeCols)

				List results = AssetDependency.createCriteria().list {
					createAlias("asset", "a")
					createAlias("dependent", "d")
					and {
						eq("a.project", project)
					}
					projections {
						depProjectionFields.each {
							property(it)
						}
					}

					readOnly true
				}

				int r = 1  //Header Offset
				for (Object dependency : results) {
					progressCount++
					Row row = WorkbookUtil.getOrCreateRow(dependencySheet, r)

					updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

					for(int i = 0; i < depProjectionFields.size(); i++){
						def prop = dependency[i]
						if ( !(prop == null || ( (prop instanceof String) && prop.size() == 0 )) ) {
							WorkbookUtil.addCellAndStyle(row, i, prop, dependencyStyles)
						}

					}
					r++
				}

				postSheetProcess(dependencySheet, autoResizeCols)
				profiler.endInfo("Dependencies", "processed %d rows", [dependencySize])
			} else {
				// Adds validations to the first dependency row
				WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 3, 1, optionsSize["DepType"],  depProjectionFields.indexOf("type"), 1, 1)
				WorkbookUtil.addCellValidation(validationSheet, dependencySheet, 4, 1, optionsSize["DepStatus"],  depProjectionFields.indexOf("status"), 1, 1)
			}

			flushAndClearSession()

			//
			// Room Export
			//
			if( doRoom ) {
				profiler.beginInfo "Rooms"

				exportedEntity += "R"

				roomSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.ROOM)

				List projectionFields = [
						  "id",
						  "roomName",
						  "location",
						  "roomDepth",
						  "roomWidth",
						  "source",
						  "address",
						  "city",
						  "country",
						  "stateProv",
						  "postalCode",
						  "dateCreated",
						  "lastUpdated"
				]

				// These fields can be exported as they are. 5 is intentionally omitted.
				List regularFields = [0, 1, 2, 3, 4, 6, 7, 8, 9, 10]
				// These fields are dates and need to be converted to the user's TZ.
				List dateFields = [11, 12]


				List results = Room.createCriteria().list {
					and {
						eq("project", project)
					}

					projections {
						projectionFields.each {
							property(it)
						}
					}

					readOnly true
				}

				List<Integer> autoResizeCols = [0]
				preSheetProcess(roomSheet, autoResizeCols)

				int r = 1
				for (Object room : results) {
					progressCount++
					Row row = WorkbookUtil.getOrCreateRow(roomSheet, r)

					updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)
					// Export most fields.
					regularFields.each { col ->
						def prop = room[col]
						if ( !(prop == null || ( (prop instanceof String) && prop.size() == 0 )) ) {
							WorkbookUtil.addCellAndStyle(row, col, prop, roomStyles)
						}

					}
					// Export date fields using the user's TZ.
					dateFields.each{ col->
						def prop = room[col] ? TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, room[col], TimeUtil.FORMAT_DATE_TIME) : ""
						WorkbookUtil.addCellAndStyle(row, col, prop, roomStyles)
					}
					// Export 'source' or 'target' accordingly.
					WorkbookUtil.addCellAndStyle(row, 5, String.valueOf(room[5] == 1 ? "Source" : "Target" ), roomStyles)
					r++
				}

				postSheetProcess(roomSheet, autoResizeCols)
				profiler.endInfo("Rooms", "processed %d rows", [roomSize])
			}

			flushAndClearSession()

			//
			// Rack Export
			//
			if ( doRack ) {
				profiler.beginInfo "Racks"
				exportedEntity +="r"

				def racks = Rack.createCriteria().list {
					and {
						eq("project", project)
					}
					resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
					readOnly true
				}

				def rackMap = ['rackId':'id', 'Tag':'tag', 'Location':'location', 'Room':'room', 'RoomX':'roomX',
				               'RoomY':'roomY', 'PowerA':'powerA', 'PowerB':'powerB', 'PowerC':'powerC', 'Type':'rackType',
				               'Front':'front', 'Model':'model', 'Source':'source', 'Model':'model'
				]

				List<Integer> autoResizeCols = [0, 1]
				preSheetProcess(rackSheet, autoResizeCols)

				int idx = 1 //Header Offset
				for (Object rack : racks) {
					Row row = WorkbookUtil.getOrCreateRow(rackSheet, idx)
					def currentRack = rack.get(Criteria.ROOT_ALIAS)
					progressCount++
					updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)

					rackSheetColumns.eachWithIndex{column, i->
						if(column == 'rackId'){
							WorkbookUtil.addCellAndStyle(row, 0, currentRack.id, rackStyles)
						} else {
							def prop = currentRack[rackMap[column]]
							if(column =="Source") {
								prop = (prop == 1) ? 'Source' : 'Target'
							}

							WorkbookUtil.addCellAndStyle(row, i, prop, rackStyles)
						}
					}
					idx++
				}

				postSheetProcess(rackSheet, autoResizeCols)
				profiler.endInfo("Racks", "processed %d rows", [rackSize])
			}

			//}

			flushAndClearSession()

			//
			// Cabling Export
			//
			if ( doCabling ) {
				profiler.beginInfo "Cabling"

				exportedEntity += "c"

				if (cablingSize > 0) {
					def cablingSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.CABLING)
					def cablingList = AssetCableMap.createCriteria().list {
						createAlias("assetFrom", "af")
						and {
							eq("af.project", project)
						}
						resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
					}
					cablingReportData(cablingList, cablingSheet, workbookCellStyles)
					progressCount += cablingSize
					cablingList = null
				}
				profiler.endInfo("Cabling", "processed %d rows", [cablingSize])
			}

			flushAndClearSession()

			//
			// Comments Export
			//
			if ( doComment ) {
				profiler.beginInfo "Comments"

				exportedEntity += "M"

				if (commentSize > 0) {
					commentSheet = WorkbookUtil.getSheetFromWorkbook(workbook, WorkbookSheetName.COMMENTS)

					def c = AssetComment.createCriteria()
					List commentList = c {
						and {
							eq('project', project)
							eq('commentType', 'comment')
							isNotNull('assetEntity')

						}
						resultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
						setReadOnly true
					}

					List<Integer> autoResizeCols = [0, 1]
					preSheetProcess(commentSheet, autoResizeCols)

					int idx = 1 //HEADER Offset
					for (Object comm : commentList) {
						Row row = WorkbookUtil.getOrCreateRow(commentSheet, idx)

						def currentComment = comm.get(Criteria.ROOT_ALIAS)
						progressCount++
						updateProgress(key, progressCount, progressTotal, 'In progress', updateOnPercent)
						def dateCommentCreated = ''
						if (currentComment.dateCreated) {
							dateCommentCreated = TimeUtil.formatDateTimeWithTZ(tzId, userDTFormat, currentComment.dateCreated, TimeUtil.FORMAT_DATE_TIME)
						}
						WorkbookUtil.addCellAndStyle(row, 0, currentComment.id, commentStyles)
						WorkbookUtil.addCellAndStyle(row, 1, currentComment.assetEntity.id, commentStyles)
						WorkbookUtil.addCellAndStyle(row, 2, currentComment.category, commentStyles)
						WorkbookUtil.addCellAndStyle(row, 3, dateCommentCreated, commentStyles)
						WorkbookUtil.addCellAndStyle(row, 4, currentComment.createdBy, commentStyles)
						WorkbookUtil.addCellAndStyle(row, 5, currentComment.comment, commentStyles)
						idx++
					}

					postSheetProcess(commentSheet, autoResizeCols)
				}
				profiler.endInfo("Comments", "processed %d rows", [commentSize])
			}

			flushAndClearSession()

			//
			// Wrap up the process
			//
			profiler.lap(mainProfTag, 'All sheets populated')

			profiler.beginInfo("Set Force Formula Recalculation")
			workbook.setForceFormulaRecalculation(true)
			profiler.endInfo("Set Force Formula Recalculation")

			profiler.beginInfo("Create temporary export file")
			String savedWorkbookAbosolutePath = WorkbookUtil.saveWorkbook(workbook)

			String fileExtension = XLSX_EXTENSION
			if ( workbook instanceof HSSFWorkbook ) {
				fileExtension = XLS_EXTENSION
			}

			progressService.updateData(key, 'filename', savedWorkbookAbosolutePath)
			profiler.endInfo("Create temporary export file")

       // @See TM-7958
       // The filename will consist of the following:
       //    - Project Client
       //    - Project Code
       //    - Bundle name(s) selected or ALL_BUNDLES
       //    - Letters symbolizing each of the tabs that were exported
       //    - The date that the spreadsheet was exported in yyyyMMdd_HHmm format
       def nameParams = [project:project,
                         moveBundle:bundles,
                         exportedEntities: exportedEntity,
                         allBundles: allBundles,
                         useForPlanning: useForPlanning]
       String filename = FilenameUtil.buildFilename(FilenameFormat.CLIENT_PROJECT_BUNDLE_CHECKBOXCODES_DATE, nameParams, fileExtension)

			filename = StringUtil.sanitizeAndStripSpaces(filename)

			def contentType = grailsApplication.config.grails.mime.types[fileExtension]
			progressService.updateData(key, 'contenttype', contentType)
			progressService.updateData(key, 'header', 'attachment; filename="' + filename + '"')
			progressService.update(key, 100, 'Completed')

			profiler.lap(mainProfTag, "Streamed spreadsheet to browser")
		} catch( Exception exportExp ) {
			log.error("Error: ${exportExp.message}", exportExp)
			progressService.update(key, 100, 'Cancelled', "An unexpected exception occurred while exporting to Excel")

			return
		} finally {
			profiler.endInfo(mainProfTag, "FINISHED")

		}
	}

	/**
	 * Perform some pre-process on the sheet, like the track of a column to resize it
	 * @param sheet
	 * @param autoResizeColList
	 */
	private void preSheetProcess(Sheet sheet, List<Integer> autoResizeColList) {
		if(sheet instanceof SXSSFSheet){
			// Autoresize columns
			for( Integer colIdx in autoResizeColList ) {
				((SXSSFSheet)sheet).trackColumnForAutoSizing(colIdx)
			}
		}
	}

	/**
	 * Perform some post-process on the sheet, like autorisizing columns
	 * @param sheet
	 * @param autoResizeColList
	 */
	private void postSheetProcess(Sheet sheet, List<Integer> autoResizeColList) {
		for( Integer colIdx in autoResizeColList ) {
			int oldColSize = sheet.getColumnWidth(colIdx)
			sheet.autoSizeColumn(colIdx)
			int newColSize = sheet.getColumnWidth(colIdx)
			if(newColSize < oldColSize) {
				sheet.setColumnWidth(colIdx, oldColSize)
			}
		}
	}

	/**
	 * This method is used to update sheet's column header with custom labels
	 * @param assetClass
	 * @param sheet
	 * @param spreadsheetColumnMapper
	 * @return
	 */
	private Sheet updateColumnHeaders(AssetClass assetClass, Sheet sheet, SpreadsheetColumnMapper spreadsheetColumnMapper) {
		log.info('Updating sheet columns headers for: {}', assetClass)
		for (map in spreadsheetColumnMapper.getColumnFieldMap()) {
			Map fieldSpec = map.value
			if (fieldSpec.udf == CustomDomainService.CUSTOM_USER_FIELD) {
				WorkbookUtil.addCell(sheet, fieldSpec.order, 0, fieldSpec.label)
			}
		}

		return sheet
	}

	/**
	 * Get field specs for a given AssetClass and current user project
	 * @param assetClass
	 * @return
	 */
	private List<Map<String, ?>> getFieldSpecsForAssetClass(AssetClass assetClass, Project project) {
		Map fieldSpecs = customDomainService.getFieldSpecsForAssetExport(project, assetClass.toString())
		return fieldSpecs[assetClass.toString()]["fields"]
	}

	/**
	 * Map and match sheet columns header names to field specs
	 * @param assetClass
	 * @param sheet
	 * @return
	 */
	private SpreadsheetColumnMapper mapSheetColumnsToFields(AssetClass assetClass, Sheet sheet, Project project) {
		List<String> templateHaders = WorkbookUtil.getSheetHeadersAsList(sheet)
		List<Map<String, ?>> fieldSpecs = getFieldSpecsForAssetClass(assetClass, project)

		SpreadsheetColumnMapper spreadsheetColumnMapper = new SpreadsheetColumnMapper(sheet.getSheetName(), templateHaders, fieldSpecs)
		return spreadsheetColumnMapper
	}

	/**
	 * Used to get the list of Type used to assign to AssetDependency.type
	 * @return List of the types
	 */
	List<String> getDependencyTypes() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
	}

	/**
	 * Used to get the list of Status used to assign to AssetDependency.status
	 * @return List of the types
	 */
	List<String> getDependencyStatuses() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
	}

	/**
	 * Use to get the list of Asset Environment options
	 * @return List of options
	 */
	List<String> getAssetEnvironmentOptions() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
	}

	/**
	 * Use to get the list of Asset Environment options
	 * @return List of options
	 */
	List<String> getAssetPlanStatusOptions() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
	}

	/**
	 * Use to get the list of Priority Options
	 * @return List of Priority values
	 */
	List<String> getAssetPriorityOptions() {
		return assetOptionsService.findAllValuesByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)
	}

	/**
	 * Updates progress for the give key only when is around 5%
	 * @param key to update
	 * @param progressCount progress count
	 * @param progressTotal total number of items
	 * @param description description used on update
	 * @param updateOnPercent used to determine the step size for updating the progress bar.
	 **/
	private void updateProgress(key, progressCount, progressTotal, description, updateOnPercent = 0.05) {
		int stepSize = Math.round(progressTotal * updateOnPercent)
		if ((progressTotal > 0) && (stepSize > 0) && ((progressCount % stepSize) == 0)) {
			progressService.update(key, ((int)((progressCount / progressTotal) * 100)), description)
		}
	}

	/**
	 * export cabling data.
	 * @param assetCablesList
	 * @param cablingSheet
	 */
	private void cablingReportData(List assetCablesList, Sheet cablingSheet, Map<Integer, CellStyle> workbookCellStyles) {
		List<Integer> autoResizeCols = [1, 4]
		preSheetProcess(cablingSheet, autoResizeCols)

		int rowNum = 2
		for (Object element : assetCablesList) {
			Row row = WorkbookUtil.getOrCreateRow(cablingSheet, rowNum)

			def cabling = element["this"] // <SL> element comes as a map with (this, af) keys, so "this" is the one that has all needed information
			WorkbookUtil.addCell(row, 0, String.valueOf(cabling.assetFromPort?.type))
			WorkbookUtil.addCell(row, 1, cabling.assetFrom ? cabling.assetFrom?.id : "", Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
			WorkbookUtil.addCell(row, 2, String.valueOf(cabling.assetFrom ? cabling.assetFrom.assetName : ""))
			WorkbookUtil.addCell(row, 3, String.valueOf(cabling.assetFromPort?.label))
			WorkbookUtil.addCell(row, 4, cabling.assetTo ? cabling.assetTo?.id : "", Cell.CELL_TYPE_NUMERIC, workbookCellStyles)
			WorkbookUtil.addCell(row, 5, String.valueOf(cabling.assetTo ? cabling.assetTo?.assetName : ""))
			if (cabling.assetFromPort && cabling.assetFromPort.type && cabling.assetFromPort.type != 'Power') {
				WorkbookUtil.addCell(row, 6, String.valueOf(cabling.assetToPort ? cabling.assetToPort?.label : ""))
			} else {
				WorkbookUtil.addCell(row, 6, String.valueOf(cabling.toPower ?: ""))
			}
			WorkbookUtil.addCell(row, 7, String.valueOf(cabling.cableComment ?: ""))
			WorkbookUtil.addCell(row, 8, String.valueOf(cabling.cableColor ?: ""))
			if (cabling.assetFrom?.getSourceRoomName()) {
				WorkbookUtil.addCell(row, 9, String.valueOf(cabling.assetFrom?.rackSource?.location + "/" + cabling.assetFrom?.getSourceRoomName() + "/" + cabling.assetFrom?.getSourceRackName()))
			} else if (cabling.assetFrom?.getTargetRoomName()) {
				WorkbookUtil.addCell(row, 9, String.valueOf(cabling.assetFrom?.rackTarget?.location + "/" + cabling.assetFrom?.getTargetRoomName() + "/" + cabling.assetFrom?.getTargetRackName()))
			} else {
				WorkbookUtil.addCell(row, 9, '')
			}
			WorkbookUtil.addCell(row, 10, String.valueOf(cabling.cableStatus ?: ""))
			WorkbookUtil.addCell(row, 11, String.valueOf(cabling.assetLoc ?: ""))
			rowNum++
		}

		postSheetProcess(cablingSheet, autoResizeCols)
	}

	/**
	 * Resolves the display string for the shutdownBy, startupBy, testingBy fields by either
	 * getting the name of the person or stripping the prefix for SME/AppOwner or Role
	 * @param byValue - application's shutdownBy, startupBy, or testingBy raw value
	 * @param stripPrefix - if true or not specified, the function will remove the # or @ character from the string
	 * @return value to display
	 */
	private def resolveByName(byValue, stripPrefix = true) {
		def byObj = ''
		if (byValue) {
			if (byValue.isNumber()) {
				byObj = Person.read(Long.parseLong(byValue))
			} else {
				byObj = (stripPrefix && ['@', '#'].contains(byValue[0])) ? byValue[1..-1] : byValue
			}
		}
		return byObj
	}

	/**
	 * Get a readable/writable instance of the workbook template so application can override title sheet and custom headers
	 * @return a Workbook instance
	 */
	private Workbook getInitWorkbookInstance( String template ) {
		File workbookTemplate =  grailsApplication.parentContext.getResource( template ).getFile()
		if (!workbookTemplate) {
			throw new RuntimeException("Unable to find template ${template}")
		}
		return WorkbookUtil.getInitWorkbookInstance(workbookTemplate)
	}

	/**
	 * Used to clear out the hibernate session of objects no longer needed to help performance. It will also merge the existing
	 */
	private void flushAndClearSession() {
		GormUtil.flushAndClearSession(sessionFactory)
	}
}
