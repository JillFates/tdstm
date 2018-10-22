package net.transitionmanager.service

import com.tdssrc.grails.GormUtil
import grails.transaction.Transactional
import net.transitionmanager.command.RoleTypeCommand
import net.transitionmanager.domain.RoleType
import org.grails.datastore.mapping.query.api.Criteria

class RoleTypeService implements ServiceMethods {

	/**
	 * Get a list of role types where type is TEAM
	 *
	 * @return a list of RoleType
	 */
	List<RoleType> list() {
		Criteria query = RoleType.where {
			type == RoleType.TEAM
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
			throw new EmptyResultException("Role type not found with id: ${roleTypeId}.")
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
	RoleType update(RoleTypeCommand roleTypeCommand) {
		RoleType roleTypeInstance = getById(roleTypeCommand.id, true)
		roleTypeCommand.populateDomain(roleTypeInstance, false, ['type', 'code'])

		if (roleTypeInstance.hasErrors() || !roleTypeInstance.save()) {
			throw new DomainUpdateException('Unable to update role type ' + GormUtil.allErrorsString(roleTypeInstance))
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
	RoleType save(RoleTypeCommand roleTypeCommand) {
		RoleType roleTypeInstance = new RoleType()
		roleTypeCommand.populateDomain(roleTypeInstance)

		if (roleTypeInstance.hasErrors() || !roleTypeInstance.save()) {
			throw new DomainUpdateException('Unable to update role type ' + GormUtil.allErrorsString(roleTypeInstance))
		}
		return roleTypeInstance
	}
}
