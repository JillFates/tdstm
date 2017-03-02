import com.tds.asset.AssetDependency
import com.tdssrc.grails.GormUtil
import org.apache.commons.lang3.RandomStringUtils

import spock.lang.Specification
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.grails.validation.ConstrainedProperty
import org.codehaus.groovy.grails.validation.Constraint
import com.tds.asset.AssetDependency
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Workflow


class GormUtilTests extends Specification {

	// IOC variables
	def sessionFactory
	def personService
	def personHelper
	def assetHelper

	def setup() {
		assetHelper = new AssetTestHelper()
	}

	def "Test isDomainClass"() {
		expect:
			GormUtil.isDomainClass(new AssetDependency())
			GormUtil.isDomainClass(Person)
			! GormUtil.isDomainClass(new Date())
			! GormUtil.isDomainClass(Date)
	}

	def "Test the getConstraintMaxSize"() {
		// Positive test first
		when:
			AssetDependency domain = new AssetDependency()
			String prop = 'comment'

		then: 'Accessing a property that has the constraint should return a value'
			0 < GormUtil.getConstraintMaxSize(domain, prop)

		//
		when: 'testing the various negative test cases'
			size = GormUtil.getConstraintMaxSize(AssetDependency, 'asset')
		then: 'Passing a property that does not have the maxSize constraint should return a null'
			size == null
		//	ex = thrown()
		//	ex.message.contains('does not have the maxSize constraint')

// TODO : Burt rewrote the above test case that needs to be validated
		/*
		when:
			GormUtil.getConstraintMaxSize(AssetDependency, 'nonExistentProperty')
		then: 'Passing an invalid property name should throw an exception'
			RuntimeException ex = thrown()
			ex.message.contains('invalid property name')
		*/

		when: 'Passing a non-domain'
			GormUtil.getConstraintMaxSize(Date, 'foo')
		then: 'an exception should thrown'
			ex = thrown()
			ex.message.contains('A non-domain class parameter was provided')

		then: 'Passing a property that does not have the maxSize constraint should throw an exception'

	}
	/*
	 * TODO Burt appeared to delete the bindData method from GormUtil
	 *
	def "Test the bindData functionality"() {
		when:
			Map args = [firstName: 'robin', lastName: 'banks']
			def pmap = new GrailsParameterMap(args, null)
			Person person = new Person()
			GormUtil.bindData(person, pmap)
		then:
			person.firstName == 'robin'
			person.lastName == 'banks'

		when:
			person = new Person()
			GormUtil.bindData(person, pmap, [exclude: 'firstName'])
		then:
			person.firstName == null
			person.lastName == 'banks'

		when:
			person = new Person()
			GormUtil.bindData(person, pmap, [include: 'firstName'])
		then:
			person.firstName == 'robin'
			person.lastName == ''
	}
	*/

	def "Test persistentProperties"() {
		when:
			Person p = new Person()
			List props = GormUtil.persistentProperties(p)
		then:
			props.size() > 10
			props.contains('firstName')
			props.contains('email')
			! props.contains('assignedProjects')
	}

	def "Test cloneDomain"() {
		when:
			Person p = new Person(firstName:'Jack', middleName:'B', lastName:'Nimble')
			Person n = GormUtil.cloneDomain(p, [lastName:'Quick'])
		then:
			n.toString().equals('Jack B Quick')

		when:
			GormUtil.cloneDomain(new String() )
		then:
			RuntimeException e = thrown()

	}

	def "Test hasCompositeKey"() {
		expect:
			GormUtil.hasCompositeKey(PartyRelationship)
			! GormUtil.hasCompositeKey(Project)

		when:
			GormUtil.hasCompositeKey(String)
		then:
			RuntimeException e = thrown()
	}

	def "Test getCompositeKeyProperties"() {
		when:
			List list = GormUtil.getCompositeKeyProperties(PartyRelationship)
		then:
			list.size() > 0
			list.contains('partyIdTo')
			list.contains('partyIdFrom')

		when:
			list = GormUtil.getCompositeKeyProperties(Project)
		then:
			list.size() == 0

	}

	def 'Test getConstrainedProperties'() {
		when:
			Map cp = GormUtil.getConstrainedProperties(Person)
		then:
			cp['firstName']
			cp['lastName']
	}

