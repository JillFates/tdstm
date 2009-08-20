import grails.converters.JSON
/*------------------------------------------------------------
 * Controller for Walk Through Process
 * @author : Lokanath Reddy
 *----------------------------------------------------------*/
class WalkThroughController {
	
	def userPreferenceService
	def jdbcTemplate
	def walkThroughService
	
    def index = { redirect(action: 'mainMenu', params: params) }
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @return : will render Main Menu
	 *----------------------------------------------------------*/
    def	mainMenu = {
    	render( view : 'mainMenu')
    }
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @return : Will render Stat Menu
	 *----------------------------------------------------------*/
    def startMenu = {
    	def currBundle = getSession().getAttribute("AUDIT_BUNDLE")
    	def currProj = getSession().getAttribute("AUDIT_PROJ")
    	if( !currBundle ){
    		userPreferenceService.loadPreferences("CURR_BUNDLE")
    		currBundle = session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE
    	}
    	if( !currProj ){
    		userPreferenceService.loadPreferences("CURR_PROJ")
    		currProj = session.getAttribute("CURR_PROJ")?.CURR_PROJ
    	}
    	render( view : 'startMenu', model:[ currProj : currProj, currBundle : currBundle ])	
    }
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @return : Racks list for selected Move Bundle
	 *----------------------------------------------------------*/
    def selectRack = {
		def moveBundleId = params.moveBundle
		def auditType = params.auditType
		def sortOrder = params.sort
		def location = params.location
		def viewType = params.viewType
		def locationQuery = "as location from asset_entity where move_bundle_id = $moveBundleId"
		def locationsList
		if(auditType == 'source'){
			locationsList = jdbcTemplate.queryForList("select distinct source_location "+locationQuery )
		} else {
			locationsList = jdbcTemplate.queryForList("select distinct target_location "+locationQuery )
		}
		def auditLocation = ""
		def auditBundle = getSession().getAttribute("AUDIT_BUNDLE")
		if( location ){
			auditLocation = location
			getSession().setAttribute("AUDIT_LOCATION",auditLocation)
		} else if(auditBundle != moveBundleId){
				auditLocation = locationsList ? locationsList[0].location : ""
				getSession().setAttribute("AUDIT_LOCATION",auditLocation)
		}else{
			auditLocation = getSession().getAttribute("AUDIT_LOCATION")
			if( !auditLocation ){
				auditLocation = locationsList ? locationsList[0].location : ""
				getSession().setAttribute("AUDIT_LOCATION",auditLocation)
			}
		}
		getSession().setAttribute("AUDIT_BUNDLE",moveBundleId)
		getSession().setAttribute("AUDIT_TYPE",auditType)
		
		def racksList
		if(auditLocation){
			def sourceRacksListQuery = "select a.sourceRoom, a.sourceRack, count(a.id) from AssetEntity a where a.sourceLocation = ? "+
										"and a.moveBundle = $moveBundleId group by a.sourceRoom, a.sourceRack"
			if(sortOrder){
				if(sortOrder == "room"){
					sourceRacksListQuery += " order by a.sourceRoom $params.order"
				} else if(sortOrder == "rack"){
					sourceRacksListQuery += " order by a.sourceRack $params.order"
				} else {
					sourceRacksListQuery += " order by count(a.id) $params.order"
				}
			} else {
				sourceRacksListQuery += " order by a.sourceRoom, a.sourceRack"
			}
			racksList = AssetEntity.executeQuery(sourceRacksListQuery,[auditLocation])
		}
		def rackListView = walkThroughService.generateRackListView( racksList, moveBundleId, auditLocation, auditType, viewType )
    	render( view : 'rackList', model:[ locationsList : locationsList, rackListView : rackListView, auditType:auditType,
    	                                   auditLocation : auditLocation, moveBundle : moveBundleId, viewType : viewType ] )	
    }
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : project
	 * @return : Move Bundles list as JSON via AJAX
	 *----------------------------------------------------------*/
    def getBundles = {
    	def moveBundleList
    	def projectId = params.id
    	if(projectId){
    		getSession().setAttribute("AUDIT_PROJ",projectId)
	    	def projectInstance = Project.findById( projectId )
	    	moveBundleList = MoveBundle.findAllByProject( projectInstance )
    	}
    	render moveBundleList as JSON
    }
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @return : Assets list for selected Rack
	 *----------------------------------------------------------*/
	def selectAsset = {
		render( view : 'assetList' )
	}
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @return : Will render Asset Menu for selected Asset
	 *----------------------------------------------------------*/
	def assetMenu = {
		render( view : 'assetMenu' )	
	}
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : location
	 * @return : Will render Asset Menu for selected Asset
	 *----------------------------------------------------------*/
	def getRacksByLocation = {
		def auditLocation = params.location
		def viewType = params.viewType
		def searchKey = params.searchKey
		getSession().setAttribute("AUDIT_LOCATION",auditLocation)
		def auditBundle = getSession().getAttribute("AUDIT_BUNDLE")
		def auditType = getSession().getAttribute("AUDIT_TYPE")
		def args = [auditLocation]
		def racksList
		
		if(auditLocation){
			def sourceRacksListQuery = new StringBuffer("select a.sourceRoom, a.sourceRack, count(a.id) from AssetEntity a "+
					"where a.sourceLocation = ? and a.moveBundle = $auditBundle ")
			if(searchKey){
				searchKey = searchKey+"%"
				sourceRacksListQuery .append(" and ( a.sourceRoom like ? or a.sourceRack like ? ) ")
				args = [auditLocation,searchKey,searchKey]
			}
			sourceRacksListQuery .append("group by a.sourceRoom, a.sourceRack order by a.sourceRoom, a.sourceRack")
			racksList = AssetEntity.executeQuery(sourceRacksListQuery.toString(),args)
		}
		def rackListView = walkThroughService.generateRackListView( racksList, auditBundle, auditLocation, auditType, viewType)
		//def racksDetails = [racksList:racksList, sortParams:sortParams]
		render rackListView
	}
}
