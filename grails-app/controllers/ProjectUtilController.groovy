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
					sourceLocation: asset.sourceLocationName,
					sourceRoom: asset.sourceRoomName,
					sourceRack: asset.sourceRackName,
					sourceRackPosition: asset.sourceRackPosition,
					targetLocation: asset.targetLocationName,
					targetRoom: asset.targetRoomName,
					targetRack: asset.targetRackName,
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
