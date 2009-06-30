import org.hibernate.lob.BlobImpl
import java.sql.Blob
class ProjectLogo {

    String name
    Blob partnerImage    
    Project project
    PartyGroup party
    
	static constraints = {
        name()
        partnerImage(nullable: true)       
        project( nullable:false)
        party( nullable:true)
    }

   
    def setData(InputStream is, long length) {
        partnerImage = new BlobImpl(is, (int)length)
    }
   
    /*def getData() {
        return partnerImage?.binaryStream
    }*/
   
    static transients = [ 'size', 'data' ]

    Long getSize() {
        return partnerImage?.length() ?: 0
    }
   
    /*def render(def response) {
        response.contentType = "application/octet-stream"
        response.outputStream << data
    }*/
   
    static fromUpload(def file) {
        if(!file) return new ProjectLogo()
       
        def origFileName = file.originalFilename
        def slashIndex = Math.max(origFileName.lastIndexOf("/"),origFileName.lastIndexOf("\\"))
        if(slashIndex > -1) origFileName = origFileName.substring(slashIndex + 1)
       
        def doc = new ProjectLogo(name: origFileName)
        doc.setData(file.inputStream, file.size)
        return doc
    }
    
    static mapping  = {
		version true
		id column: 'project_logo_id'		
	}
 }