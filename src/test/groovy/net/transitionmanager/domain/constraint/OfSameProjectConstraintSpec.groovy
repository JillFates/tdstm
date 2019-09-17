package net.transitionmanager.domain.constraint

import com.tdsops.tm.enums.domain.AuthenticationMethod
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import net.transitionmanager.action.Credential
import net.transitionmanager.action.Provider
import net.transitionmanager.common.Timezone
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.project.Project
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.validation.Errors
import org.springframework.validation.MapBindingResult
import spock.lang.Specification

class OfSameProjectConstraintSpec extends Specification implements DataTest {

	OfSameProjectConstraint ofSameProjectConstraint
	Credential credential1
	Credential credential2

	void setupSpec(){
		mockDomains PartyGroup, Project, Provider, Credential, Timezone
	}

	def setup() {
		ofSameProjectConstraint = new OfSameProjectConstraint(this.class, 'project', true, null)

		Project project1 = [
			projectCode   : RandomStringUtils.randomAlphabetic(10),
			name          : 'Project 1',
			description   : 'Test project created by the ProjectTestHelper',
			startDate     : new Date(),
			completionDate: new Date() + 30,
			guid          : StringUtil.generateGuid(),
			timezone      : Timezone.findByCode('GMT'),
			guid          : StringUtil.generateGuid(),
			client        : [name: 'something'] as PartyGroup
		] as Project

		project1.save()

		Project project2 = [
			id            : 2L,
			projectCode   : RandomStringUtils.randomAlphabetic(10),
			name          : 'Project 2',
			description   : 'Test project created by the ProjectTestHelper',
			startDate     : new Date(),
			completionDate: new Date() + 30,
			guid          : StringUtil.generateGuid(),
			timezone      : Timezone.findByCode('GMT'),
			guid          : StringUtil.generateGuid(),
			client        : [name: 'something else'] as PartyGroup
		] as Project

		project2.save()

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
