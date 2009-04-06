import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*
import grails.converters.JSON
import org.jsecurity.SecurityUtils
import com.tdssrc.eav.*
class AssetEntityController {
    
    //TODO : Fix indentation
	def missingHeader = ""
	def added = 0
	def skipped = []
    def index = {
		redirect( action:list, params:params )
	}

    //upload , export
    /*
     * render import export form
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
    		assetsByProject = AssetEntity.findAllByProject(project)
    	}
    	def	dataTransferBatchs = DataTransferBatch.count()
        render( view:"importExport", model : [ assetsByProject: assetsByProject, projectId: projectId, moveBundleInstanceList: moveBundleInstanceList, dataTransferSetImport: dataTransferSetImport, dataTransferSetExport: dataTransferSetExport, dataTransferBatchs: dataTransferBatchs ] )
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
                	
                	missingHeader = missingHeader.replaceFirst(",","")
                    flash.message = " Column Headers : ${missingHeader} not found, Please check it."
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
                                    //dataTransferValue.save()
                                    if ( dataTransferValue.save() ) {                                    	
                                        added++
                                    } else {
                                        skipped += ( r +1 )
                                    }
                                    
                                }
                            }
                	
                            /*// get fields
                            def assetName = sheet.getCell( map["Server"], r ).contents
                            def assetType = sheet.getCell( map["Type"], r ).contents
                            def assetTypeObj = AssetType.findById(assetType)
                            def serialNumber = sheet.getCell( map["S/N"], r ).contents
                            def assetTag = sheet.getCell( map["AssetTag"], r ).contents


