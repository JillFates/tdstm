import grails.converters.JSON

import java.text.DateFormat

import net.tds.util.jmesa.AssetEntityBean

import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils
import org.apache.shiro.SecurityUtils

import java.io.File;

import grails.util.GrailsUtil

import org.apache.commons.lang.math.NumberUtils
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Row
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.quartz.SimpleTrigger
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.quartz.Trigger
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*

import java.util.regex.Matcher
import org.hibernate.criterion.Order
import org.hibernate.criterion.CriteriaSpecification;

import com.tds.asset.Application
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.TaskDependency
import com.tdsops.common.lang.ExceptionUtil
import com.tdsops.tm.domain.AssetEntityHelper
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.ValidationType
import com.tdsops.tm.enums.domain.AssetDependencyStatus
import com.tdsops.tm.enums.domain.AssetDependencyType
import com.tdssrc.eav.*
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import com.tdsops.tm.asset.graph.AssetClassUtil

class AssetEntityController {

	def assetEntityAttributeLoaderService
	def assetEntityService
	def commentService
	def controllerService
	def deviceService
	def filterService
	def importService
	def moveBundleService
	def partyRelationshipService
	def personService
	def progressService
	def projectService
	def reportsService
	def securityService
	def sequenceService
	def sessionFactory
	def stateEngineService
	def supervisorConsoleService
	def taskService
	def userPreferenceService
	def userService

	def jdbcTemplate
	def quartzScheduler
	
	def assetClassUtil = new AssetClassUtil()
	
	// def missingHeader = ""
	int added = 0
	def skipped = []
	def assetEntityInstanceList = []
	def rackService

	// Contains list of the default Custom# column names (e.g. ['Custom1','Custom2','Custom3',...])
	protected static List customLabels = customLabelsNameList()

	// TODO : JPM 9/2014 : Need to remove the references to the team static vars bundleMoveAndClientTeams, targetTeamType, sourceTeamType, teamsByType
	protected static bundleMoveAndClientTeams = ['sourceTeamMt','sourceTeamLog','sourceTeamSa','sourceTeamDba','targetTeamMt','targetTeamLog','targetTeamSa','targetTeamDba']
	protected static targetTeamType = ['MOVE_TECH':'targetTeamMt', 'CLEANER':'targetTeamLog','SYS_ADMIN':'targetTeamSa',"DB_ADMIN":'targetTeamDba']
	protected static sourceTeamType = ['MOVE_TECH':'sourceTeamMt', 'CLEANER':'sourceTeamLog','SYS_ADMIN':'sourceTeamSa',"DB_ADMIN":'sourceTeamDba']
	protected static teamsByType = ["MOVE":"'MOVE_TECH','CLEANER'","ADMIN":"'SYS_ADMIN','DB_ADMIN'"]
	
	// This is a has table that sets what status from/to are available
	protected static statusOptionForRole = [
		"ALL": [
			'*EMPTY*': AssetCommentStatus.getList(),
			(AssetCommentStatus.PLANNED): AssetCommentStatus.getList(),
			(AssetCommentStatus.PENDING): AssetCommentStatus.getList(),
			(AssetCommentStatus.READY): AssetCommentStatus.getList(),
			(AssetCommentStatus.STARTED): AssetCommentStatus.getList(),
			(AssetCommentStatus.HOLD): AssetCommentStatus.getList(),
			(AssetCommentStatus.DONE): AssetCommentStatus.getList()
		],
		"LIMITED":[
			'*EMPTY*': [AssetCommentStatus.PLANNED, AssetCommentStatus.PENDING, AssetCommentStatus.HOLD],
			(AssetCommentStatus.PLANNED): [AssetCommentStatus.PLANNED],
			(AssetCommentStatus.PENDING): [AssetCommentStatus.PENDING],
			(AssetCommentStatus.READY):   [AssetCommentStatus.READY,AssetCommentStatus.STARTED, AssetCommentStatus.DONE, AssetCommentStatus.HOLD],
			(AssetCommentStatus.STARTED): [AssetCommentStatus.READY, AssetCommentStatus.STARTED, AssetCommentStatus.DONE, AssetCommentStatus.HOLD],
			(AssetCommentStatus.DONE): [AssetCommentStatus.DONE, AssetCommentStatus.HOLD],
			(AssetCommentStatus.HOLD): [AssetCommentStatus.HOLD]
		]
	]

	// The spreadsheet columns that are Date format
	static final List<String> importColumnsDateType = ['MaintExp', 'Retire']

	/**
	 * Used to generate a list of the custom label field names as Custom1, Custom2, ... to the maximum supported number of fields
	 * @return List of custom label names
	 */
	static List<String> customLabelsNameList() {
		List list = []
		(1..Project.CUSTOM_FIELD_COUNT).each { list << "Custom$it".toString() }
		return list 
	}

	/**
	 * The default index redirects to the Device List
	 */
	def index() {
		redirect action:'list', params:params
	}

	/**
	 * To Filter the Data on AssetEntityList Page 
	 * @param  Selected Filter Values
	 * @return Will return filters data to AssetEntity  
	 */
	def filter() {
		if (params.rowVal) {
			if (!params.max) params.max = params.rowVal
			userPreferenceService.setPreference( "MAX_ASSET_LIST", "${params.rowVal}" )
		} else {
			def userMax = getSession().getAttribute("MAX_ASSET_LIST")
			if ( userMax.MAX_ASSET_LIST ) {
				if ( !params.max ) params.max = userMax.MAX_ASSET_LIST
			} else {
				if ( !params.max ) params.max = 50
			}
		}
		def project = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )

		params['project.id'] = project.id

