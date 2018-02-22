import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil
import net.transitionmanager.service.DomainUpdateException
import net.transitionmanager.service.EmptyResultException
import net.transitionmanager.service.InvalidParamException
import org.apache.commons.lang3.RandomStringUtils

import spock.lang.Specification
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.validation.ConstrainedProperty
import org.codehaus.groovy.grails.validation.Constraint
import com.tds.asset.AssetDependency
import net.transitionmanager.domain.PartyRelationship

import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Workflow
import net.transitionmanager.service.PersonService
import net.transitionmanager.service.ProjectService
import grails.validation.Validateable

class GormUtilIntegrationSpec extends Specification {

	// IOC variables
	def sessionFactory
	PersonService personService
	ProjectService projectService
	PersonTestHelper personHelper
	AssetTestHelper assetHelper
	ProjectTestHelper projectHelper

	def setup() {
		assetHelper = new AssetTestHelper()
		projectHelper = new ProjectTestHelper()
		personHelper = new PersonTestHelper()
	}

	def "1. Test isDomainClass"() {
		expect:
			GormUtil.isDomainClass(new AssetDependency())
			GormUtil.isDomainClass(Person)
			! GormUtil.isDomainClass(new Date())
			! GormUtil.isDomainClass(Date)
	}

	def "2. Test the getConstraintMaxSize"() {
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
	def "3. Test the bindData functionality"() {
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

	def "4. Test persistentProperties"() {
		when:
			Person p = new Person()
			List props = GormUtil.persistentProperties(p)
		then:
			props.size() > 10
			props.contains('firstName')
			props.contains('email')
			! props.contains('assignedProjects')
	}

	def "5. Test cloneDomain"() {
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

	def "6. Test hasCompositeKey"() {
		expect:
			GormUtil.hasCompositeKey(PartyRelationship)
			! GormUtil.hasCompositeKey(Project)

		when:
			GormUtil.hasCompositeKey(String)
		then:
			RuntimeException e = thrown()
	}

	def "7. Test getCompositeKeyProperties"() {
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

	def '8. Test getConstrainedProperties'() {
		when:
			Map cp = GormUtil.getConstrainedProperties(Person)
		then:
			cp['firstName']
			cp['lastName']
	}

	def '9. Test getConstrainedProperty'() {
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

	def '10. Test getConstraint'() {
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

	def '11. Test getConstraintUniqueProperties'() {
		when:
			List props = GormUtil.getConstraintUniqueProperties(AssetDependency, 'asset')
		then:
			props.size() == 2
			props.contains('dependent')
			props.contains('type')
	}


	def '12. Test canDomainPropertyBeReplaced'() {
		expect:
			GormUtil.canDomainPropertyBeReplaced(PartyRelationship, 'comment') == true

			// Test a property that is the identity
			GormUtil.canDomainPropertyBeReplaced(Person, 'id') == false

			// Test a property that is part of a composite key
			GormUtil.canDomainPropertyBeReplaced(PartyRelationship, 'partyIdFrom') == false

			// Test a property that has a unique constraint on it
			GormUtil.canDomainPropertyBeReplaced(MoveEventStaff, 'person') == false

	}


	def '13. Test canDomainPropertiesBeReplaced'() {
		expect:
			GormUtil.canDomainPropertiesBeReplaced(PartyRelationship, ['comment', 'statusCode'] ) == true
			GormUtil.canDomainPropertiesBeReplaced(PartyRelationship, ['comment', 'partyIdFrom'] ) == false
	}

	def '14. Test getDomainProperty'() {
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

	def '15. Testing isCompositeProperty'() {
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

	def '16. Testing findAllByProperties'() {
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

	def '17. Testing mergeDomainReferences'() {
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

			projectService.addTeamMember(project, fromPerson, extraTeam)

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

	def '18. Testing mergeDomainReferences for invalid parameters'() {
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
	void '19. testGetDomainPropertiesWithConstraint'() {

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
	void '20. testFlushAndClearSession()'' {
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

	def "21. Test findInProject on different scenarios" () {
		setup: "Create some objects require for the tests"
			Project project = projectHelper.createProject()
			Project project2 = projectHelper.createProject()
			Person person = personHelper.createPerson()
			Application application = assetHelper.createApplication(person, project)

		when: "Looking up the application using the Application class"
			Application app = GormUtil.findInProject(project, Application, application.id)
		then: "The application was found"
			app != null
			app instanceof Application
			app.id == application.id

		when: "Looking up the application using the AssetEntity class"
			AssetEntity asset1 = GormUtil.findInProject(project, AssetEntity, application.id)
		then: "The application was found"
			app != null
			app instanceof Application
			app.id == application.id

		when: "Trying to find an instance of something that isn't a domain object"
			def bogus = GormUtil.findInProject(project, String, 123)
		then: "Nothing was found"
			bogus == null

		when: "Trying to find an instance of something that isn't a domain object and setting the exception flag"
			bogus = GormUtil.findInProject(project, String, 123, true)
		then: "An InvalidParamException is thrown"
			thrown EmptyResultException

		when: "Trying to find the application and passing a different project"
			app = GormUtil.findInProject(project2, Application, application.id, true)
		then: "An InvalidParamException is thrown"
			thrown EmptyResultException

		when: "Trying to find the application and passing a null project"
			app = GormUtil.findInProject(null, Application, application.id, true)
		then: "An InvalidParamException is thrown"
				thrown EmptyResultException

		when: "Trying to find something with null class"
			app = GormUtil.findInProject(project, null, application.id, true)
		then: "An InvalidParamException is thrown"
			thrown EmptyResultException

		when: "Trying to find the application and passing a null id"
			app = GormUtil.findInProject(project, Application, null, true)
		then: "An InvalidParamException is thrown"
			thrown EmptyResultException

		when: "Trying to find the application and passing an id of the wrong type"
			app = GormUtil.findInProject(project, Application, "bogus", true)
		then: "An InvalidParamException is thrown"
			thrown EmptyResultException

		when: "Trying to find the application and passing an id that doesn't exist."
			app = GormUtil.findInProject(project, Application, application.id + 1)
		then: "Null is returned."
			app == null
	}

	def "22. test domainObjectToMap under different scenarios"() {
		setup: "create a Project"
			Project project = projectHelper.createProject()
		when: "Converting the project to a map with default parameters"
			Map projectMap = GormUtil.domainObjectToMap(project)
		then: "No exceptions were thrown"
			noExceptionThrown()
		and: "The map contains the name of the project"
			projectMap["name"] != null
		and: "It has the id"
			projectMap["id"] != null
		and: "The reference to the timezone is not null"
			Map timezoneMap = projectMap["timezone"]
			timezoneMap != null
		and: "The timezone map has the id"
			timezoneMap["id"] != null
		and: "The timezone has the code"
			timezoneMap["code"] != null
		and: "Since the project doesn't have a bundle, nor does the map"
			projectMap["defaultBundle"] == null
		when: "When converting to map and excluding the timezone"
			projectMap = GormUtil.domainObjectToMap(project, null, ["timezone"])
		then: "The map doesn't have a key for the excluded property"
			!projectMap.containsKey("timezone")
		when: "Asking only a limited number of properties"
			projectMap = GormUtil.domainObjectToMap(project, ["projectCode", "workflowCode"])
		then: "The map has only two elements"
			projectMap.keySet().size() == 2
		and: "The projectCode is in the map"
			projectMap.containsKey("projectCode")
		and: "The workflowCode is also in the map"
			projectMap.containsKey("workflowCode")
	}

	void '23. Test validateErrorsI18n for validatable class'() {
        when: 'the command object has bad values'
             TestValidatableCommand tvc = new TestValidatableCommand(name:'Tom', age:120)
        then: 'it should have errors'
            ! tvc.validate() & tvc.hasErrors()
		and: 'the error message should be about the age being out of range, defaulting to US'
            ['Property age of class TestValidatableCommand with value [120] does not fall within the valid range from [1] to [110]'] == 
				GormUtil.validateErrorsI18n(tvc)
		and: 'the error should also translate to Spanish'
			['Property age of class TestValidatableCommand with value [120] does not fall within the valid range from [1] to [110]'] == 
				GormUtil.validateErrorsI18n(tvc, new Locale('es'))
			// TODO : JM 2/2018 : TM-9197 : Fix to support proper localization
			// ['La propiedad age de la clase TestValidatableCommand con valor [120] no entra en el rango válido de [1] a [110]'] == 
			// 	GormUtil.validateErrorsI18n(tvc, new Locale('es'))

	}

}


/**
 * used in conjunction with the validation function tests
 */
@Validateable
class TestValidatableCommand {
    String name
    Integer age

    static constraints = {
        name blank:false, inList:['Tom', 'Dick', 'Harry']
        age range:1..110
    }
}