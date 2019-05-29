package net.transitionmanager.command.dataview

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

	@Unroll
	void 'test can create a all assets command object content with extra filters #property and #filter'() {

		setup: 'All Assets Map filter content'
			allAssetsDataviewMap.filters.extra = [
				[
					property: property,
					filter  : filter
				]
			]

		and: 'an instance of DataviewUserParamsCommand with "All Assets" filters Map'
			DataviewUserParamsCommand command = new DataviewUserParamsCommand(allAssetsDataviewMap)

		expect: 'command is valid'
			command.validate() == valid

		where: 'it defines domain, property and filter'
			property              | filter           || valid
			'assetName'           | 'FOOBAR'         || true
			'common_assetName'    | 'FOOBAR'         || true
			'appTech'             | 'Apple'          || true
			'application_appTech' | 'Apple'          || true
			'appTech'             | ''               || true
			'appTech'             | null             || false
			null                  | 'Apple'          || false
			'_filter'             | 'physicalServer' || true
			'_event'              | '364'            || true
			'_ufp'                | 'true'           || true
			'_ufp'                | 'false'          || true
			'_ufp'                | ''               || true
			'validation'          | 'Unknown'        || true
			'common_validation'   | 'Unknown'        || true
			'sme'                 | '364'            || true
			'plannedStatus'       | 'Moved'          || true
	}

}
