package net.transitionmanager.domain

import com.tdsops.common.security.SecurityUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.PasswordHistory
import net.transitionmanager.service.DomainUpdateException

class UserLogin {

	String username
	String password = ''
	Date createdDate = TimeUtil.nowGMT()
	Date lastLogin
	String active
	Date lastPage
	Date expiryDate
	Date passwordChangedDate
	Date lastModified = TimeUtil.nowGMT()
	/** Used to signal during the login process that the user needs to change their password */
	String forcePasswordChange = 'N'
	/** The GUID use to reference a user that is authenticated by an external authority (e.g. Active Directory or SSO) */
	String externalGuid
	/** Indicates that the user authentication is local (DB) vs external authority (e.g. Active Directory) */
	Boolean isLocal = true

	Date lockedOutUntil
	Date passwordExpirationDate
	Integer failedLoginAttempts = 0
	Boolean passwordNeverExpires = false
	String saltPrefix = ''

	static hasMany = [dataTransferBatch: DataTransferBatch]

	static belongsTo = [person: Person]

	static constraints = {
		// TODO : Add a password constraint on size, and other rules
		active inList: ['Y', 'N']
		createdDate nullable: true
		externalGuid nullable: true
		forcePasswordChange blank: false
		lastLogin nullable: true
		lastPage nullable: true
		lockedOutUntil nullable: true
		password password: true
		passwordChangedDate nullable: true
		passwordExpirationDate nullable: true
		saltPrefix nullable: true
		username blank: false, unique: true, size: 2..50
	}

	static mapping = {
		version false
		autoTimestamp false
		id column: 'user_login_id'
		// TODO - active column should not be varchar(20) as it is only 1 char
		active sqlType: 'varchar(20)'
		externalGuid sqltype: 'varchar(64)'
		forcePasswordChange sqltype: 'char(1)'
		lastModified sqltype: 'DateTime'
		password sqlType: 'varchar(100)' // size must me more than 20 because it will store as hashed code
		passwordChangedDate sqltype: 'DateTime'
		saltPrefix sqltype: 'varchar(32)'
		username sqlType: 'varchar(50)'
	}

	// Transient flag set whenever the password is changed
	boolean passwordWasChanged = false

	static transients = ['currentProject', 'disabled', 'lockedOut', 'passwordWasChanged',
	                     'personDetails', 'securityRoleCodes']

	String toString() {
		username ?: 'Undefined User'
	}

	/*
	 * Render person details as firstName + lastName
	 */
	String getPersonDetails() {
		person?.toString() ?: '**Undefined**'
	}

	// Check if the user has not expired and is flagged as active
	boolean userActive() {
		!hasExpired() && !isDisabled()
	}

	boolean hasExpired() {
		expiryDate <= new Date()
	}

	boolean isDisabled() {
		active != 'Y' || person.active != 'Y'
	}

	boolean isLockedOut() {
		lockedOutUntil?.time > System.currentTimeMillis()
	}

	boolean canResetPasswordByAdmin() {
		isLocal && userActive() && hasEmail()
	}

	boolean canResetPasswordByUser() {
		isLocal && userActive() && hasEmail() && !isLockedOut()
	}

	boolean hasEmail() {
		person?.email
	}

	String getSaltPrefix() {
		if (!saltPrefix && isLocal) {
			saltPrefix = SecurityUtil.randomString(32)
		}
		saltPrefix
	}

	// Hash the password for the user. If the user doesn't have a salt then one will be assigned to the user
	String encryptPassword(String password) {
		SecurityUtil.encrypt(password, getSaltPrefix())
	}

	// Set a cleartext password on the UserLogin that will be hash. Note that the domain will
	// create a password history record automatically in the post insert/update events
	String applyPassword(String unhashedPassword) {
		if (!isLocal) {
			throw new DomainUpdateException('The user password is not managed in the application')
		}
		password = encryptPassword(unhashedPassword)
		failedLoginAttempts = 0
		lockedOutUntil = null
		//We remove the forcePassword Change Flag After the password was set
		forcePasswordChange = 'N'

		return password
	}

	// Used to set a transient flag that indicates that the password has been changed for a LocalAccount user
	void checkForPasswordChange() {
		passwordWasChanged = isLocal && dirtyPropertyNames.contains('password')
		if (passwordWasChanged) {
			passwordChangedDate = new Date()
		}
	}

	// save the password for historical reference
	void savePasswordHistory() {
		if (passwordWasChanged) {
			log.debug "savePasswordHistory() for ${this}"
			PasswordHistory ph = new PasswordHistory(userLogin: this, password: password)
			if (!ph.save(flush: true)) {
				log.error "savePasswordHistory() failed for $username : ${GormUtil.allErrorsString(ph)}"
				throw new RuntimeException('Unable to save password history')
			}
		}
	}

	/**
	 * Compare new cleartext password to the existing hashed password that will optionally check legacy hash method(s)
	 * @param unhashedPassword - the new password to compare to hashed password
	 * @param saltPrefix - the salt String to use as the prefix for hashing the user's password
	 * @param tryLegacy - a flag to control if legacy hash methods should be tried (default true)
	 * @return true if the passwords match otherwise false
	 */
	boolean comparePassword(String unhashedPassword, boolean tryLegacy = true) {
		String newPassword = encryptPassword(unhashedPassword)
		boolean matched = newPassword == password
		if (!matched && tryLegacy) {
			newPassword = SecurityUtil.encryptLegacy(unhashedPassword)
			matched = newPassword == password
		}
		return matched
	}

	List<String> getSecurityRoleCodes() {
		executeQuery('''
			SELECT roleType.id FROM PartyRole
			WHERE party=:party and roleType.type=:type
			ORDER BY roleType.level desc
		''', [party: person, type: RoleType.SECURITY])
	}

	Project getCurrentProject() {
		String prefCodeValue = executeQuery('''
				select value from UserPreference
				where userLogin=? and preferenceCode='CURR_PROJ'
			''',[this], [max: 1])[0]
		Long projectId = NumberUtil.toPositiveLong(prefCodeValue, 0)

		if (projectId) {
			return Project.get(projectId)
		}
	}

	def beforeUpdate = {
		checkForPasswordChange()
		lastModified = TimeUtil.nowGMT()
	}

	def afterInsert = {
		savePasswordHistory()
	}

	def afterUpdate = {
		savePasswordHistory()
	}
}
