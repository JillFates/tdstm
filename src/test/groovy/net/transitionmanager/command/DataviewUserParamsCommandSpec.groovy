package net.transitionmanager.command

import net.transitionmanager.service.dataview.AllAssetsFilterUnitTest
import spock.lang.Specification

class DataviewUserParamsCommandSpec extends Specification implements AllAssetsFilterUnitTest {

	void 'test can create a all assets command object content'() {

		when: 'creates an instance of DataviewUserParamsCommand with "All Assets" filters Map'
			DataviewUserParamsCommand command = new DataviewUserParamsCommand(
				allAssetsFilterMap
			)

		then: 'command is valid'
			command.validate()
	}

	void 'test can create a all assets command object content with named filters'() {

		given: 'All Assets Map filter content'
			allAssetsFilterMap.filters.named = 'server,toValidate'

		when: 'creates an instance of DataviewUserParamsCommand with "All Assets" filters Map'
			DataviewUserParamsCommand command = new DataviewUserParamsCommand(allAssetsFilterMap)

		then: 'command is valid'
			command.validate()

		and: 'and extra filters where correctly parsed'
			command.filters.namedFilterList == ['server', 'toValidate']
	}

	void 'test can create a all assets command object content with extra filters'() {

		given: 'All Assets Map filter content'
			allAssetsFilterMap.filters.extra = [
				[domain: 'common', property: 'assetName', filter: 'ACME-WB-84'],
				[property: 'ufp', filter: 'true']
			]

		when: 'creates an instance of DataviewUserParamsCommand with "All Assets" filters Map'
			DataviewUserParamsCommand command = new DataviewUserParamsCommand(allAssetsFilterMap)

		then: 'command is valid'
			command.validate()

		and: 'and extra filters where correctly parsed'
			command.filters.extra.size() == 2
			command.filters.extra.find { it.domain } == [domain: 'common', property: 'assetName', filter: 'ACME-WB-84']
			command.filters.extra.find { !it.domain } == [property: 'ufp', filter: 'true']
	}


}
