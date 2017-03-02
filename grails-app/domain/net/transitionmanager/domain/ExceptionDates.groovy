package net.transitionmanager.domain

/*
 * ExceptionDates represents dates that a person is unavailable for events
 */
class ExceptionDates {

	Date exceptionDay
	Person person

	static constraints = {
		exceptionDay nullable: false
		person nullable: false, unique: 'exceptionDay'
	}
}
