import org.hibernate.Hibernate
import java.sql.Blob
import com.tdssrc.grails.GormUtil

/**
 * Used to persist the logo that is presented in the UI at the project level
 */
class ProjectLogo {

    String name
    Blob partnerImage    
    Project project
    
	static constraints = {
        name()
        partnerImage(nullable: true)       
        project( nullable:false)
    }

    static mapping  = {
        version true
        id column: 'project_logo_id'        
    }

    static transients = [ 'size', 'data' ]
   
    def setData(InputStream is, long length) {
        partnerImage = Hibernate.createBlob(is, length)
    }
      
    Long getSize() {
        return partnerImage?.length() ?: 0
    }
   
    /**
     * A factory method to create/update a ProjectLogo object from an File which will be associated to a project. This will 
     * attempt to lookup an existing ProjectLogo by project first and only create one if it doesn't already exist. It will then
     * update the object with the content from the file object.
     *
     * @param project - the project to save logo to
     * @param file - the File resource to reference
     * @return the ProjectLogo object
     */
    static ProjectLogo createOrUpdate(Project project, Object file) {
        assert file
        assert project
       
        def origFileName = file.originalFilename
        def slashIndex = Math.max(origFileName.lastIndexOf("/"),origFileName.lastIndexOf("\\"))
        if (slashIndex > -1) {
            origFileName = origFileName.substring(slashIndex + 1)
        }
       
        ProjectLogo pl = ProjectLogo.findByProject(project)
        if (! pl) {
            pl = new ProjectLogo(name: origFileName, project: project)
        }

        pl.setData(file.inputStream, file.size)

        if (! pl.save(flush:true)) {
            // TODO : JPM : 3/3/2016 : need to figure out how we are going to handle the failure case for createOrUpdate()
            println "createOrUpdate() unable to save ProjectLogo for project $project : ${GormUtil.allErrorsString(pl)}"
        }

        return pl
    }
    
 }