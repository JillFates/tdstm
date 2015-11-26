import grails.test.*

import com.tds.test.TestDomain
import com.tdssrc.grails.GormUtil

class GormUtilTests extends GrailsUnitTestCase {
	
	// IOC variables
	def sessionFactory

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

		/*
			age nullable:true
			color 
			label nullable:true
			name blank:false
			note
			score inList:[1,2,3,4,5]
		*/

	}

	/**
	 * tests the GormUtil.flushAndClearSession and mergeWithSession functionality
	 */
	void testFlushAndClearSession() {
		def session = sessionFactory.getCurrentSession()
		assertTrue 'validate we have a session', (session != null)

		Project project = Project.findAll([max: 10])[0]
		assertTrue 'validate a project was found', (project != null)

		def projectId = project.id
		def lastAssetId = (project.lastAssetId == null ? 1 : project.lastAssetId ++)
		project.lastAssetId = lastAssetId
		if (GormUtil.flushAndClearSession(session, 1, 1)) {
			assertTrue 'Cleared passing 1,1', true

			// Now reattach the project domain object to the session
			(project) = GormUtil.mergeWithSession(session, [project])
			assertTrue 'mergeWithSession works', (project != null)
		}

		assertTrue 'Save does not error', ( project.save(flush:true) != null )

		assertTrue 'Cleared just passing 50', GormUtil.flushAndClearSession(session, 50)

		project = Project.get(projectId)
		assertTrue 'project was reloaded', (project != null)
		assertEquals 'lastAssetId was updated', lastAssetId, project.lastAssetId

	}

}