                            // save data in to db and check for added rows and skipped
                            def asset = new 	(

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
                    if (skipped.size() > 0) {
                    	flash.message = " File Uploaded Successfully with ${added} Assets and Skipped are ${skipped}. "
                    } else {
                    	flash.message = " File Uploaded Successfully with ${added} Assets. "
                    }
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
        def dataTransferSet = params.dataTransferSet
        def bundle = params.bundle
        def dataTransferSetInstance = DataTransferSet.findById( dataTransferSet )
        def dataTransferAttributeMap = DataTransferAttributeMap.findAllByDataTransferSet( dataTransferSetInstance )
        
        def project = Project.findById( projectId )
        if ( projectId == null || projectId == "") {
            flash.message = "Project Name is required"
            redirect( action:assetImport, params:[projectId:projectId] )
        }
        def asset = AssetEntity.findAllByProject( project )
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
        	def templateFilePath = appUrl + dataTransferSetInstance.templateFilename
        	def url = new URL( templateFilePath )
        	HttpURLConnection con = url.openConnection(); 
            workbook = Workbook.getWorkbook( con.getInputStream() )
            
            //set MIME TYPE as Excel
            response.setContentType( "application/vnd.ms-excel" )
            response.setHeader( "Content-Disposition", "attachment; filename=ServerListExample.xls" )

            //create workbook and sheet
            book = Workbook.createWorkbook( response.getOutputStream(), workbook )
            //def sheetNo = 1
            //def sheet = book.getSheet( sheetNo )
            def sheet
            // TODO : Use the column map that is shared between both import and export.

            //def map = [ "Server":null, "Type":null, "S/N":null, "AssetTag":null ]
            //check for column
            def map = [:]
            def serverMap = [:]        
            def dataTransferAttributeMapSheetName
            dataTransferAttributeMap.eachWithIndex { item, pos ->
    		map.put( item.columnName, null )
    		serverMap.put( "sheetName", (item.sheetName).trim() )
            }
            def sheetNames = book.getSheetNames()  
            def flag = 0
            for( int i=0;  i < sheetNames.length; i++ ) {           	
            	
                if ( serverMap.containsValue( sheetNames[i].trim()) ) {
                    flag = 1
                    sheet = book.getSheet( sheetNames[i] )
                }
            	
      		}
            if( flag == 0 ) {
            	
            	flash.message = " Sheet not found, Please check it."
                redirect( action:assetImport, params:[projectId:projectId] ) 
                
            } else {
            def col = sheet.getColumns()

            //calling method to check for Header

            def checkCol = checkHeader( col, map, sheet )
            // TODO : The logic that reads the sheet should be able to be shared between import and export - refactor this out

            // Statement to check Headers if header are not found it will return Error message
            if ( checkCol == false ) {
                	
                	missingHeader = missingHeader.replaceFirst(",","")
                    flash.message = " Column Headers : ${missingHeader} not found, Please check it."
                    redirect( action:assetImport, params:[projectId:projectId] )

             } else {

                //update data from asset table to EXCEL
                def k = 0
                for ( int r = 1; r < asset.size(); r++ ) {
                    
                    /*def assetName = new Label( map["Server"], r, asset.assetName[k] )                    
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

                    k++*/
                }


                book.write()
                book.close()
                render( view: "importExport" )
             }
            }
        } catch( Exception fileEx ) {

            flash.message = "Excel template not found "
            redirect( action:assetImport, params:[projectId:projectId] )

        }
    }
	// check the sheet headers and return boolean value
    def checkHeader( def col, def map, def sheet ){
    	
        for ( int c = 0; c < col; c++ ) {
            def cellContent = sheet.getCell( c, 0 ).contents
            if( map.containsKey( cellContent ) ) {
                map.put( cellContent, c )
            } else {
                missingHeader = missingHeader + ", " + cellContent
            }
        }
    	if( map.containsValue( null ) == true ) {
    		
    		return false

    	} else {

    		return true
    	}
    }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 15
        def projectId = params.projectId
        def project = Project.findById( projectId )
        def assetEntityInstanceList = AssetEntity.findAllByProject( project, params ) 
        [ assetEntityInstanceList: assetEntityInstanceList, projectId: projectId ]
    }   

    def delete = {
        def assetEntityInstance = AssetEntity.get( params.id )
        def projectId = params.projectId
        if(assetEntityInstance) {
            assetEntityInstance.delete()
            flash.message = "AssetEntity ${params.id} deleted"
            redirect(action:list, params:[projectId:projectId])
        }
        else {
            flash.message = "AssetEntity not found with id ${params.id}"
            redirect(action:list, params:[projectId:projectId])
        }
    }
    
    def save = {
        def assetEntityInstance = new AssetEntity(params)
        def projectId = params.projectId
        def projectInstance = Project.findById( projectId )
        assetEntityInstance.project = projectInstance
        assetEntityInstance.owner = projectInstance.client
        if(!assetEntityInstance.hasErrors() && assetEntityInstance.save()) {
            flash.message = "AssetEntity ${assetEntityInstance.id} created"
            redirect( action:list,id:assetEntityInstance.id,params:[projectId: projectId] )
        }
        else {
            render( view:'list',model:[assetEntityInstance:assetEntityInstance, projectId: projectId] )
        }
    }
    
    //remote link for asset entity dialog.
    def editShow = {
        
        def items = []
        def assetEntityInstance = AssetEntity.get( params.id )
        
        if( assetEntityInstance.assetType != null ){
        	items = [id:assetEntityInstance.id, model: assetEntityInstance.model, sourceLocation: assetEntityInstance.sourceLocation, targetLocation: assetEntityInstance.targetLocation, sourceRack: assetEntityInstance.sourceRack, targetRack: assetEntityInstance.targetRack, sourceRackPosition: assetEntityInstance.sourceRackPosition, targetRackPosition: assetEntityInstance.targetRackPosition, usize: assetEntityInstance.usize, manufacturer: assetEntityInstance.manufacturer, fiberCabinet: assetEntityInstance.fiberCabinet, hbaPort: assetEntityInstance.hbaPort, hinfo: assetEntityInstance.hinfo, ipAddress: assetEntityInstance.ipAddress, kvmDevice: assetEntityInstance.kvmDevice, kvmPort: assetEntityInstance.kvmPort, newOrOld: assetEntityInstance.newOrOld, nicPort: assetEntityInstance.nicPort, powerPort: assetEntityInstance.powerPort, remoteMgmPort: assetEntityInstance.remoteMgmPort, truck: assetEntityInstance.truck, project:assetEntityInstance.project.name, projectId:assetEntityInstance.project.id, assetType:assetEntityInstance.assetType, assetTypeId:assetEntityInstance.assetType.id, assetTag:assetEntityInstance.assetTag, assetName:assetEntityInstance.assetName, serialNumber:assetEntityInstance.serialNumber, , application:assetEntityInstance.application ]
       
        } else {
        	items = [id:assetEntityInstance.id, model: assetEntityInstance.model, sourceLocation: assetEntityInstance.sourceLocation, targetLocation: assetEntityInstance.targetLocation, sourceRack: assetEntityInstance.sourceRack, targetRack: assetEntityInstance.targetRack, sourceRackPosition: assetEntityInstance.sourceRackPosition, targetRackPosition: assetEntityInstance.targetRackPosition, usize: assetEntityInstance.usize, manufacturer: assetEntityInstance.manufacturer, fiberCabinet: assetEntityInstance.fiberCabinet, hbaPort: assetEntityInstance.hbaPort, hinfo: assetEntityInstance.hinfo, ipAddress: assetEntityInstance.ipAddress, kvmDevice: assetEntityInstance.kvmDevice, kvmPort: assetEntityInstance.kvmPort, newOrOld: assetEntityInstance.newOrOld, nicPort: assetEntityInstance.nicPort, powerPort: assetEntityInstance.powerPort, remoteMgmPort: assetEntityInstance.remoteMgmPort, truck: assetEntityInstance.truck, project:assetEntityInstance.project.name, projectId:assetEntityInstance.project.id, assetTag:assetEntityInstance.assetTag, assetName:assetEntityInstance.assetName, serialNumber:assetEntityInstance.serialNumber, application:assetEntityInstance.application ]
        }
        
        render items as JSON
        
    }
    
    //update ajax overlay
    def updateAssetEntity = {
    		 
        
    	def assetDialog = params.assetDialog.split(',')
        
        def assetItems = []
        def assetEntityInstance = AssetEntity.get( assetDialog[0] )
        assetEntityInstance.model = assetDialog[1]
        assetEntityInstance.sourceLocation = assetDialog[2]
        assetEntityInstance.targetLocation = assetDialog[3]
        assetEntityInstance.sourceRack = assetDialog[4]
        assetEntityInstance.targetRack = assetDialog[5]
        assetEntityInstance.sourceRackPosition = assetDialog[6]
        assetEntityInstance.targetRackPosition = assetDialog[7]
        assetEntityInstance.usize = assetDialog[8]
        assetEntityInstance.manufacturer = assetDialog[9]
        assetEntityInstance.fiberCabinet = assetDialog[10]
        assetEntityInstance.hbaPort = assetDialog[11]
        assetEntityInstance.hinfo = assetDialog[12]
        assetEntityInstance.ipAddress = assetDialog[13]
        assetEntityInstance.kvmDevice = assetDialog[14]
        assetEntityInstance.kvmPort = assetDialog[15]
        assetEntityInstance.newOrOld = assetDialog[16]
        assetEntityInstance.nicPort = assetDialog[17]
        assetEntityInstance.powerPort = assetDialog[18]
        assetEntityInstance.remoteMgmPort = assetDialog[19]
        assetEntityInstance.truck = assetDialog[20]
        def assetType=AssetType.findById( assetDialog[21] )
        assetEntityInstance.assetType = assetType        
        assetEntityInstance.assetName = assetDialog[22]
        assetEntityInstance.assetTag = assetDialog[23]
        assetEntityInstance.serialNumber= assetDialog[24]
        assetEntityInstance.application= assetDialog[25]
        assetEntityInstance.save()

        if( assetEntityInstance.assetType != null ){
        	assetItems = [id:assetEntityInstance.id, model: assetEntityInstance.model, sourceLocation: assetEntityInstance.sourceLocation, targetLocation: assetEntityInstance.targetLocation, sourceRack: assetEntityInstance.sourceRack, targetRack: assetEntityInstance.targetRack, sourceRackPosition: assetEntityInstance.sourceRackPosition, targetRackPosition: assetEntityInstance.targetRackPosition, usize: assetEntityInstance.usize, manufacturer: assetEntityInstance.manufacturer, fiberCabinet: assetEntityInstance.fiberCabinet, hbaPort: assetEntityInstance.hbaPort, hinfo: assetEntityInstance.hinfo, ipAddress: assetEntityInstance.ipAddress, kvmDevice: assetEntityInstance.kvmDevice, kvmPort: assetEntityInstance.kvmPort, newOrOld: assetEntityInstance.newOrOld, nicPort: assetEntityInstance.nicPort, powerPort: assetEntityInstance.powerPort, remoteMgmPort: assetEntityInstance.remoteMgmPort, truck: assetEntityInstance.truck, project:assetEntityInstance.project.name, projectId:assetEntityInstance.project.id, assetType:assetEntityInstance.assetType, assetTypeId:assetEntityInstance.assetType.id, assetTag:assetEntityInstance.assetTag, assetName:assetEntityInstance.assetName, serialNumber:assetEntityInstance.serialNumber, application:assetEntityInstance.application ]
       
        } else {
        	
        } 
        render assetItems as JSON

    }
}
