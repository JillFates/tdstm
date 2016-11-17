import com.tds.asset.Application
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.TaskDependency
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavEntityAttribute
import com.tdssrc.eav.EavEntityType
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StopWatch
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Environment
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.DataTransferAttributeMap
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.DataTransferSet
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectAssetMap
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.domain.Workflow
import net.transitionmanager.domain.WorkflowTransition
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.CommentService
import net.transitionmanager.service.ControllerService
import net.transitionmanager.service.DeviceService
import net.transitionmanager.service.ImportService
import net.transitionmanager.service.InvalidParamException
import net.transitionmanager.service.MoveBundleService
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProgressService
import net.transitionmanager.service.ProjectService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.StateEngineService
import net.transitionmanager.service.TaskImportExportService
import net.transitionmanager.service.TaskService
import net.transitionmanager.service.UnauthorizedException
import net.transitionmanager.service.InvalidRequestException
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.UserPreferenceService
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.UserService
import net.transitionmanager.utils.Profiler
import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringEscapeUtils as SEU
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.util.StreamUtils
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.text.DateFormat

@SuppressWarnings('GrMethodMayBeStatic')
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class AssetEntityController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	// This is a has table that sets what status from/to are available
	private static final Map statusOptionForRole = [
		ALL: [
			'*EMPTY*':                    AssetCommentStatus.list,
			(AssetCommentStatus.PLANNED): AssetCommentStatus.list,
			(AssetCommentStatus.PENDING): AssetCommentStatus.list,
			(AssetCommentStatus.READY):   AssetCommentStatus.list,
			(AssetCommentStatus.STARTED): AssetCommentStatus.list,
			(AssetCommentStatus.HOLD):    AssetCommentStatus.list,
			(AssetCommentStatus.DONE):    AssetCommentStatus.list
		],
		LIMITED: [
			'*EMPTY*':                    [AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING, AssetCommentStatus.HOLD],
			(AssetCommentStatus.PLANNED): [AssetCommentStatus.PLANNED],
			(AssetCommentStatus.PENDING): [AssetCommentStatus.PENDING],
			(AssetCommentStatus.READY):   [AssetCommentStatus.READY, AssetCommentStatus.STARTED,
			                               AssetCommentStatus.DONE,  AssetCommentStatus.HOLD],
			(AssetCommentStatus.STARTED): [AssetCommentStatus.READY, AssetCommentStatus.STARTED,
			                               AssetCommentStatus.DONE,  AssetCommentStatus.HOLD],
			(AssetCommentStatus.DONE):    [AssetCommentStatus.DONE,  AssetCommentStatus.HOLD],
			(AssetCommentStatus.HOLD):    [AssetCommentStatus.HOLD]
		]
	]

	// The spreadsheet columns that are Date format
	private static final List<String> importColumnsDateType = ['MaintExp', 'Retire']

	AssetEntityService assetEntityService
	CommentService commentService
	ControllerService controllerService
	DeviceService deviceService
	def filterService
	JdbcTemplate jdbcTemplate
	MoveBundleService moveBundleService
	PartyRelationshipService partyRelationshipService
	PersonService personService
	ProgressService progressService
	ProjectService projectService
	Scheduler quartzScheduler
	SecurityService securityService
	StateEngineService stateEngineService
	TaskImportExportService taskImportExportService
	TaskService taskService
	UserPreferenceService userPreferenceService
	UserService userService

	/**
	 * To Filter the Data on AssetEntityList Page
	 * @param  Selected Filter Values
	 * @return Will return filters data to AssetEntity
	 */
	def filter() {
		if (params.rowVal) {
			if (!params.max) params.max = params.rowVal
			userPreferenceService.setPreference(PREF.MAX_ASSET_LIST, params.rowVal)
		} else {
			if (!params.max) {
				String maxAssetList = userPreferenceService.getPreference(PREF.MAX_ASSET_LIST)
				if (maxAssetList) {
					params.max = maxAssetList ?: 50
				}
			}
		}

		Project project = securityService.userCurrentProject
		params['project.id'] = project.id

		List<AssetEntity> assetEntityInstanceList = filterService.filter(params, AssetEntity).findAll { it.projectId == project.id }

		try {
			render(view: 'list',
				    model: [assetEntityInstanceList: assetEntityInstanceList, params: params, maxVal: params.max,
				            assetEntityCount: filterService.count(params, AssetEntity), projectId: project.id,
				            filterParams: com.zeddware.grails.plugins.filterpane.FilterUtils.extractFilterParams(params)])
		}
		catch (e) {
			redirect (action: 'list')
		}
	}

	/**
	 * The initial Asset Import form
	 */
	@HasPermission('Import')
	def assetImport() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def prefMap = userPreferenceService.getImportPreferences()

		List assetsByProject	= AssetEntity.findAllByProject(project)
		List moveBundleInstanceList = MoveBundle.findAllByProject(project)

		def dataTransferSetImport = DataTransferSet.executeQuery("from DataTransferSet where transferMode IN ('B','I') ")
		def dataTransferSetExport = DataTransferSet.executeQuery("from DataTransferSet where transferMode IN ('B','E') ")

		def dataTransferBatchs = DataTransferBatch.findAllByProject(project).size()
		setBatchId 0
		setTotalAssets 0

		boolean isMSIE = false
		def userAgent = request.getHeader("User-Agent")
		if (userAgent.contains("MSIE") || userAgent.contains("Firefox"))
			isMSIE = true

		render(view: "importExport",
			    model: [assetsByProject: assetsByProject, projectId: project.id, error: params.error,
			            moveBundleInstanceList: moveBundleInstanceList, dataTransferSetImport: dataTransferSetImport,
			            dataTransferSetExport: dataTransferSetExport, prefMap: prefMap, message: params.message,
			            dataTransferBatchs: dataTransferBatchs, args: params.list("args"), isMSIE: isMSIE])
	}

	/**
	 * Render the Export form
	 */
	def assetExport() {}

	/**
	 * Renders the export form.
	 */
	@HasPermission('Export')
	def exportAssets() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			flash.message = " No Projects are Associated, Please select Project. "
			return
		}

		def dataTransferSetExport = DataTransferSet.executeQuery("from DataTransferSet where transferMode IN ('B','E') ")

		[dataTransferBatchs: DataTransferBatch.countByProject(project),
		 prefMap: userPreferenceService.getExportPreferences(),
		 moveBundleInstanceList: MoveBundle.findAllByProject(project),
		 dataTransferSetExport: dataTransferSetExport, projectId: project.id]
	}

	/**
	 * Helper method used get the domain column names as a list substituting the custom labels appropriately
	 * @param entityDTAMap :  dataTransferEntityMap for entity type
	 * @param project - the project to match the custom
	 * @return List
	 */
	private List getColumnNamesForDTAMap(List entityDTAMap, Project project) {
		List columnslist = []
		entityDTAMap.eachWithIndex { item, pos ->
			if (AssetEntityService.customLabels.contains(item.columnName)) {
				columnslist.add(project[item.eavAttribute?.attributeCode] ?: item.columnName)
			} else {
				columnslist.add(item.columnName)
			}
		}
		return columnslist
	}

	/**
	 * A helper closure used to convert the cells in a row into values for an insert statement
	 * @param sqlStrBuff - the StringBuffer to append the VALUES sql into
	 * @param errorMsgList - the running list of error messages
	 * @param sheetRef - the Sheet being processed
	 * @param rowOffset - the row # (offset starting at 0)
	 * @param colOffset - the column # (offset starting at 0)
	 * @param dtaMapField - the DataTransferAttribute Map property for the current column
	 * @param entityId - the id of the asset if it was referenced in import
	 * @param dtBatchId - the batch number of the import
	 * @references formatDate
	 * @return An error message if it failed to add the value to the buffer
	 */
	private String rowToImportValues(Map sheetInfo, StringBuffer sqlStrBuff, Sheet sheetRef, Integer rowOffset,
	                                 Integer colOffset, DataTransferAttributeMap dtaMapField, String entityId,
	                                 Long dtBatchId) {
		def cellValue
		String errorMsg = ''
		boolean isFirstField = !sqlStrBuff
		// Get the Excel column code (e.g. column 0 = A, column 1 = B)
		String colCode = WorkbookUtil.columnCode(colOffset)

		try {
			cellValue = WorkbookUtil.getStringCellValue(sheetRef, colOffset, rowOffset, '', true)
			if (cellValue == ImportService.NULL_INDICATOR) {
				// TODO : check for columns that don't support NULL clearing
			} else {
				if ((dtaMapField.columnName in importColumnsDateType))  {
					if (!StringUtil.isBlank(cellValue)) {
						def dateValue = WorkbookUtil.getDateCellValue(sheetRef, colOffset, rowOffset, (DateFormat) sheetInfo.dateFormatter)
						// Convert to string in the Date format
						if (dateValue) {
							cellValue = TimeUtil.formatDate(dateValue, sheetInfo.userDateFormatter)
							//cellValue = TimeUtil.formatDate(dateValue)
						} else {
							cellValue = ''
						}
					}
					// log.debug "Processing Date field $dtaMapField.columnName - dateValue=$dateValue, cellValue=$cellValue"

				} else {
					// TODO : sizeLimit can lookup known properties to know if there are limits
					int sizeLimit = 255
					if (cellValue?.size() > sizeLimit) {
						cellValue = cellValue.substring(0,sizeLimit)
						errorMsg = "Error column $dtaMapField.columnName ($colCode) value length exceeds $sizeLimit chars"
					}
				}
			}
		} catch (e) {
			log.debug "rowToImportValues() exception - ${ExceptionUtil.stackTraceToString(e)}"
			errorMsg = "Error column $dtaMapField.columnName ($colCode) - $e.message"
		}

		// Only create a value if the field isn't blank
		if (cellValue != '') {
			int cellHasError = errorMsg ? 1 : 0
			String values = (isFirstField ? '' : ', ') +
				"($entityId, '$cellValue', $rowOffset, $dtBatchId, $dtaMapField.eavAttribute.id, $cellHasError, '$errorMsg')"

			if (!errorMsg) {
				sqlStrBuff.append(values)
			}
		}

		return errorMsg
	}

	/**
	 * A helper closure used to perform the actual insert statement into the dataTransfer table
	 * @param sheetName - the name of the tab for error reporting
	 * @param rowOffset - the row # (offset starting at 0)
	 * @param dtValues - the StringBuffer that contains the VALUES(...), VALUES(...), ...
	 * @param results - a map that keeps track of errors, skips and rows added
	 * @return True if insert was successful otherwise false
	 */
	private boolean insertRowValues(String sheetName, int rowOffset, StringBuffer dtValues, Map results) {
		boolean success = true

		// SQL statement that is used to insert values the temporary import table
		String DTV_INSERT_SQL =
			'INSERT INTO data_transfer_value ' +
			'(asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id, has_error, error_text) VALUES '

		try {
			String sql = DTV_INSERT_SQL + dtValues
			log.debug "insertRowValues() SQL=$sql"
			jdbcTemplate.update(sql)
			results.added ++
		} catch (Exception e) {
			results.errors << "Insert failed : $e.message"
			// skipped << "$sheetName [row ${rowOffset + 1}] <ul><li>${errorMsgList.join('<li>')}</ul>"
			log.error("insertRowValues() Importing row ${rowOffset + 1} failed : $e.message", e)
			success = false
		}
		return success
	}

	/**
	 * Creates the transfer batch header for the various assets. Note that it also sets a value on the Session
	 * that is used for the progress bar. If it fails, the caller should return to the user.
	 * @param project
	 * @param dataTransferSet
	 * @param entityClassName - The name of the Domain class
	 * @param numOfAssets - The estimated number of assets to be imported
	 * @param exportTime - The datetime that the spreadsheet was originally exported
	 * @return The DataTransferBatch object if successfully created otherwise null
	 */
	private DataTransferBatch createTransferBatch(Project project, DataTransferSet dataTransferSet,
	                                              String entityClassName, int numOfAssets, Date exportTime) {

		def dtb = new DataTransferBatch(
				statusCode: "PENDING", transferMode: "I", dataTransferSet: dataTransferSet,
				project: project, userLogin: securityService.loadCurrentUserLogin(),
				// exportDatetime: GormUtil.convertInToGMT(exportTime, tzId),
				exportDatetime: exportTime, eavEntityType: EavEntityType.findByDomainName(entityClassName))

		if (!dtb.save()) {
			log.error "createTransferBatch() failed save - ${GormUtil.allErrorsString(dtb)}"
			return null
		}

		setBatchId dtb.id
		setTotalAssets numOfAssets
		// log.debug "createTransferBatch() created $dtb"
		return dtb
	}

	/**
	 * @return A map of the various information about the sheet:
	 * 	sheet                  // The POI Sheet object
	 * 	sheetName              // The name of the sheet as it appears on the spreadsheet tab
	 * 	dtaMapList             // The ataTransferAttribute(s) used for this sheet
	 * 	rowCount               // The number of rows in the spreadsheet
	 * 	assetCount             // The number of assets (rows with no names are not counted)
	 * 	columnCount            // The number of columns in the spreadsheet
	 * 	colNamesOrdinalMap     // Map of the columns an the values being the ordinal position/column in sheet
	 * 	nameColumnIndex        // int index/offset of the 'Name' column in the spreadsheet
	 * 	assetIdColumnLabel     // String of column label/header for the asset id property
	 * 	assetIdColumnIndex     // int of asset id column index/offset/column number
	 * 	domainPropertyNameList // The list of the column names in the spreadsheet
	 */
	private Map getSheetInfo(Project project, Workbook spreadsheetWB, String sheetName, String assetIdColLabel,
	                         String columnName, int headerRow, DataTransferSet dataTransferSet) {
		int nameColumnIndex = -1
		Map colNamesOrdinalMap = [:]

		Sheet sheet
		try {
			sheet = spreadsheetWB.getSheet(sheetName)
		} catch (e) {
			throw new RuntimeException("The '$sheetName' sheet is missing from the import spreadsheet")
		}

		// Get the DataTransferAttributeMap list of properties for the sheet
		String dtaMapName = (sheetName == 'Storage' ? 'Files' : sheetName)
		List dtaMapList = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName(dataTransferSet, dtaMapName)

		// Get the spreadsheet column header labels as a Map [label:ordinalPosition]
		int colCount = WorkbookUtil.getColumnsCount(sheet)
		for (int c = 0; c < colCount; c++) {
			String cellContent = WorkbookUtil.getStringCellValue(sheet, c, headerRow)
			colNamesOrdinalMap[cellContent] = c
		}

		List domainPropertyNameList = getColumnNamesForDTAMap(dtaMapList, project)

		// Make sure that the required columns are in the spreadsheet
		checkSheetForMissingColumns(sheetName, domainPropertyNameList, colNamesOrdinalMap)

		// Find the 'Name' column index and then look at each row to count how many assets will be imported
		nameColumnIndex = getColumnIndexForName(sheet, sheetName, columnName, colCount, headerRow)

		int numOfAssets = 0
		int rowsInSheet = sheet.lastRowNum
		for (int row = 1; row <= rowsInSheet; row++) {
			String assetName = WorkbookUtil.getStringCellValue(sheet, nameColumnIndex, row)
			if (assetName?.trim().size()) numOfAssets++
		}

		Map sheetInfo = [sheet: sheet, sheetName: sheetName, dtaMapList: dtaMapList, rowCount: rowsInSheet,
		                 assetCount: numOfAssets, columnCount: colCount, colNamesOrdinalMap: colNamesOrdinalMap,
		                 nameColumnIndex: nameColumnIndex, assetIdColumnLabel: assetIdColLabel,
		                 assetIdColumnIndex: 0, domainPropertyNameList: domainPropertyNameList]

		// log.debug "getSheetInfo() $sheetInfo"
		log.debug "getSheetInfo() dtaMapList=[0] isa ${dtaMapList[0].getClass().name} - ${dtaMapList[0]}"
		return sheetInfo
	}

	/**
	 * A helper to deal with the repeated process for validating each sheet
	 * @param sheetName - the name of the sheet being validated
	 * @param entityMapColumnList - the list of the mapped column names expected
	 * @param sheetColumnNameList - the list of the spreadsheet tab column names
	 */
	private void checkSheetForMissingColumns(String sheetName, domainPropertyList, sheetColumnNameList) {
		List missingCols = getMissingColumns(domainPropertyList, sheetColumnNameList)
		if (missingCols) {
			throw new RuntimeException("missing expected columns $sheetName:${missingCols.join(', ')}")
		}
	}

	/**
	 * Used to compare the sheet headers to the eav mapping of expected column names
	 * @param entityMapColumnList - the names that are expected
	 * @param sheetColumnNames - the column names in the sheet
	 * @return a List the missing columns or blank if okay
	 */
	private List getMissingColumns(List entityMapColumnList, Map sheetColumnNames) {
		// assert entityMapColumnList
		entityMapColumnList.findAll { String name ->
			name != "DepGroup" && !sheetColumnNames.containsKey(name)
		}
	}

	/**
	 * Look up the column index for a given column name
	 * @param sheetObject - the actual sheet object
	 * @param sheetName - the name of the sheet
	 * @param columnName - the name of the column to lookup
	 * @param colCount - the number of columns in the sheet
	 * @param rowOffset - the row offset to the header itself
	 * @return the index value as an int
	 */
	private int getColumnIndexForName(sheetObject, sheetName, columnName, colCount, rowOffset) {
		for (int index = 0; index <= colCount; index++) {
			if (WorkbookUtil.getStringCellValue(sheetObject, index, 0) == columnName) {
				return index
			}
		}

		throw new RuntimeException("unable to find '$columnName' column in sheet '$sheetName'")
	}

	// Method process one of the asset class sheets
	private Map processSheet(project, projectCustomLabels, dataTransferSet, workbook, sheetName, assetIdColName,
	                         assetNameColName, headerRowNum, domainName, timeOfExport, sheetConf) {

		Map results = initializeImportResultsMap()
		try {
			Map sheetInfo = getSheetInfo(project, workbook, sheetName, assetIdColName, 'Name', headerRowNum, dataTransferSet)
			DataTransferBatch dataTransferBatch = createTransferBatch(project, dataTransferSet, domainName,
				sheetInfo.assetCount, timeOfExport)
			if (!dataTransferBatch) {
				forward action: forwardAction, params: [error:
					"Failed to create import batch for the '$assetSheetName' tab. Please contact support if the problem persists."]
				return
			}

			sheetInfo << sheetConf
			importSheetValues(results, dataTransferBatch, projectCustomLabels, sheetInfo)
			results.dataTransferBatch = dataTransferBatch

			log.debug "processSheet() sheet $sheetName results = $results"
		} catch (e) {
			log.debug "processSheet() exception : ${ExceptionUtil.stackTraceToString(e)}"
			results.errors << "Sheet $sheetName failed to process - $e.message"
		}

		return results
	}

	/**
	 * Iterates over the spreadsheet rows and loads each of the cells into the DataTransferValue table
	 * @param results - the map used to track errors, skipped rows and count of what was added
	 * @param dataTransferBatch - the batch to insert the rows into
	 * @param projectCustomLabels - the custom label values for the project
	 * @param sheetInfo - the map of all of the sheet information
	 * @return a Map containing the following elements
	 *		List errors - a list of errors
	 *		List skipped - a list of skipped rows
	 *		Integer added - a count of rows added
	 */
	private Map importSheetValues(Map results, DataTransferBatch dataTransferBatch, Map projectCustomLabels, Map sheetInfo) {

		Sheet sheetObject = sheetInfo.sheet
		Map colNamesOrdinalMap = sheetInfo.colNamesOrdinalMap
		int assetNameColIndex = sheetInfo.nameColumnIndex
		String assetSheetName = sheetInfo.sheetName

		// log.debug "importSheetValues() sheetInfo=sheetInfo"

		Project project = dataTransferBatch.project

		// Verify that the sheet has the Asset Id Column by name that we are expecting
		if (!colNamesOrdinalMap.containsKey(sheetInfo.assetIdColumnLabel)) {
			results.errors << "$assetSheetName Sheet - missing asset id column name '$sheetInfo.assetIdColumnLabel'"
		} else {

			results.rowsProcessed = sheetInfo.rowCount

			// Iterate over each row in the spreadsheet
			for(int r = 1; r <= sheetInfo.rowCount ; r++) {
				boolean rowHasErrors = false
				String errorMsg
				def assetId
				StringBuffer sqlValues = new StringBuffer()

				// Make sure that the asset has the mandatory name
				def assetName = WorkbookUtil.getStringCellValue(sheetObject, assetNameColIndex, r)
				if (!assetName) {
					errorMsg = "missing required 'name'"
					results.errors << "$assetSheetName [row ${r + 1}] - $errorMsg"
					rowHasErrors = true
				} else {

					// Now check to see if the asset references a pre-existing asset by id #
					assetId = WorkbookUtil.getStringCellValue(sheetObject, 0, r)
					if (assetId) {
						// Switch to a positive long and if null then it is bogus
						Long id = NumberUtil.toPositiveLong(assetId)
						if (id == null) {
							errorMsg = "invalid assetId format '$assetId'"
						} else {
							def asset = AssetEntity.get(id)
							if (!asset) {
								errorMsg = "asset not found '$assetId'"
							} else if (asset.project != project) {
								errorMsg = "invalid asset id '$assetId'"
								securityService.reportViolation("attempted to access asset ($assetId) associated to different project")
							}
						}
					} else {
						assetId = 'null'
					}

					if (errorMsg) {
						results.errors << "$assetSheetName [row ${r + 1}] - $errorMsg"
						continue
					}

					for (int cols = 0; cols < sheetInfo.columnCount; cols++) {
						String attribName
						String columnHeader = WorkbookUtil.getStringCellValue(sheetObject, cols, 0)
						if (projectCustomLabels.containsKey(columnHeader)) {
							// A custom column that renamed
							attribName = projectCustomLabels[columnHeader]
						} else {
							attribName = columnHeader
						}
						def dtaAttrib = sheetInfo.dtaMapList.find{ it.columnName == attribName }

						if (dtaAttrib != null) {

							// Add the SQL VALUES(...) to the sqlValues StringBuffer for the current spreadsheet cell
							errorMsg = rowToImportValues(sheetInfo, sqlValues, sheetObject, r, cols, dtaAttrib, assetId, dataTransferBatch.id)
							if (errorMsg) {
								rowHasErrors = true
								results.errors << "$assetSheetName [row ${r + 1}] - $errorMsg"
							}
						}
					}
				}

				if (rowHasErrors) {
					log.debug "importSheetValues() rowHasErrors - $errorMsg"
					// Clear the error msg so it doesn't get reported again below since it was already reported in the above for col loop
					errorMsg = ''
				} else {
					try {
						// Attempt to actual insert the values that represent the current row of data
						insertRowValues(assetSheetName, r, sqlValues, results)
					} catch (e) {
						log.warn "importSheetValues() insert failed $e.message (sheet:$assetSheetName, row:$r) - ${ExceptionUtil.stackTraceToString(e)}"
						errorMsg = "Failed to insert data due to $e.message"
					}
				}

				if (errorMsg) {
					results.errors << "$assetSheetName [row ${r + 1}] - $errorMsg"
				}

			} // for r

			results.summary = "$results.rowsProcessed Rows read, $results.added Loaded, ${results.errors.size()} Errored"
		}

		return results
	}

	/**
	 * Returns the Map used to track the results of imports for each tab
	 * @return map template of import stats/data
	 */
	private Map initializeImportResultsMap() {
		[errors: [], skipped: [], summary: '', added: 0]
	}

	/**
	 * Upload the Data from the ExcelSheet
	 * @param DataTransferSet,Project,Excel Sheet
	 * @return currentPage(assetImport Page)
	 */
	@HasPermission('Import')
	def upload() {
		// URL action to forward to if there is an error
		String forwardAction = 'assetImport'

		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			String warnMsg = flash.message
			flash.message = null
			forward(action: forwardAction, params: [error: warnMsg])
			return
		}

		def stopwatch = new StopWatch()
		stopwatch.start()

		// ------
		// Some variables that are referenced by the following closures
		// ------
		boolean flagToManageBatches = false

		// List of all of the error/warning messages tracked during the import
		List errorMsgList = []

		// List of all of the rows that were skipped
		List skipped = []

		// The list of sheets that use the common import process (or at least for reporting Dependencies, Cabling, Comments)
		List sheetList = ['Devices', 'Applications', 'Databases', 'Storage', 'Dependencies', 'Cabling', 'Comments']
		// This will retain the results from the various spreadsheet tabs that use the common import process
		Map uploadResults = [:]
		// Initialize the results
		sheetList.each { uploadResults[it] = [addedCount: 0, skippedCount: 0, processed: false, summary: ''] }

		DataTransferBatch dataTransferBatch

		// ------
		// The following section are a few Closures to help simplify the code
		// ------

		// closure used to redirect user with an error message for sever issues
		def failWithError = { message ->
			log.error "upload() $securityService.currentUsername was $message"
			forward action: forwardAction, params: [error: message]
		}

		// A closure to track the results of the different sheets being processed
		def processResults = { theSheetName, theResults ->
			if (!uploadResults.containsKey(theSheetName)) {
				failWithError "Unhandled Sheet '$theSheetName' - please contact support"
				return
			}

			// Save the transfer batch so it can be used by the saveProcessResultsToBatch closure below
			dataTransferBatch = theResults.dataTransferBatch

			uploadResults[theSheetName].with {
				addedCount = theResults.added
				skippedCount = theResults.skipped?.size() ?: 0
				processed = true
				summary = theResults.summary
				errorList = theResults.errors ?: []
				erroredCount = (errorList?.size() ?: 0)
			}
			uploadResults.skipped = theResults.skipped

			if (theResults.skipped) {
				skipped.addAll(theResults.skipped)
			}
			if (theResults.errors) {
				errorMsgList.addAll(theResults.errors)
			}

			// Set flag so user is later prompted to process the batch(es)
			if (theResults.added > 0) {
				flagToManageBatches = true
			}
		}

		/**
		 * A closure for saving the results back into the task batch
		 * Note that the taskBatch which is created in a function get tucked into the results so that we can
		 * use it here to save the results. While ugly, this was a quick way of getting it to work.
		 * @param theSheetName - the name of the sheet
		 * @param theResults - contains the results from the processResults function
		 */
		def saveProcessResultsToBatch = { theSheetName, theResults ->
			// Generate the results and save into the batch for historical reference
			// log.debug "saveProcessResultsToBatch: theSheetName=$theSheetName, theResults = $theResults"
			StringBuffer sprtbMsg = generateResults(theResults, theResults[theSheetName].skipped, [theSheetName], false)
			dataTransferBatch.importResults = sprtbMsg.toString()
			dataTransferBatch.save()
		}

		setBatchId 0
		setTotalAssets 0

		if (!params.dataTransferSet) {
			failWithError 'Import request was missing expected parameter(s)'
			return
		}

		DataTransferSet dataTransferSet = DataTransferSet.get(params.dataTransferSet)
		if (!dataTransferSet) {
			failWithError 'Unable to locate Data Import definition for $params.dataTransferSet'
			return
		}

		// Contains map of the custom fields name values to match with the spreadsheet
		Map projectCustomLabels = [:]
		for (int i = 1; i<= Project.CUSTOM_FIELD_COUNT; i++) {
			String pcKey = 'custom' + i
			if (project[pcKey]) {
				projectCustomLabels[project[pcKey]] = 'Custom' + i
			}
		}

		// Get the uploaded spreadsheet file
		MultipartHttpServletRequest mpr = (MultipartHttpServletRequest)request
		CommonsMultipartFile file = (CommonsMultipartFile) mpr.getFile("file")

		// create workbook
		def workbook
		def titleSheet
		def sheetNameMap = ['Title', 'Applications', 'Devices', 'Databases', 'Storage', 'Dependencies', 'Cabling']
		Map appNameMap = [:]
		Map databaseNameMap = [:]
		Map filesNameMap = [:]
		Date exportTime
		def dataTransferAttributeMapSheetName
		int devicesAdded  = 0
		int appAdded   = 0
		int dbAdded  = 0
		int filesAdded = 0

		//get column name and sheets
		//retrieveColumnNames(serverDTAMap, serverColumnslist, project)
		//retrieveColumnNames(appDTAMap, appColumnslist, project)
		//retrieveColumnNames(databaseDTAMap, databaseColumnslist, project)
		//retrieveColumnNames(filesDTAMap, filesColumnslist, project)

		int dependencyCount = 0
		int cablingCount = 0

		/*
		def dependencyColumnList = ['DependentId','Type','DataFlowFreq','DataFlowDirection','status','comment']
		def dependencyMap = ['DependentId':1, 'Type':2, 'DataFlowFreq':3, 'DataFlowDirection':4, 'status':5, 'comment':6]
		def DTAMap = [0:'dependent', 1:'type', 2:'dataFlowFreq', 3:'dataFlowDirection', 4:'status', 5:'comment']
		*/

		try {
			workbook = WorkbookFactory.create(file.inputStream)
			def sheetNames = WorkbookUtil.getSheetNames(workbook)
			def flag = 0
			def sheetNamesLength = sheetNames.size()
			for(int i=0;  i < sheetNamesLength; i++) {
				if (sheetNameMap.contains(sheetNames[i].trim())) {
					flag = 1
				}
			}

			def sheetConf = [:]

			// Get the title sheet
			titleSheet = workbook.getSheet("Title")

			if (titleSheet != null) {
				try {
					String tzId = WorkbookUtil.getStringCellValue(titleSheet, 1, 8)
					String dateFormatType = WorkbookUtil.getStringCellValue(titleSheet, 1, 9)
					def dateTimeFormatter = TimeUtil.createFormatterForType(dateFormatType, TimeUtil.FORMAT_DATE_TIME_22)
					def dateFormatter = TimeUtil.createFormatterForType(dateFormatType, TimeUtil.FORMAT_DATE_TIME_12)

					String userTzId = userPreferenceService.timeZone
					String userDTFormat = userPreferenceService.dateFormat

					def userDateFormatter = TimeUtil.createFormatterForType(userDTFormat, TimeUtil.FORMAT_DATE)

					sheetConf.tzId = tzId
					sheetConf.dateFormatType = dateFormatType
					sheetConf.dateFormatter = dateFormatter
					sheetConf.userDateFormatter = userDateFormatter

					exportTime = WorkbookUtil.getDateTimeCellValue(titleSheet, 1, 7, tzId, dateTimeFormatter)
				} catch (Exception e) {
					log.info "Was unable to read the datetime for 'Export on': $e.message"
					failWithError "The 'Exported On' datetime was not found or was invalid in the Title sheet"
					return
				}
			} else {
				failWithError 'The required Title sheet was not found in the uploaded spreadsheet'
				return
			}

			Map importResults
			String sheetName, domainClassName

			log.info "upload() Initializtion loading took ${stopwatch.lap()}"

			// ----
			// Devices Sheet
			// ----
			if (params.asset == 'asset') {
				log.info "upload() beginning Devices"
				sheetName='Devices'
				domainClassName = 'AssetEntity'
				importResults = processSheet(project, projectCustomLabels, dataTransferSet, workbook, sheetName,
				                             'assetId', 'Name', 0, domainClassName, exportTime, sheetConf)
				processResults(sheetName, importResults)
				saveProcessResultsToBatch(sheetName, uploadResults)
				log.info "upload() Devices took ${stopwatch.lap()}"
			}

			// ----
			// Applications Sheet
			// ----
			if (params.application == 'application') {
				log.info "upload() beginning Applications"
				sheetName='Applications'
				domainClassName = 'Application'
				importResults = processSheet(project, projectCustomLabels, dataTransferSet, workbook, sheetName,
				                             'appId', 'Name', 0, domainClassName, exportTime, sheetConf)
				processResults(sheetName, importResults)
				saveProcessResultsToBatch(sheetName, uploadResults)
				log.info "upload() Applications took ${stopwatch.lap()}"
			}

			// ----
			// Database Sheet
			// ----
			if (params.database == 'database') {
				log.info "upload() beginning Databases"
				sheetName='Databases'
				domainClassName = 'Database'
				importResults = processSheet(project, projectCustomLabels, dataTransferSet, workbook, sheetName,
				                             'dbId', 'Name', 0, domainClassName, exportTime, sheetConf)
				processResults(sheetName, importResults)
				saveProcessResultsToBatch(sheetName, uploadResults)
				log.info "upload() Databases took ${stopwatch.lap()}"
			}

			// Storage Sheet
			if (params.storage == 'storage') {
				log.info "upload() beginning Logical Storage"
				sheetName='Storage'
				domainClassName = 'Files'
				importResults = processSheet(project, projectCustomLabels, dataTransferSet, workbook, sheetName,
				                             'filesId', 'Name', 0, domainClassName, exportTime, sheetConf)
				processResults(sheetName, importResults)
				saveProcessResultsToBatch(sheetName, uploadResults)
				log.info "upload() Logical Storage took ${stopwatch.lap()}"
			}

			// Process Dependencies
			if (params.dependency == 'dependency') {
				log.info "upload() beginning Dependencies"
				def dependencySheet = workbook.getSheet("Dependencies")
				def dependencySheetRow = dependencySheet.getLastRowNum()

				for (int row = 1; row <= dependencySheetRow; row++) {
					// Check AssetName column (C) for not being blank
					def name = WorkbookUtil.getStringCellValue(dependencySheet, 2, row)
					if (name) {
						dependencyCount++
					}
				}

				// Set the session for progress meter
				setTotalAssets dependencyCount

				importResults = initializeImportResultsMap()
				importResults.rowsProcessed = dependencySheetRow

				int dependencySkipped = 0
				int dependencyAdded = 0
				int dependencyUpdated = 0
				int dependencyErrored = 0
				int dependencyUnchanged = 0

				// A closure used to handle errors
				def dependencyError = { msg ->
					importResults.errors << msg
					dependencyErrored++
					dependencySkipped--
				}

				// def assetDepTypeList = AssetDependencyType.getList()
				// def assetDepStatusList = AssetDependencyStatus.getList()
				String lookupQuery = 'select value from AssetOptions where type=? order by value'
				List assetDepTypeList = AssetOptions.executeQuery(lookupQuery, [AssetOptions.AssetOptionsType.DEPENDENCY_TYPE])
				List assetDepStatusList = AssetOptions.executeQuery(lookupQuery, [ AssetOptions.AssetOptionsType.DEPENDENCY_STATUS ])

				def lookupValue = { value, list ->
					for (it in list) {
						if (it.equalsIgnoreCase(value)) {
							return it
						}
					}
					'Unknown'
				}

				for (int r = 1; r <= dependencySheetRow ; r++) {
					// Assume that the dependency is skipped and we'll decrement when the row is saved at the bottom
					dependencySkipped++

					int rowNum = r + 1

					// This will clear the session every 50 rows
					if (GormUtil.flushAndClearSession(rowNum)) {
						project = GormUtil.mergeWithSession(project)
					}

					def assetId
					def assetIdCell = WorkbookUtil.getStringCellValue(dependencySheet, 1, r)
					if (assetIdCell) {
						// TODO : JPM 5/2016 : upload() why is the dependency processing escaping quotes on columns that should be numbers?
						assetId = NumberUtils.toDouble(assetIdCell.replace("'","\\'"), 0).round()
					}

					String assetName
					String assetClass
					if (!assetId) {
						assetName = WorkbookUtil.getStringCellValue(dependencySheet, 2, r).replace("'","\\'")
						assetClass = WorkbookUtil.getStringCellValue(dependencySheet, 3, r).replace("'","\\'")

						if (!assetName) {
							dependencyError "Missing AssetId (in B$rowNum) or AssetName (in C$rowNum)"
							continue
						}
					}

					// ----
					// Try to lookup the AssetDependency record based on the depId (column A)
					// ----
					Long depId
					String depIdCell = WorkbookUtil.getStringCellValue(dependencySheet, 0, r)
					AssetDependency assetDep
					if (depIdCell) {
						depId = NumberUtil.toPositiveLong(depIdCell, -1)
						if (depId == -1) {
							importResults.errors << "Invalid AssetDependencyId number '$depIdCell' (in A$rowNum)"
							continue
						}
						if (depId > 0) {
							assetDep = AssetDependency.get(depId)
							if (!assetDep) {
								dependencyError "AssetDependencyId '$depId' not found (in A$rowNum)"
								continue
							}
							if (assetDep.asset.projectId != project.id) {
								securityService.reportViolation("attempted to access assetDependency ($depId) not assigned to project ($project.id)")
								dependencyError " invalid AssetDependencyId reference '$depId' (in A$rowNum)"
								continue
							}
							log.debug "upload() found dependency by id $depId"
						}
					}

					// ----
					// Lookup the asset
					// ----
					AssetEntity asset
					if (assetId) {
						asset = AssetEntity.get(assetId)
						if (!asset) {
							dependencyError "Dependency asset by AssetId ($assetId) not found (in B$rowNum)"
							continue
						}
						if (asset.project.id != project.id) {
							securityService.reportViolation("attempted to access asset ($assetId) not assigned to project ($project.id)")
							dependencyError "Invalid reference of AssetId ($assetId) (row $rowNum)"
							continue
						}
					} else {
						def assets = AssetEntity.findAllByAssetNameAndProject(assetName, project)
						if (!assets) {
							dependencyError "Asset not found by AssetName '$assetName' (row $rowNum)"
							continue
						}

						if (assets.size() > 1) {
							asset = assets.find { it.assetType == assetClass }
							if (asset == null) {
								dependencyError "Asset by AssetName '$assetName' found duplicated assets (row $rowNum)"
								continue
							}
						} else {
							asset = assets[0]
						}
					}

					// ----
					// Lookup the dependent asset
					// ----
					AssetEntity dependent
					def dependencyId = NumberUtils.toDouble(WorkbookUtil.getStringCellValue(dependencySheet, 4, r).replace("'","\\'"), 0).round()
					if (dependencyId) {
						dependent = AssetEntity.get(dependencyId)
						if (!dependent) {
							dependencyError "Asset by DependentId ($dependencyId) not found (row $rowNum)"
							continue
						}
						if (dependent.projectId != project.id) {
							securityService.reportViolation("attempted to access dependent ($dependencyId) not assigned to project ($project.id)")
							dependencyError "Invalid reference of DependentId ($dependencyId) (row $rowNum)"
							continue
						}
					} else {
						def depName = WorkbookUtil.getStringCellValue(dependencySheet, 5, r).replace("'","\\'")
						def depClass = WorkbookUtil.getStringCellValue(dependencySheet, 6, r).replace("'","\\'")
						def assets = AssetEntity.findAllByAssetNameAndProject(depName, project)
						if (!assets) {
							dependencyError "Asset by DependentName ($depName) not found (row $rowNum)"
							continue
						}
						if (assets.size() > 1) {
							dependent = assets.find { it.assetType == depClass }
							if (dependent == null) {
								dependencyError "Asset by DependentName '$depName' found duplicated names (row $rowNum)"
								continue
							}
						} else {
							dependent = assets[0]
						}
					}

					boolean isNew = false
					if (!assetDep) {

						// Try finding the dependency by the asset and the dependent
						assetDep = AssetDependency.findByAssetAndDependent(asset, dependent)

						if (!assetDep) {
							assetDep = new AssetDependency(createdBy: securityService.loadCurrentPerson())
							isNew = true
						} else {
							 String msg = message(code: "assetEntity.dependency.warning", args: [asset.assetName, dependent.assetName])
							 dependencyError "$msg (row $rowNum)"
							 continue
						}
					}

					if (assetDep) {
						assetDep.asset = asset
						assetDep.dependent = dependent

						def tmpType = WorkbookUtil.getStringCellValue(dependencySheet, 7, r, "").replace("'","\\'")
						def luv = lookupValue(tmpType, assetDepTypeList)
						if (tmpType && tmpType != 'Unknown' && luv == 'Unknown') {
							dependencyError "Invalid Type specified ($tmpType) for row $rowNum"
							continue
						}
						assetDep.type = luv

						// TODO : JPM 5/2016 : the status should probably have the same default value as the tmpType above
						def tmpStatus = WorkbookUtil.getStringCellValue(dependencySheet,10, r, "").replace("'","\\'") ?:
							(isNew ? "Unknown" : assetDep.status)
						luv = lookupValue(tmpStatus, assetDepStatusList)
						if (tmpStatus != 'Unknown' && luv == 'Unknown') {
							dependencyError "Invalid Status specified ($tmpStatus) for row $rowNum"
							continue
						}
						assetDep.status = luv

						assetDep.dataFlowFreq = WorkbookUtil.getStringCellValue(dependencySheet, 8, r, "").replace("'","\\'") ?:
							(isNew ? "Unknown" : assetDep.dataFlowFreq)
						assetDep.dataFlowDirection = WorkbookUtil.getStringCellValue(dependencySheet, 9, r, "").replace("'","\\'") ?:
							(isNew ? "Unknown" : assetDep.dataFlowDirection)

						def depComment = WorkbookUtil.getStringCellValue(dependencySheet, 11, r, "").replace("'","\\'")
						def length = depComment.length()
						if (length > 255) {
							depComment = StringUtil.ellipsis(depComment,255)
							dependencyError  "The comment was trimmed to 255 characters (row $rowNum)"
						}

						assetDep.comment = depComment
						assetDep.c1 = WorkbookUtil.getStringCellValue(dependencySheet, 12, r, "").replace("'","\\'")
						assetDep.c2 = WorkbookUtil.getStringCellValue(dependencySheet, 13, r, "").replace("'","\\'")
						assetDep.c3 = WorkbookUtil.getStringCellValue(dependencySheet,14, r, "").replace("'","\\'")
						assetDep.c4 = WorkbookUtil.getStringCellValue(dependencySheet, 15, r, "").replace("'","\\'")
						assetDep.updatedBy = securityService.loadCurrentPerson()

						// Make sure that there are no domain constraint errors
						if (assetDep.hasErrors()) {
							dependencyError "Validation errors exist (row $rowNum) : ${GormUtil.allErrorsString(assetDep)}"
							continue
						}

						if (!isNew && !assetDep.dirtyPropertyNames) {
							dependencyUnchanged++
							dependencySkipped--
							continue
						}

						if (!isNew && assetDep.dirtyPropertyNames) {
							log.info "upload() Changed fields $assetDep.dirtyPropertyNames of Dependency $assetDep.id"
						}

						// Attempt to save the record
						if (!assetDep.save(flush:true)) {
							dependencyError "Dependency save failed for row $rowNum : ${GormUtil.allErrorsString(assetDep)}"
							continue
						}

						if (isNew) {
							dependencyAdded++
						} else {
							dependencyUpdated++
						}
						dependencySkipped--
					}
				}

				importResults.summary = "$dependencySheetRow Rows read, $dependencyAdded Added, $dependencyUpdated Updated, $dependencyUnchanged Unchanged, $dependencyErrored Errored, $dependencySkipped Skipped"
				processResults('Dependencies', importResults)
				log.info "upload() Dependencies took ${stopwatch.lap()}"

			} // Process Dependencies

			// ----
			// Process Cabling
			// ----
			if (params.cabling=='cable') {
				log.info "upload() beginning Cabling"
				def cablingSheet = workbook.getSheet("Cabling")
				def cablingSheetRow = cablingSheet.getLastRowNum()
				setTotalAssets cablingSheetRow

				def resultMap = assetEntityService.saveImportCables(cablingSheet)
				importResults = initializeImportResultsMap()

				importResults.rowsProcessed = cablingSheetRow
				importResults.errors = resultMap.warnMsg
				importResults.summary = "$importResults.rowsProcessed Rows read, $resultMap.cablingUpdated Updated, $resultMap.cablingSkipped Skipped, ${importResults.errors.size()} Errored"
				processResults('Cabling', importResults)

				log.info "upload() Cabling took ${stopwatch.lap()}"
			}

			// ----
			// Process Comments Imports
			// ----
			if (params.comment=='comment') {
				log.info "upload() beginning Comments"
				def commentsSheet = workbook.getSheet("Comments")

				int commentAdded = 0
				int commentUpdated = 0
				int commentUnchanged = 0
				int commentCount = commentsSheet.getLastRowNum()
				//def skippedUpdated = 0
				//def skippedAdded = 0

				setTotalAssets commentCount

				importResults = initializeImportResultsMap()
				importResults.rowsProcessed = commentCount

				// TODO : JPM 11/2014 : Refactor the lookup of PartyGroup.get(18) to be TDS lookup - see TM-3570
				List staffList = partyRelationshipService.getAllCompaniesStaffPersons([ project.client, PartyGroup.get(18) ])
				// List staffList // not used in the PersonService at this time
				int r
				int rowNum
				try {
					for (r = 1; r <= commentCount; r++) {
						rowNum = r + 1

						// Clear the Hibernate Session periodically for performance purposes
						if (GormUtil.flushAndClearSession(rowNum)) {
							project = GormUtil.mergeWithSession(project)
						}

						boolean recordForAddition = false
						int cols = 0
						def commentIdImported = WorkbookUtil.getStringCellValue(commentsSheet, cols, r).replace("'","\\'")
						AssetComment assetComment
						if (commentIdImported) {
							def commentId = NumberUtil.toPositiveLong(commentIdImported, -1)
							if (commentId < 1) {
								//skippedUpdated++
								importResults.errors << "Invalid commentId number'$commentIdImported' (row $rowNum)"
								continue
							}
							assetComment = AssetComment.get(commentId)
							if (!assetComment) {
								//skippedUpdated++
								importResults.errors << "CommentId '$commentId' was not found (row $rowNum)"
								continue
							}
							if (assetComment.project != project) {
								securityService.reportViolation("attempted to access assetComment ($commentId) not assigned to project ($project.id)")
								importResults.errors << "Invalid CommentId '$commentIdImported' was specified (row $rowNum)"
								continue
							}
						} else {
							assetComment = new AssetComment(project: project, isImported: true)
							recordForAddition = true
						}

						assetComment.commentType = AssetCommentType.COMMENT

						String assetIdStr = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r).replace("'","\\'")
						Long assetId = NumberUtil.toPositiveLong(assetIdStr, -1)
						if (assetId > 0) {
							AssetEntity assetEntity = AssetEntity.findByIdAndProject(assetId, project)
							if (assetEntity) {
								assetComment.assetEntity = assetEntity
							} else {
								importResults.errors << "The assetId '$assetIdStr' was not found (row $rowNum)"
								//recordForAddition ? skippedAdded++ : skippedUpdated++
								continue
							}
						} else {
							importResults.errors << "An Invalid assetId '$assetIdStr' was specified (row $rowNum)"
							//recordForAddition ? skippedAdded++ : skippedUpdated++
							continue
						}

						// Grab the category
						def categoryInput = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r)?.replace("'","\\'")?.toLowerCase()?.trim()
						if (AssetCommentCategory.list.contains(categoryInput)) {
							assetComment.category = categoryInput ?: AssetCommentCategory.GENERAL
						} else {
							//recordForAddition ? skippedAdded++ : skippedUpdated++
							importResults.errors << "Invalid category '$categoryInput' specified (row $rowNum)"
							continue
						}

						// Try reading the created date as a date and if that fails try as a string and parse
						cols++
						Collection<String> validFormats = [
							TimeUtil.FORMAT_DATE_TIME,
							TimeUtil.FORMAT_DATE_TIME_22,
							TimeUtil.FORMAT_DATE_TIME_24,
							TimeUtil.FORMAT_DATE_TIME_25,
							TimeUtil.FORMAT_DATE
						]
						def dateCreated = WorkbookUtil.getDateCellValue(commentsSheet, cols, r, getSession(), validFormats)
						if (!dateCreated) {
							dateCreated = new Date()
						}

						// We need to keep track of the dateCreated change as it turns out the dirtyPropertyNames will NOT return this property
						boolean dateChanged = false
						if (dateCreated) {
							dateChanged = dateCreated != assetComment.dateCreated
							assetComment.dateCreated = dateCreated
						}

						// Get the createdBy person
						def createdByImported = StringUtils.strip(WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r))
						Person person = createdByImported ? personService.findPerson(createdByImported, project, staffList, false)?.person :
								securityService.loadCurrentPerson()

						if (person) {
							assetComment.createdBy = person
						} else {
							importResults.errors <<  "Created by person '$createdByImported' not found (row $rowNum)"
							continue
						}

						if (!personService.hasAccessToProject(person, project)) {
							importResults.errors << "Created by person '$person' does not have access to project (row $rowNum)"
							continue
						}

						assetComment.comment = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r)

						List dirty = assetComment.getDirtyPropertyNames()
						if (!recordForAddition && dirty.size() == 0 && !dateChanged) {
							commentUnchanged++
							continue
						}

						if (!assetComment.save()) {
							importResults.errors << "Save failed (row $rowNum) : ${GormUtil.allErrorsString(assetComment)}"
						} else {
							if (recordForAddition) {
								commentAdded++
							} else {
								commentUpdated++
							}
						}
					}
				} catch (e) {
					importResults.errors << "Import Failed at row $rowNum due to error '$e.message'"
					log.error "Comment Import failed for $securityService.currentUsername on row $rowNum : ${ExceptionUtil.stackTraceToString(e)}"
				}
				importResults.summary = "$importResults.rowsProcessed Rows read, $commentAdded Added, $commentUpdated Updated, $commentUnchanged Unchanged, ${importResults.errors.size()} Errors"
				processResults('Comments', importResults)
				log.info "upload() Comments took ${stopwatch.lap()}"

			} // Process Comment Imports

			StringBuffer message = generateResults(uploadResults, skipped, sheetList, flagToManageBatches)

			forward action:forwardAction, params: [ message: message.toString() ]

		} catch(NumberFormatException e) {
			log.error "AssetImport Failed ${ExceptionUtil.stackTraceToString(e)}"
			forward action:forwardAction, params: [error: e]
		} catch(Exception e) {
			log.error "AssetImport Failed ${ExceptionUtil.stackTraceToString(e)}"
			forward action:forwardAction, params: [error: e]
		}
	}

	/**
	 * Used to generate the import results
	 * @param results - a Map containing the information collected during the import process
	 * @param skipped - a List of the rows that were skipped
	 * @param sheetList - a List that contains the name of each sheet that was included in the import process
	 * @param notifyManageBatches - a boolean to control if the Manage Batches message is included in the results
	 */
	private StringBuffer generateResults(Map results, List skipped, List sheetList, boolean notifyManageBatches) {
		StringBuffer message = new StringBuffer("<h3>Spreadsheet import was successful</h3>\n")
		if (notifyManageBatches) {
			message.append("<p>Please click the Manage Batches below to review and post these changes</p><br>\n")
		}
		message.append("<p>Results: <ul>\n")
		sheetList.each {
			if (results[it].processed) {
				if (results[it].summary) {
					message.append("<li>$it: ${results[it].summary}</li>\n")
				} else {
					message.append("<li>$it: ${results[it].addedCount} loaded</li>\n")
				}
			}
		}
		message.append("</ul></p><br>\n")

		// Handle the errors and skipped rows
		if (sheetList.find { results[it].errorList?.size() }) {
			message.append("<p>Errors: <ul>\n")
			sheetList.each {
				if (results[it].processed) {
					if (results[it].errorList.size()) {
						message.append("<li>$it:<ul>")
						message.append(results[it].errorList.collect { "<li>$it</li>"}.join("\n"))
						message.append("</li></ul>\n")
					}
				}
			}
			message.append("\n</ul></p>\n")
		}

		if (skipped?.size()) {
			message.append("<br><p>Rows Skipped: <ul>\n")
			message.append("<li>${skipped.size()} spreadsheet row${skipped.size()==0 ? ' was' : 's were'} skipped: <ul>")
			message.append(skipped.collect { "<li>$it</li>" }.join("\n"))
			message.append("\n</ul></p>\n")
		}

		message.append("</p>\n")
		return message
	}

	/**
	 * Used to kick off the export process that will schedule a Quartz job to run in background so that the user will get
	 * an immediate response and can poll for the status of the job.
	 */
	@HasPermission('Export')
	def export() {
		def key = "AssetExport-" + UUID.randomUUID()
		progressService.create(key)

		log.info "Initiate Export"

		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl("TM-" + key, null, new Date(System.currentTimeMillis() + 2000))
		trigger.jobDataMap.putAll(params)

		trigger.jobDataMap.bundle = request.getParameterValues("bundle")
		trigger.jobDataMap.key = key
		trigger.jobDataMap.username = securityService.currentUsername
		trigger.jobDataMap.projectId = securityService.userCurrentProjectId
		trigger.jobDataMap.tzId = userPreferenceService.timeZone
		trigger.jobDataMap.userDTFormat = userPreferenceService.dateFormat
		trigger.jobDataMap[Profiler.KEY_NAME] = session[Profiler.KEY_NAME]

		trigger.setJobName('ExportAssetEntityJob')
		trigger.setJobGroup('tdstm-export-asset')
		quartzScheduler.scheduleJob(trigger)

		progressService.update(key, 1, 'In progress')

		renderSuccessJson(key: key)
	}

