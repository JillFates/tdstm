package net.transitionmanager.security

import com.tdssrc.grails.GormUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.command.TeamCommand
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.EmptyResultException
import net.transitionmanager.service.ServiceMethods
import org.grails.datastore.mapping.query.api.Criteria

/**
 * A service for creating, updating, listing and deleting RoleTypes
 *
 * This is currently only used for Teams, but the service is agnostic, so it could be used in the future for other RoleTypes.
 */
class RoleTypeService implements ServiceMethods {

	/**
	 * Get a list of role types where type is TEAM
	 *
	 * @return a list of RoleType
	 */
	List<RoleType> list(String roleType) {
		Criteria query = RoleType.where {
			type == roleType
		}

		query.order('type', 'asc')
				.order('level', 'desc')
				.order('id', 'asc')

		return query.list()
	}

	/**
	 * Get a role type by id
	 *
	 * @param roleTypeId - role type id
	 * @param throwException - whether to throw an exception if role type is not found
	 * @return a RoleType if found
	 */
	RoleType getById(String roleTypeId, boolean throwException = false) {
		RoleType roleType = RoleType.where { id == roleTypeId }.get()
		if (!roleType && throwException) {
			throw new EmptyResultException("Not found with id: ${roleTypeId}.")
		}

		return roleType
	}

	/**
	 * Test if a role exists
	 *
	 * @param roleTypeId - role type id
	 * @return true if role exists, or false
	 */
	boolean roleTypeExists(String roleTypeId) {
		return RoleType.exists(roleTypeId)
	}

	/**
	 * Delete a role type by id
	 *
	 * @param id - role type id
	 * @return
	 */
	@Transactional
	def delete(String id) {
		RoleType roleType = getById(id, true)
		roleType.delete(flush: true)
	}

	/**
	 * Update a role type in database
	 *
	 * @param roleTypeCommand - role type command
	 * @return and updated instance of RoleType
	 */
	@Transactional
	RoleType update(TeamCommand roleTypeCommand) {
		RoleType roleTypeInstance = getById(roleTypeCommand.id, true)
		roleTypeCommand.populateDomain(roleTypeInstance, false, ['type', 'code'])

		if (!roleTypeInstance.save(failOnError: false)) {
			throw new DomainUpdateException("Unable to update $roleTypeInstance.type ${GormUtil.allErrorsString(roleTypeInstance)}")
		}
		return roleTypeInstance
	}

	/**
	 * Save a new role type in database
	 *
	 * @param roleTypeCommand
	 * @return
	 */
	@Transactional
	RoleType save(TeamCommand roleTypeCommand) {
		RoleType roleTypeInstance = new RoleType()
		roleTypeCommand.populateDomain(roleTypeInstance)

		if (!roleTypeInstance.save(failOnError: false)) {
			throw new DomainUpdateException("Unable to update $roleTypeInstance.type ${GormUtil.allErrorsString(roleTypeInstance)}")
		}
		return roleTypeInstance
	}
}
