import com.tdssrc.grails.GormUtil
import org.jsecurity.SecurityUtils
class Project extends PartyGroup {

	String projectCode
	String description
	String trackChanges = 'Y'
	Date startDate	// Date that the project will start
	Date completionDate	// Date that the project will finish
	PartyGroup client
    String workflowCode
    String projectType = "Standard"
	Integer lastAssetId
    
	// Custom field labels
    	String custom1
    	String custom2
    	String custom3
    	String custom4
    	String custom5
    	String custom6
    	String custom7
    	String custom8
		
	static hasMany = [ dataTransferBatch : DataTransferBatch ]
	
	static constraints = {
		name( ) // related party Group
		projectCode( blank:false, nullable:false,unique:'client' )
		client( nullable:false )
		description( blank:true, nullable:true )
		trackChanges( blank:false, nullable:false, inList:['Y', 'N'] )
		startDate( nullable:true )
		completionDate( nullable:true )
		dateCreated( ) // related to party
		lastUpdated( ) // related to party
		workflowCode( blank:false, nullable:false )
		projectType( blank:false, nullable:false, inList:['Standard', 'Template', 'Demo'] )
		// custom fields
		custom1( blank:true, nullable:true )
		custom2( blank:true, nullable:true )
		custom3( blank:true, nullable:true )
		custom4( blank:true, nullable:true )
		custom5( blank:true, nullable:true )
		custom6( blank:true, nullable:true )
		custom7( blank:true, nullable:true )
		custom8( blank:true, nullable:true )
		lastAssetId( blank:true, nullable:true )
	}

	static mapping  = {
		version true
		autoTimestamp false
		id column: 'project_id'
		columns {
			trackChanges sqlType: 'char(1)'
			projectCode sqlType: 'varchar(20)', index: 'project_projectcode_idx', unique: true
		}
	}
	
	String toString() {
		"$projectCode : $name"
	}
	
	static def getActiveProject( timeNow ){
		def project = Project.createCriteria().list {
			and {
				ge("completionDate", timeNow)
			}
		}
		return  project
	}
	static def getCompletedProject( timeNow ){
		def project = Project.createCriteria().list {
			and {
				le("completionDate", timeNow)
			}
		}
		return  project
	}
}
