import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat

import jxl.*
import jxl.read.biff.*
import jxl.write.*
import net.tds.util.jmesa.AssetEntityBean

import org.apache.commons.io.IOUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils
import org.apache.shiro.SecurityUtils
import java.io.File;
import org.apache.commons.lang.math.NumberUtils
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*

import com.tds.asset.Application
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetOptions
import com.tds.asset.AssetTransition
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.TaskDependency
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.AssetCommentCategory
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.eav.*
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.DateUtil
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.ControllerUtil as CU

class AssetEntityController {

	def missingHeader = ""
	int added = 0
	def skipped = []
	def partyRelationshipService
	def stateEngineService
	def workflowService
	def userPreferenceService
	def supervisorConsoleService
	def assetEntityInstanceList = []
	def jdbcTemplate
	def filterService
	def moveBundleService
	def sessionFactory
	def assetEntityAttributeLoaderService
	def assetEntityService
	def commentService
	def securityService
	def taskService
	def projectService
	def personService
	def sequenceService
	def progressService
	def quartzScheduler
	def userService
	
	protected static customLabels = ['Custom1','Custom2','Custom3','Custom4','Custom5','Custom6','Custom7','Custom8','Custom9','Custom10',
		'Custom11','Custom12','Custom13','Custom14','Custom15','Custom16','Custom17','Custom18','Custom19','Custom20','Custom21','Custom22','Custom23','Custom24']
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
	
	def index = {
		redirect action:'list', params:params
	}

