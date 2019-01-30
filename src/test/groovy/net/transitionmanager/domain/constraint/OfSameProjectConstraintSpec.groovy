package net.transitionmanager.domain.constraint

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdssrc.grails.StringUtil
import grails.test.mixin.Mock
import net.transitionmanager.domain.Credential
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import net.transitionmanager.domain.Timezone
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.validation.Errors
import org.springframework.validation.MapBindingResult
import spock.lang.Specification

@Mock([PartyGroup, Project, Provider, Credential, Timezone])
class OfSameProjectConstraintSpec extends Specification {

	OfSameProjectConstraint ofSameProjectConstraint
	Credential credential1
	Credential credential2

	def setup() {
		ofSameProjectConstraint = new OfSameProjectConstraint(this.class, 'project', true, null)

		Project project1 = [
			projectCode   : RandomStringUtils.randomAlphabetic(10),
			name          : 'Project 1',
			description   : 'Test project created by the ProjectTestHelper',
			startDate     : new Date(),
			completionDate: new Date() + 30,
			guid          : StringUtil.generateGuid(),
			workflowCode  : 'STD_PROCESS',
			timezone      : Timezone.findByCode('GMT'),
			guid          : StringUtil.generateGuid(),
			client        : [name: 'something'] as PartyGroup
		] as Project

		project1.save(failOnError: true)

		Project project2 = [
			id            : 2L,
			projectCode   : RandomStringUtils.randomAlphabetic(10),
			name          : 'Project 2',
			description   : 'Test project created by the ProjectTestHelper',
			startDate     : new Date(),
			completionDate: new Date() + 30,
			guid          : StringUtil.generateGuid(),
			workflowCode  : 'STD_PROCESS',
			timezone      : Timezone.findByCode('GMT'),
			guid          : StringUtil.generateGuid(),
			client        : [name: 'something else'] as PartyGroup
		] as Project

		project2.save(failOnError: true)

		Provider provider1 = [
			name       : RandomStringUtils.randomAlphabetic(10),
			comment    : 'Test comment',
			description: 'Test description',
			project    : project1
		] as Provider

		Provider provider2 = [
			name       : RandomStringUtils.randomAlphabetic(10),
			comment    : 'Test comment',
			description: 'Test description',
			project    : project2
		] as Provider

		//A credential that has a provider, that has a project, that matches the project, of the credential.
		credential1 = [
			project      : project1,
			provider     : provider1,
			authMethod   : AuthenticationMethod.BASIC_AUTH,
			sessionName  : '',
			validationExp: '',
			authUrl      : '',
			renewUrl     : '',
			httpMethod   : null,
			requestMode  : null
		] as Credential

		//A credential that has a provider, that has a project, that doesn't match, the project of the credential.
		credential2 = [
			project      : project1,
			provider     : provider2,
			authMethod   : AuthenticationMethod.BASIC_AUTH,
			sessionName  : '',
			validationExp: '',
			authUrl      : '',
			renewUrl     : '',
			httpMethod   : null,
			requestMode  : null
		] as Credential
	}

	void 'Test validate constraint valid provider'() {
		when: 'Validating a credentials provider using ofSameProjectConstraint'
			Errors errors = new MapBindingResult([:], 'test')
			ofSameProjectConstraint.validate(credential1, credential1.provider, errors)
		then: 'there are no errors returned'
			!errors.errorCount
	}

	void 'Test validate constraint invalid provider'() {
		when: 'Validating a credentials provider using ofSameProjectConstraint'
			Errors errors = new MapBindingResult([:], 'test')
			ofSameProjectConstraint.validate(credential2, credential2.provider, errors)
		then: 'there are errors returned'
			errors.errorCount
	}

}