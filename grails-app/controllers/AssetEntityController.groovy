import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import jxl.*
import jxl.read.biff.*
import jxl.write.*
import net.tds.util.jmesa.AssetCommentBean;
import net.tds.util.jmesa.AssetEntityBean

import java.io.File;
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit
import org.jsecurity.SecurityUtils
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*

import com.tds.asset.Application
import com.tds.asset.ApplicationAssetMap
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetDependency
import com.tds.asset.AssetDependencyBundle
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.CommentNote
import com.tds.asset.AssetOptions
import com.tds.asset.AssetTransition
import com.tds.asset.Database
import com.tds.asset.Files
import com.tds.asset.TaskDependency
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.AssetCommentStatus
import groovy.time.TimeCategory

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

	protected static customLabels = ['Custom1','Custom2','Custom3','Custom4','Custom5','Custom6','Custom7','Custom8']
	protected static bundleMoveAndClientTeams = ['sourceTeamMt','sourceTeamLog','sourceTeamSa','sourceTeamDba','targetTeamMt','targetTeamLog','targetTeamSa','targetTeamDba']
	protected static targetTeamType = ['MOVE_TECH':'targetTeamMt', 'CLEANER':'targetTeamLog','SYS_ADMIN':'targetTeamSa',"DB_ADMIN":'targetTeamDba']
	protected static sourceTeamType = ['MOVE_TECH':'sourceTeamMt', 'CLEANER':'sourceTeamLog','SYS_ADMIN':'sourceTeamSa',"DB_ADMIN":'sourceTeamDba']
	protected static teamsByType = ["MOVE":"'MOVE_TECH','CLEANER'","ADMIN":"'SYS_ADMIN','DB_ADMIN'"]
	
	protected static statusOptionForRole = [
		"PROJ_MGR": [
			'*EMPTY*': AssetCommentStatus.getList(),
			(AssetCommentStatus.PLANNED): AssetCommentStatus.getList(),
			(AssetCommentStatus.PENDING): AssetCommentStatus.getList(),
			(AssetCommentStatus.READY): AssetCommentStatus.getList(),
			(AssetCommentStatus.STARTED): AssetCommentStatus.getList(),
			(AssetCommentStatus.HOLD): AssetCommentStatus.getList(),
			(AssetCommentStatus.DONE): AssetCommentStatus.getList()
		],
		"USER":[
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
		redirect( action:list, params:params )
	}
	/* -----------------------------------------------------
	 * To Filter the Data on AssetEntityList Page 
	 * @author Bhuvana
	 * @param  Selected Filter Values
	 * @return Will return filters data to AssetEntity  
	 * ------------------------------------------------------ */
	def filter = {
		if(params.rowVal){
			if(!params.max) params.max = params.rowVal
			userPreferenceService.setPreference( "MAX_ASSET_LIST", "${params.rowVal}" )
		}else{
			def userMax = getSession().getAttribute("MAX_ASSET_LIST")
			if( userMax.MAX_ASSET_LIST ) {
				if( !params.max ) params.max = userMax.MAX_ASSET_LIST
			} else {
				if( !params.max ) params.max = 50
			}
		}
		def project = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )

		params['project.id'] = project.id

		def assetEntityList = filterService.filter( params, AssetEntity )
		assetEntityList.each{
			if( it.project.id == project.id ) {
				assetEntityInstanceList<<it
			}
		}
		try{
			render( view:'list', model:[ assetEntityInstanceList: assetEntityInstanceList,
						assetEntityCount: filterService.count( params, AssetEntity ),
						filterParams: com.zeddware.grails.plugins.filterpane.FilterUtils.extractFilterParams(params),
						params:params, projectId:project.id,maxVal : params.max ] )
		} catch(Exception ex){
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
		if( projectId != null ) {
			projectInstance = Project.findById( projectId )
			moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
		}
		def dataTransferSetImport = DataTransferSet.findAll(" from DataTransferSet dts where dts.transferMode IN ('B','I') ")
		def dataTransferSetExport = DataTransferSet.findAll(" from DataTransferSet dts where dts.transferMode IN ('B','E') ")
		if( projectId == null ) {
			//get project id from session
			def currProj = getSession().getAttribute( "CURR_PROJ" )
			projectId = currProj.CURR_PROJ
			projectInstance = Project.findById( projectId )
			moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
			if( projectId == null ) {
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
		if( params.message ) {
			flash.message = params.message
		}
		render( view:"importExport", model : [ assetsByProject: assetsByProject,
					projectId: projectId,
					moveBundleInstanceList: moveBundleInstanceList,
					dataTransferSetImport: dataTransferSetImport,
					dataTransferSetExport: dataTransferSetExport,
					dataTransferBatchs: dataTransferBatchs ] )
	}
	/* -----------------------------------------------------
	 * To Export the assets
	 * @author Mallikarjun 
	 * render export form
	 *------------------------------------------------------*/
	def assetExport = {
		render( view:"assetExport" )
	}
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
		dataTransferSetExport = DataTransferSet.findAll(" from DataTransferSet dts where dts.transferMode IN ('B','E') ")
		if( projectId == null ) {
			//get project id from session
			def currProj = getSession().getAttribute( "CURR_PROJ" )
			projectId = currProj.CURR_PROJ
			projectInstance = Project.findById( projectId )
			moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
			if( projectId == null ) {
				flash.message = " No Projects are Associated, Please select Project. "
				redirect( controller:"project",action:"list" )
			}
		}
		if ( projectId != null ) {
			project = Project.findById(projectId)
		}
		def	dataTransferBatchs = DataTransferBatch.findAllByProject(project).size()
		render (view:"exportAssets",model : [projectId: projectId,
						dataTransferBatchs:dataTransferBatchs,
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
		def projectId
		def project
		def dataTransferSet = params.dataTransferSet
		def dataTransferSetInstance = DataTransferSet.findById( dataTransferSet )
		def serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance, "Servers" )
		def appDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance, "Applications" )
		def databaseDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance, "Databases" )
		def filesDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance, "Files" )
		try {
			projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
			if ( projectId == null || projectId == "" ) {
				flash.message = "Project Name is required"
				redirect( controller:"asset", action:"assetImport" )
				return;
			}
			project = Project.findById( projectId )
		}catch ( Exception ex ) {
			flash.message = " Project Name is required. "
			redirect( controller:"asset", action:"assetImport" )
			return;
		}
		def projectCustomLabels = new HashMap()
		for(int i = 1; i< 9; i++){
			if (project["custom"+i]) projectCustomLabels.put(project["custom"+i], "Custom"+i)
		}
		// get File
		MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
		CommonsMultipartFile file = ( CommonsMultipartFile ) mpr.getFile("file")
		// create workbook
		def workbook
		def titleSheet
		def sheetNameMap = ['Title','Servers','Applications','Databases','Files','Dependencies']
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
		int dependencyUpdated = 0
		//get column name and sheets
		serverDTAMap.eachWithIndex { item, pos ->
			if(customLabels.contains( item.columnName )){
				def customLabel = project[item.eavAttribute?.attributeCode] ? project[item.eavAttribute?.attributeCode] : item.columnName
				serverColumnslist.add( customLabel )
			} else {
				serverColumnslist.add( item.columnName )
			}
		}
		def appColumnslist = appDTAMap.columnName
		def databaseColumnslist = databaseDTAMap.columnName
		def filesColumnslist = filesDTAMap.columnName
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
			titleSheet = workbook.getSheet( "Title" )
			if( titleSheet != null) {
				SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
				try {
					exportTime = format.parse( (titleSheet.getCell( 1,5 ).contents).toString() )
				}catch ( Exception e) {
					flash.message = " Export Date Time Not Found, Please check it."
					redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
					return;
				}

			} else {
				flag = 0
			}
			if( flag == 0 ) {
				flash.message = " Sheet not found, Please check it."
				redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
				return;
			} else {
				def serverSheet = workbook.getSheet( "Servers" )
				def appSheet = workbook.getSheet( "Applications" )
				def databaseSheet = workbook.getSheet( "Databases" )
				def filesSheet = workbook.getSheet( "Files" )
				def dependencySheet = workbook.getSheet( "Dependencies" )
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
				if ( !checkHeader( serverColumnslist, serverColumnNames ) ) {
					def missingHeader = missingHeader.replaceFirst(",","")
					flash.message = " Server Column Headers : ${missingHeader} not found, Please check it."
					redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
					return;
				} else if ( !checkHeader( appColumnslist, appColumnNames ) ) {
					def missingHeader = missingHeader.replaceFirst(",","")
					flash.message = " Applciations Column Headers : ${missingHeader} not found, Please check it."
					redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
					return;
				} else if ( !checkHeader( databaseColumnslist, databaseColumnNames ) ) {
					def missingHeader = missingHeader.replaceFirst(",","")
					flash.message = " Databases Column Headers : ${missingHeader} not found, Please check it."
					redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
					return;
				} else if ( !checkHeader( filesColumnslist, filesColumnNames ) ) {
					def missingHeader = missingHeader.replaceFirst(",","")
					flash.message = " Files Column Headers : ${missingHeader} not found, Please check it."
					redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
					return;
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
					//Add Data to dataTransferBatch.
					def serverColNo = 0
					for (int index = 0; index < serverCol; index++) {
						if(serverSheet.getCell( index, 0 ).contents == "Server"){
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
					session.setAttribute("TOTAL_ASSETS",(assetsCount+filesCount+databaseCount+appCount+dependencyCount))
					if(params.asset == 'asset'){
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
												flash.message = "AssetId should be Integer"
												redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
												return;
											}
										}
										def dataTransferValues = "("+assetId+",'"+serverSheet.getCell( cols, r ).contents.replace("'","\\'")+"',"+r+","+serverDataTransferBatch.id+","+dataTransferAttributeMapInstance.eavAttribute.id+")"
										dataTransferValueList.append(dataTransferValues)
										dataTransferValueList.append(",")
									}
								}
								try{
									jdbcTemplate.update("insert into data_transfer_value( asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id ) values "+dataTransferValueList.toString().substring(0,dataTransferValueList.lastIndexOf(",")))
									serverAdded = r
								} catch (Exception e) {
									skipped += ( r +1 )
								}
							}
							if (r%50 == 0){
								sessionFactory.getCurrentSession().flush();
								sessionFactory.getCurrentSession().clear();
							}
						}
					}
					//  Process applciation
					if(params.application=='application'){
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
							if(name){
								def dataTransferValueList = new StringBuffer()
								for( int cols = 0; cols < appCol; cols++ ) {
									def dataTransferAttributeMapInstance = appDTAMap.find{it.columnName == appSheet.getCell( cols, 0 ).contents}

									if( dataTransferAttributeMapInstance != null ) {
										def assetId
										if( appColumnNames.containsKey("appId") && (appSheet.getCell( 0, r ).contents != "") ) {
											try{
												assetId = Integer.parseInt(appSheet.getCell( 0, r ).contents)
											} catch( NumberFormatException ex ) {
												flash.message = "AppId should be Integer"
												redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
												return;
											}
										}
										def dataTransferValues = "("+assetId+",'"+appSheet.getCell( cols, r ).contents.replace("'","\\'")+"',"+r+","+appDataTransferBatch.id+","+dataTransferAttributeMapInstance.eavAttribute.id+")"
										dataTransferValueList.append(dataTransferValues)
										dataTransferValueList.append(",")
									}
								}
								try{
									jdbcTemplate.update("insert into data_transfer_value( asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id ) values "+dataTransferValueList.toString().substring(0,dataTransferValueList.lastIndexOf(",")))
									appAdded = r
								} catch (Exception e) {
									skipped += ( r +1 )
								}
							}
							if (r%50 == 0){
								sessionFactory.getCurrentSession().flush();
								sessionFactory.getCurrentSession().clear();
							}
						}
					}
					//  Process database
					if(params.database=='database'){
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
									def dataTransferAttributeMapInstance = databaseDTAMap.find{it.columnName == databaseSheet.getCell( cols, 0 ).contents}

									if( dataTransferAttributeMapInstance != null ) {
										def assetId
										if( databaseColumnNames.containsKey("dbId") && (databaseSheet.getCell( 0, r ).contents != "") ) {
											try{
												assetId = Integer.parseInt(databaseSheet.getCell( 0, r ).contents)
											} catch( NumberFormatException ex ) {
												flash.message = "DBId should be Integer"
												redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
												return;
											}
										}
										def dataTransferValues = "("+assetId+",'"+databaseSheet.getCell( cols, r ).contents.replace("'","\\'")+"',"+r+","+dbDataTransferBatch.id+","+dataTransferAttributeMapInstance.eavAttribute.id+")"
										dataTransferValueList.append(dataTransferValues)
										dataTransferValueList.append(",")
									}
								}
								try{
									jdbcTemplate.update("insert into data_transfer_value( asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id ) values "+dataTransferValueList.toString().substring(0,dataTransferValueList.lastIndexOf(",")))
									dbAdded = r
								} catch (Exception e) {
									skipped += ( r +1 )
								}
							}
							if (r%50 == 0){
								sessionFactory.getCurrentSession().flush();
								sessionFactory.getCurrentSession().clear();
							}
						}
					}
					//  Process files
					if(params.files=='files'){
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
									def dataTransferAttributeMapInstance = filesDTAMap.find{it.columnName == filesSheet.getCell( cols, 0 ).contents}

									if( dataTransferAttributeMapInstance != null ) {
										def assetId
										if( filesColumnNames.containsKey("filesId") && (filesSheet.getCell( 0, r ).contents != "") ) {
											try{
												assetId = Integer.parseInt(filesSheet.getCell( 0, r ).contents)
											} catch( NumberFormatException ex ) {
												flash.message = "filesId should be Integer"
												redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
												return;
											}
										}
										String dataTransferValues = "("+assetId+",'"+filesSheet.getCell( cols, r ).contents.replace("'","\\'")+"',"+r+","+fileDataTransferBatch.id+","+dataTransferAttributeMapInstance.eavAttribute.id+")"
										dataTransferValueList.append(dataTransferValues)
										dataTransferValueList.append(",")
									}
								}
								try{
									jdbcTemplate.update("insert into data_transfer_value( asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id ) values "+dataTransferValueList.toString().substring(0,dataTransferValueList.lastIndexOf(",")))
									filesAdded = r
								} catch (Exception e) {
									skipped += ( r +1 )
								}
							}
							if (r%50 == 0){
								sessionFactory.getCurrentSession().flush();
								sessionFactory.getCurrentSession().clear();
							}
						}
					}
					if(params.dependency=='dependency'){
						session.setAttribute("TOTAL_ASSETS",dependencyCount)
						def currentProjectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
						def projectInstance = Project.get(currentProjectId)
						def subjects = SecurityUtils.subject
						def principals = subject.principal
						def userLogins = UserLogin.findByUsername( principals )
						def skippedUpdated =0
						def skippedAdded=0
						for ( int r = 1; r < dependencySheetRow ; r++ ) {
							int cols = 0 ;
							def name = dependencySheet.getCell( cols, r ).contents
							def dependencyTransferValueList = new StringBuffer()
							cols=0
							if(dependencySheet.getCell( cols, r ).contents.replace("'","\\'")){
								int id = Integer.parseInt(dependencySheet.getCell( cols, r ).contents.replace("'","\\'"))
								AssetDependency asset =  AssetDependency.get(id)
								if(asset){
									def assetId = AssetEntity.get(Integer.parseInt(dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")))
									def dependentId = AssetEntity.get(Integer.parseInt(dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")))
									if(assetId.project.id==projectInstance.id && dependentId.project.id==projectInstance.id ){
										asset.asset = assetId
										asset.dependent = dependentId
										asset.type = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
										asset.dataFlowFreq = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
										asset.dataFlowDirection = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
										asset.status = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
										asset.comment = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
										asset.updatedBy = userLogins.person
										//asset.createdBy = userLogin.person
										if(!asset.save(flush:true)){
											asset.errors.allErrors.each { log.error  it }
											skipped += ( r +1 )
											skippedUpdated = skipped.size()
										}
									}else{
											skipped += ( r +1 )
											skippedUpdated = skipped.size()
									}
								}
							}else{
								AssetDependency dependency = new AssetDependency()
								def assetId = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
								def dependentId = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
								def assetInstance = AssetEntity.get(Integer.parseInt(assetId))
								def dependentInstance = AssetEntity.get(Integer.parseInt(dependentId))
								if(assetInstance.project.id==projectInstance.id && dependentInstance.project.id == projectInstance.id){
									if(assetId){
										dependency.asset = assetInstance
									}
									if(dependentId){
										dependency.dependent = dependentInstance
									}
									dependency.type = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
									dependency.dataFlowFreq = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
									dependency.dataFlowDirection = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
									dependency.status = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
									dependency.comment = dependencySheet.getCell( ++cols, r ).contents.replace("'","\\'")
									dependency.createdBy = userLogins?.person
									dependency.updatedBy = userLogins?.person
									if(!dependency.save(flush:true)){
										dependency.errors.allErrors.each { log.error it }
										skipped += ( r +1 )
										skippedAdded = skipped.size()
									}
								}else{
								      skipped += ( r +1 )
								      skippedAdded = skipped.size()
								}

							}
							dependencyAdded = (r-(skippedAdded+skippedUpdated))
							if (r%50 == 0){
								sessionFactory.getCurrentSession().flush();
								sessionFactory.getCurrentSession().clear();
							}

						}

					}
					for( int i=0;  i < sheetNamesLength; i++ ) {
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
					}

				} // generate error message
				workbook.close()
				added = serverAdded + appAdded   + dbAdded + filesAdded + dependencyAdded
				if (skipped.size() > 0) {
					flash.message = " File Uploaded Successfully with ${added} records. and  ${skipped} Records skipped Please click the Manage Batches to review and post these changes."
				} else {
					flash.message = " File uploaded successfully with ${added} records.  Please click the Manage Batches to review and post these changes."
				}
				redirect( action:assetImport, params:[ message:flash.message] )
				return;
			}
		} catch( NumberFormatException ex ) {
			ex.printStackTrace()
			flash.message = ex
			redirect( action:assetImport, params:[ message:flash.message] )
			return;
		} catch( Exception ex ) {
			ex.printStackTrace()
			flash.message = grailsApplication.metadata[ 'app.file.format' ]+ex
			redirect( action:assetImport, params:[ message:flash.message] )
			return;
		}
	}
	/*------------------------------------------------------------
	 * download data form Asset Entity table into Excel file
	 * @author Mallikarjun
	 * @param Datatransferset,Project,Movebundle
	 *------------------------------------------------------------*/
	def export = {
		//get project Id
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def dataTransferSet = params.dataTransferSet
		def bundle = request.getParameterValues( "bundle" )
		def bundleList = new StringBuffer()
		def bundleNameList = new StringBuffer()
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		def bundleSize = bundle.size()
		for ( int i=0; i< bundleSize ; i++ ) {
			if( bundle[i] == "" ) {
				bundleNameList.append("ALL")
			} else if( i != bundleSize - 1) {
				bundleNameList.append( MoveBundle.findById( bundle[i] ) )
				bundleNameList.append( "," )
				bundleList.append( bundle[i] + "," )
			} else {
				bundleList.append( bundle[i] )
				bundleNameList.append( MoveBundle.findById( bundle[i] )?.name )
			}
		}
		def dataTransferSetInstance = DataTransferSet.findById( dataTransferSet )
		def serverDTAMap = DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Servers" )
		def appDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Applications" )
		def dbDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Databases" )
		def fileDTAMap =  DataTransferAttributeMap.findAllByDataTransferSetAndSheetName( dataTransferSetInstance,"Files" )

		def project = Project.findById( projectId )
		if ( projectId == null || projectId == "" ) {
			flash.message = " Project Name is required. "
			redirect( action:assetImport, params:[message:flash.message] )
			return;
		}
		def asset
		def application
		def database
		def files
		def assetEntityInstance
		if(bundle[0] == "" ) {
			asset = AssetEntity.findAllByProjectAndAssetTypeNotInList( project,["Application","Database","Files"], params )
			application =Application.findAllByProject( project )
			database =Database.findAllByProject( project )
			files =Files.findAllByProject( project )
		} else {
			asset = AssetEntity.findAll( "from AssetEntity m where m.project = project and m.assetType not in('Application','Database','Files') and m.moveBundle in ( $bundleList ) " )
			application = Application.findAll( "from Application m where m.project = project and m.moveBundle in ( $bundleList )" )
			database = Database.findAll( "from Database m where m.project = project and m.moveBundle in ( $bundleList )")
			files = Files.findAll( "from Files m where m.project = project and m.moveBundle in ( $bundleList )" )
		}
		//get template Excel
		def workbook
		def book
		try {
			def assetDepBundleList = AssetDependencyBundle.findAllByProject(project)
			def filenametoSet = dataTransferSetInstance.templateFilename
			File file =  ApplicationHolder.application.parentContext.getResource(filenametoSet).getFile()
			// Going to use temporary file because we were getting out of memory errors constantly on staging server
			WorkbookSettings wbSetting = new WorkbookSettings()
			wbSetting.setUseTemporaryFileDuringWrite(true)
			workbook = Workbook.getWorkbook( file, wbSetting )
			//set MIME TYPE as Excel
			def exportType = filenametoSet.split("/")[2]
			exportType = exportType.substring(0,exportType.indexOf("_template.xls"))
			def filename = project?.name?.replace(" ","_")+"-"+bundleNameList.toString().replace(" ","_")
			response.setContentType( "application/vnd.ms-excel" )
			response.setHeader( "Content-Disposition", "attachment; filename=\""+exportType+'-'+filename+".xls\"" )
			//create workbook and sheet
			book = Workbook.createWorkbook( response.getOutputStream(), workbook )
			def serverSheet
			def appSheet
			def dbSheet
			def fileSheet
			def titleSheet
			def dependencySheet

			def serverMap = [:]
			def serverSheetColumnNames = [:]
			def serverColumnNameList = new ArrayList()
			def serverSheetNameMap = [:]
			def serverDataTransferAttributeMapSheetName

			def appMap = [:]
			def appSheetColumnNames = [:]
			def appColumnNameList = new ArrayList()
			def appSheetNameMap = [:]
			def appDataTransferAttributeMapSheetName

			def dbMap = [:]
			def dbSheetColumnNames = [:]
			def dbColumnNameList = new ArrayList()
			def dbSheetNameMap = [:]
			def dbDataTransferAttributeMapSheetName

			def fileMap = [:]
			def fileSheetColumnNames = [:]
			def fileColumnNameList = new ArrayList()
			def fileSheetNameMap = [:]
			def fileDataTransferAttributeMapSheetName


			serverDTAMap.eachWithIndex { item, pos ->
				serverMap.put( item.columnName, null )
				serverColumnNameList.add(item.columnName)
				serverSheetNameMap.put( "sheetName", (item.sheetName).trim() )
			}
			serverMap.put("DepGroup", null )
			serverColumnNameList.add("DepGroup")
			
			appDTAMap.eachWithIndex { item, pos ->
				appMap.put( item.columnName, null )
				appColumnNameList.add(item.columnName)
				appSheetNameMap.put( "sheetName", (item.sheetName).trim() )
			}
			appMap.put("DepGroup", null )
			appColumnNameList.add("DepGroup")
			
			dbDTAMap.eachWithIndex { item, pos ->
				dbMap.put( item.columnName, null )
				dbColumnNameList.add(item.columnName)
				dbSheetNameMap.put( "sheetName", (item.sheetName).trim() )
			}
			dbMap.put("DepGroup", null )
			dbColumnNameList.add("DepGroup")
			
			fileDTAMap.eachWithIndex { item, pos ->
				fileMap.put( item.columnName, null )
				fileColumnNameList.add(item.columnName)
				fileSheetNameMap.put( "sheetName", (item.sheetName).trim() )
			}
			fileMap.put("DepGroup", null )
			fileColumnNameList.add("DepGroup")
			
			def sheetNames = book.getSheetNames()
			def flag = 0
			def sheetNamesLength = sheetNames.length
			for( int i=0;  i < sheetNamesLength; i++ ) {
				if ( serverSheetNameMap.containsValue( sheetNames[i].trim()) ) {
					flag = 1

				}
			}
			serverSheet = book.getSheet( sheetNames[1] )
			appSheet = book.getSheet( sheetNames[2] )
			dbSheet = book.getSheet( sheetNames[3] )
			fileSheet = book.getSheet( sheetNames[4] )
			dependencySheet = book.getSheet( sheetNames[5] )

			if( flag == 0 ) {
				flash.message = " Sheet not found, Please check it."
				redirect( action:assetImport, params:[message:flash.message] )
				return;
			} else {
				def serverCol = serverSheet.getColumns()
				for ( int c = 0; c < serverCol; c++ ) {
					def serverCellContent = serverSheet.getCell( c, 0 ).contents
					serverSheetColumnNames.put(serverCellContent, c)
					if( serverMap.containsKey( serverCellContent ) ) {
						serverMap.put( serverCellContent, c )
					}
				}
				def appCol = appSheet.getColumns()
				for ( int c = 0; c < appCol; c++ ) {
					def appCellContent = appSheet.getCell( c, 0 ).contents
					appSheetColumnNames.put(appCellContent, c)
					if( appMap.containsKey( appCellContent ) ) {
						appMap.put( appCellContent, c )

					}
				}
				def dbCol = dbSheet.getColumns()
				for ( int c = 0; c < dbCol; c++ ) {
					def dbCellContent = dbSheet.getCell( c, 0 ).contents
					dbSheetColumnNames.put(dbCellContent, c)
					if( dbMap.containsKey( dbCellContent ) ) {
						dbMap.put( dbCellContent, c )
					}
				}
				def filesCol = fileSheet.getColumns()
				for ( int c = 0; c < filesCol; c++ ) {
					def fileCellContent = fileSheet.getCell( c, 0 ).contents
					fileSheetColumnNames.put(fileCellContent, c)
					if( fileMap.containsKey( fileCellContent ) ) {
						fileMap.put( fileCellContent, c )
					}
				}
				//calling method to check for Header
				def serverCheckCol = checkHeader( serverColumnNameList, serverSheetColumnNames )
				def appCheckCol = checkHeader( appColumnNameList, appSheetColumnNames )
				def dbCheckCol = checkHeader( dbColumnNameList, dbSheetColumnNames )
				def filesCheckCol = checkHeader( fileColumnNameList, fileSheetColumnNames )
				// Statement to check Headers if header are not found it will return Error message
				if ( serverCheckCol == false || appCheckCol == false || dbCheckCol == false || filesCheckCol == false) {
					missingHeader = missingHeader.replaceFirst(",","")
					flash.message = " Column Headers : ${missingHeader} not found, Please check it."
					redirect( action:assetImport, params:[message:flash.message] )
					return;
				} else {
					//Add Title Information to master SpreadSheet
					titleSheet = book.getSheet("Title")
					SimpleDateFormat format = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a");
					def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
					def currDate = GormUtil.convertInToUserTZ(GormUtil.convertInToGMT( "now", "EDT" ),tzId)
					if(titleSheet != null) {
						def titleInfoMap = new ArrayList();
						titleInfoMap.add (project.client )
						titleInfoMap.add( projectId )
						titleInfoMap.add( project.name )
						titleInfoMap.add( partyRelationshipService.getProjectManagers(projectId) )
						titleInfoMap.add( format.format( currDate ) )
						titleInfoMap.add( loginUser.person )
						titleInfoMap.add( bundleNameList )
						partyRelationshipService.exportTitleInfo(titleInfoMap,titleSheet)
						titleSheet.addCell(new Label(0,30,"Note: All times are in ${tzId ? tzId : 'EDT'} time zone"))
					}
					//update data from Asset Entity table to EXCEL
					def assetSize = asset.size()
					def appSize = application.size()
					def dbSize = database.size()
					def fileSize = files.size()
					def serverColumnNameListSize = serverColumnNameList.size()
					def appcolumnNameListSize = appColumnNameList.size()
					def dbcolumnNameListSize = dbColumnNameList.size()
					def filecolumnNameListSize = fileColumnNameList.size()
					// update column header
					for ( int head =0; head <= serverSheetColumnNames.size(); head++ ) {
						def cellData = serverSheet.getCell(head,0)?.getContents()
						def attributeMap = serverDTAMap.find{it.columnName ==  cellData }?.eavAttribute
						if(attributeMap?.attributeCode && customLabels.contains( cellData )){
							def columnLabel = project[attributeMap?.attributeCode] ? project[attributeMap?.attributeCode] : cellData
							def customColumn = new Label(head,0, columnLabel )
							serverSheet.addCell(customColumn)
						}
					}
					if(params.asset=='asset'){
						for ( int r = 1; r <= assetSize; r++ ) {
							//Add assetId for walkthrough template only.
							if( serverSheetColumnNames.containsKey("assetId") ) {
								def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
								def addAssetId = new Number(0, r, (asset[r-1].id))
								serverSheet.addCell( addAssetId )
							}
							
							for ( int coll = 0; coll < serverColumnNameListSize; coll++ ) {
								def addContentToSheet
								def attribute = serverDTAMap.eavAttribute.attributeCode[coll]
								def colName = serverColumnNameList.get(coll)
								if(colName == "DepGroup"){
									def depGroup = assetDepBundleList.find{it.asset.id==asset[r-1].id}?.dependencyBundle?.toString()
									addContentToSheet = new Label(serverMap[colName], r, depGroup?: "" )
								} else if ( attribute != "usize" && asset[r-1][attribute] == null ) {
									addContentToSheet = new Label( serverMap[colName], r, "" )
								} else if(attribute == "usize"){
									addContentToSheet = new Label(serverMap[colName], r, asset[r-1]?.model?.usize?.toString() ?:"" )
								} else {
									//if attributeCode is sourceTeamMt or targetTeamMt export the teamCode
									if( bundleMoveAndClientTeams.contains(serverDTAMap.eavAttribute.attributeCode[coll]) ) {
										addContentToSheet = new Label( serverMap[colName], r, String.valueOf(asset[r-1].(serverDTAMap.eavAttribute.attributeCode[coll]).teamCode) )
									}else {
										addContentToSheet = new Label( serverMap[colName], r, String.valueOf(asset[r-1].(serverDTAMap.eavAttribute.attributeCode[coll])) )
									}
								}
								serverSheet.addCell( addContentToSheet )
							}
						}
					}
					if(params.application=='application'){
						for ( int r = 1; r <= appSize; r++ ) {
							//Add assetId for walkthrough template only.
							if( appSheetColumnNames.containsKey("appId") ) {
								def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
								def addAppId = new Number(0, r, (application[r-1].id))
								appSheet.addCell( addAppId )
							}
							for ( int coll = 0; coll < appcolumnNameListSize; coll++ ) {
								def addContentToSheet
								def attribute = appDTAMap.eavAttribute.attributeCode[coll]
								//if attributeCode is sourceTeamMt or targetTeamMt export the teamCode
								def colName = appColumnNameList.get(coll)
								addContentToSheet = new Label( appMap[colName], r, "" )
								if(colName == "DepGroup"){
									def depGroup = assetDepBundleList.find{it.asset.id==application[r-1].id}?.dependencyBundle?.toString()
									addContentToSheet = new Label(appMap[colName], r, depGroup?:"" )
								} else if ( application[r-1][attribute] == null ) {
									addContentToSheet = new Label( appMap[colName], r, "" )
								}else {
									if( bundleMoveAndClientTeams.contains(appDTAMap.eavAttribute.attributeCode[coll]) ) {
										addContentToSheet = new Label( appMap[colName], r, String.valueOf(application[r-1].(appDTAMap.eavAttribute.attributeCode[coll]).teamCode) )
									}else {
										addContentToSheet = new Label( appMap[colName], r, String.valueOf(application[r-1].(appDTAMap.eavAttribute.attributeCode[coll])) )
									}
								}
								appSheet.addCell( addContentToSheet )
							}
							
						}
					}
					if(params.database=='database'){
						for ( int r = 1; r <= dbSize; r++ ) {
							//Add assetId for walkthrough template only.
							if( dbSheetColumnNames.containsKey("dbId") ) {
								def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
								def addDBId = new Number(0, r, (database[r-1].id))
								dbSheet.addCell( addDBId )
							}
							for ( int coll = 0; coll < dbcolumnNameListSize; coll++ ) {
								def addContentToSheet
								def attribute = dbDTAMap.eavAttribute.attributeCode[coll]
								//if attributeCode is sourceTeamMt or targetTeamMt export the teamCode
								def colName = dbColumnNameList.get(coll)
								if(colName == "DepGroup"){
									def depGroup = assetDepBundleList.find{it.asset.id==database[r-1].id}?.dependencyBundle?.toString()
									addContentToSheet = new Label(dbMap[colName], r, depGroup ?:"" )
								} else if ( database[r-1][attribute] == null ) {
									addContentToSheet = new Label(  dbMap[colName], r, "" )
								}else {
									if( bundleMoveAndClientTeams.contains(dbDTAMap.eavAttribute.attributeCode[coll]) ) {
										addContentToSheet = new Label( dbMap[colName], r, String.valueOf(database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll]).teamCode) )
									}else {
										addContentToSheet = new Label( dbMap[colName], r, String.valueOf(database[r-1].(dbDTAMap.eavAttribute.attributeCode[coll])) )
									}
								}
								dbSheet.addCell( addContentToSheet )
							}
						}
					}
					if(params.files=='files'){
						for ( int r = 1; r <= fileSize; r++ ) {
							//Add assetId for walkthrough template only.
							if( fileSheetColumnNames.containsKey("filesId") ) {
								def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
								def addFileId = new Number(0, r, (files[r-1].id))
								fileSheet.addCell( addFileId )
							}
							for ( int coll = 0; coll < filecolumnNameListSize; coll++ ) {
								def addContentToSheet
								def attribute = fileDTAMap.eavAttribute.attributeCode[coll]
								def colName = fileColumnNameList.get(coll)
								if(colName == "DepGroup"){
									def depGroup = assetDepBundleList.find{it.asset.id==files[r-1].id}?.dependencyBundle?.toString()
									addContentToSheet = new Label(fileMap[colName], r, depGroup ?:"" )
								} else if ( files[r-1][attribute] == null ) {
									addContentToSheet = new Label( fileMap[colName], r, "" )
								} else {
									//if attributeCode is sourceTeamMt or targetTeamMt export the teamCode
									if( bundleMoveAndClientTeams.contains(fileDTAMap.eavAttribute.attributeCode[coll]) ) {
										addContentToSheet = new Label( fileMap[colName], r, String.valueOf(files[r-1].(fileDTAMap.eavAttribute.attributeCode[coll]).teamCode) )
									}else {
										addContentToSheet = new Label( fileMap[colName], r, String.valueOf(files[r-1].(fileDTAMap.eavAttribute.attributeCode[coll])) )
									}
								}
								fileSheet.addCell( addContentToSheet )
							}
						}
					}

					if(params.dependency=='dependency'){
						def assetDependent = AssetDependency.findAll("from AssetDependency where asset.project = ? ",[project])
						def dependencyMap = ['AssetId':1,'DependentId':2, 'Type':3, 'DataFlowFreq':4, 'DataFlowDirection':5, 'status':6, 'comment':7]
						def dependencyColumnNameList = ['AssetId','DependentId', 'Type', 'DataFlowFreq', 'DataFlowDirection', 'status', 'comment']
						def DTAMap = [0:'asset',1:'dependent', 2:'type', 3:'dataFlowFreq', 4:'dataFlowDirection', 5:'status', 6:'comment']
						def dependentSize = assetDependent.size()
						for ( int r = 1; r <= dependentSize; r++ ) {
							//Add assetId for walkthrough template only.
							def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
							def addAssetDependentId = new Number(0, r, (assetDependent[r-1].id))
							dependencySheet.addCell( addAssetDependentId )

							for ( int coll = 0; coll < 7; coll++ ) {
								def addContentToSheet
								if ( assetDependent[r-1].(DTAMap[coll]) == null ) {
									addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, "" )
								}else {
									if(DTAMap[coll]=="dependent"){
										addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, String.valueOf(assetDependent[r-1].(DTAMap[coll]).id) )
									}else if(DTAMap[coll]=="asset"){
										addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, String.valueOf(assetDependent[r-1].(DTAMap[coll]).id) )
									}else{
										addContentToSheet = new Label( dependencyMap[dependencyColumnNameList.get(coll)], r, String.valueOf(assetDependent[r-1].(DTAMap[coll])) )
									}
								}
								dependencySheet.addCell( addContentToSheet )
							}
						}
					}
				}
				//update data from Asset Comment table to EXCEL
				for( int sl=0;  sl < sheetNamesLength; sl++ ) {
					def commentIt = new ArrayList()
					if(sheetNames[sl] == "Comments"){
						def commentSheet = book.getSheet("Comments")
						asset.each{
							commentIt.add(it.id)
						}
						def commentList = new StringBuffer()
						def commentSize = commentIt.size()
						for ( int k=0; k< commentSize ; k++ ) {
							if( k != commentSize - 1) {
								commentList.append( commentIt[k] + "," )
							} else {
								commentList.append( commentIt[k] )
							}
						}
						def assetcomment = AssetComment.findAll("from AssetComment cmt where cmt.assetEntity in ($commentList)")
						def assetId
						def commentType
						def comment
						def commentId
						def assetName
						for(int cr=1 ; cr<=assetcomment.size() ; cr++){
							assetId = new Label(0,cr,String.valueOf(assetcomment[cr-1].assetEntity.id))
							commentSheet.addCell(assetId)
							commentId = new Label(1,cr,String.valueOf(assetcomment[cr-1].id))
							commentSheet.addCell(commentId)
							assetName = new Label(2,cr,String.valueOf(assetcomment[cr-1].assetEntity.assetName))
							commentSheet.addCell(assetName)
							commentType = new Label(3,cr,String.valueOf(assetcomment[cr-1].commentType))
							commentSheet.addCell(commentType)
							comment = new Label(4,cr,String.valueOf(assetcomment[cr-1].comment))
							commentSheet.addCell(comment)
						}
					}
				}
				book.write()
				book.close()
				render( view: "importExport" )
			}
		} catch( Exception fileEx ) {

			flash.message = "Exception occurred wile exporting Excel. "
			fileEx.printStackTrace();
			redirect( action:assetImport, params:[ message:flash.message] )
			return;
		}
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
		def filterAttributes = [tag_f_assetName:params.tag_f_assetName,tag_f_model:params.tag_f_model,tag_f_sourceLocation:params.tag_f_sourceLocation,tag_f_sourceRack:params.tag_f_sourceRack,tag_f_targetLocation:params.tag_f_targetLocation,tag_f_targetRack:params.tag_f_targetRack,tag_f_assetType:params.tag_f_assetType,tag_f_assetType:params.tag_f_assetType,tag_f_serialNumber:params.tag_f_serialNumber,tag_f_moveBundle:params.tag_f_moveBundle,tag_f_depUp:params.tag_f_depUp,tag_f_depDown:params.tag_f_depDown,tag_s_1_application:params.tag_s_1_application,tag_s_2_assetName:params.tag_s_2_assetName,tag_s_3_model:params.tag_s_3_model,tag_s_4_sourceLocation:params.tag_s_4_sourceLocation,tag_s_5_sourceRack:params.tag_s_5_sourceRack,tag_s_6_targetLocation:params.tag_s_6_targetLocation,tag_s_7_targetRack:params.tag_s_7_targetRack,tag_s_8_assetType:params.tag_s_8_assetType,tag_s_9_assetTag:params.tag_s_9_assetTag,tag_s_10_serialNumber:params.tag_s_10_serialNumber,tag_s_11_moveBundle:params.tag_s_11_moveBundle,tag_s_12_depUp:params.tag_s_12_depUp,tag_s_13_depDown:params.tag_s_13_depDown]
		session.setAttribute('filterAttributes', filterAttributes)
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.findById( projectId )
		def moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
		String moveBundle = moveBundleList.id
		moveBundle = moveBundle.replace("[","('").replace(",","','").replace("]","')")
		String bundle
		def bundledAsset
		if(params.moveEvent && params.moveEvent!='unAssigned'){
			def moveEvent = MoveEvent.get(params.moveEvent)
			def moveBundles = moveEvent.moveBundles
		    def  bundles = moveBundles.findAll {it.useOfPlanning == true}.id
			bundledAsset = moveBundles.findAll {it.useOfPlanning == true}
			String filterdBundle = bundles
			bundle = filterdBundle.replace("[","('").replace(",","','").replace("]","')")
		}
		def assetEntityInstanceList

		// TODO - Some of the else if test params.moveEvent while others don't which doesn't seem correct. 
		if(params.moveEvent=='unAssigned' && params.filter=='All'){
			
			assetEntityInstanceList= AssetEntity.findAll("from AssetEntity where project = $projectId and assetType in ('Server','VM','Blade') and (planStatus is null or planStatus in ('Unassigned','')) and moveBundle in $moveBundle ")
			
		}else if(params.moveEvent && params.moveEvent!='unAssigned' && params.filter=='All'){
		
			assetEntityInstanceList= AssetEntity.findAll("from AssetEntity where project = $projectId and assetType in ('Server','VM','Blade')  and moveBundle in $bundle ")
			
		}else if(params.moveEvent=='unAssigned' && params.filter=='physical'){
		
			assetEntityInstanceList= AssetEntity.findAll("from AssetEntity where project = $projectId and assetType in ('Server','Blade') and (planStatus is null or planStatus in ('Unassigned','')) and moveBundle in $moveBundle ")
			
		}else if(params.moveEvent && params.moveEvent!='unAssigned' && params.filter=='physical'){
		
			assetEntityInstanceList= AssetEntity.findAll("from AssetEntity where project = $projectId and assetType in ('Server','Blade')  and moveBundle in $bundle ")
			
		}else if(params.moveEvent=='unAssigned' && params.filter=='virtual'){
		
			assetEntityInstanceList= AssetEntity.findAll("from AssetEntity where project = $projectId and assetType in ('VM') and (planStatus is null or planStatus in ('Unassigned','')) and moveBundle in $moveBundle ")
			
		}else if(params.moveEvent && params.moveEvent!='unAssigned' && params.filter=='virtual'){
		
			assetEntityInstanceList= AssetEntity.findAll("from AssetEntity where project = $projectId and assetType in ('VM')  and moveBundle in $bundle ")
			
		}else if(params.moveEvent=='unAssigned' && params.filter=='other'){
		
		    assetEntityInstanceList =AssetEntity.findAll("from AssetEntity where project = $projectId and assetType not in ('Server','VM','Blade','Application','Database','Files') and (planStatus is null or planStatus in ('Unassigned','')) and moveBundle in $moveBundle ")
		
		}else if(params.moveEvent && params.moveEvent!='unAssigned' && params.filter=='other'){
		
		    assetEntityInstanceList =AssetEntity.findAllByMoveBundleInListAndAssetTypeNotInList(bundledAsset,['Server','VM','Blade','Application','Files','Database'])
		 
		}else if(params.filter=='physicalServer'){
		
		    assetEntityInstanceList = AssetEntity.findAllByMoveBundleInListAndAssetTypeInList(moveBundleList,['Server','Blade'])
			
		}else if(params.filter=='virtual'){
		
		    assetEntityInstanceList = AssetEntity.findAllByMoveBundleInListAndAssetTypeInList(moveBundleList,['VM'])
			
		} else{
		
		    assetEntityInstanceList = AssetEntity.findAllByProjectAndAssetTypeNotInList( project,["Application","Database","Files"], params )
		}
		def assetEntityList =  new ArrayList()
		assetEntityInstanceList.each { assetEntity->
			AssetEntityBean assetBean = new AssetEntityBean();
			assetBean.setId(assetEntity.id)
			assetBean.setAssetName(assetEntity.assetName)
			assetBean.setAssetType(assetEntity.assetType)
			assetBean.setAssetTag(assetEntity.assetTag)
			assetBean.setModel(assetEntity.model?.modelName)
			assetBean.setSourceLocation(assetEntity.sourceLocation)
			assetBean.setSourceRack(assetEntity.rackSource?.tag)
			assetBean.setTargetLocation(assetEntity.targetLocation)
			assetBean.setTargetRack(assetEntity.rackTarget?.tag)
			assetBean.setMoveBundle(assetEntity.moveBundle?.name)
			assetBean.setSerialNumber(assetEntity.serialNumber)
			assetBean.setPlanStatus(assetEntity.planStatus)
			assetBean.setDepUp(AssetDependency.countByDependentAndStatusNotEqual(assetEntity, "Validated"))
			assetBean.setDepDown(AssetDependency.countByAssetAndStatusNotEqual(assetEntity, "Validated"))
			assetBean.setDependencyBundleNumber(AssetDependencyBundle.findByAsset(assetEntity)?.dependencyBundle)

			if(AssetComment.find("from AssetComment where assetEntity = ${assetEntity?.id} and commentType = ? and isResolved = ?",['issue',0])){
				assetBean.setCommentType("issue")
			} else if(AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id)){
				assetBean.setCommentType("comment")
			} else {
				assetBean.setCommentType("blank")
			}

			assetEntityList.add(assetBean)
		}
		def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$projectId order by assetName asc")
		def applications = Application.findAll('from Application where assetType = ? and project =? order by assetName asc',['Application', project])
		def dbs = Database.findAll('from Database where assetType = ? and project =? order by assetName asc',['Database', project])
		def files = Files.findAll('from Files where assetType = ? and project =? order by assetName asc',['Files', project])
		
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)

		try{
			TableFacade tableFacade = new TableFacadeImpl("tag",request)
			tableFacade.items = assetEntityList
			Limit limit = tableFacade.limit
			if(limit.isExported()){
				tableFacade.setExportTypes(response,limit.getExportType())
				tableFacade.setColumnProperties("id","application","assetName","shortName","serialNumber","assetTag","manufacturer","model","assetType","ipAddress","os","sourceLocation","sourceRoom","sourceRack","sourceRackPosition","sourceBladeChassis","sourceBladePosition","targetLocation","targetRoom","targetRack","targetRackPosition","targetBladeChassis","targetBladePosition","custom1","custom2","custom3","custom4","custom5","custom6","custom7","custom8","moveBundle","sourceTeamMt","targetTeamMt","sourceTeamLog","targetTeamLog","sourceTeamSa","targetTeamSa","sourceTeamDba","targetTeamDba","truck","cart","shelf","railType","appOwner","appSme","priority")
				tableFacade.render()
			}else
				return [assetEntityList : assetEntityList,projectId: projectId, servers : servers,
					applications : applications, dbs : dbs, files : files, assetDependency: new AssetDependency(), dependencyType:dependencyType, dependencyStatus:dependencyStatus,
					staffRoles:taskService.getRolesForStaff() ]
		} catch(Exception ex ){
			return [assetEntityInstanceList : null,projectId: projectId, servers : servers,
				applications : applications, dbs : dbs, files : files, assetDependency: new AssetDependency()]
		}

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
			ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.asset = ${assetEntityInstance.id}")
			AssetTransition.executeUpdate("delete from AssetTransition ast where ast.assetEntity = ${assetEntityInstance.id}")
			ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset = ${assetEntityInstance.id}")
			AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar aev where aev.assetEntity = ${assetEntityInstance.id}")
			ProjectTeam.executeUpdate("update ProjectTeam pt set pt.latestAsset = null where pt.latestAsset = ${assetEntityInstance.id}")
			AssetCableMap.executeUpdate("delete AssetCableMap where fromAsset = ? ",[assetEntityInstance])
			AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
											toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
											where toAsset = ?""",[assetEntityInstance])
			AssetDependency.executeUpdate("delete AssetDependency where asset = ? or dependent = ? ",[assetEntityInstance, assetEntityInstance])
			AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset = ${assetEntityInstance.id}")
            
			flash.message = "AssetEntity ${assetEntityInstance.assetName} deleted"
			assetEntityInstance.delete()
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
				case "console":
					redirect( action:dashboardView, params:[ showAll:'show'])
					break;
				case "dashboardView":
					redirect( action:dashboardView, params:[ showAll:'show'])
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
				case "planningConsole":
					forward( action:'getLists', params:[entity: 'server',dependencyBundle:session.getAttribute("dependencyBundle")])
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
		} else if ( params.moveBundleId ){
			redirect( action:dashboardView, params:[ moveBundle : params.moveBundleId, showAll : params.showAll] )
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
		def formatter = new SimpleDateFormat("MM/dd/yyyy")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def maintExpDate = params.maintExpDate
		def redirectTo = params?.redirectTo
		session.setAttribute("USE_FILTERS", "true")
		if(maintExpDate){
			params.maintExpDate =  GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
		}
		def retireDate = params.retireDate
		if(retireDate){
			params.retireDate =  GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
		}
		if(redirectTo.contains("room_")){
			def newRedirectTo = redirectTo.split("_")
			redirectTo = newRedirectTo[0]
			def rackId = newRedirectTo[1]
			session.setAttribute("RACK_ID", rackId)
		}

		def bundleId = getSession().getAttribute( "CURR_BUNDLE" )?.CURR_BUNDLE
		def assetEntityInstance = new AssetEntity(params)
		def projectId =  getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def projectInstance = Project.findById( projectId )
		assetEntityInstance.project = projectInstance
		assetEntityInstance.owner = projectInstance.client
		if(!params.assetTag){
			def lastAssetId = projectInstance.lastAssetId
			if(!lastAssetId){
				lastAssetId = jdbcTemplate.queryForInt("select max(asset_entity_id) FROM asset_entity WHERE project_id = ${projectInstance.id}")
			}
			while(AssetEntity.findByAssetTagAndProject("TDS-${lastAssetId}",projectInstance)){
				lastAssetId = lastAssetId+1
			}
			assetEntityInstance.assetTag = "TDS-${lastAssetId}"
			projectInstance.lastAssetId = lastAssetId + 1
			if(!projectInstance.save(flush:true)){
				log.error "Error while updating project.lastAssetId : ${projectInstance}"
				projectInstance.errors.each { log.error  it }
			}
		}

		if(!assetEntityInstance.hasErrors() && assetEntityInstance.save()) {
			assetEntityInstance.updateRacks()

			if(assetEntityInstance.model){
				assetEntityAttributeLoaderService.createModelConnectors( assetEntityInstance )
			}
			assetEntityService.createOrUpdateAssetEntityDependencies(params, assetEntityInstance)
			flash.message = "AssetEntity ${assetEntityInstance.assetName} created"
			if(redirectTo == "room"){
				redirect( controller:'room',action:list )
			} else if(redirectTo == "rack"){
				redirect( controller:'rackLayouts',action:'create' )
			} else {
				redirect( action:list )
			}
		}
		else {

			flash.message = "AssetEntity ${assetEntityInstance.assetName} not created"
			def etext = "Unable to Update Asset" +
					GormUtil.allErrorsString( assetEntityInstance )
			log.error  etext
			if(params.redirectTo == "room"){
				redirect( controller:'room',action:list )
			} else if(params.redirectTo == "rack"){
				redirect( controller:'rackLayouts',action:'create' )
			} else {
				redirect( action:list, params:[projectId: projectId] )
			}
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
						AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
														toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
														where toAsset = ? """,[assetEntityInstance])

						AssetCableMap.executeUpdate("delete from AssetCableMap where fromAsset = ?",[assetEntityInstance])
						assetEntityAttributeLoaderService.createModelConnectors( assetEntityInstance )
					}
					if(existingTargetRack != assetEntityInstance.targetRack || existingUposition != assetEntityInstance.targetRackPosition){

						AssetCableMap.executeUpdate("""Update AssetCableMap set toAssetRack='${assetEntityInstance.targetRack}',
	            				toAssetUposition=${assetEntityInstance.targetRackPosition} where toAsset = ? """,[assetEntityInstance])
					}
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
		def assetCommentsInstance = AssetComment.findAllByAssetEntity( assetEntityInstance )
		def assetCommentsList = []
		def today = new Date()
		def css //= 'white'
		assetCommentsInstance.each {
			css = it.dueDate < today ? 'Lightpink' : 'White'
			assetCommentsList <<[ commentInstance : it, assetEntityId : it.assetEntity.id,cssClass:css,assetName: it.assetEntity.assetName]
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
		def etStart
		def etFinish
		def atStart
		def dueDate
		def estformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def dateFormatter = new SimpleDateFormat("MM/dd/yyyy ");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetComment = AssetComment.get(params.id)
		if(assetComment){
			if(assetComment.createdBy){
				personCreateObj = Person.find("from Person p where p.id = $assetComment.createdBy.id")
				dtCreated = estformatter.format(TimeUtil.convertInToUserTZ(assetComment.dateCreated, tzId));
			}
			if(assetComment.resolvedBy){
				personResolvedObj = Person.find("from Person p where p.id = $assetComment.resolvedBy.id")
				dtResolved = estformatter.format(TimeUtil.convertInToUserTZ(assetComment.dateResolved, tzId));
			}
			if(assetComment.estStart){
				etStart = estformatter.format(TimeUtil.convertInToUserTZ(assetComment.estStart, tzId));
			}
			if(assetComment.estFinish){
				etFinish = estformatter.format(TimeUtil.convertInToUserTZ(assetComment.estFinish, tzId));
			}
			if(assetComment.actStart){
				atStart = estformatter.format(TimeUtil.convertInToUserTZ(assetComment.actStart, tzId));
			}
			if(assetComment.dueDate){
				dueDate = dateFormatter.format(assetComment.dueDate);
			}
	
			def workflowTransition = assetComment?.workflowTransition
			def workflow = workflowTransition?.name
			
			def noteList = assetComment.notes.sort{it.dateCreated}
			def notes = []
			noteList.each {
				def dateCreated = it.dateCreated.format("E, d MMM 'at ' HH:mma")
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
					def taskDependencies = assetComment.taskDependencies.sort{ it.predecessor.taskNumber }
			//taskDependencies.sort{ it.predecessor.taskNumber }
			if (taskDependencies.size() > 0) {
				predecessorTable = new StringBuffer('<table cellspacing="0" style="border:0px;"><tbody>')
				taskDependencies.each() { taskDep ->
					def task = taskDep.predecessor
					def css = taskService.getCssClassForStatus(task.status)
					def taskDesc = task.comment?.length()>50 ? task.comment.substring(0,50): task.comment
					predecessorTable.append("""<tr class="${css}" style="cursor:pointer;" onClick="showAssetComment(${task.id}, 'show')"><td>${task.category}</td><td>${task.taskNumber ? task.taskNumber+':' :''}${taskDesc}</td></tr>""")
			    }
				predecessorTable.append('</tbody></table>')
			}
			def taskSuccessors = TaskDependency.findAllByPredecessor( assetComment ).sort{ it.assetComment.taskNumber }
			def successorTable = ""
			if (taskSuccessors.size() > 0) {
				successorTable = new StringBuffer('<table  cellspacing="0" style="border:0px;" ><tbody>')
				taskSuccessors.each() { successor ->
					def task = successor.assetComment
					def css = taskService.getCssClassForStatus(task.status)
					successorTable.append("""<tr class="${css}" style="cursor:pointer;" onClick="showAssetComment(${task.id}, 'show')"><td>${task.category}</td><td>${task}</td>""")
				}
				successorTable.append("""</tbody></table>""")
			
			}
			def cssForCommentStatus = taskService.getCssClassForStatus(assetComment.status)
		 
		// TODO : Security : Should reduce the person objects (create,resolved,assignedTo) to JUST the necessary properties using a closure
			commentList << [ 
				assetComment:assetComment, personCreateObj:personCreateObj, personResolvedObj:personResolvedObj, dtCreated:dtCreated ?: "",
				dtResolved:dtResolved ?: "", assignedTo:assetComment.assignedTo?:'', assetName:assetComment.assetEntity?.assetName ?: "",
				eventName:assetComment.moveEvent?.name ?: "", dueDate:dueDate?:'', etStart:etStart?:'', etFinish:etFinish?:'',atStart:atStart,notes:notes,
				workflow:workflow,roles:roles, predecessorTable:predecessorTable, successorTable:successorTable,maxVal:maxVal,
				cssForCommentStatus:cssForCommentStatus, statusWarn:taskService.canChangeStatus ( assetComment ) ? 0 : 1]
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
		render map as JSON
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
		} else {
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
	/*---------------------------------------------------------------------------------------
	 *	User to get the details for Supervisor Console
	 * 	@author:	Lokanath Reddy
	 * 	@param :	CURR_PROJ and movebundle
	 * 	@return:	AssetEntity details and Transition details for all MoveBundle Teams
	 *--------------------------------------------------------------------------------------*/
	def dashboardView = {
		
		def userLogin = securityService.getUserLogin()
		def projectInstance = securityService.getUserCurrentProject()
		def projectId = projectInstance.id
		params.projectId = projectId

		//
		// Deal with which moveBundle that the user should be viewing
		//
		def bundleId = params.moveBundle
		def moveBundleInstance
		if (bundleId) {
			// Okay, so the user selected one from the view.  Let's make sure that they aren't hacking around and trying to 
			// get to a bundle outside their current project
			moveBundleInstance = MoveBundle.findByIdAndProject(bundleId, projectInstance)
			if (moveBundleInstance) {
				userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
			} else {
				log.error "dashboardView: Bundle(${bundleId}) not associated with user's(${loginUser}) current project(${project.id})"
				flash.message = "The bundle specified was unrelated to your current project"
				bundleId = null
			}
		}
		if ( ! bundleId ) {
			// No bundle was specified so let's see if they have a preference or get one from the user's project
			userPreferenceService.loadPreferences("CURR_BUNDLE")
			def defaultBundle = getSession().getAttribute("CURR_BUNDLE")
			if(defaultBundle?.CURR_BUNDLE){
				moveBundleInstance = MoveBundle.findById(defaultBundle.CURR_BUNDLE)
				def mbProject = moveBundleInstance?.project
				// log.info "dashboardView: mbProject=${mbProject?.id}, projectId=${projectId}"
				if( mbProject?.id != projectId ){
//				if( mbProject?.id != Integer.parseInt(projectId) ){
					moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
				}
			} else {
				moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
			}
		}
		
		// Get list of all moveBundles
		def moveBundleInstanceList = MoveBundle.findAll("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
		
		def filterAttr = [tag_f_priority:params.tag_f_priority,tag_f_assetTag:params.tag_f_assetTag,tag_f_assetName:params.tag_f_assetName,tag_f_status:params.tag_f_status,tag_f_sourceTeamMt:params.tag_f_sourceTeamMt,tag_f_targetTeamMt:params.tag_f_targetTeamMt,tag_f_commentType:params.tag_f_commentType,tag_s_1_priority:params.tag_s_1_priority,tag_s_2_assetTag:params.tag_s_2_assetTag,tag_s_3_assetName:params.tag_s_3_assetName,tag_s_4_status:params.tag_s_4_status,tag_s_5_sourceTeamMt:params.tag_s_5_sourceTeamMt,tag_s_6_targetTeamMt:params.tag_s_6_targetTeamMt,tag_s_7_commentType:params.tag_s_7_commentType]
		session.setAttribute('filterAttr', filterAttr)
		def showAll = params.showAll
		def currentState = params.currentState
		def assetList
		def bundleTeams = []
		def assetsList = []
		def totalAsset = []
		def totalAssetsSize = 0
		def supportTeam = new HashMap()
		
		// Set the teamType to params or user's preferences or default to 'MOVE'
		def teamType = params.teamType ?: ( getSession().getAttribute( "CONSOLE_TEAM_TYPE" )?.CONSOLE_TEAM_TYPE ?: "MOVE")

		userPreferenceService.setPreference( "CONSOLE_TEAM_TYPE", "${teamType}" )
		def stateVal
		def taskVal
		/* user role check*/
		def role = ""
		def subject = SecurityUtils.subject
		if(subject.hasRole("ADMIN") || subject.hasRole("SUPERVISOR")){
			role = "SUPERVISOR"
		} else if(subject.hasRole("MANAGER")){
			role = "MANAGER"
		}
		
		// get the list of assets order by Hold and recent asset Transition
		if( moveBundleInstance != null ){
			//  Get Id for respective States
			stateEngineService.loadWorkflowTransitionsIntoMap(moveBundleInstance.workflowCode, 'project')
			def holdId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, "Hold" ) )
			def releasedId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, "Release" ) )

			def unrackedId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, "Unracked" ) )

			def cleanedId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, "Cleaned" ) )
			def onCartId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, "OnCart" ) )
			def stagedId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, "Staged" ) )

			def rerackedId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, "Reracked" ) )

			def onTruckId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, "OnTruck" ) )
			def offTruckId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, "OffTruck" ) )

			def queryHold = supervisorConsoleService.getQueryForConsole( moveBundleInstance, params, 'hold')
			def queryNotHold = supervisorConsoleService.getQueryForConsole(moveBundleInstance,params, 'notHold')
			def holdTotalAsset = jdbcTemplate.queryForList( queryHold )
			def otherTotalAsset = jdbcTemplate.queryForList( queryNotHold )
			def today = GormUtil.convertInToGMT("now", "EDT" )

			if(!currentState && !params.assetStatus || params.assetStatus?.contains("pend")){
				holdTotalAsset.each{
					totalAsset<<it
				}
			}
			if( showAll ){
				otherTotalAsset.each{
					totalAsset<<it
				}
			}

			totalAssetsSize = moveBundleService.assetCount( moveBundleInstance.id )
			def projectTeamList = ProjectTeam.findAll("from ProjectTeam pt where pt.moveBundle = ${moveBundleInstance.id} and pt.role in (${teamsByType.get(teamType)}) order by pt.role, pt.name asc")

			def bundleAssetsList = AssetEntity.findAllWhere( moveBundle : moveBundleInstance )

			projectTeamList.each{ projectTeam->
				def swimlane = Swimlane.findByNameAndWorkflow(projectTeam.role ? projectTeam.role : "MOVE_TECH", Workflow.findByProcess(moveBundleInstance.workflowCode) )

				def minSource = swimlane.minSource ? swimlane.minSource : "Release"
				def minSourceId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, minSource ) )

				def minTarget = swimlane.minTarget ? swimlane.minTarget : "Staged"
				def minTargetId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, minTarget ) )


				def maxSource = swimlane.maxSource ? swimlane.maxSource : "Unracked"
				def maxSourceId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, maxSource ) )

				def maxTarget = swimlane.maxTarget ? swimlane.maxTarget : "Reracked"
				def maxTargetId = Integer.parseInt( stateEngineService.getStateId( moveBundleInstance.workflowCode, maxTarget ) )

				def teamId = projectTeam.id
				def teamRole = projectTeam.role
				def teamMembers = partyRelationshipService.getBundleTeamMembersDashboard(projectTeam.id)
				def member
				if(teamMembers.length() > 0){
					member = teamMembers.delete((teamMembers.length()-1), teamMembers.length())
				}

				def sourceAssetsList = bundleAssetsList.findAll{it[sourceTeamType.get(teamRole)]?.id == teamId }

				def sourceAssets = sourceAssetsList.size()

				def sourcePendAssets = sourceAssetsList.findAll{it.currentStatus < minSourceId || !it.currentStatus }.size()

				def sourceProcessAssets = sourceAssetsList.findAll{it.currentStatus > minSourceId && it.currentStatus < maxSourceId }.size()

				def maxSourceAssets = sourceAssetsList.findAll{it.currentStatus >= maxSourceId }.size()

				def sourceAvailassets = sourceAssetsList.findAll{it.currentStatus >= minSourceId && it.currentStatus < maxSourceId }.size()

				if(projectTeam?.role == "CLEANER"){

					sourceAssets = bundleAssetsList.size()

					sourcePendAssets = bundleAssetsList.findAll{ it.currentStatus < maxSourceId || !it.currentStatus }.size()

					sourceProcessAssets = bundleAssetsList.findAll{ it.currentStatus == maxSourceId }.size()

					maxSourceAssets = bundleAssetsList.findAll{ it.currentStatus >= cleanedId }.size()

					sourceAvailassets = bundleAssetsList.findAll{ it.currentStatus == maxSourceId }.size()

				}
				def targetAssetsList = bundleAssetsList.findAll{it[targetTeamType.get(teamRole)]?.id == teamId }
				def targetAssets = targetAssetsList.size()

				def targetPendAssets = targetAssetsList.findAll{it.currentStatus < minTargetId || !it.currentStatus }.size()

				def targetProcessAssets = targetAssetsList.findAll{it.currentStatus > minTargetId && it.currentStatus < maxTargetId }.size()

				def maxTargetAssets = targetAssetsList.findAll{it.currentStatus >= maxTargetId }.size()

				def targetAvailAssets = targetAssetsList.findAll{it.currentStatus >= minTargetId && it.currentStatus < maxTargetId }.size()

				def latestAssetCreated = AssetTransition.findAll("FROM AssetTransition a where a.assetEntity = ? and a.projectTeam = ? Order By a.id desc",[projectTeam.latestAsset, projectTeam],[max:1])
				def elapsedTime = "00:00m"
				if(latestAssetCreated.size() > 0){
					elapsedTime = convertIntegerIntoTime(today.getTime() - latestAssetCreated[0].dateCreated.getTime() )?.toString()
					elapsedTime = elapsedTime?.substring(0,elapsedTime.lastIndexOf(":")) + "m"
				}

				def headColor = 'done'
				if(projectTeam.currentLocation != "Target"){
					if(sourceProcessAssets > 0 && sourceAssets > 0){
						headColor = 'process'
					} else if(sourceAvailassets > 0){
						headColor = 'ready'
					} else if(sourceAssets != maxSourceAssets && sourceAssets > 0){
						headColor = 'pending'
					}
				} else {
					if(targetProcessAssets > 0 && targetAssets > 0){
						headColor = 'process'
					} else if(targetAvailAssets > 0){
						headColor = 'ready'
					} else if(targetAssets != maxTargetAssets && targetAssets > 0){
						headColor = 'pending'
					}
				}
				bundleTeams <<[team:projectTeam,members:member, sourceAssets:sourceAssets,
							maxSourceAssets:maxSourceAssets, sourceAvailassets:sourceAvailassets ,
							targetAvailAssets:targetAvailAssets , targetAssets:targetAssets,
							maxTargetAssets:maxTargetAssets, sourcePendAssets:sourcePendAssets, headColor:headColor,
							targetPendAssets:targetPendAssets, elapsedTime:elapsedTime, eventActive : projectTeam.moveBundle.moveEvent?.inProgress  ]
			}

			/*
			 * @@@@ Cleaning team included into other teams
			 * 
			 * 	def sourcePendCleaned = bundleAssetsList.findAll{ it.currentStatus < unrackedId || !it.currentStatus }.size()
			 def sourceAvailCleaned = bundleAssetsList.findAll{ it.currentStatus == unrackedId }.size()
			 def sourceCleaned = bundleAssetsList.findAll{ it.currentStatus >= cleanedId }.size()
			 */
			def sourceMover = bundleAssetsList.findAll{ it.currentStatus >= onCartId }.size()

			def sourceTransportAvail = bundleAssetsList.findAll{ it.currentStatus == cleanedId }.size()

			def sourceTransportPend = bundleAssetsList.findAll{ it.currentStatus < cleanedId || !it.currentStatus }.size()

			def targetMover = bundleAssetsList.findAll{ it.currentStatus >= stagedId }.size()

			def targetTransportAvail = bundleAssetsList.findAll{ it.currentStatus >= onTruckId && it.currentStatus < offTruckId }.size()

			def targetTransportPend = bundleAssetsList.findAll{ it.currentStatus < onTruckId || !it.currentStatus }.size()

			def cleaningTeam = ProjectTeam.findByTeamCodeAndMoveBundle("Logistics", moveBundleInstance)
			def transportTeam = ProjectTeam.findByTeamCodeAndMoveBundle("Transport", moveBundleInstance)
			def cleaningMembers
			if ( cleaningTeam ) {
				cleaningMembers = partyRelationshipService.getBundleTeamMembersDashboard(cleaningTeam.id)
			}
			def transportMembers
			if ( transportTeam ) {
				transportMembers = partyRelationshipService.getBundleTeamMembersDashboard(transportTeam.id)
			}
			supportTeam.put("sourceTransportAvail", sourceTransportAvail )
			supportTeam.put("sourceTransportPend", sourceTransportPend )
			supportTeam.put("targetTransportAvail", targetTransportAvail )
			supportTeam.put("targetTransportPend", targetTransportPend )
			supportTeam.put("totalAssets", totalAssetsSize )
			supportTeam.put("sourceMover", sourceMover )
			supportTeam.put("targetMover", targetMover )
			supportTeam.put("cleaning", cleaningTeam )
			supportTeam.put("cleaningMembers", cleaningMembers ? cleaningMembers?.delete((cleaningMembers?.length()-1), cleaningMembers?.length()) : "" )
			supportTeam.put("transport", transportTeam )
			supportTeam.put("transportMembers", transportMembers ? transportMembers?.delete((transportMembers?.length()-1), transportMembers?.length()) : "" )
			totalAsset.each{
				// log.info "dashboardView: it=${it}"
				def check = true
				def curId = it.currentState

				stateVal = curId ? stateEngineService.getState( moveBundleInstance.workflowCode, curId ) : null
				if (stateVal){
					taskVal = stateEngineService.getTasks( moveBundleInstance.workflowCode, "SUPERVISOR", stateVal )
					if(taskVal.size() == 0){
						check = false
					}
				}
				def cssClass
				if(it.minstate == holdId ){
					def holdAssetTransition = AssetTransition.findAll("FROM AssetTransition t WHERE t.assetEntity = ${it.id} AND t.stateTo = '${holdId}' AND t.voided = 0")
					cssClass = 'asset_hold'
					if(holdAssetTransition.size() > 0){
						def holdTimer = holdAssetTransition[0]?.holdTimer
						cssClass = (holdTimer && holdTimer.getTime() < today.getTime()) ? 'asset_hold_overtime' : 'asset_hold'
					}
				} else if(curId < releasedId && curId != holdId ){
					cssClass = 'asset_pending'
				} else if(curId > rerackedId){
					cssClass = 'asset_done'
				}
				def status = curId ? stateEngineService.getStateLabel( moveBundleInstance.workflowCode, curId ) : ''
				assetsList<<[asset: it, status:status, cssClass : cssClass, checkVal:check]
			}
			def totalSourcePending = bundleAssetsList.findAll{ it.currentStatus < releasedId || !it.currentStatus }.size()

			def totalUnracked = bundleAssetsList.findAll{ it.currentStatus >= unrackedId }.size()

			def totalSourceAvail = bundleAssetsList.findAll{ it.currentStatus >= releasedId && it.currentStatus < unrackedId }.size()

			def totalTargetPending = bundleAssetsList.findAll{ it.currentStatus < stagedId || !it.currentStatus }.size()

			def totalReracked = bundleAssetsList.findAll{ it.currentStatus >= rerackedId }.size()

			def totalTargetAvail = bundleAssetsList.findAll{ it.currentStatus >= stagedId && it.currentStatus < rerackedId}.size()

			def totalAssetsOnHold = jdbcTemplate.queryForInt("SELECT count(a.asset_entity_id) FROM asset_entity a left join asset_transition t on "+
					"(a.asset_entity_id = t.asset_entity_id and t.voided = 0)  where "+
					"a.move_bundle_id = ${moveBundleInstance.id} and t.state_to = $holdId")
			userPreferenceService.loadPreferences("SUPER_CONSOLE_REFRESH")
			def timeToUpdate = getSession().getAttribute("SUPER_CONSOLE_REFRESH")
			/*Get data for filter dropdowns*/
			def applicationList=AssetEntity.executeQuery("select distinct ae.application , count(ae.id) from AssetEntity "+
					"ae where  ae.moveBundle=${moveBundleInstance.id} "+
					"group by ae.application order by ae.application")
			def appOwnerList=AssetEntity.executeQuery("select distinct ae.appOwner, count(ae.id) from AssetEntity ae where "+
					"ae.moveBundle=${moveBundleInstance.id} group by ae.appOwner order by ae.appOwner")
			def appSmeList=AssetEntity.executeQuery("select distinct ae.appSme, count(ae.id) from AssetEntity ae where "+
					" ae.moveBundle=${moveBundleInstance.id} group by ae.appSme order by ae.appSme")
			/* Get list of Transitions states*/
			def transitionStates = []
			def processTransitions = stateEngineService.getTasks(moveBundleInstance.workflowCode, "TASK_ID")
			processTransitions.each{
				def stateId = Integer.parseInt( it )
				transitionStates << [state:stateEngineService.getState( moveBundleInstance.workflowCode, stateId ),
							stateLabel:stateEngineService.getStateLabel( moveBundleInstance.workflowCode, stateId )]
			}
			List assetBeansList = new ArrayList()
			assetsList.each{
				AssetEntityBean assetEntityBean = new AssetEntityBean()
				assetEntityBean.setId(it.asset.id)
				assetEntityBean.setAssetTag(it.asset.assetTag)
				assetEntityBean.setAssetName(it.asset.assetName)
				if(AssetComment.find("from AssetComment where assetEntity = ${it?.asset?.id} and commentType = ? and isResolved = ?",['issue',0])){
					assetEntityBean.setCommentType("issue")
				} else if(AssetComment.find('from AssetComment where assetEntity = '+ it?.asset?.id)){
					assetEntityBean.setCommentType("comment")
				} else {
					assetEntityBean.setCommentType("blank")
				}
				assetEntityBean.setPriority(it.asset.priority)
				if(it?.asset.sourceTeamMt){
					assetEntityBean.setSourceTeamMt(ProjectTeam.findById(it?.asset?.sourceTeamMt)?.name)
				}
				if(it?.asset.targetTeamMt){
					assetEntityBean.setTargetTeamMt(ProjectTeam.findById(it?.asset?.targetTeamMt)?.name)
				}
				assetEntityBean.setStatus(it.status)
				assetEntityBean.setCssClass(it.cssClass ? it.cssClass : "")
				assetEntityBean.setCheckVal(it.checkVal)
				assetBeansList.add( assetEntityBean )
			}
			//Statements for JMESA integration
			TableFacade tableFacade = new TableFacadeImpl("tag",request)
			tableFacade.items = assetBeansList

			def servers = AssetEntity.findAllByAssetTypeAndProject('Server',projectInstance)
			def applications = Application.findAllByAssetTypeAndProject('Application',projectInstance)
			def dbs = Database.findAllByAssetTypeAndProject('Database',projectInstance)
			def files = Files.findAllByAssetTypeAndProject('Files',projectInstance)

			def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
			def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
			
			return[ moveBundleInstanceList: moveBundleInstanceList, projectId:projectId, bundleTeams:bundleTeams,
				assetBeansList:assetBeansList, moveBundleInstance:moveBundleInstance, project : projectInstance,
				supportTeam:supportTeam, totalUnracked:totalUnracked, totalSourceAvail:totalSourceAvail,
				totalTargetAvail:totalTargetAvail, totalReracked:totalReracked, totalAsset:totalAssetsSize,
				timeToUpdate : timeToUpdate ? timeToUpdate.SUPER_CONSOLE_REFRESH : "never", showAll : showAll,
				applicationList : applicationList, appOwnerList : appOwnerList, appSmeList : appSmeList,
				transitionStates : transitionStates, params:params, totalAssetsOnHold:totalAssetsOnHold,
				totalSourcePending: totalSourcePending, totalTargetPending: totalTargetPending, role: role, teamType:teamType, assetDependency: new AssetDependency() ,
				servers:servers , applications:applications ,dbs:dbs,files:files, dependencyType:dependencyType, dependencyStatus:dependencyStatus,
				staffRoles:taskService.getRolesForStaff() ]
		} else {
			flash.message = "Please create bundle to view Console"
			redirect(controller:'project',action:'show')
		}
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
	/*-------------------------------------------------------------------
	 *  Used to create Transaction for Supervisor 
	 * @author: Lokanath Reddy
	 * @param : AssetEntity id, priority,assigned to value and from and to States
	 * @return: AssetEntity details and AssetTransition details
	 *------------------------------------------------------------------*/
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
	/*-------------------------------------------
	 * @author : Bhuvaneshwari
	 * @param  : List of assets selected for transition
	 * @return : Common tasks for selected assets
	 *-------------------------------------------*/
	//  To get unique list of task for list of assets through ajax
	def getList = {
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
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

	/*-------------------------------------------
	 *To change the status for an asset
	 *	@author : Bhuvaneshwari
	 * 	@param  : List of assets selected for transition and tostate to change the state
	 * 	@return : Once transition is completed it will redirect to dashboardView method
	 * -------------------------------------------*/
	def changeStatus = {
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		def assetId = params.assetVal
		def assetEnt = AssetEntity.findAll("from AssetEntity ae where ae.id in ($assetId)")
		try{
			assetEnt.each{
				def bundle = it.moveBundle
				def principal = SecurityUtils.subject.principal
				def loginUser = UserLogin.findByUsername(principal)
				def team = it.sourceTeamMt

				def workflow = workflowService.createTransition(bundle.workflowCode,"SUPERVISOR",params.taskList,it,bundle,loginUser,team,params.enterNote)
				if(workflow.success){
					if(params.enterNote != ""){
						def assetComment = new AssetComment()
						assetComment.comment = params.enterNote
						assetComment.commentType = 'issue'
						assetComment.createdBy = loginUser.person
						assetComment.assetEntity = it
						assetComment.save()
					}
				}else{
					flash.message = message(code :workflow.message)
				}
			}
		} catch(Exception ex){
			log.error "changeStatus: unexpected Exception occurred - ${ex.toString()}"
		}
		redirect(action:'dashboardView',params:[moveBundle:params.moveBundle, showAll:params.showAll] )
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
	def create = {
		def assetEntityInstance = new AssetEntity(appOwner:'')

		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute , [sort:"value"])
		def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",["Server"])?.manufacturer

		/*def planStatusAttribute = EavAttribute.findByAttributeCode('planStatus')
		def planStatusOptions = EavAttributeOption.findAllByAttribute(planStatusAttribute)*/

		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)

		def moveBundleList = MoveBundle.findAllByProject(project)
		
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		
		def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)
		

		def railTypeAttribute = EavAttribute.findByAttributeCode('railType')
		def railTypeOption = EavAttributeOption.findAllByAttribute(railTypeAttribute)

		/*def priorityAttribute = EavAttribute.findByAttributeCode('priority')
		def priorityOption = EavAttributeOption.findAllByAttribute(priorityAttribute)*/

		[assetEntityInstance:assetEntityInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId ,railTypeOption:railTypeOption?.value,
					priorityOption:priorityOption?.value ,project:project, manufacturers:manufacturers,redirectTo:params?.redirectTo]



	}
	def show ={
		def items = []
		def assetEntityInstance = AssetEntity.get( params.id )
		def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntityInstance.attributeSet.id order by eav.sortOrder ")
		def projectId = getSession().getAttribute( "CURR_PROJ" )?.CURR_PROJ
		def project = Project.findById( projectId )
		def attributeOptions
		def options
		def frontEndLabel
		def dependentAssets
		def supportAssets
		def assetComment
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
		if(!assetEntityInstance) {
			flash.message = "Asset not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntityInstance])
			supportAssets 	= AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntityInstance])
		}
		if(AssetComment.find("from AssetComment where assetEntity = ${assetEntityInstance?.id} and commentType = ? and isResolved = ?",['issue',0])){
			assetComment = "issue"
		} else if(AssetComment.find('from AssetComment where assetEntity = '+ assetEntityInstance?.id)){
			assetComment = "comment"
		} else {
			assetComment = "blank"
		}
		def assetCommentList = AssetComment.findAllByAssetEntity(assetEntityInstance)
		
		[label:frontEndLabel, assetEntityInstance:assetEntityInstance,supportAssets: supportAssets,
					dependentAssets:dependentAssets, redirectTo : params.redirectTo ,assetComment:assetComment, assetCommentList:assetCommentList,dependencyBundlenumber:AssetDependencyBundle.findByAsset(assetEntityInstance)?.dependencyBundle]
	}
	def edit ={
		def assetEntityInstance = AssetEntity.get(params.id)
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')

		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute,[sort:"value"])
		def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetEntityInstance.assetType])?.manufacturer
		def models = assetEntityInstance.manufacturer ? Model.findAllByManufacturer( assetEntityInstance.manufacturer,[sort:'modelName',order:'asc'] )?.findAll{it.assetType == assetEntityInstance.assetType } : []


		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)

		def moveBundleList = MoveBundle.findAllByProject(project)

		def railTypeAttribute = EavAttribute.findByAttributeCode('railType')
		def railTypeOption = EavAttributeOption.findAllByAttribute(railTypeAttribute)



		def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntityInstance])
		def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntityInstance])
		
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)

		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$projectId order by assetName asc")

		[assetEntityInstance:assetEntityInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId, project: project, railTypeOption:railTypeOption?.value,
					priorityOption:priorityOption?.value,dependentAssets:dependentAssets,supportAssets:supportAssets,
					manufacturers:manufacturers, models:models,redirectTo:params?.redirectTo, dependencyType:dependencyType,
					dependencyStatus:dependencyStatus,servers:servers]

	}

	def update={
		def attribute = session.getAttribute('filterAttr')
		def filterAttr = session.getAttribute('filterAttributes')
		session.setAttribute("USE_FILTERS","true")
		def redirectTo = params.redirectTo
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def formatter = new SimpleDateFormat("MM/dd/yyyy")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def maintExpDate = params.maintExpDate
		if(maintExpDate){
			params.maintExpDate =  GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
		}
		def retireDate = params.retireDate
		if(redirectTo.contains("room_")){
			def newRedirectTo = redirectTo.split("_")
			redirectTo = newRedirectTo[0]
			def rackId = newRedirectTo[1]
			session.setAttribute("RACK_ID", rackId)
		}
		if(retireDate){
			params.retireDate =  GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
		}
		def assetEntityInstance = AssetEntity.get(params.id)
		assetEntityInstance.properties = params
		if(!assetEntityInstance.hasErrors() && assetEntityInstance.save(flush:true)) {
			flash.message = "Asset ${assetEntityInstance.assetName} Updated"
			assetEntityInstance.updateRacks()
			assetEntityService.createOrUpdateAssetEntityDependencies(params, assetEntityInstance)
			if(params.updateView == 'updateView'){
				forward(action:'show', params:[id: params.id])
				
			}else{
				switch(redirectTo){
					case "room":
						redirect( controller:'room',action:list )
						break;
					case "rack":
						redirect( controller:'rackLayouts',action:'create' )
						break;
					case "console":
						redirect( action:dashboardView, params:[ showAll:'show',tag_f_assetName:params.tag_f_assetName,tag_f_priority:attribute.tag_f_priority,tag_f_assetTag:attribute.tag_f_assetTag,tag_f_assetName:attribute.tag_f_assetName,tag_f_status:attribute.tag_f_status,tag_f_sourceTeamMt:attribute.tag_f_sourceTeamMt,tag_f_targetTeamMt:attribute.tag_f_targetTeamMt,tag_f_commentType:attribute.tag_f_commentType,tag_s_1_priority:attribute.tag_s_1_priority,tag_s_2_assetTag:attribute.tag_s_2_assetTag,tag_s_3_assetName:attribute.tag_s_3_assetName,tag_s_4_status:attribute.tag_s_4_status,tag_s_5_sourceTeamMt:attribute.tag_s_5_sourceTeamMt,tag_s_6_targetTeamMt:attribute.tag_s_6_targetTeamMt,tag_s_7_commentType:attribute.tag_s_7_commentType])
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
					case "planningConsole":
				        forward(action:'getLists', params:[entity: params.tabType,labelsList:params.labels,dependencyBundle:session.getAttribute("dependencyBundle")])
						break;
					default:
						redirect( action:list,params:[tag_f_assetName:filterAttr.tag_f_assetName, tag_f_model:filterAttr.tag_f_model, tag_f_sourceLocation:filterAttr.tag_f_sourceLocation, tag_f_sourceRack:filterAttr.tag_f_sourceRack, tag_f_targetLocation:filterAttr.tag_f_targetLocation, tag_f_targetRack:filterAttr.tag_f_targetRack, tag_f_assetType:filterAttr.tag_f_assetType, tag_f_serialNumber:filterAttr.tag_f_serialNumber, tag_f_moveBundle:filterAttr.tag_f_moveBundle, tag_f_depUp:filterAttr.tag_f_depUp, tag_f_depDown:filterAttr.tag_f_depDown,tag_s_1_application:filterAttr.tag_s_1_application,tag_s_2_assetName:filterAttr.tag_s_2_assetName,tag_s_3_model:filterAttr.tag_s_3_model,tag_s_4_sourceLocation:filterAttr.tag_s_4_sourceLocation,tag_s_5_sourceRack:filterAttr.tag_s_5_sourceRack,tag_s_6_targetLocation:filterAttr.tag_s_6_targetLocation,tag_s_7_targetRack:filterAttr.tag_s_7_targetRack,tag_s_8_assetType:filterAttr.tag_s_8_assetType,tag_s_9_assetTag:filterAttr.tag_s_9_assetTag,tag_s_10_serialNumber:filterAttr.tag_s_10_serialNumber,tag_s_11_moveBundle:filterAttr.tag_s_11_moveBundle,tag_s_12_depUp:filterAttr.tag_s_12_depUp,tag_s_13_depDown:filterAttr.tag_s_13_depDown])
	
				}
			}
		}
		else {
			flash.message = "Asset not created"
			assetEntityInstance.errors.allErrors.each{ flash.message += it }
			redirect( action:list,params:[tag_f_assetName:filterAttr.tag_f_assetName, tag_f_model:filterAttr.tag_f_model, tag_f_sourceLocation:filterAttr.tag_f_sourceLocation, tag_f_sourceRack:filterAttr.tag_f_sourceRack, tag_f_targetLocation:filterAttr.tag_f_targetLocation, tag_f_targetRack:filterAttr.tag_f_targetRack, tag_f_assetType:filterAttr.tag_f_assetType, tag_f_serialNumber:filterAttr.tag_f_serialNumber, tag_f_moveBundle:filterAttr.tag_f_moveBundle, tag_f_depUp:filterAttr.tag_f_depUp, tag_f_depDown:filterAttr.tag_f_depDown,tag_s_1_application:filterAttr.tag_s_1_application,tag_s_2_assetName:filterAttr.tag_s_2_assetName,tag_s_3_model:filterAttr.tag_s_3_model,tag_s_4_sourceLocation:filterAttr.tag_s_4_sourceLocation,tag_s_5_sourceRack:filterAttr.tag_s_5_sourceRack,tag_s_6_targetLocation:filterAttr.tag_s_6_targetLocation,tag_s_7_targetRack:filterAttr.tag_s_7_targetRack,tag_s_8_assetType:filterAttr.tag_s_8_assetType,tag_s_9_assetTag:filterAttr.tag_s_9_assetTag,tag_s_10_serialNumber:filterAttr.tag_s_10_serialNumber,tag_s_11_moveBundle:filterAttr.tag_s_11_moveBundle,tag_s_12_depUp:filterAttr.tag_s_12_depUp,tag_s_13_depDown:filterAttr.tag_s_13_depDown])

		}


	}

	def getManufacturersList = {
		def assetType = params.assetType
		def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
		render (view :'manufacturerView' , model:[manufacturers : manufacturers])
	}
	def getModelsList = {
		def manufacturer = params.manufacturer
		def assetType = params.assetType
		def models
		if(manufacturer!="null"){
			def manufacturerInstance = Manufacturer.read(manufacturer)
			models = manufacturerInstance ? Model.findAllByManufacturer( manufacturerInstance,[sort:'modelName',order:'asc'] )?.findAll{it.assetType == assetType } : null
		}
		render (view :'ModelView' , model:[models : models])
	}
	
	/**
	 * Used to generate the List for Task Manager, which leverages a shared closure with listComment
	 */
	def listTasks = {
		params.commentType=AssetCommentType.TASK
		listCommentsOrTasks{}
	}

	/**
	 * Used to generate the List of Comments, which leverages a shared closeure with the above listTasks controller
	 */
	def listComment = {
		params.commentType=AssetCommentType.COMMENT
		listCommentsOrTasks{}
	}
	
	/**
	 * Shared closure used by listTasks and listComment controller methods
	 */
	def listCommentsOrTasks = {
		def project = securityService.getUserCurrentProject()
		def moveEvents = MoveEvent.findAllByProject(project)
		def person = securityService.getUserLoginPerson()

		// Deal with the parameters
		def isTask = params.commentType == AssetCommentType.TASK
		def view = isTask ? 'listTasks' : 'listComment'
		// def action = params.issueBox == "on" ? "issue" : params.resolvedBox  ? "resolved" : params.filter
		def today = new Date()
		def moveEvent
		def filterEvent = 0	    

		def assetCommentQuery = "FROM AssetComment a WHERE a.project = :project"
		def sqlArgs = [project:project]
		
		if ( ! isTask ) {
			// For comments we will filter just on the commentType
			assetCommentQuery += " AND commentType = :commentType "
			sqlArgs << [ commentType:AssetCommentType.COMMENT]
		} else {
			// For tasks, we have a number of different filters that we need to manage
			
		    if ( params.moveEvent?.size() > 0) {
				// zero (0) = All events
				log.info "listCommentsOrTasks: Handling MoveEvent based on params ${params.moveEvent}"
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
				// Add filter to SQL statement and update the user's preferences
				assetCommentQuery += " AND a.moveEvent = :moveEvent "
				sqlArgs << [ moveEvent:moveEvent]
				userPreferenceService.setPreference( 'MOVE_EVENT', "${moveEvent.id}" )
				filterEvent = moveEvent.id
				// log.info "listCommentsOrTasks: setting MOVE_EVENT preference to ${moveEvent.id} for ${person}"
			}

			def action = params.filter
			def justMyTasks = params.issueBox == 'on'
			def justRemainingTasks = params.resolvedBox == 'on'
			
			assetCommentQuery += " AND commentType = :commentType "
			sqlArgs << [ commentType: AssetCommentType.TASK ]
			
			if (justMyTasks) {
				assetCommentQuery += ' AND a.assignedTo = :assignedTo'
				sqlArgs << [ assignedTo:person]
			}
			if (! justRemainingTasks) {
				assetCommentQuery += ' AND status != :status'
				sqlArgs << [ status:AssetCommentStatus.COMPLETED ]
			}
			
			switch (action){
				// Believe that most of these actions come from the Planning Dashboard
				case "issue":
					if(params.resolvedBox == "on" ) {
						assetCommentQuery += " AND a.assignedTo = :assignedTo AND status != :status"
						sqlArgs << [ assignedTo:person, status:AssetCommentStatus.COMPLETED]
					} else {
						assetCommentQuery += " AND  a.assignedTo = :assignedTo "
						sqlArgs << [ assignedTo:person]
					}
					break
				case "openIssue" :
					assetCommentQuery += " AND a.status != :status AND category = :category"
					sqlArgs << [ status:AssetCommentStatus.COMPLETED, category:"discovery" ]
					break
				case "generalOverDue" :
					assetCommentQuery += " AND category in ('general','planning') AND a.dueDate < :dueDate AND a.status != :status"
					sqlArgs << [ dueDate:today, status:AssetCommentStatus.COMPLETED ]
					break
				case "Discovery" :
					assetCommentQuery += " AND a.category= :category "
					sqlArgs << [ category:"discovery"]
					break
				case "dueOpenIssue" :
					assetCommentQuery += " AND a.category= :category AND a.dueDate < :dueDate"
					sqlArgs << [ category:"discovery", dueDate:today]
					break
				case "resolved" :
					if(params.resolvedBox == "on" ) {
						assetCommentQuery += "AND status != :status"
						sqlArgs << [ status:AssetCommentStatus.COMPLETED]
					}
					break
				// this should not be the default any more...	
				//default :
				//	assetCommentQuery += " AND status != :status "
				//	sqlArgs << [ status:AssetCommentStatus.COMPLETED ]
				//	break
					
		   }
			assetCommentQuery += " ORDER BY score DESC, taskNumber ASC, dueDate ASC, dateCreated DESC "
		}
		//log.info "listCommentsOrTasks: SQL filter=${assetCommentQuery}"
		def commentList = AssetComment.findAll(assetCommentQuery,sqlArgs)
		// def start = new Date()
		// log.info "_listCommentsOrTasks:after SQL : ${TimeCategory.minus(new Date(), start)}"
		// start = new Date()
		
		// Initialize all comment property in to bean for jmesa.
		def assetCommentList = new ArrayList(commentList.size())	// preallocate the size of the array to improve performance
		def i=0
		commentList.each{comment->
			AssetCommentBean assetBean = new AssetCommentBean();
			assetBean.with {
				setId(comment.id)
				setTaskNumber(comment.taskNumber)
				setDescription(comment.comment)
				setCommentType(comment.commentType)
				setAssetName(comment.assetEntity?.assetName ?:'')
				setAssetType(comment.assetEntity?.assetType ?:'')
				setStatus(comment.status)
				setLastUpdated(isTask && comment.isRunbookTask() ? comment.statusUpdated : comment.lastUpdated)
				setDueDate(isTask && comment.isRunbookTask() ? comment.estFinish : comment.dueDate)
				setAssignedTo(comment.assignedTo ? (comment.assignedTo?.firstName +" "+ comment.assignedTo?.lastName) : '' )
				setRole(comment.role)
				setCategory(org.apache.commons.lang.StringUtils.capitalize(comment.category))
				setScore(comment.score ?: 0)
				setAssetEntityId( comment.assetEntity?.id )
				assetBean.setRunbookTask( comment.isRunbookTask() )
				setSuccCount( TaskDependency.findAllByPredecessor( comment ).size())
				// TODO - Performance Issue - some reason calling the method on the Service class here is causing a 1+ min delay so the status style is hard coded
				// assetBean.setStatusClass( taskService.getCssClassForStatus(comment.status) )
				setStatusClass( comment.status ? "task_${comment.status.toLowerCase()}" : 'task_na' )

				// Set the Last Updated duration appropriately
				def elapsed = TimeUtil.elapsed(comment.statusUpdated, GormUtil.convertInToGMT( "now", "EDT" ))
				def elapsedSec = elapsed.toMilliseconds() / 1000
				if (comment.status == AssetCommentStatus.READY) {
					if (elapsedSec >= 600) {
						setUpdatedClass('task_late')
					} else if (elapsedSec >= 300) {
						setUpdatedClass('task_tardy')
					}
				} else if (comment.status == AssetCommentStatus.STARTED) {
					def dueInSecs = elapsedSec - (comment.duration ?: 0) * 60
					if (dueInSecs >= 600) {
						setUpdatedClass('task_late')
					} else if (dueInSecs >= 300) {
						setUpdatedClass('task_tardy')
					}
				}
				if (comment.estFinish) {
					elapsed = TimeUtil.elapsed(comment.estFinish, GormUtil.convertInToGMT( "now", "EDT" ))
					elapsedSec = elapsed.toMilliseconds() / 1000
					if (elapsedSec > 300) {
						setDueClass('task_overdue')
					}
				}
			}
			assetCommentList[i++] = assetBean
		}
		//log.info "listCommentsOrTasks: creating list took: ${TimeCategory.minus(new Date(), start)}"
		//start = new Date()
		
		TableFacade tableFacade = new TableFacadeImpl("tag",request)
		tableFacade.items = assetCommentList
		Limit limit = tableFacade.limit
		if(limit.isExported()){
			tableFacade.setExportTypes(response,limit.getExportType())
			tableFacade.setColumnProperties("comment","commentType","assetEntity","mustVerify","isResolved","resolution","resolvedBy","createdBy","commentCode","category",'score',"displayOption")
			tableFacade.render()
		} else {
			// log.info "_listCommentsOrTasks:about to render : ${TimeCategory.minus(new Date(), start)}"
			def model = [ assetCommentList:assetCommentList, rediectTo:'comment', 
				s:params.resolvedBox, issueBox:params.issueBox,
				moveEvents:moveEvents, filterEvent:filterEvent,staffRoles:taskService.getRolesForStaff()]
	      	render (view:view ,model:model )
		}
	}

	def assetOptions = {
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		
		def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)
		
		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		
		return [planStatusOptions:planStatusOptions, priorityOption:priorityOption,dependencyType:dependencyType, dependencyStatus:dependencyStatus]

	}
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
	
	def deleteAssetOptions ={
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
	def assetSummary ={
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.findById( projectId )
		List moveBundleList = MoveBundle.findAllByProject(project,[sort:'name'])
		List assetSummaryList = []
		int totalAsset = 0;
		int totalApplication =0;
		int totalDatabase = 0;
		int totalFiles = 0;
		int unassignedAssetCount = 0;
		int unassignedAppCount = 0;
		int unassignedDBCount = 0;
		int unassignedFilesCount = 0;

		moveBundleList.each{ moveBundle->
			def assetCount = AssetEntity.findAllByMoveBundleAndAssetTypeNotInList( moveBundle,["Application","Database","Files"], params ).size()
			def applicationCount = AssetEntity.findAllByMoveBundleAndAssetType(moveBundle,"Application").size()
			def databaseCount = AssetEntity.findAllByMoveBundleAndAssetType(moveBundle,"Database").size()
			def filesCount = AssetEntity.findAllByMoveBundleAndAssetType(moveBundle,"Files").size()
			assetSummaryList << ["name":moveBundle, "assetCount":assetCount, "applicationCount":applicationCount, "databaseCount":databaseCount, "filesCount":filesCount]
		}

		unassignedAssetCount = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId and assetType not in ('Application','Database','Files')").size()
		unassignedAppCount = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId and assetType='Application'").size()
		unassignedDBCount = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId and assetType='Database'").size()
		unassignedFilesCount = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId and assetType='Files'").size()

		assetSummaryList.each{asset->
			totalAsset=totalAsset + asset.assetCount
			totalApplication = totalApplication + asset.applicationCount
			totalDatabase = totalDatabase + asset.databaseCount
			totalFiles = totalFiles + asset.filesCount

		}
		totalAsset = totalAsset + unassignedAssetCount ;
		totalApplication = totalApplication + unassignedAppCount;
		totalDatabase = totalDatabase + unassignedDBCount;
		totalFiles =totalFiles + unassignedFilesCount;

		return [assetSummaryList:assetSummaryList, totalAsset:totalAsset, totalApplication:totalApplication, totalDatabase:totalDatabase,
			totalFiles:totalFiles,unassignedAssetCount:unassignedAssetCount, unassignedAppCount:unassignedAppCount, unassignedDBCount:unassignedDBCount,
			unassignedFilesCount:unassignedFilesCount]

	}
	def getLists ={
		session.removeAttribute('assetDependentlist')
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
	    def project = Project.findById( projectId )
		def assetDependentlist
		if(params.dependencyBundle!= 'null'){
			 assetDependentlist = AssetDependencyBundle.findAllByDependencyBundleAndProject(params.dependencyBundle,project)
		}else{
			 assetDependentlist = AssetDependencyBundle.findAllByProject(project)?.sort{it.dependencyBundle}
		}
		session.setAttribute('dependencyBundle',params.dependencyBundle)
		session.setAttribute('assetDependentlist',assetDependentlist)
		switch(params.entity){
		case "Apps" :
			def applicationList = assetDependentlist.findAll{it.asset.assetType ==  'Application' }
			def filesDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Files' }.size()
			def assetEntityListSize = assetDependentlist.findAll{it.asset.assetType ==  'VM' || it.asset.assetType ==  'Server' }.size()
			def dbDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Database'}.size()
			def applicationListSize = applicationList.size()
			render(template:"appList",model:[appList:applicationList.asset,assetEntityListSize:assetEntityListSize,applicationListSize:applicationListSize,dependencyBundle:params.dependencyBundle,
				                               filesDependentListSize:filesDependentListSize,appDependentListSize:applicationListSize,dbDependentListSize:dbDependentListSize,asset:'Apps'])
			break;
		case "server":
			def assetEntityList = assetDependentlist.findAll{it.asset.assetType ==  'VM' || it.asset.assetType ==  'Server'}
			def filesDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Files' }.size()
			def appDependentListSize = assetDependentlist.findAll{it.asset.assetType=='Application' }.size()
			def dbDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Database'}.size()
			def assetEntityListSize = assetEntityList.size()
			render(template:"assetList",model:[assetList:assetEntityList.asset,assetEntityListSize:assetEntityListSize,dependencyBundle:params.dependencyBundle,
				                               filesDependentListSize:filesDependentListSize,appDependentListSize:appDependentListSize,dbDependentListSize:dbDependentListSize,asset:'server'])
			break;
		case "database" :
			def databaseList = assetDependentlist.findAll{it.asset.assetType ==  'Database' }
			def filesDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Files' }.size()
			def appDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Application' }.size()
			def assetEntityListSize = assetDependentlist.findAll{it.asset.assetType == 'VM' || it.asset.assetType ==  'Server' }.size()
			def dbDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Database'}.size()
			def dbListSize = databaseList.size()
			render(template:"dbList",model:[databaseList:databaseList.asset,assetEntityListSize:assetEntityListSize,dependencyBundle:params.dependencyBundle,
				                               filesDependentListSize:filesDependentListSize,appDependentListSize:appDependentListSize,dbDependentListSize:dbListSize,asset:'database'])
			break;
		case "files" :
			def filesList = assetDependentlist.findAll{it.asset.assetType ==  'Files' }
			def dbDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Database' }.size()
			def appDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Application' }.size()
			def assetEntityListSize = assetDependentlist.findAll{it.asset.assetType ==  'VM' || it.asset.assetType ==  'Server' }.size()
			def filesListSize = filesList.size()
			render(template:"filesList",model:[filesList:filesList.asset,assetEntityListSize:assetEntityListSize,dependencyBundle:params.dependencyBundle,
										   filesDependentListSize:filesListSize,appDependentListSize:appDependentListSize,dbDependentListSize:dbDependentListSize,asset:'files'])
			break;
		case "graph" :
			def filesListSize = assetDependentlist.findAll{it.asset.assetType ==  'Files' }.size()
			def dbDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Database' }.size()
			def appDependentListSize = assetDependentlist.findAll{it.asset.assetType ==  'Application' }.size()
			def assetEntityListSize = assetDependentlist.findAll{it.asset.assetType ==  'VM' || it.asset.assetType ==  'Server' }.size()
			def graphData = [:]
			def force 
			def distance 
			def friction = params.friction && params.friction != 'undefined' ? params.friction : 0.8
			def height 
			def width 
			def moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
			Set uniqueMoveEventList = moveBundleList.moveEvent
		    uniqueMoveEventList.remove(null)
		    List moveEventList = []
		    moveEventList =  uniqueMoveEventList.toList()
		    moveEventList.sort{it?.name}
			def eventColorCode = [:]
			int colorDiff
			if(moveEventList.size()){
			   colorDiff = (232/moveEventList.size()).intValue()
			}
			def labelList = params.labelsList
			labelList = labelList?.replace(" ","")
			List labels = labelList ?  labelList.split(",") : []
			moveEventList.eachWithIndex{ event, i -> 
				def colorCode = colorDiff * i
				def colorsCode = "rgb(${colorCode},${colorCode},${colorCode})"
				eventColorCode << [(event.name):colorsCode]
			}
			if(assetDependentlist.size()<30){
				force = -100
				distance = 40
				graphData << ["force":-100]
				graphData << ["linkdistance":40]
		    }else if(assetDependentlist.size()<200){
				force = -80
				distance = 30
				graphData << ["force":-80]
				graphData << ["linkdistance":30]
		    }else{
				force = -70
				distance = 20
				graphData << ["force":-70]
				graphData << ["linkdistance":20]
		    }
			graphData << ["friction":friction]
			def graphNodes = []
			if(assetDependentlist.size()<30){
				height = 400
				width = 800
				graphData << ["width":800]
				graphData << ["height":400]
		    }else if(assetDependentlist.size()<200){
				height = 800
				width = 1200
				graphData << ["width":1200]
				graphData << ["height":800]
		    }else{
				height = 2200
				width = 1800
				graphData << ["width":1800]
				graphData << ["height":2200]
		    }
			
			assetDependentlist.each{
				def name = ""
				def shape = "circle"
				def size = 150
				def title = it.asset.assetName
				def color = ''				
				if(it.asset.assetType == "Application"){
					if(labels.contains("apps"))
						name = it.asset.assetName
					shape = "gray"
				} else if(['VM','Server'].contains(it.asset.assetType)){
					if(labels.contains("servers"))
						name = it.asset.assetName
					shape = "square"
				} else if(['Database','Files'].contains(it.asset.assetType)){
					if(labels.contains("files"))
						name = it.asset.assetName
					shape = "triangle-up"
				}
				def moveEventName = it.asset.moveBundle?.moveEvent?.name
				graphNodes << ["id":it.asset.id,"name":name,"type":it.asset.assetType,"group":it.dependencyBundle, 
								shape:shape, size : size, title: title,color:eventColorCode[moveEventName]?eventColorCode[moveEventName]:"red"]
		}
			graphData << ["nodes":graphNodes]
			def assetDependencies = AssetDependency.findAll("From AssetDependency where asset.project = :project OR dependent.project = :project",[project:project])
			def graphLinks = []
			assetDependencies.each{
				def opacity
				def statusColor = ''
				if(it.status=='Questioned'){
				statusColor='red'
				opacity = 1
				}else if(it.status=='Unknown'|| it.status=='Validated') {
					statusColor='gray'
					opacity = 1
				}else{
					statusColor='gray'
					opacity = 0.2
				}
				def sourceIndex = graphNodes.id.indexOf(it.asset.id)
				def targetIndex = graphNodes.id.indexOf(it.dependent.id)
				if(sourceIndex != -1 && targetIndex != -1){
					graphLinks << ["source":sourceIndex,"target":targetIndex,"value":2,"statusColor":statusColor,"opacity":opacity,"distance":50]
				}
			}
			graphData << ["links":graphLinks]
			JSON output = graphData as JSON
			def currentfile = ApplicationHolder.application.parentContext.getResource( "/d3/force/force.js" ).getFile()
			assetEntityService.deleteTempGraphFiles("/d3/force", "G_")
			def file_name = "G_${new Date().getTime()}"
			def file_path = currentfile.absolutePath.replace("force.js",file_name)
			File file = new File(file_path)
			file.write(output.toString())
			render(template:'dependencyGraph',model:[assetEntityListSize:assetEntityListSize,dependencyBundle:params.dependencyBundle,
										   filesDependentListSize:filesListSize,appDependentListSize:appDependentListSize,dbDependentListSize:dbDependentListSize,
										   asset:'graph', force:force, distance:distance,friction:friction,height:height,width:width, labels:labels , appChecked:labels.contains('apps') ? true : false , serverChecked:labels.contains('servers') ? true : false,
										   filesChecked:labels.contains('files') ? true : false,eventColorCode:eventColorCode, file_name:file_name])
			break;
		}
	}
	
	def reloadMap={
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.findById( projectId )
		def graphData = [:]
		def assetDependentlist = session.getAttribute('assetDependentlist')
		def labelList = params.labelsList
		labelList = labelList.replace(" ","")
		List labels = labelList ?  labelList.split(",") : []
		def force = params.force && params.force != 'undefined' ? Integer.parseInt(params.force) : -70
		def distance = params.distance && params.distance != 'undefined' ? Integer.parseInt(params.distance) : 20
		def friction = params.friction && params.friction != 'undefined' ? params.friction : 0.9
		def height = params.height && params.height != 'undefined' ? params.height : 800
		def width = params.width && params.width!= 'undefined' ? params.width : 1200
		def moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
		Set uniqueMoveEventList = moveBundleList.moveEvent
		uniqueMoveEventList.remove(null)
		List moveEventList = []
		moveEventList =  uniqueMoveEventList.toList()
		moveEventList.sort{it?.name}
		def eventColorCode = [:]
		int colorDiff
		if(moveEventList.size()){
		colorDiff = (232/moveEventList.size()).intValue()
		}
		moveEventList.eachWithIndex{ event, i ->
			def colorCode = colorDiff * i
			def colorsCode = "rgb(${colorCode},${colorCode},${colorCode})"
			eventColorCode << [(event.name):colorsCode]
		}
		
		graphData << ["force":force]
		graphData << ["linkdistance":distance]
		def graphNodes = []
		graphData << ["width":width]
		graphData << ["height":height]
		graphData << ["friction":friction]
		assetDependentlist.each{
			def name = ""
			def shape = "circle"
			def size = 150
			def title = it.asset.assetName
			def color = ''
			if(it.asset.assetType == "Application"){
				if(labels.contains("apps"))
					name = it.asset.assetName
				shape = "circle"
			} else if(['VM','Server'].contains(it.asset.assetType)){
				if(labels.contains("servers"))
					name = it.asset.assetName
				shape = "square"
			} else if(['Database','Files'].contains(it.asset.assetType)){
				if(labels.contains("files"))
					name = it.asset.assetName
				shape = "triangle-up"
			}
			def moveEventName = it.asset.moveBundle?.moveEvent?.name
				graphNodes << ["id":it.asset.id,"name":name,"type":it.asset.assetType,"group":it.dependencyBundle, 
								shape:shape, size : size, title: title,color:eventColorCode[moveEventName]?eventColorCode[moveEventName]:"red"]
		}
		graphData << ["nodes":graphNodes]
		def assetDependencies = AssetDependency.findAll("From AssetDependency where asset.project = :project OR dependent.project = :project",[project:project])
		def graphLinks = []
		assetDependencies.each{
			def opacity
			def statusColor = ''
			if(it.status=='Questioned'){
				statusColor='red'
				opacity = 1
			}else if(it.status=='Unknown'|| it.status=='Validated') {
				statusColor='gray'
				opacity = 1
			}else{
				statusColor='gray'
				opacity = 0.2
			}
			def sourceIndex = graphNodes.id.indexOf(it.asset.id)
			def targetIndex = graphNodes.id.indexOf(it.dependent.id)
			if(sourceIndex != -1 && targetIndex != -1){
				graphLinks << ["source":sourceIndex,"target":targetIndex,"value":2,"statusColor":statusColor,"opacity":opacity,"distance":50]
			}
		}
		graphData << ["links":graphLinks]
		JSON output = graphData as JSON
		def currentfile = ApplicationHolder.application.parentContext.getResource( "/d3/force/force.js" ).getFile()
		assetEntityService.deleteTempGraphFiles("/d3/force", "G_")
		def file_name = "G_${new Date().getTime()}"
		def file_path = currentfile.absolutePath.replace("force.js",file_name)
		File file = new File(file_path)
		file.write(output.toString())			
		render(template:'map',model:[asset:'graph', force:force, distance:distance,friction:friction,height:height,width:width, labels:labels,eventColorCode:eventColorCode,file_name:file_name])
		
	}
	/**
	* Delete multiple  Assets.
	*/
	
	def deleteBulkAsset={
		def assetArray = params['assetLists[]']
		def assetList
		if(assetArray.class.toString() == "class java.lang.String"){
		  assetList = assetArray.split(",")
		}else{
		  assetList = assetArray
		}
		def assetNames = []
		def assetEntityInstance
		def projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
	
			for(int i=0 ; i<assetList.size();i++){
			    assetEntityInstance = AssetEntity.get( assetList[i] )
				
				
				if(assetEntityInstance) {
					assetNames.add(assetEntityInstance.assetName)
					ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.asset = ${assetEntityInstance.id}")
					AssetTransition.executeUpdate("delete from AssetTransition ast where ast.assetEntity = ${assetEntityInstance.id}")
					AssetComment.executeUpdate("delete from AssetComment ac where ac.assetEntity = ${assetEntityInstance.id}")
					ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset = ${assetEntityInstance.id}")
					AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar aev where aev.assetEntity = ${assetEntityInstance.id}")
					ProjectTeam.executeUpdate("update ProjectTeam pt set pt.latestAsset = null where pt.latestAsset = ${assetEntityInstance.id}")
					AssetCableMap.executeUpdate("delete AssetCableMap where fromAsset = ? ",[assetEntityInstance])
					AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
													toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
													where toAsset = ?""",[assetEntityInstance])
					AssetDependency.executeUpdate("delete AssetDependency where asset = ? or dependent = ? ",[assetEntityInstance, assetEntityInstance])
					AssetDependencyBundle.executeUpdate("delete from AssetDependencyBundle ad where ad.asset = ${assetEntityInstance.id}")
					
					
					assetEntityInstance.delete()
				}
				String names = assetNames.toString().replace('[','').replace(']','')
				flash.message = "AssetEntity ${names} deleted"
		}
	  render "success"
	}
	
	def getWorkflowTransition={
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def assetCommentId = params.assetCommentId
		def assetEntity = AssetEntity.get(params.assetId)
		def workFlowInstance = Workflow.findByProcess(project.workflowCode)
		def workFlowTransition = WorkflowTransition.findAllByWorkflowAndCategory(workFlowInstance, params.category)
		//def workFlowTransition = WorkflowTransition.findAllByWorkflow(workFlowInstance) TODO : should be removed after completion of this new feature
		if(assetEntity){
			def existingWorkflows = assetCommentId ? AssetComment.findAllByAssetEntityAndIdNotEqual(assetEntity, assetCommentId ).workflowTransition : AssetComment.findAllByAssetEntity(assetEntity ).workflowTransition
			workFlowTransition.removeAll(existingWorkflows)
		}
		def selectControl = ''
		if(workFlowTransition.size()){
			def paramsMap = ["selectId":"workFlowId", "selectName":"workFlow", "from":workFlowTransition, "optionKey":"id", "optionValue":"name"]
			selectControl = HtmlUtil.genHtmlSelect( paramsMap )
		}
		render selectControl
	}
    
	/**
	 * @param: commentId
	 * @return : HTML Select of prdecessor list
	 */
	def getPredecessor = {
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		
		String queryForPredecessor = "FROM AssetComment a WHERE a.project=${projectId} AND a.category='${params.category}' AND a.commentType='${AssetCommentType.TASK}' "
		if(params.assetCommentId){ 
			queryForPredecessor += "AND a.id != ${params.assetCommentId}"
			def assetComment = AssetComment.get(params.assetCommentId)
			def assetForComment = assetComment.assetEntity
			def moveEventForComment =  assetForComment ? assetForComment.moveBundle?.moveEvent : assetComment.moveEvent
			if(moveEventForComment){
				queryForPredecessor += "AND (a.assetEntity.moveBundle.moveEvent.id = ${moveEventForComment?.id} OR a.moveEvent.id = ${moveEventForComment?.id})"
			}
		}
		queryForPredecessor += "ORDER BY a.taskNumber ASC"
		def prdecessors = AssetComment.findAll(queryForPredecessor)
		def taskId = params.assetCommentId ? 'taskDependencyEditId' : 'taskDependencyId'
	    def selectName = params.assetCommentId ? 'taskDependencyEdit' : 'taskDependencySave'
		def paramsMap = ["selectId":"${taskId}", "selectName":"${selectName}", "from":prdecessors,  "optionKey":"id"]
		def selectControl = HtmlUtil.genHtmlSelect( paramsMap )

		render selectControl
	}
	/**
	 * @param: forView
	 * @return: HTML select of staff belongs to company and TDS
	 * 
	 */
	def updateAssignedToSelect = {
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def viewId = params.forView
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
		//log.debug "updateAssignedToSelect(): id=${params.id}, person=${person}, selectedId=${selectedId}" 

		def projectStaff = partyRelationshipService.getProjectStaff( projectId )?.staff
		projectStaff.sort{it.firstName}
		def paramsMap = ["selectId":"${viewId}", "selectName":"${viewId}", "from":projectStaff, "optionKey":"id", "selection":selectedId ]
		def assignedToSelect = HtmlUtil.genHtmlSelect( paramsMap )
		render assignedToSelect
	}
	/**
	 * Generates an HTML SELECT control for the AssetComment.status property according to user role and current status of AssetComment(id)
	 * @param	params.id	The ID of the AssetComment to generate the SELECT for
	 * @return render HTML
	 */
	def updateStatusSelect = {
		
		def mapKey = securityService.hasRole("PROJ_MGR") ? "PROJ_MGR" : "USER" 
		def optionForRole = statusOptionForRole.get(mapKey)
		def status = AssetComment.read(params.id)?.status ?: '*EMPTY*'
		def optionList = optionForRole.get(status)
		def selected
		def paramsMap = ["selectId":"statusEditId", "selectName":"statusEditId", "jsEvent":"onChange='showResolve(this.value)'", 
							"from":optionList, "selection":status, "noSelectionString":["key":"", 'value':"Please Select"]]
		def statusSelect = HtmlUtil.genHtmlSelect( paramsMap )
		
		render statusSelect
	  }
	
	/**
	 * Loads List of predecessors HTML SELECT control for the AssetComment at editing time
	 * @param	params.id	The ID of the AssetComment to load  predecessor SELECT for
	 * @return render HTML
	 */
	def loadPredecessor = {
		def assetComment = AssetComment.get(params.id)
		def taskDependencies = assetComment.taskDependencies
		def predEditTable = new StringBuffer("""<table id="predecessorEditTableId" cellspacing="0" style="border:0px;width:0px"><tbody>""")
		taskDependencies.each{ taskDep ->
			def predecessor = taskDep.predecessor
			def optionList = AssetComment.constraints.category.inList.toList()
			def paramsMap = ["selectId":"predecessorCategoryEditId_${taskDep.id}", "selectName":"category", "from":optionList, 
                             	"jsEvent":"onChange='fillPredecessor(this.id,this.value,${assetComment.id})'", "selection":predecessor.category]
			// To get html select from HtmlUtil class
			def selectCategory = HtmlUtil.genHtmlSelect(paramsMap)
			
			def predFortask = taskService.genSelectForTaskDependency(taskDep, assetComment)
			predEditTable.append("""<tr id="row_Edit_${taskDep.id}"><td>${selectCategory}</td>
										<td id="taskDependencyEditTdId_${taskDep.id}">${predFortask}</td>
										<td><a href="javascript:deleteRow('row_Edit_${taskDep.id}')"><span class="clear_filter"><u>X</u></span></a></td>""")
			selectCategory = ""
		}
		render predEditTable
	}
}
