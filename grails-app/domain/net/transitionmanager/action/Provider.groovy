package net.transitionmanager.action

/*
 * The ApiAction domain represents the individual mapped API methods that can be
 * invoked by the TransitionManager application in Tasks and other places.
 */

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.imports.DataScript
import net.transitionmanager.project.Project

class Provider {
    String name
    String description
    String comment

    Date dateCreated
    Date lastUpdated

    // used by GormUtil.findDomainByAlternateKey(...)
    static String alternateKey = 'name'

    static belongsTo = [ project: Project ]

    static hasMany = [
        apiActions: ApiAction,
        credentials: Credential,
        dataScripts: DataScript
    ]

    static constraints = {
        name blank: false, size:1..255, unique: 'project'
        description size:0..255, nullable: true
        comment size:0..65254, nullable: true
        lastUpdated nullable: true
    }

    static mapping = {
        id column: 'provider_id'
        name sqlType: 'VARCHAR(255)'
        description sqlType: 'VARCHAR(255)'
        comment sqlType: 'TEXT'

        sort 'name'

    }

    def beforeInsert = {
        dateCreated = TimeUtil.nowGMT()
    }
    def beforeUpdate = {
        lastUpdated = TimeUtil.nowGMT()
    }

    /**
     * Return a map representation of this Provider.
     * @param minimalInfo: if set to true only the id and name will be returned.
     * @return
     */
    Map toMap(boolean minimalInfo = false) {
        Map dataMap = [
            id: id,
            name: name
        ]
        if (! minimalInfo) {
            Map additionalFields = [
                description: description,
                comment: comment,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated
            ]
            dataMap.putAll(additionalFields)
        }
        return dataMap
    }
}
