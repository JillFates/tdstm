import java.util.Date

import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit
import org.apache.shiro.SecurityUtils

import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tds.asset.AssetTransition
import com.tdssrc.grails.GormUtil

class ProjectUtilController {

	def userPreferenceService
	def partyRelationshipService
	def jdbcTemplate
	def stateEngineService
	def workflowService
	def stepSnapshotService

	def index = {

		try {
			def principal = SecurityUtils.subject.principal
			def userLogin = UserLogin.findByUsername( principal )
			def userPreference = UserPreference.findAllByUserLoginAndPreferenceCode( userLogin, "CURR_PROJ" )
			def projectInstance
			if ( userPreference != null && userPreference != []) {
				projectInstance = Project.findById( userPreference.value[0] )
			}
			if (projectInstance) {
				redirect( controller:"project", action:"show")
			} else {
				userPreferenceService.removePreference("CURR_PROJ")
				if (params.message) {
					flash.message = params.message
				}
				redirect( controller:"project", action:"list" )
			}
		} catch (Exception e) {
			throw e
		}
	}

	/*
	 * Action to return a list of projects , sorted desc by dateCreated 
	 */

	def searchList = {
		def projectList
		def partyProjectList
		def projectHasPermission = RolePermissions.hasPermission("ShowAllProjects")
		def loginUser = UserLogin.findByUsername(SecurityUtils.subject.principal)
		def sort = params.sort ? params.sort : 'dateCreated'
		def order = params.order ? params.order : 'desc'
		if(projectHasPermission){
			projectList = Project.findAll( [sort:sort, order:order] )
		}else{
			def userCompany = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'STAFF' "+
					"and partyIdTo = ${loginUser.person.id} and roleTypeCodeFrom = 'COMPANY' and roleTypeCodeTo = 'STAFF' ")
			def query = "from Project p where p.id in (select pr.partyIdFrom from PartyRelationship pr where "+
					"pr.partyIdTo = ${userCompany?.partyIdFrom?.id} and roleTypeCodeFrom = 'PROJECT') "+
					"or p.client = ${userCompany?.partyIdFrom?.id} order by ${sort} ${order}"
			projectList = Project.findAll(query)
		}
		TableFacade tableFacade = new TableFacadeImpl("tag",request)
		tableFacade.items = projectList
		Limit limit = tableFacade.limit
		if(limit.isExported()){
			tableFacade.setExportTypes(response,limit.getExportType())
			tableFacade.setColumnProperties("projectCode","name","dateCreated","lastUpdated","comment")
			tableFacade.render()
		}else
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
	def createDemo = { return }
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
			def timeDelta = startDateTime.getTime() - templateInstance?.startDate?.getTime() > 0 ? startDateTime.getTime() - templateInstance?.startDate?.getTime() : 0
			def completionDateTime = templateInstance?.completionDate?.getTime() ? new Date(templateInstance?.completionDate?.getTime() + timeDelta ) : null
			projectInstance = new Project(name:name,
					projectCode:name,
					comment:templateInstance?.comment,
					description:templateInstance?.description,
					client:templateInstance?.client,
					workflowCode:templateInstance?.workflowCode,
					projectType:"Demo",
					startDate:startDateTime,
					completionDate:completionDateTime
					)
			if(!projectInstance.hasErrors() && projectInstance.save(flush:true)){
				// create party relation ship to demo project
				def companyParty = PartyGroup.findByName( "TDS" )
				def projectCompanyRel = partyRelationshipService.savePartyRelationship("PROJ_COMPANY", projectInstance, "PROJECT", companyParty, "COMPANY" )
				// create project partner
				def tempProjectPartner = PartyRelationship.find("from PartyRelationship p where p.partyRelationshipType = 'PROJ_PARTNER' and p.partyIdFrom = ${template} and p.roleTypeCodeFrom = 'PROJECT' and p.roleTypeCodeTo = 'PARTNER' ")
				if(tempProjectPartner){
					def projectPartnerRel = partyRelationshipService.savePartyRelationship("PROJ_PARTNER", projectInstance, "PROJECT", tempProjectPartner.partyIdTo, "PARTNER" )
				}
				/* copy Project staff  */
				def tempProjectStaff = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType = 'PROJ_STAFF' and p.partyIdFrom = ${template} and p.roleTypeCodeFrom = 'PROJECT' ")
				tempProjectStaff.each{ staff->
					def partyRelationship = new PartyRelationship(
							partyRelationshipType : staff.partyRelationshipType,
							partyIdFrom : projectInstance,
							partyIdTo : staff.partyIdTo,
							roleTypeCodeFrom : staff.roleTypeCodeFrom,
							roleTypeCodeTo : staff.roleTypeCodeTo,
							statusCode : staff.statusCode,
							comment : staff.comment
							)
					if ( ! partyRelationship.validate() || ! partyRelationship.save(insert : true, flush:true) ) {
						def etext = "Unable to create asset ${partyRelationship}" +
								GormUtil.allErrorsString( partyRelationship )
						println etext
					}
				}
				/* Create project logo*/
				def tempProjectLogo = ProjectLogo.findByProject( templateInstance )
				if(tempProjectLogo){
					def newProjectLogo = new ProjectLogo(project:projectInstance, name:tempProjectLogo.name, partnerImage:tempProjectLogo.partnerImage,party:tempProjectLogo.party).save(insert : true, flush:true)
				}
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
							startTime:bundle.startTime?.getTime() ? new Date(bundle.startTime?.getTime() + timeDelta ) : null,
							completionTime:bundle?.completionTime?.getTime() ? new Date(bundle?.completionTime?.getTime() + timeDelta ) : null
							)
					if ( ! moveBundle.validate() || ! moveBundle.save(insert : true, flush:true) ) {
						def etext = "Unable to create asset ${moveBundle}" +
								GormUtil.allErrorsString( moveBundle )
						println etext
					} else {
						/*
						 *  Create Bundle Steps
						 */
						copyMoveBundleStep(moveBundle, bundle, timeDelta )

						/* Copy assets from template project to demo Project*/
						copyAssetEntity(moveBundle, bundle, timeDelta, attributeSet, userLogin  )

						/* copy bundle teams*/
						copyBundleTeams(moveBundle, bundle)

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
				redirect(controller:'project', action:'show', id: projectInstance.id)

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
					railType: asset.railType,
					ipAddress: asset.ipAddress,
					os: asset.os,
					hasRemoteMgmt: asset.hasRemoteMgmt,
					planStatus: asset.planStatus,
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
					}

				}
			} else {
				def etext = "Unable to create asset ${assetEntity.assetName}" +
						GormUtil.allErrorsString( assetEntity )
				println etext
			}
		}
	}
	/*
	 *  copy news from temp project to demo project.
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
			}
		}
	}
	/*
	 *  Copy bundleTeams and associated staff
	 */
	def copyBundleTeams(def moveBundle, def oldBundle){
		def tempBundleTeams = partyRelationshipService.getBundleTeamInstanceList( oldBundle  )
		def teamRelationshipType = PartyRelationshipType.findById("PROJ_TEAM")
		def teamRole = RoleType.findById("TEAM")
		def teamMemberRole = RoleType.findById("TEAM_MEMBER")
		tempBundleTeams.each{ obj->
			def bundleTeam = new ProjectTeam(
					name : obj.projectTeam?.name,
					comment : obj.projectTeam?.comment,
					teamCode : obj.projectTeam?.teamCode,
					currentLocation : obj.projectTeam?.currentLocation,
					isIdle : obj.projectTeam?.isIdle,
					isDisbanded : obj.projectTeam?.isDisbanded,
					moveBundle : moveBundle,
					latestAsset : null
					)
			if ( bundleTeam.validate() && bundleTeam.save(insert : true, flush:true) ) {
				// Create Partyrelation ship to BundleTeam members.
				obj.teamMembers.each{ member->
					def teamRelationship = new PartyRelationship(
							partyRelationshipType : teamRelationshipType,
							partyIdFrom : bundleTeam,
							partyIdTo : member.staff,
							roleTypeCodeFrom : teamRole,
							roleTypeCodeTo : teamMemberRole,
							statusCode : "ENABLED",
							comment : null
							)
					if ( ! teamRelationship.validate() || ! teamRelationship.save(insert : true, flush:true) ) {
						def etext = "Unable to create asset ${teamRelationship}" +
								GormUtil.allErrorsString( teamRelationship )
						println etext
					}
				}

			} else {
				def etext = "Unable to create asset ${bundleTeam}" +
						GormUtil.allErrorsString( bundleTeam )
				println etext
			}
		}
	}
}
