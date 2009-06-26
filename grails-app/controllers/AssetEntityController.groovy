import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*
import grails.converters.JSON
import org.jsecurity.SecurityUtils
import com.tdssrc.eav.*
import org.codehaus.groovy.grails.commons.ApplicationHolder
import com.tdssrc.grails.GormUtil
import java.text.DateFormat
import java.text.SimpleDateFormat
class AssetEntityController {	
    //TODO : Fix indentation
	def missingHeader = ""
	def added = 0
	def skipped = []
	def partyRelationshipService
	def stateEngineService
	def workflowService
	def userPreferenceService
	def assetEntityInstanceList = []
	def jdbcTemplate
    def filterService
    def sessionFactory 
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
        // get File
        MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
        CommonsMultipartFile file = ( CommonsMultipartFile ) mpr.getFile("file")
        // create workbook
        def workbook
        def sheet
        def sheetColumnNames = [:]
        def sheetNameMap = [:]
        def list = new ArrayList()
        def dataTransferAttributeMapSheetName
        //get column name and sheets
        dataTransferAttributeMap.eachWithIndex { item, pos ->
            list.add( item.columnName )
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
	                        		def dataTransferAttributeMapInstance = DataTransferAttributeMap.findByColumnName(sheet.getCell( cols, 0 ).contents)
	                            	if( dataTransferAttributeMapInstance != null ) {
	                            		def assetId
	                            		if( sheetColumnNames.containsKey("assetId") && (sheet.getCell( 0, r ).contents != "") ) {
	                            			assetId = Integer.parseInt(sheet.getCell( 0, r ).contents)
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
        }catch( Exception ex ) {
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
        def bundleSize = bundle.size()
        for ( int i=0; i< bundleSize ; i++ ) {
	        if( bundle[i] == "" ) {
	        } else if( i != bundleSize - 1) {
                bundleList.append( bundle[i] + "," )
            } else {
                bundleList.append( bundle[i] )
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
            // Statements to get context details
            /*def tempProtocol = request.getProtocol()
            def protocol = tempProtocol.substring(0,tempProtocol.indexOf("/"))
            def serverName = request.getServerName()
            def serverPort = request.getServerPort()
            def contextPath = request.getContextPath()
            // construct application URL
            def appUrl = protocol + "://" + serverName + ":" + serverPort + contextPath
            // get connection
            def filenametoSet = dataTransferSetInstance.templateFilename
            def templateFilePath = appUrl + filenametoSet
            def url = new URL( templateFilePath )
            HttpURLConnection con = url.openConnection()*/
            def filenametoSet = dataTransferSetInstance.templateFilename
            File file =  ApplicationHolder.application.parentContext.getResource(filenametoSet).getFile()
            // Going to use temporary file because we were getting out of memory errors constantly on staging server
            WorkbookSettings wbSetting = new WorkbookSettings()
            wbSetting.setUseTemporaryFileDuringWrite(true)
            workbook = Workbook.getWorkbook( file, wbSetting )
            //set MIME TYPE as Excel
            filenametoSet = filenametoSet.split("/")
            response.setContentType( "application/vnd.ms-excel" )
            response.setHeader( "Content-Disposition", "attachment; filename= ${filenametoSet[2]}" )
            //create workbook and sheet
            book = Workbook.createWorkbook( response.getOutputStream(), workbook )
            def sheet
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
                    //update data from Asset Entity table to EXCEL
                    def assetSize = asset.size()
                    def columnNameListSize = columnNameList.size()
                    for ( int r = 1; r <= assetSize; r++ ) {
                        //Add assetId for walkthrough template only.
                        if( sheetColumnNames.containsKey("assetId") ) {
                            def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
                            def addAssetId = new Number(0, r, (asset[r-1].id))
                            sheet.addCell( addAssetId )
                        }
                        for ( int coll = 0; coll < columnNameListSize; coll++ ) {
                            def addContentToSheet
	                                                        	
                            if ( asset[r-1].(dataTransferAttributeMap.eavAttribute.attributeCode[coll]) == null ) {
                                addContentToSheet = new Label( map[columnNameList.get(coll)], r, "" )
                            } 
                            else {
                            	//if attributeCode is sourceTeam or TargetTeam export the teamCode 
                            	if( dataTransferAttributeMap.eavAttribute.attributeCode[coll] == "sourceTeam" || dataTransferAttributeMap.eavAttribute.attributeCode[coll] == "targetTeam" ) {
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
            flash.message = "Excel template not found. "
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
    	
        userPreferenceService.loadPreferences("MAX_ASSET_LIST")
        def userMax = getSession().getAttribute("MAX_ASSET_LIST")
        if(userMax.MAX_ASSET_LIST){
        	if(!params.max) params.max = userMax.MAX_ASSET_LIST
        }else{
        	if(!params.max) params.max = 50
        }
        def projectId = params.projectId
        if(projectId == null || projectId == ""){
        	projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
        }
        def project = Project.findById( projectId )
        def assetEntityInstanceList = AssetEntity.findAllByProject( project, params ) 
        [ assetEntityInstanceList: assetEntityInstanceList, projectId: projectId,maxVal : params.max ]
    }   
    /* ----------------------------------------
     * delete assetEntity
     * @param assetEntityId
     * @return assetEntityList
     * --------------------------------------- */
    def delete = {
        def assetEntityInstance = AssetEntity.get( params.id )
        def projectId = params.projectId
        if(assetEntityInstance) {
            ProjectAssetMap.executeUpdate("delete from ProjectAssetMap pam where pam.asset = ${assetEntityInstance.id}")
            AssetTransition.executeUpdate("delete from AssetTransition ast where ast.assetEntity = ${assetEntityInstance.id}")
            AssetComment.executeUpdate("delete from AssetComment ac where ac.assetEntity = ${assetEntityInstance.id}")
            ApplicationAssetMap.executeUpdate("delete from ApplicationAssetMap aam where aam.asset = ${assetEntityInstance.id}")
            AssetEntityVarchar.executeUpdate("delete from AssetEntityVarchar aev where aev.assetEntity = ${assetEntityInstance.id}")
            AssetEntity.executeUpdate("delete from AssetEntity ae where ae.id = ${assetEntityInstance.id}")
          
            flash.message = "AssetEntity ${assetEntityInstance.assetName} deleted"         
        }
        else {
            flash.message = "AssetEntity not found with id ${params.id}"          
        }
    	if ( params.clientList ){
    		redirect( controller:"clientConsole", action:"list", params:[projectId:projectId, moveBundle:params.moveBundle] )
    	} else if ( params.moveBundle ) {
         	redirect( action:dashboardView, params:[projectId:projectId, moveBundle:params.moveBundle] )
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
            	
            AssetEntity.executeUpdate("update AssetEntity ae set ae.moveBundle = null , ae.project = null , ae.sourceTeam = null , ae.targetTeam = null where ae.id = ${params.id}")
            flash.message = "AssetEntity ${assetEntityInstance.assetName} Removed from Project"
                           
        }
        else {
            flash.message = "AssetEntity not found with id ${params.id}"
        }
        if ( params.clientList ){
    		redirect( controller:"clientConsole", action:"list", params:[projectId:projectId, moveBundle:params.moveBundle] )
    	} else if ( params.moveBundle ){
            redirect( action:dashboardView, params:[projectId:projectId, moveBundle : params.moveBundle] )
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
        def assetEntityInstance = new AssetEntity(params)
        def projectId = params.projectId
        def projectInstance = Project.findById( projectId )
        assetEntityInstance.project = projectInstance
        assetEntityInstance.owner = projectInstance.client
        if(!assetEntityInstance.hasErrors() && assetEntityInstance.save()) {
            flash.message = "AssetEntity ${assetEntityInstance.assetName} created"
            redirect( action:list, params:[projectId: projectId] )
        }
        else {
        	flash.message = "AssetEntity ${assetEntityInstance.assetName} not created"
            redirect( action:list, params:[projectId: projectId] )
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
        /*if( assetEntityInstance.assetType != null ){
        items = [id:assetEntityInstance.id, model: assetEntityInstance.model, sourceLocation: assetEntityInstance.sourceLocation, targetLocation: assetEntityInstance.targetLocation, sourceRack: assetEntityInstance.sourceRack, targetRack: assetEntityInstance.targetRack, sourceRackPosition: assetEntityInstance.sourceRackPosition, targetRackPosition: assetEntityInstance.targetRackPosition, usize: assetEntityInstance.usize, manufacturer: assetEntityInstance.manufacturer, fiberCabinet: assetEntityInstance.fiberCabinet, hbaPort: assetEntityInstance.hbaPort, hinfo: assetEntityInstance.hinfo, ipAddress: assetEntityInstance.ipAddress, kvmDevice: assetEntityInstance.kvmDevice, kvmPort: assetEntityInstance.kvmPort, newOrOld: assetEntityInstance.newOrOld, nicPort: assetEntityInstance.nicPort, powerPort: assetEntityInstance.powerPort, remoteMgmtPort: assetEntityInstance.remoteMgmtPort, truck: assetEntityInstance.truck, project:assetEntityInstance.project.name, projectId:assetEntityInstance.project.id, assetType:assetEntityInstance.assetType, assetTypeId:assetEntityInstance.assetType.id, assetTag:assetEntityInstance.assetTag, assetName:assetEntityInstance.assetName, serialNumber:assetEntityInstance.serialNumber, , application:assetEntityInstance.application ]
        } else {
        items = [id:assetEntityInstance.id, model: assetEntityInstance.model, sourceLocation: assetEntityInstance.sourceLocation, targetLocation: assetEntityInstance.targetLocation, sourceRack: assetEntityInstance.sourceRack, targetRack: assetEntityInstance.targetRack, sourceRackPosition: assetEntityInstance.sourceRackPosition, targetRackPosition: assetEntityInstance.targetRackPosition, usize: assetEntityInstance.usize, manufacturer: assetEntityInstance.manufacturer, fiberCabinet: assetEntityInstance.fiberCabinet, hbaPort: assetEntityInstance.hbaPort, hinfo: assetEntityInstance.hinfo, ipAddress: assetEntityInstance.ipAddress, kvmDevice: assetEntityInstance.kvmDevice, kvmPort: assetEntityInstance.kvmPort, newOrOld: assetEntityInstance.newOrOld, nicPort: assetEntityInstance.nicPort, powerPort: assetEntityInstance.powerPort, remoteMgmtPort: assetEntityInstance.remoteMgmtPort, truck: assetEntityInstance.truck, project:assetEntityInstance.project.name, projectId:assetEntityInstance.project.id, assetTag:assetEntityInstance.assetTag, assetName:assetEntityInstance.assetName, serialNumber:assetEntityInstance.serialNumber, application:assetEntityInstance.application ]
        }*/
        def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntityInstance.attributeSet.id order by eav.sortOrder ")
        entityAttributeInstance.each{
        	def attributeOptions = EavAttributeOption.findAllByAttribute( it.attribute )
    		def options = []
    		attributeOptions.each{option ->
    			options<<[option:option.value]
    		}
        	if( it.attribute.attributeCode != "moveBundle" && it.attribute.attributeCode != "sourceTeam" && it.attribute.attributeCode != "targetTeam" ){
        		items << [label:it.attribute.frontendLabel, attributeCode:it.attribute.attributeCode, 
        		          frontendInput:it.attribute.frontendInput, 
        		          options : options, 
        		          value:assetEntityInstance.(it.attribute.attributeCode) ? assetEntityInstance.(it.attribute.attributeCode) : ""]
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
	    	def assetEntityParamsList = assetEntityParams.split(",")
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
	        if(assetEntityInstance) {
	        	assetEntityInstance.properties = map
	            if(!assetEntityInstance.hasErrors() && assetEntityInstance.save()) {
	            	def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntityInstance.attributeSet.id order by eav.sortOrder ")
	            	entityAttributeInstance.each{
	                	if( it.attribute.attributeCode != "moveBundle" && it.attribute.attributeCode != "sourceTeam" && it.attribute.attributeCode != "targetTeam" ){
	                		assetItems << [id:assetEntityInstance.id, attributeCode:it.attribute.attributeCode, 
	                		               frontendInput:it.attribute.frontendInput, 
	                		               value:assetEntityInstance.(it.attribute.attributeCode) ? assetEntityInstance.(it.attribute.attributeCode) : ""]
	                	}
	                }
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
    	entityAttributeInstance.each{
    		def attributeOptions = EavAttributeOption.findAllByAttribute( it.attribute )
    		def options = []
    		attributeOptions.each{option ->
    			options<<[option:option.value]
    		}
    		if( it.attribute.attributeCode != "moveBundle" && it.attribute.attributeCode != "sourceTeam" && it.attribute.attributeCode != "targetTeam"){
    			items<<[ label:it.attribute.frontendLabel, attributeCode:it.attribute.attributeCode, 
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
    		if( it.attribute.attributeCode != "moveBundle" && it.attribute.attributeCode != "sourceTeam" && it.attribute.attributeCode != "targetTeam"){
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
    			def assetEntity = AssetEntity.executeQuery( "select distinct $it from AssetEntity where owner = $project.client.id" ) 
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
            assetCommentInstance.dateResolved = new Date()
        }
    	if(!assetCommentInstance.hasErrors() && assetCommentInstance.save()) {
    		render assetCommentInstance as JSON
        } else {
        	render assetComments as JSON
        }
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
        formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss");
 	   
        def assetComment = AssetComment.get(params.id)
        if(assetComment.createdBy){
            personCreateObj = Person.find("from Person p where p.id = $assetComment.createdBy.id")
            dtCreated = formatter.format(assetComment.dateCreated);
        }
        if(assetComment.resolvedBy){
            personResolvedObj = Person.find("from Person p where p.id = $assetComment.resolvedBy.id")
            dtResolved = formatter.format(assetComment.dateResolved);
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
        def principal = SecurityUtils.subject.principal
        def loginUser = UserLogin.findByUsername(principal)
    	def assetCommentInstance = AssetComment.get(params.id)
    	if(params.isResolved == '1' && assetCommentInstance.isResolved == 0 ){
            assetCommentInstance.resolvedBy = loginUser.person
            assetCommentInstance.dateResolved = new Date()
        }else if(params.isResolved == '1' && assetCommentInstance.isResolved == 1){
        }else{
        	assetCommentInstance.resolvedBy = null
            assetCommentInstance.dateResolved = null
        }
    	assetCommentInstance.properties = params      
    	
    	if(!assetCommentInstance.hasErrors()) {
    		assetCommentInstance.save()
        }
    	render assetCommentInstance as JSON
    }
    /* delete the comment record
     * @param assetComment
     * @author Lokanath
     * @return assetCommentList 
     */
    def deleteComment = {
        def assetCommentInstance = AssetComment.get(params.id)
        if(assetCommentInstance){
            assetCommentInstance.delete()
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
        def projectId = params.projectId
        def bundleId = params.moveBundle
        def sortField = params.sort
        def orderField = params.order
        def assetList
        def bundleTeams = []
        def assetsList = []
        def totalAsset = []
        def supportTeam = new HashMap()
        def projectInstance = Project.findById( projectId )
        def moveBundleInstanceList = MoveBundle.findAll("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
        def moveBundleInstance
        def stateVal
        def taskVal
        if(bundleId){
        	userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
            moveBundleInstance = MoveBundle.findById(bundleId)
        } else {
            userPreferenceService.loadPreferences("CURR_BUNDLE")
            def defaultBundle = getSession().getAttribute("CURR_BUNDLE")
            if(defaultBundle.CURR_BUNDLE){
            	moveBundleInstance = MoveBundle.findById(defaultBundle.CURR_BUNDLE)
            	if( moveBundleInstance.project.id != Integer.parseInt(projectId) ){
            		moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
            	}
            } else {
            	moveBundleInstance = MoveBundle.find("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
            }
        }
        // get the list of assets order by Hold and recent asset Transition
        if( moveBundleInstance != null ){
        	def holdTotalAsset
        	def otherTotalAsset
        	def queryHold = new StringBuffer("select max(at.date_created) as dateCreated, ae.asset_entity_id as id, ae.priority, ae.asset_tag as assetTag, ae.asset_name as assetName, " + 
        								     "ae.source_team_id as sourceTeam, ae.target_team_id as targetTeam, pm.current_state_id as currentState FROM asset_entity ae " +
        								     "LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id) LEFT JOIN project_asset_map pm ON (pm.asset_id = ae.asset_entity_id) " + 
        	                                 "where ae.project_id = ${moveBundleInstance.project.id} and ae.move_bundle_id = ${moveBundleInstance.id} and pm.current_state_id = 10 group by ae.asset_entity_id")
        	
        	def queryNotHold = new StringBuffer("select max(at.date_created) as dateCreated, ae.asset_entity_id as id, ae.priority, ae.asset_tag as assetTag, ae.asset_name as assetName, " + 
					                            "ae.source_team_id as sourceTeam, ae.target_team_id as targetTeam, pm.current_state_id as currentState FROM asset_entity ae " +
					                            "LEFT JOIN asset_transition at ON (at.asset_entity_id = ae.asset_entity_id) LEFT JOIN project_asset_map pm ON (pm.asset_id = ae.asset_entity_id) " + 
                                                "where ae.project_id = ${moveBundleInstance.project.id} and ae.move_bundle_id = ${moveBundleInstance.id} and ( pm.current_state_id != 10 or pm.current_state_id is null ) group by ae.asset_entity_id")
        	
	        if( sortField ) {
		        holdTotalAsset = jdbcTemplate.queryForList( queryHold.append(" order by ${sortField} ${orderField}").toString() )
		        otherTotalAsset = jdbcTemplate.queryForList( queryNotHold.append(" order by ${sortField} ${orderField}").toString() )
	        } else {
	        	holdTotalAsset = jdbcTemplate.queryForList( queryHold.append(" order by dateCreated desc").toString() )
	        	otherTotalAsset = jdbcTemplate.queryForList( queryNotHold.append(" order by dateCreated desc").toString() )
	        }
        	holdTotalAsset.each{
        	totalAsset<<it
        	}
	        otherTotalAsset.each{
	        	totalAsset<<it
	        }
	        def projectTeamList = ProjectTeam.findAll("from ProjectTeam pt where pt.moveBundle = ${moveBundleInstance.id} and "+
	        											"pt.teamCode != 'Cleaning' and pt.teamCode != 'Transport'  order by pt.name asc")
	        // Get Id for respective States
	        def cleanedId = stateEngineService.getStateId( "STD_PROCESS", "Cleaned" )
	        def rerackedId = stateEngineService.getStateId( "STD_PROCESS", "Reracked" )
	        def onCartId = stateEngineService.getStateId( "STD_PROCESS", "OnCart" )
	        def stagedId = stateEngineService.getStateId( "STD_PROCESS", "Staged" )
	        def unrackedId = stateEngineService.getStateId( "STD_PROCESS", "Unracked" )
	        def holdId = stateEngineService.getStateId( "STD_PROCESS", "Hold" )
	        def releasedId = stateEngineService.getStateId( "STD_PROCESS", "Release" )
	        projectTeamList.each{
	            def teamMembers = partyRelationshipService.getBundleTeamMembersDashboard(it.id)
	            def member
	            if(teamMembers.length() > 0){
	            	member = teamMembers.delete((teamMembers.length()-1), teamMembers.length())
	            }
	            def sourceAssets = AssetEntity.findAll("from AssetEntity where moveBundle = ${moveBundleInstance.id} and sourceTeam = ${it.id} " ).size()
	            
	            def unrackedAssets = jdbcTemplate.queryForList("SELECT max(cast(t.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_transition t left join asset_entity e on "+
	        														"(t.asset_entity_id = e.asset_entity_id) where e.move_bundle_id = ${moveBundleInstance.id} "+
	        														"and e.source_team_id = ${it.id} group by e.asset_entity_id having maxstate >= $unrackedId").size() 
	            
	            def sourceAvailassets = jdbcTemplate.queryForList("SELECT max(cast(t.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_transition t left join asset_entity e on "+
	        														"(t.asset_entity_id = e.asset_entity_id) where t.state_to >= $releasedId and e.move_bundle_id = ${moveBundleInstance.id}"+
	        														" and e.source_team_id = ${it.id} group by e.asset_entity_id HAVING maxstate < $unrackedId ").size()
	            def targetAssets = AssetEntity.findAll("from AssetEntity  where moveBundle = ${moveBundleInstance.id} and targetTeam = ${it.id} " ).size()
	
	            def rerackedAssets = jdbcTemplate.queryForList("SELECT max(cast(t.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_transition t left join asset_entity e on "+
																"(t.asset_entity_id = e.asset_entity_id) where e.move_bundle_id = ${moveBundleInstance.id} "+
																"and e.target_team_id = ${it.id} group by e.asset_entity_id HAVING maxstate >= $rerackedId ").size()
				
	            def targetAvailAssets = jdbcTemplate.queryForList("SELECT max(cast(t.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_transition t left join asset_entity e on "+
	        														"(t.asset_entity_id = e.asset_entity_id) where t.state_to >= $stagedId and e.move_bundle_id = ${moveBundleInstance.id} "+
	        														"and e.target_team_id = ${it.id} group by e.asset_entity_id HAVING maxstate < $rerackedId ").size()
	            bundleTeams <<[team:it,members:member, sourceAssets:sourceAssets, 
	                           unrackedAssets:unrackedAssets, sourceAvailassets:sourceAvailassets , 
	                           targetAvailAssets:targetAvailAssets , targetAssets:targetAssets, 
	                           rerackedAssets:rerackedAssets ]
	        }
	        def sourceCleaned = ProjectAssetMap.findAll("from ProjectAssetMap where currentStateId >= $cleanedId and asset in (select id from AssetEntity  "+
	        												"where moveBundle = ${moveBundleInstance.id} )" ).size()
	        def sourceMover = ProjectAssetMap.findAll("from ProjectAssetMap where currentStateId >= $onCartId and asset in (select id from AssetEntity  "+
	        											"where moveBundle = ${moveBundleInstance.id} )" ).size()
	        def targetMover = ProjectAssetMap.findAll("from ProjectAssetMap where currentStateId >= $stagedId and asset in (select id from AssetEntity  "+
	        											"where moveBundle = ${moveBundleInstance.id} )" ).size()
	        def cleaningTeam = ProjectTeam.findByTeamCode("Cleaning")
	        def transportTeam = ProjectTeam.findByTeamCode("Transport")
	        def cleaningMembers = partyRelationshipService.getBundleTeamMembersDashboard(cleaningTeam.id)
	        def transportMembers = partyRelationshipService.getBundleTeamMembersDashboard(transportTeam.id)
		        supportTeam.put("totalAssets", totalAsset.size() )
		        supportTeam.put("sourceCleaned", sourceCleaned )
		        supportTeam.put("sourceMover", sourceMover )
		        supportTeam.put("targetMover", targetMover )
		        supportTeam.put("cleaning", cleaningTeam )
		        supportTeam.put("cleaningMembers", cleaningMembers.delete((cleaningMembers.length()-1), cleaningMembers.length()) )
		        supportTeam.put("transport", transportTeam )
		        supportTeam.put("transportMembers", transportMembers.delete((transportMembers.length()-1), transportMembers.length()) )
	        totalAsset.each{
	        	def check = true
	        	def projectAssetMap = it.currentState
	        	def curId = 0
	        	if(projectAssetMap){
	        		curId = it.currentState
	        	}
	        	stateVal = stateEngineService.getState( "STD_PROCESS", curId )
				if(stateVal){
		        	taskVal = stateEngineService.getTasks( "STD_PROCESS", "SUPERVISOR", stateVal )
					if(taskVal.size() == 0){
						check = false    				
					}
				}
	        	def cssClass
	        	if(curId == Integer.parseInt(holdId) ){
	        		cssClass = 'asset_hold'
	        	} else if(curId < Integer.parseInt(releasedId) && curId != Integer.parseInt(holdId) ){
	        		cssClass = 'asset_pending'
	        	} else if(curId > Integer.parseInt(rerackedId)){
	        		cssClass = 'asset_done'
	        	}
	        	assetsList<<[asset: it, status: stateEngineService.getStateLabel( "STD_PROCESS", curId ), cssClass : cssClass, checkVal:check]
	        }
	        def totalUnracked = jdbcTemplate.queryForList("SELECT max(cast(t.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_transition t left join asset_entity e on "+
															"(t.asset_entity_id = e.asset_entity_id) where e.move_bundle_id = ${moveBundleInstance.id} "+
															"group by e.asset_entity_id having maxstate >= $unrackedId").size()
	        
	        def totalSourceAvail = jdbcTemplate.queryForList("SELECT max(cast(t.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_transition t left join asset_entity e on"+
	        												" (t.asset_entity_id = e.asset_entity_id) where t.state_to >= $releasedId and e.move_bundle_id = ${moveBundleInstance.id}"+
	        												" group by e.asset_entity_id HAVING maxstate < $unrackedId ").size() 
	        
	        def totalReracked = jdbcTemplate.queryForList("SELECT max(cast(t.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_transition t left join asset_entity e on "+
															"(t.asset_entity_id = e.asset_entity_id) where e.move_bundle_id = ${moveBundleInstance.id} "+
															"group by e.asset_entity_id HAVING maxstate >= $rerackedId ").size()
	        
	        def totalTargetAvail = jdbcTemplate.queryForList("SELECT max(cast(t.state_to as UNSIGNED INTEGER)) as maxstate FROM asset_transition t left join asset_entity e on "+
	        											" (t.asset_entity_id = e.asset_entity_id) where t.state_to >= $stagedId and e.move_bundle_id = ${moveBundleInstance.id} "+
	        											" group by e.asset_entity_id HAVING maxstate < $rerackedId ").size() 
	        	userPreferenceService.loadPreferences("SUPER_CONSOLE_REFRESH")
	        def timeToRefresh = getSession().getAttribute("SUPER_CONSOLE_REFRESH")
	        return[ moveBundleInstanceList: moveBundleInstanceList, projectId:projectId, bundleTeams:bundleTeams, 
	                assetsList:assetsList, moveBundleInstance:moveBundleInstance, 
	                supportTeam:supportTeam, totalUnracked:totalUnracked, totalSourceAvail:totalSourceAvail, 
	                totalTargetAvail:totalTargetAvail, totalReracked:totalReracked, totalAsset:totalAsset.size(), 
	                timeToRefresh : timeToRefresh ? timeToRefresh.SUPER_CONSOLE_REFRESH : "never"]
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
	        def teamName = assetDetail.sourceTeam
	        def assetTransition = AssetTransition.findAllByAssetEntity( assetDetail, [ sort:"dateCreated", order:"desc"] )
	        assetTransition.each{
	        	def taskLabel = stateEngineService.getStateLabel("STD_PROCESS",Integer.parseInt(it.stateTo))
	        	def time = it.dateCreated.toString().substring(11,19)
	    	    def timeElapsed 
	    	    if(it.timeElapsed != 0){
		    	    def hours = (it.timeElapsed / (1000*60*60)).toString()
		    	    def minutes = ((it.timeElapsed % (1000*60*60)) / (1000*60)).toString()
		    	    def seconds = (((it.timeElapsed % (1000*60*60)) % (1000*60)) / 1000).toString()
		    	    timeElapsed = hours.substring(0,hours.indexOf("."))+":"+minutes.substring(0,minutes.indexOf("."))+":"+seconds.substring(0,seconds.indexOf("."))
	    	    } else {
	    	    	timeElapsed = "0:0:0"
	    	    }
	        	recentChanges<<[time+"/"+timeElapsed+" "+taskLabel+' ('+ it.userLogin.person.lastName +')']
	        }
	        def currentState = 0
	        def projectAssetMap = ProjectAssetMap.findByAsset(assetDetail)
	        if(projectAssetMap){
	        	currentState = projectAssetMap.currentStateId
	        }
	        def state = stateEngineService.getState( "STD_PROCESS", currentState )
	        def validStates
	        if(state){
	        	validStates= stateEngineService.getTasks( "STD_PROCESS", "SUPERVISOR", state )
	        } else {
	        	validStates = ["Ready"] 
	        	//stateEngineService.getTasks("STD_PROCESS","TASK_NAME")
	        }
	        validStates.each{
	        	stateIdList<<stateEngineService.getStateIdAsInt( "STD_PROCESS", it )
	        }
	        stateIdList.sort().each{
	        	statesList<<[id:stateEngineService.getState("STD_PROCESS",it),label:stateEngineService.getStateLabel("STD_PROCESS",it)]
	        }
	        def map = new HashMap()
	        map.put("assetDetail",assetDetail)
	        if(teamName){
                map.put("teamName",teamName.name)
	        }else{
                map.put("teamName","")
	        }
	        map.put("currentState",stateEngineService.getStateLabel("STD_PROCESS",currentState))
	        map.put("state",state)
	        def sourceQuery = new StringBuffer("from ProjectTeam where moveBundle = $assetDetail.moveBundle.id and teamCode != 'Cleaning' and teamCode != 'Transport'")
	        def targetQuery = new StringBuffer("from ProjectTeam where moveBundle = $assetDetail.moveBundle.id and teamCode != 'Cleaning' and teamCode != 'Transport'")
	        if(assetDetail.sourceTeam){
	        	sourceQuery.append(" and id != $assetDetail.sourceTeam.id ")
	        }
	        if(assetDetail.targetTeam){
	        	targetQuery.append(" and id != $assetDetail.targetTeam.id ")
	        }
	        def sourceTeams = ProjectTeam.findAll(sourceQuery.toString())
	        def targetTeams = ProjectTeam.findAll(targetQuery.toString())
	        assetStatusDetails<<[ 'assetDetails':map, 'statesList':statesList, 
	                              'recentChanges':recentChanges, 'sourceTeams':sourceTeams,
	                              'targetTeams':targetTeams ]
        }
        render assetStatusDetails as JSON
        		
    }
    /*----------------------------------
     * @author: Lokanath Redy
     * @param : fromState and toState
     * @return: boolean value to validate comment field
     *---------------------------------*/
    def getFlag = {
    	def toState = params.toState
    	def fromState = params.fromState
    	def status = []
    	def flag = stateEngineService.getFlags("STD_PROCESS","SUPERVISOR", fromState, toState)
    	if(flag.contains("comment") || flag.contains("issue")){
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
	    	def rerackedId = stateEngineService.getStateId("STD_PROCESS","Reracked")
	        def holdId = stateEngineService.getStateId("STD_PROCESS","Hold")
	        def releasedId = stateEngineService.getStateId("STD_PROCESS","Release")
	        def projectAssetMap = ProjectAssetMap.findByAsset(assetEntity)
	        def currentStateId 
	        if(projectAssetMap){
	        	currentStateId = projectAssetMap.currentStateId
	        }
	    	if(status != "" ){
		    	def principal = SecurityUtils.subject.principal
		    	def loginUser = UserLogin.findByUsername(principal)
		    	def transactionStatus = workflowService.createTransition("STD_PROCESS","SUPERVISOR", status, assetEntity, assetEntity.moveBundle, loginUser, null, comment )
		    	if ( transactionStatus.success ) {
		    		if(comment){
			    		assetComment = new AssetComment()
		          		assetComment.comment = comment
		          		assetComment.assetEntity = assetEntity
		          		assetComment.commentType = 'issue'		          			
		          		assetComment.createdBy = loginUser.person
		          		assetComment.save()
		    		}
		    		stateIdList = getStates(status)
				    stateIdList.sort().each{
				    	statesList<<[id:stateEngineService.getState("STD_PROCESS",it),label:stateEngineService.getStateLabel("STD_PROCESS",it)]
				    }
				    statusLabel = stateEngineService.getStateLabel("STD_PROCESS",stateEngineService.getStateIdAsInt("STD_PROCESS",status))
				    statusName = stateEngineService.getState("STD_PROCESS",stateEngineService.getStateIdAsInt("STD_PROCESS",status))
		    	} else {
		        	statusLabel = stateEngineService.getStateLabel("STD_PROCESS",currentStateId)
		        	statusName = stateEngineService.getState("STD_PROCESS",currentStateId)
		        	stateIdList = getStates(stateEngineService.getState("STD_PROCESS",currentStateId))
		        	stateIdList.sort().each{
				    	statesList<<[id:stateEngineService.getState("STD_PROCESS",it),label:stateEngineService.getStateLabel("STD_PROCESS",it)]
				    }
		    	}
	    	} else {
	        	statusLabel = stateEngineService.getStateLabel("STD_PROCESS",currentStateId)
	        	statusName = stateEngineService.getState("STD_PROCESS",currentStateId)
	        	stateIdList = getStates(stateEngineService.getState("STD_PROCESS",currentStateId))
	        	stateIdList.sort().each{
			    	statesList<<[id:stateEngineService.getState("STD_PROCESS",it),label:stateEngineService.getStateLabel("STD_PROCESS",it)]
			    }
	    	}
    		if(priority){
                assetEntity.priority = Integer.parseInt( priority )
    		}
    		if(assignTo){
	    		def assignToList = assignTo.split('/')
	    		def projectTeam = ProjectTeam.get(assignToList[1])
	    		if(assignToList[0] == 's'){
	    			assetEntity.sourceTeam = projectTeam
	    		} else if(assignToList[0] == 't'){
	    			assetEntity.targetTeam = projectTeam
	    		}
    		}
    		def currentStatus = 0
    		def projectAsset = ProjectAssetMap.findByAsset(assetEntity)
    		if(projectAsset){
    			currentStatus = projectAsset.currentStateId
    		}
    		
			if(statesList.size() == 0){
				check = false    				
			}else{
                check = true
			}
    		def cssClass
    		if(currentStatus == Integer.parseInt(holdId) ){
        		cssClass = 'asset_hold'
        	} else if(currentStatus < Integer.parseInt(releasedId) && currentStatus != Integer.parseInt(holdId) ){
        		cssClass = 'asset_pending'
        	} else if(currentStatus > Integer.parseInt(rerackedId)){
        		cssClass = 'asset_done'
        	}
    		assetEntity.save()
    		def sourceTeam
    		def targetTeam
    		if(assetEntity.sourceTeam){
    			sourceTeam = assetEntity.sourceTeam.name
    		}
		    if(assetEntity.targetTeam){
		    	targetTeam = assetEntity.targetTeam.name
		    }
		    def sourceQuery = new StringBuffer("from ProjectTeam where moveBundle = $assetEntity.moveBundle.id and teamCode != 'Cleaning' and teamCode != 'Transport'")
	        def targetQuery = new StringBuffer("from ProjectTeam where moveBundle = $assetEntity.moveBundle.id and teamCode != 'Cleaning' and teamCode != 'Transport'")
	        if(assetEntity.sourceTeam){
	        	sourceQuery.append(" and id != $assetEntity.sourceTeam.id ")
	        }
	        if(assetEntity.targetTeam){
	        	targetQuery.append(" and id != $assetEntity.targetTeam.id ")
	        }
	        def sourceTeams = ProjectTeam.findAll(sourceQuery.toString())
	        def targetTeams = ProjectTeam.findAll(targetQuery.toString())
		    assetList <<['assetEntity':assetEntity, 'sourceTeam':sourceTeam, 'targetTeam':targetTeam, 
		                 'sourceTeams':sourceTeams,'targetTeams':targetTeams, 'statesList':statesList,
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
        def stateIdList = []
        def validStates
        if(state){
            validStates = stateEngineService.getTasks("STD_PROCESS","SUPERVISOR", state)
        } else {
            validStates = ["Ready"]
            //stateEngineService.getTasks("STD_PROCESS","TASK_NAME")
        }
        validStates.each{
		 	stateIdList<<stateEngineService.getStateIdAsInt("STD_PROCESS",it)
        }
        return stateIdList
    }
    /*---------------------------------------------
     * Set browser refresh time interval as user preference
     * @author : Lokanath Reddy
     * @param : time interval
     * @return : time interval
     *-------------------------------------------*/
    def setTimePreference = {
    	def timer = params.timer
    	def refreshTime =[]
    	if(timer){
    		userPreferenceService.setPreference( "SUPER_CONSOLE_REFRESH", "${timer}" )
    	}
    	def timeToRefresh = getSession().getAttribute("SUPER_CONSOLE_REFRESH")
    	refreshTime <<[refreshTime:timeToRefresh] 
    	render refreshTime as JSON
    }
    /*-------------------------------------------
     * @author : Bhuvaneshwari
     * @param  : List of assets selected for transition
     * @return : Common tasks for selected assets
     *-------------------------------------------*/
    //  To get unique list of task for list of assets through ajax
	def getList = {
    		
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
        		def projectAssetMap = ProjectAssetMap.find("from ProjectAssetMap pam where pam.asset = $asset")
        		if(projectAssetMap){
        			stateVal = stateEngineService.getState("STD_PROCESS",projectAssetMap.currentStateId)
                    temp = stateEngineService.getTasks("STD_PROCESS","SUPERVISOR",stateVal)
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
           		tempTaskList << stateEngineService.getStateIdAsInt("STD_PROCESS",it)
       		}
       		tempTaskList.sort().each{
       			sortList << [state:stateEngineService.getState("STD_PROCESS",it),label:stateEngineService.getStateLabel("STD_PROCESS",it)]
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
        def assetId = params.assetVal
        def assetEnt = AssetEntity.findAll("from AssetEntity ae where ae.id in ($assetId)")
        assetEnt.each{
	        def bundle = it.moveBundle
	        def principal = SecurityUtils.subject.principal
	        def loginUser = UserLogin.findByUsername(principal)
	        def team = it.sourceTeam
			     
	        def workflow = workflowService.createTransition("STD_PROCESS","SUPERVISOR",params.taskList,it,bundle,loginUser,team,params.enterNote)
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
        redirect(action:'dashboardView',params:["projectId":params.projectId,"moveBundle":params.moveBundle])
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
    
}
