package net.transitionmanager.common

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.JsonUtil
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import net.transitionmanager.exception.DomainUpdateException
import net.transitionmanager.project.Project
import net.transitionmanager.common.Setting
import net.transitionmanager.service.ServiceMethods
import org.apache.commons.lang3.StringUtils
import org.grails.web.json.JSONObject
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder as LCH

@CompileStatic
@Transactional
class SettingService implements ServiceMethods {
    public static final String VERSION_KEY = 'version'
    public static final String FIELDS_KEY = 'fields'
    public static final String LABEL_KEY = 'label'
    public static final String PLAN_METHODOLOGY_KEY = 'planMethodology'

    MessageSource messageSource

    /**
     * Get project setting from database and return it as JSON
     * @param project
     * @param type
     * @param key
     * @return
     */
    String getAsJson(Project project, SettingType type, String key) {
        Setting setting = getSetting(project, type, key)
        return setting?.json
    }

    /**
     * Get project setting from database and return it as Map
     * @param project
     * @param type
     * @param key
     * @return
     */
    Map getAsMap(Project project, SettingType type, String key) {
        Setting setting = getSetting(project, type, key)
        if (setting) {
            return getSettingAsMap(setting)
        }
        return null
    }

    /**
     * Get global setting from database and return it as JSON
     * @param type
     * @param key
     * @return
     */
    String getAsJson(SettingType type, String key) {
        Setting setting = getSetting(null, type, key)
        return setting?.json
    }

    /**
     * Get global setting from database and return it as Map
     * @param type
     * @param key
     * @return
     */
    Map getAsMap(SettingType type, String key) {
        Setting setting = getSetting(null, type, key)
        if (setting) {
            return getSettingAsMap(setting)
        }
        return null
    }

    /**
     * Service method to get settings from database
     * @param project
     * @param type
     * @param key
     * @return
     */
    protected Setting getSetting(Project project, SettingType type, String key) {
        if (project) {
            return Setting.findWhere([project: project, type: type, key: key])
        } else {
            return Setting.findWhere([type: type, key: key])
        }
    }

    /**
     * Given a setting entity, constructs a Map with the JSON property
     * and adds the entity version as an entry in the resultant map
     * @param setting
     * @return
     */
    protected Map getSettingAsMap(Setting setting) {
        if (setting) {
            try {
                Map<String, ?> settingMap = JsonUtil.convertJsonToMap(setting.json)
                settingMap.put(VERSION_KEY, setting.version)
                if (setting.key == AssetClass.APPLICATION.name()) {
                    settingMap.put(PLAN_METHODOLOGY_KEY, setting.project.planMethodology)
                }
                return settingMap
            } catch (Exception e) {
                log.error(e.message, e)
                return null
            }
        } else {
            return null
        }
    }

    /**
     * Save a project setting in the database
     * @param project
     * @param type
     * @param key
     * @param json
     * @param version
     */
    void save(Project project, SettingType type, String key, String json, Integer version=0) {
        Setting setting = getSetting(project, type, key)
        save(setting, project, type, key, json, version)
    }

    /**
     * Sava a global setting in the database
     * @param type
     * @param key
     * @param json
     * @param version
     */
    void save(SettingType type, String key, String json, Integer version=0) {
        Setting setting = getSetting(null, type, key)
        save(setting, null, type, key, json, version)
    }

    /**
     * Service method to save settings in the database
     * @param setting
     * @param project
     * @param type
     * @param key
     * @param json
     * @param version
     */
    @Transactional
    protected void save(Setting setting, Project project, SettingType type, String key, String json, Integer version) {
        String jsonValidatedAndOptimized = validateAndOptimizeJSON(json)

        if (setting) {
            // verify user is updating the right version of the setting
            if (setting.version != version) {
                throw new DomainUpdateException(messageSource.getMessage("exception.DomainUpdateException.ChangeConflict", null, LCH.getLocale()))
            }
        } else {
            setting = new Setting()
            setting.project = project
            setting.type = type
            setting.key = key.toUpperCase()
        }

        try {
            setting.json = jsonValidatedAndOptimized
            setting.save(flush:true)
        } catch (Exception e) {
            log.error(e.message, e)
            throw new DomainUpdateException("Unable to save Setting")
        }
    }

    /**
     * Validate and optimize given JSON
     * @param json
     * @return
     */
    private String validateAndOptimizeJSON(String json) {
        JSONObject parsedJson = JsonUtil.parseJson(json)
        if (parsedJson) {
            removeVersionKeyIfPresent(parsedJson)
            sanitizeFieldLabels(parsedJson)
        }
        return JsonUtil.validateJsonAndConvertToString(parsedJson)
    }

    /**
     * Remove VERSION_KEY from given JSONObject
     * @param jsonObject
     */
    private void removeVersionKeyIfPresent(JSONObject jsonObject) {
        if (jsonObject.containsKey(VERSION_KEY)) {
            jsonObject.remove(VERSION_KEY)
        }
    }

    /**
     * Remove spaces/blanks from both ends of the field label
     * @param jsonObject
     */
    private void sanitizeFieldLabels(JSONObject jsonObject) {
        if (jsonObject.containsKey(FIELDS_KEY)) {
            jsonObject.get(FIELDS_KEY).each { Map<String, ?> field ->
                field.put(LABEL_KEY, StringUtils.trim(field[LABEL_KEY] as String))
            }
        }
    }
}
