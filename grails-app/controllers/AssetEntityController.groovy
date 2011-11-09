import grails.converters.JSON

import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat

import jxl.*
import jxl.read.biff.*
import jxl.write.*
import net.tds.util.jmesa.AssetEntityBean

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
import com.tds.asset.AssetEntity
import com.tds.asset.AssetEntityVarchar
import com.tds.asset.AssetTransition
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdssrc.eav.*
import com.tdssrc.grails.GormUtil

class AssetEntityController {

	def missingHeader = ""
	def added = 0
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

	protected static customLabels = ['Custom1','Custom2','Custom3','Custom4','Custom5','Custom6','Custom7','Custom8']
	protected static bundleMoveAndClientTeams = ['sourceTeamMt','sourceTeamLog','sourceTeamSa','sourceTeamDba','targetTeamMt','targetTeamLog','targetTeamSa','targetTeamDba']
	protected static targetTeamType = ['MOVE_TECH':'targetTeamMt', 'CLEANER':'targetTeamLog','SYS_ADMIN':'targetTeamSa',"DB_ADMIN":'targetTeamDba']
	protected static sourceTeamType = ['MOVE_TECH':'sourceTeamMt', 'CLEANER':'sourceTeamLog','SYS_ADMIN':'sourceTeamSa',"DB_ADMIN":'sourceTeamDba']
	protected static teamsByType = ["MOVE":"'MOVE_TECH','CLEANER'","ADMIN":"'SYS_ADMIN','DB_ADMIN'"]
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
		def projectId = params.projectId
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
		def dataTransferAttributeMap = DataTransferAttributeMap.findAllByDataTransferSet( dataTransferSetInstance )
		try {
			projectId = params["projectIdImport"]
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
		def sheet
		def titleSheet
		def sheetColumnNames = [:]
		def sheetNameMap = [:]
		def list = new ArrayList()
		Date exportTime
		def dataTransferAttributeMapSheetName
		//get column name and sheets
		dataTransferAttributeMap.eachWithIndex { item, pos ->
			if(customLabels.contains( item.columnName )){
				def customLabel = project[item.eavAttribute?.attributeCode] ? project[item.eavAttribute?.attributeCode] : item.columnName
				list.add( customLabel )
			} else {
				list.add( item.columnName )
			}
			sheetNameMap.put( "sheetName", (item.sheetName).trim() )
		}
		try {
			workbook = Workbook.getWorkbook( file.inputStream )
			def sheetNames = workbook.getSheetNames()
			def flag = 0
			def sheetNamesLength = sheetNames.length
			for( int i=0;  i < sheetNamesLength; i++ ) {
				if ( sheetNameMap.containsValue(sheetNames[i].trim()) ) {
					flag = 1
					sheet = workbook.getSheet( sheetNames[i] )
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

			}else {
				flag = 1
			}
			if( flag == 0 ) {
				flash.message = " Sheet not found, Please check it."
				redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
				return;
			} else {
				//check for column
				def col = sheet.getColumns()
				for ( int c = 0; c < col; c++ ) {
					def cellContent = sheet.getCell( c, 0 ).contents
					sheetColumnNames.put(cellContent, c)
				}
				def checkCol = checkHeader( list, sheetColumnNames )
				// Statement to check Headers if header are not found it will return Error message
				if ( checkCol == false ) {
					missingHeader = missingHeader.replaceFirst(",","")
					flash.message = " Column Headers : ${missingHeader} not found, Please check it."
					redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
					return;
				} else {
					//get user name.
					def subject = SecurityUtils.subject
					def principal = subject.principal
					def userLogin = UserLogin.findByUsername( principal )
					//Add Data to dataTransferBatch.
					def dataTransferBatch = new DataTransferBatch()
					dataTransferBatch.statusCode = "PENDING"
					dataTransferBatch.transferMode = "I"
					dataTransferBatch.dataTransferSet = dataTransferSetInstance
					dataTransferBatch.project = project
					dataTransferBatch.userLogin = userLogin
					dataTransferBatch.exportDatetime = GormUtil.convertInToGMT( exportTime, tzId )
					if(dataTransferBatch.save()){
						session.setAttribute("BATCH_ID",dataTransferBatch.id)
						def eavAttributeInstance
						def colNo = 0
						for (int index = 0; index < col; index++) {
							if(sheet.getCell( index, 0 ).contents == "Server"){
								colNo = index
							}
						}
						def sheetrows = sheet.rows
						def assetsCount = 0
						for (int row = 1; row < sheetrows; row++) {
							def server = sheet.getCell( colNo, row ).contents
							if(server){
								assetsCount = row
							}
						}
						session.setAttribute("TOTAL_ASSETS",assetsCount)
						for ( int r = 1; r < sheetrows ; r++ ) {
							def server = sheet.getCell( colNo, r ).contents
							if(server){
								def dataTransferValueList = new StringBuffer()
								for( int cols = 0; cols < col; cols++ ) {
									def dataTransferAttributeMapInstance
									def projectCustomLabel = projectCustomLabels[sheet.getCell( cols, 0 ).contents.toString()]
									if(projectCustomLabel){
										dataTransferAttributeMapInstance = dataTransferAttributeMap.find{it.columnName == projectCustomLabel}
									} else {
										dataTransferAttributeMapInstance = dataTransferAttributeMap.find{it.columnName == sheet.getCell( cols, 0 ).contents}
									}

									//dataTransferAttributeMapInstance = DataTransferAttributeMap.findByColumnName(sheet.getCell( cols, 0 ).contents)
									if( dataTransferAttributeMapInstance != null ) {
										def assetId
										if( sheetColumnNames.containsKey("assetId") && (sheet.getCell( 0, r ).contents != "") ) {
											try{
												assetId = Integer.parseInt(sheet.getCell( 0, r ).contents)
											} catch( NumberFormatException ex ) {
												flash.message = "AssetId should be Integer"
												redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
												return;
											}
										}
										def dataTransferValues = "("+assetId+",'"+sheet.getCell( cols, r ).contents.replace("'","\\'")+"',"+r+","+dataTransferBatch.id+","+dataTransferAttributeMapInstance.eavAttribute.id+")"
										dataTransferValueList.append(dataTransferValues)
										dataTransferValueList.append(",")
										/*dataTransferValue = new DataTransferValue()
										 eavAttributeInstance = dataTransferAttributeMapInstance.eavAttribute
										 dataTransferValue.importValue = sheet.getCell( cols, r ).contents
										 dataTransferValue.rowId = r
										 dataTransferValue.dataTransferBatch = dataTransferBatch
										 dataTransferValue.eavAttribute = eavAttributeInstance
										 if( sheetColumnNames.containsKey("assetId") && (sheet.getCell( 0, r ).contents != "") ) {
										 dataTransferValue.assetEntityId = Integer.parseInt(sheet.getCell( 0, r ).contents)
										 }
										 if ( dataTransferValue.save() ) {
										 added = r
										 } else {
										 skipped += ( r +1 )
										 }
										 */
									}
								}
								try{
									jdbcTemplate.update("insert into data_transfer_value( asset_entity_id, import_value,row_id, data_transfer_batch_id, eav_attribute_id ) values "+dataTransferValueList.toString().substring(0,dataTransferValueList.lastIndexOf(",")))
									added = r
								} catch (Exception e) {
									skipped += ( r +1 )
								}
							}
							if (r%50 == 0){
								sessionFactory.getCurrentSession().flush();
								sessionFactory.getCurrentSession().clear();
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
					}

				} // generate error message
				workbook.close()
				if (skipped.size() > 0) {
					flash.message = " File Uploaded Successfully with ${added} records. and  ${skipped} Records skipped Please click the Manage Batches to review and post these changes."
				} else {
					flash.message = " File uploaded successfully with ${added} records.  Please click the Manage Batches to review and post these changes."
				}
				redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
				return;
			}
		} catch( NumberFormatException ex ) {
			flash.message = ex
			redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
			return;
		} catch( Exception ex ) {
			flash.message = grailsApplication.metadata[ 'app.file.format' ]
			redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
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
		def projectId = params[ "projectIdExport" ]
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
		def dataTransferAttributeMap = DataTransferAttributeMap.findAllByDataTransferSet( dataTransferSetInstance )
		def project = Project.findById( projectId )
		if ( projectId == null || projectId == "" ) {
			flash.message = " Project Name is required. "
			redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
			return;
		}
		def asset
		def assetEntityInstance
		if(bundle[0] == "" ) {
			asset = AssetEntity.findAllByProject( project )
		} else {
			asset = AssetEntity.findAll( "from AssetEntity m where m.project = project and m.moveBundle in ( $bundleList )" )
		}
		//get template Excel
		def workbook
		def book
		try {
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
			response.setHeader( "Content-Disposition", "attachment; filename= ${exportType}-${filename}.xls" )
			//create workbook and sheet
			book = Workbook.createWorkbook( response.getOutputStream(), workbook )
			def sheet
			def titleSheet
			//check for column
			def map = [:]
			def sheetColumnNames = [:]
			def columnNameList = new ArrayList()
			def sheetNameMap = [:]
			def dataTransferAttributeMapSheetName
			//get columnNames in to map
			dataTransferAttributeMap.eachWithIndex { item, pos ->
				map.put( item.columnName, null )
				columnNameList.add(item.columnName)
				sheetNameMap.put( "sheetName", (item.sheetName).trim() )
			}
			def sheetNames = book.getSheetNames()
			def flag = 0
			def sheetNamesLength = sheetNames.length
			for( int i=0;  i < sheetNamesLength; i++ ) {
				if ( sheetNameMap.containsValue( sheetNames[i].trim()) ) {
					flag = 1
					sheet = book.getSheet( sheetNames[i] )
				}
			}
			if( flag == 0 ) {
				flash.message = " Sheet not found, Please check it."
				redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
				return;
			} else {
				def col = sheet.getColumns()
				for ( int c = 0; c < col; c++ ) {
					def cellContent = sheet.getCell( c, 0 ).contents
					sheetColumnNames.put(cellContent, c)
					if( map.containsKey( cellContent ) ) {
						map.put( cellContent, c )
					}
				}
				//calling method to check for Header
				def checkCol = checkHeader( columnNameList, sheetColumnNames )
				// Statement to check Headers if header are not found it will return Error message
				if ( checkCol == false ) {
					missingHeader = missingHeader.replaceFirst(",","")
					flash.message = " Column Headers : ${missingHeader} not found, Please check it."
					redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
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
					def columnNameListSize = columnNameList.size()
					// update column header
					for ( int head =0; head <= sheetColumnNames.size(); head++ ) {
						def cellData = sheet.getCell(head,0)?.getContents()
						def attributeMap = dataTransferAttributeMap.find{it.columnName ==  cellData }?.eavAttribute
						if(attributeMap?.attributeCode && customLabels.contains( cellData )){
							def columnLabel = project[attributeMap?.attributeCode] ? project[attributeMap?.attributeCode] : cellData
							def customColumn = new Label(head,0, columnLabel )
							sheet.addCell(customColumn)
						}
					}

					for ( int r = 1; r <= assetSize; r++ ) {
						//Add assetId for walkthrough template only.
						if( sheetColumnNames.containsKey("assetId") ) {
							def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
							def addAssetId = new Number(0, r, (asset[r-1].id))
							sheet.addCell( addAssetId )
						}
						for ( int coll = 0; coll < columnNameListSize; coll++ ) {
							def addContentToSheet
							def attribute = dataTransferAttributeMap.eavAttribute.attributeCode[coll]
							if ( attribute != "usize" && asset[r-1][attribute] == null ) {
								addContentToSheet = new Label( map[columnNameList.get(coll)], r, "" )
							} else if(attribute == "usize"){
								addContentToSheet = new Label(map[columnNameList.get(coll)], r, asset[r-1]?.model?.usize?.toString() ?:"" )
							}else {
								//if attributeCode is sourceTeamMt or targetTeamMt export the teamCode
								if( bundleMoveAndClientTeams.contains(dataTransferAttributeMap.eavAttribute.attributeCode[coll]) ) {
									addContentToSheet = new Label( map[columnNameList.get(coll)], r, String.valueOf(asset[r-1].(dataTransferAttributeMap.eavAttribute.attributeCode[coll]).teamCode) )
								}else {
									addContentToSheet = new Label( map[columnNameList.get(coll)], r, String.valueOf(asset[r-1].(dataTransferAttributeMap.eavAttribute.attributeCode[coll])) )
								}
							}
							sheet.addCell( addContentToSheet )
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
			}
		} catch( Exception fileEx ) {
			flash.message = "Exception occurred wile exporting Excel. "
			redirect( action:assetImport, params:[projectId:projectId, message:flash.message] )
			return;
		}
	}
	/* -------------------------------------------------------
	 * To check the sheet headers
	 * @param attributeList, SheetColumnNames
	 * @author Mallikarjun
	 * @return bollenValue 
	 *------------------------------------------------------- */  
	def checkHeader( def list, def sheetColumnNames  ) {
		def listSize = list.size()
		for ( int coll = 0; coll < listSize; coll++ ) {
			if( sheetColumnNames.containsKey( list[coll] ) ) {
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

		def projectId = params.projectId
		if(projectId == null || projectId == ""){
			projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		}
		def project = Project.findById( projectId )
		def assetEntityInstanceList = AssetEntity.findAllByProjectAndAssetTypeNotInList( project,["Application","Database","Files"], params )
		def assetEntityList =  new ArrayList()
		assetEntityInstanceList.each { assetEntity->
			AssetEntityBean assetBeanInstance = new AssetEntityBean();
			assetBeanInstance.setId(assetEntity.id)
			assetBeanInstance.setAssetName(assetEntity.assetName)
			assetBeanInstance.setAssetType(assetEntity.assetType)
			assetBeanInstance.setAssetTag(assetEntity.assetTag)
			assetBeanInstance.setModel(assetEntity.model?.modelName)
			assetBeanInstance.setSourceLocation(assetEntity.sourceLocation)
			assetBeanInstance.setSourceRack(assetEntity.rackSource?.tag)
			assetBeanInstance.setTargetLocation(assetEntity.targetLocation)
			assetBeanInstance.setTargetRack(assetEntity.rackTarget?.tag)
			assetBeanInstance.setMoveBundle(assetEntity.moveBundle?.name)
			assetBeanInstance.setSerialNumber(assetEntity.serialNumber)
			assetBeanInstance.setDepUp(AssetDependency.countByDependentAndStatusNotEqual(assetEntity, "Validated"))
			assetBeanInstance.setDepDown(AssetDependency.countByDependentAndStatusNotEqual(assetEntity, "Validated"))

			if(AssetComment.find("from AssetComment where assetEntity = ${assetEntity?.id} and commentType = ? and isResolved = ?",['issue',0])){
				assetBeanInstance.setCommentType("issue")
			} else if(AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id)){
				assetBeanInstance.setCommentType("comment")
			} else {
				assetBeanInstance.setCommentType("blank")
			}

			assetEntityList.add(assetBeanInstance)
		}
		def servers = AssetEntity.findAllByAssetTypeAndProject('Server',project)
		def applications = Application.findAllByAssetTypeAndProject('Application',project)
		def dbs = Database.findAllByAssetTypeAndProject('Database',project)
		def files = Files.findAllByAssetTypeAndProject('Files',project)
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
					applications : applications, dbs : dbs, files : files, assetDependency: new AssetDependency()]
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
		def assetEntityInstance = AssetEntity.get( params.id )
		def projectId = params.projectId ? params.projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		if(assetEntityInstance) {
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
			AssetEntity.executeUpdate("delete from AssetEntity ae where ae.id = ${assetEntityInstance.id}")

			flash.message = "AssetEntity ${assetEntityInstance.assetName} deleted"
		}
		else {
			flash.message = "AssetEntity not found with id ${params.id}"
		}
		if ( params.clientList ){
			redirect( controller:"clientConsole", action:"list", params:[projectId:projectId, moveBundle:params.moveBundleId] )
		} else if ( params.moveBundleId ) {
			redirect( action:dashboardView, params:[projectId:projectId, moveBundle:params.moveBundleId, showAll : params.showAll] )
		} else {
			redirect( action:list, params:[projectId:projectId] )
		}
	}

	/*--------------------------------------------------
	 * To remove the asseet from project
	 * @param assetEntityId
	 * @author Mallikarjun
	 * @return assetList page
	 *-------------------------------------------------*/
	def remove = {
		def assetEntityInstance = AssetEntity.get( params.id )
		def projectId = params.projectId
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
			redirect( controller:"clientConsole", action:"list", params:[projectId:projectId, moveBundle:params.moveBundleId] )
		} else if ( params.moveBundleId ){
			redirect( action:dashboardView, params:[projectId:projectId, moveBundle : params.moveBundleId, showAll : params.showAll] )
		}else{
			redirect( action:list, params:[projectId:projectId] )
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
		if(maintExpDate){
			params.maintExpDate =  GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
		}
		def retireDate = params.retireDate
		if(retireDate){
			params.retireDate =  GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
		}

		def bundleId = getSession().getAttribute( "CURR_BUNDLE" )?.CURR_BUNDLE
		def assetEntityInstance = new AssetEntity(params)
		def projectId = params.projectId ? params.projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
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
				println"Error while updating project.lastAssetId : ${projectInstance}"
				projectInstance.errors.each { println it }
			}
		}

		if(!assetEntityInstance.hasErrors() && assetEntityInstance.save()) {
			assetEntityInstance.updateRacks()

			if(assetEntityInstance.model){
				assetEntityAttributeLoaderService.createModelConnectors( assetEntityInstance )
			}
			assetEntityService.createOrUpdateAssetEntityDependencies(params, assetEntityInstance)
			flash.message = "AssetEntity ${assetEntityInstance.assetName} created"
			if(params.redirectTo == "room"){
				redirect( controller:'room',action:list, params:[projectId: projectId] )
			} else if(params.redirectTo == "rack"){
				redirect( controller:'rackLayouts',action:'create', params:[projectId: projectId] )
			} else {
				redirect( action:list, params:[projectId: projectId] )
			}
		}
		else {

			flash.message = "AssetEntity ${assetEntityInstance.assetName} not created"
			def etext = "Unable to Update Asset" +
					GormUtil.allErrorsString( assetEntityInstance )
			println etext
			if(params.redirectTo == "room"){
				redirect( controller:'room',action:list, params:[projectId: projectId] )
			} else if(params.redirectTo == "rack"){
				redirect( controller:'rackLayouts',action:'create', params:[projectId: projectId] )
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
					println etext
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
		assetCommentsInstance.each {
			assetCommentsList <<[ commentInstance : it, assetEntityId : it.assetEntity.id ]
		}
		render assetCommentsList as JSON
	}
	/* ----------------------------------------------------------------
	 * To save the Comment record
	 * @param assetComment
	 * @author Lokanath
	 * @return assetComments
	 * -----------------------------------------------------------------*/
	def saveComment = {
		def assetComments = []
		def assetCommentInstance = new AssetComment(params)
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		assetCommentInstance.createdBy = loginUser.person
		if(params.isResolved == '1'){
			assetCommentInstance.resolvedBy = loginUser.person
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			assetCommentInstance.dateResolved = GormUtil.convertInToGMT( "now", tzId )
		}
		if(!assetCommentInstance.hasErrors() && assetCommentInstance.save()) {
			def status = AssetComment.find('from AssetComment where assetEntity = ? and commentType = ? and isResolved = ?',[assetCommentInstance.assetEntity,'issue',0])
			assetComments << [assetComment : assetCommentInstance, status : status ? true : false]
		}
		render assetComments as JSON
	}
	/* ------------------------------------------------------------------------
	 * return the commet record
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
		DateFormat formatter ;
		formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def assetComment = AssetComment.get(params.id)
		if(assetComment.createdBy){
			personCreateObj = Person.find("from Person p where p.id = $assetComment.createdBy.id")
			dtCreated = formatter.format(GormUtil.convertInToUserTZ(assetComment.dateCreated, tzId));
		}
		if(assetComment.resolvedBy){
			personResolvedObj = Person.find("from Person p where p.id = $assetComment.resolvedBy.id")
			dtResolved = formatter.format(GormUtil.convertInToUserTZ(assetComment.dateResolved, tzId));
		}
		commentList<<[ assetComment:assetComment,personCreateObj:personCreateObj,
					personResolvedObj:personResolvedObj,dtCreated:dtCreated?dtCreated:"",
					dtResolved:dtResolved?dtResolved:"" ]
		render commentList as JSON
	}
	/* ------------------------------------------------------------
	 * update comments
	 * @param assetCommentId
	 * @author Lokanath
	 * @return assetComment
	 * ------------------------------------------------------------ */
	def updateComment = {
		def assetComments = []
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername(principal)
		def assetCommentInstance = AssetComment.get(params.id)
		if(params.isResolved == '1' && assetCommentInstance.isResolved == 0 ){
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			assetCommentInstance.resolvedBy = loginUser.person
			assetCommentInstance.dateResolved = GormUtil.convertInToGMT( "now", tzId )
		}else if(params.isResolved == '1' && assetCommentInstance.isResolved == 1){
		}else{
			assetCommentInstance.resolvedBy = null
			assetCommentInstance.dateResolved = null
		}
		assetCommentInstance.properties = params

		if(!assetCommentInstance.hasErrors() && assetCommentInstance.save(flush:true) ) {
			def status = AssetComment.find('from AssetComment where assetEntity = ? and commentType = ? and isResolved = ?',[assetCommentInstance.assetEntity,'issue',0])
			assetComments << [assetComment : assetCommentInstance, status : status ? true : false]
		}
		render assetComments as JSON
	}
	/* delete the comment record
	 * @param assetComment
	 * @author Lokanath
	 * @return assetCommentList 
	 */
	def deleteComment = {
		def assetCommentInstance = AssetComment.get(params.id)
		if(assetCommentInstance){
			assetCommentInstance.delete(flush:true)
		}
		def assetEntityInstance = AssetEntity.get( params.assetEntity )
		def assetCommentsInstance = AssetComment.findAllByAssetEntityAndIdNotEqual( assetEntityInstance, params.id )
		def assetCommentsList = []
		assetCommentsInstance.each {
			assetCommentsList <<[ commentInstance : it, assetEntityId : it.assetEntity.id ]
		}
		render assetCommentsList as JSON
	}
	/*---------------------------------------------------------------------------------------
	 *	User to get the deatails for Supervisor Console
	 * 	@author:	Lokanath Reddy
	 * 	@param :	CURR_PROJ and movebundle
	 * 	@return:	AssetEntity details and Transition details for all MoveBundle Teams
	 *--------------------------------------------------------------------------------------*/
	def dashboardView = {
		def showAll = params.showAll
		def projectId = params.projectId
		def bundleId = params.moveBundle
		def currentState = params.currentState
		def assetList
		def bundleTeams = []
		def assetsList = []
		def totalAsset = []
		def totalAssetsSize = 0
		def supportTeam = new HashMap()
		projectId = projectId ? projectId : getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def teamType = params.teamType
		teamType = teamType ? teamType : getSession().getAttribute( "CONSOLE_TEAM_TYPE" )?.CONSOLE_TEAM_TYPE
		if(!teamType){
			teamType = "MOVE"
		}
		userPreferenceService.setPreference( "CONSOLE_TEAM_TYPE", "${teamType}" )
		def projectInstance = Project.findById( projectId )
		def moveBundleInstanceList = MoveBundle.findAll("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
		def moveBundleInstance
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
		if(bundleId){
			userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
			moveBundleInstance = MoveBundle.findById(bundleId)
		} else {
			userPreferenceService.loadPreferences("CURR_BUNDLE")
			def defaultBundle = getSession().getAttribute("CURR_BUNDLE")
			if(defaultBundle?.CURR_BUNDLE){
				moveBundleInstance = MoveBundle.findById(defaultBundle.CURR_BUNDLE)
				if( moveBundleInstance?.project?.id != Integer.parseInt(projectId) ){
					moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
				}
			} else {
				moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
			}
		}
		// get the list of assets order by Hold and recent asset Transition
		if( moveBundleInstance != null ){
			//  Get Id for respective States

			def holdId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, "Hold" ) )
			def releasedId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, "Release" ) )

			def unrackedId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, "Unracked" ) )

			def cleanedId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, "Cleaned" ) )
			def onCartId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, "OnCart" ) )
			def stagedId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, "Staged" ) )

			def rerackedId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, "Reracked" ) )

			def onTruckId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, "OnTruck" ) )
			def offTruckId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, "OffTruck" ) )

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
				def swimlane = Swimlane.findByNameAndWorkflow(projectTeam.role ? projectTeam.role : "MOVE_TECH", Workflow.findByProcess(projectInstance.workflowCode) )

				def minSource = swimlane.minSource ? swimlane.minSource : "Release"
				def minSourceId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, minSource ) )

				def minTarget = swimlane.minTarget ? swimlane.minTarget : "Staged"
				def minTargetId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, minTarget ) )


				def maxSource = swimlane.maxSource ? swimlane.maxSource : "Unracked"
				def maxSourceId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, maxSource ) )

				def maxTarget = swimlane.maxTarget ? swimlane.maxTarget : "Reracked"
				def maxTargetId = Integer.parseInt( stateEngineService.getStateId( projectInstance.workflowCode, maxTarget ) )

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
				def check = true
				def curId = it.currentState

				stateVal = stateEngineService.getState( projectInstance.workflowCode, curId )
				if(stateVal){
					taskVal = stateEngineService.getTasks( projectInstance.workflowCode, "SUPERVISOR", stateVal )
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
				assetsList<<[asset: it, status: stateEngineService.getStateLabel( projectInstance.workflowCode, curId ), cssClass : cssClass, checkVal:check]
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
			def processTransitions = stateEngineService.getTasks(projectInstance.workflowCode, "TASK_ID")
			processTransitions.each{
				def stateId = Integer.parseInt( it )
				transitionStates << [state:stateEngineService.getState( projectInstance.workflowCode, stateId ),
							stateLabel:stateEngineService.getStateLabel( projectInstance.workflowCode, stateId )]
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

			return[ moveBundleInstanceList: moveBundleInstanceList, projectId:projectId, bundleTeams:bundleTeams,
				assetBeansList:assetBeansList, moveBundleInstance:moveBundleInstance, project : projectInstance,
				supportTeam:supportTeam, totalUnracked:totalUnracked, totalSourceAvail:totalSourceAvail,
				totalTargetAvail:totalTargetAvail, totalReracked:totalReracked, totalAsset:totalAssetsSize,
				timeToUpdate : timeToUpdate ? timeToUpdate.SUPER_CONSOLE_REFRESH : "never", showAll : showAll,
				applicationList : applicationList, appOwnerList : appOwnerList, appSmeList : appSmeList,
				transitionStates : transitionStates, params:params, totalAssetsOnHold:totalAssetsOnHold,
				totalSourcePending: totalSourcePending, totalTargetPending: totalTargetPending, role: role, teamType:teamType, assetDependency: new AssetDependency() ,
				servers:servers , applications:applications ,dbs:dbs,files:files]
		} else {
			flash.message = "Please create bundle to view Console"
			redirect(controller:'project',action:'show',params:["id":params.projectId])
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
				def taskLabel = stateEngineService.getStateLabel(assetDetail.project.workflowCode,Integer.parseInt(it.stateTo))
				def time = GormUtil.convertInToUserTZ(it.dateCreated, tzId ).toString().substring(11,19)
				def timeElapsed = convertIntegerIntoTime( it.timeElapsed )
				if(it.voided == 1){
					cssClass = "void_transition"
				}
				recentChanges<<[transition:time+"/"+timeElapsed+" "+taskLabel+' ('+ it.userLogin.person.lastName +')',cssClass:cssClass]
			}
			def holdId = stateEngineService.getStateId( assetDetail.project.workflowCode, "Hold" )
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

			def state = stateEngineService.getState( assetDetail.project.workflowCode, currentState )
			def validStates
			if(state){
				validStates= stateEngineService.getTasks( assetDetail.project.workflowCode, "SUPERVISOR", state )
			} else {
				validStates = ["Ready"]
				//stateEngineService.getTasks("STD_PROCESS","TASK_NAME")
			}
			validStates.each{
				stateIdList<<stateEngineService.getStateIdAsInt( assetDetail.project.workflowCode, it )
			}
			stateIdList.sort().each{
				statesList<<[id:stateEngineService.getState(assetDetail.project.workflowCode,it),label:stateEngineService.getStateLabel(assetDetail.project.workflowCode,it)]
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
			map.put("currentState",stateEngineService.getStateLabel(assetDetail.project.workflowCode,currentState))
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
		def toState = params.toState
		def fromState = params.fromState
		def status = []
		def flag = stateEngineService.getFlags(projectInstance.workflowCode,"SUPERVISOR", fromState, toState)
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
		if(assetEntity){
			def status = params.state
			def assignTo = params.assignTo
			def priority = params.priority
			def comment = params.comment
			def holdTime = params.holdTime
			def principal = SecurityUtils.subject.principal
			def loginUser = UserLogin.findByUsername(principal)
			def rerackedId = stateEngineService.getStateId(assetEntity.project.workflowCode,"Reracked")
			if(!rerackedId) {
				rerackedId = stateEngineService.getStateId(assetEntity.project.workflowCode,"Reracked")
			}
			def holdId = stateEngineService.getStateId(assetEntity.project.workflowCode,"Hold")
			def releasedId = stateEngineService.getStateId(assetEntity.project.workflowCode,"Release")
			def projectAssetMap = ProjectAssetMap.findByAsset(assetEntity)
			def currentStateId
			if(projectAssetMap){
				currentStateId = projectAssetMap.currentStateId
			}

			if(status != "" ){
				def transactionStatus = workflowService.createTransition(assetEntity.project.workflowCode,"SUPERVISOR", status, assetEntity, assetEntity.moveBundle, loginUser, null, comment )
				if ( transactionStatus.success ) {
					stateIdList = getStates(status)
					stateIdList.sort().each{
						statesList<<[id:stateEngineService.getState(assetEntity.project.workflowCode,it),label:stateEngineService.getStateLabel(assetEntity.project.workflowCode,it)]
					}
					statusLabel = stateEngineService.getStateLabel(assetEntity.project.workflowCode,stateEngineService.getStateIdAsInt(assetEntity.project.workflowCode,status))
					statusName = stateEngineService.getState(assetEntity.project.workflowCode,stateEngineService.getStateIdAsInt(assetEntity.project.workflowCode,status))
				} else {
					statusLabel = stateEngineService.getStateLabel(assetEntity.project.workflowCode,currentStateId)
					statusName = stateEngineService.getState(assetEntity.project.workflowCode,currentStateId)
					stateIdList = getStates(stateEngineService.getState(assetEntity.project.workflowCode,currentStateId))
					stateIdList.sort().each{
						statesList<<[id:stateEngineService.getState(assetEntity.project.workflowCode,it),label:stateEngineService.getStateLabel(assetEntity.project.workflowCode,it)]
					}
				}
			} else {
				statusLabel = stateEngineService.getStateLabel(assetEntity.project.workflowCode,currentStateId)
				statusName = stateEngineService.getState(assetEntity.project.workflowCode,currentStateId)
				stateIdList = getStates(stateEngineService.getState(assetEntity.project.workflowCode,currentStateId))
				stateIdList.sort().each{
					statesList<<[id:stateEngineService.getState(assetEntity.project.workflowCode,it),label:stateEngineService.getStateLabel(assetEntity.project.workflowCode,it)]
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
						'statusName':statusName, assetComment:assetComment]
		}
		render assetList as JSON
	}
	/*-----------------------------------------
	 *@param : state value
	 *@return: List of valid stated for param state
	 *----------------------------------------*/
	def getStates(def state){
		def projectInstance = Project.findById( getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ )
		def stateIdList = []
		def validStates
		if(state){
			validStates = stateEngineService.getTasks(projectInstance.workflowCode,"SUPERVISOR", state)
		} else {
			validStates = ["Ready"]
			//stateEngineService.getTasks("STD_PROCESS","TASK_NAME")
		}
		validStates.each{
			stateIdList<<stateEngineService.getStateIdAsInt(projectInstance.workflowCode,it)
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
		if(assetArray){

			def assetList = assetArray.split(",")
			assetList.each{ asset->
				def assetEntity = AssetEntity.findById(asset)
				def holdId = stateEngineService.getStateId( assetEntity.project.workflowCode, "Hold" )
				//def projectAssetMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = $asset")
				def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
						"where t.asset_entity_id = $asset and t.voided = 0 and ( t.type = 'process' or t.state_To = $holdId ) "+
						"order by t.date_created desc, stateTo desc limit 1 ")
				if(transitionStates.size()){
					stateVal = stateEngineService.getState(projectInstance.workflowCode,transitionStates[0].stateTo)
					temp = stateEngineService.getTasks(projectInstance.workflowCode,"SUPERVISOR",stateVal)
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
				tempTaskList << stateEngineService.getStateIdAsInt(projectInstance.workflowCode,it)
			}
			tempTaskList.sort().each{
				sortList << [state:stateEngineService.getState(projectInstance.workflowCode,it),label:stateEngineService.getStateLabel(projectInstance.workflowCode,it)]
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

				def workflow = workflowService.createTransition(projectInstance.workflowCode,"SUPERVISOR",params.taskList,it,bundle,loginUser,team,params.enterNote)
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
			println"$ex"
		}
		redirect(action:'dashboardView',params:[moveBundle:params.moveBundle, showAll:params.showAll,
					projectId:params.projectId] )
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
		def state = stateEngineService.getStateLabel( projectInstance.workflowCode.toString(),  stateId)
		def assetEntityInstance = AssetEntity.findById(assetId)
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
	def create ={
		def assetEntityInstance = new AssetEntity(appOwner:'TDS')

		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')
		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",["Server"])?.manufacturer

		def planStatusAttribute = EavAttribute.findByAttributeCode('planStatus')
		def planStatusOptions = EavAttributeOption.findAllByAttribute(planStatusAttribute)

		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)

		def moveBundleList = MoveBundle.findAllByProject(project)

		def railTypeAttribute = EavAttribute.findByAttributeCode('railType')
		def railTypeOption = EavAttributeOption.findAllByAttribute(railTypeAttribute)

		def priorityAttribute = EavAttribute.findByAttributeCode('priority')
		def priorityOption = EavAttributeOption.findAllByAttribute(priorityAttribute)

		[assetEntityInstance:assetEntityInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId ,railTypeOption:railTypeOption?.value,
					priorityOption:priorityOption?.value ,project:project, manufacturers:manufacturers,redirectTo:params?.redirectTo ]



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
			dependentAssets = AssetDependency.findAllByAsset(assetEntityInstance)
			supportAssets 	= AssetDependency.findAllByDependent(assetEntityInstance)
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
					dependentAssets:dependentAssets, redirectTo : params.redirectTo ,assetComment:assetComment, assetCommentList:assetCommentList]
	}
	def edit ={
		def assetEntityInstance = AssetEntity.get(params.id)
		def assetTypeAttribute = EavAttribute.findByAttributeCode('assetType')

		def assetTypeOptions = EavAttributeOption.findAllByAttribute(assetTypeAttribute)
		def manufacturers = Model.findAll("From Model where assetType = ? group by manufacturer order by manufacturer.name",[assetEntityInstance.assetType])?.manufacturer
		def models = assetEntityInstance.manufacturer ? Model.findAllByManufacturer( assetEntityInstance.manufacturer,[sort:'modelName',order:'asc'] )?.findAll{it.assetType == assetEntityInstance.assetType } : []

		def planStatusAttribute = EavAttribute.findByAttributeCode('planStatus')

		def planStatusOptions = EavAttributeOption.findAllByAttribute(planStatusAttribute)

		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ
		def project = Project.read(projectId)

		def moveBundleList = MoveBundle.findAllByProject(project)

		def railTypeAttribute = EavAttribute.findByAttributeCode('railType')
		def railTypeOption = EavAttributeOption.findAllByAttribute(railTypeAttribute)

		def priorityAttribute = EavAttribute.findByAttributeCode('priority')
		def priorityOption = EavAttributeOption.findAllByAttribute(priorityAttribute)


		def dependentAssets = AssetDependency.findAllByAsset(assetEntityInstance)
		def supportAssets = AssetDependency.findAllByDependent(assetEntityInstance)

		[assetEntityInstance:assetEntityInstance, assetTypeOptions:assetTypeOptions?.value, moveBundleList:moveBundleList,
					planStatusOptions:planStatusOptions?.value, projectId:projectId, project: project, railTypeOption:railTypeOption?.value,
					priorityOption:priorityOption?.value,dependentAssets:dependentAssets,supportAssets:supportAssets,
					manufacturers:manufacturers, models:models,redirectTo:params?.redirectTo]

	}
	def update={
		def redirectTo = params.redirectTo
		def projectId = session.getAttribute( "CURR_PROJ" ).CURR_PROJ

		def formatter = new SimpleDateFormat("MM/dd/yyyy")
		def tzId = session.getAttribute( "CURR_TZ" )?.CURR_TZ
		def maintExpDate = params.maintExpDate
		if(maintExpDate){
			params.maintExpDate =  GormUtil.convertInToGMT(formatter.parse( maintExpDate ), tzId)
		}
		def retireDate = params.retireDate
		if(retireDate){
			params.retireDate =  GormUtil.convertInToGMT(formatter.parse( retireDate ), tzId)
		}
		def assetEntityInstance = AssetEntity.get(params.id)
		assetEntityInstance.properties = params
		if(!assetEntityInstance.hasErrors() && assetEntityInstance.save(flush:true)) {
			flash.message = "Asset ${assetEntityInstance.assetName} Updated"
			assetEntityInstance.updateRacks()
			assetEntityService.createOrUpdateAssetEntityDependencies(params, assetEntityInstance)
			switch(redirectTo){
				case "room":
					redirect( controller:'room',action:list, params:[projectId: projectId] )
					break;
				case "rack":
					redirect( controller:'rackLayouts',action:'create', params:[projectId: projectId] )
					break;
				case "console":
					redirect( action:dashboardView, params:[projectId: projectId, showAll:'show'])
					break;
				case "clientConsole":
					redirect( controller:'clientConsole', action:list, params:[projectId: projectId])
					break;
				case "application":
					redirect( controller:'application', action:list, params:[projectId: projectId])
					break;
				case "database":
					redirect( controller:'database', action:list, params:[projectId: projectId])
					break;
				case "files":
					redirect( controller:'files', action:list, params:[projectId: projectId])
					break;
				default:
					redirect( action:list)
			}
		}
		else {
			flash.message = "Asset not created"
			assetEntityInstance.errors.allErrors.each{ flash.message += it }
			redirect(action:list)
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
		if(manufacturer){
			def manufacturerInstance = Manufacturer.read(manufacturer)
			models = manufacturerInstance ? Model.findAllByManufacturer( manufacturerInstance,[sort:'modelName',order:'asc'] )?.findAll{it.assetType == assetType } : null
		}
		render (view :'ModelView' , model:[models : models])
	}
}
