import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*
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
        render( view:"assetImport" )
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
        def projectId = params["projectName.id"]

        if ( projectId == null ) {
            flash.message = "Project Name is required"
            redirect( controller:"asset", action:"assetImport" )
        }

        def project = Project.findById( projectId )
        //delete previous records existed for Project

        def assetDelete = Asset.executeUpdate("delete from Asset a where a.project = $project.id " )

        // get File
        MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request;
        CommonsMultipartFile file = ( CommonsMultipartFile ) mpr.getFile("file");

        // create workbook
        def workbook
        def sheet
        def sheetNo = 1
        def map = [ "Server":null, "Type":null, "S/N":null, "AssetTag":null ]
        try{
            workbook = Workbook.getWorkbook( file.inputStream )
            sheet = workbook.getSheet( sheetNo )

            // TODO : All columns should be done using maps as this will get unwieldly to manage with 20+ columns.  Both the import and
            // export should use the same map.

            //check for column
            def col = sheet.getColumns()
            def checkCol = checkHeader( col, map, sheet )

            // Statement to check Headers if header are not found it will return Error message

            // TODO : map here too.
            if ( checkCol == false ) {
                flash.message = " Column Headers not found, Please check it."
                redirect( controller:"asset", action:"assetImport" )

            } else {
                def added = 0
                def skipped = []
                for ( int r = 1; r < sheet.rows; r++ ) {
                    // get fields
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
                    }
                }

                // generate error message
                flash.message = " ${added} Asset added. "
                if ( skipped.size() > 0 ) {
                    flash.message += "  Rows ${skipped.join(', ')} were skipped because they were incomplete."
                }
                workbook.close()
                redirect( controller:"asset", action:"list" )
            }
        } catch( Exception ex ) {
            flash.message = grailsApplication.metadata[ 'app.file.format' ]
            redirect( controller:"asset", action:"assetImport" )
        } 
    }
    /*
     * download data form Asset table into Excel file
     */
    def export = {

        //get project Name
        def projectId = params["projectName.id"]
        def project = Project.findById( projectId )
        if ( projectId == null ) {
            flash.message = "Project Name is required"
            redirect( controller:"asset", action:"assetExport" )
        }
        def asset = Asset.findAllByProject( project )
        //get template Excel
        def workbook
        def book
        try{
            workbook = Workbook.getWorkbook( new File( grailsApplication.metadata['app.file.path'] ) )
            
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
                redirect( controller:"asset", action:"assetExport" )

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
                render( view: "assetExport" )
            }
        } catch( Exception fileEx ) {

            flash.message = "Excel template not found "
            redirect( controller:"asset", action:"assetExport" )

        }
    }
	// check the sheet headers and return boolean value
    def checkHeader( def col, def map, def sheet ){

        for ( int c = 0; c < col; c++ ) {
            def cellContent = sheet.getCell( c, 0 ).contents
            if( map.containsKey( cellContent ) ) {
                map.put( cellContent,c )
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
    // return the list of asset records
    def list = {
        if ( !params.max ) params.max = 25
        [ assetInstanceList: Asset.list( params ) ]
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
    // delate asset details
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
        if ( !assetInstance.hasErrors() && assetInstance.save() ) {
            flash.message = "Asset ${assetInstance.id} created"
            redirect( action:show, id:assetInstance.id )
        } else {
            render( view:'create', model:[assetInstance:assetInstance] )
        }
    }
}
