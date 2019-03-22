package test.helper


import com.tdssrc.grails.GormUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import net.transitionmanager.domain.Party
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.PartyRelationshipService

/**
 * ProjectTestHelper is a helper class that can be used by the test cases to fetch, create and do other
 * helpful data preparation necessary to be used by the integration tests. The intent of these helper classes
 * is to do the heavy lifting for the ITs so that they an focus on the good stuff.
 *
 * These helpers should not rely on any pre-existing data and will generate anything that is necessary. At least
 * that's the idea...
 */

import net.transitionmanager.service.PersonService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang3.RandomStringUtils as RSU
import org.apache.commons.lang3.RandomStringUtils

@Transactional
class PersonTestHelper {
	PersonService personService
	SecurityService securityService
	PartyRelationshipService partyRelationshipService
	UserPreferenceService userPreferenceService

	Long adminPersonId = 100

	PersonTestHelper() {
		personService = Holders.applicationContext.getBean('personService')
		securityService = Holders.applicationContext.getBean('securityService')
		partyRelationshipService = Holders.applicationContext.getBean('partyRelationshipService')
		userPreferenceService = Holders.applicationContext.getBean('userPreferenceService')
	}

	/**
	 * Creates test person assigning them to the company and team(s) specified
	 * @param currentPerson - the person that is attempting to create the person
	 * @param company - the company that the person should belong to
	 * @param project - the project to assign the person to (optional default null)
	 * @param personProps - a map of person property settings to override the defaults (optional)
	 * @param teams - a list of teams to add the person to (default ['PROJ_MGR'])
	 * @return the newly created person
	 */
	Person createPerson(Person currentPerson, PartyGroup company, Project project=null, Map personProps=null, List teams=['PROJ_MGR'], roles=null) {
		Map personMap = [active:'Y', function: teams ]

		// Apply any changes passed into the method
		if (personProps) {
			personMap << personProps
		}

		// Make sure that all of the properties are set to something
		['firstName', 'middleName', 'lastName'].each {prop ->
			if (! personMap?.containsKey(prop)) {
				personMap[prop] = RandomStringUtils.randomAlphabetic(12)
			}
		}

		if (! personProps?.containsKey('email')) {
			personMap.email = personMap.firstName +
				(personMap.middleName ? ".${personMap.middleName}" : '') +
				(personMap.lastName ? ".${personMap.lastName}" : '') +
				'@example.com'
		}

		Party companyParty = Party.get(company.id)
		Person person = new Person(personMap)
		person.save(failOnError: true, flush:true)

		if(project) {
			personService.addToProjectSecured(project, person)
		}

		partyRelationshipService.addCompanyStaff(companyParty, person)


		assert person

		if (roles) {
			if (roles instanceof String) [
				roles = [roles]
			]
			securityService.assignRoleCodes(person, roles)
		}

		return person
	}

	/**
	 * Used to create a person
	 * @param firstName
	 * @param middleName
	 * @param lastName
	 * @return the person that was created and assigned to the company
	 */
	Person createPerson(String firstName=null, String middleName=null, String lastName=null, String email=null) {
		if (firstName == null) firstName = RSU.randomAlphabetic(10)
		if (middleName == null) middleName = RSU.randomAlphabetic(10)
		if (lastName == null) lastName = RSU.randomAlphabetic(10)
		if (email == null) email = "$firstName.$lastName@" + RSU.randomAlphabetic(10) + '.com'

		Person person = new Person([firstName:firstName, middleName: middleName, lastName: lastName, email:email] )
		personService.save(person, true)
	//	person.save(failOnError:true)

		return person
	}

	/**
	 * @deprecated This method has been replaced by createPerson method.
	 *
	 * Used to create the person that will be the staff of a company. The first, middle or last name can be assigned
	 * but if not then a random string is set for the properties.
	 * @param company - the company to assign the staff to
	 * @param firstName
	 * @param middleName
	 * @param lastName
	 * @return the person that was created and assigned to the company
	 *
	 */
	Person createStaff(PartyGroup company, String firstName=null, String middleName=null, String lastName=null, String email=null) {
		Person staff = createPerson(firstName, middleName, lastName, email)
		partyRelationshipService.addCompanyStaff(company, staff)
		return staff
	}

	/**
	 * Used to create a user for a person and optionally assign roles
	 * @param person - the person to create the user for
	 * @param roles - a list of role codes to create (optionally)
	 * @return the newly created UserLogin
	 */
	UserLogin createUserLoginWithRoles(Person person, List roles=[], Project project=null, Boolean signIn=false) {

		UserLogin u
		if (project) {
			u = createUserLogin(person, [:], project, signIn)
		} else {
			u = createUserLogin(person, [:], signIn)
		}

		if (roles.size()) {
			securityService.assignRoleCodes(person, roles)
		}

		return u
	}


	/**
	 * Used to create a UserLogin for a person with optional properties and set as default project
	 * @param person - the person for whom to create the UserLogin
	 * @param props - a map containing the various UserLogin properties to be set
	 * @param project - a project object to associate the user to as the default project
	 * @param signIn - a flag to control if the user will be logged (default false)
	 * @return a newly minted UserLogin object
	 */
	UserLogin createUserLogin(Person person, Map props=null, Project project, Boolean signIn=false) {
		UserLogin user = createUserLogin(person, props, signIn)
		userPreferenceService.setCurrentProjectId(user, project.id)
		return user
	}

	/**
	 * Used to create a UserLogin for a person with optional properties
	 * @param person - the person for whom to create the UserLogin
	 * @param props - a map containing the various UserLogin properties to be set
	 * @param signIn - a flag to control if the user will be logged (default false)
	 * @return a newly minted UserLogin object
	 */
	UserLogin createUserLogin(Person person, Map props=null, Boolean signIn=false) {
		UserLogin user = new UserLogin(person:person)
		user.username = RandomStringUtils.randomAlphabetic(12)
		if (props) {
			props.each { prop, value -> user[prop]=value}
		}
		if (!user.username) {
			user.username = RandomStringUtils.randomAlphabetic(12)
		}
		if (! user.active) {
			user.active = 'Y'
		}
		if (! user.expiryDate) {
			user.expiryDate = new Date()
		}

		if (! user.save(flush:true)) {
			throw new RuntimeException("Unable to save new UserLogin due to " + GormUtil.allErrorsString(user))
		}

		if (signIn) {
            securityService.assumeUserIdentity(user.username, false)
		}

		return user
	}

	/**
	 * Used to get the Admin person to use for tests
	 * @return a Person that has Administration privileges
	 */
	Person getAdminPerson(PartyGroup company) {
		if (! company) {
			// Yuk -- hard-coding TDS company for tests - bad form...
			company = PartyGroup.get(18)
		}
		Person admin = this.createStaff(company)

		assert admin

		this.createUserLoginWithRoles(admin, ['ROLE_ADMIN'])

		return admin
	}

}