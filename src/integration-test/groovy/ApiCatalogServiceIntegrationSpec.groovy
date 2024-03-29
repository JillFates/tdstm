import grails.core.GrailsApplication
import grails.gorm.transactions.Rollback
import grails.test.mixin.integration.Integration
import net.transitionmanager.command.ApiCatalogCommand
import net.transitionmanager.action.ApiCatalog
import net.transitionmanager.project.Project
import net.transitionmanager.action.ApiCatalogService
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.project.ProviderService
import net.transitionmanager.security.SecurityService
import org.hibernate.SessionFactory
import spock.lang.See
import spock.lang.Specification
import test.helper.ApiCatalogTestHelper

@Integration
@Rollback
class ApiCatalogServiceIntegrationSpec extends Specification{
	ApiCatalogService apiCatalogService
	ProviderService   providerService
	GrailsApplication grailsApplication
	SessionFactory    sessionFactory

	ProjectTestHelper projectTestHelper = new ProjectTestHelper()

	def setup() {
		sessionFactory = grailsApplication.getMainContext().getBean('sessionFactory')
		Project project = projectTestHelper.createProject()
		providerService.securityService = [getUserCurrentProject: { return project }] as SecurityService
		apiCatalogService.securityService = [getUserCurrentProject: { return project }] as SecurityService
	}

	@See('TM-10608')
	void '1. find api catalog by id returns api catalog'() {
		setup:
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: ApiCatalogTestHelper.DICTIONARY)
			ApiCatalog apiCatalog = apiCatalogService.saveOrUpdate(command)

		when:
			def foundApiCatalog = apiCatalogService.findById(apiCatalog.id)

		then:
			foundApiCatalog
			null != foundApiCatalog.id
	}

	@See('TM-10608')
	void '2. delete api catalog by id effectively deletes api catalog'() {
		setup:
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: ApiCatalogTestHelper.DICTIONARY)
			ApiCatalog apiCatalog = apiCatalogService.saveOrUpdate(command)

		when:
			def foundApiCatalogId = apiCatalog.id
			apiCatalogService.deleteById(foundApiCatalogId)

		then:
			null == apiCatalogService.findById(foundApiCatalogId)
	}

	@See('TM-10608')
	void '3. save api catalog giving invalid dictionary json throws invalid param exception'() {
		setup:
			String dictionary =  '{"key": "value","key"}'
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: dictionary)

		when:
			apiCatalogService.saveOrUpdate(command)

		then:
			thrown InvalidParamException
	}

	@See('TM-10608')
	void '4. save api catalog with wrong version throws domain update exception'() {
		setup:
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: ApiCatalogTestHelper.DICTIONARY)
			ApiCatalog apiCatalog = apiCatalogService.saveOrUpdate(command)

		when:
			command = new ApiCatalogCommand(dictionary: ApiCatalogTestHelper.DICTIONARY, id: apiCatalog.id, version: -1)
			apiCatalogService.saveOrUpdate(command)

		then:
			thrown DomainUpdateException
	}

	@See('TM-10608')
	void '5. save api catalog with an existing/duplicate name throws domain update exception'() {
		setup:
			ApiCatalogCommand command = new ApiCatalogCommand(dictionary: ApiCatalogTestHelper.DICTIONARY)
			apiCatalogService.saveOrUpdate(command)

		when:
			apiCatalogService.saveOrUpdate(command)

		then:
			thrown DomainUpdateException
	}

}
