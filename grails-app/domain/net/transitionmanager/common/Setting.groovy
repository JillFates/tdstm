package net.transitionmanager.common

import com.tdsops.tm.enums.domain.SettingType
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.project.Project

class Setting {
	Project     project
	SettingType type
	String      key = ""
	String      json
	Date        dateCreated
	Date        lastModified

    static constraints = {
        project nullable: true
        type nullable: false
        key nullable: false
        json nullable: false
        lastModified nullable: true
    }

    static mapping = {
        id column: "setting_id"
        json sqltype: 'text'
        type sqltype: 'varchar(32)', unique: ['key', 'project']
        key column: "setting_key"
        project column: "project_id"
    }

    def beforeInsert = {
        dateCreated = dateCreated = TimeUtil.nowGMT()
    }
    def beforeUpdate = {
        lastModified = TimeUtil.nowGMT()
    }
}
