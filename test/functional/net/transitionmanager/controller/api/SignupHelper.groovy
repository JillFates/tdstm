package net.transitionmanager.controller.api

/**
 * ProjectTestHelper is a helper class that can be used by the test cases to fetch, create and do other
 * helpful data preparation necessary to be used by the integration tests. The intent of these helper classes
 * is to do the heavy lifting for the ITs so that they an focus on the good stuff.
 *
 * These helpers should not rely on any pre-existing data and will generate anything that is necessary. At least
 * that's the idea...
 */

import com.tdsops.common.grails.ApplicationContextHolder
import groovy.time.TimeCategory
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyRole
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.SecurityService
import org.apache.commons.lang.RandomStringUtils as RSU

class SignupHelper {
    PersonService personService
    SecurityService securityService
    PartyRelationshipService partyRelationshipService

    Long adminPersonId = 100

    SignupHelper() {
        personService = ApplicationContextHolder.getService('personService')
        securityService = ApplicationContextHolder.getService('securityService')
        partyRelationshipService = ApplicationContextHolder.getService('partyRelationshipService')
        assert (personService instanceof PersonService)
        assert (securityService instanceof SecurityService)
        assert (partyRelationshipService instanceof PartyRelationshipService)
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
        Map personMap = [firstName:"Test ${new Date()}", lastName: 'User', active:'Y', function: teams ]

        // Apply any changes passed into the method
        if (personProps) {
            personMap << personProps
        }

        Person person = personService.savePerson(personMap, currentPerson, company.id, project, true)
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
        person.save(failOnError:true)

        return person
    }

    /**
     * Used to create the person that will be the staff of a company. The first, middle or last name can be assigned
     * but if not then a random string is set for the properties.
     * @param company - the company to assign the staff to
     * @param firstName
     * @param middleName
     * @param lastName
     * @return the person that was created and assigned to the company
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
    UserLogin createUserLogin(Person person, List roles=[]) {
        Map map = [
                username: RSU.randomAlphabetic(10),
                active: 'Y',
                expiryDate: (new Date() + 365),
                person: person
        ]
        UserLogin u = new UserLogin(map)
        u.save(failOnError:true)
        if (roles.size()) {
            securityService.assignRoleCodes(person, roles)
        }

        return u
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

        this.createUserLogin(admin, ['ADMIN'])

        return admin
    }

    void savePersonWithRoles(String username, String password, List<String> roles) {

        def company = PartyGroup.get(18)

        assert company

        Person person = createStaff(company)
        person.save(failOnError: true)

        assert person

        def expiryDate = new Date()
        use(TimeCategory) {
            expiryDate += 365.day
        }
        Map map = [
                username: username,
                active: 'Y',
                expiryDate: expiryDate,
                person: person,
                passwordNeverExpires: false,
                forcePasswordChange: 'N',
                passwordExpirationDate: null
        ]
        UserLogin u = new UserLogin(map)
        u.save(failOnError:true)
        if (roles) {
            securityService.assignRoleCodes(person, roles)
        }

        u = UserLogin.findByUsername(username)
        u.applyPassword(password)
        u.save(flush: true, failOnError: true)

        person
    }

    void deleteUserLoginByUsername(String username) {
        def userLogin = UserLogin.findByUsername(username)
        def person = userLogin.person
        def partyRoles = PartyRole.findAllByParty(person)
        partyRoles*.delete()
        def users = UserLogin.findAllByPerson(person)
        users*.delete()
        person.delete(flush: true, failOnError: true)
        //userLogin.delete(flush: true, failOnError: true)
    }

    void disablePasswordExpirationByUsername(String username) {
        UserLogin.executeUpdate("update UserLogin set forcePasswordChange = 'N' where username = '${username}'")
        UserLogin.executeUpdate("update UserLogin set passwordNeverExpires = true where username = '${username}'")
        UserLogin.executeUpdate("update UserLogin set passwordExpirationDate = null where username = '${username}'")
    }
}

