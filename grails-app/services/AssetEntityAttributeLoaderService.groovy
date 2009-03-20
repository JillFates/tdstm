import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavEntityType
import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
class AssetEntityAttributeLoaderService {

    boolean transactional = true
    def eavAttribute
   //upload records in to EavAttribute table from from AssetEntity.xls
	def uploadEavAttribute = {def stream ->
		//get Entity TYpe
		def entityTypeId
        def entityType
        try {
        	entityTypeId = 1
        	if ( entityTypeId == null || entityTypeId == "" ) {
       	}
        entityType = EavEntityType.findById( entityTypeId )
        }catch ( Exception ex ) {
        	ex.printStackTrace
        }	
        // create workbook
        def workbook
        def sheet
        def sheetNo = 1
        def map = [ "Attribute Code":null, "Label":null, "Type":null, "sortOrder":null, "Note":null, "Input type":null, "Required":null, "Unique":null, "Business Rules (hard/soft errors)":null, "Options":null ]
        try{
        	workbook = Workbook.getWorkbook( stream )
        	sheet = workbook.getSheet( 0 )
        	// export should use the same map.
        	//check for column
        	def col = sheet.getColumns()
        	def checkCol = checkHeader( col, map, sheet )
        	// Statement to check Headers if header are not found it will return Error message
        	// TODO : map here too.
        	if ( checkCol == false ) {
        		return false
            }else{
            	def added = 0
            	def skipped = []
            	for ( int r = 1; r < sheet.rows; r++ ) {
            		// get fields
            		def applicationCode = sheet.getCell( map["Attribute Code"], r ).contents
            		def backEndType = sheet.getCell( map["Type"], r ).contents                    
            		def eavEntityTypeObj = EavEntityType.findById(entityTypeId)                   
            		def frontEndInput = sheet.getCell( map["Input type"], r ).contents
            		def fronEndLabel = sheet.getCell( map["Label"], r ).contents
            		def isRequired = sheet.getCell( map["Required"], r ).contents
            		def isUnique = sheet.getCell( map["Unique"], r ).contents
            		def note = sheet.getCell( map["Note"], r ).contents
            		def sortOrder = sheet.getCell( map["sortOrder"], r ).contents
            		def validation = sheet.getCell( map["Business Rules (hard/soft errors)"], r ).contents
            		def options = sheet.getCell( map["Options"], r ).contents
            		// save data in to db and check for added rows and skipped
            		eavAttribute = new EavAttribute(attributeCode:applicationCode,
            				note:note,
            				backendType:"varchar",
            				frontendInput:"select",
            				entityType:eavEntityTypeObj,
            				frontendLabel:fronEndLabel,
            				defaultValue:"null",
            				validation:validation,
            				isRequired:0,
            				isUnique:0,
            				sortOrder:sortOrder 
            		)
            		// TODO : This logic will ALWAY return true since the EavAttribute is created.  It should be testing asset.save() and not called above.
            		if ( eavAttribute ) {
            			eavAttribute.save()
            			if(options != ""){
            				String s3 = options;
                        	String[] eavAttributeOptions = null
                        	eavAttributeOptions = s3.split(",");
                        	for( int attributeOption = 0; attributeOption < eavAttributeOptions.length; attributeOption++ ){
                        		def eavAttributeOption = new EavAttributeOption( 
                        				attribute:eavAttribute,
                        				sortOrder:sortOrder,
                        				value:eavAttributeOptions[attributeOption].trim()
                        		)
                        		if( eavAttributeOption ){
                        			eavAttributeOption.save()
                        		}
                        	}
                        }
                        added++
            		}else{
            			skipped += ( r +1 )
            		}
            	}
            	workbook.close()
            	return true
            }
        }
        catch( Exception ex ) {
        	return false
        } 
	}
    //  check the sheet headers and return boolean value
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

}
