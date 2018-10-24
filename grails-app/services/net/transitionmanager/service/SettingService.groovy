package net.transitionmanager.service

import com.tdsops.tm.enums.domain.AssetClass
import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.JsonUtil
import grails.transaction.Transactional
import groovy.transform.CompileStatic
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Setting
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder as LCH

@CompileStatic
class SettingService implements ServiceMethods {
    public static final String VERSION_KEY = "version"
    public static final String PLAN_METHODOLOGY_KEY = "planMethodology"

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
            setting.save()
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
        removeVersionKeyIfPresent(parsedJson)
        return JsonUtil.validateJsonAndConvertToString(parsedJson)
    }

    /**
     * Remove VERSION_KEY from given JSONObject
     * @param jsonObject
     */
    private void removeVersionKeyIfPresent(JSONObject jsonObject) {
        if (jsonObject) {
            jsonObject.remove(VERSION_KEY)
        }
    }
}