		def assetEntityList = filterService.filter( params, AssetEntity )
		assetEntityList.each{
			if ( it.project.id == project.id ) {
				assetEntityInstanceList<<it
			}
		}
		try {
			render( view:'list', model:[ assetEntityInstanceList: assetEntityInstanceList,
						assetEntityCount: filterService.count( params, AssetEntity ),
						filterParams: com.zeddware.grails.plugins.filterpane.FilterUtils.extractFilterParams(params),
						params:params, projectId:project.id,maxVal : params.max ] )
		} catch (Exception ex) {
			redirect( controller:"assetEntity", action:"list" )
		}
	}

	/**
	 * The initial Asset Import form
	 */
	def assetImport() {
		Project projectInstance
		UserLogin userLogin

		(projectInstance, userLogin) = controllerService.getProjectAndUserForPage(this, 'import') 
		if (!projectInstance)
			return 
	
		//get id of selected project from project view
		def projectId = projectInstance.id

		List assetsByProject	= AssetEntity.findAllByProject(projectInstance)
		List moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )

		def dataTransferSetImport = DataTransferSet.findAll(" from DataTransferSet dts where dts.transferMode IN ('B','I') ")
		def dataTransferSetExport = DataTransferSet.findAll(" from DataTransferSet dts where dts.transferMode IN ('B','E') ")

		def	dataTransferBatchs = DataTransferBatch.findAllByProject(projectInstance).size()
		session.setAttribute("BATCH_ID",0)
		session.setAttribute("TOTAL_ASSETS",0)
		
		def prefMap = [:]
		['ImportApplication','ImportServer','ImportDatabase','ImportStorage','ImportDependency','ImportCabling', 'ImportComment'].each{t->
		   prefMap << [(t) : userPreferenceService.getPreference(t)]
		}
		
		def isMSIE = false
		def userAgent = request.getHeader("User-Agent")
		if (userAgent.contains("MSIE") || userAgent.contains("Firefox"))
			isMSIE = true
		
		render( view:"importExport", model: [ 
			assetsByProject: assetsByProject,
			projectId: projectId,
			moveBundleInstanceList: moveBundleInstanceList,
			dataTransferSetImport: dataTransferSetImport,
			dataTransferSetExport: dataTransferSetExport, 
			prefMap: prefMap,
			dataTransferBatchs: dataTransferBatchs, 
			args: params.list("args"), 
			isMSIE: isMSIE, 
			message: params.message, 
			error: params.error] )
	}

	/**
	 * To render the Export form
	 */
	def assetExport() {
		render( view:"assetExport" )
	}
	
	/**
	 * This action is used to redirect control export view  
	 * render export form
	 */
	def exportAssets() {
		if (!controllerService.checkPermission(this, 'Export')){
			return
		}
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project
		def projectInstance
		def assetsByProject
		def dataTransferSetExport
		def moveBundleInstanceList
		if( projectId != null ) {
			projectInstance = Project.findById( projectId )
			moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
		}
		dataTransferSetExport = DataTransferSet.findAll("from DataTransferSet dts where dts.transferMode IN ('B','E') ")
		if( projectId == null ) {
			//get project id from session
			def currProj = getSession().getAttribute( "CURR_PROJ" )
			projectId = currProj.CURR_PROJ
			if( projectId == null ) {
				flash.message = " No Projects are Associated, Please select Project. "
				redirect( controller:"project",action:"list" )
			}
			projectInstance = Project.findById( projectId )
			moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
		}
		if ( projectId != null ) {
			project = Project.findById(projectId)
		}
		def	dataTransferBatchs = DataTransferBatch.findAllByProject(project).size()
		
		def prefMap = [:]
		['ImportApplication','ImportServer','ImportDatabase','ImportStorage','ImportDependency','ImportRoom','ImportRack', 'ImportCabling','ImportComment'].each {t->
		   prefMap << [(t) : userPreferenceService.getPreference(t)]
		}
		
		render (view:"exportAssets", model : [projectId: projectId,
			dataTransferBatchs:dataTransferBatchs, prefMap:prefMap,
			moveBundleInstanceList: moveBundleInstanceList,
			dataTransferSetExport: dataTransferSetExport])
	}

	/**
	 * Helper method used get the domain column names as a list substituting the custom labels appropriately
	 * @param entityDTAMap :  dataTransferEntityMap for entity type
	 * @param project - the project to match the custom
	 * @references customLabels - static in class
	 * @return List
	 */
	private List getColumnNamesForDTAMap(List entityDTAMap, Project project) {
		List columnslist = []
		entityDTAMap.eachWithIndex { item, pos ->
			if (customLabels.contains( item.columnName )){
				def customLabel = project[item.eavAttribute?.attributeCode] ? project[item.eavAttribute?.attributeCode] : item.columnName
				columnslist.add( customLabel )
			} else {
				columnslist.add( item.columnName )
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
	private String rowToImportValues(
		StringBuffer sqlStrBuff,
		HSSFSheet sheetRef, 
		Integer rowOffset, 
		Integer colOffset, 
		DataTransferAttributeMap dtaMapField, 
		String entityId, 
		Long dtBatchId
	) {
		def cellValue
		String errorMsg = ''
		boolean isFirstField = sqlStrBuff?.size() == 0
		// Get the Excel column code (e.g. column 0 = A, column 1 = B)
		String colCode = WorkbookUtil.columnCode(colOffset)

		try {
			cellValue = WorkbookUtil.getStringCellValue(sheetRef, colOffset, rowOffset, '', true)
			if (cellValue == ImportService.NULL_INDICATOR ) {
				// TODO : check for columns that don't support NULL clearing
			} else {
				if ( (dtaMapField.columnName in importColumnsDateType) )  {
					if (!StringUtil.isBlank(cellValue)) {
						def dateValue = WorkbookUtil.getDateCellValue(sheetRef, colOffset, rowOffset, getSession(), TimeUtil.FORMAT_DATE_TIME_12)
						// Convert to string in the date format
						if (dateValue) {
							cellValue = TimeUtil.formatDate(getSession(), dateValue)
						} else {
							cellValue = ''
						}
					}
					// log.debug "Processing Date field ${dtaMapField.columnName} - dateValue=$dateValue, cellValue=$cellValue"

				} else {
					// TODO : sizeLimit can lookup known properties to know if there are limits
					int sizeLimit = 255
					if (cellValue?.size() > sizeLimit) {
						cellValue = cellValue.substring(0,sizeLimit)
						errorMsg = "Error column ${dtaMapField.columnName} ($colCode) value length exceeds $sizeLimit chars"
					}
				}
			}
		} catch (e) {
			log.debug "rowToImportValues() exception - ${ExceptionUtil.stackTraceToString(e)}"
			errorMsg = "Error column ${dtaMapField.columnName} ($colCode) - ${e.getMessage()}"
		}

		// Only create a value if the field isn't blank
		if (cellValue != '') {
			int cellHasError = (errorMsg ? 1 : 0)
			String values = (isFirstField ? '' : ', ') +
				"($entityId, '$cellValue', $rowOffset, $dtBatchId, ${dtaMapField.eavAttribute.id}, $cellHasError, '$errorMsg')"

			if (! errorMsg) {
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
			String sql = DTV_INSERT_SQL + dtValues.toString()
			log.debug "insertRowValues() SQL=$sql"
			jdbcTemplate.update(sql)
			results.added ++
		} catch (Exception e) {
			results.errors << "Insert failed : ${e.getMessage()}"
			// skipped << "$sheetName [row ${( rowOffset + 1 )}] <ul><li>${errorMsgList.join('<li>')}</ul>"
			log.error("insertRowValues() Importing row ${( rowOffset + 1 )} failed : ${e.getMessage()}", e)
			success = false
		}
		return success
	}

	/**
	 * Creates the transfer batch header for the various assets. Note that it also sets a value on the Session 
	 * that is used for the progress bar. If it fails, the caller should return to the user.
	 * @param project
	 * @param userLogin
	 * @param dataTransferSet
	 * @param entityClassName - The name of the Domain class
	 * @param numOfAssets - The estimated number of assets to be imported 
	 * @param exportTime - The datetime that the spreadsheet was originally exported
	 * @return The DataTransferBatch object if successfully created otherwise null
	 */
	private DataTransferBatch createTransferBatch(
		Project project, 
		UserLogin userLogin, 
		DataTransferSet dataTransferSet, 
		String entityClassName, 
		int numOfAssets, 
		Date exportTime
	) {
		def eavEntityType = EavEntityType.findByDomainName(entityClassName)
		def dtb = new DataTransferBatch()
		dtb.statusCode = "PENDING"
		dtb.transferMode = "I"
		dtb.dataTransferSet = dataTransferSet
		dtb.project = project
		dtb.userLogin = userLogin
		// dtb.exportDatetime = GormUtil.convertInToGMT( exportTime, tzId )
		dtb.exportDatetime = exportTime
		dtb.eavEntityType = eavEntityType

		if ( dtb.save() ) {
			session.setAttribute("BATCH_ID", dtb.id)
			session.setAttribute("TOTAL_ASSETS", numOfAssets)
		} else {
			log.error "createTransferBatch() failed save - ${GormUtil.allErrorsString(dtb)}"
			return null
		}

		// log.debug "createTransferBatch() created $dtb"
		return dtb
	}	

	/**
	 * Used to get the number of rows in the each of the sheets and the number of assets with a name
	 * @param sheetObject - the Sheet object
	 * @param sheetName - the Name of the sheet
	 * @param assetIdColLabel - the column label for the asset id column
	 * @param columnName - the column name of the Name property
	 * @param colCount - the number of columns in the sheet
	 * @param headerRow - the row of the header
	 * @param dataTransferSet - the DataTransferSet used for the sheet
	 * @return A map of the various information about the sheet
	 */
	private Map getSheetInfo(
		Project project,
		HSSFWorkbook spreadsheetWB, 
		String sheetName, 
		String assetIdColLabel, 
		String columnName, 
		int headerRow,
		DataTransferSet dataTransferSet
	) {
		int rowsInSheet = 0
		int numOfAssets = 0
		int colCount = 0
		int nameColumnIndex = -1
		Map colNamesOrdinalMap = [:]
		def sheetObject 

		try { 
			sheetObject = spreadsheetWB.getSheet( sheetName )
		} catch (e) {
			throw new RuntimeException("The '$sheetName' sheet is missing from the import spreadsheet")
		}

		// Get the DataTransferAttributeMap list of properties for the sheet
		String dtaMapName = ( sheetName == 'Storage' ? 'Files' : sheetName )
		List dtaMapList = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSet, dtaMapName )

		// Get the spreadsheet column header labels as a Map [label:ordinalPosition]
		colCount = WorkbookUtil.getColumnsCount(sheetObject)
		for ( int c = 0; c < colCount; c++ ) {
			String cellContent = WorkbookUtil.getStringCellValue(sheetObject, c, headerRow)
			colNamesOrdinalMap.put(cellContent, c)
		}

		List domainPropertyNameList = getColumnNamesForDTAMap(dtaMapList, project)

		// Make sure that the required columns are in the spreadsheet
		checkSheetForMissingColumns(sheetName, domainPropertyNameList, colNamesOrdinalMap)

		// Find the 'Name' column index and then look at each row to count how many assets will be imported
		nameColumnIndex = getColumnIndexForName(sheetObject, sheetName, columnName, colCount, headerRow)

		rowsInSheet = sheetObject.getLastRowNum()
		for (int row = 1; row <= rowsInSheet; row++) {
			String assetName = WorkbookUtil.getStringCellValue(sheetObject, nameColumnIndex, row )
			if (assetName?.trim().size()) numOfAssets++
		}

		Map sheetInfo = [
			// The POI Sheet object 
			sheet: sheetObject,
			// The name of the sheet as it appears on the spreadsheet tab
			sheetName: sheetName,
			// The List of the DataTransferAttribute(s) used for this sheet
			dtaMapList: dtaMapList, 
			// The number of rows in the spreadsheet
			rowCount: rowsInSheet, 
			// The number of assets (rows with no names are not counted)
			assetCount: numOfAssets, 
			// The number of columns in the spreadsheet
			columnCount: colCount, 
			// Map of the columns an the values being the ordinal position/column in sheet
			colNamesOrdinalMap: colNamesOrdinalMap, 
			// int index/offset of the 'Name' column in the spreadsheet
			nameColumnIndex: nameColumnIndex,
			// String of column label/header for the asset id property
			assetIdColumnLabel: assetIdColLabel,
			// int of asset id column index/offset/column number
			assetIdColumnIndex: 0,
			// The list of the column names in the spreadsheet
			domainPropertyNameList: domainPropertyNameList
		]

		// log.debug "getSheetInfo() $sheetInfo"
		log.debug "getSheetInfo() dtaMapList=[0] isa ${dtaMapList[0].getClass().getName()} - ${dtaMapList[0]}"
		return sheetInfo
	}

	/**
	 * A helper closure to deal with the repeated process for validating each sheet
	 * @param sheetName - the name of the sheet being validated
	 * @param entityMapColumnList - the list of the mapped column names expected
	 * @param sheetColumnNameList - the list of the spreadsheet tab column names
	 */
	private void checkSheetForMissingColumns(String sheetName, domainPropertyList, sheetColumnNameList) { 
		List missingCols = getMissingColumns(domainPropertyList, sheetColumnNameList )

		if (missingCols) {
			throw new RuntimeException("missing expected columns ${missingCols.join(', ')}")
		}
	}

	/**
	 * Used to compare the sheet headers to the eav mapping of expected column names
	 * @param entityMapColumnList - the names that are expected
	 * @param sheetColumnNames - the column names in the sheet
	 * @return a List the missing columns or blank if okay 
	 */  
	private List getMissingColumns(List entityMapColumnList, Map sheetColumnNames) {
		List missing = []
		
		// assert entityMapColumnList.size() > 0

		entityMapColumnList.each { entityColumnName ->
			if ( ! (entityColumnName == "DepGroup" || sheetColumnNames.containsKey( entityColumnName ) ) ) {
				missing << entityColumnName
			}
		}

		return missing
	}

	/** 
	 * Used to looking up the column index for a given column name
	 * @param sheetObject - the actual sheet object
	 * @param sheetName - the name of the sheet
	 * @param columnName - the name of the column to lookup
	 * @param colCount - the number of columns in the sheet
	 * @param rowOffset - the row offset to the header itself
	 * @return the index value as an int
	 */ 
	private int getColumnIndexForName(sheetObject, sheetName, columnName, colCount, rowOffset) {
		int columnIdx = -1
		for (int index = 0; index <= colCount; index++) {
			if(WorkbookUtil.getStringCellValue(sheetObject, index, 0 ) == columnName) {
				columnIdx = index
				break
			}
		}
		if (columnIdx == -1) {
			throw new RuntimeException("unable to find '$columnName' column in sheet '$sheetName'")
		}
		return columnIdx
	}

	// Method process one of the asset class sheets
	private Map processSheet(project, userLogin, projectCustomLabels, dataTransferSet, workbook, sheetName, assetIdColName, assetNameColName, headerRowNum, domainName, timeOfExport) {

		Map results = initializeImportResultsMap()
		try {
			Map sheetInfo = getSheetInfo(project, workbook, sheetName, assetIdColName, 'Name', headerRowNum, dataTransferSet)
			DataTransferBatch dataTransferBatch = createTransferBatch(project, userLogin, dataTransferSet, domainName, sheetInfo.assetCount, timeOfExport)
			if (! dataTransferBatch) {
				failForTransferBatchError(assetSheetName)
			}

			importSheetValues(results, dataTransferBatch, projectCustomLabels, sheetInfo)

			// dataTransferBatch.

			log.debug "processSheet() sheet $sheetName results = $results"
		} catch (e) {
			log.debug "import() exception : ${ExceptionUtil.stackTraceToString(e)}"
			results.errors << "Sheet $sheetName failed to process - ${e.getMessage()}"
		}

		return results
	}

	/**
	 * This method iterates over the spreadsheet rows and loads each of the cells into the DataTransferValue table
	 * @param results - the map used to track errors, skipped rows and count of what was added
	 * @param dataTransferBatch - the batch to insert the rows into
	 * @param projectCustomLabels - the custom label values for the project
	 * @param sheetInfo - the map of all of the sheet information
	 * @return a Map containing the following elements
	 *		List errors - a list of errors
	 *		List skipped - a list of skipped rows
	 *		Integer added - a count of rows added
	 */
	private Map importSheetValues(
		Map results,
		DataTransferBatch dataTransferBatch,
		Map projectCustomLabels,
		Map sheetInfo
	) {

		Sheet sheetObject = sheetInfo.sheet
		Map colNamesOrdinalMap = sheetInfo.colNamesOrdinalMap
		int assetNameColIndex = sheetInfo.nameColumnIndex
		String assetSheetName = sheetInfo.sheetName

log.debug "importSheetValues() sheetInfo=sheetInfo" 

		Project project = dataTransferBatch.project

		// Verify that the sheet has the Asset Id Column by name that we are expecting
		if (! colNamesOrdinalMap.containsKey(sheetInfo.assetIdColumnLabel)) {
			results.errors << "$assetSheetName Sheet - missing asset id column name '${sheetInfo.assetIdColumnLabel}'"
		} else {

			results.rowsProcessed = sheetInfo.rowCount

			// Iterate over each row in the spreadsheet
			for( int r = 1; r <= sheetInfo.rowCount ; r++ ) {
				boolean rowHasErrors = false
				String errorMsg
				def assetId
				StringBuffer sqlValues = new StringBuffer()

				// Make sure that the asset has the mandatory name
				def assetName = WorkbookUtil.getStringCellValue(sheetObject, assetNameColIndex, r )
				if (! assetName) {
					errorMsg = "missing required 'name'"
					results.errors << "$assetSheetName [row ${( r + 1 )}] - $errorMsg"
					rowHasErrors = true
				} else { 

					// Now check to see if the asset references a pre-existing asset by id #
					assetId = WorkbookUtil.getStringCellValue(sheetObject, 0, r ) 
					if (assetId) {
						// Switch to a positive long and if null then it is bogus
						Long id = NumberUtil.toPositiveLong(assetId)
						if (id == null) {
							errorMsg = "invalid assetId format '$assetId'"
						} else {
							def asset = AssetEntity.get(id)
							if (! asset) {
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
						results.errors << "$assetSheetName [row ${( r + 1 )}] - $errorMsg"
						continue
					}

					for ( int cols = 0; cols < sheetInfo.columnCount; cols++ ) {
						String attribName
						String columnHeader = WorkbookUtil.getStringCellValue(sheetObject, cols, 0 )
						if (projectCustomLabels.containsKey(columnHeader)) {
							// A custom column that renamed
							attribName = projectCustomLabels[columnHeader]
						} else {
							attribName = columnHeader
						}
						def dtaAttrib = sheetInfo.dtaMapList.find{ it.columnName == attribName }

						if ( dtaAttrib != null ) {

							// Add the SQL VALUES(...) to the sqlValues StringBuffer for the current spreadsheet cell
							errorMsg = rowToImportValues(sqlValues, sheetObject, r, cols, dtaAttrib, assetId, dataTransferBatch.id)
							if (errorMsg) {
								rowHasErrors = true
								results.errors << "$assetSheetName [row ${( r + 1 )}] - $errorMsg"
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
						log.warn "importSheetValues() insert failed ${e.getMessage()} (sheet:$assetSheetName, row:$r) - ${ExceptionUtil.stackTraceToString(e)}"
						errorMsg = "Failed to insert data due to ${e.getMessage()}"
					}
				}

				if (errorMsg) {
					results.errors << "$assetSheetName [row ${( r + 1 )}] - $errorMsg"	
				}

			} // for r

			results.summary = "${results.rowsProcessed} Rows read, $results.added Loaded, ${results.errors.size()} Errored"

		}

		return results
	}

	/**
	 * Returns the Map used to track the results of imports for each tab
	 * @return map template of import stats/data
	 */
	private Map initializeImportResultsMap() {
		return [ errors: [], skipped: [], summary: '', added:0 ]
	}

	/**
	 * To upload the Data from the ExcelSheet
	 * @param DataTransferSet,Project,Excel Sheet 
	 * @return currentPage( assetImport Page)
	 */
	def upload() {

		// ----------------------------------------------
		// Actual start of the method
		// ----------------------------------------------

		def (project, userLogin) = controllerService.getProjectAndUserForPage(this, 'Import')
		if (!project) {
			warnMsg=flash.message
			flash.message=null
			forward (action:forwardAction, params:[error:warnMsg])
			return
		}

		// ------
		// Some variables that are referenced by the following closures
		// ------
		def flagToManageBatches = false

		// URL action to forward to if there is an error
		String forwardAction = 'assetImport'

		// List of all of the error/warning messages tracked during the import
		List errorMsgList = []

		// List of all of the rows that were skipped
		List skipped = []

		// The list of sheets that use the common import process (or at least for reporting Dependencies, Cabling, Comments)
		List sheetList = ['Devices', 'Applications', 'Databases', 'Storage', 'Dependencies', 'Cabling', 'Comments']
		// This will retain the results from the various spreadsheet tabs that use the common import process
		Map uploadResults = [:]
		// Initialize the results
		sheetList.each { uploadResults[it] = [addedCount:0, skippedCount:0, processed:false, summary:''] }
		
		// ------
		// The following section are a few Closures to help simplify the code
		// ------

		// closure used to redirect user when creation of TransferBatch fail
		def failForTransferBatchError = { sheetName ->
			forward action:forwardAction, params: [
				error: "Failed to create import batch for the '$sheetName' tab. Please contact support if the problem persists."
			]
			return
		}

		// closure used to redirect user with an error message for sever issues
		def failWithError = { message -> 
			log.error "upload() $userLogin was $message"
			forward action:forwardAction, params: [ error: message ]
		}

		// A closure to track the results of the different sheets being processed
		def processResults = { theSheetName, theResults ->
			if (! uploadResults.containsKey(theSheetName) ) {
				failWithError "Unhandled Sheet '$theSheetName' - please contact support"
			}

			uploadResults[theSheetName].with {
				addedCount = theResults.added
				skippedCount = theResults.skipped?.size() ?: 0
				processed = true
				summary = theResults.summary
				errorList = theResults.errors
				erroredCount = (errorList?.size() ?: 0)
			}
			
			if (theResults.skipped) {
				skipped.addAll(theResults.skipped)
			}
			if (theResults.errors) {
				errorMsgList.addAll(theResults.errors)
			}

			// Set flag so user is later prompted to process the batch(es)
			if (added > 0 ) {
				flagToManageBatches = true
			}
		}

		session.setAttribute("BATCH_ID",0)
		session.setAttribute("TOTAL_ASSETS",0)

		def projectId = project.id
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		
		if (! params.dataTransferSet) {
			failWithError 'Import request was missing expected parameter(s)'
			return
		}

		DataTransferSet dataTransferSet = DataTransferSet.findById( params.dataTransferSet )
		if (! dataTransferSet) {
			failWithError 'Unable to locate Data Import definition for ${params.dataTransferSet}'
		}

/*
		// List serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSet, "Devices" )
		List appDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSet, "Applications" )
		List databaseDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSet, "Databases" )
		List filesDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSet, "Files" )

		List appColumnslist = getColumnNamesForDTAMap(appDTAMap, project)
		List databaseColumnslist = getColumnNamesForDTAMap(databaseDTAMap, project)
		List filesColumnslist = getColumnNamesForDTAMap(filesDTAMap, project)
*/

		// Contains map of the custom fields name values to match with the spreadsheet
		Map projectCustomLabels = new HashMap()
		for (int i = 1; i<= Project.CUSTOM_FIELD_COUNT; i++) {
			String pcKey = "custom${i}"
			if ( project[ pcKey] ) {
				projectCustomLabels.put(project[pcKey], "Custom${i}")
			}
		}
				
		// Get the uploaded spreadsheet file
		MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
		CommonsMultipartFile file = ( CommonsMultipartFile ) mpr.getFile("file")

		// create workbook
		def workbook
		def titleSheet
		def sheetNameMap = ['Title','Applications','Devices','Databases','Storage','Dependencies','Cabling']
		def appNameMap = [:]
		def databaseNameMap = [:]
		def filesNameMap = [:]
		Date exportTime
		def dataTransferAttributeMapSheetName
		int devicesAdded  = 0
		int appAdded   = 0
		int dbAdded  = 0
		int filesAdded = 0
		def currentUser = null

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
			workbook = new HSSFWorkbook( file.inputStream );
			def sheetNames = WorkbookUtil.getSheetNames(workbook)
			def flag = 0
			def sheetNamesLength = sheetNames.size()
			for( int i=0;  i < sheetNamesLength; i++ ) {
				if ( sheetNameMap.contains(sheetNames[i].trim()) ) {
					flag = 1
				}
			}

			// Get the title sheet
			titleSheet = workbook.getSheet( "Title" )

			if (titleSheet != null) {			
				try {
					exportTime = TimeUtil.parseDateTime(getSession(), WorkbookUtil.getStringCellValue(titleSheet, 1, 5)) 
				} catch ( Exception e) {
					log.info "Was unable to read the datetime for 'Export on': " + e.message
					failWithError "The 'Exported On' datetime was not found or was invalid in the Title sheet"
					return
				}
			} else {
				failWithError 'The required Title sheet was not found in the uploaded spreadsheet'
				return
			}

			def hibernateSession = sessionFactory.getCurrentSession()

			DataTransferBatch dataTransferBatch
			Map importResults
			String sheetName, domainClassName

			// ----
			// Devices Sheet
			// ----
			if (params.asset == 'asset') {
				sheetName='Devices'
				domainClassName = 'AssetEntity'
				importResults = processSheet(project, userLogin, projectCustomLabels, dataTransferSet, workbook, sheetName, 'assetId', 'Name', 0, domainClassName, exportTime)
				processResults(sheetName, importResults)
			}

			// ----
			// Applications Sheet
			// ----
			if (params.application == 'application') {
				sheetName='Applications'
				domainClassName = 'Application'
				importResults = processSheet(project, userLogin, projectCustomLabels, dataTransferSet, workbook, sheetName, 'appId', 'Name', 0, domainClassName, exportTime)
				processResults(sheetName, importResults)
			}

			// ----
			// Database Sheet
			// ----
			if (params.database == 'database') {
				sheetName='Databases'
				domainClassName = 'Database'
				importResults = processSheet(project, userLogin, projectCustomLabels, dataTransferSet, workbook, sheetName, 'dbId', 'Name', 0, domainClassName, exportTime)
				processResults(sheetName, importResults)
			}

			// ----
			// Storage Sheet
			// ----
			if (params.storage == 'storage') {
				sheetName='Storage'
				domainClassName = 'Files'
				importResults = processSheet(project, userLogin, projectCustomLabels, dataTransferSet, workbook, sheetName, 'filesId', 'Name', 0, domainClassName, exportTime)
				processResults(sheetName, importResults)
			}

			// ----
			// Process Dependencies
			// ----
			if (params.dependency=='dependency') {
				def dependencySheet = workbook.getSheet( "Dependencies" )

				def dependencySheetRow = dependencySheet.getLastRowNum()

				for (int row = 1; row <= dependencySheetRow; row++) {
					// Check AssetName column (C) for not being blank
					def name = WorkbookUtil.getStringCellValue(dependencySheet, 2, row )
					if (name) {
						dependencyCount++
					}
				}

				// Set the session for progress meter
				session.setAttribute("TOTAL_ASSETS",dependencyCount)

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

				def assetDepTypeList = AssetDependencyType.getList()
				def assetDepStatusList = AssetDependencyStatus.getList()

				def lookupValue = { value, list ->
				    def result = "Unknown"
				    list.each{
				        if(it.equalsIgnoreCase(value)){
				            result = it
				        }
				    }
				    return result

				}

				for ( int r = 1; r <= dependencySheetRow ; r++ ) {
					// Assume that the dependency is skipped and we'll decrement when the row is saved at the bottom
					dependencySkipped++

					int rowNum = r+1

					if (GormUtil.flushAndClearSession(hibernateSession, rowNum)) {
						(project, userLogin) = GormUtil.mergeWithSession(hibernateSession, [project, userLogin])
					}

					def assetId = null
					def assetIdCell = WorkbookUtil.getStringCellValue(dependencySheet, 1, r )
					if (assetIdCell) {
						assetId = NumberUtils.toDouble(assetIdCell.replace("'","\\'"), 0).round()
					}

					String assetName
					String assetClass
					if (! assetId) {
						assetName = WorkbookUtil.getStringCellValue(dependencySheet, 2, r ).replace("'","\\'")
						assetClass = WorkbookUtil.getStringCellValue(dependencySheet, 3, r ).replace("'","\\'")
						
						if (! assetName) {
							dependencyError "Missing AssetId (in B$rowNum) or AssetName (in C$rowNum)"
							continue
						}
					}

					// ----
					// Try to lookup the AssetDependency record based on the depId (column A)
					// ----
					Long depId
					String depIdCell = WorkbookUtil.getStringCellValue(dependencySheet, 0, r )
					AssetDependency assetDep
					if (depIdCell) {
						depId = NumberUtil.toPositiveLong(depIdCell, -1)
						if (depId == -1) {
							importResults.errors << "Invalid AssetDependencyId number '$depIdCell' (in A$rowNum)"
							continue
						}
						if (depId > 0) {
							assetDep = AssetDependency.get(depId)
							if (! assetDep) {
								dependencyError "AssetDependencyId '$depId' not found (in A$rowNum)"
								continue
							}
							if (assetDep.asset.project.id != project.id) {
								securityService.reportViolation("attempted to access assetDependency ($depId) not assigned to project (${project.id})", userLogin)
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
						if (!asset){
							dependencyError "Dependency asset by AssetId ($assetId) not found (in B$rowNum)"
							continue
						}
						if (asset.project.id != project.id) {
							securityService.reportViolation("attempted to access asset ($assetId) not assigned to project (${project.id})", userLogin)
							dependencyError "Invalid reference of AssetId ($assetId) (row $rowNum)"
							continue
						}
					} else {
						def assets = AssetEntity.findAllByAssetNameAndProject(assetName, project)
						if(assets.size() == 0) {
							dependencyError "Asset not found by AssetName '$assetName' (row $rowNum)"
							continue
						} else if(assets.size() > 1){
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
					def dependencyId = NumberUtils.toDouble(WorkbookUtil.getStringCellValue(dependencySheet, 4, r ).replace("'","\\'"), 0).round()
					if (dependencyId) {
						dependent = AssetEntity.get(dependencyId)
						if (!dependent) {
							dependencyError "Asset by DependentId ($dependencyId) not found (row $rowNum)"
							continue
						}
						if (dependent.project.id != project.id) {
							securityService.reportViolation("attempted to access dependent ($dependencyId) not assigned to project (${project.id})", userLogin)
							dependencyError "Invalid reference of DependentId ($dependencyId) (row $rowNum)"
							continue
						}
					} else {
						def depName = WorkbookUtil.getStringCellValue(dependencySheet, 5, r ).replace("'","\\'")
						def depClass = WorkbookUtil.getStringCellValue(dependencySheet, 6, r ).replace("'","\\'")
						def assets = AssetEntity.findAllByAssetNameAndProject(depName, project)
						if (assets.size() == 0){
							dependencyError "Asset by DependentName ($depName) not found (row $rowNum)"
							continue
						} else if(assets.size() > 1) {
							dependent = assets.find { it.assetType == depClass }
							if (dependent == null) {
								dependencyError "Asset by DependentName '$depName' found duplicated names (row $rowNum)"
								continue
							}
						} else {
							dependent = assets[0]
						}
					}

					def isNew = false
					if (!assetDep) {

						// Try finding the dependency by the asset and the dependent
						assetDep = AssetDependency.findByAssetAndDependent(asset, dependent)

						if (! assetDep) {						
							assetDep = new AssetDependency()
							assetDep.createdBy = userLogin.person
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
						assetDep.type = lookupValue(tmpType, assetDepTypeList)
						
						assetDep.dataFlowFreq = WorkbookUtil.getStringCellValue(dependencySheet, 8, r, "").replace("'","\\'") ?: 
							(isNew ? "Unknown" : assetDep.dataFlowFreq)
						assetDep.dataFlowDirection = WorkbookUtil.getStringCellValue(dependencySheet, 9, r, "").replace("'","\\'") ?: 
							(isNew ? "Unknown" : assetDep.dataFlowDirection)
						
						def tmpStatus = WorkbookUtil.getStringCellValue(dependencySheet,10, r , "").replace("'","\\'") ?: 
							(isNew ? "Unknown" : assetDep.status)
						assetDep.status = lookupValue(tmpStatus, assetDepStatusList)
						
						def depComment = WorkbookUtil.getStringCellValue(dependencySheet, 11, r , "").replace("'","\\'")
						def length = depComment.length()
						if (length > 255) {
							depComment = StringUtil.ellipsis(depComment,255)
							dependencyError  "The comment was trimmed to 255 characters (row $rowNum)"
						}

						assetDep.comment = depComment
						assetDep.c1 = WorkbookUtil.getStringCellValue(dependencySheet, 12, r , "").replace("'","\\'")
						assetDep.c2 = WorkbookUtil.getStringCellValue(dependencySheet, 13, r , "").replace("'","\\'")
						assetDep.c3 = WorkbookUtil.getStringCellValue(dependencySheet,14, r , "").replace("'","\\'")
						assetDep.c4 = WorkbookUtil.getStringCellValue(dependencySheet, 15, r , "").replace("'","\\'")
						assetDep.updatedBy = userLogin.person

						// Make sure that there are no domain constraint errors
						if (assetDep.hasErrors()) {
							dependencyError "Validation errors exist (row $rowNum) : ${GormUtil.allErrorsString(assetDep)}"
							continue
						} 

						if (! isNew && ! assetDep.dirtyPropertyNames) {
							dependencyUnchanged++
							dependencySkipped--
							continue
						} else {
							log.info "Changed fields ${assetDep.dirtyPropertyNames}"
						}

						// Attempt to save the record
						if (!assetDep.save(flush:true)){
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

			} // Process Dependencies
			
			// ----
			// Process Cabling
			// ----
			if (params.cabling=='cable'){
				def cablingSheet = workbook.getSheet( "Cabling" )
				def cablingSheetRow = cablingSheet.getLastRowNum()
				session.setAttribute("TOTAL_ASSETS",cablingSheetRow)
				
				def resultMap = assetEntityService.saveImportCables(cablingSheet)
				importResults = initializeImportResultsMap()

				importResults.rowsProcessed = cablingSheetRow
				importResults.errors = resultMap.warnMsg
				importResults.summary = "${importResults.rowsProcessed } Rows read, $resultMap.cablingUpdated Updated, $resultMap.cablingSkipped Skipped, ${importResults.errors.size()} Errored"
				processResults('Cabling', importResults)

			}
			
			// ----
			// Process Comments Imports
			// ----
			if (params.comment=='comment') {
				def commentsSheet = workbook.getSheet( "Comments" )

				int commentAdded=0
				int commentUpdated=0	
				int commentUnchanged=0		
 				int commentCount=commentsSheet.getLastRowNum()
				//def skippedUpdated=0
				//def skippedAdded=0

				session.setAttribute("TOTAL_ASSETS",commentCount)

				importResults = initializeImportResultsMap()
				importResults.rowsProcessed = commentCount

				// TODO : JPM 11/2014 : Refactor the lookup of PartyGroup.get(18) to be TDS lookup - see TM-3570
				List staffList = partyRelationshipService.getAllCompaniesStaffPersons([ project.client, PartyGroup.get(18) ])
				// List staffList // not used in the PersonService at this time
				int r
				int rowNum
				try {
					for ( r = 1; r <= commentCount ; r++ ) {
						rowNum = r + 1

						// Clear the Hibernate Session periodically for performance purposes
						if (GormUtil.flushAndClearSession(hibernateSession, rowNum)) {
							(project, userLogin) = GormUtil.mergeWithSession(hibernateSession, [project, userLogin])
						}

						def recordForAddition = false
						int cols=0
						def commentIdImported = WorkbookUtil.getStringCellValue(commentsSheet, cols, r ).replace("'","\\'")
						def assetComment
						if (commentIdImported) {
							def commentId = NumberUtil.toPositiveLong(commentIdImported, -1)
							if (commentId < 1) {
								//skippedUpdated++
								importResults.errors << "Invalid commentId number'$commentIdImported' (row ${rowNum})"
								continue
							}
							assetComment = AssetComment.get( commentId )
							if (!assetComment) {
								//skippedUpdated++
								importResults.errors << "CommentId '$commentId' was not found (row ${rowNum})"
								continue
							}
							if (assetComment.project != project) {
								securityService.reportViolation("attempted to access assetComment ($commentId) not assigned to project (${project.id})", userLogin)
								importResults.errors << "Invalid CommentId '$commentIdImported' was specified (row ${rowNum})"
								continue
							}
						} else {
							assetComment = new AssetComment()
							assetComment.project = project
							recordForAddition = true
						}

						assetComment.commentType = AssetCommentType.COMMENT
						
						String assetIdStr = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r ).replace("'","\\'")
						Long assetId = NumberUtil.toPositiveLong(assetIdStr, -1)
						if (assetId > 0) {
							AssetEntity assetEntity = AssetEntity.findByIdAndProject(assetId, project)
							if (assetEntity) {
								assetComment.assetEntity = assetEntity
							} else {
								importResults.errors << "The assetId '$assetIdStr' was not found (row ${rowNum})"
								//recordForAddition ? skippedAdded++ : skippedUpdated++
								continue
							}
						} else {
							importResults.errors << "An Invalid assetId '$assetIdStr' was specified (row ${rowNum})"
							//recordForAddition ? skippedAdded++ : skippedUpdated++
							continue
						}

						// Grab the category
						def categoryInput = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r )?.replace("'","\\'")?.toLowerCase()?.trim()
						if (AssetCommentCategory.list.contains(categoryInput)){
							assetComment.category =  categoryInput ?: AssetCommentCategory.GENERAL
						} else {
							//recordForAddition ? skippedAdded++ : skippedUpdated++
							importResults.errors << "Invalid category '$categoryInput' specified (row ${rowNum})"
							continue
						}
						
						// Try reading the created date as a date and if that fails try as a string and parse
						cols++
						def dateCreated
						def createdDateInput = WorkbookUtil.getDateCellValue(commentsSheet, cols, r, getSession(), TimeUtil.FORMAT_DATE)	
						if (createdDateInput) {
							dateCreated = createdDateInput
						} else {
							// Try parsing the input
							createdDateInput = WorkbookUtil.getStringCellValue(commentsSheet, cols, r )?.replace("'","\\'")
							if (createdDateInput) {
								dateCreated = TimeUtil.parseDate(getSession(), createdDateInput)
								if ( ! (dateCreated instanceof Date) ) {
									importResults.errors << "Invalid Created Date '$createdDateInput' (row ${rowNum})"
									continue
								}
							} else if (recordForAddition) {
								dateCreated = new Date()
							}
						}

						// We need to keep track of the dateCreated change as it turns out the dirtyPropertyNames will NOT return this property
						boolean dateChanged = false
						if (dateCreated) {
							dateChanged = dateCreated != assetComment.dateCreated
							assetComment.dateCreated = dateCreated
						}				
							
						// Get the createdBy person					
						def createdByImported = StringUtils.strip(WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r ))
						if (currentUser == null) {
							currentUser = securityService.getUserLoginPerson()
						}
						def person = createdByImported ? personService.findPerson(createdByImported, project, staffList, false)?.person : currentUser
						
						if (person) {
							assetComment.createdBy = person
						} else {
							importResults.errors <<  "Created by person '$createdByImported' not found (row ${rowNum})"
							continue
						}
						
						if (! personService.hasAccessToProject(person, project)) {
							importResults.errors << "Created by person '$person' does not have access to project (row ${rowNum})"						
							continue
						}

						assetComment.comment = WorkbookUtil.getStringCellValue(commentsSheet, ++cols, r )

						List dirty = assetComment.getDirtyPropertyNames()
						if ( !recordForAddition && dirty.size() == 0 && !dateChanged) {
							commentUnchanged++
							continue
						}

						if (! assetComment.save()) {
							importResults.errors << "Save failed (row ${rowNum}) : ${GormUtil.allErrorsString(assetComment)}"
						} else {
							if (recordForAddition) {
								commentAdded++ 
							} else {
								commentUpdated++
							}
						}
					}
				} catch (e) {
					importResults.errors << "Import Failed at row $rowNum due to error '${e.getMessage()}'"
					log.error "Comment Import failed for $userLogin on row $rowNum : ${ExceptionUtil.stackTraceToString(e)}"
				}
				importResults.summary = "${importResults.rowsProcessed} Rows read, $commentAdded Added, $commentUpdated Updated, $commentUnchanged Unchanged, ${importResults.errors.size()} Errors"
				processResults('Comments', importResults)

			} // Process Comment Imports


			// -----
			// Construct the results detail to display to the user
			// -----
			StringBuffer message = new StringBuffer( "<b>Spreadsheet import was successful</b><br>\n")
			if (flagToManageBatches) {
				message.append("<p>Please click the Manage Batches below to review and post these changes</p><br>\n")
			}
			message.append("<br><p>Results: <ul>\n")
			sheetList.each { 
				if (uploadResults[it].processed) {
					if (uploadResults[it].summary) {
						message.append("<li>$it: ${uploadResults[it].summary}</li>\n")
					} else {
						message.append("<li>$it: ${uploadResults[it].addedCount} loaded</li>\n")
					}
				}
			}
			message.append("</ul><br>\n")

			// Handle the errors and skipped rows
			message.append("<p>Errors: <ul>\n")
			sheetList.each { 
				if (uploadResults[it].processed) {
					if (uploadResults[it].errorList.size()) {
						message.append("<li>$it:<ul>")
						message.append( uploadResults[it].errorList.collect { "<li>$it</li>"}.join("\n") )
						message.append("</li></ul>\n")
					}
				}
			}
/*
			if (errorMsgList.size()) {
				message.append( errorMsgList.collect { "<li>$it</li>"}.join("\n") )
			}
*/
			if (skipped.size()) {
				message.append("</ul></p>\n<br><p>Rows Skipped: <ul>\n")
				message.append( "<li>${skipped.size()} spreadsheet row${skipped.size()==0 ? ' was' : 's were'} skipped: <ul>")
				message.append( skipped.collect { "<li>$it</li>" }.join("\n") )
			}

			message.append("</ul></p>\n")				
			
			forward action:forwardAction, params: [message: message.toString()]

		} catch( NumberFormatException e ) {
			log.error "AssetImport Failed ${ExceptionUtil.stackTraceToString(e)}"
			forward action:forwardAction, params: [error: e]
		} catch( Exception e ) {
			log.error "AssetImport Failed ${ExceptionUtil.stackTraceToString(e)}"
			forward action:forwardAction, params: [error: e]
		}
	}
	
	def export() {
		if (!controllerService.checkPermission(this, 'Export')){
			return
		}
		def key = "AssetExport-" + UUID.randomUUID().toString()
		progressService.create(key)
		
		def username = securityService.getUserLogin().username
		def projectId = RequestContextHolder.currentRequestAttributes().getSession().getAttribute( "CURR_PROJ" )?.CURR_PROJ
		
		
		def jobName = "TM-" + key
		log.info "Initiate Export"
		
		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTriggerImpl(jobName, null, new Date(System.currentTimeMillis() + 2000) )
		trigger.jobDataMap.putAll(params)
		
		def bundle = request.getParameterValues( "bundle" )
		trigger.jobDataMap.put('bundle', bundle)
		trigger.jobDataMap.put('key', key)
		trigger.jobDataMap.put('username', username)
		trigger.jobDataMap.put('projectId', projectId)
		trigger.jobDataMap.put('tzId', getSession().getAttribute( TimeUtil.TIMEZONE_ATTR )?.CURR_TZ)
		trigger.jobDataMap.put('userDTFormat', getSession().getAttribute( TimeUtil.DATE_TIME_FORMAT_ATTR )?.CURR_DT_FORMAT)

		trigger.setJobName('ExportAssetEntityJob')
		trigger.setJobGroup('tdstm-export-asset')
		quartzScheduler.scheduleJob(trigger)

		progressService.update(key, 1, 'In progress')
		
		render(ServiceResults.success(['key' : key]) as JSON)
	}
	
	def downloadExport() {
		if (!controllerService.checkPermission(this, 'Export')){
			return
		}
		def key = params.key
		def filename = progressService.getData(key, 'filename')
		def header = progressService.getData(key, 'header')
		
		java.io.File file = new java.io.File(filename);
		FileInputStream io = new FileInputStream(file)
		
		response.setContentType( "application/vnd.ms-excel" )
		response.setHeader("Content-Disposition", header)
		
		OutputStream out = response.getOutputStream();
		IOUtils.copy(io, out);
		out.flush();
		IOUtils.closeQuietly(io);
		IOUtils.closeQuietly(out);
	}

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	/**
	 * Used for the assetEntity List to load the initial model. The list subsequently calls listJson to get the 
	 * actual data which is rendered by JQ-Grid
	 * @param project, filter, number of properties
	 * @return model data to initate the asset list
	 **/
	def list() {
		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		def userLogin = securityService.getUserLogin()
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		
		def model = assetEntityService.getDeviceModelForList(project, userLogin, session, params, tzId)

		return model
	}
	
	/**
	 * This method is used by JQgrid to load assetList
	 */
	def listJson() {
		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		def userLogin = securityService.getUserLogin()
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ

		def data = assetEntityService.getDeviceDataForList(project, userLogin, session, params, tzId)		
		render data as JSON
	}
	
	/* ----------------------------------------
	 * delete assetEntity
	 * @param assetEntityId
	 * @return assetEntityList
	 * --------------------------------------- */
	def delete() {
		def redirectAsset = params.dstPath
		def assetEntityInstance = AssetEntity.get( params.id )
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if(assetEntityInstance) {
			assetEntityService.deleteAsset(assetEntityInstance)
			assetEntityInstance.delete()
			flash.message = "AssetEntity ${assetEntityInstance.assetName} deleted"
			
			if(redirectAsset?.contains("room_")){
				def newredirectAsset = redirectAsset.split("_")
				redirectAsset = newredirectAsset[0]
				def rackId = newredirectAsset[1]
				session.setAttribute("RACK_ID", rackId)
			}
			switch(redirectAsset){
				case "room":
					redirect( controller:'room',action:'list' )
					break;
				case "rack":
					redirect( controller:'rackLayouts',action:'create' )
					break;
				case "application":
					redirect( controller:'application', action:'list')
					break;
				case "database":
					redirect( controller:'database', action:'list')
					break;
				case "files":
					redirect( controller:'files', action:'list')
					break;
				case "dependencyConsole":
					forward( action:'retrieveLists', params:[entity: 'server',dependencyBundle:session.getAttribute("dependencyBundle")])
					break;
				case "assetAudit":
					render "AssetEntity ${assetEntityInstance.assetName} deleted"
					break;
				default:
					redirect( action:'list')
			}

		}
		else {
			flash.message = "AssetEntity not found with id ${params.id}"
		}
	}

	/*--------------------------------------------------
	 * To remove the asset from project
	 * @param assetEntityId
	 * @author Mallikarjun
	 * @return assetList page
	 *-------------------------------------------------*/
	def remove() {
		def assetEntityInstance = AssetEntity.get( params.id )
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if(assetEntityInstance) {
			ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.asset = ${params.id}")
			ProjectTeam.executeUpdate("update ProjectTeam pt set pt.latestAsset = null where pt.latestAsset = ${params.id}")
			AssetEntity.executeUpdate("update AssetEntity ae set ae.moveBundle = null , ae.project = null , ae.sourceTeamMt = null , ae.targetTeamMt = null, ae.sourceTeamLog = null , ae.targetTeamLog = null, ae.sourceTeamSa = null , ae.targetTeamSa = null, ae.sourceTeamDba = null , ae.targetTeamDba = null where ae.id = ${params.id}")
			flash.message = "AssetEntity ${assetEntityInstance.assetName} Removed from Project"

		}
		else {
			flash.message = "AssetEntity not found with id ${params.id}"
		}
		redirect( action:'list' )
	}

	/**
	 * Used to handle the appropriate redirections
	 * 
	 */	
	private void redirectToReq(model, entity, redirectTo, saved, errorMsg="" ) {
		log.debug "**** redirectToReq to $redirectTo, $list"
		def project = securityService.getUserCurrentProject()

		switch (redirectTo) {
			case "room":
				  redirect( controller:'room', action:'list' )
				  break;
			case "rack":
				  session.setAttribute("USE_FILTERS", "true")
				  redirect( controller:'rackLayouts',action:'create' )
				  break;
			case "assetAudit":
				if (saved)
					render(template:'auditDetails',	model:[assetEntity:entity, source:model.source, assetType:model.assetType])
				else
					forward(action:'create', params:model)
				  break;
		    case "application":
				  redirect( controller:'application', action:'list')
				  break;
			case "database":
				  redirect( controller:'database', action:'list')
				  break;
			case "files":
				  redirect( controller:'files', action:'list')
				  break;
			case "listComment":
				  forward(action:'listComment')
				  break;
		    case "roomAudit":
				  forward(action:'show', params:[redirectTo:redirectTo, source:model.source, assetType:model.assetType])
				  break;
		    case "dependencyConsole":
				  forward(action:'retrieveLists', params:[entity:model.tabType,labelsList:model.labels, dependencyBundle:session.getAttribute("dependencyBundle")])
				  break;
			case "listTask":
				  render "Asset ${entity.assetName} updated."
				  break;
		    case "dependencies":
				  redirect(action:'listDependencies')
				  break;

			case 'list':
			default:
				// Handle results as standardized Ajax return value
				if (errorMsg.size()) {
					render ServiceResults.errors(errorMsg) as JSON
				} else if (!entity) {
					render ServiceResults.errors("Asset not returned") as JSON
				} else {
					model = deviceService.getModelForShow(project, entity, params)
					if (!model) {
						ServiceResults.errors("Asset model not loaded")
						return 
					}
					render(ServiceResults.success(model) as JSON)
				}
		  }
	}

	/*--------------------------------------------------------
	 * remote link for asset entity dialog.
	 *@param assetEntityId
	 *@author Mallikarjun
	 *@return retun to assetEntity to assetEntity Dialog
	 *--------------------------------------------------------- */
	def editShow() {
		def items = []
		def assetEntityInstance = AssetEntity.get( params.id )
		def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntityInstance.attributeSet.id order by eav.sortOrder ")
		def projectId = getSession().getAttribute( "CURR_PROJ" )?.CURR_PROJ
		def project = Project.findById( projectId )
		entityAttributeInstance.each{
			def attributeOptions = EavAttributeOption.findAllByAttribute( it.attribute,[sort:'value',order:'asc'] )
			def options = []
			attributeOptions.each{option ->
				options<<[option:option.value]
			}
			if( !bundleMoveAndClientTeams.contains(it.attribute.attributeCode) && it.attribute.attributeCode != "currentStatus" && it.attribute.attributeCode != "usize" ){
				def frontEndLabel = it.attribute.frontendLabel
				if( customLabels.contains( frontEndLabel ) ){
					frontEndLabel = project[it.attribute.attributeCode] ? project[it.attribute.attributeCode] : frontEndLabel
				}
				items << [label:frontEndLabel, attributeCode:it.attribute.attributeCode,
							frontendInput:it.attribute.frontendInput,
							options : options,
							value:assetEntityInstance.(it.attribute.attributeCode) ? assetEntityInstance.(it.attribute.attributeCode).toString() : "",
							bundleId:assetEntityInstance?.moveBundle?.id, modelId:assetEntityInstance?.model?.id,
							manufacturerId:assetEntityInstance?.manufacturer?.id]
			}
		}
		render items as JSON
	}

	/*To get the  Attributes
	 *@param attributeSet
	 *@author Lokanath
	 *@return attributes as a JSON Object 
	 */
	def retrieveAttributes() {
		def attributeSetId = params.attribSet
		def items = []
		def entityAttributeInstance = []
		if(attributeSetId != null &&  attributeSetId != ""){
			def attributeSetInstance = EavAttributeSet.findById( attributeSetId )
			//entityAttributeInstance =  EavEntityAttribute.findAllByEavAttributeSetOrderBySortOrder( attributeSetInstance )
			entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $attributeSetId order by eav.sortOrder ")
		}
		def projectId = getSession().getAttribute( "CURR_PROJ" )?.CURR_PROJ
		def project = Project.findById( projectId )
		entityAttributeInstance.each{
			def attributeOptions = EavAttributeOption.findAllByAttribute( it.attribute,[sort:'value',order:'asc'] )
			def options = []
			attributeOptions.each{option ->
				options<<[option:option.value]
			}
			if( it.attribute.attributeCode != "moveBundle" && !bundleMoveAndClientTeams.contains(it.attribute.attributeCode) && it.attribute.attributeCode != "currentStatus" && it.attribute.attributeCode != "usize"){
				def frontEndLabel = it.attribute.frontendLabel
				if( customLabels.contains( frontEndLabel ) ){
					frontEndLabel = project[it.attribute.attributeCode] ? project[it.attribute.attributeCode] : frontEndLabel
				}
				items<<[ label:frontEndLabel, attributeCode:it.attribute.attributeCode,
							frontendInput:it.attribute.frontendInput, options : options ]
			}
		}
		render items as JSON
	}
	/* --------------------------------------------------
	 * To get the  asset Attributes
	 * @param attributeSet
	 * @author Lokanath
	 * @return attributes as a JSON Object
	 * -----------------------------------------------------*/
	def retrieveAssetAttributes() {
		def assetId = params.assetId
		def items = []
		def entityAttributeInstance = []
		if(assetId != null &&  assetId != ""){
			def assetEntity = AssetEntity.findById( assetId )
			//entityAttributeInstance =  EavEntityAttribute.findAllByEavAttributeSetOrderBySortOrder( attributeSetInstance )
			entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntity.attributeSet.id order by eav.sortOrder ")
		}
		entityAttributeInstance.each{
			if( !bundleMoveAndClientTeams.contains(it.attribute.attributeCode) && it.attribute.attributeCode != "currentStatus" && it.attribute.attributeCode != "usize"){
				items<<[ attributeCode:it.attribute.attributeCode, frontendInput:it.attribute.frontendInput ]
			}
		}
		render items as JSON
	}
	/* ----------------------------------------------------------
	 * will return data for auto complete fields
	 * @param autocomplete param
	 * @author Lokanath
	 * @return autoCompletefield data as JSON
	 *-----------------------------------------------------------*/
	def retrieveAutoCompleteDate() {
		def autoCompAttribs = params.autoCompParams
		def data = []
		if(autoCompAttribs){
			def autoCompAttribsList = autoCompAttribs.split(",")
			def currProj = getSession().getAttribute( "CURR_PROJ" )
			def projectId = currProj.CURR_PROJ
			def project = Project.findById( projectId )
			autoCompAttribsList.each{
				def assetEntity = AssetEntity.executeQuery( "select distinct a.$it from AssetEntity a where a.owner = $project.client.id" )
				data<<[value:assetEntity , attributeCode : it]
			}
		}
		render data as JSON
	}
	/* ------------------------------------------------------------
	 * get comments for selected asset entity
	 * @param assetEntity
	 * @author Lokanath
	 * @return commentList as JSON
	 *-------------------------------------------------------------*/
	def listComments() {
		def assetEntityInstance = AssetEntity.get( params.id )
		def commentType = params.commentType;
		def assetCommentsInstance
		def canEditComments = true
		if (commentType) {
			if (commentType != 'comment') {
				commentType = 'issue'
			}
			canEditComments = userCanEditComments(commentType)
			assetCommentsInstance = AssetComment.findAllByAssetEntityAndCommentType( assetEntityInstance, commentType )
		} else {
			assetCommentsInstance = AssetComment.findAllByAssetEntity( assetEntityInstance )
		}
		def assetCommentsList = []
		def today = new Date()
		def css //= 'white'
		def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && userPreferenceService.getPreference("viewUnpublished") == 'true')
		assetCommentsInstance.each {
			css = it.dueDate < today ? 'Lightpink' : 'White'

			if (viewUnpublished || it.isPublished)
				assetCommentsList <<[ commentInstance : it, assetEntityId : it.assetEntity.id,cssClass:css, 
										assetName: it.assetEntity.assetName,assetType:it.assetEntity.assetType,
										assignedTo: it.assignedTo?it.assignedTo.toString():'', role: it.role?it.role:'',
										canEditComments: canEditComments]

		}
		render assetCommentsList as JSON
	}
	/* ------------------------------------------------------------------------
	 * return the comment record
	 * @param assetCommentId
	 * @author Lokanath
	 * @return assetCommentList
	 * ---------------------------------------------------------------------- */
	def showComment() {
		def commentList = []
		def personResolvedObj
		def personCreateObj
		def dtCreated
		def dtResolved

		def assetComment = AssetComment.get(params.id)
		if(assetComment){
			if(assetComment.createdBy){
				personCreateObj = Person.find("from Person p where p.id = $assetComment.createdBy.id")?.toString()
				dtCreated = TimeUtil.formatDateTime(getSession(), assetComment.dateCreated);
			}
			if (assetComment.dateResolved) {
				personResolvedObj = Person.find("from Person p where p.id = $assetComment.resolvedBy.id")?.toString()
				dtResolved = TimeUtil.formatDateTime(getSession(), assetComment.dateResolved);
			}
			
			def etStart =  assetComment.estStart ? TimeUtil.formatDateTime(getSession(), assetComment.estStart) : ''
			
			def etFinish = assetComment.estFinish ? TimeUtil.formatDateTime(getSession(), assetComment.estFinish) : ''
			
			def atStart = assetComment.actStart ? TimeUtil.formatDateTime(getSession(), assetComment.actStart) : ''
			
		    def dueDate = assetComment.dueDate ? TimeUtil.formatDate(getSession(), assetComment.dueDate): ''
	
			def workflowTransition = assetComment?.workflowTransition
			def workflow = workflowTransition?.name
			
			def noteList = assetComment.notes.sort{it.dateCreated}
			def notes = []
			noteList.each {
				def dateCreated = it.dateCreated ? TimeUtil.formatDateTime(getSession(), it.dateCreated, TimeUtil.FORMAT_DATE_TIME_3) : ''
				notes << [ dateCreated , it.createdBy.toString() ,it.note]
			}
			
			// Get the name of the User Role by Name to display
			def roles = securityService.getRoleName(assetComment.role)

			def instructionsLinkURL = null
			def instructionsLinkLabel = null

			if(assetComment.instructionsLink){
				instructionsLinkURL = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)[1]
				instructionsLinkLabel = HtmlUtil.parseMarkupURL(assetComment.instructionsLink)[0]
			}

			
			
			def predecessorTable = ""
            def predecessorList = []
			def taskDependencies = assetComment.taskDependencies
			if (taskDependencies.size() > 0) {
				taskDependencies = taskDependencies.sort{ it.predecessor.taskNumber }
				predecessorTable = new StringBuffer('<table cellspacing="0" style="border:0px;"><tbody>')
				taskDependencies.each() { taskDep ->
					def task = taskDep.predecessor
					def css = taskService.getCssClassForStatus(task.status)
					def taskDesc = task.comment?.length()>50 ? task.comment.substring(0,50): task.comment
                    predecessorList << [id: taskDep.id, taskId: task.id, category: task.category, desc: taskDesc, taskNumber: task.taskNumber, status: task.status]
					predecessorTable.append("""<tr class="${css}" style="cursor:pointer;" onClick="showAssetComment(${task.id}, 'show')"><td>${task.category}</td><td>${task.taskNumber ? task.taskNumber+':' :''}${taskDesc}</td></tr>""")
			    }
				predecessorTable.append('</tbody></table>')
			}
			def taskSuccessors = TaskDependency.findAllByPredecessor( assetComment )
			def successorsCount= taskSuccessors.size()
			def predecessorsCount = taskDependencies.size()
			def successorTable = ""
            def successorList = []
			if (taskSuccessors.size() > 0) {
				taskSuccessors = taskSuccessors.sort{ it.assetComment.taskNumber }
				successorTable = new StringBuffer('<table  cellspacing="0" style="border:0px;" ><tbody>')
				taskSuccessors.each() { successor ->
					def task = successor.assetComment
					def css = taskService.getCssClassForStatus(task.status)
                    def succDesc = task.comment?.length()>50 ? task.comment.substring(0,50): task.comment
                    successorList << [id: successor.id, taskId: task.id, category: task.category, desc: succDesc, taskNumber: task.taskNumber, status: task.status]
					successorTable.append("""<tr class="${css}" style="cursor:pointer;" onClick="showAssetComment(${task.id}, 'show')"><td>${task.category}</td><td>${task}</td>""")
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
				predecessorTable:predecessorTable, 
				successorTable:successorTable,
				cssForCommentStatus: cssForCommentStatus, 
				statusWarn: taskService.canChangeStatus ( assetComment ) ? 0 : 1, 
				successorsCount: successorsCount, 
				predecessorsCount: predecessorsCount, 
				assetId: assetComment.assetEntity?.id ?: "" ,
				assetType: assetComment.assetEntity?.assetType,
				assetClass: assetComment.assetEntity?.assetClass.toString(),
				predecessorList: predecessorList, 
				successorList: successorList,
				instructionsLinkURL: instructionsLinkURL ?: "",
				instructionsLinkLabel: instructionsLinkLabel ?: "",
				canEdit: canEdit 
			]
		} else {
		 def errorMsg = " Task Not Found : Was unable to find the Task for the specified id - ${params.id} "
		 log.error "showComment: show comment view - "+errorMsg
		 commentList << [error:errorMsg]
		}
		render commentList as JSON
	}
	/* ----------------------------------------------------------------
	 * To save the Comment record
	 * @param assetComment
	 * @author Lokanath
	 * @return assetComments
	 * -----------------------------------------------------------------*/
	// def saveComment() { com.tdsops.tm.command.AssetCommentCommand cmd ->
	def saveComment() {
		def map = commentService.saveUpdateCommentAndNotes(session, params, true, flash)
		if( params.forWhom == "update" ){
			def assetEntity = AssetEntity.get(params.prevAsset)
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)
			render(template:"commentList",model:[assetCommentList:assetCommentList])
		} else {
			render map as JSON
		}
	}
	/* ------------------------------------------------------------
	 * update comments
	 * @param assetCommentId
	 * @author Lokanath
	 * @return assetComment
	 * ------------------------------------------------------------ */
	def updateComment() {
		def map = commentService.saveUpdateCommentAndNotes(session, params, false, flash)
		if ( params.open == "view" ) {
			if (map.error) {
				flash.message = map.error
			}
			forward(action:"showComment", params:[id:params.id] )
		} else if( params.view == "myTask" ) {
			if (map.error) {
				flash.message = map.error
			}
			forward(controller:"assetEntity", action:"listComment", params:[view:params.view, tab:params.tab])
		} else if( params.open != "view" ){
			render map as JSON
		}
	}
	
	/* delete the comment record
	 * @param assetComment
	 * @author Lokanath
	 * @return assetCommentList 
	 */
	def deleteComment() {
		// TODO - SECURITY - deleteComment - verify that the asset is part of a project that the user has the rights to delete the note
		def assetCommentInstance = AssetComment.get(params.id)
		if(assetCommentInstance){
			def taskDependency = TaskDependency.executeUpdate("delete from TaskDependency td where td.assetComment = ${assetCommentInstance.id} OR td.predecessor = ${assetCommentInstance.id}")
			assetCommentInstance.delete()
		}
		// TODO - deleteComment - Need to be fixed to handle non-asset type comments
		def assetCommentsList = []
		if(params.assetEntity){
			def assetEntityInstance = AssetEntity.get( params.assetEntity )
			def assetCommentsInstance = AssetComment.findAllByAssetEntityAndIdNotEqual( assetEntityInstance, params.id )
			assetCommentsInstance.each {
				assetCommentsList <<[ commentInstance : it, assetEntityId : it.assetEntity.id ]
			}
		}
		render assetCommentsList as JSON
	}

	/*----------------------------------
	 * @author: Lokanath Redy
	 * @param : fromState and toState
	 * @return: boolean value to validate comment field
	 *---------------------------------*/
	def retrieveFlag() {
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		def moveBundleInstance = MoveBundle.get(params.moveBundle)
		def toState = params.toState
		def fromState = params.fromState
		def status = []
		def flag = stateEngineService.getFlags(moveBundleInstance.workflowCode,"SUPERVISOR", fromState, toState)
		if(flag?.contains("comment") || flag?.contains("issue")){
			status<< ['status':'true']
		}
		render status as JSON
	}

	/*-----------------------------------------
	 *@param : state value
	 *@return: List of valid stated for param state
	 *----------------------------------------*/
	def retrieveStates(def state,def assetEntity){
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		def stateIdList = []
		def validStates
		if(state){
			validStates = stateEngineService.getTasks(assetEntity.moveBundle.workflowCode,"SUPERVISOR", state)
		} else {
			validStates = ["Ready"]
			//stateEngineService.getTasks("STD_PROCESS","TASK_NAME")
		}
		validStates.each{
			stateIdList<<stateEngineService.getStateIdAsInt(assetEntity.moveBundle.workflowCode,it)
		}
		return stateIdList
	}

	/* --------------------------------------
	 * 	@author : Lokanada Reddy
	 * 	@param : batch id and total assts from session 
	 *	@return imported data for progress bar
	 * -------------------------------------- */
	def retrieveProgress() {
		def importedData
		def progressData = []
		def batchId = session.getAttribute("BATCH_ID")
		def total = session.getAttribute("TOTAL_ASSETS")
		if ( batchId ){
			importedData = jdbcTemplate.queryForList("select count(distinct row_id) as rows from data_transfer_value where data_transfer_batch_id="+batchId).rows
		}
		progressData<<[imported:importedData,total:total]
		render progressData as JSON
	}
	/* --------------------------------------
	 * 	@author : Lokanada Reddy
	 * 	@param : time as ms 
	 *	@return time as HH:MM:SS formate
	 * -------------------------------------- */
	def convertIntegerIntoTime(def time) {
		def timeFormate
		if(time != 0){
			def hours = (Integer)(time / (1000*60*60))
			timeFormate = hours >= 10 ? hours : '0'+hours
			def minutes = (Integer)((time % (1000*60*60)) / (1000*60))
			timeFormate += ":"+(minutes >= 10 ? minutes : '0'+minutes)
			def seconds = (Integer)(((time % (1000*60*60)) % (1000*60)) / 1000)
			timeFormate += ":"+(seconds >= 10 ? seconds : '0'+seconds)
		} else {
			timeFormate = "00:00:00"
		}
		return timeFormate
	}

	/**
	 * This action is for presenting the CRUD form for a new Device entry form
	 * @param params.redirectTo - used to redirect the user back to the appropriate page afterward
	 * @return : render to create page based on condition as if redirectTo is assetAudit then redirecting 
	 * to auditCreate view
	 */
	def create() {
		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		def (device, model) = assetEntityService.getDeviceAndModelForCreate(project, params)

		model.action = 'save'
		model.whom = 'Device'

		// TODO : JPM 10/2014 : I'm guessing this is needed to make the save action work correctly
		model.redirectTo = params.redirectTo ?: 'list'

		if ( params.redirectTo == "assetAudit") {
			model << [source:params.source, assetType:params.assetType]
			render( template:"createAuditDetails", model:model)
		} else {
			// model.each { n,v -> println "$n:\t$v"}
			render ('view': 'createEdit', 'model' : model)
		}
		
	}

	/**
	 * This action is used to redirect to edit view .
	 * @param : redirectTo
	 * @return : render to edit page based on condition as if 'redirectTo' is roomAudit then redirecting
	 * to auditEdit view
	 */
	def edit() {
		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		def (device, model) = assetEntityService.getDeviceModelForEdit(project, params.id, params)

		if (! device) {
			render '<span class="error">Unable to find asset to edit</span>'
			return
		}

		if (params.redirectTo == "roomAudit") {
			// TODO : JPM 9/2014 : Need to determine the assetType
			model.putAll( [
				assetType:params.assetType,
				source:params.source 
			] )

			render(template:"auditEdit", model: model)
			return
		}

		model.action = 'update'

		// model.each { n,v -> println "$n:\t$v"}
		render ('view': 'createEdit', 'model' : model)
	}

	/**
	 * Used to create and save a new device and associated dependencies. Upon success or failure it will redirect the 
	 * user to the place that they came from based on the params.redirectTo param. The return content varies based on that
	 * param as well.
	 */
	def save() {
		controllerService.saveUpdateAssetHandler(this, session, deviceService, AssetClass.DEVICE, params)
		session.AE?.JQ_FILTERS = params
	}

	/**
	 * This action is used to update assetEntity 
	 * @param redirectTo : a flag to redirect view to page after update
	 * @param id : id of assetEntity
	 * @return : render to appropriate view
	 */
	def update() {
		controllerService.saveUpdateAssetHandler(this, session, deviceService, AssetClass.DEVICE, params)
		session.AE?.JQ_FILTERS = params
	}
 	
	/**
	* Renders the detail of an AssetEntity 
	*/
	def show() {

		def (project, userLogin) = controllerService.getProjectAndUserForPage( this )
		if (! project) 
			return

		def assetId = params.id
		def assetEntity = controllerService.getAssetForPage(this, project, AssetClass.DEVICE, assetId)

		if (!assetEntity) {
			flash.message = "Unable to find asset within current project with id ${params.id}"
			log.warn "show - asset id (${params.id}) not found for project (${project.id}) by user ${userLogin}"
			def errorMap = [errMsg : flash.message]
			render errorMap as JSON
		} else {
			def model = deviceService.getModelForShow(project, assetEntity, params)
			if (!model) {
				render "Unable to load specified asset"
				return 
			}

			if (params.redirectTo == "roomAudit") {
				// model << [source:params.source, assetType:params.assetType]
				render(template: "auditDetails", model: model)
			}

			//model.each { n,v -> println "$n:\t$v"}
			return model
		}
	}

	/**
     * This action is used to get list of all Manufacturerss ordered by manufacturer name display at
     * assetEntity CRUD and AssetAudit CRUD
     * @param assetType : requested assetType for which we need to get manufacturer list
     * @return : render to manufacturerView
     */
	def retrieveManufacturersList() {
		def assetType = params.assetType
		def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
		def prefVal =  userPreferenceService.getPreference("lastManufacturer")
		def selectedManu = prefVal ? Manufacturer.findByName( prefVal )?.id : null
		render (view :'manufacturerView' , model:[manufacturers : manufacturers, selectedManu:selectedManu,forWhom:params.forWhom ])
	}
	
	/**
	 * Used to set showAllAssetTasks preference , which is used to show all or hide the inactive tasks
	 */
	def setShowAllPreference() {
		userPreferenceService.setPreference("showAllAssetTasks", params.selected=='1' ? 'TRUE' : 'FALSE')
		render true
	}
	
	/**
	 * Used to set showAllAssetTasks preference , which is used to show all or hide the inactive tasks
	 */
	def setViewUnpublishedPreference () {
		userPreferenceService.setPreference("viewUnpublished", (params.viewUnpublished == '1' || params.viewUnpublished == 'true') ? ('true') : ('false'))
		render true
	}
	
	/**
	 * This action is used to get list of all Models to display at assetEntity CRUD and AssetAudit CRUD
	 * @param assetType : requested assetType for which we need to get manufacturer list
	 * @return : render to manufacturerView
	 */
	def retrieveModelsList() {
		def manufacturer = params.manufacturer
		def models=[]
		if(manufacturer!="null"){
			def manufacturerInstance = Manufacturer.read(manufacturer)
			models=assetEntityService.getModelSortedByStatus(manufacturerInstance)
		}
		render (view :'_deviceModelSelect' , model:[models : models, forWhom:params.forWhom])
	}
	
	/**
	 * Used to generate the List for Task Manager, which leverages a shared closure with listComment
	 */
	def listTasks() {

		def user = securityService.getUserLogin()
		try {
			if (!RolePermissions.hasPermission('ViewTaskManager')) {
				log.warn "Unauthorized user $user attempted to see Task Manager"
				//while using 'UnauthorizedException' getting  java.lang.IncompatibleClassChangeError:
				//the number of constructors during runtime and compile time for java.lang.RuntimeException do not match. Expected 4 but got 5
				//So using 'RuntimeException' for now.
				throw new RuntimeException('User does not have permission to see Task Manager')
				
			}
			
			if (params.containsKey('viewUnpublished') && params.viewUnpublished in ['0', '1']) {
				def viewUnpublishedBoolean = (params.viewUnpublished == '1')
				userPreferenceService.setPreference("viewUnpublished", viewUnpublishedBoolean.toString())
			}
			
			params.commentType = AssetCommentType.TASK
			
			if (params.initSession)
				session.TASK = [:]
				
			def project = securityService.getUserCurrentProject();
			if (!project) {
				flash.message = "Please select project to view Tasks"
				redirect(controller:'project',action:'list')
				return
			}
			def moveEvents = MoveEvent.findAllByProject(project)
			def filters = session.TASK?.JQ_FILTERS
			
			// Deal with the parameters
			def isTask = AssetCommentType.TASK
			def taskPref = assetEntityService.getExistingPref('Task_Columns')
			def assetCommentFields = AssetComment.getTaskCustomizeFieldAndLabel()
			def modelPref = [:]
			taskPref.each {key,value->
				modelPref <<  [ (key): assetCommentFields[value] ]
			}
			def filterEvent = 0
			if (params.moveEvent) {
				filterEvent = params.moveEvent
			}
			def moveEvent
			
			if (params.containsKey("justRemaining")) {
				userPreferenceService.setPreference("JUST_REMAINING", params.justRemaining)
			}
			if ( params.moveEvent?.size() > 0) {
				// zero (0) = All events
				// log.info "listCommentsOrTasks: Handling MoveEvent based on params ${params.moveEvent}"
				if (params.moveEvent != '0') {
					moveEvent = MoveEvent.findByIdAndProject(params.moveEvent,project)
					if (! moveEvent) {
						log.warn "listCommentsOrTasks: ${person} tried to access moveEvent ${params.moveEvent} that was not found in project ${project.id}"
					}
				}
			} else {
				// Try getting the move Event from the user's session
				def moveEventId = userPreferenceService.getPreference('MOVE_EVENT')
				// log.info "listCommentsOrTasks: getting MOVE_EVENT preference ${moveEventId} for ${person}"
				if (moveEventId) {
					moveEvent = MoveEvent.findByIdAndProject(moveEventId,project)
				}
			}
			if (moveEvent && params.section != 'dashBoard') {
				// Add filter to SQL statement and update the user's preferences
				userPreferenceService.setPreference( 'MOVE_EVENT', "${moveEvent.id}" )
				filterEvent = moveEvent.id
			} else {
				userPreferenceService.removePreference( 'MOVE_EVENT' );
			}
			def justRemaining = userPreferenceService.getPreference("JUST_REMAINING") ?: "1"
			// Set the Checkbox values to that which were submitted or default if we're coming into the list for the first time
			def justMyTasks = params.containsKey('justMyTasks') ? params.justMyTasks : "0"
			def viewUnpublished = (userPreferenceService.getPreference("viewUnpublished") == 'true') ? '1' : '0'
			def timeToRefresh = userPreferenceService.getPreference("TASKMGR_REFRESH")
			def entities = assetEntityService.entityInfo( project )
			def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
			def companiesList = partyRelationshipService.getCompaniesList()
			return [timeToUpdate : timeToRefresh ?: 60,servers:entities.servers, applications:entities.applications, dbs:entities.dbs,
					files:entities.files,networks:entities.networks, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus, assetDependency: new AssetDependency(),
					moveEvents:moveEvents, filterEvent:filterEvent , justRemaining:justRemaining, justMyTasks:justMyTasks, filter:params.filter,
					comment:filters?.comment ?:'', taskNumber:filters?.taskNumber ?:'', assetName:filters?.assetEntity ?:'', assetType:filters?.assetType ?:'',
					dueDate : filters?.dueDate ?:'', status : filters?.status ?:'', assignedTo : filters?.assignedTo ?:'', role: filters?.role ?:'',
					category: filters?.category ?:'', moveEvent:moveEvent, moveBundleList : moveBundleList, viewUnpublished : viewUnpublished,
					staffRoles:taskService.getTeamRolesForTasks(), taskPref:taskPref, attributesList: assetCommentFields.keySet().sort{it}, modelPref:modelPref,
					//staffRoles:taskService.getRolesForStaff(), 
					sizePref:userPreferenceService.getPreference("assetListSize")?: '25',
					partyGroupList: companiesList,
					company: project.client]
		} catch (RuntimeException uex) {
			log.error uex.getMessage()
			response.sendError( 401, "Unauthorized Error")
		}
	}

	/**
	 * Used to generate the List of Comments, which leverages a shared closeure with the above listTasks controller
	 */
	def listComment() {
			def project = securityService.getUserCurrentProject();
			if (!project) {
				flash.message = "Please select project to view Comments"
				redirect(controller:'project',action:'list')
				return
			}
			def entities = assetEntityService.entityInfo( project )
			def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
			def canEditComments = controllerService.checkPermissionForWS('AssetEdit', false)
		    return [ rediectTo:'comment', servers:entities.servers, applications:entities.applications, dbs:entities.dbs,
				files:entities.files, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus, assetDependency: new AssetDependency(),
				moveBundleList:moveBundleList, canEditComments: canEditComments ]
	}
	
	/**
	 * Used to generate list of comments using jqgrid
	 * @return : list of tasks as JSON
	 */
	def listCommentJson() {
		def sortIndex = params.sidx ?: 'lastUpdated'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		
		def project = securityService.getUserCurrentProject()
		def lastUpdatedTime = params.lastUpdated ? AssetComment.findAll("from AssetComment where project =:project \
						and commentType =:comment and lastUpdated like '%${params.lastUpdated}%'",
						[project:project,comment:AssetCommentType.COMMENT])?.lastUpdated : []
		
		
		def assetCommentList = AssetComment.createCriteria().list(max: maxRows, offset: rowOffset) {
				eq("project", project)
				eq("commentType", AssetCommentType.COMMENT)
				createAlias("assetEntity","ae")
				if (params.comment)
					ilike('comment', "%${params.comment}%")
				if (params.commentType)
					ilike('commentType', "%${params.commentType}%")
				if (params.category)
					ilike('category', "%${params.category}%")
				if(lastUpdatedTime)
					'in'('lastUpdated',lastUpdatedTime)
				if (params.assetType)
					ilike('ae.assetType',"%${params.assetType}%")
				if (params.assetName)
					ilike('ae.assetName',"%${params.assetName}%")
				def sid = sortIndex  =='assetName' || sortIndex  =='assetType' ? "ae.${sortIndex}" : sortIndex
				order(new Order(sid, sortOrder=='asc').ignoreCase())
			}
		def totalRows = assetCommentList.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)

		def results = assetCommentList?.collect {
			[ cell: 
				[
					'',
					(it.comment?.length()>50 ? (it.comment.substring(0,50) + '...') : it.comment).replace("\n",""), 
					it.lastUpdated ? TimeUtil.formatDate(getSession(), it.lastUpdated):'',
					it.commentType ,
					it.assetEntity?.assetName ?:'',
					it.assetEntity?.assetType ?:'',
					it.category,
					it.assetEntity?.id,
					it.assetEntity?.assetClass.toString()
				], 
				id: it.id
			]
		}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	/**
	 * This will be called from TaskManager screen to load jqgrid
	 * @return : list of tasks as JSON
	 */
	def listTaskJSON() {
		def sortIndex =  params.sidx ? params.sidx : session.TASK?.JQ_FILTERS?.sidx
		def sortOrder =  params.sidx ? params.sord : session.TASK?.JQ_FILTERS?.sord
		
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		
		userPreferenceService.setPreference("assetListSize", "${maxRows}")

		def project = securityService.getUserCurrentProject()
		def person = securityService.getUserLoginPerson()
		def moveBundleList
		def today = new Date()
		def moveEvent		
		if ( params.moveEvent?.size() > 0) {
			// zero (0) = All events
			// log.info "listCommentsOrTasks: Handling MoveEvent based on params ${params.moveEvent}"
			if (params.moveEvent != '0') {
				moveEvent = MoveEvent.findByIdAndProject(params.moveEvent,project)
				if (! moveEvent) {
					log.warn "listCommentsOrTasks: ${person} tried to access moveEvent ${params.moveEvent} that was not found in project ${project.id}"
				}
			}
		} else {
			// Try getting the move Event from the user's session
			def moveEventId = userPreferenceService.getPreference('MOVE_EVENT')
			// log.info "listCommentsOrTasks: getting MOVE_EVENT preference ${moveEventId} for ${person}"
			if (moveEventId) {
				moveEvent = MoveEvent.findByIdAndProject(moveEventId,project)
			}
		}
		if (moveEvent) {
			userPreferenceService.setPreference( 'MOVE_EVENT', "${moveEvent.id}" )
		}
		
		def assetType = params.filter  ? ApplicationConstants.assetFilters[ params.filter ] : []

		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%${params.moveBundle}%", project) : []
		def models = params.model ? Model.findAllByModelNameIlike("%${params.model}%") : []
		
		def taskNumbers = params.taskNumber ? AssetComment.findAll("from AssetComment where project =:project \
			and taskNumber like '%${params.taskNumber}%'",[project:project])?.taskNumber : []
		
		def durations = params.duration ? AssetComment.findAll("from AssetComment where project =:project \
			and duration like '%${params.duration}%'",[project:project])?.duration : []
			
		def viewUnpublished = (RolePermissions.hasPermission("PublishTasks") && userPreferenceService.getPreference("viewUnpublished") == 'true')

		// TODO TM-2515 - SHOULD NOT need ANY of these queries as they should be implemented directly into the criteria
		def dates = params.dueDate ? AssetComment.findAll("from AssetComment where project =:project and dueDate like '%${params.dueDate}%' ",[project:project])?.dueDate : []
		def estStartdates = params.estStart ? AssetComment.findAll("from AssetComment where project=:project and estStart like '%${params.estStart}%' ",[project:project])?.estStart : []
		def actStartdates = params.actStart ? AssetComment.findAll("from AssetComment where project=:project and actStart like '%${params.actStart}%' ",[project:project])?.actStart : []
		def dateCreateddates = params.dateCreated ? AssetComment.findAll("from AssetComment where project=:project and dateCreated like '%${params.dateCreated}%' ",[project:project])?.dateCreated : []
		def dateResolveddates = params.dateResolved ? AssetComment.findAll("from AssetComment where project=:project and dateResolved like '%${params.dateResolved}%' ",[project:project])?.dateResolved : []
		def estFinishdates = params.estFinish ? AssetComment.findAll("from AssetComment where project=:project and estFinish like '%${params.estFinish}%' ",[project:project])?.estFinish : []
		
		// TODO TM-2515 - ONLY do the lookups if params used by the queries are populated
		def assigned = params.assignedTo ? Person.findAllByFirstNameIlikeOrLastNameIlike("%${params.assignedTo}%","%${params.assignedTo}%" ) : []
		def createdBy = params.createdBy ? Person.findAllByFirstNameIlikeOrLastNameIlike("%${params.createdBy}%","%${params.createdBy}%" ) : [] 
		def resolvedBy = params.resolvedBy ? Person.findAllByFirstNameIlikeOrLastNameIlike("%${params.resolvedBy}%","%${params.resolvedBy}%" ) : []

		def tasks = AssetComment.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			eq("commentType", AssetCommentType.TASK) 
			createAlias('assetEntity', 'assetEntity', CriteriaSpecification.LEFT_JOIN)
			if (!viewUnpublished)
				eq("isPublished", true)
			if (params.assetType)
				ilike('assetEntity.assetType', "%${params.assetType}%")
			if (params.assetName)
				ilike('assetEntity.assetName', "%${params.assetName}%")
			if (params.comment)
				ilike('comment', "%${params.comment}%")
			if (params.status)
				ilike('status', "%${params.status}%")
			if (params.role)
				ilike('role', "%${params.role}%")
			if(params.commentType)
				ilike('commentType', "%${params.commentType}%")
			if(params.displayOption)
				ilike('displayOption', "%${params.displayOption}%")
			if(durations)
				'in'('duration', durations)
			if(params.durationScale)
				ilike('durationScale', "%${params.durationScale}%")
			if (params.category)
				ilike('category', "%${params.category}%")
			if (params.attribute)
				ilike('attribute', "%${params.attribute}%")
			if (params.autoGenerated)
				eq('autoGenerated', params.autoGenerated)
			if (taskNumbers)
				'in'('taskNumber' , taskNumbers)
				
			if((params.isResolved || params.isResolved == '0') && params.isResolved?.isNumber())
				eq('isResolved', new Integer(params.isResolved))
				
			if((params.hardAssigned || params.hardAssigned == '0') && params.hardAssigned?.isNumber())
				eq('hardAssigned', new Integer(params.hardAssigned))
				
			if (dates) {
				and {
					or {
						'in'('dueDate' , dates)
						'in'('estFinish', dates)
					}
				}
			}
			if(estStartdates)
				'in'('estStart',estStartdates)
			if(actStartdates)
				'in'('actStart',actStartdates)
			if(estFinishdates)
				'in'('estFinish',estFinishdates)
			if(dateCreateddates)
				'in'('dateCreated',dateCreateddates)
			if(dateResolveddates)
				'in'('dateResolved',dateResolveddates)	
			
			if(createdBy)
				'in'('createdBy',createdBy)
			if(resolvedBy)
				'in'('createdBy',resolvedBy)
			if (assigned )
				'in'('assignedTo' , assigned)
			
			if(sortIndex && sortOrder){
				if(sortIndex  =='assetName' || sortIndex  =='assetType'){
					order(new Order('assetEntity.' + sortIndex, sortOrder=='asc').ignoreCase())
				}else{
					order(new Order(sortIndex, sortOrder=='asc').ignoreCase())
				}
			} else {
				and{
					order('score','desc')
					order('taskNumber','asc')
					order('dueDate','asc')
					order('dateCreated','desc')
				}
			}
			if(moveEvent)
				eq("moveEvent", moveEvent)
				
			if (params.justRemaining == "1") {
				ne("status", AssetCommentStatus.COMPLETED)
			}
			if (params.justMyTasks == "1") {
				eq("assignedTo",person)
			}
			switch(params.filter){
				case "openIssue" :
					'in'('category', AssetComment.discoveryCategories )
					break;
				case "dueOpenIssue":
					'in'('category', AssetComment.discoveryCategories )
					lt('dueDate',today)
					break;
				case "analysisIssue" :
					eq("status", AssetCommentStatus.READY)
					'in'('category', AssetComment.planningCategories)
					break;
				case "generalOverDue" :
					'in'('category', AssetComment.planningCategories)
					 lt('dueDate',today)
					 break;
			}
		}

		def createJsonTime = new Date()

		def totalRows = tasks.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)
		def updatedTime
		def updatedClass
		def dueClass
		def nowGMT = TimeUtil.nowGMT()
		def taskPref=assetEntityService.getExistingPref('Task_Columns')

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
			
			def dueDate='' 
			if (isRunbookTask) {
				dueDate = it.estFinish ? TimeUtil.formatDateTime(getSession(), it.estFinish, TimeUtil.FORMAT_DATE_TIME_4) : ''
			} else {
				dueDate = it.dueDate ? TimeUtil.formatDate(getSession(), it.dueDate) : ''
			}
			
			def deps = TaskDependency.findAllByPredecessor( it )
			def depCount = 0
			deps.each {
				if (viewUnpublished || (it.assetComment?.isPublished && it.predecessor?.isPublished))
					++depCount
			}
			
			// Have the dependency count be a link to the Task Neighborhood graph if there are dependencies
			def nGraphUrl = depCount == 0 ? depCount : '<a href="' + HtmlUtil.createLink([controller:'task', action:'taskGraph']) +
				'?neighborhoodTaskId=' + it.id + '" target="_blank",>' + depCount + '</a>'

			def status = it.status
			def userSelectedCols = []
			(1..5).each { colId ->
				def value = taskManagerValues(taskPref["$colId"],it)
				userSelectedCols << ( value?.getClass()?.isEnum() ? value?.value() : value )
			}
			
			def instructionsLinkURL;
			
			if(HtmlUtil.isMarkupURL(it.instructionsLink))
			{
				instructionsLinkURL = HtmlUtil.parseMarkupURL(it.instructionsLink)[1];
			}
			else
			{
				instructionsLinkURL = it.instructionsLink;
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
				status ? "task_${it.status.toLowerCase()}" : 'task_na',
				updatedClass, 
				dueClass, 
				it.assetEntity?.id, // 16
				it.assetEntity?.assetType, // 17
				it.assetEntity?.assetClass.toString(), // 18
				instructionsLinkURL // 19
				], 
				id:it.id
			]
		}
		
		// If sessions variables exists, set them with params and sort
		session.TASK?.JQ_FILTERS = params
		session.TASK?.JQ_FILTERS?.sidx = sortIndex
		session.TASK?.JQ_FILTERS?.sord = sortOrder
		
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	/**
	 * This method is used to get assetColumn value based on field name. .
	 */
	def taskManagerValues (value, task) {
		def result
		switch (value) {
			case 'assetName':
				result = task.assetEntity?.assetName
			break;
			case 'assetType':
				result = task.assetEntity?.assetType
			break;
			case 'assignedTo':
				def assignedTo  = (task.assignedTo ? task.assignedTo.toString(): '')
				result = task.hardAssigned ? '*'+assignedTo : '' + assignedTo
			break;
			case 'resolvedBy':
				result = task.resolvedBy ? task.resolvedBy.toString(): ''
			break;
			case 'createdBy':
				result = task.createdBy ? task.createdBy.toString(): ''
			break;
			case ~/statusUpdated|estFinish|dateCreated|dateResolved|estStart|actStart/:
				result = task[value] ? TimeUtil.formatDate(getSession(), task[value]) : ''
			break;
			default :
				result= task[value]
			break;
		}
		return result
	}
	
	/**
	 * This action is used to get AssetOptions by type to display at admin's AssetOption page .
	 */
	def assetOptions() {
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		
		def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)
		
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		
		def environment = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		
		return [planStatusOptions:planStatusOptions, priorityOption:priorityOption,dependencyType:dependencyType, 
				dependencyStatus:dependencyStatus, environment:environment]

	}
	
	/**
	 * This action is used to save AssetOptions by type to display at admin's AssetOption page .
	 */
	def saveAssetoptions() {
		def assetOptionInstance = new AssetOptions()
		def planStatusList = []
		def planStatus
		if(params.assetOptionType=="planStatus"){
			assetOptionInstance.type = AssetOptions.AssetOptionsType.STATUS_OPTION
			assetOptionInstance.value = params.planStatus
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = assetOptionInstance.id
			
			
		}else if(params.assetOptionType=="Priority"){
			assetOptionInstance.type = AssetOptions.AssetOptionsType.PRIORITY_OPTION
			assetOptionInstance.value = params.priorityOption
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = assetOptionInstance.id
			
		}else if(params.assetOptionType=="dependency"){
			assetOptionInstance.type = AssetOptions.AssetOptionsType.DEPENDENCY_TYPE
			assetOptionInstance.value = params.dependencyType
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = assetOptionInstance.id
			
		}else if(params.assetOptionType=="environment"){
			assetOptionInstance.type = AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION
			assetOptionInstance.value = params.environment
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = assetOptionInstance.id
			
		}else {
		    assetOptionInstance.type = AssetOptions.AssetOptionsType.DEPENDENCY_STATUS
			assetOptionInstance.value = params.dependencyStatus
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = assetOptionInstance.id
			
		}
		planStatusList =['id':planStatus]
		
	    render planStatusList as JSON
	}
	
	/**
	 * This action is used to delete  AssetOptions by type from admin's AssetOption page .
	 */
	def deleteAssetOptions() {
		def assetOptionInstance
		if(params.assetOptionType=="planStatus"){
			 assetOptionInstance = AssetOptions.get(params.assetStatusId)
			 if(!assetOptionInstance.delete(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			 }
		}else if(params.assetOptionType=="Priority"){
			assetOptionInstance = AssetOptions.get(params.priorityId)
			if(!assetOptionInstance.delete(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
		}else if(params.assetOptionType=="dependency"){
		    assetOptionInstance = AssetOptions.get(params.dependecyId)
			if(!assetOptionInstance.delete(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
		}else if(params.assetOptionType=="environment"){
		    assetOptionInstance = AssetOptions.get(params.environmentId)
			if(!assetOptionInstance.delete(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
		}else{
			assetOptionInstance = AssetOptions.get(params.dependecyId)
			if(!assetOptionInstance.delete(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
		}
		render assetOptionInstance.id
	}
	/**
	* Render Summary of assigned and unassgined assets.
	*/
	def assetSummary() {
		def project = controllerService.getProjectForPage( this )
		if (! project) 
			return

		List moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		List assetSummaryList = []
		int totalAsset = 0
		int totalPhysical = 0
		int totalApplication =0
		int totalDatabase = 0
		int totalFiles = 0

		def phySQL = 'from AssetEntity a where a.moveBundle=:mb and a.assetClass=:ac and coalesce(a.assetType) not in (:at)'

		moveBundleList.each{ moveBundle->
			def physicalCount = AssetEntity.findAll(phySQL, [mb:moveBundle, ac:AssetClass.DEVICE, at:AssetType.getVirtualServerTypes()]).size()
			def assetCount = AssetEntity.countByMoveBundleAndAssetTypeInList( moveBundle,AssetType.getServerTypes(), params )
			def applicationCount = Application.countByMoveBundle(moveBundle)
			def databaseCount = Database.countByMoveBundle(moveBundle)
			def filesCount = Files.countByMoveBundle(moveBundle)
			assetSummaryList << [ "name":moveBundle, "assetCount":assetCount, "applicationCount":applicationCount, "physicalCount":physicalCount,
				"databaseCount":databaseCount, "filesCount":filesCount, id:moveBundle.id]
		}
		
		assetSummaryList.each{ asset->
			totalAsset=totalAsset + asset.assetCount
			totalPhysical=totalPhysical+ asset.physicalCount
			totalApplication = totalApplication + asset.applicationCount
			totalDatabase = totalDatabase + asset.databaseCount
			totalFiles = totalFiles + asset.filesCount
		}

		return [
			assetSummaryList:assetSummaryList,
			totalAsset:totalAsset,
			totalApplication:totalApplication,
			totalDatabase:totalDatabase,totalPhysical:totalPhysical,
			totalFiles:totalFiles
		]
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
		
		def loginUser = securityService.getUserLogin()
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		
		def depGroups = []
		def depGroupsJson = session.getAttribute('Dep_Groups')
		if (depGroupsJson)
			depGroups = JSON.parse(session.getAttribute('Dep_Groups'))
		if (depGroups.size() == 0) {
			depGroups = [-1]
		}

		def assetDependentlist
		def selectionQuery = ''
		def mapQuery = ''
		def nodesQuery = ''
		def multiple = false;
		if (params.dependencyBundle && params.dependencyBundle.isNumber() ) {
			// Get just the assets for a particular dependency group id
			mapQuery = " AND deps.bundle = ${params.dependencyBundle}"
			depGroups = [params.dependencyBundle]
			nodesQuery = " AND dependency_bundle = ${params.dependencyBundle} "
		} else if (params.dependencyBundle == 'onePlus') {
			// Get all the groups other than zero - these are the groups that have interdependencies
			multiple = true;
			mapQuery = " AND deps.bundle in (${WebUtil.listAsMultiValueString(depGroups-[0])})"
			depGroups = depGroups-[0]
			nodesQuery = " AND dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups-[0])})"
		} else {
			// Get 'all' assets that were bundled
			multiple = true;
			mapQuery = " AND deps.bundle in (${WebUtil.listAsMultiValueString(depGroups)})"
			nodesQuery = " AND dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups)})"
		}
		def queryFordepsList = """
			SELECT DISTINCT deps.asset_id AS assetId, ae.asset_name AS assetName, deps.dependency_bundle AS bundle, mb.move_bundle_id AS moveBundleId, mb.name AS moveBundleName, 
			ae.asset_type AS type, ae.asset_class AS assetClass, me.move_event_id AS moveEvent, me.name AS eventName, app.criticality AS criticality,
			IF(ac_task.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus, IF(ac_comment.comment_type IS NULL, 'noComments','comments') AS commentsStatus
			FROM ( 
				SELECT * FROM tdstm.asset_dependency_bundle 
				WHERE project_id=${projectId} ${nodesQuery} 
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
		def sortOn = params.sort?:"assetName"
		def orderBy = params.orderBy?:"asc"
		def asset
		
		if (params.entity != 'graph') {
			depMap = moveBundleService.dependencyConsoleMap(projectId, null, null, (params.dependencyBundle!="null")?(params.dependencyBundle):("all") )
			depMap = depMap.gridStats
			
		} else {
			depMap = moveBundleService.dependencyConsoleMap(projectId, null, null, (params.dependencyBundle!="null")?(params.dependencyBundle):("all"), true)
		}
		def model = [entity: (params.entity ?: 'apps'), stats:depMap]
		
		model.dependencyBundle = params.dependencyBundle
		model.asset = params.entity
		model.orderBy = orderBy
		model.sortBy = sortOn
		model.haveAssetEditPerm = controllerService.checkPermissionForWS('AssetEdit', false)

		// Switch on the desired entity type to be shown, and render the page for that type 
		switch(params.entity) {
			case "all" :
				def assetList = []
				
				assetDependentlist.each {
					asset = AssetEntity.read(it.assetId)
					def type = asset.assetType
					if (it.assetClass == AssetClass.STORAGE.toString())
						type = "Logical Storage"
					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus, type:type]
				}
				assetList = sortAssetByColumn(assetList,(sortOn != "type") ? (sortOn) : "assetType",orderBy)
				model.assetList = assetList
				model.assetListSize = assetDependentlist.size()

				render( template:"allList", model:model )
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

				render( template:"appList", model:model )
				break

			case "server":
				def assetList = []
				def serverTypes = AssetType.getAllServerTypes()
				def assetEntityList = assetDependentlist.findAll { serverTypes.contains(it.type) }
				
				assetEntityList.each {
					asset = AssetEntity.read(it.assetId)
					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus, locRoom:asset.roomSource]
				}
				assetList = sortAssetByColumn(assetList,sortOn,orderBy)
				model.assetList = assetList
				model.assetEntityListSize = assetEntityList.size()
				render( template:"assetList", model:model)
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
				def filesList = assetDependentlist.findAll{(it.assetClass == AssetClass.STORAGE.toString()) || (it.assetClass == AssetClass.DEVICE.toString() && it.type in AssetType.getStorageTypes())}
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
					if (it.asset.assetClass.toString().equals('DEVICE')) {
						item.fileFormat = ''
						item.storageType = 'Server'
						item.type = item.assetType
					} else {
						item.fileFormat = Files.read(it.asset.id)?.fileFormat?:''
						item.storageType = 'Files'
						item.type = 'Logical'
					}
					
					fileList << [asset: item, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus]
				}
				fileList = sortAssetByColumn(fileList,sortOn,orderBy)
				model.filesList = fileList
				model.filesDependentListSize = fileList.size()
				
				render(template:"filesList", model:model)
				break

			case "graph" :
				def moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project,true)
				Set uniqueMoveEventList = moveBundleList.moveEvent
				uniqueMoveEventList.remove(null)
				List moveEventList = []

				moveEventList =  uniqueMoveEventList.toList()
				moveEventList.sort{it?.name}
				
				def defaultPrefs = ['colorBy':'group', 'appLbl':'true', 'maxEdgeCount':'4']
				def graphPrefs = userPreferenceService.getPreference('depGraph', loginUser)
				def prefsObject = [:]
				if (graphPrefs)
					prefsObject = JSON.parse(graphPrefs)
				else
					prefsObject = defaultPrefs
				
				
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
				def criticalitySizes = ['Minor':150, 'Important':200, 'Major':325, 'Critical':500]
				TreeMap<Long, String> dependencyBundleMap = new TreeMap<Long, String>();
				TreeMap<Long, String> moveBundleMap = new TreeMap<Long, String>();
				TreeMap<Long, String> moveEventMap = new TreeMap<Long, String>();
				def t1 = TimeUtil.elapsed(start).getMillis() + TimeUtil.elapsed(start).getSeconds()*1000
				
				assetDependentlist.each {
					assetType = it.model?.assetType?:it.type
					assetClass = it.assetClass
					size = 150
					
					type = assetClassUtil.getImageName(assetClass, assetType)
					if (type == AssetType.APPLICATION.toString())
						size = it.criticality ? criticalitySizes[it.criticality] : 200
					
					if (! dependencyBundleMap.containsKey(it.bundle))
						dependencyBundleMap.put(it.bundle, 'Group ' + it.bundle);
					if (! moveBundleMap.containsKey(it.moveBundleId))
						moveBundleMap.put(it.moveBundleId, it.moveBundleName);
					
					def moveEventName = it.eventName ?: 'No Event'
					color = it.eventName ? 'grey' : 'red'
					def moveEventId = it.moveEvent ?: (long)0.0
					def hasMoveEvent = it.eventName ? true : false
					
					if (! moveEventMap.containsKey(moveEventId))
						moveEventMap.put(moveEventId, moveEventName);
					
					graphNodes << [
						id:it.assetId, name:it.assetName, 
						type:type, depBundleId:it.bundle, 
						moveBundleId:it.moveBundleId, moveEventId:moveEventId, hasMoveEvent:hasMoveEvent,
						shape:shape, size:size, title:it.assetName, 
						color:color, dependsOn:[], supports:[],
						assetClass:it.assetClass, cutGroup:0
					]
				}
				
				// set the dep bundle, move bundle, and move event properties to indices
				def sortedDepBundles = new ArrayList<Long>(dependencyBundleMap.keySet())
				def sortedMoveBundles = new ArrayList<Long>(moveBundleMap.keySet())
				def sortedMoveEvents = new ArrayList<Long>(moveEventMap.keySet())
				def dependencyGroupIndexMap = [:]
				def moveBundleIndexMap = [:]
				def moveEventIndexMap = [:]
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
				def nodeIds = []
				graphNodes.each {
					nodeIds << it.id
				}
				
				// Report the time it took to create the nodes
				def t2 = TimeUtil.elapsed(start).getMillis() + TimeUtil.elapsed(start).getSeconds()*1000
				def td = t2-t1
				def avg = 0
				if (assetDependentlist.size() > 0){
					avg = td / assetDependentlist.size()
				}
				
				// Set the defaults map to be used in the dependeny graph
				def defaults = moveBundleService.getMapDefaults(graphNodes.size())
				if ( multiple ) {
					defaults.force = -200
					defaults.linkSize = 140
				}
				
				// Query for only the dependencies that will be shown
				def depBundle = (params.dependencyBundle.isNumber() ? params.dependencyBundle : 0)
				
				def assetDependencies = AssetDependency.executeQuery("""
					SELECT NEW MAP (ad.asset AS ASSET, ad.status AS status, ad.isFuture AS future, 
						ad.isStatusResolved AS resolved, adb1.asset.id AS assetId, adb2.asset.id AS dependentId,
						(CASE WHEN (ad.asset.moveBundle != ad.dependent.moveBundle AND ad.status in (${WebUtil.listAsMultiValueString(["'"+AssetDependencyStatus.UNKNOWN+"'", "'"+AssetDependencyStatus.VALIDATED+"'", "'"+AssetDependencyStatus.QUESTIONED+"'"])})) THEN (true) ELSE (false) END) AS bundleConflict)
					FROM AssetDependency ad, AssetDependencyBundle adb1, AssetDependencyBundle adb2, Project p
					WHERE ad.asset = adb1.asset
						AND ad.dependent = adb2.asset
						AND adb1.dependencyBundle in (${WebUtil.listAsMultiValueString(depGroups)})
						AND adb2.dependencyBundle in (${WebUtil.listAsMultiValueString(depGroups)})
						AND adb1.project = p
						AND adb2.project = p
						AND p.id = ${projectId}
					GROUP BY ad.id
				""")
				
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
					def notApplicable = false
					def future = false
					if (! it.resolved) {
						statusColor='red'
					} else if (it.status == AssetDependencyStatus.FUTURE) {
						future = true
					} else if( ! (it.status in [AssetDependencyStatus.UNKNOWN, AssetDependencyStatus.VALIDATED]) ) {
						notApplicable = true
					}
					
					def sourceIndex = nodeIds.indexOf(it.assetId)
					def targetIndex = nodeIds.indexOf(it.dependentId)
					if (sourceIndex != -1 && targetIndex != -1) {
						
						// check if this link is the 2nd part of a 2-way dependency
						if (! linkTable[sourceIndex])
							linkTable[sourceIndex] = []
						linkTable[sourceIndex][targetIndex] = true
						def duplicate = (linkTable[targetIndex] && linkTable[targetIndex][sourceIndex])
						
						graphLinks << ["id":i, "source":sourceIndex, "target":targetIndex, "value":2, "statusColor":statusColor, "opacity":opacity, "unresolved":!it.resolved, "notApplicable":notApplicable, "future":future, "bundleConflict":it.bundleConflict, "duplicate":duplicate]
						i++
					}
				}
				
				// Set the dependency properties of the nodes
				graphLinks.each {
					def source = it.source
					def target = it.target
					graphNodes[source].dependsOn.add(it.id)
					graphNodes[target].supports.add(it.id)
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
		def preferenceName = params.preferenceName ?: 'depGraph'
		userPreferenceService.removePreference(preferenceName)
		render true
	}

	/**
	* Delete multiple  Assets, Apps, Databases and files .
	* @param : assetLists[]  : list of ids for which assets are requested to deleted
	* @return : appropriate message back to view
	* 
	*/
	def deleteBulkAsset() {
		def respMap = [resp : assetEntityService.deleteBulkAssets(params.type, params.list("assetLists[]"))]
		render respMap as JSON
	}
	
	/**
	 * This action is used to get workflowTransition select for comment id
	 * @param assetCommentId : id of assetComment
     * @param format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return select or a JSON array
	 */
	def retrieveWorkflowTransition() {
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def format = params.format
		def assetCommentId = params.assetCommentId
		def assetComment = AssetComment.read(assetCommentId)
		def assetEntity = AssetEntity.get(params.assetId)
		def workflowCode = assetEntity?.moveBundle?.workflowCode ?: project.workflowCode
		def workFlowInstance = Workflow.findByProcess(workflowCode)
		def workFlowTransition = WorkflowTransition.findAllByWorkflowAndCategory(workFlowInstance, params.category)
        
		//def workFlowTransition = WorkflowTransition.findAllByWorkflow(workFlowInstance) TODO : should be removed after completion of this new feature
		if(assetEntity){
			def existingWorkflows = assetCommentId ? AssetComment.findAllByAssetEntityAndIdNotEqual(assetEntity, assetCommentId ).workflowTransition : AssetComment.findAllByAssetEntity(assetEntity ).workflowTransition
			workFlowTransition.removeAll(existingWorkflows)
		}
		def result
		if (format == 'json') {
			def items = []
			workFlowTransition.each {
				items << [ id:it.id, name: it.name]
			}
			result = ServiceResults.success(items) as JSON
		} else {
			result = ''
			if(workFlowTransition.size()){
				def paramsMap = [selectId:'workFlowId', selectName:'workFlow', options:workFlowTransition, firstOption : [value:'', display:''],
						  optionKey:'id', optionValue:'name', optionSelected:assetComment?.workflowTransition?.id]
				result = HtmlUtil.generateSelect( paramsMap )
			}
		}
		render result
	}
    
	/**
	 * Provides a SELECT control with Staff associated with a project and the assigned staff selected if task id included
	 * @param forView - The CSS ID for the SELECT control
	 * @param id - the id of the existing task (aka comment)
     * @param format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return HTML select of staff belongs to company and TDS or a JSON array
	 * 
	 */
	def updateAssignedToSelect() {
		
		// TODO : Need to refactor this function to use the new TaskService.assignToSelectHtml method
		
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def viewId = params.forView
		def format = params.format
		def selectedId = 0
		def person

		// Find the person assigned to existing comment or default to the current user
		if (params.containsKey('id')) {
			if (params.id && params.id != '0') {
				def comment = AssetComment.findByIdAndProject(params.id, project);
				person = comment?.assignedTo
			} else {
				person = securityService.getUserLoginPerson()
			}
		}
		if (person) selectedId = person.id

		def projectStaff = partyRelationshipService.getProjectStaff( projectId )
		
		// Now morph the list into a list of name: Role names
		def list = []
		projectStaff.each {
			list << [ id:it.staff.id, 
				nameRole:"${it.role.description.split(':')[1]?.trim()}: ${it.staff.toString()}",
				sortOn:"${it.role.description.split(':')[1]?.trim()},${it.staff.firstName} ${it.staff.lastName}"
			]
		}
		list.sort { it.sortOn }
		
		def result
		if (format == 'json') {
			result = ServiceResults.success(list) as JSON
		} else {
			def firstOption = [value:'0', display:'Unassigned']
			def paramsMap = [selectId:viewId, selectName:viewId, options:list, 
			optionKey:'id', optionValue:'nameRole', 
			optionSelected:selectedId, firstOption:firstOption ]
			result = HtmlUtil.generateSelect( paramsMap )
		}
		render result
	}

	def isAllowToChangeStatus() {
		def taskId = params.id
		def allowed = true
		if(taskId){
			def status = AssetComment.read(taskId)?.status
			def isChangePendingStatusAllowed = securityService.isChangePendingStatusAllowed()
			if(status == "Pending" && !isChangePendingStatusAllowed){
				allowed = false
			}
		}
		render([isAllowToChangeStatus : allowed] as JSON)
	}

	/**
	 * Generates an HTML SELECT control for the AssetComment.status property according to user role and current status of AssetComment(id)
	 * @param	params.id	The ID of the AssetComment to generate the SELECT for
	 * @param   format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return render HTML or a JSON array
	 */
	def updateStatusSelect() {
		//Changing code to populate all select options without checking security roles.
		def mapKey = 'ALL'//securityService.hasRole( ['ADMIN','SUPERVISOR','CLIENT_ADMIN','CLIENT_MGR'] ) ? 'ALL' : 'LIMITED'
		def optionForRole = statusOptionForRole.get(mapKey)
		def format = params.format
		def taskId = params.id
		def status = taskId ? (AssetComment.read(taskId)?.status?: '*EMPTY*') : AssetCommentStatus.READY
		def optionList = optionForRole.get(status)
		def firstOption = [value:'', display:'Please Select']
		def selectId = taskId ? "statusEditId" : "statusCreateId"
		def optionSelected = taskId ? (status != '*EMPTY*' ? status : 'na' ): AssetCommentStatus.READY

		def result
		if (format == 'json') {
			result = ServiceResults.success(optionList) as JSON
		} else {
			def paramsMap = [selectId:selectId, selectName:'statusEditId', selectClass:"task_${optionSelected.toLowerCase()}",
			javascript:"onChange='this.className=this.options[this.selectedIndex].className'", 
			options:optionList, optionSelected:optionSelected, firstOption:firstOption,
			optionClass:""]
			result = HtmlUtil.generateSelect( paramsMap )
		}

		render result
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
		def project = securityService.getUserCurrentProject()
		def task = AssetComment.findByIdAndProject(params.commentId, project)
		if ( ! task ) {
			log.error "predecessorTableHtml - unable to find task $params.commentId for project $project.id"
			render "An unexpected error occured"
		} else {
			def taskDependencies = task.taskDependencies
			render taskService.genTableHtmlForDependencies(taskDependencies, task, "predecessor")
		}
	}

	/**
	 * Generats options for task dependency select
	 * @param : taskId  : id of task for which select options are generating .
	 * @param : category : category for options .
	 * @return : options
	 */
	def generateDepSelect() {
		
		def taskId=params.taskId
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		
		def task = AssetComment.read(taskId)
		def category = params.category
		
		def queryForPredecessor = new StringBuffer("""FROM AssetComment a WHERE a.project=${projectId} \
			AND a.category='${category}'\
			AND a.commentType='${AssetCommentType.TASK}'\
			AND a.id != ${task.id} """)
		if (task.moveEvent) {
			queryForPredecessor.append("AND a.moveEvent.id=${task.moveEvent.id}")
		}
		queryForPredecessor.append(""" ORDER BY a.taskNumber ASC""")
		def predecessors = AssetComment.findAll(queryForPredecessor.toString())
		
		StringBuffer options = new StringBuffer("")
		predecessors.each{
			options.append("<option value='${it.id}' >"+it.toString()+"</option>")
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
	def project = securityService.getUserCurrentProject()
	def task = AssetComment.findByIdAndProject(params.commentId, project)
	if ( ! task ) {
		log.error "successorTableHtml - unable to find task $params.commentId for project $project.id"
		render "An unexpected error occured"
	} else {
		def taskSuccessors = TaskDependency.findAllByPredecessor( task ).sort{ it.assetComment.taskNumber }
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
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def task
        def format=params.format
        def moveEventId=params.moveEvent
		
		if (params.commentId) { 
			task = AssetComment.findByIdAndProject(params.commentId, project)
			if ( ! task ) {
				log.warn "predecessorSelectHtml - Unable to find task id ${params.commentId} in project $project.id"
			}
		}

		def taskList = taskService.genSelectForPredecessors(project, params.category, task, moveEventId)
        def result
	    
        if (format=='json') {
            def list = []
            list << [ id: '', desc: 'Please Select', category: '', taskNumber: '']
            taskList.each {
                def desc = it.comment?.length()>50 ? it.comment.substring(0,50): it.comment
                list << [ id: it.id, desc: desc, category: it.category, taskNumber: it.taskNumber]
            }
            result = ServiceResults.success(list) as JSON
        } else {
            // Build the SELECT HTML
            def cssId = task ? 'taskDependencyEditId' : 'taskDependencyId'
            def selectName = params.forWhom
            def firstOption = [value:'', display:'Please Select']
            def paramsMap = [ selectId:cssId, selectName:selectName, options:taskList, optionKey:'id', firstOption:firstOption]
            result = HtmlUtil.generateSelect( paramsMap )
        }

		render result
	}
	
	/**
	 * Export Special Report 
	 * @param NA
	 * @return : Export data in WH project format
	 * 
	 */
	def exportSpecialReport() {
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def today = TimeUtil.formatDateTime(getSession(), new Date(), TimeUtil.FORMAT_DATE_TIME_5)
		try{
			def filePath = "/templates/TDS-Storage-Inventory.xls"
			def filename = "${project.name}SpecialExport-${today}"
			def book = ExportUtil.workBookInstance(filename, filePath, response) 
			def spcExpSheet = book.getSheet("SpecialExport")
			def storageInventoryList = assetEntityService.getSpecialExportData( project )
			def spcColumnList = ["server_id", "app_id", "server_name", "server_type", "app_name", "tru", "tru2", "move_bundle", "move_date",
									"status","group_id", "environment", "criticality" ]
			
			for ( int r = 0; r < storageInventoryList.size(); r++ ) {
				 for( int c = 0; c < spcColumnList.size(); c++){
					def valueForSheet = storageInventoryList[r][spcColumnList[c]] ? String.valueOf(storageInventoryList[r][spcColumnList[c]]) : ""
					WorkbookUtil.addCell(spcExpSheet, c, r+1, valueForSheet )
				 }
			}
			book.write(response.getOutputStream())
		}catch( Exception ex ){
			log.error "Exception occurred while exporting data"+ex.printStackTrace()
			flash.message = ex.getMessage()
			redirect( action:"exportAssets")
		}
		redirect( action:"exportAssets")
	}
	
	/**
	 * Fetch Asset's modelType to use to select asset type fpr asset acording to model 
	 * @param : id - Requested model's id
	 * @return : assetType if exist for requested model else 0
	 */
	def retrieveAssetModelType() {
		def assetType = 0
		if(params.id && params.id.isNumber()){
			def model = Model.read( params.id )
			assetType = model.assetType ?: 0
		} 
		render assetType
	}
	
    /**
     * This action is used to populate the dependency section of the asset forms for support and dependent relationships
     * @param id : asset id
     * @return : HTML code containing support and dependent edit form
     */
	def populateDependency() {

		def returnMap = [:]
		def project = securityService.getUserCurrentProject()

		// TODO : JPM 10/2014 : Move populateDependency method to a service to avoid muliple transations just to populate a model

		if (params.id && params.id.isLong()) {
			def assetEntity = AssetEntity.findByIdAndProject( params.id.toLong(), project )
			if( assetEntity ) {

				def dependentAssets = assetEntityService.getDependentAssets(assetEntity)
				def supportAssets = assetEntityService.getSupportingAssets(assetEntity)
				def dependencyType = assetEntityService.getDependencyTypes()
				def dependencyStatus = assetEntityService.getDependencyStatuses()
				def moveBundleList = assetEntityService.getMoveBundles(project)

				// TODO - JPM 8/2014 - Why do we have this? Seems like we should NOT be passing that to the template...
				def nonNetworkTypes = [AssetType.SERVER.toString(),AssetType.APPLICATION.toString(),AssetType.VM.toString(),
					AssetType.FILES.toString(),AssetType.DATABASE.toString(),AssetType.BLADE.toString()]
				
				returnMap = [ 
					assetClassOptions: AssetClass.getClassOptions(),
					assetEntity: assetEntity,
					dependencyStatus: dependencyStatus, 
					dependencyType: dependencyType, 
					dependentAssets: dependentAssets, 
					moveBundleList: moveBundleList,
					nonNetworkTypes: nonNetworkTypes,
					supportAssets: supportAssets, 
					whom: params.whom
				]
			} else {
				render "Unable to find requested asset"
				return
			}
		} else {
			render "An invalid asset id was submitted"
			return
		}
		render(template: 'dependentCreateEdit', model: returnMap)
	}

	/**
	 * Returns a lightweight list of assets filtered on  on the asset class
	 * @param id - class of asset to filter on (e.g. Application, Database, Server)
	 * @return JSON array of asset id, assetName
	 */
	def assetSelectDataByClass() {
		def project = securityService.getUserCurrentProject()

		def assetList = assetEntityService.getAssetsByType(params.id, project)
		//log.info "getAssetSelectDataByClass() $assetList"
		def assets = []
		assetList?.each() { assets << [value: it.id, caption: it.assetName] 
		}
		def map = [assets:assets]
		render map as JSON
	}
	
	/**
	 * This method is used to get validation for particular fields
	 * @param type,validation
	 * @return
	 */
	def retrieveAssetImportance() {
		def assetType = params.type
		def validation = params.validation
		def configMap = assetEntityService.getConfig(assetType,validation)
		render configMap.config as JSON
	}
	/**
	 * This method is used to get highlighting css for particular fields
	 * @param type,validation
	 * @return
	 */
	def retrieveHighlightCssMap() {
		def assetType = params.type
		def validation = params.validation
		def assetEntityInstance
		def forWhom
		switch(assetType){
			case 'Application': 
				assetEntityInstance = params.id? Application.get(params.id): new Application(appOwner:'')
			break;
			case 'Database':
				assetEntityInstance = params.id? Database.get(params.id): new Database(appOwner:'')
			break;
			case 'Files':
				assetEntityInstance = params.id? Files.get(params.id): new Files(appOwner:'')
			break;
			default:
				assetEntityInstance = params.id? AssetEntity.get(params.id): new AssetEntity(appOwner:'')
		}
		def configMap = assetEntityService.getConfig(assetType,validation)
		def highlightMap = assetEntityService.getHighlightedInfo(assetType, assetEntityInstance, configMap)
		render highlightMap as JSON
	}

	
	/**
	 * This method is used to update columnList with custom labels.
	 * @param entityDTAMap :  dataTransferEntityMap for entity type
	 * @param columnslist :  column Names
	 * @param project :project instance
	 * @return
	 */
	def retrieveColumnNames(entityDTAMap, columnslist, project) {
		entityDTAMap.eachWithIndex { item, pos ->
			if(customLabels.contains( item.columnName )){
				def customLabel = project[item.eavAttribute?.attributeCode] ? project[item.eavAttribute?.attributeCode] : item.columnName
				columnslist.add( customLabel )
			} else {
				columnslist.add( item.columnName )
			}
		}
		return columnslist
	}

	/**
	 * This method is used to set Import perferences.(ImportApplication,ImportServer,ImportDatabase,
	 * ImportStorage,ImportRoom,ImportRack,ImportDependency)
	 * @param preference
	 * @param value
	 */
	def setImportPerferences() {
		def key = params.preference
		def value = params.value
		if (value) {
			userPreferenceService.setPreference( key, value )
			session.setAttribute(key,value)
		}
		render true
	}

	 /**
	  * Action to return on list Dependency
	  */
	def listDependencies() {
		def project = securityService.getUserCurrentProject();
		if (!project) {
			flash.message = "Please select project to view Dependencies"
			redirect(controller:'project',action:'list')
			return
		}
		def entities = assetEntityService.entityInfo( project )
		def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		def depPref= assetEntityService.getExistingPref('Dep_Columns')
		def attributes =[ 'c1':'C1','c2':'C2','c3':'C3','c4':'C4','frequency':'Frequency','comment':'Comment','direction':'Direction']
		def columnLabelpref = [:]
		depPref.each{key,value->
			columnLabelpref << [ (key):attributes[value] ]
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
	* This method is to show list of dependencies using jqgrid.
	*/
	def listDepJson() {
		def sortIndex = params.sidx ?: 'asset'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def project = securityService.getUserCurrentProject()
		def sid
		 
		def filterParams = ['assetName':params.assetName, 'assetType':params.assetType, 'assetBundle':params.assetBundle, 'type':params.type,
							'dependentName':params.dependentName, 'dependentType':params.dependentType,'dependentBundle':params.dependentBundle,
							'status':params.status,'frequency':params.frequency, 'comment':params.comment,'c1':params.c1,'c2':params.c2,'c3':params.c3,
							'c4':params.c4, 'direction':params.direction]
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
				WHERE ae.project_id = ${project.id} 
				ORDER BY ${sortIndex + " " + sortOrder}
			) AS deps 
		 """)
		 
		// Handle the filtering by each column's text field
		def firstWhere = true
		filterParams.each {
			if(it.getValue())
				if(firstWhere){
					query.append(" WHERE deps.${it.getKey()} LIKE '%${it.getValue()}%'")
					firstWhere = false
				} else {
					query.append(" AND deps.${it.getKey()} LIKE '%${it.getValue()}%'")
				}
		}
		
		
		def dependencies = jdbcTemplate.queryForList(query.toString())
		
		def totalRows = Long.parseLong(dependencies.size().toString())
		def numberOfPages = Math.ceil(totalRows / maxRows)
		
		if(totalRows > 0)
			dependencies = dependencies[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			dependencies = []
		
		def results = dependencies?.collect {
			[ cell:
				[
					it.assetName, 
					it.assetType, 
					it.assetBundle, 
					it.type,
					it.dependentName, 
					it.dependentType, 
					it.dependentBundle,
					(depPref['1']!='comment') ? it[depPref['1']] : (it[depPref['1']]? "<div class='commentEllip'>${it.comment}</div>" : ''), 
					(depPref['2']!='comment') ? it[depPref['2']] : (it[depPref['2']]? "<div class='commentEllip'>${it.comment}</div>" : ''),
					it.status,
					it.assetId, // 10
					it.dependentId, // 11
					it.assetClass,	// 12
					it.dependentClass	// 13
				], id: it.id
			]}
		
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		
		render jsonData as JSON
	}
	/**
	* This method is used to change bundle when on change of asset in dependency.
	* @param dependentId
	* @param assetId
	* @render resultMap
	*/
	 
	def retrieveChangedBundle() {
		def dependentId = params.dependentId
		def dependent = AssetDependency.read(dependentId.isInteger() ? dependentId.toInteger() : -1)
		def depBundle = params.dependentId == "support" ? dependent?.asset?.moveBundle?.id : dependent?.dependent?.moveBundle?.id
		def resultMap = ["id": AssetEntity.read(params.assetId)?.moveBundle?.id ,"status":dependent?.status, 
			"depBundle":depBundle]
		render resultMap as JSON
	}
	
	/**
	 * This method is used to sort AssetList in dependencyConsole
	 * @param Assetlist
	 * @param sortParam
	 * @param orderParam
	 * @return list
	 */
	def sortAssetByColumn (assetlist, sortOn, orderBy) {
		assetlist.sort { a, b ->
			if (orderBy == 'asc') {
				(a.asset?."${sortOn}".toString() <=> b.asset?."${sortOn}".toString())
			} else {
				(b.asset?."${sortOn}".toString() <=> a.asset?."${sortOn}".toString())
			}
		}
		return assetlist
	}

	/**
	 * Used to return a SELECT for a specified roomId and sourceType
	 * @param params.roomId - room id
	 * @param params.rackId - the rack id of the currently selected rack
	 * @param params.sourceTarget - S)ource or T)arget
	 * @param params.forWhom - indicates if it is Create or Edit
	 */
	def retrieveRackSelectForRoom() {
		def project = controllerService.getProjectForPage(this)
		def roomId = params.roomId
		def rackId = params.rackId
		def options = assetEntityService.getRackSelectOptions(project, roomId, true)
		def sourceTarget = params.sourceTarget
		def forWhom = params.forWhom
		def tabindex = params.tabindex
		
		def rackDomId
		def rackDomName
		def clazz
		
		if ( sourceTarget== 'S' ) {
			rackDomId = 'rackSId'
			rackDomName = 'rackSourceId'
			clazz = 'config.sourceRack'
		} else {
			rackDomId = 'rackTId'
			rackDomName = 'rackTargetId'
			clazz = 'config.targetRack'
		}
		render(template:'deviceRackSelect',	model:[options:options, rackDomId:rackDomId, rackDomName:rackDomName, clazz:clazz, rackId:rackId, forWhom:forWhom, tabindex:tabindex?tabindex:0, sourceTarget: sourceTarget])
	}

	/**
	 * Used to return a SELECT for a specified roomId and sourceType
	 * @param params.roomId - room id
	 * @param params.id - the chassis id of the currently selected chassis
	 * @param params.sourceTarget - S)ource or T)arget
	 * @param params.forWhom - indicates if it is Create or Edit
	 */
	def retrieveChassisSelectForRoom() {
		def project = controllerService.getProjectForPage(this)
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
		
		if ( sourceTarget== 'S' ) {
			rackDomId = 'chassisSelectSId'
			rackDomName = 'chassisSelectSourceId'
			clazz = ''
		} else {
			rackDomId = 'chassisSelectTId'
			rackDomName = 'chassisSelectTargetId'
			clazz = ''
		}
		domClass=(domClass==null)?clazz:domClass
		render(template:'deviceChassisSelect', model:[options:options, domId:rackDomId, domName:rackDomName, domClass:domClass, value:id, forWhom:forWhom, sourceTarget:sourceTarget, tabindex:tabindex])
	}


	def retrieveAssetsByType() {
		def project = securityService.getUserCurrentProject();
		if (!project) {
			flash.message = "Please select project to view assets"
			redirect(controller:'project',action:'list')
			return
		}
		def assetType= params.assetType

		try {
			if (assetType == 'Other') {
				assetType = AssetType.NETWORK.toString();
			}
			def groups = [assetType]
			def info = assetEntityService.entityInfo(project, groups)
			def assets = []
			switch (assetType) {
				case AssetType.SERVER.toString():
					assets = info.servers;
					break;
				case AssetType.APPLICATION.toString():
					assets = info.applications;
					break;
				case AssetType.DATABASE.toString():
					assets = info.dbs;
					break;
				case AssetType.STORAGE.toString():
					assets = info.files;
					break;
				case AssetType.NETWORK.toString():
					assets = info.networks;
					break;
			}
			def result = [:]
			result.list = []
			result.type = assetType
        	assets.each {
        		result.list << [ id:it[0], name: it[1]]
        	}
			render(ServiceResults.success(result) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	/**
	 * This service retrieves all the assets for a given asset class.
	 */
	def assetsByClass = {
		def results = assetEntityService.getAssetsByClass(params)
		render(ServiceResults.success(results) as JSON)
	}

	def assetClasses = {
		def classes = assetEntityService.getAssetClasses()
		def results = []
		classes.each{ k,v -> results << [key:k, label:v]}
		render(ServiceResults.success(results) as JSON)
	}

	def classForAsset = {
		def asset = AssetEntity.get(params.id)
		def assetClass = AssetClass.getClassOptionForAsset(asset)
		render(ServiceResults.success([assetClass : assetClass]) as JSON)
	}

	/**
	 * 
	 */
	def poiDemo() {
		return 
	}
	
	def exportPoiDemo() {
		def filePath = "/templates/TDSMaster_Poi_template.xls" // Template file Path
		def today = TimeUtil.formatDateTime(getSession(), new Date(), TimeUtil.FORMAT_DATE_TIME_5)
		def filename = "Demo_POI_Export-${today}" // Export file name
		def project = securityService.getUserCurrentProject()

		def assetEntities = AssetEntity.findAllByProject(project, [max:50])
		File file  = ApplicationHolder.application.parentContext.getResource(filePath).getFile()

		//getting FileInputStream instance for template file
		FileInputStream fileInputStream = new FileInputStream( file );

		//creating HSSFWorkbook insatnce with using template fileInput stream 
		HSSFWorkbook workbook = new HSSFWorkbook(fileInputStream);
		// Get sheet with name 'Server'
		HSSFSheet sheet = workbook.getSheet("Servers");
		
		org.apache.poi.ss.usermodel.Cell cell = null;
		Row row = null;
		
		//Get all column count of first row or header
		def colCount = sheet.getRow(0).physicalNumberOfCells
		def dataTransferSetInstance = DataTransferSet.findById( 1 )
		def serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Servers" )
		def serverMap = [:]
		def serverColumnNameList =[]
		def serverSheetColumnNames = [:]
		serverDTAMap.eachWithIndex { item, pos ->
			serverMap.put( item.columnName, null )
			serverColumnNameList.add(item.columnName)
		}
		serverMap.put("DepGroup", null )
		serverColumnNameList.add("DepGroup")

		def serverCol = sheet.getRow(0).getPhysicalNumberOfCells()
		for ( int c = 0; c < serverCol; c++ ) {
			def serverCellContent = sheet.getRow(0).getCell(c).stringCellValue
			serverSheetColumnNames.put(serverCellContent, c)
			if( serverMap.containsKey( serverCellContent ) ) {
				serverMap.put( serverCellContent, c )
			}
		}
		for (int r=1; r<=assetEntities.size(); r++){
			// creating row here 
			row = sheet.createRow(r);
			// creating cell for id 
			cell = row.createCell(0);
			cell.setCellValue(assetEntities[r-1].id);
			for (int c=0; c<serverColumnNameList.size(); c++){
				def colName = serverColumnNameList.get(c)
				
				// creating cell for specified column
				cell = row.createCell(serverMap[colName]);
				def attribute = serverDTAMap.eavAttribute.attributeCode[serverMap[colName]]
				if(attribute)
					cell.setCellValue(String.valueOf(assetEntities[r-1]."$attribute" ?:"") );
			}
		}

		response.setContentType( "application/vnd.ms-excel" )
		filename = filename.replace(".xls",'')
		response.setHeader( "Content-Disposition", "attachment; filename=\""+filename+".xls\"" )

		try {
			def snagIT = new File(file.getAbsolutePath())
			FileOutputStream out =  new FileOutputStream(file);
			workbook.write(out);
			out.close();
			response.outputStream << snagIT.getBytes()
			response.outputStream.flush()

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
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
		def total = 0

		def project = securityService.getUserCurrentProject()
		if (project) {

			// The following will perform a count query and then a query for a subset of results based on the max and page
			// params passed into the request. The query will be constructed with @COLS@ tag that can be substitued when performing 
			// the actual queries.

			// TODO - need to fix this so that the values are handled correctly (reusable too)
			def max = 10
			if (params.containsKey('max') && params.max.isInteger()) {
				max = Integer.valueOf(params.max)
				if (max > 25)
					max = 25
			}
			def currentPage = 1
			if (params.containsKey('page') && params.page.isInteger()) {
				currentPage = Integer.valueOf(params.page)
				if (currentPage == 0)
					currentPage = 1
			}
			def offset = currentPage == 1 ? 0 : (currentPage - 1) * max
			

			// This map will drive how the query is constructed for each of the various options
			def qmap = [
				'APPLICATION': 		[ assetClass: AssetClass.APPLICATION, domain: Application ],
				'SERVER-DEVICE': 		[ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.getServerTypes() ],
				'DATABASE': 		[ assetClass: AssetClass.DATABASE, domain: Database ],
				'NETWORK-DEVICE': 	[ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.getNetworkDeviceTypes() ],
				// 'NETWORK-LOGICAL': 	[],
				'STORAGE-DEVICE': 	[ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.getStorageTypes() ],
				'STORAGE-LOGICAL': 	[ assetClass: AssetClass.STORAGE, domain: Files ],
				'OTHER-DEVICE': 		[ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.getNonOtherTypes(), notIn: true ],
				'ALL': 			[ domain: AssetEntity ]
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
					qparams.q = "%${params.q}%"
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

				if (log.isDebugEnabled())
					log.debug "***** Query: ${query.toString()}\nParams: $qparams}"

				// Perform query and move data into normal map
				def cquery = query.toString().replace('@COLS@', queryCount)
				if (log.isDebugEnabled())
					log.debug "***** Count Query: $cquery"
				
				total = qm.domain.executeQuery(cquery, qparams)[0]

				if (total > 0) {
					def rquery = query.toString().replace('@COLS@', queryColumns) 
					rquery = rquery + " ORDER BY a.assetName"
					if (log.isDebugEnabled())
						log.debug "***** Results Query: $rquery"

					results = qm.domain.executeQuery(rquery, qparams, [max:max, offset:offset, sort:'assetName' ])

					// Convert the columns into a map that Select2 requires
					results = results.collect{ r -> [ id:r[0], text:r[1] ]}			
				}
	 		} else {
				// TODO - Return an error perhaps by setting total to -1 and adding an extra property for a message
				log.error "assetListForSelect2() doesn't support param assetClassOption ${params.assetClassOption}"
			}

		}
		
		def map = [results:results, total: total]
		
		render map as JSON
	}
	
	/**
	 * Returns the list of models for a specific manufacturer and asset type
	 */
	def modelsOf() {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}

		def manufacturerId = params.manufacturerId
		def assetType = params.assetType
		def term = params.term
		def currentProject = securityService.getUserCurrentProject()

		try {
			def models = assetEntityService.modelsOf(manufacturerId, assetType, term, currentProject)

			render(ServiceResults.success(['models' : models]) as JSON)
		} catch (UnauthorizedException e) {
			ExceptionUtil.stackTraceToString(e)
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ExceptionUtil.stackTraceToString(e)
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ExceptionUtil.stackTraceToString(e)
			ServiceResults.forbidden(response)
		} catch (Exception e) {
			ExceptionUtil.stackTraceToString(e)
			ServiceResults.internalError(response, log, e)
		}
	}
	
	/**
	 * Returns the list of models for a specific asset type
	 */
	def manufacturer() {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def assetType = params.assetType
		def term = params.term
		def currentProject = securityService.getUserCurrentProject()

		try {
			def manufacturers = assetEntityService.manufacturersOf(assetType, term, currentProject)

			render(ServiceResults.success(['manufacturers' : manufacturers]) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	/**
	 * Returns the list of asset types for a specific manufactures
	 */
	def assetTypesOf() {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def manufacturerId = params.manufacturerId
		def term = params.term
		def currentProject = securityService.getUserCurrentProject()

		try {
			def assetTypes = assetEntityService.assetTypesOf(manufacturerId, term, currentProject)

			render(ServiceResults.success(['assetTypes' : assetTypes]) as JSON)
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	def architectureViewer() {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		
		def project = securityService.getUserCurrentProject()
		def levelsUp = NumberUtils.toInt(params.levelsUp)
		def levelsDown = NumberUtils.toInt(params.levelsDown)
		if (levelsUp == 0)
			levelsUp = 0
		if (levelsDown == 0)
			levelsDown = 3
		
		def assetName = null 
		if (params.assetId) {
			def asset = AssetEntityHelper.getAssetById(project, null, params.assetId)
			assetName = asset.assetName
		}
		
		def assetClassesForSelect = AssetClass.getClassOptions()
		assetClassesForSelect.put('ALL', 'All Classes');
		
		def defaultPrefs = ['levelsUp':'0', 'levelsDown':'3', 'showCycles':true, 'appLbl':true, 'labelOffset':'2', 'assetClasses':'ALL']
		def graphPrefs = userPreferenceService.getPreference('archGraph', loginUser)
		def prefsObject = [:]
		if (graphPrefs)
			prefsObject = JSON.parse(graphPrefs)
		else
			prefsObject = defaultPrefs
			
		def assetTypes = assetEntityService.ASSET_TYPE_NAME_MAP

		def model = [
			assetId : params.assetId,
			assetName: assetName,
			levelsUp: levelsUp,
			levelsDown: levelsDown,
			assetClassesForSelect: assetClassesForSelect,
			moveBundleList: assetEntityService.getMoveBundles(project),
			dependencyStatus: assetEntityService.getDependencyStatuses(),
			dependencyType: assetEntityService.getDependencyTypes(),
			assetTypes: assetTypes,
			defaultPrefs:defaultPrefs as JSON, 
			graphPrefs:prefsObject,
			assetClassesForSelect2: AssetClass.getClassOptionsDefinition()
		]
		render([assetId:params.assetId, model: model, view:'architectureGraph'])
	}
	
	/**
	 * Returns the data needed to generate the application architecture graph
	 */
	def applicationArchitectureGraph() {
		def loginUser = securityService.getUserLogin()
		if (loginUser == null) {
			ServiceResults.unauthorized(response)
			return
		}
		try {
			def project = securityService.getUserCurrentProject()
			def assetId = NumberUtils.toInt(params.assetId)
			def asset = AssetEntity.get(assetId)
			def levelsUp = NumberUtils.toInt(params.levelsUp)
			def levelsDown = NumberUtils.toInt(params.levelsDown)
			def deps = []
			def sups = []
			def assetsList = []
			def dependencyList = []
			
			// maps asset type names to simpler versions
			def assetTypes = assetEntityService.ASSET_TYPE_NAME_MAP
			
			// Check if the parameters are null
			if ((assetId == null || assetId == -1) || (params.levelsUp == null || params.levelsDown == null)) {
				def model = [nodes:[] as JSON, links:[] as JSON, assetId:params.assetId, levelsUp:params.levelsUp, levelsDown:params.levelsDown, assetTypes:assetTypes, assetTypesJson:assetTypes as JSON, environment: GrailsUtil.environment]
				render(view:'_applicationArchitectureGraph', model:model)
				return true
			}
			
			if (asset.project != project) {
				throw new UnauthorizedException();
			}
			
			// build the graph based on a specific asset
			if (params.mode.equals("assetId")) {
				
				// recursively get all the nodes and links that depend on the asset
				def stack = []
				def constructDeps
				constructDeps = { a, l ->
					deps.push(a)
					if ( ! (a in assetsList) )
						assetsList.push(a)
					if (l > 0) {
						def dependent = AssetDependency.findAllByAsset(a)
						dependent.each {
							if ( ! (it in dependencyList) )
								dependencyList.push(it)
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
					if ( ! (a in assetsList) )
						assetsList.push(a)
					if (l > 0) {
						def supports = AssetDependency.findAllByDependent(a)
						supports.each {
							if ( ! (it in dependencyList) )
								dependencyList.push(it)
							constructSups(it.asset, l-1)
						}
					}
				}
				constructSups(asset, levelsUp)
			
			// this mode hasn't been implemented yet
			} else if (params.mode.equals("dependencyBundle")) {
				def bundle = params.dependencyBundle
				def assets = assetDependencyBundle.findAllWhere(project:project, dependencyBundle:bundle)
			}
			
			// find any links between assets that weren't found with the DFS
			def assetIds = assetsList.id
			def extraDependencies = []
			assetsList.each { a ->
				def depsList = AssetDependency.findAllByAssetAndDependentInList(a, assetsList)
				depsList.each { dep ->
					if (! (dep in dependencyList)) {
						extraDependencies.push(dep)
					}
				}
			}
			
			// add in any extra dependencies that were found
			extraDependencies.each {
				dependencyList += it
			}
			
			def serverTypes = AssetType.getAllServerTypes()
			
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
			def criticalitySizes = ['Minor':150, 'Important':200, 'Major':325, 'Critical':500]
			
			// create a node for each asset
			assetsList.each {
				
				// get the type used to determine the icon used for this asset's node 
				assetType = it.model?.assetType?:it.assetType
				assetClass = it.assetClass?.toString() ?: ''
				size = 150
				
				type = assetClassUtil.getImageName(assetClass, assetType)
				if (type == AssetType.APPLICATION.toString())
					size = it.criticality ? criticalitySizes[it.criticality] : 200
				
				graphNodes << [
					id:it.id, name:it.assetName, 
					type:type, assetClass:it.assetClass.toString(),
					shape:shape, size:size, title:it.assetName, 
					color: ((it == asset)?('red'):('grey')), 
					parents:[], children:[], checked:false, siblings:[]
				]
			}
			
			// Create a seperate list of just the node ids to use while creating the links (this makes it much faster)
			def nodeIds = []
			graphNodes.each {
				nodeIds << it.id
			}
			def defaults = moveBundleService.getMapDefaults(graphNodes.size())
			
			
			// Create the links
			def graphLinks = []
			def i = 0
			def opacity = 1
			def statusColor = 'grey'
			dependencyList.each {
				def notApplicable = false
				def future = it.isFuture
				def unresolved = !it.isStatusResolved
				if (it.status in [AssetDependencyStatus.ARCHIVED, AssetDependencyStatus.NA, AssetDependencyStatus.TESTING])
					notApplicable = true
				def sourceIndex = nodeIds.indexOf(it.asset.id)
				def targetIndex = nodeIds.indexOf(it.dependent.id)
				if (sourceIndex != -1 && targetIndex != -1) {
					graphLinks << ["id":i, "parentId":it.asset.id, "childId":it.dependent.id, "child":targetIndex, "parent":sourceIndex, 
						"value":2, "opacity":opacity, "redundant":false, "mutual":null, "notApplicable":notApplicable, "future":future, "unresolved":unresolved]
					++i
				}
			}
			
			// Set the dependency properties of the nodes
			graphLinks.each {
				def child = it.child
				def parent = it.parent
				if ( ! it.cyclical ) {
					graphNodes[child].parents.add(it.id)
					graphNodes[parent].children.add(it.id)
				}
			}
			
			def model = [nodes:graphNodes as JSON, links:graphLinks as JSON, assetId:params.assetId, levelsUp:params.levelsUp, levelsDown:params.levelsDown, assetTypes:assetTypes, assetTypesJson:assetTypes as JSON, environment: GrailsUtil.environment]
			render(view:'_applicationArchitectureGraph', model:model)
			
		} catch (UnauthorizedException e) {
			ServiceResults.forbidden(response)
		} catch (EmptyResultException e) {
			ServiceResults.methodFailure(response)
		} catch (IllegalArgumentException e) {
			ServiceResults.forbidden(response)
		} catch (Exception e) {
			ServiceResults.internalError(response, log, e)
		}
	}

	def graphLegend () {
		def assetTypes = assetEntityService.ASSET_TYPE_NAME_MAP
		def model = [assetTypes:assetTypes]
		render(view:'_graphLegend', model:model)
	}
	
	/**
	 * Check if a user have permissions to create/edit comments
	 */
	private def userCanEditComments(commentType) {
		return ((commentType == AssetCommentType.TASK) || controllerService.checkPermissionForWS('AssetEdit', false))
	}

}
