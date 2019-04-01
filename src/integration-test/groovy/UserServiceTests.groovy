import com.tdssrc.grails.TimeUtil
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.person.Person
import net.transitionmanager.security.UserLogin
import net.transitionmanager.security.SecurityService
import net.transitionmanager.person.UserService
import spock.lang.Specification
import spock.lang.Stepwise

@Stepwise
@Integration
@Rollback
class UserServiceTests extends Specification {
    private static final int INACTIVITY_DAYS_OFFSET = 10
    SecurityService securityService
    UserService userService


    private UserLogin createUserAccount(String username = null, Date createdDate = null, Date lastLogin = null) {
        Date now = TimeUtil.nowGMT()
        Person person = new Person(firstName: 'Jack', lastName: 'Rabbit', staffType: 'Salary').save()
        UserLogin userLogin = new UserLogin(username: username ?: 'bendover', password: 'guessit', person: person, active: 'Y',
                createdDate: createdDate,
                expiryDate: now + 1,
                lastLogin: lastLogin,
                lockedOutUntil: null
        ).save()
        return userLogin
    }

    def setup() {
        securityService.metaClass.getLoginConfig = { [inactiveDaysLockout: 60, inactivityWhitelist: ['whitelistedusername']] }
    }

    void 'Scenario 1: A previously active user successfully logs in'() {
        given:
            def now = TimeUtil.nowGMT()
            def userLogin = createUserAccount(null, now, now)
            def lockedOut
        when: 'When the user logs in'
            lockedOut = securityService.shouldLockoutAccount(userLogin)
        then: 'Then the login should be successful'
            !lockedOut
        then: 'The user\'s lastLogin time is updated to the current time'
            println "Account last login: " + userLogin.lastLogin
            userLogin.lastLogin != null
    }

    void 'Scenario 2: A new user successfully logs in'() {
        given:
            def now = TimeUtil.nowGMT()
            def userLogin = createUserAccount(null, now, now)
            def lockedOut
        when: 'When the user logs in'
            lockedOut = securityService.shouldLockoutAccount(userLogin)
        then: 'Then the login should be successful'
            !lockedOut
        then: 'The user\'s lastLogin time is updated to the current time'
            println "Account last login: " + userLogin.lastLogin
            userLogin.lastLogin != null

    }

    void 'Scenario 3: A previously active user logs in after exceeding the inactivity period'() {
        given:
            def now = TimeUtil.nowGMT()
            def inactiveDaysLockout = (securityService.getLoginConfig().inactiveDaysLockout as int) + INACTIVITY_DAYS_OFFSET
            def userLogin = createUserAccount(null, now, now - inactiveDaysLockout)
            def lockedOut
        when: 'When the user logs in'
            lockedOut = securityService.shouldLockoutAccount(userLogin)
            if (lockedOut) {
                userService.lockoutAccountByInactivityPeriod(userLogin)
            }
        then: 'Then the login should fail'
            lockedOut
        then: 'UserLogin.lockedOutUntil property should be set to now + 100 years'
            println "Account locked out until: " + userLogin.lockedOutUntil
            userLogin.lockedOutUntil != null

    }

    void 'Scenario 4: New user logs in after inactivity period since account was created which exceeds the inactiveDaysLockout setting'() {
        given:
            def now = TimeUtil.nowGMT()
            def inactiveDaysLockout = (securityService.getLoginConfig().inactiveDaysLockout as int) + INACTIVITY_DAYS_OFFSET
            def userLogin = createUserAccount(null, now - inactiveDaysLockout, null)
            def lockedOut
        when: 'When the user logs in'
            lockedOut = securityService.shouldLockoutAccount(userLogin)
            if (lockedOut) {
                userService.lockoutAccountByInactivityPeriod(userLogin)
            }
        then: 'Then the login should fail'
            lockedOut
        then: 'UserLogin.lockedOutUntil property should be set to now + 100 years'
            println "Account locked out until: " + userLogin.lockedOutUntil
            userLogin.lockedOutUntil != null

    }

    void 'Scenario 5: A previously active whitelisted user logs in where their inactive days exceeded the inactiveDaysLockout setting'() {
        given:
            def now = TimeUtil.nowGMT()
            def inactiveDaysLockout = (securityService.getLoginConfig().inactiveDaysLockout as int) + INACTIVITY_DAYS_OFFSET
            def inactivityWhitelist = securityService.getLoginConfig().inactivityWhitelist as List<String>
            def whitelistedUsername = inactivityWhitelist.get(0)
            def userLogin = createUserAccount(whitelistedUsername, now - inactiveDaysLockout, now - inactiveDaysLockout)
            def lockedOut
        when: 'When the user logs in'
            lockedOut = securityService.shouldLockoutAccount(userLogin)
        then: 'Then the login will succeed'
            !lockedOut
        then: 'UserLogin.lockedOutUntil property should be set to null if set'
            println "Account locked out until: " + userLogin.lockedOutUntil
            userLogin.lockedOutUntil == null

    }

    void 'Scenario 6: A new whitelisted user logs in after the elapsed time since their account was created exceeds the inactiveDaysLockout setting'() {
        given:
            def now = TimeUtil.nowGMT()
            def inactiveDaysLockout = (securityService.getLoginConfig().inactiveDaysLockout as int) + INACTIVITY_DAYS_OFFSET
            def inactivityWhitelist = securityService.getLoginConfig().inactivityWhitelist as List<String>
            def whitelistedUsername = inactivityWhitelist.get(0)
            def userLogin = createUserAccount(whitelistedUsername, now - inactiveDaysLockout, null)
            def lockedOut
        when: 'When the user logs in'
            lockedOut = securityService.shouldLockoutAccount(userLogin)
        then: 'Then the login will succeed'
            !lockedOut
        then: 'UserLogin.lockedOutUntil property should be set to null if set'
            println "Account locked out until: " + userLogin.lockedOutUntil
            userLogin.lockedOutUntil == null

    }

}
