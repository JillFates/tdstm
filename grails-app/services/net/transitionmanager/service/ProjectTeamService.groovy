package net.transitionmanager.service

import grails.transaction.Transactional
import net.transitionmanager.domain.PartyRelationship
import net.transitionmanager.domain.ProjectTeam

@Transactional
class ProjectTeamService {

	PartyRelationshipService partyRelationshipService

	ProjectTeam updateWithPartyRelationship(ProjectTeam projectTeam, List teamMemberIds) {

		projectTeam.save()

		PartyRelationship.executeUpdate('''\
				delete from PartyRelationship
				where partyRelationshipType.id = 'PROJ_TEAM'
				  and partyIdFrom.id = : projectTeamId
				  and roleTypeCodeFrom.id = 'TEAM'
			''', [projectTeamId: projectTeam.id])

		partyRelationshipService.createBundleTeamMembers(projectTeam, teamMemberIds)

		return projectTeam
	}

	ProjectTeam createWithPartyRelationship(ProjectTeam projectTeam, List teamMemberIds) {

		projectTeam.save()
		partyRelationshipService.createBundleTeamMembers(projectTeam, teamMemberIds)
		return projectTeam
	}
}
