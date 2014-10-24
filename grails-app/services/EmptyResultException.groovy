
class EmptyResultException extends RuntimeException {

	public EmptyResultException() {
		super()
	}
		
	public EmptyResultException(String message) {
		super(message)
	}

	public EmptyResultException(groovy.lang.GString message) {
		super(message.toString())
	}

}
