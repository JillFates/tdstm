import com.tdssrc.grails.GormUtil

class DomainUpdateException extends RuntimeException {

	public DomainUpdateException() {
		super()
	}
	public DomainUpdateException(String message) {
		super(message)
	}
	public DomainUpdateException(groovy.lang.GString message) {
		super(message.toString())
	}

	public DomainUpdateException(message, Object domainObj) {
		super( (message + " : " + GormUtil.allErrorsString(domainObj)).toString() )
	}
}