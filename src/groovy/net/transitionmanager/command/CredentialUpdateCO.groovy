package net.transitionmanager.command

import grails.validation.Validateable
import net.transitionmanager.domain.Credential

/**
 * Command object to handle credential form data binding upon updates
 */
@Validateable
class CredentialUpdateCO extends CredentialCreateCO {
	Long id
	Long version

	static constraints = {
		importFrom Credential, include: [
				'name'
		]
		password nullable: true
	}
}
