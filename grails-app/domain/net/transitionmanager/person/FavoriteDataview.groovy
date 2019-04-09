package net.transitionmanager.person

import groovy.transform.EqualsAndHashCode
import net.transitionmanager.imports.Dataview

@EqualsAndHashCode(includes = ['person', 'dataview'])
class FavoriteDataview implements Serializable {

	Dataview dataview

    static belongsTo = [person: Person]

    static mapping = {
        version false
        id composite: ['person', 'dataview'], generator: 'assigned'
    }
}
