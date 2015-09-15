import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.GormUtil
import com.tdsops.common.security.SecurityUtil
import net.transitionmanager.PasswordHistory

class UserLogin {

	// def securityService

	String username
	String password
	Date createdDate = TimeUtil.nowGMT()
	Date lastLogin
	String active
	Date lastPage
	Date expiryDate
	Date passwordChangedDate = TimeUtil.nowGMT()
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
    
	static belongsTo = [ person:Person ]
	static hasMany = [
		dataTransferBatch : DataTransferBatch
	]
	
	static constraints = {
		person( nullable: false )
		username( blank: false, unique:true, size:2..50 )
		// TODO : Add a password constraint on size, and other rules
		password( blank: false, nullable: false, password: true )
		createdDate( nullable: true )
		lastLogin( nullable: true )
		active( nullable:false, inList:['Y', 'N'] )
		lastPage( nullable: true )
		expiryDate( nullable: false )
		passwordChangedDate( nullable: false)
		lastModified( nullable: false )
		isLocal( nullable:false )
		externalGuid( nullable:true, blank:true)
		forcePasswordChange( nullable: false, blank: false)
		lockedOutUntil( nullable:true )
		passwordExpirationDate( nullable:true )
		saltPrefix( nullable: true )
	}

	static mapping  = {
		version false
		autoTimestamp false
		id column:'user_login_id'
		username sqlType: 'varchar(50)'
		password sqlType: 'varchar(100)'  // size must me more than 20 because it will store as encrypted code
		// TODO - active column should not be varchar(20) as it is only 1 char
		active sqlType:'varchar(20)'
		forcePasswordChange sqltype: 'char(1)'
		passwordChangedDate sqltype: 'DateTime'
		lastModified sqltype: 'DateTime'
		externalGuid sqltype: 'varchar(64)'
		saltPrefix sqltype: 'varchar(32)'
	}

	// Transient flag set whenever the password is changed
	boolean passwordWasChanged = false

	static transients = [
		'applyPassword',
		'canResetPasswordByAdmin',
		'canResetPasswordByUser',
		'comparePassword',
		'disabled', 
		'lockedOut', 
		'hasEmail',
		'passwordWasChanged', 
		'personDetails', 
		'userActive'
	]
	
	String toString() {
		return username ?:'Undefined User'
	}

	/*
	 *  Render person details as firstName + lastName
	 */
	def getPersonDetails() {
		return this.person?.toString() ?: '**Undefined**'
	}
	
	// Check if the user has not expired and is flagged as active
	def userActive() {
		return ! hasExpired() && ! isDisabled()
	}
	
	// Used to determine if the user account has expired
	boolean hasExpired() {
		return expiryDate <= new Date()
	}

	// Used to determine if the user or associated person is disabled
	boolean isDisabled() {
		return (active != 'Y' || person.active != 'Y')
	}

	// Used to determine if the user account has been locked out
	boolean isLockedOut() {
		return (lockedOutUntil != null) && ( lockedOutUntil.getTime() > TimeUtil.nowGMT().getTime() )
	}

	// Used to determine if a password reset process be initiated by an administrator
	boolean canResetPasswordByAdmin(Person whom) {
		isLocal && userActive() && hasEmail()
	}

	// Used to determine if a password reset process be initiated by the user himself
	boolean canResetPasswordByUser() {
		isLocal && userActive() && hasEmail() && ! isLockedOut() 
	}

	// Used to determine if the user has an email address
	boolean hasEmail() {
		(person?.email ? true : false)
	}

	// Used to access/create a Salt Prefix for the UserLogin if ever accessed
	String getSaltPrefix() {
		if (! this.saltPrefix && this.isLocal) {
			this.saltPrefix = SecurityUtil.randomString(32)
		}
		this.saltPrefix
	}

	// Used to encrypt the password for the user. If the user doesn't have a salt then one will be assigned to the user
	String encryptPassword(String password) {
		String sp = getSaltPrefix()
		SecurityUtil.encrypt(password, sp)
	}

	// Used to set an unencrypted password on the UserLogin that will be encrypted. Note that the domain will 
	// create a password history record automatically in the post insert/update events
	String applyPassword(String unencryptedPassword) {
		if (! this.isLocal) {
			throw new DomainUpdateException('The user password is not managed in the application')
		}
		this.password = encryptPassword(unencryptedPassword)
		this.forcePasswordChange = 'N'
		this.failedLoginAttempts = 0
		this.lockedOutUntil = null
		return this.password
	}

	// Used to set a transient flag that indicates that the password has been changed for a LocalAccount user
	void checkForPasswordChange() {
		this.passwordWasChanged = (this.isLocal && this.dirtyPropertyNames.contains('password'))
		if (this.passwordWasChanged) {
			this.passwordChangedDate = new Date()
		}
	}

	// Used to save the password for historical reference
	void savePasswordHistory() {
		if (this.passwordWasChanged) {
			log.debug "savePasswordHistory() for ${this.toString()}"
			PasswordHistory ph = new PasswordHistory()
			ph.userLogin = this
			ph.password = this.password
			ph.createdDate = new Date()
			if (! ph.save(flush:true) ) {
				log.error "savePasswordHistory() failed for ${this.username} : " + GormUtil.allErrorsString(ph)
				throw new RuntimeException('Unable to save password history')
			}
		}
	}

	/**
	 * Is used to compare new unencrypted password to the existing encrypted password that will optionally check legacy encryption method(s)
	 * @param unencryptedPswd - the new password to compare to encrypted password
	 * @param saltPrefix - the salt String to use as the prefix for encrypting the user's password
	 * @param tryLegacy - a flag to control if legacy encryption methods should be tried (default true)
	 * @return true if the passwords match otherwise false
	 */
	boolean comparePassword(String unencryptedPswd, boolean tryLegacy = true) {
		boolean matched = false
		String newPswd = encryptPassword(unencryptedPswd)

		matched = newPswd.equals(this.password)
		if (!matched && tryLegacy) {
			newPswd = SecurityUtil.encryptLegacy(unencryptedPswd)
			matched = newPswd.equals(this.password)
		}
		return matched
	}

	// -------------------------------
	// Insert/Update Events
	// -------------------------------
	def beforeInsert = {
		checkForPasswordChange()
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
