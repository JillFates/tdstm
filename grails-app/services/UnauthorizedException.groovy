
class UnauthorizedException extends RuntimeException {

	public UnauthorizedException() {
		super()
	}

	public UnauthorizedException(String message) {
		super(message)
	}

	public UnauthorizedException(groovy.lang.GString message) {
		super(message.toString())
	}

	
}
