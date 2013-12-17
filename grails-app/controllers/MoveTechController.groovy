/*
 * MoveTech Login 
 */
/*import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.UsernamePasswordToken
import org.apache.shiro.SecurityUtils
import org.codehaus.groovy.grails.commons.ApplicationHolder*/
import com.tds.asset.AssetCableMap
import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdssrc.grails.GormUtil
class MoveTechController {
    def shiroSecurityManager
    def userPreferenceService
    def partyRelationshipService
    def stateEngineService
    def workflowService
    def jdbcTemplate
	def clientTeamsService
	
    def static final statusDetails = ["missing":"Unknown", "cabledDetails":"Cabled with Details","empty":"Empty","cabled":"Cabled"]
    /*------------------------------------------------------------
     * Index action to enter to home if the session is not expired
     * @param  : String user, String team, String location
     * @return : Redirect to home page with Array of arguments
     *------------------------------------------------------------*/
    def index = {
    	def browserTest = false
    	if ( !request.getHeader ( "User-Agent" ).contains ( "MSIE" ) ) {
    		browserTest = true
    	}	
    	if ( params.fMess ) {
    		flash.clear()
    	}
    	def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
    	// Checking user existence
    	if( principal ){
	        if( params.user == "mt" ) {
	        	def projectTeamInstance = ProjectTeam.findById( params.team )
	            def team = projectTeamInstance.name
	            def teamMembers = partyRelationshipService.getTeamMemberNames( params.team )
	            def bundleInstance = MoveBundle.findById(params.bundle)
	            def location = ""
	            if ( params.location == 's' ) {
	            	location = "Unracking"
	                projectTeamInstance.currentLocation = "Source"
	                projectTeamInstance.save()
	            } else if ( params.location == 't' ) {
	                location = "Reracking"
	                projectTeamInstance.currentLocation = "Target"
	                projectTeamInstance.save()
	            }
						
	            render ( view:'home',
                    model:[ projectTeam:team, members:teamMembers, project:params.project,
                            loc:location, bundle:params.bundle,bundleName:bundleInstance.name,
                            team:params.team, location:params.location
                            ])
	        } else if( params.user == "st" ) {
	        	def projectTeamInstance = ProjectTeam.findById( params.team )
	            def team = projectTeamInstance.name
	            def teamMembers = partyRelationshipService.getTeamMemberNames( params.team )
	            def bundleInstance = MoveBundle.findById(params.bundle)
	            def location = ""
	            if ( params.location == 's' ) {
	            	location = "Unracking"
	                projectTeamInstance.currentLocation = "Source"
	                projectTeamInstance.save()
	            } else if ( params.location == 't' ) {
	                location = "Reracking"
	                projectTeamInstance.currentLocation = "Target"
	                projectTeamInstance.save()
	            }
						
	            render ( view:'clientHome',
                    model:[ projectTeam:team, members:teamMembers, project:params.project,
                            loc:location, bundle:params.bundle,bundleName:bundleInstance.name,
                            team:params.team, location:params.location
                            ])
	        } else if ( params.user == "ct" ) {
	        	def projectTeamInstance = ProjectTeam.findById( params.team )
	            def team = projectTeamInstance.name
	            def teamMembers = partyRelationshipService.getTeamMemberNames( params.team )
	            def bundleInstance = MoveBundle.findById(params.bundle)
	            def teamLocation = ""
	            if ( params.location == 's' ) {
	            	projectTeamInstance.currentLocation = "Source"
	            	projectTeamInstance.save()
	            	teamLocation = projectTeamInstance.currentLocation
	            } else if ( params.location == 't' ) {
	            	projectTeamInstance.currentLocation = "Target"
	            	projectTeamInstance.save()
	            	teamLocation = projectTeamInstance.currentLocation
	            }
	        	//userPreferenceService.setPreference("CURR_BUNDLE","${bundleInstance?.id}")
	            render ( view:'logisticsHome', model:[ projectTeam:team, members:teamMembers, project:params.project,
	                                                      loc:teamLocation, bundle:params.bundle,bundleName:bundleInstance.name, team:params.team,
	                                                      location:params.location, browserTest:browserTest 
	                                                      ])
					
	        } else {
	        	redirect( action:'login' )
	        }
        } else {
        	// Redirect to Login page when session expired
        	flash.message = "Your login has expired and must login again."
        	redirect( action:'login' )
        }
    }
    
    //moveTech login
    def moveTechLogin = {    	
        redirect( action:'login' )
    }
    
    /*----------------------------------------------------------------------------------------
     * Login to the user if session is not expired for that user
     * @param  : String username
     * @return : Redirect to home page if session is not expired else redirect to signIn method 
     *----------------------------------------------------------------------------------------*/
    def login = {
    	def validate = true
		def message = flash.message
		if( message ) {
			if( message.contains( "Unknown" ) || message.contains( "Invalid" ) 
					|| message.contains( "No assets assigned" ) || message.contains( "presently inactive" ) ) {
				validate = false
			}
		}
    	if( validate ) {
    		def username = session.getAttribute ( "USERNAME" ) 
			def newUser = params.username
			if ( username == newUser ) {
				redirect ( action:'signIn', params:["username":username] )
			} else {
				session.setAttribute( "USERNAME","")
				return [ username: params.username, rememberMe: (params.rememberMe != null), 
						 targetUri: params.targetUri
		                ]	
			}
    	} else {
    		session.setAttribute( "USERNAME","")
			return [ username: params.username, rememberMe: (params.rememberMe != null), 
					 targetUri: params.targetUri
					 ]
    	}
     }
    
