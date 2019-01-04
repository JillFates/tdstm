package net.transitionmanager.command

import net.transitionmanager.domain.Person
/**
 * PersonCommand is to be used with the creation and editing of Person domain
 */
@grails.validation.Validateable
class PersonCO implements CommandObject {

	String firstName
	String middleName
	String lastName
	String nickName
	String active
	String title
	String email
	String department
	String location
	String stateProv
	String country
	String workPhone
	String mobilePhone
	String keyWords
	String tdsNote
	String tdsLink
	String staffType
	Integer travelOK

	static constraints = {
		importFrom Person, include: [
			'firstName',
			'middleName',
			'lastName',
			'nickName',
			'active',
			'title',
			'email',
			'department',
			'location',
			'stateProv',
			'country',
			'workPhone',
			'mobilePhone',
			'keyWords',
			'tdsNote',
			'tdsLink',
			'staffType',
			'travelOK' ]
	}
}
