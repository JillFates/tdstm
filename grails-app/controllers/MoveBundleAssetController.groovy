import grails.converters.JSON
import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetEntity;
import com.tdssrc.grails.GormUtil
import java.text.SimpleDateFormat
import java.text.DateFormat
class MoveBundleAssetController {
	def partyRelationshipService
	def assetEntityAttributeLoaderService
	def userPreferenceService
	def jdbcTemplate
	def supervisorConsoleService
	def securityService
	protected static targetTeamType = ['MOVE_TECH':'targetTeamMt', 'CLEANER':'targetTeamLog','SYS_ADMIN':'targetTeamSa',"DB_ADMIN":'targetTeamDba']
	protected static sourceTeamType = ['MOVE_TECH':'sourceTeamMt', 'CLEANER':'sourceTeamLog','SYS_ADMIN':'sourceTeamSa',"DB_ADMIN":'sourceTeamDba']
	
    def index = { redirect( action:list, params:params ) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
        [ moveBundleAssetInstanceList: AssetEntity.list( params ) ]
    }

    def show = {
        def moveBundleAssetInstance = AssetEntity.get( params.id )

        if(!moveBundleAssetInstance) {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ moveBundleAssetInstance : moveBundleAssetInstance ] }
    }

    def delete = {
        def moveBundleAssetInstance = AssetEntity.get( params.id )
        if(moveBundleAssetInstance) {
            moveBundleAssetInstance.delete(flush:true)
            flash.message = "MoveBundleAsset ${params.id} deleted"
            redirect( action:list )
        }
        else {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect( action:list )
        }
    }

    def edit = {
        def moveBundleAssetInstance = AssetEntity.get( params.id )

        if(!moveBundleAssetInstance) {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect( action:list )
        }
        else {
            return [ moveBundleAssetInstance : moveBundleAssetInstance ]
        }
    }

    def update = {
        def moveBundleAssetInstance = AssetEntity.get( params.id )
        if(moveBundleAssetInstance) {
            moveBundleAssetInstance.properties = params
            if(!moveBundleAssetInstance.hasErrors() && moveBundleAssetInstance.save()) {
                flash.message = "MoveBundleAsset ${params.id} updated"
                redirect( action:show, id:moveBundleAssetInstance.id )
            }
            else {
                render( view:'edit', model:[ moveBundleAssetInstance:moveBundleAssetInstance ] )
            }
        }
        else {
            flash.message = "MoveBundleAsset not found with id ${params.id}"
            redirect( action:edit, id:params.id )
        }
    }

    def create = {
        def moveBundleAssetInstance = new AssetEntity()
        moveBundleAssetInstance.properties = params
        return ['moveBundleAssetInstance':moveBundleAssetInstance]
    }

    def save = {
        def moveBundleAssetInstance = new AssetEntity( params )
        if(!moveBundleAssetInstance.hasErrors() && moveBundleAssetInstance.save()) {
            flash.message = "MoveBundleAsset ${moveBundleAssetInstance.id} created"
            redirect( action:show, id:moveBundleAssetInstance.id )
        } else {
            render( view:'create', model:[ moveBundleAssetInstance:moveBundleAssetInstance ] )
        }
    }
	/*
	 *  Return asset details to assignAssets page
	 */
    def assignAssetsToBundle = {
		def moveBundleInstance
		def project = securityService.getUserCurrentProject()
		if(params.containsKey('bundleId') && params.bundleId){
			if( !params.bundleId.isNumber() ){
				log.error "assignAssetsToBundle: Invalid bundle id (${params.bundleId}"
			}else{
				def bundleId = params.bundleId
				moveBundleInstance = MoveBundle.findById( bundleId)
			}
		} else {
		   moveBundleInstance = MoveBundle.findByProject(project,[sort:'name',order:'asc']  )
        }
    	def moveBundles = MoveBundle.findAll("from MoveBundle where project.id = $moveBundleInstance.project.id order by name")
    	def currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $moveBundleInstance.id")
    	def moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $moveBundleInstance.project.id ")
    	render( view:'assignAssets', model:[moveBundles:moveBundles, currentBundleAssets: currentBundleAssets, 
    	                                    moveBundleInstance:moveBundleInstance, moveBundleAssets:moveBundleAssets ] )
    }
    def assignAssetsToBundleChange = {
        def bundleRight = params.bundleRight
        def bundleLeft = params.bundleLeft
        def sortField
        if( params.sortField == "lapplication" ) {
            sortField = "application"
        } else {
            sortField = params.sortField
        }
        def sideField = params.sideField
        def currentBundleAssets
        def moveBundleAssets
        def moveBundleInstanceRight = MoveBundle.findById( bundleRight )
        def moveBundleInstanceLeft = MoveBundle.findById( bundleLeft )
        def moveBundles = MoveBundle.findAll("from MoveBundle where project.id = $moveBundleInstanceRight.project.id order by name")
	    def sessionSort = request.getSession(true).getAttribute("sessionSort")
        def sessionSide = request.getSession(true).getAttribute("sessionSide")
        def sessionOrder = request.getSession(true).getAttribute("sessionOrder")
        if( bundleRight ){
            if ( sideField == "right" ) {
                currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $moveBundleInstanceRight.id "+
                											"order by  $sortField $params.orderField")
	    			
            } else {
                if ( sessionSide == "right" ) {
                    currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $moveBundleInstanceRight.id "+
                    											"order by  $sessionSort $sessionOrder")
                } else {
                    currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $moveBundleInstanceRight.id")
                }
            }
        }
        if( bundleLeft ){
            if( sideField == "left" ){
                moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $moveBundleInstanceLeft.id "+
                    									"order by  $sortField $params.orderField")
	    			
            } else {
                if( sessionSide == "left" ){
                    moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $moveBundleInstanceLeft.id "+
                    										"order by  $sessionSort $sessionOrder")
                } else {
                    moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $moveBundleInstanceLeft.id")
                }
            }
        } else {
            def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
            if( sideField == "left" ){
                moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId "+
                    									"order by  $sortField $params.orderField")
            } else {
                if( sessionSide == "left" ){
	    			moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId "+
                    										"order by  $sessionSort $sessionOrder")	
                } else {
	    		    moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId")
                }
            }
        }
        render( view:'assignAssets', model:[moveBundles:moveBundles, currentBundleAssets: currentBundleAssets, 
                                            moveBundleInstance:moveBundleInstanceRight,moveBundleAssets:moveBundleAssets,
                                            leftBundleInstance:moveBundleInstanceLeft,sortField:params.sortField,
                                            orderField:params.orderField,sideField:params.sideField] )
    }
	
	 
	/*
	 *  Sort Assets By Selected Row Column 
	 */
	def sortAssetList = {
		def rightBundleId = params.rightBundle
		def leftBundleId = params.leftBundle
		def sortField
		if( params.sort == "lapplication" ) {
			sortField = "application"
		}else {
			sortField = params.sort
		}
		if( params.sortField == "lapplication" ) {
			params.sortField = "application"
		}
		def sideList = params.side
		def rightMoveBundleInstance
		def leftMoveBundleInstance
		def moveBundles
		def currentBundleAssets
		def moveBundleAssets
		def sessionSort
		def sessionSide
		def sessionOrder
		def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
		if( ( sideList == "right" && params.sideField == "left" )||( sideList == "left" && params.sideField == "right" ) ){
			request.getSession(true).setAttribute( "sessionSort", params.sortField )
			request.getSession(true).setAttribute( "sessionSide", params.sideField )
			request.getSession(true).setAttribute( "sessionOrder", params.orderField )	
		}
		if( ( sideList == "right" && params.sideField == "right" )||( sideList == "left" && params.sideField == "left" ) ){
            sessionSort = request.getSession(true).getAttribute( "sessionSort" )
            sessionSide = request.getSession(true).getAttribute( "sessionSide" )
            sessionOrder = request.getSession(true).getAttribute( "sessionOrder" )
		}
		//Right Side AssetTable Sort
		if( sideList == "right" ) {
			rightMoveBundleInstance = MoveBundle.findById( rightBundleId )
			moveBundles = MoveBundle.findAll("from MoveBundle where project.id = $rightMoveBundleInstance.project.id")
			currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $rightMoveBundleInstance.id "+
                    									"order by  $sortField $params.order")
			if( leftBundleId != null && leftBundleId != "" ) {
				leftMoveBundleInstance = MoveBundle.findById( leftBundleId )
				if( params.sideField == "left" ){
                    moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $leftMoveBundleInstance.id "+
                    										"order by  $params.sortField $params.orderField")
				
				} else if( params.sideField == "right" ) {
					if( sessionSort != null ){
						moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $leftMoveBundleInstance.id "+
                    											"order by  $sessionSort $sessionOrder")
						
					} else {
                        moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $leftMoveBundleInstance.id ")
					}
					
				} else {
                    moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $leftMoveBundleInstance.id ")
				}
			} else {
				if( params.sideField == "left" ){
                    moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId "+
                    										"order by  $params.sortField $params.orderField")
				} else if ( params.sideField == "right" ) {
					if( sessionSort != null ){
						moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId "+
                    											"order by  $sessionSort $sessionOrder")	
					} else {
						moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId")		
					}
				} else {
                    moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId")
				}
			}
		}
		//Left Side AssetTable Sort
		else {
			if( leftBundleId != null && leftBundleId != "" ) {
				leftMoveBundleInstance = MoveBundle.findById( leftBundleId )
				moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $leftMoveBundleInstance.id "+
                    									"order by  $sortField $params.order ")
		    } else {
		    	moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId "+
                    									"order by  $sortField $params.order")
		    }
			rightMoveBundleInstance = MoveBundle.findById( rightBundleId )
			moveBundles = MoveBundle.findAll("from MoveBundle where project.id = $rightMoveBundleInstance.project.id")
			if( params.sideField == "right" ){
                currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $rightMoveBundleInstance.id "+
                    									"order by  $params.sortField $params.orderField")
			} else if ( params.sideField == "left" ) {
				if( sessionSort != null ){
					currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $rightMoveBundleInstance.id "+
                    											"order by  $sessionSort $sessionOrder")	
				} else {
					currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $rightMoveBundleInstance.id ")	
				}
			} else {
                currentBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle.id = $rightMoveBundleInstance.id ")
			}
		}
		render( view:'assignAssets', model:[moveBundles:moveBundles, currentBundleAssets: currentBundleAssets, 
		                                    moveBundleInstance:rightMoveBundleInstance, leftBundleInstance:leftMoveBundleInstance, 
		                                    moveBundleAssets:moveBundleAssets, sortField:params.sort, 
		                                    orderField:params.order, sideField:params.side ] )
	}
	/*
	 *  Save Assets for corresponding Bundle
	 */
    def saveAssetsToBundle = {
		def items = []
    	def bundleFrom = params.bundleFrom
    	def bundleTo = params.bundleTo
    	def assets = params.assets
    	def moveBundleAssets = assetEntityAttributeLoaderService.saveAssetsToBundle( bundleTo, bundleFrom, assets )
    	if( moveBundleAssets != null ){
	    	moveBundleAssets.each{bundleAsset ->
		    	bundleAsset.updateRacks()
				items <<[id:bundleAsset.id, assetName:bundleAsset.assetName, assetTag:bundleAsset.assetTag, 
				         application:bundleAsset.application, srcLocation:bundleAsset.sourceLocation  +"/"+bundleAsset.sourceRack  ]
	    	}
        } else {
    		def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
    		def assetEntities = AssetEntity.findAll("from AssetEntity where moveBundle = null and project = $projectId ")
			assetEntities.each{assetEntity ->
				items <<[id:assetEntity.id, assetName:assetEntity.assetName, assetTag:assetEntity.assetTag, 
				         application:assetEntity.application, srcLocation:assetEntity.sourceLocation +"/"+assetEntity.sourceRack  ]
			}
    	}
		render items as JSON
    }
    /*
     * Get the Assets and Teams those belongs to Selected  MoveBundle
     */
    def bundleTeamAssignment = {
    	def bundleId = params.moveBundle
		if(!params.role) params.role = "MOVE_TECH"
    	if( bundleId ){
    		userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
    	} else {
            userPreferenceService.loadPreferences("CURR_BUNDLE")
    		bundleId = getSession().getAttribute("CURR_BUNDLE").CURR_BUNDLE
    	}
		def bundleInstance 
		if(bundleId){
		   bundleInstance =  MoveBundle.findById(bundleId)
		}else{
		   def project = Project.findById(getSession().getAttribute("CURR_PROJ").CURR_PROJ)
		   bundleInstance = MoveBundle.findByProject(project)
		}
    	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.role = '${params.role}' " )
    	def teamAssetCounts = []
    	def cartAssetCounts
    	def assetEntitysRacks
    	def rackPlan
    	def assetEntityList = AssetEntity.findAllByMoveBundle( bundleInstance )
    	if( params.rackPlan == 'RerackPlan' ) {
    		rackPlan = "RerackPlan"
			def assetEntityInstanceList = []
			assetEntityList.each{ assetEntity->
				assetEntityInstanceList << [id:assetEntity.id,
											assetTag:assetEntity.assetTag,
											assetName:assetEntity.assetName,
											modelName:assetEntity.model ,
											usize:assetEntity.model?.usize,
											room:assetEntity.targetRoom,
											rack:assetEntity.targetRack,
											rackPosition:assetEntity.targetRackPosition,
											teamCode:assetEntity[targetTeamType.get(params.role)]?.teamCode,
											teamId:assetEntity[targetTeamType.get(params.role)]?.id,
											cart:assetEntity.cart,
											shelf:assetEntity.shelf
											]
			}
    		assetEntitysRacks = AssetEntity.findAll("from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.targetRack")
    		projectTeamInstanceList.each{projectTeam ->
				def assetCount = assetEntityInstanceList.findAll{it.teamId == projectTeam.id}?.size()
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		def unAssignCount = assetEntityInstanceList.findAll{!it.teamId}?.size()
			teamAssetCounts << [ teamCode: "UnAssigned" , assetCount:unAssignCount ]
    		def cartList = AssetEntity.findAll("from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.cart")
			cartAssetCounts = assetEntityAttributeLoaderService.getCartAssetCounts(bundleId)
    		render(view:'bundleTeamAssignment',model:[assetEntityInstanceList: assetEntityInstanceList, moveBundleInstance:bundleInstance, 
    		                                          projectTeamInstance:projectTeamInstanceList, teamAssetCount:teamAssetCounts, 
    		                                          assetEntitysRacks:assetEntitysRacks, rack:rackPlan, cartAssetCountList:cartAssetCounts, role:params.role ])
    	}else 
    	{
    		rackPlan = "UnrackPlan"
			def assetEntityInstanceList = []
			assetEntityList.each{ assetEntity->
				assetEntityInstanceList << [id:assetEntity.id,
											assetTag:assetEntity.assetTag,
											assetName:assetEntity.assetName,
											modelName:assetEntity.model ,
											usize:assetEntity.model?.usize,
											room:assetEntity.sourceRoom,
											rack:assetEntity.sourceRack,
											rackPosition:assetEntity.sourceRackPosition,
											teamCode:assetEntity[sourceTeamType?.get(params.role)]?.teamCode,
											teamId:assetEntity[sourceTeamType.get(params.role)]?.id
											]
			}
    		assetEntitysRacks = AssetEntity.findAll("from AssetEntity ma  where ma.moveBundle = $bundleInstance.id group by ma.sourceRack") 
    		projectTeamInstanceList.each{projectTeam ->
				def assetCount = assetEntityInstanceList.findAll{it.teamId == projectTeam.id}?.size()//AssetEntity.countByMoveBundleAndSourceTeamMt( bundleInstance, projectTeam )
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		def unAssignCount = assetEntityInstanceList.findAll{!it.teamId}?.size()
			teamAssetCounts << [ teamCode: "UnAssigned" , assetCount:unAssignCount ]
    		render(view:'bundleTeamAssignment',model:[assetEntityInstanceList: assetEntityInstanceList, moveBundleInstance:bundleInstance, 
    		                                          projectTeamInstance:projectTeamInstanceList, teamAssetCount:teamAssetCounts, 
    		                                          assetEntitysRacks:assetEntitysRacks, rack:rackPlan, role:params.role ])
    	}
    }
    
    /*
     * Assign Asset to Selected MoveBundleTeam
     */ 
    def assetTeamAssign = {
    	def team = params.teamId
    	def asset = params.asset
		def role = params.role
    	def rackPlan = params.rackPlan
    	def bundleId = params.bundleId
    	def teamAssetCounts = []
    	def bundleInstance = MoveBundle.findById(bundleId)
    	def assetAssigned = []
    	if( team != null && team != "" && team != "null" )
    	{
    		def projectTeamInstance = ProjectTeam.findById(team)
    		if( projectTeamInstance ) {
    			def assetEntityInstance = AssetEntity.findById(asset)
    			if( rackPlan == "UnrackPlan" ) {
    				assetEntityInstance[sourceTeamType.get(role)] = projectTeamInstance
    			} else {
    				assetEntityInstance[targetTeamType.get(role)] = projectTeamInstance
    			}
    			assetEntityInstance.save(flush:true)
    			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleInstance, rackPlan, role )
    		}
    	} else {
    		def assetEntityInstance = AssetEntity.findById(asset)
			if( rackPlan == "UnrackPlan" ) {
				assetEntityInstance[sourceTeamType.get(role)] = null
			} else {
				assetEntityInstance[targetTeamType.get(role)] = null
			}
    		assetEntityInstance.save(flush:true)
			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleInstance, rackPlan, role )
    	}
    	render teamAssetCounts as JSON
    }
    /*
     * AutoFill with Selected Team  for list of Assets
     */
    def autoFillTeamAssign = {
    	def team = params.teamCode
    	def assetString = params.assets
    	def  assets= assetString.tokenize(",")
    	def bundleId = params.bundleId
    	def rackPlan = params.rackPlan
		def role = params.role
    	def teamAssetCounts
    	def assetEntityList
    	def teamAssetList = []
    	def assetList
    	def teamId= null
    	def teamName = ""
    	def bundleInstance = MoveBundle.findById(bundleId)
    	if( team == "UnAssign" ) {
    		def modifiedAssetList = []
    		for( int assetRow = 0; assetRow < assets.size(); assetRow ++ ) {
				def assetEntityInstance = AssetEntity.findById(assets[ assetRow ] )
				if( rackPlan == "UnrackPlan" ) {
					assetEntityInstance[sourceTeamType.get(role)] = null
				} else {
					assetEntityInstance[targetTeamType.get(role)] = null
				}
				assetEntityInstance.save(flush:true)
				modifiedAssetList.add(assetEntityInstance)
				assetEntityList = modifiedAssetList
				assetList = assetEntityAttributeLoaderService.getAssetList( assetEntityList, rackPlan, bundleInstance, role)
				teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleInstance, rackPlan, role )
			}
    	} else {
    		def modifiedAssetList = []
    		def projectTeamInstance = ProjectTeam.findById(team)
    		if( projectTeamInstance ) {
    			for( int assetRow = 0; assetRow < assets.size(); assetRow ++ ) {
    				def assetEntityInstance = AssetEntity.findById(assets[assetRow] )
    				if( rackPlan == "UnrackPlan" || team == "UnAssign" ) {
    					assetEntityInstance[sourceTeamType.get(role)] = projectTeamInstance
    				} else {
    					assetEntityInstance[targetTeamType.get(role)] = projectTeamInstance
    				}
    				assetEntityInstance.save(flush:true)
    				modifiedAssetList.add(assetEntityInstance)
    			}
    		}
    		assetEntityList = modifiedAssetList
			assetList = assetEntityAttributeLoaderService.getAssetList( assetEntityList, rackPlan, bundleInstance, role)
			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleInstance, rackPlan, role )
    	}
    	teamAssetList << [ teamAssetCounts:teamAssetCounts, assetList:assetList ]
    	render teamAssetList as JSON
    }
    /*
     * Filter Assets By Team
     */
    def filterAssetByTeam = {
    	def team = params.teamCode
    	def bundleId = params.bundleId
    	def rackPlan = params.rackPlan
		def role = params.role
    	def bundleInstance = MoveBundle.findById(bundleId)
    	def assetEntity = []
    	def projectTeam = []
    	def assetEntityList
    	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.role = '${role}' " )
		def projectTeamInstance = team != "unAssign" ? ProjectTeam.findById(team) : null
       	projectTeamInstanceList.each{teams ->
            projectTeam << [ teamCode: teams.teamCode, id:teams.id ]
        }
    	if( team == "" || team == null ) {
    		assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id ")
    	} else if ( rackPlan == "UnrackPlan" ) {
    		if( team == "unAssign" ) {
    			assetEntityList = AssetEntity.findAll( " from AssetEntity where moveBundle = $bundleInstance.id and ${sourceTeamType.get(params.role)} = null ")
        	} else {
        		
        		assetEntityList = AssetEntity.findAll( " from AssetEntity where moveBundle = $bundleInstance.id and ${sourceTeamType.get(params.role)} = $projectTeamInstance.id ")
        	}
    	} else {
    		if( team == "unAssign" ) {
    			assetEntityList = AssetEntity.findAll( " from AssetEntity where moveBundle = $bundleInstance.id and ${targetTeamType.get(params.role)} = null ")
        	} else {
        		assetEntityList = AssetEntity.findAll( " from AssetEntity where moveBundle = $bundleInstance.id and ${targetTeamType.get(params.role)} = $projectTeamInstance.id ")
        	}
    	}
    	for( int assetRow = 0; assetRow < assetEntityList.size(); assetRow++) {
    		def displayTeam  
    		if( rackPlan == "RerackPlan" ) {
    			displayTeam = assetEntityList[assetRow][targetTeamType.get(params.role)]?.teamCode
    		} else {
    			displayTeam = assetEntityList[assetRow][sourceTeamType.get(params.role)]?.teamCode
    		}
    		def assetEntityInstance = AssetEntity.findById( assetEntityList[assetRow]?.id )
    		assetEntity <<[id:assetEntityInstance?.id, assetName:assetEntityInstance?.assetName, model:assetEntityInstance?.model?.toString(), 
    						sourceLocation:assetEntityInstance?.sourceLocation, sourceRack:assetEntityInstance?.sourceRack, 
    						targetLocation:assetEntityInstance?.targetLocation, targetRack:assetEntityInstance?.targetRack, 
    						sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, 
    						uSize:assetEntityInstance?.model?.usize,team:displayTeam, cart:assetEntityList[assetRow]?.cart, 
    						shelf:assetEntityList[assetRow]?.shelf, projectTeam:projectTeam, assetTag:assetEntityInstance?.assetTag]
    	}
    	render assetEntity as JSON
    }
    /*
     * Filter Assets By Rack
     */
    def filterAssetByRack = {
    	def rackList = []
    	rackList = (params.rack).tokenize(",")
    	def rackString = "("
    	if( rackList.size()<=0 || rackList[0] == null || rackList[0] == "all" ){
    		rackString = null
    	} else {
    		for( int rack = 0; rack<rackList.size(); rack++ ) {
    			rackString = rackString +"'"+ rackList[rack]+"'"
    			if( rack+1 >= rackList.size ){
    				rackString = rackString+')'
    			} else {
    				rackString = rackString+','
    			}
    		}
    	}
       	def bundleId = params.bundleId
       	def rackPlan = params.rackPlan
		def role = params.role
       	def bundleInstance = MoveBundle.findById(bundleId)
       	def moveBundleAsset = []
       	def moveBundleAssetList
       	def projectTeam = []
       	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.role = '${role}' " )
       	projectTeamInstanceList.each{team ->
            projectTeam << [ teamCode: team.teamCode, id:team.id ]
        }
       	if( rackString == "" || rackString == null ) {
       		moveBundleAssetList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id ")
       	} else if ( rackPlan == "UnrackPlan" ) {
       		moveBundleAssetList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and ma.sourceRack in $rackString ")
       	} else {
       		moveBundleAssetList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and ma.targetRack in $rackString ")
       	}
       	
    	if( moveBundleAssetList != null ) {
    		for( int assetRow = 0; assetRow < moveBundleAssetList.size(); assetRow++) {
    			def displayTeam  
    			if( rackPlan == "RerackPlan" ) {
    				displayTeam = moveBundleAssetList[assetRow][targetTeamType.get(params.role)]?.teamCode
    			}else {
    				displayTeam = moveBundleAssetList[assetRow][sourceTeamType.get(params.role)]?.teamCode
    			}
    			def assetEntityInstance = AssetEntity.findById( moveBundleAssetList[assetRow]?.id )
    			moveBundleAsset << [id:assetEntityInstance?.id, assetName:assetEntityInstance?.assetName, model:assetEntityInstance?.model?.toString(), 
			    					sourceLocation:assetEntityInstance?.sourceLocation, sourceRack:assetEntityInstance?.sourceRack, 
			    					targetLocation:assetEntityInstance?.targetLocation, targetRack:assetEntityInstance?.targetRack, 
			    					sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, 
			    					uSize:assetEntityInstance?.model?.usize, team:displayTeam, cart:moveBundleAssetList[assetRow]?.cart, 
			    					shelf:moveBundleAssetList[assetRow]?.shelf, projectTeam:projectTeam, assetTag:assetEntityInstance?.assetTag]
    		}
    	}
        render moveBundleAsset as JSON
    }
	//Updating #Cart for Selected Asset 
	def assetCartAssign = {
		def cart = params.cartNumber
	    def asset = params.asset
	    def bundleId = params.bundleId
	    def cartAssetCounts 
	    def bundleInstance = MoveBundle.findById(bundleId)
	    if( cart != null && cart != "")
	    {
	    	def assetEntityInstance = AssetEntity.findById( asset )
	    	def moveBundleAsset = AssetEntity.executeUpdate(" update AssetEntity ma set ma.cart = :cart where "+
                    										"ma.moveBundle = $bundleInstance.id and  ma.id = $assetEntityInstance.id ",[cart:cart])
	    }
		def cartList = AssetEntity.findAll("from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.cart")
		cartAssetCounts = assetEntityAttributeLoaderService.getCartAssetCounts(bundleId)
	    render cartAssetCounts as JSON
	}
	//Updating #Shelf for Selected Asset
	def assetShelfAssign = {
		def shelf = params.shelfNumber
	    def asset = params.asset
	    def bundleId = params.bundleId
	    def teamAssetCounts = []
	    def bundleInstance = MoveBundle.findById(bundleId)
	    if( shelf != null )
	    {
	    	def assetEntityInstance = AssetEntity.findById( asset )
	    	def moveBundleAsset = AssetEntity.executeUpdate(" update AssetEntity ma set ma.shelf = '$shelf' where "+
                    										"ma.moveBundle = $bundleInstance.id and  ma.id = $assetEntityInstance.id ")
	    }
	    render teamAssetCounts as JSON
	}
    
    //get teams for selected bundles.
    def getTeamsForBundles = {
    	def bundleId = params.bundleId
    	def projectId = session.CURR_PROJ.CURR_PROJ
    	def projectInstance = Project.findById( projectId )
    	def TeamInstance 
        if( bundleId == "") {
            TeamInstance = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle in ( select m.id from MoveBundle m where "+
                    							"m.project = $projectId ) " )
        } else {
            def moveBundleInstance = MoveBundle.findById( bundleId )
            TeamInstance = ProjectTeam.findAllByMoveBundle( moveBundleInstance )
        }
        def teams = []
        TeamInstance.each {
            teams <<[id:it.id, name:it.teamCode]
        }
        render teams as JSON
    }
    //Get the List of Racks corresponding to Selected Bundle
    def getRacksForBundles = {
    	def bundleId = params.bundleId
        def projectId = session.CURR_PROJ.CURR_PROJ
        def projectInstance = Project.findById( projectId )
        def movebundleInstance = MoveBundle.findById(bundleId)
       	def assetEntityList = AssetEntity.findAllByMoveBundle(movebundleInstance)
       	def racks = []
    	assetEntityList.each {
           	racks <<[id:it.sourceRack, name:it.sourceRack]    		
    	}
    	render racks as JSON
    }
	
	
	/*----------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : move bundle id
	 * @return : list of racks and rooms
	 *---------------------------------------*/
	 def getRackDetails = {
    	def bundleId = params.bundleId
    	def rackDetails = []
    	def sourceRackList
    	def targetRackList
    	def queryForSourceRacks = "select source_location as location, source_rack as rack, source_room as room from asset_entity where asset_type NOT IN ('VM', 'Blade') and source_rack != '' and source_rack is not null "
    	def queryForTargetRacks = "select target_location as location, target_rack as rack, target_room as room from asset_entity where asset_type NOT IN ('VM', 'Blade') and target_rack != '' and target_rack is not null "
    	def sourceGroup = "group by source_location, source_rack, source_room"
    	def targetGroup = "group by target_location, target_rack, target_room"
    	if(bundleId){
    		userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
    		sourceRackList = jdbcTemplate.queryForList(queryForSourceRacks + "and move_bundle_id = $bundleId " + sourceGroup )
    		targetRackList = jdbcTemplate.queryForList(queryForTargetRacks + "and move_bundle_id = $bundleId " + targetGroup)
    	} else if(bundleId == ""){
    		def projectId = getSession().getAttribute("CURR_PROJ").CURR_PROJ
     		sourceRackList = jdbcTemplate.queryForList(queryForSourceRacks + "and project_id = $projectId " + sourceGroup)
			targetRackList = jdbcTemplate.queryForList(queryForTargetRacks + "and project_id = $projectId " + targetGroup)
    	}
    	rackDetails << [sourceRackList:sourceRackList, targetRackList:targetRackList]
    	render rackDetails as JSON
	 }
	/*----------------------------------------------
	 * return the asset d
	 *---------------------------------------------*/
	def getAssetTagLabelData = {
		def moveBundleId = params.moveBundle
		def location = params.location
		def projectId = params.project
	    def reportFields = []
		if( !moveBundleId || !projectId ) {    		
			reportFields <<[ 'flashMessage': "Please Select Bundles."]
			render reportFields as JSON
	    } else {
			def assetsQuery = new StringBuffer(""" SELECT ae.asset_entity_id as id, ae.asset_name as assetName, ae.asset_tag as assetTag,
								ae.move_bundle_id as bundle, ae.asset_type as type, ae.source_blade_chassis as chassis, ae.source_rack as rack, 
								ae.source_blade_position as bladePos, ae.source_rack_position as uposition
								FROM asset_entity ae WHERE ae.project_id = ${projectId} """)
			if(moveBundleId != "all"){
				assetsQuery.append(" AND ae.move_bundle_id = ${moveBundleId} ")
			}
			assetsQuery.append("ORDER BY ae.source_rack, ae.source_rack_position DESC")
	    	def assetEntityList = jdbcTemplate.queryForList( assetsQuery.toString() )
			if( !assetEntityList ){
				reportFields <<[ 'flashMessage': "Team Members not Found for selected Teams"]
			} else {
				assetEntityList.each{
					if(it.type == "Blade"){
						def chassisAsset = AssetEntity.findWhere(assetTag:it.chassis,moveBundle:MoveBundle.get(it.bundle))
						def pos = it.bladePos ? "-"+it.bladePos : ""
						it.rack = chassisAsset?.sourceRack +  pos
						it.assetName = chassisAsset?.assetName
					} else {
						def pos = it.uposition ? "-"+it.uposition : ""
						it.rack = it.rack + pos
					}
				}
				
				reportFields << assetEntityList
			}
			println "==============================="+reportFields
			render reportFields  as JSON
	    }
	}
	/*********************************************************************
     * Sort Assets By selected value
     * @param : sort field, order, filters(rack, team), bundle, rackPlan
     * @return : assetEntity list as JSON
     ********************************************************************/
    def sortAssets = {
    	def rackList = []
    	rackList = (params.rack).tokenize(",")
    	def rackString = "("
    	if( rackList.size()<=0 || rackList[0] == null || rackList[0] == "all" ){
    		rackString = null
    	} else {
    		for( int rack = 0; rack<rackList.size(); rack++ ) {
    			rackString = rackString +"'"+ rackList[rack]+"'"
    			if( rack+1 >= rackList.size ){
    				rackString = rackString+')'
    			} else {
    				rackString = rackString+','
    			}
    		}
    	}
       	def bundleId = params.bundleId
       	def rackPlan = params.rackPlan
		def moveTeam = params.team
       	def bundleInstance = MoveBundle.findById(bundleId)
       	def moveBundleAsset = []
       	def moveBundleAssetList
       	def projectTeam = []
       	def sortBy = params.sortBy
		def order = params.order
       	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.role = '${params.role}'" )
       	projectTeamInstanceList.each{team ->
            projectTeam << [ teamCode: team.teamCode, id:team.id ]
        }
       	def queryString = new StringBuffer(" from AssetEntity ma where ma.moveBundle = $bundleInstance.id ")
       	if( rackString != "" && rackString != null ) {
	       	if ( rackPlan == "UnrackPlan" ) {
	       		queryString.append( " and ma.sourceRack in $rackString ")
	       	} else {
	       		queryString.append( " and ma.targetRack in $rackString ")
	       	}
       	}
		   
       	if( moveTeam != "" && moveTeam != null ) {
	       	if ( rackPlan == "UnrackPlan" ) {
	    		if( moveTeam == "unAssign" ) {
	    			queryString.append( " and (ma.${sourceTeamType.get(params.role)} = null or ma.${sourceTeamType.get(params.role)} = '')")
	        	} else {
	        		def projectTeamInstance = ProjectTeam.findById(moveTeam)
	        		queryString.append(" and ma.${sourceTeamType.get(params.role)} = $projectTeamInstance.id ")
	        	}
	    	} else {
	    		if( moveTeam == "unAssign" ) {
	    			queryString.append( " and (ma.${targetTeamType.get(params.role)} = null or ma.${targetTeamType.get(params.role)} = '')")
	        	} else {
	        		def projectTeamInstance = ProjectTeam.findById(moveTeam)
					queryString.append(" and ma.${targetTeamType.get(params.role)} = $projectTeamInstance.id ")
	        	}
	    	}
       	}
       	switch(sortBy ){
       	case "assetTag":
       		queryString.append( " order by ma.assetTag ${order} ")
       		break;
    	case "assetName":
    		queryString.append( " order by ma.assetName ${order} ")
       		break;
    	case "model":
    		queryString.append( " order by ma.model.modelName ${order} ")
       		break;
    	case "room":
    		if ( rackPlan == "UnrackPlan" ) {
    			queryString.append( " order by ma.sourceRoom ${order} ")
    		} else {
    			queryString.append( " order by ma.targetRoom ${order} ")
    		}
       		break;
    	case "rack":
    		if ( rackPlan == "UnrackPlan" ) {
    			queryString.append( " order by ma.sourceRack ${order} ")
    		} else {
    			queryString.append( " order by ma.targetRack ${order} ")
    		}
       		break;
    	case "uposition":
    		if ( rackPlan == "UnrackPlan" ) {
    			queryString.append( " order by ma.sourceRackPosition ${order} ")
    		} else {
    			queryString.append( " order by ma.targetRackPosition ${order} ")
    		}
       		break;
    	case "usize":
    		queryString.append( " order by ma.model.usize ${order} ")
       		break;
    	case "teamHeader":
    		if ( rackPlan == "UnrackPlan" ) {
    			queryString.append( " order by ma.${sourceTeamType.get(params.role)} ${order} ")
    		} else {
    			queryString.append( " order by ma.${targetTeamType.get(params.role)} ${order} ")
    		}
       		break;
    	case "cart":
    		queryString.append( " order by ma.cart ${order} ")
       		break;
    	case "shelf":
    		queryString.append( " order by ma.shelf ${order} ")
       		break;
       	}
       	moveBundleAssetList = AssetEntity.findAll(queryString.toString())
    	if( moveBundleAssetList != null ) {
    		for( int assetRow = 0; assetRow < moveBundleAssetList.size(); assetRow++) {
    			def displayTeam  
    			if( rackPlan == "RerackPlan" ) {
    				displayTeam = moveBundleAssetList[assetRow][targetTeamType.get(params.role)]?.teamCode
    			}else {
    				displayTeam = moveBundleAssetList[assetRow][sourceTeamType.get(params.role)]?.teamCode
    			}
    			def assetEntityInstance = AssetEntity.findById( moveBundleAssetList[assetRow]?.id )
    			moveBundleAsset << [id:assetEntityInstance?.id, assetName:assetEntityInstance?.assetName, model:assetEntityInstance?.model?.toString(), 
			    					sourceLocation:assetEntityInstance?.sourceLocation, sourceRack:assetEntityInstance?.sourceRack, 
			    					targetLocation:assetEntityInstance?.targetLocation, targetRack:assetEntityInstance?.targetRack, 
			    					sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, 
			    					uSize:assetEntityInstance?.model?.usize, team:displayTeam, cart:moveBundleAssetList[assetRow]?.cart, 
			    					shelf:moveBundleAssetList[assetRow]?.shelf, projectTeam:projectTeam, assetTag:assetEntityInstance?.assetTag]
    		}
    	}
        render moveBundleAsset as JSON
    }
}   
