import grails.test.*

import com.tds.test.TestDomain
import com.tdssrc.grails.GormUtil

class GormUtilTests extends GrailsUnitTestCase {
	
	protected void setUp() {
		super.setUp()
	}

	protected void tearDown() {
		super.tearDown()
	}

	/**
	 * 
	 */
	void testGetDomainPropertiesWithConstraint() {

		def list = []

		list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'nullable', true).sort()
		assertEquals 'Test "nullable" for true', ['age', 'label'], list

		list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'nullable', false).sort()
		assertEquals 'Test "nullable" for false', ['color', 'name', 'note', 'score'], list

		// nullable is always set regardless
		list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'nullable').sort()
		assertEquals 'Test "nullable" for null', ['age', 'color', 'label', 'name', 'note', 'score'], list

		list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'blank', true).sort()
		assertEquals 'Test "blank" for true', ['color', 'label', 'note'], list

		list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'blank', false).sort()
		assertEquals 'Test "blank" for false', ['age', 'name', 'score'], list

		list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'blank').sort()
		assertEquals 'Test "blank" for null', ['age', 'color', 'label', 'name', 'note', 'score'], list

		list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'range', (1..5) ).sort()
		assertEquals 'Test "range" matching', ['score'], list
		
		list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'range', (2..4) )
		assertEquals 'Test "range" no matching', [], list
		
		list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'range').sort()
		assertEquals 'Test "range" for null', ['score'], list

		//
		// inList functionality doesn't work presently
		//
		//list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'inList', ['red','green','blue','yellow','orange'])
		//assertEquals 'Test "inList" matching', ['color'], list
		
		//list = GormUtil.getDomainPropertiesWithConstraint(TestDomain, 'inList').sort()
		//assertEquals 'Test "inList" for null', ['color'], list
		
	}

/*
		age nullable:true
		color 
		label nullable:true
		name blank:false
		note
		score inList:[1,2,3,4,5]
*/
}