    /*------------------------------------------------------------------
     * Sign in for moveTech by reading the barcode as userName
     * @param  : String username
     * @return : Redirect to checkAuth function if user login successful
     *-----------------------------------------------------------------*/
    def signIn = {
        def moveBundleInstance
        def projectTeamInstance
        if( params.username ) {
        	session.setAttribute( "USERNAME",params.username )
            //Getting current project instance
            def projectInstance
            def barcodeText  = params.username.tokenize("-")
            //checking for valid barcode format or not size is 4 (mt-moveid- teamid- s/t)
            if ( barcodeText.size() == 4 ) {
            	try{
            		def nowDate = GormUtil.convertInToGMT( "now", "EDT" )
	                if ( barcodeText.get(0) == "mt" ) {
	                	moveBundleInstance = MoveBundle.findById ( barcodeText.get(1) )
	                    //checkin for movebundle and team instances
	                    if ( moveBundleInstance ) {
	                    	projectInstance = Project.findById ( moveBundleInstance.project.id )
	                    	if ( projectInstance ) {
	                    		projectTeamInstance = ProjectTeam.findById( barcodeText.get(2) )
	                    		if ( projectTeamInstance ){
	                                //Validating is Logindate between startdate and completedate
									if ( nowDate < moveBundleInstance.startTime || nowDate > moveBundleInstance.completionTime ) {
	                                    flash.message = message( code :"Bundle presently inactive" )
	                                    redirect( action: 'login' )
	                                    return;
	                                } else if ( nowDate < projectInstance.startDate || nowDate > projectInstance.completionDate ) {
	                                    flash.message = message( code :"Bundle Project presently inactive" )
	                                    redirect( action: 'login' )
	                                    return;
	                                } else {
	                                    def assetEntityInstance
	                                    def query = new StringBuffer("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id") 
	                                    if ( barcodeText.get(3) == 's' ) {
	                                    	query.append(" and ae.sourceTeamMt = $projectTeamInstance.id")
	                                    	assetEntityInstance = AssetEntity.find( query.toString() )
	                                    } else if ( barcodeText.get(3) == 't' ) {
	                                    	query.append(" and ae.targetTeamMt = $projectTeamInstance.id" )
	                                        assetEntityInstance = AssetEntity.find( query.toString() )
	                                    }
	                                    //checking for Assets corresponding to moveBundle exist or not
	                                    if( assetEntityInstance != null ) {
	                                        def moveTech = [ user: barcodeText.get(0) ]
	                                        moveTech['bundle'] = moveBundleInstance.id
	                                        moveTech['team'] = Integer.parseInt( barcodeText.get(2) )
	                                        moveTech['location'] = barcodeText.get(3)
	                                        moveTech['project'] = projectInstance.name
	                                        checkAuth( barcodeText.get(0), moveTech )
	                                    } else {
	                                        flash.message = message ( code : "No assets assigned to team for bundle" )
	                                        redirect( action: 'login' )
	                                        return;
	                                    }
	                                }
	                            } else {
	                            	flash.message = message ( code : "Unknown bundle team" )
	                                redirect( action: 'login' )
	                                return;
	                            }
	                    	} else {
	                    		flash.message = message ( code : "Unknown project" )
	                            redirect( action: 'login' )
	                            return;
	                    	}
	                    } else {
	                    	flash.message = message ( code : "Unknown bundle" )
	                        redirect ( action: 'login' )
	                        return;
	                    }
	                } else if ( barcodeText.get(0) == "ct" ) {
	                    moveBundleInstance = MoveBundle.findById ( barcodeText.get(1) )
	                    if ( moveBundleInstance ) {
	                    	projectInstance = Project.findById ( moveBundleInstance.project.id )
	                    	if ( projectInstance ) {
	                    		projectTeamInstance = ProjectTeam.findById ( barcodeText.get(2) )
	                    		if ( projectTeamInstance != null && projectTeamInstance.teamCode == "Logistics" ) {
	                                //Validating is Logindate between startdate and completedate
	                                if ( nowDate < moveBundleInstance.startTime || nowDate > moveBundleInstance.completionTime ) {
	                                    flash.message = message( code :"bundle presently inactive" )
	                                    redirect( action: 'login' )
	                                    return;
	                                } else if ( nowDate < projectInstance.startDate || nowDate > projectInstance.completionDate ) {
	                                    flash.message = message ( code : "bundle Project presently inactive" )
	                                    redirect ( action: 'login' )
	                                    return;
	                                } else {
	                                    def assetEntityInstance
	                                    if ( barcodeText.get(3) == 's' ) {
	                                        assetEntityInstance = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id" )
	                                    } else if ( barcodeText.get(3) == 't' ) {
	                                        assetEntityInstance = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = $moveBundleInstance.id" )
	                                    }
	                                    //checking for Assets corresponding to moveBundle exist or not
	                                    if ( assetEntityInstance != null ) {
	                                        def moveTech = [ user: barcodeText.get(0) ]
	                                        moveTech[ 'bundle' ] = moveBundleInstance.id
	                                        moveTech[ 'team' ] = Integer.parseInt( barcodeText.get(2) )
	                                        moveTech[ 'location' ] = barcodeText.get(3)
	                                        moveTech[ 'project' ] = projectInstance.name
	                                        checkAuth( barcodeText.get(0), moveTech )
	                                    } else {
	                                        flash.message = message ( code : "No assets assigned to team for bundle" )
	                                        redirect ( action: 'login' )
	                                        return;
	                                    }
	                                }
	                            } else {
	                            	flash.message = message ( code : "Unknown Logistics team" )
	                                redirect ( action: 'login' )
	                                return;
	                            }
	                    	} else {
	                    		flash.message = message ( code : "Unknown project" )
	                            redirect ( action: 'login' )
	                            return;
	                    	}
	                    } else {
	                    	flash.message = message ( code : "Unknown bundle" )
	                        redirect ( action: 'login' )
	                        return;
	                    }
	                } else {
	                    flash.message = message ( code : "Invalid username" )
	                    redirect( action: 'login' )
	                    return;
	                }
            	} catch ( Exception e ) {
                    flash.message = message ( code : "Invalid Login" )
                    redirect ( action : 'login' )
                    return;
				}
            } else {
                flash.message = message ( code : "Invalid username format" )
                redirect ( action : 'login' )
                return;
            }
        } else {
        	flash.message = message ( code : "Invalid username format" )
            redirect ( action: 'login' )
            return;
        }
    }
		
    /*-------------------------------------------------------------------------------------------------
     * check authentication for a user
     * @param  : String barcodeText, String actionScreen 
     * @return : Redirect to Relevent Home pages if user is an authenticated user else redirect to login
     *--------------------------------------------------------------------------------------------------*/
    def checkAuth ( def barcodeText, def actionScreen ) {
    	 
    	 def workStationUser = UserLogin.findByUsername(barcodeText)
		 if( workStationUser ){
			 session.setAttribute( "PRINCIPAL", barcodeText)
			 if( barcodeText == "ct" ) {
	            redirect ( controller:'moveTech', params:actionScreen )
	            return;
            } else {
            	redirect( controller:'moveTech', action:'moveTechSuccessLogin', params:actionScreen )
            	return;
            }
		 } else {
			 	//	Authentication failed, so display the appropriate message
	            // on the login page.
	            log.info "Authentication failure for user '${params.username}'."
	            flash.message = message ( code: "login.failed" )
	            // Keep the username and "remember me" setting so that the
	            // user doesn't have to enter them again.
	            def m = [ username: params.username ]
	            if ( params.rememberMe ) {
	                m['rememberMe'] = true
	            }
	            // Remember the target URI too.
	            if ( params.targetUri ) {
	                m['targetUri'] = params.targetUri
	            }
	            // Now redirect back to the login page.
	            redirect ( action: 'moveTechLogin', params: m  )
	            return;
		 }
        /*def authToken = new UsernamePasswordToken( barcodeText, 'xyzzy' )
        try{
            // Perform the actual login. An AuthenticationException
            // will be thrown if the username is unrecognised or the
            // password is incorrect.
            this.shiroSecurityManager.login( authToken )
            // Check User and Person Activi status
            if( barcodeText == "ct" ) {
	            redirect ( controller:'moveTech', params:actionScreen )
	            return;
            } else {
            	redirect( controller:'moveTech', action:'moveTechSuccessLogin', params:actionScreen )
            	return;
            }
        }
        catch ( AuthenticationException ex ) {
            // Authentication failed, so display the appropriate message
            // on the login page.
            log.info "Authentication failure for user '${params.username}'."
            flash.message = message ( code: "login.failed" )
            // Keep the username and "remember me" setting so that the
            // user doesn't have to enter them again.
            def m = [ username: params.username ]
            if ( params.rememberMe ) {
                m['rememberMe'] = true
            }
            // Remember the target URI too.
            if ( params.targetUri ) {
                m['targetUri'] = params.targetUri
            }
            // Now redirect back to the login page.
            redirect ( action: 'moveTechLogin', params: m  )
            return;
        }*/
    }
    
