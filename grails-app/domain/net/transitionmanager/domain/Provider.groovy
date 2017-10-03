package net.transitionmanager.domain

class Provider {
    String name

    Date dateCreated
    Date lastUpdated

    static belongsTo = [ project: Project ]

    static constraints = {
        name size:1..255, unique: 'project'
        lastUpdated nullable: true
    }

    static mapping = {
        name sqlType: 'VARCHAR(255)'

        sort 'name'
    }
}