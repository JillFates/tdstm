package net.transitionmanager.domain

class Provider {
    String name
    String description
    String comment

    Date dateCreated
    Date lastUpdated

    static belongsTo = [ project: Project ]

    static constraints = {
        name size:1..255, unique: 'project'
        description size:0..255
        comment size:0..65254
        lastUpdated nullable: true
    }

    static mapping = {
        name sqlType: 'VARCHAR(255)'
        description sqlType: 'VARCHAR(255)'
        comment sqlType: 'TEXT'

        sort 'name'
    }
}