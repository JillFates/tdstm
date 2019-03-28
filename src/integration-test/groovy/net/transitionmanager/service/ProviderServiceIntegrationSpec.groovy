package net.transitionmanager.service

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Title
import test.helper.ApiActionTestHelper
import test.helper.ApiCatalogTestHelper
import test.helper.DataScriptTestHelper
import test.helper.PersonTestHelper
import test.helper.ProjectTestHelper
import test.helper.ProviderTestHelper

@Integration
@Rollback
@Title('Integration tests for the ProviderService')
class ProviderServiceIntegrationSpec extends Specification {

	@Autowired
	ProviderService service
	@Shared
	ProjectTestHelper projectTestHelper
	@Shared
	ProviderTestHelper providerHelper
	@Shared
	ApiActionTestHelper apiActionHelper
	@Shared
	ApiCatalogTestHelper apiCatalogHelper
	@Shared
	DataScriptTestHelper etlScriptHelper
	@Shared
	PersonTestHelper personTestHelper
	@Shared
	Project project
	@Shared
	Provider provider
	@Shared
	ApiCatalog apiCatalog
	@Shared
	Person whom

	def setup() {

		projectTestHelper = new ProjectTestHelper()
		providerHelper = new ProviderTestHelper()
		apiActionHelper = new ApiActionTestHelper()
		apiCatalogHelper = new ApiCatalogTestHelper()
		etlScriptHelper = new DataScriptTestHelper()
		personTestHelper = new PersonTestHelper()

		project = projectTestHelper.createProject()
		provider = providerHelper.createProvider(project)
		apiCatalog = apiCatalogHelper.createApiCatalog(project, provider)
		whom = personTestHelper.createPerson()

		service.securityService = Mock(SecurityService)
		service.securityService.getUserCurrentProject() >> { return project }
	}

	void 'test can retrieve context association for deleting a Provider without any reference'() {

		when:
			Map<String, Integer> association = service.contextForDeleteProvider(provider.id)

		then:
			association.actionCount == 0
			association.credentialCount == 0
			association.etlScriptCount == 0
	}

	void 'test can retrieve context association for deleting a Provider associated to an ApiAction'() {

		given:
			apiActionHelper.createApiAction(project, provider, apiCatalog)

		when:
			Map<String, Number> association = service.contextForDeleteProvider(provider.id)

		then:
			association.actionCount == 1
			association.credentialCount == 0
			association.etlScriptCount == 0
	}

	void 'test can retrieve context association for deleting a Provider associated to an ApiAction and DataScript'() {

		given:
			apiActionHelper.createApiAction(project, provider, apiCatalog)

		and:
			etlScriptHelper.createDataScript(project, provider, whom, "")

		when:
			Map<String, Number> association = service.contextForDeleteProvider(provider.id)

		then:
			association.actionCount == 1
			association.credentialCount == 0
			association.etlScriptCount == 1
	}
}
