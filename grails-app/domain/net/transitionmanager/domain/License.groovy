package net.transitionmanager.domain

/**
 * Created by octavio on 9/20/16.
 */
class License {
	String id = UUID.randomUUID().toString()
	String instalationNum
	String email
	String environment
	String project
	String method = ""
	int    max = 0
	Date   requestDate
	Date   validStart
	Date   validEnd
	String requestNote
	String hash

	static mapping = {
		id 			generator: 'assigned'
		requestNote type:'text'
		hash 		type:'text'
		version 	false
		tablePerHierarchy false
	}

	static constraints = {
		method 		nullable:true
		validStart 	nullable:true
		validEnd 	nullable:true
		requestNote nullable:true
		hash 		nullable:true
	}

	public boolean isActive(){
		return (hash)? true : false
	}
}
