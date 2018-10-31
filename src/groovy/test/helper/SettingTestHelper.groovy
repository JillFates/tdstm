package test.helper

import com.tdssrc.grails.JsonUtil
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Setting
import org.codehaus.groovy.grails.web.json.JSONObject

class SettingTestHelper {

    void createCustomApplicationFieldSetting(Project project, JSONObject field){
        Setting setting = Setting.findWhere([project: project, key: "APPLICATION"])
        Map jsonMap = JsonUtil.convertJsonToMap(setting.json)
        def existing = jsonMap.fields.find { it.label == field.label }
        if (!existing) {
            List<String> customFieldIds = jsonMap.fields.findAll { it.field.contains("custom") }.field
            List<Integer> ids = []
            customFieldIds.each { id ->
                ids.add(id.substring("custom".length()).toInteger())
            }
            Map customField = JsonUtil.convertJsonToMap(field)
            customField.put("field", "custom${ids.max()+1}")
            jsonMap.fields.add(customField)
            setting.json = JsonUtil.convertMapToJsonString(jsonMap)
            setting.save(flush: true)
        }
    }
}