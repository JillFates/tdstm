package net.transitionmanager.asset

import com.tds.asset.AssetOptions
import grails.test.spock.IntegrationSpec
import spock.lang.Ignore
import spock.lang.Shared

class AssetUtilsIntegrationSpec extends IntegrationSpec {

	void '1 Test getEnvironmentOptions'() {
		given:
			List<String> optionsList = AssetUtils.getAssetOptionsValues(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
		expect:
			optionsList.size() > 0
			optionsList.contains('Production')
	}

	void '2 Test matchAssetOptionCaseInsensitive'() {
		given:
			List<String> optionsList = AssetUtils.getAssetOptionsValues(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)

		expect:
			expected == AssetUtils.matchAssetOptionCaseInsensitive(value, optionsList, defaultValue)

		where:
			value		| defaultValue	| expected
			'Validated'	| null			| 'Validated'
			'vAlIdAtEd' | null			| 'Validated'
			'bogus'		| null			| null
			'bogus2'	| 'xyzzy'		| 'xyzzy'
	}


}