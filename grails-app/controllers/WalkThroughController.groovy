import grails.converters.JSON

import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetCableMap
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil
/*------------------------------------------------------------
 * Controller for Walk Through Process
 * @author : Lokanath Reddy
 *----------------------------------------------------------*/
class WalkThroughController {
	
	def userPreferenceService
	def jdbcTemplate
	def walkThroughService
	def workflowService
	def assetEntityAttributeLoaderService
	
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
		def projectId = params.project
		def currentProj = getSession().getAttribute("AUDIT_PROJ")
		if(projectId){
			getSession().setAttribute("AUDIT_PROJ",projectId)
		}
		def moveBundle = params.moveBundle
		if(moveBundle){
			getSession().setAttribute("AUDIT_BUNDLE",moveBundle)
		}
		def location = params.location
		if(location){
			getSession().setAttribute("AUDIT_LOCATION",location)
		}
		def locationList
    	def currBundle = getSession().getAttribute("AUDIT_BUNDLE")
    	def currProj = getSession().getAttribute("AUDIT_PROJ")
    	def currLocation = getSession().getAttribute("AUDIT_LOCATION")
    	if( !currBundle ){
    		userPreferenceService.loadPreferences("CURR_BUNDLE")
    		currBundle = session.getAttribute("CURR_BUNDLE")?.CURR_BUNDLE
    		getSession().setAttribute("AUDIT_BUNDLE",currBundle)
    	}
    	if( !currProj ){
    		userPreferenceService.loadPreferences("CURR_PROJ")
    		currProj = session.getAttribute("CURR_PROJ")?.CURR_PROJ
    		getSession().setAttribute("AUDIT_PROJ",currProj)
    	}
    	def projectInstance = Project.findById( currProj )
    	def moveBundlesList = MoveBundle.findAll("from MoveBundle m where m.project = $currProj")
    	if((projectId == null || (projectId == currentProj)) && currBundle){
    		def bundleInstance = MoveBundle.findById( currBundle )
    		locationList = AssetEntity.executeQuery("select distinct(a.sourceLocation) from AssetEntity a where a.moveBundle =$bundleInstance.id and a.owner = $projectInstance.client.id and a.sourceLocation is not null and a.sourceLocation != ''")
		}else if(moveBundlesList.size > 0) {
			if(projectId){
				getSession().setAttribute("AUDIT_PROJ",projectId)
			}
			def bundleInstance = MoveBundle.findById( moveBundlesList[0].id )
    		locationList = AssetEntity.executeQuery("select distinct(a.sourceLocation) from AssetEntity a where a.moveBundle =$bundleInstance.id and a.owner = $projectInstance.client.id and a.sourceLocation is not null and a.sourceLocation != ''")
		}
    	render( view : 'startMenu', model:[ currProj : currProj, currBundle : currBundle, currLocation:currLocation, moveBundlesList : moveBundlesList, locationList:locationList ] )	
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
		def searchKey = params.search
		def searchType = params.searchType
		def viewType = params.viewType ? params.viewType : 'todo' 
		def type = 'source'
		def locationQuery = "as location from asset_entity where move_bundle_id = $moveBundleId"
		def locationsList
		if(auditType == 'source'){
			locationsList = jdbcTemplate.queryForList("select distinct source_location "+locationQuery+" and source_location is not null"+
														" and source_location != ''" )
		} else {
			type = 'target'
			locationsList = jdbcTemplate.queryForList("select distinct target_location "+locationQuery +" and target_location is not null"+
														" and target_location != ''" )
		}
		def auditLocation = getSession().getAttribute("AUDIT_LOCATION")
		if(location != auditLocation){
			searchKey = ""
		}
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
		def args = [auditLocation]
        AssetEntity.executeUpdate( "update AssetEntity a set a.${type}Room = null where a.${type}Room = '' " )
        AssetEntity.executeUpdate( "update AssetEntity a set a.${type}Rack = null where a.${type}Rack = '' " )
		if(auditLocation){
			def racksListQuery = new StringBuffer("select a.${type}Room, a.${type}Rack, count(a.id) from AssetEntity a where a.${type}Location = ? "+
										"and a.moveBundle = $moveBundleId ")
			if( searchKey ){
				racksListQuery.append(" and ( a.${type}Room like ? or a.${type}Rack like ? ) ")
				args = [auditLocation,"%"+searchKey+"%","%"+searchKey+"%"]
			}
			racksListQuery.append(" group by a.${type}Room, a.${type}Rack ")
			if(sortOrder){
				if(sortOrder == "room"){
					racksListQuery += " order by a.${type}Room $params.order"
				} else if(sortOrder == "rack"){
					racksListQuery += " order by a.${type}Rack $params.order"
				} else {
					racksListQuery += " order by count(a.id) $params.order"
				}
			} else {
				racksListQuery += " order by a.${type}Room, a.${type}Rack"
			}
			racksList = AssetEntity.executeQuery(racksListQuery,args)
		}
		if(searchType){
			getSession().setAttribute("SEARCH_TYPE",searchType)
		}
		searchType = getSession().getAttribute("SEARCH_TYPE") ? getSession().getAttribute("SEARCH_TYPE") : 'rack'
		def rackListView = walkThroughService.generateRackListView( racksList, moveBundleId, auditLocation, auditType, viewType )
    	render( view : 'rackList', model:[ locationsList : locationsList, rackListView : rackListView, auditType:auditType, searchType : searchType,
    	                                   auditLocation : auditLocation, moveBundle : moveBundleId, viewType : viewType, search:searchKey] )	
    }
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : location
	 * @return : Will render Asset Menu for selected Asset
	 *----------------------------------------------------------*/
	/*def getRacksByLocation = {
		def auditLocation = params.location
		def viewType = params.viewType
		def searchKey = params.searchKey
		getSession().setAttribute("AUDIT_LOCATION",auditLocation)
		def auditBundle = getSession().getAttribute("AUDIT_BUNDLE")
		def auditType = getSession().getAttribute("AUDIT_TYPE")
		def args = [auditLocation]
		def racksList
		def type = 'source'
		if(auditType !='source'){
			type = 'target'
		}
		if(auditLocation){
			def racksListQuery = new StringBuffer("select a.${type}Room, a.${type}Rack, count(a.id) from AssetEntity a "+
					"where a.${type}Location = ? and a.moveBundle = $auditBundle ")
			if(searchKey){
				searchKey = "%"+searchKey+"%"
				racksListQuery .append(" and ( a.${type}Room like ? or a.${type}Rack like ? ) ")
				args = [auditLocation,searchKey,searchKey]
			}
			racksListQuery .append("group by a.${type}Room, a.${type}Rack order by a.${type}Room, a.${type}Rack")
			racksList = AssetEntity.executeQuery(racksListQuery.toString(),args)
		}
		def rackListView = walkThroughService.generateRackListView( racksList, auditBundle, auditLocation, auditType, viewType)
		//def racksDetails = [racksList:racksList, sortParams:sortParams]
		render rackListView
	}*/
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Bundle, location, room, rack
	 * @return : Assets list for selected Rack
	 *----------------------------------------------------------*/
	def selectAsset = {
		def currentProj = getSession().getAttribute("AUDIT_PROJ")
		if(currentProj){
			def project = Project.findById( currentProj )
			def moveBundle = params.moveBundle
			def auditLocation = params.location
			def searchKey = params.search
			def auditRoom = params.room
			def auditRack = params.rack
			def sortOrder = params.sort
			def auditType = getSession().getAttribute("AUDIT_TYPE")
			def currBundle = getSession().getAttribute("AUDIT_BUNDLE")
			def currLocation = getSession().getAttribute("AUDIT_LOCATION")
			def viewType = params.viewType ? params.viewType : 'todo'
			def searchType = params.searchType
			if(searchType){
				getSession().setAttribute("SEARCH_TYPE",searchType)
			}			
			def assetsList
			def type = 'source'
			if(auditType != 'source'){
				type = 'target'
			}
			if(auditRoom == 'null' || auditRoom == ''){
				auditRoom = null
			}
			if(auditRack == 'null' || auditRack == ''){
				auditRack = null
			}
			if(auditLocation){
				def asstesListQuery = new StringBuffer("from AssetEntity a where a.owner = ${project?.client?.id}")
				def args = [auditLocation]
				if(searchKey){
					searchKey = "%"+searchKey+"%"
					asstesListQuery.append(" and ( a.assetTag like ? or a.assetName like ? or a.serialNumber like ? ) ")
					args = [searchKey, searchKey, searchKey]
					/*if(auditRoom && auditRack ){
						asstesListQuery.append(" and a.${type}Room = ? and a.${type}Rack = ? ")
						args = [searchKey,searchKey,auditRoom,auditRack]
					} else if(!auditRoom && auditRack){
						asstesListQuery.append(" and (a.${type}Room is null or a.${type}Room = '') and a.${type}Rack = ? ")
						args = [searchKey,searchKey,auditRack]
					} else if(auditRoom && !auditRack){
						asstesListQuery.append(" and a.${type}Room = ? and (a.${type}Rack is null or a.${type}Rack = '' ) ")
						args = [searchKey,searchKey,auditRoom]
					} else {
						asstesListQuery.append(" and (a.${type}Room is null or a.${type}Room = '') and (a.${type}Rack is null or a.${type}Rack = '' ) ")
						args = [searchKey,searchKey]
					}*/
				} else {
					asstesListQuery.append(" and a.${type}Location = ?  and a.moveBundle = ${moveBundle} ")
					def constructedQuery = walkThroughService.constructQuery( auditRoom, auditRack, auditLocation, type )
					asstesListQuery.append( constructedQuery.query )
					args = constructedQuery.args
				}
				if(sortOrder && params.order){
					asstesListQuery.append(" order by a.${sortOrder} ${params.order}")
				}else {
					asstesListQuery.append(" order by a.${type}RackPosition desc, a.assetTag")
				}
				assetsList = AssetEntity.executeQuery(asstesListQuery.toString(),args)
			}
			if(assetsList?.size() != 1 || !searchKey){
				def assetsListView = walkThroughService.generateAssetListView( assetsList, auditLocation, auditType, viewType )
		    	render( view : 'assetList', model:[ assetsListView : assetsListView, params:params, auditType:auditType, 
		    	                                    viewType:viewType, searchKey : params.search, searchType : searchType] )
			} else {
				def walkthruComments = walkthruComments()
				def room = assetsList[0]?.sourceRoom
				def rack = assetsList[0]?.sourceRack
				if(auditType != 'source'){
					room = assetsList[0]?.targetRoom
					rack = assetsList[0]?.targetRack	
				}
				def commentCodes = walkThroughCodes( assetsList[0] )
				render(view:'assetMenu', model:[ moveBundle:currBundle, location:currLocation, room:room,searchKey : params.search,
					                                rack:rack, assetEntity:assetsList[0], commentCodes:commentCodes, walkthruComments:walkthruComments,
					                                assetBundle : assetsList[0]?.moveBundle?.id, auditType:auditType ] )
			}
		} else {
			flash.message = "Your login has expired and must login again"
			redirect(controller:"auth", action:"signOut")
		}
	}
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : searck Key, room , rack
	 * @return : Will return list of assets for a search key
	 *----------------------------------------------------------*/
	/*def searchAssets = {
		def auditLocation = getSession().getAttribute("AUDIT_LOCATION")
		def moveBundle = getSession().getAttribute("AUDIT_BUNDLE")
		def auditRoom = params.room
		def auditRack = params.rack
		def auditType = getSession().getAttribute("AUDIT_TYPE")
		def searchKey = params.searchKey
		def viewType = params.viewType
		def assetsList
		def type = 'source'
		if(auditType != 'source'){
			type = 'target'
		}
		if(auditRoom == 'null' || auditRoom == ''){
			auditRoom = null
		}
		if(auditRack == 'null' || auditRack == ''){
			auditRack = null
		}
		if(auditLocation){
			def asstesListQuery = new StringBuffer("from AssetEntity a where a.${type}Location = ? ")
			def args = [auditLocation]
			if(searchKey){
				searchKey = "%"+searchKey+"%"
				asstesListQuery.append(" and ( a.assetTag like ? or a.assetName like ? ) ")
				if(auditRoom && auditRack ){
					asstesListQuery.append(" and a.${type}Room = ? and a.${type}Rack = ? ")
					args = [auditLocation,searchKey,searchKey,auditRoom,auditRack]
				} else if(!auditRoom && auditRack){
					asstesListQuery.append(" and (a.${type}Room is null or a.${type}Room = '') and a.${type}Rack = ? ")
					args = [auditLocation,searchKey,searchKey,auditRack]
				} else if(auditRoom && !auditRack){
					asstesListQuery.append(" and a.${type}Room = ? and (a.${type}Rack is null or a.${type}Rack = '' ) ")
					args = [auditLocation,searchKey,searchKey,auditRoom]
				} else {
					asstesListQuery.append(" and (a.${type}Room is null or a.${type}Room = '') and (a.${type}Rack is null or a.${type}Rack = '' ) ")
					args = [auditLocation,searchKey,searchKey]
				}
			} else {
				asstesListQuery.append(" and a.moveBundle = ${moveBundle} ")
				def constructedQuery = walkThroughService.constructQuery( auditRoom, auditRack, auditLocation, type )
				asstesListQuery.append( constructedQuery.query )
				args = constructedQuery.args
			}
			asstesListQuery.append(" order by a.${type}RackPosition desc, a.assetTag")
			assetsList = AssetEntity.executeQuery(asstesListQuery.toString(),args)
		}
		def assetsListView = walkThroughService.generateAssetListView( assetsList, auditLocation, auditType, viewType )
		//render assetsListView
		render( view : 'assetList', model:[ assetsListView : assetsListView, params:params, auditType:auditType, viewType:viewType, searchKey : params.searchKey] )
	}*/
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Id
	 * @return : Will return boolean flag if asset bundle does not match with Audit bundle
	 *----------------------------------------------------------*/
	/*def confirmAssetBundle = {
		def assetEntity = AssetEntity.findById(params.id)
		def assetBundle = assetEntity?.moveBundle?.id
		def auditBundle = Integer.parseInt(getSession().getAttribute("AUDIT_BUNDLE"))
		def message = ""
		if(assetBundle != auditBundle){
			message = "The asset ${assetEntity?.assetName} is not part of the bundle ${assetEntity?.moveBundle}. Do you want to proceed?"
		}
		render message
	}*/
	/*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Id, Audit bundle, location, room, rack
	 * @return : Will render Asset Menu for selected Asset
	 *----------------------------------------------------------*/
	def assetMenu = {
		def assetEntity = AssetEntity.findById(params.id)
		def walkthruComments = walkthruComments()
		def commentCodes = walkThroughCodes( assetEntity )
		render(view:'assetMenu', model:[ moveBundle:params.moveBundle, location:params.location, room:params.room,
		                                rack:params.rack, assetEntity:assetEntity, commentCodes:commentCodes, walkthruComments:walkthruComments ] )
	}
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset Id
	 * @return : Mark asset as missing Asset
	 *----------------------------------------------------------*/
	def missingAsset = {
		def principal = SecurityUtils.subject.principal
		def loginUser = UserLogin.findByUsername ( principal )
		def type = params.type
		def auditType = getSession().getAttribute("AUDIT_TYPE")
		def assetComment
		def assetEntity = AssetEntity.findById( params.id )
		if(assetEntity){
			if(type != 'create'){
				def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
				assetComment = AssetComment.find("from AssetComment where assetEntity = ${assetEntity.id} and commentType = ? and isResolved = ? and commentCode = ?" ,['issue',0,'ASSET_MISSING'])
				assetComment?.isResolved = 1
				assetComment?.resolution = "Asset Missing issue is resolved while in Audit"
				assetComment?.resolvedBy = loginUser.person
				assetComment?.dateResolved = GormUtil.convertInToGMT( "now", tzId )
				assetComment?.save()
			} else {
				assetComment = new AssetComment(commentType:'issue', assetEntity:assetEntity, isResolved:0, commentCode:'ASSET_MISSING', category:'walkthru', createdBy:loginUser.person ).save()
			}
		}
		def walkthruComments = walkthruComments()
		def commentCodes = walkThroughCodes( assetEntity )
		render(view:'assetMenu', model:[ moveBundle:assetEntity?.moveBundle?.id, location:params.location, room:params.room,
		                                rack:params.rack, assetEntity:assetEntity, commentCodes:commentCodes, walkthruComments:walkthruComments ] )
	}
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Manufacturer and device type
	 * @return : list of Models for selected Manufacturer and Device
	 *----------------------------------------------------------*/
    def getModels = {
		def manufacturer = params.manufacturer
		def deviceType = params.device
		def modelQuery = "from Model m "
		if( manufacturer ){
			def manu = Manufacturer.findByName(manufacturer)
			modelQuery += " where manufacturer = $manu.id "
		}
		def model = Model.findAll(modelQuery)
		render model as JSON
	}
    /*------------------------------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : Asset properties
	 * @return : Will do the Save and Complete for Front/Rear audit
	 *----------------------------------------------------------*/
    def saveAndCompleteAudit = {
		def assetEntity = AssetEntity.get( params.id )
		def existingModelId = assetEntity.model?.id 
		def walkthruComments = walkthruComments()
		if( assetEntity ){
			def principal = SecurityUtils.subject.principal
			def loginUser = UserLogin.findByUsername ( principal )
			def auditType = getSession().getAttribute("AUDIT_TYPE")
			def currBundle = getSession().getAttribute("AUDIT_BUNDLE")
			def currLocation = getSession().getAttribute("AUDIT_LOCATION")
			def type = "source"
			def room = assetEntity.sourceRoom
			def rack = assetEntity.sourceRack
			def stateTo = "SourceWalkthru"
			if(auditType != "source"){
				type = "target"
				room = assetEntity.targetRoom
				rack = assetEntity.targetRack
				stateTo = "TargetWalkthru"
			}
			assetEntity.properties = params
			if(!assetEntity.hasErrors() && assetEntity.save() ) {
				if(existingModelId != assetEntity.model?.id){
            		AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
													assetToPort=null where assetTo = ? """,[assetEntity])

            		AssetCableMap.executeUpdate("delete from AssetCableMap where assetFrom = ?",[assetEntity])
            		assetEntityAttributeLoaderService.createModelConnectors( assetEntity )
            	}
				
				if(params.submitType != "save"){
					def transactionStatus = workflowService.createTransition(assetEntity.moveBundle.workflowCode,"SUPERVISOR", stateTo, assetEntity, assetEntity.moveBundle, loginUser, null, "" )
				}
				def query = "from AssetComment where assetEntity = ${assetEntity.id} and commentType = ? and isResolved = ? and commentCode = ?"
				if(params.needAssetTag == "Y"){
					createComments( 'NEED_ASSET_TAG', loginUser, query, assetEntity , "Need Asset Tag" )
				} else {
					resolveComments( 'NEED_ASSET_TAG', loginUser, query )
				}
				
				if(params.hasAmber == "Y"){
					createComments( 'AMBER_LIGHTS', loginUser, query, assetEntity, "Has Amber Lights" )
				} else {
					resolveComments( 'AMBER_LIGHTS', loginUser, query )
				}
				
				if(params.stuffOnTop == "Y"){
					createComments( 'STACKED_ON_TOP', loginUser, query, assetEntity, "Stuff Stacked On Top" )
				} else {
					resolveComments( 'STACKED_ON_TOP', loginUser, query )
				}
				
				if(params.poweredOff == "Y"){
					createComments( 'POWERED_OFF', loginUser, query, assetEntity, "Is Powered OFF" )
				} else {
					resolveComments( 'POWERED_OFF', loginUser, query )
				}
				
				if(params.hasObstruction == "Y"){
					createComments( 'HAS_OBSTRUCTION', loginUser, query, assetEntity, "Has Obstruction" )
				} else {
					resolveComments( 'HAS_OBSTRUCTION', loginUser, query )
				}
				
				def generalComment = params.generalComment
				if(generalComment.lastIndexOf(",") != -1){
					def commentDescription = generalComment.substring(0,generalComment.lastIndexOf(",") > 255 ? 255 : generalComment.lastIndexOf(","))
					new AssetComment(assetEntity : assetEntity, commentType : 'comment', category : 'walkthru', 
									comment : commentDescription, createdBy : loginUser.person ).save( flush:true )
				}
				def commentCodes = walkThroughCodes( assetEntity )
				assetEntity.updateRacks()
				render(view:'assetMenu', model:[ moveBundle:currBundle, location:currLocation, room:params.room,  viewType:'assetMenu',
				                                rack:params.rack, assetEntity:assetEntity, commentCodes:commentCodes, walkthruComments:walkthruComments ] )
			} else {
				def commentCodes = walkThroughCodes( assetEntity )
				render(view:'assetMenu', model:[ moveBundle:currBundle, location:currLocation, room:params.room,  viewType:'assetMenu',
				                                rack:params.rack, assetEntity:assetEntity, commentCodes:commentCodes, walkthruComments:walkthruComments ] )
			}
		} else {
			def commentCodes = walkThroughCodes( assetEntity )
			render(view:'assetMenu', model:[ moveBundle:currBundle, location:currLocation, room:params.room,  viewType:'assetMenu',
			                                rack:params.rack, assetEntity:assetEntity, commentCodes:commentCodes, walkthruComments:walkthruComments ] )
		}
	}
	
	 /*------------------------------------------------------------
	  * To create asset comments
	  * @author : Mallikarjun 
	  * @param  : Asset Comment properties
	  *----------------------------------------------------------*/
	def createComments ( def commentCode, def loginUser, def query, def assetEntity, def comment ) {
		def hasComment = AssetComment.find(query, ["issue", 0, commentCode])
		if(!hasComment){
			new AssetComment(assetEntity : assetEntity, comment:comment, isResolved : 0, commentType : 'issue', category : 'walkthru', commentCode : commentCode, createdBy : loginUser.person ).save( flush:true )
		}	 
	}
	 
	 /*------------------------------------------------------------
	  * To resolve asset comments
	  * @author : Mallikarjun 
	  * @param  : Asset Comment properties
	  *----------------------------------------------------------*/
	def resolveComments ( def commentCode, def loginUser, def query ) {
		def assetComment = AssetComment.find(query, ["issue", 0, commentCode])
		if( assetComment ) {
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			assetComment?.isResolved = 1
			assetComment?.resolvedBy = loginUser.person	
			assetComment?.dateResolved = GormUtil.convertInToGMT( "now", tzId )
			assetComment?.save(flush:true)
		}	 
	}
    /*------------------------------------------------------------
	 * @author : Mallikarjun 
	 * @return : comments validation 
	 *----------------------------------------------------------*/
    def validateComments = {
		def asset = AssetEntity.findById( params.id )
        def comment = params.comment
        def commentType = params.commentType
        def checkCommentQuery = new StringBuffer("from AssetComment where assetEntity=${asset.id} and "+
        						"comment= ? and commentType='${commentType}'")
        if(commentType == "issue"){
        	checkCommentQuery.append(" and isResolved=0 ")
        }
		def assetComment = AssetComment.findAll( checkCommentQuery.toString(), [ comment ] )
		def flag = true
        if ( assetComment ) {
        	flag = false
        }
        render flag
	}
    /*------------------------------------------------------------
	 * @author : Mallikarjun 
	 * @return : save new comment
	 *----------------------------------------------------------*/
    def saveComment = {
    	def principal = SecurityUtils.subject.principal
    	def loginUser = UserLogin.findByUsername ( principal )
		def assetEntity = AssetEntity.findById( params.assetId )
		def commentType = params.commentType
    	def assetComment = new AssetComment()
    	assetComment.comment = params.comments
    	assetComment.assetEntity = assetEntity
		if(commentType =='instruction') assetComment.mustVerify = 1
    	assetComment.commentType = commentType
    	assetComment.category = 'walkthru'
    	assetComment.createdBy = loginUser.person
    	assetComment.save()
    	def walkthruComments = walkthruComments()
		def commentCodes = walkThroughCodes( assetEntity )
		render(view:'assetMenu', model:[ moveBundle:params.moveBundle, location:params.location, room:params.room,  viewType:'view_comments', commentType : 'all',
		                                rack:params.rack, assetEntity:assetEntity, commentCodes:commentCodes, walkthruComments:walkthruComments ] )
	 }
	
 /*------------------------------------------------------------
  * @author : Mallikarjun 
  * @param  : commentsList
  * @return : Will return comment list view as string
  *----------------------------------------------------------*/
	def getComments = {
		def assetEntity = AssetEntity.findById( params.id )
		def assetCommentsList
		if ( params.commentType != 'all' ) {
			assetCommentsList = AssetComment.findAll(" from AssetComment where assetEntity=${assetEntity.id} and commentType='${params.commentType}' order by ${params.orderType} ${params.sort}")
		} else {
			assetCommentsList = AssetComment.findAll(" from AssetComment where assetEntity=${assetEntity.id} order by ${params.orderType} ${params.sort}")
		}
		def commentListSize = assetCommentsList.size()
		def commentListView = new StringBuffer()
		if ( assetCommentsList ) {
			for ( int i=0; i<commentListSize; i++ ) {
				switch ( assetCommentsList[i].commentType ) {
					case "comment" 		: commentListView.append("<TR class='comment_font'><TD>Cmnt</TD>")
						commentListView.append("<TD>${assetCommentsList[i].comment}</TD><TD></TD></TR>")
						break;
					case "instruction" 	: commentListView.append("<TR class='comment_font'><TD>Inst</TD>")
						commentListView.append("<TD>${assetCommentsList[i].comment}</TD><TD></TD></TR>")
						break;
					case "issue" 		: commentListView.append("<TR class='comment_font'><TD>Iss</TD>")
						if ( assetCommentsList[i].isResolved == 1 ) {
							commentListView.append("<TD>${assetCommentsList[i].comment}</TD><TD><input type='checkbox' checked='yes' disabled='true'/></TD></TR>")
						} else {
							commentListView.append("<TD>${assetCommentsList[i].comment}</TD><TD><input type='checkbox' disabled='true'/></TD></TR>")
						}
						break;
				}
			}
		} else {
			commentListView.append("<TR class='comment_font'><TD colSpan='3' align='center' class='norecords_display'>No records found</TD></TR>")
		}
		def walkthruComments = walkthruComments()
		def commentCodes = walkThroughCodes( assetEntity )
		render(view:'assetMenu', model:[ moveBundle:params.moveBundle, location:params.location, room:params.room,  
		                                 viewType:'view_comments', commentListView : commentListView, sort : params.sort, commentType : params.commentType,
			                             rack:params.rack, assetEntity:assetEntity, commentCodes:commentCodes, walkthruComments:walkthruComments ] )
	}
    /*-------------------------------------------------------
     * @author : Lokanath Reddy 
     * @param  : assetEntity
     * @return : Will return commentCodes
     *------------------------------------------------------*/
    def walkThroughCodes(def assetEntity){
    	def query = "from AssetComment where assetEntity = ${assetEntity.id} and commentType = ? and isResolved = ? and commentCode = ?"
    	def commentCodes = [needAssetTag : AssetComment.find(query,["issue", 0, "NEED_ASSET_TAG"])?.commentCode,
    						amberLights : AssetComment.find(query,["issue", 0, "AMBER_LIGHTS"])?.commentCode,
    		                stackedOnTop : AssetComment.find(query,["issue", 0, "STACKED_ON_TOP"])?.commentCode,
    		                poweredOff : AssetComment.find(query,["issue", 0, "POWERED_OFF"])?.commentCode,
    		                cablesMoved : AssetComment.find(query,["issue", 0, "HAS_OBSTRUCTION"])?.commentCode]
    }
    def walkthruComments(){
    	def walkthruComments = []
		def walkthruCommentsCount = Integer.parseInt(message ( code: "walkthru.defComment.count" ))
		for ( int i=1; i<=walkthruCommentsCount; i++ ) {
			walkthruComments << message ( code: "walkthru.defComment.${i}" )
		}
    	return walkthruComments;
    }
}
