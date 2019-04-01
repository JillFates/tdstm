import com.tdsops.tm.enums.domain.ApiCatalogDictionaryKey
import net.transitionmanager.action.ApiCatalog
import net.transitionmanager.common.DatabaseMigrationService
import org.grails.web.json.JSONObject

/**
 * This changelog convert any black tags from testing to grey tags.
 */
databaseChangeLog = {
	changeSet(author: 'slopez', id: '20180829 TM-11893-1') {
		comment('Rename agent_method column.')

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'api_action', columnName: 'agent_method')
		}

		renameColumn(tableName: 'api_action', oldColumnName: 'agent_method', newColumnName: 'connector_method', columnDataType: 'VARCHAR(64)')
	}

	changeSet(author: 'slopez', id: '20180829 TM-11893-2') {
		comment('Rename the internal property name of Action Dictionary for the agent.')
		grailsChange {
			change {
				// Fetch all the existing dictionaries
				List<ApiCatalog> apiCatalogList = ApiCatalog.list()

				// variables
				String oldKey = 'agent'
				String newKey = 'connector'
				String httpConnector = 'HttpConnector'

				// Script that remove old agent key and put new connector key with appropriate HttpConnector value in
				// dictionary field
				Closure updateDictionaryScript = { JSONObject dictionary ->
					Map<String, ?> info = dictionary.get(ApiCatalogDictionaryKey.DICTIONARY).get(ApiCatalogDictionaryKey.INFO)
					if (info.containsKey(oldKey)) {
						info.remove(oldKey)
						info.put(newKey, httpConnector)
					}

					return dictionary
				}

				DatabaseMigrationService databaseMigrationService = ctx.getBean('databaseMigrationService')
				databaseMigrationService.updateJsonObjects(apiCatalogList, 'dictionary', updateDictionaryScript)
				databaseMigrationService.updateJsonObjects(apiCatalogList, 'dictionaryTransformed', updateDictionaryScript)
			}
		}
	}

}
