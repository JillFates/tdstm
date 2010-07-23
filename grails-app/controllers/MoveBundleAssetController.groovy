import grails.converters.JSON
import org.jsecurity.SecurityUtils
import com.tdssrc.grails.GormUtil
import java.text.SimpleDateFormat
import java.text.DateFormat
class MoveBundleAssetController {
	def partyRelationshipService
	def assetEntityAttributeLoaderService
	def userPreferenceService
	def jdbcTemplate
	def supervisorConsoleService
	
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
    	def bundleId = params.bundleId
    	if(!bundleId){
        	userPreferenceService.loadPreferences("CURR_BUNDLE")
            bundleId = getSession().getAttribute("CURR_BUNDLE").CURR_BUNDLE
        }
    	def moveBundleInstance = MoveBundle.findById( bundleId )
    	def moveBundles = MoveBundle.findAll("from MoveBundle where project.id = $moveBundleInstance.project.id")
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
        def moveBundles = MoveBundle.findAll("from MoveBundle where project.id = $moveBundleInstanceRight.project.id")
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
    	if(!params.max) params.max = 10
    	
    	def bundleId = params.moveBundle
    	if( bundleId ){
    		userPreferenceService.setPreference( "CURR_BUNDLE", "${bundleId}" )
    	} else {
            userPreferenceService.loadPreferences("CURR_BUNDLE")
    		bundleId = getSession().getAttribute("CURR_BUNDLE").CURR_BUNDLE
    	}
    	def bundleInstance = MoveBundle.findById(bundleId)
    	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and "+
                    										"pt.teamCode != 'Cleaning' and pt.teamCode != 'Transport' " )
    	def teamAssetCounts = []
    	def cartAssetCounts
    	def assetEntitysRacks
    	def rackPlan
    	def assetEntityList = AssetEntity.findAllByMoveBundle( bundleInstance )
    	if( params.rackPlan == 'RerackPlan' ) {
    		rackPlan = "RerackPlan"
    		assetEntitysRacks = AssetEntity.findAll("from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.targetRack")
    		projectTeamInstanceList.each{projectTeam ->
    			def assetCount = AssetEntity.countByMoveBundleAndTargetTeam( bundleInstance, projectTeam )
    			teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		def unAssignCount = AssetEntity.countByMoveBundleAndTargetTeam( bundleInstance, null )
			teamAssetCounts << [ teamCode: "UnAssigned" , assetCount:unAssignCount ]
    		def cartList = AssetEntity.findAll("from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.cart")
			cartAssetCounts = assetEntityAttributeLoaderService.getCartAssetCounts(bundleId)
    		render(view:'bundleTeamAssignment',model:[assetEntityInstanceList: assetEntityList, moveBundleInstance:bundleInstance, 
    		                                          projectTeamInstance:projectTeamInstanceList, teamAssetCount:teamAssetCounts, 
    		                                          assetEntitysRacks:assetEntitysRacks, rack:rackPlan, cartAssetCountList:cartAssetCounts ])
    	}else 
    	{
    		rackPlan = "UnrackPlan"
    		assetEntitysRacks = AssetEntity.findAll("from AssetEntity ma  where ma.moveBundle = $bundleInstance.id group by ma.sourceRack") 
    		projectTeamInstanceList.each{projectTeam ->
				def assetCount = AssetEntity.countByMoveBundleAndSourceTeam( bundleInstance, projectTeam )
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		def unAssignCount = AssetEntity.countByMoveBundleAndSourceTeam( bundleInstance, null )
			teamAssetCounts << [ teamCode: "UnAssigned" , assetCount:unAssignCount ]
    		render(view:'bundleTeamAssignment',model:[assetEntityInstanceList: assetEntityList, moveBundleInstance:bundleInstance, 
    		                                          projectTeamInstance:projectTeamInstanceList, teamAssetCount:teamAssetCounts, 
    		                                          assetEntitysRacks:assetEntitysRacks, rack:rackPlan ])
    	}
    }
    
    /*
     * Assign Asset to Selected MoveBundleTeam
     */ 
    def assetTeamAssign = {
    	def team = params.teamId
    	def asset = params.asset
    	def rackPlan = params.rackPlan
    	def bundleId = params.bundleId
    	def teamAssetCounts = []
    	def bundleInstance = MoveBundle.findById(bundleId)
    	def assetAssigned = []
    	if( team != null && team != "" && team != "null" )
    	{
    		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
    		if( projectTeamInstance ) {
    			def assetEntityInstance = AssetEntity.findById(asset)
    			if( rackPlan == "UnrackPlan" ) {
    				assetEntityInstance.sourceTeam = projectTeamInstance
    			} else {
    				assetEntityInstance.targetTeam = projectTeamInstance
    			}
    			assetEntityInstance.save(flush:true)
    			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleInstance, rackPlan )
    		}
    	} else {
    		def assetEntityInstance = AssetEntity.findById(asset)
			if( rackPlan == "UnrackPlan" ) {
				assetEntityInstance.sourceTeam = null
			} else {
				assetEntityInstance.targetTeam = null
			}
    		assetEntityInstance.save(flush:true)
			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleInstance, rackPlan )
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
					assetEntityInstance.sourceTeam = null
				} else {
					assetEntityInstance.targetTeam = null
				}
				assetEntityInstance.save(flush:true)
				modifiedAssetList.add(assetEntityInstance)
				assetEntityList = modifiedAssetList
				assetList = assetEntityAttributeLoaderService.getAssetList( assetEntityList, rackPlan, bundleInstance)
				teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleInstance, rackPlan )
			}
    	} else {
    		def modifiedAssetList = []
    		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
    		if( projectTeamInstance ) {
    			for( int assetRow = 0; assetRow < assets.size(); assetRow ++ ) {
    				def assetEntityInstance = AssetEntity.findById(assets[assetRow] )
    				if( rackPlan == "UnrackPlan" || team == "UnAssign" ) {
    					assetEntityInstance.sourceTeam = projectTeamInstance
    				} else {
    					assetEntityInstance.targetTeam = projectTeamInstance
    				}
    				assetEntityInstance.save(flush:true)
    				modifiedAssetList.add(assetEntityInstance)
    			}
    		}
    		assetEntityList = modifiedAssetList
			assetList = assetEntityAttributeLoaderService.getAssetList( assetEntityList, rackPlan, bundleInstance)
			teamAssetCounts = assetEntityAttributeLoaderService.getTeamAssetCount( bundleInstance, rackPlan )
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
    	def bundleInstance = MoveBundle.findById(bundleId)
    	def assetEntity = []
    	def projectTeam = []
    	def assetEntityList
    	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and "+
                    										"pt.teamCode != 'Cleaning' and pt.teamCode != 'Transport' " )
       	projectTeamInstanceList.each{teams ->
            projectTeam << [ teamCode: teams.teamCode ]
        }
    	if( team == "" || team == null ) {
    		assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id ")
    	} else if ( rackPlan == "UnrackPlan" ) {
    		if( team == "unAssign" ) {
    			assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and ma.sourceTeam = null ")
        	} else {
        		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
        		assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and "+
                    									"ma.sourceTeam = $projectTeamInstance.id ")
        	}
    	} else {
    		if( team == "unAssign" ) {
    			assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and ma.targetTeam = null ")
        	} else {
        		def projectTeamInstance = ProjectTeam.find( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and  pt.teamCode = '${team}' " )
        		assetEntityList = AssetEntity.findAll( " from AssetEntity ma where ma.moveBundle = $bundleInstance.id and "+
        												"ma.targetTeam = $projectTeamInstance.id ")
        	}
    	}
    	for( int assetRow = 0; assetRow < assetEntityList.size(); assetRow++) {
    		def displayTeam  
    		if( rackPlan == "RerackPlan" ) {
    			displayTeam = assetEntityList[assetRow]?.targetTeam?.teamCode
    		} else {
    			displayTeam = assetEntityList[assetRow]?.sourceTeam?.teamCode
    		}
    		def assetEntityInstance = AssetEntity.findById( assetEntityList[assetRow]?.id )
    		assetEntity <<[id:assetEntityInstance?.id, assetName:assetEntityInstance?.assetName, model:assetEntityInstance?.model, 
    						sourceLocation:assetEntityInstance?.sourceLocation, sourceRack:assetEntityInstance?.sourceRack, 
    						targetLocation:assetEntityInstance?.targetLocation, targetRack:assetEntityInstance?.targetRack, 
    						sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, 
    						uSize:assetEntityInstance?.usize,team:displayTeam,, cart:assetEntityList[assetRow]?.cart, 
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
       	def bundleInstance = MoveBundle.findById(bundleId)
       	def moveBundleAsset = []
       	def moveBundleAssetList
       	def projectTeam = []
       	def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and "+
                    										"pt.teamCode != 'Cleaning' and pt.teamCode != 'Transport' " )
       	projectTeamInstanceList.each{team ->
            projectTeam << [ teamCode: team.teamCode ]
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
    				displayTeam = moveBundleAssetList[assetRow]?.targetTeam?.teamCode
    			}else {
    				displayTeam = moveBundleAssetList[assetRow]?.sourceTeam?.teamCode
    			}
    			def assetEntityInstance = AssetEntity.findById( moveBundleAssetList[assetRow]?.id )
    			moveBundleAsset << [id:assetEntityInstance?.id, assetName:assetEntityInstance?.assetName, model:assetEntityInstance?.model, 
			    					sourceLocation:assetEntityInstance?.sourceLocation, sourceRack:assetEntityInstance?.sourceRack, 
			    					targetLocation:assetEntityInstance?.targetLocation, targetRack:assetEntityInstance?.targetRack, 
			    					sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, 
			    					uSize:assetEntityInstance?.usize, team:displayTeam, cart:moveBundleAssetList[assetRow]?.cart, 
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
    //Generate Report Dialog
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
            default: 
				            	render( view:'teamWorkSheets',
				            			model:[moveBundleInstanceList: moveBundleInstanceList, projectInstance:projectInstance])
						        break;
		}
    }
    //get teams for selected bundles.
    def getTeamsForBundles = {
    	def bundleId = params.bundleId
    	def projectId = params.projectId
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
    //Generate a TeamSheet jasper Report  
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
                    											"asset.sourceTeam = $projectTeamInstance.id order By asset.sourceTeam,"+
                    											"asset.moveBundle,asset.assetName,asset.assetTag")
    				}else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id "+
                    												"and asset.targetTeam = $projectTeamInstance.id order By asset.targetTeam,"+
                    												"asset.moveBundle,asset.assetName,asset.assetTag")
    				}else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    											"asset.sourceTeam = $projectTeamInstance.id order By asset.sourceTeam,"+
                    											"asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    												"asset.targetTeam = $projectTeamInstance.id order By asset.targetTeam,"+
                    												"asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}else {
    				//source Location selected
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    											"asset.sourceTeam != null order By asset.sourceTeam,asset.moveBundle,"+
                    											"asset.assetName,asset.assetTag")
    				}
    				//target Location selected
    				else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    												"asset.targetTeam != null order By asset.targetTeam,asset.moveBundle,"+
                    												"asset.assetName,asset.assetTag")
    				}
    				//Location Both selected
    				else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    										"asset.sourceTeam != null order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.moveBundle = $moveBundleInstance.id and "+
                    										"asset.targetTeam != null order By asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}
    		}
    		//All Bundles Selected
    		else {
    			//team Selected
    			if( projectTeamInstance  ) {
    				teamName = projectTeamInstance?.teamCode
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.sourceTeam = $projectTeamInstance.id and "+
                    											"asset.project.id = $projectInstance.id and asset.moveBundle != null order By "+
                    											"asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.targetTeam = $projectTeamInstance.id and "+
                    												"asset.project.id = $projectInstance.id and asset.moveBundle != null order By "+
                    												"asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.sourceTeam = $projectTeamInstance.id  and "+
                    											"asset.project.id = $projectInstance.id and asset.moveBundle != null order By "+
                    											"asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.targetTeam = $projectTeamInstance.id and "+
                    												"asset.project.id = $projectInstance.id and asset.moveBundle != null order By "+
                    												"asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}
    			}
    			//Team MoveBundle Both not selected (moveBundle="AllBundles)
    			else {
    				if(location == "source"){
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id and "+
                    											"asset.moveBundle != null and asset.sourceTeam != null order By asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else if(location == "target"){
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset where asset.project.id = $projectInstance.id "+
                    												"and asset.moveBundle != null and asset.targetTeam != null order By "+
                    												"asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    				}else {
    					assetEntityList = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id and "+
                    											"asset.moveBundle != null and asset.sourceTeam != null order By "+
                    											"asset.sourceTeam,asset.moveBundle,asset.assetName,asset.assetTag")
    					targetAssetEntitylist = AssetEntity.findAll("from AssetEntity asset  where asset.project.id = $projectInstance.id "+
                    										"and asset.moveBundle != null and asset.targetTeam != null order By "+
                    										"asset.targetTeam,asset.moveBundle,asset.assetName,asset.assetTag")
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
    			if( asset.sourceTeam != null ) {
    				teamPartyGroup = PartyGroup.findById(asset.sourceTeam.id)
    				projectTeamLocationInstance = ProjectTeam.findById(asset.sourceTeam.id)
    			}
    			def assetCommentList = AssetComment.findAllByAssetEntity(asset)
    			def assetCommentString =""
    			assetCommentList.each { assetComment ->
    				assetCommentString = assetCommentString + assetComment.comment +"\n"
   			 	}
    			def teamMembers = partyRelationshipService.getTeamMemberNames(asset.sourceTeam?.id)
    			def rackPos = (asset.sourceRack ? asset.sourceRack : "")+"/"+ (asset.sourceRackPosition ? asset.sourceRackPosition : "")
   				if (rackPos == "/"){
   					rackPos = ""
   				}
   			 	reportFields <<['assetName':asset.assetName , 'assetTag':asset.assetTag, "assetType":asset.assetType, 
   			 	                "manufacturer":asset.manufacturer, "model":asset.model, "sourceTargetrack":rackPos, 
   			 	                "position":asset.sourceRackPosition, 
   			 	                "sourceTargetPos":(projectTeamLocationInstance?.currentLocation ? projectTeamLocationInstance?.currentLocation : "") +"(source/ unracking)", 
   			 	                "usize":asset.usize, "cart":asset.cart, "shelf":asset.shelf,"source_team_id":asset?.sourceTeam?.id, 
   			 	                "move_bundle_id":asset?.moveBundle?.id, "clientName":projectInstance?.client?.name,
   			 	                'projectName':partyGroupInstance?.name,'startAt':GormUtil.convertInToUserTZ( projectInstance?.startDate, tzId ), 
   			 	                'completedAt':GormUtil.convertInToUserTZ( projectInstance?.completionDate, tzId ), 'bundleName':bundleInstance?.name,
   			 	                'teamName':teamPartyGroup?.name +"-"+teamMembers, 'teamMembers':teamMembers,
   			 	                'location':"Source Team", 'rack':"SourceRack",'rackPos':"SourceRackPosition",'truck':asset.truck, 
   			 	                'room':asset.sourceRoom, 'PDU':asset.pduPort,'NIC':asset.nicPort,
   			 	                'kvmPort':asset.kvmDevice ? asset.kvmDevice : '' + asset.kvmPort ? asset.kvmPort :'', 
   			 	                'hbaPort':asset.fiberCabinet + asset.hbaPort, 'instructions':assetCommentString, 'sourcetargetLoc':"s",
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
   				if(asset.targetTeam != null ) {
   					teamPartyGroup = PartyGroup.findById(asset.targetTeam.id)
   					projectTeamLocationInstance = ProjectTeam.findById(asset.targetTeam.id)
   					teamMembers = partyRelationshipService.getTeamMemberNames(asset.targetTeam.id) 
   				}
   				def assetCommentList = AssetComment.findAllByAssetEntity(asset)
   				def assetCommentString =""
   				assetCommentList.each { assetComment ->
   					assetCommentString = assetCommentString + assetComment?.comment +"\n"
   				}
   				def kvmPort = (asset.kvmDevice ? asset.kvmDevice : '')+" "+ (asset.kvmPort ? asset.kvmPort : '')
   				def hbaPort = (asset.fiberCabinet ? asset.fiberCabinet : '')+" "+ (asset.hbaPort ? asset.hbaPort : '')
   				def rackPos = (asset.targetRack ? asset.targetRack : "")+"/"+ (asset.targetRackPosition ? asset.targetRackPosition : "")
   				if (rackPos == "/"){
   					rackPos = ""
   				}
   				def cartShelf = (asset.cart ? asset.cart : "")+"/"+ (asset.shelf ? asset.shelf : "")
   				if (cartShelf == "/"){
   					cartShelf = ""
   				}
   				reportFields <<['assetName':asset.assetName , 'assetTag':asset.assetTag, "assetType":asset.assetType, 
   				                "manufacturer":asset.manufacturer, "model":asset.model, "sourceTargetrack":rackPos, 
   				                "position":asset.targetRackPosition, 
   				                "sourceTargetPos":(projectTeamLocationInstance?.currentLocation ? projectTeamLocationInstance?.currentLocation : "") +"(target/ reracking)", 
   				                "usize":asset.usize, "cart":asset.cart, "shelf":asset.shelf,"source_team_id":asset?.targetTeam?.id, 
   				                "move_bundle_id":asset?.moveBundle?.id, "clientName":projectInstance?.client?.name,
   				                'projectName':partyGroupInstance?.name,'startAt':GormUtil.convertInToUserTZ( projectInstance?.startDate, tzId ), 
   				                'completedAt':GormUtil.convertInToUserTZ( projectInstance?.completionDate, tzId ), 'bundleName':bundleInstance?.name, 
   				                'teamName':teamPartyGroup?.name +"-"+teamMembers, 'teamMembers':teamMembers,
   				                'location':"Target Team", 'rack':"TargetRack",'rackPos':"TargetRackPosition",
   				                'truck':(asset.truck ? asset.truck : "")+"\n"+cartShelf,'room':asset.targetRoom,'PDU':asset.pduPort,'NIC':asset.nicPort, 
   				                'kvmPort':kvmPort, 'hbaPort':hbaPort,'instructions':assetCommentString,'sourcetargetLoc':"t",
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
    //Get the List of Racks corresponding to Selected Bundle
    def getRacksForBundles = {
    	def bundleId = params.bundleId
        def projectId = params.projectId
        def projectInstance = Project.findById( projectId )
        def movebundleInstance = MoveBundle.findById(bundleId)
       	def assetEntityList = AssetEntity.findAllByMoveBundle(movebundleInstance)
       	def racks = []
    	assetEntityList.each {
           	racks <<[id:it.sourceRack, name:it.sourceRack]    		
    	}
    	render racks as JSON
    }
	//cart Asset report
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
                        teamPartyGroup =  ProjectTeam.findBymoveBundleAndTeamCode(bundleInstance, 'Cleaning')
                    }else {
       					teamPartyGroup =  ProjectTeam.findBymoveBundleAndTeamCode(bundleInstance, 'Transport')
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
       				def teamTagSort = (asset.sourceTeam ? asset.sourceTeam?.name : "") +" "+ (asset.assetTag ? asset.assetTag : "")
       				
       				def roomTagSort = (asset.sourceRoom ? asset.sourceRoom : "") +" "+ (asset.sourceRack ? asset.sourceRack : "") +" "+ (asset.usize ? asset.usize : "")
       				
       				def truckTagSort = (asset.truck ? asset.truck : "") +" "+ (asset.cart ? asset.cart : "") +" "+ (asset.shelf ? asset.shelf : "")
       				
       				def teamMembers = partyRelationshipService.getTeamMemberNames(teamPartyGroup?.id) 
       				reportFields <<['assetName':asset.assetName , "model":asset.model, 
       				                "sourceTargetPos":(teamPartyGroup?.currentLocation ? teamPartyGroup?.currentLocation : "") +"(source/ unracking)", 
       				                "cart":cartShelf, "shelf":asset.shelf,"source_team_id":teamPartyGroup?.id, 
       				                "move_bundle_id":asset?.moveBundle?.id,dlocation:asset.sourceLocation,
       				                'projectName':partyGroupInstance?.name,'startAt':GormUtil.convertInToUserTZ( projectInstance?.startDate, tzId ), 
       				                'completedAt':GormUtil.convertInToUserTZ( projectInstance?.completionDate, tzId ), 'bundleName':bundleInstance?.name, 
       				                'teamName':teamPartyGroup?.teamCode ? teamPartyGroup?.name+" - "+teamMembers : "", 
       				                'teamMembers':teamMembers,'location':"Source Team", 'truck':asset.truck, 
       				                'room':asset.sourceRoom,'moveTeam':asset?.sourceTeam?.name, 'instructions':assetCommentString,
       				                'teamTagSort':teamTagSort, 'roomTagSort':roomTagSort,'truckTagSort':truckTagSort,
       				                'assetTagSort': (asset.assetTag ? asset.assetTag : ""),'sourcetargetLoc':"s", 'usize':asset.usize,
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
        		
				def name = reportName == "cartAsset" ? "CleanTeam" : "TransportTeam"
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
     		def isAdmin = SecurityUtils.getSubject().hasRole("PROJ_MGR")
     		if( !isAdmin ) {
     			isAdmin = SecurityUtils.getSubject().hasRole("PROJECT_ADMIN")
     		}
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
    	            	def rackSize = assetEntity.usize != 0 ? assetEntity.usize : 1
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
			            			assetEntity.usize = 1
			            			overlapError = true
			            		}
		            			assetDetail << [assetEntity:assetEntity, assetTag:assetEntity.assetTag, position:position, overlapError:overlapError, 
		            			              rowspan:assetEntity.usize, currentHigh : position, currentLow : newLow]
		            		}
		            	}else{
		            		if(position > maxUSize) {
		            			position = maxUSize
		            			newLow = maxUSize
		            			assetEntity.usize = 1
		            			overlapError = true
		            		}
		            		assetDetail << [assetEntity:assetEntity, assetTag:assetEntity.assetTag, position:position, overlapError:overlapError, 
		            		              rowspan:assetEntity.usize, currentHigh : position, currentLow : newLow ]
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
    	            	backViewRows = getRackLayout( isAdmin, assetDetails, includeBundleName, backView )
    	            }
    	            if(frontView){
    	            	frontViewRows = getRackLayout( isAdmin, assetDetails, includeBundleName, null )
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
    	if( params.reportSort == "sourceLocation" ) {
    		sortBy = params.reportSort+",source_room,source_rack,source_rack_position"
    	}else if( params.reportSort == "targetLocation" ){
    		sortBy = params.reportSort+",target_room,target_rack,target_rack_position"
    	}
    	if( params.reportResolveInfo == "false" ){
    		resolvedInfoInclude = "Resolved issues were not included"
    	}
    	if(params.moveBundle == "null") {    		
    		flash.message = " Please Select Bundles. "
    		redirect( action:'getBundleListForReportDialog', params:[reportId: 'Issue Report'] )
        }
    	else {
    		def moveBundleInstance = MoveBundle.findById(params.moveBundle)
    		def bundleName = "All Bundles"
    		def assetCommentList
    		def targetAssetEntitylist
    		if( moveBundleInstance != null ){
    			assetCommentList = AssetComment.findAll("from AssetComment ac where ac.assetEntity.id in(select ae.id from AssetEntity ae "+
                    									"where ae.moveBundle.id = $moveBundleInstance.id ) and ac.commentType= 'issue' "+
                    									"order by ac.assetEntity.${sortBy}")
    			bundleNames = moveBundleInstance?.name
    			bundleName = bundleNames
    		}else {
    			assetCommentList = AssetComment.findAll("from AssetComment ac where ac.assetEntity.id in(select ae.id from AssetEntity ae "+
                    									"where ae.project.id = $projectInstance.id ) and ac.commentType= 'issue' "+
                    									"order by ac.assetEntity.${sortBy}")
    			bundleNames = "All"
        	}
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
    				reportFields <<['assetName':assetComment?.assetEntity?.assetName, 'assetTag':assetComment?.assetEntity?.assetTag, 
    								'sourceTargetRoom':sourceTargetRoom,
    								'model':(assetComment?.assetEntity?.manufacturer ? assetComment?.assetEntity?.manufacturer : "")+" "+(assetComment?.assetEntity?.model ? assetComment?.assetEntity?.model : "" ), 
    								'occuredAt':GormUtil.convertInToUserTZ( assetComment?.dateCreated, tzId ), 'createdBy':assetComment?.createdBy?.firstName+" "+assetComment?.createdBy?.lastName, 
    								'issue':assetComment?.comment, 'bundleNames':bundleNames,'projectName':partyGroupInstance?.name, 
    								'clientName':projectInstance?.client?.name,"resolvedInfoInclude":resolvedInfoInclude,
    								'timezone':tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate ) )]
    			}
    			if( params.reportResolveInfo == "true" && assetComment.isResolved == 1 ) {
    				reportFields <<['assetName':null, 'assetTag':null, 'sourceTargetRoom':null,'model':null, 
									'occuredAt':GormUtil.convertInToUserTZ( assetComment?.dateResolved, tzId ), 
    								'createdBy':assetComment?.resolvedBy?.firstName+" "+assetComment?.resolvedBy?.lastName, 
    								'issue':assetComment?.resolution, 'bundleNames':bundleNames,'projectName':partyGroupInstance?.name, 
    								'clientName':projectInstance?.client?.name,
    								'timezone':tzId ? tzId : "EDT", "rptTime":String.valueOf(formatter.format( currDate ) )]
    			}
    		}
    		if(reportFields.size() <= 0) {    		
        		flash.message = " No Issues Were found for  selected values  "
        		redirect( action:'getBundleListForReportDialog', params:[reportId: 'Issue Report'] )
        	}else {
        		def filename = 	"IssueReport-${projectInstance.name}-${bundleName}"
					filename = filename.replace(" ", "_")
        		chain(controller:'jasper',action:'index',model:[data:reportFields],
						params:["_format":"PDF","_name":"${filename}","_file":"${params._file}"])
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
    				if(member.partyIdFrom.teamCode == "Cleaning") {
    					teamCode = "ct"
    				}
    				if ( params.location == "source" || params.location == "both" ) {
    					reportFields <<[ 'name': member.partyIdTo.firstName +" "+ member.partyIdTo.lastName,
                                         'teamName': member.partyIdFrom.name+" - Source","sortField":member.partyIdFrom.moveBundle.name+member.partyIdTo.firstName+member.partyIdTo.lastName,
                                         'bundleName': client+" - "+member.partyIdFrom.moveBundle.name+" "+(member.partyIdFrom.moveBundle.startTime ? partyRelationshipService.convertDate(member.partyIdFrom.moveBundle.startTime) : " "),
                                         'barCode': teamCode+'-'+member.partyIdFrom.moveBundle.id+'-'+member.partyIdFrom.id+'-s' 
                                         ]
    				}
    				if ( member.partyIdFrom.teamCode != "Cleaning" && (params.location == "target" || params.location == "both") ) {
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
	/*----------------------------------------
	 * @author : Lokanath Reddy
	 * @param  : asset details
	 * @return : rack rows
	 *---------------------------------------*/
	def getRackLayout( def isAdmin, def asset, def includeBundleName, def backView){
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
    		 			def assetInstance = AssetEntity.findByAssetTag( tag )
    		 			moveBundle += (assetInstance?.moveBundle ? assetInstance?.moveBundle.name : "") + "<br/>"
    		 			assetTag += "<a href='javascript:openAssetEditDialig(${assetInstance?.id})' >$it</a> <br/>"
    		 		}
    		 		if( !isAdmin ){
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
    		 				def cablingString = "${it.asset?.assetEntity?.pduPort ? 'PDU: '+ it.asset?.assetEntity?.pduPort +' | ' : '' }"+ 
    		 									"${it.asset?.assetEntity?.nicPort ? 'NIC: '+ it.asset?.assetEntity?.nicPort +' | ' : ''}"+
    		 									"${it.asset?.assetEntity?.kvmDevice && it.asset?.assetEntity?.kvmDevice != 'blank / blank'? 'KVM: '+ it.asset?.assetEntity?.kvmDevice +' | ' : ''}"+
    		 									"${it.asset?.assetEntity?.remoteMgmtPort ? 'RMgmt: '+ it.asset?.assetEntity?.remoteMgmtPort +' | ': ''}"+
												"${it.asset?.assetEntity?.fiberCabinet && it.asset?.assetEntity?.fiberCabinet != 'blank / blank / blank' ? 'Fiber: '+ it.asset?.assetEntity?.fiberCabinet +' | ' : ''}"
							
							if ( cablingString ) {
								cablingString = cablingString.substring( 0, cablingString.length() - 2 )
							}
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
			def assetsQuery = new StringBuffer(" SELECT ae.asset_entity_id as id, ae.asset_name as assetName, ae.asset_tag as assetTag,"+
								" ae.source_rack as rack, ae.source_rack_position as uposition"+
								" FROM asset_entity ae WHERE ae.project_id = ${projectId} ")
			if(moveBundleId != "all"){
				assetsQuery.append(" AND ae.move_bundle_id = ${moveBundleId} ")
			}
			assetsQuery.append("ORDER BY ae.source_rack, ae.source_rack_position DESC")
	    	def assetEntityList = jdbcTemplate.queryForList( assetsQuery.toString() )
			if( !assetEntityList ){
				reportFields <<[ 'flashMessage': "Team Members not Found for selected Teams"]
			} else {
				reportFields << assetEntityList
			}
			render reportFields  as JSON
	    }
	}
}   
