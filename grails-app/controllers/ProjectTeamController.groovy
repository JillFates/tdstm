import com.tds.asset.AssetEntity
import com.tdsops.common.security.spring.HasPermission
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.MoveBundle
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.ProjectTeam
import net.transitionmanager.security.Permission
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.SecurityService

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ProjectTeamController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	PartyRelationshipService partyRelationshipService
	SecurityService securityService

	@HasPermission(Permission.ProjectTeamView)
	def list(Long bundleId) {
		MoveBundle moveBundle = bundleId ?
			MoveBundle.get(bundleId) :
			MoveBundle.findByProject(securityService.userCurrentProject)
		[projectTeamInstanceList: partyRelationshipService.getBundleTeamInstanceList(moveBundle),
		 bundleInstance: moveBundle]
	}

	@HasPermission(Permission.ProjectTeamView)
	def show(Long bundleId) {
		ProjectTeam projectTeam = projectTeamFromParams()
		if (!projectTeam) return

		[projectTeamInstance: projectTeam, bundleInstance: MoveBundle.get(bundleId),
		 teamMembers: partyRelationshipService.getBundleTeamMembers(projectTeam)]
	}

	@HasPermission(Permission.ProjectTeamDelete)
	def delete() {
		ProjectTeam projectTeam = projectTeamFromParams()
		if (!projectTeam) return

		Map args = [projectTeam: projectTeam]

		PartyRelationship.executeUpdate('''\
			delete from PartyRelationship
			where partyRelationshipType.id = 'PROJ_TEAM'
			  and partyIdFrom = : projectTeam
			  and roleTypeCodeFrom.id = 'TEAM'
		''', args)

		for (String property in ['sourceTeamMt', 'targetTeamMt', 'sourceTeamLog', 'targetTeamLog',
		                         'sourceTeamSa', 'targetTeamSa', 'sourceTeamDba', 'targetTeamDba']) {
			AssetEntity.executeUpdate('update AssetEntity ae set ae.' + property + ' = null ' +
			                          'where ae.' + property + ' = :projectTeam', args)
		}

		projectTeam.delete()

		flash.message = "ProjectTeam $projectTeam deleted"
		redirect(action: 'list', params: [bundleId: params.bundleId])
	}

	@HasPermission(Permission.ProjectTeamEdit)
	def edit(Long bundleId, Long id) {
		ProjectTeam projectTeam = projectTeamFromParams()
		if (!projectTeam) return

		MoveBundle moveBundle = MoveBundle.get(bundleId)

		[projectTeamInstance : projectTeam, bundleInstance: moveBundle,
		 availableStaff: partyRelationshipService.getAvailableTeamMembers(moveBundle.projectId, projectTeam),
		 teamMembers: partyRelationshipService.getBundleTeamMembers(projectTeam)]
	}

	@HasPermission(Permission.ProjectTeamEdit)
	def update(Long bundleId, Long id) {

		// TODO : Security : Need to check to see if the person is associated to the project and has permissions.

		ProjectTeam projectTeam = projectTeamFromParams()
		if (!projectTeam) return

		List teamMemberIds = params.list('teamMembers')

		projectTeam.properties = params
		if (!projectTeam.hasErrors() && projectTeam.save(flush: true)) {

			PartyRelationship.executeUpdate('''\
				delete from PartyRelationship
				where partyRelationshipType.id = 'PROJ_TEAM'
				  and partyIdFrom.id = : projectTeamId
				  and roleTypeCodeFrom.id = 'TEAM'
			''', [projectTeamId: projectTeam.id])

			partyRelationshipService.createBundleTeamMembers(projectTeam, teamMemberIds)
			flash.message = "ProjectTeam $projectTeam updated"
			redirect(action: 'show', id: projectTeam.id, params: [bundleId: bundleId])
		}
		else {
			render view: 'edit', model: modelForCreateOrEdit(projectTeam, MoveBundle.get(bundleId), teamMemberIds)
		}
	}

	@HasPermission(Permission.ProjectTeamCreate)
	def create() {
		MoveBundle moveBundle = MoveBundle.get(params.bundleId)
		[projectTeamInstance: new ProjectTeam(params), bundleInstance: moveBundle,
		 availableStaff: partyRelationshipService.getProjectStaff(moveBundle.projectId)]
	}

	@HasPermission(Permission.ProjectTeamCreate)
	def save(Long bundleId) {
		MoveBundle moveBundle = MoveBundle.get(bundleId)
		ProjectTeam projectTeam = new ProjectTeam(params)
		List teamMemberIds = params.list('teamMembers')

		if (projectTeam.hasErrors() || !projectTeam.save()) {
			render view: 'create', model: modelForCreateOrEdit(projectTeam, moveBundle, teamMemberIds)
			return
		}

		partyRelationshipService.createBundleTeamMembers(projectTeam, teamMemberIds)
		flash.message = "ProjectTeam $projectTeam created"
		redirect(action: 'show', id: projectTeam.id, params: [bundleId: bundleId])
	}

	private ProjectTeam projectTeamFromParams() {
		ProjectTeam projectTeam = ProjectTeam.get(params.id)
		if (projectTeam) {
			projectTeam
		}
		else {
			flash.message = "ProjectTeam not found with id $params.id"
			redirect action: 'list', params: [bundleId: params.bundleId]
			null
		}
	}

	private Map modelForCreateOrEdit(ProjectTeam projectTeam, MoveBundle moveBundle, List teamMemberIds) {
		[projectTeamInstance: projectTeam, bundleInstance: moveBundle,
		 availableStaff: partyRelationshipService.getAvailableProjectStaff(moveBundle.project, teamMemberIds),
		 teamMembers: partyRelationshipService.getProjectTeamStaff(moveBundle.project, teamMemberIds)]
	}
}