    /*------------------------------------------------------------------------------
     * Log out action
     * @return Redirect to login page if user has logged out
     *------------------------------------------------------------------------------*/
    def signOut = {
        // Log the user out of the application.
        //SecurityUtils.subject.logout()
		session.setAttribute( "PRINCIPAL", null)
        // For now, redirect back to the login page.
        redirect ( action : 'moveTechLogin' )
    }
    
    /*------------------------------------------------------------------------------
     * Home Page action
     * @return Home page if the user session is not expired
     *------------------------------------------------------------------------------*/
    def home = {
    	def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
    	if( principal ){
    		render ( view:'home' )
    	} else {
    		flash.message = "Your login has expired and must login again."
    		redirect ( action : 'login' )
    	}
    }
    
    /*------------------------------------------------------------------------------
     * To view the list of assets for that particular bundle, team and location
     * @author Bhuvana
     * @param  String bundle, String team, String location
     * @return Array of arguments  
     *------------------------------------------------------------------------------*/
	def assetTask = {
		if ( params.fMess ) {
			flash.clear()
		}
		String message = flash.message
		def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
		if( principal ) {
            def bundleId = params.bundle
            def tab = params.tab
            def proAssetMap
            def team = params.team
            def stateVal
            def todoSize
            def allSize
            def assetList = []
            def colorCss
            def rdyState
            def ipState = new ArrayList()
            def moveBundleInstance = MoveBundle.findById( bundleId )
			flash.message = ""
            def holdState = stateEngineService.getStateIdAsInt( moveBundleInstance.workflowCode, "Hold" ) 
            if ( params.location == "s" ) {
                rdyState = stateEngineService.getStateIdAsInt( moveBundleInstance.workflowCode, "Release" )
                ipState.add( stateEngineService.getStateIdAsInt( moveBundleInstance.workflowCode, "Unracking" ) )
            } else {
                rdyState = stateEngineService.getStateIdAsInt( moveBundleInstance.workflowCode, "Staged" )
                ipState.add(  stateEngineService.getStateIdAsInt( moveBundleInstance.workflowCode, "Reracking" ) )
            }
            def countQuery = """select a.asset_entity_id as id, a.asset_tag as assetTag, a.source_rack as sourceRack, 
							a.source_rack_position as sourceRackPosition, a.target_rack as targetRack,
				            min(cast(t.state_to as UNSIGNED INTEGER)) as minstate,
				            a.target_rack_position as targetRackPosition, m.name as model, p.current_state_id as currentStateId 
				            from asset_entity a left join project_asset_map p on (a.asset_entity_id = p.asset_id) 
				            left join asset_transition t on(a.asset_entity_id = t.asset_entity_id and t.voided = 0)
            				left join model m on (a.model_id = m.model_id )
				            where a.move_bundle_id = $bundleId"""
            def query = new StringBuffer (countQuery)
            if ( params.location == "s" ) {
                stateVal = stateEngineService.getStateId ( moveBundleInstance.workflowCode, "Unracked" )
                query.append (" and a.source_team_id = $team" )
                countQuery +=" and a.source_team_id = $team"
            } else {
            	stateVal = stateEngineService.getStateId ( moveBundleInstance.workflowCode, "Reracked" )
                query.append (" and a.target_team_id = $team" )
                countQuery += " and a.target_team_id = $team" 
            }
            allSize = jdbcTemplate.queryForList ( query.toString() + " group by a.asset_entity_id ").size()
            if ( tab == "Todo" ) {
                query.append (" and ( p.current_state_id < $stateVal or t.state_to = $holdState )")
            }
            query.append(" group by a.asset_entity_id ")
            if( params.sort != null ){
            	if( params.sort == "source_rack" ) {
            		query.append(" order by min(cast(t.state_to as UNSIGNED INTEGER)) = $holdState desc ,"+
            					"p.current_state_id in ${ipState.toString().replace(']',')').replace('[','(')} desc, p.current_state_id = $rdyState desc, "+
            					"p.current_state_id < $rdyState desc , a.$params.sort $params.order, a.source_rack_position $params.order" )
            	}else {
            		query.append(" order by min(cast(t.state_to as UNSIGNED INTEGER)) = $holdState desc ,"+
            					"p.current_state_id in ${ipState.toString().replace(']',')').replace('[','(')} desc, p.current_state_id = $rdyState desc, "+
            					"p.current_state_id < $rdyState desc , a.$params.sort $params.order" )
            	}
            }else {
            	query.append(" order by min(cast(t.state_to as UNSIGNED INTEGER)) = $holdState desc ,"+
            				"p.current_state_id in ${ipState.toString().replace(']',')').replace('[','(')} desc, p.current_state_id = $rdyState desc, "+
            				"p.current_state_id < $rdyState desc , a.source_rack, a.source_rack_position" )
            }
            proAssetMap = jdbcTemplate.queryForList ( query.toString() )
            todoSize = proAssetMap.size()
			def sortOrder = 5
            proAssetMap.each {
                if ( it.currentStateId ) {
	                if ( it.minstate == holdState ) {
	                    colorCss = "asset_hold"
						sortOrder = 1
	                } else if ( it.currentStateId == rdyState ) {
	                    colorCss = "asset_ready"
						sortOrder = 3
	                } else if ( ipState.contains( it.currentStateId ) ) {
	                    colorCss = "asset_process"
						sortOrder = 2
	                } else if ( ( it.currentStateId > holdState ) && ( it.currentStateId < rdyState ) ) {
	                    colorCss = "asset_pending"
						sortOrder = 4
	                } else if ( ( it.currentStateId >= rdyState ) ) {
	                    colorCss = "asset_done"
						sortOrder = 5
	                }
                } else {
                	colorCss = "asset_pending"
					sortOrder = 4
                }
                assetList << [ item:it, cssVal:colorCss, sortOrder:sortOrder ]
            }
			assetList.sort {
               it.sortOrder
            }
            if ( tab == "All" ) {
            	countQuery += " and (p.current_state_id < $stateVal or t.state_to = $holdState) group by a.asset_entity_id" 
                todoSize = jdbcTemplate.queryForList ( countQuery ).size()
                
            }
            if(!flash.message){
            	flash.message = message
            }
            return[ bundle:bundleId, team:team, project:params.project, location:params.location, 
                    assetList:assetList, allSize:allSize, todoSize:todoSize, 'tab':tab
                    ]
		} else {
			flash.message = "Your login has expired and must login again."
			redirect ( action:'login' )
		}
	}

