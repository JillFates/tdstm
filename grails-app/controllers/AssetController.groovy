import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*
import grails.converters.JSON
import org.jsecurity.SecurityUtils
import com.tdssrc.eav.*
class AssetController {

	// TODO : Fix indentation
	
    def index = {
		redirect( action:list, params:params )
	}

    //upload , export
    /*
     * render import form
     */
    def assetImport = {
        //get id of selected project from project view
        def projectId = params.projectId
        def assetsByProject
        def projectInstance
        def moveBundleInstanceList
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
    		def project = Project.findById(projectId)
    		assetsByProject = Asset.findAllByProject(project)
    	}
    	
        render( view:"assetImport", model : [ assetsByProject: assetsByProject, projectId: projectId, moveBundleInstanceList: moveBundleInstanceList, dataTransferSetImport: dataTransferSetImport, dataTransferSetExport: dataTransferSetExport ] )
    }
    /*
     * render export form
     */
    def assetExport = {
        render( view:"assetExport" )
    }
    /*
     * upload excel file into Asset table
     */
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
                
            }

            project = Project.findById( projectId )
            
            //delete previous records existed for Project
        
            //def assetDelete = Asset.executeUpdate("delete from Asset a where a.project = $project.id " )

        }catch ( Exception ex ) {
            
            flash.message = " Project Name is required. "
            redirect( controller:"asset", action:"assetImport" )
        }

        // get File
        MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
        CommonsMultipartFile file = ( CommonsMultipartFile ) mpr.getFile("file")

        // create workbook
        def workbook
        def sheet
        //def sheetNo = 1
        //def map = [ "Server":null, "Type":null, "S/N":null, "AssetTag":null ]
        
        def map = [:]
        def serverMap = [:]
        def dataTransferAttributeMapSheetName
        dataTransferAttributeMap.eachWithIndex { item, pos ->
    		map.put( item.columnName, null )
    		serverMap.put( "sheetName", (item.sheetName).trim() )
        }
        try {
            workbook = Workbook.getWorkbook( file.inputStream )
            def sheetNames = workbook.getSheetNames()        
            def flag = 0
            for( int i=0;  i < sheetNames.length; i++ ) {           	
            	
                if ( serverMap.containsValue(sheetNames[i].trim()) ) {
                    flag = 1
                    sheet = workbook.getSheet( sheetNames[i] )
                }
            	
      		}
            
            if( flag == 0 ) {
            	
            	flash.message = " Sheet not found, Please check it."
                redirect( action:assetImport, params:[projectId:projectId] ) 
                
            } else {           
           
            
                // TODO : All columns should be done using maps as this will get unwieldly to manage with 20+ columns.  Both the import and
                // export should use the same map.

                //check for column
                def col = sheet.getColumns()
                def checkCol = checkHeader( col, map, sheet )
                // Statement to check Headers if header are not found it will return Error message

                // TODO : map here too.
                if ( checkCol == false ) {
                    flash.message = " Column Headers not found, Please check it."
                    redirect( action:assetImport, params:[projectId:projectId] )

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
                        def added = 0
                        def skipped = []
                        def dataTransferValue
                        def eavAttributeInstance
                        for( int cols = 0; cols < col; cols++ ) {
                            def dataTransferAttributeMapInstance = DataTransferAttributeMap.findByColumnName(sheet.getCell( cols, 0 ).contents)
                            if( dataTransferAttributeMapInstance != null ) {
                                for ( int r = 1; r < sheet.rows; r++ ) {
                                    dataTransferValue = new DataTransferValue()
                                    eavAttributeInstance = dataTransferAttributeMapInstance.eavAttribute
                                    dataTransferValue.importValue = sheet.getCell( cols, r ).contents
                                    dataTransferValue.rowId = r
                                    dataTransferValue.dataTransferBatch = dataTransferBatch
                                    dataTransferValue.eavAttribute = eavAttributeInstance
                                    dataTransferValue.save()
                    		
                                }
                            }
                	
                            /*// get fields
                            def assetName = sheet.getCell( map["Server"], r ).contents
                            def assetType = sheet.getCell( map["Type"], r ).contents
                            def assetTypeObj = AssetType.findById(assetType)
                            def serialNumber = sheet.getCell( map["S/N"], r ).contents
                            def assetTag = sheet.getCell( map["AssetTag"], r ).contents


                            // save data in to db and check for added rows and skipped
                            def asset = new Asset(

                            project: project,
                            assetType: assetTypeObj,
                            assetName: assetName,
                            assetTag: assetTag,
                            serialNumber: serialNumber,
                            deviceFunction: "")

                            // TODO : This logic will ALWAY return true since the asset is created.  It should be testing asset.save() and not called above.
                            if ( asset ) {
                            asset.save()
                            added++
                            } else {
                            skipped += ( r +1 )
                            }*/
                        }

                    } // generate error message
                    workbook.close()
                    redirect( action:assetImport, params:[projectId:projectId] )
                }
            }
        }catch( Exception ex ) {
            flash.message = grailsApplication.metadata[ 'app.file.format' ]
            redirect( action:assetImport, params:[projectId:projectId] )
        }
    }
    /*
     * download data form Asset table into Excel file
     */
    def export = {

        //get project Id
        def projectId = params["projectIdExport"]
        
        def project = Project.findById( projectId )
        if ( projectId == null || projectId == "") {
            flash.message = "Project Name is required"
            redirect( controller:"asset", action:"assetImport" )
        }
        def asset = Asset.findAllByProject( project )
        //get template Excel
        def workbook
        def book
        try {
        	// Statements to get context details
        	def tempProtocol = request.getProtocol()
        	def protocol = tempProtocol.substring(0,tempProtocol.indexOf("/"))
        	def serverName = request.getServerName() 
        	def serverPort = request.getServerPort()
        	// construct application URL
        	def appUrl = protocol + "://" + serverName + ":" + serverPort + "/" + grailsApplication.metadata['app.name']
        	// get connection
        	def url = new URL( appUrl + "/templates/ServerListExample.xls" )
        	HttpURLConnection con = url.openConnection(); 
            workbook = Workbook.getWorkbook( con.getInputStream() )
            
            //set MIME TYPE as Excel
            response.setContentType( "application/vnd.ms-excel" )
            response.setHeader( "Content-Disposition", "attachment; filename=ServerListExample.xls" )

            //create workbook and sheet
            book = Workbook.createWorkbook( response.getOutputStream(), workbook )
            def sheetNo = 1
            def sheet = book.getSheet( sheetNo )

            // TODO : Use the column map that is shared between both import and export.

            def map = [ "Server":null, "Type":null, "S/N":null, "AssetTag":null ]
            //check for column

            def col = sheet.getColumns()

            //calling method to check for Header

            def checkCol = checkHeader( col, map, sheet )

            // TODO : The logic that reads the sheet should be able to be shared between import and export - refactor this out

            // Statement to check Headers if header are not found it will return Error message
            if ( checkCol == false ) {

                flash.message = " Column Headers not found, Please check it. "
                redirect( controller:"asset", action:"assetImport" )

            } else {

                //update data from asset table to EXCEL
                def k = 0
                for ( int r = 1; r < asset.size(); r++ ) {
                    
                    def assetName = new Label( map["Server"], r, asset.assetName[k] )                    
                    sheet.addCell( assetName )
                    def assetType
                    
                    // Statement to check null values
                   
                    if( asset.assetType[k] != null ) {                       
                        assetType = new Label( map["Type"], r, "${asset.assetType[k]}" )
                    } else {                        
                        assetType = new Label( map["Type"], r, "" )
                    }                   
                    sheet.addCell( assetType )

                    def serialNumber = new Label( map["S/N"], r, asset.serialNumber[k] )
                    sheet.addCell( serialNumber )

                    def assetTag = new Label( map["AssetTag"], r, asset.assetTag[k] )
                    sheet.addCell( assetTag )

                    k++
                }


                book.write()
                book.close()
                render( view: "assetImport" )
            }
        } catch( Exception fileEx ) {

            flash.message = "Excel template not found "
            redirect( controller:"asset", action:"assetImport" )

        }
    }
	// check the sheet headers and return boolean value
    def checkHeader( def col, def map, def sheet ){

        for ( int c = 0; c < col; c++ ) {
            def cellContent = sheet.getCell( c, 0 ).contents
            if( map.containsKey( cellContent ) ) {
                map.put( cellContent, c )
            }
        }
    	if( map.containsValue( null ) == true ) {

    		return false

    	} else {

    		return true
    	}
    }


    // the delete, save and update actions only accept POST requests
    def allowedMethods = [ delete:'POST', save:'POST', update:'POST' ]
    // return the list of asset records for project present in current scope.
    def list = {
        def project
        try {
            if ( !params.max ) params.max = 25
            def currProj = getSession().getAttribute( "CURR_PROJ" )
            def projId = currProj.CURR_PROJ
            if( projId == null ) {

                flash.message = " No Projects are Associated, Please select Project. "
                redirect( controller:"project",action:"list" )

            }
            project = Project.findById( projId )
        } catch ( Exception ex ) { }
        def query = "from PartyGroup as p where p.partyType = 'COMPANY' "
        def partyGroupList = PartyGroup.findAll( query )
        //get asset list for project present in current scope.        
        return [ assetInstanceList: Asset.findAllByProject( project, params ), project:project, partyGroupList:partyGroupList ]
    }
    // return asset details
    def show = {
        def assetInstance = Asset.get( params.id )

        if ( !assetInstance ) {
            flash.message = "Asset not found with id ${params.id}"
            redirect( action:list )
        }
        else { return [ assetInstance : assetInstance ] }
    }
    // delete asset details
    def delete = {
        def assetInstance = Asset.get( params.id )
        if( assetInstance ) {
            assetInstance.delete()
            flash.message = "Asset ${params.id} deleted"
            redirect( action:list )
        }
        else {
            flash.message = "Asset not found with id ${params.id}"
            redirect( action:list )
        }
    }
    // provide edit form with asset details
    def edit = {
        def assetInstance = Asset.get( params.id )

        if ( !assetInstance ) {
            flash.message = "Asset not found with id ${params.id}"
            redirect(action:list)
        } else {
            return [ assetInstance : assetInstance ]
        }
    }
    // update asset details
    def update = {
        def assetInstance = Asset.get( params.id )
        if ( assetInstance ) {
            assetInstance.properties = params
            if( !assetInstance.hasErrors() && assetInstance.save() ) {
                flash.message = "Asset ${params.id} updated"
                redirect( action:show,id:assetInstance.id )
            } else {
                render( view:'edit', model:[assetInstance:assetInstance] )
            }
        }
        else {
            flash.message = "Asset not found with id ${params.id}"
            redirect( action:edit, id:params.id )
        }
    }
    // provide asset create form
    def create = {
        def assetInstance = new Asset()
        assetInstance.properties = params
        return ['assetInstance':assetInstance]
    }
    // save asset details
    def save = {
        def assetInstance = new Asset( params )
        def currProj = getSession().getAttribute( "CURR_PROJ" )
        def projectInstance
        def projectId = currProj.CURR_PROJ
        if( projectId != null ) {
            projectInstance = Project.findById( projectId )
            assetInstance.project = projectInstance
        }
        if ( !assetInstance.hasErrors() && assetInstance.save() ) {
            flash.message = "Asset ${assetInstance.id} created"
            redirect( action:list, id:assetInstance.id )
        } else {
            render( view:'create', model:[assetInstance:assetInstance] )
        }
    }
    // remote link for asset dialog
    def editShow = {
        
        def items = []
        def assetInstance = Asset.get( params.id )
        // def partyGroupInstance = PartyGroup.get(assetInstance.owner.id)
        if( assetInstance.assetType != null && assetInstance.owner != null ){
        	items = [id:assetInstance.id, project:assetInstance.project.name, projectId:assetInstance.project.id, assetType:assetInstance.assetType, assetTypeId:assetInstance.assetType.id, assetOwner:assetInstance.owner.name, assetOwnerId:assetInstance.owner.id, assetTag:assetInstance.assetTag, assetName:assetInstance.assetName, serialNumber:assetInstance.serialNumber, deviceFunction:assetInstance.deviceFunction ]
        	
        } else if( assetInstance.assetType != null && assetInstance.owner == null ) {
        	items = [id:assetInstance.id, project:assetInstance.project.name, projectId:assetInstance.project.id, assetType:assetInstance.assetType, assetTypeId:assetInstance.assetType.id, assetTag:assetInstance.assetTag, assetName:assetInstance.assetName, serialNumber:assetInstance.serialNumber, deviceFunction:assetInstance.deviceFunction ]
            
        }else if( assetInstance.assetType == null && assetInstance.owner != null ){
        	items = [id:assetInstance.id, project:assetInstance.project.name, projectId:assetInstance.project.id, assetOwner:assetInstance.owner.name, assetOwnerId:assetInstance.owner.id, assetTag:assetInstance.assetTag, assetName:assetInstance.assetName, serialNumber:assetInstance.serialNumber, deviceFunction:assetInstance.deviceFunction ]
        }
        else{
        	items = [id:assetInstance.id, project:assetInstance.project.name, projectId:assetInstance.project.id, assetTag:assetInstance.assetTag, assetName:assetInstance.assetName, serialNumber:assetInstance.serialNumber, deviceFunction:assetInstance.deviceFunction ]
        	
        }
        render items as JSON
        
    }
    // update ajax overlay 
    def updateAsset = {
    		 
        def assetDialog= params.assetDialog.split(',')
        
        def assetItems = []
        def assetInstance = Asset.get( assetDialog[0] )
        def assetType=AssetType.findById( assetDialog[1] )
        assetInstance.assetType = assetType
        assetInstance.assetName = assetDialog[2]
        assetInstance.assetTag = assetDialog[3]
        assetInstance.serialNumber= assetDialog[4]
        assetInstance.deviceFunction = assetDialog[5]
        if( assetDialog[6] != "" && assetDialog[6] != 'null' ){
        	def PartyGroupInstance = PartyGroup.findById(assetDialog[6])
    	    assetInstance.owner = PartyGroupInstance
        }else{
            assetInstance.owner = null
        }
        assetInstance.save()

        if( assetInstance.assetType == null ) {
            assetItems = [id:assetInstance.id, project:assetInstance.project.name, projectId:assetInstance.project.id, assetTag:assetInstance.assetTag, assetName:assetInstance.assetName, serialNumber:assetInstance.serialNumber, deviceFunction:assetInstance.deviceFunction ]
        } else {
            assetItems = [id:assetInstance.id, project:assetInstance.project.name, projectId:assetInstance.project.id, assetType:assetInstance.assetType, assetTypeId:assetInstance.assetType.id, assetTag:assetInstance.assetTag, assetName:assetInstance.assetName, serialNumber:assetInstance.serialNumber, deviceFunction:assetInstance.deviceFunction ]
        }

        render assetItems as JSON

    }

}
