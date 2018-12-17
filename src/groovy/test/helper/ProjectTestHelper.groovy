package test.helper

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.*
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService
import org.apache.commons.lang3.RandomStringUtils as RSU

/**
 * Fetches, creates and does other helpful data preparation in the integration tests, doing the heavy lifting
 * for the ITs so that they an focus on the good stuff.
 *
 * Should not rely on any pre-existing data and will generate anything that is necessary. At least that's the idea...
 */
class ProjectTestHelper {

	static ProjectService projectService
	static PartyRelationshipService partyRelationshipService
	static final long projectId = 2445
    private MoveBundleTestHelper bundleHelper

	// Initialize
	ProjectTestHelper() {
		projectService = ApplicationContextHolder.getService('projectService')
		partyRelationshipService = ApplicationContextHolder.getService('partyRelationshipService')
	    bundleHelper = new MoveBundleTestHelper()
	}


	/**
	 * Get the project to test with
	 * @return a project with the default id (2445)
	 */
	Project getProject() {
		Project project = Project.get(projectId)
		assert project
		return project
	}

	/**
	 * Get the first MoveEvent of a Project.
	 * @param project  the project
	 * @return the move event
	 */
	MoveEvent getFirstMoveEvent(Project project) {
		MoveEvent moveEvent = MoveEvent.findByProject(project) // Grab any one of the events
		assert moveEvent
		return moveEvent
	}

	Project createProjectWithDefaultBundle(PartyGroup company=null) {
		Project project = createProject(company)
        MoveBundle bundle = bundleHelper.createBundle(project)
        project.defaultBundle = bundle
        project.save(flush:true, failOnError:true)

        return project

	}

	/**
	 * Create a project
	 */
	Project createProject(PartyGroup company=null) {

		if (!company) {
			company = createCompany('Owner')
		}
		// println "company=$company, partyType=${company?.partyType}(${company?.partyType?.id}"

		Project project = new Project()
		project.with {
			client = createClient(company)
			projectCode = RSU.randomAlphabetic(10)
			name = 'Project ' + projectCode
			description = 'Test project created by the ProjectTestHelper'
			startDate = new Date()
			completionDate = startDate + 30
			guid = StringUtil.generateGuid()
			workflowCode = 'STD_PROCESS'
			timezone = Timezone.findByCode('GMT')
			guid = StringUtil.generateGuid()
		}
		project.save(failOnError:true)

		project.projectService = projectService

		// Assigning the owner to a project is done through the PartyRelationship so the project must be saved first
		project.owner = company
		project.save(failOnError:true)

		projectService.cloneDefaultSettings(project)
		return project
	}

	/**
	 * Create a company.
	 *
	 * @param prefix  the prefix of the company name if provided
	 * @return the company
	 */
	PartyGroup createCompany(String prefix) {
		PartyType pt = PartyType.get('COMPANY')
		assert pt
		PartyGroup company = new PartyGroup()
		company.with {
			partyType = pt
			name = (prefix ? "$prefix " : '') + RSU.randomAlphabetic(10)
		}

		company.save(failOnError:true, flush:true)
	}

	/**
	 * Create a company as a client and assign them as a client of the specified company.
	 * @param company  the client's owning company
	 * @return the client
	 */
	PartyGroup createClient(PartyGroup company) {
		PartyType pt = PartyType.get('COMPANY')

		PartyGroup client = createCompany('Client')
		partyRelationshipService.assignClientToCompany(client, company)
		return client
	}

	/**
	 * Create a company as a client and assign them as a client of the specified company.
	 * @param company  the client's owning company
	 * @return the client
	 */
	PartyGroup createPartner(PartyGroup company, Project project) {
		PartyType pt = PartyType.get('COMPANY')

		PartyGroup partner = createCompany('Partner')
		partyRelationshipService.assignPartnerToCompany(partner, company)

		if (project) {
			partyRelationshipService.assignPartnerToProject(partner, project)
		}

		return partner
	}

}