	def 'Test getConstrainedProperty'() {
		when:
			ConstrainedProperty cp = GormUtil.getConstrainedProperty(Person, 'firstName')
			Range range = cp.getAppliedConstraint('size').getRange()
		then:
			range.from == 1
			range.to > 1

			cp.getAppliedConstraint('blank').isBlank() == false

		when:
			cp = GormUtil.getConstrainedProperty(Person, 'lastName')
		then:
			cp.getAppliedConstraint('blank').isBlank() == true

	}

	def 'Test getConstraint'() {
		when:
			Constraint cp = GormUtil.getConstraint(Person, 'firstName', 'blank')
		then:
			cp != null

		when:
			cp = GormUtil.getConstraint(Person, 'firstName', 'bogus')
		then:
			cp == null

		when: 'Checking for Unique Constraints'
			cp = GormUtil.getConstraint(AssetDependency, 'asset', 'unique')
		then:
			cp != null
			List props = cp.getUniquenessGroup()
			props.contains('dependent')
			props.contains('type')

		when: 'Checking the maxSize Constraint'
			cp = GormUtil.getConstraint(DataTransferBatch, 'importResults', 'maxSize')
		then:
			cp != null
			cp.getMaxSize() > 16380000
	}

	def 'Test getConstraintUniqueProperties'() {
		when:
			List props = GormUtil.getConstraintUniqueProperties(AssetDependency, 'asset')
		then:
			props.size() == 2
			props.contains('dependent')
			props.contains('type')
	}


	def 'Test canDomainPropertyBeReplaced'() {
		expect:
			GormUtil.canDomainPropertyBeReplaced(PartyRelationship, 'comment') == true

			// Test a property that is the identity
			GormUtil.canDomainPropertyBeReplaced(Person, 'id') == false

			// Test a property that is part of a composite key
			GormUtil.canDomainPropertyBeReplaced(PartyRelationship, 'partyIdFrom') == false

			// Test a property that has a unique constraint on it
			GormUtil.canDomainPropertyBeReplaced(MoveEventStaff, 'person') == false

	}


	def 'Test canDomainPropertiesBeReplaced'() {
		expect:
			GormUtil.canDomainPropertiesBeReplaced(PartyRelationship, ['comment', 'statusCode'] ) == true
			GormUtil.canDomainPropertiesBeReplaced(PartyRelationship, ['comment', 'partyIdFrom'] ) == false
	}

	def 'Test getDomainProperty'() {
		when:
			GrailsDomainClassProperty prop = GormUtil.getDomainProperty(Person, 'firstName')
		then:
			prop != null
			prop.isPersistent()
			! prop.isIdentity()

		when:
			prop = GormUtil.getDomainProperty(Person, 'id')
		then:
			prop != null
			prop.isPersistent()
			prop.isIdentity()

		when:
			prop = GormUtil.getDomainProperty(PartyRelationship, 'partyIdFrom')
		then:
			prop != null
			prop.isPersistent()

		when: 'Test for invalid property'
			prop = GormUtil.getDomainProperty(Person, 'bogus')
		then:
			org.codehaus.groovy.grails.exceptions.InvalidPropertyException e = thrown()
	}

	def 'Testing isCompositeProperty'() {
		expect:
			! GormUtil.isCompositeProperty(Person, 'firstName')
			GormUtil.isCompositeProperty(PartyRelationship, 'partyIdFrom')

		when: 'Check when passing property object instead of the name'
			GrailsDomainClassProperty property = GormUtil.getDomainProperty(PartyRelationship, 'partyIdFrom')
		then:
			GormUtil.isCompositeProperty(PartyRelationship, property)

		when: 'Check the negative case'
			property = GormUtil.getDomainProperty(Person, 'firstName')
		then:
			! GormUtil.isCompositeProperty(Person, property)
	}

	def 'Testing findAllByProperties'() {
		when:
			def projectHelper = new ProjectTestHelper()
			Project project = projectHelper.getProject()
			def personHelper = new PersonTestHelper()
			Person adminPerson = personHelper.getAdminPerson()

			String title = 'B.F.D.'
			Map personMap = [lastName:'Bullafarht', title: title]
			Person newPerson1 = personHelper.createPerson(adminPerson, project.client, project, personMap)
			Person newPerson2 = personHelper.createPerson(adminPerson, project.client, project, personMap)
			List persons = GormUtil.findAllByProperties(Person, personMap)
		then:
			persons.size() == 2
			persons[0].lastName == personMap.lastName
			persons[1].lastName == personMap.lastName

		when: 'Add a third person with the same title so we can check an OR query vs AND'
			Person newPerson3 = personHelper.createPerson(adminPerson, project.client, project, [title:title])
		then:
			GormUtil.findAllByProperties(Person, personMap).size() == 2
			GormUtil.findAllByProperties(Person, personMap, GormUtil.Operator.OR).size() == 3
	}