//	@HasPermission('Export')
	def downloadExport() {
		String key = params.key
		InputStream io = new FileInputStream(new File(progressService.getData(key, 'filename')))

		response.setContentType(progressService.getData(key, 'contenttype'))
		response.setHeader("Content-Disposition", progressService.getData(key, 'header'))

		OutputStream out = response.outputStream
		IOUtils.copy(io, out)
		out.flush()
		IOUtils.closeQuietly(io)
		IOUtils.closeQuietly(out)
	}

	/**
	 * Used for the assetEntity List to load the initial model. The list subsequently calls listJson to get the
	 * actual data which is rendered by JQ-Grid
	 * @param project, filter, number of properties
	 * @return model data to initate the asset list
	 **/
	def list() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		assetEntityService.getDeviceModelForList(project, session, params, userPreferenceService.timeZone)
	}

	/**
	 * Used by JQgrid to load assetList
	 */
	def listJson() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		renderAsJson assetEntityService.getDeviceDataForList(project, session, params, userPreferenceService.timeZone)
	}

	def delete() {
		AssetEntity assetEntityInstance = AssetEntity.get(params.id)
		if (!assetEntityInstance) {
			flash.message = "AssetEntity not found with id $params.id"
			redirect(action: 'list')
			return
		}

		assetEntityService.deleteAsset(assetEntityInstance)
		assetEntityInstance.delete()
		flash.message = "AssetEntity $assetEntityInstance.assetName deleted"

		def redirectAsset = params.dstPath
		if (redirectAsset?.contains("room_")) {
			def newredirectAsset = redirectAsset.split("_")
			redirectAsset = newredirectAsset[0]
			session.setAttribute("RACK_ID", newredirectAsset[1])
		}

		switch (redirectAsset) {
			case "room": redirect(controller: 'room', action: 'list'); break
			case "rack": redirect(controller: 'rackLayouts', action: 'create'); break
			case "application": redirect(controller: 'application', action: 'list'); break
			case "database": redirect(controller: 'database', action: 'list'); break
			case "files": redirect(controller: 'files', action: 'list'); break
			case "dependencyConsole": forward(action: 'retrieveLists',
			                                  params: [entity: 'server',
			                                           dependencyBundle: session.getAttribute("dependencyBundle")])
				break
			case "assetAudit": render "AssetEntity $assetEntityInstance.assetName deleted"; break
			default: redirect(action: 'list')
		}
	}

	/**
	 * Remove the asset from project
	 */
	def remove() {
		AssetEntity assetEntity = AssetEntity.get(params.id)
		if (assetEntity) {
			ProjectAssetMap.executeUpdate('delete ProjectAssetMap where asset=?', [assetEntity])
			ProjectTeam.executeUpdate('update ProjectTeam set latestAsset=null where latestAsset=?', [assetEntity])
			AssetEntity.executeUpdate('''
				update AssetEntity
				set moveBundle=null, project=null, sourceTeamMt=null, targetTeamMt=null, sourceTeamLog=null,
				    targetTeamLog=null, sourceTeamSa=null, targetTeamSa=null, sourceTeamDba=null, targetTeamDba=null
				where id=?''', assetEntity.id)
			flash.message = "AssetEntity $assetEntity.assetName Removed from Project"
		}
		else {
			flash.message = "AssetEntity not found with id $params.id"
		}
		redirect(action: 'list')
	}

	/**
	 * remote link for asset entity dialog.
	 */
	def editShow() {
		Project project = securityService.userCurrentProject
		AssetEntity assetEntity = AssetEntity.get(params.id)
		List<EavEntityAttribute> entityAttributes = EavEntityAttribute.executeQuery(
			'from EavEntityAttribute where eavAttributeSet=? order by sortOrder', [assetEntity.attributeSetId])
		def items = entityAttributes.collect { EavEntityAttribute entityAttribute ->
			EavAttribute attribute = entityAttribute.attribute
			String code = attribute.attributeCode
			if (!AssetEntityService.bundleMoveAndClientTeams.contains(code) && code != "currentStatus" && code != "usize") {
				String frontEndLabel = attribute.frontendLabel
				if (AssetEntityService.customLabels.contains(frontEndLabel)) {
					frontEndLabel = project[code] ?: frontEndLabel
				}

				List<EavAttributeOption> attributeOptions = EavAttributeOption.findAllByAttribute(attribute, [sort: 'value', order: 'asc'])
				[label: frontEndLabel, attributeCode: code, frontendInput: attribute.frontendInput,
				 options: attributeOptions.collect { [option: it.value] }, modelId: assetEntity?.modelId,
				 value: assetEntity.(code)?.toString() ?: '', bundleId: assetEntity?.moveBundleId,
				 manufacturerId: assetEntity?.manufacturerId]
			}
		}
		renderAsJson items
	}

	def retrieveAttributes() {
		def items = []

		if (params.attribSet) {
			Project project = securityService.userCurrentProject
			List<EavEntityAttribute> entityAttributes = EavEntityAttribute.executeQuery('''
				from EavEntityAttribute
				where eavAttributeSet = :attributeSetId
				order by sortOrder
			''', [attributeSetId: params.attribSet])
			for (EavEntityAttribute it in entityAttributes) {
				List<EavAttributeOption> attributeOptions = EavAttributeOption.findAllByAttribute(it.attribute,
					[sort: 'value', order: 'asc'])
				def options = attributeOptions.collect { option -> [option: option.value] }
				String code = it.attribute.attributeCode
				if (!AssetEntityService.bundleMoveAndClientTeams.contains(code) &&
					 	code != "moveBundle" && code != "currentStatus" && code != "usize") {
					def frontEndLabel = it.attribute.frontendLabel
					if (AssetEntityService.customLabels.contains(frontEndLabel)) {
						frontEndLabel = project[code] ?: frontEndLabel
					}
					items << [label: frontEndLabel, attributeCode: code,
					          frontendInput: it.attribute.frontendInput, options: options]
				}
			}
		}

		renderAsJson items
	}

	def retrieveAssetAttributes() {
		def items = []
		if (params.assetId) {
			def entityAttributes = EavEntityAttribute.executeUpdate(
				'from EavEntityAttribute where eavAttributeSet = :attributeSetId order by sortOrder',
				[attributeSetId: AssetEntity.load(params.assetId).attributeSetId])
			for (EavEntityAttribute it in entityAttributes) {
				String code = it.attribute.attributeCode
				if (!AssetEntityService.bundleMoveAndClientTeams.contains(code) &&
						code != "currentStatus" && code != "usize") {
					items << [attributeCode: code, frontendInput: it.attribute.frontendInput]
				}
			}
		}
		renderAsJson items
	}

	def retrieveAutoCompleteDate(String autoCompAttribs) {
		def data = []
		if (autoCompAttribs) {
			Project project = securityService.userCurrentProject
			autoCompAttribs.split(",").each {
				def value = AssetEntity.executeQuery('''
					select distinct ''' + it + '''
					from AssetEntity
					where owner.id = :ownerId
				''', [ownerId: project.clientId])
				data << [value: value, attributeCode: it]
			}
		}
		renderAsJson data
	}

	def listComments() {
		def assetEntityInstance = AssetEntity.get(params.id)
		def commentType = params.commentType
		def assetCommentsInstance
		boolean canEditComments = true
		if (commentType) {
			if (commentType != 'comment') {
				commentType = 'issue'
			}
			canEditComments = userCanEditComments(commentType)
			assetCommentsInstance = AssetComment.findAllByAssetEntityAndCommentType(assetEntityInstance, commentType)
		} else {
			assetCommentsInstance = AssetComment.findAllByAssetEntity(assetEntityInstance)
		}

		def assetCommentsList = []
		def today = new Date()
		boolean viewUnpublished = securityService.viewUnpublished()

		assetCommentsInstance.each {
			if (viewUnpublished || it.isPublished)
				assetCommentsList <<[commentInstance: it, assetEntityId: it.assetEntity.id,
				                     cssClass: it.dueDate < today ? 'Lightpink' : 'White',
				                     assetName: it.assetEntity.assetName, assetType: it.assetEntity.assetType,
				                     assignedTo: it.assignedTo?.toString() ?: '', role: it.role ?: '',
				                     canEditComments: canEditComments]
		}

		renderAsJson assetCommentsList
	}

	def showComment() {
		def commentList = []
		def personResolvedObj
		def personCreateObj
		def dtCreated
		def dtResolved

		AssetComment assetComment = AssetComment.get(params.id)
		if (assetComment) {
			if (assetComment.createdBy) {
				personCreateObj = assetComment.createdBy.toString()
				dtCreated = TimeUtil.formatDateTime(assetComment.dateCreated)
			}
			if (assetComment.dateResolved) {
				personResolvedObj = assetComment.resolvedBy?.toString()
				dtResolved = TimeUtil.formatDateTime(assetComment.dateResolved)
			}

			String etStart = TimeUtil.formatDateTime(assetComment.estStart)
			String etFinish = TimeUtil.formatDateTime(assetComment.estFinish)
			String atStart = TimeUtil.formatDateTime(assetComment.actStart)
			String dueDate = TimeUtil.formatDate(assetComment.dueDate)

			def workflowTransition = assetComment?.workflowTransition
			String workflow = workflowTransition?.name

			def noteList = assetComment.notes.sort{it.dateCreated}
			def notes = []
			noteList.each {
				notes << [TimeUtil.formatDateTime(it.dateCreated, TimeUtil.FORMAT_DATE_TIME_3),
				          it.createdBy?.toString(), it.note, it.createdBy?.id]
			}

			// Get the name of the User Role by Name to display
			def roles = securityService.getRoleName(assetComment.role)

			def instructionsLinkURL
			def instructionsLinkLabel

			if (assetComment.instructionsLink) {
				List<String> instructionsLinkInfo = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)
				if (instructionsLinkInfo) {
					if (instructionsLinkInfo.size() > 1) {
						instructionsLinkURL = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)[1]
						instructionsLinkLabel = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)[0]
					}
					else {
						instructionsLinkURL = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)[0]
					}
				}
			}

			StringBuilder predecessorTable
			def predecessorList = []
			def taskDependencies = assetComment.taskDependencies
			if (taskDependencies.size() > 0) {
				taskDependencies = taskDependencies.sort{ it.predecessor.taskNumber }
				predecessorTable = new StringBuilder('<table cellspacing="0" style="border:0px;"><tbody>')
				taskDependencies.each { taskDep ->
					def task = taskDep.predecessor
					def css = taskService.getCssClassForStatus(task.status)
					def taskDesc = task.comment?.length()>50 ? task.comment.substring(0,50): task.comment
					predecessorList << [id: taskDep.id, taskId: task.id, category: task.category, desc: taskDesc, taskNumber: task.taskNumber, status: task.status]
					predecessorTable << """<tr class="$css" style="cursor:pointer;" onClick="showAssetComment($task.id, 'show')">""" <<
							"""<td>$task.category</td><td>${task.taskNumber ? task.taskNumber+':' :''}$taskDesc</td></tr>"""
				}
				predecessorTable.append('</tbody></table>')
			}
			def taskSuccessors = TaskDependency.findAllByPredecessor(assetComment)
			def successorsCount= taskSuccessors.size()
			def predecessorsCount = taskDependencies.size()
			StringBuilder successorTable
			def successorList = []
			if (taskSuccessors) {
				taskSuccessors = taskSuccessors.sort { it.assetComment.taskNumber }
				successorTable = new StringBuilder('<table  cellspacing="0" style="border:0px;" ><tbody>')
				taskSuccessors.each { successor ->
					def task = successor.assetComment
					def css = taskService.getCssClassForStatus(task.status)
					def succDesc = task.comment?.length()>50 ? task.comment.substring(0,50): task.comment
					successorList << [id: successor.id, taskId: task.id, category: task.category, desc: succDesc,
					                  taskNumber: task.taskNumber, status: task.status]
					successorTable << """<tr class="$css" style="cursor:pointer;" onClick="showAssetComment($task.id, 'show')">""" <<
							"""<td>$task.category</td><td>$task</td>"""
				}
				successorTable.append("""</tbody></table>""")
			}

			def cssForCommentStatus = taskService.getCssClassForStatus(assetComment.status)
			def canEdit = userCanEditComments(assetComment.commentType)

		// TODO : Security : Should reduce the person objects (create,resolved,assignedTo) to JUST the necessary properties using a closure
			assetComment.durationScale = assetComment.durationScale.toString()
			commentList << [
				assetComment:assetComment,
				durationScale:assetComment.durationScale.value(),
				personCreateObj:personCreateObj,
				personResolvedObj:personResolvedObj,
				dtCreated:dtCreated ?: "",
				dtResolved:dtResolved ?: "",
				assignedTo:assetComment.assignedTo?.toString() ?:'Unassigned',
				assetName:assetComment.assetEntity?.assetName ?: "",
				eventName:assetComment.moveEvent?.name ?: "",
				dueDate:dueDate,
				etStart:etStart,
				etFinish:etFinish,
				atStart:atStart,
				notes:notes,
				workflow:workflow,
				roles:roles?:'Unassigned',
				predecessorTable:predecessorTable ?: '',
				successorTable:successorTable ?: '',
				cssForCommentStatus: cssForCommentStatus,
				statusWarn: taskService.canChangeStatus (assetComment) ? 0 : 1,
				successorsCount: successorsCount,
				predecessorsCount: predecessorsCount,
				assetId: assetComment.assetEntity?.id ?: "",
				assetType: assetComment.assetEntity?.assetType,
				assetClass: assetComment.assetEntity?.assetClass?.toString(),
				predecessorList: predecessorList,
				successorList: successorList,
				instructionsLinkURL: instructionsLinkURL ?: "",
				instructionsLinkLabel: instructionsLinkLabel ?: "",
				canEdit: canEdit
			]
		} else {
			def errorMsg = " Task Not Found : Was unable to find the Task for the specified id - $params.id "
			log.error "showComment: show comment view - $errorMsg"
			commentList << [error:errorMsg]
		}
		renderAsJson commentList
	}

	// def saveComment() { com.tdsops.tm.command.AssetCommentCommand cmd ->
	def saveComment() {
		String tzId = userPreferenceService.timeZone
		String userDTFormat = userPreferenceService.dateFormat
		def map = commentService.saveUpdateCommentAndNotes(tzId, userDTFormat, params, true, flash)
		if (params.forWhom == "update") {
			def assetEntity = AssetEntity.get(params.prevAsset)
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)
			render(template: "commentList", model: [assetCommentList: assetCommentList])
		} else {
			renderAsJson map
		}
	}

	def updateComment() {
		String tzId = userPreferenceService.timeZone
		String userDTFormat = userPreferenceService.dateFormat
		def map = commentService.saveUpdateCommentAndNotes(tzId, userDTFormat, params, false, flash)
		if (params.open == "view") {
			if (map.error) {
				flash.message = map.error
			}
			forward(action: "showComment", params: [id: params.id])
		} else if (params.view == "myTask") {
			if (map.error) {
				flash.message = map.error
			}
			forward(action: 'listComment', params: [view: params.view, tab: params.tab])
		} else if (params.open != "view") {
			renderAsJson map
		}
	}

	/* delete the comment record
	 * @param assetComment
	 * @author Lokanath
	 * @return assetCommentList
	 */
	def deleteComment() {
		// TODO - SECURITY - deleteComment - verify that the asset is part of a project that the user has the rights to delete the note
		AssetComment assetComment = AssetComment.get(params.id)
		if (assetComment) {
			TaskDependency.executeUpdate('delete TaskDependency where assetComment=:assetComment OR predecessor=:assetComment',
					[assetComment: assetComment])
			assetComment.delete()
		}

		// TODO - deleteComment - Need to be fixed to handle non-asset type comments
		def assetCommentsList = []
		if (params.assetEntity) {
			AssetComment.findAllByAssetEntityAndIdNotEqual(AssetEntity.load(params.assetEntity), params.id).each {
				assetCommentsList << [commentInstance: it, assetEntityId: it.assetEntity.id]
			}
		}
		renderAsJson assetCommentsList
	}

	/*----------------------------------
	 * @author: Lokanath Redy
	 * @param : fromState and toState
	 * @return: boolean value to validate comment field
	 *---------------------------------*/
	def retrieveFlag() {
		def moveBundleInstance = MoveBundle.get(params.moveBundle)
		def toState = params.toState
		def fromState = params.fromState
		def status = []
		def flag = stateEngineService.getFlags(moveBundleInstance.workflowCode,"SUPERVISOR", fromState, toState)
		if (flag?.contains("comment") || flag?.contains("issue")) {
			status << ['status':'true']
		}
		renderAsJson status
	}

	/*-----------------------------------------
	 *@param : state value
	 *@return: List of valid stated for param state
	 *----------------------------------------*/
	def retrieveStates(def state,def assetEntity) {
		def stateIdList = []
		def validStates
		if (state) {
			validStates = stateEngineService.getTasks(assetEntity.moveBundle.workflowCode,"SUPERVISOR", state)
		} else {
			validStates = ["Ready"]
			//stateEngineService.getTasks("STD_PROCESS","TASK_NAME")
		}
		validStates.each {
			stateIdList<<stateEngineService.getStateIdAsInt(assetEntity.moveBundle.workflowCode,it)
		}
		return stateIdList
	}

	def retrieveProgress() {
		def importedData
		def progressData = []
		Long batchId = getBatchId()
		Integer total = getTotalAssets()
		if (batchId) {
			importedData = jdbcTemplate.queryForList('''
				select count(distinct row_id) as rows
				from data_transfer_value
				where data_transfer_batch_id=?''', batchId).rows
		}
		progressData << [imported: importedData, total: total]
		renderAsJson progressData
	}

	/**
	 * Presents the CRUD form for a new Device entry form
	 * @param params.redirectTo - used to redirect the user back to the appropriate page afterward
	 * @return : render to create page based on condition as if redirectTo is assetAudit then redirecting
	 * to auditCreate view
	 */
	def create() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def (device, model) = assetEntityService.getDeviceAndModelForCreate(project, params)

		model.action = 'save'
		model.whom = 'Device'

		// TODO : JPM 10/2014 : I'm guessing this is needed to make the save action work correctly
		model.redirectTo = params.redirectTo ?: 'list'

		if (params.redirectTo == "assetAudit") {
			model.source = params.source
			model.assetType = params.assetType
			render(template: "createAuditDetails", model: model)
		} else {
			// model.each { n,v -> println "$n:\t$v"}
			render(view: 'createEdit', model: model)
		}
	}

	/**
	 * Render the edit view.
	 * @param : redirectTo
	 * @return : render to edit page based on condition as if 'redirectTo' is roomAudit then redirecting
	 * to auditEdit view
	 */
	def edit() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def (device, Map model) = assetEntityService.getDeviceModelForEdit(project, params.id, params)

		if (!device) {
			render '<span class="error">Unable to find asset to edit</span>'
			return
		}

		if (params.redirectTo == "roomAudit") {
			// TODO : JPM 9/2014 : Need to determine the assetType
			model.assetType = params.assetType
			model.source = params.source

			render(template: "auditEdit", model: model)
			return
		}

		model.action = 'update'

		// model.each { n,v -> println "$n:\t$v"}
		render (view: 'createEdit', 'model' : model)
	}

	/**
	 * Used to create and save a new device and associated dependencies. Upon success or failure it will redirect the
	 * user to the place that they came from based on the params.redirectTo param. The return content varies based on that
	 * param as well.
	 */
	def save() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, deviceService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.AE?.JQ_FILTERS = params
	}

	/**
	 * Update an AssetEntity.
	 * @param redirectTo : a flag to redirect view to page after update
	 * @param id : id of assetEntity
	 * @return : render to appropriate view
	 */
	def update() {
		String errorMsg = controllerService.saveUpdateAssetHandler(this, deviceService, params)
		if (errorMsg) session.AE?.JQ_FILTERS = params

		session.AE?.JQ_FILTERS = params
	}

	/**
	* Renders the detail of an AssetEntity
	*/
	def show() {

		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def assetId = params.id
		def assetEntity = controllerService.getAssetForPage(this, project, AssetClass.DEVICE, assetId)

		if (!assetEntity) {
			flash.message = "Unable to find asset within current project with id $params.id"
			log.warn "show - asset id ($params.id) not found for project ($project.id) by user $securityService.currentUsername"
			renderAsJson(errMsg: flash.message)
		} else {
			def model = deviceService.getModelForShow(project, assetEntity, params)
			if (!model) {
				render "Unable to load specified asset"
				return
			}

			if (params.redirectTo == "roomAudit") {
				// model.source = params.source
				// model.assetType = params.assetType
				render(template: "auditDetails", model: model)
			}

			//model.each { n,v -> println "$n:\t$v"}
			return model
		}
	}

	/**
	 * Get Manufacturers ordered by manufacturer name display at
	 * assetEntity CRUD and AssetAudit CRUD
	 * @param assetType : requested assetType for which we need to get manufacturer list
	 * @return : render to manufacturerView
	 */
	def retrieveManufacturersList() {
		def assetType = params.assetType
		def manufacturers = Model.executeQuery("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
		def prefVal =  userPreferenceService.getPreference(PREF.LAST_MANUFACTURER)
		def selectedManu = prefVal ? Manufacturer.findByName(prefVal)?.id : null
		render(view: 'manufacturerView', model: [manufacturers: manufacturers, selectedManu: selectedManu, forWhom: params.forWhom])
	}

	/**
	 * Used to set showAllAssetTasks preference, which is used to show all or hide the inactive tasks
	 */
	def setShowAllPreference() {
		userPreferenceService.setPreference(PREF.SHOW_ALL_ASSET_TASKS, params.selected == '1')
		render true
	}

	/**
	 * Used to set showAllAssetTasks preference, which is used to show all or hide the inactive tasks
	 */
	def setViewUnpublishedPreference () {
		userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED,
			params.viewUnpublished == '1' || params.viewUnpublished == 'true')
		render true
	}

	/**
	 * Get Models to display at assetEntity CRUD and AssetAudit CRUD
	 * @param assetType : requested assetType for which we need to get manufacturer list
	 * @return : render to manufacturerView
	 */
	def retrieveModelsList() {
		def manufacturer = params.manufacturer
		def models=[]
		if (manufacturer!="null") {
			def manufacturerInstance = Manufacturer.read(manufacturer)
			models=assetEntityService.getModelSortedByStatus(manufacturerInstance)
		}
		render (view :'_deviceModelSelect', model:[models : models, forWhom:params.forWhom])
	}

	/**
	 * Used to generate the List for Task Manager, which leverages a shared closure with listComment
	 */
	def listTasks() {
		securityService.requirePermission 'ViewTaskManager'

		Project project = controllerService.getProjectForPage(this, 'to view Tasks')
		if (!project) return

		try {
			if (params.containsKey('viewUnpublished') && params.viewUnpublished in ['0', '1']) {
				userPreferenceService.setPreference(PREF.VIEW_UNPUBLISHED, params.viewUnpublished == '1')
			}

			params.commentType = AssetCommentType.TASK

			if (params.initSession) {
				session.TASK = [:]
			}

			List<MoveEvent> moveEvents = MoveEvent.findAllByProject(project)
			def filters = session.TASK?.JQ_FILTERS

			// Deal with the parameters
			def taskPref = assetEntityService.getExistingPref('Task_Columns')
			def assetCommentFields = AssetComment.getTaskCustomizeFieldAndLabel()
			def modelPref = [:]
			taskPref.each { key, value -> modelPref[key] = assetCommentFields[value] }
			int filterEvent = params.moveEvent ?: 0
			def moveEvent

			if (params.containsKey("justRemaining")) {
				userPreferenceService.setPreference(PREF.JUST_REMAINING, params.justRemaining)
			}
			if (params.moveEvent) {
				// zero (0) = All events
				// log.info "listCommentsOrTasks: Handling MoveEvent based on params $params.moveEvent"
				if (params.moveEvent != '0') {
					moveEvent = MoveEvent.findByIdAndProject(params.moveEvent,project)
					if (!moveEvent) {
						log.warn "listCommentsOrTasks: $person tried to access moveEvent $params.moveEvent that was not found in project $project.id"
					}
				}
			} else {
				// Try getting the move Event from the user's session
				def moveEventId = userPreferenceService.moveEventId
				// log.info "listCommentsOrTasks: getting MOVE_EVENT preference $moveEventId for $person"
				if (moveEventId) {
					moveEvent = MoveEvent.findByIdAndProject(moveEventId,project)
				}
			}
			if (moveEvent && params.section != 'dashBoard') {
				// Add filter to SQL statement and update the user's preferences
				userPreferenceService.setMoveEventId moveEvent.id
				filterEvent = moveEvent.id
			} else {
				userPreferenceService.removePreference(PREF.MOVE_EVENT)
			}
			def justRemaining = userPreferenceService.getPreference(PREF.JUST_REMAINING) ?: "1"
			// Set the Checkbox values to that which were submitted or default if we're coming into the list for the first time
			def justMyTasks = params.containsKey('justMyTasks') ? params.justMyTasks : "0"
			boolean viewUnpublished = (userPreferenceService.getPreference(PREF.VIEW_UNPUBLISHED) == 'true') ? '1' : '0'
			def timeToRefresh = userPreferenceService.getPreference(PREF.TASKMGR_REFRESH)
			def entities = assetEntityService.entityInfo(project)
			def moveBundleList = MoveBundle.findAllByProject(project, [sort: 'name'])
			def companiesList = partyRelationshipService.getCompaniesList()
			def role = filters?.role ?: params.role ?: ''
			return [timeToUpdate: timeToRefresh ?: 60, servers: entities.servers, applications: entities.applications,
			        dbs: entities.dbs, files: entities.files, networks: entities.networks, moveEvents:moveEvents,
			        dependencyType: entities.dependencyType, dependencyStatus: entities.dependencyStatus,
			        assetDependency: new AssetDependency(), filterEvent: filterEvent, justRemaining: justRemaining,
			        justMyTasks: justMyTasks, filter: params.filter, comment: filters?.comment ?:'', role: role,
			        taskNumber: filters?.taskNumber ?:'', assetName: filters?.assetEntity ?:'', modelPref: modelPref,
			        assetType: filters?.assetType ?:'', dueDate: filters?.dueDate ?:'', status: filters?.status ?:'',
			        assignedTo: filters?.assignedTo ?:'', category: filters?.category ?:'', moveEvent: moveEvent,
			        moveBundleList: moveBundleList, viewUnpublished: viewUnpublished, taskPref: taskPref,
			        staffRoles: taskService.getTeamRolesForTasks(), attributesList: assetCommentFields.keySet().sort(),
			        //staffRoles: taskService.getRolesForStaff(),
			        sizePref: userPreferenceService.getPreference(PREF.ASSET_LIST_SIZE) ?: '25', status: params.status,
			        partyGroupList: companiesList, company: project.client, step: params.step]
		} catch (RuntimeException e) {
			log.error e.message, e
			response.sendError(401, "Unauthorized Error")
		}
	}

	/**
	 * Generates the List of Comments, which leverages a shared closeure with the above listTasks controller.
	 */
	def listComment() {
		Project project = controllerService.getProjectForPage(this, 'to view Comments')
		if (!project) return

		def entities = assetEntityService.entityInfo(project)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		boolean canEditComments = securityService.hasPermission('AssetEdit')

		[rediectTo: 'comment', servers: entities.servers, applications: entities.applications, dbs: entities.dbs,
		 files: entities.files, dependencyType: entities.dependencyType, dependencyStatus: entities.dependencyStatus,
		 assetDependency: new AssetDependency(), moveBundleList: moveBundleList, canEditComments: canEditComments]
	}

	/**
	 * Used to generate list of comments using jqgrid
	 * @return : list of tasks as JSON
	 */
	def listCommentJson() {
		String sortIndex = params.sidx ?: 'lastUpdated'
		String sortOrder = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows

		Project project = securityService.userCurrentProject
		List<Date> lastUpdatedTime = params.lastUpdated ? AssetComment.executeQuery('''
			select lastUpdated from AssetComment
			where project=:project
			  and commentType=:comment
			  and str(lastUpdated) like :lastUpdated
	  ''', [project: project, comment: AssetCommentType.COMMENT, lastUpdated: '%' + params.lastUpdated + '%']) : []

		def assetCommentList = AssetComment.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			eq("commentType", AssetCommentType.COMMENT)
			createAlias("assetEntity","ae")
			if (params.comment) {
				ilike('comment', "%$params.comment%")
			}
			if (params.commentType) {
				ilike('commentType', "%$params.commentType%")
			}
			if (params.category) {
				ilike('category', "%$params.category%")
			}
			if (lastUpdatedTime) {
				'in'('lastUpdated',lastUpdatedTime)
			}
			if (params.assetType) {
				ilike('ae.assetType',"%$params.assetType%")
			}
			if (params.assetName) {
				ilike('ae.assetName',"%$params.assetName%")
			}
			String sid = sortIndex == 'assetName' || sortIndex  == 'assetType' ? "ae.$sortIndex" : sortIndex
			order(new Order(sid, sortOrder == 'asc').ignoreCase())
		}

		int totalRows = assetCommentList.totalCount
		int numberOfPages = Math.ceil(totalRows / maxRows)

		def results = assetCommentList?.collect {
			[id: it.id,
			 cell: ['',
			        StringUtil.ellipsis(it.comment ?: '', 50).replace("\n", ""),
			        TimeUtil.formatDate(it.lastUpdated),
			        it.commentType,
			        it.assetEntity?.assetName ?:'',
			        it.assetEntity?.assetType ?:'',
			        it.category,
			        it.assetEntity?.id,
			        it.assetEntity?.assetClass?.toString()
				]
			]
		}

		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	/**
	 * This will be called from TaskManager screen to load jqgrid
	 * @return : list of tasks as JSON
	 */
	def listTaskJSON() {
		String sortIndex =  params.sidx ?: session.TASK?.JQ_FILTERS?.sidx
		String sortOrder =  params.sord ?: session.TASK?.JQ_FILTERS?.sord
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows

		userPreferenceService.setPreference(PREF.ASSET_LIST_SIZE, maxRows)

		Project project = securityService.userCurrentProject
		def today = new Date()
		def moveEvent
		if (params.moveEvent) {
			// zero (0) = All events
			// log.info "listCommentsOrTasks: Handling MoveEvent based on params $params.moveEvent"
			if (params.moveEvent != '0') {
				moveEvent = MoveEvent.findByIdAndProject(params.moveEvent,project)
				if (!moveEvent) {
					log.warn "listCommentsOrTasks: $securityService.currentUsername tried to access moveEvent $params.moveEvent that was not found in project $project.id"
				}
			}
		} else {
			// Try getting the move Event from the user's session
			def moveEventId = userPreferenceService.moveEventId
			// log.info "listCommentsOrTasks: getting MOVE_EVENT preference $moveEventId for $securityService.currentUsername"
			if (moveEventId) {
				moveEvent = MoveEvent.findByIdAndProject(moveEventId,project)
			}
		}
		if (moveEvent) {
			userPreferenceService.setMoveEventId moveEvent.id
		}

		def assetType = params.filter ? ApplicationConstants.assetFilters[params.filter ] : []

		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%$params.moveBundle%", project) : []
		def models = params.model ? Model.findAllByModelNameIlike("%$params.model%") : []

		def taskNumbers = params.taskNumber ? AssetComment.findAll("from AssetComment where project =:project \
			and taskNumber like '%$params.taskNumber%'",[project:project])?.taskNumber : []

		def durations = params.duration ? AssetComment.findAll("from AssetComment where project =:project \
			and duration like '%$params.duration%'",[project:project])?.duration : []

		boolean viewUnpublished = securityService.viewUnpublished()

		// TODO TM-2515 - SHOULD NOT need ANY of these queries as they should be implemented directly into the criteria
		def dates = params.dueDate ? AssetComment.findAll("from AssetComment where project =:project and dueDate like '%$params.dueDate%' ",[project:project])?.dueDate : []
		def estStartdates = params.estStart ? AssetComment.findAll("from AssetComment where project=:project and estStart like '%$params.estStart%' ",[project:project])?.estStart : []
		def actStartdates = params.actStart ? AssetComment.findAll("from AssetComment where project=:project and actStart like '%$params.actStart%' ",[project:project])?.actStart : []
		def dateCreateddates = params.dateCreated ? AssetComment.findAll("from AssetComment where project=:project and dateCreated like '%$params.dateCreated%' ",[project:project])?.dateCreated : []
		def dateResolveddates = params.dateResolved ? AssetComment.findAll("from AssetComment where project=:project and dateResolved like '%$params.dateResolved%' ",[project:project])?.dateResolved : []
		def estFinishdates = params.estFinish ? AssetComment.findAll("from AssetComment where project=:project and estFinish like '%$params.estFinish%' ",[project:project])?.estFinish : []

		// TODO TM-2515 - ONLY do the lookups if params used by the queries are populated
		def assigned = params.assignedTo ? Person.findAllByFirstNameIlikeOrLastNameIlike("%$params.assignedTo%","%$params.assignedTo%") : []
		def createdBy = params.createdBy ? Person.findAllByFirstNameIlikeOrLastNameIlike("%$params.createdBy%","%$params.createdBy%") : []
		def resolvedBy = params.resolvedBy ? Person.findAllByFirstNameIlikeOrLastNameIlike("%$params.resolvedBy%","%$params.resolvedBy%") : []

		def tasks = AssetComment.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			eq("commentType", AssetCommentType.TASK)
			createAlias('assetEntity', 'assetEntity', CriteriaSpecification.LEFT_JOIN)
			createAlias("moveEvent", "moveEvent", CriteriaSpecification.LEFT_JOIN)
			createAlias("assetEntity.moveBundle", "moveBundle", CriteriaSpecification.LEFT_JOIN)
			if (params.step) {
				createAlias("workflowTransition", "workflowTransition", CriteriaSpecification.LEFT_JOIN)
				eq("workflowTransition.id", params.step.toLong())
			}
			if (!viewUnpublished) {
				eq("isPublished", true)
			}
			if (params.assetType) {
				ilike('assetEntity.assetType', "%$params.assetType%")
			}
			if (params.assetName) {
				ilike('assetEntity.assetName', "%$params.assetName%")
			}
			if (params.comment) {
				ilike('comment', "%$params.comment%")
			}
			if (params.status) {
				ilike('status', "%$params.status%")
			}
			if (params.role) {
				ilike('role', "%$params.role%")
			}
			if (params.commentType) {
				ilike('commentType', "%$params.commentType%")
			}
			if (params.displayOption) {
				ilike('displayOption', "%$params.displayOption%")
			}
			if (durations) {
				'in'('duration', durations)
			}
			if (params.durationScale) {
				ilike('durationScale', "%$params.durationScale%")
			}
			if (params.category) {
				ilike('category', "%$params.category%")
			}
			if (params.attribute) {
				ilike('attribute', "%$params.attribute%")
			}
			if (params.autoGenerated) {
				eq('autoGenerated', params.autoGenerated)
			}
			if (params.event) {
				ilike("moveEvent.name", "%$params.event%")
			}
			if (params.bundle) {
				ilike("moveBundle.name", "%$params.bundle%")
			}
			if (taskNumbers) {
				'in'('taskNumber', taskNumbers)
			}

			if (params.isResolved?.isNumber()) {
				eq('isResolved', params.int('isResolved'))
			}

			if (params.hardAssigned?.isNumber()) {
				eq('hardAssigned', params.int('hardAssigned'))
			}

			if (dates) {
				and {
					or {
						'in'('dueDate', dates)
						'in'('estFinish', dates)
					}
				}
			}

			if (estStartdates) {
				'in'('estStart',estStartdates)
			}
			if (actStartdates) {
				'in'('actStart',actStartdates)
			}
			if (estFinishdates) {
				'in'('estFinish',estFinishdates)
			}
			if (dateCreateddates) {
				'in'('dateCreated',dateCreateddates)
			}
			if (dateResolveddates) {
				'in'('dateResolved',dateResolveddates)
			}

			if (createdBy) {
				'in'('createdBy', createdBy)
			}
			if (resolvedBy) {
				'in'('createdBy', resolvedBy)
			}
			if (assigned) {
				'in'('assignedTo', assigned)
			}

			if (sortIndex && sortOrder) {
				String sortIdx
				switch(sortIndex) {
					case "assetName":
					case "assetType":
						sortIdx = "assetEntity." + sortIndex
						break
					case "bundle":
						sortIdx = "moveBundle.name"
						break
					case "event":
						sortIdx = "moveEvent.name"
						break
					default:
						sortIdx = sortIndex
						break
				}
///				order((sortOrder == 'asc' ? Order.asc(sortIndex) : Order.desc(sortIndex)).ignoreCase())

			} else {
				and {
					order('score','desc')
					order('taskNumber','asc')
					order('dueDate','asc')
					order('dateCreated','desc')
				}
			}
			if (moveEvent) {
				eq("moveEvent", moveEvent)
			}

			if (params.justRemaining == "1") {
				ne("status", AssetCommentStatus.COMPLETED)
			}
			if (params.justMyTasks == "1") {
				eq("assignedTo", securityService.loadCurrentPerson())
			}
			switch(params.filter) {
				case "openIssue" :
					'in'('category', AssetComment.discoveryCategories)
					break
				case "dueOpenIssue":
					'in'('category', AssetComment.discoveryCategories)
					lt('dueDate',today)
					break
				case "analysisIssue" :
					eq("status", AssetCommentStatus.READY)
					'in'('category', AssetComment.planningCategories)
					break
				case "generalOverDue" :
					'in'('category', AssetComment.planningCategories)
					lt('dueDate',today)
					break
			}
		}

		def createJsonTime = new Date()

		def totalRows = tasks.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)
		def updatedTime
		def updatedClass
		def dueClass
		def nowGMT = TimeUtil.nowGMT()
		def taskPref = assetEntityService.getExistingPref('Task_Columns')

		def results = tasks?.collect {
			def isRunbookTask = it.isRunbookTask()
			updatedTime =  isRunbookTask ? it.statusUpdated : it.lastUpdated

			def elapsed = TimeUtil.elapsed(it.statusUpdated, nowGMT)
			def elapsedSec = elapsed.toMilliseconds() / 1000

			// clear out the CSS classes for overDue/Updated
			updatedClass = dueClass = ''

			if (it.status == AssetCommentStatus.READY) {
				if (elapsedSec >= 600) {
					updatedClass = 'task_late'
				} else if (elapsedSec >= 300) {
					updatedClass = 'task_tardy'
				}
			} else if (it.status == AssetCommentStatus.STARTED) {
				def dueInSecs = elapsedSec - (it.duration ?: 0) * 60
				if (dueInSecs >= 600) {
					updatedClass='task_late'
				} else if (dueInSecs >= 300) {
					updatedClass='task_tardy'
				}
			}

			if (it.estFinish) {
				elapsed = TimeUtil.elapsed(it.estFinish, nowGMT)
				elapsedSec = elapsed.toMilliseconds() / 1000
				if (elapsedSec > 300) {
					dueClass = 'task_overdue'
				}
			}

			String dueDate = ''
			if (isRunbookTask) {
				dueDate = TimeUtil.formatDateTime(it.estFinish, TimeUtil.FORMAT_DATE_TIME_4)
			} else {
				dueDate = TimeUtil.formatDate(it.dueDate)
			}

			def deps = TaskDependency.findAllByPredecessor(it)
			def depCount = 0
			deps.each {
				if (viewUnpublished || (it.assetComment?.isPublished && it.predecessor?.isPublished))
					++depCount
			}

			// Have the dependency count be a link to the Task Neighborhood graph if there are dependencies
			def nGraphUrl = depCount == 0 ? depCount : '<a href="' + createLink(controller:'task', action:'taskGraph') +
					'?neighborhoodTaskId=' + it.id + '" target="_blank",>' + depCount + '</a>'

			def status = it.status
			def userSelectedCols = []
			(1..5).each { colId ->
				def value = taskManagerValues(taskPref[colId.toString()], it)
				userSelectedCols << (value?.getClass()?.isEnum() ? value?.value() : value)
			}

			def instructionsLinkURL
			if (HtmlUtil.isMarkupURL(it.instructionsLink)) {
				instructionsLinkURL = HtmlUtil.parseMarkupURL(it.instructionsLink)[1]
			}
			else {
				instructionsLinkURL = it.instructionsLink
			}

			[ cell: [
					'',
					it.taskNumber,
					it.comment,
					userSelectedCols[0], // taskManagerValues(taskPref["1"],it),
					userSelectedCols[1], // taskManagerValues(taskPref["2"],it),
					updatedTime ? TimeUtil.ago(updatedTime, TimeUtil.nowGMT()) : '',
					dueDate,
					status ?: '',
					userSelectedCols[2], // taskManagerValues(taskPref["3"],it),
					userSelectedCols[3], // taskManagerValues(taskPref["4"],it),
					userSelectedCols[4], // taskManagerValues(taskPref["5"],it),
					nGraphUrl,
					it.score ?: 0,
					status ? 'task_' + it.status.toLowerCase() : 'task_na',
					updatedClass,
					dueClass,
					it.assetEntity?.id, // 16
					it.assetEntity?.assetType, // 17
					it.assetEntity?.assetClass?.toString(), // 18
					instructionsLinkURL // 19
			],
			  id:it.id
			]
		}

		// If sessions variables exists, set them with params and sort
		session.TASK?.JQ_FILTERS = params
		session.TASK?.JQ_FILTERS?.sidx = sortIndex
		session.TASK?.JQ_FILTERS?.sord = sortOrder

		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	/**
	 * Get assetColumn value based on field name. .
	 */
	private taskManagerValues(value, task) {
		def result
		switch (value) {
			case 'assetName': result = task.assetEntity?.assetName; break
			case 'assetType': result = task.assetEntity?.assetType; break
			case 'assignedTo': result = (task.hardAssigned ? '*' + task.assignedTo : '') + (task.assignedTo?.toString() ?: ''); break
			case 'resolvedBy': result = task.resolvedBy?.toString() ?: ''; break
			case 'createdBy': result = task.createdBy?.toString() ?: ''; break
			case ~/statusUpdated|estFinish|dateCreated|dateResolved|estStart|actStart/:
				result = TimeUtil.formatDate(task[value])
			break
			case "event": result = task.moveEvent?.name; break
			case "bundle": result = task.assetEntity?.moveBundle?.name; break
			default: result = task[value]
		}
		return result
	}

	/**
	 * Get AssetOptions by type to display at admin's AssetOption page .
	 */
	def assetOptions() {
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		def environment = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		[planStatusOptions: planStatusOptions, priorityOption: priorityOption, dependencyType: dependencyType,
		 dependencyStatus: dependencyStatus, environment: environment]
	}

	/**
	 * Save AssetOptions by type to display at admin's AssetOption page .
	 */
	def saveAssetoptions() {
		AssetOptions assetOption = new AssetOptions()
		switch(params.assetOptionType) {
			case 'planStatus':
				assetOption.type = AssetOptions.AssetOptionsType.STATUS_OPTION
				assetOption.value = params.planStatus
				break
			case 'Priority':
				assetOption.type = AssetOptions.AssetOptionsType.PRIORITY_OPTION
				assetOption.value = params.priorityOption
				break
			case 'dependency':
				assetOption.type = AssetOptions.AssetOptionsType.DEPENDENCY_TYPE
				assetOption.value = params.dependencyType
				break
			case 'environment':
				assetOption.type = AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION
				assetOption.value = params.environment
				break
			default:
				assetOption.type = AssetOptions.AssetOptionsType.DEPENDENCY_STATUS
				assetOption.value = params.dependencyStatus
		}

		if (!assetOption.save(flush:true)) {
			assetOption.errors.allErrors.each { log.error  it }
		}

		renderAsJson(id: assetOption.id)
	}

	/**
	 * Deletes AssetOptions by type from admin's AssetOption page .
	 */
	def deleteAssetOptions() {
		String idParamName
		switch(params.assetOptionType) {
			case 'planStatus':  idParamName = 'assetStatusId'; break
			case 'Priority':    idParamName = 'priorityId'; break
			case 'dependency':  idParamName = 'dependecyId'; break
			case 'environment': idParamName = 'environmentId'; break
			default:            idParamName = 'dependecyId'; break
		}

		AssetOptions assetOption = AssetOptions.get(params[idParamName])
		assetOption.delete(flush: true)
		render assetOption.id
	}

	/**
	 * Render Summary of assigned and unassgined assets.
	 */
	def assetSummary() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		int totalAsset = 0
		int totalPhysical = 0
		int totalApplication = 0
		int totalDatabase = 0
		int totalFiles = 0

		List assetSummaryList = MoveBundle.findAllByProject(project, [sort: 'name']).collect { MoveBundle moveBundle ->
			int physicalCount = AssetEntity.executeQuery('''
				select count(*) from AssetEntity
				where moveBundle=:mb
				  and assetClass=:ac
				  and coalesce(assetType) not in (:at)
			''', [mb: moveBundle, ac: AssetClass.DEVICE, at: AssetType.virtualServerTypes])[0]
			int assetCount = AssetEntity.countByMoveBundleAndAssetTypeInList(moveBundle, AssetType.serverTypes, params)
			int applicationCount = Application.countByMoveBundle(moveBundle)
			int databaseCount = Database.countByMoveBundle(moveBundle)
			int filesCount = Files.countByMoveBundle(moveBundle)

			totalAsset += assetCount
			totalPhysical += physicalCount
			totalApplication += applicationCount
			totalDatabase += databaseCount
			totalFiles += filesCount

			[name: moveBundle, assetCount: assetCount, applicationCount: applicationCount, physicalCount: physicalCount,
			 databaseCount: databaseCount, filesCount: filesCount, id: moveBundle.id]
		}

		[assetSummaryList: assetSummaryList, totalAsset: totalAsset, totalApplication: totalApplication,
		 totalDatabase: totalDatabase,totalPhysical: totalPhysical, totalFiles: totalFiles]
	}

	/**
	 * Used by the dependency console to load up the individual tabs for a dependency bundle
	 * @param String entity - the entity type to view (server,database,file,app)
	 * @param Integer dependencyBundle - the dependency bundle ID
	 * @return String HTML representing the page
	 */
	def retrieveLists() {

		def start = new Date()
		session.removeAttribute('assetDependentlist')

		Project project = securityService.userCurrentProject

		def depGroups = []
		def depGroupsJson = session.getAttribute('Dep_Groups')
		if (depGroupsJson) {
			depGroups = JSON.parse(depGroupsJson)
		}
		if (depGroups.size() == 0) {
			depGroups = [-1]
		}

		def assetDependentlist
		String selectionQuery = ''
		String mapQuery
		String nodesQuery
		boolean multiple = false
		if (params.dependencyBundle?.isNumber()) {
			// Get just the assets for a particular dependency group id
			mapQuery = " AND deps.bundle = $params.dependencyBundle"
			depGroups = [params.dependencyBundle]
			nodesQuery = " AND dependency_bundle = $params.dependencyBundle "
		} else if (params.dependencyBundle == 'onePlus') {
			// Get all the groups other than zero - these are the groups that have interdependencies
			multiple = true
			mapQuery = " AND deps.bundle in (${WebUtil.listAsMultiValueString(depGroups-[0])})"
			depGroups = depGroups-[0]
			nodesQuery = " AND dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups-[0])})"
		} else {
			// Get 'all' assets that were bundled
			multiple = true
			mapQuery = " AND deps.bundle in (${WebUtil.listAsMultiValueString(depGroups)})"
			nodesQuery = " AND dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups)})"
		}
		def queryFordepsList = """
			SELECT DISTINCT deps.asset_id AS assetId, ae.asset_name AS assetName, deps.dependency_bundle AS bundle, mb.move_bundle_id AS moveBundleId, mb.name AS moveBundleName,
			ae.asset_type AS type, ae.asset_class AS assetClass, me.move_event_id AS moveEvent, me.name AS eventName, app.criticality AS criticality,
			if (ac_task.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus, if (ac_comment.comment_type IS NULL, 'noComments','comments') AS commentsStatus
			FROM (
				SELECT * FROM tdstm.asset_dependency_bundle
				WHERE project_id=$project.id $nodesQuery
				ORDER BY dependency_bundle
			) AS deps
			LEFT OUTER JOIN asset_entity ae ON ae.asset_entity_id = deps.asset_id
			LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id = ae.move_bundle_id
			LEFT OUTER JOIN move_event me ON me.move_event_id = mb.move_event_id
			LEFT OUTER JOIN application app ON app.app_id = ae.asset_entity_id
			LEFT OUTER JOIN asset_comment ac_task ON ac_task.asset_entity_id=ae.asset_entity_id AND ac_task.comment_type = 'issue'
			LEFT OUTER JOIN asset_comment ac_comment ON ac_comment.asset_entity_id=ae.asset_entity_id AND ac_comment.comment_type = 'comment'
			"""

		assetDependentlist = jdbcTemplate.queryForList(queryFordepsList)
		//log.error "getLists() : query for assetDependentlist took ${TimeUtil.elapsed(start)}"
		// Took 0.296 seconds

		// Save the group id into the session as it is used to redirect the user back after updating assets or doing assignments
		session.setAttribute('dependencyBundle', params.dependencyBundle)
		// TODO : This pig of a list should NOT be stored into the session and the logic needs to be reworked
		//session.setAttribute('assetDependentlist', assetDependentlist)

		def depMap
		String sortOn = params.sort ?: 'assetName'
		String orderBy = params.orderBy ?: 'asc'
		def asset

		if (params.entity != 'graph') {
			depMap = moveBundleService.dependencyConsoleMap(project, null, null,
				params.dependencyBundle != "null" ? params.dependencyBundle : "all")
			depMap = depMap.gridStats
		}
		else {
			depMap = moveBundleService.dependencyConsoleMap(project, null, null,
				params.dependencyBundle != "null" ? params.dependencyBundle : "all", true)
		}
		def model = [entity: params.entity ?: 'apps', stats: depMap]

		model.dependencyBundle = params.dependencyBundle
		model.asset = params.entity
		model.orderBy = orderBy
		model.sortBy = sortOn
		model.haveAssetEditPerm = securityService.hasPermission('AssetEdit')

		// Switch on the desired entity type to be shown, and render the page for that type
		switch(params.entity) {
			case "all" :
				def assetList = []

				assetDependentlist.each {
					asset = AssetEntity.read(it.assetId)
					String type = it.assetClass == AssetClass.STORAGE.toString() ? 'Logical Storage' : asset.assetType
					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus, type: type]
				}
				assetList = sortAssetByColumn(assetList, sortOn != "type" ? (sortOn) : "assetType", orderBy)
				model.assetList = assetList
				model.assetListSize = assetDependentlist.size()

				render(template:"allList", model:model)
				break

			case "apps" :
				def applicationList = assetDependentlist.findAll { it.type ==  AssetType.APPLICATION.toString() }
				def appList = []

				applicationList.each {
					asset = Application.read(it.assetId)

					appList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus]
				}
				appList = sortAssetByColumn(appList,sortOn,orderBy)
				model.appList = appList
				model.applicationListSize = applicationList.size()

				render(template:"appList", model:model)
				break

			case "server":
				def assetList = []
				def assetEntityList = assetDependentlist.findAll { AssetType.allServerTypes.contains(it.type) }

				assetEntityList.each {
					asset = AssetEntity.read(it.assetId)
					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus, locRoom:asset.roomSource]
				}
				assetList = sortAssetByColumn(assetList,sortOn,orderBy)
				model.assetList = assetList
				model.assetEntityListSize = assetEntityList.size()
				render(template:"assetList", model:model)
				break

			case "database" :
				def databaseList = assetDependentlist.findAll{it.type == AssetType.DATABASE.toString() }
				def dbList = []

				databaseList.each {
					asset = Database.read(it.assetId)

					dbList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus]
				}
				dbList = sortAssetByColumn(dbList,sortOn,orderBy)
				model.databaseList = dbList
				model.dbDependentListSize = databaseList.size()
				render(template:'dbList', model:model)
				break

			case "files" :
				def filesList = assetDependentlist.findAll { it.assetClass == AssetClass.STORAGE.toString() ||
				                                             (it.assetClass == AssetClass.DEVICE.toString() &&
						                                             it.type in AssetType.storageTypes)}
				def assetList = []
				def fileList = []

				filesList.each {
					asset = AssetEntity.read(it.assetId)

					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus]
				}

				assetList.each {
					def item = [id:it.asset.id, assetName:it.asset.assetName, assetType:it.asset.assetType,
						validation:it.asset.validation, moveBundle:it.asset.moveBundle, planStatus:it.asset.planStatus,
						depToResolve:it.asset.depToResolve, depToConflic:it.asset.depToConflict, assetClass:it.asset.assetClass]

					// check if the object is a logical or physical strage
					if (it.asset.assetClass.toString() == 'DEVICE') {
						item.fileFormat = ''
						item.storageType = 'Server'
						item.type = item.assetType
					} else {
						item.fileFormat = Files.read(it.asset.id)?.fileFormat ?: ''
						item.storageType = 'Files'
						item.type = 'Logical'
					}

					fileList << [asset: item, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus]
				}

				fileList = sortAssetByColumn(fileList,sortOn,orderBy)
				model.filesList = fileList
				model.filesDependentListSize = fileList.size()

				render(template: 'filesList', model: model)
				break

			case "graph" :
				def moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project, true)
				Set uniqueMoveEventList = moveBundleList.moveEvent
				uniqueMoveEventList.remove(null)
				List moveEventList = uniqueMoveEventList.toList()
				moveEventList.sort { it?.name }

				def defaultPrefs = [colorBy: 'group', appLbl: 'true', maxEdgeCount: '4']
				def graphPrefs = userPreferenceService.getPreference(PREF.DEP_GRAPH)
				def prefsObject = graphPrefs ? JSON.parse(graphPrefs) : defaultPrefs

				// Create the Nodes
				def graphNodes = []
				def name = ''
				def shape = 'circle'
				def size = 150
				def title = ''
				def color = ''
				def type = ''
				def assetType = ''
				def assetClass = ''
				def criticalitySizes = [Minor: 150, Important: 200, Major: 325, Critical: 500]
				Map<Long, String> dependencyBundleMap = new TreeMap<Long, String>()
				Map<Long, String> moveBundleMap = new TreeMap<Long, String>()
				Map<Long, String> moveEventMap = new TreeMap<Long, String>()
				def t1 = TimeUtil.elapsed(start).millis + TimeUtil.elapsed(start).seconds * 1000

				assetDependentlist.each {
					assetType = it.model?.assetType?:it.type
					assetClass = it.assetClass
					size = 150

					type = getImageName(assetClass, assetType)
					if (type == AssetType.APPLICATION.toString())
						size = it.criticality ? criticalitySizes[it.criticality] : 200

					if (!dependencyBundleMap.containsKey(it.bundle)) {
						dependencyBundleMap[it.bundle] = 'Group ' + it.bundle
					}
					if (!moveBundleMap.containsKey(it.moveBundleId))  {
						moveBundleMap[it.moveBundleId] = it.moveBundleName
					}

					String moveEventName = it.eventName ?: 'No Event'
					color = it.eventName ? 'grey' : 'red'
					long moveEventId = it.moveEvent ?: (long)0
					boolean hasMoveEvent = it.eventName

					if (!moveEventMap.containsKey(moveEventId)) {
						moveEventMap[moveEventId] = moveEventName
					}

					graphNodes << [id: it.assetId, name: it.assetName, type: type, depBundleId: it.bundle,
					               moveBundleId: it.moveBundleId, moveEventId: moveEventId, hasMoveEvent: hasMoveEvent,
					               shape: shape, size: size, title: it.assetName, color: color, dependsOn: [],
					               supports: [], assetClass: it.assetClass, cutGroup: -1]
				}

				// set the dep bundle, move bundle, and move event properties to indices
				List<Long> sortedDepBundles = dependencyBundleMap.keySet() as List
				List<Long> sortedMoveBundles = moveBundleMap.keySet() as List
				List sortedMoveEvents = moveEventMap.keySet() as List
				Map dependencyGroupIndexMap = [:]
				Map moveBundleIndexMap = [:]
				Map  moveEventIndexMap = [:]
				graphNodes.each {
					def mbid = it.depBundleId
					def index = sortedDepBundles.indexOf(mbid)
					it.depBundleIndex = index
					dependencyGroupIndexMap[index] = dependencyBundleMap[mbid]

					mbid = it.moveBundleId
					index = sortedMoveBundles.indexOf(mbid)
					it.moveBundleIndex = index
					moveBundleIndexMap[index] = moveBundleMap[mbid]

					mbid = it.moveEventId
					index = sortedMoveEvents.indexOf(mbid)
					it.moveEventIndex = index
					moveEventIndexMap[index] = moveEventMap[mbid]
				}

				// Define a map of all the options for asset types
				def assetTypes = assetEntityService.ASSET_TYPE_NAME_MAP

				// Create a seperate list of just the node ids to use while creating the links (this makes it much faster)
				def nodeIds = graphNodes*.id

				// Report the time it took to create the nodes
				def t2 = TimeUtil.elapsed(start).millis + TimeUtil.elapsed(start).seconds * 1000
				def td = t2 - t1
				float avg = 0
				if (assetDependentlist) {
					avg = td / assetDependentlist.size()
				}

				// Set the defaults map to be used in the dependeny graph
				def defaults = moveBundleService.getMapDefaults(graphNodes.size())
				if (multiple) {
					defaults.force = -200
					defaults.linkSize = 140
				}

				// Query for only the dependencies that will be shown
				def depBundle = params.dependencyBundle.isNumber() ? params.dependencyBundle : 0

				//map Groups array String values to Integer
				depGroups = NumberUtil.mapToPositiveInteger(depGroups)

				def assetDependencies = AssetDependency.executeQuery('''
					SELECT NEW MAP (ad.asset AS ASSET, ad.status AS status, ad.isFuture AS future,
					                ad.isStatusResolved AS resolved, adb1.asset.id AS assetId, adb2.asset.id AS dependentId,
					                (CASE WHEN ad.asset.moveBundle != ad.dependent.moveBundle
					                       AND ad.status in (:statuses)
					                      THEN true ELSE false
					                 END) AS bundleConflict)
					FROM AssetDependency ad, AssetDependencyBundle adb1, AssetDependencyBundle adb2, Project p
					WHERE ad.asset = adb1.asset
						AND ad.dependent = adb2.asset
						AND adb1.dependencyBundle in (:depGroups)
						AND adb2.dependencyBundle in (:depGroups)
						AND adb1.project = p
						AND adb2.project = p
						AND p.id = :projectId
					GROUP BY ad.id
				''', [
						statuses: [AssetDependencyStatus.UNKNOWN, AssetDependencyStatus.VALIDATED, AssetDependencyStatus.QUESTIONED],
						depGroups: depGroups,
						projectId: project.id
				])

				def multiCheck = new Date()

				// Create the links
				def graphLinks = []
				def linkTable = [][]
				def i = 0
				def opacity = 1
				def statusColor = 'grey'
				assetDependencies.each {
					opacity = 1
					statusColor = 'grey'
					boolean notApplicable = false
					boolean future = false
					if (!it.resolved) {
						statusColor='red'
					} else if (it.status == AssetDependencyStatus.FUTURE) {
						future = true
					} else if (!(it.status in [AssetDependencyStatus.UNKNOWN, AssetDependencyStatus.VALIDATED])) {
						notApplicable = true
					}

					def sourceIndex = nodeIds.indexOf(it.assetId)
					def targetIndex = nodeIds.indexOf(it.dependentId)
					if (sourceIndex != -1 && targetIndex != -1) {

						// check if this link is the 2nd part of a 2-way dependency
						if (!linkTable[sourceIndex]) {
							linkTable[sourceIndex] = []
						}
						linkTable[sourceIndex][targetIndex] = true
						def duplicate = (linkTable[targetIndex] && linkTable[targetIndex][sourceIndex])

						graphLinks << [id: i, source: sourceIndex, target: targetIndex, value: 2, statusColor: statusColor,
						               opacity: opacity, unresolved: !it.resolved, notApplicable: notApplicable,
						               future: future, bundleConflict: it.bundleConflict, duplicate: duplicate]
						i++
					}
				}

				// Set the dependency properties of the nodes
				graphLinks.each {
					graphNodes[it.source].dependsOn.add(it.id)
					graphNodes[it.target].supports.add(it.id)
				}

				// Create the model that will be used while rendering the page
				model.defaults = defaults
				model.defaultsJson = defaults as JSON
				model.defaultPrefs = defaultPrefs as JSON
				model.graphPrefs = prefsObject
				model.showControls = params.showControls
				model.fullscreen = params.fullscreen ?: false
				model.nodes = graphNodes as JSON
				model.links = graphLinks as JSON
				model.multiple = multiple
				model.assetTypes = assetTypes
				model.assetTypesJson = assetTypes as JSON
				model.depBundleMap = dependencyGroupIndexMap as JSON
				model.moveBundleMap = moveBundleIndexMap as JSON
				model.moveEventMap = moveEventIndexMap as JSON
				model.depGroup = params.dependencyBundle

				// Render dependency graph
				render(template:'dependencyGraph', model:model)
				break
		} // switch
		log.info "Loading dependency console took ${TimeUtil.elapsed(start)}"
	}

	// removes the user's dependency analyzer map related preferences
	def removeUserGraphPrefs () {
		userPreferenceService.removePreference(params.preferenceName ?: 'depGraph')
		render true
	}

	/**
	* Delete multiple  Assets, Apps, Databases and files .
	* @param : assetLists[]  : list of ids for which assets are requested to deleted
	*/
	def deleteBulkAsset() {
		renderAsJson(resp: assetEntityService.deleteBulkAssets(params.type, params.list("assetLists[]")))
	}

	/**
	 * Get workflowTransition select for comment id
	 * @param assetCommentId : id of assetComment
	 * @param format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return select or a JSON array
	 */
	def retrieveWorkflowTransition() {
		Project project = securityService.userCurrentProject
		def format = params.format
		def assetCommentId = params.assetCommentId
		AssetComment assetComment = AssetComment.read(assetCommentId)
		AssetEntity assetEntity = AssetEntity.get(params.assetId)
		String workflowCode = assetEntity?.moveBundle?.workflowCode ?: project.workflowCode
		Workflow workFlow = Workflow.findByProcess(workflowCode)
		List<WorkflowTransition> workFlowTransitions = WorkflowTransition.findAllByWorkflowAndCategory(workFlow, params.category)

		//def workFlowTransitions = WorkflowTransition.findAllByWorkflow(workFlow) TODO : should be removed after completion of this new feature
		if (assetEntity) {
			def existingWorkflows = assetCommentId ? AssetComment.findAllByAssetEntityAndIdNotEqual(assetEntity, assetCommentId).workflowTransition : AssetComment.findAllByAssetEntity(assetEntity).workflowTransition
			workFlowTransitions.removeAll(existingWorkflows)
		}

		if (format == 'json') {
			renderSuccessJson(workFlowTransitions.collect { [ id:it.id, name: it.name] })
		} else {
			String result = ''
			if (workFlowTransitions) {
				result = HtmlUtil.generateSelect(selectId: 'workFlowId', selectName: 'workFlow', options: workFlowTransitions,
					firstOption: [value: '', display: ''], optionKey: 'id', optionValue: 'name',
					optionSelected: assetComment?.workflowTransitionId)
			}
			render result
		}
	}

	/**
	 * Provides a SELECT control with Staff associated with a project and the assigned staff selected if task id included
	 * @param forView - The CSS ID for the SELECT control
	 * @param id - the id of the existing task (aka comment)
	 * @param format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return HTML select of staff belongs to company and TDS or a JSON array
	 */
	def updateAssignedToSelect() {

		// TODO : Need to refactor this function to use the new TaskService.assignToSelectHtml method

		Project project = securityService.userCurrentProject
		def viewId = params.forView
		def format = params.format
		def selectedId = 0
		Person person

		// Find the person assigned to existing comment or default to the current user
		if (params.containsKey('id')) {
			if (params.id && params.id != '0') {
				person = AssetComment.findByIdAndProject(params.id, project)?.assignedTo
			} else {
				person = securityService.userLoginPerson
			}
		}
		if (person) selectedId = person.id

		def projectStaff = partyRelationshipService.getProjectStaff(project.id)

		// Now morph the list into a list of name: Role names
		def list = []
		projectStaff.each {
			list << [ id:it.staff.id,
				nameRole:"${it.role.description.split(':')[1]?.trim()}: $it.staff",
				sortOn:"${it.role.description.split(':')[1]?.trim()},$it.staff.firstName $it.staff.lastName"
			]
		}
		list.sort { it.sortOn }

		if (format == 'json') {
			renderSuccessJson(list)
			return
		}

		render HtmlUtil.generateSelect(selectId: viewId, selectName: viewId, options: list, optionKey: 'id',
		                               optionValue: 'nameRole', optionSelected: selectedId,
		                               firstOption: [value: '0', display: 'Unassigned'])
	}

	def isAllowToChangeStatus() {
		def taskId = params.id
		boolean allowed = true
		if (taskId) {
			def status = AssetComment.read(taskId)?.status
			def isChangePendingStatusAllowed = securityService.isChangePendingStatusAllowed()
			if (status == "Pending" && !isChangePendingStatusAllowed) {
				allowed = false
			}
		}
		renderAsJson(isAllowToChangeStatus: allowed)
	}

	/**
	 * Generates an HTML SELECT control for the AssetComment.status property according to user role and current status of AssetComment(id)
	 * @param	params.id	The ID of the AssetComment to generate the SELECT for
	 * @param   format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return render HTML or a JSON array
	 */
	def updateStatusSelect() {
		//Changing code to populate all select options without checking security roles.
		def mapKey = 'ALL'//securityService.hasRole([ADMIN.name(),SUPERVISOR.name(),CLIENT_ADMIN.name(),CLIENT_MGR.name()]) ? 'ALL' : 'LIMITED'
		def optionForRole = statusOptionForRole.get(mapKey)
		def format = params.format
		def taskId = params.id
		def status = taskId ? (AssetComment.read(taskId)?.status?: '*EMPTY*') : AssetCommentStatus.READY
		def optionList = optionForRole.get(status)
		def firstOption = [value:'', display:'Please Select']
		def selectId = taskId ? "statusEditId" : "statusCreateId"
		def optionSelected = taskId ? (status != '*EMPTY*' ? status : 'na'): AssetCommentStatus.READY

		if (format == 'json') {
			renderSuccessJson(optionList)
		} else {
			render HtmlUtil.generateSelect(selectId: selectId, selectName: 'statusEditId', options: optionList,
				selectClass: "task_${optionSelected.toLowerCase()}", optionSelected: optionSelected,
				javascript: "onChange='this.className=this.options[this.selectedIndex].className'",
				firstOption: firstOption, optionClass: '')
		}
	}

	/**
	 * Generates an HTML table containing all the predecessor for a task with corresponding Category and Tasks SELECT controls for
	 * a speciied assetList of predecessors HTML SELECT control for the AssetComment at editing time
	 * @param	params.id	The ID of the AssetComment to load  predecessor SELECT for
	 * @return render HTML
	 */
	def predecessorTableHtml() {
		//def sw = new org.springframework.util.StopWatch("predecessorTableHtml Stopwatch")
		//sw.start("Get current project")
		def task = AssetComment.findByIdAndProject(params.commentId, securityService.loadUserCurrentProject())
		if (! task) {
			log.error "predecessorTableHtml - unable to find task $params.commentId for project $securityService.userCurrentProjectId"
			render "An unexpected error occured"
		} else {
			render taskService.genTableHtmlForDependencies(task.taskDependencies, task, "predecessor")
		}
	}

	/**
	 * Generats options for task dependency select
	 * @param : taskId  : id of task for which select options are generating .
	 * @param : category : category for options .
	 * @return : options
	 */
	def generateDepSelect() {
		def task = AssetComment.read(params.taskId)
		def category = params.category

		def queryForPredecessor = new StringBuilder('''
			FROM AssetComment a
			WHERE a.project.id=projectId
			AND a.category=:category
			AND a.commentType.id=:commentTypeId
			AND a.id!=:taskId
		''')
		Map queryArgs = [projectId: securityService.userCurrentProjectId, category: category,
		                 commentTypeId: AssetCommentType.TASK.toString(), taskId: task.id]
		if (task.moveEvent) {
			queryForPredecessor.append('AND a.moveEvent=:moveEvent')
			queryArgs.moveEvent = task.moveEvent
		}
		queryForPredecessor.append(' ORDER BY a.taskNumber ASC')
		def predecessors = AssetComment.executeQuery(queryForPredecessor.toString())

		StringBuilder options = new StringBuilder()
		predecessors.each {
			options << "<option value='" << it.id << "'>" << it << '</option>'
		}
		render options.toString()
	}

	/**
	 * Generates an HTML table containing all the successor for a task with corresponding Category and Tasks SELECT controls for
	 * a speciied assetList of successors HTML SELECT control for the AssetComment at editing time
	 * @param   params.id   The ID of the AssetComment to load  successor SELECT for
	 * @return render HTML
	 */
	def successorTableHtml() {
		def task = AssetComment.findByIdAndProject(params.commentId, securityService.loadUserCurrentProject())
		if (! task) {
			log.error "successorTableHtml - unable to find task $params.commentId for project $securityService.userCurrentProjectId"
			render "An unexpected error occured"
		} else {
			def taskSuccessors = TaskDependency.findAllByPredecessor(task).sort{ it.assetComment.taskNumber }
			render taskService.genTableHtmlForDependencies(taskSuccessors, task, "successor")
		}
	}

	/**
	 * Generates the HTML SELECT for a single Predecessor
	 * Used to generate a SELECT control for a project and category with an optional task. When a task is presented the list will
	 * also be filtered on tasks from the moveEvent.
	 * If a taskId is included, the SELECT will have CSS ID taskDependencyEditId otherwise taskDependencyId and the SELECT name of
	 * taskDependencyEdit or taskDependencySave accordingly since having an Id means that we're in edit mode vs create mode.
	 * @param commentId - the comment (aka task) that the predecessor will use
	 * @param category - comment category to filter the list of tasks by
	 * @param format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return String - HTML Select of prdecessor list or a JSON
	 */
	def predecessorSelectHtml() {
		Project project = securityService.userCurrentProject
		def task
		def format=params.format
		def moveEventId=params.moveEvent

		if (params.commentId) {
			task = AssetComment.findByIdAndProject(params.commentId, project)
			if (! task) {
				log.warn "predecessorSelectHtml - Unable to find task id $params.commentId in project $project.id"
			}
		}

		def taskList = taskService.genSelectForPredecessors(project, params.category, task, moveEventId)

		if (format=='json') {
			def list = []
			list << [ id: '', desc: 'Please Select', category: '', taskNumber: '']
			taskList.list.each {
				def desc = it.comment?.length()>50 ? it.comment.substring(0,50): it.comment
				list << [ id: it.id, desc: desc, category: it.category, taskNumber: it.taskNumber]
			}
			renderSuccessJson(list)
		} else {
			// Build the SELECT HTML
			render HtmlUtil.generateSelect(selectId: task ? 'taskDependencyEditId' : 'taskDependencyId',
				selectName: params.forWhom, options: taskList.list, optionKey: 'id',
				firstOption: [value:'', display:'Please Select'])
		}
	}

	/**
	 * returns a list of tasks paginated and filtered
	 */
	def tasksSearch() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def task
		def moveEventId=params.moveEvent
		def page=Long.parseLong(params.page)
		def pageSize=Long.parseLong(params.pageSize)
		def filterDesc=params['filter[filters][0][value]']

		if (params.commentId) {
			task = AssetComment.findByIdAndProject(params.commentId, project)
			if (! task) {
				log.warn "predecessorSelectHtml - Unable to find task id $params.commentId in project $project.id"
			}
		}

		def tasksData = taskService.genSelectForPredecessors(project, params.category, task, moveEventId, page, pageSize, filterDesc)

		def list = []

		list << [ id: '', desc: 'Please Select', category: '', taskNumber: '']
		tasksData.total++

		tasksData.list.each {
			def desc = it.comment?.length()>50 ? it.comment.substring(0,50): it.comment
			list << [ id: it.id, desc: it.taskNumber + ': ' + desc, category: it.category, taskNumber: it.taskNumber]
		}

		tasksData.list = list
		renderSuccessJson(tasksData)
	}

	/**
	 * Return the task index in the search
	 */
	def taskSearchMap() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		def moveEventId = params.moveEvent
		def taskId = params.taskId
		def task

		if (params.commentId) {
			task = AssetComment.findByIdAndProject(params.commentId, project)
			if (!task) {
				log.warn "predecessorSelectHtml - Unable to find task id $params.commentId in project $project.id"
			}
		}

		def taskIdx = taskService.searchTaskIndexForTask(project, params.category, task, moveEventId, taskId)

		renderSuccessJson([taskIdx.intValue()])
	}

	/**
	 * Export Special Report
	 * @param NA
	 * @return : Export data in WH project format
	 */
	def exportSpecialReport() {
		Project project = securityService.userCurrentProject
		def today = TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_TIME_5)
		try{
			String filePath = "/templates/TDS-Storage-Inventory.xls"
			def book = ExportUtil.loadSpreadsheetTemplate(filePath)

			def spcExpSheet = book.getSheet("SpecialExport")
			def storageInventoryList = assetEntityService.getSpecialExportData(project)
			def spcColumnList = ["server_id", "app_id", "server_name", "server_type", "app_name", "tru", "tru2",
			                     "move_bundle", "move_date", "status","group_id", "environment", "criticality" ]

			for (int r = 0; r < storageInventoryList.size(); r++) {
				 for(int c = 0; c < spcColumnList.size(); c++) {
					def valueForSheet = storageInventoryList[r][spcColumnList[c]] ? String.valueOf(storageInventoryList[r][spcColumnList[c]]) : ""
					WorkbookUtil.addCell(spcExpSheet, c, r+1, valueForSheet)
				 }
			}

			ExportUtil.setContentType(response, project.name + 'SpecialExport-' + today +
				"." + ExportUtil.getWorkbookExtension(book))
			book.write(response.outputStream)
		}
		catch(e) {
			log.error "Exception occurred while exporting data: $e.message", e
			flash.message = e.message
		}
		redirect(action:"exportAssets")
	}

	/**
	 * Fetch Asset's modelType to use to select asset type fpr asset acording to model
	 * @param : id - Requested model's id
	 * @return : assetType if exist for requested model else 0
	 */
	def retrieveAssetModelType() {
		def assetType = 0
		if (params.id?.isNumber()) {
			assetType = Model.read(params.id).assetType ?: 0
		}
		render assetType
	}

	/**
	 * Populates the dependency section of the asset forms for support and dependent relationships
	 * @param id : asset id
	 * @return : HTML code containing support and dependent edit form
	 */
	def populateDependency() {

		Project project = securityService.userCurrentProject

		// TODO : JPM 10/2014 : Move populateDependency method to a service to avoid muliple transations just to populate a model

		Long id = params.long('id')
		if (id) {
			render "An invalid asset id was submitted"
			return
		}

		def assetEntity = AssetEntity.findByIdAndProject(id, project)
		if (!assetEntity) {
			render "Unable to find requested asset"
			return
		}

		// TODO - JPM 8/2014 - Why do we have this? Seems like we should NOT be passing that to the template...
		def nonNetworkTypes = [AssetType.SERVER, AssetType.APPLICATION, AssetType.VM, AssetType.FILES,
		                       AssetType.DATABASE, AssetType.BLADE]*.toString()

		render(template: 'dependentCreateEdit',
		       model: [assetClassOptions: AssetClass.classOptions, assetEntity: assetEntity,
		               dependencyStatus: assetEntityService.getDependencyStatuses(),
		               dependencyType: assetEntityService.getDependencyTypes(),
		               whom: params.whom, nonNetworkTypes: nonNetworkTypes,
		               supportAssets: assetEntityService.getSupportingAssets(assetEntity),
		               dependentAssets: assetEntityService.getDependentAssets(assetEntity),
		               moveBundleList: assetEntityService.getMoveBundles(project)])
	}

	/**
	 * Returns a lightweight list of assets filtered on  on the asset class
	 * @param id - class of asset to filter on (e.g. Application, Database, Server)
	 * @return JSON array of asset id, assetName
	 */
	def assetSelectDataByClass() {
		renderAsJson(assets: assetEntityService.getAssetsByType(params.id).collect { [value: it.id, caption: it.assetName] })
	}

	/**
	 * Gets validation for particular fields
	 * @param type,validation
	 */
	def retrieveAssetImportance() {
		renderAsJson assetEntityService.getConfig(params.type, params.validation).config
	}

	/**
	 * Gets highlighting css for particular fields
	 */
	def retrieveHighlightCssMap() {
		def assetType = params.type
		Class clazz
		switch(assetType) {
			case 'Application': clazz = Application; break
			case 'Database':    clazz = Database; break
			case 'Files':       clazz = Files; break
			default:            clazz = AssetEntity; break
		}

		def assetEntity = params.id ? clazz.get(params.id) : clazz.newInstance(appOwner: '')
		def configMap = assetEntityService.getConfig(assetType, params.validation)
		renderAsJson assetEntityService.getHighlightedInfo(assetType, assetEntity, configMap)
	}

	/**
	 * Updates columnList with custom labels.
	 * @param entityDTAMap :  dataTransferEntityMap for entity type
	 * @param columnslist :  column Names
	 * @param project :project instance
	 */
	private retrieveColumnNames(entityDTAMap, columnslist, project) {
		entityDTAMap.eachWithIndex { item, pos ->
			if (AssetEntityService.customLabels.contains(item.columnName)) {
				columnslist.add(project[item.eavAttribute?.attributeCode] ?: item.columnName)
			} else {
				columnslist.add(item.columnName)
			}
		}
		return columnslist
	}

	/**
	 * Sets Import preferences.(ImportApplication,ImportServer,ImportDatabase, ImportStorage,ImportRoom,ImportRack,ImportDependency)
	 * @param preference
	 * @param value
	 */
	def setImportPerferences() {
		def key = params.preference
		def value = params.value
		if (value) {
			userPreferenceService.setPreference(key, value)
			session.setAttribute(key,value)
		}
		render true
	}

	/**
	 * Action to return on list Dependency
	 */
	def listDependencies() {
		Project project = controllerService.getProjectForPage(this, 'to view Dependencies')
		if (!project) return

		def entities = assetEntityService.entityInfo(project)
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		def depPref = assetEntityService.getExistingPref('Dep_Columns')
		def attributes = ['c1':'C1','c2':'C2','c3':'C3','c4':'C4','frequency':'Frequency','comment':'Comment','direction':'Direction']
		def columnLabelpref = [:]
		depPref.each { key, value ->
			columnLabelpref[key] = attributes[value]
		}

		return [
			applications: entities.applications,
			assetDependency: new AssetDependency(),
			attributesList: attributes.keySet().sort{it},
			columnLabelpref:columnLabelpref,
			dbs: entities.dbs,
			depPref: depPref,
			dependencyStatus: entities.dependencyStatus,
			dependencyType: entities.dependencyType,
			files: entities.files,
			moveBundleList: moveBundleList,
			networks: entities.networks,
			// projectId: project.id,
			servers: entities.servers
		]
	}

	/**
	 * Show list of dependencies using jqgrid.
	 */
	def listDepJson() {
		String sortIndex = params.sidx ?: 'asset'
		String sortOrder = params.sord ?: 'asc'
		int maxRows = params.int('rows', 25)
		int currentPage = params.int('page', 1)
		int rowOffset = (currentPage - 1) * maxRows
		def sid

		def filterParams = [assetName: params.assetName, assetType: params.assetType, assetBundle: params.assetBundle,
		                    type: params.type, dependentName: params.dependentName, dependentType: params.dependentType,
		                    dependentBundle: params.dependentBundle, status: params.status,frequency: params.frequency,
		                    comment: params.comment, c1: params.c1, c2: params.c2, c3: params.c3, c4: params.c4,
		                    direction: params.direction]
		def depPref= assetEntityService.getExistingPref('Dep_Columns')
		StringBuffer query = new StringBuffer("""
			SELECT * FROM (
				SELECT asset_dependency_id AS id,
					ae.asset_name AS assetName,
					ae.asset_class AS assetClass,
					ae.asset_type AS assetType,
					mb.name AS assetBundle,
					ad.type AS type,
					aed.asset_name AS dependentName,
					aed.asset_class AS dependentClass,
					aed.asset_type AS dependentType,
					mbd.name AS dependentBundle,
					ad.status AS status,ad.comment AS comment, ad.data_flow_freq AS frequency, ae.asset_entity_id AS assetId,
					aed.asset_entity_id AS dependentId,
					ad.c1 AS c1, ad.c2 AS c2, ad.c3 AS c3,ad.c4 AS c4,
					ad.data_flow_direction AS direction
				FROM tdstm.asset_dependency ad
				LEFT OUTER JOIN asset_entity ae ON ae.asset_entity_id = asset_id
				LEFT OUTER JOIN asset_entity aed ON aed.asset_entity_id = dependent_id
				LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id = ae.move_bundle_id
				LEFT OUTER JOIN move_bundle mbd ON mbd.move_bundle_id = aed.move_bundle_id
				WHERE ae.project_id = $securityService.userCurrentProjectId
				ORDER BY ${sortIndex + " " + sortOrder}
			) AS deps
		 """)

		// Handle the filtering by each column's text field
		boolean firstWhere = true
		filterParams.each {
			if (it.value) {
				if (firstWhere) {
					query.append(' WHERE ')
					firstWhere = false
				}
				else {
					query.append(' AND ')
				}
				query.append("deps.$it.key LIKE '%$it.value%'")
			}
		}

		def dependencies = jdbcTemplate.queryForList(query.toString())
		int totalRows = dependencies.size()
		int numberOfPages = Math.ceil(totalRows / maxRows)

		if (totalRows) {
			dependencies = dependencies[rowOffset..Math.min(rowOffset + maxRows, totalRows - 1)]
		}

		def results = dependencies?.collect {
			[id: it.id,
			 cell: [it.assetName,
			        it.assetType,
			        it.assetBundle,
			        it.type,
			        it.dependentName,
			        it.dependentType,
			        it.dependentBundle,
			        (depPref['1']!='comment') ? it[depPref['1']] : (it[depPref['1']]? "<div class='commentEllip'>$it.comment</div>" : ''),
			        (depPref['2']!='comment') ? it[depPref['2']] : (it[depPref['2']]? "<div class='commentEllip'>$it.comment</div>" : ''),
			        it.status,
			        it.assetId, // 10
			        it.dependentId, // 11
			        it.assetClass,	// 12
			        it.dependentClass]	// 13
			]
		}

		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	/**
	* Change bundle when on change of asset in dependency.
	* @param dependentId
	* @param assetId
	* @render resultMap
	*/
	def retrieveChangedBundle() {
		def dependentId = params.dependentId
		def dependent = AssetDependency.read(dependentId.isInteger() ? dependentId.toInteger() : -1)
		Long depBundle = dependentId == "support" ? dependent?.asset?.moveBundleId : dependent?.dependent?.moveBundleId
		renderAsJson(id: AssetEntity.read(params.assetId)?.moveBundle?.id, status: dependent?.status, depBundle: depBundle)
	}

	private List sortAssetByColumn(List assetlist, String sortOn, String orderBy) {
		assetlist.sort { a, b ->
			if (orderBy == 'asc') {
				a.asset?.getAt(sortOn)?.toString() <=> b.asset?.getAt(sortOn)?.toString()
			} else {
				b.asset?.getAt(sortOn)?.toString() <=> a.asset?.getAt(sortOn)?.toString()
			}
		}
	}

	/**
	 * Used to return a SELECT for a specified roomId and sourceType
	 * @param params.roomId - room id
	 * @param params.rackId - the rack id of the currently selected rack
	 * @param params.sourceTarget - S)ource or T)arget
	 * @param params.forWhom - indicates if it is Create or Edit
	 */
	def retrieveRackSelectForRoom() {
		Project project = controllerService.getProjectForPage(this)
		def roomId = params.roomId
		def rackId = params.rackId
		def options = assetEntityService.getRackSelectOptions(project, roomId, true)
		def sourceTarget = params.sourceTarget
		def forWhom = params.forWhom
		def tabindex = params.tabindex

		def rackDomId
		def rackDomName
		def clazz

		if (sourceTarget == 'S') {
			rackDomId = 'rackSId'
			rackDomName = 'rackSourceId'
			clazz = 'config.sourceRack'
		}
		else {
			rackDomId = 'rackTId'
			rackDomName = 'rackTargetId'
			clazz = 'config.targetRack'
		}

		render(template: 'deviceRackSelect',
		       model: [options: options, rackDomId: rackDomId, rackDomName: rackDomName, clazz: clazz,
		               rackId: rackId, forWhom: forWhom, tabindex: tabindex?: 0, sourceTarget: sourceTarget])
	}

	/**
	 * Used to return a SELECT for a specified roomId and sourceType
	 * @param params.roomId - room id
	 * @param params.id - the chassis id of the currently selected chassis
	 * @param params.sourceTarget - S)ource or T)arget
	 * @param params.forWhom - indicates if it is Create or Edit
	 */
	def retrieveChassisSelectForRoom() {
		Project project = controllerService.getProjectForPage(this)
		def roomId = params.roomId
		def id = params.id
		def options = assetEntityService.getChassisSelectOptions(project, roomId)
		def sourceTarget = params.sourceTarget
		def forWhom = params.forWhom
		def tabindex = params.tabindex

		def rackDomId
		def rackDomName
		def domClass=params.domClass
		def clazz

		if (sourceTarget == 'S') {
			rackDomId = 'chassisSelectSId'
			rackDomName = 'chassisSelectSourceId'
			clazz = ''
		}
		else {
			rackDomId = 'chassisSelectTId'
			rackDomName = 'chassisSelectTargetId'
			clazz = ''
		}

		render(template: 'deviceChassisSelect',
				 model: [options: options, domId: rackDomId, domName: rackDomName, domClass: domClass ?: clazz,
				         value: id, forWhom: forWhom, sourceTarget: sourceTarget, tabindex: tabindex])
	}

	def retrieveAssetsByType() {
		Project project = controllerService.getProjectForPage(this, 'to view assets')
		if (!project) return

		def assetType = params.assetType

		try {
			if (assetType == 'Other') {
				assetType = AssetType.NETWORK.toString()
			}
			def groups = [assetType]
			def info = assetEntityService.entityInfo(project, groups)
			def assets = []
			switch (assetType) {
				case AssetType.SERVER.toString():
					assets = info.servers
					break
				case AssetType.APPLICATION.toString():
					assets = info.applications
					break
				case AssetType.DATABASE.toString():
					assets = info.dbs
					break
				case AssetType.STORAGE.toString():
					assets = info.files
					break
				case AssetType.NETWORK.toString():
					assets = info.networks
					break
			}
			def result = [list: [], type: assetType]
			assets.each {
				result.list << [id:it[0], name: it[1]]
			}
			renderSuccessJson(result)
		}
		catch (e) {
			handleException e, log
		}
	}

	/**
	 * This service retrieves all the assets for a given asset class.
	 */
	def assetsByClass() {
		renderSuccessJson(assetEntityService.getAssetsByClass(params))
	}

	def assetClasses() {
		def results = []
		assetEntityService.getAssetClasses().each { k,v -> results << [key:k, label:v]}
		renderSuccessJson(results)
	}

	def classForAsset() {
		renderSuccessJson(assetClass: AssetClass.getClassOptionForAsset(AssetEntity.load(params.id)))
	}

	def poiDemo() {
	}

	def exportPoiDemo() {
		String filePath = "/templates/TDSMaster_Poi_template.xls" // Template file Path
		String today = TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_TIME_5)
		String filename = "Demo_POI_Export-$today" // Export file name

		def assetEntities = AssetEntity.findAllByProject(securityService.loadUserCurrentProject(), [max:50])
		File file = grailsApplication.parentContext.getResource(filePath).getFile()

		//creating Workbook insatnce with using template fileInput stream
		Workbook workbook = WorkbookFactory.create(new FileInputStream(file))
		Sheet sheet = workbook.getSheet("Servers")

		//Get all column count of first row or header
		def dataTransferSetInstance = DataTransferSet.get(1)
		def serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName(dataTransferSetInstance,"Servers")
		def serverMap = [:]
		def serverColumnNameList =[]
		def serverSheetColumnNames = [:]
		serverDTAMap.eachWithIndex { item, pos ->
			serverMap[item.columnName] = null
			serverColumnNameList << item.columnName
		}
		serverMap.DepGroup = null
		serverColumnNameList << "DepGroup"

		def serverCol = sheet.getRow(0).getPhysicalNumberOfCells()
		for (int c = 0; c < serverCol; c++) {
			String serverCellContent = sheet.getRow(0).getCell(c).stringCellValue
			serverSheetColumnNames[serverCellContent] = c
			if (serverMap.containsKey(serverCellContent)) {
				serverMap[serverCellContent] = c
			}
		}
		for (int r=1; r<=assetEntities.size(); r++) {
			// creating row here
			Row row = sheet.createRow(r)
			Cell cell = row.createCell(0)
			cell.setCellValue(assetEntities[r-1].id)
			for (String colName in serverColumnNameList) {
				cell = row.createCell(serverMap[colName])
				def attribute = serverDTAMap.eavAttribute.attributeCode[serverMap[colName]]
				if (attribute) {
					cell.setCellValue(String.valueOf(assetEntities[r-1][attribute] ?: ""))
				}
			}
		}

		response.setContentType('application/vnd.ms-excel')
		filename = filename.replace('.xls', '')
		response.setHeader('Content-Disposition', 'attachment; filename="' +  filename + '.xls"')

		try {
			OutputStream out = new FileOutputStream(file)
			workbook.write(out)
			out.close()

			StreamUtils.copy new FileInputStream(new File(file.getAbsolutePath())), response.outputStream
		}
		catch (IOException e) {
			log.error e.message, e
		}
	}

	/**
	 * Returns a JSON object containing the data used by Select2 javascript library
	 * @param assetClassOption
	 * @param max
	 * @param page
	 * @param q
	 */
	def assetListForSelect2() {
		def results = []
		long total = 0

		Project project = securityService.userCurrentProject
		if (project) {

			// The following will perform a count query and then a query for a subset of results based on the max and page
			// params passed into the request. The query will be constructed with @COLS@ tag that can be substitued when performing
			// the actual queries.

			int max = NumberUtil.limit(params.int('max', 10), 1, 25)
			int currentPage = NumberUtil.limit(params.int('page', 1), 1, 1000)
			int offset = (currentPage - 1) * max

			// This map will drive how the query is constructed for each of the various options
			Map qmap = [
				APPLICATION:         [ assetClass: AssetClass.APPLICATION, domain: Application ],
				'SERVER-DEVICE':     [ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.serverTypes ],
				DATABASE:            [ assetClass: AssetClass.DATABASE, domain: Database ],
				'NETWORK-DEVICE':    [ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.networkDeviceTypes ],
				// 'NETWORK-LOGICAL':   [],
				'STORAGE-DEVICE':    [assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.storageTypes ],
				'STORAGE-LOGICAL':   [assetClass: AssetClass.STORAGE, domain: Files ],
				'OTHER-DEVICE':      [assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.nonOtherTypes, notIn: true ],
				ALL:                 [domain: AssetEntity ]
			]

			String queryColumns = 'a.id as id, a.assetName as text'
			String queryCount = 'COUNT(a)'

			StringBuffer query = new StringBuffer("SELECT @COLS@ FROM ")

			if (qmap.containsKey(params.assetClassOption)) {
				def qm = qmap[params.assetClassOption]
				def assetClass = qm.assetClass
				def qparams = [ project:project ]
				if (assetClass)
					qparams = [ project:project, assetClass:qm.assetClass ]

				query.append(qm.domain.name + ' AS a ')

				def doJoin = qm.containsKey('assetType')
				def notIn = qm.containsKey('notIn') && qm.notIn
				if (doJoin) {
					if (notIn) {
						query.append('LEFT OUTER JOIN a.model AS m ')
					} else {
						query.append('JOIN a.model AS m ')
					}
				}

				if (assetClass)
					query.append('WHERE a.project=:project AND a.assetClass=:assetClass ')
				else
					query.append('WHERE a.project=:project ')

				if (params.containsKey('q') && params.q.size() > 0) {
					query.append('AND a.assetName LIKE :q ')
					qparams.q = "%$params.q%"
				}

				if (doJoin) {
					if (notIn) {
						query.append("AND COALESCE(m.assetType,'') NOT ")
					} else {
						query.append("AND m.assetType ")
					}
					query.append('IN (:assetType)')
					qparams.assetType = qm.assetType
				}

				query.append("ORDER BY a.assetName ASC")

				log.debug "***** Query: $query\nParams: $qparams"

				// Perform query and move data into normal map
				def cquery = query.toString().replace('@COLS@', queryCount)
				log.debug "***** Count Query: $cquery"

				total = qm.domain.executeQuery(cquery, qparams)[0]

				if (total > 0) {
					def rquery = query.toString().replace('@COLS@', queryColumns)
					// rquery = rquery + " ORDER BY a.assetName"
					log.debug "***** Results Query: $rquery"

					results = qm.domain.executeQuery(rquery, qparams, [max:max, offset:offset, sort:'assetName' ])

					// Convert the columns into a map that Select2 requires
					results = results.collect{ r -> [ id:r[0], text: SEU.escapeHtml(SEU.escapeJavaScript(r[1])) ]}
				}
			} else {
				// TODO - Return an error perhaps by setting total to -1 and adding an extra property for a message
				log.error "assetListForSelect2() doesn't support param assetClassOption $params.assetClassOption"
			}
		}

		renderAsJson(results: results, total: total)
	}

	/**
	 * Returns the list of models for a specific manufacturer and asset type
	 */
	@Secured('isAuthenticated()')
	def modelsOf() {
		try {
			def models = assetEntityService.modelsOf(params.manufacturerId, params.assetType, params.term)
			renderSuccessJson(models: models)
		}
		catch (e) {
			handleException e, log
		}
	}

	/**
	 * Returns the list of models for a specific asset type
	 */
	@Secured('isAuthenticated()')
	def manufacturer() {
		try {
			renderSuccessJson(manufacturers: assetEntityService.manufacturersOf(params.assetType, params.term))
		}
		catch (e) {
			handleException e, log
		}
	}

	/**
	 * Returns the list of asset types for a specific manufactures
	 */
	@Secured('isAuthenticated()')
	def assetTypesOf() {
		try {
			renderSuccessJson(assetTypes: assetEntityService.assetTypesOf(params.manufacturerId, params.term))
		}
		catch (e) {
			handleException e, log
		}
	}

	@Secured('isAuthenticated()')
	def architectureViewer() {
		Project project = securityService.userCurrentProject
		def levelsUp = NumberUtils.toInt(params.levelsUp)
		int levelsDown = NumberUtils.toInt(params.levelsDown) ?: 3

		def assetName
		if (params.assetId) {
			assetName = AssetEntityHelper.getAssetById(project, null, params.assetId).assetName
		}

		Map<String, String> defaultPrefs = [levelsUp: '0', levelsDown: '3', showCycles: true,
		                                    appLbl: true, labelOffset: '2', assetClasses: 'ALL']
		def graphPrefs = userPreferenceService.getPreference(PREF.ARCH_GRAPH)
		Map prefsObject = graphPrefs ? JSON.parse(graphPrefs) : defaultPrefs

		def model = [
			assetId : params.assetId,
			assetName: assetName,
			levelsUp: levelsUp,
			levelsDown: levelsDown,
			assetClassesForSelect: [ALL: 'All Classes'] + AssetClass.classOptions,
			moveBundleList: assetEntityService.getMoveBundles(project),
			dependencyStatus: assetEntityService.getDependencyStatuses(),
			dependencyType: assetEntityService.getDependencyTypes(),
			assetTypes: AssetEntityService.ASSET_TYPE_NAME_MAP,
			defaultPrefs:defaultPrefs as JSON,
			graphPrefs:prefsObject,
			assetClassesForSelect2: AssetClass.classOptionsDefinition
		]
		render(view: 'architectureGraph', model: model, assetId: params.assetId)
	}

	/**
	 * Returns the data needed to generate the application architecture graph
	 */
	@Secured('isAuthenticated()')
	def applicationArchitectureGraph() {
		try {
			Project project = securityService.userCurrentProject
			def assetId = NumberUtils.toInt(params.assetId)
			def asset = AssetEntity.get(assetId)
			def levelsUp = NumberUtils.toInt(params.levelsUp)
			def levelsDown = NumberUtils.toInt(params.levelsDown)
			def deps = []
			def sups = []
			def assetsList = []
			def dependencyList = []

			// maps asset type names to simpler versions
			def assetTypes = AssetEntityService.ASSET_TYPE_NAME_MAP

			// Check if the parameters are null
			if ((assetId == null || assetId == -1) || (params.levelsUp == null || params.levelsDown == null)) {
				def model = [nodes: [] as JSON, links: [] as JSON, assetId: params.assetId, levelsUp: params.levelsUp,
				             levelsDown: params.levelsDown, assetTypes: assetTypes, assetTypesJson: assetTypes as JSON,
				             environment: Environment.current.name]
				render(view: '_applicationArchitectureGraph', model: model)
				return
			}

			if (asset.project != project) {
				throw new UnauthorizedException()
			}

			// build the graph based on a specific asset
			if (params.mode == "assetId") {

				// recursively get all the nodes and links that depend on the asset
				def stack = []
				def constructDeps
				constructDeps = { a, l ->
					deps.push(a)
					if (! (a in assetsList)) {
						assetsList.push(a)
					}
					if (l > 0) {
						def dependent = AssetDependency.findAllByAsset(a)
						dependent.each {
							if (! (it in dependencyList)) {
								dependencyList.push(it)
							}
							constructDeps(it.dependent, l-1)
						}
					}
				}
				constructDeps(asset, levelsDown)

				// recursively get all the nodes and links that support the asset
				stack = []
				def constructSups
				constructSups = { a, l ->
					sups.push(a)
					if (! (a in assetsList)) {
						assetsList.push(a)
					}
					if (l > 0) {
						def supports = AssetDependency.findAllByDependent(a)
						supports.each {
							if (! (it in dependencyList)) {
								dependencyList.push(it)
							}
							constructSups(it.asset, l-1)
						}
					}
				}
				constructSups(asset, levelsUp)

			// this mode hasn't been implemented yet
			} else if (params.mode == "dependencyBundle") {
				def bundle = params.dependencyBundle
				def assets = assetDependencyBundle.findAllWhere(project:project, dependencyBundle:bundle)
			}

			// find any links between assets that weren't found with the DFS
			def assetIds = assetsList.id
			def extraDependencies = []
			assetsList.each { a ->
				AssetDependency.findAllByAssetAndDependentInList(a, assetsList).each { dep ->
					if (!(dep in dependencyList)) {
						extraDependencies.push(dep)
					}
				}
			}

			// add in any extra dependencies that were found
			dependencyList.addAll extraDependencies

			def serverTypes = AssetType.allServerTypes

			// Create the Nodes
			def graphNodes = []
			String name = ''
			def shape = 'circle'
			def size = 150
			String title = ''
			String color = ''
			String type = ''
			String assetType = ''
			String assetClass = ''
			Map criticalitySizes = [Minor: 150, Important: 200, Major: 325, Critical: 500]

			// create a node for each asset
			assetsList.each {

				// get the type used to determine the icon used for this asset's node
				assetType = it.model?.assetType ?: it.assetType
				assetClass = it.assetClass?.toString() ?: ''
				size = 150

				type = getImageName(assetClass, assetType)
				if (type == AssetType.APPLICATION.toString()) {
					size = it.criticality ? criticalitySizes[it.criticality] : 200
				}

				graphNodes << [
					id:it.id,
					name: SEU.escapeHtml(SEU.escapeJavaScript(it.assetName)),
					type:type, assetClass:it.assetClass.toString(),
					shape:shape, size:size,
					title: SEU.escapeHtml(SEU.escapeJavaScript(it.assetName)),
					color: it == asset ? 'red' : 'grey',
					parents:[], children:[], checked:false, siblings:[]
				]
			}

			// Create a seperate list of just the node ids to use while creating the links (this makes it much faster)
			def nodeIds = graphNodes*.id
			def defaults = moveBundleService.getMapDefaults(graphNodes.size())

			// Create the links
			def graphLinks = []
			def i = 0
			def opacity = 1
			def statusColor = 'grey'
			dependencyList.each {
				boolean notApplicable = !(it.status in [AssetDependencyStatus.ARCHIVED, AssetDependencyStatus.NA, AssetDependencyStatus.TESTING])
				def future = it.isFuture
				def unresolved = !it.isStatusResolved
				def sourceIndex = nodeIds.indexOf(it.asset.id)
				def targetIndex = nodeIds.indexOf(it.dependent.id)
				if (sourceIndex != -1 && targetIndex != -1) {
					graphLinks << [id: i, parentId: it.asset.id, childId: it.dependent.id, child: targetIndex,
					               parent: sourceIndex, value: 2, opacity: opacity, redundant: false, mutual: null,
					               notApplicable: notApplicable, future: future, unresolved: unresolved]
					++i
				}
			}

			// Set the dependency properties of the nodes
			graphLinks.each {
				if (!it.cyclical) {
					graphNodes[it.child].parents.add(it.id)
					graphNodes[it.parent].children.add(it.id)
				}
			}

			render(view:'_applicationArchitectureGraph',
			       model: [nodes: graphNodes as JSON, links: graphLinks as JSON, assetId: params.assetId,
			               levelsUp: params.levelsUp, levelsDown: params.levelsDown, assetTypes: assetTypes,
			               assetTypesJson: assetTypes as JSON, environment: Environment.current.name])
		}
		catch (e) {
			handleException e, log
		}
	}

	def graphLegend() {
		render(view: '_graphLegend', model: [assetTypes: assetEntityService.ASSET_TYPE_NAME_MAP])
	}

	/**
	 * Used to retrieve the account information during the import process after it has been read in from the
	 * uploaded spreadsheet and reviewed for errors.
	 * @params filename - the filename that the temporary uploaded spreadsheet was saved as
	 * @return JSON { accounts: List of accounts }
	 */
	@HasPermission('GenerateTasks')
	def importTaskReviewData() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) {
			renderErrorJson flash.remove('message')
			return
		}

		if (!params.filename) {
			renderErrorJson 'Request was missing the required filename reference'
			return
		}

		try {
			// TODO : JPM 4/2016 : importAccountsReviewData This method should be refactored so that the bulk of the logic
			// is implemented in the service.

			Map formOptions = taskImportExportService.importParamsToOptionsMap(params)
			renderAsJson taskImportExportService.generateReviewData(project, params.filename, formOptions)
		}
		catch(e) {
			log.error "Exception occurred while importing data: $e.message", e

			renderErrorJson(['An error occurred while attempting to import tasks', e.message])
		}
	}

	/**
	 * Used to import tasks. This is a three
	 * step form that take param.step to track at what point the user is in the process. The steps include:
	 *     start  - The user is presented a form
	 *     upload - The user has uploaded the spreadsheet which is saved to a temporary random filename and the user
	 *              is presented with the validation results
	 *     post   - The previously confirmed and this submission will reload the saved spreadsheet and post the
	 *              changes to the database and delete the spreadsheet.
	 */
	@HasPermission('GenerateTasks')
	def importTask() {
		Project project = controllerService.getProjectForPage(this)
		if (!project) return

		String formAction = 'importTask'
		String currentStep = params.step ?: 'start'

		// fileParamName is the name of the parameter that the file will be uploaded as
		String fileParamName = 'importTasksSpreadsheet'
		Map model = [ step:currentStep, projectName:project.name, fileParamName:fileParamName ]


		Map options = taskImportExportService.importParamsToOptionsMap(params)
		String view = 'importTasks'

		// There is a bug or undocumented feature that doesn't allow overriding params when forwarding which is used
		// in the upload step to forward to the review so we look for the stepAlt and use it if found.
		String step = params.stepAlt ?: params.step
		try {

			switch(step) {

				case 'upload':
					// This step will save the spreadsheet that was posted to the server after reading it
					// and verifying that it has some accounts in it. If successful it will do a forward to
					// the review step.

					options.fileParamName = fileParamName
					model = taskImportExportService.processFileUpload(request, project, options)

					// forward to the Review step
					forward(action: formAction, params: [stepAlt: 'review', filename: model.filename,
					                                     importOption: params.importOption])
					return

				case 'review':
					// This step will serve up the review template that in turn fetch the review data
					// via an Ajax request.
					model << taskImportExportService.generateModelForReview(project, options)
					// log.debug "importAccounts() case 'review':\n\toptions=$options\n\tmodel=$model"
					if (!options.filename && model.filename) {
						// log.debug "importAccounts() step=$step set filename=${model.filename}"
						options.filename = model.filename
					}

					// This is used by the AJAX request in the form to construct the URL appropriately
					model.paramsForReviewDataRequest = [filename:model.filename, importOption:params.importOption]
					view = formAction + 'Review'
					break

				case 'post':
					// This is the daddy of the steps in that it is going to post the changes back to the
					// database.

					List optionErrors = taskImportExportService.validateImportOptions(options)
					if (optionErrors) {
						throw new InvalidParamException(optionErrors.toString())
					}

					options.testMode = params.testMode == 'Y'

					// Here's the money maker call that will update existing accounts and create new ones accordingly
					model.results = taskImportExportService.postChangesToTasks(project, options)

					log.debug "importTasks() post results = ${model.results}"

					// TODO BB generateModelForPostResults() is in AccountImportExportService
					model << taskImportExportService.generateModelForPostResults(project, options)
					if (!options.filename && model.filename) {
						options.filename = model.filename
					}

					// This is used by the AJAX request in the form to construct the URL appropriately
					model.paramsForReviewDataRequest = [filename:model.filename, importOption:params.importOption]

					view = formAction + 'Results'
					log.debug "importTasks() view = $view"

					break

				default:
					// The default which is the first step to prompt for the spreadsheet to upload
					break
			}
		} catch (e) {
			switch (e) {
				case InvalidRequestException:
				case DomainUpdateException:
				case InvalidParamException:
				case EmptyResultException:
					log.debug "importTasks() exception ${e.getClass().name} $e.message"
					flash.message = e.message
					break
				default:
					log.error "Exception occurred while importing data (step $currentStep)", e
					flash.message = "An error occurred while attempting to import accounts"
			}
			// Attempt to delete the temporary uploaded worksheet if an exception occurred
			if (options.filename) {
				log.error e.message, e
				taskImportExportService.deletePreviousUpload(options)
			}
		}

		// log.debug "importAccounts() Finishing up controller step=$step, view=$view, model=$model"
		render view:view, model:model
	}

	/**
	 * Check if a user have permissions to create/edit comments
	 */
	private boolean userCanEditComments(commentType) {
		commentType == AssetCommentType.TASK || securityService.hasPermission('AssetEdit')
	}

	private String getImageName(String assetClassId, String type) {
		switch (assetClassId) {
			case AssetClass.APPLICATION.toString():
			case AssetClass.DATABASE.toString(): return assetClassId
			case AssetClass.STORAGE.toString(): return AssetType.FILES.toString()
			case AssetClass.DEVICE.toString():
				if (type in AssetType.virtualServerTypes) {
					return AssetType.VM.toString()
				}
				if (type in AssetType.physicalServerTypes) {
					return AssetType.SERVER.toString()
				}
				if (type in AssetType.storageTypes) {
					return AssetType.STORAGE.toString()
				}
				if (type in AssetType.networkDeviceTypes) {
					return AssetType.NETWORK.toString()
				}
		}

		return 'Other'
	}

	private Long getBatchId() {
		(Long) session.getAttribute('BATCH_ID')
	}

	private void setBatchId(long id) {
		session.setAttribute 'BATCH_ID', id
	}

	private Long getTotalAssets() {
		(Long) session.getAttribute('TOTAL_ASSETS')
	}

	private void setTotalAssets(long count) {
		session.setAttribute 'TOTAL_ASSETS', count
	}
}
