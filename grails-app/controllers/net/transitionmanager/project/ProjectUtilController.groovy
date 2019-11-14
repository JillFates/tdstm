package net.transitionmanager.project


import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.party.PartyRelationship
import net.transitionmanager.party.PartyRelationshipType
import net.transitionmanager.security.RoleType
import net.transitionmanager.security.Permission
import net.transitionmanager.party.PartyRelationshipService
import net.transitionmanager.person.UserPreferenceService
import org.springframework.jdbc.core.JdbcTemplate

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
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

			moveNews.save(flush:true)
		}
	}
	/*
	 *  Copy bundleTeams and associated staff
	 */
	def copyBundleTeams(moveBundle, oldBundle){
		def tempBundleTeams = partyRelationshipService.getBundleTeamInstanceList(oldBundle)
		PartyRelationshipType teamRelationshipType = PartyRelationshipType.load("ROLE_PROJ_TEAM")
		RoleType teamRole = RoleType.load(RoleType.CODE_PARTY_TEAM)
		RoleType teamMemberRole = RoleType.load(RoleType.CODE_TEAM_MEMBER)
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