    /*------------------------------------------------------------------------------
     * To view the details and to change the state of an asset
     * @author Bhuvana
     * @param  String search, String team, String location
     * @return Array of arguments   
     *------------------------------------------------------------------------------*/
	def assetSearch = {
    	if(flash.message?.contains("was not located")){
    		flash.clear()
    	}
    	flash.message= ""
		def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
		if ( principal ) {
            def assetItem
            def assetComment
            def projMap
            def team = params.team
            def search = params.search
            def stateVal
            def taskList
            def taskSize
            def label
            def actionLabel
            def checkHome = params.home
            def moveBundleInstance = MoveBundle.findById( params.bundle )
            def loginTeam
            if ( team ) {
            	loginTeam = ProjectTeam.findById( params.team )
            }
            def commentsList = clientTeamsService.getCommentsFromRemainderList( session )
            if ( search != null ) {
            	def query = new StringBuffer ("from AssetEntity ae where ae.moveBundle=${moveBundleInstance.id} and ae.assetTag = :search ")
                assetItem = AssetEntity.find( query.toString(), [ search : search ] )
                if ( assetItem == null ) {
                    flash.message += message ( code : "<li>Asset Tag number '${search}' was not located</li>" )
                    if ( checkHome ) {
                        redirect ( action : 'index', 
                            params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                    "location":params.location, "user":"mt" 
                                    ])
                        return;
                    } else {
                        redirect ( action : 'assetTask', 
                            params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                    "location":params.location,"tab":params.tab 
                                    ])
                        return;
                    }
                } else {
                    def teamName
                    def teamId
                    /*def bundleName = assetItem.moveBundle?.id        
                    if ( bundleName != Integer.parseInt ( params.bundle ) ) {
                        flash.message = message( code : "The asset [${assetItem.assetName}] is not part of bundle [${params.bundle}] " )
                        if ( checkHome ) {
                            redirect ( action: 'index',
                                params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                        "location":params.location, "user":"mt" 
                                        ])
                            return;
                        } else {
                            redirect ( action: 'assetTask',
                                params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                        "location":params.location,"tab":params.tab 
                                        ])
                            return;
                        }
                    } else {*/
                        if ( params.location == "s" ) {
                            if ( assetItem.sourceTeamMt ) {
                                teamId = ( assetItem.sourceTeamMt.id ).toString()
                                teamName = assetItem.sourceTeamMt.name
                            } else {
                                flash.message += message ( code : "<li>The asset [${assetItem.assetName}] is not assigned to team [${loginTeam}] </li>" )
                                if ( checkHome ) {
                                    redirect ( action: 'index',
                                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                                "location":params.location, "user":"mt" 
                                                ])
                                    return;
                                } else {
                                    redirect ( action: 'assetTask',
                                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                                "location":params.location, "tab":params.tab 
                                                ])
                                    return;
                                }
                            }
                        } else {
                            if ( assetItem.targetTeamMt ) {
                                teamId = ( assetItem.targetTeamMt.id ).toString()
                                teamName = assetItem.targetTeamMt.name
                            } else {
                                flash.message += message( code : "<li>The asset [${assetItem.assetName}] is not assigned to team [${loginTeam}] </li>" )
                                if ( checkHome ) {
                                    redirect ( action: 'index',
                                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                                "location":params.location, "user":"mt" 
                                                ])
                                    return;
                                } else {
                                    redirect ( action: 'assetTask',
                                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                                "location":params.location, "tab":params.tab 
                                                ])
                                    return;
                                }
                            }
                        }
                        if ( teamId != params.team ) {
                            flash.message += message ( code : "<li>The asset [${assetItem.assetName}] is assigned to team [${teamName}] </li>" )
                            // commented as per JIRA:TM-199 
                            /* if ( checkHome ) {
                            redirect ( action: 'index',
                            params:["bundle":params.bundle, "team":params.team, "project":params.project,
                            "location":params.location,"user":"mt" ])
                            return;
                            } else {
                            redirect ( action: 'assetTask',
                            params:["bundle":params.bundle, "team":params.team, "project":params.project,
                            "location":params.location,"tab":params.tab ])
                            return;
                            }*/
                        }
                        def holdId = stateEngineService.getStateId( moveBundleInstance.workflowCode, "Hold" )
                        def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
                        												"where t.asset_entity_id = ${assetItem.id} and voided = 0 and ( t.type = 'process' or t.state_To = $holdId ) "+
                        												"order by date_created desc, stateTo desc limit 1 ")
                        projMap = ProjectAssetMap.findByAsset( assetItem )
                        if( !transitionStates.size() ) {
                        	flash.message += message ( code :"<li> No actions for this asset </li>" )
                        	if ( checkHome ) {
                        		redirect ( action: 'index',
                                    params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                            "location":params.location, "user":"mt" 
                                            ])
                                return;
                        	} else {
                        		redirect ( action: 'assetTask', 
                                    params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                            "location":params.location, "tab":params.tab 
                                            ])
                        		return;
                        	}
                        } else {
                        	stateVal = stateEngineService.getState( moveBundleInstance.workflowCode, transitionStates[0].stateTo )
                        	if ( stateVal == "Hold" ) {
                        		flash.message += message ( code : "<li>The asset is on Hold. Please contact manager to resolve issue.</li>" )
                        		if( checkHome ) {
                        			redirect ( action: 'index',
                                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                                "location":params.location, "user":"mt" 
                                                ])
                        			return;
                        		} else {
                        			redirect ( action: 'assetTask', 
                                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                                "location":params.location, "tab":params.tab 
                                                ])
                        			return;
                        		}
                        	}
                        	taskList = stateEngineService.getTasks ( moveBundleInstance.workflowCode, "MOVE_TECH", stateVal )
                        	taskSize = taskList.size()
                        	if ( taskSize == 1 ) {
                        		if ( taskList.contains ( "Hold" ) ) {
                        			flash.message += message ( code : "<li>NO ACTIONS FOR ASSET. You may place it on hold to alert the move coordinator </li>" )
                        		} else {
                        			actionLabel = taskList[0]
                    				label =	stateEngineService.getStateLabel ( moveBundleInstance.workflowCode, stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode,actionLabel) )
                        		}
                        	} else if ( taskSize > 1 ) {
                        		
                        		taskList.each {
                        			if ( it != "Hold" && !actionLabel) {
                        				actionLabel = it
                        				label =	stateEngineService.getStateLabel ( moveBundleInstance.workflowCode, stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode,it) )
										return;
                        			}
	                    			
                        		}
                        	}
                        	assetComment = AssetComment.findAllByAssetEntityAndCommentType( assetItem,'instruction' )
                        	def stateLabel = stateEngineService.getStateLabel( moveBundleInstance.workflowCode, transitionStates[0].stateTo )
							def modelConnectors
							if(assetItem.model)
								modelConnectors = ModelConnector.findAllByModel( assetItem.model )
								
							def assetCableMapList = AssetCableMap.findAllByAssetFrom( assetItem )
							def currRoomRackAssets = []
							if(assetItem.roomSource){
								currRoomRackAssets = AssetEntity.findAllByRoomSourceAndSourceRack(assetItem.roomSource,assetItem.sourceRack)
							}else{
								currRoomRackAssets = AssetEntity.findAllByRoomTargetAndTargetRack(assetItem.roomTarget,assetItem.targetRack)
							}
							def dependencyStatus = AssetCableStatus.list
							def assetCablingDetails = []
							assetCableMapList.each {
								
								def connectorLabel = it.assetToPort ? it.assetToPort.label :""
								if(it.assetFromPort.type == "Power"){
									connectorLabel = it.toPower ? it.toPower : ""
								}
								assetCablingDetails << [connector : it.assetFromPort.connector, type:it.assetFromPort.type,
														labelPosition:it.assetFromPort.labelPosition, label:it.assetFromPort.label, 
														status:it.cableStatus,displayStatus:it.cableStatus, color:it.cableColor ? it.cableColor : "",
														connectorPosX:it.assetFromPort.connectorPosX, connectorPosY:it.assetFromPort.connectorPosY,
														hasImageExist:assetItem.model.rearImage && assetItem.model?.useImage ? true : false,
														rackUposition : rackUposition,currRoomRackAssets:currRoomRackAssets-assetItem, 
														dependencyStatus:dependencyStatus, fromAssetId :(it.assetTo? it.assetTo?.assetName+"/"+connectorLabel:'') ]
							}	
								
                        	render ( view:'assetSearch',
                                model:[ projMap:projMap, assetComment:assetComment?assetComment :"", stateVal:stateVal, bundle:params.bundle, 
                                		team:params.team, project:params.project, location:params.location, search:params.search, label:label,
                                		actionLabel:actionLabel, commentsList: commentsList, stateLabel: stateLabel, assetCablingDetails : assetCablingDetails
                                		])
                        }
                    //}
                }
            }
		 
        } else {
            flash.message = "Your login has expired and must login again."
            redirect ( action:'login' )
            return;
        }
	}
	
    /*------------------------------------------------------------------------------
     * To change the state of an asset to hold
     * @author Bhuvana
     * @param  String enterNote, String team, String location, String bundle
     * @return boolean for indication of transitions   
     *------------------------------------------------------------------------------*/
	def placeHold = {
        def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
        if ( principal ) {
        	def enterNote = params.enterNote
        	def moveBundleInstance = MoveBundle.findById( params.bundle )
        	if ( params.similarComment == 'nosimilar' ) {
        		clientTeamsService.appendCommentsToRemainderList( params, session )
        	}
        	def loginTeam = ProjectTeam.findById(params.team)
    		def asset = getAssetEntity ( params.search, params.user )
    		def redirectAction = "assetTask"
    		if(asset){
            def bundle = asset.moveBundle
            def loginUser = UserLogin.findByUsername ( principal )
            def workflow
            if ( params.user == "mt" ) {
                workflow = workflowService.createTransition ( moveBundleInstance.workflowCode, "MOVE_TECH", "Hold", asset,bundle, loginUser, loginTeam, params.enterNote )
                if ( workflow.success ) {
                	
                	if(params.location == 's' && asset.sourceTeamMt.id != loginTeam.id ){
				asset.sourceTeamMt = loginTeam
				asset.save(flush:true)
            		} else if(params.location == 't' && asset.targetTeamMt.id != loginTeam.id ){
				asset.targetTeamMt = loginTeam
				asset.save(flush:true)
            		}
                	
                    def assetComment = new AssetComment()
                    assetComment.comment = enterNote
                    assetComment.assetEntity = asset
                    assetComment.commentType = 'issue'
                    assetComment.category = 'moveday'
                    assetComment.createdBy = loginUser.person
                    assetComment.save()
                    redirect ( action: 'assetTask', 
                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                "location":params.location, "tab":"Todo"
                                ])
                } else {
                    flash.message = message ( code : workflow.message )
                    redirect ( action : 'assetTask', 
                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                "location":params.location, "tab":"Todo"
                                ])
                }

            } else if ( params.user == "st" ) {
                workflow = workflowService.createTransition ( moveBundleInstance.workflowCode, "ENGINEER", "Hold", asset,bundle, loginUser, loginTeam, params.enterNote )
                if ( workflow.success ) {
                	
                	if(params.location == 's' && asset.sourceTeamMt.id != loginTeam.id ){
				asset.sourceTeamMt = loginTeam
				asset.save(flush:true)
            		} else if(params.location == 't' && asset.targetTeamMt.id != loginTeam.id ){
				asset.targetTeamMt = loginTeam
				asset.save(flush:true)
            		}
                	
                    def assetComment = new AssetComment()
                    assetComment.comment = enterNote
                    assetComment.assetEntity = asset
                    assetComment.commentType = 'issue'
                    assetComment.category = 'moveday'
                    assetComment.createdBy = loginUser.person
                    assetComment.save()
                    redirect ( action: 'assetTask', 
                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                "location":params.location, "tab":"Todo"
                                ])
                } else {
                    flash.message = message ( code : workflow.message )
                    redirect ( action : 'assetTask', 
                        params:["bundle":params.bundle, "team":params.team, "project":params.project,
                                "location":params.location, "tab":"Todo"
                                ])
                }
          
            } else {
                workflow = workflowService.createTransition ( moveBundleInstance.workflowCode, "CLEANER", "Hold", asset, bundle, loginUser, loginTeam, params.enterNote )
                def projMap = []
                def assetComment
                def stateVal = null
                def label = null
                def actionLabel = null
                if ( workflow.success ) {
                    assetComment = new AssetComment()
                    assetComment.comment = enterNote
                    assetComment.assetEntity = asset
                    assetComment.commentType = 'issue'
                    assetComment.category = 'moveday'
                    assetComment.createdBy = loginUser.person
                    assetComment.save()
                    render(view: 'logisticsAssetSearch',
                        model:[ projMap:projMap, assetComment:assetComment, stateVal:stateVal, "bundle":params.bundle, "team":params.team,
								"project":params.project, "location":params.location, "tab":"Todo", label:label, actionLabel:actionLabel
								])
                } else {
                    flash.message = message( code : workflow.message )
                    render( view: 'logisticsAssetSearch',
                        model:[ projMap:projMap, assetComment:assetComment, stateVal:stateVal, "bundle":params.bundle, "team":params.team,
                                "project":params.project, "location":params.location, "tab":"Todo", label:label, actionLabel:actionLabel
                                ])
                }
            }
    		} else {
    			if ( params.user != "mt" ){
    				redirectAction = "logisticsAssetSearch"
    			}
    			redirect ( action : redirectAction, params:params )
    		}
        } else {
        	flash.message = "Your login has expired and must login again."
        	redirect( action:'login' )
        }
	}
	
    /*------------------------------------------------------------------------------------------------------
     * To unrack the state of an asset 
     * @author Bhuvana
     * @param  String assetComment, String team, String location, String actionLabel, String search, String user
     * @return redirect to Asset details page if transition flag is busy otherwise redirect to asset task page   
     *--------------------------------------------------------------------------------------------------------*/
	def unRack = {
        def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
        if( principal ) {
        	def asset = getAssetEntity ( params.search, params.user )
        	if(asset){
	            def bundle = asset.moveBundle
	            def moveBundleInstance = MoveBundle.findById( params.bundle )
	            def loginTeam = ProjectTeam.findById(params.team)
	            def actionLabel = params.actionLabel
	            //def projectAssetMap = ProjectAssetMap.findByAsset( asset )
	            def holdId = stateEngineService.getStateId( moveBundleInstance.workflowCode, "Hold" )
	            def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
                        										"where t.asset_entity_id = ${asset.id} and voided = 0 and ( t.type = 'process' or t.state_To = $holdId )"+
                    											"order by date_created desc, stateTo desc limit 1 ")
	            def currentState = ""
	            if(transitionStates.size()){
	            	currentState = stateEngineService.getState( moveBundleInstance.workflowCode, transitionStates[0].stateTo )
	            }
	            def flags = stateEngineService.getFlags( moveBundleInstance.workflowCode, "MOVE_TECH", currentState, actionLabel )
	            def loginUser = UserLogin.findByUsername( principal )
	            def workflow = workflowService.createTransition( moveBundleInstance.workflowCode, "MOVE_TECH", actionLabel, asset, bundle, loginUser, loginTeam, params.enterNote )
	            if ( workflow.success ) {
	            	if(params.location == 's' && asset.sourceTeamMt.id != loginTeam.id ){
            			asset.sourceTeamMt = loginTeam
						asset.save(flush:true)
            		} else if(params.location == 't' && asset.targetTeamMt.id != loginTeam.id ){
            			asset.targetTeamMt = loginTeam
						asset.save(flush:true)
            		}
	            	if(flags?.contains("busy")){
	            		flash.message = message ( code : workflow.message )
	                    redirect ( action:'assetSearch', params:params)
	            	} else {
	            		redirect ( action: 'assetTask', 
	            			params:[ "bundle":params.bundle, "team":params.team, "project":params.project,
	            			         "location":params.location, "tab":"Todo" 
	            			         ])
	            	}
	            } else {
	                flash.message = message ( code : workflow.message )
	                redirect ( action:'assetSearch',params:params)
	            }
        	} else {
        		flash.message = 'Asset not found'
                redirect ( action:'assetSearch',params:params)
        	}
        } else {
        	flash.message = "Your login has expired and must login again."
        	redirect( action:'login' )
        }
	}
	
    /**-------------------------------------------------------------------------------------------
     * To view the list of assets for that particular bundle, team and location on logistics screen
     * @author Mallikarjun
     * @param  String bundle, String team, String location
     * @return Array of arguments  
     *--------------------------------------------------------------------------------------------*/
	def logisticsMyTasks = {
		def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
		if( principal ) {
            def bundleId = params.bundle
            def tab = params.tab
            def proAssetMap
            def team = params.team
            def stateVal
            def todoSize
            def allSize
            def assetList = []
            def colorCss
            def rdyState
            def ipState
            def holdState
            def issuecomments
            def assetIssueCommentListSize
            def moveBundleInstance = MoveBundle.findById( bundleId )
            def query = new StringBuffer("""select a.asset_entity_id as id, a.asset_tag as assetTag,
					a.source_rack as sourceRack, a.source_rack_position as sourceRackPosition,
					a.target_rack as targetRack, a.target_rack_position as targetRackPosition,
					min(cast(t.state_to as UNSIGNED INTEGER)) as minstate,
					m.name as model, p.current_state_id as currentStateId from asset_entity a 
					left join asset_transition t on(a.asset_entity_id = t.asset_entity_id and t.voided = 0) 
					left join project_asset_map p on (a.asset_entity_id = p.asset_id)
            		left join model m on (a.model_id = m.model_id) 
            		where a.move_bundle_id = $bundleId """)
            
            stateVal = stateEngineService.getStateId ( moveBundleInstance?.workflowCode, "Cleaned" )
            allSize = jdbcTemplate.queryForList( query.toString() +" group by a.asset_entity_id" ).size()
            holdState = stateEngineService.getStateIdAsInt( moveBundleInstance?.workflowCode, "Hold" )
            if ( tab == "Todo" ) {
                query.append ( " and ( p.current_state_id < $stateVal or t.state_to = $holdState )" )
            }
            proAssetMap = jdbcTemplate.queryForList ( query.toString() +" group by a.asset_entity_id" )
            todoSize = proAssetMap.size()
            if ( params.location == "s" ) {
                rdyState = stateEngineService.getStateIdAsInt( moveBundleInstance?.workflowCode, "Cleaned" )
                ipState = stateEngineService.getStateIdAsInt( moveBundleInstance?.workflowCode, "Unracked" )
            } else {
                rdyState = stateEngineService.getStateIdAsInt( moveBundleInstance?.workflowCode, "Cleaned" )
                ipState = stateEngineService.getStateIdAsInt( moveBundleInstance?.workflowCode, "Staged" )
            }
			def sortOrder = 4
            proAssetMap.each {
                if ( it.currentStateId ) {
	                if ( it.minstate == holdState ) {
	                    colorCss = "asset_hold"
						sortOrder = 1
	                } else if ( it.currentStateId == ipState ) {
	                    colorCss = "asset_ready"
						sortOrder = 2
	                } else if ( ( it.currentStateId > holdState ) && ( it.currentStateId < ipState ) ) {
	                    colorCss = "asset_pending"
						sortOrder = 3
	                } else if ( ( it.currentStateId >= rdyState ) ) {
	                    colorCss = "asset_done"
						sortOrder = 4
	                }
                } else {
                	colorCss = "asset_pending"
					sortOrder = 3
                }
                assetList << [ item:it, cssVal:colorCss, sortOrder:sortOrder ]
            }
            assetList.sort {
               it.sortOrder
            }
            if ( tab == "All" ) {
                query.append (" and (p.current_state_id < $stateVal or t.state_to = $holdState ) group by a.asset_entity_id")
                todoSize = jdbcTemplate.queryForList ( query.toString() ).size()
            }
            def assetIssueCommentList
            if ( params.issueAssetId ) {
            	def assetItem = AssetEntity.findById( params.issueAssetId )
            	assetIssueCommentList = AssetComment.findAll("from AssetComment ac where ac.assetEntity = ${assetItem.id} and ac.commentType = 'issue' and ac.isResolved = 0 ")
            	assetIssueCommentListSize = assetIssueCommentList.size()
            }
            return[ bundle:bundleId, team:team, project:params.project, location:params.location, 
                    assetList:assetList, allSize:allSize, todoSize:todoSize, 'tab':tab, issuecomments: assetIssueCommentList,
                    assetIssueCommentListSize: assetIssueCommentListSize
                    ]
		} else {
			flash.message = "Your login has expired and must login again."
            redirect( action:'login' )
		}
	}
	 
    /*-----------------------------------------------------------------------------
     * To view the details and to change the state of an asset for logistics screen
     * @author Mallikarjun
     * @param  String search, String team, String location
     * @return Array of arguments   
     *-----------------------------------------------------------------------------*/ 
	def logisticsAssetSearch = {
		def browserTest = false
		if ( !request.getHeader( "User-Agent" ).contains( "MSIE" ) ) {
			browserTest = true
		}	
        def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
        if ( principal ) {
            def textSearch = params.textSearch
            def assetItem
            def assetComment
            def projMap = []
            def team = params.team
            def search = params.search
            if ( textSearch ) {
                search = textSearch
            }
            def stateVal
            def taskList
            def taskSize
            def label
            def actionLabel
            def teamMembers
            def loginTeam
            def issuecomments
            def assetIssueCommentListSize
			def moveBundleId = params.bundle
			moveBundleId = moveBundleId ? moveBundleId : session.getAttribute( "CURR_BUNDLE" )?.CURR_BUNDLE
            def moveBundleInstance = MoveBundle.findById( moveBundleId )
            if ( team ) {
            	loginTeam = ProjectTeam.findById ( params.team )
            }
            def commentsList = clientTeamsService.getCommentsFromRemainderList( session )
			flash.message = ""
            if ( params.menu == "true" ) {
            	render(view:'logisticsAssetSearch', 
                    model:[ projMap:projMap, assetComment:assetComment, stateVal:stateVal, bundle:moveBundleId,
                            team:params.team, project:params.project, location:params.location, search:search,
                            label:label, actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList
                            ])
            	return;
            } else if ( search != null ) {
            	def query = "from AssetEntity where moveBundle=${moveBundleInstance.id} and assetTag = :search "
                assetItem = AssetEntity.find ( query.toString(), [ search : search ] )
                if ( assetItem == null ) {
                    flash.message = message ( code : "Asset Tag number '${search}' was not located" )
                    if ( textSearch ) {
                        render ( view:'logisticsAssetSearch',
                            model:[ projMap:projMap, assetComment:assetComment, stateVal:stateVal, bundle:moveBundleId,
                                    team:params.team, project:params.project, location:params.location,
                                    search:search, label:label, actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList
                                    ])
                        return;
                    } else {
                        redirect( action:'logisticsTask',
                            params:[ "bundle":moveBundleId, "team":params.team, "project":params.project,
                                     "location":params.location, "tab":params.tab
                                     ])
                        return;
                    }
                } else {
                    teamMembers = partyRelationshipService.getTeamMemberNames( assetItem.sourceTeamMt?.id )
                    def membersCount = ( ( teamMembers.toString() ).tokenize("/") ).size()
                    teamMembers = membersCount + "(" + teamMembers.toString() + ")"
                    def bundleId = assetItem.moveBundle?.id
                   /* def teamId
                    def teamName
                    if ( assetItem.sourceTeamMt ) {
                        teamId = ( assetItem.sourceTeamMt.id ).toString()
                        teamName = assetItem.sourceTeamMt.name
                    } else {
                        flash.message = message ( code : "The asset [${assetItem.assetName}] is not assigned to team [${loginTeam}]" )
                        if ( textSearch ) {
                            render ( view : 'logisticsAssetSearch',
                                model:[ teamMembers:teamMembers, projMap:projMap, assetComment:assetComment, stateVal:stateVal, bundle:moveBundleId,
                                        team:params.team, project:params.project, location:params.location, search:search, label:label,
                                        actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList
                                        ])
                            return;
                        } else {
                            redirect ( action: 'logisticsMyTasks', 
                                params:[ "bundle":moveBundleId, "team":params.team, "project":params.project,
                                         "location":params.location, "tab":params.tab
                                         ])
                            return;
                        }
                    }*/
                    /*if ( bundleId != Integer.parseInt ( moveBundleId ) ) {
                        flash.message = message ( code : "The asset [${assetItem.assetName}] is not part of bundle [${moveBundleId}]" )
                        if ( textSearch ) {
                            render ( view:'logisticsAssetSearch',
                                model:[ teamMembers:teamMembers, projMap:projMap, assetComment:assetComment, stateVal:stateVal,
                                        bundle:moveBundleId, team:params.team, project:params.project, location:params.location,
                                        search:search, label:label, actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList
                                        ])
                            return;
                        } else {
                            redirect ( action: 'logisticsMyTasks', 
                                params:[ "bundle":moveBundleId, "team":params.team, "project":params.project,
                                         "location":params.location, "tab":params.tab
                                         ])
                            return;
                        }
                    } else {*/
                    	def holdId = stateEngineService.getStateId( moveBundleInstance.workflowCode, "Hold" )
                    	def transitionStates = jdbcTemplate.queryForList("select cast(t.state_to as UNSIGNED INTEGER) as stateTo from asset_transition t "+
                    													"where t.asset_entity_id = ${assetItem.id} and voided = 0 and ( t.type = 'process' or t.state_To = $holdId )"+
                    													"order by date_created desc, stateTo desc limit 1 ")
                        projMap = ProjectAssetMap.findByAsset( assetItem )
                        if( !transitionStates.size()) {
                            flash.message = message ( code : " No actions for this asset " )
                            if ( textSearch ) {
                                render(view:'logisticsAssetSearch',
                                    model:[ teamMembers:teamMembers, projMap:projMap, assetComment:assetComment, stateVal:stateVal,
                                            bundle:moveBundleId, team:params.team, project:params.project, location:params.location,
                                            search:search, label:label, actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList
                                            ])
                                return;
                            } else {
                                redirect ( action: 'logisticsMyTasks',
                                    params:[ "bundle":moveBundleId, "team":params.team, "project":params.project, 
                                             "location":params.location,"tab":params.tab
                                             ])
                                return;
                            }
                        } else {
                            stateVal = stateEngineService.getState ( moveBundleInstance.workflowCode, transitionStates[0].stateTo )
                            if ( stateVal == "Hold" ) {
                            	def assetIssueCommentList = AssetComment.findAll("from AssetComment ac where ac.assetEntity = ${assetItem.id} and ac.commentType = 'issue' and ac.isResolved = 0")
                            	assetIssueCommentListSize = assetIssueCommentList.size()
                                flash.message = message ( code :"The asset is currently on HOLD because: " )
                                if ( textSearch ) {
                                    render ( view:'logisticsAssetSearch',
                                        model:[	teamMembers:teamMembers, projMap:projMap,assetComment:assetComment, stateVal:stateVal,
                                               	bundle:moveBundleId, team:params.team, project:params.project, location:params.location,
                                               	search:search, label:label, actionLabel:actionLabel, browserTest:browserTest, 
                                               	issuecomments: assetIssueCommentList, assetIssueCommentListSize: assetIssueCommentListSize,
                                               	commentsList: commentsList
                                               	])
                                    return;
                                } else {
                                    redirect ( action: 'logisticsMyTasks',
                                        params:[ "bundle":moveBundleId, "team":params.team, "project":params.project, 
                                                 "location":params.location,"tab":params.tab, "issueAssetId" : String.valueOf( assetItem.id )
                                                 ])
                                    return;
                                }
                            }
                            taskList = stateEngineService.getTasks ( moveBundleInstance.workflowCode, "CLEANER", stateVal )
                            taskSize = taskList.size()
                            if ( taskSize == 1 ) {
                                if ( taskList.contains ( "Hold" ) ) {
                                    flash.message = message ( code : "NO ACTIONS FOR ASSET. You may place it on hold to alert the move coordinator" )
                                } else {
                                	 actionLabel = taskList[0]
                                     label =	stateEngineService.getStateLabel ( moveBundleInstance.workflowCode, stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode,actionLabel) )
                                }
	                        	
                            } else if ( taskSize > 1 ) {
                                taskList.each {
                                    if ( it != "Hold" && !actionLabel ) {
                                        actionLabel = it
                                        label =	stateEngineService.getStateLabel ( moveBundleInstance.workflowCode, stateEngineService.getStateIdAsInt(moveBundleInstance.project.workflowCode,it) )
										return;
                                    }
                                }
                            }
                            assetComment = AssetComment.findAllByAssetEntityAndCommentType( assetItem,'instruction' )
                            def cleanedId = stateEngineService.getStateIdAsInt( moveBundleInstance.workflowCode, "Cleaned" )
                            def cartAssetCountQuery = new StringBuffer(" select count(a.asset_entity_id) as assetCount from asset_entity a "+
                            											"left join project_asset_map p on ( a.asset_entity_id = p.asset_id  ) " +
                            											"where a.cart = '$assetItem.cart' and a.move_bundle_id = $bundleId "+
                            											"and p.project_id = $assetItem.project.id and p.current_state_id < $cleanedId ")
                			def cartAssetCount = jdbcTemplate.queryForInt( cartAssetCountQuery.toString() )
                			def cartQty
                			if ( cartAssetCount == 1 ) {
                				def totalCartAssetCountQuery = new StringBuffer(" select count(a.asset_entity_id) as assetCount from asset_entity a "+    									
    																			"where a.cart = '$assetItem.cart' and a.move_bundle_id = $bundleId "+
    																			"and a.project_id = $assetItem.project.id ")
                				cartQty = jdbcTemplate.queryForInt( totalCartAssetCountQuery.toString() )
                				flash.message = "This is the last asset for cart "+assetItem.cart+" which should contain "+cartQty+" assest(s)"
                			}
                            render ( view:'logisticsAssetSearch',
                                model:[ teamMembers:teamMembers, projMap:projMap, assetComment:assetComment ? assetComment :"", stateVal:stateVal, bundle:moveBundleId,
                                		team:params.team, project:params.project, location:params.location, search:search, label:label,
                                		actionLabel:actionLabel, browserTest:browserTest, commentsList: commentsList, cartQty: cartQty
                                		])
                        }
                    //}
                }
            }
            
        } else {
            flash.message = "Your login has expired and must login again."
            redirect ( action:'login' )
        }
	}
	 
    /*--------------------------------------------------------------------------------------------------------
     * To change the state of an asset to CLEANING 
     * @author Mallikarjun
     * @param  String assetComment, String team, String location, String actionLabel, String search, String user
     * @return Message with boolean for indication of transitions   
     *-------------------------------------------------------------------------------------------------------*/ 
	def cleaning = {
        def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
        if ( principal ) {
        	def asset = getAssetEntity ( params.search, params.user )//AssetEntity.findByAssetTag(params.search)
        	if(asset){
	            def bundle = asset.moveBundle
	            //def moveBundleInstance = MoveBundle.findById( params.bundle )
	            def actionLabel = params.actionLabel
	            def loginUser = UserLogin.findByUsername ( principal )
	            def loginTeam = ProjectTeam.findById(params.team)
	            def workflow = workflowService.createTransition ( asset.moveBundle.workflowCode, "CLEANER", actionLabel, asset, bundle, loginUser, loginTeam, params.enterNote )
	            if ( workflow.success ) {
	                def projMap = []
	                def stateVal = null
	                def label = null
	                actionLabel = null
	                render(view: 'logisticsAssetSearch',
	                    model:[ projMap:projMap, stateVal:stateVal, "search":params.search, "bundle":params.bundle,
								"team":params.team, "project":params.project, "location":params.location, "tab":"Todo", label:label,
								actionLabel:actionLabel
								])
	            } else {
	                flash.message = message ( code : workflow.message )
	                redirect ( action:'logisticsAssetSearch', 
	                    params:[ "bundle":params.bundle, "team":params.team, "project":params.project, "location":params.location,
	                             "search":params.search, "label":params.label, "actionLabel":actionLabel
	                             ])
	            }
        	} else {
                flash.message = "Asset not found"
                redirect ( action:'logisticsAssetSearch', params:params)
        	}
        } else {
        	flash.message = "Your login has expired and must login again."
        	redirect( action:'login' )
        }
	}
	
    /*--------------------------------------------------------------------------------------
     * To cancel asset search in logistics screen 
     * @author Mallikarjun
     * @param  String actionLabel, String team, String location, String search, String user
     * @return render the logisticsAssetSearch with required params  
     *--------------------------------------------------------------------------------------*/
	def cancelAssetSearch = {
		def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
		if ( principal ) {
			def asset = getAssetEntity ( params.search, params.user )
			if(asset){
				def bundle = asset.moveBundle
				def loginUser = UserLogin.findByUsername ( principal )
				def team
				def projMap = []
				render(view: 'logisticsAssetSearch',
	                model:[	projMap:projMap, "search":params.search, "bundle":params.bundle, "team":params.team, 
	                       	"project":params.project, "location":params.location, "tab":"Todo" ])
			} else {
				flash.message = "Asset not found"
				redirect ( action:'logisticsAssetSearch', params:params)
            }
		} else {
			flash.message = "Your login has expired and must login again."
			redirect ( action:'login' )
		}
	}
          
    /*----------------------------------------------------
     * Redirect to MyTasks after logged in.
     * @author Mallikarjun
     * @param  String team, String location
     * @return Redirect to assetTask with required params
     *----------------------------------------------------*/
	def moveTechSuccessLogin = {
    	def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
    	// Checking user existence
    	if ( principal ) {	        
            def projectTeamInstance = ProjectTeam.findById ( params.team )
            def team = projectTeamInstance.name
            def teamMembers = partyRelationshipService.getTeamMemberNames ( params.team )
            def location =""
            if ( params.location == 's' ) {
                location = "Unracking"
                projectTeamInstance.currentLocation = "Source"
                projectTeamInstance.save(flush:true)
            } else if ( params.location == 't' ) {
                location = "Reracking"
                projectTeamInstance.currentLocation = "Target"
                projectTeamInstance.save(flush:true)
            }
            //userPreferenceService.setPreference("CURR_BUNDLE","${params.bundle}")
            redirect ( action:'assetTask', 
                params:[ projectTeam:team, members:teamMembers, project:params.project, loc:location,
                         bundle:params.bundle, team:params.team, location:params.location, "tab":"Todo"
                         ])
    	} else {
			flash.message = "Your login has expired and must login again."
            redirect ( action:'login' )
		}
	}
	
    /*----------------------------------------------------------------------------------
     * This method containing common code to return the AssetEntity object for assetTag
     * @author Lokanath
     * @param  String assetTag, String user
     * @return AssetEntity Object
     *----------------------------------------------------------------------------------*/
	def getAssetEntity ( assetTag, user ) {
		def loginCode = session.getAttribute( "USERNAME" )
		def loginDetails = loginCode.split("-")
		def movebundle = loginDetails[1]
		def bundleteam = loginDetails[2]
		def location = loginDetails[3]
		def query = new StringBuffer("from AssetEntity where assetTag = :assetTag and moveBundle = $movebundle")
		// commented as per JIRA:TM-199  
		/*if ( user != "ct" ) {
        if ( location == "s" ) {
        query.append(" and sourceTeamMt = $bundleteam")
        } else {
        query.append(" and targetTeamMt = $bundleteam")
        }
		}*/
		def asset 
		if(loginCode && assetTag){
			asset = AssetEntity.find( query.toString(), [ assetTag : assetTag ] )
		}
		return asset 
	}
    
    /*----------------------------------------------------------
	* To load the installed printers into session
	* @author : Lokanath Reddy
	*---------------------------------------------------------*/
    def setPrintersIntoSession = {
		def printers 
		if(params.dropdown){
			printers = params.dropdown.split(",")
		}
    	session.setAttribute( "PRINTERS", printers)
    }
    /*----------------------------------------------------------------------------------
     * @author Lokanath Reddy
     * @param  String assetTag, project,bundle
     * @return Create a Comment for AssetEntity
     *----------------------------------------------------------------------------------*/
	def addComment = {
		def principal = session.getAttribute ( "PRINCIPAL" )//SecurityUtils.subject.principal
		if( principal ){
			def loginUser = UserLogin.findByUsername ( principal )
			def asset = getAssetEntity ( params.search, params.user )
			if(asset){
				def assetComment = new AssetComment()
					assetComment.comment = params.enterNote
					if ( params.similarComment == "nosimilar" ) {
						clientTeamsService.appendCommentsToRemainderList( params, session )
					}
	                assetComment.assetEntity = asset
	                assetComment.commentType = 'comment'
	                assetComment.category = 'moveday'
	                assetComment.createdBy = loginUser.person
	                assetComment.save()
			} else {
				flash.message = "Asset not found"
			}
	        if( params.user != "ct" ) {
	        	redirect( action:assetSearch, params:params )
	        } else {
	        	redirect( action:logisticsAssetSearch, params:params )
	        }
		} else {
        	flash.message = "Your login has expired and must login again."
        	redirect( action:'login' )
		}
	}
		
}