	/* -----------------------------------------------------
	 * To Filter the Data on AssetEntityList Page 
	 * @author Bhuvana
	 * @param  Selected Filter Values
	 * @return Will return filters data to AssetEntity  
	 * ------------------------------------------------------ */
	def filter = {
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
	/* -------------------------------------------------------
	 * To import the asset form data
	 * @param project
	 * @ render import export form
	 * --------------------------------------------------------*/
	def assetImport = {
		//get id of selected project from project view
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def assetsByProject
		def projectInstance
		def moveBundleInstanceList
		def project
		if ( projectId != null ) {
			projectInstance = Project.findById( projectId )
			moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
		}
		def dataTransferSetImport = DataTransferSet.findAll(" from DataTransferSet dts where dts.transferMode IN ('B','I') ")
		def dataTransferSetExport = DataTransferSet.findAll(" from DataTransferSet dts where dts.transferMode IN ('B','E') ")
		if ( projectId == null ) {
			//get project id from session
			def currProj = getSession().getAttribute( "CURR_PROJ" )
			projectId = currProj.CURR_PROJ
			projectInstance = Project.findById( projectId )
			moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
			if ( projectId == null ) {
				flash.message = " No Projects are Associated, Please select Project. "
				redirect( controller:"project",action:"list" )
			}
		}
		if ( projectId != null ) {
			project = Project.findById(projectId)
			assetsByProject = AssetEntity.findAllByProject(project)
		}
		def	dataTransferBatchs = DataTransferBatch.findAllByProject(project).size()
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
		
		render( view:"importExport", model : [ assetsByProject: assetsByProject,
					projectId: projectId,
					moveBundleInstanceList: moveBundleInstanceList,
					dataTransferSetImport: dataTransferSetImport,
					dataTransferSetExport: dataTransferSetExport, prefMap:prefMap,
					dataTransferBatchs: dataTransferBatchs, args:params.list("args"), isMSIE:isMSIE, message:params.message, error:params.error] )
	}
	/* -----------------------------------------------------
	 * To Export the assets
	 * @author Mallikarjun 
	 * render export form
	 *------------------------------------------------------*/
	def assetExport = {
		render( view:"assetExport" )
	}
	
	/**
	 * This action is used to redirect control export view  
	 * render export form
	 */
	def exportAssets = {
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
	/* ---------------------------------------------------
	 * To upload the Data from the ExcelSheet
	 * @author Mallikarjun
	 * @param DataTransferSet,Project,Excel Sheet 
	 * @return currentPage( assetImport Page)
	 * --------------------------------------------------- */
	def upload = {

		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
		session.setAttribute("BATCH_ID",0)
		session.setAttribute("TOTAL_ASSETS",0)

		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		//get project Name
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def warnMsg = ''
		def forwardAction = 'assetImport'
		def flagToManageBatches = false
		def dataTransferSet = params.dataTransferSet

		if (!dataTransferSet) {
			forward action:forwardAction, params: [error: 'Import submission was missing expected parameter(s).']
			return
		}

		def dataTransferSetInstance = DataTransferSet.findById( dataTransferSet )
		def serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance, "Devices" )
		def appDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance, "Applications" )
		def databaseDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance, "Databases" )
		def filesDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance, "Files" )
		
		def projectCustomLabels = new HashMap()
		for(int i = 1; i< 25; i++){
			if (project["custom"+i]) projectCustomLabels.put(project["custom"+i], "Custom"+i)
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
		def serverColumnslist = new ArrayList()
		Date exportTime
		def dataTransferAttributeMapSheetName
		int serverAdded  = 0
		int appAdded   = 0
		int dbAdded  = 0
		int filesAdded = 0
		int dependencyAdded = 0
		int cablingAdded = 0
		int dependencyUpdated = 0
		int cablingUpdated = 0
		int commentAdded = 0;
		int commentUpdated = 0;
		int commentCount = 0
		def appColumnslist = []
		def databaseColumnslist = []
		def filesColumnslist = []
		def currentUser = null
		//get column name and sheets
		getColumnNames(serverDTAMap, serverColumnslist, project)
		getColumnNames(appDTAMap, appColumnslist, project)
		getColumnNames(databaseDTAMap, databaseColumnslist, project)
		getColumnNames(filesDTAMap, filesColumnslist, project)
		
		/*	def dependencyColumnList = ['DependentId','Type','DataFlowFreq','DataFlowDirection','status','comment']
		 def dependencyMap = ['DependentId':1, 'Type':2, 'DataFlowFreq':3, 'DataFlowDirection':4, 'status':5, 'comment':6]
		 def DTAMap = [0:'dependent', 1:'type', 2:'dataFlowFreq', 3:'dataFlowDirection', 4:'status', 5:'comment']*/
		try {
			workbook = Workbook.getWorkbook( file.inputStream )
			def sheetNames = workbook.getSheetNames()
			def flag = 0
			def sheetNamesLength = sheetNames.length
			for( int i=0;  i < sheetNamesLength; i++ ) {
				if ( sheetNameMap.contains(sheetNames[i].trim()) ) {
					flag = 1
				}
			}

			// Get the title sheet
			titleSheet = workbook.getSheet( "Title" )
			if( titleSheet != null) {
				SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
				try {
					exportTime = format.parse( (titleSheet.getCell( 1,5 ).contents).toString() )
				}catch ( Exception e) {
					forward action:forwardAction, params: [error: 'The Export date time was not found on the Title sheet.']
					return
				}

			} else {
				forward action:forwardAction, params: [error: 'The required Title tab was not found in the spreadsheet.']
				return
			}

			def serverSheet = workbook.getSheet( "Devices" )
			def appSheet = workbook.getSheet( "Applications" )
			def databaseSheet = workbook.getSheet( "Databases" )
			def filesSheet = workbook.getSheet( "Storage" )
			def dependencySheet = workbook.getSheet( "Dependencies" )
			def cablingSheet = workbook.getSheet( "Cabling" )
			def commentsSheet = workbook.getSheet( "Comments" )
			def serverColumnNames = [:]
			def appColumnNames = [:]
			def databaseColumnNames = [:]
			def filesColumnNames = [:]
			//check for column
			def serverCol = serverSheet.getColumns()
			for ( int c = 0; c < serverCol; c++ ) {
				def serverCellContent = serverSheet.getCell( c, 0 ).contents
				serverColumnNames.put(serverCellContent, c)
			}
			def appCol = appSheet.getColumns()
			for ( int c = 0; c < appCol; c++ ) {
				def appCellContent = appSheet.getCell( c, 0 ).contents
				appColumnNames.put(appCellContent, c)
			}
			def databaseCol = databaseSheet.getColumns()
			for ( int c = 0; c < databaseCol; c++ ) {
				def databaseCellContent = databaseSheet.getCell( c, 0 ).contents
				databaseColumnNames.put(databaseCellContent, c)
			}
			def filesCol = filesSheet.getColumns()
			for ( int c = 0; c < filesCol; c++ ) {
				def filesCellContent = filesSheet.getCell( c, 0 ).contents
				filesColumnNames.put(filesCellContent, c)
			}
			// Statement to check Headers if header are not found it will return Error message
			if ( params.asset == 'asset' && !checkHeader( serverColumnslist, serverColumnNames ) ) {
				def missingHeader = missingHeader.replaceFirst(",","")
				forward action:forwardAction, params: [error: " Server Column Headers : ${missingHeader} not found, Please check it."]
				return
			} else if ( params.application == 'application' && !checkHeader( appColumnslist, appColumnNames ) ) {
				def missingHeader = missingHeader.replaceFirst(",","")
				forward action:forwardAction, params: [error: " Applciations Column Headers : ${missingHeader} not found, Please check it."]
				return
			} else if ( params.database == 'database' && !checkHeader( databaseColumnslist, databaseColumnNames ) ) {
				def missingHeader = missingHeader.replaceFirst(",","")
				forward action:forwardAction, params: [error: " Databases Column Headers : ${missingHeader} not found, Please check it."]
				return
			} else if ( params.files == 'files' && !checkHeader( filesColumnslist, filesColumnNames ) ) {
				def missingHeader = missingHeader.replaceFirst(",","")
				forward action:forwardAction, params: [error: " Storage Column Headers : ${missingHeader} not found, Please check it."]
				return
			} else {
				//get user name.
				def subject = SecurityUtils.subject
				def principal = subject.principal
				def userLogin = UserLogin.findByUsername( principal )
				int assetsCount = 0
				int appCount = 0
				int databaseCount = 0
				int filesCount = 0
				int dependencyCount = 0
				int cablingCount = 0
				
				//Add Data to dataTransferBatch.
				def serverColNo = 0
				for (int index = 0; index < serverCol; index++) {
					if(serverSheet.getCell( index, 0 ).contents == "Name"){
						serverColNo = index
					}
				}
				def serverSheetrows = serverSheet.rows
				if(params.asset=='asset'){
					assetsCount
					for (int row = 1; row < serverSheetrows; row++) {
						def server = serverSheet.getCell( serverColNo, row ).contents
						if(server){
							assetsCount = row
						}
					}
				}
				def appColNo = 0
				for (int index = 0; index < appCol; index++) {
					if(appSheet.getCell( index, 0 ).contents == "Name"){
						appColNo = index
					}
				}
				def appSheetrows = appSheet.rows
				if(params.application == 'application'){
					appCount
					for (int row = 1; row < appSheetrows; row++) {
						def name = appSheet.getCell( appColNo, row ).contents
						if(name){
							appCount = row
						}
					}
				}
				def databaseSheetrows = databaseSheet.rows
				if(params.database=='database'){
					databaseCount
					for (int row = 1; row < databaseSheetrows; row++) {
						def name = databaseSheet.getCell( appColNo, row ).contents
						if(name){
							databaseCount = row
						}
					}
				}
				def filesSheetrows = filesSheet.rows
				if(params.files=='files'){
					filesCount
					for (int row = 1; row < filesSheetrows; row++) {
						def name = filesSheet.getCell( appColNo, row ).contents
						if(name){
							filesCount = row
						}
					}
				}
				def dependencySheetRow = dependencySheet.rows
				if(params.dependency=='dependency'){
					dependencyCount
					for (int row = 1; row < dependencySheetRow; row++) {
						def name = dependencySheet.getCell( appColNo, row ).contents
						if(name){
							dependencyCount = row
						}
					}
				}
				def cablingSheetRow = cablingSheet.rows
				if(params.cabling=='cable'){
					cablingCount
					for (int row = 1; row < cablingSheetRow; row++) {
						def name = cablingSheet.getCell( appColNo, row ).contents
						if(name){
							cablingCount = row
						}
					}
				}
				
				commentCount = commentsSheet.rows
				//
				// Assets
				//
				if(params.asset == 'asset') {
					flagToManageBatches = true
					session.setAttribute("TOTAL_ASSETS",assetsCount)
					def eavEntityType = EavEntityType.findByDomainName('AssetEntity')
					def serverDataTransferBatch = new DataTransferBatch()
					serverDataTransferBatch.statusCode = "PENDING"
					serverDataTransferBatch.transferMode = "I"
					serverDataTransferBatch.dataTransferSet = dataTransferSetInstance
					serverDataTransferBatch.project = project
					serverDataTransferBatch.userLogin = userLogin
					serverDataTransferBatch.exportDatetime = GormUtil.convertInToGMT( exportTime, tzId )
					serverDataTransferBatch.eavEntityType = eavEntityType
					if(serverDataTransferBatch.save()){
						session.setAttribute("BATCH_ID",serverDataTransferBatch.id)
					}
					for ( int r = 1; r < serverSheetrows ; r++ ) {
						def server = serverSheet.getCell( serverColNo, r ).contents
						if(server){
							def dataTransferValueList = new StringBuffer()
							for( int cols = 0; cols < serverCol; cols++ ) {
								def dataTransferAttributeMapInstance
								def projectCustomLabel = projectCustomLabels[serverSheet.getCell( cols, 0 ).contents.toString()]
								if(projectCustomLabel){
									dataTransferAttributeMapInstance = serverDTAMap.find{it.columnName == projectCustomLabel}
								} else {
									dataTransferAttributeMapInstance = serverDTAMap.find{it.columnName == serverSheet.getCell( cols, 0 ).contents}
								}

								//dataTransferAttributeMapInstance = DataTransferAttributeMap.findByColumnName(serverSheet.getCell( cols, 0 ).contents)
								if( dataTransferAttributeMapInstance != null ) {
									def assetId
									if( serverColumnNames.containsKey("assetId") && (serverSheet.getCell( 0, r ).contents != "") ) {
										try{
											assetId = Integer.parseInt(serverSheet.getCell( 0, r ).contents)
										} catch( NumberFormatException ex ) {
											forward action:forwardAction, params: [error: "AssetId must be an Integer on the Server tab at row ${r+1}"]
											return
										}
									}
									def dataTransferValues = "("+assetId+",'"+serverSheet.getCell( cols, r ).contents.replace("'","\\'").replace("\\", "\\\\")+"',"+r+","+serverDataTransferBatch.id+","+dataTransferAttributeMapInstance.eavAttribute.id+")"
									dataTransferValueList.append(dataTransferValues)
									dataTransferValueList.append(",")
								}
							}
							try{
								jdbcTemplate.update("insert into data_transfer_value( asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id ) values "+dataTransferValueList.toString().substring(0,dataTransferValueList.lastIndexOf(",")))
								serverAdded = r
							} catch (Exception e) {
								skipped << "Devices [row ${( r + 1 )}]"
							}
						} else {
								skipped << "Devices [row ${( r + 1 )}] - blank name"							
						}
						if (r%50 == 0){
							sessionFactory.getCurrentSession().flush();
							sessionFactory.getCurrentSession().clear();
						}
					}
				}

				//
				//  Process application
				//
				if(params.application=='application') {
					flagToManageBatches = true
					session.setAttribute("TOTAL_ASSETS",appCount)
					def eavEntityType = EavEntityType.findByDomainName('Application')
					def appDataTransferBatch = new DataTransferBatch()
					appDataTransferBatch.statusCode = "PENDING"
					appDataTransferBatch.transferMode = "I"
					appDataTransferBatch.dataTransferSet = dataTransferSetInstance
					appDataTransferBatch.project = project
					appDataTransferBatch.userLogin = userLogin
					appDataTransferBatch.exportDatetime = GormUtil.convertInToGMT( exportTime, tzId )
					appDataTransferBatch.eavEntityType = eavEntityType
					if(appDataTransferBatch.save()){
						session.setAttribute("BATCH_ID",appDataTransferBatch.id)
					}
					for ( int r = 1; r < appSheetrows ; r++ ) {
						def name = appSheet.getCell( appColNo, r ).contents
						if (name){
							def dataTransferValueList = new StringBuffer()
							// TODO - change the string appends to stringbuffer
							for( int cols = 0; cols < appCol; cols++ ) {
								
								def dataTransferAttributeMapInstance
								def projectCustomLabel = projectCustomLabels[appSheet.getCell( cols, 0 ).contents.toString()]
								if(projectCustomLabel){
									dataTransferAttributeMapInstance = appDTAMap.find{it.columnName == projectCustomLabel}
								} else {
									dataTransferAttributeMapInstance = appDTAMap.find{it.columnName == appSheet.getCell( cols, 0 ).contents}
								}

								if( dataTransferAttributeMapInstance != null ) {
									def assetId
									if( appColumnNames.containsKey("appId") && (appSheet.getCell( 0, r ).contents != "") ) {
										try{
											assetId = Integer.parseInt(appSheet.getCell( 0, r ).contents)
										} catch( NumberFormatException ex ) {
											forward action:'assetImport', params: [error: "AppId must be an Integer on the Application tab at row ${r+1}"]
											return
										}
									}
									def dataTransferValues = "("+assetId+",'"+appSheet.getCell( cols, r ).contents.replace("'","\\'").replace("\\", "\\\\")+"',"+r+","+appDataTransferBatch.id+","+dataTransferAttributeMapInstance.eavAttribute.id+")"
									dataTransferValueList.append(dataTransferValues)
									dataTransferValueList.append(",")
								}
							}
							try{
								jdbcTemplate.update(
									"insert into data_transfer_value( asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id ) values "
									+ dataTransferValueList.toString().substring(0,dataTransferValueList.lastIndexOf(","))
								)
								appAdded = r
							} catch (Exception e) {
								skipped << "Applications [row ${( r + 1 )}]"
							}
						} else {
							skipped << "Applications [row ${( r + 1 )}] - blank name"
						}
						if (r % 50 == 0){
							sessionFactory.getCurrentSession().flush();
							sessionFactory.getCurrentSession().clear();
						}
					}
				}

				//
				//  Process database
				//
				if (params.database=='database') {
					flagToManageBatches = true
					session.setAttribute("TOTAL_ASSETS",databaseCount)
					def eavEntityType = EavEntityType.findByDomainName('Database')
					def dbDataTransferBatch = new DataTransferBatch()
					dbDataTransferBatch.statusCode = "PENDING"
					dbDataTransferBatch.transferMode = "I"
					dbDataTransferBatch.dataTransferSet = dataTransferSetInstance
					dbDataTransferBatch.project = project
					dbDataTransferBatch.userLogin = userLogin
					dbDataTransferBatch.exportDatetime = GormUtil.convertInToGMT( exportTime, tzId )
					dbDataTransferBatch.eavEntityType = eavEntityType
					if(dbDataTransferBatch.save()){
						session.setAttribute("BATCH_ID",dbDataTransferBatch.id)
					}
					for ( int r = 1; r < databaseSheetrows ; r++ ) {
						def name = databaseSheet.getCell( appColNo, r ).contents
						if(name){
							def dataTransferValueList = new StringBuffer()
							for( int cols = 0; cols < databaseCol; cols++ ) {
								def dataTransferAttributeMapInstance
								def projectCustomLabel = projectCustomLabels[databaseSheet.getCell( cols, 0 ).contents.toString()]
								if(projectCustomLabel){
									dataTransferAttributeMapInstance = databaseDTAMap.find{it.columnName == projectCustomLabel}
								} else {
									dataTransferAttributeMapInstance = databaseDTAMap.find{it.columnName == databaseSheet.getCell( cols, 0 ).contents}
								}

								if( dataTransferAttributeMapInstance != null ) {
									def assetId
									if( databaseColumnNames.containsKey("dbId") && (databaseSheet.getCell( 0, r ).contents != "") ) {
										try{
											assetId = Integer.parseInt(databaseSheet.getCell( 0, r ).contents)
										} catch( NumberFormatException ex ) {
											forward action:forwardAction, params: [error: "DBId must be an Integer on the Database tab at row ${r+1}"]
										}
									}
									def dataTransferValues = "("+assetId+",'"+databaseSheet.getCell( cols, r ).contents.replace("'","\\'").replace("\\", "\\\\")+"',"+r+","+dbDataTransferBatch.id+","+dataTransferAttributeMapInstance.eavAttribute.id+")"
									dataTransferValueList.append(dataTransferValues)
									dataTransferValueList.append(",")
								}
							}
							try{
								jdbcTemplate.update("insert into data_transfer_value( asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id ) values "+dataTransferValueList.toString().substring(0,dataTransferValueList.lastIndexOf(",")))
								dbAdded = r
							} catch (Exception e) {
								skipped << "Database [row ${( r + 1 )}]"
							}
						} else {
							skipped << "Database [row ${( r + 1 )}] - blank name"
						}
						if (r%50 == 0){
							sessionFactory.getCurrentSession().flush();
							sessionFactory.getCurrentSession().clear();
						}
					}
				}

				//
				//  Process Storage (aka files)
				//
				if (params.storage=='storage') {
					flagToManageBatches = true
					session.setAttribute("TOTAL_ASSETS",filesCount)
					def eavEntityType = EavEntityType.findByDomainName('Files')
					def fileDataTransferBatch = new DataTransferBatch()
					fileDataTransferBatch.statusCode = "PENDING"
					fileDataTransferBatch.transferMode = "I"
					fileDataTransferBatch.dataTransferSet = dataTransferSetInstance
					fileDataTransferBatch.project = project
					fileDataTransferBatch.userLogin = userLogin
					fileDataTransferBatch.exportDatetime = GormUtil.convertInToGMT( exportTime, tzId )
					fileDataTransferBatch.eavEntityType = eavEntityType
					if(fileDataTransferBatch.save()){
						session.setAttribute("BATCH_ID",fileDataTransferBatch.id)
					}
					for ( int r = 1; r < filesSheetrows ; r++ ) {
						def name = filesSheet.getCell( appColNo, r ).contents
						if(name){
							def dataTransferValueList = new StringBuffer()
							for( int cols = 0; cols < filesCol; cols++ ) {
								def dataTransferAttributeMapInstance
								def projectCustomLabel = projectCustomLabels[filesSheet.getCell( cols, 0 ).contents.toString()]
								if(projectCustomLabel){
									dataTransferAttributeMapInstance = filesDTAMap.find{it.columnName == projectCustomLabel}
								} else {
									dataTransferAttributeMapInstance = filesDTAMap.find{it.columnName == filesSheet.getCell( cols, 0 ).contents}
								}

								if( dataTransferAttributeMapInstance != null ) {
									def assetId
									if( filesColumnNames.containsKey("filesId") && (filesSheet.getCell( 0, r ).contents != "") ) {
										try{
											assetId = Integer.parseInt(filesSheet.getCell( 0, r ).contents)
										} catch( NumberFormatException ex ) {
											forward action:forwardAction, params: [error: "StorageId must be an Integer on the Storage tab at row ${r+1}"]
											return
										}
									}
									String dataTransferValues = "("+assetId+",'"+filesSheet.getCell( cols, r ).contents.replace("'","\\'").replace("\\", "\\\\")+"',"+r+","+fileDataTransferBatch.id+","+dataTransferAttributeMapInstance.eavAttribute.id+")"
									dataTransferValueList.append(dataTransferValues)
									dataTransferValueList.append(",")
								}
							}
							try{
								jdbcTemplate.update("insert into data_transfer_value( asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id ) values "+dataTransferValueList.toString().substring(0,dataTransferValueList.lastIndexOf(",")))
								filesAdded = r
							} catch (Exception e) {
								skipped << "Storage [row ${( r + 1 )}]"
							}
						} else {
							skipped << "Storage [row ${( r + 1 )}] - blank name"
						}
						if (r%50 == 0){
							sessionFactory.getCurrentSession().flush();
							sessionFactory.getCurrentSession().clear();
						}
					}
				}
				
				//
				// Process Dependencies
				//
				if (params.dependency=='dependency') {
					session.setAttribute("TOTAL_ASSETS",dependencyCount)
					warnMsg += '<ul>'
					def projectInstance = securityService.getUserCurrentProject()
					def userLogins = securityService.getUserLogin()
					def skippedUpdated =0
					def skippedAdded=0
					for ( int r = 1; r < dependencySheetRow ; r++ ) {
						def assetId = NumberUtils.toDouble(dependencySheet.getCell( 1, r ).contents.replace("'","\\'"), 0).round()
						def assetName
						def assetClass
						if(!assetId){
							assetName = dependencySheet.getCell( 2, r ).contents.replace("'","\\'")
							assetClass = dependencySheet.getCell( 3, r ).contents.replace("'","\\'")
							
							if(!assetName){
								warnMsg +="<li> no asset ID or asset name for row # "+r+"</li>"
								continue;
							}
						}
						def depId = NumberUtils.toDouble(dependencySheet.getCell( 0, r ).contents.replace("'","\\'"), 0).round()
						def assetDep =  depId ? AssetDependency.get(depId) : null
						def asset
						if(assetId){
							asset = AssetEntity.get(assetId)
						} else {
							def assets = AssetEntity.findAllByAssetNameAndAssetType(assetName, assetClass)
							if(assets.size() == 0){
								warnMsg +="<li> no asset match found for asset name "+assetName +"</li>"
								continue;
							} else if(assets.size() > 1){
								warnMsg +="<li>no unique asset match for "+assetName+"</li>"
								continue;
							} else {
								asset = assets[0]
							}
						}
						def dependencyId = NumberUtils.toDouble(dependencySheet.getCell( 4, r ).contents.replace("'","\\'"), 0).round()
						def dependent
						if(dependencyId){
							dependent = AssetEntity.get(dependencyId)
						} else {
							def depName = dependencySheet.getCell( 5, r ).contents.replace("'","\\'")
							def depClass = dependencySheet.getCell( 6, r ).contents.replace("'","\\'")
							def assets = AssetEntity.findAllByAssetNameAndAssetType(depName, depClass)
							if(assets.size() == 0){
								warnMsg +="<li> no asset match found for dependent name "+depName + "<li>"
								continue;
							} else if(assets.size() > 1){
								warnMsg +="<li> no unique asset match for dependent name "+depName+"</li>"
								continue;
							} else {
								dependent = assets[0]
							}
						}
						
						def depExist = AssetDependency.findByAssetAndDependent(asset, dependent)
						assetEntityService.validateAssetList([asset?.id ]+[dependent?.id]+[assetDep?.asset?.id]+[assetDep?.dependent?.id],  project)
						def isNew = false
						if(!assetDep){
							if(!depExist){
								assetDep = new AssetDependency()
								isNew = true
							} else {
							     def msg = message(code: "assetEntity.dependency.warning", args: [asset.assetName, dependent.assetName])
								 skippedAdded +=1
								 log.error msg
								 warnMsg += "<li>$msg"
							}
						}
						if (assetDep) {
							assetDep.asset = asset
							assetDep.dependent = dependent
							assetDep.type = dependencySheet.getCell( 7, r ).contents.replace("'","\\'")
							
							assetDep.dataFlowFreq = dependencySheet.getCell( 8, r ).contents.replace("'","\\'") ?: 
								(isNew ? "Unknown" : assetDep.dataFlowFreq)
							assetDep.dataFlowDirection = dependencySheet.getCell( 9, r ).contents.replace("'","\\'") ?: 
								(isNew ? "Unknown" : assetDep.dataFlowDirection)
							assetDep.status = dependencySheet.getCell(10, r ).contents.replace("'","\\'") ?: 
								(isNew ? "Unknown" : assetDep.status)
							
							def depComment = dependencySheet.getCell( 11, r ).contents.replace("'","\\'")
							def length = depComment.length()
							if(length > 254){
								depComment = StringUtil.ellipsis(depComment,254)
								warnMsg += "<li> Comment With Dependency $dependent.assetName trimmed at 254"
							}
							assetDep.comment = depComment
							assetDep.c1 = dependencySheet.getCell( 12, r ).contents.replace("'","\\'")
							assetDep.c2 = dependencySheet.getCell( 13, r ).contents.replace("'","\\'")
							assetDep.c3 = dependencySheet.getCell(14, r ).contents.replace("'","\\'")
							assetDep.c4 = dependencySheet.getCell( 15, r ).contents.replace("'","\\'")
							assetDep.updatedBy = userLogins.person
							if(isNew)
								assetDep.createdBy = userLogin.person
								
							if(!assetDep.save(flush:true)){
								assetDep.errors.allErrors.each { log.error  it }
								isNew ? (skippedAdded +=1) : (skippedUpdated +=1)
							} else {
								isNew ? (dependencyAdded +=1) : (dependencyUpdated +=1)
							}
						}
						if (r%50 == 0){
							sessionFactory.getCurrentSession().flush();
							sessionFactory.getCurrentSession().clear();
						}

					}
					warnMsg += """
						<li>$dependencyUpdated dependencies updated
						<li>$skippedAdded new dependencies skipped 
						<li>$skippedUpdated existing dependencies skipped</ul>"""
				}
				
				/**
				 * imports Cabling data
				 */
				if(params.cabling=='cable'){
					session.setAttribute("TOTAL_ASSETS",cablingCount)
					cablingAdded = cablingCount-1
					warnMsg += '<ul>'
					def resultMap = assetEntityService.saveImportCables(cablingSheet)
					warnMsg += """
						<li>$resultMap.cablingUpdated cables updated
						<li>$resultMap.cablingSkipped new cables skipped
						$resultMap.warnMsg</ul>"""
				}
				
				/**
				 * Importing coment
				 */
				if (params.comment=='comment') {
					session.setAttribute("TOTAL_ASSETS",commentCount)
					def errorMsg = new StringBuilder("<ul>");
					def projectInstance = securityService.getUserCurrentProject()
					def userLogins = securityService.getUserLogin()
					def skippedUpdated =0
					def skippedAdded=0
					def staffList = partyRelationshipService.getAllCompaniesStaffPersons([project.client, PartyGroup.get(18)])
					
					for ( int r = 1; r < commentCount ; r++ ) {
						def recordForAddition = false
						int cols = 0 ;
						def commentIdImported = commentsSheet.getCell( cols, r ).contents.replace("'","\\'")
						def assetComment
						if(commentIdImported){
							def commentId = NumberUtils.toDouble(commentIdImported, 0).round()
							assetComment = AssetComment.get( commentId )
							if(!assetComment){
								skippedUpdated++
								errorMsg.append("<li>commentId $commentIdImported not found</li>")	
							}
						} else {
							assetComment = new AssetComment();
							recordForAddition = true
						}
						
						assetComment.commentType = AssetCommentType.COMMENT
						assetComment.project = project
						
						def assetId = commentsSheet.getCell( ++cols, r ).contents.replace("'","\\'")
						if(assetId){
							def formattedId = NumberUtils.toDouble(assetId, 0).round()
							def assetEntity = AssetEntity.findByIdAndProject(formattedId, project)
							if(assetEntity){
								assetComment.assetEntity = assetEntity
							}else{
								recordForAddition ? skippedAdded++ : skippedUpdated++
								errorMsg.append("<li>assetId $assetId not found</li>")
								continue;
							}
						} else {
							recordForAddition ? skippedAdded++ : skippedUpdated++
							continue;
						}
						
						
						
						def categoryInput = commentsSheet.getCell( ++cols, r ).contents?.replace("'","\\'")?.toLowerCase()?.trim()
						
						if(AssetCommentCategory.list.contains(categoryInput)){
							assetComment.category =  categoryInput ?: AssetCommentCategory.GENERAL
						} else{
							recordForAddition ? skippedAdded++ : skippedUpdated++
							errorMsg.append("<li>Category $categoryInput not standerd.</li>")
							continue;
						}
						
						
						def createdDateInput = commentsSheet.getCell( ++cols, r ).contents?.replace("'","\\'")
						def dateCreated = DateUtil.parseImportedCreatedDate(createdDateInput)
						if(dateCreated instanceof Date){
							assetComment.dateCreated = TimeUtil.convertInToGMT(dateCreated, tzId)
						} else {
							recordForAddition ? skippedAdded++ : skippedUpdated++
							errorMsg.append("<li> $dateCreated .</li>")
							continue;
						}
						
						
						def createdByImported = StringUtils.strip(commentsSheet.getCell( ++cols, r ).contents)
						if (currentUser == null) {
							currentUser = securityService.getUserLoginPerson()
						}
						def person = createdByImported ? personService.findPerson(createdByImported, project, staffList)?.person : currentUser
						
						if(person){
							assetComment.createdBy = person
						} else {
							recordForAddition ? skippedAdded++ : skippedUpdated++
							errorMsg.append("<li>Person $createdByImported not found.</li>")
							continue;
						}
						
						assetComment.comment = commentsSheet.getCell( ++cols, r ).contents
						
						if(!assetComment.save()){
							log.info GormUtil.allErrorsString(assetComment)
							recordForAddition ? skippedAdded++ : skippedUpdated++
						} else {
							recordForAddition ? commentAdded++ : commentUpdated++
						}
						
						
						if (r%50 == 0){
							sessionFactory.getCurrentSession().flush();
							sessionFactory.getCurrentSession().clear();
						}

					}
					
					errorMsg.append(commentAdded ? "<li>$commentAdded Comment added. </li>" : "");
					errorMsg.append(commentUpdated ? "<li>$commentUpdated Comment updated. </li>" : "");
					errorMsg.append(skippedAdded ? "<li>$skippedAdded Comment skipped while adding. </li>" : "");
					errorMsg.append(skippedUpdated ? "<li>$commentUpdated Comment skipped while updations. </li>" : "");
					
					errorMsg.append("</ul>");
					warnMsg += errorMsg.toString();
				}
				/*for( int i=0;  i < sheetNamesLength; i++ ) {
					if(sheetNames[i] == "Comments"){
						def commentSheet = workbook.getSheet(sheetNames[i])
						for( int rowNo = 1; rowNo < commentSheet.rows; rowNo++ ) {
							def dataTransferComment
							def commentId = commentSheet.getCell(1,rowNo).contents
							def assetCommentId
							if( commentId != "" && commentId != null ) {
								assetCommentId = Integer.parseInt(commentId)
								dataTransferComment = DataTransferComment.findByCommentId(commentId)
							}
							if( dataTransferComment == null ) {
								dataTransferComment = new DataTransferComment()
							}
							dataTransferComment.commentId = assetCommentId
							dataTransferComment.assetId = Integer.parseInt(commentSheet.getCell(0,rowNo).contents)
							dataTransferComment.commentType = commentSheet.getCell(3,rowNo).contents
							dataTransferComment.comment = commentSheet.getCell(4,rowNo).contents
							dataTransferComment.rowId = rowNo
							dataTransferComment.dataTransferBatch = dataTransferBatch
							dataTransferComment.save()
						}
					}
				}*/

			} // generate error message
			workbook.close()
			added = serverAdded + appAdded + dbAdded + filesAdded + dependencyAdded
			
			def message = "<b>Spreadsheet import was successful</b>" +
				( flagToManageBatches ? '<p>Please click the Manage Batches below to review and post these changes</p>' : '' ) +
				'<p>Results: <ul>' +
				"<li>${serverAdded} Servers loaded" + 
				"<li>$appAdded Applications loaded" + 
				"<li>$dbAdded Databases loaded" +
				"<li>$filesAdded Storage loaded" + 
				"<li>$dependencyAdded Dependency loaded" +
				"<li>$cablingAdded cables loaded" +
				"<li>${commentCount-1} Comments loaded" +
				warnMsg +
				( skipped.size() ? "<li>${skipped.size()} spreadsheet row${skipped.size()==0 ? ' was' : 's were'} skipped: <ul><li>${skipped.join('<li>')}</ul>" : '' ) +
				'</ul></p>'
			
			forward action:forwardAction, params: [message: message]

		} catch( NumberFormatException nfe ) {
			nfe.printStackTrace()
			forward action:forwardAction, params: [error: nfe]
		} catch( Exception ex ) {
			ex.printStackTrace()
			forward action:forwardAction, params: [error: ex]
		}
	}
	
	def export = {
		def key = "AssetExport-" + UUID.randomUUID().toString()
		progressService.create(key)
		
		def username = securityService.getUserLogin().username
		def projectId = RequestContextHolder.currentRequestAttributes().getSession().getAttribute( "CURR_PROJ" )?.CURR_PROJ
		
		
		def jobName = "TM-" + key
		log.info "Initiate Export"
		
		// Delay 2 seconds to allow this current transaction to commit before firing off the job
		Trigger trigger = new SimpleTrigger(jobName, null, new Date(System.currentTimeMillis() + 2000) )
		trigger.jobDataMap.putAll(params)
		
		def bundle = request.getParameterValues( "bundle" )
		trigger.jobDataMap.put('bundle', bundle)
		trigger.jobDataMap.put('key', key)
		trigger.jobDataMap.put('username', username)
		trigger.jobDataMap.put('projectId', projectId)
		trigger.jobDataMap.put('tzId', getSession().getAttribute( "CURR_TZ" )?.CURR_TZ)

		trigger.setJobName('ExportAssetEntityJob')
		trigger.setJobGroup('tdstm-export-asset')
		quartzScheduler.scheduleJob(trigger)

		progressService.update(key, 1, 'In progress')
		
		render(ServiceResults.success(['key' : key]) as JSON)
	}
	
	def downloadExport = {
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

	/* -------------------------------------------------------
	 * To check the sheet headers
	 * @param attributeList, SheetColumnNames
	 * @author Mallikarjun
	 * @return bollenValue 
	 *------------------------------------------------------- */  
	def checkHeader( def list, def serverSheetColumnNames  ) {
		def listSize = list.size()
		for ( int coll = 0; coll < listSize; coll++ ) {
			if( serverSheetColumnNames.containsKey( list[coll] ) || list[coll] == "DepGroup") {
				//Nonthing to perform.
			} else {
				missingHeader = missingHeader + ", " + list[coll]
			}
		}
		if( missingHeader == "" ) {
			return true
		} else {
			return false
		}
	}

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	/*------------------------------------------
	 * To get the assetEntity List 
	 * @param project
	 * @return assetList
	 *-------------------------------------------*/
	def list = {
		def filters = session.AE?.JQ_FILTERS
		session.AE?.JQ_FILTERS = []

		def project = CU.getProjectForPage( this )
		if (! project) 
			return
		
		// def prefType = (listType=='server') ? 'Asset_Columns' : 'Physical_Columns'
		def prefType = 'Asset_Columns'
		def fieldPrefs = assetEntityService.getExistingPref(prefType)
		
		Map model = assetEntityService.getDefaultModelForLists('AssetEntity', project, fieldPrefs, params, filters)

		// Check the assetEntityService.getDefaultModelForLists before adding to this list. This should ONLY be AssetEntity specific properties
		model.assetName = filters?.assetNameFilter ?:'' 
		model.assetPref = fieldPrefs 
		model.assetTag = filters?.assetTagFilter ?:'' 
		model.assetType = filters?.assetTypeFilter ?:'' 
		model.model = filters?.modelFilter ?:'' 
		model.prefType = prefType 
		model.serialNumber = filters?.serialNumberFilter ?:'' 
		model.sourceLocation = filters?.sourceLocationFilter ?:'' 
		model.sourceRack = filters?.sourceRackFilter ?:''
		model.targetLocation = filters?.targetLocationFilter ?:'' 
		model.targetRack = filters?.targetRackFilter ?:'' 
		// Used for filter toValidate from the Planning Dashboard - want a better parameter (JPM 9/2014)
		model.type = params.type

		// The customized title of the list
		def titleByFilter = [
			all: 'All Devices',
			other: 'Other Devices',
			physical: 'Physical Device',
			physicalServer: 'Physical Server',
			server: 'Server',
			storage:  'Storage Device',
			virtualServer: 'Virtual Server',
		]
		model.title = ( titleByFilter.containsKey(params.filter) ? titleByFilter[params.filter] : titleByFilter['all'] ) + ' List'

		return model
	}
	
	/**
	 * This method is used by JQgrid to load assetList
	 */
	def listJson = {
		def filterParams = [
			assetName: params.assetName, 
			assetType: params.assetType, 
			depConflicts: params.depConflicts, 
			depNumber: params.depNumber, 
			depToResolve: params.depToResolve,
			event: params.event,
			model: params.model, 
			moveBundle: params.moveBundle, 
			planStatus: params.planStatus, 
			sourceLocation: params.sourceLocation, 
			sourceRack: params.sourceRack, 
		]
		def validSords = ['asc', 'desc']
		def sortOrder = (validSords.indexOf(params.sord) != -1) ? (params.sord) : ('asc')
		def maxRows = Integer.valueOf(params.rows) 
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def project = securityService.getUserCurrentProject()
		def moveBundleList
		
		def attributes = projectService.getAttributes('AssetEntity')
		
		// def prefType = (listType=='server') ? 'Asset_Columns' : 'Physical_Columns'
		def prefType = 'Asset_Columns'
		def assetPref= assetEntityService.getExistingPref(prefType)
		
		def assetPrefVal = assetPref.collect{it.value}
		attributes.each{ attribute ->
			if(attribute.attributeCode in assetPrefVal)
				filterParams << [ (attribute.attributeCode): params[(attribute.attributeCode)]]
		}
		def sortIndex = (params.sidx in filterParams.keySet()) ? (params.sidx) : ('assetName')
		
		session.AE = [:]
		userPreferenceService.setPreference("assetListSize", "${maxRows}")
		
		if (params.event && params.event.isNumber()) {
			def moveEvent = MoveEvent.read( params.event )
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useForPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseForPlanning(project,true)
		}
		
		def assetType = params.filter ? ApplicationConstants.assetFilters[ params.filter ] : []

		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%${params.moveBundle}%", project) : []
		
		StringBuffer altColumns = new StringBuffer()
		StringBuffer joinQuery = new StringBuffer()

		// Until sourceRack is optional on the list we have to do this one
		altColumns.append(", srcRack.tag AS sourceRack")
		joinQuery.append("\n LEFT OUTER JOIN rack AS srcRack ON srcRack.rack_id=ae.rack_source_id ")

		// Tweak the columns selected and addition joins based on the user's selected columns
		assetPref.each { key,value->
			switch (value) {
				case 'appOwner':
					altColumns.append(", CONCAT(CONCAT(p1.first_name, ' '), IFNULL(p1.last_name,'')) AS appOwner")
					joinQuery.append("\n LEFT OUTER JOIN person p1 ON p1.person_id=ae.app_owner_id ")
					break
				case 'os':
					altColumns.append(", ae.hinfo AS os")
					break
				case ~/custom\d{1,}/:
					altColumns.append(", ae.${value} AS ${value}")
					break
				case 'lastUpdated':
					altColumns.append(", ee.last_updated AS ${value}")
					joinQuery.append("\n LEFT OUTER JOIN eav_entity ee ON ee.entity_id=ae.asset_entity_id ")
					break
				case 'manufacturer':
					altColumns.append(", manu.name AS manufacturer")
					joinQuery.append("\n LEFT OUTER JOIN manufacturer manu ON manu.manufacturer_id=m.manufacturer_id ")
					break
				case 'modifiedBy':
					altColumns.append(", CONCAT(CONCAT(p.first_name, ' '), IFNULL(p.last_name,'')) AS modifiedBy")
					joinQuery.append("\n LEFT OUTER JOIN person p ON p.person_id=ae.modified_by ")
					break

				case 'sourceLocation':
				case 'sourceRoom':
					altColumns.append(", srcRoom.room_name AS sourceRoom, srcRoom.location AS sourceLocation")
					joinQuery.append("\n LEFT OUTER JOIN room srcRoom ON srcRoom.room_id=ae.room_source_id ")
					break
				case 'sourceRack':
						// Already handled above
					break

				case 'targetLocation':
				case 'targetRoom':
					altColumns.append(", tgtRoom.room_name AS targetRoom, tgtRoom.location AS targetLocation")
					joinQuery.append("\n LEFT OUTER JOIN room tgtRoom ON tgtRoom.room_id=ae.room_target_id ")
					break
				case 'targetRack':
					altColumns.append(", tgtRack.tag AS targetRack")
					joinQuery.append("\n LEFT OUTER JOIN rack tgtRack ON tgtRack.rack_id=ae.rack_target_id ")
					break
				case 'validation':
					break;
				default:
					if (!['targetLocation', 'targetRack'].contains(value))
						altColumns.append(", ae.${WebUtil.splitCamelCase(value)} AS ${value} ")
			}
		}

		def query = new StringBuffer(""" 
			SELECT * FROM ( 
				SELECT ae.asset_entity_id AS assetId, ae.asset_name AS assetName, ae.asset_type AS assetType, m.name AS model,  
				IF(at.comment_type IS NULL, 'noTasks','tasks') AS tasksStatus, 
				IF(ac.comment_type IS NULL, 'noComments','comments') AS commentsStatus, 
				me.move_event_id AS event, ae.plan_status AS planStatus, 
				mb.name AS moveBundle, ae.validation AS validation
			""" )

		if (altColumns.length())
			query.append("\n${ altColumns.toString() }")

		query.append("""
				FROM asset_entity ae
				LEFT OUTER JOIN move_bundle mb ON mb.move_bundle_id=ae.move_bundle_id
				LEFT OUTER JOIN move_event me ON me.move_event_id=mb.move_event_id
				LEFT OUTER JOIN model m ON m.model_id=ae.model_id
				LEFT OUTER JOIN asset_comment at ON at.asset_entity_id=ae.asset_entity_id AND at.comment_type = '${AssetCommentType.TASK}'
				LEFT OUTER JOIN asset_comment ac ON ac.asset_entity_id=ae.asset_entity_id AND ac.comment_type = '${AssetCommentType.COMMENT}'
				""")

		if (joinQuery.length())
			query.append(joinQuery)
			
		//
		// Begin the WHERE section of the query	
		//
		query.append("\nWHERE ae.project_id = ${project.id}\nAND ae.asset_class = '${AssetClass.DEVICE}'")

		def justPlanning = userPreferenceService.getPreference("assetJustPlanning")?:'true'
		/*
		// This was being added to correct the issue when coming from the Planning Dashboard but there are some ill-effects still
		if (params.justPlanning)
			justPlanning = params.justPlanning
		*/
		if (justPlanning=='true')
			query.append("\n AND mb.use_for_planning=${justPlanning}")
		
		query.append("\n AND ae.asset_class='${AssetClass.DEVICE}'")

		def filter = params.filter ?: 'all'

		// Filter the list of assets based on if param listType == 'server' to all server types otherwise filter NOT server types
		switch(filter) {
			case 'physical':
				query.append("\n AND COALESCE(ae.asset_type,'') NOT IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getVirtualServerTypes())}) " )
				break
			case 'physicalServer':
				def phyServerTypes = AssetType.getAllServerTypes() - AssetType.getVirtualServerTypes()
				query.append("\n AND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(phyServerTypes)}) " )
				break
			case 'server':
				query.append("\n AND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getAllServerTypes())}) ")
				break
			case 'storage':
				query.append("\n AND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getStorageTypes())}) " )
				break
			case 'virtualServer':
				query.append("\n AND ae.asset_type IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getVirtualServerTypes())}) " )
				break
			case 'other':
				query.append("\n AND COALESCE(ae.asset_type,'') NOT IN (${GormUtil.asQuoteCommaDelimitedString(AssetType.getNonOtherTypes())}) " )
				break

 			case 'all': 
				break
		}

		if (params.event && params.event.isNumber() && moveBundleList)
			query.append( "\n AND ae.move_bundle_id IN (${GormUtil.asQuoteCommaDelimitedString(moveBundleList.id)})" )
			
		if (params.unassigned) {
			def unasgnMB = MoveBundle.findAll("FROM MoveBundle mb WHERE mb.moveEvent IS NULL AND mb.useForPlanning=true AND mb.project=:project ", [project:project])
			
			if (unasgnMB) {
				def unasgnmbId = GormUtil.asQuoteCommaDelimitedString(unasgnMB?.id)
				query.append( " AND (ae.move_bundle_id IN (${unasgnmbId}) OR ae.move_bundle_id IS NULL)" )
			}
		}
			
		query.append(""" GROUP BY assetId ORDER BY ${sortIndex} ${sortOrder}
			) AS assets
		""")
		
		// Setup a helper closure that is used to set WHERE or AND for the additional query specifications
		def firstWhere = true
		def whereAnd = { 
			if (firstWhere) {
				firstWhere = false
				return ' WHERE'
			} else {
				return ' AND'
			}
		}

		// Handle the filtering by each column's text field
		filterParams.each { fkey, fvalue ->
			if ( fvalue ) {
				// single quotes are stripped from the filter to prevent SQL injection
				query.append( whereAnd() + " assets.${fkey} LIKE '%${fvalue.replaceAll("'", "")}%'")
				firstWhere = false
			}
		}
		
		if (params.moveBundleId) {
			if (params.moveBundleId!='unAssigned') {
				def bundleName = MoveBundle.get(params.moveBundleId)?.name
				query.append( whereAnd() + " assets.moveBundle  = '${bundleName}' ")
			} else {
				query.append( whereAnd() + " assets.moveBundle IS NULL ")
			}
		}
		
		if (params.type && params.type == 'toValidate') {
			query.append( whereAnd() + " assets.validation='Discovery' ") //eq ('validation','Discovery')
		}

		// Allow filtering on the Validate
		if (params.toValidate && params.toValidate && ValidationType.getList().contains(params.toValidate)) {
			query.append( whereAnd() + " assets.validation='${params.toValidate}' ")
		}

		if (params.plannedStatus) {
			query.append(whereAnd() + " assets.planStatus='${params.plannedStatus}'")
		}
		
		log.debug  "query = ${query}"
		def assetList = jdbcTemplate.queryForList(query.toString())
		
		// Cut the list of selected applications down to only the rows that will be shown in the grid
		def totalRows = assetList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)
		if (totalRows > 0)
			assetList = assetList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)]
		else
			assetList = []
			
		def results = assetList?.collect {
			def commentType = it.commentType
			[ 	
				cell: [ '', it.assetName, (it.assetType ?: ''), it.model, 
					it.sourceLocation, it.sourceRack, (it[assetPref["1"]] ?: ''), it[assetPref["2"]], it[assetPref["3"]], it[assetPref["4"]], it.planStatus, it.moveBundle, 
					/*it.depNumber, (it.depToResolve==0)?(''):(it.depToResolve), (it.depConflicts==0)?(''):(it.depConflicts),*/
					it.tasksStatus, it.assetType, it.event, it.commentsStatus
				], id: it.assetId
			]
		}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]
		
		render jsonData as JSON
	}
	
	/* ----------------------------------------
	 * delete assetEntity
	 * @param assetEntityId
	 * @return assetEntityList
	 * --------------------------------------- */
	def delete = {
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
					redirect( controller:'room',action:list )
					break;
				case "rack":
					redirect( controller:'rackLayouts',action:'create' )
					break;
				case "clientConsole":
					redirect( controller:'clientConsole', action:list)
					break;
				case "application":
					redirect( controller:'application', action:list)
					break;
				case "database":
					redirect( controller:'database', action:list)
					break;
				case "files":
					redirect( controller:'files', action:list)
					break;
				case "dependencyConsole":
					forward( action:'getLists', params:[entity: 'server',dependencyBundle:session.getAttribute("dependencyBundle")])
					break;
				case "assetAudit":
					render "AssetEntity ${assetEntityInstance.assetName} deleted"
					break;
				default:
					redirect( action:list)
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
	def remove = {
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
		if ( params.clientList ){
			redirect( controller:"clientConsole", action:"list", params:[moveBundle:params.moveBundleId] )
		}else{
			redirect( action:list )
		}
	}

	/* -------------------------------------------
	 * To create New assetEntity
	 * @param assetEntity Attribute
	 * @author Mallikarjun
	 * @return assetList Page
	 * ------------------------------------------ */
	def save = {

		def project = CU.getProjectForPage( this )
		if (! project) 
			return

		assetEntityService.applyExpDateAndRetireDate(params, session.getAttribute("CURR_TZ")?.CURR_TZ)

		def redirectTo = params?.redirectTo
		def modelName = params.models
		def manufacturerName = params.manufacturers
		def assetType = params.assetType ?: 'Server'
		if (params.("manufacturer.id") && params.("manufacturer.id").isNumber()){
		   userPreferenceService.setPreference("lastManufacturer", Manufacturer.read(params.manufacturer.id)?.name)
		} 
		userPreferenceService.setPreference("lastType", assetType)
		if(redirectTo.contains("room_")){
			def newRedirectTo = redirectTo.split("_")
			redirectTo = newRedirectTo[0]
			def rackId = newRedirectTo[1]
			session.setAttribute("RACK_ID", rackId)
		}
		if( manufacturerName ){
			params.manufacturer = assetEntityAttributeLoaderService.getdtvManufacturer( manufacturerName )
			params.model = assetEntityAttributeLoaderService.findOrCreateModel(manufacturerName, modelName, assetType)
		}
		
		def bundleId = getSession().getAttribute( "CURR_BUNDLE" )?.CURR_BUNDLE
		
		if(params.assetType == "Blade")
			setBladeRoomAndLoc( params, project)
		
		def assetEntity = new AssetEntity(params)	
		assetEntity.project = project
		assetEntity.owner = project.client
		
		if(params.roomSourceId && params.roomSourceId != '-1')
			assetEntity.setRoomAndLoc( params.roomSourceId, true )
		if(params.roomTargetId && params.roomTargetId != '-1')
			assetEntity.setRoomAndLoc( params.roomTargetId, false )
		
		if(params.rackSourceId && params.rackSourceId != '-1')
			assetEntity.setRack( params.rackSourceId, true )
			
		if(params.rackTargetId && params.rackTargetId != '-1')
			assetEntity.setRack( params.rackTargetId, false )
		
		if(!params.assetTag){
			assetEntity.assetTag = projectService.getNextAssetTag(project) 
			if(!project.save(flush:true)){
				log.error "Error while updating project.lastAssetId : ${project}"
				project.errors.each { log.error  it }
			}
		}
			if(!assetEntity.hasErrors() && assetEntity.save()) {
				if( assetEntity.sourceRoom || assetEntity.targetRoom){
					assetEntity.updateRacks()
				}
					
				def loginUser = securityService.getUserLogin()
				if(assetEntity.model){
					assetEntityAttributeLoaderService.createModelConnectors( assetEntity )
				}
				flash.message = "AssetEntity ${assetEntity.assetName} created "
				def errors = assetEntityService.createOrUpdateAssetEntityDependencies(params, assetEntity, loginUser, project)
				flash.message += "</br>"+errors 
				if(params.showView == 'showView'){
					forward(action:'show', params:[id: assetEntity.id, errors:errors])
					
				} else if(params.showView == 'closeView'){
					render flash.message
				}else{
					redirectToReq( params, assetEntity, redirectTo, true )
				}
			}else {
				flash.message = "AssetEntity ${assetEntity.assetName} not created"
				def etext = "Unable to Update Asset" +
						GormUtil.allErrorsString( assetEntity )
				log.error  etext
				redirectToReq(params, assetEntity, redirectTo, false )
		}
	}
	
	def redirectToReq(params, entity, redirectTo, saved ){
		switch (redirectTo){
			case "room":
				  redirect( controller:'room',action:list )
				  break;
			case "rack":
				  session.setAttribute("USE_FILTERS", "true")
				  redirect( controller:'rackLayouts',action:'create' )
				  break;
			case "assetAudit":
					if(saved)
						render(template:'auditDetails',	model:[assetEntity:entity, source:params.source, assetType:params.assetType])
					else
						forward(action:'create', params:params)
				  break;
			case "clientConsole":
				  redirect( controller:'clientConsole', action:list)
				  break;
		    case "application":
				  redirect( controller:'application', action:list)
				  break;
			case "database":
				  redirect( controller:'database', action:list)
				  break;
			case "files":
				  redirect( controller:'files', action:list)
				  break;
			case "listComment":
				  forward(action:'listComment')
				  break;
		    case "roomAudit":
				  forward(action:'show', params:[redirectTo:redirectTo, source:params.source, assetType:params.assetType])
				  break;
		    case "dependencyConsole":
				  forward(action:'getLists', params:[entity:params.tabType,labelsList:params.labels, dependencyBundle:session.getAttribute("dependencyBundle")])
				  break;
			case "listTask":
				  render "Asset ${entity.assetName} updated."
				  break;
		    case "dependencies":
				  redirect(action:listDependencies)
				  break;
			default:
			  	redirect( action:list)
		  }
	}

	/*--------------------------------------------------------
	 * remote link for asset entity dialog.
	 *@param assetEntityId
	 *@author Mallikarjun
	 *@return retun to assetEntity to assetEntity Dialog
	 *--------------------------------------------------------- */
	def editShow = {
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
	/*--------------------------------------------------------
	 * To update assetEntity ajax overlay
	 * @param assetEntity Attributes
	 * @author Mallikarjun
	 * @return assetEnntiAttribute JSON oObject 
	 * ----------------------------------------------------------*/
	def updateAssetEntity = {
		def assetItems = []
		def assetEntityParams = params.assetEntityParams
		if(assetEntityParams) {
			def assetEntityParamsList = ( assetEntityParams.substring( 0, assetEntityParams.lastIndexOf('~') ) ).split("~,")
			def map = new HashMap()
			assetEntityParamsList.each {
				def assetParam = it.split(":")
				if(assetParam.length > 1){
					map.put(assetParam[0],assetParam[1] )
				} else {
					map.put(assetParam[0],"" )
				}
			}
			def assetEntityInstance = AssetEntity.get( params.id )
			def existingModelId = assetEntityInstance.model?.id
			def existingTargetRack = assetEntityInstance.targetRack
			def existingUposition = assetEntityInstance.targetRackPosition
			if(assetEntityInstance) {
				def bundleId = map.get('moveBundle')
				if(bundleId){
					if(Integer.parseInt(bundleId) != assetEntityInstance.moveBundle?.id){
						map.put('sourceTeamMt',null)
						map.put('targetTeamMt',null)
						map.put('sourceTeamLog',null)
						map.put('targetTeamLog',null)
						map.put('sourceTeamSa',null)
						map.put('targetTeamSa',null)
						map.put('sourceTeamDba',null)
						map.put('targetTeamDba',null)
					}
					map.put('moveBundle',MoveBundle.get(bundleId))
				} else {
					map.put('moveBundle',null)
					map.put('sourceTeamMt',null)
					map.put('targetTeamMt',null)
					map.put('sourceTeamLog',null)
					map.put('targetTeamLog',null)
					map.put('sourceTeamSa',null)
					map.put('targetTeamSa',null)
					map.put('sourceTeamDba',null)
					map.put('targetTeamDba',null)
				}

				def manufacturerId = map.get('manufacturer')
				if( manufacturerId )
					map.put('manufacturer', Manufacturer.get(manufacturerId) )

				def modelId = map.get('model')
				if( modelId )
					map.put('model',Model.get(modelId) )

				assetEntityInstance.properties = map
				assetEntityInstance.lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
				if(!assetEntityInstance.assetTag){
					assetEntityInstance.assetTag = "TDS-${assetEntityInstance.id}"
				}
				if(!assetEntityInstance.hasErrors() && assetEntityInstance.save()) {
					assetEntityInstance.updateRacks()
					def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntityInstance.attributeSet.id order by eav.sortOrder ")
					entityAttributeInstance.each{
						if( !bundleMoveAndClientTeams.contains(it.attribute.attributeCode) && it.attribute.attributeCode != "currentStatus" && it.attribute.attributeCode != "usize" ){
							assetItems << [id:assetEntityInstance.id, attributeCode:it.attribute.attributeCode,
										frontendInput:it.attribute.frontendInput,
										value:assetEntityInstance.(it.attribute.attributeCode) ? assetEntityInstance.(it.attribute.attributeCode).toString() : ""]
						}
					}
					if(existingModelId != assetEntityInstance.model?.id){
						AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
														assetToPort=null where assetTo = ? """,[assetEntityInstance])

						AssetCableMap.executeUpdate("delete from AssetCableMap where assetFrom = ?",[assetEntityInstance])
						assetEntityAttributeLoaderService.createModelConnectors( assetEntityInstance )
					}
					//we are no more using toAssetRack and toAssetUposition in assetCableMap
					/*if(existingTargetRack != assetEntityInstance.targetRack || existingUposition != assetEntityInstance.targetRackPosition){

						AssetCableMap.executeUpdate("""Update AssetCableMap set toAssetRack='${assetEntityInstance.targetRack}',
	            				toAssetUposition=${assetEntityInstance.targetRackPosition} where assetTo = ? """,[assetEntityInstance])
					}*/
				} else {
					def etext = "Unable to Update Asset" +
							GormUtil.allErrorsString( assetEntityInstance )
					log.error  etext
					log.error( etext )
				}
			}
		}
		render assetItems as JSON
	}
	/*To get the  Attributes
	 *@param attributeSet
	 *@author Lokanath
	 *@return attributes as a JSON Object 
	 */
	def getAttributes = {
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
	def getAssetAttributes = {
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
	def getAutoCompleteDate = {
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
	def listComments = {
		def assetEntityInstance = AssetEntity.get( params.id )
		def commentType = params.commentType;
		def assetCommentsInstance
		if (commentType) {
			if (commentType != 'comment') {
				commentType = 'issue'
			}
			assetCommentsInstance = AssetComment.findAllByAssetEntityAndCommentType( assetEntityInstance, commentType )
		} else {
			assetCommentsInstance = AssetComment.findAllByAssetEntity( assetEntityInstance )
		}
		def assetCommentsList = []
		def today = new Date()
		def css //= 'white'
		assetCommentsInstance.each {
			css = it.dueDate < today ? 'Lightpink' : 'White'
			assetCommentsList <<[ commentInstance : it, assetEntityId : it.assetEntity.id,cssClass:css, 
									assetName: it.assetEntity.assetName,assetType:it.assetEntity.assetType,
									assignedTo: it.assignedTo?it.assignedTo.toString():'', role: it.role?it.role:'']
		}
		render assetCommentsList as JSON
	}
	/* ------------------------------------------------------------------------
	 * return the comment record
	 * @param assetCommentId
	 * @author Lokanath
	 * @return assetCommentList
	 * ---------------------------------------------------------------------- */
	def showComment = {
		def commentList = []
		def personResolvedObj
		def personCreateObj
		def dtCreated
		def dtResolved
		
		def estformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def dateFormatter = new SimpleDateFormat("MM/dd/yyyy ");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetComment = AssetComment.get(params.id)
		if(assetComment){
			if(assetComment.createdBy){
				personCreateObj = Person.find("from Person p where p.id = $assetComment.createdBy.id")?.toString()
				dtCreated = estformatter.format(TimeUtil.convertInToUserTZ(assetComment.dateCreated, tzId));
			}
			if(assetComment.dateResolved){
				personResolvedObj = Person.find("from Person p where p.id = $assetComment.resolvedBy.id")?.toString()
				dtResolved = estformatter.format(TimeUtil.convertInToUserTZ(assetComment.dateResolved, tzId));
			}
			
			def etStart =  assetComment.estStart ? estformatter.format(TimeUtil.convertInToUserTZ(assetComment.estStart, tzId)) : ''
			
			def etFinish = assetComment.estFinish ? estformatter.format(TimeUtil.convertInToUserTZ(assetComment.estFinish, tzId)) : ''
			
			def atStart = assetComment.actStart ? estformatter.format(TimeUtil.convertInToUserTZ(assetComment.actStart, tzId)) : ''
			
		    def dueDate = assetComment.dueDate ? dateFormatter.format(TimeUtil.convertInToUserTZ(assetComment.dueDate, tzId)): ''
	
			def workflowTransition = assetComment?.workflowTransition
			def workflow = workflowTransition?.name
			
			def noteList = assetComment.notes.sort{it.dateCreated}
			def notes = []
			noteList.each {
				def dateCreated = it.dateCreated ? TimeUtil.convertInToUserTZ(it.dateCreated, tzId).format("E, d MMM 'at ' HH:mma") : ''
				notes << [ dateCreated , it.createdBy.toString() ,it.note]
			}
			
			// Get the name of the User Role by Name to display
			def roles = securityService.getRoleName(assetComment.role)
			
			// TODO : Runbook : the use of maxVal is incorrect.  I believe that this is for the max assetComment.taskNumber but is getting taskDependency.id. This fails
			// when there are no taskDependencies as Null gets incremented down in the map return.  Plus the property should be completely calculated here instead of incrementing
			// while assigning in the map.  Logic should test for null.
			def maxVal = TaskDependency.list([sort:'id',order:'desc',max:1])?.id[0]
			if (maxVal) {
				maxVal++
			} else {
				maxVal = 1
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
		 
		// TODO : Security : Should reduce the person objects (create,resolved,assignedTo) to JUST the necessary properties using a closure
			assetComment.durationScale = assetComment.durationScale.toString()
			commentList << [ 
				assetComment:assetComment,
				durationScale:assetComment.durationScale.value(),
				personCreateObj:personCreateObj, personResolvedObj:personResolvedObj, dtCreated:dtCreated ?: "",
				dtResolved:dtResolved ?: "", assignedTo:assetComment.assignedTo?.toString() ?:'Unassigned', assetName:assetComment.assetEntity?.assetName ?: "",
				eventName:assetComment.moveEvent?.name ?: "", dueDate:dueDate, etStart:etStart, etFinish:etFinish,atStart:atStart,notes:notes,
				workflow:workflow,roles:roles?:'Unassigned', predecessorTable:predecessorTable, successorTable:successorTable,maxVal:maxVal,
				cssForCommentStatus:cssForCommentStatus, statusWarn:taskService.canChangeStatus ( assetComment ) ? 0 : 1, 
				successorsCount:successorsCount, predecessorsCount:predecessorsCount, assetId:assetComment.assetEntity?.id ?: "" ,assetType:assetComment.assetEntity?.assetType, predecessorList: predecessorList, successorList: successorList]
		}else{
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
	// def saveComment = { com.tdsops.tm.command.AssetCommentCommand cmd ->
	def saveComment = {
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
	def updateComment = {
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
			forward(controller:"clientTeams", action:"listComment", params:[view:params.view, tab:params.tab])
		} else if( params.open != "view" ){
			render map as JSON
		}
	}
	
	/* delete the comment record
	 * @param assetComment
	 * @author Lokanath
	 * @return assetCommentList 
	 */
	def deleteComment = {
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

	/*--------------------------------------------
	 * 	Get asset details part in dashboard page
	 * 	@author: 	Lokanath Reddy
	 * 	@param :	AssetEntity
	 * 	@return:	AssetEntity Details , Recent Transitions and MoveBundle Teams
	 *-------------------------------------------*/
	def assetDetails = {
		def assetId = params.assetId
		def assetStatusDetails = []
		def statesList = []
		def recentChanges = []
		def stateIdList = []
		if(assetId){
			def assetDetail = AssetEntity.findById(assetId)
			def teamName = assetDetail.sourceTeamMt
			def assetTransition = AssetTransition.findAllByAssetEntity( assetDetail, [ sort:"dateCreated", order:"desc"] )
			def sinceTimeElapsed = "00:00:00"
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			if( assetTransition ){
				sinceTimeElapsed = convertIntegerIntoTime( GormUtil.convertInToGMT("now", tzId ).getTime() - assetTransition[0]?.dateCreated?.getTime() )
			}
			assetTransition.each{
				def cssClass
				def taskLabel = stateEngineService.getStateLabel(assetDetail.moveBundle.workflowCode,Integer.parseInt(it.stateTo))
				def time = GormUtil.convertInToUserTZ(it.dateCreated, tzId ).toString().substring(11,19)
				def timeElapsed = convertIntegerIntoTime( it.timeElapsed )
				if(it.voided == 1){
					cssClass = "void_transition"
				}
				recentChanges<<[transition:time+"/"+timeElapsed+" "+taskLabel+' ('+ it.userLogin.person.lastName +')',cssClass:cssClass]
			}
			def holdId = stateEngineService.getStateId(assetDetail.moveBundle.workflowCode, "Hold" )
			def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo, t.hold_timer as holdTimer from asset_transition t "+
					"where t.asset_entity_id = $assetId and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId ) "+
					"order by t.date_created desc, stateTo desc  limit 1 ")
			def currentState = 0
			def holdTimer = ""
			if(transitionStates.size()){
				def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
				currentState = transitionStates[0].stateTo
				holdTimer = transitionStates[0].holdTimer ? formatter.format(GormUtil.convertInToUserTZ(transitionStates[0].holdTimer, tzId )) : ""
			}
			/*def projectAssetMap = ProjectAssetMap.findByAsset(assetDetail)
			 if(projectAssetMap){
			 currentState = projectAssetMap.currentStateId
			 }*/

			def state = currentState ? stateEngineService.getState( assetDetail.moveBundle.workflowCode, currentState ) : null
			def validStates
			if(state){
				validStates= stateEngineService.getTasks( assetDetail.moveBundle.workflowCode, "SUPERVISOR", state )
			} else {
				validStates = ["Ready"]
				//stateEngineService.getTasks("STD_PROCESS","TASK_NAME")
			}
			validStates.each{
				stateIdList<<stateEngineService.getStateIdAsInt( assetDetail.moveBundle.workflowCode, it )
			}
			stateIdList.sort().each{
				statesList<<[id:stateEngineService.getState(assetDetail.moveBundle.workflowCode,it),label:stateEngineService.getStateLabel(assetDetail.moveBundle.workflowCode,it)]
			}
			def map = new HashMap()
			map.put("assetDetail",assetDetail)
			map.put("model",assetDetail.model?.modelName)
			map.put("srcRack",(assetDetail.sourceRoom ? assetDetail.sourceRoom : '') +" / "+
					(assetDetail.sourceRack ? assetDetail.sourceRack : '') +" / "+
					(assetDetail.sourceRackPosition ? assetDetail.sourceRackPosition : ''))
			map.put("tgtRack",(assetDetail.targetRoom ? assetDetail.targetRoom : '') +" / "+
					(assetDetail.targetRack ? assetDetail.targetRack : '') +" / "+
					(assetDetail.targetRackPosition ? assetDetail.targetRackPosition : ''))
			if(teamName){
				map.put("teamName",teamName.name)
			}else{
				map.put("teamName","")
			}
			map.put("currentState",stateEngineService.getStateLabel(assetDetail.moveBundle.workflowCode,currentState))
			map.put("state",state)
			def sourceQuery = new StringBuffer("from ProjectTeam where moveBundle = $assetDetail.moveBundle.id and role = 'MOVE_TECH'")
			def targetQuery = new StringBuffer("from ProjectTeam where moveBundle = $assetDetail.moveBundle.id and role = 'MOVE_TECH'")
			if(assetDetail.sourceTeamMt){
				sourceQuery.append(" and id != $assetDetail.sourceTeamMt.id ")
			}
			if(assetDetail.targetTeamMt){
				targetQuery.append(" and id != $assetDetail.targetTeamMt.id ")
			}
			def sourceTeamMts = ProjectTeam.findAll(sourceQuery.toString())
			def targetTeamMts = ProjectTeam.findAll(targetQuery.toString())
			assetStatusDetails<<[ 'assetDetails':map, 'statesList':statesList, holdTimer:holdTimer,
						'recentChanges':recentChanges, 'sourceTeamMts':sourceTeamMts,
						'targetTeamMts':targetTeamMts, 'sinceTimeElapsed':sinceTimeElapsed ]
		}
		render assetStatusDetails as JSON

	}
	/*----------------------------------
	 * @author: Lokanath Redy
	 * @param : fromState and toState
	 * @return: boolean value to validate comment field
	 *---------------------------------*/
	def getFlag = {
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
	/**
	  * Used to create Transaction for Supervisor 
	  * TODO : JPM 9/2014 - createTransition method should no longer be needed once the AssetTracker (clientConsole/index) is removed
	  * @author: Lokanath Reddy
	  * @param : AssetEntity id, priority,assigned to value and from and to States
	  * @return: AssetEntity details and AssetTransition details
	  */
	def createTransition = {
		def assetId = params.asset
		def assetEntity = AssetEntity.get(assetId)
		def assetList = []
		def statesList = []
		def stateIdList = []
		def statusLabel
		def statusName
		def check
		def assetComment
		def message = ''
		
		if(assetEntity){
			def status = params.state
			def assignTo = params.assignTo
			def priority = params.priority
			def comment = params.comment
			def holdTime = params.holdTime
			def principal = SecurityUtils.subject.principal
			def loginUser = UserLogin.findByUsername(principal)
			def rerackedId = stateEngineService.getStateId(assetEntity.moveBundle.workflowCode,"Reracked")
			if(!rerackedId) {
				rerackedId = stateEngineService.getStateId(assetEntity.moveBundle.workflowCode,"Reracked")
			}
			def holdId = stateEngineService.getStateId(assetEntity.moveBundle.workflowCode,"Hold")
			def releasedId = stateEngineService.getStateId(assetEntity.moveBundle.workflowCode,"Release")
			def projectAssetMap = ProjectAssetMap.findByAsset(assetEntity)
			def currentStateId
			if(projectAssetMap){
				currentStateId = projectAssetMap.currentStateId
			}

			if(status != "" ){
				def transactionStatus = workflowService.createTransition(assetEntity.moveBundle.workflowCode,"SUPERVISOR", status, assetEntity, assetEntity.moveBundle, loginUser, null, comment )
				if ( transactionStatus.success ) {
					stateIdList = getStates(status,assetEntity)
					stateIdList.sort().each{
						statesList<<[id:stateEngineService.getState(assetEntity.moveBundle.workflowCode,it),label:stateEngineService.getStateLabel(assetEntity.moveBundle.workflowCode,it)]
					}
					statusLabel = stateEngineService.getStateLabel(assetEntity.moveBundle.workflowCode,stateEngineService.getStateIdAsInt(assetEntity.moveBundle.workflowCode,status))
					statusName = stateEngineService.getState(assetEntity.moveBundle.workflowCode,stateEngineService.getStateIdAsInt(assetEntity.moveBundle.workflowCode,status))
				} else {
					statusLabel = stateEngineService.getStateLabel(assetEntity.moveBundle.workflowCode,currentStateId)
					statusName = stateEngineService.getState(assetEntity.moveBundle.workflowCode,currentStateId)
					stateIdList = getStates(stateEngineService.getState(assetEntity.moveBundle.workflowCode,currentStateId),assetEntity)
					stateIdList.sort().each{
						statesList<<[id:stateEngineService.getState(assetEntity.moveBundle.workflowCode,it),label:stateEngineService.getStateLabel(assetEntity.moveBundle.workflowCode,it)]
					}
					message = transactionStatus.message
				}
			} else {
				statusLabel = stateEngineService.getStateLabel(assetEntity.moveBundle.workflowCode,currentStateId)
				statusName = stateEngineService.getState(assetEntity.moveBundle.workflowCode,currentStateId)
				stateIdList = getStates(stateEngineService.getState(assetEntity.moveBundle.workflowCode,currentStateId),assetEntity)
				stateIdList.sort().each{
					statesList<<[id:stateEngineService.getState(assetEntity.moveBundle.workflowCode,it),label:stateEngineService.getStateLabel(assetEntity.moveBundle.workflowCode,it)]
				}
			}
			if(priority){
				assetEntity.priority = Integer.parseInt( priority )
			}
			if(assignTo){
				def assignToList = assignTo.split('/')
				def projectTeam = ProjectTeam.get(assignToList[1])
				if(assignToList[0] == 's'){
					assetEntity.sourceTeamMt = projectTeam
				} else if(assignToList[0] == 't'){
					assetEntity.targetTeamMt = projectTeam
				}
			}
			if(comment){
				assetComment = new AssetComment()
				assetComment.comment = comment
				assetComment.assetEntity = assetEntity
				assetComment.commentType = 'issue'
				assetComment.category = 'moveday'
				assetComment.createdBy = loginUser.person
				assetComment.save()
			}
			def transitionStates = jdbcTemplate.queryForList("select t.asset_transition_id as id, cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
					"where t.asset_entity_id = $assetId and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId ) "+
					"order by t.date_created desc, stateTo desc limit 1 ")
			def currentStatus = 0
			if(transitionStates.size()){
				currentStatus = transitionStates[0].stateTo
				if(holdTime){
					def formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a")
					def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
					def assetTransition = AssetTransition.get(transitionStates[0].id)
					if(assetTransition.stateTo == holdId){
						assetTransition.holdTimer =  GormUtil.convertInToGMT(formatter.parse( holdTime ), tzId)
						assetTransition.save(flush:true)
					}
				}
			}
			if(statesList.size() == 0){
				check = false
			}else{
				check = true
			}
			def cssClass
			if(currentStatus == Integer.parseInt(holdId) ){
				cssClass = 'asset_hold'
				def holdAssetTransition = AssetTransition.findAll("FROM AssetTransition t WHERE t.assetEntity = ${assetId} AND t.stateTo = '${holdId}' AND t.voided = 0")
				cssClass = 'asset_hold'
				if(holdAssetTransition.size() > 0){
					def holdTimer = holdAssetTransition[0]?.holdTimer
					cssClass = (holdTimer && holdTimer.getTime() < GormUtil.convertInToGMT("now", "EDT" ).getTime()) ? 'asset_hold_overtime' : 'asset_hold'
				}
			} else if(currentStatus < Integer.parseInt(releasedId) && currentStatus != Integer.parseInt(holdId) ){
				cssClass = 'asset_pending'
			} else if(currentStatus > Integer.parseInt(rerackedId)){
				cssClass = 'asset_done'
			}
			assetEntity.save()
			def sourceTeamMt
			def targetTeamMt
			if(assetEntity.sourceTeamMt){
				sourceTeamMt = assetEntity.sourceTeamMt.name
			}
			if(assetEntity.targetTeamMt){
				targetTeamMt = assetEntity.targetTeamMt.name
			}
			def sourceQuery = new StringBuffer("from ProjectTeam where moveBundle = $assetEntity.moveBundle.id and teamCode != 'Logistics' and teamCode != 'Transport'")
			def targetQuery = new StringBuffer("from ProjectTeam where moveBundle = $assetEntity.moveBundle.id and teamCode != 'Logistics' and teamCode != 'Transport'")
			if(assetEntity.sourceTeamMt){
				sourceQuery.append(" and id != $assetEntity.sourceTeamMt.id ")
			}
			if(assetEntity.targetTeamMt){
				targetQuery.append(" and id != $assetEntity.targetTeamMt.id ")
			}
			def sourceTeamMts = ProjectTeam.findAll(sourceQuery.toString())
			def targetTeamMts = ProjectTeam.findAll(targetQuery.toString())
			assetList <<['assetEntity':assetEntity, 'sourceTeamMt':sourceTeamMt, 'targetTeamMt':targetTeamMt,
						'sourceTeamMts':sourceTeamMts,'targetTeamMts':targetTeamMts, 'statesList':statesList,
						'status':statusLabel,'cssClass':cssClass,'checkVal':check,
						'statusName':statusName, 'assetComment':assetComment,
						'message':message]
		}
		render assetList as JSON
	}

	/*-----------------------------------------
	 *@param : state value
	 *@return: List of valid stated for param state
	 *----------------------------------------*/
	def getStates(def state,def assetEntity){
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
	/*---------------------------------------------
	 * Set browser Update time interval as user preference
	 * @author : Lokanath Reddy
	 * @param : time interval
	 * @return : time interval
	 *-------------------------------------------*/
	def setTimePreference = {
		def timer = params.timer
		def updateTime =[]
		if(timer){
			userPreferenceService.setPreference( "SUPER_CONSOLE_REFRESH", "${timer}" )
		}
		def timeToUpdate = getSession().getAttribute("SUPER_CONSOLE_REFRESH")
		updateTime <<[updateTime:timeToUpdate]
		render updateTime as JSON
	}
	/** 
	 * To get unique list of task for list of assets through ajax
	 * @author : Bhuvaneshwari
	 * @param  : List of assets selected for transition
	 * @return : Common tasks for selected assets
	 */

	def getList = {
		def projectInstance = securityService.getUserCurrentProject()
		def assetArray = params.assetArray
		Set common = new HashSet()
		def taskList = []
		def temp
		def totalList = []
		def tempTaskList = []
		def sortList = []
		def stateVal
		def moveBundleInstance = MoveBundle.get(params.moveBundle)
		if(assetArray){

			def assetList = assetArray.split(",")
			assetList.each{ asset->
				def assetEntity = AssetEntity.findById(asset)
				def holdId = stateEngineService.getStateId( assetEntity.moveBundle.workflowCode, "Hold" )
				//def projectAssetMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = $asset")
				def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
						"where t.asset_entity_id = $asset and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId ) "+
						"order by t.date_created desc, stateTo desc limit 1 ")
				if(transitionStates.size()){
					stateVal = stateEngineService.getState(assetEntity.moveBundle.workflowCode,transitionStates[0].stateTo)
					temp = stateEngineService.getTasks(assetEntity.moveBundle.workflowCode,"SUPERVISOR",stateVal)
					taskList << [task:temp]
				} else {
					taskList << [task:["Ready"] ]
				}
			}
			common = (HashSet)(taskList[0].task);
			for(int i=1; i< taskList.size();i++){
				common.retainAll((HashSet)(taskList[i].task))
			}
			common.each{
				tempTaskList << stateEngineService.getStateIdAsInt(moveBundleInstance.workflowCode,it)
			}
			tempTaskList.sort().each{
				sortList << [state:stateEngineService.getState(moveBundleInstance.workflowCode,it),label:stateEngineService.getStateLabel(moveBundleInstance.workflowCode,it)]
			}
			totalList << [item:sortList,asset:assetArray]
		}
		render totalList as JSON

	}

	/* --------------------------------------
	 * 	@author : Lokanada Reddy
	 * 	@param : batch id and total assts from session 
	 *	@return imported data for progress bar
	 * -------------------------------------- */
	def getProgress = {
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
	/*
	 * @author : Srinivas
	 * @param : assetId,StatusId
	 * @return : status message
	 */
	def showStatus = {
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		def arrayId = params.id.split("_")
		def statusMsg =""
		def assetId = arrayId[0]
		def stateId = Integer.parseInt(arrayId[1])
		def stateTo = arrayId[1]
		def assetEntityInstance = AssetEntity.findById(assetId)
		def state = stateEngineService.getStateLabel( assetEntityInstance.moveBundle.workflowCode.toString(),  stateId)
		def assetTrasitionInstance = AssetTransition.find( "from AssetTransition where assetEntity = $assetEntityInstance.id and voided=0 and stateTo= '$stateTo' and isNonApplicable = 0" )
		if( assetTrasitionInstance ) {
			DateFormat formatter ;
			def formatterTime = new SimpleDateFormat("hh:mm a");
			def formatterDate = new SimpleDateFormat("MM/dd/yyyy");
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def lastupdated = GormUtil.convertInToUserTZ(assetTrasitionInstance.lastUpdated, tzId)
			def updatedTime = formatterTime.format(lastupdated)
			def updatedDate = formatterDate.format(lastupdated)
			statusMsg = "$assetEntityInstance.assetName : $state is done and was updated by $assetTrasitionInstance.userLogin.person.firstName $assetTrasitionInstance.userLogin.person.lastName at $updatedTime on $updatedDate "
		}else if( AssetTransition.find( "from AssetTransition where assetEntity = $assetEntityInstance.id and voided=0 and stateTo= '$stateTo' and isNonApplicable = 1" ) ) {
			statusMsg = "$assetEntityInstance.assetName : $state is not applicable "
		}else {
			statusMsg = "$assetEntityInstance.assetName : $state pending "
		}
		render statusMsg
	}
	
	/**
	 * This action is used to redirect to create view .
	 * @param : redirectTo 
	 * @return : render to create page based on condition as if redirectTo is assetAudit then redirecting 
	 * to auditCreate view
	 */
	def create = {
		def project = securityService.getUserCurrentProject()
		def errorMsg
		def map = [:]
		if(params.containsKey("assetEntityId")){
			 if(params.assetEntityId.isNumber()){
				 def assetEntity = AssetEntity.read(params.assetEntityId)
				 if(assetEntity){
					 if(assetEntity.project.id !=  project.id){
						 log.error "create : assetEntity.project (${assetEntity.id}) does not match user's current project (${project.id})"
						 errorMsg = "An unexpected condition with the move event occurred that is preventing an update"
					 }
				 }else{
					 log.error "create: Specified moveEvent (${params.assetEntityId}) was not found})"
					 errorMsg = "An unexpected condition with the move event occurred that is preventing an update."
				 }
			  }
		}
		if( errorMsg ) {
		   def errorMap = [errMsg : errorMsg]
		   render errorMap as JSON
		} else {
			def assetEntityInstance = new AssetEntity(appOwner:'')
	
			def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
			def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute , [sort:"value"])
			def assetType = userPreferenceService.getPreference("lastType") ?: "Server"
			def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
			def sessionManu = userPreferenceService.getPreference("lastManufacturer")
			def manufacuterer =  sessionManu ? Manufacturer.findByName(sessionManu) : manufacturers[0]
			def models=[]
			models=assetEntityService.getModelSortedByStatus(manufacuterer)
				
			def moveBundleList = MoveBundle.findAllByProject(project,[sort:"name"])
			
			def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
			
			def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)
			
			def environmentOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
	
			def railTypeAttribute = EavAttribute.findByAttributeCode('railType')
			def railTypeOption = EavAttributeOption.findAllByAttribute(railTypeAttribute)
			
			//fieldImportance for Discovery by default
			def configMap = assetEntityService.getConfig('AssetEntity','Discovery')
			
			def rooms = Room.findAll("FROM Room WHERE project =:project order by location, roomName", [project:project])
			def highlightMap = assetEntityService.getHighlightedInfo('AssetEntity', assetEntityInstance, configMap)
			
			def paramsMap = [assetEntityInstance:assetEntityInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
								planStatusOptions:planStatusOptions?.value, projectId:project.id ,railTypeOption:railTypeOption?.value,
								priorityOption:priorityOption?.value ,project:project, manufacturers:manufacturers,redirectTo:params?.redirectTo,
								models:models, assetType:assetType, manufacuterer:manufacuterer, config:configMap.config ,customs:configMap.customs,
								rooms:rooms, environmentOptions:environmentOptions?.value, highlightMap:highlightMap]
			 
			 if(params.redirectTo == "assetAudit") {
				 paramsMap << ['source':params.source, 'assetType':params.assetType]
				 render(template:"createAuditDetails",model:paramsMap)
			 }
			 
			return paramsMap
		}
	}
	
	/**
	* Renders the detail of an AssetEntity 
	*/
	def show = {

		def project = CU.getProjectForPage( this )
		if (! project) 
			return

		def assetEntity = CU.getAssetForPage(this, project, AssetEntity, params.id, true)

		if (!assetEntity) {
			flash.message = "Unable to find asset within current project with id ${params.id}"
			log.warn "show - asset id (${params.id}) not found for project (${project.id}) by user ${userLogin}"
			def errorMap = [errMsg : flash.message]
			render errorMap as JSON
		} else {
			def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntity.attributeSet.id order by eav.sortOrder ")
			def attributeOptions
			def options
			def frontEndLabel

			entityAttributeInstance.each{
				attributeOptions = EavAttributeOption.findAllByAttribute( it.attribute,[sort:'value',order:'asc'] )
				options = []
				attributeOptions.each{option ->
					options<<[option:option.value]
				}
				if( !bundleMoveAndClientTeams.contains(it.attribute.attributeCode) && it.attribute.attributeCode != "currentStatus" && it.attribute.attributeCode != "usize" ){
					frontEndLabel = it.attribute.frontendLabel
					if( customLabels.contains( frontEndLabel ) ){
						frontEndLabel = project[it.attribute.attributeCode] ? project[it.attribute.attributeCode] : frontEndLabel
					}
				}
			}

			def model = [
				assetEntity: assetEntity, 
				label: frontEndLabel
			]

			model.putAll( assetEntityService.getDefaultModelForShows('AssetEntity', project, params, assetEntity) )

			if(params.redirectTo == "roomAudit") {
				model << [source:params.source, assetType:params.assetType]
				render(template: "auditDetails", model: model)
			}
			return model
		}
	}

	/**
	 * Used to set showAllAssetTasks preference , which is used to show all or hide the inactive tasks
	 */
	def setShowAllPreference = {
		userPreferenceService.setPreference("showAllAssetTasks", params.selected=='1' ? 'TRUE' : 'FALSE')
		render true
	}
	
	/**
	 * This action is used to redirect to edit view .
	 * @param : redirectTo
	 * @return : render to edit page based on condition as if 'redirectTo' is roomAudit then redirecting
	 * to auditEdit view
	 */
	def edit = {
		def project = CU.getProjectForPage( this )
		if (! project) 
			return

		def assetEntityInstance = CU.getAssetForPage(this, project, AssetEntity, params.id, true)
		if (!assetEntityInstance) {
			render '<span class="error">Unable to find asset to edit</span>'
			return
		}

		def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)

		def railTypeAttribute = EavAttribute.findByAttributeCode('railType')
		def railTypeOption = EavAttributeOption.findAllByAttribute(railTypeAttribute)

		def rooms = assetEntityService.getRooms(project)

		def targetRacks
		def sourceRacks
		if(assetEntityInstance.roomTarget)
			targetRacks = Rack.findAllByRoom(Room.get(assetEntityInstance.roomTarget.id))

		if(assetEntityInstance.roomSource)
			sourceRacks = Rack.findAllByRoom(Room.get(assetEntityInstance.roomSource.id))

		def sourceBladeChassis = assetEntityInstance.roomSource ? AssetEntity.findAllByRoomSource(assetEntityInstance.roomSource)?.findAll{it.assetType == 'Blade Chassis'} : []
		def targetBladeChassis = assetEntityInstance.roomTarget ? AssetEntity.findAllByRoomTarget(assetEntityInstance.roomTarget)?.findAll{it.assetType == 'Blade Chassis'} : []
		def sourceChassisSelect = []
		def targetChassisSelect = []

		sourceBladeChassis.each{
			sourceChassisSelect << [it.assetTag, "${it.assetTag+'-'+''+it.assetName}"]
		}
		targetBladeChassis.each{
			targetChassisSelect << [it.assetTag, "${it.assetTag+'-'+''+it.assetName}"]
		}

		def model = [
			assetEntityInstance: assetEntityInstance, 
			manufacturers: assetEntityService.getManufacturers(assetEntityInstance.assetType), 
			models: assetEntityService.getModelSortedByStatus(assetEntityInstance.manufacturer),
			nonNetworkTypes: AssetType.getNonNetworkTypes(), 
			priorityOption: priorityOption?.value,
			quotelessName: assetEntityInstance.assetName?.replaceAll('\"', {''}), 
			railTypeOption: railTypeOption?.value, 
			rooms: rooms,
			targetRacks: targetRacks, 
			sourceChassisSelect: sourceChassisSelect, 
			sourceRacks: sourceRacks, 
			targetChassisSelect: targetChassisSelect
		]

		model.putAll( assetEntityService.getDefaultModelForEdits('AssetEntity', project, assetEntityInstance, params) )

		if (params.redirectTo == "roomAudit") {
			paramsMap << ['rooms':rooms, 'source':params.source,'assetType':params.assetType]
			render(template:"auditEdit",model:model)
		}

		return model		
	}

	/**
	 * This action is used to update assetEntity 
	 * @param redirectTo : a flag to redirect view to page after update
	 * @param id : id of assetEntity
	 * @return : render to appropriate view
	 */
	def update = {
		def project = CU.getProjectForPage( this )
		if (! project) 
			return

		def redirectTo = params.redirectTo	
		def modelName = params.models
		def manufacturerName = params.manufacturers
		def assetType = params.assetType ?: 'Server'
		
		if (params.("manufacturer.id") && params.("manufacturer.id").isNumber())
			userPreferenceService.setPreference("lastManufacturer", Manufacturer.read(params.manufacturer.id)?.name)
			
		userPreferenceService.setPreference("lastType", assetType)

		assetEntityService.applyExpDateAndRetireDate(params, session.getAttribute("CURR_TZ")?.CURR_TZ)
		def assetEntityInstance = AssetEntity.get(params.id)
		assetEntityInstance.properties = assetEntityInstance

		if (redirectTo.contains("room_")) {
			def newRedirectTo = redirectTo.split("_")
			redirectTo = newRedirectTo[0]
			def rackId = newRedirectTo[1]
			session.setAttribute("RACK_ID", rackId)
		}
		if ( manufacturerName ) {
			params.manufacturer = assetEntityAttributeLoaderService.getdtvManufacturer( manufacturerName )
			params.model = assetEntityAttributeLoaderService.findOrCreateModel(manufacturerName, modelName, assetType)
		}
		
		if(params.assetType == "Blade")
			setBladeRoomAndLoc( params, project) 
		
		if (params.roomSourceId && params.roomSourceId != '-1')
			assetEntityInstance.setRoomAndLoc( params.roomSourceId, true ) 
		if (params.roomTargetId && params.roomTargetId != '-1')
			assetEntityInstance.setRoomAndLoc( params.roomTargetId, false )
		
		if (params.rackSourceId && params.rackSourceId != '-1')
			assetEntityInstance.setRack( params.rackSourceId, true )
		if (params.rackTargetId && params.rackTargetId != '-1')
			assetEntityInstance.setRack( params.rackTargetId, false )
			
		if (!assetEntityInstance.hasErrors() && assetEntityInstance.save(flush:true)) {
			if ( assetEntityInstance.sourceRoom || assetEntityInstance.targetRoom) {
				assetEntityInstance.updateRacks()
			}
			def loginUser = securityService.getUserLogin()
			flash.message = "Asset ${assetEntityInstance.assetName} Updated <br/>"
			def errors = assetEntityService.createOrUpdateAssetEntityDependencies(params, assetEntityInstance, loginUser, project)
			flash.message += errors
			if (params.updateView == 'updateView') {
				forward(action:'show', params:[id: params.id, errors:errors])		
			} else if(params.updateView == 'closeView') {
				render flash.message
			} else {
				redirectToReq(params, assetEntityInstance, redirectTo, false )
			}
		}
		else {
			flash.message = "Asset not Updated"
            assetEntityInstance.errors.allErrors.each{ flash.message += it }
			session.AE?.JQ_FILTERS = params
            render flash.message
		}
	}
	
    /**
     * This action is used to get list of all Manufacturerss ordered by manufacturer name display at
     * assetEntity CRUD and AssetAudit CRUD
     * @param assetType : requested assetType for which we need to get manufacturer list
     * @return : render to manufacturerView
     */
	def getManufacturersList = {
		def assetType = params.assetType
		def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
		def prefVal =  userPreferenceService.getPreference("lastManufacturer")
		def selectedManu = prefVal ? Manufacturer.findByName( prefVal )?.id : null
		render (view :'manufacturerView' , model:[manufacturers : manufacturers, selectedManu:selectedManu,forWhom:params.forWhom ])
	}
	
	/**
	 * This action is used to get list of all Models to display at assetEntity CRUD and AssetAudit CRUD
	 * @param assetType : requested assetType for which we need to get manufacturer list
	 * @return : render to manufacturerView
	 */
	def getModelsList = {
		def manufacturer = params.manufacturer
		def models=[]
		if(manufacturer!="null"){
			def manufacturerInstance = Manufacturer.read(manufacturer)
			models=assetEntityService.getModelSortedByStatus(manufacturerInstance)
		}
		render (view :'_modelView' , model:[models : models, forWhom:params.forWhom])
	}
	
	/**
	 * Used to generate the List for Task Manager, which leverages a shared closure with listComment
	 */
	def listTasks = {
		def user = securityService.getUserLogin()
		try{
			if (!RolePermissions.hasPermission('ViewTaskManager')) {
				log.warn "Unauthorized user $user attempted to see Task Manager"
				//while using 'UnauthorizedException' getting  java.lang.IncompatibleClassChangeError:
				//the number of constructors during runtime and compile time for java.lang.RuntimeException do not match. Expected 4 but got 5
				//So using 'RuntimeException' for now.
				throw new RuntimeException('User does not have permission to see Task Manager')
				
			}
			
			params.commentType=AssetCommentType.TASK
			
			if(params.initSession)
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
			def taskPref= assetEntityService.getExistingPref('Task_Columns')
			def assetCommentFields = AssetComment.getTaskCustomizeFieldAndLabel()
			def modelPref = [:]
			taskPref.each{key,value->
				modelPref <<  [ (key): assetCommentFields[value] ]
			}
			def filterEvent = 0
			if(params.moveEvent){
				filterEvent=params.moveEvent
			}
			def moveEvent
			
			if(params.containsKey("justRemaining")){
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
			def viewUnpublished = params.containsKey('viewUnpublished') ? params.viewUnpublished : "0"
			def timeToRefresh = userPreferenceService.getPreference("TASKMGR_REFRESH")
			def entities = assetEntityService.entityInfo( project )
			def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
			return [timeToUpdate : timeToRefresh ?: 60,servers:entities.servers, applications:entities.applications, dbs:entities.dbs,
					files:entities.files,networks:entities.networks, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus, assetDependency: new AssetDependency(),
					moveEvents:moveEvents, filterEvent:filterEvent , justRemaining:justRemaining, justMyTasks:justMyTasks, filter:params.filter,
					comment:filters?.comment ?:'', taskNumber:filters?.taskNumber ?:'', assetName:filters?.assetEntity ?:'', assetType:filters?.assetType ?:'',
					dueDate : filters?.dueDate ?:'', status : filters?.status ?:'', assignedTo : filters?.assignedTo ?:'', role: filters?.role ?:'',
					category: filters?.category ?:'', moveEvent:moveEvent, moveBundleList : moveBundleList, viewUnpublished : viewUnpublished,
					staffRoles:taskService.getTeamRolesForTasks(), taskPref:taskPref, attributesList: assetCommentFields.keySet().sort{it}, modelPref:modelPref,
					//staffRoles:taskService.getRolesForStaff(), 
					sizePref:userPreferenceService.getPreference("assetListSize")?: '25']
		} catch (RuntimeException uex){
			log.error uex.getMessage()
			response.sendError( 401, "Unauthorized Error")
		}
	}

	/**
	 * Used to generate the List of Comments, which leverages a shared closeure with the above listTasks controller
	 */
	def listComment = {
			def project = securityService.getUserCurrentProject();
			if (!project) {
				flash.message = "Please select project to view Comments"
				redirect(controller:'project',action:'list')
				return
			}
			def entities = assetEntityService.entityInfo( project )
			def moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		    return [ rediectTo:'comment', servers:entities.servers, applications:entities.applications, dbs:entities.dbs,
				files:entities.files, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus, assetDependency: new AssetDependency(),
				moveBundleList:moveBundleList ]
	}
	
	/**
	 * Used to generate list of comments using jqgrid
	 * @return : list of tasks as JSON
	 */
	def listCommentJson = {
		def sortIndex = params.sidx ?: 'lastUpdated'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		
		def project = securityService.getUserCurrentProject()
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def dueFormatter = new SimpleDateFormat("MM/dd/yyyy")
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
				order(sid, sortOrder).ignoreCase()
			}
		def totalRows = assetCommentList.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)

		def results = assetCommentList?.collect {
			[ cell: ['',(it.comment?.length()>50 ? (it.comment.substring(0,50) + '...'): it.comment).replace("\n",""), 
					it.lastUpdated ? dueFormatter.format(TimeUtil.convertInToUserTZ(it.lastUpdated, tzId)):'',
					it.commentType ,
					it.assetEntity?.assetName ?:'',
					it.assetEntity?.assetType ?:'',
					it.category,
					 it.assetEntity?.id], 
					id: it.id]
			}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
	}
	/**
	 * This will be called from TaskManager screen to load jqgrid
	 * @return : list of tasks as JSON
	 */
	def listTaskJSON = {
		def sortIndex =  params.sidx ? params.sidx : session.TASK?.JQ_FILTERS?.sidx
		def sortOrder =  params.sidx ? params.sord : session.TASK?.JQ_FILTERS?.sord
		
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		
		userPreferenceService.setPreference("assetListSize", "${maxRows}")

		def project = securityService.getUserCurrentProject()
		def person = securityService.getUserLoginPerson()
		def moveBundleList
		def today = new Date()
		def runBookFormatter = new SimpleDateFormat("MM/dd kk:mm")
		def dueFormatter = new SimpleDateFormat("MM/dd/yyyy")
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
			if (params.viewUnpublished.equals("0"))
				eq("isPublished", true)
			assetEntity {
				if (params.assetType)
					ilike('assetType', "%${params.assetType}%")
				if (params.assetName)
					ilike('assetName', "%${params.assetName}%")
			}
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
					assetEntity {
						order(sortIndex, sortOrder).ignoreCase()
					}
				}else{
					order(sortIndex, sortOrder).ignoreCase()
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
				dueDate = it.estFinish ? runBookFormatter.format(TimeUtil.convertInToUserTZ(it.estFinish, tzId)) : ''
			} else {
				dueDate = it.dueDate ? dueFormatter.format(TimeUtil.convertInToUserTZ(it.dueDate, tzId)) : ''
			}
			
			def depCount = TaskDependency.countByPredecessor( it )
			// Have the dependency count be a link to the Task Neighborhood graph if there are dependencies
			def nGraphUrl = depCount == 0 ? depCount : '<a href="' + HtmlUtil.createLink([controller:'task', action:'taskGraph']) +
				'?neighborhoodTaskId=' + it.id + '" target="_blank",>' + depCount + '</a>'

			def status = it.status
			def userSelectedCols = []
			(1..5).each { colId ->
				def value = taskManagerValues(taskPref["$colId"],it)
				userSelectedCols << ( value?.getClass()?.isEnum() ? value?.value() : value )
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
				updatedClass, dueClass, it.assetEntity?.id, it.assetEntity?.assetType
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
				def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
				def dueFormatter = new SimpleDateFormat("MM/dd/yyyy")
				result = task[value] ? dueFormatter.format(TimeUtil.convertInToUserTZ(task[value], tzId)) : ''
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
	def assetOptions = {
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
	def saveAssetoptions = {
		def assetOptionInstance = new AssetOptions()
		def planStatusList = []
		def planStatus
		if(params.assetOptionType=="planStatus"){
			assetOptionInstance.type = AssetOptions.AssetOptionsType.STATUS_OPTION
			assetOptionInstance.value = params.planStatus
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = AssetOptions.findByValue(params.planStatus).id
			
		}else if(params.assetOptionType=="Priority"){
			assetOptionInstance.type = AssetOptions.AssetOptionsType.PRIORITY_OPTION
			assetOptionInstance.value = params.priorityOption
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = AssetOptions.findByValue(params.priorityOption).id
			
		}else if(params.assetOptionType=="dependency"){
			assetOptionInstance.type = AssetOptions.AssetOptionsType.DEPENDENCY_TYPE
			assetOptionInstance.value = params.dependencyType
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = AssetOptions.findByValue(params.dependencyType).id
			
		}else if(params.assetOptionType=="environment"){
			assetOptionInstance.type = AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION
			assetOptionInstance.value = params.environment
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = AssetOptions.findByValue(params.environment).id
			
		}else {
		    assetOptionInstance.type = AssetOptions.AssetOptionsType.DEPENDENCY_STATUS
			assetOptionInstance.value = params.dependencyStatus
			if(!assetOptionInstance.save(flush:true)){
				assetOptionInstance.errors.allErrors.each { log.error  it }
			}
			planStatus = AssetOptions.findByValue(params.dependencyStatus).id
			
		}
		planStatusList =['id':planStatus]
		
	    render planStatusList as JSON
	}
	
	/**
	 * This action is used to delete  AssetOptions by type from admin's AssetOption page .
	 */
	def deleteAssetOptions = {
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
	def assetSummary = {
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = securityService.getUserCurrentProject();
		if (!project) {
			flash.message = "Please select project to view Asset Summary"
			redirect(controller:'project',action:'list')
			return
		}
		List moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		List assetSummaryList = []
		int totalAsset = 0;
		int totalPhysical = 0;
		int totalApplication =0;
		int totalDatabase = 0;
		int totalFiles = 0;

		moveBundleList.each{ moveBundle->
			def physicalCount = AssetEntity.countByMoveBundleAndAssetTypeNotInList( moveBundle,AssetType.getNonPhysicalTypes(), params )
			def assetCount = AssetEntity.countByMoveBundleAndAssetTypeInList( moveBundle,AssetType.getServerTypes(), params )
			def applicationCount = Application.countByMoveBundle(moveBundle)
			def databaseCount = Database.countByMoveBundle(moveBundle)
			def filesCount = Files.countByMoveBundle(moveBundle)
			assetSummaryList << ["name":moveBundle, "assetCount":assetCount, "applicationCount":applicationCount, "physicalCount":physicalCount,
				"databaseCount":databaseCount, "filesCount":filesCount, id:moveBundle.id]
		}
		
		def unassignedPhysicalCount = AssetEntity.executeQuery("SELECT COUNT(*) FROM AssetEntity WHERE moveBundle = null \
						AND project = $projectId AND ifnull(assetType,'') NOT IN (${WebUtil.listAsMultiValueQuotedString(AssetType.getNonPhysicalTypes())})")[0]
		def unassignedAssetCount = AssetEntity.executeQuery("SELECT COUNT(*) FROM AssetEntity WHERE moveBundle = null \
						AND project = $projectId AND assetType IN (${WebUtil.listAsMultiValueQuotedString(AssetType.getServerTypes())})")[0]

		def unassignedAppCount = Application.executeQuery("SELECT COUNT(*) FROM Application WHERE moveBundle = null AND project = $projectId ")[0]
		def unassignedDBCount = Database.executeQuery("SELECT COUNT(*) FROM Database WHERE moveBundle = null AND project = $projectId ")[0]
		def unassignedFilesCount = Files.executeQuery("SELECT COUNT(*) FROM Files WHERE moveBundle = null AND project = $projectId ")[0]

		assetSummaryList.each{asset->
			totalAsset=totalAsset + asset.assetCount
			totalPhysical=totalPhysical+ asset.physicalCount
			totalApplication = totalApplication + asset.applicationCount
			totalDatabase = totalDatabase + asset.databaseCount
			totalFiles = totalFiles + asset.filesCount
		}
		totalAsset = totalAsset + unassignedAssetCount ;
		totalPhysical = totalPhysical + unassignedPhysicalCount;
		totalApplication = totalApplication + unassignedAppCount;
		totalDatabase = totalDatabase + unassignedDBCount;
		totalFiles =totalFiles + unassignedFilesCount;

		return [assetSummaryList:assetSummaryList, totalAsset:totalAsset, totalApplication:totalApplication, totalDatabase:totalDatabase,totalPhysical:totalPhysical,
			totalFiles:totalFiles,unassignedAssetCount:unassignedAssetCount, unassignedAppCount:unassignedAppCount, unassignedDBCount:unassignedDBCount,unassignedPhysicalCount:unassignedPhysicalCount,
			unassignedFilesCount:unassignedFilesCount]

	}

	/**
	 * Used by the dependency console to load up the individual tabs for a dependency bundle
	 * @param String entity - the entity type to view (server,database,file,app)
	 * @param Integer dependencyBundle - the dependency bundle ID 
	 * @return String HTML representing the page
	 */
	def getLists = {
		def start = new Date()
		session.removeAttribute('assetDependentlist')

		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def depGroups = JSON.parse(session.getAttribute('Dep_Groups'))
		
		def depSql = """SELECT  
		   sum(if(a.asset_type in ( ${AssetType.getPhysicalServerTypesAsString()} ), 1, 0)) as serverCount,
		   sum(if(a.asset_type in ( ${AssetType.getVirtualServerTypesAsString()} ), 1, 0)) as vmCount,
		   sum(if(a.asset_type in ( ${AssetType.getStorageTypesAsString()} ), 1, 0)) as storageCount,
		   sum(if(a.asset_type = '${AssetType.DATABASE.toString()}', 1, 0)) as dbCount,
		   sum(if(a.asset_type = '${AssetType.APPLICATION.toString()}', 1, 0)) as appCount
		from asset_dependency_bundle adb
		join asset_entity a ON a.asset_entity_id=adb.asset_id
		where adb.project_id=${projectId}
		"""

		def assetDependentlist
		def selectionQuery = ''
		def mapQuery = ''
		def nodesQuery = ''
		def multiple = false;
		if (params.dependencyBundle && params.dependencyBundle.isNumber() ) {
			// Get just the assets for a particular dependency group id
			selectionQuery = " and adb.dependency_bundle = ${params.dependencyBundle}"
			mapQuery = " AND deps.bundle = ${params.dependencyBundle}"
			nodesQuery = " AND dependency_bundle = ${params.dependencyBundle} "
		} else if (params.dependencyBundle == 'onePlus') {
			// Get all the groups other than zero - these are the groups that have interdependencies
			multiple = true;
			selectionQuery = " and adb.dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups-[0])})"
			mapQuery = " AND deps.bundle in (${WebUtil.listAsMultiValueString(depGroups-[0])})"
			nodesQuery = " AND dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups-[0])})"
		} else {
			// Get 'all' assets that were bundled
			multiple = true;
			selectionQuery = " and adb.dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups)})"
			mapQuery = " AND deps.bundle in (${WebUtil.listAsMultiValueString(depGroups)})"
			nodesQuery = " AND dependency_bundle in (${WebUtil.listAsMultiValueString(depGroups)})"
		}
		def queryFordepsList = """
			SELECT DISTINCT deps.asset_id AS assetId, ae.asset_name AS assetName, deps.dependency_bundle AS bundle, 
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
		depSql += selectionQuery
		//log.error "getLists() : query for assetDependentlist took ${TimeUtil.elapsed(start)}"
		// Took 0.296 seconds
		
		// Save the group id into the session as it is used to redirect the user back after updating assets or doing assignments
		session.setAttribute('dependencyBundle', params.dependencyBundle)
		// TODO : This pig of a list should NOT be stored into the session and the logic needs to be reworked
		//session.setAttribute('assetDependentlist', assetDependentlist)
		
		def stats = jdbcTemplate.queryForMap(depSql)
		
		def model = [entity: (params.entity ?: 'apps'), stats:stats]
		
		def sortOn = params.sort?:"assetName"
		def orderBy = params.orderBy?:"asc"
		model.dependencyBundle = params.dependencyBundle
		model.asset = params.entity
		model.orderBy = orderBy
		model.sortBy = sortOn
		
		def serverTypes = AssetType.getAllServerTypes()
		def asset

		
		// Switch on the desired entity type to be shown, and render the page for that type 
		switch(params.entity) {
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
				def assetEntityList = assetDependentlist.findAll { serverTypes.contains(it.type) }
				def assetList = []
				
				assetEntityList.each {
					asset = AssetEntity.read(it.assetId)

					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus]
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
				def filesList = assetDependentlist.findAll{it.type in [AssetType.FILES.toString(), AssetType.STORAGE.toString()] }
				def assetList = []
				def fileList = []
				
				filesList.each {
					asset = AssetEntity.read(it.assetId)

					assetList << [asset: asset, tasksStatus: it.tasksStatus, commentsStatus: it.commentsStatus]
				}
				
				assetList.each {
					def item = [id:it.asset.id, assetName:it.asset.assetName, assetType:it.asset.assetType, 
						validation:it.asset.validation, moveBundle:it.asset.moveBundle, planStatus:it.asset.planStatus, 
						depToResolve:it.asset.depToResolve, depToConflic:it.asset.depToConflict]
					
					// check if the object is a logical or physical strage
					if (it.asset.assetClass.toString().equals('DEVICE')) {
						item.fileFormat = ''
						item.storageType = 'Server'
					} else {
						item.fileFormat = Files.read(it.asset.id)?.fileFormat?:''
						item.storageType = 'Files'
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
				
				def eventColorCode = [:]
				int colorDiff
				if (moveEventList.size()) {
					colorDiff = (232/moveEventList.size()).intValue()
				}
				
				def labelMap = ['Application':userPreferenceService.getPreference('dependencyConsoleApplicationLabel')?:'true',
							    'Server':userPreferenceService.getPreference('dependencyConsoleServerLabel')?:'', 
								'Database':userPreferenceService.getPreference('dependencyConsoleDatabaseLabel')?:'', 
								'Files':userPreferenceService.getPreference('dependencyConsoleFilesLabel')?:'', 
								'Network':userPreferenceService.getPreference('dependencyConsoleNetworkLabel')?:'']
				def labelList = params.labelsList
				labelList = labelList?.replace(" ","")
				List labels = labelList ?  labelList.split(",") : []
				
				moveEventList.eachWithIndex{ event, i -> 
					def colorCode = colorDiff * i
					def colorsCode = "rgb(${colorCode},${colorCode},${colorCode})"
					eventColorCode << [(event.name):colorsCode]
				}
				
				// Create the Nodes
				def graphNodes = []
				def name = ''
				def shape = 'circle'
				def size = 150
				def title = ''
				def color = ''
				def type = ''
				def assetType = ''
				def criticalitySizes = ['Minor':150, 'Important':200, 'Major':325, 'Critical':500]
				def t1 = TimeUtil.elapsed(start).getMillis() + TimeUtil.elapsed(start).getSeconds()*1000
				log.info "t1 = ${t1}"
				log.info "Iterating through list of ${assetDependentlist.size()} items"
				
				
				assetDependentlist.each {
					assetType = it.type
					size = 150
					
					if (assetType == AssetType.APPLICATION.toString() ) {
						type = AssetType.APPLICATION.toString()
						shape = 'circle'
						size = it.criticality ? criticalitySizes[it.criticality] : 200
					} else if (serverTypes.contains(assetType)) {
						type = AssetType.SERVER.toString()
						shape = 'square'
					} else if (assetType == AssetType.DATABASE.toString() ) {
						type = AssetType.DATABASE.toString()
						shape = 'triangle-up'
					} else if (assetType in [AssetType.FILES.toString(), AssetType.STORAGE.toString()] ) {
						type = AssetType.FILES.toString()
						shape = 'diamond'
					} else if (assetType == AssetType.NETWORK.toString() ) {
						type = AssetType.NETWORK.toString()
						shape = 'cross'
					} else {
						type = ''
						shape = 'circle'
					}
					
					def moveEventName = it.eventName ?: ''
					graphNodes << [
						id:it.assetId, name:it.assetName, 
						type:type, group:it.bundle, 
						shape:shape, size:size, title:it.assetName, 
						color:eventColorCode[moveEventName]?:'red', 
						dependsOn:[], supports:[],
						assetClass:it.assetClass
					]
				}
				
				// Create a seperate list of just the node ids to use while creating the links (this makes it much faster)
				def nodeIds = []
				graphNodes.each {
					nodeIds << it.id
				}
				
				// Report the time it took to create the nodes
				def t2 = TimeUtil.elapsed(start).getMillis() + TimeUtil.elapsed(start).getSeconds()*1000
				def td = t2-t1
				log.info "Iterating through list of ${assetDependentlist.size()} items took ${td} millis, with an average of ${(td)/(assetDependentlist.size())} millis per item"
				
				// Set the defaults map to be used in the dependeny graph
				def defaults = moveBundleService.getMapDefaults(graphNodes.size())
				if ( multiple ) {
					defaults.force = -200
					defaults.linkSize = 100
				}
				
				if (params.blackBackground in ['true','false'])
					userPreferenceService.setPreference('dependencyConsoleBlackBg', params.blackBackground)
				defaults.blackBackground = false
				if ( userPreferenceService.getPreference('dependencyConsoleBlackBg') == 'true' ) {
					defaults.blackBackground = userPreferenceService.getPreference('dependencyConsoleBlackBg') == 'true'
				}
				
				// Query for only the dependencies that will be shown
				def depBundle = (params.dependencyBundle.isNumber() ? params.dependencyBundle : 0)
				def query = """ 
					SELECT * FROM ( 
						SELECT ad.asset_id AS assetId, ad.dependent_id AS dependentId, status, 
							IFNULL(adb.project_id, adb2.project_id) AS projectId, 
							IFNULL(adb.dependency_bundle, adb2.dependency_bundle) AS bundle
						FROM tdstm.asset_dependency ad 
							LEFT OUTER JOIN asset_dependency_bundle adb ON (adb.asset_id = ad.asset_id) 
							LEFT OUTER JOIN asset_dependency_bundle adb2 ON (adb2.asset_id = ad.dependent_id) 
							GROUP BY asset_dependency_id 
					) AS deps 
					WHERE deps.projectId=${projectId} ${mapQuery}"""
				def assetDependencies = jdbcTemplate.queryForList(query)
				def multiCheck = new Date()
				def dependenciesList = new ArrayList(assetDependencies)
				
				
				// Create the links
				def graphLinks = []
				def i = 0
				def opacity = 1
				def statusColor = 'grey'
				assetDependencies.each {
					if(it.status=='Questioned') {
						opacity = 1
						statusColor='red'
					} else if( ! ( it.status in ['Unknown','Validated'] ) ) {
						opacity = 0.2
						statusColor = 'grey'
					} else {
						opacity = 1
						statusColor = 'grey'
					}
					def sourceIndex = nodeIds.indexOf(it.assetId)
					def targetIndex = nodeIds.indexOf(it.dependentId)
					if(sourceIndex != -1 && targetIndex != -1){
						graphLinks << ["id":i, "source":sourceIndex,"target":targetIndex,"value":2,"statusColor":statusColor,"opacity":opacity]
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
				model.labels = labels
				model.labelMap = labelMap
				model.showControls = params.showControls
				model.appChecked = labels.contains('apps')
				model.serverChecked = labels.contains('servers')
				model.filesChecked = labels.contains('files')
				model.eventColorCode = eventColorCode
				model.nodes = graphNodes as JSON
				model.links = graphLinks as JSON
				model.multiple = multiple
				
				// Render dependency graph
				render(template:'dependencyGraph',model:model)
				break
		} // switch
		log.error "Loading dependency console took ${TimeUtil.elapsed(start)}"
	}

	/**
	* Delete multiple  Assets, Apps, Databases and files .
	* @param : assetLists[]  : list of ids for which assets are requested to deleted
	* @return : appropriate message back to view
	* 
	*/
	def deleteBulkAsset = {
		def respMap = [resp : assetEntityService.deleteBulkAssets(params.type, params.list("assetLists[]"))]
		render respMap as JSON
	}
	
	/**
	 * This action is used to get workflowTransition select for comment id
	 * @param assetCommentId : id of assetComment
     * @param format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return select or a JSON array
	 */
	def getWorkflowTransition = {
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
	def updateAssignedToSelect = {
		
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
	/**
	 * Generates an HTML SELECT control for the AssetComment.status property according to user role and current status of AssetComment(id)
	 * @param	params.id	The ID of the AssetComment to generate the SELECT for
     * @param   format - if format is equals to "json" then the methods returns a JSON array instead of a SELECT
	 * @return render HTML or a JSON array
	 */
	def updateStatusSelect = {
	
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
	def predecessorTableHtml = {
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
	def generateDepSelect = {
		
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
    def successorTableHtml = {
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
	def predecessorSelectHtml = {
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
	def exportSpecialReport = {
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def formatter = new SimpleDateFormat("yyyyMMdd")
		def today = formatter.format(new Date())
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
					spcExpSheet.addCell( new Label( c, r+1, valueForSheet ) )
				 }
			}
			book.write()
			book.close()
		}catch( Exception ex ){
			log.error "Exception occurred while exporting data"+ex.printStackTrace()
			flash.message = ex.getMessage()
			redirect( action:exportAssets)
		}
	 redirect( action:exportAssets)
	}
	
	/**
	 * Fetch Asset's modelType to use to select asset type fpr asset acording to model 
	 * @param : id - Requested model's id
	 * @return : assetType if exist for requested model else 0
	 */
    def getAssetModelType = {
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
	def populateDependency = {

		def returnMap = [:]
		def project = securityService.getUserCurrentProject()

		if (params.id && params.id.isLong()) {
			def assetEntity = AssetEntity.findByIdAndProject( params.id.toLong(), project )
			if( assetEntity ) {

				def dependentAssets = assetEntityService.getDependentAssets(assetEntity)
				def supportAssets = assetEntityService.getSupportingAssets(assetEntity)
				def dependencyType = assetEntityService.getDependencyTypes()
				def dependencyStatus = assetEntityService.getDependencyStatuses()
				def moveBundleList = assetEntityService.getMoveBundles(project)
/*	
	Removed 7/16/03 - can remove soon	
				def assetsMap = [
					(AssetType.APPLICATION.toString()): assetEntityService.getAssetsByType(AssetType.APPLICATION.toString()), 
					(AssetType.DATABASE.toString()): assetEntityService.getAssetsByType(AssetType.DATABASE.toString()),
					(AssetType.FILES.toString()): assetEntityService.getAssetsByType(AssetType.FILES.toString()), 
					(AssetType.SERVER.toString()): assetEntityService.getAssetsByType(AssetType.SERVER.toString()),
					(AssetType.NETWORK.toString()): assetEntityService.getAssetsByType(AssetType.NETWORK.toString()) ]
*/				
				// TODO - JPM 8/2014 - Why do we have this? Seems like we should NOT be passing that to the template...
				def nonNetworkTypes = [AssetType.SERVER.toString(),AssetType.APPLICATION.toString(),AssetType.VM.toString(),
					AssetType.FILES.toString(),AssetType.DATABASE.toString(),AssetType.BLADE.toString()]
				

				returnMap = [ 
					assetClassOptions: AssetClass.getClassOptions(),
//					assetsMap:assetsMap,
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
		render(template: 'dependent', model: returnMap)
	}

	/**
	 * Returns a lightweight list of assets filtered on  on the asset class
	 * @param id - class of asset to filter on (e.g. Application, Database, Server)
	 * @return JSON array of asset id, assetName
	 */
	def assetSelectDataByClass = {
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
	def getassetImportance = {
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
	def getHighlightCssMap= {
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
	def getColumnNames(entityDTAMap, columnslist, project){
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
	 * @param prefFor
	 * @param selected
	  */
	 def setImportPerferences ={
		 def key = params.prefFor
		 def selected=params.selected
		 if(selected){
			 userPreferenceService.setPreference( key, selected )
			 session.setAttribute(key,selected)
		 }
		 render true
	 }
	 /**
	  * Action to return on list Dependency
	  */
	 def listDependencies ={
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
		 
	 	return [projectId: project.id, assetDependency: new AssetDependency(),
			 	servers: entities.servers,applications: entities.applications,dbs: entities.dbs, 
				files: entities.files, networks: entities.networks, 
				dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus,
			    moveBundleList:moveBundleList, depPref:depPref,attributesList:attributes.keySet().sort{it}, columnLabelpref:columnLabelpref]
	 }
	/**
	* This method is to show list of dependencies using jqgrid.
	*/
	def listDepJson ={
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
				SELECT asset_dependency_id AS id, ae.asset_name AS assetName, ae.asset_type AS assetType, mb.name AS assetBundle, ad.type AS type, 
					aed.asset_name AS dependentName, aed.asset_type AS dependentType, mbd.name AS dependentBundle, 
					ad.status AS status,ad.comment AS comment, ad.data_flow_freq AS frequency, ae.asset_entity_id AS assetId,  
					aed.asset_entity_id AS dependentId, ad.c1 AS c1, ad.c2 AS c2, ad.c3 AS c3,ad.c4 AS c4,ad.data_flow_direction AS direction
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
				it.assetName, it.assetType, it.assetBundle, it.type,
				it.dependentName, it.dependentType, it.dependentBundle,
				(depPref['1']!='comment') ? it[depPref['1']] : (it[depPref['1']]? "<div class='commentEllip'>${it.comment}</div>" : ''), 
				(depPref['2']!='comment') ? it[depPref['2']] : (it[depPref['2']]? "<div class='commentEllip'>${it.comment}</div>" : ''),
				it.status,
				it.assetId, it.dependentId
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
	 
	def getChangedBundle = {
		def dependent = AssetDependency.read(params.dependentId)
		def depBundle = params.dependentId == "support" ? dependent?.asset?.moveBundle?.id : dependent?.dependent?.moveBundle?.id
		def resultMap = ["id": AssetEntity.read(params.assetId)?.moveBundle?.id ,"status":dependent?.status, 
			"depBundle":depBundle]
		render resultMap as JSON
	}
	
	/**
	 * Setting Blades Location and room as respective to their chassis location and rooms
	 * @param params : params map
	 * @param project: project instance
	 * @return void
	 */
	def setBladeRoomAndLoc( params, project ){
		def sourceBladeChassis = params.sourceBladeChassis ? AssetEntity.findByAssetTagAndProject( params.sourceBladeChassis, project ) : null
		def targetBladeChassis = params.targetBladeChassis ? AssetEntity.findByAssetTagAndProject( params.targetBladeChassis, project ) : null
		params.sourceLocation = sourceBladeChassis?.sourceLocation
		//params.sourceRoom = sourceBladeChassis?.sourceRoom
		//params.sourceRack = sourceBladeChassis?.sourceRack
		//params.roomSource = sourceBladeChassis?.roomSource
		//params.rackSource = sourceBladeChassis?.rackSource
		
		params.targetLocation = targetBladeChassis?.targetLocation
		//params.targetRoom = targetBladeChassis?.targetRoom
		//params.targetRack = sourceBladeChassis?.targetRack
		//params.roomTarget = sourceBladeChassis?.roomTarget
		//params.rackTarget = sourceBladeChassis?.rackTarget
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
	def getRacksPerRoom = {
		def roomInstance = Room.get(NumberUtils.toInt(params.roomId))
		def roomType= params.sourceType
		def assetEntityInstance = AssetEntity.get(NumberUtils.toInt(params.assetId))
		def racks = []
		
		if(roomInstance)
			racks = Rack.findAllByRoom(roomInstance)
			
		def rackId
		def rackName
		def clazz
		
		if( roomType== 'S' ){
			rackId = 'rackSId'
			rackName = 'rackSourceId'
			clazz = 'config.sourceRack'
		}else{
			rackId = 'rackTId'
			rackName = 'rackTargetId'
			clazz = 'config.targetRack'
		}
		render(template:'rackView',	model:[racks:racks, rackId:rackId, rackName:rackName, roomType:roomType, clazz:clazz, assetEntity:assetEntityInstance,forWhom:params.forWhom])
	}

	def getAssetsByType = {
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
	 * 
	 */
	def poiDemo ={
		return 
	}
	
	def exportPoiDemo ={
		def filePath = "/templates/TDSMaster_Poi_template.xls" // Template file Path
		def formatter = new SimpleDateFormat("yyyyMMdd")
		def today = formatter.format(new Date())
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
	def assetListForSelect2 = {
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
				'SERVER-DEVICE': 	[ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.getServerTypes() ],
				'DATABASE': 		[ assetClass: AssetClass.DATABASE, domain: Database ],
				'NETWORK-DEVICE': 	[ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.getNetworkDeviceTypes() ],
				// 'NETWORK-LOGICAL': 	[],
				'STORAGE-DEVICE': 	[ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.getStorageTypes() ],
				'STORAGE-LOGICAL': 	[ assetClass: AssetClass.STORAGE, domain: Files ],
				'OTHER-DEVICE': 	[ assetClass: AssetClass.DEVICE, domain: AssetEntity, assetType: AssetType.getNonOtherTypes(), notIn: true ]
			]


			String queryColumns = 'a.id as id, a.assetName as text'
			String queryCount = 'COUNT(a)'

			StringBuffer query = new StringBuffer("SELECT @COLS@ FROM ")

			if (qmap.containsKey(params.assetClassOption)) {
				def qm=qmap[params.assetClassOption]
				def assetClass = qm.assetClass
				def qparams = [ project:project, assetClass:qm.assetClass ]

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

				query.append('WHERE a.project=:project AND a.assetClass=:assetClass ')

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

				if (log.isDebugEnabled())
					log.debug "***** Query: ${query.toString()}\nParams: $qparams}"

				// Perform query and move data into normal map
				def cquery = query.toString().replace('@COLS@', queryCount)
				if (log.isDebugEnabled())
					log.debug "***** Count Query: $cquery"
				
				total = qm.domain.executeQuery(cquery, qparams)[0]

				if (total > 0) {
					def rquery = query.toString().replace('@COLS@', queryColumns) 
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
}
