package test.helper

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdssrc.grails.StringUtil
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Timezone
import net.transitionmanager.service.PartyRelationshipService
import net.transitionmanager.service.ProjectService

/**
 * Fetches, creates and does other helpful data preparation in the integration tests to persist at database
 * and use by QA E2E Automation Project
 */
class E2eInformationHelper {

	static ProjectService projectService
	static PartyRelationshipService partyRelationshipService

	// Initialize
	E2eInformationHelper() {
		projectService = ApplicationContextHolder.getService('projectService')
		partyRelationshipService = ApplicationContextHolder.getService('partyRelationshipService')
	}

	/**
	 * Create a project from given data in map, if exists update completion date adding 30 days
	 * @param projectData = [projectName: string, projectCode: string, projectDesc: string, projectClient: string,
	 * projectCompany: string]
	 * @return the project
	 */
	static createProject(Map projectData) {
		def project = Project.findWhere([name: projectData.projectName])
		def nowDate = new Date()
		if (!project){
			def company = createCompany(projectData.projectCompany)
			project = new Project()
			project.with {
				client = createClient(company, projectData.projectClient)
				projectCode = projectData.projectCode
				name = projectData.projectName
				description = projectData.projectDesc
				startDate = nowDate
				completionDate = nowDate + 30
				guid = StringUtil.generateGuid()
				workflowCode = 'STD_PROCESS'
				timezone = Timezone.findByCode('GMT')
			}
			project.save(flush: true)

			project.projectService = projectService
			// Assigning the owner to a project is done through the PartyRelationship so the project must be saved first
			project.owner = company
			project.save(flush: true)

			projectService.cloneDefaultSettings(project)
		} else {
			project.completionDate = nowDate + 30
			project.save(flush: true)
		}

		project
	}

	/**
	 * Create a company.
	 *
	 * @param companyName
	 * @return the company
	 */
	static createCompany(companyName) {
		if (!PartyGroup.findWhere([name: companyName])){
			def pt = PartyType.get('COMPANY')
			def company = new PartyGroup()
			company.with {
				partyType = pt
				name = companyName
			}
			company.save(flush:true)
		}
	}

	/**
	 * Create a company as a client and assign them as a client of the specified company.
	 * @param company  the client's owning company
	 * @param clientName
	 * @return the client
	 */
	static createClient(PartyGroup company, clientName) {
		def client = createCompany(clientName)
		partyRelationshipService.assignClientToCompany(client, company)
		client
	}
}
