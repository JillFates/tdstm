import grails.converters.JSON
import org.jsecurity.SecurityUtils
/*------------------------------------------------------------
 * Controller for Walk Through Process
 * @author : Lokanath Reddy
 *----------------------------------------------------------*/
class WalkThroughController {
	
	def userPreferenceService
	def jdbcTemplate
	def walkThroughService
	def workflowService
	
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
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Bundle, location, room, rack
	 * @return : Assets list for selected Rack
	 *----------------------------------------------------------*/
	def selectAsset = {
		def moveBundle = params.moveBundle
		def auditLocation = params.location
		def auditRoom = params.room
		def auditRack = params.rack
		def sortOrder = params.sort
		def auditType = getSession().getAttribute("AUDIT_TYPE")
		def viewType = params.viewType
		def assetsList
		if(auditRoom == 'null' || auditRoom == ''){
			auditRoom = null
		}
		if(auditRack == 'null' || auditRack == ''){
			auditRack = null
		}
		if(auditLocation){
			def asstesListQuery = new StringBuffer("from AssetEntity a where a.moveBundle = $moveBundle and a.sourceLocation = ? ")
			def args = [auditLocation]
			if(auditRoom && auditRack ){
				asstesListQuery.append(" and a.sourceRoom = ? and a.sourceRack = ? ")
				args = [auditLocation,auditRoom,auditRack]
			} else if(!auditRoom && auditRack){
				asstesListQuery.append(" and a.sourceRoom is null and a.sourceRack = ? ")
				args = [auditLocation,auditRack]
			} else if(auditRoom && !auditRack){
				asstesListQuery.append(" and a.sourceRoom = ? and a.sourceRack is null")
				args = [auditLocation,auditRoom]
			} else {
				asstesListQuery.append(" and a.sourceRoom is null and a.sourceRack is null")
				args = [auditLocation]
			}
			if(sortOrder){
				if(sortOrder == "assetTag"){
					asstesListQuery.append(" order by a.assetTag $params.order")
				} else if(sortOrder == "usize"){
					asstesListQuery.append(" order by a.usize $params.order")
				} else {
					asstesListQuery.append(" order by a.sourceRackPosition $params.order")
				}
			} else {
				asstesListQuery.append(" order by a.sourceRackPosition ")
			}
			assetsList = AssetEntity.executeQuery(asstesListQuery.toString(),args)
		}
		def assetsListView = walkThroughService.generateAssetListView( assetsList, auditLocation, auditType, viewType )
    	render( view : 'assetList', model:[ assetsListView : assetsListView, params:params, auditType:auditType] )
	}
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : searck Key, room , rack
	 * @return : Will return list of assets for a search key
	 *----------------------------------------------------------*/
	def searchAssets = {
		def auditLocation = getSession().getAttribute("AUDIT_LOCATION")
		def moveBundle = getSession().getAttribute("AUDIT_BUNDLE")
		def auditRoom = params.room
		def auditRack = params.rack
		def auditType = getSession().getAttribute("AUDIT_TYPE")
		def searchKey = params.searchKey
		def viewType = params.viewType
		def assetsList
		if(auditRoom == 'null' || auditRoom == ''){
			auditRoom = null
		}
		if(auditRack == 'null' || auditRack == ''){
			auditRack = null
		}
		if(auditLocation){
			def asstesListQuery = new StringBuffer("from AssetEntity a where a.sourceLocation = ? ")
			def args = [auditLocation]
			if(searchKey){
				searchKey = searchKey+"%"
				asstesListQuery.append(" and ( a.assetTag like ? or a.assetName like ? ) ")
				if(auditRoom && auditRack ){
					asstesListQuery.append(" and a.sourceRoom = ? and a.sourceRack = ? ")
					args = [auditLocation,searchKey,searchKey,auditRoom,auditRack]
				} else if(!auditRoom && auditRack){
					asstesListQuery.append(" and a.sourceRoom is null and a.sourceRack = ? ")
					args = [auditLocation,searchKey,searchKey,auditRack]
				} else if(auditRoom && !auditRack){
					asstesListQuery.append(" and a.sourceRoom = ? and a.sourceRack is null")
					args = [auditLocation,searchKey,searchKey,auditRoom]
				} else {
					asstesListQuery.append(" and a.sourceRoom is null and a.sourceRack is null")
					args = [auditLocation,searchKey,searchKey]
				}
			} else {
				asstesListQuery.append(" and a.moveBundle = ${moveBundle} ")
				if(auditRoom && auditRack ){
					asstesListQuery.append(" and a.sourceRoom = ? and a.sourceRack = ? ")
					args = [auditLocation,auditRoom,auditRack]
				} else if(!auditRoom && auditRack){
					asstesListQuery.append(" and a.sourceRoom is null and a.sourceRack = ? ")
					args = [auditLocation,auditRack]
				} else if(auditRoom && !auditRack){
					asstesListQuery.append(" and a.sourceRoom = ? and a.sourceRack is null")
					args = [auditLocation,auditRoom]
				} else {
					asstesListQuery.append(" and a.sourceRoom is null and a.sourceRack is null")
					args = [auditLocation]
				}
			}
			asstesListQuery.append(" order by a.sourceRackPosition ")
			assetsList = AssetEntity.executeQuery(asstesListQuery.toString(),args)
		}
		def assetsListView = walkThroughService.generateAssetListView( assetsList, auditLocation, auditType, viewType )
		render assetsListView
	}
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Id
	 * @return : Will return boolean flag if asset bundle does not match with Audit bundle
	 *----------------------------------------------------------*/
	def confirmAssetBundle = {
		def assetEntity = AssetEntity.findById(params.id)
		def assetBundle = assetEntity?.moveBundle?.id
		def auditBundle = Integer.parseInt(getSession().getAttribute("AUDIT_BUNDLE"))
		def message = ""
		if(assetBundle != auditBundle){
			message = "The asset ${assetEntity?.assetName} is not part of the bundle ${assetEntity?.moveBundle}. Do you want to proceed?"
		}
		render message
	}
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Id, Audit bundle, location, room, rack
	 * @return : Will render Asset Menu for selected Asset
	 *----------------------------------------------------------*/
	def assetMenu = {
		def assetEntity = AssetEntity.findById(params.id)
		def query = "from AssetComment where assetEntity = ${assetEntity.id} and commentType = ? and isResolved = ? and commentCode = ?"
		def commentCodes = [needAssetTag : AssetComment.find(query,["issue", 0, "NEED_ASSET_TAG"])?.commentCode,
		                    amberLights : AssetComment.find(query,["issue", 0, "AMBER_LIGHTS"])?.commentCode,
		                    stackedOnTop : AssetComment.find(query,["issue", 0, "STACKED_ON_TOP"])?.commentCode,
		                    poweredOff : AssetComment.find(query,["issue", 0, "POWERED_OFF"])?.commentCode,
		                    cablesMoved : AssetComment.find(query,["issue", 0, "NEED_CABLES_MOVED"])?.commentCode]
		render(view:'assetMenu', model:[ moveBundle:params.moveBundle, location:params.location, room:params.room,
		                                rack:params.rack, assetEntity:assetEntity, commentCodes:commentCodes ] )
	}
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Id
	 * @return : Mark asset as missing Asset
	 *----------------------------------------------------------*/
    def missingAsset = {
		def type = params.type
		def assetComment
		def assetEntity = AssetEntity.findById( params.id )
		if(assetEntity){
			if(type != 'create'){
				assetComment = AssetComment.find("from AssetComment where assetEntity = ${assetEntity.id} and commentType = ? and isResolved = ? and commentCode = ?" ,['issue',0,'ASSET_MISSING'])
				assetComment.isResolved = 1
				assetComment.save()
			} else {
				assetComment = new AssetComment(commentType:'issue', assetEntity:assetEntity, isResolved:0, commentCode:'ASSET_MISSING', category:'walkthru' ).save()
			}
		}
		def message = "success"
		if(!assetComment){
			message = "failure"
		}
		render message
	}
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset properties
	 * @return : Will do the Save and Complete for Front/Rear audit
	 *----------------------------------------------------------*/
    def saveAndCompleteAudit = {
		def assetEntity = AssetEntity.get( params.id )
		if( assetEntity ){
			assetEntity.properties = params
			if(!assetEntity.hasErrors() && assetEntity.save() ) {
				if(params.submitType != "save"){
					def principal = SecurityUtils.subject.principal
			    	def loginUser = UserLogin.findByUsername(principal)
					def transactionStatus = workflowService.createTransition(assetEntity.project.workflowCode,"SUPERVISOR", "SourceWalkthru", assetEntity, assetEntity.moveBundle, loginUser, null, "" )
				}
				def query = "from AssetComment where assetEntity = ${assetEntity.id} and commentType = ? and isResolved = ? and commentCode = ?"
				if(params.needAssetTag == "Y"){
					def needAssetTagComment = AssetComment.find(query, ["issue", 0, "NEED_ASSET_TAG"])
					if(!needAssetTagComment){
						new AssetComment(assetEntity : assetEntity, isResolved : 0, commentType : 'issue', category : 'walkthru', commentCode : 'NEED_ASSET_TAG').save()
					}
				}
				if(params.hasAmber == "Y"){
					def hasAmberComment = AssetComment.find(query, ["issue", 0, "AMBER_LIGHTS"])
					if(!hasAmberComment){
						new AssetComment(assetEntity : assetEntity, isResolved : 0, commentType : 'issue', category : 'walkthru', commentCode : 'AMBER_LIGHTS' ).save()
					}
				}
				if(params.stuffOnTop == "Y"){
					def stuffOnTopComment = AssetComment.find(query, ["issue", 0, "STACKED_ON_TOP"])
					if(!stuffOnTopComment){
						new AssetComment(assetEntity : assetEntity, isResolved : 0, commentType : 'issue', category : 'walkthru', commentCode : 'STACKED_ON_TOP' ).save()
					}
				}
				if(params.poweredOff == "Y"){
					def poweredOffComment = AssetComment.find(query, ["issue", 0, "POWERED_OFF"])
					if(!poweredOffComment){
						new AssetComment(assetEntity : assetEntity, isResolved : 0, commentType : 'issue', category : 'walkthru', commentCode : 'POWERED_OFF' ).save()
					}
				}
				if(params.moveCables == "Y"){
					def moveCablesComment = AssetComment.find(query, ["issue", 0, "NEED_CABLES_MOVED"])
					if(!moveCablesComment){
						new AssetComment(assetEntity : assetEntity, isResolved : 0, commentType : 'issue', category : 'walkthru', commentCode : 'NEED_CABLES_MOVED' ).save()
					}
				}
				redirect(action:selectAsset,params:[moveBundle: getSession().getAttribute("AUDIT_BUNDLE"),
				                                    location : getSession().getAttribute("AUDIT_LOCATION"),
				                                    room : assetEntity.sourceRoom, rack : assetEntity.sourceRack ])
			} else {
				redirect(action:selectAsset,params:[id : assetEntity.id ])
			}
		} else {
			redirect(action:selectAsset,params:[moveBundle: getSession().getAttribute("AUDIT_BUNDLE"),
			                                    location : getSession().getAttribute("AUDIT_LOCATION"),
			                                    room : assetEntity.sourceRoom, rack : assetEntity.sourceRack ])
		}
	}
    /*------------------------------------------------------------
	 * @author : Mallikarjun 
	 * @param  : 
	 * @return : 
	 *----------------------------------------------------------*/
    def validateComments = {
		def asset = AssetEntity.findById( params.id )
        def comment = params.comment
        def commentType = params.commentType
        def checkCommentQuery = new StringBuffer()
        checkCommentQuery.append("from AssetComment where assetEntity=${asset.id} and comment='${comment}' and commentType='${commentType}'") 
        def assetComment
        	switch ( commentType ) {
        		case "comment"     : assetComment = AssetComment.findAll( checkCommentQuery.toString() )
        							 break;
        		case "instruction" : assetComment = AssetComment.findAll( checkCommentQuery.toString() )
        							 break;
        		case "issue"       : assetComment = AssetComment.findAll( ( checkCommentQuery.append(" and isResolved=0 ") ).toString() )
        							 break;
        	}
        def flag = []
        	if ( assetComment.size()>0 ) {
        		flag << false
        	} else {
        		flag << true
        	}
        render flag as JSON
	}
    /*------------------------------------------------------------
	 * @author : Mallikarjun 
	 * @param  : 
	 * @return : 
	 *----------------------------------------------------------*/
    def saveComment = {
		def asset = AssetEntity.findById( params.assetId )
    	def assetComment = new AssetComment()
    	assetComment.comment = params.comments
    	assetComment.assetEntity = asset
    	assetComment.commentType = params.saveCommentType
    	assetComment.category = 'walkthru'
    	assetComment.save()
    	render( view : 'assetMenu' )
	 }
}
