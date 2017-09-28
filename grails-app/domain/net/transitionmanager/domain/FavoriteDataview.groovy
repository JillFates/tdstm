package net.transitionmanager.domain

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes = ['person', 'dataview'])
class FavoriteDataview implements Serializable {

    Dataview dataview

    static constraints = {
        dataview blank: false
    }

    static belongsTo = [person: Person]

    static mapping = {
        version false
        id composite: ['person', 'dataview'], generator: 'assigned'
    }
}