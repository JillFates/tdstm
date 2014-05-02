import com.tdssrc.grails.GormUtil
import org.apache.shiro.SecurityUtils
import com.tds.asset.AssetEntity
import com.tds.asset.Application
import com.tds.asset.Database
import com.tds.asset.Files

class Project extends PartyGroup {

	def static final DEFAULT_PROJECT_ID = 2
	def static final CUSTOM_FIELD_COUNT = 64
	
	static isDefaultProject(aProjectRef) {
		if (aProjectRef instanceof Project) {
			return aProjectRef.id == DEFAULT_PROJECT_ID
		} else {
			return aProjectRef == DEFAULT_PROJECT_ID
		}
	}
	
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
		String custom25
		String custom26
		String custom27
		String custom28
		String custom29
		String custom30
		String custom31
		String custom32
		String custom33
		String custom34
		String custom35
		String custom36
		String custom37
		String custom38
		String custom39
		String custom40
		String custom41
		String custom42
		String custom43
		String custom44
		String custom45
		String custom46
		String custom47
		String custom48
		String custom49
		String custom50
		String custom51
		String custom52
		String custom53
		String custom54
		String custom55
		String custom56
		String custom57
		String custom58
		String custom59
		String custom60
		String custom61
		String custom62
		String custom63
		String custom64
		
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
		custom25( blank:true, nullable:true )
		custom26( blank:true, nullable:true )
		custom27( blank:true, nullable:true )
		custom28( blank:true, nullable:true )
		custom29( blank:true, nullable:true )
		custom30( blank:true, nullable:true )
		custom31( blank:true, nullable:true )
		custom32( blank:true, nullable:true )
		custom33( blank:true, nullable:true )
		custom34( blank:true, nullable:true )
		custom35( blank:true, nullable:true )
		custom36( blank:true, nullable:true )
		custom37( blank:true, nullable:true )
		custom38( blank:true, nullable:true )
		custom39( blank:true, nullable:true )
		custom40( blank:true, nullable:true )
		custom41( blank:true, nullable:true )
		custom42( blank:true, nullable:true )
		custom43( blank:true, nullable:true )
		custom44( blank:true, nullable:true )
		custom45( blank:true, nullable:true )
		custom46( blank:true, nullable:true )
		custom47( blank:true, nullable:true )
		custom48( blank:true, nullable:true )
		custom49( blank:true, nullable:true )
		custom50( blank:true, nullable:true )
		custom51( blank:true, nullable:true )
		custom52( blank:true, nullable:true )
		custom53( blank:true, nullable:true )
		custom54( blank:true, nullable:true )
		custom55( blank:true, nullable:true )
		custom56( blank:true, nullable:true )
		custom57( blank:true, nullable:true )
		custom58( blank:true, nullable:true )
		custom59( blank:true, nullable:true )
		custom60( blank:true, nullable:true )
		custom61( blank:true, nullable:true )
		custom62( blank:true, nullable:true )
		custom63( blank:true, nullable:true )
		custom64( blank:true, nullable:true )
		customFieldsShown( nullable:false, inList:[0,4,8,12,16,20,24,28,32,36,40,44,48,52,56,60,64] )
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
	
	static transients = [ 'isDefaultProject', 'getDefaultProject', 'readDefaultProject', 'active', 'status' ]

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

	/**
	 * Can be used to determine if this is an active project
	 * @return Boolean - true if the project is active
	 */
	Boolean isActive() {
		//TODO: check time GMT
		completionDate.compareTo(new Date()) > 0
	}

	/**
	 * Can be used to determine project status, valid values are active or completed
	 * @return String - could be active or completed
	 */
	String getStatus() {
		isActive()?'active':'completed'
	}

}
