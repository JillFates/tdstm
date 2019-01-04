import com.tdsops.tm.enums.domain.UserPreferenceEnum
import net.transitionmanager.domain.UserPreference
import net.transitionmanager.service.DatabaseMigrationService
import org.grails.web.json.JSONObject

databaseChangeLog = {
	changeSet(author: 'arecordon', id: 'TM-10482-1') {
		comment('Fix the User Preference value for users who had location/rack/room source/target before TM-9770')
		grailsChange {
			change {

				List<UserPreference> userPreferences = UserPreference.createCriteria().list{
					eq('preferenceCode', UserPreferenceEnum.Asset_Columns.name())
				}

				// Map where the keys are the incorrect field names and the values are the corrected names.
				Map<String, String> renameMap = [
					sourceLocation:	'locationSource',
					sourceRack: 'rackSource',
					sourceRoom: 'roomSource',
					targetLocation:	'locationTarget',
					targetRack: 'rackTarget',
					targetRoom: 'roomTarget'
				]

				// Iterate over the preference map replacing the value where needed.
				Closure updatePreferenceScript = { JSONObject preferenceValueJson ->
					for (preference in preferenceValueJson.keySet()) {
						String preferenceValue = preferenceValueJson[preference]
						if (renameMap.containsKey(preferenceValue)) {
							preferenceValueJson[preference] = renameMap[preferenceValue]
						}
					}
					return preferenceValueJson
				}

				DatabaseMigrationService databaseMigrationService = ctx.getBean("databaseMigrationService")
				// Update the preferences' values

				databaseMigrationService.updateJsonObjects(userPreferences, "value", updatePreferenceScript)
			}
		}
	}
}

