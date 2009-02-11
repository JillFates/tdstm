import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*
class AssetController {
    
    def index = { redirect(action:list,params:params) }
    
    //upload , export
    /*
     * render import form
     */
    def assetImport= { 
    		render(view:"assetImport")
    }
    /*
     * render export form
     */
    def assetExport= { 
    		render(view:"assetExport")
    }
    /*
     * upload excel file into Asset table
     */
    def upload = {  
   		 //get project Name 
		     def projectId=params["projectName.id"]
		     if(projectId == null){
		    	 flash.message="Project Name is required"
		    	 redirect(controller:"asset", action:"assetImport")
		     }
		     def project=Project.find("from Project p where p.id="+projectId)
		     
		    
		     
		     
		     
		     //delete previous records existed for Project
		     def dpDelete=Asset.executeUpdate("delete from Asset a where a.projectName='${project.id}'")
		     
		     // get File
		     MultipartHttpServletRequest mpr = (MultipartHttpServletRequest)request;  
		     CommonsMultipartFile file = (CommonsMultipartFile) mpr.getFile("file");  
		       
		     // create workbook 
		     Workbook workbook 
		     Sheet sheet
		     def sheetNo=1
		     try{
   		      workbook = Workbook.getWorkbook(file.inputStream)  
   		      sheet = workbook.getSheet(sheetNo)
		     }catch(Exception ex){
		    	 flash.message=grailsApplication.metadata['app.file.format']
		    	 redirect(controller:"asset", action:"assetImport")
		     }
		     
		     //check for column
		    def serverColNo
		    def typeColNo
		    def snColNo
		    def assetTagColNo
		    
		    def col=sheet.getColumns()
		    for(int c=0;c<col;c++){
		    	def cellContent=sheet.getCell(c, 0).contents
		    	if(cellContent.equalsIgnoreCase("Server")){
		    		 serverColNo=c;    		    		
		    	}else if(cellContent.equalsIgnoreCase("Type")){
		    		 typeColNo=c;
		    	}else if(cellContent.equalsIgnoreCase("S/N")){
		    		 snColNo=c;
		    	}else if(cellContent.equalsIgnoreCase("AssetTag")){
		    		 assetTagColNo=c;
		    	}
		    	
		    }
		     
		    // Statement to check Headers if header are not found it will return Error message
		    
		    def headerSkipped =new StringBuffer()
			   
		    if(serverColNo == null ){
		    	headerSkipped.append("Server")
		    }
		    if(typeColNo == null){
		    	headerSkipped.append(" Type")
		    }
		    if(snColNo == null){
		    	headerSkipped.append(" S/N")
		    }
		    if(assetTagColNo == null){
		    	headerSkipped.append("AssetTag")
		    }
		   
		    if(serverColNo == null  || typeColNo == null || snColNo == null || assetTagColNo == null){
		    	
		    	 flash.message="  Columns ${headerSkipped} not found, Please check it"
  		    	 redirect(controller:"asset", action:"assetImport")
		    }
		    def added = 0  
		    def skipped = []  
		    for (int r = 1; r < sheet.rows; r++) {  
		     // get fields  
		    def assetName = sheet.getCell(serverColNo, r).contents    		  
		    def assetType = sheet.getCell(typeColNo, r).contents  
		    def assetTypeObj=AssetType.find("from AssetType where assetType='${assetType}'")    		    
		    def serialNumber =sheet.getCell(snColNo, r).contents 
		    def assetTag = sheet.getCell(assetTagColNo, r).contents    
		    
		     
		    // save data in to db and check for added rows and skipped
		    def asset =new Asset( 
		    		
	    		       projectName:project,  
	    		       assetType:assetTypeObj,  
	    		       assetName:"${assetName}",  
	    		       assetTag:"${assetTag}",    		           		            		        
	    		       serialNumber:"${serialNumber}",
	    		       deviceFunction:"").save()
		    if (asset) {  
		      added++  
		    } else {  
		      skipped += (r +1)  
		     }  
		   } 
		    
		    workbook.close()
    
		    
    
		    // generate error message  
		    flash.message = "${added} records added into Asset."  
		    if (skipped.size() > 0) {  
		     flash.message += "  Rows ${skipped.join(', ')} were skipped because they were incomplete"  
		    }
		    redirect(controller:"asset", action:"list")  
}
    /*
     * download data form Asset table into Excel file
     */
    def export={
    		

    		 //get project Name 
    		def projectId=params["projectName.id"]
    		if(projectId == null){
		    	 flash.message="Project Name is required"
		    	 redirect(controller:"asset", action:"assetExport")
		    }
    		def asset=Asset.findAll("from Asset where projectName="+projectId)
    		
    		//get template Excel
    		def workbook
    		try{
    			workbook = Workbook.getWorkbook(new File(grailsApplication.metadata['app.file.path']));
    		}catch(Exception fileEx){
    			flash.message="Excel template not found "
      		    redirect(controller:"asset", action:"assetExport")
    		}
    		 
    		//set MIME TYPE as Excel
    		response.setContentType("application/vnd.ms-excel")
    		response.setHeader("Content-Disposition", "attachment; filename=ServerListExample.xls")
    		
    		//create workbook and sheet
    		def book = Workbook.createWorkbook(response.getOutputStream(), workbook)
    		def sheetNo=1
    		def sheet = book.getSheet(sheetNo)
    		
    		 //check for column
    		def serverColNo
		    def typeColNo
		    def snColNo
		    def assetTagColNo
		    
		    def col=sheet.getColumns()
		    for(int c=0;c<col;c++){
		    	// celcontent will return Cell Header 
		    	def cellContent=sheet.getCell(c, 0).contents
		    	if(cellContent.equalsIgnoreCase("Server")){
		    		 serverColNo=c;    		    		
		    	}else if(cellContent.equalsIgnoreCase("Type")){
		    		 typeColNo=c;
		    	}else if(cellContent.equalsIgnoreCase("S/N")){
		    		 snColNo=c;
		    	}else if(cellContent.equalsIgnoreCase("AssetTag")){
		    		 assetTagColNo=c;
		    	}
		    	
		    }
		   // Statement to check Headers if header are not found it will return Error message
		   def headerSkipped =new StringBuffer()
		   
		    if(serverColNo == null ){
		    	headerSkipped.append("Server")
		    }
		    if(typeColNo == null){
		    	headerSkipped.append(" Type")
		    }
		    if(snColNo == null){
		    	headerSkipped.append(" S/N")
		    }
		    if(assetTagColNo == null){
		    	headerSkipped.append("AssetTag")
		    }
		    if(serverColNo == null || typeColNo == null || snColNo == null || assetTagColNo == null){
		    	
		    	 flash.message="  Columns ${headerSkipped} not found, Please check it"
   		    	 redirect(controller:"asset", action:"assetExport")
		    	
		    }
    		
    		
   		//update data from asset table to EXCEL
   		def k=0   		
    		for (int r = 1; r < asset.size(); r++) { 
    			
    		    
    			def assetName = new Label(serverColNo, r, "${asset.assetName[k]}");
        		sheet.addCell(assetName);
        		def assetType
        		// Statement to check null values
        		if(asset.assetType[k] != null){
        			assetType = new Label(typeColNo, r, "${asset.assetType[k]}");
        		}else{
        			assetType = new Label(typeColNo, r, "");
        		}
        		sheet.addCell(assetType);
        		
        		def serialNumber = new Label(snColNo, r, "${asset.serialNumber[k]}");
        		sheet.addCell(serialNumber);
        		
        		def assetTag = new Label(assetTagColNo, r, "${asset.assetTag[k]}");
        		sheet.addCell(assetTag);
    		    
        		k++
    		}
    		

    		book.write();
    		
    		book.close();
    		
    		render(view:"assetExport")
    	   
    		
    }
    

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 25
        [ assetInstanceList: Asset.list( params ) ]
    }

    def show = {
        def assetInstance = Asset.get( params.id )

        if(!assetInstance) {
            flash.message = "Asset not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ assetInstance : assetInstance ] }
    }

    def delete = {
        def assetInstance = Asset.get( params.id )
        if(assetInstance) {
            assetInstance.delete()
            flash.message = "Asset ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "Asset not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def assetInstance = Asset.get( params.id )

        if(!assetInstance) {
            flash.message = "Asset not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ assetInstance : assetInstance ]
        }
    }

    def update = {
        def assetInstance = Asset.get( params.id )
        if(assetInstance) {
            assetInstance.properties = params
            if(!assetInstance.hasErrors() && assetInstance.save()) {
                flash.message = "Asset ${params.id} updated"
                redirect(action:show,id:assetInstance.id)
            }
            else {
                render(view:'edit',model:[assetInstance:assetInstance])
            }
        }
        else {
            flash.message = "Asset not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def assetInstance = new Asset()
        assetInstance.properties = params
        return ['assetInstance':assetInstance]
    }

    def save = {
        def assetInstance = new Asset(params)
        if(!assetInstance.hasErrors() && assetInstance.save()) {
            flash.message = "Asset ${assetInstance.id} created"
            redirect(action:show,id:assetInstance.id)
        }
        else {
            render(view:'create',model:[assetInstance:assetInstance])
        }
    }
}