	/**
	 * Helper method to create a Workflow object
	 * @param whom - the person that last modified the domain record
	 * @return a newly minted Workflow that references the person
	 */
	private Workflow createWorkflow(Person whom) {
		Workflow wf = new Workflow(process: RandomStringUtils.randomAlphabetic(10), updatedBy:whom)
		if (!wf.save(flush:true)) {
			throw new RuntimeException("Unable to save new Workflow due to " + GormUtil.allErrorsString(wf))
		}
		return wf
	}

	def 'Testing mergeDomainReferences'() {
		// This logic need to test several different aspects of merging persons which include:
		//    1. Replacing references (e.g. Workflow.updatedBy)
		//    2. Cloning domain where references are part of domain identity (e.g. PartyRelationship)
		//			a) Ignore domain records that the target already has an equivilant (Staff assignment to same company and project)
		//			b) Clone a new domain record where To domain does not have equivilant (e.g. Team assignments)
		//			c) Delete original domain rows
		when: 'setting up the initial test cases'
			def projectHelper = new ProjectTestHelper()
			Project project = projectHelper.getProject()
			def personHelper = new PersonTestHelper()
			Person adminPerson = personHelper.getAdminPerson()
			Map results = [:]
			Map personMap = [lastName:'Bullafarht']
			String extraTeam = 'DB_ADMIN'

			Person fromPerson = personHelper.createPerson(adminPerson, project.client, project, personMap+[firstName:'From'])
			personService.addToProjectTeam(adminPerson.userLogin, project.id.toString(), fromPerson.id.toString(), extraTeam, results)

			Person toPerson = personHelper.createPerson(adminPerson, project.client, project, personMap+[firstName:'To'])

			Workflow wf = createWorkflow(fromPerson)

			// println "***** fromPerson=$fromPerson (${fromPerson.id}), toPerson=$toPerson (${toPerson.id})"
		then: 'we should expect the following'
			GormUtil.findAllByProperties(Person, personMap).size() == 2

			// The from person should have a number of PartyRelationships
			GormUtil.findAllByProperties(PartyRelationship, [partyIdFrom:fromPerson, partyIdTo:fromPerson], GormUtil.Operator.OR).size() > 0

			// Validate that the Workflow was properly created
			wf.id > 0

			// The From Person should have the extraTeam reference
			personService.getPersonTeamCodes(fromPerson).contains(extraTeam)

		when: 'the persons are merged together things should look differently'
			GormUtil.mergeDomainReferences(fromPerson, toPerson, true)
			List persons = GormUtil.findAllByProperties(Person, personMap)

			// Reload the Workflow so we can check for changed references
			wf.refresh()
		then:
			// We should now only have one person that matches the To person
			persons.size() == 1
			persons[0].id == toPerson.id

			// Validate that direct references switch over (case 1)
			wf.updatedBy.id == toPerson.id

			// The case 2A is validated by the fact that the merge did not encounter duplicate distinct key references when cloning over PartyRelationship

			// The To person should now have the extraTeam reference - tests cloning where non-equivalants are cloned over (case 2B)
			personService.getPersonTeamCodes(toPerson).contains(extraTeam)

			// All PartyRelationship for the From person should now be gone - tests cloning where equivilants are ignored (case 2C)
			GormUtil.findAllByProperties(PartyRelationship, [partyIdFrom:fromPerson, partyIdTo:fromPerson], GormUtil.Operator.OR).size() == 0

	}

	def 'Testing mergeDomainReferences for invalid parameters'() {
		when: 'called with non-domain classes'
			GormUtil.mergeDomainReferences(new Date(), new Date())
		then: 'an exception should occur'
			RuntimeException e = thrown()

		when: 'called with domain classes missing necessary domainReferences property'
			Workflow wf1 = createWorkflow(null)
			Workflow wf2 = createWorkflow(null)
			GormUtil.mergeDomainReferences(wf1, wf2)
		then: 'an exception should occur'
			e = thrown()

	}

	/*
	void testGetDomainPropertiesWithConstraint() {

		def list

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

			// age nullable:true
			// color
			// label nullable:true
			// name blank:false
			// note
			// score inList:[1,2,3,4,5]
	}
	// tests the GormUtil.flushAndClearSession and mergeWithSession functionality
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
	*/

}
