import com.tds.asset.AssetComment
import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import grails.plugin.springsecurity.annotation.Secured
import groovy.util.logging.Slf4j
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.MoveBundleStep
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventNews
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.PartyRelationshipType
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.ProjectAssetMap
import net.transitionmanager.domain.ProjectLogo
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.domain.RoleType
import net.transitionmanager.security.Permission
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.UserPreferenceService
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
@Slf4j(value='logger', category='grails.app.controllers.ProjectUtilController')
class ProjectUtilController implements ControllerMethods {

	JdbcTemplate jdbcTemplate
	PartyRelationshipService partyRelationshipService
	UserPreferenceService userPreferenceService

	@HasPermission(Permission.ProjectView)
	def index() {
		Project project = securityService.userCurrentProject
		if (project) {
			redirect(controller: "project", action: "show")
		}
		else {
			userPreferenceService.removePreference(PREF.CURR_PROJ)
			if (params.message) {
				flash.message = params.message
			}
			redirect(controller: "project", action: "list")
		}
	}

	@HasPermission(Permission.UserGeneralAccess)
	def addUserPreference() {
		Project project = Project.findByProjectCode(params.selectProject)
		userPreferenceService.setCurrentProjectId(project.id)

		redirect(controller: 'project', action: "show", id: project.id)
	}

	/**
	 * show the project demo create project
	 */
	@HasPermission(Permission.ProjectCreate)
	def createDemo() {}

	/**
	 *  Copy all the temp project associates to demo project
	 */
	@HasPermission(Permission.ProjectCreate)
	def saveDemoProject() {
		def template = params.template
		def name = params.name
		def startDate = params.startDate
		def cleanupDate = params.cleanupDate
		Project project

		if (template && name && startDate) {
			Project templateInstance = Project.get(template)
			def startDateTime = TimeUtil.parseDate(startDate)
			int timeDelta = startDateTime.time - templateInstance?.startDate?.time > 0 ? startDateTime.time - templateInstance?.startDate?.time : 0
			def completionDateTime = templateInstance?.completionDate?.time ? new Date(templateInstance?.completionDate?.time + timeDelta) : null

			project = new Project(
					name: name,
					projectCode: name,
					guid: StringUtil.generateGuid(),
					comment: templateInstance?.comment,
					description: templateInstance?.description,
					client: templateInstance?.client,
					workflowCode: templateInstance?.workflowCode,
					projectType: "Demo",
					startDate: startDateTime,
					completionDate: completionDateTime,
					collectMetrics: 0
			)

			if (!project.hasErrors() && project.save(flush:true)) {
				// create party relation ship to demo project
				def companyParty = PartyGroup.findByName("TDS")
				partyRelationshipService.savePartyRelationship("PROJ_COMPANY", project, "PROJECT", companyParty, "COMPANY")
				// create project partner
				List<PartyRelationship> tempProjectPartners = PartyRelationship.executeQuery("""
					from PartyRelationship p
					where p.partyRelationshipType = 'PROJ_PARTNER'
					  and p.partyIdFrom = :template
					  and p.roleTypeCodeFrom = 'PROJECT'
					  and p.roleTypeCodeTo = 'PARTNER' """, [template: templateInstance])
				for (tempProjectPartner in tempProjectPartners) {
					partyRelationshipService.savePartyRelationship("PROJ_PARTNER", project, "PROJECT",
					                                               tempProjectPartner.partyIdTo, "PARTNER")
				}

				/* copy Project staff  */
				List<PartyRelationship> tempProjectStaff = PartyRelationship.executeQuery("""
					from PartyRelationship
					where partyRelationshipType = 'PROJ_STAFF'
					  and partyIdFrom = :template
					  and roleTypeCodeFrom = 'PROJECT'
				""", [template: templateInstance])

				for (PartyRelationship staff in tempProjectStaff) {
					def partyRelationship = new PartyRelationship(
							partyRelationshipType: staff.partyRelationshipType,
							partyIdFrom: project,
							partyIdTo: staff.partyIdTo,
							roleTypeCodeFrom: staff.roleTypeCodeFrom,
							roleTypeCodeTo: staff.roleTypeCodeTo,
							statusCode: staff.statusCode,
							comment: staff.comment)
					if (!partyRelationship.validate() || !partyRelationship.save(flush: true)) {
						def etext = "Unable to create asset $partyRelationship: ${GormUtil.allErrorsString(partyRelationship)}"
						println etext
					}
				}

				/* Create project logo*/
				def tempProjectLogo = ProjectLogo.findByProject(templateInstance)
				if (tempProjectLogo) {
					new ProjectLogo(project: project,
					                name: tempProjectLogo.name,
					                partnerImage: tempProjectLogo.partnerImage).save(flush:true)
				}

				/* Create Demo Bundle */
				EavAttributeSet attributeSet = EavAttributeSet.get(1)
				MoveBundle.findAllByProject(templateInstance).each { bundle ->
					def moveBundle = new MoveBundle(project: project, name: bundle.name, description: bundle.description,
							moveEvent: null, startTime: bundle.startTime?.time ? new Date(bundle.startTime?.time + timeDelta) : null,
							completionTime: bundle?.completionTime?.time ? new Date(bundle?.completionTime?.time + timeDelta) : null)
					if (! moveBundle.validate() || ! moveBundle.save(flush:true)) {
						def etext = "Unable to create asset $moveBundle: ${GormUtil.allErrorsString(moveBundle)}"
						println etext
					}
					else {
						copyMoveBundleStep(moveBundle, bundle, timeDelta)
						copyAssetEntity(moveBundle, bundle, attributeSet)
						copyBundleTeams(moveBundle, bundle)
					}
				}

				/* Create Demo Event */
				MoveEvent.findAllByProject(templateInstance).each { MoveEvent event ->
					MoveEvent moveEvent = new MoveEvent(project: project, name: event.name, description: event.description,
						newsBarMode: event.newsBarMode, calcMethod: event.calcMethod)
					saveWithWarnings moveEvent
					if (!moveEvent.hasErrors()) {
						// assign event to appropriate bundles.
						List<MoveBundle> newEventBundles = MoveBundle.executeQuery('''
							from MoveBundle
							where project=:project
							  and name in (select name from MoveBundle where moveEvent=:event)''',
							[project: project, event: event])
						for (MoveBundle newBundle in newEventBundles) {
							moveEvent.addToMoveBundles(newBundle.id)
						}

						copyMoveEventNews(moveEvent, event)
					}
				}

				userPreferenceService.setCurrentProjectId(project.id)
				redirect(controller: 'project', action: 'show', id: project.id)
			}
			else {
				project.errors.allErrors.each { println it }
				render(view: "createDemo",
				       model: [name: name, template: template, startDate: startDate, cleanupDate: cleanupDate,
				               nameError: "Demo Project Name must be unique"])
			}
		} else {
			render(view: "createDemo",
			       model: [name: name, template: template, startDate: startDate,cleanupDate: cleanupDate,
			               nameError: name ? "" : "Demo Project Name should not be blank",
			               startDateError: startDate ? "" : "Demo start Date should not be blank"])
		}
	}

