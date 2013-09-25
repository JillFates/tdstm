/**
 * Class TestDomain
 * Used by the integrated testing
 */

package com.tds.test

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
		name  	blank:false, unique:true
		color 	blank:true, inList:['red','green','blue','yellow','orange']
		label 	nullable:true
		note ()
		age 	nullable:true
		score 	range: 1..5
	}

	static mapping  = {	
		columns {
			name sqlType: 'varchar(30)'
			color sqlType: 'varchar(10)'
			label sqlType: 'varchar(10)'
		}
	}
}