import com.tdssrc.grails.GormUtil
import org.apache.shiro.SecurityUtils
import com.tds.asset.AssetEntity
import com.tds.asset.Application
import com.tds.asset.Database
import com.tds.asset.Files

class Project extends PartyGroup {

	def static final DEFAULT_PROJECT_ID = 2
	def static final CUSTOM_FIELD_COUNT = 96
	def projectService
	
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
	MoveBundle defaultBundle
    
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
		String custom65
		String custom66
		String custom67
		String custom68
		String custom69
		String custom70
		String custom71
		String custom72
		String custom73
		String custom74
		String custom75
		String custom76
		String custom77
		String custom78
		String custom79
		String custom80
		String custom81
		String custom82
		String custom83
		String custom84
		String custom85
		String custom86
		String custom87
		String custom88
		String custom89
		String custom90
		String custom91
		String custom92
		String custom93
		String custom94
		String custom95
		String custom96
		
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
		defaultBundle( nullable:true )
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
		custom65( blank:true, nullable:true )
		custom66( blank:true, nullable:true )
		custom67( blank:true, nullable:true )
		custom68( blank:true, nullable:true )
		custom69( blank:true, nullable:true )
		custom70( blank:true, nullable:true )
		custom71( blank:true, nullable:true )
		custom72( blank:true, nullable:true )
		custom73( blank:true, nullable:true )
		custom74( blank:true, nullable:true )
		custom75( blank:true, nullable:true )
		custom76( blank:true, nullable:true )
		custom77( blank:true, nullable:true )
		custom78( blank:true, nullable:true )
		custom79( blank:true, nullable:true )
		custom80( blank:true, nullable:true )
		custom81( blank:true, nullable:true )
		custom82( blank:true, nullable:true )
		custom83( blank:true, nullable:true )
		custom84( blank:true, nullable:true )
		custom85( blank:true, nullable:true )
		custom86( blank:true, nullable:true )
		custom87( blank:true, nullable:true )
		custom88( blank:true, nullable:true )
		custom89( blank:true, nullable:true )
		custom90( blank:true, nullable:true )
		custom91( blank:true, nullable:true )
		custom92( blank:true, nullable:true )
		custom93( blank:true, nullable:true )
		custom94( blank:true, nullable:true )
		custom95( blank:true, nullable:true )
		custom96( blank:true, nullable:true )

		customFieldsShown( nullable:false, inList:[0,4,8,12,16,20,24,28,32,36,40,44,48,52,56,60,64,68,72,76,80,84,88,92,96] )
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
	
	static transients = [ 'isDefaultProject', 'getDefaultProject', 'readDefaultProject', 'active', 'status', 'getProjectDefaultBundle' ]

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
     * 
     * Method csn used to get default move bundle for for a current project.
     * @return default moveBundle for current project
     */
	def getProjectDefaultBundle() {
		return projectService.getDefaultBundle(this)
	}
	/**
	 * Can be used to determine project status, valid values are active or completed
	 * @return String - could be active or completed
	 */
	String getStatus() {
		isActive()?'active':'completed'
	}

}
