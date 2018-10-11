import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import net.transitionmanager.command.DataviewApiParamsCommand
import spock.lang.Specification


@TestMixin(GrailsUnitTestMixin)
class DataViewApiParamsCommandSpec extends Specification {

	void 'test can define offset and limit'(){

		when: 'default values are used'
			DataviewApiParamsCommand command = new DataviewApiParamsCommand()

		then: 'validation succeeds'
			command.validate()

		when: 'limit offset is less than 0'
			command = new DataviewApiParamsCommand()
			command.offset = -1

		then: 'validation fails'
			!command.validate()

		when: 'limit is less than zero'
			command = new DataviewApiParamsCommand()
			command.limit = -1

		then: 'validation fails'
			!command.validate()
	}

	void 'test can define filters'(){

		when: 'default values are used'
			DataviewApiParamsCommand command = new DataviewApiParamsCommand()

		then: 'validation succeeds'
			command.validate()

		when: 'filters contains 1 column with a simple value'
			command.filter = ['environment=Production']

		then: 'validation succeeds'
			command.validate()
			command.filterParams[0].domain == null
			command.filterParams[0].fieldName == 'environment'
			command.filterParams[0].filter == 'Production'

		when: 'filters contains multiple columns with simple values'
			command = new DataviewApiParamsCommand()
			command.filter = ['common.environment=Production', 'assetName=PDV*']

		then: 'validation succeeds'
			command.validate()
			command.filterParams[0].domain == 'common'
			command.filterParams[0].fieldName == 'environment'
			command.filterParams[0].filter == 'Production'
			command.filterParams[1].domain == null
			command.filterParams[1].fieldName == 'assetName'
			command.filterParams[1].filter == 'PDV*'

		when: 'filters contains incorrect field definition'
			command = new DataviewApiParamsCommand()
			command.filter = ['common.=Production']

		then: 'validation fails'
			!command.validate()

		when: 'filters contains incorrect field definition'
			command = new DataviewApiParamsCommand()
			command.filter = ['.environment=Production']

		then: 'validation fails'
			!command.validate()

		when: 'filters contains incorrect field definition'
			command = new DataviewApiParamsCommand()
			command.filter = ['.=Production']

		then: 'validation fails'
			!command.validate()

		when: 'filters contains incorrect field definition'
			command = new DataviewApiParamsCommand()
			command.filter = ['environment=']

		then: 'validation fails'
			!command.validate()
	}
}
