package net.transitionmanager.service

import com.tds.asset.AssetComment
import com.tdsops.tm.enums.domain.UserPreferenceEnum
import com.tdssrc.grails.GormUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.AppMoveEvent
import net.transitionmanager.domain.MoveEvent
import net.transitionmanager.domain.MoveEventStaff
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.RoleType
import org.springframework.jdbc.core.JdbcTemplate

@Transactional
class MoveEventService implements ServiceMethods {

	JdbcTemplate jdbcTemplate

	MoveEvent create(Project project, String name) {
		MoveEvent me = new MoveEvent([project:project, name:name])
		save me, true
		me
	}

	/**
	 * This function using to verify events for requested project
	 * @param reqEventIds : reqEventIds is list of requested id from browser
	 * @param project : project list
	 * @return
	 */
	@Transactional(readOnly = true)
	def verifyEventsByProject(List reqEventIds, project){
		def nonProjEventIds
		if(project){
			def projEventsId = MoveEvent.findAllByProject( project )?.id
			//Now Checking whether requested event list has any bad or non project associated id.
			nonProjEventIds = reqEventIds - projEventsId
			if(nonProjEventIds){
				log.error "Event ids $nonProjEventIds is not associated with current project.\
						    Kindly request for project associated  Event ids ."
			}
		}
		return nonProjEventIds
	}

	/**
	 * Used to retrieve a person to a move event for a specified team
	 * @param moveEvent - the move event to search for the member
	 * @param person - the team member on the event
	 * @param teamRoleType - a valid TEAM role
	 * @return An error message if unable to create the team otherwise null
	 */
	@Transactional(readOnly = true)
	MoveEventStaff getTeamMember(MoveEvent moveEvent, Person person, RoleType teamRoleType) {
		String query = 'from MoveEventStaff mes where mes.moveEvent=:me and mes.person=:p and mes.role=:teamRole'
		MoveEventStaff.find(query, [me:moveEvent, p:person, teamRole:teamRoleType] )
	}

	/**
	 * Used to assign a person to a move event for a specified team
	 * @param moveEvent - the move event to assign the person to
	 * @param person - the new team member for the event
	 * @param teamRoleType - a valid TEAM role
	 * @return An error message if unable to create the team otherwise null
	 */
	MoveEventStaff addTeamMember(MoveEvent moveEvent, Person person, RoleType teamRoleType) {
		assert moveEvent
		assert person

		if (teamRoleType.type != RoleType.TEAM) {
			throw new InvalidParamException('Invalid team code $teamRoleType.id was specified')
		}

		// Try finding the assignment first
		MoveEventStaff mes = getTeamMember(moveEvent, person, teamRoleType)

		if (! mes) {
			// TODO : JPM 4/2016 : addTeamMember() should validate that the person is associated with the project

			// Now create the MoveEventStaff record
			mes = new MoveEventStaff()
			mes.person = person
			mes.moveEvent = moveEvent
			mes.role = teamRoleType
			if (! mes.save(flush:true)) {
				log.error "addTeamMember() failed to create MoveEventStaff($person.id, $moveEvent.id, $teamCode) : ${GormUtil.allErrorsString(moveEventStaff)}"
				throw new DomainUpdateException('An error occurred while assigning person to the event')
			}
		}

		return mes
	}

	/**
	 * Used to assign a person to a move event for a specified team
	 * @param moveEvent - the move event to assign the person to
	 * @param person - the new team member for the event
	 * @param teamRoleType - a valid TEAM role
	 * @return An error message if unable to create the team otherwise null
	 */
	MoveEventStaff addTeamMember(MoveEvent moveEvent, Person person, String teamCode) {
		RoleType teamRoleType = teamRoleType(teamCode)
		if (!teamRoleType) {
			throw new InvalidParamException("Invalid team code '$teamCode' was specified")
		}
		return addTeamMember(moveEvent, person, teamRoleType)
	}

	/**
	 * Used to retrieve a TEAM RoleType based on the code
	 * @param teamCode - the TEAM string code
	 * @return the TEAM RoleType if found otherwise null
	 */
	@Transactional(readOnly = true)
	RoleType teamRoleType(String teamCode) {
		RoleType.findByIdAndType(teamCode, RoleType.TEAM)
	}

	/**
	 * Used to unassign a person from a move event for a specified team
	 * @param moveEvent - the move event to unassign the person from
	 * @param person - the team member associated to the event
	 * @param teamRoleType - the team role
	 * @return true if the team member was deleted or false if not found
	 */
	boolean removeTeamMember(MoveEvent moveEvent, Person person, RoleType teamRoleType) {
		assert moveEvent
		assert person
		boolean status = false

		if (teamRoleType.type != RoleType.TEAM) {
			throw new InvalidParamException("Invalid team code '$teamRoleType.id' was specified")
		}

		// Try finding the assignment first
		MoveEventStaff mes = getTeamMember(moveEvent, person, teamRoleType)
		if (mes) {
			status = mes.delete()
		}
		return status
	}

	/**
	 * Used to unassign a person from a move event for a specified team
	 * @param moveEvent - the move event to unassign the person from
	 * @param person - the team member associated to the event
	 * @param teamCode - the team role code
	 * @return true if the team member was deleted or false if not found
	 */
	MoveEventStaff removeTeamMember(MoveEvent moveEvent, Person person, String teamCode) {
		RoleType teamRoleType = teamRoleType(teamCode)
		if (!teamRoleType) {
			throw new InvalidParamException("Invalid team code '$teamCode' was specified")
		}
		return removeTeamMember(moveEvent, person, teamRoleType)
	}

	/**
	 * This method deletes a MoveEvent and, additionally, performs the following
	 * operations:
	 * - Delete all MoveEventSnapshot for the event.
	 * - Delete all news for the event.
	 * - Nulls out reference to the event in Move Bundle.
	 * - Deletes User References pointing to this event.
	 * - Deletes all AppMoveEvents for the event.
	 * - Nulls out references to this event for tasks and comments.
	 * @param moveEvent
	 */
	void deleteMoveEvent(MoveEvent moveEvent) {
		if (moveEvent) {
			// Deletes MoveEventSnapshots for this event.
			jdbcTemplate.update('DELETE FROM move_event_snapshot             WHERE move_event_id = ?', moveEvent.id)
			// Deletes all news for this event.
			jdbcTemplate.update('DELETE FROM move_event_news                 WHERE move_event_id = ?', moveEvent.id)
			// Nulls out the reference to this event in MoveBundle.
			jdbcTemplate.update('UPDATE move_bundle SET move_event_id = NULL WHERE move_event_id = ?', moveEvent.id)
			// Deletes all UserPreference pointing to this event.
			jdbcTemplate.update('DELETE FROM user_preference WHERE preference_code = ? and value = ?', UserPreferenceEnum.MOVE_EVENT as String, moveEvent.id)
			// Deletes all AppMoveEvent related to this event.
			AppMoveEvent.executeUpdate('DELETE AppMoveEvent WHERE moveEvent.id =  ?', [moveEvent.id])
			// Nulls out references to this event in comments and tasks.
			AssetComment.executeUpdate("UPDATE AssetComment SET moveEvent = NULL WHERE moveEvent.id = ?", [moveEvent.id])
			// Deletes the event.
			moveEvent.delete()
		}
	}
}
