import com.tdssrc.grails.GormUtil
import org.apache.shiro.SecurityUtils

class Project extends PartyGroup {

	def static final DEFAULT_PROJECT_ID = 2

	String projectCode
	String description
	String trackChanges = 'Y'
	Date startDate	// Date that the project will start
	Date completionDate	// Date that the project will finish
	PartyGroup client
    String workflowCode
    String projectType = "Standard"
	Integer lastAssetId
	Integer runbookOn=1		// Flag that indicates that the project should use the runbook mode for various screens
    Integer customFieldsShown = 8
	String depConsoleCriteria 
    
	// Custom field labels
    	String custom1
    	String custom2
    	String custom3
    	String custom4
    	String custom5
    	String custom6
    	String custom7
    	String custom8
    	String custom9
		String custom10
		String custom11
		String custom12
		String custom13
		String custom14
		String custom15
		String custom16
		String custom17
		String custom18
		String custom19
		String custom20
		String custom21
		String custom22
		String custom23
		String custom24
		
	static hasMany = [ dataTransferBatch : DataTransferBatch ]
	
	static constraints = {
		name( ) // related party Group
		projectCode( blank:false, nullable:false,unique:'client' )
		client( nullable:false )
		description( blank:true, nullable:true )
		trackChanges( blank:false, nullable:false, inList:['Y', 'N'] )
		startDate( nullable:true )
		completionDate( nullable:false )
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
		custom9( blank:true, nullable:true )
		custom10( blank:true, nullable:true )
		custom11( blank:true, nullable:true )
		custom12( blank:true, nullable:true )
		custom13( blank:true, nullable:true )
		custom14( blank:true, nullable:true )
		custom15( blank:true, nullable:true )
		custom16( blank:true, nullable:true )
		custom17( blank:true, nullable:true )
		custom18( blank:true, nullable:true )
		custom19( blank:true, nullable:true )
		custom20( blank:true, nullable:true )
		custom21( blank:true, nullable:true )
		custom22( blank:true, nullable:true )
		custom23( blank:true, nullable:true )
		custom24( blank:true, nullable:true )
		customFieldsShown( nullable:false, inList:[0, 4 ,8, 12, 16 ,20, 24] )
		lastAssetId( nullable:true )
		runbookOn(nullable:true)
		depConsoleCriteria( blank:true, nullable:true )
	}

	static mapping  = {
		version true
		autoTimestamp false
		id column: 'project_id'
		columns {
			trackChanges sqlType: 'char(1)'
			projectCode sqlType: 'varchar(20)', index: 'project_projectcode_idx', unique: true
			runbookOn sqlType: 'tinyint'
			depConsoleCriteria sqlType : 'TEXT'
		}
	}
	
	static transients = [ 'isDefaultProject', 'getDefaultProject', 'readDefaultProject' ]

	String toString() {
		"$projectCode : $name"
	}
	
	/**
	 * Used to retrieve the default Project for the appliction wit the get operator
	 * @return Project - the default Project object
	 */
	static Project getDefaultProject() {
		Project.get( DEFAULT_PROJECT_ID )
	}

	/**
	 * Used to retrieve the default Project for the appliction with the read operator
	 * @return Project - the default Project object
	 */
	static Project readDefaultProject() {
		Project.read( DEFAULT_PROJECT_ID )
	}

	/**
	 * Can be used to determine if this is the default project for the application
	 * @return Boolean - true if the project is the default
	 */
	Boolean isDefaultProject() {
		id == DEFAULT_PROJECT_ID
	}
}
