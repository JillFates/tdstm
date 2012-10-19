
class ExceptionDates {
	
	Date exceptionDay
	Person person
	
	static constraints = {
		exceptionDay( nullable:true )
		person( nullable:true )
	}

}
