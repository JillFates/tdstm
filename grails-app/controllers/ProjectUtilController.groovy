import java.math.BigInteger;
import java.util.Date;

import org.jsecurity.SecurityUtils
import com.tdssrc.grails.GormUtil
class ProjectUtilController {

	def userPreferenceService
	def partyRelationshipService
	def jdbcTemplate
	def stateEngineService
	def workflowService
	def stepSnapshotService
	
    def index = { 
    		 
        try{
            def principal = SecurityUtils.subject.principal
            def userLogin = UserLogin.findByUsername( principal )
            def userPreference = UserPreference.findAllByUserLoginAndPreferenceCode( userLogin, "CURR_PROJ" )
            if ( userPreference != null && userPreference != [] ) {
            	def projectInstance = Project.findById( userPreference.value[0] )
                redirect( controller:"project", action:"show",id:projectInstance.id)
            } else {
            	if(params.message){
            		flash.message = params.message
            	}
            	redirect( action:"searchList" )
            }
        } catch (Exception e){
            flash.message = "Your login has expired and must login again"
            redirect(controller:'auth', action:'login')
        }
    }
    
    /*
     * Action to return a list of projects , sorted desc by dateCreated 
     */
        
    def searchList = {
			def projectList
    		def partyProjectList
    		def isAdmin = SecurityUtils.getSubject().hasRole("ADMIN")
    		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
    	if(isAdmin){	
        	  projectList = Project.findAll( "from Project as p order by p.dateCreated desc" )
    	}else{
    		def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
    							"and partyIdTo = ${loginUser.person.id} and roleTypeCodeFrom = 'COMPANY' and roleTypeCodeTo = 'STAFF' ")
    		def query = "from Project p where p.id in (select pr.partyIdFrom from PartyRelationship pr where "+
    					"pr.partyIdTo = ${userCompany?.partyIdFrom?.id} and roleTypeCodeFrom = 'PROJECT') or p.client = ${userCompany?.partyIdFrom?.id}"
    			projectList = Project.findAll(query)
    	}
    	return [ projectList:projectList ]
    }
    /*
     * Action to setPreferences
     */
    def addUserPreference = {
    		
        def projectInstance = Project.findByProjectCode(params.selectProject)
    		
        userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )

        redirect(controller:'project', action:"show", id: projectInstance.id )
    		
    }
    /*
	 *  show the prject demo create project
	 */
	def createDemo = {
			return
	}
	/*
	 *  Copy all the temp project associates to demo project 
	 */
	def saveDemoProject = {
		def template = params.template
		def name = params.name
		def startDate = params.startDate
		def cleanupDate = params.cleanupDate
		def projectInstance
		if(template && name && startDate){
			
			/*
			 *  Create Project
			 */
			def templateInstance = Project.get(template)
			def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
			def startDateTime = GormUtil.convertInToGMT(new Date("$startDate"), tzId)
			def timeDelta = startDateTime.getTime() - templateInstance?.startDate?.getTime()
			def completionDateTime = new Date(templateInstance?.completionDate?.getTime() + timeDelta )
			projectInstance = new Project(name:name, projectCode:name, description:templateInstance?.description, 
											client:templateInstance?.client, workflowCode:templateInstance?.workflowCode, 
											projectType:"Demo", startDate:startDateTime, completionDate:completionDateTime )
			if(!projectInstance.hasErrors() && projectInstance.save(flush:true)){
				// create party relation ship to demo project
				def companyParty = PartyGroup.findByName( "TDS" )
	        	def projectCompanyRel = partyRelationshipService.savePartyRelationship("PROJ_COMPANY", projectInstance, "PROJECT", companyParty, "COMPANY" )
				
				/* Create Demo Bundle */
				def attributeSet = com.tdssrc.eav.EavAttributeSet.findById(1)
				def principal = SecurityUtils.subject.principal
				def userLogin = UserLogin.findByUsername( principal )
				def tempMoveBundleList = MoveBundle.findAllByProject( templateInstance )
				tempMoveBundleList.each{ bundle ->
					def moveBundle = new MoveBundle(project:projectInstance,
												name:bundle.name,
												description:bundle.description,
												moveEvent:null,
												startTime:new Date(bundle.startTime?.getTime() + timeDelta ), 
												completionTime:new Date(bundle?.completionTime?.getTime() + timeDelta )
												)
					if ( ! moveBundle.validate() || ! moveBundle.save(insert : true, flush:true) ) {
						def etext = "Unable to create asset ${moveBundle}" +
						GormUtil.allErrorsString( moveBundle )
						println etext
						log.error( etext )
					} else {
						/*
						 *  Create Move Bundle Steps
						 */
						copyMoveBundleStep(moveBundle, bundle, timeDelta )
						
						/* Copy assets from template project to demo Project*/
						copyAssetEntity(moveBundle, bundle, timeDelta, attributeSet, userLogin  )
						
						//stepSnapshotService.process( moveBundle.id )
					}
				}
				
				/* Create Demo Event */
	        	def templateMoveEvents = MoveEvent.findAllByProject(templateInstance)
	        	templateMoveEvents.each{ event ->
					def moveEvent = new MoveEvent( project:projectInstance,
													name:event.name,
													description:event.description,
													inProgress:event.inProgress,
													calcMethod:event.calcMethod
													)
					if ( ! moveEvent.validate() || ! moveEvent.save(insert : true, flush:true) ) {
						def etext = "Unable to create asset ${moveEvent}" +
						GormUtil.allErrorsString( moveEvent )
						println etext
						log.error( etext )
					} else {
						// assign event to appropriate bundles.
						def newEventBundles = MoveBundle.findAll(" From MoveBundle mb where mb.project = ${projectInstance.id} and mb.name in ( select tmb.name from MoveBundle tmb where tmb.moveEvent = ${event.id})" )
						newEventBundles.each{ newBundle ->
							moveEvent.addToMoveBundles( MoveBundle.get( newBundle.id ) )
							stepSnapshotService.process( newBundle.id )
						}
						// copy template event news
						copyMoveEventNews(moveEvent, event)
					}
				}
				
				userPreferenceService.setPreference( "CURR_PROJ", "${projectInstance.id}" )
				redirect(controller:'project', action:'list')
				
			} else {
				projectInstance.errors.allErrors.each() { println it }
				render(view:"createDemo", model:[ name:name, template:template, startDate:startDate,cleanupDate:cleanupDate,
												  nameError: "Demo Project Name must be unique" ]  )
			}
		} else {
			render(view:"createDemo", model:[ name:name, template:template, startDate:startDate,cleanupDate:cleanupDate,
											  nameError:name ? "" :"Demo Project Name should not be blank", startDateError: startDate ? "" :"Demo start Date should not be blank" ] )
		}
	}

	/*
	 *  Copy MoveBundleSteps from template project bundle.
	 */
	def copyMoveBundleStep( def moveBundle, def oldBundle, def timeDelta ){
		def tempBundleSteps = MoveBundleStep.findAllByMoveBundle( oldBundle )
		tempBundleSteps.each{ step->
			
			def moveBundleStep = new MoveBundleStep(
				moveBundle: moveBundle,
				transitionId: step.transitionId,
				label: step.label,
				planStartTime: new Date(step.planStartTime?.getTime() + timeDelta ),
				planCompletionTime: new Date(step.planCompletionTime?.getTime() + timeDelta ),
				calcMethod: step.calcMethod
			)
			if ( ! moveBundleStep.validate() || ! moveBundleStep.save(insert : true, flush:true) ) {
				def etext = "Unable to create asset ${moveBundleStep}" +
				GormUtil.allErrorsString( moveBundleStep )
				println etext
				log.error( etext )
			}
		}
	}
	/*
	 * Copy Assets from template project bundle.
	 */
	def copyAssetEntity(def moveBundle, def oldBundle, def timeDelta, def attributeSet, def userLogin ){
		def assetEntityList = AssetEntity.findAllByMoveBundle( oldBundle )
		assetEntityList.each{ asset->
			def assetEntity = new AssetEntity(
					attributeSet:attributeSet,
					assetName: asset.assetName,
					assetType: asset.assetType,
					assetTag: asset.assetTag,
					serialNumber: asset.serialNumber,
					manufacturer: asset.manufacturer,
					model: asset.model,
					application: asset.application,
					owner: moveBundle?.project?.client,
					sourceLocation: asset.sourceLocation,
					sourceRoom: asset.sourceRoom,
					sourceRack: asset.sourceRack,
					sourceRackPosition: asset.sourceRackPosition,
					targetLocation: asset.targetLocation,
					targetRoom: asset.targetRoom,
					targetRack: asset.targetRack,
					targetRackPosition: asset.targetRackPosition,
					usize: asset.usize,
					railType: asset.railType,
					fiberCabinet: asset.fiberCabinet,
					fiberType: asset.fiberType,
					fiberQuantity: asset.fiberQuantity,
					hbaPort: asset.hbaPort,
					ipAddress: asset.ipAddress,
					hinfo: asset.hinfo,
					kvmDevice: asset.kvmDevice,
					kvmPort: asset.kvmPort,
					hasKvm: asset.hasKvm,
					hasRemoteMgmt: asset.hasRemoteMgmt,
					newOrOld: asset.newOrOld,
					nicPort: asset.nicPort,
					powerType: asset.powerType,
					pduType: asset.pduType,
					pduPort: asset.pduPort,
					pduQuantity: asset.pduQuantity,
					remoteMgmtPort: asset.remoteMgmtPort,
					truck: asset.truck,
					appOwner: asset.appOwner,
					appSme: asset.appSme,
					priority : asset.priority,
					project: moveBundle?.project,
					shortName: asset.shortName,
					moveBundle: moveBundle,
					cart: asset.cart,
					shelf: asset.shelf,
					custom1: asset.custom1,
					custom2: asset.custom2,
					custom3: asset.custom3,
					custom4: asset.custom4,
					custom5: asset.custom5,
					custom6: asset.custom6,
					custom7: asset.custom7,
					custom8: asset.custom8
			)
			if ( assetEntity.validate() && assetEntity.save(insert : true, flush:true) ) {
				/*
				 *  Copy assetTransitions
				 */
				def assetTransitions = AssetTransition.findAllByAssetEntity( asset )
				assetTransitions.each{ trans ->
					def assetTransition = new AssetTransition( 
							assetEntity : assetEntity,
							moveBundle  : assetEntity.moveBundle,
							projectTeam : null,
							userLogin   : userLogin,
							stateFrom: trans.stateFrom,
							stateTo: trans.stateTo,
							timeElapsed: trans.timeElapsed,
							wasOverridden: trans.wasOverridden,
							wasSkippedTo: trans.wasSkippedTo,
							comment: trans.comment,
							dateCreated: new Date(trans.dateCreated?.getTime() + timeDelta ),
							voided: trans.voided,
							type: trans.type,
							isNonApplicable: trans.isNonApplicable
							)
					if ( ! assetTransition.validate() || ! assetTransition.save(insert : true, flush:true) ) {
						def etext = "Unable to create asset ${assetTransition}" +
						GormUtil.allErrorsString( assetTransition )
						println etext
						log.error( etext )
					}
				}
				/*
				 *  Copy project asset map
				 */
				def assetProjectMap = ProjectAssetMap.findByAsset( asset )
				if ( assetProjectMap){
					def newProjectAssetMap = new ProjectAssetMap( project : assetEntity.project, asset : assetEntity, currentStateId : assetProjectMap.currentStateId ).save(insert : true, flush:true)
				}
				/*
				 *  Copy asset comments
				 */
				def tempAssetComments = AssetComment.findAllByAssetEntity( asset )
				tempAssetComments.each{ comment->
					def assetComment = new AssetComment(
							comment : comment.comment,
							commentType : comment.commentType,
							mustVerify : comment.mustVerify,
							assetEntity : assetEntity,
							dateCreated : comment.dateCreated,
							isResolved : comment.isResolved,
							dateResolved : comment.dateResolved,
							resolution : comment.resolution,
							resolvedBy : comment.resolvedBy,
							createdBy : comment.createdBy,
							commentCode : comment.commentCode,
							category : comment.category,
							displayOption : comment.displayOption
							)
					if ( ! assetComment.validate() || ! assetComment.save(insert : true, flush:true) ) {
						def etext = "Unable to create asset ${assetComment}" +
						GormUtil.allErrorsString( assetComment )
						println etext
						log.error( etext )
					}
					
				}
			} else {
				def etext = "Unable to create asset ${assetEntity.assetName}" +
				GormUtil.allErrorsString( assetEntity )
				println etext
				log.error( etext )
			}
		}
	}
	/*
	 *  copy move news from temp project to demo project.
	 */
	def copyMoveEventNews( def moveEvent, def oldEvent){
		def tempMoveEventNews = MoveEventNews.findAllByMoveEvent( oldEvent )
		tempMoveEventNews.each{ news->
			def moveNews = new MoveEventNews(
					moveEvent : moveEvent,
					message  : news.message,
					isArchived  : news.isArchived,
					dateCreated : news.dateCreated,
					dateArchived  : news.dateArchived,
					resolution : news.resolution,
					archivedBy : news.archivedBy,
					createdBy : news.createdBy
					)
			if ( ! moveNews.validate() || ! moveNews.save(insert : true, flush:true) ) {
				def etext = "Unable to create asset ${moveNews}" +
				GormUtil.allErrorsString( moveNews )
				println etext
				log.error( etext )
			}
		}
	}
}
