import com.tdssrc.grails.JsonUtil
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Setting
import org.grails.web.json.JSONObject

class CustomDomainTestHelper {

	/**
	 * Helper method that will find a setting given its project and key (domain) and
	 * will execute the closure that will update field spec accordingly.
	 * @param project - current project
	 * @param domain - domain for filtering the field specs.
	 * @param updateFieldSpecClosure - code that will do the actual update.
	 */
	void updateFieldSpec(Project project, String domain, Closure updateFieldSpecClosure) {
		// Find the setting.
		Setting setting = Setting.where {
			project == project
			key == domain
		}.list()[0]

		// Update the setting.
		updateFieldSpec(setting, updateFieldSpecClosure)
	}

	/**
	 * Update the field spec by executing the given closure.
	 *
	 * @param setting - the setting to be updated.
	 * @param updateFieldSpecClosure - the closure that will update the setting.
	 */
	void updateFieldSpec(Setting setting, Closure updateFieldSpecClosure) {
		JSONObject fieldSpecJson = JsonUtil.parseJson(setting.json)
		updateFieldSpecClosure(fieldSpecJson)
		setting.json = JsonUtil.validateJsonAndConvertToString(fieldSpecJson)
		setting.save(flush: true)
	}

}
