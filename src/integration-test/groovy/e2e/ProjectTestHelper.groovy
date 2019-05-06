package e2e

import com.tdssrc.grails.StringUtil
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.party.PartyType
import net.transitionmanager.project.Project
import net.transitionmanager.common.Timezone

/**
 * Fetches, creates and does other helpful data preparation in the e2e project integration test *
 * Should not rely on any pre-existing data and will generate anything that is necessary.
 */

class ProjectTestHelper extends test.helper.ProjectTestHelper {

    /**
     * Create a project from given data in map for E2EProjectSpec, if exists update completion date adding 30 days
     * @param projectData = [projectName: string, projectCode: string, projectDesc: string, projectClient: string,
     * projectCompany: string]
     * @return the project
     */
    Project createProject(Map projectData) {
        Project project = Project.findWhere([name: projectData.projectName])
        Date nowDate = new Date()
        if (!project){
            PartyGroup company = createCompany(projectData.projectCompany)
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

            //project.projectService = projectService
            // Assigning the owner to a project is done through the PartyRelationship so the project must be saved first
            projectService.setOwner(project,company)
            project.save(flush: true)

            projectService.cloneDefaultSettings(project)
        } else {
            project.completionDate = nowDate + 30
            project.save(flush: true)
        }

        return project
    }

    /**
     * Create a company for E2EProjectSpec.
     * @param companyName = name of the company to be created if not exists
     * @return the company
     */
    PartyGroup createCompany(String companyName) {
        PartyGroup existingCompany = PartyGroup.findWhere([name: companyName])
        if (!existingCompany){
            def pt = PartyType.get('COMPANY')
            def company = new PartyGroup()
            company.with {
                partyType = pt
                name = companyName
            }
            company.save(flush:true)
            return company
        } else {
            return existingCompany
        }
    }

    /**
     * Create a company as a client and assign them as a client of the specified company for E2EProjectSpec.
     * @param company  the client's owning company
     * @param clientName
     * @return the client
     */
    PartyGroup createClient(PartyGroup company, String clientName) {
        PartyGroup client = createCompany(clientName)
        if (client != company) {
            partyRelationshipService.assignClientToCompany(client, company)
        }
        return client
    }

}
