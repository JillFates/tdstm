import grails.converters.JSON

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.io.*

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jxl.*
import jxl.read.biff.*
import jxl.write.*

import org.apache.commons.lang.math.NumberUtils
import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetCommentStatus
import com.tdsops.tm.enums.domain.AssetCommentType
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.WebUtil
import com.tds.util.workbook.*;

class ReportsController {
	
	def partyRelationshipService
	def assetEntityAttributeLoaderService
	def userPreferenceService
	def jdbcTemplate
	def supervisorConsoleService 
	def reportsService
	def securityService
	def moveEventService
	def moveBundleService
	def index = { 
		render(view:'home')
	}
    
    // Generate Report Dialog
    def getBundleListForReportDialog = {
    	def reportId = params.reportId
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
        def projectId = currProj.CURR_PROJ
        def projectInstance = Project.findById( projectId )
        def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
		def browserTest = false
		if ( !request.getHeader ( "User-Agent" ).contains ( "MSIE" ) ) {
			browserTest = true
		}
		switch (reportId) {
	case "Home" :
					render( view:'home',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
						break;
	case "Rack Layout" :  
					userPreferenceService.loadPreferences("CURR_BUNDLE")
					def currentBundle = getSession().getAttribute("CURR_BUNDLE")
					render( view:'rackLayout',
						model:[moveBundleInstanceList: moveBundleInstanceList, 
						projectInstance:projectInstance, currentBundle:currentBundle])
						break;
	case "cart Asset":  
					render( view:'cartAssetReport',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
						break;
	case "Issue Report":  
					render( view:'issueReport',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
						break;
	case "Task Report":
					def moveEventInstanceList  = MoveEvent.findAllByProject(projectInstance,[sort:'name'])
					render( view:'taskReport',
						model:[moveEventInstanceList: moveEventInstanceList, projectInstance:projectInstance])
						break;
	case "Transportation Asset List":  
					render( view:'transportationAssetReport',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
						break;
	case "Asset Tag" :
					render( view:'assetTagLabel',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance,
						browserTest: browserTest])
						break;
	case "Login Badges":  
					render( view:'loginBadgeLabelReport',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance, 
						browserTest: browserTest])
						break;
	case "MoveResults" :
					def moveEventInstanceList = MoveEvent.findAllByProject( projectInstance )
					render( view:'moveResults', model:[moveEventInstanceList : moveEventInstanceList ])
						break;
	case "CablingQA":  
					render( view:'cablingQAReport',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance, type:'QA'])
						break;
	case "CablingConflict":
					render( view:'cablingQAReport',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance, type:'conflict'])
						break;
	case "CablingData":  
					render( view:'cablingData',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance ])
						break;
	default: 
					render( view:'teamWorkSheets',
						model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
						break;
		}
    }
//  Generate a TeamSheet jasper Report  
    def teamSheetReport = {
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
    	def projectId = currProj.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def partyGroupInstance = PartyGroup.get(projectInstance.id)
    	//if no Bundle selected	
    	if(params.moveBundle == "null") {    		
    		flash.message = " Please Select Bundles. "
    		redirect( action:'getBundleListForReportDialog', params:[reportId: 'Team Worksheets'] )
    	} else {
    		def moveBundleInstance = MoveBundle.findById(params.moveBundle)
    		def projectTeamInstance
    		def location = params.location
    		def reportFields = []
    		def bundleName = "All Bundles"
    		def teamName = "All Teams"
    		def assetEntityList
    		def targetAssetEntitylist
    		if(params.teamFilter != "null"){
   	   			projectTeamInstance = ProjectTeam.findById( params.teamFilter )
   	   		}
    		//if moveBundleinstance is selected (single moveBundle)
    		if( moveBundleInstance ) {
    			bundleName = moveBundleInstance?.name
    			//if Projectteam and moveBundle both selected
    			if( projectTeamInstance ) {
    				teamName = projectTeamInstance?.teamCode
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    											"asset.sourceTeamMt = $projectTeamInstance.id order By asset.sourceTeamMt,"+
                    											"asset.moveBundle,asset.assetName,asset.assetTag")
    				}else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id "+
                    												"and asset.targetTeamMt = $projectTeamInstance.id order By asset.targetTeamMt,"+
                    												"asset.moveBundle,asset.assetName,asset.assetTag")
    				}else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    											"asset.sourceTeamMt = $projectTeamInstance.id order By asset.sourceTeamMt,"+
                    											"asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    												"asset.targetTeamMt = $projectTeamInstance.id order By asset.targetTeamMt,"+
                    												"asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}else {
    				//source Location selected
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    											"asset.sourceTeamMt != null order By asset.sourceTeamMt,asset.moveBundle,"+
                    											"asset.assetName,asset.assetTag")
    				}
    				//target Location selected
    				else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    												"asset.targetTeamMt != null order By asset.targetTeamMt,asset.moveBundle,"+
                    												"asset.assetName,asset.assetTag")
    				}
    				//Location Both selected
    				else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    										"asset.sourceTeamMt != null order By asset.sourceTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    										"asset.targetTeamMt != null order By asset.targetTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}
    		}
    		//All Bundles Selected
    		else {
    			//team Selected
    			if( projectTeamInstance  ) {
    				teamName = projectTeamInstance?.teamCode
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.sourceTeamMt = $projectTeamInstance.id and "+
                    											"asset.project.id = $projectInstance.id and asset.moveBundle != null order By "+
                    											"asset.sourceTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.targetTeamMt = $projectTeamInstance.id and "+
                    												"asset.project.id = $projectInstance.id and asset.moveBundle != null order By "+
                    												"asset.targetTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.sourceTeamMt = $projectTeamInstance.id  and "+
                    											"asset.project.id = $projectInstance.id and asset.moveBundle != null order By "+
                    											"asset.sourceTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.targetTeamMt = $projectTeamInstance.id and "+
                    												"asset.project.id = $projectInstance.id and asset.moveBundle != null order By "+
                    												"asset.targetTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}
    			//Team MoveBundle Both not selected (moveBundle="AllBundles)
    			else {
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id and "+
                    											"asset.moveBundle != null and asset.sourceTeamMt != null order By asset.sourceTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset where asset.project.id = $projectInstance.id "+
                    												"and asset.moveBundle != null and asset.targetTeamMt != null order By "+
                    												"asset.targetTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id and "+
                    											"asset.moveBundle != null and asset.sourceTeamMt != null order By "+
                    											"asset.sourceTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id "+
                    										"and asset.moveBundle != null and asset.targetTeamMt != null order By "+
                    										"asset.targetTeamMt,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}
    		}
    		//Source List of Assets
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def currDate = GormUtil.convertInToUserTZ(GormUtil.convertInToGMT( "now", "EDT" ),tzId)
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
    		assetEntityList.each { asset ->
    			def bundleInstance
    			if(asset.moveBundle != null) {
    				bundleInstance = MoveBundle.findById(asset.moveBundle.id)
    			}
    			def teamPartyGroup 
    			def projectTeamLocationInstance
    			if( asset.sourceTeamMt != null ) {
    				teamPartyGroup = PartyGroup.findById(asset.sourceTeamMt.id)
    				projectTeamLocationInstance = ProjectTeam.findById(asset.sourceTeamMt.id)
    			}
    			def assetCommentList = AssetComment.findAllByAssetEntity(asset)
    			def assetCommentString =""
    			assetCommentList.each { assetComment ->
    				assetCommentString = assetCommentString + assetComment.comment +"\n"
   			 	}
    			def teamMembers = partyRelationshipService.getTeamMemberNames(asset.sourceTeamMt?.id)
    			def rackPos = (asset.sourceRack ? asset.sourceRack : "")+"/"+ (asset.sourceRackPosition ? asset.sourceRackPosition : "")
   				if (rackPos == "/"){
   					rackPos = ""
   				}
   			 	reportFields <<['assetName':asset.assetName , 'assetTag':asset.assetTag, "assetType":asset.assetType, 
   			 	                "manufacturer":asset.manufacturer?.toString(), "model":asset.model?.toString(), "sourceTargetrack":rackPos, 
   			 	                "position":asset.sourceRackPosition, 
   			 	                "sourceTargetPos":(projectTeamLocationInstance?.currentLocation ? projectTeamLocationInstance?.currentLocation : "") +"(source/ unracking)", 
   			 	                "usize":asset?.model?.usize, "cart":asset.cart, "shelf":asset.shelf,"source_team_id":asset?.sourceTeamMt?.id, 
   			 	                "move_bundle_id":asset?.moveBundle?.id, "clientName":projectInstance?.client?.name,
   			 	                'projectName':partyGroupInstance?.name,'startAt':GormUtil.convertInToUserTZ( projectInstance?.startDate, tzId ), 
   			 	                'completedAt':GormUtil.convertInToUserTZ( projectInstance?.completionDate, tzId ), 'bundleName':bundleInstance?.name,
   			 	                'teamName':teamPartyGroup?.name +"-"+teamMembers, 'teamMembers':teamMembers,
   			 	                'location':"Source Team", 'rack':"SourceRack",'rackPos':"SourceRackPosition",'truck':asset.truck, 
   			 	                'room':asset.sourceRoom,  'instructions':assetCommentString, 'sourcetargetLoc':"s",
								'timezone':tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate ) )]
    		}
    		//Target List of Assets
    		targetAssetEntitylist.each { asset ->
                def bundleInstance
                if(asset.moveBundle != null) {
                    bundleInstance = MoveBundle.findById(asset.moveBundle.id)
                }
   				def teamPartyGroup 
   				def projectTeamLocationInstance
   				def teamMembers
   				if(asset.targetTeamMt != null ) {
   					teamPartyGroup = PartyGroup.findById(asset.targetTeamMt.id)
   					projectTeamLocationInstance = ProjectTeam.findById(asset.targetTeamMt.id)
   					teamMembers = partyRelationshipService.getTeamMemberNames(asset.targetTeamMt.id) 
   				}
   				def assetCommentList = AssetComment.findAllByAssetEntity(asset)
   				def assetCommentString =""
   				assetCommentList.each { assetComment ->
   					assetCommentString = assetCommentString + assetComment?.comment +"\n"
   				}
   				def rackPos = (asset.targetRack ? asset.targetRack : "")+"/"+ (asset.targetRackPosition ? asset.targetRackPosition : "")
   				if (rackPos == "/"){
   					rackPos = ""
   				}
   				def cartShelf = (asset.cart ? asset.cart : "")+"/"+ (asset.shelf ? asset.shelf : "")
   				if (cartShelf == "/"){
   					cartShelf = ""
   				}
   				reportFields <<['assetName':asset.assetName , 'assetTag':asset.assetTag, "assetType":asset.assetType, 
   				                "manufacturer":asset.manufacturer?.toString(), "model":asset.model?.toString(), "sourceTargetrack":rackPos, 
   				                "position":asset.targetRackPosition, 
   				                "sourceTargetPos":(projectTeamLocationInstance?.currentLocation ? projectTeamLocationInstance?.currentLocation : "") +"(target/ reracking)", 
   				                "usize":asset?.model?.usize, "cart":asset.cart, "shelf":asset.shelf,"source_team_id":asset?.targetTeamMt?.id, 
   				                "move_bundle_id":asset?.moveBundle?.id, "clientName":projectInstance?.client?.name,
   				                'projectName':partyGroupInstance?.name,'startAt':GormUtil.convertInToUserTZ( projectInstance?.startDate, tzId ), 
   				                'completedAt':GormUtil.convertInToUserTZ( projectInstance?.completionDate, tzId ), 'bundleName':bundleInstance?.name, 
   				                'teamName':teamPartyGroup?.name +"-"+teamMembers, 'teamMembers':teamMembers,
   				                'location':"Target Team", 'rack':"TargetRack",'rackPos':"TargetRackPosition",
   				                'truck':(asset.truck ? asset.truck : "")+"\n"+cartShelf,'room':asset.targetRoom,'instructions':assetCommentString,'sourcetargetLoc':"t",
								'timezone':tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate ) )]
    		}
    		//No assets were found for selected MoveBundle,Team and Location
    		if(reportFields.size() <= 0) {    		
    			flash.message = " No Assets Were found for  selected values  "
    			redirect( action:'getBundleListForReportDialog', params:[reportId: 'Team Worksheets'] )
    		}else {
    			def filename = 	"MoveTeam-${projectInstance.name}-${bundleName}-${teamName}"
					filename = filename.replace(" ", "_")
    			chain(controller:'jasper',action:'index',model:[data:reportFields],
						params:["_format":"PDF","_name":"${filename}","_file":"workSheetsReport"])
    		}
    	}
    }
	//  cart Asset report
	def cartAssetReport = {
    	def reportName = params.reportName
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
    	def projectId = currProj.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def partyGroupInstance = PartyGroup.get(projectInstance.id)
    	def sortOrder = params.sortType
    	def teamPartyGroup
    	// if no moveBundle was selected
    	if(params.moveBundle == "null") {
            flash.message = " Please Select Bundles. "
            if(reportName == 'cartAsset') {
            	redirect( action:'getBundleListForReportDialog', params:[reportId: 'cart Asset'] )
            }else {
            	redirect( action:'getBundleListForReportDialog', params:[reportId: 'Transportation Asset List'] )
            }
        } else {
            def moveBundleInstance = MoveBundle.findById(params.moveBundle)
            def reportFields = []
            def bundleName = "All Bundles"
            def teamName = "All Teams"
            def assetEntityList
            def targetAssetEntitylist
            //if moveBundleinstance is selected (single moveBundle)
            if( moveBundleInstance ) {
                bundleName = moveBundleInstance?.name
                assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id  "+
                    									"order By asset.moveBundle,asset.cart,asset.shelf")
            }
            //All Bundles Selected
            else {
       			assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id and "+
                    									"asset.moveBundle != null order By asset.moveBundle,asset.cart,asset.shelf")
       		}
            
            def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
            def currDate = GormUtil.convertInToUserTZ(GormUtil.convertInToGMT( "now", "EDT" ),tzId)
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
       		//Source AssetList 
       		if( assetEntityList != null) {
       			assetEntityList.each { asset ->
       				def bundleInstance
       				if(asset.moveBundle != null) {
       					bundleInstance = MoveBundle.findById(asset.moveBundle.id)
       				}
                    if(reportName == 'cartAsset') {
                        teamPartyGroup =  ProjectTeam.findByMoveBundleAndTeamCode(bundleInstance, 'Logistics')
                    }else {
       					teamPartyGroup =  ProjectTeam.findByMoveBundleAndTeamCode(bundleInstance, 'Transport')
                    }
       				def assetCommentList = AssetComment.findAllByAssetEntity(asset)
       				def moveTeamName
       				if(teamPartyGroup != null ){
       					def moveteamInstance = PartyGroup.findById(teamPartyGroup.id)
       					moveTeamName = moveteamInstance?.name
       				}
       				def assetCommentString =""
       				assetCommentList.each { assetComment ->
       					assetCommentString = assetCommentString + assetComment.comment +"\n"
       				}
       				def cartShelf = (asset.cart ? asset.cart : "")+"/"+ (asset.shelf ? asset.shelf : "")
       				if (cartShelf == "/"){
       					cartShelf = ""
       				}
       				
       				// sort options for reportFields
       				def teamTagSort = (asset.sourceTeamMt ? asset.sourceTeamMt?.name : "") +" "+ (asset.assetTag ? asset.assetTag : "")
       				
       				def roomTagSort = (asset.sourceRoom ? asset.sourceRoom : "") +" "+ (asset.sourceRack ? asset.sourceRack : "") +" "+ (asset?.model?.usize ? asset?.model?.usize : "")
       				
       				def truckTagSort = (asset.truck ? asset.truck : "") +" "+ (asset.cart ? asset.cart : "") +" "+ (asset.shelf ? asset.shelf : "")
       				
       				def teamMembers = partyRelationshipService.getTeamMemberNames(teamPartyGroup?.id) 
       				reportFields <<['assetName':asset.assetName , "model":asset.model?.toString(), 
       				                "sourceTargetPos":(teamPartyGroup?.currentLocation ? teamPartyGroup?.currentLocation : "") +"(source/ unracking)", 
       				                "cart":cartShelf, "shelf":asset.shelf,"source_team_id":teamPartyGroup?.id, 
       				                "move_bundle_id":asset?.moveBundle?.id,dlocation:asset.sourceLocation,
       				                'projectName':partyGroupInstance?.name,'startAt':GormUtil.convertInToUserTZ( projectInstance?.startDate, tzId ), 
       				                'completedAt':GormUtil.convertInToUserTZ( projectInstance?.completionDate, tzId ), 'bundleName':bundleInstance?.name, 
       				                'teamName':teamPartyGroup?.teamCode ? teamPartyGroup?.name+" - "+teamMembers : "", 
       				                'teamMembers':teamMembers,'location':"Source Team", 'truck':asset.truck, 
       				                'room':asset.sourceRoom,'moveTeam':asset?.sourceTeamMt?.name, 'instructions':assetCommentString,
       				                'teamTagSort':teamTagSort, 'roomTagSort':roomTagSort,'truckTagSort':truckTagSort,
       				                'assetTagSort': (asset.assetTag ? asset.assetTag : ""),'sourcetargetLoc':"s", 'usize':asset?.model?.usize,
									'timezone':tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate ) )]
       			}
       		}
       		//No Assets were found for selected moveBundle,team and Location
       		if(reportFields.size() <= 0) {    		
       			flash.message = " No Assets Were found for  selected values  "
       			if(reportName == 'cartAsset') {
       				redirect( action:'getBundleListForReportDialog', params:[reportId: 'cart Asset'] )
        		}else {
        			redirect( action:'getBundleListForReportDialog', params:[reportId: 'Transportation Asset List'] )
        		}
        	}else {
        		//sort reportFields by selected sort options
                if ( sortOrder ) {
                	if ( sortOrder == "TEAM_ASSET" ) {
                		reportFields.sort{ it.teamTagSort }
                	} else if ( sortOrder == "ROOM_RACK_USIZE" ) {
                		reportFields.sort{ it.roomTagSort }
                	} else if ( sortOrder == "TRUCK_CART_SHELF" ) {
                		reportFields.sort{ it.truckTagSort }
                	} else if ( sortOrder == "ASSET_TAG" ) {
                		reportFields.sort{ it.assetTagSort }
                	}
                }
        		
				def name = reportName == "cartAsset" ? "LogisticsTeam" : "TransportTeam"
				def filename = 	"${name}-${projectInstance.name}-${bundleName}"
					filename = filename.replace(" ", "_")
        		
				chain(controller:'jasper',action:'index',model:[data:reportFields],
						params:["_format":"PDF","_name":"${filename}","_file":"${params._file}"])
        	}
        }
    }
    /*----------------------------------------------------------
     * @author : Lokanath Reddy
     * @param  : move bundle and params from rack elevation form
     * @return : Data for Rack Elevation report  
     *---------------------------------------------------------*/
    def rackLayoutReport = {
    	def bundleId = params.moveBundle
   		def maxUSize = 42
        if(bundleId == "null") {
            flash.message = " Please Select Bundles. "
            redirect( action:'getBundleListForReportDialog', params:[reportId: 'Rack Layout'] )
        } else {
        	def includeOtherBundle = params.otherBundle
        	def includeBundleName = params.bundleName
        	def printQuantity = params.printQuantity
        	def location = params.locationName
        	def frontView = params.frontView
        	def backView = params.backView
        	def racks = []
        	def rack
        	def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
        	def rackLayout = []

			def moveBundle = MoveBundle.findById(bundleId)
     		def reportsHasPermission = RolePermissions.hasPermission("reports")
        	if(location == "source"){
        		rack = request.getParameterValues("sourcerack")
        		rack.each{
        			racks<<[rack:it]
            	}
        		if( rack[0] == "" ){
        			def sourceRackQuery = "select CONCAT_WS('~',IFNULL(source_location ,'blank'),IFNULL(source_room,'blank'),"+
            								"IFNULL(source_rack,'blank')) as rack from asset_entity where"
            		if( bundleId && !includeOtherBundle){
            			sourceRackQuery += " move_bundle_id = $bundleId "
            		} else {
            			sourceRackQuery += " project_id = $projectId "
            		}
        			sourceRackQuery += " and asset_type NOT IN ('VM', 'Blade') and source_rack != '' and source_rack is not null "
        			racks = jdbcTemplate.queryForList(sourceRackQuery + "group by source_location, source_rack, source_room")
            	}
            } else {
            	rack = request.getParameterValues("targetrack")
            	rack.each{
            		racks<<[rack:it]
            	}
            	if(rack[0] == ""){
            		def targetRackQuery = "select CONCAT_WS('~',IFNULL(target_location ,'blank'),IFNULL(target_room,'blank'), "+
            								"IFNULL(target_rack,'blank')) as rack from asset_entity where"
					if( bundleId && !includeOtherBundle){
						targetRackQuery += " move_bundle_id = $bundleId "
					} else {
						targetRackQuery += " project_id = $projectId "
					}
            		targetRackQuery += " and asset_type NOT IN ('VM', 'Blade') and target_rack != '' and target_rack is not null "
            		racks = jdbcTemplate.queryForList( targetRackQuery  + "group by target_location, target_rack, target_room")
            		
            		}
            	
            	}
        	def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ	
        	racks.each{
        		def rackRooms = it?.rack.split("~")
        		if(rackRooms?.size() == 3){
    	            def assetDetails = []
                	def assetDetail = []
                	def finalAssetList = []
                	def assetsDetailsQuery = supervisorConsoleService.getQueryForRackElevation( bundleId, projectId, includeOtherBundle, rackRooms, location )
    	            def assetEntityList = jdbcTemplate.queryForList( assetsDetailsQuery.toString() )
    	            assetEntityList.each{assetEntity ->
    	            	def overlapError = false
    	            	def rackPosition = assetEntity.rackPosition != 0 ? assetEntity.rackPosition : 1
    	            	def rackSize = assetEntity?.usize != 0 ? assetEntity?.usize : 1
		            	def position = rackPosition + rackSize - 1
		            	def newHigh = position
            			def newLow = rackPosition
		            	if(assetDetail.size() > 0){
		            		def flag = true
		            		assetDetail.each{asset->
		            			flag = true
		            			def currentHigh = asset.currentHigh
				            	def currentLow = asset.currentLow
		            			def ignoreLow = (currentLow <= newLow && currentHigh >= newHigh )
		            			def changeBoth = (currentLow >= newLow && currentHigh <= newHigh )
		            			def changeLow = (currentLow >= newLow && currentHigh >= newHigh && currentLow <= newHigh)
		            			def changeHigh = (currentLow <= newLow && currentHigh <= newHigh && currentHigh <= newLow)
		            			if(position > maxUSize){
		            				asset.position = maxUSize
	    	            			asset.rowspan = 1 
	    	            			asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag
	    	            			asset.overlapError = true
	    	            			asset.cssClass = "rack_error"
	    	            			flag = false
		            			} else if(ignoreLow){
		            				asset.position = currentHigh
	    	            			asset.rowspan = currentHigh - currentLow + 1 
	    	            			asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag
	    	            			asset.overlapError = true
	    	            			asset.cssClass = "rack_error"
	    	            			flag = false
		            			} else if(changeBoth) {
		            				asset.currentHigh = newHigh
		            				asset.currentLow = newLow
		            				asset.position = newHigh
	    	            			asset.rowspan = newHigh - newLow + 1
	    	            			asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag
	    	            			asset.overlapError = true
	    	            			asset.cssClass = "rack_error"
	    	            			flag = false
		            			} else if(changeHigh){
		            				asset.currentHigh = newHigh
		            				asset.position = newHigh
	    	            			asset.rowspan = newHigh - currentLow  + 1
	    	            			asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag
	    	            			asset.overlapError = true
	    	            			asset.cssClass = "rack_error"
	    	            			flag = false
		            			} else if(changeLow){
			            			asset.currentLow = newLow
		            				asset.position = currentHigh
	    	            			asset.rowspan = currentHigh - newLow +1
	    	            			asset.assetTag = asset.assetTag +"<br/>"+assetEntity.assetTag
	    	            			asset.overlapError = true
	    	            			asset.cssClass = "rack_error"
	    	            			flag = false
		            			}
	    	            	}
		            		
		            		if(flag){
		            			if(position > maxUSize) {
			            			position = maxUSize
			            			newLow = maxUSize
			            			assetEntity?.usize = 1
			            			overlapError = true
			            		}
		            			assetDetail << [assetEntity:assetEntity, assetTag:assetEntity.assetTag, position:position, overlapError:overlapError, 
		            			              rowspan:assetEntity?.usize, currentHigh : position, currentLow : newLow]
		            		}
		            	}else{
		            		if(position > maxUSize) {
		            			position = maxUSize
		            			newLow = maxUSize
		            			assetEntity?.usize = 1
		            			overlapError = true
		            		}
		            		assetDetail << [assetEntity:assetEntity, assetTag:assetEntity.assetTag, position:position, overlapError:overlapError, 
		            		              rowspan:assetEntity?.usize, currentHigh : position, currentLow : newLow ]
		            	}
		            }
		            for (int i = maxUSize; i > 0; i--) {
    	            	def assetEnity
    	            	def cssClass = "empty"
        	            def rackStyle = "rack_past"
        	            assetDetail.each {
    	            		if(it.position == i ){
    		            		assetEnity = it
    		            	}
    		            }
    	            	if(assetEnity){
    	            		if(assetEnity.assetEntity?.racksize > 1 ){
    	            			cssClass = 'rack_error'
    	            			rackStyle = 'rack_error'
    	            		} else if(assetEnity.overlapError){
    	            			cssClass = 'rack_error'
    	            			rackStyle = 'rack_error'
    	            		} else if(bundleId && assetEnity.assetEntity?.bundleId != Integer.parseInt(bundleId)){
    	            			def currentTime = GormUtil.convertInToGMT( "now", tzId ).getTime()
    	            			def startTime = moveBundle.startTime ? moveBundle.startTime.getTime() : 0
    	            			if(startTime < currentTime){
    	            				cssClass = 'rack_past'
    	            			} else {
    	            				cssClass = "rack_future"
    	            			}
    	            		} else{
    	            			cssClass = 'rack_current'
    	            			rackStyle = 'rack_current'
    	            		}
    	            		if(assetEnity.assetEntity?.rackPosition == 0 || assetEnity.assetEntity?.usize == 0 ){
    	            			rackStyle = 'rack_error'
    	            		}
    	            		assetDetails<<[asset:assetEnity, rack:i, cssClass:cssClass, rackStyle:rackStyle]
    	            	}else {
    	            		assetDetails<<[asset:null, rack:i, cssClass:cssClass, rackStyle:rackStyle]
    	            	}
    	            }
    	            def backViewRows
    	            def frontViewRows
    	            if(backView){
    	            	backViewRows = getRackLayout( reportsHasPermission, assetDetails, includeBundleName, backView )
    	            }
    	            if(frontView){
    	            	frontViewRows = getRackLayout( reportsHasPermission, assetDetails, includeBundleName, null )
    	            }
    	            if(rackRooms.size() == 3){
    	            	rackLayout << [ assetDetails : assetDetails, rack : rackRooms[2] , room : rackRooms[1] , 
    	            	                location : rackRooms[0] +"("+location+")" , frontViewRows : frontViewRows, backViewRows : backViewRows ]
    	            } else {
    	            	rackLayout << [ assetDetails : assetDetails, rack : "", room : "", location : "("+location+")" , 
    	            	                frontViewRows : frontViewRows, backViewRows : backViewRows ]
    	            }
        	}
        }
        render(view:'rackLayoutReport',model:[rackLayout : rackLayout, frontView : frontView, backView : backView])
        }
    }
	/*
	 * Generate Issue Report
	 */
	def issueReport = {
    	def subject = SecurityUtils.subject
        def principal = subject.principal
    	def personInstance = Person.findByFirstName( principal )
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
    	def projectId = currProj.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def partyGroupInstance = PartyGroup.get(projectInstance.id)
    	def bundleNames = ""
    	def reportFields = []
    	def resolvedInfoInclude
    	def sortBy = params.reportSort
		def comment = params.commentInfo
		def reportFormat = params._format
    	if( params.reportSort == "sourceLocation" ) {
    		sortBy = params.reportSort+",source_room,source_rack,source_rack_position"
    	}else if( params.reportSort == "targetLocation" ){
    		sortBy = params.reportSort+",target_room,target_rack,target_rack_position"
    	}
    	if( params.reportResolveInfo == "false" ){
    		resolvedInfoInclude = "Resolved issues were not included"
    	}
		String moveBundles = params.moveBundle
		moveBundles = moveBundles.replace("[","('").replace(",]","')").replace(",","','")
    	if(params.moveBundle == "null") {    		
    		flash.message = " Please Select Bundles. "
    		redirect( action:'getBundleListForReportDialog', params:[reportId: 'Issue Report'] )
        } else {
    		String bundleName = "All Bundles"
    		def targetAssetEntitylist
			def commentType = "('issue','comment')"
	    	if(comment == "false"){
	    		commentType = "('issue')"
	    	}
    		def commentsQuery = new StringBuffer("from AssetComment ac where ac.commentType in ${commentType} ")
				
    		if( moveBundles.size() > 4 ){
    			commentsQuery.append(" and ac.assetEntity.id in (select ae.id from AssetEntity ae where ae.moveBundle.id in $moveBundles ) order by ac.assetEntity.${sortBy} ")
    			bundleNames = MoveBundle.findAll("from MoveBundle where id in $moveBundles").name.toString()
    		    bundleName = bundleNames
    		}else {
    			commentsQuery.append(" and ac.assetEntity.id in (select ae.id from AssetEntity ae where ae.project.id = $projectInstance.id ) order by ac.assetEntity.${sortBy} ")
    			bundleNames = "All"
        	}
    		
    		def assetCommentList = AssetComment.findAll( commentsQuery.toString() )
			
    		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
    		def currDate = GormUtil.convertInToUserTZ(GormUtil.convertInToGMT( "now", "EDT" ),tzId)
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
    		assetCommentList.each { assetComment ->
    			def createdBy
    			def sourceTargetRoom
    			sourceTargetRoom = (assetComment?.assetEntity?.sourceRoom ? assetComment?.assetEntity?.sourceRoom : "--")+
    								"/"+(assetComment?.assetEntity?.sourceRack ? assetComment?.assetEntity?.sourceRack : "--")+
    								"/"+(assetComment?.assetEntity?.sourceRackPosition ? assetComment?.assetEntity?.sourceRackPosition : "--")+"\n"+
    								(assetComment?.assetEntity?.targetRoom ? assetComment?.assetEntity?.targetRoom : "--")+"/"+
    								(assetComment?.assetEntity?.targetRack ? assetComment?.assetEntity?.targetRack : "--")+"/"+
    								(assetComment?.assetEntity?.targetRackPosition ? assetComment?.assetEntity?.targetRackPosition : "--")
    			if( params.reportResolveInfo == "true" || assetComment.isResolved != 1 ) {
    				reportFields <<['assetName':assetComment?.assetEntity?.assetName, 'assetTag':assetComment?.assetEntity?.assetTag,'moveBundle' :assetComment?.assetEntity?.moveBundle?.name,
    								'sourceTargetRoom':sourceTargetRoom,
    								'commentType':assetComment.commentType == 'issue' ? 'Task' : assetComment.commentType,
    								'model':(assetComment?.assetEntity?.manufacturer ? assetComment?.assetEntity?.manufacturer?.toString() : "")+" "+(assetComment?.assetEntity?.model ? assetComment?.assetEntity?.model : "" ), 
    								'occuredAt':GormUtil.convertInToUserTZ( assetComment?.dateCreated, tzId ), 'createdBy':assetComment?.createdBy?.firstName+" "+assetComment?.createdBy?.lastName, 
									'owner':assetComment?.assignedTo ? assetComment?.assignedTo?.firstName+" "+assetComment?.assignedTo?.lastName : '',
    								'issue':assetComment?.comment, 'bundleNames':bundleNames,'projectName':partyGroupInstance?.name, 
    								'clientName':projectInstance?.client?.name,"resolvedInfoInclude":resolvedInfoInclude,
    								'timezone':tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate )),
									'previousNote':WebUtil.listAsMultiValueString(assetComment.notes) ]
    			}
    			if( params.reportResolveInfo == "true" && assetComment.isResolved == 1 ) {
    				reportFields <<['assetName':null, 'assetTag':null, 'moveBundle' :null,'sourceTargetRoom':null,'model':null, 
									'commentType':assetComment.commentType == 'issue' ? 'Task' : assetComment.commentType,
									'occuredAt':GormUtil.convertInToUserTZ( assetComment?.dateResolved, tzId ), 
    								'createdBy':assetComment?.resolvedBy?.firstName+" "+assetComment?.resolvedBy?.lastName, 
									'owner':assetComment?.assignedTo ? assetComment?.assignedTo?.firstName+" "+assetComment?.assignedTo?.lastName : '',
    								'issue':assetComment?.resolution, 'bundleNames':bundleNames,'projectName':partyGroupInstance?.name, 
    								'clientName':projectInstance?.client?.name,
    								'timezone':tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate )),
									'previousNote':WebUtil.listAsMultiValueString(assetComment.notes) ]
    			}
    		}
			if( params.newsInfo == "true" ) {
				def moveEvent = MoveEvent.findByProject(projectInstance)
				def moveEventNewsList=MoveEventNews.findAllByMoveEvent(moveEvent)
				moveEventNewsList.each{ moveEventNews ->
					moveEventNews?.resolution = moveEventNews?.resolution ? moveEventNews?.resolution : ''
					reportFields <<['assetName':'', 'assetTag':'', 'moveBundle' :'','sourceTargetRoom':'','model':'',
								'commentType':"news",
								'occuredAt':GormUtil.convertInToUserTZ( moveEventNews?.dateCreated, tzId ),
								'createdBy':moveEventNews?.createdBy.toString(),
								'owner':'',
								'issue':moveEventNews.message +"/"+  moveEventNews?.resolution , 'bundleNames':'','projectName':projectInstance?.name,
								'clientName':projectInstance?.client?.name,
								'timezone':tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate ) ),
								'previousNote':'']
				}

			}
    		if(reportFields.size() <= 0) {    		
        		flash.message = " No Issues Were found for  selected values  "
        		redirect( action:'getBundleListForReportDialog', params:[reportId: 'Issue Report'] )
        	}else {
        		def filename = 	"IssueReport-${projectInstance.name}-${bundleName}"
					filename = filename.replace(" ", "_")
				if(reportFormat == "PDF"){
	        		chain(controller:'jasper',action:'index',model:[data:reportFields],
							params:["_format":"PDF","_name":"${filename}","_file":"${params._file}"])
				} else { // Generate XLS report
					try {
						File file =  ApplicationHolder.application.parentContext.getResource( "/templates/IssueReport.xls" ).getFile()
						WorkbookSettings wbSetting = new WorkbookSettings()
						wbSetting.setUseTemporaryFileDuringWrite(true)
						def workbook = Workbook.getWorkbook( file, wbSetting )
						//set MIME TYPE as Excel
						response.setContentType( "application/vnd.ms-excel" )
						response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
						response.setHeader( "Content-Disposition", "attachment; filename=\""+filename+".xls\"" )
						
						def book = Workbook.createWorkbook( response.getOutputStream(), workbook )
						
						def sheet = book.getSheet("issues")
						sheet.addCell( new Label( 1, 1, String.valueOf( projectInstance?.client?.name )) )
						sheet.addCell( new Label( 1, 2, String.valueOf( partyGroupInstance?.name )) )
						sheet.addCell( new Label( 1, 3, String.valueOf( bundleNames )) )
						for ( int r = 0; r < reportFields.size(); r++ ) {
							sheet.addCell( new Label( 0, r+6, String.valueOf(reportFields[r].assetName ?:'')) )
							sheet.addCell( new Label( 1, r+6, String.valueOf(reportFields[r].assetTag ?:'')) )
							sheet.addCell( new Label( 2, r+6, String.valueOf(reportFields[r].moveBundle ?:'')) )
							sheet.addCell( new Label( 3, r+6, String.valueOf(reportFields[r].sourceTargetRoom ?:'')) )
							sheet.addCell( new Label( 4, r+6, String.valueOf(reportFields[r].model ?:'')) )
							sheet.addCell( new Label( 5, r+6, String.valueOf(reportFields[r].commentCode ?:'')) )
							sheet.addCell( new Label( 6, r+6, String.valueOf(reportFields[r].commentType ?:'')) )
							sheet.addCell( new Label( 7, r+6, String.valueOf(reportFields[r].occuredAt ? formatter.format( reportFields[r].occuredAt ) : "")) )
							sheet.addCell( new Label( 8, r+6, String.valueOf(reportFields[r].createdBy ?:'')) )
							sheet.addCell( new Label( 9, r+6, String.valueOf(reportFields[r].owner ?:'')) )
							sheet.addCell( new Label( 10, r+6, String.valueOf(WebUtil.listAsMultiValueString(reportFields[r].previousNote)?:'')) )
							sheet.addCell( new Label( 11, r+6, String.valueOf(reportFields[r].issue ?:'')) )
							
						}
						sheet.addCell( new Label( 0, reportFields.size()+7, String.valueOf("Note : All times are in "+reportFields[0].timezone+" time zone") ))
						
						book.write()
						book.close()
					} catch( Exception ex ) {
						flash.message = "Exception occurred while exporting data"+ex
						redirect( controller:'reports', action:"getBundleListForReportDialog", params:[reportId:'Issue Report'] )
						return;
					}
				}
        	}
    		
        }
    }

	/*-------------------------------------------------------------------
	 * To Get the Team Login Badge labes Data
	 * @author Srinivas
	 * @param Project,MoveBundle,Team,Location
	 * @return logn badge labels data
	 *--------------------------------------------------------------------*/
	def getLabelBadges = {
    	def moveBundle = params.bundle
    	def location = params.location
    	def projectInstance = Project.findById(params.project)
    	def projectId = params.project
    	def client = projectInstance.client.name
    	def startDate = projectInstance.startDate
    	def reportFields = []
    	def teamMembers = []
    	if(params.moveBundle == "null") {    		
    		reportFields <<[ 'flashMessage': "Please Select Bundles."]
    		render reportFields as JSON
    	} else {
    		def moveBundleInstance = MoveBundle.findById(params.moveBundle)
    		def projectTeamInstance
    		def loginBadges = []
    		def bundleName = "All Bundles"
    		def teamName = "All Teams"
    		def assetEntityList
    		def targetAssetEntitylist
    		if(params.teamFilter != "null"){
   	   			projectTeamInstance = ProjectTeam.findById( params.teamFilter )
   	   		}
    		//if moveBundleinstance is selected (single moveBundle)
    		if( moveBundleInstance ) {
    			bundleName = moveBundleInstance?.name
    			if( projectTeamInstance ) {
    				teamName = projectTeamInstance?.name
    				def members = partyRelationshipService.getTeamMembers(projectTeamInstance.id)
    				teamMembers.add(members)
    			}else {
    				def teamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $moveBundleInstance.id " )
    				teamInstanceList.each { team ->
    					def members = partyRelationshipService.getTeamMembers(team.id)
    					teamMembers.add(members)
    				}
    			}
    		} else {
    			if( projectTeamInstance ) {
    				teamName = projectTeamInstance?.name
    				def members = partyRelationshipService.getTeamMembers(projectTeamInstance.id)
    				teamMembers.add(members)
    			} else {
    					def teamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle in ( select m.id from MoveBundle m "+
                    												"where m.project = $projectId ) " )
    					teamInstanceList.each { team ->
                            def members = partyRelationshipService.getTeamMembers(team.id)
                            teamMembers.add(members)
    				}
    			}
    		}
    		teamMembers.each { members ->
    			members.each { member ->
    				def teamCode = "mt"
    				if(member.partyIdFrom.teamCode == "Logistics") {
    					teamCode = "ct"
    				}
    				if ( params.location == "source" || params.location == "both" ) {
    					reportFields <<[ 'name': member.partyIdTo.firstName +" "+ member.partyIdTo.lastName,
                                         'teamName': member.partyIdFrom.name+" - Source","sortField":member.partyIdFrom.moveBundle.name+member.partyIdTo.firstName+member.partyIdTo.lastName,
                                         'bundleName': client+" - "+member.partyIdFrom.moveBundle.name+" "+(member.partyIdFrom.moveBundle.startTime ? partyRelationshipService.convertDate(member.partyIdFrom.moveBundle.startTime) : " "),
                                         'barCode': teamCode+'-'+member.partyIdFrom.moveBundle.id+'-'+member.partyIdFrom.id+'-s' 
                                         ]
    				}
    				if ( member.partyIdFrom.teamCode != "Logistics" && (params.location == "target" || params.location == "both") ) {
    					reportFields <<[ 'name': member.partyIdTo.firstName +" "+ member.partyIdTo.lastName,
    					                 'teamName': member.partyIdFrom.name+" - Target","sortField": member.partyIdFrom.moveBundle.name+member.partyIdTo.firstName+member.partyIdTo.lastName, 
    					                 'bundleName': client+" - "+member.partyIdFrom.moveBundle.name+" "+(member.partyIdFrom.moveBundle.startTime ? partyRelationshipService.convertDate(member.partyIdFrom.moveBundle.startTime) : " "),
    					                 'barCode': 'mt-'+member.partyIdFrom.moveBundle.id+'-'+member.partyIdFrom.id+'-t' 
    					                 ]
    				}
    			}
    		}
        	if(reportFields.size <= 0) { 
        		reportFields <<[ 'flashMessage': "Team Members not Found for selected Teams"]
        		render reportFields as JSON
        	}else {
        		reportFields.sort{it.sortField }
        		render reportFields as JSON
        	}
    	}
    }
	/*----------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : asset details
	 * @return : rack rows
	 *---------------------------------------*/
	def getRackLayout( def reportsHasPermission, def asset, def includeBundleName, def backView){
    	def rows= new StringBuffer()
    	def rowspan = 1
    	def cssClass = "empty"
    	def rackStyle = ""
    	asset.each{
    		 def row = new StringBuffer("<tr>")
    		 	if(it.asset){
    		 		rowspan = it.asset?.rowspan != 0 ? it.asset?.rowspan : 1
    		 		rackStyle = it.rackStyle
    		 		def assetTagsList = (it.asset?.assetTag).split("<br/>")
    		 		def moveBundle = "" 
    		 		def assetTag = ""
    		 		def assetEntityId = it.asset.assetEntity.assetEntityId
    		 		if(it.cssClass == "rack_error")
		 			    assetTag += "Devices Overlap:<br />"
		 			
    		 		assetTagsList.each{
    		 			def index = it.indexOf('-')
    		 			def tag
    		 			if (index != -1) {
    		 				tag = it?.substring(0,index)
    		 			} else {
    		 				tag = it
    		 			}
    		 			def assetInstance = AssetEntity.get( assetEntityId )
    		 			moveBundle += (assetInstance?.moveBundle ? assetInstance?.moveBundle.name : "") + "<br/>"
    		 			assetTag += "<a href='javascript:openAssetEditDialig(${assetInstance?.id})' >$it</a> <br/>"
    		 		}
    		 		if( !reportsHasPermission ){
    		 			assetTag = it.asset?.assetTag
    		 		}
    		 		row.append("<td class='${it.rackStyle}'>${it.rack}</td><td rowspan='${rowspan}' class='${it.cssClass}'>${assetTag}</td>")
    		 		if(includeBundleName){
    		 			row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>${moveBundle}</td>")
    		 		}else{
    		 			row.append("<td rowspan='${rowspan}' class='${it.cssClass}'></td>")
    		 		}
    		 		if(backView){
    		 			if(it.cssClass != "rack_error"){
    		 				def cablingString = ""/*"${it.asset?.assetEntity?.pduPort ? 'PDU: '+ it.asset?.assetEntity?.pduPort +' | ' : '' }"+ 
    		 									"${it.asset?.assetEntity?.nicPort ? 'NIC: '+ it.asset?.assetEntity?.nicPort +' | ' : ''}"+
    		 									"${it.asset?.assetEntity?.kvmDevice && it.asset?.assetEntity?.kvmDevice != 'blank / blank'? 'KVM: '+ it.asset?.assetEntity?.kvmDevice +' | ' : ''}"+
    		 									"${it.asset?.assetEntity?.remoteMgmtPort ? 'RMgmt: '+ it.asset?.assetEntity?.remoteMgmtPort +' | ': ''}"+
												"${it.asset?.assetEntity?.fiberCabinet && it.asset?.assetEntity?.fiberCabinet != 'blank / blank / blank' ? 'Fiber: '+ it.asset?.assetEntity?.fiberCabinet +' | ' : ''}"
							
							if ( cablingString ) {
								cablingString = cablingString.substring( 0, cablingString.length() - 2 )
							}*/
							if ( cablingString.length() > 90 ) {
								row.append("<td rowspan='${rowspan}' style='font-size:6px;' class='${it.cssClass}'>${cablingString}</td>")
							} else {
								row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>${cablingString}</td>")
							}
    		 			} else {
    		 				row.append("<td rowspan='${rowspan}' class='${it.cssClass}'>Devices Overlap</td>")
    		 			}
    		 		}
    		 	} else if(rowspan <= 1) {
    		 		rowspan = 1
    		 		rackStyle = it.rackStyle
    		 		row.append("<td class='${it.rackStyle}'>${it.rack}</td><td rowspan=1 class=${it.cssClass}></td><td>&nbsp;</td>")
    		 		if(backView){
    		 			row.append("<td>&nbsp;</td>")
    		 		}
    		 	} else {
    		 		row.append("<td class='${rackStyle}'>${it.rack}</td>")
    		 		rowspan--
    		 	}
    		 row.append("<td class='${rackStyle}'>${it.rack}</td>")
    		 /*if(rackStyle == "rack_current" && backView && rowspan == 1){
    			 row.append("<td class='${rackStyle}'>&nbsp;</td><td class='${rackStyle}'>&nbsp;</td><td class='${rackStyle}'>&nbsp;</td>"+
    			 			"<td class='${rackStyle}'>&nbsp;</td><td class='${rackStyle}'>&nbsp;</td><td class='${rackStyle}'>&nbsp;</td>"+
    			 			"<td class='${rackStyle}'>&nbsp;</td><td class='${rackStyle}'>&nbsp;</td>")
    		 }*/
    		 
    		 row.append("</tr>")
    		 rows.append(row.toString())
    	}
    	return rows
     }
	/*
	 *  Generate PDF Cabling QA / Conflict report 
	 */
	def cablingQAReport = {
    	def reportName = params.reportName
    	def currProj = getSession().getAttribute( "CURR_PROJ" )
    	def projectId = currProj.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def cableType = params.cableType
    	// if no moveBundle was selected
    	if(params.moveBundle == "null") {
            flash.message = " Please Select Bundles. "
			redirect( action:'getBundleListForReportDialog', params:[reportId: 'CablingQA'] )
        } else {
            def moveBundleInstance = MoveBundle.findById(params.moveBundle)
            def reportFields = []
            def bundleName = "All Bundles"
			def cablesQuery = new StringBuffer("from AssetCableMap acm where acm.fromAsset.project.id = $projectInstance.id ")
            //if moveBundleinstance is selected (single moveBundle)
            if( moveBundleInstance ) {
                bundleName = moveBundleInstance?.name
                cablesQuery.append(" and acm.fromAsset.moveBundle = $moveBundleInstance.id ")
            }
            //All Bundles Selected
            else {
            	cablesQuery.append(" and acm.fromAsset.moveBundle != null ")
       		}
            if(cableType){
            	cablesQuery.append(" and acm.fromConnectorNumber.type = '${cableType}' ")
            }
            
            
			List assetCablesList = new ArrayList()
			if(reportName == "cablingQA"){
				cablesQuery.append(" order By acm.fromAsset ")
				assetCablesList = AssetCableMap.findAll( cablesQuery.toString() )
			} else {
				def conflictQuery = " and acm.toConnectorNumber is not null group by acm.toConnectorNumber having count(acm.toConnectorNumber) > 1"
				def conflictList =  AssetCableMap.findAll( cablesQuery.toString() + conflictQuery )

				def unknownList = AssetCableMap.findAll( cablesQuery.toString() + " and acm.status ='missing' " )

				def orphanedQuery = " and acm.toConnectorNumber is not null and acm.toConnectorNumber not in( select mc.id from ModelConnector mc )"
				def orphanedList = AssetCableMap.findAll( cablesQuery.toString() + orphanedQuery )

				if(conflictList.size() > 0) assetCablesList.addAll(conflictList)
				if(unknownList.size() > 0) assetCablesList.addAll(unknownList)
				if(orphanedList.size() > 0) assetCablesList.addAll(orphanedList)
			}
			
            def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
            def currDate = GormUtil.convertInToUserTZ(GormUtil.convertInToGMT( "now", "EDT" ),tzId)
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
       		//Source AssetList 
       		if( assetCablesList != null) {
       			assetCablesList.each { cable ->
       				def bundleInstance
       				if(cable.fromAsset.moveBundle != null) {
       					bundleInstance = MoveBundle.findById(cable.fromAsset.moveBundle.id)
       				}
       				
       				reportFields <<['from_asset_name':cable.fromAsset.assetName,
       								'from_asset_tag':cable.fromAsset.assetTag,
									'cable_type': cable.fromConnectorNumber.type,
									'from_rack':cable.fromAsset?.rackTarget?.tag,
									'to_rack':cable.toAsset ? cable.toAsset?.rackTarget?.tag : "",
									'from_upos':cable.fromAsset?.targetRackPosition,
									'color':cable.color ? cable.color : "",
									'to_upos':cable.toAssetUposition ? cable.toAssetUposition  : "" ,
									'from_connector_label':cable.fromConnectorNumber.label,
									'to_connector_label':cable.toConnectorNumber ? cable.toConnectorNumber.label : "",
									'to_asset_tag': cable.toAsset ? cable.toAsset?.assetTag :"",
									'to_asset_name':cable.toAsset ? cable.toAsset?.assetName :"",
       				                "asset_id":cable.fromAsset?.id,
       				                'project_name':projectInstance?.name,
       				                'bundle_name':bundleInstance?.name,
       				                'report_type': reportName == 'cablingQA' ? "Cabling QA Report" : "Cabling Conflict Report",
									'timezone':tzId ? tzId : "EDT", "rpt_time":String.valueOf(formatter.format( currDate ) )]
       			}
       		}
       		//No Assets were found for selected moveBundle,team and Location
       		if(reportFields.size() <= 0) {    		
       			flash.message = " No Cables were found for  selected values  "
				redirect( action:'getBundleListForReportDialog', params:[reportId: 'CablingQA'] )
        	}else {
				def name = reportName == 'cablingQA' ? "CablingQA" : "CablingConflict"
				def filename = 	"${name}-${projectInstance.name}-${bundleName}"
					filename = filename.replace(" ", "_")
        		
				chain(controller:'jasper',action:'index',model:[data:reportFields],
						params:["_format":"PDF","_name":"${filename}","_file":"${params._file}"])
        	}
        }
    }
	/*
	 *  Generate XLS Structured Cabling data report 
	 */
	def cablingDataReport = {
		def currProj = getSession().getAttribute( "CURR_PROJ" )
    	def projectId = currProj.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def cableType = params.cableType
    	// if no moveBundle was selected
    	if(params.moveBundle == "null") {
            flash.message = " Please Select Bundles. "
			redirect( action:'getBundleListForReportDialog', params:[reportId: 'CablingQA'] )
        } else {
        	def moveBundleInstance = MoveBundle.findById(params.moveBundle)
            def reportFields = []
            def bundleName = "All Bundles"
			def cablesQuery = new StringBuffer("from AssetCableMap acm where acm.fromAsset.project.id = $projectInstance.id ")
            //if moveBundleinstance is selected (single moveBundle)
            if( moveBundleInstance ) {
                bundleName = moveBundleInstance?.name
                cablesQuery.append(" and acm.fromAsset.moveBundle = $moveBundleInstance.id ")
            }
            //All Bundles Selected
            else {
            	cablesQuery.append(" and acm.fromAsset.moveBundle != null ")
       		}
            if(cableType){
            	cablesQuery.append(" and acm.fromConnectorNumber.type = '${cableType}' ")
            }
            cablesQuery.append(" order By acm.fromAsset ")
			def assetCablesList = AssetCableMap.findAll( cablesQuery.toString() )
			try {
				File file =  ApplicationHolder.application.parentContext.getResource( "/templates/Cabling_Details.xls" ).getFile()
				WorkbookSettings wbSetting = new WorkbookSettings()
				wbSetting.setUseTemporaryFileDuringWrite(true)
				def workbook = Workbook.getWorkbook( file, wbSetting )
				//set MIME TYPE as Excel
				response.setContentType( "application/vnd.ms-excel" )
				def filename = 	"CablingData-${projectInstance.name}-${bundleName}.xls"
					filename = filename.replace(" ", "_")
				response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
				
				def book = Workbook.createWorkbook( response.getOutputStream(), workbook )
				
				def sheet = book.getSheet("cabling_data")
				def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
				DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
				
				for ( int r = 2; r <= assetCablesList.size(); r++ ) {
					sheet.addCell( new Label( 0, r, String.valueOf(assetCablesList[r-2].fromConnectorNumber.type )) )
					sheet.addCell( new Label( 1, r, String.valueOf(assetCablesList[r-2].fromAsset.assetName )) )
					sheet.addCell( new Label( 2, r, String.valueOf(assetCablesList[r-2].fromAsset.assetTag )) )
					sheet.addCell( new Label( 3, r, String.valueOf(assetCablesList[r-1].fromConnectorNumber.label )) )
					sheet.addCell( new Label( 4, r, String.valueOf(assetCablesList[r-1].fromAsset?.rackTarget?.tag )) )
					sheet.addCell( new Label( 5, r, String.valueOf(assetCablesList[r-1].fromAsset?.targetRackPosition )) )
					sheet.addCell( new Label( 7, r, String.valueOf(assetCablesList[r-1].color ? assetCablesList[r-1].color : "" )) )
					sheet.addCell( new Label( 8, r, String.valueOf(assetCablesList[r-1].toAsset ? assetCablesList[r-1].toAsset?.assetName :"" )) )
					sheet.addCell( new Label( 9, r, String.valueOf(assetCablesList[r-1].toAsset ? assetCablesList[r-1].toAsset?.assetTag :"" )) )
					sheet.addCell( new Label( 10, r, String.valueOf(assetCablesList[r-1].toConnectorNumber ? assetCablesList[r-1].toConnectorNumber?.label :"" )) )
					sheet.addCell( new Label( 11, r, String.valueOf(assetCablesList[r-1].toAsset ? assetCablesList[r-1].toAsset?.rackTarget?.tag :"" )) )
					sheet.addCell( new Label( 12, r, String.valueOf(assetCablesList[r-1].toAssetUposition ? assetCablesList[r-1].toAssetUposition :"" )) )
				}
				
				book.write()
				book.close()
			} catch( Exception ex ) {
				flash.message = "Exception occurred while exporting data"
				redirect( controller:'reports', action:"getBundleListForReportDialog", params:[reportId:'CablingData', message:flash.message] )
				return;
			}
        }
	}
	/*
	 * request page for power report
	 */
	def powerReport = {
		def currProj = getSession().getAttribute( "CURR_PROJ" )
		def projectId = currProj.CURR_PROJ
		def projectInstance = Project.findById( projectId )
		def moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
		userPreferenceService.loadPreferences("CURR_BUNDLE")
		def currentBundle = getSession().getAttribute("CURR_BUNDLE")?.CURR_BUNDLE
		/* set first bundle as default if user pref not exist */
		def isCurrentBundle = true
		def models = AssetEntity.findAll('FROM AssetEntity WHERE project = ? GROUP BY model',[ projectInstance ])?.model
		if(!currentBundle){
			currentBundle = moveBundleInstanceList[0]?.id?.toString()
			isCurrentBundle = false
		}
		
		return [moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance, 
				currentBundle:currentBundle, isCurrentBundle : isCurrentBundle, models:models]
	}
	/*
	 *  Generate Power report in WEb, PDF, Excel format based on user input
	 */
	def powerReportDetails = {

		List bundleId = request.getParameterValues("moveBundle")
		def maxUSize = 42
		if(bundleId == "null") {
			return [errorMessage: "Please Select a Bundle."]
		} else {
			def includeOtherBundle = params.otherBundle
			def includeBundleName = params.bundleName
			def printQuantity = params.printQuantity
			def frontView = params.frontView
			def backView = params.backView
			def sourceRacks = new ArrayList()
			def targetRacks = new ArrayList()
			def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
			def rackLayout = []
			def project = Project.findById(projectId)
			def moveBundles = MoveBundle.findAllByProject( project )
			def powerType = params.powerType ? params.powerType : session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE
			if(!bundleId.contains("all")){
				def bundlesString = bundleId.toString().replace("[","(").replace("]",")")
				moveBundles = MoveBundle.findAll("from MoveBundle m where id in ${bundlesString} ")
			}
			def reportsHasPermission = RolePermissions.hasPermission("reports")
			
			if(request.getParameterValues("sourcerack") != ['none']) {
				def rack = request.getParameterValues("sourcerack")
				if(rack[0] == "") {
					moveBundles.each{ bundle->
						bundle.sourceRacks.each{ sourceRack->
							if( !sourceRacks.contains( sourceRack ) )
								sourceRacks.add( sourceRack )		
						}
					}
				} else {
					rack.each {
						def thisRack = Rack.get(new Long(it))
						if( !sourceRacks.contains( thisRack ) )
							sourceRacks.add( thisRack )
					}
				}
				sourceRacks = sourceRacks.sort { it.tag }
			}

			if(request.getParameterValues("targetrack") != ['none']) {
				def rack = request.getParameterValues("targetrack")
				if(rack[0] == "") {
					moveBundles.each{ bundle->
						bundle.targetRacks.each{ targetRack->
							if( !targetRacks.contains( targetRack ) )
								targetRacks.add( targetRack	)
						}
					}
				} else {
					rack.each {
						def thisRack = Rack.get(new Long(it))
						if( !targetRacks.contains( thisRack ) )
							targetRacks.add( thisRack )
					}
				}
				targetRacks = targetRacks.sort { it.tag }
			}
			
			def racks = sourceRacks + targetRacks
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ	
			def reportDetails = []
			racks.each { rack->
				def assets = rack.assets.findAll { it.assetType !='Blade' && moveBundles?.id?.contains(it.moveBundle?.id) && it.project == project }
				def powerA = 0
				def powerB = 0
				def powerC = 0
				def powerTBD = 0
				def totalPower = 0
				assets.each{ asset->
					def assetPowerCabling = AssetCableMap.findAll("FROM AssetCableMap cap WHERE cap.fromConnectorNumber.type = ? AND cap.fromAsset = ?",["Power",asset])
					def powerConnectors = assetPowerCabling.findAll{it.toPower != null && it.toPower != '' }.size()
					def powerDesign = asset.model?.powerDesign ? asset.model?.powerDesign : 0
					totalPower += powerDesign
					if(powerConnectors){
						def powerUseForConnector = powerDesign ? powerDesign / powerConnectors : 0
						assetPowerCabling.each{ cables ->
							if(cables.toPower){
								switch(cables.toPower){
									case "A": powerA += powerUseForConnector
									break;
									case "B": powerB += powerUseForConnector
									break;
									case "C": powerC += powerUseForConnector
									break;
								}
							}
						}
					} else {
						powerTBD += powerDesign
					}
				}
				powerA = powerType != "Watts" ?  powerA ? (powerA / 120).toFloat().round(1) : 0.0 : powerA ? Math.round(powerA):0 
	            powerB = powerType != "Watts" ?  powerB ? (powerB / 120).toFloat().round(1) : 0.0 : powerB ? Math.round(powerB):0
	            powerC = powerType != "Watts" ?  powerC ? (powerC / 120).toFloat().round(1) : 0.0 : powerC ? Math.round(powerC):0
				powerTBD = powerType != "Watts" ?  powerTBD ? (powerTBD / 120).toFloat().round(1) : 0.0 : powerTBD ? Math.round(powerTBD):0
				totalPower = powerType != "Watts" ?  totalPower ? (totalPower / 120).toFloat().round(1) : 0.0 : totalPower ? Math.round(totalPower):0
				
				reportDetails << [location:rack.location, room:rack.room, rack:rack.tag, devices:assets.size(),
								  powerA:powerA,powerB:powerB,powerC:powerC,powerTBD:powerTBD,totalPower:totalPower]
			}
			if(params.output == "excel"){
				try {
					File file =  ApplicationHolder.application.parentContext.getResource( "/templates/Power_Report.xls" ).getFile()
					WorkbookSettings wbSetting = new WorkbookSettings()
					wbSetting.setUseTemporaryFileDuringWrite(true)
					def workbook = Workbook.getWorkbook( file, wbSetting )
					//set MIME TYPE as Excel
					response.setContentType( "application/vnd.ms-excel" )
					def filename = 	"Power_Report-${project.name}.xls"
						filename = filename.replace(" ", "_")
					response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
					
					def book = Workbook.createWorkbook( response.getOutputStream(), workbook )
					
					def sheet = book.getSheet("Power_Report")
					
					for ( int r = 1; r <= reportDetails.size(); r++ ) {
						sheet.addCell( new Label( 0, r, String.valueOf(reportDetails[r-1].location )) )
						sheet.addCell( new Label( 1, r, String.valueOf(reportDetails[r-1].room )) )
						sheet.addCell( new Label( 2, r, String.valueOf(reportDetails[r-1].rack )) )
						sheet.addCell( new Label( 3, r, String.valueOf(reportDetails[r-1].devices )) )
						sheet.addCell( new Label( 4, r, String.valueOf(reportDetails[r-1].powerA )) )
						sheet.addCell( new Label( 5, r, String.valueOf(reportDetails[r-1].powerB )) )
						sheet.addCell( new Label( 6, r, String.valueOf(reportDetails[r-1].powerC )) )
						sheet.addCell( new Label( 7, r, String.valueOf(reportDetails[r-1].powerTBD )) )
						sheet.addCell( new Label( 8, r, String.valueOf(reportDetails[r-1].totalPower )) )
					}
					
					book.write()
					book.close()
				} catch( Exception ex ) {
					println "Exception occurred while exporting data"+ex
					return;
				}
			} else if(params.output == "pdf"){
				def filename = 	"Power_Report-${project.name}"
					filename = filename.replace(" ", "_")
        		
				chain(controller:'jasper',action:'index',model:[data:reportDetails],
						params:["_format":"PDF","_name":"${filename}","_file":"Power_Report"])
			}
			return [reportDetails : reportDetails]
		}
	}
	def preMoveCheckList={
		def currProj = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
    	def projectInstance = Project.findById( currProj ) 
		def moveEventList = MoveEvent.findAllByProject(projectInstance)
		def moveEventId = securityService.getUserCurrentMoveEventId()
		return ['moveEvents':moveEventList,moveEventId:moveEventId]
	}
	def generateCheckList={
		def project = securityService.getUserCurrentProject()
		def moveEventId = params.moveEvent
		def moveEventInstance
		def errorMsg = "Please select a MoveEvent"
		
		if( moveEventId && moveEventId.isNumber() ){
			def isProjMoveEvent  = MoveEvent.findByIdAndProject( moveEventId, project )
			if ( !isProjMoveEvent ) {
				errorMsg = " User tried to access moveEvent ${moveEventId} that was not found in project : ${project} "
				log.warn "generateCheckList: User tried to access moveEvent ${moveEventId} that was not found in project : ${project}"
			} else {
			    errorMsg = ""
				userPreferenceService.setPreference( "MOVE_EVENT", "${moveEventId}" )
				moveEventInstance = MoveEvent.get(moveEventId)
				return reportsService.generatePreMoveCheckList(project.id, moveEventInstance)
			}
		}
		flash.message = errorMsg
		redirect( action:"preMoveCheckList")
		
	}
	def applicationConflicts = {
		def currProj = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
		def projectInstance = Project.findById( currProj ) 
		def moveBundleList = MoveBundle.findAllByProject(projectInstance)
		def moveBundleId = securityService.getUserCurrentMoveBundleId()
		return ['moveBundles':moveBundleList,moveBundleId:moveBundleId]
	}
	def generateApplicationConflicts = {
		def project = securityService.getUserCurrentProject()
		def moveBundleId = params.moveBundle
		def moveBundleInstance
		def errorMsg = "Please select a MoveBundle"
		def conflicts = params.conflicts == 'on'
		def unresolved = params.unresolved == 'on'
		if( params.moveBundle == 'useForPlanning' )
			return reportsService.genApplicationConflicts(project.id, moveBundleId, conflicts, unresolved, true)
		
		if( moveBundleId && moveBundleId.isNumber() ){
			def isProjMoveBundle  = MoveBundle.findByIdAndProject( moveBundleId, project )
			if ( !isProjMoveBundle ) {
				errorMsg = " User tried to access moveBundle ${moveBundleId} that was not found in project : ${project} "
				log.warn "generateCheckList: User tried to access moveBundle ${moveBundleId} that was not found in project : ${project}"
			} else {
				errorMsg = ""
				userPreferenceService.setPreference( "MOVE_BUNDLE", "${moveBundleId}" )
				moveBundleInstance = MoveBundle.get(moveBundleId)
				//def eventsProjectInfo = getEventsProjectInfo(moveEventInstance,projectInstance,currProj,moveBundles,eventErrorList)
				return reportsService.genApplicationConflicts(project.id, moveBundleId, conflicts, unresolved, false)//.add(['time':])
			}
		}
		flash.message = errorMsg
		redirect( action:"applicationConflicts")
	}
	/*
	 * Generate Issue Report
	 */
	def tasksReport = {
		def taskList = []
		def reqEvents = params.list("moveEvent").toList()
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		
		if(reqEvents) {
			def project = securityService.getUserCurrentProject()
			def allBundles = reqEvents.find{it=='all'} ? true : false
			def badReqEventIds
			
			if( !allBundles ){
				reqEvents = reqEvents.collect {id-> NumberUtils.toDouble(id, 0).round() }
				//Verifying events id are in same project or not.
				badReqEventIds = moveEventService.verifyEventsByProject(reqEvents, project)
			}
			
			//if found any bad id returning to the user
			if( badReqEventIds ){
				flash.message = "Event ids $badReqEventIds is not associated with current project.\
							    Kindly request for project associated  Event ids ."
				return
			}
			
			def argMap = ["type":AssetCommentType.ISSUE, "project":project]
			def taskListHql = "FROM AssetComment WHERE project =:project AND commentType =:type "
			
			if(!allBundles){
				taskListHql +=" AND moveEvent.id IN (:events) "
				argMap <<["events":reqEvents]
			}
			
			if( params.wUnresolved ){
				taskListHql += "AND status != :status"
				argMap << ["status":AssetCommentStatus.COMPLETED]
			}
			
			taskList = AssetComment.findAll(taskListHql, argMap)
			taskList.addAll( params.wComment ? AssetComment.findAllByCommentTypeAndProject(AssetCommentType.COMMENT, project): [])
			
			//Generating XLS Sheet
			
			switch(params._action_tasksReport){
				case "Generate Xls" :
					  exportTaskReportExcel(taskList, tzId, project)
					  break;
					  
				case "Generate Pdf" :
					  exportTaskReportPdf(taskList, tzId, project)
					  break;
					  
			    default :
					 render (view :'tasksReport', model:[taskList : taskList, tzId:tzId]) 
					 break;
			}
		} else{
			flash.message = "Please select move event to get the task report."
			redirect( action:"getBundleListForReportDialog", params:[reportId:"Task Report"])
		}
		
	}
	
	/**
	 * 
	 * Export task report in XLS format
	 * @param taskList : list of tasks 
	 * @param tzId : timezone
	 * @param project : project instance
	 * @return : will generate a XLS file having task task list
	 */
	def exportTaskReportExcel(taskList, tzId, project){
		def book = WorkbookObject.getWritableWorkbook(response, "${project.name}-TaskReport", "/templates/TaskReport.xls" );
		def tasksSheet = book.getSheet("tasks")
		def preMoveColumnList = ['taskNumber', 'taskDependencies', 'assetEntity', 'comment','assignedTo', 'role', 'status',
					'estStart','','', 'notes', 'duration', 'estStart','estFinish','actStart', 'actFinish', 'workflow',
					'dateCreated', 'createdBy', 'moveEvent']
		moveBundleService.issueExport(taskList, preMoveColumnList, tasksSheet, tzId, 7)
		
		book.write()
		book.close()
	}
	
	/**
	 * Export task report in pdf format
	 * @param taskList : list of tasks 
	 * @param tzId : timezone
	 * @param project : project instance
	 * @return : will generate a pdf file having task task list
	 */
	def exportTaskReportPdf(taskList, tzId, project){
		def currDate = GormUtil.convertInToUserTZ(GormUtil.convertInToGMT( "now", "EDT" ),tzId)
		DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		def reportFields = []
		taskList.each{task ->
			reportFields <<[
				'taskNumber':task.taskNumber?.toString() , 
				'taskDependencies': WebUtil.listAsMultiValueString(task.taskDependencies?.assetComment?.comment) ,
				"assetEntity":task.assetEntity?.assetName,"comment":task.comment,
				"assignedTo":task.assignedTo? task.assignedTo.toString():"", "status":task.status,
				"datePlanned":"","outStanding":"","dateRequired":"", 'workflow':"",
				"clientName":project?.client?.name,"team":task.role? task.role.toString():"",
				'projectName':project?.name,'notes':task.notes? WebUtil.listAsMultiValueString(task.notes):"",
				'duration':task.duration ? task.duration.toString():"", 
				'estStart':task.estStart? formatter.format(GormUtil.convertInToUserTZ(task.estStart, tzId )).toString():"",
				'estFinish':task.estFinish? formatter.format(GormUtil.convertInToUserTZ(task.estFinish, tzId )).toString(): "",
				'actStart':task.actStart? formatter.format(GormUtil.convertInToUserTZ(task.actStart, tzId )).toString():"",
				'actFinish':task.actFinish? formatter.format(GormUtil.convertInToUserTZ(task.actFinish, tzId )).toString():"",
				"createdOn":task.dateCreated? formatter.format(GormUtil.convertInToUserTZ(task.dateCreated, tzId )).toString():"",
				"createdBy":task.createdBy.toString() ,"moveEvent":task.moveEvent? task.moveEvent.toString():"",
				'timezone':tzId ? tzId : "EDT","rptTime":String.valueOf(formatter.format( currDate ) )]
		}
		if(reportFields.size() <= 0) {
			flash.message = " No Assets Were found for  selected values  "
			redirect( action:'getBundleListForReportDialog', params:[reportId: 'Task Report'] )
		}else {
			def filename = 	"${project.name}-TaskReport"
			chain(controller:'jasper',action:'index',model:[data:reportFields],
					params:["_format":"PDF","_name":"${filename}","_file":"taskReport"])
		}
	} 
}
 