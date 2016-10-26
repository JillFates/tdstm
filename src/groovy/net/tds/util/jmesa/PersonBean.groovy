package net.tds.util.jmesa

import groovy.transform.CompileStatic

@CompileStatic
class PersonBean implements Serializable {
	private static final long serialVersionUID = 1

	long id
	long userLoginId
	String firstName
	String middleName
	String lastName
	String userLogin
	Date dateCreated
	Date lastUpdated
	Integer modelScore
	String userCompany
}
