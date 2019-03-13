package net.transitionmanager.service

import com.tdssrc.grails.ApiCatalogUtil
import com.tdssrc.grails.StringUtil
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import net.transitionmanager.domain.ApiAction
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.DataScript
import net.transitionmanager.domain.DataScriptMode
import net.transitionmanager.domain.PartyGroup
import net.transitionmanager.domain.PartyType
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.apache.commons.lang3.RandomStringUtils
import spock.lang.Shared
import spock.lang.Specification

class ProviderServiceSpec extends Specification implements ServiceUnitTest<ProviderService>, DataTest {

	@Shared  Project project

	void setupSpec() {
		mockDomains Project, Provider, PartyType, PartyGroup
	}

	def setup(){
		defineBeans {
			service.grailsApplication = grailsApplication
		}
		PartyType partyTypeCompany = new PartyType(description:  'COMPANY').save(failOnError: true)

		PartyGroup company = new PartyGroup()
		company.partyType = partyTypeCompany
		company.name = RandomStringUtils.randomAlphabetic(10)
		company.save(failOnError:true)

		PartyGroup client  = new PartyGroup()
		client.partyType = partyTypeCompany
		client.name = 'PARTY '
		client.save(failOnError:true)

		project = new Project(
			workflowCode: 'STD_PROCESS',
			startDate: new Date(),
			completionDate: new Date() + 30,
			name: 'Project UNIT TEST',
			client:  client,
			projectCode: RandomStringUtils.randomAlphabetic(10),
			guid: StringUtil.generateGuid()
		).save(failOnError: true)

		service.securityService = Mock(SecurityService)
		service.securityService.getUserCurrentProject() >> { return project }
	}

	void 'test can retrieve context association for deleting a Provider without any reference'(){

		given:
			Provider provider = new Provider(
				name: 'FOOBAR',
				description: 'Description',
				project: project
			).save(failOnError: true)

		when:
			Map<String, Integer> association = service.contextForDeleteProvider(provider.id)

		then:
			Provider.count() == 1
			association.actionCount == 0
			association.credentialCount == 0
			association.etlScriptCount == 0
	}

	void 'test can retrieve context association for deleting a Provider associated to an ApiAction'(){

		given:
			Provider provider = new Provider(
				name: 'FOOBAR',
				description: 'Description',
				project: project
			).save(failOnError: true)

		and:
			ApiCatalog apiCatalog = new ApiCatalog(
				project: project,
				provider: provider,
				name: RandomStringUtils.randomAlphabetic(10),
			).save(failOnError: true)

			new ApiAction(project: project,
				provider: provider,
				name: RandomStringUtils.randomAlphanumeric(10),
				description: RandomStringUtils.randomAlphanumeric(10),
				apiCatalog: apiCatalog,
				connectorMethod: 'executeCall',
				methodParams: '[]',
				reactionScripts: '{"SUCCESS": "success","STATUS": "status","ERROR": "error"}',
				reactionScriptsValid: 1,
				callbackMode: null,
				endpointUrl: 'http://www.google.com',
				endpointPath: '/'
			).save(failOnError: true)


		when:
			Map<String, Number> association = service.contextForDeleteProvider(provider.id)

		then:
			Provider.count() == 1
			association.actionCount == 1
			association.credentialCount == 0
			association.etlScriptCount == 0
	}

	void 'test can retrieve context association for deleting a Provider associated to an ApiAction and DataScript'(){

		given:
			Provider provider = new Provider(
				name: 'FOOBAR',
				description: 'Description',
				project: project
			).save(failOnError: true)

		and:
			ApiCatalog apiCatalog = new ApiCatalog(
				project: project,
				provider: provider,
				name: RandomStringUtils.randomAlphabetic(10),
			).save(failOnError: true)

			new ApiAction(project: project,
				provider: provider,
				name: RandomStringUtils.randomAlphanumeric(10),
				description: RandomStringUtils.randomAlphanumeric(10),
				apiCatalog: apiCatalog,
				connectorMethod: 'executeCall',
				methodParams: '[]',
				reactionScripts: '{"SUCCESS": "success","STATUS": "status","ERROR": "error"}',
				reactionScriptsValid: 1,
				callbackMode: null,
				endpointUrl: 'http://www.google.com',
				endpointPath: '/'
			).save(failOnError: true)

		and:
			new DataScript(
				name: 'Test DataScript-' + RandomStringUtils.randomAlphabetic(10),
				description: 'Test description',
				target: 'Test target',
				etlSourceCode: 'console on',
				project: project,
				provider: provider,
				mode: DataScriptMode.IMPORT
			).save(failOnError: true)

		when:
			Map<String, Number> association = service.contextForDeleteProvider(provider.id)

		then:
			Provider.count() == 1
			association.actionCount == 1
			association.credentialCount == 0
			association.etlScriptCount == 1
	}
}
