package com.tds.test

/**
 * For integration testing.
 */
class TestDomain {

	String name
	String color
	String label
	String note
	Integer age
	Integer score

	static constraints = {
		// Defaults:
		//    nullable false
		//    blank true
		name blank: false, unique: true
		color inList: ['red', 'green', 'blue', 'yellow', 'orange']
		label nullable: true
		age nullable: true
		score range: 1..5
	}

	static mapping = {
		columns {
			name sqlType: 'varchar(30)'
			color sqlType: 'varchar(10)'
			label sqlType: 'varchar(10)'
		}
	}
}
