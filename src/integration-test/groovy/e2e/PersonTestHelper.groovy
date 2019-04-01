package e2e

import net.transitionmanager.party.PartyGroup
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.project.ProjectService
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired

/**
 * Fetches, creates and does other helpful data preparation in the e2e project integration test *
 * Should not rely on any pre-existing data and will generate anything that is necessary.
 */

class PersonTestHelper extends test.helper.PersonTestHelper {

    @Autowired
    ProjectService projectService

    /**
     * Create a person for E2EProjectSpec by given map
     * @param personData = [firstName: string, middleName: string, lastName: string, email: string, username: string]
     * @return the person
     */
    Person createPerson(Map personData) {
        Person person = new Person([
                firstName: personData.firstName,
                middleName: personData.middleName,
                lastName: personData.lastName,
                email: personData.email,
                username: personData.username
        ])
        person.save(flush: true)
        return person
    }

    /**
     * Used to create the person that will be the staff of a company. The first, middle or last name can be assigned
     * but if not then a random string is set for the properties.
     * @param company - the company to assign the staff to
     * @param personData = [firstName: string, middleName: string, lastName: string, email: string, username: string]
     * @return the person that was created and assigned to the company
     */
    Person createStaff(PartyGroup company, Map personData) {
        Person staff = createPerson(personData)
        staff.partyRelationshipService = partyRelationshipService
        partyRelationshipService.addCompanyStaff(company, staff)
        return staff
    }

    /**
     * Used to create a user for a person and optionally assign roles
     * @param person - the person to create the user for
     * @param roles - a list of role codes to create (optionally)
     * @return the newly created UserLogin
     */
    UserLogin createUserLoginWithRoles(Person person, List roles=[], Project project, String userPassword, Boolean signIn=false) {
        UserLogin user = createUserLogin(person, [:], project, userPassword, signIn)
        if (roles.size()) {
            securityService.assignRoleCodes(person, roles)
        }
        return user
    }

    /**
     * Create a UserLogin for a person with optional properties used by E2EProjectSpec
     * @param person - the person for whom to create the UserLogin
     * @param props - a map containing the various UserLogin properties to be set
     * @param project - a project object to associate the user to as the default project
     * @param userPassword
     * @param signIn - a flag to control if the user will be logged (default false)
     * @return a newly minted UserLogin object
     */
    UserLogin createUserLogin(Person person, Map props=null, Project project, String userPassword, Boolean signIn=false) {
        UserLogin user = new UserLogin(person: person, username: person.email, active: "Y", expiryDate: new Date())
        user.password = userPassword ? user.applyPassword(userPassword) : user.applyPassword(RandomStringUtils.randomAlphabetic(12))

        if (props) {
            props.each { prop, value -> user[prop]=value}
        }

        user.save(flush:true)

        if (signIn) {
            securityService.assumeUserIdentity(user.username, false)
        }

        userPreferenceService.setCurrentProjectId(user, project.id)
        return user
    }

    /**
     * Create a LoginUser from given data in map for E2EProjectSpec, if exists update expiration date adding 30 days
     * @param personData = [firstName: string, middleName: string, lastName: string, email: string,
     * roles: list, password: string]
     * @return the user login
     */
    UserLogin createPersonWithLoginAndRoles(Map personData, Project project){
        UserLogin user = UserLogin.findWhere([username: personData.email])
        if (!user){
            Person person = createPerson(personData)
            partyRelationshipService.addCompanyStaff(projectService.getOwner(project), person)
            user = createUserLoginWithRoles(person, personData.roles, project, personData.password, false)
        }
        user.passwordNeverExpires = true
        user.expiryDate = new Date() + 30
        user.save(flush: true)

        return user
    }
}
