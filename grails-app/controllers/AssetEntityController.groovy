import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

import jxl.*
import jxl.read.biff.*
import jxl.write.*
import net.tds.util.jmesa.AssetCommentBean
import net.tds.util.jmesa.AssetEntityBean

import org.apache.commons.lang.StringUtils
import org.apache.shiro.SecurityUtils
import java.io.File;
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit
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
import com.tds.asset.FieldImportance
import com.tds.asset.Files
import com.tds.asset.TaskDependency
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdsops.tm.enums.domain.EntityType
import com.tdsops.tm.enums.domain.ValidationType
import com.tdssrc.eav.*
import com.tdssrc.grails.ApplicationConstants
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil

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
					dataTransferBatchs: dataTransferBatchs, args:params.list("args")] )
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
		def sheetNameMap = ['Title','Applications','Servers','Databases','Storage','Dependencies']
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
				def filesSheet = workbook.getSheet( "Storage" )
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
					flash.message = " Storage Column Headers : ${missingHeader} not found, Please check it."
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
					if(params.asset == 'asset'){
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
												flash.message = "StorageId should be Integer"
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
				added = serverAdded + appAdded + dbAdded + filesAdded + dependencyAdded
				
				flash.message =  "assetEntity.upload.message" 
				def addMessage = serverAdded ? ("${serverAdded} Servers, "): "" 
					addMessage += appAdded ? ("$appAdded Applications, "): ""
					addMessage += dbAdded ? ("$dbAdded Databases, "): ""
					addMessage += filesAdded ? ("$filesAdded Storage, "): ""
					
				def args = [added, addMessage ? addMessage.substring(0,addMessage.lastIndexOf(",")) :" ", 
							skipped.size() ? (skipped+" Records skipped. ") : " " ]
				
				redirect( action:assetImport, params:[  message:flash.message, args:args] )
				return;
			}
		} catch( NumberFormatException ex ) {
			ex.printStackTrace()
			flash.message = ex
			redirect( action:assetImport, params:[ message:flash.message] )
			return;
		} catch( Exception ex ) {
			ex.printStackTrace()
			flash.message = ex
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
			SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd");
			def exportDate = fileFormat.format(TimeUtil.nowGMT())
			def filename = project?.name?.replace(" ","_")+"-"+bundleNameList.toString().replace(" ","_")+"-"+exportDate
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
			def roomSheet
			def rackSheet

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
			roomSheet = book.getSheet( sheetNames[6] )
			rackSheet = book.getSheet( sheetNames[7] )

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
								} else if(attribute == "retireDate" || attribute == "maintExpDate"){
									addContentToSheet = new Label(serverMap[colName], r, (asset[r-1].(serverDTAMap.eavAttribute.attributeCode[coll]) ? fileFormat.format(asset[r-1].(serverDTAMap.eavAttribute.attributeCode[coll])) :''))
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
					//Export rooms
					if(params.room=='room'){
						def formatter = new SimpleDateFormat("MM/dd/yyyy")
						def rooms = Room.findAllByProject(project)
						def roomSize = rooms.size()
						def roomMap = ['roomId':'id', 'Name':'roomName', 'Location':'location', 'Depth':'roomDepth', 'Width':'roomWidth',
									   'Source':'source', 'Address':'address', 'City':'city', 'Country':'country', 'StateProv':'stateProv',
									   'Postal Code':'postalCode', 'Date Created':'dateCreated', 'Last Updated':'lastUpdated'
									  ]
						
						def roomCol = fileSheet.getColumns()
						def roomSheetColumns = []
						for ( int c = 0; c < roomCol; c++ ) {
							def roomCellContent = roomSheet.getCell( c, 0 ).contents
							roomSheetColumns << roomCellContent
						}
						roomSheetColumns.removeAll('')
						for ( int r = 1; r <= roomSize; r++ ) {
							roomSheetColumns.eachWithIndex{column, i->
								def addContentToSheet
								if(column == 'roomId'){
									def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
									addContentToSheet = new Number(0, r, (rooms[r-1].id))
								} else {
								   if(column=='Date Created' || column=='Last Updated')
								   		addContentToSheet = new Label( i, r, rooms[r-1]."${roomMap[column]}" ? 
											   String.valueOf( formatter.format(rooms[r-1]."${roomMap[column]}")): "" )
								   else if(column =="Source")
								 		addContentToSheet = new Label( i, r, String.valueOf(rooms[r-1]."${roomMap[column]}" ==1 ? "Source" : "Target" ) )
								   else
										addContentToSheet = new Label( i, r, String.valueOf(rooms[r-1]."${roomMap[column]}"?: "" ) )
								}
								roomSheet.addCell( addContentToSheet )
							}
					  }
					}
					
					//Rack Exporting 
					if(params.rack=='rack'){
						def racks = Rack.findAllByProject(project)
						def rackSize = racks.size()
						def rackMap = ['rackId':'id', 'Tag':'tag', 'Location':'location', 'Room':'room', 'RoomX':'roomX',
									   'RoomY':'roomY', 'PowerA':'powerA', 'PowerB':'powerB', 'PowerC':'powerC', 'Type':'rackType',
									   'Front':'front', 'Model':'model', 'Source':'source', 'Model':'model'
									  ]
						
						def rackCol = fileSheet.getColumns()
						def rackSheetColumns = []
						for ( int c = 0; c < rackCol; c++ ) {
							def rackCellContent = rackSheet.getCell( c, 0 ).contents
							rackSheetColumns << rackCellContent
						}
						rackSheetColumns.removeAll('')
						for ( int r = 1; r <= rackSize; r++ ) {
							rackSheetColumns.eachWithIndex{column, i->
								def addContentToSheet
								if(column == 'rackId'){
									def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
									addContentToSheet = new Number(0, r, (racks[r-1].id))
								} else {
								   if(column =="Source")
										 addContentToSheet = new Label( i, r, String.valueOf(racks[r-1]."${rackMap[column]}" ==1 ? "Source" : "Target" ) )
								   else
										addContentToSheet = new Label( i, r, String.valueOf(racks[r-1]."${rackMap[column]}"?: "" ) )
								}
								rackSheet.addCell( addContentToSheet )
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
			redirect( action:exportAssets, params:[ message:flash.message] )
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
		def filters = session.AE?.JQ_FILTERS
		session.AE?.JQ_FILTERS = []
		
		def project = securityService.getUserCurrentProject()
		def entities = assetEntityService.entityInfo( project )
		
		render(view:'list', model:[assetDependency : new AssetDependency(), dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus,
			event:params.moveEvent, filter:params.filter, type:params.type, plannedStatus:params.plannedStatus,  servers : entities.servers, 
			applications : entities.applications, dbs : entities.dbs, files : entities.files,  networks :entities.networks, assetName:filters?.assetNameFilter ?:'', 
			assetType:filters?.assetTypeFilter ?:'', planStatus:filters?.planStatusFilter ?:'', sourceRack:filters?.sourceRackFilter ?:'',
			moveBundle:filters?.moveBundleFilter ?:'', model:filters?.modelFilter ?:'', sourceLocation:filters?.sourceLocationFilter ?:'', 
			targetLocation:filters?.targetLocationFilter ?:'', targetRack:filters?.targetRackFilter ?:'', assetTag:filters?.assetTagFilter ?:'', 
			serialNumber:filters?.serialNumberFilter ?:'', sortIndex:filters?.sortIndex, sortOrder:filters?.sortOrder, moveBundleId:params.moveBundleId,
			staffRoles:taskService.getRolesForStaff(), sizePref:userPreferenceService.getPreference("assetListSize")?: '25'  ]) 
	}
	/**
	 * This method is used by JQgrid to load assetList
	 */
	def listJson = {
		log.info "start"
		def sortIndex = params.sidx ?: 'assetName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows =  Integer.valueOf(params.rows) 
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows

		def project = securityService.getUserCurrentProject()
		def moveBundleList
		
		session.AE = [:]
		userPreferenceService.setPreference("assetListSize", "${maxRows}")
		
		if(params.event && params.event.isNumber()){
			def moveEvent = MoveEvent.read( params.event )
			moveBundleList = moveEvent?.moveBundles?.findAll {it.useOfPlanning == true}
		} else {
			moveBundleList = MoveBundle.findAllByProjectAndUseOfPlanning(project,true)
		}
		
		def assetType = params.filter  ? ApplicationConstants.assetFilters[ params.filter ] : []

		def bundleList = params.moveBundle ? MoveBundle.findAllByNameIlikeAndProject("%${params.moveBundle}%", project) : []
		def models = params.model ? Model.findAllByModelNameIlike("%${params.model}%") : []
		
		log.info "before criteria"
		def assetEntities = AssetEntity.createCriteria().list(max: maxRows, offset: rowOffset) {	
			log.info "doot"
			eq("project", project)
			if (params.assetName) 
				ilike('assetName', "%${params.assetName.trim()}%")
			if (params.assetType) 
				ilike('assetType', "%${params.assetType}%")
			if (models)
				'in'('model', models)
			if (params.sourceLocation)
				ilike('sourceLocation', "%${params.sourceLocation}%")
			if (params.sourceRack) 
				ilike('sourceRack', "%${params.sourceRack}%")
			if (params.targetLocation) 
				ilike('targetLocation', "%${params.targetLocation}%")
			if (params.targetRack) 
				ilike('targetRack', "%${params.targetRack}%")
			if (params.assetTag)
				ilike('assetTag', "%${params.assetTag}%")
			if (params.serialNumber)
				ilike('serialNumber', "%${params.serialNumber}%")
			if (params.planStatus)  
				ilike('planStatus', "%${params.planStatus}%")
			if (bundleList) 
				'in'('moveBundle', bundleList)
			if(params.plannedStatus) 
				eq("planStatus", params.plannedStatus)
				
			if(params.moveBundleId){
				if(params.moveBundleId =='unAssigned'){
					isNull('moveBundle')
				} else {
					eq('moveBundle', MoveBundle.read(params.moveBundleId))
				}
			}
			
			log.info "dooot"
			// if filter have some value then this one is getting requested from planning dashboard to filter the asset list. else it will be blank.
			if ( params.filter ) {
				if (params.filter !='other')  // filter is not other means filter is in (Server, VM , Blade) and others is excepts (Server, VM , Blade).
					'in'('assetType', assetType)
				else 
					not {'in'("assetType",  assetType)}
					
				or{
					and {
						'in'('moveBundle', moveBundleList) 
					} 
					and {
						isNull('moveBundle')
					}
				}
				if( params.type=='toValidate') 
					eq ('validation','Discovery')
				
			} else {
				not {'in'("assetType",  ["Application","Database","Files"])}
			}
			
			log.info "doooot"
			order(sortIndex, sortOrder).ignoreCase()
		}
		
		log.info "done criteria"
		
		def totalRows = assetEntities.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)
		
		assetEntities.each{
			log.info "dep: ${AssetDependency.countByDependentAndStatusNotEqual(it, "Validated")} asset: ${AssetDependency.countByAssetAndStatusNotEqual(it, "Validated")}"
		}
		
		def results = assetEntities?.collect { [ cell: ['',it.assetName, it.assetType,it.model?.modelName, it.sourceLocation,
					it.sourceRack, it.targetLocation, it.targetRack, it.assetTag, it.serialNumber,it.planStatus,it.moveBundle?.name,
					AssetDependencyBundle.findByAsset(it)?.dependencyBundle,
					(AssetDependency.countByDependentAndStatusNotEqual(it, "Validated")+AssetDependency.countByAssetAndStatusNotEqual(it, "Validated")!=0)?(AssetDependency.countByDependentAndStatusNotEqual(it, "Validated")+AssetDependency.countByAssetAndStatusNotEqual(it, "Validated")):(''),
					AssetComment.find("from AssetComment ac where ac.assetEntity=:entity and commentType=:type and status!=:status",
					[entity:it, type:'issue', status:'completed']) ? 'issue' :
					(AssetComment.find("from AssetComment ac where ac.assetEntity=:entity",[entity:it]) ? 'comment' : 'blank')], id: it.id,
			]}

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
		def modelName = params.models
		def manufacturerName = params.manufacturers
		def assetType = params.assetType ?: 'Server'
		if (params.("manufacturer.id") && params.("manufacturer.id").isNumber()){
		   userPreferenceService.setPreference("lastManufacturer", Manufacturer.read(params.manufacturer.id)?.name)
		} 
		userPreferenceService.setPreference("lastType", assetType)
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
		if( manufacturerName ){
			params.manufacturer = assetEntityAttributeLoaderService.getdtvManufacturer( manufacturerName )
			params.model = assetEntityAttributeLoaderService.findOrCreateModel(manufacturerName, modelName, assetType)
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
				session.setAttribute("USE_FILTERS", "true")
				redirect( controller:'rackLayouts',action:'create' )
			} else if(redirectTo == "assetAudit"){
				render(template:'auditDetails', model:[assetEntity:assetEntityInstance, source:params.source, assetType:params.assetType])
			} else {
				session.AE?.JQ_FILTERS = params
				redirect( action:list)
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
			} else if(redirectTo == "assetAudit"){
				render(template:'createAuditDetails', model:[assetEntity:assetEntityInstance, source:params.source, assetType:params.assetType])
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
		
		def estformatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		def dateFormatter = new SimpleDateFormat("MM/dd/yyyy ");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetComment = AssetComment.get(params.id)
		if(assetComment){
			if(assetComment.createdBy){
				personCreateObj = Person.find("from Person p where p.id = $assetComment.createdBy.id")
				dtCreated = estformatter.format(TimeUtil.convertInToUserTZ(assetComment.dateCreated, tzId));
			}
			if(assetComment.dateResolved){
				personResolvedObj = Person.find("from Person p where p.id = $assetComment.resolvedBy.id")
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
				def dateCreated = TimeUtil.convertInToUserTZ(it.dateCreated, tzId).format("E, d MMM 'at ' HH:mma")
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
			def taskDependencies = assetComment.taskDependencies
			if (taskDependencies.size() > 0) {
				taskDependencies = taskDependencies.sort{ it.predecessor.taskNumber }
				predecessorTable = new StringBuffer('<table cellspacing="0" style="border:0px;"><tbody>')
				taskDependencies.each() { taskDep ->
					def task = taskDep.predecessor
					def css = taskService.getCssClassForStatus(task.status)
					def taskDesc = task.comment?.length()>50 ? task.comment.substring(0,50): task.comment
					predecessorTable.append("""<tr class="${css}" style="cursor:pointer;" onClick="showAssetComment(${task.id}, 'show')"><td>${task.category}</td><td>${task.taskNumber ? task.taskNumber+':' :''}${taskDesc}</td></tr>""")
			    }
				predecessorTable.append('</tbody></table>')
			}
			def taskSuccessors = TaskDependency.findAllByPredecessor( assetComment )
			def successorsCount= taskSuccessors.size()
			def predecessorsCount = taskDependencies.size()
			def successorTable = ""
			if (taskSuccessors.size() > 0) {
				taskSuccessors = taskSuccessors.sort{ it.assetComment.taskNumber }
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
				eventName:assetComment.moveEvent?.name ?: "", dueDate:dueDate, etStart:etStart, etFinish:etFinish,atStart:atStart,notes:notes,
				workflow:workflow,roles:roles, predecessorTable:predecessorTable, successorTable:successorTable,maxVal:maxVal,
				cssForCommentStatus:cssForCommentStatus, statusWarn:taskService.canChangeStatus ( assetComment ) ? 0 : 1, 
				successorsCount:successorsCount, predecessorsCount:predecessorsCount]
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
		} else if(subject.hasRole("EDITOR")){
			role = "EDITOR"
		}
		// NOTE - I don't see role referenced in the remainder of this code so it might not be used JM 12/7/2012
		
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
			def networks = AssetEntity.findAllByAssetTypeAndProject('Network',projectInstance)

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
				servers:servers , applications:applications ,dbs:dbs,files:files,networks:networks, dependencyType:dependencyType, dependencyStatus:dependencyStatus,
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
			models=getModelSortedByStatus(manufacuterer)
			
			def moveBundleList = MoveBundle.findAllByProject(project)
			
			def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
			
			def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)
			
	
			def railTypeAttribute = EavAttribute.findByAttributeCode('railType')
			def railTypeOption = EavAttributeOption.findAllByAttribute(railTypeAttribute)
			
			//fieldImportance for Discovery by default
			def configMap = assetEntityService.getConfig('AssetEntity','Discovery')
				
			def paramsMap = [assetEntityInstance:assetEntityInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
								planStatusOptions:planStatusOptions?.value, projectId:project.id ,railTypeOption:railTypeOption?.value,
								priorityOption:priorityOption?.value ,project:project, manufacturers:manufacturers,redirectTo:params?.redirectTo,
								models:models, assetType:assetType, manufacuterer:manufacuterer, config:configMap.config ,customs:configMap.customs]
			 
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
	def show ={
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def userLogin = securityService.getUserLogin()
		def assetEntity
		flash.message = null
		
		if (params.containsKey('id')) {
			assetEntity = AssetEntity.findByIdAndProject( params.id, project )
			if (!assetEntity) {
				flash.message = "Unable to find asset within current project"
				log.warn "show - asset id (${params.id}) not found for project (${project.id}) by user ${userLogin}"				
			}
		} else {
			flash.message = "Asset reference id was missing from request"
			log.error "show - missing params.id in request by user ${userLogin}"
		}
		if (flash.message) {
		   def errorMap = [errMsg : flash.message]
		   render errorMap as JSON
		} else {
		
			def items = []
			def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntity.attributeSet.id order by eav.sortOrder ")
			def attributeOptions
			def options
			def frontEndLabel
			def dependentAssets
			def supportAssets
			// def assetComment
				
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
			
			dependentAssets = AssetDependency.findAll("from AssetDependency as a where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntity])
			supportAssets 	= AssetDependency.findAll("from AssetDependency as a where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntity])
		
			def prefValue= userPreferenceService.getPreference("showAllAssetTasks") ?: 'FALSE'
			
			//field importance styling for respective validation.
			def validationType = assetEntity.validation
			def configMap = assetEntityService.getConfig('AssetEntity',validationType)
			
			def assetCommentList = AssetComment.findAllByAssetEntity(assetEntity)
			def paramsMap = [label:frontEndLabel, assetEntity:assetEntity,
				supportAssets:supportAssets, dependentAssets:dependentAssets, 
				redirectTo:params.redirectTo, project:project,
				assetCommentList:assetCommentList,
				dependencyBundleNumber:AssetDependencyBundle.findByAsset(assetEntity)?.dependencyBundle ,
				 prefValue:prefValue, config:configMap.config, customs:configMap.customs]
		
			if(params.redirectTo == "roomAudit") {
				paramsMap << [source:params.source, assetType:params.assetType]
				render(template:"auditDetails",model:paramsMap)
			}
			return paramsMap
		}
	}
	/**
	 * Used to set showAllAssetTasks preference , which is used to show all or hide the inactive tasks
	 */
	def setShowAllPreference={
		userPreferenceService.setPreference("showAllAssetTasks", params.selected=='1' ? 'TRUE' : 'FALSE')
		render true
	}
	def edit ={
		def assetEntityInstance = AssetEntity.get(params.id)
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')

		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute,[sort:"value"])
		def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetEntityInstance.assetType])?.manufacturer
		def models=[]
		models=getModelSortedByStatus(assetEntityInstance.manufacturer)


		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)

		def moveBundleList = MoveBundle.findAllByProject(project)

		def railTypeAttribute = EavAttribute.findByAttributeCode('railType')
		def railTypeOption = EavAttributeOption.findAllByAttribute(railTypeAttribute)
		
		//fieldImportance Styling for default validation.
		def validationType = assetEntityInstance.validation
		def configMap = assetEntityService.getConfig('AssetEntity',validationType)
		
		def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by a.dependent.assetType,a.dependent.assetName asc",[assetEntityInstance])
		def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by a.asset.assetType,a.asset.assetName asc",[assetEntityInstance])
		
		def planStatusOptions = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.STATUS_OPTION)
		def priorityOption = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.PRIORITY_OPTION)

		def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
		def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
		def servers = AssetEntity.findAll("from AssetEntity where assetType in ('Server','VM','Blade') and project =$projectId order by assetName asc")
		
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
		
		def nonNetworkTypes = [AssetType.SERVER.toString(),AssetType.APPLICATION.toString(),AssetType.VM.toString(),
			AssetType.FILES.toString(),AssetType.DATABASE.toString(),AssetType.BLADE.toString()]
		
		def paramsMap = [assetEntityInstance:assetEntityInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
							planStatusOptions:planStatusOptions?.value, projectId:projectId, project: project, railTypeOption:railTypeOption?.value,
							priorityOption:priorityOption?.value,dependentAssets:dependentAssets,supportAssets:supportAssets,
							manufacturers:manufacturers, models:models,redirectTo:params?.redirectTo, dependencyType:dependencyType,
							dependencyStatus:dependencyStatus,servers:servers, sourceChassisSelect:sourceChassisSelect, 
							targetChassisSelect:targetChassisSelect, nonNetworkTypes:nonNetworkTypes, config:configMap.config, customs:configMap.customs]
		
		if(params.redirectTo == "roomAudit") {
			def rooms = Room.findAllByProject(project)
					paramsMap << ['rooms':rooms, 'source':params.source,'assetType':params.assetType]
			render(template:"auditEdit",model:paramsMap)
		}
		
		return paramsMap
	}

	def update={
		
		def attribute = session.getAttribute('filterAttr')
		def filterAttr = session.getAttribute('filterAttributes')
		def redirectTo = params.redirectTo
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def formatter = new SimpleDateFormat("MM/dd/yyyy")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def maintExpDate = params.maintExpDate
		def modelName = params.models
		def manufacturerName = params.manufacturers
		def assetType = params.assetType ?: 'Server'
		
		if(params.("manufacturer.id") && params.("manufacturer.id").isNumber())
			userPreferenceService.setPreference("lastManufacturer", Manufacturer.read(params.manufacturer.id)?.name)
			
		userPreferenceService.setPreference("lastType", assetType)
		
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
		if( manufacturerName ){
			params.manufacturer = assetEntityAttributeLoaderService.getdtvManufacturer( manufacturerName )
			params.model = assetEntityAttributeLoaderService.findOrCreateModel(manufacturerName, modelName, assetType)
		}
		if(!params.SourceRoom)
			params.roomSource=null
			
		if(!params.TargetRoom)
			params.roomTarget=null
		
		if(!params.sourceRack)
			params.rackSource=null
		
		if(!params.TargetRack)
			params.rackTarget=null
		
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
						session.setAttribute("USE_FILTERS","true")
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
					case "roomAudit":
						forward(action:'show', params:[redirectTo:redirectTo, source:params.source, assetType:params.assetType])
						break;
					case "dependencyConsole":
				        forward(action:'getLists', params:[entity: params.tabType,labelsList:params.labels,dependencyBundle:session.getAttribute("dependencyBundle")])
						break;
					case "listTask":
						render "Asset ${assetEntityInstance.assetName} updated."
						break;
					default:
						session.AE?.JQ_FILTERS = params
						redirect( action:list)
	
				}
			}
		}
		else {
			flash.message = "Asset not Updated"
			assetEntityInstance.errors.allErrors.each{ flash.message += it }
			session.AE?.JQ_FILTERS = params
			redirect( action:list)

		}


	}

	def getManufacturersList = {
		def assetType = params.assetType
		def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetType])?.manufacturer
		def prefVal =  userPreferenceService.getPreference("lastManufacturer")
		def selectedManu = prefVal ? Manufacturer.findByName( prefVal )?.id : null
		render (view :'manufacturerView' , model:[manufacturers : manufacturers, selectedManu:selectedManu,forWhom:params.forWhom ])
	}
	def getModelsList = {
		def manufacturer = params.manufacturer
		def models=[]
		if(manufacturer!="null"){
			def manufacturerInstance = Manufacturer.read(manufacturer)
			models=getModelSortedByStatus(manufacturerInstance)
		}
		render (view :'ModelView' , model:[models : models, forWhom:params.forWhom])
	}
	
	/**
	 * 
	 * @param manufacturer
	 * @return
	 */
	def getModelSortedByStatus(manufacturerInstance){
		def models= []
		def modelListFull = Model.findAllByManufacturerAndModelStatus( manufacturerInstance,'full',[sort:'modelName',order:'asc'] )
		def modelListValid =Model.findAllByManufacturerAndModelStatus(manufacturerInstance,'valid',[sort:'modelName',order:'asc'] )
		def modelListNew = Model.findAllByManufacturerAndModelStatusInList(manufacturerInstance,['new','',null],[sort:'modelName',order:'asc'] )
		models = modelListFull+modelListValid+modelListNew
		
		return models
	}
	
	/**
	 * Used to generate the List for Task Manager, which leverages a shared closure with listComment
	 */
	def listTasks = {
		params.commentType=AssetCommentType.TASK
		
		if(params.initSession)
			session.TASK = [:]
			
		def project = securityService.getUserCurrentProject()
		def moveEvents = MoveEvent.findAllByProject(project)
		def filters = session.TASK?.JQ_FILTERS
		
		// Deal with the parameters
		def isTask = AssetCommentType.TASK
		
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
		def timeToRefresh = userPreferenceService.getPreference("TASKMGR_REFRESH")
		def entities = assetEntityService.entityInfo( project )
		
		return [timeToUpdate : timeToRefresh ?: 60,servers:entities.servers, applications:entities.applications, dbs:entities.dbs,
				files:entities.files,networks:entities.networks, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus, assetDependency: new AssetDependency(),
				moveEvents:moveEvents, filterEvent:filterEvent , justRemaining:justRemaining, justMyTasks:justMyTasks, filter:params.filter,
				comment:filters?.comment ?:'', taskNumber:filters?.taskNumber ?:'', assetName:filters?.assetEntity ?:'', assetType:filters?.assetType ?:'',
				dueDate : filters?.dueDate ?:'', status : filters?.status ?:'', assignedTo : filters?.assignedTo ?:'', role: filters?.role ?:'',
				category: filters?.category ?:'', moveEvent:moveEvent, 
				staffRoles:taskService.getTeamRolesForTasks(), 
//				staffRoles:taskService.getRolesForStaff(), 
				sizePref:userPreferenceService.getPreference("assetListSize")?: '25']
	}

	/**
	 * Used to generate the List of Comments, which leverages a shared closeure with the above listTasks controller
	 */
	def listComment = {
			def project = securityService.getUserCurrentProject()
			def entities = assetEntityService.entityInfo( project )
		    return [ rediectTo:'comment', servers:entities.servers, applications:entities.applications, dbs:entities.dbs,
				files:entities.files, dependencyType:entities.dependencyType, dependencyStatus:entities.dependencyStatus, assetDependency: new AssetDependency(),
				]
	}
	
	/**
	 * Used to generate list of comments using jqgrid
	 * @return : list of tasks as JSON
	 */
	def listCommentJson ={
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
			[ cell: ['',it.comment, 
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
	def listTaskJSON ={
		
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

	    def dates = params.dueDate ? AssetComment.findAll("from AssetComment where project =:project \
			and dueDate like '%${params.dueDate}%'",[project:project])?.dueDate : []

		def assigned = params.assignedTo ? Person.findAllByFirstNameIlikeOrLastNameIlike("%${params.assignedTo}%","%${params.assignedTo}%" ) : []

		
		def tasks = AssetComment.createCriteria().list(max: maxRows, offset: rowOffset) {
			eq("project", project)
			eq("commentType", AssetCommentType.TASK)
			createAlias("assetEntity","ae")
			if (params.comment)
				ilike('comment', "%${params.comment}%")
			if (params.status)
				ilike('status', "%${params.status}%")
			if (params.role)
				ilike('role', "%${params.role}%")
			if (params.category)
				ilike('category', "%${params.category}%")
			if (params.assetType)
					ilike('ae.assetType',"%${params.assetType}%")
			if (params.assetName)
					ilike('ae.assetName',"%${params.assetName}%")
			if (taskNumbers)
				'in'('taskNumber' , taskNumbers)
			if (dates) {
				and {
					or {
						'in'('dueDate' , dates)
						'in'('estFinish', dates)
					}
				}
			}
			if (assigned )
				'in'('assignedTo' , assigned)
				
			def sid = sortIndex  =='assetName' || sortIndex  =='assetType' ? "ae.${sortIndex}" : sortIndex
			if(sortIndex && sortOrder){
				order(sid, sortOrder).ignoreCase()
			} else {
				and{
					order('score','desc')
					order('taskNumber','asc')
					order('dueDate','asc')
					order('dateCreated','desc')
				}
			}
			if(moveEvent)
				eq('moveEvent', moveEvent)
				
			if (params.justRemaining == "1") {
				ne("status", AssetCommentStatus.COMPLETED)
			}
			if (params.justMyTasks == "1") {
				eq("assignedTo",person)
			}
			switch(params.filter){
				case "openIssue" :
					eq('category',"discovery" )
					break;
				case "dueOpenIssue":
					eq('category',"discovery" )
					lt('dueDate',today)
					break;
				case "analysisIssue" :
					eq("status", AssetCommentStatus.READY)
					'in'('category', ['general','planning'])
					break;
				case "generalOverDue" :
					'in'('category', ['general','planning'])
					 lt('dueDate',today)
					 break;
			}
		}

		def totalRows = tasks.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)
		def updatedTime
		def updatedClass
		def dueClass
		def nowGMT = TimeUtil.nowGMT()
		def results = tasks?.collect { 
			updatedTime =  it.isRunbookTask() ? it.statusUpdated : it.lastUpdated
			
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
			def assignedTo  = (it.assignedTo ? it.assignedTo.toString() : '')
			
			def dueDate='' 
			if (it.isRunbookTask()) {
				dueDate = it.estFinish ? runBookFormatter.format(TimeUtil.convertInToUserTZ(it.estFinish, tzId)) : ''
			} else {
				dueDate = it.dueDate ? dueFormatter.format(TimeUtil.convertInToUserTZ(it.dueDate, tzId)) : ''
			}

			def depCount = TaskDependency.countByPredecessor( it )
			// Have the dependency count be a link to the Task Neighborhood graph if there are dependencies
			def nGraphUrl = depCount == 0 ? depCount : '<a href="' + HtmlUtil.createLink([controller:'task', action:'neighborhoodGraph', id:it.id]) +
				'" target="_blank",>' + depCount + '</a>'

			[ cell: [
				'',
				it.taskNumber, 
				it.comment, 
				it.assetEntity?.assetName, it.assetEntity?.assetType,
				updatedTime ? TimeUtil.ago(updatedTime, TimeUtil.nowGMT()) : '',
				dueDate,
				it.status ?: '', it.hardAssigned ? '*'+assignedTo : '' + assignedTo,
				it.role, it.category, 
				nGraphUrl, 
				it.score ?: 0,
				it.status ? "task_${it.status.toLowerCase()}" : 'task_na',
				updatedClass, dueClass, it.assetEntity?.id
				], 
				id:it.id,
			]}
		
		// If sessions variables exists, set them with params and sort
		session.TASK?.JQ_FILTERS = params
		session.TASK?.JQ_FILTERS?.sidx = sortIndex
		session.TASK?.JQ_FILTERS?.sord = sortOrder
		
		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
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

		moveBundleList.each{ moveBundle->
			def assetCount = AssetEntity.countByMoveBundleAndAssetTypeNotInList( moveBundle,["Application","Database","Files"], params )
			def applicationCount = Application.countByMoveBundle(moveBundle)
			def databaseCount = Database.countByMoveBundle(moveBundle)
			def filesCount = Files.countByMoveBundle(moveBundle)
			assetSummaryList << ["name":moveBundle, "assetCount":assetCount, "applicationCount":applicationCount, 
				"databaseCount":databaseCount, "filesCount":filesCount, id:moveBundle.id]
		}
		
		def unassignedAssetCount = AssetEntity.executeQuery("SELECT COUNT(*) FROM AssetEntity WHERE moveBundle = null \
						AND project = $projectId AND assetType NOT IN ('Application','Database','Files')")[0]
		def unassignedAppCount = Application.executeQuery("SELECT COUNT(*) FROM Application WHERE moveBundle = null AND project = $projectId ")[0]
		def unassignedDBCount = Database.executeQuery("SELECT COUNT(*) FROM Database WHERE moveBundle = null AND project = $projectId ")[0]
		def unassignedFilesCount = Files.executeQuery("SELECT COUNT(*) FROM Files WHERE moveBundle = null AND project = $projectId ")[0]

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
			graphData << ["friction":friction]
			
			if (assetDependentlist.size()<30) {
				force = ApplicationConstants.graphDefaultSmallMap["force"]
				distance = ApplicationConstants.graphDefaultSmallMap["linkdistance"]
				width =  ApplicationConstants.graphDefaultSmallMap["width"]
				height = ApplicationConstants.graphDefaultSmallMap["height"]
				graphData << ApplicationConstants.graphDefaultSmallMap
		    } else if(assetDependentlist.size()<200) {
				force = ApplicationConstants.graphDefaultMediumMap["force"]
				distance = ApplicationConstants.graphDefaultMediumMap["linkdistance"]
				width =  ApplicationConstants.graphDefaultMediumMap["width"]
				height = ApplicationConstants.graphDefaultMediumMap["height"]
				graphData << ApplicationConstants.graphDefaultMediumMap
		    } else {
				force = ApplicationConstants.graphDefaultLargeMap["force"]
				distance = ApplicationConstants.graphDefaultLargeMap["linkdistance"]
				width =  ApplicationConstants.graphDefaultLargeMap["width"]
				height = ApplicationConstants.graphDefaultLargeMap["height"]
				graphData << ApplicationConstants.graphDefaultLargeMap
		    }
			def graphNodes = []
			
			
			
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
				} else if(['VM','Server','Blade'].contains(it.asset.assetType)){
					if(labels.contains("servers"))
						name = it.asset.assetName
					shape = "square"
				} else if(it.asset.assetType == 'Database'){
					if(labels.contains("databases"))
						name = it.asset.assetName
					shape = "triangle-up"
				} else if(it.asset.assetType == 'Files'){
					if(labels.contains("files"))
						name = it.asset.assetName
					shape = "diamond"
				}else if(it.asset.assetType == 'Network'){
					if(labels.contains("networks"))
						name = it.asset.assetName
					shape = "cross"
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
			} else if(['VM','Server','Blade'].contains(it.asset.assetType)){
				if(labels.contains("servers"))
					name = it.asset.assetName
				shape = "square"
			} else if(it.asset.assetType == 'Database'){
				if(labels.contains("databases"))
					name = it.asset.assetName
				shape = "triangle-up"
			} else if(it.asset.assetType == 'Files'){
				if(labels.contains("files"))
					name = it.asset.assetName
				shape = "diamond"
			} else if(it.asset.assetType == 'Network'){
					if(labels.contains("networks"))
						name = it.asset.assetName
					shape = "cross"
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
		def assetList = params.id.split(",")
		def assetNames = []
		assetList.each{assetId->
		    def assetEntity = AssetEntity.get( assetId )
			if(assetEntity) {
				assetNames.add(assetEntity.assetName)
				assetEntityService.deleteAsset(assetEntity)
				assetEntity.delete()
			}
		}
        def names = assetNames.toString().replace('[','').replace(']','')
	  render"AssetEntity ${names} deleted"
	}
	
	def getWorkflowTransition={
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
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
		def selectControl = ''
		if(workFlowTransition.size()){
			def paramsMap = [selectId:'workFlowId', selectName:'workFlow', options:workFlowTransition, firstOption : [value:'', display:''],
                            optionKey:'id', optionValue:'name', optionSelected:assetComment?.workflowTransition?.id]
			selectControl = HtmlUtil.generateSelect( paramsMap )
		}
		render selectControl
	}
    
	/**
	 * Provides a SELECT control with Staff associated with a project and the assigned staff selected if task id included
	 * @param forView - The CSS ID for the SELECT control
	 * @param id - the id of the existing task (aka comment)
	 * @return HTML select of staff belongs to company and TDS
	 * 
	 */
	def updateAssignedToSelect = {
		
		// TODO : Need to refactor this function to use the new TaskService.assignToSelectHtml method
		
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

		def projectStaff = partyRelationshipService.getProjectStaff( projectId )
		
		// Now morph the list into a list of name: Role names
		def list = []
		projectStaff.each {
			list << [ id:it.staff.id, 
				nameRole:"${it.role.description.split(':')[1]?.trim()}: ${it.staff.firstName} ${it.staff.lastName}",
				sortOn:"${it.role.description.split(':')[1]?.trim()},${it.staff.firstName} ${it.staff.lastName}"
			]
		}
		list.sort { it.sortOn }
		
		def firstOption = [value:'0', display:'Unassigned']
		def paramsMap = [selectId:viewId, selectName:viewId, options:list, 
			optionKey:'id', optionValue:'nameRole', 
			optionSelected:selectedId, firstOption:firstOption ]
		def assignedToSelect = HtmlUtil.generateSelect( paramsMap )
		render assignedToSelect
	}
	/**
	 * Generates an HTML SELECT control for the AssetComment.status property according to user role and current status of AssetComment(id)
	 * @param	params.id	The ID of the AssetComment to generate the SELECT for
	 * @return render HTML
	 */
	def updateStatusSelect = {
	
		//Changing code to populate all select options without checking security roles.
		def mapKey = 'ALL'//securityService.hasRole( ['ADMIN','SUPERVISOR','CLIENT_ADMIN','CLIENT_MGR'] ) ? 'ALL' : 'LIMITED'
		def optionForRole = statusOptionForRole.get(mapKey)
		def taskId = params.id
		def status = taskId ? (AssetComment.read(taskId)?.status?: '*EMPTY*') : AssetCommentStatus.READY
		def optionList = optionForRole.get(status)
		def firstOption = [value:'', display:'Please Select']
		def selectId = taskId ? "statusEditId" : "statusCreateId"
		def optionSelected = taskId ? (status != '*EMPTY*' ? status : 'na' ): AssetCommentStatus.READY
		def paramsMap = [selectId:selectId, selectName:'statusEditId', selectClass:"task_${optionSelected.toLowerCase()}",
			javascript:"onChange='this.className=this.options[this.selectedIndex].className'", 
			options:optionList, optionSelected:optionSelected, firstOption:firstOption,
			optionClass:""]
		def statusSelect = HtmlUtil.generateSelect( paramsMap )
		
		render statusSelect
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
	 * @param commentId - the comment (aka task) that the predecessor will use
	 * @param category - comment category to filter the list of tasks by
	 * @return String - HTML Select of prdecessor list
	 */
	def predecessorSelectHtml = {
		def project = securityService.getUserCurrentProject()
		def projectId = project.id
		def task
		
		if (params.commentId) { 
			task = AssetComment.findByIdAndProject(params.commentId, project)
			if ( ! task ) {
				log.warn "predecessorSelectHtml - Unable to find task id ${params.commentId} in project $project.id"
			}
		}

		def selectControl = taskService.genSelectForPredecessors(project, params.category, task, params.forWhom)
	    
		render selectControl
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
		def formatter = new SimpleDateFormat("yyyy/MM/dd")
		def today = formatter.format(new Date())
		try{
			def filePath = "/templates/TDS-Storage-Inventory.xls"
			def filename = "${project.name}SpecialExport-${today}"
			def book = ExportUtil.workBookInstance(filename, filePath, response) 
			def spcExpSheet = book.getSheet("SpecialExport")
			def storageInventoryList = assetEntityService.getSpecialExportData( project )
			def spcColumnList = ["server_id", "app_id", "server_name", "server_type", "app_name", "tru", "tru2", "move_bundle", "move_date",
									"group_id", "storage_inventory", "dr_tier", "status" ]
			
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
			return
		}
	 return
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
     * This action is used to populate support and dependent asset for asset edit.
     * @param id : asset id
     * @return : HTML code containing support and dependent edit form .
     */
	def populateDependency ={
		def returnMap = [:]
		if(params.id && params.id.isNumber()){
			def assetEntity = AssetEntity.read( params.id )
			if( assetEntity ){
				def dependentAssets = AssetDependency.findAll("from AssetDependency as a  where asset = ? order by \
					a.dependent.assetType,a.dependent.assetName asc",[assetEntity])
				def supportAssets = AssetDependency.findAll("from AssetDependency as a  where dependent = ? order by \
					a.asset.assetType,a.asset.assetName asc",[assetEntity])
		
				def assetsMap = [(AssetType.APPLICATION.toString()) : getAssetsByType(AssetType.APPLICATION.toString()), (AssetType.DATABASE.toString()) : getAssetsByType(AssetType.DATABASE.toString()),
					(AssetType.FILES.toString()):getAssetsByType(AssetType.FILES.toString()), (AssetType.SERVER.toString()):getAssetsByType(AssetType.SERVER.toString()),
					(AssetType.NETWORK.toString()):getAssetsByType(AssetType.NETWORK.toString()) ]
				
				def nonNetworkTypes = [AssetType.SERVER.toString(),AssetType.APPLICATION.toString(),AssetType.VM.toString(),
					AssetType.FILES.toString(),AssetType.DATABASE.toString(),AssetType.BLADE.toString()]
				
				def dependencyType = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
				def dependencyStatus = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
				
				returnMap = [dependentAssets:dependentAssets, supportAssets:supportAssets, assetsMap:assetsMap,
								nonNetworkTypes:nonNetworkTypes, dependencyType:dependencyType, dependencyStatus:dependencyStatus]
			}
		}
		render(template: 'dependent', model:returnMap)
	}
	
	/**
	 * This method is used to get assets by asset type
	 * @param assetType
	 * @return
	 */
	def getAssetsByType(assetType){
		def entities = []
		def project = securityService.getUserCurrentProject()
		if(assetType){
			if(assetType=='Server' || assetType=='Blade' || assetType=='VM'){
			  entities = AssetEntity.findAll('from AssetEntity where assetType in (:type) and project = :project order by assetName asc ',
				  [type:["Server", "VM", "Blade"], project:project])
			} else if(assetType != 'Application' && assetType != 'Database' && assetType != 'Files'){
			  entities = AssetEntity.findAll('from AssetEntity where assetType not in (:type) and project = :project order by assetName asc ',
				  [type:["Server", "VM", "Blade", "Application", "Database", "Files"], project:project])
			} else{
			  entities = AssetEntity.findAll('from AssetEntity where assetType = ? and project = ? order by assetName asc ',[assetType, project])
			}
		}
	  return entities
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
	  * This method is used to get custom list.
	  * @param type,validation
	  * @return
	  */
	 def getCustoms ={
		 def assetEntityInstance = params.id ? AssetEntity.get(params.id): new AssetEntity(appOwner:'')
		 def assetType = params.type
		 def validation = params.validation
		 def configMap = assetEntityService.getConfig(assetType,validation)
		 render (template:'customEdit', model:[project:configMap.project, customs:configMap.customs, assetEntityInstance:assetEntityInstance])
	 }
	
}