	/*
	 *  Copy MoveBundleSteps from template project bundle.
	 */
	private void copyMoveBundleStep(MoveBundle moveBundle, MoveBundle oldBundle, int timeDelta) {
		MoveBundleStep.findAllByMoveBundle(oldBundle).each { step ->
			def moveBundleStep = new MoveBundleStep(
					moveBundle: moveBundle,
					transitionId: step.transitionId,
					label: step.label,
					planStartTime: new Date(step.planStartTime?.time + timeDelta),
					planCompletionTime: new Date(step.planCompletionTime?.time + timeDelta),
					calcMethod: step.calcMethod)
			if (!moveBundleStep.save()) {
				def etext = "Unable to create asset $moveBundleStep: ${GormUtil.allErrorsString(moveBundleStep)}"
				println etext
			}
		}
	}

	/*
	 * Copy Assets from template project bundle.
	 */
	private void copyAssetEntity(MoveBundle moveBundle, MoveBundle oldBundle, EavAttributeSet attributeSet) {
		AssetEntity.findAllByMoveBundle(oldBundle).each { asset ->
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
					custom8: asset.custom8)
			if (assetEntity.save()) {
				// Copy project asset map
				def assetProjectMap = ProjectAssetMap.findByAsset(asset)
				if (assetProjectMap) {
					new ProjectAssetMap(project: assetEntity.project, asset: assetEntity,
						                 currentStateId: assetProjectMap.currentStateId).save()
				}

				// Copy asset comments
				def tempAssetComments = AssetComment.findAllByAssetEntity(asset)
				tempAssetComments.each { comment->
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
							displayOption : comment.displayOption)
					if (! assetComment.validate() || ! assetComment.save(flush:true)) {
						def etext = "Unable to create asset $assetComment: ${GormUtil.allErrorsString(assetComment)}"
						println etext
					}
				}
			}
			else {
				def etext = "Unable to create asset $assetEntity.assetName: ${GormUtil.allErrorsString(assetEntity)}"
				println etext
			}
		}
	}

	/*
	 * copy news from temp project to demo project.
	 */
	def copyMoveEventNews(def moveEvent, def oldEvent){
		def tempMoveEventNews = MoveEventNews.findAllByMoveEvent(oldEvent)
		tempMoveEventNews.each { news->
			def moveNews = new MoveEventNews(
					moveEvent : moveEvent,
					message  : news.message,
					isArchived  : news.isArchived,
					dateCreated : news.dateCreated,
					dateArchived  : news.dateArchived,
					resolution : news.resolution,
					archivedBy : news.archivedBy,
					createdBy : news.createdBy)
			if (! moveNews.validate() || ! moveNews.save(flush:true)) {
				def etext = "Unable to create asset $moveNews: ${GormUtil.allErrorsString(moveNews)}"
				println etext
			}
		}
	}
	/*
	 *  Copy bundleTeams and associated staff
	 */
	def copyBundleTeams(moveBundle, oldBundle){
		def tempBundleTeams = partyRelationshipService.getBundleTeamInstanceList(oldBundle)
		PartyRelationshipType teamRelationshipType = PartyRelationshipType.load("PROJ_TEAM")
		RoleType teamRole = RoleType.load("TEAM")
		RoleType teamMemberRole = RoleType.load("TEAM_MEMBER")
		for (obj in tempBundleTeams) {
			def bundleTeam = new ProjectTeam(name: obj.projectTeam?.name, comment: obj.projectTeam?.comment,
					teamCode: obj.projectTeam?.teamCode, currentLocation: obj.projectTeam?.currentLocation,
					isIdle: obj.projectTeam?.isIdle, isDisbanded: obj.projectTeam?.isDisbanded,
					moveBundle: moveBundle, latestAsset: null)
			saveWithWarnings bundleTeam, true
			if (!bundleTeam.hasErrors()) {
				// Create Partyrelation ship to BundleTeam members.
				for (member in obj.teamMembers) {
					def teamRelationship = new PartyRelationship(partyRelationshipType: teamRelationshipType,
							partyIdFrom: bundleTeam, partyIdTo: member.staff, roleTypeCodeFrom: teamRole,
							roleTypeCodeTo: teamMemberRole, statusCode: "ENABLED", comment: null)
					saveWithWarnings teamRelationship, true
				}
			}
		}
	}
}
