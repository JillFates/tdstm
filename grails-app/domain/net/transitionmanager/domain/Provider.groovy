package net.transitionmanager.domain

import com.tdssrc.grails.TimeUtil

class Provider {
    String name
    String description
    String comment

    Date dateCreated
    Date lastUpdated

    static belongsTo = [ project: Project ]

    static hasMany = [
        dataScripts: DataScript,
        credentials: Credential
    ]

    static constraints = {
        name size:1..255, unique: 'project'
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
     * @return
     */
    Map toMap() {
        Map dataMap = [
                id: id,
                name: name,
                description: description,
                comment: comment,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated
        ]
        return dataMap
    }
}
