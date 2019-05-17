package net.transitionmanager.command

import net.transitionmanager.service.dataview.AllAssetsFilterUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class DataviewUserParamsCommandSpec extends Specification implements AllAssetsFilterUnitTest {

	void 'test can create a all assets command object content'() {

		when: 'creates an instance of DataviewUserParamsCommand with "All Assets" filters Map'
			DataviewUserParamsCommand command = new DataviewUserParamsCommand(
				allAssetsDataviewMap
			)

		then: 'command is valid'
			command.validate()
	}

	void 'test can create a all devices command object content'() {

		when: 'creates an instance of DataviewUserParamsCommand with "All Assets" filters Map'
			DataviewUserParamsCommand command = new DataviewUserParamsCommand(
				devicesDataviewMap
			)

		then: 'command is valid'
			command.validate()
	}

	void 'test can create a all applications command object content'() {

		when: 'creates an instance of DataviewUserParamsCommand with "All Assets" filters Map'
			DataviewUserParamsCommand command = new DataviewUserParamsCommand(
				applicationsDataviewMap
			)

		then: 'command is valid'
			command.validate()
	}

	void 'test can create a all assets command object content with named filters'() {

		given: 'All Assets Map filter content'
			allAssetsDataviewMap.filters.named = 'server,toValidate'

		when: 'creates an instance of DataviewUserParamsCommand with "All Assets" filters Map'
			DataviewUserParamsCommand command = new DataviewUserParamsCommand(allAssetsDataviewMap)

		then: 'command is valid'
			command.validate()

		and: 'and extra filters where correctly parsed'
			command.filters.namedFilterList == ['server', 'toValidate']
	}

	@Unroll
	void 'test can create a all assets command object content with extra filters #domain, #property and #filter'() {

		setup: 'All Assets Map filter content'
			allAssetsDataviewMap.filters.extra = [
				[
					domain  : domain,
					property: property,
					filter  : filter
				]
			]

		and: 'an instance of DataviewUserParamsCommand with "All Assets" filters Map'
			DataviewUserParamsCommand command = new DataviewUserParamsCommand(allAssetsDataviewMap)

		expect: 'command is valid'
			command.validate() == valid

		where: 'it defines domain, property and filter'
			domain        | property    | filter   || valid
			'common'      | 'assetName' | 'FOOBAR' || true
			'application' | 'appTech'   | 'Apple'  || true
			'application' | 'appTech'   | ''       || true
			'application' | 'appTech'   | null     || false
			'application' | null        | 'Apple'  || false
	}


}
